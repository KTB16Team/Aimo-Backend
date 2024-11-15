package aimo.backend.domains.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import aimo.backend.common.util.memberLoader.MemberLoader;
import aimo.backend.domains.member.dto.parameter.DeleteProfileImageParameter;
import aimo.backend.domains.member.dto.parameter.SignUpParameter;
import aimo.backend.domains.member.dto.parameter.UpdateNicknameParameter;
import aimo.backend.domains.member.dto.parameter.DeleteMemberParameter;
import aimo.backend.domains.member.dto.parameter.FindMyInfoParameter;
import aimo.backend.domains.member.dto.parameter.SaveFileMetaDataParameter;
import aimo.backend.domains.member.dto.parameter.UpdatePasswordParameter;
import aimo.backend.domains.member.dto.request.CheckNicknameExistsRequest;
import aimo.backend.domains.member.dto.request.DeleteMemberRequest;
import aimo.backend.domains.member.dto.request.LogoutRequest;
import aimo.backend.domains.member.dto.response.FindMyInfoResponse;
import aimo.backend.domains.member.dto.response.NicknameExistsResponse;
import aimo.backend.domains.member.dto.request.SendTemporaryPasswordRequest;
import aimo.backend.common.dto.DataResponse;
import aimo.backend.domains.auth.security.jwtFilter.JwtTokenProvider;
import aimo.backend.domains.member.dto.request.UpdateNicknameRequest;
import aimo.backend.domains.member.dto.request.UpdatePasswordRequest;
import aimo.backend.infrastructure.s3.S3Service;
import aimo.backend.infrastructure.s3.dto.request.CreatePresignedUrlRequest;
import aimo.backend.infrastructure.s3.dto.response.CreatePresignedUrlResponse;
import aimo.backend.infrastructure.s3.dto.request.SaveFileMetaDataRequest;

import aimo.backend.domains.member.service.MemberService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;
	private final S3Service s3Service;

	@PostMapping("/logout")
	public ResponseEntity<DataResponse<Void>> logoutMember(HttpServletRequest request) {
		String accessToken = jwtTokenProvider.extractAccessToken(request)
			.orElse(null);

		memberService.logoutMember(new LogoutRequest(accessToken));

		return ResponseEntity.status(HttpStatus.CREATED).body(DataResponse.created());
	}

	@PostMapping("/signup")
	public ResponseEntity<DataResponse<Void>> signupMember(@RequestBody @Valid SignUpParameter parameter) {
		memberService.signUp(parameter);

		return ResponseEntity.status(HttpStatus.CREATED).body(DataResponse.created());
	}

	@PostMapping
	public ResponseEntity<DataResponse<Void>> deleteMember(@RequestBody @Valid DeleteMemberRequest request) {
		Long memberId = MemberLoader.getMemberId();

		DeleteMemberParameter parameter = DeleteMemberParameter.of(memberId, request.password());

		memberService.deleteMember(parameter);

		return ResponseEntity.status(HttpStatus.CREATED).body(DataResponse.created());
	}

	@GetMapping("/profile/presigned/{filename}")
	public ResponseEntity<DataResponse<CreatePresignedUrlResponse>> createProfileImagePreSignedUrl(
		@PathVariable("filename") String filename
	) {
		CreatePresignedUrlRequest createPresignedUrlRequest = CreatePresignedUrlRequest.of(filename);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(DataResponse.created(s3Service.createProfilePresignedUrl(createPresignedUrlRequest)));
	}

	@PostMapping("/profile/success")
	public ResponseEntity<DataResponse<Void>> saveProfileImageMetaData(
		@RequestBody @Valid SaveFileMetaDataRequest request
	) {
		Long memberId = MemberLoader.getMemberId();

		SaveFileMetaDataParameter parameter = SaveFileMetaDataParameter.of(memberId, request);

		memberService.saveProfileImageMetaData(parameter);

		return ResponseEntity.status(HttpStatus.CREATED).body(DataResponse.created());
	}

	@DeleteMapping("/profile")
	public ResponseEntity<DataResponse<Void>> deleteProfileImage() {
		Long memberId = MemberLoader.getMemberId();

		DeleteProfileImageParameter parameter = DeleteProfileImageParameter.from(memberId);

		memberService.deleteProfileImage(parameter);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(DataResponse.noContent());
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<DataResponse<Void>> loginMember(
		@RequestParam("email") String email,
		@RequestParam("password") String password
	) {
		// 실제로 실행되지 않고 Swagger 문서용도로만 사용됩니다.
		return ResponseEntity.ok(DataResponse.ok());
	}

	// 내 정보 조회 (프로필 이미지 포함)
	@GetMapping
	public ResponseEntity<DataResponse<FindMyInfoResponse>> findMyInfo() {
		Long memberId = MemberLoader.getMemberId();

		FindMyInfoResponse response = memberService.findMyInfo(FindMyInfoParameter.from(memberId));

		return ResponseEntity.ok(DataResponse.from(response));
	}

	// 비밀번호 수정(현재 비밀번호 확인)
	@PutMapping("/password")
	public ResponseEntity<DataResponse<Void>> updatePassword(
		@Valid @RequestBody UpdatePasswordRequest updatePasswordRequest
	) {
		Long memberId = MemberLoader.getMemberId();

		UpdatePasswordParameter parameter = UpdatePasswordParameter.of(memberId, updatePasswordRequest);

		memberService.updatePassword(parameter);

		return ResponseEntity.status(HttpStatus.CREATED).body(DataResponse.created());
	}

	// 비밀번호 재발급(임시 비밀번호를 이메일로 전송)
	@PostMapping("/password/temp")
	public ResponseEntity<DataResponse<Void>> sendTemporaryPassword(
		@Valid @RequestBody SendTemporaryPasswordRequest sendTemporaryPasswordRequest
	) throws MessagingException {
		memberService.updateTemporaryPasswordAndSendMail(sendTemporaryPasswordRequest);

		return ResponseEntity.status(HttpStatus.CREATED).body(DataResponse.created());
	}

	@GetMapping("/nickname/{nickname}/exists")
	public ResponseEntity<DataResponse<NicknameExistsResponse>> checkNicknameExists(
		@PathVariable("nickname") String nickname
	) {
		CheckNicknameExistsRequest request = CheckNicknameExistsRequest.from(nickname);

		NicknameExistsResponse exist = NicknameExistsResponse.from(memberService.isNicknameExists(request));

		return ResponseEntity.status(HttpStatus.OK).body(DataResponse.from(exist));
	}

	@PutMapping("/nickname")
	public ResponseEntity<DataResponse<Void>> updateNickname(
		@RequestBody @Valid UpdateNicknameRequest updateNicknameRequest
	) {
		Long memberId = MemberLoader.getMemberId();

		UpdateNicknameParameter parameter = UpdateNicknameParameter.of(memberId, updateNicknameRequest);
		memberService.updateNickname(parameter);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(DataResponse.created());
	}
}
