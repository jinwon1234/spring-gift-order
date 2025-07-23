package gift.option.service;

import gift.domain.Option;
import gift.domain.Product;
import gift.global.exception.BadRequestEntityException;
import gift.global.exception.NotFoundEntityException;
import gift.member.dto.AuthMember;
import gift.member.service.MemberService;
import gift.option.dto.OptionCreateRequest;
import gift.option.dto.OptionResponse;
import gift.option.dto.OptionUpdateRequest;
import gift.option.repository.OptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class OptionServiceV1 implements OptionService{

    private final OptionRepository optionRepository;
    private final MemberService memberService;

    public OptionServiceV1(OptionRepository optionRepository, MemberService memberService) {
        this.optionRepository = optionRepository;
        this.memberService = memberService;
    }

    @Override
    public void save(List<OptionCreateRequest> options, Product product, AuthMember authMember) {

        memberService.isOwnerOrAdmin(authMember.getEmail(), product.getMember().getId());

        if (options.isEmpty()) return;

        List<String> optionNames = options.stream().map(OptionCreateRequest::optionName).toList();

        if (optionRepository.countByProductIdAndOptionNames(optionNames,product.getId()) > 0)
            throw new BadRequestEntityException("중복된 옵션 이름은 등록할 수 없습니다.");

        options.forEach(option -> {
            Option save = optionRepository.save(new Option(option.optionName(), option.quantity(), product));
            product.getOptions().add(save);
        });
    }

    @Override
    public void deleteById(AuthMember authMember, Long id) {

        Option findOption = validateIsOwner(authMember, id);

        List<Option> allOptions = findOption.getProduct().getOptions();

        if (allOptions.size() == 1){
            throw new BadRequestEntityException("상품은 항상 하나 이상의 옵션이 존재해야합니다.");
        }

        optionRepository.deleteById(id);
        allOptions.remove(findOption);
    }

    @Override
    public Option findByIdWithProduct(Long id) {

        return optionRepository.findByIdWithProduct(id)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 옵션이 아닙니다."));
    }

    @Override
    public OptionResponse findById(Long id) {

        Option option = optionRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 옵션이 아닙니다."));

        return new OptionResponse(option.getId(), option.getName(), option.getQuantity());
    }

    @Override
    public List<OptionResponse> findByProduct(Product product) {

        return optionRepository.findByProductId(product.getId())
                .stream().map(option -> new OptionResponse(option.getId(), option.getName(), option.getQuantity()))
                .toList();
    }

    @Override
    public void changeQuantity(AuthMember authMember, Long id, OptionUpdateRequest optionUpdateRequest) {

        Option option = validateIsOwner(authMember, id);

        option.changeQuantity(optionUpdateRequest.quantity());

    }

    private Option validateIsOwner(AuthMember authMember, Long id) {
        Option option = optionRepository.findByIdWithProduct(id)
                .orElseThrow(() -> new NotFoundEntityException("존재하는 옵션이 아닙니다."));

        memberService.isOwnerOrAdmin(authMember.getEmail(), option.getProduct().getMember().getId());

        return option;
    }
}
