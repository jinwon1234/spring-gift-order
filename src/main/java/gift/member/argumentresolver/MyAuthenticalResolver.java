package gift.member.argumentresolver;

import gift.global.MySecurityContextHolder;
import gift.member.annotation.MyAuthenticalPrincipal;
import gift.member.dto.AuthMember;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class MyAuthenticalResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        boolean hasAnnotation = parameter.hasParameterAnnotation(MyAuthenticalPrincipal.class);

        boolean hasParameter = AuthMember.class.isAssignableFrom(parameter.getParameterType());

        return hasParameter && hasAnnotation;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        return MySecurityContextHolder.get();
    }
}
