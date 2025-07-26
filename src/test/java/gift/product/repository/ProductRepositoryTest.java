package gift.product.repository;

import gift.domain.Member;
import gift.domain.Product;
import gift.domain.Role;
import gift.domain.Social;
import gift.member.repository.MemberRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace =  AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("상품 저장 & 조회")
    void save() {

        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        Product product = new Product("스윙칩", 3000, "image", member);
        memberRepository.save(member);
        Product saved = productRepository.save(product);

        // when
        Product findProduct = productRepository.findById(saved.getId()).get();

        // then
        assertSoftly(softly -> {
            softly.assertThat(findProduct.getId()).isEqualTo(saved.getId());
            softly.assertThat(findProduct.getName()).isEqualTo(saved.getName());
            softly.assertThat(findProduct.getPrice()).isEqualTo(saved.getPrice());
            softly.assertThat(findProduct.getImageUrl()).isEqualTo(saved.getImageUrl());
        });
    }

    @Test
    @DisplayName("상품 저장 & 회원 아이디로 조회")
    void findByMemberIdWithOptions() {

        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        memberRepository.save(member);
        Product product = new Product("스윙칩", 3000, "image", member);
        productRepository.save(product);

        // when
        List<Product> result = productRepository.findByMemberIdWithOptions(member.getId());

        // then
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("상품 삭제")
    void deleteById() {

        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        memberRepository.save(member);
        Product product = new Product("스윙칩", 3000, "image", member);
        productRepository.save(product);

        // when
        productRepository.deleteById(product.getId());
        Optional<Product> findProduct = productRepository.findById(product.getId());

        // then
        assertThat(findProduct).isNotPresent();

    }

    @Test
    @DisplayName("나의 상품 페이징 조회")
    void findByMemberIdWithPage() {

        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
        memberRepository.save(member);

        for (int i=0; i<11; i++) {
            Product product = new Product("스윙칩"+i, 3000, "image", member);
            productRepository.save(product);
        }

        // when
        Page<Product> result = productRepository.findByMemberIdWithPage(member.getId(), PageRequest.of(0, 5));

        // then
        assertThat(result.getSize()).isEqualTo(5);
    }
}