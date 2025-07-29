package gift.wishproduct.repository;

import gift.domain.WishProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WishProductRepository extends JpaRepository<WishProduct, Long> {


    @Query("select w from WishProduct w where w.option.id = :optionId and w.owner.id = :ownerId")
    Optional<WishProduct> findByOwnerIdAndOptionId(Long ownerId, Long optionId);

    @EntityGraph(attributePaths = {"product", "option"})
    @Query("select w from WishProduct w where w.owner.id = :ownerId")
    List<WishProduct> findByOwnerIdWithFetch(Long ownerId);

    @EntityGraph(attributePaths = {"product", "option"})
    @Query(value = "select w from WishProduct w  where w.owner.id = :ownerId",
            countQuery = "select count(w.id) from WishProduct w where w.owner.id = :ownerId")
    Page<WishProduct> findByOwnerIdWithPageAndFetch(Long ownerId, Pageable pageable);

    @Query("select w from WishProduct w join fetch w.option join fetch w.product where w.id = :id")
    Optional<WishProduct> findByIdFetchAll(Long id);
}
