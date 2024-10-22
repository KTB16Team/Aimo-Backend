package aiin.backend.member.service;

import aiin.backend.auth.security.jwtFilter.JwtTokenProvider;
import aiin.backend.common.exception.ApiException;
import aiin.backend.common.exception.ErrorCode;
import aiin.backend.member.dto.DeleteRequest;
import aiin.backend.member.dto.SignUpRequest;
import aiin.backend.member.entity.Member;
import aiin.backend.member.entity.RefreshToken;
import aiin.backend.member.mapper.MemberMapper;
import aiin.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final MemberMapper memberMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 이메일로 멤버 조회
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // 회원 가입
    @Transactional
    public void signUp(SignUpRequest signUpRequest) {
        if(isDuplicateEmail(signUpRequest.email())){
            throw ApiException.from(ErrorCode.EMAIL_DUPLICATE);
        }

        Member member = memberMapper.signUpMemberEntity(signUpRequest);
        memberRepository.save(member);
    }


    //로그아웃
    @Transactional
    public void logoutMember(String accessToken, String refreshToken) {
        // 회원의 refreshToken 만료 처리
        RefreshToken expiredToken = new RefreshToken(accessToken, refreshToken);
        refreshTokenService.save(expiredToken);
        log.info("Logout successful {}", refreshTokenService.existsByAccessToken(accessToken));
    }

    @Transactional
    public void deleteMember(String accessToken, DeleteRequest deleteRequest) {
        Long memberId = jwtTokenProvider
            .extractMemberId(accessToken)
            .orElseThrow(() -> ApiException.from(ErrorCode.INVALID_ACCESS_TOKEN));

        Member member = memberRepository
            .findById(memberId)
            .orElseThrow(() -> ApiException.from(ErrorCode.MEMBER_NOT_FOUND));

        if(!isValid(deleteRequest.password(), member.getPassword())){
            throw ApiException.from(ErrorCode.INVALID_PASSWORD);
        }

        memberRepository.delete(member);
    }

    protected boolean isDuplicateEmail(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    protected boolean isValid(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }
}
