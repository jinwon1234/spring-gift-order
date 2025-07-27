package gift.order.service;

import gift.domain.*;
import gift.global.exception.BadRequestEntityException;
import gift.member.dto.AuthMember;
import gift.member.service.MemberService;
import gift.oauth2.service.KakaoService;
import gift.order.dto.OrderCreateRequest;
import gift.order.dto.OrderResponse;
import gift.order.repository.OrderRepository;
import gift.wishproduct.repository.WishProductRepository;
import gift.wishproduct.service.WishProductService;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceV1Test {

    @InjectMocks
    private OrderServiceV1 orderServiceV1;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private WishProductService wishProductService;

    @Mock
    private KakaoService kakaoService;

    @Test
    @DisplayName("주문 성공")
    void createOrderSuccess() {

        // given
        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProduct wishProduct = addWishProduct(product, member, option,10);
        OrderCreateRequest dto = new OrderCreateRequest(wishProduct.getId(), 10, "메시지");

        Order order = addOrder(option,member,dto);

        given(memberService.findByEmail(member.getEmail()))
                .willReturn(member);

        given(wishProductService.findByIdWithOptionAndProduct(any()))
                .willReturn(wishProduct);

        given(orderRepository.save(any()))
                .willReturn(order);


        // when
        OrderResponse save = orderServiceV1.save(dto, new AuthMember(member.getEmail(), member.getRole()));


        // then
        assertSoftly(softly-> {
            softly.assertThat(save.message()).isEqualTo(order.getMessage());
            softly.assertThat(save.quantity()).isEqualTo(order.getQuantity());
            softly.assertThat(save.optionId()).isEqualTo(order.getOption().getId());
        });

        verify(memberService).findByEmail(member.getEmail());
        verify(wishProductService).findByIdWithOptionAndProduct(any());
        verify(orderRepository).save(any());
        verify(wishProductService).deleteById(any(), any());
        verifyNoMoreInteractions(wishProductService,orderRepository,memberService,kakaoService);
    }

    @Test
    @DisplayName("주문 실패 - 재고보다 많은 수량 주문")
    void createOrderFail() {

        // given
        Member member = addMemberCase();
        Product product = addProductCase(member);
        Option option = addOptionCase(product);
        WishProduct wishProduct = addWishProduct(product, member, option,10);
        OrderCreateRequest dto = new OrderCreateRequest(wishProduct.getId(), 10001, "메시지");

        given(memberService.findByEmail(member.getEmail()))
                .willReturn(member);

        given(wishProductService.findByIdWithOptionAndProduct(any()))
                .willReturn(wishProduct);


        // when & then
        assertThatThrownBy(()->orderServiceV1.save(
                dto, new AuthMember(member.getEmail(), member.getRole()))
        ).isInstanceOf(BadRequestEntityException.class)
                .satisfies(exception -> {
                    exception.getMessage().equals("죄송합니다. 해당 상품의 재고는 현재 " + option.getQuantity() + "개 입니다");
                });

        verify(memberService).findByEmail(member.getEmail());
        verify(wishProductService).findByIdWithOptionAndProduct(any());
        verifyNoMoreInteractions(wishProductService,orderRepository,memberService,kakaoService);
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

    private Order addOrder(Option option, Member member, OrderCreateRequest dto) {
        return new Order(dto.quantity(),dto.message(),option, member);
    }



}