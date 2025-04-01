package org.example.what_seoul.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.user.dto.*;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.DuplicateFieldException;
import org.example.what_seoul.exception.PasswordMismatchException;
import org.example.what_seoul.repository.user.UserRepository;
import org.example.what_seoul.service.user.dto.LoginUserInfoDTO;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;


    @Transactional // TODO: @Transactional 세부 옵션 설정
    public CommonResponse<CreateUserRes> createUser(CreateUserReq req) {
        if (userRepository.findByUserId(req.getUserId()).isPresent()) {
            throw new DuplicateFieldException("userId");
        }

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new DuplicateFieldException("email");
        }

        if (userRepository.findByNickName(req.getNickName()).isPresent()) {
            throw new DuplicateFieldException("nickName");
        }

        User newUser = new User(
                req.getUserId(),
                encoder.encode(req.getPassword()),
                req.getEmail(),
                req.getNickName()
        );
        userRepository.save(newUser);
        CreateUserRes newUserRes = new CreateUserRes(
                newUser.getUserId(),
                newUser.getEmail(),
                newUser.getNickName()
        );
        return new CommonResponse<>(
                true,
                "User Created",
                newUserRes
        );
    }

    @Transactional(readOnly = true)
    public CommonResponse<Page<GetUserSummaryRes>> getUserList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findAll(pageable);
        long totalUserCount = userRepository.count();

        if (size > totalUserCount) {
            throw new IllegalArgumentException("Invalid Page Size.");
        }

        List<GetUserSummaryRes> userSummaryList = new ArrayList<>();
        for (User user : users) {
            userSummaryList.add(
                    new GetUserSummaryRes(
                            user.getId(),
                            user.getUserId(),
                            user.getEmail(),
                            user.getNickName()
                    )
            );
        }
        return new CommonResponse<>(true, "Get User Detail List Success", new PageImpl<>(userSummaryList, pageable, users.getTotalElements()));
    }

    @Transactional(readOnly = true) // TODO: @Transactional 세부 옵션 설정
    public CommonResponse<GetUserDetailRes> getUserDetailById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));

        GetUserDetailRes userDetailRes = new GetUserDetailRes(
                user.getId(),
                user.getUserId(),
                user.getEmail(),
                user.getNickName(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt()
        );
        return new CommonResponse<>(
                true,
                "Get User Detail Success",
                userDetailRes
        );
    }

    @Transactional
    public CommonResponse<?> updateUserInfo(Long id, UpdateUserInfoReq req) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
        String currPassword = user.getPassword();
        String currEmail = user.getEmail();
        String currNickName = user.getNickName();
        String reqCurrPassword = req.getCurrPassword();
        String reqNewPassword = req.getNewPassword();
        String reqNewEmail = req.getNewEmail();
        String reqNewNickName = req.getNewNickName();

        if (!currPassword.equals(reqCurrPassword)) {
            throw new PasswordMismatchException();
        }

        if (currPassword.equals(reqNewPassword)) {
            throw new IllegalArgumentException("새로운 비밀번호로 변경해 주세요.");
        }

        if (currEmail.equals(reqNewEmail)) {
            throw new IllegalArgumentException("새로운 이메일로 변경해 주세요.");
        }

        if (currNickName.equals(reqNewNickName)) {
            throw new IllegalArgumentException("새로운 닉네임으로 변경해 주세요.");
        }

        user.changeUserInfo(reqNewPassword, reqNewEmail, reqNewNickName);
        return new CommonResponse<>(
                true,
                "Update User Info Success",
                null // TODO: null 반환이 괜찮은지
        );
    }

    public LoginUserInfoDTO getLoginUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if (user != null) {
            log.info("getLoginUserInfo: user not null");
        } else {
            log.info("getLoginUserInfo: user null");

        }
        return LoginUserInfoDTO.from(user);
    }

    public Object getAuthenticationPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication 객체: {}", authentication);

        Object principal = authentication.getPrincipal();
        log.info("principal : {}", principal);
        if (!principal.equals("anonymousUser")) {
            User user = (User) authentication.getPrincipal();
            log.info("loginUserId: {}", user.getUserId());
        }
        return principal;

    }




}
