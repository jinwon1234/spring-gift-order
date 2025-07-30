package gift.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.util.CookieProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.io.IOException;
import java.util.Map;

public class CustomLogoutFilter implements Filter {

    private final ObjectMapper objectMapper;
    private final CookieProperties cookieProperties;

    public CustomLogoutFilter(ObjectMapper objectMapper, CookieProperties cookieProperties) {
        this.objectMapper = objectMapper;
        this.cookieProperties = cookieProperties;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rep, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) rep;

        String method = request.getMethod();

        if (!"POST".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }


        response.setHeader("Set-Cookie", createCookie("Authorization", null));
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", "로그아웃 완료")));
    }

    private String createCookie(String key, String value) {
        return ResponseCookie.from(key,value)
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .domain(cookieProperties.domain())
                .sameSite(cookieProperties.sameSite())
                .secure(cookieProperties.secure())
                .build()
                .toString();
    }
}
