package org.example.what_seoul.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateUserReq {
    private String userId;
    private String password;

    private String email;

    private String nickName;
}
