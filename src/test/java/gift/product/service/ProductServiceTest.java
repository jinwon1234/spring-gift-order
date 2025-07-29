package gift.product.service;

import gift.domain.Member;
import gift.domain.Product;
import gift.domain.Role;
import gift.domain.Social;
import gift.global.exception.BadRequestEntityException;
import gift.global.exception.NotFoundEntityException;
import gift.member.dto.AuthMember;
import gift.member.repository.MemberRepository;
import gift.option.dto.OptionCreateRequest;
import gift.product.dto.ProductCreateRequest;
import gift.product.dto.ProductResponse;
import gift.product.dto.ProductUpdateRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.*;


@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    ProductService productService;
    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("상품 추가, 조회 성공")
    void addProductSuccess() {
        Member member = addMemberCase();
        Product product = addProductCase(member);

        ProductResponse findProduct = productService.findProduct(product.getId());

        assertSoftly(softly -> {
            softly.assertThat(product.getId()).isEqualTo(findProduct.getId());
            softly.assertThat(product.getName()).isEqualTo(findProduct.getName());
            softly.assertThat(product.getPrice()).isEqualTo(findProduct.getPrice());
            softly.assertThat(product.getImageUrl()).isEqualTo(findProduct.getImageURL());
        });
    }

    @Test
    @DisplayName("자신의 모든 상품 조회")
    void getAllMyProductsSuccess() {

        Member member = addMemberCase();

        for (int i=0; i<10; i++) {
            addProductCase(member);
        }

        List<ProductResponse> allProducts = productService.findByEmail(new AuthMember(member.getEmail(), member.getRole()));

        assertThat(allProducts.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("자신의 상품 페이징 조회")
    void getAllMyProductsWithPageSuccess() {

        Member member = addMemberCase();

        for (int i=0; i<10; i++) {
            addProductCase(member);
        }

        Page<ProductResponse> result = productService.findByEmailWithPage(new AuthMember(member.getEmail(), member.getRole()),
                PageRequest.of(0,5));

        assertThat(result.getSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("상품 조회 실패 - 존재하지 않는 상품")
    void getProductSuccess() {
        assertThatThrownBy(()-> productService.findProduct(1L))
                .isInstanceOf(NotFoundEntityException.class);
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProductSuccess() {
        Member member = addMemberCase();
        Product product = addProductCase(member);
        productService.deleteProduct(product.getId(), new AuthMember(member.getEmail(), member.getRole()));

        assertThatThrownBy(()-> productService.findProduct(product.getId()))
                .isInstanceOf(NotFoundEntityException.class);
    }

    @Test
    @DisplayName("상품 삭제 성공 - 관리자는 다른 회원의 상품을 삭제할 수 있음")
    void deleteProductSuccess2() {
        Member member = addMemberCase();
        Product product = addProductCase(member);

        Member adminMember = memberRepository.save(new Member("admin@naver.com", "Qwer1234", Role.ADMIN, Social.NONE));

        productService.deleteProduct(product.getId(), new AuthMember(adminMember.getEmail(), adminMember.getRole()));

        assertThatThrownBy(()-> productService.findProduct(product.getId()))
                .isInstanceOf(NotFoundEntityException.class);
    }

    @Test
    @DisplayName("상품 삭제 실패 - 존재하지 않는 상품")
    void deleteProductFail() {
        assertThatThrownBy(()-> productService.findProduct(1L))
                .isInstanceOf(NotFoundEntityException.class);
    }

    @Test
    @DisplayName("상품 삭제 실패 - 자신의 상품이 아님")
    void deleteProductFail2() {
        Member member = addMemberCase();
        Product product = addProductCase(member);

        Member badMember = memberRepository.save(new Member("bad@naver.com", "Qwer1234", Role.REGULAR, Social.NONE));

        assertThatThrownBy(()->productService
                .deleteProduct(product.getId(), new AuthMember(badMember.getEmail(), badMember.getRole()))
        ).isInstanceOf(BadRequestEntityException.class);

    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProductSuccess() {
        Member member = addMemberCase();
        Product product = addProductCase(member);
        ProductUpdateRequest updateDto = new ProductUpdateRequest("스윙칩", 3500, "data:image/~base64,");

        productService.updateProduct(product.getId(), updateDto, new AuthMember(member.getEmail(), member.getRole()));

        ProductResponse response = productService.findProduct(product.getId());

        assertSoftly(softly -> {
            softly.assertThat(response.getName()).isEqualTo(updateDto.getName());
            softly.assertThat(response.getPrice()).isEqualTo(updateDto.getPrice());
            softly.assertThat(response.getImageURL()).isEqualTo(updateDto.getImageURL());
        });
    }

    @Test
    @DisplayName("상품 수정 실패 - 본인의 상품 아님")
    void updateProductFail2() {

        Member member = addMemberCase();
        Product product = addProductCase(member);

        Member badMember = memberRepository.save(new Member("bad@naver.com", "Qwer1234", Role.REGULAR, Social.NONE));

        ProductUpdateRequest updateDto = new ProductUpdateRequest("스윙칩", 3500, "data:image/~base64,");

        assertThatThrownBy(()->productService.updateProduct(product.getId(), updateDto,  new AuthMember(badMember.getEmail(), badMember.getRole())))
                .isInstanceOf(BadRequestEntityException.class);
    }

    @Test
    @DisplayName("상품 수정 실패 - 자신의 상품이 아님")
    void updateProductFail() {

        Member member = addMemberCase();
        Product product = addProductCase(member);

        ProductUpdateRequest updateDto = new ProductUpdateRequest("스윙칩", 3500, "data:image/~base64,");

        assertThatThrownBy(()->productService.updateProduct(1L, updateDto,  new AuthMember(member.getEmail(), member.getRole())))
                .isInstanceOf(NotFoundEntityException.class);
    }

    private Member addMemberCase() {
        return memberRepository.save(new Member("ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE));
    }

    private Product addProductCase(Member member) {
        OptionCreateRequest options = new OptionCreateRequest("옵션1", 10);
        Long id = productService.save(new ProductCreateRequest("스윙칩", 3000, "data:image/~base64,", List.of(options)), member.getEmail());
        return productService.findById(id);
    }
}