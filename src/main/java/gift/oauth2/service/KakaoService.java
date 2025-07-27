package gift.oauth2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.domain.KakaoToken;
import gift.domain.Member;
import gift.domain.Social;
import gift.global.exception.KakaoTokenExpiredException;
import gift.global.exception.NotFoundEntityException;
import gift.jwt.JWTUtil;
import gift.member.service.MemberService;
import gift.oauth2.dto.*;
import gift.oauth2.errorhandler.KakaoKAuthResponseHandler;
import gift.oauth2.errorhandler.KakaoKApiResponseHandler;
import gift.oauth2.properties.KakaoProperties;
import gift.oauth2.repository.KakaoTokenRepository;
import gift.order.dto.KakaoOrderMessageTemplate;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import java.util.Optional;

import static gift.oauth2.dto.KakaoUserInfoResponse.*;

@Service
public class KakaoService {

    private final RestClient restClient;
    private final MemberService memberService;
    private final JWTUtil jwtUtil;
    private final KakaoProperties kakaoProperties;
    private final KakaoTokenRepository kakaoTokenRepository;
    private final ObjectMapper objectMapper;

    public KakaoService(RestClient.Builder builder, MemberService memberService, JWTUtil jwtUtil, ObjectMapper objectMapper, KakaoProperties kakaoProperties, KakaoTokenRepository kakaoTokenRepository) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.restClient = builder.build();
        this.kakaoProperties = kakaoProperties;
        this.kakaoTokenRepository = kakaoTokenRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Cookie socialLogin(String code) {
        KakaoTokenResponse token = getToken(code)
                .orElseThrow(()-> new IllegalStateException("API 응답이 비어있습니다. [카카오]."));
        KakaoUserInfoResponse userInfo = getUserInfo(token)
                .orElseThrow(()-> new IllegalStateException("API 응답이 비어있습니다. [카카오]"));
        String accessToken = createAccessToken(userInfo, token);
        return createCookie("Authorization", accessToken);
    }

    @Retryable(
            retryFor = KakaoTokenExpiredException.class,
            maxAttempts = 2
    )
    @Async("kakaoMessage")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOrderMessage(KakaoOrderMessageTemplate messageTemplate, Long memberId) {

        KakaoToken kakaoToken = kakaoTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new NotFoundEntityException("해당 회원의 토큰이 없습니다."));


        LinkedMultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("template_id", String.valueOf(122829));

        try {
            String templateArgs = objectMapper.writeValueAsString(messageTemplate);
            form.add("template_args",templateArgs);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("메시지 템플릿 파싱 실패");
        }

        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri(kakaoProperties.getkApiUri() + "/v2/api/talk/memo/send")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoToken.getAccessToken())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString())
                    .body(form)
                    .retrieve()
                    .onStatus(new KakaoKApiResponseHandler())
                    .toEntity(Void.class);
        } catch (KakaoTokenExpiredException ex) {
            reissueToken(kakaoToken);
            throw ex;
        }
    }

    public Optional<KakaoTokenResponse> getToken(String code) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakaoProperties.getKakaoRestApiKey());
        form.add("redirect_uri",kakaoProperties.getKakaoRedirectUri());
        form.add("code", code);

        ResponseEntity<KakaoTokenResponse> kakaoTokenResponse = restClient.post()
                .uri(kakaoProperties.getkAuthUri() + "/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(new KakaoKAuthResponseHandler())
                .toEntity(KakaoTokenResponse.class);

        return Optional.ofNullable(kakaoTokenResponse.getBody());

    }

    public Optional<KakaoUserInfoResponse> getUserInfo(KakaoTokenResponse response) {

        ResponseEntity<KakaoUserInfoResponse> userInfoResponse = restClient.get()
                .uri(kakaoProperties.getkApiUri() + "/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.access_token())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(new KakaoKApiResponseHandler())
                .toEntity(KakaoUserInfoResponse.class);

        return Optional.ofNullable(userInfoResponse.getBody());
    }

    private void reissueToken(KakaoToken kakaoToken) {

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id",kakaoProperties.getKakaoRestApiKey());
        form.add("refresh_token", kakaoToken.getRefreshToken());


        ResponseEntity<KakaoTokenResponse> response = restClient.post()
                .uri(kakaoProperties.getkAuthUri() + "/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(new KakaoKAuthResponseHandler())
                .toEntity(KakaoTokenResponse.class);

        KakaoToken findToken = kakaoTokenRepository.findById(kakaoToken.getId())
                .orElseThrow(() -> new NotFoundEntityException("존재하지 않는 카카오 토큰 입니다."));

        KakaoTokenResponse newToken = response.getBody();

        if (newToken.access_token() != null)
            findToken.changeAccessToken(newToken.access_token());
        if (newToken.refresh_token() != null)
            findToken.changeRefreshToken(newToken.refresh_token());
    }

    private String createAccessToken(KakaoUserInfoResponse userInfoResponse, KakaoTokenResponse tokenResponse) {
        KakaoAccount kakaoAccount = userInfoResponse.kakao_account();

        Member member = memberService.socialLogin(new SocialLoginRequest(kakaoAccount.email(), Social.KAKAO));

        kakaoTokenRepository.deleteByMemberId(member.getId());

        kakaoTokenRepository.save(new KakaoToken(tokenResponse.access_token(), tokenResponse.refresh_token(), member));

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
