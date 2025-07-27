package org.example.what_seoul.swagger.operation.description.user;

public class UserDescription {
    public static final String LOGIN = """
            일반 유저 계정으로 로그인합니다. \s
            - 로그인 성공 시 AccessToken, RefreshToken을 HttpOnly 쿠키로 전달합니다.
            """;

    public static final String UPDATE_USER_INFO = """
            회원 정보를 수정합니다. 아래의 조건을 충족해야 수정 가능합니다.\s
            - 1. 회원 정보 필드별 유효성 검사를 통과해야 합니다. \s
            - 2. 로그인한 계정의 현재 비밀번호를 정확히 입력합니다. \s
            - 3. 비밀번호/이메일/닉네임 중 최소 한 항목에 새로운 값을 입력합니다. \s 
            - 4. 비밀번호/이메일/닉네임을 수정할 경우, 기존과 다른 새로운 값으로 입력해햐 합니다. \s
            - 5. 이미 사용 중인 이메일/닉네임은 사용할 수 없습니다.
            """;

    public static final String WITHDRAW_USER = """
            현재 로그인한 사용자의 계정을 탈퇴 처리합니다.\s
            - soft delete 방식으로 회원을 탈퇴 처리하여, null 이었던 deleted_at 값을 갱신합니다.\s
            - 탈퇴처리 완료 직후, 현재 사용자 인증 정보를 제거하고 세션을 무효화합니다.\s
            - 매일 1회, 스케줄러를 통해 탈퇴 처리한지 30일 이상의 시간이 지난 유저 정보를 확인한 후 hard delete 합니다.
            """;
}
