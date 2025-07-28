package gift.order.service;

import gift.member.dto.AuthMember;
import gift.order.dto.CartOrderCreateRequest;
import gift.order.dto.DirectOrderCreateRequest;
import gift.order.dto.OrderResponse;

public interface OrderService {

    OrderResponse saveCartOrder(CartOrderCreateRequest orderCreateRequest, AuthMember authMember);

    OrderResponse saveDirectOrder(DirectOrderCreateRequest directOrderCreateRequest, AuthMember authMember);
}
