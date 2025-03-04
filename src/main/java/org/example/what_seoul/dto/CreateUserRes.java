package org.example.what_seoul.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRes {
    private String userId;
    private String email;
    private String nickName;
}
