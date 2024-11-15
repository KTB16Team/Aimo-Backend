package aimo.backend.domains.like.service;

import static aimo.backend.common.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aimo.backend.common.exception.ApiException;
import aimo.backend.domains.like.entity.PostLike;
import aimo.backend.domains.like.model.LikeType;
import aimo.backend.domains.like.repository.PostLikeRepository;
import aimo.backend.domains.member.entity.Member;
import aimo.backend.domains.member.repository.MemberRepository;
import aimo.backend.domains.like.dto.parameter.LikePostParameter;
import aimo.backend.domains.post.entity.Post;
import aimo.backend.domains.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

	private final PostLikeRepository postLikeRepository;
	private final PostRepository postRepository;
	private final MemberRepository memberRepository;

	@Transactional(rollbackFor = Exception.class)
	public void likePost(LikePostParameter parameter) {
		Long postId = parameter.postId();
		Long memberId = parameter.memberId();
		LikeType likeType = parameter.likeType();

		Post post = postRepository.findById(postId)
			.orElseThrow(() -> ApiException.from(POST_NOT_FOUND));
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> ApiException.from(MEMBER_NOT_FOUND));

		if (likeType == LikeType.LIKE) {
			// 라이크가 존재하면 중복 등록 방지
			if (postLikeRepository.existsByPostIdAndMemberId(postId, memberId))
				return;

			PostLike postLike = PostLike.from(member, post);
			postLikeRepository.save(postLike);
			return ;
		}

		// 라이크가 이미 존재하면 삭제
		postLikeRepository.deleteByMember_IdAndPost_Id(member.getId(), postId);
	}
}
