package org.example.what_seoul.controller.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetMyBoardDTO {
    @Schema(description = "나의 행사 후기 ID", example = "1")
    private Long id;

    @Schema(description = "나의 행사 후기 내용", example = "후기 내용입니다.")
    private String content;

    @Schema(description = "나의 행사 후기 작성 일자", example = "2025-07-15T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "나의 행사 후기 수정 일자 (null 허용)", example = "2025-07-15T13:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "행사 이름", example = "[서울생활문화센터 낙원] 좋은노래공작소 시리즈 1 [아낙동]")
    private String eventName;

    @Schema(description = "행사 장소", example = "서울생활문화센터 낙원 안내실(5번)")
    private String eventPlace;

    @Schema(description = "행사 상세 정보 url", example = "https://culture.seoul.go.kr/culture/culture/cultureEvent/view.do?cultcode=152540&menuNo=200011")
    private String url;

    @Schema(description = "행사가 진행된 서울시 주요 장소의 이름", example = "인사동")
    private String areaName;

    @Schema(description = "행사가 진행된 서울시 주요 장소의 삭제 처리일자 (null 허용)", example = "2025-07-11T13:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime areaDeletedAt;

    @Schema(description = "행사 종료 여부(true/false)", example = "false")
    private boolean isEnded;
}
