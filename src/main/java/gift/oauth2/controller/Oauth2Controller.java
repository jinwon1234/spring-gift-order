package gift.oauth2.controller;

import gift.oauth2.dto.KakaoTokenRequest;
import gift.oauth2.service.Oauth2Service;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class Oauth2Controller {

    private final Oauth2Service loginService;

    @Value("${oauth2.key.kakao}")
    private String kakaoRestApiKey;
    @Value("${oauth2.redirect.kakao}")
    private String kakaoRedirectUri;

    public Oauth2Controller(Oauth2Service loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login/oauth/kakao")
    public RedirectView kakaoLogin(@RequestParam String code,
                                   @RequestParam(required = false, defaultValue = "/") String state,
                                   HttpServletResponse response) {

        Cookie accessToken = loginService.socialLogin(new KakaoTokenRequest(code, kakaoRestApiKey, kakaoRedirectUri));

        response.addCookie(accessToken);
        return new RedirectView(state);
    }
}
