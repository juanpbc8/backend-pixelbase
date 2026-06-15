package com.pixelbase.backend.modules.user.domain;

import com.pixelbase.backend.common.entity.AuditableEntity;
import com.pixelbase.backend.common.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 10)
    private DocumentType documentType;

    @Column(name = "document_number", length = 20)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
