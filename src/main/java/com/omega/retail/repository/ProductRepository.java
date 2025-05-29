package com.omega.retail.repository;

import com.omega.retail.entity.Product;
import com.omega.retail.enums.PurchaseOrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(nativeQuery = true, value = "SELECT count(p)>0 FROM product p WHERE :id = p.id AND p.current_stock > 0")
    boolean existsByCurrentStock(@Param("id") Long id);

    @Query(
            nativeQuery = true,
            value = """
        SELECT COUNT(*) = 0
        FROM purchase_order po
        JOIN purchase_order_detail pod ON pod.purchase_order_id = po.id
        WHERE pod.product_id = :id
        AND po.state IN (:states)
    """
    )
    boolean existsByActivePurchaseOrder(@Param("id") Long id, @Param("states") List<PurchaseOrderState> states);
}
