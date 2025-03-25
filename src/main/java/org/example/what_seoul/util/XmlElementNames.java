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
    FCST_PPLTN_MAX("FCST_PPLTN_MAX", "예측 실시간 인구 지표 최대값"),

    // 날씨 현황
    WEATHER_STTS("WEATHER_STTS", "날씨 현황"),
    TEMP("TEMP", "기온"),
    MAX_TEMP("MAX_TEMP", "일 최고온도"),
    MIN_TEMP("MIN_TEMP", "일 최저온도"),
    PM25_INDEX("PM25_INDEX", "초미세먼지지표"),
    PM25("PM25", "초미세먼지농도"),
    PM10_INDEX("PM10_INDEX", "미세먼지지표"),
    PM10("PM10", "미세먼지농도"),
    PCP_MSG("PCP_MSG", "강수관련 메세지"),
    WEATHER_TIME("WEATHER_TIME", "날씨데이터 업데이트 시간"),

    // 문화행사 현황
    EVENT_STTS("EVENT_STTS", "문화행사 현황"),
    EVENT_NM("EVENT_NM", "문화행사명"),
    EVENT_PERIOD("EVENT_PERIOD", "문화행사 기간"),
    EVENT_PLACE("EVENT_PLACE", "문화행사 장소"),
    EVENT_X("EVENT_X", "문화행사 X 좌표(경도)"),
    EVENT_Y("EVENT_Y", "문화행사 Y 좌표(위도)"),
    THUMBNAIL("THUMBNAIL", "문화행사 대표 이미지"),
    URL("URL", "문화행사 상세정보 URL");

    private final String xmlElementName;
    private final String xmlElementNameKr;

    XmlElementNames(String xmlElementName, String xmlElementNameKr) {
        this.xmlElementName = xmlElementName;
        this.xmlElementNameKr = xmlElementNameKr;
    }

}
