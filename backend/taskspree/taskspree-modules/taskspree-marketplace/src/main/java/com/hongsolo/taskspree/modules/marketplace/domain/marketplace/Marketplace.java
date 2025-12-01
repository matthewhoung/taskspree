package com.hongsolo.taskspree.modules.marketplace.domain.marketplace;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.enums.MarketplaceStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "marketplaces", schema = "marketplace")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Marketplace extends BaseEntity {

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "logo_file_id")
    private UUID logoFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MarketplaceStatus status;

    // === Settings ===
    @Column(name = "default_task_duration_days", nullable = false)
    private Integer defaultTaskDurationDays;

    @Column(name = "auto_close_slots_percentage", nullable = false)
    private Integer autoCloseSlotsPercentage;

    @Column(name = "reservation_timeout_days", nullable = false)
    private Integer reservationTimeoutDays;

    // === Timestamps ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private Marketplace(UUID ownerId, String name, String slug) {
        this.ownerId = ownerId;
        this.name = name;
        this.slug = slug;
        this.status = MarketplaceStatus.ACTIVE;
        this.defaultTaskDurationDays = 7;
        this.autoCloseSlotsPercentage = 80;
        this.reservationTimeoutDays = 3;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Factory method to create a new Marketplace
     */
    public static Marketplace create(UUID ownerId, String name, String slug) {
        return new Marketplace(ownerId, name, slug);
    }

    /**
     * Update marketplace basic info
     */
    public void updateInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * Update marketplace settings
     */
    public void updateSettings(
            Integer defaultTaskDurationDays,
            Integer autoCloseSlotsPercentage,
            Integer reservationTimeoutDays
    ) {
        if (defaultTaskDurationDays != null) {
            this.defaultTaskDurationDays = defaultTaskDurationDays;
        }
        if (autoCloseSlotsPercentage != null) {
            this.autoCloseSlotsPercentage = autoCloseSlotsPercentage;
        }
        if (reservationTimeoutDays != null) {
            this.reservationTimeoutDays = reservationTimeoutDays;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Update logo
     */
    public void updateLogo(UUID logoFileId) {
        this.logoFileId = logoFileId;
        this.updatedAt = Instant.now();
    }

    /**
     * Archive the marketplace
     */
    public void archive() {
        this.status = MarketplaceStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    /**
     * Reactivate archived marketplace
     */
    public void reactivate() {
        this.status = MarketplaceStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Transfer ownership to another user
     */
    public void transferOwnership(UUID newOwnerId) {
        this.ownerId = newOwnerId;
        this.updatedAt = Instant.now();
    }

    /**
     * Check if marketplace is active
     */
    public boolean isActive() {
        return this.status == MarketplaceStatus.ACTIVE;
    }

    /**
     * Check if user is the owner
     */
    public boolean isOwner(UUID userId) {
        return this.ownerId.equals(userId);
    }
}