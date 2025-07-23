package gift.oauth2.controller;

import gift.oauth2.dto.KakaoTokenRequest;
import gift.oauth2.service.LoginService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    private final LoginService loginService;

    @Value("${oauth2.key.kakao}")
    private String kakaoRestApiKey;
    @Value("${oauth2.redirect.kakao}")
    private String kakaoRedirectUri;
    @Value("${oauth2.host.kakao}")
    private String kakaoHost;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("kakaoRestApiKey", kakaoRestApiKey);
        model.addAttribute("kakaoRedirectUri", kakaoRedirectUri);

        return "login";
    }

    @GetMapping("/login/oauth/kakao")
    public String kakaoLogin(@RequestParam String code) {

        loginService.login(new KakaoTokenRequest(code, kakaoRestApiKey, kakaoHost, kakaoRedirectUri));

        return "body";
    }
}
