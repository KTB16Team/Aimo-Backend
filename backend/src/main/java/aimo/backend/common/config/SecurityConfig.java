package aimo.backend.common.config;

import aimo.backend.common.properties.SecurityProperties;
import aimo.backend.domains.auth.security.exceptionHandlingFilter.ExceptionHandlingFilter;
import aimo.backend.domains.auth.security.jwtFilter.JwtAuthenticationFilter;
import aimo.backend.domains.auth.security.jwtFilter.JwtTokenProvider;
import aimo.backend.domains.auth.security.loginFilter.LoginFilter;
import aimo.backend.domains.member.service.MemberService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final MemberService memberService;

	private final UserDetailsService userDetailsService;
	private final JwtTokenProvider jwtTokenProvider;

	private final UrlBasedCorsConfigurationSource ConfigurationSource;
	private final SecurityProperties securityProperties;
	private final PasswordEncoder passwordEncoder;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// 비활성확 목록
		http
			.csrf(AbstractHttpConfigurer::disable) // 세션을 사용안하므로 csrf 공격 없으므로 csrf 비활성화
			.httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 방식 비활성화
			.formLogin(AbstractHttpConfigurer::disable) //json을 이용하여 로그인을 하므로 기본 Login 비활성화
			.logout(LogoutConfigurer::disable) // 로그아웃 비활성화
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));  // 토큰을 사용하기 때문에 Session 사용 x

		// cors 설정
		http
			.cors(cors -> cors.configurationSource(ConfigurationSource));

		// url 관리
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers(securityProperties.getPermitUrls()).permitAll() // PERMIT_URLS만 바로 접근 가능
				.anyRequest().authenticated());

		//필터 체인 추가
		http
			.addFilterAfter(loginFilter(), LogoutFilter.class)
			.addFilterBefore(jwtAuthenticationFilter(), LoginFilter.class)
			.addFilterBefore(exceptionHandlingFilter(), JwtAuthenticationFilter.class);

		// X-frame option 해제 (h2 인메모리 DB 사용시 활성화)
		http
			.headers(headers -> headers
				.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
			);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);

		return new ProviderManager(provider);
	}

	@Bean
	public LoginFilter loginFilter() {
		LoginFilter loginFilter = new LoginFilter(jwtTokenProvider, memberService);

		loginFilter.setAuthenticationManager(authenticationManager());

		return loginFilter;
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider, securityProperties, pathMatcher);
	}

	@Bean
	public ExceptionHandlingFilter exceptionHandlingFilter() {
		return new ExceptionHandlingFilter();
	}
}
