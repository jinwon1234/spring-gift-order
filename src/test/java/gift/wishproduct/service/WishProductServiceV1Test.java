package gift.wishproduct.service;

import gift.domain.*;
import gift.global.exception.BadRequestEntityException;
import gift.global.exception.NotFoundEntityException;
import gift.member.service.MemberService;
import gift.option.service.OptionService;
import gift.wishproduct.dto.WishProductCreateReq;
import gift.wishproduct.dto.WishProductResponse;
import gift.wishproduct.dto.WishProductUpdateReq;
import gift.wishproduct.repository.WishProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class WishProductServiceV1Test {

    @InjectMocks
    private WishProductServiceV1 wishProductService;

    @Mock
    private WishProductRepository wishProductRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private OptionService optionService;

    @Test
    @DisplayName("위시 상품 추가 성공 - 새로운 위시 상품")
    void addWishProductSuccess() {

        // given
        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProductCreateReq dto = new WishProductCreateReq(product.getId(), option.getId(), 10);
        WishProduct wishProduct = addWishProduct(product, member, option,dto.getQuantity());


        given(optionService.findByIdWithProduct(option.getId()))
            .willReturn(option);

        given(memberService.findByEmail(member.getEmail()))
                .willReturn(member);

        given(wishProductRepository.findByOwnerIdAndOptionId(member.getId(), option.getId()))
                .willReturn(Optional.empty());

        given(wishProductRepository.save(any(WishProduct.class)))
                .willReturn(wishProduct);


        // when
        Long savedId = wishProductService.save(dto, member.getEmail());

        // then
        assertThat(savedId).isEqualTo(wishProduct.getId());
        verify(wishProductRepository).save(any(WishProduct.class));
        verify(optionService).findByIdWithProduct(option.getId());
        verify(memberService).findByEmail(member.getEmail());
        verify(wishProductRepository).findByOwnerIdAndOptionId(member.getId(), option.getId());
        verifyNoMoreInteractions(wishProductRepository, optionService, memberService);
    }

    @Test
    @DisplayName("위시 상품 추가 성공 - 기존 위시 상품에 수량 추가")
    void updateWishProductSuccess() {
        // given
        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProductCreateReq dto = new WishProductCreateReq(product.getId(), option.getId(), 10);
        WishProduct wishProduct = addWishProduct(product, member, option,15);

        given(optionService.findByIdWithProduct(option.getId()))
                .willReturn(option);

        given(memberService.findByEmail(member.getEmail()))
                .willReturn(member);

        given(wishProductRepository.findByOwnerIdAndOptionId(member.getId(), option.getId()))
                .willReturn(Optional.of(wishProduct));

        // when
        Long updatedId = wishProductService.save(dto, member.getEmail());

        // then
        assertThat(updatedId).isEqualTo(wishProduct.getId());
        verify(optionService).findByIdWithProduct(option.getId());
        verify(memberService).findByEmail(member.getEmail());
        verify(wishProductRepository).findByOwnerIdAndOptionId(member.getId(), option.getId());
        verifyNoMoreInteractions(wishProductRepository, optionService, memberService);
    }

    @Test
    @DisplayName("위시 상품 추가 실패 - 존재하지 않는 회원")
    void addWishProductFail() {
        // given
        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProductCreateReq dto = new WishProductCreateReq(product.getId(),option.getId(), 10);


        given(optionService.findByIdWithProduct(option.getId()))
                .willReturn(option);

        given(memberService.findByEmail(member.getEmail()))
                .willThrow(new NotFoundEntityException("존재하지 않는 회원입니다."));

        // when & then
        assertThatThrownBy(() -> wishProductService.save(dto, member.getEmail()))
                .isInstanceOf(NotFoundEntityException.class);
        verify(optionService).findByIdWithProduct(option.getId());
        verify(memberService).findByEmail(member.getEmail());
        verifyNoMoreInteractions(wishProductRepository, optionService, memberService);

    }

    @Test
    @DisplayName("위시 상품 추가 실패 - 존재하지 않는 옵션")
    void addWishProductFail2() {
        // given
        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProductCreateReq dto = new WishProductCreateReq(product.getId(),option.getId(), 10);


        given(optionService.findByIdWithProduct(option.getId()))
                .willThrow(new NotFoundEntityException("존재하지 않는 옵션입니다."));


        // when & then
        assertThatThrownBy(() -> wishProductService.save(dto, member.getEmail()))
                .isInstanceOf(NotFoundEntityException.class);
        verify(optionService).findByIdWithProduct(option.getId());
        verifyNoMoreInteractions(wishProductRepository, optionService, memberService);

    }

    @Test
    @DisplayName("위시 상품 조회")
    void getWishProductSuccess() {
        // given
        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProduct wishProduct = addWishProduct(product, member, option, 15);

        given(memberService.findByEmail(member.getEmail()))
                .willReturn(member);

        given(wishProductRepository.findByOwnerIdWithFetch(member.getId()))
                .willReturn(List.of(wishProduct));

        // when
        List<WishProductResponse> result = wishProductService.findByEmail(member.getEmail());

        // then
        assertThat(result.size()).isEqualTo(1);
        verify(memberService).findByEmail(member.getEmail());
        verify(wishProductRepository).findByOwnerIdWithFetch(member.getId());
        verifyNoMoreInteractions(wishProductRepository, memberService, optionService);

    }

    @Test
    @DisplayName("위시 상품 삭제 성공")
    void deleteWishProductSuccess() {
        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProduct wishProduct = addWishProduct(product, member, option,15);

        // given

        given(wishProductRepository.findById(wishProduct.getId()))
                .willReturn(Optional.of(wishProduct));

        given(memberService.findByEmail(member.getEmail()))
                .willReturn(member);

        // when
        wishProductService.deleteById(wishProduct.getId(), member.getEmail());


        // then
        verify(memberService).findByEmail(member.getEmail());
        verify(wishProductRepository).findById(wishProduct.getId());
        verify(wishProductRepository).deleteById(wishProduct.getId());
        verifyNoMoreInteractions(wishProductRepository, memberService, optionService);
    }

    @Test
    @DisplayName("위시 상품 삭제 성공 - 자신의 위시 상품이 아님")
    void deleteWishProductFail() {
        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProduct wishProduct = addWishProduct(product, member, option,15);

        // given

        given(wishProductRepository.findById(wishProduct.getId()))
                .willReturn(Optional.of(wishProduct));

        given(memberService.findByEmail(member.getEmail()))
                .willReturn(new Member(2L, "temp@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE));

        // when
        assertThatThrownBy(()->wishProductService.deleteById(wishProduct.getId(), member.getEmail()))
                .isInstanceOf(BadRequestEntityException.class);


        // then
        verify(memberService).findByEmail(member.getEmail());
        verify(wishProductRepository).findById(wishProduct.getId());
        verifyNoMoreInteractions(wishProductRepository, memberService, optionService);
    }

    @Test
    @DisplayName("위시 상품 수량 수정 성공")
    void changeQuantitySuccess() {
        // given

        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProduct wishProduct = addWishProduct(product, member, option,15);

        given(wishProductRepository.findById(wishProduct.getId()))
                .willReturn(Optional.of(wishProduct));

        given(memberService.findByEmail(member.getEmail()))
                .willReturn(member);

        // when

        wishProductService.updateQuantity(wishProduct.getId(), new WishProductUpdateReq(10), member.getEmail());


        // then

        verify(wishProductRepository).findById(wishProduct.getId());
        verify(memberService).findByEmail(member.getEmail());
        verifyNoMoreInteractions(wishProductRepository, memberService, optionService);
    }

    @Test
    @DisplayName("위시 상품 수량 수정 실패 - 자신의 위시 상품이 아님")
    void changeQuantityFail() {
        // given

        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProduct wishProduct = addWishProduct(product, member, option, 15);

        given(wishProductRepository.findById(wishProduct.getId()))
                .willReturn(Optional.of(wishProduct));

        given(memberService.findByEmail(anyString()))
                .willReturn(new Member(2L, "temp@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE));

        // when

        assertThatThrownBy(()->wishProductService.updateQuantity(wishProduct.getId(),
                new WishProductUpdateReq(10), member.getEmail())
        ).isInstanceOf(BadRequestEntityException.class);


        // then

        verify(wishProductRepository).findById(wishProduct.getId());
        verify(memberService).findByEmail(member.getEmail());
        verifyNoMoreInteractions(wishProductRepository, memberService, optionService);
    }



    private Member addMemberCase() {
        return new Member(1L, "ljw2109@naver.com", "Qwer1234!!", Role.REGULAR, Social.NONE);
    }

    private Product addProductCase(Member member) {
        return new Product(1L,"스윙칩",3000, "data:image/~base64,",member);
    }

    private Option addOptionCase(Product product) {
        return new Option(1L, "옵션1",1000, product);
    }
    private WishProduct addWishProduct(Product product, Member member,Option option, int quantity) {
        return new WishProduct(1L, quantity,
                member, product,option);
    }



}