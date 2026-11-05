package com.mss301.brand.entity;

import jakarta.persistence.*;
import lombok.*;

// [TODO-5.1] Brands belong exclusively to DBBrand.
@Entity
@Table(name = "WatchBrands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BrandID")
    private Integer brandId;

    @Column(name = "BrandName", length = 200, nullable = false)
    private String brandName;

    @Column(name = "CountryOfOrigin", length = 50, nullable = false)
    private String countryOfOrigin;
}
