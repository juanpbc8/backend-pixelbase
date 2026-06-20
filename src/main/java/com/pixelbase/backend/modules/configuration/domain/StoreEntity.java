package com.pixelbase.backend.modules.configuration.domain;

import com.pixelbase.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(length = 100, nullable = false)
    private String department;

    @Column(length = 100, nullable = false)
    private String province;

    @Column(length = 100, nullable = false)
    private String district;

    @Column(nullable = false)
    private boolean active;
}
