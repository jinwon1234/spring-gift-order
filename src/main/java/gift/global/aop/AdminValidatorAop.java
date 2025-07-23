package gift.global.aop;

import gift.domain.Role;
import gift.global.MySecurityContextHolder;
import gift.global.exception.AuthorizationException;
import gift.member.dto.AuthMember;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminValidatorAop {

    @Around("@annotation(gift.global.annotation.OnlyForAdmin)")
    public Object validateAdmin(ProceedingJoinPoint joinPoint) throws Throwable{

        AuthMember authMember = MySecurityContextHolder.get();
        if (authMember.getRole() != Role.ADMIN)
            throw new AuthorizationException("권한 부족");

        return joinPoint.proceed();
    }
}
