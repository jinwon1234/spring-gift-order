package gift.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.jwt.JWTUtil;
import gift.jwt.JWTValidator;
import gift.jwt.filter.ApiFilter;
import gift.jwt.filter.CustomLoginFilter;
import gift.jwt.filter.CustomLogoutFilter;
import gift.jwt.filter.ViewFilter;
import gift.member.argumentresolver.MyAuthenticalResolver;
import gift.member.service.MemberService;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private final MemberService memberService;
    private final JWTValidator jwtValidator;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;


    public SecurityConfig(MemberService memberService, JWTUtil jwtUtil, ObjectMapper objectMapper) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.jwtValidator = new JWTValidator(jwtUtil, memberService);
    }

    @Bean
    public FilterRegistrationBean customLoginFilter() {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new CustomLoginFilter(memberService, jwtUtil, objectMapper));

        filterFilterRegistrationBean.addUrlPatterns("/api/members/login");
        filterFilterRegistrationBean.setOrder(3);
        return filterFilterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean jwtFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ApiFilter(objectMapper, jwtValidator));

        filterRegistrationBean.addUrlPatterns("/api/*");
        filterRegistrationBean.setOrder(2);
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean viewFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ViewFilter(jwtValidator));
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(1);

        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean customLogoutFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new CustomLogoutFilter(objectMapper));
        filterRegistrationBean.addUrlPatterns("/api/members/logout");
        filterRegistrationBean.setOrder(4);
        return filterRegistrationBean;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MyAuthenticalResolver());
    }
}
