package com.pixelbase.backend.modules.user.domain;

import com.pixelbase.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address_line")
    private String addressLine;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String district;

    private String reference;

    private boolean isDefault;

    @Column(name = "contact_first_name")
    private String contactFirstName;

    @Column(name = "contact_last_name")
    private String contactLastName;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
