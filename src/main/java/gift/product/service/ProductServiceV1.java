package gift.product.service;

import gift.domain.Member;
import gift.domain.Product;
import gift.global.exception.NotFoundEntityException;
import gift.member.dto.AuthMember;
import gift.member.service.MemberService;
import gift.option.dto.OptionResponse;
import gift.option.service.OptionService;
import gift.product.dto.ProductCreateRequest;
import gift.product.dto.ProductResponse;
import gift.product.dto.ProductUpdateRequest;
import gift.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class ProductServiceV1 implements ProductService{

    private final ProductRepository productRepository;
    private final MemberService memberService;
    private final OptionService optionService;

    public ProductServiceV1(ProductRepository productRepository, MemberService memberService, OptionService optionService) {
        this.productRepository = productRepository;
        this.memberService = memberService;
        this.optionService = optionService;
    }


    public Long save(ProductCreateRequest dto, String email) {
        Member findMember = memberService.findByEmail(email);
        Product save = productRepository.save(new Product(dto.getName(), dto.getPrice(), dto.getImageURL(), findMember));

        optionService.save(dto.getOptions(), save, new AuthMember(findMember.getEmail(), findMember.getRole()));

        return save.getId();
    }

    public List<ProductResponse> findAllProducts() {
        return productRepository.findAll()
                .stream().map(p-> new ProductResponse(p, p.getOptions()
                        .stream().map(o->new OptionResponse(o.getId(), o.getName(), o.getQuantity()))
                        .toList()))
                .toList();
    }

    @Override
    public Page<ProductResponse> findAllProductsWithPage(Pageable pageable) {
        Page<Product> products = productRepository.findAllWithPage(pageable);

        return products.map(
                p-> new ProductResponse(p, p.getOptions()
                        .stream().map(o->new OptionResponse(o.getId(), o.getName(), o.getQuantity()))
                        .toList())
        );
    }


    public ProductResponse findProduct(Long id) {
        Product findProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException("상품이 존재하지 않습니다."));
        return new ProductResponse(findProduct, findProduct.getOptions()
                .stream().map(o->new OptionResponse(o.getId(), o.getName(), o.getQuantity()))
                .toList()
        );
    }

    public void deleteProduct(Long id, AuthMember authMember) {

        Product findProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException("상품이 존재하지 않습니다."));


        memberService.isOwnerOrAdmin(authMember.getEmail(),findProduct.getMember().getId());

        productRepository.deleteById(id);
    }

    public void updateProduct(Long id, ProductUpdateRequest dto, AuthMember authMember) {


        Product findProduct = productRepository.findByIdWithOptions(id)
                .orElseThrow(() -> new NotFoundEntityException("상품이 존재하지 않습니다."));

        memberService.isOwnerOrAdmin(authMember.getEmail(), findProduct.getMember().getId());

        findProduct.changeName(dto.getName());
        findProduct.changePrice(dto.getPrice());
        findProduct.changeImageUrl(dto.getImageURL());
    }

    @Override
    public List<ProductResponse> findByEmail(AuthMember authMember) {
        Member findMember = memberService.findByEmail(authMember.getEmail());

       return productRepository.findByMemberIdWithOptions(findMember.getId())
                .stream().map(p->new ProductResponse(p, p.getOptions()
                       .stream().map(o->new OptionResponse(o.getId(),o.getName(),o.getQuantity()))
                       .toList()
                       )
               ).toList();
    }

    @Override
    public Page<ProductResponse> findByEmailWithPage(AuthMember authMember, Pageable pageable) {
        Member findMember = memberService.findByEmail(authMember.getEmail());

        return productRepository.findByMemberIdWithPage(findMember.getId(), pageable)
                .map(p -> new ProductResponse(p, p.getOptions()
                        .stream().map(o -> new OptionResponse(o.getId(), o.getName(), o.getQuantity()))
                        .toList())
                );
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(()->new NotFoundEntityException("존재하는 상품이 아닙니다"));
    }

    @Override
    public List<OptionResponse> findAllOptions(AuthMember authMember, Long id) {

        Product product = productRepository.findByIdWithOptions(id)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 상품이 아닙니다"));
        memberService.isOwnerOrAdmin(authMember.getEmail(), product.getMember().getId());

        return product.getOptions().stream().map(o->new OptionResponse(o.getId(),o.getName(),o.getQuantity()))
                .toList();
    }
}
