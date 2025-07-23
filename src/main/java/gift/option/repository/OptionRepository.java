package gift.option.repository;

import gift.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OptionRepository extends JpaRepository<Option, Long> {


    @Query("select o from Option o join fetch o.product where o.id = :id")
    Optional<Option> findByIdWithProduct(Long id);

    @Query("select o from Option o where o.product.id = :productId")
    List<Option> findByProductId(Long productId);

    @Query("select count(o) from Option o where o.product.id = :productId and o.name in (:optionNames)")
    long countByProductIdAndOptionNames(List<String> optionNames, Long productId);
}
