package aimo.backend.common.service;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import aimo.backend.common.exception.ApiException;
import aimo.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ExternalApiService {

	private final WebClient webClient;

	public <T, R> Mono<R> post(String url, T body, Class<R> responseType) {
		return webClient.post()
			.uri(url)
			.bodyValue(body)
			.retrieve()
			.onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
				throw ApiException.from(ErrorCode.AI_BAD_GATEWAY);
			})
			.onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
				throw ApiException.from(ErrorCode.AI_SEVER_ERROR);
			})
			.bodyToMono(responseType);
	}
}
