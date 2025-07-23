package gift.product.controller;

import gift.member.annotation.MyAuthenticalPrincipal;
import gift.member.dto.AuthMember;
import gift.option.dto.OptionResponse;
import gift.product.dto.ProductCreateRequest;
import gift.product.dto.ProductResponse;
import gift.product.dto.ProductUpdateRequest;
import gift.product.service.ProductService;
import gift.util.LocationGenerator;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Void> addProduct(@Valid @RequestBody ProductCreateRequest dto, @MyAuthenticalPrincipal AuthMember authMember) {

        Long savedId = productService.save(dto, authMember.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).location(
                LocationGenerator.generate(savedId)
        ).build();
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(@PageableDefault(page = 0, size = 10,sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<ProductResponse> response = productService.findAllProductsWithPage(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<ProductResponse>> getMyProducts(@MyAuthenticalPrincipal AuthMember authMember,
                                                               @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<ProductResponse> response = productService.findByEmailWithPage(authMember, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {

        ProductResponse response = productService.findProduct(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @MyAuthenticalPrincipal AuthMember authMember) {
        productService.deleteProduct(id, authMember);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest dto,
                                              @MyAuthenticalPrincipal AuthMember authMember) {

        productService.updateProduct(id, dto, authMember);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/options")
    public ResponseEntity<List<OptionResponse>> getOptions(@MyAuthenticalPrincipal AuthMember authMember, @PathVariable Long id) {

        List<OptionResponse> response = productService.findAllOptions(authMember, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
