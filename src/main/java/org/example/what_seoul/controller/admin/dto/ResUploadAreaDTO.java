package org.example.what_seoul.controller.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResUploadAreaDTO {
    private int totalCount;      // 전체 feature 수
    private int savedCount;      // 실제 저장된 Area 수
    private int skippedCount;    // 중복 등으로 스킵된 수
}
