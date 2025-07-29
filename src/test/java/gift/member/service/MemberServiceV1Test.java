package gift.member.service;


import gift.domain.Member;
import gift.domain.Role;
import gift.domain.Social;
import gift.global.exception.AuthorizationException;
import gift.global.exception.BadRequestEntityException;
import gift.global.exception.DuplicateEntityException;
import gift.global.exception.NotFoundEntityException;
import gift.member.dto.MemberCreateRequest;
import gift.member.dto.MemberResponse;
import gift.member.dto.MemberUpdateRequest;
import gift.member.repository.MemberRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class MemberServiceV1Test {

    @InjectMocks
    private MemberServiceV1 memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원 조회 성공")
    void getMemberSuccess() {

        // given
        Member member = createMember();

        given(memberRepository.findById(member.getId()))
                .willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.findById(member.getId());

        //then
        assertSoftly(softly-> {
            softly.assertThat(response.getEmail()).isEqualTo(member.getEmail());
            softly.assertThat(response.getRole()).isEqualTo(member.getRole());
        });
        verify(memberRepository).findById(member.getId());
    }

    @Test
    @DisplayName("회원 조회 실패")
    void getMemberFail() {


        // given
        given(memberRepository.findById(1L))
                .willReturn(Optional.empty());


        // when & then
        assertThatThrownBy(()->memberService.findById(1L))
                .isInstanceOf(NotFoundEntityException.class);
        verify(memberRepository).findById(1L);
    }


    @Test
    @DisplayName("회원가입 성공")
    void joinMemberSuccess() {

        // given

        MemberCreateRequest memberDto = createRequest();
        Member joinMember = createMember();

        given(memberRepository.findByEmail(memberDto.getEmail()))
                .willReturn(Optional.empty());

        given(memberRepository.save(any(Member.class)))
                .willReturn(joinMember);

        // when
        Long saveId = memberService.save(memberDto);

        // then
        assertThat(saveId).isEqualTo(joinMember.getId());
        verify(memberRepository).save(any(Member.class));
        verify(memberRepository).findByEmail(memberDto.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    void joinMemberFail() {

        // given
        MemberCreateRequest memberDto = createRequest();
        Member joinMember = createMember();
        given(memberRepository.findByEmail(memberDto.getEmail()))
                .willReturn(Optional.of(joinMember));

        //when & then
        assertThatThrownBy(() -> memberService.save(memberDto))
                .isInstanceOf(DuplicateEntityException.class);
        verify(memberRepository).findByEmail(memberDto.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("회원가입 실패 - 서로 다른 비밀번호")
    void joinMemberFail2() {

        // given
        MemberCreateRequest memberDto =
                new MemberCreateRequest("ljw2109@naver.com", "Qwer1234!!", "Qwer1234!!5" );

        //when & then
        assertThatThrownBy(() -> memberService.save(memberDto))
                .isInstanceOf(BadRequestEntityException.class);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteMemberSuccess() {

        Member joinMember = createMember();
        // given
        given(memberRepository.findByEmail(joinMember.getEmail()))
                .willReturn(Optional.of(joinMember));

        // when
        memberService.deleteByEmail(joinMember.getEmail());

        // then
        verify(memberRepository).deleteById(joinMember.getId());
        verify(memberRepository).findByEmail(joinMember.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하는 회원 없음")
    void deleteMemberFail1() {

        // given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        // when
        assertThatThrownBy(()->memberService.deleteByEmail(anyString()))
                .isInstanceOf(NotFoundEntityException.class);

        // then
        verify(memberRepository).findByEmail(anyString());
        verifyNoMoreInteractions(memberRepository);
    }


    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePasswordSuccess() {

        // given
        Member member = createMember();
        MemberUpdateRequest updateRequest = new MemberUpdateRequest("curPassword","changePassword", "changePassword");

        given(memberRepository.findByEmail(member.getEmail()))
                .willReturn(Optional.of(member));

        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);
        // when
        memberService.changePassword(member.getEmail(), updateRequest);

        verify(memberRepository).findByEmail(member.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 인증 실패")
    void updatePasswordFail1() {

        // given
        Member member = createMember();
        MemberUpdateRequest updateRequest = new MemberUpdateRequest("wrongPassword","changePassword", "changePassword");

        given(memberRepository.findByEmail(member.getEmail()))
                .willReturn(Optional.of(member));

        given(passwordEncoder.matches(anyString(),anyString()))
                .willReturn(false);

        // when
        assertThatThrownBy(()->memberService.changePassword(member.getEmail(), updateRequest))
                .isInstanceOf(AuthorizationException.class);

        verify(memberRepository).findByEmail(member.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - newPassword와 confirmPassword가 다름")
    void updatePasswordFail2() {

        // given
        Member member = createMember();
        MemberUpdateRequest updateRequest = new MemberUpdateRequest("curPassword","changePassword", "changePassword2");

        given(memberRepository.findByEmail(member.getEmail()))
                .willReturn(Optional.of(member));

        given(passwordEncoder.matches(anyString(),anyString()))
                .willReturn(true);

        // when
        assertThatThrownBy(()->memberService.changePassword(member.getEmail(), updateRequest))
                .isInstanceOf(BadRequestEntityException.class);

        verify(memberRepository).findByEmail(member.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("인증 성공")
    void AuthenticationSuccess() {

        // given
        Member member = createMember();

        given(memberRepository.findByEmail(member.getEmail()))
                .willReturn(Optional.of(member));

        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);
        // when
        MemberResponse result = memberService.validate(member.getEmail(), member.getPassword());

        // then
        assertSoftly(softly-> {
            softly.assertThat(result.getEmail()).isEqualTo(member.getEmail());
            softly.assertThat(result.getId()).isEqualTo(member.getId());
            softly.assertThat(result.getRole()).isEqualTo(member.getRole());
        });
        verify(memberRepository).findByEmail(member.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("인증 실패 - 존재하는 회원 없음")
    void AuthenticationFail1() {

        // given
        Member member = createMember();

        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(()->memberService.validate(anyString(), member.getPassword()))
                .isInstanceOf(NotFoundEntityException.class);
        verify(memberRepository).findByEmail(anyString());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("인증 실패 - 비밀번호 불일치")
    void AuthenticationFail2() {

        // given
        Member member = createMember();

        given(memberRepository.findByEmail(member.getEmail()))
                .willReturn(Optional.of(member));

        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(false);

        //when & then
        assertThatThrownBy(()->memberService.validate(member.getEmail(), "1234Qwer!!"))
                .isInstanceOf(AuthorizationException.class);
        verify(memberRepository).findByEmail(member.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("토큰 검증 성공")
    void tokenValidateSuccess() {
        // given
        Member member = createMember();

        given(memberRepository.findByEmail(member.getEmail()))
                .willReturn(Optional.of(member));

        // when
        memberService.validateToken(member.getEmail(), member.getRole().toString());

        // then
        verify(memberRepository).findByEmail(member.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("토큰 검증 실패 - 존재하는 회원 없음")
    void tokenValidateFail() {
        // given

        Member member = createMember();

        given(memberRepository.findByEmail(member.getEmail()))
                .willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(()->memberService.validateToken(member.getEmail(), "ADMIN"))
                .isInstanceOf(NotFoundEntityException.class);
        verify(memberRepository).findByEmail(member.getEmail());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("토큰 검증 실패 - 역할이 다름")
    void tokenValidateFail2() {
        // given

        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(()->memberService.validateToken(anyString(), "REGULAR"))
                .isInstanceOf(NotFoundEntityException.class);
        verify(memberRepository).findByEmail(anyString());
        verifyNoMoreInteractions(memberRepository);
    }

    MemberCreateRequest createRequest() {
        return new MemberCreateRequest("ljw2109@naver.com", "curPassword", "curPassword");
    }

    Member createMember() {
        return new Member("ljw2109@naver.com", "curPassword", Role.REGULAR, Social.NONE);
    }

}