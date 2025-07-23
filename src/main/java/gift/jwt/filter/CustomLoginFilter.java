package gift.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.global.exception.AuthorizationException;
import gift.global.exception.NotFoundEntityException;
import gift.jwt.JWTUtil;
import gift.member.dto.MemberLoginRequest;
import gift.member.dto.MemberResponse;
import gift.member.service.MemberService;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CustomLoginFilter implements Filter {

    private final MemberService memberService;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;


    public CustomLoginFilter(MemberService memberService, JWTUtil jwtUtil, ObjectMapper objectMapper) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rep, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) rep;

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        try {
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, UTF_8);
            MemberLoginRequest memberLoginRequest = objectMapper.readValue(messageBody, MemberLoginRequest.class);

            MemberResponse validateMember = memberService.validate(memberLoginRequest.getEmail(), memberLoginRequest.getPassword());
            String jwt = jwtUtil.createJWT(
                    validateMember.getEmail(),
                    validateMember.getRole().toString(),
                    1000 * 60 * 60L
            );

            response.setStatus(HttpServletResponse.SC_OK);
            response.addCookie(createCookie("Authorization", jwt));

        } catch (NotFoundEntityException | AuthorizationException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", ex.getMessage())));
        }
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
