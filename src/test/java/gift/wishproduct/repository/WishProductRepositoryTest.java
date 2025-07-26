package gift.wishproduct.repository;

import gift.domain.*;
import gift.member.repository.MemberRepository;
import gift.option.repository.OptionRepository;
import gift.product.repository.ProductRepository;
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


@Transactional
@DataJpaTest
@AutoConfigureTestDatabase(replace =  AutoConfigureTestDatabase.Replace.NONE)
class WishProductRepositoryTest {

    @Autowired
    private WishProductRepository wishProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Test
    @DisplayName("위시 상품 저장 & 조회")
    void save() {
        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR);
        Product product = new Product("스윙칩", 3000, "image", member);
        Option option = new Option("옵션1", 10, product);
        memberRepository.save(member);
        productRepository.save(product);
        optionRepository.save(option);
        WishProduct saved = wishProductRepository.save(new WishProduct(30, member, product,option ));

        // when
        WishProduct findWishProduct = wishProductRepository.findById(saved.getId()).get();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(saved.getId()).isEqualTo(findWishProduct.getId());
            softly.assertThat(findWishProduct.getOwner().getId()).isEqualTo(member.getId());
            softly.assertThat(findWishProduct.getProduct().getId()).isEqualTo(product.getId());
            softly.assertThat(findWishProduct.getQuantity()).isEqualTo(saved.getQuantity());
        });
    }

    @Test
    @DisplayName("ownerId & productId로 조회")
    void findByOwnerIdAndProductId() {

        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR);
        Product product = new Product("스윙칩", 3000, "image", member);
        Option option = new Option("옵션1", 10, product);
        memberRepository.save(member);
        productRepository.save(product);
        optionRepository.save(option);
        WishProduct saved = wishProductRepository.save(new WishProduct(30, member, product,option));

        // when
        WishProduct findWishProduct = wishProductRepository.findByOwnerIdAndOptionId(member.getId(), option.getId()).get();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(saved.getId()).isEqualTo(findWishProduct.getId());
            softly.assertThat(findWishProduct.getOwner().getId()).isEqualTo(member.getId());
            softly.assertThat(findWishProduct.getProduct().getId()).isEqualTo(product.getId());
            softly.assertThat(findWishProduct.getQuantity()).isEqualTo(saved.getQuantity());
        });

    }

    @Test
    @DisplayName("자신의 위시리스트 조회")
    void findWithProductByOwnerId() {
        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR);
        Product product = new Product("스윙칩", 3000, "image", member);
        Option option = new Option("옵션1", 10, product);
        memberRepository.save(member);
        productRepository.save(product);
        optionRepository.save(option);
        wishProductRepository.save(new WishProduct(30, member, product,option));

        // when
        List<WishProduct> result = wishProductRepository.findByOwnerIdWithFetch(member.getId());

        // then
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("위시 상품 삭제")
    void deleteById() {
        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR);
        Product product = new Product("스윙칩", 3000, "image", member);
        Option option = new Option("옵션1", 10, product);
        memberRepository.save(member);
        productRepository.save(product);
        optionRepository.save(option);
        WishProduct saved = wishProductRepository.save(new WishProduct(30, member, product, option));

        // when
        wishProductRepository.deleteById(saved.getId());
        Optional<WishProduct> findWishProduct = wishProductRepository.findById(saved.getId());

        // then
        assertThat(findWishProduct).isNotPresent();
    }

    @Test
    @DisplayName("위시 상품 페이징 조회")
    void findWithProductByOwnerIdWithPage() {

        // given
        Member member = new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR);
        Product product = new Product("스윙칩", 3000, "image", member);
        Option option = new Option("옵션1", 10, product);
        memberRepository.save(member);
        productRepository.save(product);
        optionRepository.save(option);
        WishProduct saved = wishProductRepository.save(new WishProduct(30, member, product, option));

        // when
        Page<WishProduct> result = wishProductRepository.findByOwnerIdWithPageAndFetch(member.getId(), PageRequest.of(0, 1));

        // then
        assertThat(result.getSize()).isEqualTo(1);


    }
}