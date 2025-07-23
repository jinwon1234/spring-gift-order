package gift.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static gift.util.PatternUtil.PASSWORD_PATTERN;

public class MemberCreateRequest implements MemberCreateDto{

    @Email
    private String email;

    @Size(min = 10, max = 20, message = "비밀번호는 10자이상 20자이하로 입력해주세요")
    @Pattern(
            regexp = PASSWORD_PATTERN,
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

    @Size(min = 10, max = 20, message = "비밀번호는 10자이상 20자이하로 입력해주세요")
    @Pattern(
            regexp = PASSWORD_PATTERN,
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    @NotBlank(message = "확인 비밀번호를 입력해주세요")
    private String confirmPassword;

    public MemberCreateRequest(String email, String password, String confirmPassword) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    protected MemberCreateRequest(){}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public String getRole() {
        return "REGULAR";
    }
}
