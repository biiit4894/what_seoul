package org.example.what_seoul.controller.citydata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.event.CultureEvent;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetCultureEventDataDTO {
    @Schema(description = "문화행사 현황 데이터 ID", example = "3594")
    private Long id;

    @Schema(description = "문화행사명", example = "[DDP] 2025 DDP 봄축제 [어린이투어]")
    private String eventName;

    @Schema(description = "문화행사 기간", example = "2025-05-03~2025-07-31")
    private String eventPeriod;

    @Schema(description = "문화행사 장소", example = "DDP 전역")
    private String eventPlace;

    @Schema(description = "문화행사 X 좌표(경도)", example = "127.00977973484339")
    private String eventX;

    @Schema(description = "문화행사 Y 좌표(위도)", example = "37.56735731522952")
    private String eventY;

    @Schema(description = "문화행사 대표 이미지", example = "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=d80fc1e9a01a42d48878e8fc9b7b1e12&thumb=Y")
    private String thumbnail;

    @Schema(description = "문화행사 상세정보 URL", example = "https://culture.seoul.go.kr/culture/culture/cultureEvent/view.do?cultcode=153369&menuNo=200011")
    private String url;

    @Schema(description = "문화행사 종료 여부", example = "false")
    private Boolean isEnded;

    @Schema(description = "관련 장소 ID", example = "2")
    private Long areaId;

    public ResGetCultureEventDataDTO(CultureEvent cultureEvent) {
        this.id = cultureEvent.getId();
        this.eventName = cultureEvent.getEventName();
        this.eventPeriod = cultureEvent.getEventPeriod();
        this.eventPlace = cultureEvent.getEventPlace();
        this.eventX = cultureEvent.getEventX();
        this.eventY = cultureEvent.getEventY();
        this.thumbnail = cultureEvent.getThumbnail();
        this.url = cultureEvent.getUrl();
        this.isEnded = cultureEvent.getIsEnded();
        this.areaId = cultureEvent.getArea().getId();
    }

    public static List<ResGetCultureEventDataDTO> from(List<CultureEvent> cultureEventList) {
        return cultureEventList.stream()
                .map(ResGetCultureEventDataDTO::new)
                .collect(Collectors.toList());
    }
}
