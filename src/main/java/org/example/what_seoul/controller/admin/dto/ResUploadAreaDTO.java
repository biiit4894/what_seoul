package org.example.what_seoul.controller.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResUploadAreaDTO {
    private int totalCount;      // 전체 feature 수 (geoJson 파일에 있는 전체 Area 수)
    private int savedCount;      // 실제 저장된 Area 수
    private int updatedCount;    // 수정된 Area 수
    private int skippedCount;    // 중복되어 스킵한 Area 수
}
