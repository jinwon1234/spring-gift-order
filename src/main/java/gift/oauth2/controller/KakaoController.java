package gift.oauth2.controller;

import gift.oauth2.service.KakaoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class KakaoController {

    private final KakaoService loginService;


    public KakaoController(KakaoService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login/oauth/kakao")
    public RedirectView kakaoLogin(@RequestParam String code,
                                   @RequestParam(required = false, defaultValue = "/") String state,
                                   HttpServletResponse response) {

        String accessToken = loginService.socialLogin(code);

        response.addHeader("Set-Cookie", accessToken);
        return new RedirectView(state);
    }
}
