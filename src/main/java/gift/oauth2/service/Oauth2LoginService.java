package gift.oauth2.service;

import gift.member.service.MemberService;
import gift.oauth2.dto.KakaoTokenRequest;
import gift.oauth2.dto.KakaoTokenResponse;
import gift.oauth2.dto.KakaoUserInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import static gift.oauth2.dto.KakaoUserInfoResponse.*;

@Service
public class LoginService {

    private final RestClient restClient;
    private final MemberService memberService;

    public LoginService(MemberService memberService) {
        this.memberService = memberService;
        this.restClient = RestClient.builder().build();
    }

    public void login(KakaoTokenRequest request) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", request.key());
        form.add("redirect_uri", request.redirectUri());
        form.add("code", request.code());

        ResponseEntity<KakaoTokenResponse> kakaoTokenResponse = restClient.post()
                .uri(request.host())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toEntity(KakaoTokenResponse.class);

        if (kakaoTokenResponse.getBody() == null)
            throw new IllegalStateException("토큰 응답이 비어있습니다. [카카오 oauth2]");
        getUserInfo(kakaoTokenResponse.getBody());

    }

    private void getUserInfo(KakaoTokenResponse response) {

        System.out.println(response.access_token());

        ResponseEntity<KakaoUserInfoResponse> userInfoResponse = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.access_token())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString())
                .retrieve()
                .toEntity(KakaoUserInfoResponse.class);

        KakaoAccount kakaoAccount = userInfoResponse.getBody().kakao_account();

        memberService
    }
}
