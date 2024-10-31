package aimo.backend.common.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import aimo.backend.domains.member.dto.SignUpRequest;
import aimo.backend.domains.member.entity.Member;
import aimo.backend.domains.member.model.MemberRole;
import aimo.backend.domains.member.model.Provider;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberMapper {
	private final PasswordEncoder passwordEncoder;

	public Member signUpMemberEntity(SignUpRequest signUpRequest){
		return Member
			.builder()
			.username(signUpRequest.username())
			.password(passwordEncoder.encode(signUpRequest.password()))
			.email(signUpRequest.email())
			.memberRole(MemberRole.USER)
			.gender(signUpRequest.gender())
			.provider(Provider.AIMO)
			.birthDate(signUpRequest.birth())
			.build();
	}
}
