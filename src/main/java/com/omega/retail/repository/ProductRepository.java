package com.omega.retail.repository;

import com.omega.retail.entity.Product;
import com.omega.retail.enums.ProductState;
import com.omega.retail.enums.PurchaseOrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(nativeQuery = true, value = "SELECT count(p)>0 FROM product p WHERE :id = p.id AND p.current_stock > 0")
    boolean existsByCurrentStock(@Param("id") Long id);

    @Query(
            nativeQuery = true,
            value = """
        SELECT COUNT(*) > 0
        FROM purchase_order po
        JOIN purchase_order_detail pod ON pod.purchase_order_id = po.id
        WHERE pod.product_id = :id
        AND po.purchase_order_state IN (:states)
    """
    )
    boolean existsByActivePurchaseOrder(@Param("id") Long id, @Param("states") List<PurchaseOrderState> states);

    @Query(value = """
        SELECT *
        FROM product p
        WHERE p.product_state = 'ALTA'
          AND (
            (p.inventory_policy = 'LOTE_FIJO' AND p.fixed_lot_policy_id IS NOT NULL AND p.current_stock < (
              SELECT flp.safety_stock
              FROM fixed_lot_policy flp
              WHERE flp.id = p.fixed_lot_policy_id
            ))
            OR
            (p.inventory_policy = 'INTERVALO_FIJO' AND p.fixed_interval_policy_id IS NOT NULL AND p.current_stock < (
              SELECT fip.safety_stock
              FROM fixed_interval_policy fip
              WHERE fip.id = p.fixed_interval_policy_id
            ))
          )
     """, nativeQuery = true)
    List<Product> findBelowStockSecurity();



    @Query("""
        SELECT p
        FROM Product p
        WHERE p.currentStock < p.fixedLotPolicy.reorderPoint
          AND p.productState = 'ALTA'
          AND p.id NOT IN (
            SELECT pod.product.id
            FROM PurchaseOrderDetail pod
            JOIN pod.purchaseOrder po
            WHERE po.purchaseOrderState IN :states
          )
    """)
    List<Product> findBelowReorderPointWithoutPendingOrders(@Param("states") List<PurchaseOrderState> states);


    @Query("SELECT p FROM Product p " +
            "JOIN p.productProviders pp " +
            "WHERE pp.provider.id = :providerId " +
            "AND p.productState = 'ALTA' " +
            "AND pp.productProviderState = 'ALTA'")
    List<Product> findActiveProductsByProviderId(Long providerId);

    List<Product> findByProductState(ProductState state);

}
