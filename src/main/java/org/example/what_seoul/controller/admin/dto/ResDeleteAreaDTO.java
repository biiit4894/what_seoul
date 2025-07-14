package org.example.what_seoul.controller.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.Area;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResDeleteAreaDTO {
    private Long id;
    private String category;
    private String areaCode;
    private String areaName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    public ResDeleteAreaDTO(Area area) {
        this.id = area.getId();
        this.category = area.getCategory();
        this.areaCode = area.getAreaCode();
        this.areaName = area.getAreaName();
        this.createdAt = area.getCreatedAt();
        this.updatedAt = area.getUpdatedAt();
        this.deletedAt = area.getDeletedAt();
    }

    public static ResDeleteAreaDTO from(Area area) {
        return new ResDeleteAreaDTO(area);
    }


}
