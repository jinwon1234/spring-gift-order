package gift.global.validator;

import gift.global.annotation.ImageURLConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageURLValidator implements ConstraintValidator<ImageURLConstraint, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) return false;

        boolean isHTTPImage = false;
        boolean isDataImage = false;
        if (value.startsWith("http://") || value.startsWith("https://")) {
            if ((value.endsWith(".jpg") || value.endsWith(".jpeg") || value.endsWith(".png") || value.endsWith(".gif") || value.endsWith(".webp") || value.endsWith(".svg")))
                isHTTPImage = true;
        }

        if (value.startsWith("data:image/") && value.contains("base64,"))
            isDataImage = true;

        return isHTTPImage || isDataImage;
    }
}
