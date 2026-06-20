package com.pixelbase.backend.modules.order.repository;

import com.pixelbase.backend.modules.order.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderCode(String orderCode);

    @Query(value = "SELECT nextval('order_code_seq')", nativeQuery = true)
    Long getNextOrderCodeSequence();
}
