package gift.option.repository;

import gift.domain.*;
import gift.global.exception.NotFoundEntityException;
import gift.member.repository.MemberRepository;
import gift.product.repository.ProductRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@DataJpaTest
@AutoConfigureTestDatabase(replace =  AutoConfigureTestDatabase.Replace.NONE)
class OptionRepositoryTest {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("옵션 추가 & 조회(Product 페치 조인)")
    void findByIdWithProduct() {
        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product("스윙칩", 3000, "image", member);
        memberRepository.save(member);
        productRepository.save(product);
        Option save = optionRepository.save(new Option("옵션1", 100, product));

        // when
        Option option = optionRepository.findByIdWithProduct(save.getId()).get();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(option.getId()).isEqualTo(save.getId());
            softly.assertThat(option.getProduct().getId()).isEqualTo(save.getProduct().getId());
            softly.assertThat(option.getProduct().getMember().getId()).isEqualTo(save.getProduct().getMember().getId());
        });

    }

    @Test
    @DisplayName("옵션 삭제")
    void deleteById() {
        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product("스윙칩", 3000, "image", member);
        memberRepository.save(member);
        productRepository.save(product);
        Option save = optionRepository.save(new Option("옵션1", 100, product));

        // when
        optionRepository.deleteById(save.getId());

        // then
        assertThatThrownBy(()->optionRepository.findById(save.getId())
                .orElseThrow(()-> new NotFoundEntityException("논리삭제 시 조회 안됨 - @SQLRestriction"))
        ).isInstanceOf(NotFoundEntityException.class);
    }

    @Test
    @DisplayName("중복된 이름 숫자 조회")
    void countByProductIdAndOptionNames() {
        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product("스윙칩", 3000, "image", member);
        memberRepository.save(member);
        productRepository.save(product);
        Option save = optionRepository.save(new Option("옵션1", 100, product));

        // when
        long result = optionRepository.countByProductIdAndOptionNames(List.of("옵션1"), product.getId());

        // then
        assertThat(result).isEqualTo(1L);
    }
}