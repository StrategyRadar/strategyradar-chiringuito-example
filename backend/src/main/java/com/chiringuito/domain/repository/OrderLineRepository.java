package com.chiringuito.domain.repository;

import com.chiringuito.domain.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, UUID> {

    List<OrderLine> findByOrderId(UUID orderId);

    Optional<OrderLine> findByOrderIdAndMenuItemId(UUID orderId, UUID menuItemId);
}