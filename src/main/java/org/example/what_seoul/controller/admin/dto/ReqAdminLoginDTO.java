package org.example.what_seoul.controller.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqAdminLoginDTO {
    private String userId;
    private String password;
}
