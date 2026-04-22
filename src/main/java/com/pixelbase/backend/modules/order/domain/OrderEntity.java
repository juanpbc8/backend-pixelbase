package com.pixelbase.backend.modules.order.domain;

import com.pixelbase.backend.common.entity.AuditableEntity;
import com.pixelbase.backend.modules.user.domain.DocumentType;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private String notes;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_first_name")
    private String customerFirstName;

    @Column(name = "customer_last_name")
    private String customerLastName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_doc_type")
    private DocumentType customerDocumentType;

    @Column(name = "customer_doc_number")
    private String customerDocumentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
