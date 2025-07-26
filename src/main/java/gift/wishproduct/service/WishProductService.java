package gift.wishproduct.service;

import gift.domain.WishProduct;
import gift.wishproduct.dto.WishProductCreateReq;
import gift.wishproduct.dto.WishProductResponse;
import gift.wishproduct.dto.WishProductUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WishProductService {

    Long save(WishProductCreateReq wishProductCreateReq, String email);

    List<WishProductResponse> findByEmail(String email);

    Page<WishProductResponse> findByEmailWithPage(String email, Pageable pageable);

    void deleteById(Long id, String email);

    void updateQuantity(Long id, WishProductUpdateReq wishProductUpdateReq, String email);

    WishProduct findByIdWithOption(Long id);
}
