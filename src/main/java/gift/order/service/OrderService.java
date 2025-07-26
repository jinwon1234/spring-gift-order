package gift.order.service;

import gift.member.dto.AuthMember;
import gift.order.dto.OrderCreateRequest;
import gift.order.dto.OrderResponse;

public interface OrderService {

    OrderResponse save(OrderCreateRequest orderCreateRequest, AuthMember authMember);
}
