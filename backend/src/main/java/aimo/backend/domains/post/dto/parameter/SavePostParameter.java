package aimo.backend.domains.post.dto.parameter;

import aimo.backend.domains.post.model.Category;
import aimo.backend.domains.privatePost.model.OriginType;

public record SavePostParameter(
	Long memberId,
	Long privatePostId,
	String title,
	String stancePlaintiff,
	String stanceDefendant,
	String summaryAi,
	String judgement,
	Integer faultRateDefendant,
	Integer faultRatePlaintiff,
	OriginType originType,
	Category category
) {

	public static SavePostParameter of(
		Long memberId,
		Long privatePostId,
		String title,
		String stancePlaintiff,
		String stanceDefendant,
		String summaryAi,
		String judgement,
		Integer faultRateDefendant,
		Integer faultRatePlaintiff,
		OriginType originType,
		Category category
	) {
		return new SavePostParameter(
			memberId,
			privatePostId,
			title,
			stancePlaintiff,
			stanceDefendant,
			summaryAi,
			judgement,
			faultRateDefendant,
			faultRatePlaintiff,
			originType,
			category);
	}
}