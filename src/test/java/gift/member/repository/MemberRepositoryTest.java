package gift.member.repository;

import gift.domain.Member;
import gift.domain.Role;
import gift.domain.Social;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.*;


@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace =  AutoConfigureTestDatabase.Replace.NONE)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("화원 저장 성공")
    void save() {

        // given
        Member member = new Member("ljw0626@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);

        // when
        Member save = memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId())
                .get();

        // then
        assertSoftly(softly -> {
            softly.assertThat(findMember.getId()).isEqualTo(save.getId());
            softly.assertThat(findMember.getEmail()).isEqualTo(save.getEmail());
        });
    }

    @Test
    @DisplayName("이메일로 회원 조회")
    void findByEmail() {
        // given
        Member member = new Member("ljw0626@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);

        // when
        Member save = memberRepository.save(member);

        Member findMember = memberRepository.findByEmail(member.getEmail())
                .get();

        // then
        assertSoftly(softly -> {
            softly.assertThat(findMember.getId()).isEqualTo(save.getId());
            softly.assertThat(findMember.getEmail()).isEqualTo(save.getEmail());
        });
    }

    @Test
    @DisplayName("회원 삭제")
    void deleteById() {
        // given
        Member member = new Member("ljw0626@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);

        // when
        Member save = memberRepository.save(member);
        memberRepository.deleteById(save.getId());
        Optional<Member> findMember = memberRepository.findById(save.getId());

        // then
        assertThat(findMember).isNotPresent();
    }

    @Test
    @DisplayName("회원 페이징 쿼리")
    void findAllWithPage() {

        // given
        for (int i=0; i<11; i++) {
            Member member = new Member(i + "user@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
            memberRepository.save(member);
        }

        // when
        Page<Member> result = memberRepository.findAllWithPage(PageRequest.of(0, 5));

        // then
        assertThat(result.getSize()).isEqualTo(5);
    }
}