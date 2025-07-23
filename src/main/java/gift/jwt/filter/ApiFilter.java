package gift.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.global.MySecurityContextHolder;
import gift.global.exception.JWTValidateException;
import gift.jwt.JWTValidator;
import gift.member.dto.AuthMember;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class ApiFilter implements Filter {

    private final ObjectMapper objectMapper;
    private final JWTValidator jwtValidator;

    public ApiFilter(ObjectMapper objectMapper, JWTValidator jwtValidator) {
        this.objectMapper = objectMapper;
        this.jwtValidator = jwtValidator;
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse rep, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) rep;

        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        if(passRequestURI(method, requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            AuthMember authMember = jwtValidator.validateJWT(request);
            MySecurityContextHolder.set(authMember);
            chain.doFilter(request,response);
        } catch (JWTValidateException e) {
            writeUnauthorizedResponse(response, e.getMessage());
        }
        finally {
            MySecurityContextHolder.clear();
        }

    }

    private boolean passRequestURI(String method, String requestURI) {
        if (requestURI.equals("/login")) return true;
        if (requestURI.equals("/api/members/login"))  return true;
        if (requestURI.equals("/api/members") && "POST".equalsIgnoreCase(method)) return true;

        return false;
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", message)));
    }
}
