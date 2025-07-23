package gift.product.service;


import gift.domain.Product;
import gift.member.dto.AuthMember;
import gift.option.dto.OptionResponse;
import gift.product.dto.ProductCreateRequest;
import gift.product.dto.ProductResponse;
import gift.product.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Long save(ProductCreateRequest dto, String email);
    List<ProductResponse> findAllProducts();
    Page<ProductResponse> findAllProductsWithPage(Pageable pageable);
    ProductResponse findProduct(Long id);
    void deleteProduct(Long id, AuthMember authMember);
    void updateProduct(Long id, ProductUpdateRequest dto, AuthMember authMember);
    List<ProductResponse> findByEmail(AuthMember authMember);
    Page<ProductResponse> findByEmailWithPage(AuthMember authMember, Pageable pageable);
    Product findById(Long id);
    List<OptionResponse> findAllOptions(AuthMember authMember, Long id);
}
