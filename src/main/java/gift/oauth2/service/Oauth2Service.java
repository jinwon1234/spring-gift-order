package gift.oauth2.service;

import gift.domain.Member;
import gift.jwt.JWTUtil;
import gift.member.service.MemberService;
import gift.oauth2.dto.KakaoTokenRequest;
import gift.oauth2.dto.KakaoTokenResponse;
import gift.oauth2.dto.KakaoUserInfoResponse;
import gift.oauth2.dto.SocialLoginRequest;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import static gift.oauth2.dto.KakaoUserInfoResponse.*;

@Service
public class Oauth2Service {

    private final RestClient restClient;
    private final MemberService memberService;
    private final JWTUtil jwtUtil;

    public Oauth2Service(MemberService memberService, JWTUtil jwtUtil) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.restClient = RestClient.builder().build();
    }

    public Cookie socialLogin(KakaoTokenRequest kakaoTokenRequest) {
        KakaoTokenResponse token = getToken(kakaoTokenRequest);
        String accessToken = createAccessToken(token);
        return createCookie("Authorization", accessToken);
    }

    private KakaoTokenResponse getToken(KakaoTokenRequest request) {
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

        if (kakaoTokenResponse.getBody() == null)
            throw new IllegalStateException("토큰 응답이 비어있습니다. [카카오 oauth2]");
        return kakaoTokenResponse.getBody();

    }

    private String createAccessToken(KakaoTokenResponse response) {

        ResponseEntity<KakaoUserInfoResponse> userInfoResponse = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.access_token())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString())
                .retrieve()
                .toEntity(KakaoUserInfoResponse.class);

        KakaoAccount kakaoAccount = userInfoResponse.getBody().kakao_account();

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
