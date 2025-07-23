package gift.member.dto;

import gift.domain.Role;

public class AuthMember {

    private String email;
    private Role role;

    public AuthMember(String email, Role role) {
        this.email = email;
        this.role = role;
    }

    protected AuthMember() {

    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
