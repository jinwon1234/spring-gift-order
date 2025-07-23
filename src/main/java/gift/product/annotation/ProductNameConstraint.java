package gift.product.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = gift.product.validator.ProductNameValidator.class)
public @interface ProductNameConstraint {

    String message() default "카카오가 포함된 문구는 담당 MD와 협의 후 사용할 수 있습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}