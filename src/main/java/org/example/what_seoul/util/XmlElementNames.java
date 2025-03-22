package org.example.what_seoul.util;

import lombok.Getter;

/*
도시데이터 XML Element별 태크 네임을 관리하는 enum 클래스
 */

@Getter
public enum XmlElementNames {
    // 실시간 인구현황
    LIVE_PPLTN_STTS("LIVE_PPLTN_STTS", "실시간 인구현황"),
    AREA_CONGEST_LVL("AREA_CONGEST_LVL", "장소 혼잡도 지표"),
    AREA_CONGEST_MSG("AREA_CONGEST_MSG", "장소 혼잡도 지표 관련 메세지"),
    AREA_PPLTN_MIN("AREA_PPLTN_MIN", "실시간 인구 지표 최소값"),
    AREA_PPLTN_MAX("AREA_PPLTN_MAX", "실시간 인구 지표 최대값"),
    PPLTN_TIME("PPLTN_TIME", "실시간 인구 데이터 업데이트 시간"),

    // 실시간 인구현황 - 인구 예측값
    FCST_PPLTN("FCST_PPLTN", "인구 예측값"),
    FCST_TIME("FCST_TIME", "인구 예측시점"),
    FCST_CONGEST_LVL("FCST_CONGEST_LVL", "장소 예측 혼잡도 지표"),
    FCST_PPLTN_MIN("FCST_PPLTN_MIN", "예측 실시간 인구 지표 최소값"),
    FCST_PPLTN_MAX("FCST_PPLTN_MAX", "예측 실시간 인구 지표 최대값");

    private final String xmlElementName;
    private final String xmlElementNameKr;

    XmlElementNames(String xmlElementName, String xmlElementNameKr) {
        this.xmlElementName = xmlElementName;
        this.xmlElementNameKr = xmlElementNameKr;
    }

}
