package aimo.backend.domains.privatePost.dto.request;

import java.time.LocalDate;
import aimo.backend.domains.member.model.Gender;
import jakarta.validation.constraints.NotNull;

public record SummaryAndJudgementRequest(
	@NotNull(message = "대화록이 비었습니다.")
	String script,
	@NotNull(message = "유저명이 비었습니다.")
	String nickname,
	@NotNull(message = "성별이 비었습니다.")
	Gender gender,
	@NotNull(message = "생년월일이 비었습니다.")
	LocalDate birthdate
) {
}
