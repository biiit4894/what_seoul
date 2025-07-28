package org.example.what_seoul.swagger.operation.description.board;

public class BoardDescription {
    public static final String CREATE_BOARD_SUCCESS = """
            후기 작성 성공
            - message : 문화행사 후기 작성 성공
            """;

    public static final String GET_BOARDS_BY_CULTURE_EVENT_ID_SUCCESS = """
            후기 목록 조회 성공
            - message : 문화행사별 문화행사 후기 목록 조회 성공
            """;

    public static final String GET_BOARD_BY_ID = """
            후기 상세 조회 성공
            - message : 문화행사 후기 조회 성공
            """;
    public static final String GET_MY_BOARDS = """
            로그인한 사용자가 작성한 문화행사 후기 목록을 조회합니다. \s
            - 문화행사가 진행된 장소 이름을 기준으로, 후기 작성일자를 기준으로 필터링이 가능합니다. \s
            - 정렬 기준은 작성일자 기준 오름차순/내림차순을 선택할 수 있습니다.
            """;

    public static final String GET_MY_BOARDS_SUCCESS = """
            작성한 후기 목록 조회 성공
            - message : 작성한 문화행사 후기 목록 조회 성공
            """;

    public static final String UPDATE_BOARD = """
            특정 문화행사 후기를 수정합니다. \s
            - 일반 유저는 본인이 작성한 후기만 수정할 수 있습니다. \s
            - 관리자는 모든 후기를 수정할 수 있습니다.
            - 후기는 1자 이상, 300자 이하로 작성해야 합니다.
            """;

    public static final String UPDATE_BOARD_SUCCESS = """
            후기 수정 성공
            - message : 문화행사 후기 수정 성공
            """;

    public static final String DELETE_BOARD = """
            특정 문화행사 후기를 삭제합니다. \s
            - 일반 유저는 본인이 작성한 후기만 삭제할 수 있습니다.
            - 관리자는 모든 후기를 삭제할 수 있습니다.
            """;

    public static final String DELETE_BOARD_SUCCESS = """
            후기 삭제 성공
            - message : 문화행사 후기 삭제 성공
            """;
}
