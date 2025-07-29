package gift.member.controller;

import gift.domain.Member;
import gift.domain.Role;
import gift.domain.Social;
import gift.jwt.JWTUtil;
import gift.member.dto.*;
import gift.member.repository.MemberRepository;
import gift.member.service.MemberServiceV1;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JWTUtil jwtUtil;

    private RestClient restClient;

    private RestClient loginRestClient;
  
    @Autowired
    private MemberServiceV1 memberServiceV1;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/members")
                .build();
        loginRestClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/members/login")
                .build();
    }

    @AfterEach
    void clear() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원 가입 성공")
    void joinMemberSuccess() {
        MemberCreateRequest memberCreateRequest = memberCreateRequest();

        ResponseEntity<Void> response = restClient.post()
                .body(memberCreateRequest)
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("회원 가입 실패 - 비밀번호에 특수문자 없음")
    void joinMemberFail() {
        MemberCreateRequest wrongRequest = new MemberCreateRequest("ljw2109@naver.com", "Qwer123456", "Qwer123456");

        assertThatThrownBy(()->restClient.post()
                .body(wrongRequest)
                .retrieve()
                .toEntity(Void.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 형식에 맞지 않음")
    void joinMemberFail2() {
        MemberCreateRequest wrongRequest = new MemberCreateRequest("ljw2109", "Qwer1234!!", "Qwer1234!!");

        assertThatThrownBy(()->restClient.post()
                .body(wrongRequest)
                .retrieve()
                .toEntity(Void.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    @DisplayName("회원 가입 실패 - 비밀번호가 확인 비밀번호와 서로 다름")
    void joinMemberFail3() {
        MemberCreateRequest wrongRequest = new MemberCreateRequest("ljw2109@naver.com", "Qwer1234!!!", "Qwer1234!!");

        assertThatThrownBy(()->restClient.post()
                .body(wrongRequest)
                .retrieve()
                .toEntity(Void.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() {

        memberRepository.save(createMember(Role.REGULAR));

        ResponseEntity<Void> response = loginRestClient.post()
                .body(new MemberLoginRequest("ljw2109@naver.com", "Qwer1234!!"))
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void loginFail() {
        memberRepository.save(createMember(Role.REGULAR));

        assertThatThrownBy(()->loginRestClient.post()
                .body(new MemberLoginRequest("ljw2109@naver.com", "Qwer1235!!"))
                .retrieve()
                .toEntity(Void.class))
                .isInstanceOf(HttpClientErrorException.Forbidden.class)
                .hasMessageContaining("비밀번호가 다릅니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 아이디 틀림")
    void loginFail2() {
        memberRepository.save(createMember(Role.REGULAR));

        assertThatThrownBy(()->loginRestClient.post()
                .body(new MemberLoginRequest("ljw2108@naver.com", "Qwer1234!!"))
                .retrieve()
                .toEntity(Void.class))
                .isInstanceOf(HttpClientErrorException.Forbidden.class)
                .hasMessageContaining("존재하는 회원이 아닙니다");
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withDrawSuccess() {
        Member member = createMember(Role.REGULAR);
        Member savedMember = memberRepository.save(member);

        ResponseEntity<Void> response = restClient.delete()
                .cookie("Authorization",createJWT(savedMember))
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 회원")
    void withDrawFail() {
        String jwt = jwtUtil.createJWT("unkhown@email.com", Role.REGULAR.toString(),  1000 * 60L);
        assertThatThrownBy(()->restClient.delete()
                .cookie("Authorization", jwt)
                .retrieve()
                .toEntity(Void.class)
        ).isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    @DisplayName("회원 비밀번호 변경 성공")
    void changePasswordSuccess() {
        Member member = createMember(Role.REGULAR);
        Member savedMember = memberRepository.save(member);
        MemberUpdateRequest updateRequest =
                new MemberUpdateRequest("Qwer1234!!", "Qwer12345!!", "Qwer12345!!");

        ResponseEntity<Void> response = restClient.patch()
                .body(updateRequest)
                .cookie("Authorization",createJWT(savedMember))
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }

    @Test
    @DisplayName("회원 비밀 번호 변경 실패 - 존재하지 않는 회원")
    void changePasswordFail() {
        MemberUpdateRequest updateRequest =
                new MemberUpdateRequest("Qwer1234!!", "Qwer12345!!", "Qwer12345!!");

        String jwt = jwtUtil.createJWT("unkhown@email.com", Role.REGULAR.toString(),  1000 * 60L);

        assertThatThrownBy(()->restClient.patch()
                .cookie("Authorization",jwt)
                .body(updateRequest)
                .retrieve()
                .toEntity(Void.class)
        ).isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    @DisplayName("관리자 권한 회원 조회 성공")
    void getMembersSuccessForAdmin() {
        Member member = createMember(Role.ADMIN);
        memberRepository.save(member);

        String jwt = createJWT(member);

        ResponseEntity<Object> response = restClient.get()
                .cookie("Authorization", jwt)
                .retrieve().toEntity(Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("권한 부족으로 회원 조회 실패")
    void getMembersFailForAdmin() {
        Member member = createMember(Role.REGULAR);
        memberRepository.save(member);

        String jwt = createJWT(member);

        assertThatThrownBy(()->restClient.get()
                .cookie("Authorization", jwt)
                .retrieve().toEntity(List.class))
                .isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    @DisplayName("관리자 권한 회원 삭제 성공")
    void deleteMemberSuccessForAdmin() {
        Member member = createMember(Role.ADMIN);
        memberRepository.save(member);

        String jwt = createJWT(member);

        ResponseEntity<Void> response = restClient.delete()
                .uri("/{id}",member.getId().toString())
                .cookie("Authorization", jwt)
                .retrieve().toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("권한 부족으로 회원 삭제 실패")
    void deleteMemberFailForAdmin() {
        Member member = createMember(Role.REGULAR);
        memberRepository.save(member);

        String jwt = createJWT(member);

        assertThatThrownBy(()->restClient.delete()
                .uri("/{id}",member.getId().toString())
                .cookie("Authorization", jwt)
                .retrieve().toEntity(Void.class))
                .isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    @DisplayName("관리자 권한으로 회원 추가 성공")
    void addMemberSuccessForAdmin() {
        Member member = createMember(Role.ADMIN);
        memberRepository.save(member);
        String jwt = createJWT(member);


        ResponseEntity<Void> response = restClient.post()
                .uri("/admin")
                .cookie("Authorization", jwt)
                .body(new MemberCreateReqForAdmin("temp@naver.com", "Qwer1234!!", "Qwer1234!!", "REGULAR"))
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("관리자 권한으로 회원 추가 실패 - 권한 부족")
    void addMemberFailForAdmin() {
        Member member = createMember(Role.REGULAR);
        memberRepository.save(member);
        String jwt = createJWT(member);


        assertThatThrownBy(()->restClient.post()
                .uri("/admin")
                .cookie("Authorization", jwt)
                .body(new MemberCreateReqForAdmin("temp@naver.com", "Qwer1234!!", "Qwer1234!!", "REGULAR"))
                .retrieve()
                .toEntity(Void.class)
        ).isInstanceOf(HttpClientErrorException.Forbidden.class);
    }


    @Test
    @DisplayName("관리자 권한으로 회원 수정 실패 - 권한 부족")
    void editMemberFailForAdmin() {
        Member member = createMember(Role.REGULAR);
        memberRepository.save(member);
        String jwt = createJWT(member);


        assertThatThrownBy(()->restClient.put()
                .uri("/{id}", member.getId().toString())
                .cookie("Authorization", jwt)
                .body(new MemberUpdateReqForAdmin(member.getPassword(), "Qwer12345!!", "Qwer12345!!",member.getId().toString() ))
                .retrieve()
                .toEntity(Void.class)
        ).isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    @DisplayName("관리자 권한으로 회원 수정 성공")
    void editMemberSuccessForAdmin() {
        Member member = createMember(Role.ADMIN);
        memberRepository.save(member);
        String jwt = createJWT(member);


        ResponseEntity<Void> response = restClient.put()
                .uri("/{id}", member.getId().toString())
                .cookie("Authorization", jwt)
                .body(new MemberUpdateReqForAdmin("Qwer1234!!", "Qwer12345!!", "Qwer12345!!", member.getRole().toString()))
                .retrieve()
                .toEntity(Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logoutSuccess() {
        Member member = createMember(Role.REGULAR);
        memberRepository.save(member);
        String jwt = createJWT(member);

        ResponseEntity<Map> response = restClient.post()
                .uri("/logout")
                .cookie("Authorization", jwt)
                .retrieve()
                .toEntity(Map.class);

        assertSoftly(softly-> {
            softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            softly.assertThat(response.getHeaders().get("Set-Cookie")).hasSize(1);
            softly.assertThat(response.getBody().get("message")).isEqualTo("로그아웃 완료");
        });
    }



    private String createJWT(Member member) {
        return jwtUtil.createJWT(member.getEmail(), member.getRole().toString(), 1000 * 60L);
    }

    private Member createMember(Role role) {
        String password = passwordEncoder.encode("Qwer1234!!");
        return new Member("ljw2109@naver.com", password, role, Social.NONE);
    }

    private MemberCreateRequest memberCreateRequest() {
        return new MemberCreateRequest("ljw2109@naver.com", "Qwer1234!!", "Qwer1234!!");
    }
}