package gift.option.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

import static gift.util.PatternUtil.OPTION_NAME_PATTERN;

public record OptionCreateRequest(
        @Size(min = 1, max = 50, message = "옵션 이름은 1글자 이상 50글자 이하여야합니다.")
        @Pattern(regexp = OPTION_NAME_PATTERN)
        String optionName,

        @Range(min = 1, max = 99999999)
        int quantity
) {
}
