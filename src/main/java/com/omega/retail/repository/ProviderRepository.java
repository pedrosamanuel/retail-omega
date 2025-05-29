package com.omega.retail.repository;

import com.omega.retail.entity.Provider;
import com.omega.retail.enums.PurchaseOrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    @Query("""
    SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
    FROM Provider p
    JOIN p.products pp
    WHERE p.id = :providerId AND pp.isDefault = true
    """)
    boolean existsDefaultProvider(@Param("providerId") Long providerId);

    @Query("""
    SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
    FROM Provider p
    JOIN p.purchaseOrders po
    WHERE p.id = :providerId AND po.purchaseOrderState IN :states
    """)
    boolean existsByOrderStatus(@Param("providerId") Long providerId,
                                @Param("states") List<PurchaseOrderState> states);
}
