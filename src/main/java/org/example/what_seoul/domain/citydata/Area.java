package org.example.what_seoul.domain.citydata;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String areaCode;

    @Column(nullable = false)
    private String areaName;

    @Column(nullable = false)
    private String engName;

    public Area(String category, String areaCode, String areaName, String engName) {
        this.category = category;
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.engName = engName;
    }

}
