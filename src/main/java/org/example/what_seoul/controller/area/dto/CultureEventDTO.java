package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CultureEventDTO {

    @Schema(description = "문화행사 ID", example = "3677")
    private Long cultureEventId;

    @Schema(description = "문화행사명", example = "[서울공예박물관] 염원을 담아-실로 새겨 부처에 이르다")
    private String eventName;

    @Schema(description = "문화행사 기간", example = "2025-05-02~2025-07-27")
    private String eventPeriod;

    @Schema(description = "문화행사 장소", example = "서울공예박물관 전시1동 3층 기획전시실")
    private String eventPlace;

    @Schema(description = "문화행사 X 좌표(경도)", example = "126.983533363501")
    private String eventX;

    @Schema(description = "문화행사 Y 좌표(위도)", example = "37.5766481740232")
    private String eventY;

    @Schema(description = "문화행사 대표 이미지", example = "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=6d2674e5d5b1419c9aea5c44deaa5b15&thumb=Y")
    private String thumbnail;

    @Schema(description = "문화행사 상세정보 URL", example = "https://culture.seoul.go.kr/culture/culture/cultureEvent/view.do?cultcode=153267&menuNo=200009")
    private String url;

    @Schema(
            description = """
            문화행사 종료 여부(true/false)
            - 문화행사 기간이 현재 날짜보다 이전인 경우, 종료된 행사로 처리됩니다.
            """,
            example = "false"
    )
    private Boolean isEnded;
}
