package gift.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static gift.util.PatternUtil.PASSWORD_PATTERN;

public class MemberUpdateRequest {

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

    @Size(min = 10, max = 20, message = "비밀번호는 10자이상 20자이하로 입력해주세요")
    @Pattern(
            regexp = PASSWORD_PATTERN,
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    @NotBlank(message = "새로운 비밀번호를 입력해주세요")
    private String newPassword;

    @Size(min = 10, max = 20, message = "비밀번호는 10자이상 20자이하로 입력해주세요")
    @Pattern(
            regexp = PASSWORD_PATTERN,
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    @NotBlank(message = "확인 비밀번호를 입력해주세요")
    private String confirmPassword;

    public MemberUpdateRequest(String password, String newPassword, String confirmPassword) {
        this.password = password;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getPassword() {
        return password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}
