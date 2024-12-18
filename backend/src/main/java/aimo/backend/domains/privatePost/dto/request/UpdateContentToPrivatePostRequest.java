package aimo.backend.domains.privatePost.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateContentToPrivatePostRequest(
	@NotNull(message = "accessKey가 필요합니다.")
	String accessKey,
	@NotNull(message = "status가 필요합니다.")
	Boolean status,
	@NotNull(message = "id가 필요합니다.")
	Long id,
	String title,
	String stancePlaintiff,
	String stanceDefendant,
	String summaryAi,
	String judgement,
	Float faultRate
) {
}
