package org.example.what_seoul.service.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.user.dto.*;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.CustomValidationException;
import org.example.what_seoul.exception.PasswordMismatchException;
import org.example.what_seoul.repository.user.UserRepository;
import org.example.what_seoul.service.user.dto.LoginUserInfoDTO;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;


    /**
     * 유저 생성 기능
     * - request DTO 유효성 검증과 중복 값 검증을 함께 진행한다.
     * - 이를 위해 @Valid 애노테이션을 사용하는 대신, ValidatorFactory를 수동으로 생성한다.
     * @param req 유저 생성에 필요한 요청 데이터 DTO
     * @return 유저 생성 성공 시 CommonResponse를, 실패 시 CommonErrorResponse를 반환한다.
     */
    @Transactional
    public CommonResponse<ResCreateUserDTO> createUser(ReqCreateUserDTO req) {
        Map<String, List<String>> errors = new HashMap<>();

        // 1. Request DTO 유효성 검증
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ReqCreateUserDTO>> violations = validator.validate(req);

        for (ConstraintViolation<ReqCreateUserDTO> violation : violations) {
            errors.computeIfAbsent(violation.getPropertyPath().toString(), key -> new ArrayList<>())
                    .add(violation.getMessage());
        }

        // 2. 중복 값 검증
        if (userRepository.findByUserId(req.getUserId()).isPresent()) {
            if (errors.containsKey("userId")) {
                errors.get("userId").add("이미 사용 중인 아이디입니다.");
            } else {
                errors.computeIfAbsent("userId", key -> new ArrayList<>()).add("이미 사용 중인 아이디입니다.");
            }
        }

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            if (errors.containsKey("email")) {
                errors.get("email").add("이미 사용 중인 이메일입니다.");
            } else {
                errors.computeIfAbsent("email", key -> new ArrayList<>()).add("이미 사용 중인 이메일입니다.");
            }
        }

        if (userRepository.findByNickName(req.getNickName()).isPresent()) {
            if (errors.containsKey("nickName")) {
                errors.get("nickName").add("이미 사용 중인 닉네임입니다.");
            } else {
                errors.computeIfAbsent("nickName", key -> new ArrayList<>()).add("이미 사용 중인 닉네임입니다.");
            }
        }

        // 3. 1)유효성 검증 및 2)중복 검증에서 발생한 모든 에러를 포함하여 예외를 던진다.
        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }

        User newUser = new User(
                req.getUserId(),
                encoder.encode(req.getPassword()),
                req.getEmail(),
                req.getNickName()
        );

        userRepository.save(newUser);

        return new CommonResponse<>(
                true,
                "User Created",
                ResCreateUserDTO.from(newUser)
        );
    }

    @Transactional(readOnly = true)
    public CommonResponse<Page<ResGetUserSummaryDTO>> getUserList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findAll(pageable);
        long totalUserCount = userRepository.count();

        if (size > totalUserCount) {
            throw new IllegalArgumentException("Invalid Page Size.");
        }

        List<ResGetUserSummaryDTO> userSummaryList = new ArrayList<>();
        for (User user : users) {
            userSummaryList.add(
                    new ResGetUserSummaryDTO(
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
    public CommonResponse<ResGetUserDetailDTO> getUserDetailById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));

        ResGetUserDetailDTO userDetailRes = new ResGetUserDetailDTO(
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
    public CommonResponse<?> updateUserInfo(Long id, ReqUpdateUserInfoDTO req) {
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
