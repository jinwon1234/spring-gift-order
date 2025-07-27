package gift.order.service;

import gift.domain.*;
import gift.member.dto.AuthMember;
import gift.member.service.MemberService;
import gift.oauth2.service.KakaoService;
import gift.order.dto.KakaoOrderMessageTemplate;
import gift.order.dto.OrderCreateRequest;
import gift.order.dto.OrderResponse;
import gift.order.repository.OrderRepository;
import gift.wishproduct.service.WishProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceV1 implements OrderService {

    private final OrderRepository orderRepository;
    private final MemberService memberService;
    private final WishProductService wishProductService;
    private final KakaoService kakaoService;

    public OrderServiceV1(OrderRepository orderRepository, MemberService memberService, WishProductService wishProductService, KakaoService kakaoService) {
        this.orderRepository = orderRepository;
        this.memberService = memberService;
        this.wishProductService = wishProductService;
        this.kakaoService = kakaoService;
    }

    public OrderResponse save(OrderCreateRequest orderCreateRequest, AuthMember authMember) {

        Member findMember = memberService.findByEmail(authMember.getEmail());

        WishProduct wishProduct = wishProductService.findByIdWithOptionAndProduct(orderCreateRequest.wishProductId());
        Option option = wishProduct.getOption();
        Product product = wishProduct.getProduct();

        option.subtractQuantity(orderCreateRequest.quantity());

        if (findMember.getSocial() == Social.KAKAO) {
            kakaoService.sendOrderMessage(new KakaoOrderMessageTemplate(
                    product.getName(), option.getName(), product.getPrice(),
                    orderCreateRequest.quantity(),orderCreateRequest.message(),
                    product.getPrice() * orderCreateRequest.quantity()), findMember.getId()
            );
        }

        Order save = orderRepository.save(new Order(orderCreateRequest.quantity(), orderCreateRequest.message(),
                option, findMember));

        wishProductService.deleteById(wishProduct.getId(), authMember.getEmail());

        return new OrderResponse(save.getId(), save.getOption().getId(),
                save.getQuantity(), save.getCreatedDate(), save.getMessage());
    }
}
