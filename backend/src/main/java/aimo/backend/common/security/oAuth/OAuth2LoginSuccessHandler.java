package aimo.backend.common.security.oAuth;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import aimo.backend.common.properties.FrontProperties;
import aimo.backend.common.security.dto.CustomUserDetails;
import aimo.backend.common.security.filter.jwtFilter.JwtTokenProvider;
import aimo.backend.domains.member.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final FrontProperties frontProperties;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException {
		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
		Member member = userDetails.getMember();

		String accessToken = jwtTokenProvider.createAccessToken(member.getId());
		String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

		// RefreshToken 저장
		jwtTokenProvider.saveOrUpdateRefreshToken(member.getId(), refreshToken);

		// React의 Redirect URI로 리다이렉트
		String redirectUrl = String.format(
			"%s/oauth/callback/kakao?accessToken=%s&refreshToken=%s",
			frontProperties.getDomain(),
			accessToken,
			refreshToken
		);

		getRedirectStrategy().sendRedirect(request, response, redirectUrl);
	}
}
