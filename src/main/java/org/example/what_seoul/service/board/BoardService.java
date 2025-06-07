package org.example.what_seoul.service.board;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.common.validation.CustomValidator;
import org.example.what_seoul.controller.board.dto.*;
import org.example.what_seoul.domain.board.Board;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.CustomValidationException;
import org.example.what_seoul.repository.board.BoardRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.service.user.UserService;
import org.example.what_seoul.service.user.dto.LoginUserInfoDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {
    private final UserService userService;
    private final BoardRepository boardRepository;
    private final CultureEventRepository cultureEventRepository;
    private final CustomValidator customValidator;

    @Transactional
    public CommonResponse<ResCreateBoardDTO> createBoard(ReqCreateBoardDTO req) {
        User user = (User) userService.getAuthenticationPrincipal();

        Long cultureEventId = req.getCultureEventId();
        CultureEvent cultureEvent = cultureEventRepository.findById(cultureEventId).orElseThrow(() -> new EntityNotFoundException("문화행사를 찾을 수 없습니다. 문화행사 id = " + cultureEventId));

        Board newBoard = new Board(req.getContent(), user, cultureEvent);

        boardRepository.save(newBoard);

        return new CommonResponse<>(true, "문화행사 후기 작성 성공", ResCreateBoardDTO.from(newBoard));
    }

    @Transactional(readOnly = true)
    public CommonResponse<Slice<ResGetBoardDTO>> getBoardsByCultureEventId(Long cultureEventId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Direction.DESC, "createdAt"));
        Slice<Board> boardSlice = boardRepository.findSliceByCultureEventId(cultureEventId, pageable);

        LoginUserInfoDTO loginUserInfo = userService.getLoginUserInfo();
        Slice<ResGetBoardDTO> result = boardSlice.map(board -> ResGetBoardDTO.from(board, loginUserInfo));

        return new CommonResponse<>(true, "장소별 문화행사 후기 목록 조회 성공", result);
    }

    @Transactional(readOnly = true)
    public CommonResponse<ResGetBoardDTO> getBoardById(Long id) {
        LoginUserInfoDTO loginUserInfo = userService.getLoginUserInfo();
        Board board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("문화행사 후기를 찾을 수 없습니다. 후기 id = " + id));
        return new CommonResponse<>(true, "문화행사 후기 조회 성공", ResGetBoardDTO.from(board, loginUserInfo));
    }

    @Transactional(readOnly = true)
    public CommonResponse<Slice<ResGetMyBoardDTO>> getMyBoards(int page, int size, LocalDate startDate, LocalDate endDate, String sort, ReqGetMyBoardDTO req) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일과 같거나 이후여야 합니다.");
        }

        Direction direction = "asc".equalsIgnoreCase(sort) ? Direction.ASC : Direction.DESC;
        Pageable pageable = PageRequest.of(page, size);
        LoginUserInfoDTO loginUserInfo = userService.getLoginUserInfo();

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        List<String> selectedAreaNames = (req != null) ? req.getSelectedAreaNames() : null;

        Slice<ResGetMyBoardDTO> boardSlice = boardRepository.findMyBoardsSlice(
                loginUserInfo.getId(),
                startDateTime,
                endDateTime,
                selectedAreaNames,
                pageable,
                direction

        );

        return new CommonResponse<>(true, "작성한 문화행사 후기 목록 조회 성공", boardSlice);
    }

    @Transactional
    public CommonResponse<ResUpdateBoardDTO> updateBoard(Long id, ReqUpdateBoardDTO req) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("문화행사 후기를 찾을 수 없습니다. 후기 id = " + id));

        User user = (User) userService.getAuthenticationPrincipal();
        if (user.getRole() == RoleType.USER && !Objects.equals(board.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        // 기존 후기 내용
        String currContent = board.getContent();
        // 수정 요청 DTO로 전달된 후기 내용
        String reqContent = req.getContent();

        Map<String, List<String>> errors = new HashMap<>();

        // 1. Request DTO 유효성 검증
        Set<ConstraintViolation<ReqUpdateBoardDTO>> violations = customValidator.validate(req);

        for (ConstraintViolation<ReqUpdateBoardDTO> violation : violations) {
            errors.computeIfAbsent(violation.getPropertyPath().toString(), key -> new ArrayList<>())
                    .add(violation.getMessage());
        }

        // 2. 비즈니스 검증
        // 기존과 다른 내용으로 변경해야 후기 내용을 수정할 수 있다.
        if (currContent.equals(reqContent)) {
            errors.computeIfAbsent("content", key -> new ArrayList<>()).add("기존과 동일한 내용입니다.");
        }

        // 3. 1) Request DTO 유효성 검증 및 2) 비즈니스 검증에서 발생한 모든 에러를 포함하여 예외를 던진다.
        if (!errors.isEmpty()) {
            log.warn("문화행사 후기 수정 실패 - validation errors: {}", errors);
            throw new CustomValidationException(errors);
        }

        board.updateBoard(req.getContent());

        return new CommonResponse<>(true, "문화행사 후기 수정 성공", ResUpdateBoardDTO.from(board));
    }

    @Transactional
    public CommonResponse<ResDeleteBoardDTO> deleteBoard(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("문화행사 후기를 찾을 수 없습니다. 후기 id = " + id));

        User user = (User) userService.getAuthenticationPrincipal();
        if (user.getRole() == RoleType.USER && !Objects.equals(board.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
        ResDeleteBoardDTO resDTO = ResDeleteBoardDTO.from(board); // LAZY 로딩 방지

        boardRepository.delete(board);

        return new CommonResponse<>(true, "문화행사 후기 삭제 성공", resDTO);

    }
}
