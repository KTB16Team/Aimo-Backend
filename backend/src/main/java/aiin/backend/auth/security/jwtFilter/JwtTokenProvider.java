package aiin.backend.auth.security.jwtFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface JwtTokenProvider {

	Authentication getAuthentication(String accessToken);

	String createAccessToken(Long memberId);

	String createRefreshToken(Long memberId);

	void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken);

	void sendAccessToken(HttpServletResponse response, String accessToken);

	Optional<String> extractAccessToken(HttpServletRequest request);

	Optional<String> extractRefreshToken(HttpServletRequest request);

	Claims parseClaims(String accessToken);

	Optional<Long> extractMemberId(String accessToken);

	void setAccessTokenHeader(HttpServletResponse response, String accessToken);

	void setRefreshTokenHeader(HttpServletResponse response, String refreshToken);

	boolean isTokenValid(String token);

	void expireRefreshToken(Long memberId, String accessToken, String refreshToken);

	void checkRefreshTokenAndReIssueAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken);

	boolean isRefreshTokenValid(String refreshToken);
	
	boolean isLogout(String accessToken);
}