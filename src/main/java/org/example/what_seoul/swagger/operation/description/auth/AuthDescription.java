package org.example.what_seoul.swagger.operation.description.auth;

public class AuthDescription {
    public static final String REISSUE_ACCESS_TOKEN = """
            Refresh Token을 이용해 Access Token을 재발급합니다.\s
            - Refresh Token은 쿠키에 담겨 전달되어야 합니다. (refreshToken 쿠키 사용)\s
            - 재발급된 Access Token은 쿠키에 담아 응답됩니다.
            """;

    public static final String REISSUE_ACCESS_TOKEN_SUCCESS = """
            액세스 토큰 재발급 성공
            - message : 액세스 토큰 재발급 성공
            """;

    public static final String LOGOUT = """
            현재 로그인된 사용자의 토큰을 무효화하고 쿠키에서 제거합니다.
            - Access Token은 accessToken 쿠키를 통해 전달되어야 합니다.
            - Redis에 저장된 Refresh Token도 삭제됩니다.
            - AccessToken, RefreshToken 쿠키 모두 즉시 만료 처리됩니다.
            """;

    public static final String LOGOUT_SUCCESS = """
            로그아웃 성공
            - message : 로그아웃 성공
            """;
}
