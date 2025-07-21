package org.example.what_seoul.exception;

import lombok.Getter;

/**
 * 데이터베이스 관련 예외를 처리하기 위한 커스텀 예외 클래스
 *
 * - 주로 특정 예외로 분류되지 않는 일반적인 DB 오류나,
 * - 하위 DB 예외(SQLIntegrityConstraintViolationException, ConstraintViolationException 등)를
 * - 감싸 메시지를 전달하고자 할 때 사용한다.
 */
@Getter
public class DatabaseException extends RuntimeException{
    /**
     * 상세 오류 메시지를 DatabaseException을 생성한다.
     *
     * @param message 사용자에게 전달할 오류 메시지
     */
    public DatabaseException(String message) {
        super(message);
    }
}
