package gift.wishproduct.service;

import gift.domain.Member;
import gift.domain.Option;
import gift.domain.Product;
import gift.domain.WishProduct;
import gift.global.exception.BadRequestEntityException;
import gift.global.exception.NotFoundEntityException;
import gift.member.service.MemberService;
import gift.option.service.OptionService;
import gift.wishproduct.dto.WishProductCreateReq;
import gift.wishproduct.dto.WishProductResponse;
import gift.wishproduct.dto.WishProductUpdateReq;
import gift.wishproduct.repository.WishProductRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WishProductServiceV1 implements WishProductService {

    private final WishProductRepository wishProductRepository;
    private final MemberService memberService;
    private final OptionService optionService;

    public WishProductServiceV1(WishProductRepository wishProductRepository, MemberService memberService, EntityManager em, OptionService optionService) {
        this.wishProductRepository = wishProductRepository;
        this.memberService = memberService;
        this.optionService = optionService;
    }


    @Override
    public Long save(WishProductCreateReq dto, String email) {

        Option option = optionService.findByIdWithProduct(dto.getOptionId());

        Product product = option.getProduct();

        if (!product.getId().equals(dto.getProductId()))
            throw new BadRequestEntityException("상품과 옵션이 정보가 일치하지 않습니다.");

        Member owner = memberService.findByEmail(email);

        WishProduct wishProduct = wishProductRepository.findByOwnerIdAndOptionId(owner.getId(), option.getId())
                .orElse(null);

        if (wishProduct == null) {
            WishProduct saved = wishProductRepository.save(new WishProduct(dto.getQuantity(), owner, product, option));

            return saved.getId();
        }

        wishProduct.changeQuantity(dto.getQuantity() + wishProduct.getQuantity());
        return wishProduct.getId();
    }

    @Override
    public List<WishProductResponse> findByEmail(String email) {

        Member owner = memberService.findByEmail(email);

        return wishProductRepository.findByOwnerIdWithFetch(owner.getId())
                .stream().map(WishProductResponse::new)
                .toList();
    }

    @Override
    public Page<WishProductResponse> findByEmailWithPage(String email, Pageable pageable) {
        Member owner = memberService.findByEmail(email);

        return wishProductRepository.findByOwnerIdWithPageAndFetch(owner.getId(), pageable)
                .map(WishProductResponse::new);
    }

    @Override
    public void deleteById(Long id, String email) {

        WishProduct wishProduct = wishProductRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException("존재하지 않는 위시 상품입니다."));

        Member owner = memberService.findByEmail(email);

        if (!owner.getId().equals(wishProduct.getOwner().getId()))
            throw new BadRequestEntityException("자신의 위시 상품만 삭제할 수 있습니다");

        wishProductRepository.deleteById(wishProduct.getId());
    }

    @Override
    public void updateQuantity(Long id, WishProductUpdateReq dto, String email) {

        WishProduct wishProduct = wishProductRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException("존재하지 않는 위시 상품입니다."));

        Member owner = memberService.findByEmail(email);

        if (!owner.getId().equals(wishProduct.getOwner().getId()))
            throw new BadRequestEntityException("자신의 위시 상품만 수정할 수 있습니다");

        wishProduct.changeQuantity(dto.getQuantity());

    }


}
