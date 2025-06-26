package org.example.what_seoul.exception;

import lombok.Getter;

@Getter

public class CitydataSchedulerException extends RuntimeException{
    public CitydataSchedulerException(String message) {
        super(message);
    }
}
