package gift.member.dto;


import gift.domain.Role;

public class MemberResponse {

    private Long id;
    private String email;
    private Role role;

    public MemberResponse(Long id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    protected MemberResponse(){}

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
