package aimo.backend.domains.view.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import aimo.backend.common.dto.DataResponse;
import aimo.backend.domains.view.dto.IncreasePostViewRequest;
import aimo.backend.domains.view.service.PostViewService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ViewController {

	private final PostViewService postViewService;

	@PostMapping("/{postId}/views")
	public ResponseEntity<DataResponse<Void>> increasePostView(
		@PathVariable("postId") IncreasePostViewRequest increasePostViewRequest
	) {
		postViewService.increasePostViewBy(increasePostViewRequest);
		return ResponseEntity.ok(DataResponse.created());
	}

}
