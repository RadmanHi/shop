package com.radman.shop.product.model.dao;

import com.radman.shop.product.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductDao extends JpaRepository<Product, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select p
                from Product p
                where p.id in :ids
            """)
    List<Product> findAllForUpdate(@Param("ids") Collection<String> ids);
}