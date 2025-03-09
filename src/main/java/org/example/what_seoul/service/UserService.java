package org.example.what_seoul.service;

import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.dto.*;
import org.example.what_seoul.entity.User;
import org.example.what_seoul.exception.DuplicateFieldException;
import org.example.what_seoul.exception.PasswordMismatchException;
import org.example.what_seoul.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

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
                req.getPassword(),
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
    public CommonResponse<?> updateUserPassword(Long id, UpdateUserPasswordReq req) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
        String currPassword = user.getPassword();
        String reqCurrPassword = req.getCurrPassword();
        String reqNewPassword = req.getNewPassword();

        if (!currPassword.equals(reqCurrPassword)) {
            throw new PasswordMismatchException();
        }

        if (currPassword.equals(reqNewPassword)) {
            throw new IllegalArgumentException("새로운 비밀번호로 변경해 주세요.");
        }

        user.changePassword(reqNewPassword);
        return new CommonResponse<>(
                true,
                "Update User Password Success",
                null // TODO: null 반환이 괜찮은지
        );
    }




}
