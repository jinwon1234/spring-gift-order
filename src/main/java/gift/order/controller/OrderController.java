package gift.order.controller;

import gift.member.annotation.MyAuthenticalPrincipal;
import gift.member.dto.AuthMember;
import gift.order.dto.OrderCreateRequest;
import gift.order.dto.OrderResponse;
import gift.order.service.OrderService;
import gift.util.LocationGenerator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping()
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest orderCreateRequest,
                                                    @MyAuthenticalPrincipal AuthMember authMember) {

        OrderResponse save = orderService.save(orderCreateRequest, authMember);


        return ResponseEntity.status(HttpStatus.CREATED).
                location(LocationGenerator.generate(save.id())).body(save);
    }
}
