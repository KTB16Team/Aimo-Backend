package aimo.backend.domains.privatePost.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import aimo.backend.common.dto.DataResponse;

import aimo.backend.common.util.memberLoader.MemberLoader;
import aimo.backend.domains.privatePost.dto.parameter.DeletePrivatePostParameter;
import aimo.backend.domains.privatePost.dto.parameter.FindPrivatePostParameter;
import aimo.backend.domains.privatePost.dto.parameter.FindPrivatePostPreviewParameter;
import aimo.backend.domains.privatePost.dto.parameter.SpeechToTextParameter;
import aimo.backend.domains.privatePost.dto.parameter.JudgementToAiParameter;
import aimo.backend.domains.privatePost.dto.response.PrivatePostPreviewResponse;
import aimo.backend.domains.privatePost.dto.response.PrivatePostResponse;
import aimo.backend.domains.privatePost.dto.request.SaveAudioSuccessRequest;
import aimo.backend.domains.privatePost.dto.response.SaveAudioSuccessResponse;
import aimo.backend.domains.privatePost.dto.request.SpeechToTextRequest;
import aimo.backend.domains.privatePost.dto.response.SavePrivatePostResponse;
import aimo.backend.domains.privatePost.dto.response.SpeechToTextResponse;

import aimo.backend.domains.privatePost.dto.request.TextRecordRequest;
import aimo.backend.domains.privatePost.model.OriginType;
import aimo.backend.domains.privatePost.service.AudioRecordService;
import aimo.backend.domains.privatePost.service.PrivatePostService;

import aimo.backend.domains.privatePost.service.SaveAudioSuccessParameter;
import aimo.backend.infrastructure.s3.S3Service;
import aimo.backend.infrastructure.s3.dto.request.CreatePresignedUrlRequest;
import aimo.backend.infrastructure.s3.dto.response.CreatePresignedUrlResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/private-posts")
@RequiredArgsConstructor
public class PrivatePostController {

	private final AudioRecordService audioRecordService;
	private final PrivatePostService privatePostService;
	private final S3Service s3Service;

	// 대화록 업로드 + 판결
	@PostMapping("/judgement/text")
	public ResponseEntity<DataResponse<SavePrivatePostResponse>> uploadTextRecordAndJudgement(
		@Valid @RequestBody TextRecordRequest textRecordRequest
	) {
		Long memberId = MemberLoader.getMemberId();

		JudgementToAiParameter judgementToAiParameter = JudgementToAiParameter.of(
			memberId,
			textRecordRequest.content(),
			OriginType.TEXT);

		Long privatePostId = privatePostService.serveTextRecordToAi(judgementToAiParameter);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(DataResponse.created(SavePrivatePostResponse.of(privatePostId)));
	}

	// @PostMapping("/chat")
	// public ResponseEntity<DataResponse<SavePrivatePostResponse>> uploadChatRecord(
	// 	@Valid @RequestParam("chat_record") ChatRecordRequest chatRecordRequest
	// ) throws IOException {
	//
	//
	// 	return ResponseEntity.status(HttpStatus.CREATED)
	// 		.body(DataResponse.created(new SavePrivatePostResponse(privatePostId)));
	// }

	@PostMapping("/speech-to-text")
	public ResponseEntity<DataResponse<SpeechToTextResponse>> speechToText(
		@Valid @RequestBody SpeechToTextRequest speechToTextRequest
	) {
		SpeechToTextParameter parameter = SpeechToTextParameter.from(speechToTextRequest);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(DataResponse.created(audioRecordService.speechToText(parameter)));
	}

	@GetMapping("/audio/presigned/{filename}")
	public ResponseEntity<DataResponse<CreatePresignedUrlResponse>> getPresignedUrlTo(
		@Valid @PathVariable("filename") String filename
	) {
		CreatePresignedUrlRequest createPresignedUrlRequest = CreatePresignedUrlRequest.of(filename);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(DataResponse.created(s3Service.createAudioPreSignedUrl(createPresignedUrlRequest)));
	}

	@PostMapping("/audio/success")
	public ResponseEntity<DataResponse<SaveAudioSuccessResponse>> saveAudioRecord(
		@Valid @RequestBody SaveAudioSuccessRequest saveAudioSuccessRequest
	) {
		SaveAudioSuccessParameter parameter = SaveAudioSuccessParameter.from(saveAudioSuccessRequest);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(DataResponse.created(audioRecordService.save(parameter)));
	}

	// 개인글 조회
	@GetMapping("/{privatePostId}")
	public ResponseEntity<DataResponse<PrivatePostResponse>> findPrivatePost(
		@Valid @PathVariable Long privatePostId
	) {
		Long memberId = MemberLoader.getMemberId();

		FindPrivatePostParameter parameter = FindPrivatePostParameter.of(memberId, privatePostId);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(DataResponse.from(privatePostService.findPrivatePostResponseBy(parameter)));
	}

	@GetMapping
	public ResponseEntity<DataResponse<Page<PrivatePostPreviewResponse>>> findPrivatePostPage(
		@Valid @RequestParam(defaultValue = "0") Integer page,
		@Valid @RequestParam(defaultValue = "10") Integer size
	) {
		Long memberId = MemberLoader.getMemberId();

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		FindPrivatePostPreviewParameter parameter = FindPrivatePostPreviewParameter.of(memberId, pageable);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(DataResponse.from(privatePostService.findPrivatePostPreviewsBy(parameter)));
	}

	@DeleteMapping("/{privatePostId}")
	public ResponseEntity<DataResponse<Void>> deletePrivatePost(
		@Valid @PathVariable("privatePostId") Long privatePostId
	) {
		Long memberId = MemberLoader.getMemberId();

		DeletePrivatePostParameter parameter = DeletePrivatePostParameter.of(memberId, privatePostId);

		privatePostService.deletePrivatePostBy(parameter);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(DataResponse.noContent());
	}
}
