package org.example.what_seoul.swagger.operation.description.admin;

public class AdminDescription {
    public static final String SIGNUP = """
            관리자 계정을 생성합니다. \s
            - accessToken 쿠키를 통해 관리자 권한을 확인합니다. \s
            - userId, email, nickName 중복 및 DTO 유효성 검사를 수행합니다.
            """;

    public static final String SIGNUP_SUCCESS = """
            관리자 계정 생성 성공\s
            - message : 관리자 계정 생성 성공
            """;

    public static final String LOGIN = """
            관리자 계정으로 로그인합니다. \s
            - 로그인 성공 시 AccessToken, RefreshToken을 HttpOnly 쿠키로 전달합니다.
            """;

    public static final String LOGIN_SUCCESS = """
            관리자 로그인 성공\s
            - message : 관리자 로그인 성공
            """;

    public static final String GET_AREA_LIST = """
            서울시 주요 장소 목록을 페이지 단위로 조회합니다. \s
            - 검색어(`areaName`)는 요청 바디로 전달됩니다 (선택값). \s
            - 결과는 Slice 형태로 반환됩니다.
            """;

    public static final String GET_AREA_LIST_SUCCESS = """
            장소 목록 조회 성공
            - message : 서울시 주요 장소 목록 조회 성공
            """;

    public static final String UPLOAD_AREA = """
            관리자가 .zip 형식의 Shapefile 데이터를 업로드하면, \s
            Python 스크립트를 통해 GeoJSON으로 변환 후 장소 정보를 저장합니다. \s
            저장된 항목 수, 수정된 항목 수, 중복으로 건너뛴 항목 수 등을 응답합니다.\s
            - 서울시 공공데이터 API에서 제공하는 서울시 주요 장소 정보가 변경되는 경우,\s
            - 해당 변경 사항에 맞추어 WhatSeoul 서비스의 장소 데이터를 갱신하기 위한 기능입니다.\s
            """;

    public static final String UPLOAD_AREA_SUCCESS = """
            업로드 및 저장 성공\s
            - message : 서울시 주요 장소 정보 업로드 성공
            """;

    public static final String DELETE_AREAS = """
            전달받은 ID 목록에 해당하는 장소들을 삭제 처리합니다. \s
            실제로는 deletedAt을 업데이트하는 soft delete 방식입니다.\s
            - 서울시 공공데이터 API에서, 서울시 주요 장소 목록 중 일부 장소를 제거하여 더 이상 실시간 도시데이터를 제공하지 않는 경우,\s
            - 해당 변경 사항에 맞추어 WhatSeoul의 장소 데이터를 삭제 처리하기 위한 기능입니다.\s
            """;

    public static final String DELETE_AREAS_SUCCESS = """
            삭제 처리 성공\s
            - message : 서울시 주요 장소 정보 삭제 처리 성공
            """;
}
