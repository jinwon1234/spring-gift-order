package gift.product.repository;

import gift.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p from Product p left join fetch p.options where p.member.id = :memberId")
    List<Product> findByMemberIdWithOptions(Long memberId);

    @Query(
            value = "select p from Product p where p.member.id = :memberId",
            countQuery = "select count(p) from Product p")
    @EntityGraph(attributePaths = "options")
    Page<Product> findByMemberIdWithOptionsAndPage(Long memberId, Pageable pageable);

    @Query(
            value = "select p from Product p",
            countQuery = "select count(p) from Product p"
    )
    @EntityGraph(attributePaths = "options")
    Page<Product> findAllWithOptionsAndPage(Pageable pageable);

    @Query("select p from Product p join fetch p.options")
    List<Product> findAllWithOptions();

    @Query("select p from Product p left join fetch p.options where p.id = :id")
    Optional<Product> findByIdWithOptions(@Param("id") Long id);
}
