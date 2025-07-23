package gift.wishproduct.controller;


import gift.member.annotation.MyAuthenticalPrincipal;
import gift.member.dto.AuthMember;
import gift.util.LocationGenerator;
import gift.wishproduct.dto.WishProductCreateReq;
import gift.wishproduct.dto.WishProductResponse;
import gift.wishproduct.dto.WishProductUpdateReq;
import gift.wishproduct.service.WishProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wish-products")
public class WishProductController {

    private final WishProductService wishProductService;

    public WishProductController(WishProductService wishProductService) {
        this.wishProductService = wishProductService;
    }

    @PostMapping()
    public ResponseEntity<Void> addWishProduct(@MyAuthenticalPrincipal AuthMember authMember,
            @Valid @RequestBody WishProductCreateReq wishProductCreateReq) {

        Long savedId = wishProductService.save(wishProductCreateReq, authMember.getEmail());


        return ResponseEntity.status(HttpStatus.CREATED).
                location(LocationGenerator.generate(savedId))
                .build();
    }

    @GetMapping()
    public ResponseEntity<Page<WishProductResponse>> getWishList(@MyAuthenticalPrincipal AuthMember authMember,
                                                                 @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<WishProductResponse> response = wishProductService.findByEmailWithPage(authMember.getEmail(), pageable);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWishProduct(@MyAuthenticalPrincipal AuthMember authMember, @PathVariable Long id) {

        wishProductService.deleteById(id, authMember.getEmail());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateWishProduct(@MyAuthenticalPrincipal AuthMember authMember, @PathVariable Long id,
                                                  @Valid @RequestBody WishProductUpdateReq wishProductUpdateReq) {

        wishProductService.updateQuantity(id, wishProductUpdateReq, authMember.getEmail());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
