package gift.oauth2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.domain.Member;
import gift.global.exception.KakaoApiException;
import gift.jwt.JWTUtil;
import gift.member.service.MemberService;
import gift.oauth2.dto.*;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static gift.oauth2.dto.KakaoUserInfoResponse.*;

@Service
public class KakaoService {

    private final RestClient restClient;
    private final MemberService memberService;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public KakaoService(RestClient.Builder builder, MemberService memberService, JWTUtil jwtUtil, ObjectMapper objectMapper) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;

        this.restClient = builder
                .defaultStatusHandler(httpStatusCode -> {
                    if (httpStatusCode.getStatusCode().is4xxClientError() || httpStatusCode.getStatusCode().is5xxServerError()) {
                        KakaoExceptionResponse response = objectMapper.readValue(httpStatusCode.getBody(), KakaoExceptionResponse.class);
                        throw new KakaoApiException(response.msg(), response.code());
                    }
                    return true;
                })
                .build();
    }

    public Cookie socialLogin(KakaoTokenRequest kakaoTokenRequest) {
        KakaoTokenResponse token = getToken(kakaoTokenRequest)
                .orElseThrow(()-> new IllegalStateException("API 응답이 비어있습니다. [카카오]."));
        KakaoUserInfoResponse userInfo = getUserInfo(token)
                .orElseThrow(()-> new IllegalStateException("API 응답이 비어있습니다. [카카오]"));
        String accessToken = createAccessToken(userInfo);
        return createCookie("Authorization", accessToken);
    }

    public Optional<KakaoTokenResponse> getToken(KakaoTokenRequest request) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", request.key());
        form.add("redirect_uri", request.redirectUri());
        form.add("code", request.code());

        ResponseEntity<KakaoTokenResponse> kakaoTokenResponse = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toEntity(KakaoTokenResponse.class);

        return Optional.ofNullable(kakaoTokenResponse.getBody());

    }

    public Optional<KakaoUserInfoResponse> getUserInfo(KakaoTokenResponse response) {

        ResponseEntity<KakaoUserInfoResponse> userInfoResponse = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.access_token())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString())
                .retrieve()
                .toEntity(KakaoUserInfoResponse.class);

        return Optional.ofNullable(userInfoResponse.getBody());
    }

    private String createAccessToken(KakaoUserInfoResponse userInfoResponse) {
        KakaoAccount kakaoAccount = userInfoResponse.kakao_account();

        Member member = memberService.socialLogin(new SocialLoginRequest(kakaoAccount.email()));

        String jwt = jwtUtil.createJWT(
                member.getEmail(),
                member.getRole().toString(),
                1000 * 60 * 60L
        );

        return jwt;
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
