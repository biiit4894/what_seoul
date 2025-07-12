package org.example.what_seoul.service.admin;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.common.validation.CustomValidator;
import org.example.what_seoul.config.JwtTokenProvider;
import org.example.what_seoul.controller.admin.dto.*;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.CustomValidationException;
import org.example.what_seoul.exception.UnauthorizedException;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.user.UserRepository;
import org.example.what_seoul.util.GeoJsonParser;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final BCryptPasswordEncoder encoder;
    private final CustomValidator customValidator;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${file.storage.shp-temp-path}")
    private String shpTempPath;

    @Value("${file.storage.geojson-temp-path}")
    private String geojsonTempPath;

    @Value("${python.script-path}")
    private String pythonScriptPath;

    private final GeoJsonParser geoJsonParser;

    /**
     * 관리자 계정 생성 기능
     * - request DTO 유효성 검증과 중복 값 검증을 함께 진행한다.
     * - 이를 위해 @Valid 애노테이션을 사용하는 대신, ValidatorFactory를 수동으로 생성한다.
     * @param req 회원 가입에 필요한 요청 데이터 DTO
     * @return 관리자 계정 생성 성공 시 CommonResponse를, 실패 시 CommonErrorResponse를 반환한다.
     */
    @Transactional
    public CommonResponse<ResCreateAdminDTO> createAdminUser(String accessToken, ReqCreateAdminDTO req) {
        // 1. 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(accessToken)) {
            log.info("adminService - createAdminUser");
            throw new UnauthorizedException("유효하지 않거나 만료된 관리자 토큰입니다.");
        }

        // 2. role 확인
        Claims claims = jwtTokenProvider.getClaimsFromToken(accessToken);
        String role = claims.get("role", String.class);
        if (!role.equals("ADMIN")) {
            throw new AccessDeniedException("관리자 권한이 없습니다.");
        }

        Map<String, List<String>> errors = new HashMap<>();

        // 3. Request DTO 유효성 검증
        Set<ConstraintViolation<ReqCreateAdminDTO>> violations = customValidator.validate(req);

        for (ConstraintViolation<ReqCreateAdminDTO> violation : violations) {
            errors.computeIfAbsent(violation.getPropertyPath().toString(), key -> new ArrayList<>())
                    .add(violation.getMessage());
        }

        // 4. 중복 값 검증
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

        // 5. 3)유효성 검증 및 4)중복 검증에서 발생한 모든 에러를 포함하여 예외를 던진다.
        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }

        User newUser = new User(
                req.getUserId(),
                encoder.encode(req.getPassword()),
                req.getEmail(),
                req.getNickName(),
                RoleType.ADMIN
        );

        userRepository.save(newUser);

        return new CommonResponse<>(
                true,
                "관리자 계정 생성 성공",
                ResCreateAdminDTO.from(newUser)
        );
    }

    /**
     * 관리자 계정 로그인 기능
     * @param req
     * @return
     */
    @Transactional
    public CommonResponse<ResAdminLoginDTO> login(ReqAdminLoginDTO req, HttpServletResponse response) {
        User admin = userRepository.findByUserId(req.getUserId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        if (!admin.getRole().equals(RoleType.ADMIN)) {
            throw new IllegalArgumentException("관리자 계정이 아닙니다.");
        }

        if (!encoder.matches(req.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(admin.getUserId(), RoleType.ADMIN.name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(admin.getUserId(), RoleType.ADMIN.name());

        redisTemplate.opsForValue().set(
                "RT:" + admin.getUserId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationMs(),
                TimeUnit.MILLISECONDS
        );

        // 3. AccessToken 쿠키로 전달
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 전송
                .path("/")
                .maxAge(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        // 4. RefreshToken 쿠키로 전달
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new CommonResponse<>(
                true,
                "관리자 로그인 성공",
                new ResAdminLoginDTO(admin.getUserId(), jwtTokenProvider.getAccessTokenExpirationTime(accessToken))
        );
    }

    /**
     * 서울시 주요 장소 목록 조회 기능
     * @param page
     * @param size
     * @param req
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<Slice<ResGetAreaListDTO>> getAreaList(int page, int size, ReqGetAreaListDTO req) {
        Pageable pageable = PageRequest.of(page, size);

        String areaName = (req != null) ? req.getAreaName() : null;

        Slice<ResGetAreaListDTO> result = areaRepository.findAreasSlice(
                areaName,
                pageable
        );

        return new CommonResponse<>(true, "서울시 주요 장소 목록 조회 성공", result);
    }

    @Transactional
    public CommonResponse<List<ResDeleteAreaDTO>> deleteArea(List<Long> ids) {
        List<Area> areas = areaRepository.findAllById(ids);

        if (areas.isEmpty()) {
            throw new EntityNotFoundException("삭제할 장소 정보를 찾을 수 없습니다.");
        }

        List<ResDeleteAreaDTO> deletedAreas = new ArrayList<>();

        for (Area area : areas) {
            area.setDeletedAt();
            deletedAreas.add(ResDeleteAreaDTO.from(area));
        }

        return new CommonResponse<>(true, "서울시 주요 장소 정보 삭제 처리 성공", deletedAreas);
    }

    /**
     * 업로드된 .shp 파일 처리
     * @param multipartFile
     * @return
     */
    @Transactional
    public CommonResponse<ResUploadAreaDTO> processShapeFile(MultipartFile multipartFile) {
        try {
            // 1. 임시 디렉토리 생성
            File shpDir = new File(shpTempPath);
            if (!shpDir.exists()) {
                shpDir.mkdirs();
            }

            File geojsonDir = new File(geojsonTempPath);
            if (!geojsonDir.exists()) {
                geojsonDir.mkdirs();
            }

            // 2. shapefile 저장 (.shp 외에 .dbf, .shx도 필요할 수 있으니 zip으로 받아도 OK)
            File savedShp = new File(shpDir, "uploaded.shp");
            multipartFile.transferTo(savedShp);
            log.info("Shapefile saved at :{}", savedShp.getAbsolutePath());

            // 3. Python 스크립트 실행 (geopandas를 이용해 .shp -> geojson 변환)
            ProcessBuilder pb = new ProcessBuilder("python3", pythonScriptPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(log::info);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python 스크립트 실행 실패 (exit code: " + exitCode + ")");
            }

            // 4. GeoJSON 파일 읽고 파싱
            File geoJsonFile = new File(geojsonTempPath, "converted.geojson");
            if (!geoJsonFile.exists()) {
                throw new FileNotFoundException("변환된 GeoJSON 파일이 존재하지 않습니다.");
            }

            ResUploadAreaDTO res = geoJsonParser.extractAreasFromGeoJsonAndSave(geoJsonFile);
            return new CommonResponse<>(true, "장소 정보 업로드 성공", res);

        } catch (InterruptedException | IOException | ParseException e) {
            log.error("지역 업로드 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("지역 업로드 중 오류가 발생했습니다.", e);
        }
    }
}
