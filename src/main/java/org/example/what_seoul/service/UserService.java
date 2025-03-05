package org.example.what_seoul.service;

import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.dto.CreateUserReq;
import org.example.what_seoul.dto.CreateUserRes;
import org.example.what_seoul.dto.GetUserDetailRes;
import org.example.what_seoul.entity.User;
import org.example.what_seoul.exception.DuplicateFieldException;
import org.example.what_seoul.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
}
