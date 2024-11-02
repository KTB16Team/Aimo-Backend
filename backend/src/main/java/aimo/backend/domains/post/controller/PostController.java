package aimo.backend.domains.post.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import aimo.backend.common.dto.DataResponse;
import aimo.backend.domains.post.dto.SavePostRequest;
import aimo.backend.domains.post.dto.response.FindPostsByPostTypeResponse;
import aimo.backend.domains.post.model.PostType;
import aimo.backend.domains.post.service.PostService;
import aimo.backend.util.memberLoader.MemberLoader;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;
	private final MemberLoader memberLoader;

	@PostMapping
	public ResponseEntity<DataResponse<Void>> savePost(@RequestBody @Valid SavePostRequest request) {
		postService.save(request, memberLoader.getMember());

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(DataResponse.created());
	}

	@GetMapping
	public ResponseEntity<DataResponse<Page<FindPostsByPostTypeResponse>>> findPostsByPostType(
		@RequestParam("type") @NotNull PostType postType,
		@RequestParam("page") @NotNull Integer page,
		@RequestParam("size") @NotNull Integer size
	) {
		Page<FindPostsByPostTypeResponse> responses = postService.findPostDtosByPostType(memberLoader.getMember(),
			postType, page, size);

		return ResponseEntity.ok(DataResponse.from(responses));
	}
}
