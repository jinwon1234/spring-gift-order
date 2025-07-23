package gift.product.validator;

import gift.domain.Role;
import gift.global.MySecurityContextHolder;
import gift.member.dto.AuthMember;
import gift.product.annotation.ProductNameConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProductNameValidator implements ConstraintValidator<ProductNameConstraint, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        AuthMember authMember = MySecurityContextHolder.get();
        if (authMember.getRole() == Role.ADMIN) return true;

        if (value == null) return false;
        if (value.contains("카카오")) return  false;
        return true;
    }
}
