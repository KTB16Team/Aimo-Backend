package aimo.backend.domains.privatePost.dto.parameter;

import aimo.backend.domains.privatePost.dto.request.UpdateContentToPrivatePostRequest;
import jakarta.validation.constraints.NotNull;

public record UpdateContentToPrivatePostParameter(
	Long memberId,
	String accessKey,
	Boolean status,
	Long id,
	String title,
	String stancePlaintiff,
	String stanceDefendant,
	String summaryAi,
	String judgement,
	Float faultRate
) {
	public static UpdateContentToPrivatePostParameter from(UpdateContentToPrivatePostRequest request, Long memberId) {
		return new UpdateContentToPrivatePostParameter(
			memberId,
			request.accessKey(),
			request.status(),
			request.id(),
			request.title(),
			request.stancePlaintiff(),
			request.stanceDefendant(),
			request.summaryAi(),
			request.judgement(),
			request.faultRate()
		);
	}
}

