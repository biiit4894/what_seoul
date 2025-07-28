package org.example.what_seoul.controller.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetAreaListDTO {
    @Schema(description = "조회한 서울시 주요 장소의 ID", example = "1")
    private Long id;

    @Schema(description = "조회한 서울시 주요 장소의 카테고리 (관광특구, 고궁·문화유산, 인구밀집지역, 발달상권, 공원)", example = "인구밀집지역")
    private String category;

    @Schema(description = "조회한 서울시 주요 장소의 장소코드", example = "POI033")
    private String areaCode;

    @Schema(description = "조회한 서울시 주요 장소의 장소명", example = "서울역")
    private String areaName;

    @Schema(description = "조회한 서울시 주요 장소의 등록일자", example = "2025-03-15T18:23:17")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "조회한 서울시 주요 장소의 수정일자 (null 허용)", example = "2025-07-14T11:39:07")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "조회한 서울시 주요 장소의 삭제처리일자 (null 허용)", example = "2025-07-11T17:46:50")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;
}
