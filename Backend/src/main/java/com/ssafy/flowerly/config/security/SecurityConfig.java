package com.ssafy.flowerly.config.security;

import com.ssafy.flowerly.JWT.JWTAuthenticationProcessingFilter;
import com.ssafy.flowerly.JWT.JWTService;
import com.ssafy.flowerly.config.auth.CustomOAuth2MemberService;
import com.ssafy.flowerly.config.auth.handler.OAuth2LoginFailureHandler;
import com.ssafy.flowerly.config.auth.handler.OAuth2LoginSuccessHandler;
import com.ssafy.flowerly.member.model.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JWTService jwtService;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2MemberService customOAuth2MemberService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //Cors 설정
        http.cors().configurationSource(corsConfigurationSource());

        //Spring Security 설정
        http.httpBasic().disable()                  // httpBasic 사용X
                .formLogin().disable()              // FormLogin 사용X
                .csrf().disable()                   // CSRF 보안 사용X
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //Session이 아니니 STATELESS
                .and()
                //===========URL 별 권한 옵션 =============
                .authorizeRequests()
                //=============채팅 요청에 대한 검증=================
                //==========================================
                .antMatchers("/**").permitAll()
                .antMatchers("/api/member").authenticated()
                .antMatchers("/api/member/dummy/seller").permitAll()
                .antMatchers("/api/member/dummy/buyer").permitAll()
                .antMatchers("/api/member/signup/**").authenticated()
                .antMatchers("/api/member/need-login").permitAll();

        //Oauth 설정
        http.oauth2Login()
                .loginPage("https://flower-ly.co.kr/api/member/need-login")    //로그인에 실패할 경우 이동시킬 url이다
                //.loginPage("http://localhost:6090/api/member/need-login")
                .successHandler(oAuth2LoginSuccessHandler)  //성공할 경우 수행할 핸들러
                .failureHandler(oAuth2LoginFailureHandler)  //실패할 경우 수행할 핸들러
                .userInfoEndpoint()                         //Oauth2 정보를 가져오는데 사용할 서비스
                .userService(customOAuth2MemberService);

        return http.build();
    }


    //CORS 허용 적용ㅎ
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.addAllowedOriginPattern("*"); //모든 도메인에 대해 Request 허용
        corsConfiguration.addAllowedHeader("Access-Control-Expose-Headers");        //모든 헤더 허용
        corsConfiguration.addAllowedHeader("*");        //모든 헤더 허용
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000/", "http://localhost:6090/", "http://localhost:5173/", "https://flower-ly.co.kr/"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));        //모든 메소드 허용
        corsConfiguration.setAllowCredentials(true);    //모든 요청, 응답에서 인증정보 처리

        //인스턴스를 생성해 모든 경로에 대해 corsConfiguration을 적용하도록 한다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }


    @Bean
    public JWTAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JWTAuthenticationProcessingFilter(jwtService, memberRepository, redisTemplate);
    }
}
