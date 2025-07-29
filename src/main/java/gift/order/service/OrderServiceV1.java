package gift.order.service;

import gift.domain.*;
import gift.member.dto.AuthMember;
import gift.member.service.MemberService;
import gift.oauth2.service.KakaoService;
import gift.option.service.OptionService;
import gift.order.dto.DirectOrderCreateRequest;
import gift.order.dto.KakaoOrderMessageTemplate;
import gift.order.dto.CartOrderCreateRequest;
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
    private final OptionService optionService;

    public OrderServiceV1(OrderRepository orderRepository, MemberService memberService, WishProductService wishProductService, KakaoService kakaoService, OptionService optionService) {
        this.orderRepository = orderRepository;
        this.memberService = memberService;
        this.wishProductService = wishProductService;
        this.kakaoService = kakaoService;
        this.optionService = optionService;
    }

    public OrderResponse saveCartOrder(CartOrderCreateRequest orderCreateRequest, AuthMember authMember) {

        Member findMember = memberService.findByEmail(authMember.getEmail());

        WishProduct wishProduct = wishProductService.findByIdWithOptionAndProduct(orderCreateRequest.wishProductId());
        Option option = wishProduct.getOption();
        Product product = wishProduct.getProduct();

        option.subtractQuantity(orderCreateRequest.quantity());

        Order save = orderRepository.save(new Order(orderCreateRequest.quantity(), orderCreateRequest.message(),
                option, findMember));

        wishProductService.deleteById(wishProduct.getId(), authMember.getEmail());

        sendKakaoMessage(findMember, product, option, orderCreateRequest.quantity(), orderCreateRequest.message());

        return new OrderResponse(save.getId(), save.getOption().getId(),
                save.getQuantity(), save.getCreatedDate(), save.getMessage());
    }

    private void sendKakaoMessage(Member findMember, Product product, Option option, int orderCreateRequest, String orderCreateRequest1) {
        if (findMember.getSocial() == Social.KAKAO) {
            kakaoService.sendOrderMessage(new KakaoOrderMessageTemplate(
                    product.getName(), option.getName(), product.getPrice(),
                    orderCreateRequest, orderCreateRequest1,
                    product.getPrice() * orderCreateRequest), findMember.getId()
            );
        }
    }

    public OrderResponse saveDirectOrder(DirectOrderCreateRequest directOrderCreateRequest, AuthMember authMember) {
        Member findMember = memberService.findByEmail(authMember.getEmail());

        Option option = optionService.findByIdWithProduct(directOrderCreateRequest.optionId());
        Product product = option.getProduct();

        option.subtractQuantity(directOrderCreateRequest.quantity());

        Order save = orderRepository.save(new Order(directOrderCreateRequest.quantity(), directOrderCreateRequest.message(),
                option, findMember));

        sendKakaoMessage(findMember, product, option, directOrderCreateRequest.quantity(), directOrderCreateRequest.message());

        return new OrderResponse(save.getId(), save.getOption().getId(),
                save.getQuantity(), save.getCreatedDate(), save.getMessage());
    }
}
