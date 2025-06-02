package com.omega.retail.repository;

import com.omega.retail.entity.PurchaseOrder;
import com.omega.retail.enums.PurchaseOrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
    @Query("""
        SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END
        FROM PurchaseOrder po
        JOIN po.details d
        WHERE d.product.id = :productId
          AND po.purchaseOrderState IN :states
    """)
    boolean existsActiveByProductId(@Param("productId") Long productId ,@Param("states") List<PurchaseOrderState> states);

    Optional<PurchaseOrder> findFirstByProviderIdAndPurchaseOrderState(Long providerId, PurchaseOrderState state);

}
