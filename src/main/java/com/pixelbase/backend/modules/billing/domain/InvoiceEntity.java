package com.pixelbase.backend.modules.billing.domain;

import com.pixelbase.backend.common.entity.AuditableEntity;
import com.pixelbase.backend.modules.order.domain.OrderEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 10)
    private String serie;

    @Column(length = 20)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private InvoiceType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private InvoiceStatus status;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    @Column(name = "hash_code", unique = true)
    private String hashCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
}
