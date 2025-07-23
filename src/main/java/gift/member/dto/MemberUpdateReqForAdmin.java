package gift.member.dto;

import gift.domain.Role;
import gift.global.annotation.EnumConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static gift.util.PatternUtil.PASSWORD_PATTERN;

public class MemberUpdateReqForAdmin {

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

    @EnumConstraint(enumClass = Role.class)
    private String role;

    public MemberUpdateReqForAdmin(String password, String newPassword, String confirmPassword, String role) {
        this.password = password;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
        this.role = role;
    }

    protected MemberUpdateReqForAdmin() {
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

    public String getRole() {
        return role;
    }
}
