package gift.jwt;

import gift.domain.Role;
import gift.global.exception.JWTValidateException;
import gift.global.exception.NotFoundEntityException;
import gift.member.dto.AuthMember;
import gift.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;


public class JWTValidator {

    private final JWTUtil jwtUtil;
    private final MemberService memberService;

    public JWTValidator(JWTUtil jwtUtil, MemberService memberService) {
        this.jwtUtil = jwtUtil;
        this.memberService = memberService;
    }

    public AuthMember validateJWT(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) throw new JWTValidateException("쿠키가 존재하지 않습니다.");

        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("Authorization"))
                .findFirst()
                .map(cookie -> cookie.getValue())
                .orElse(null);

        if (accessToken == null) {
            throw new JWTValidateException("토큰이 존재하지 않습니다.");
        }

        Claims claims;

        try {
            claims = jwtUtil.getClaims(accessToken);
        } catch (ExpiredJwtException e) {
            throw new JWTValidateException("만료된 토큰입니다.");
        }


        try {
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            memberService.validateToken(email, role);
            return new AuthMember(email, Role.valueOf(role));
        } catch (NotFoundEntityException e) {
            throw new JWTValidateException(e.getMessage());
        }


    }
}
