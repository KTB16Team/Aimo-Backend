package aimo.backend.domains.post.service;

import static aimo.backend.common.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aimo.backend.common.exception.ApiException;
import aimo.backend.common.mapper.PostMapper;
import aimo.backend.domains.comment.entity.ParentComment;
import aimo.backend.domains.member.entity.Member;
import aimo.backend.domains.post.dto.requset.FindCommentedPostsByIdRequest;
import aimo.backend.domains.post.dto.requset.SavePostRequest;
import aimo.backend.domains.post.dto.response.FindJudgementResponse;
import aimo.backend.domains.post.dto.response.FindPostAndCommentsByIdResponse;
import aimo.backend.domains.post.dto.response.FindPostsByPostTypeResponse;
import aimo.backend.domains.post.entity.Post;
import aimo.backend.domains.post.model.PostType;
import aimo.backend.domains.post.repository.PostRepository;
import aimo.backend.domains.privatePost.service.PrivatePostService;
import aimo.backend.util.memberLoader.MemberLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostService {

	private final PrivatePostService privatePostService;
	private final PostRepository postRepository;
	private final PostCommentService postCommentService;
	private final MemberLoader memberLoader;

	// 글 저장
	@Transactional
	public Post save(SavePostRequest savePostRequest) {
		Member member = memberLoader.getMember();
		privatePostService.publishPrivatePost(savePostRequest.privatePostId());
		return postRepository.save(PostMapper.toEntity(savePostRequest, member));
	}

	// 글 조회
	public Post findById(Long postId) {
		return postRepository.findById(postId)
			.orElseThrow(() -> ApiException.from(POST_NOT_FOUND));
	}

	public FindJudgementResponse findJudgementBy(Long postId) {
		Post post = findById(postId);
		return PostMapper.toJudgement(post);
	}

	// 글 조회, dto로 응답
	public FindPostAndCommentsByIdResponse findPostAndCommentsDtoById(Long postId) {
		Post post = findById(postId);
		Member member = memberLoader.getMember();
		// 부모 댓글 조회
		List<ParentComment> parentComments = postCommentService.findParentCommentsByPostId(postId);

		// dto로 변환
		return PostMapper.toFindPostAndCommentsByIdResponse(member, post, parentComments);
	}

	// PostType으로 글 조회
	public Page<FindPostsByPostTypeResponse> findPostDtosByPostType(
		PostType postType,
		Integer page,
		Integer size
	) {
		Page<FindPostsByPostTypeResponse> posts;
		Member member = memberLoader.getMember();
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

		if (postType == PostType.MY) {
			posts = findMyPosts(member.getId(), pageable);
		} else if (postType == PostType.POPULAR) {
			posts = findPopularPosts(pageable);
		} else if (postType == PostType.COMMENTED) {
			posts = findCommentedPosts(member.getId(), pageable);
		} else {
			posts = findAnyPosts(pageable);
		}

		posts.forEach(post -> log.info("post: {}", post));

		return posts;
	}

	// 내가 쓴 글 조회
	private Page<FindPostsByPostTypeResponse> findMyPosts(Long memberId, Pageable pageable) {
		return postRepository
			.findAllByMember_Id(memberId, pageable)
			.map(PostMapper::toFindPostsByPostTypeResponse);
	}

	// 인기 글 조회
	private Page<FindPostsByPostTypeResponse> findPopularPosts(Pageable pageable) {
		return postRepository
			.findByViewsCount(pageable)
			.map(PostMapper::toFindPostsByPostTypeResponse);
	}

	// 최신 글 조회
	private Page<FindPostsByPostTypeResponse> findAnyPosts(Pageable pageable) {
		return postRepository
			.findAllByOrderByIdDesc(pageable)
			.map(PostMapper::toFindPostsByPostTypeResponse);
	}

	// 댓글 단 글 조회
	private Page<FindPostsByPostTypeResponse> findCommentedPosts(Long memberId, Pageable pageable) {
		List<ParentComment> parentComments = postCommentService.findParentCommentsByMemberId(memberId);

		List<FindCommentedPostsByIdRequest> commentedPosts = new ArrayList<>();

		parentComments
			.forEach((p) -> {
				FindCommentedPostsByIdRequest commentedPost = PostMapper.toFindCommentedPostsByIdRequest(p);
				int index = commentedPosts.indexOf(commentedPost);
				if (index != -1 && commentedPosts.get(index).commentedAt().isAfter(p.getCreatedAt())) {
					commentedPosts.set(index, commentedPost);
				} else {
					commentedPosts.add(commentedPost);
				}
			});

		commentedPosts.sort((a, b) -> b.commentedAt().compareTo(a.commentedAt()));

		// Pageable에 맞게 부분 리스트 추출
		int start = (int)pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), commentedPosts.size());

		List<FindPostsByPostTypeResponse> pagedPosts = commentedPosts
			.subList(start, end)
			.stream()
			.map(PostMapper::toFindPostsByPostTypeResponse)
			.toList();

		// Page 객체 생성
		return new PageImpl<>(pagedPosts, pageable, commentedPosts.size());
	}

	// 글 삭제
	@Transactional
	public void deletePostBy(Long postId) {
		Long memberId = memberLoader.getMember().getId();
		validateDeletePost(memberId, postId);

		Long privatePostId = postRepository
			.findById(postId)
			.orElseThrow(() -> ApiException.from(POST_NOT_FOUND))
			.getPrivatePostId();

		privatePostService.unpublishPrivatePost(privatePostId);
		postRepository.deleteById(postId);
	}

	// 글 삭제 권한 확인
	public void validateDeletePost(Long memberId, Long postId) {
		Boolean exists = postRepository.existsByIdAndMember_Id(postId, memberId);

		if (!exists) {
			throw ApiException.from(POST_DELETE_UNAUTHORIZED);
		}
	}

	public void softDeleteBy(Long postId) {
		Post post = findById(postId);
		privatePostService.deletePrivatePostBy(post.getPrivatePostId());
		post.softDelete();
	}
}
