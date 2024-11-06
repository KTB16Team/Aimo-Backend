package aimo.backend.domains.comment.dto.request;

import jakarta.validation.constraints.Size;

public record SaveParentCommentRequest(
	@Size(max = 1500, message = "댓글은 한글 기준 최대 500자까지 입력할 수 있습니다.")
	String content
) { }