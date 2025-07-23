package gift.option.controller;

import gift.domain.Product;
import gift.member.annotation.MyAuthenticalPrincipal;
import gift.member.dto.AuthMember;
import gift.option.dto.OptionCreateListRequest;
import gift.option.dto.OptionResponse;
import gift.option.dto.OptionUpdateRequest;
import gift.option.service.OptionService;
import gift.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/options")
public class OptionController {

    private final OptionService optionService;
    private final ProductService productService;

    public OptionController(OptionService optionService, ProductService productService) {
        this.optionService = optionService;
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OptionResponse> getOptionById(@PathVariable Long id) {
        OptionResponse response = optionService.findById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOption(@MyAuthenticalPrincipal AuthMember authMember, @PathVariable Long id) {

        optionService.deleteById(authMember, id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateQuantity(@MyAuthenticalPrincipal AuthMember authMember,
                                               @RequestBody OptionUpdateRequest optionUpdateRequest,
                                               @PathVariable Long id) {

        optionService.changeQuantity(authMember,id, optionUpdateRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @PostMapping()
    public ResponseEntity<Void> addOptions(@MyAuthenticalPrincipal AuthMember authMember,
                                           @RequestBody OptionCreateListRequest optionCreateListRequest) {

        Product product = productService.findById(optionCreateListRequest.productId());

        optionService.save(optionCreateListRequest.options(), product, authMember);


        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
