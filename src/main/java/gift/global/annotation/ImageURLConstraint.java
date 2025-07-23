package gift.global.annotation;

import gift.global.validator.ImageURLValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ImageURLValidator.class)
public @interface ImageURLConstraint {
    String message() default "이미지 url 형식에 맞게 입력해주세요";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
