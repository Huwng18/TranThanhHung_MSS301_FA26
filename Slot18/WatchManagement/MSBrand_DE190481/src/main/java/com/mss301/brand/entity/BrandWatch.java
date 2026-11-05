package com.mss301.brand.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// [TODO-5.1] WatchID is a remote identifier, never a cross-database foreign key.
@Entity
@Table(name = "BrandWatches", uniqueConstraints =
    @UniqueConstraint(name = "UQ_BrandWatches_WatchID", columnNames = "WatchID"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandWatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "WatchID", nullable = false)
    private Integer watchId;

    @Column(name = "BrandID", nullable = false)
    private Integer brandId;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
