package org.example.what_seoul.swagger.operation.description.area;

public class AreaDescription {
    public static final String GET_AREA_BY_LOCATION = """
        유저의 현위치 위도와 경도를 기준으로 가장 가까운 서울시 주요 장소 리스트를 조회합니다.\s
        - GPS 좌표를 기반으로 인접 장소들을 반환합니다.
        - 유저 위치를 포함하는 장소가 있다면, 해당 장소를 한 곳을 반환합니다.
        - 유저 위치를 포함하는 장소가 없다면, 가장 가까운 3개 장소를 반환합니다.
        - 반환되는 장소는 거리순으로 정렬됩니다.
        """;

    public static final String GET_AREA_LIST_BY_KEYWORD = """
            입력된 키워드에 해당하는 서울시 주요 장소들을 반환합니다. \s
            - 삭제처리 되지 않은 장소만 검색됩니다. \s
            """;

    public static final String GET_ALL_AREA_LIST = """
            서울시 주요 장소 전체 목록을 List 형태로 반환합니다.
            - 삭제처리 되지 않은 모든 장소를 조회합니다.
            """;

    public static final String GET_ALL_AREA_LIST_WITH_CONGESTION_LEVEL = """
            실시간 혼잡도 정보가 포함된 서울시 주요 장소 전체 리스트를 반환합니다.
            - 삭제처리 되지 않은 모든 장소를 조회합니다.
            """;

    public static final String GET_ALL_AREA_LIST_WITH_WEATHER = """
            실시간 날씨 정보가 포함된 서울시 주요 장소 전체 리스트를 반환합니다.
            - 삭제처리 되지 않은 모든 장소를 조회합니다.
            """;

    public static final String GET_ALL_AREAS_WITH_CULTURE_EVENT = """
            문화행사 정보가 포함된 서울시 주요 장소 전체 리스트를 반환합니다.
            - 삭제처리 되지 않은 모든 장소를 조회합니다.
            """;

    public static final String GET_AREA_NAMES_WITH_MY_BOARDS = """
            로그인한 사용자가 작성한 후기들을 기반으로,
            사용자가 후기를 작성한 이력이 있는 장소 이름 목록을 조회합니다.
            - 삭제 처리된 장소를 포함하여 모두 조회합니다.
            - 반환되는 장소명은 중복 제거된 리스트입니다.
            - 마이페이지의 작성한 후기 목록 조회 화면에서, 장소명을 선택하여 조건부로 후기를 조회하기 위해 사용하는 기능입니다.
                        
            """;
}
