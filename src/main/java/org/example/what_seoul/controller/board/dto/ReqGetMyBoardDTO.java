package org.example.what_seoul.controller.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqGetMyBoardDTO {
    List<String> selectedAreaNames;
}
