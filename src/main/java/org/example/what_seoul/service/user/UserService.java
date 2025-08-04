package org.example.what_seoul.service.user;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.common.validation.CustomValidator;
import org.example.what_seoul.config.JwtTokenProvider;
import org.example.what_seoul.controller.user.dto.*;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.CustomValidationException;
import org.example.what_seoul.repository.user.UserRepository;
import org.example.what_seoul.service.user.dto.LoginUserInfoDTO;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final CustomValidator customValidator;
    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 일반 회원 가입 기능
     * - request DTO 유효성 검증과 중복 값 검증을 함께 진행한다.
     * - 이를 위해 @Valid 애노테이션을 사용하는 대신, ValidatorFactory를 수동으로 생성한다.
     * @param req 회원 가입에 필요한 요청 데이터 DTO
     * @return 회원 가입 성공 시 CommonResponse를, 실패 시 CommonErrorResponse를 반환한다.
     */
    @Transactional
    public CommonResponse<ResCreateUserDTO> createUser(ReqCreateUserDTO req) {
        Map<String, List<String>> errors = new HashMap<>();

        // 1. Request DTO 유효성 검증
        Set<ConstraintViolation<ReqCreateUserDTO>> violations = customValidator.validate(req);

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
            log.error("일반 회원가입 실패 - validation errors: {}", errors);
            throw new CustomValidationException(errors);
        }

        User newUser = new User(
                req.getUserId(),
                encoder.encode(req.getPassword()),
                req.getEmail(),
                req.getNickName(),
                RoleType.USER
        );

        userRepository.save(newUser);

        return new CommonResponse<>(
                true,
                "회원 가입 성공",
                ResCreateUserDTO.from(newUser)
        );
    }

    /**
     * 일반 유저 로그인 기능
     * @param req
     * @return
     */
    public CommonResponse<ResUserLoginDTO> login(ReqUserLoginDTO req, HttpServletResponse response) {
        User user = userRepository.findByUserId(req.getUserId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (!user.getRole().equals(RoleType.USER)) {
            throw new IllegalArgumentException("일반 회원 계정이 아닙니다.");
        }

        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        }

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), RoleType.USER.name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId(), RoleType.USER.name());

        redisTemplate.opsForValue().set(
                "RT:" + user.getUserId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationMs(),
                TimeUnit.MILLISECONDS
        );

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 전송
                .path("/")
                .maxAge(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 전송
                .path("/")
                .maxAge(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new CommonResponse<>(
                true,
                "회원 로그인 성공",
                new ResUserLoginDTO(user.getUserId(), jwtTokenProvider.getAccessTokenExpirationTime(accessToken))
        );
    }

    /**
     * 회원 정보 리스트 조회 기능
     * @param page
     * @param size
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<Page<ResGetUserDetailSummaryDTO>> getUserList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findAll(pageable);

        List<ResGetUserDetailSummaryDTO> userSummaryList = users.stream()
                .map(ResGetUserDetailSummaryDTO::from)
                .collect(Collectors.toList());

        return new CommonResponse<>(true, "회원 정보 리스트 조회 성공", new PageImpl<>(userSummaryList, pageable, users.getTotalElements()));
    }

    /**
     * 회원 정보 상세 조회 기능
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<ResGetUserDetailDTO> getUserDetailById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        ResGetUserDetailDTO userDetailRes = ResGetUserDetailDTO.from(user);

        return new CommonResponse<>(
                true,
                "회원 정보 상세 조회 성공",
                userDetailRes
        );
    }

    /**
     * 회원 정보 수정 기능
     * - request DTO 유효성 검증과 비즈니스 로직상의 검증을 함께 진행한다.
     * - 이를 위해 @Valid 애노테이션을 사용하는 대신, ValidatorFactory를 수동으로 생성한다.
     * @param req 회원 정보 수정에 필요한 요청 데이터 DTO
     * @return 회원 정보 수정 성공 시 CommonResponse를, 실패 시 CommonErrorResponse를 반환한다.
     */
    @Transactional
    public CommonResponse<ResUpdateUserDTO> updateUserInfo(ReqUpdateUserInfoDTO req) {
        Long id = getLoginUserInfo().getId();
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 실제 회원정보
        String currPassword = user.getPassword();
        String currEmail = user.getEmail();
        String currNickName = user.getNickName();

        // 요청 DTO로 전달된 회원정보
        String reqCurrPassword = req.getCurrPassword();
        String reqNewPassword = req.getNewPassword();
        String reqNewEmail = req.getNewEmail();
        String reqNewNickName = req.getNewNickName();

        boolean nothingToUpdate = reqNewPassword == null && reqNewEmail == null && reqNewNickName == null;

        Map<String, List<String>> errors = new HashMap<>();

        // 1. Request DTO 유효성 검증
        Set<ConstraintViolation<ReqUpdateUserInfoDTO>> violations = customValidator.validate(req);

        for (ConstraintViolation<ReqUpdateUserInfoDTO> violation : violations) {
            errors.computeIfAbsent(violation.getPropertyPath().toString(), key -> new ArrayList<>())
                    .add(violation.getMessage());
        }

        // 2. 비즈니스 검중
        // 현재 비밀번호를 정확히 입력해야 개인정보 수정 가능
        // 현재 비밀번호를 정확히 입력하더라도, 아무런 값도 변경하지 않으면 개인정보 수정 불가 (비밀번호, 이메일, 별명 중 최소 한 항목은 수정해야 한다.)
        // 각 항목을 기존과 다른 새로운 값으로 변경해야 개인정보 수정 가능
        // 이미 사용 중인 이메일, 별명 사용 불가능

        if (reqCurrPassword != null) {
            // 현재 비밀번호를 정확히 입력하지 않은 경우
            if (!encoder.matches(reqCurrPassword, currPassword)) {
                errors.computeIfAbsent("currPassword", key -> new ArrayList<>()).add("비밀번호가 일치하지 않습니다."); // matches(raw, encoded)
            }
            // 현재 비밀번호는 정확히 입력했으나 아무런 값도 변경하지 않는 경우
            if (encoder.matches(reqCurrPassword, currPassword) && nothingToUpdate) {
                errors.computeIfAbsent("nothingToUpdate", key -> new ArrayList<>()).add("수정할 정보를 입력해 주세요.");
            }
        }

        if (reqNewPassword != null) {
            // 기존의 비밀번호와 같은 비밀번호로 변경하는 경우
            if(encoder.matches(reqNewPassword, currPassword)) {
                errors.computeIfAbsent("newPassword", key -> new ArrayList<>()).add("새로운 비밀번호로 변경해 주세요.");
            }
        }

        if (reqNewEmail != null) {
            // 기존의 이메일과 같은 이메일로 변경하는 경우
            if (currEmail.equals(reqNewEmail)) {
                errors.computeIfAbsent("newEmail", key -> new ArrayList<>()).add("새로운 이메일로 변경해 주세요.");
            }
            // 다른 계정이 사용 중인 이메일로 변경하는 경우
            else if (userRepository.existsByEmail(reqNewEmail)) {
                errors.computeIfAbsent("newEmail", key -> new ArrayList<>()).add("이미 사용 중인 이메일입니다.");
            }
        }

        if (reqNewNickName != null) {
            // 기존의 별명과 같은 별명으로 변경하는 경우
            if (currNickName.equals(reqNewNickName)) {
                errors.computeIfAbsent("newNickName", key -> new ArrayList<>()).add("새로운 별명으로 변경해 주세요.");
            }
            // 다른 계정이 사용 중인 별명으로 변경하는 경우
            else if(userRepository.existsByNickName(reqNewNickName)) {
                errors.computeIfAbsent("newNickName", key -> new ArrayList<>()).add("이미 사용 중인 별명입니다.");
            }
        }

        // 3. 1) Request DTO 유효성 검증 및 2) 비즈니스 검증에서 발생한 모든 에러를 포함하여 예외를 던진다.
        if (!errors.isEmpty()) {
            log.warn("회원 정보 수정 실패 - validation errors: {}", errors);
            throw new CustomValidationException(errors);
        }

        user.updateUserInfo(
                reqNewPassword != null ? encoder.encode(reqNewPassword) : null, // rawPassword cannot be null 방지
                reqNewEmail,
                reqNewNickName
        );

        return new CommonResponse<>(true, "회원 정보 수정 성공", ResUpdateUserDTO.from(user));
    }

    /**
     * 회원 탈퇴 기능
     * - soft delete 방식으로 회원을 탈퇴 처리한다.
     * - deletedAt 필드에 나타난 DateTime으로부터 30일 이상의 시간이 지났다면 hard delete 처리한다.
     * - 매일 오전 3시 마다 UserCleanupScheduler에서 삭제해야 하는 유저를 확인한 후 처리한다.
     * - 현재 사용자 인증 정보를 제거하고 세션을 무효화한다.
     * - TODO: 향후 유저가 직접 생성하는 데이터가 더 추가된다면, soft delete 상태에 있는 유저 데이터는 타 유저가 조회할 수 없도록 세부적인 구현을 추가한다.
     * @return 회원 탈퇴 성공 시 CommonResponse를, 실패 시 CommonErrorResponse를 반환한다.
     */
    @Transactional
    public CommonResponse<ResWithdrawUserDTO> withdrawUser(HttpServletRequest request, HttpServletResponse response) {
        Long id = getLoginUserInfo().getId();
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        if (user.getDeletedAt() != null) {
            throw new IllegalStateException("이미 탈퇴한 사용자입니다.");
        }

        user.deactivate();

        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());

        return new CommonResponse<>(
                true,
                "회원 탈퇴 성공",
                ResWithdrawUserDTO.from(user)
        );
    }

    /**
     * 사용자가 입력한 이메일 주소로 해당 계정의 아이디를 전송한다.
     *
     * 이메일이 존재하지 않으면 EntityNotFoundException이 발생하며,
     * 메일 전송에 실패할 경우 RuntimeException이 발생한다.
     *
     * @param req  사용자가 입력한 이메일 정보를 담은 요청 객체
     * @return  응답 본문은 null이며, 성공 여부와 메시지만 포함된다.
     * @throws EntityNotFoundException 이메일이 존재하지 않는 경우
     * @throws RuntimeException 메일 전송에 실패한 경우
     */
    @Transactional(readOnly = true)
    public CommonResponse<Void> findUserIdByEmail(ReqFindUserIdDTO req) {
        try {
            User user = userRepository.findByEmail(req.getEmail()).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

            String email = user.getEmail();
            String userId = user.getUserId();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@whatseoul.com");
            message.setTo(email);
            message.setSubject("[WhatSeoul] 아이디 찾기 결과를 안내드립니다.");
            message.setText(String.format("""
            안녕하세요, WhatSeoul입니다.
        
            요청하신 아이디 찾기 결과입니다:
            아이디: %s
        
            감사합니다.
            """, userId));

            javaMailSender.send(message);

            return new CommonResponse<>(true, "아이디 찾기 성공", null);
        } catch (MailException e) {
            log.error("아이디 찾기 이메일 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("아이디 찾기 이메일 전송에 실패했습니다.");
        }
    }

    /**
     * 사용자가 입력한 이메일 주소로 초기화된 비밀번호를 전송한다.
     *
     * 이메일이 존재하지 않으면 EntityNotFoundException이 발생하며,
     * 메일 전송에 실패할 경우 RuntimeException이 발생한다.
     *
     * @param req  사용자가 입력한 이메일 정보를 담은 요청 객체
     * @return  응답 본문은 null이며, 성공 여부와 메시지만 포함된다.
     * @throws EntityNotFoundException 이메일이 존재하지 않는 경우
     * @throws RuntimeException 메일 전송에 실패한 경우
     */
    @Transactional
    public CommonResponse<Void> resetPassword(ReqFindPasswordDTO req) {
        try {
            User user = userRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

            String tempPassword = generateRandomPassword();
            user.setPassword(encoder.encode(tempPassword));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("[WhatSeoul] 임시 비밀번호 안내드립니다.");
            message.setText(String.format("""
           
            안녕하세요, WhatSeoul입니다.
    
            임시 비밀번호가 발급되었습니다:
            비밀번호: %s
    
            로그인 후 반드시 비밀번호를 변경해주세요.
    
            감사합니다.
            """, tempPassword));

            javaMailSender.send(message);
            return new CommonResponse<>(true, "비밀번호 찾기 성공", null);
        } catch (MailException e) {
            log.error("비밀번호 찾기 이메일 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("비밀번호 찾기 이메일 전송에 실패했습니다.");
        }

    }

    public LoginUserInfoDTO getLoginUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if (user != null) {
            log.info("getLoginUserInfo: user not null");
        } else {
            throw new IllegalArgumentException("로그인한 사용자 정보가 없습니다.");
        }
        return LoginUserInfoDTO.from(user);
    }

    public Object getAuthenticationPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        log.info("Authentication 객체: {}", authentication);

        Object principal = authentication.getPrincipal();
//        log.info("principal : {}", principal);
        if (!principal.equals("anonymousUser")) {
            User user = (User) authentication.getPrincipal();
            log.info("anonymousUser loginUserId: {}", user.getUserId());
        }
        return principal;

    }

    /**
     * 랜덤 문자열(특수문자 포함) 생성
     */
    private String generateRandomPassword() {
        String base = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        String specials = "!@#$%";
        SecureRandom random = new SecureRandom();
        char specialChar = specials.charAt(random.nextInt(specials.length()));
        return base + specialChar;
    }

}
