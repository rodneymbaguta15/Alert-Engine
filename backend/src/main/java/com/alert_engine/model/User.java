package com.alert_engine.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Trader account.
 * Password and full auth fields become required in Phase 7 with JWT.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // Nullable for now; required once JWT/auth is wired up in Phase 7.
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    // Managed by DB DEFAULT and trigger; insertable/updatable=false so Hibernate never writes them.
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;
}