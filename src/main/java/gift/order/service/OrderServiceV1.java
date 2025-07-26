package gift.order.service;

import gift.option.dto.OptionResponse;
import gift.order.dto.OrderCreateRequest;
import gift.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OptionResponse save(OrderCreateRequest orderCreateRequest) {

    }
}
