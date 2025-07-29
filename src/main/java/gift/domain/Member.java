package gift.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Social social;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishProduct> wishProducts = new ArrayList<>();
  
    public Member(String email, String password, Role role, Social social) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.social = social;
    }

    public Member(Long id, String email, String password, Role role, Social social) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.social = social;
    }

    protected Member() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Social getSocial() {
        return social;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}
