package gift.option.service;

import gift.domain.Option;
import gift.domain.Product;
import gift.member.dto.AuthMember;
import gift.option.dto.OptionCreateRequest;
import gift.option.dto.OptionResponse;
import gift.option.dto.OptionUpdateRequest;

import java.util.List;

public interface OptionService {

    void save(List<OptionCreateRequest> options, Product product, AuthMember authMember);

    void deleteById(AuthMember authMember, Long id);

    Option findByIdWithProduct(Long id);

    OptionResponse findById(Long id);

    List<OptionResponse> findByProduct(Product product);

    void changeQuantity(AuthMember authMember, Long id, OptionUpdateRequest optionUpdateRequest);
}
