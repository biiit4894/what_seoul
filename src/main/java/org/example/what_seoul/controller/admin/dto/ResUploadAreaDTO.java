package org.example.what_seoul.controller.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResUploadAreaDTO {
    @Schema(description = "업로드한 GeoJSON 파일에 포함된 전체 장소의 수", example = "50")
    private int totalCount;

    @Schema(description = "업로드 후 신규로 저장된 장소의 수", example = "30")
    private int savedCount;

    @Schema(description = "업로드 후 기존 정보가 수정된 장소의 수", example = "10")
    private int updatedCount;

    @Schema(description = "중복으로 인해 건너뛴 장소의 수", example = "10")
    private int skippedCount;
}
