package com.omega.retail.repository;

import com.omega.retail.entity.ProductProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductProviderRepository extends JpaRepository<ProductProvider, Long> {
    @Modifying
    @Query("UPDATE ProductProvider pp SET pp.isDefault = false WHERE pp.product.id = :productId")
    void unsetAllDefaultByProduct(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE ProductProvider pp SET pp.isDefault = true WHERE pp.id = :productProviderId")
    void setDefaultById(@Param("productProviderId") Long productProviderId);
}
