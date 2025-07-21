package org.example.what_seoul.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.user.UserRepository;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${seoul.open.api.hot-spot.file-path}")
    private String filePath;

    @Value("${area-initializer.enabled}")
    private Boolean areaInitializerEnabled;

    @Value("${area-initializer.source-type}")
    private String sourceType;

    private static final String EXCEL = "excel";
    private static final String GEOJSON = "geojson";

    @Override
    public void run(String[] args) throws IOException {
//        if (userRepository.count() == 0) {
//            userRepository.save(new User("admin", encoder.encode("admin1234!"), "admin@admin.com", "관리자", RoleType.ADMIN, LocalDateTime.now().minusDays(1)));

//            // 테스트용 유저 100인 생성
//            List<User> testUsers = new ArrayList<>();
//
//            for (int i = 0; i < 100; i++) {
//                testUsers.add(
//                        new User(
//                                "test" + (i + 1),
//                                encoder.encode("test" + (i + 1)),
//                                "test" + (i + 1) + "@test" + (i + 1) + ".com",
//                                "닉네임" + (i + 1),
//                                LocalDateTime.now().minusDays(i + 1)
//                        )
//                );
//            }
//
//            userRepository.saveAll(testUsers);
//        }


        if (areaInitializerEnabled && sourceType.equals(EXCEL)) {
            // 서울시 주요 장소 정보를 나타내는 액셀 파일을 활용해 Area 정보 저장하기
            // 단, 해당 액셀 파일에는 장소별 영역을 나타내는 geometry 값이 존재하지 않음

            // 서울시 116개 핫스팟 정보 저장 => 120개 (8개 항목 제거-DB에서 제거하지 않음, 12개 항목 추가)
            FileInputStream stream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(stream);
            Sheet sheet = workbook.getSheetAt(0);

            List<Area> areas = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                Cell categoryCell = row.getCell(0);
                Cell areaCdCell = row.getCell(2);
                Cell areaNmCell = row.getCell(3);

                if (areaCdCell != null && areaNmCell != null) {
                    String category = categoryCell.getStringCellValue();
                    String areaCode = areaCdCell.getStringCellValue();
                    String areaName = areaNmCell.getStringCellValue();

                    // DB에 이미 존재하는 장소명인 경우 건너뜀
                    if (areaRepository.existsByAreaNameAndDeletedAtIsNull(areaName)) {
                        log.info("skipping saving area :{}", areaName);
                        continue;
                    }

                    // Area 객체 생성 및 리스트에 추가
                    areas.add(new Area(category, areaCode, areaName));
                }

            }

            // 데이터베이스에 저장
            areaRepository.saveAll(areas);

            // 파일 스트림 닫기
            workbook.close();
            stream.close();
        }

        if (areaInitializerEnabled && sourceType.equals(GEOJSON)) {
            // 서울시 주요 장소 영역을 나타내는 shapefile을 변환한 geojson 파일을 활용해 Area 정보 저장하기
            // 액셀 파일과 달리, 장소별 영역을 나타내는 geometry 값이 존재함
            try {
                // GeoJSON 파일 읽기
                InputStream inputStream = new ClassPathResource("data/seoul_zones_120.geojson").getInputStream();
                String geoJson = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();

                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(geoJson);
                GeoJsonReader reader = new GeoJsonReader();
                WKTWriter wktWriter = new WKTWriter();

                // "features" 배열 얻기
                JSONArray features = (JSONArray) jsonObject.get("features");
                log.info("서울시 주요 장소 {}개 로드 시작", features.size());

                for (Object featureObj : features) {
                    JSONObject feature = (JSONObject) featureObj;
                    JSONObject properties = (JSONObject) feature.get("properties");
                    JSONObject geometryJson = (JSONObject) feature.get("geometry");

                    String areaName = properties.get("AREA_NM").toString(); // 장소명
                    String areaCode = properties.get("AREA_CD").toString(); // 장소 코드
                    String category = properties.get("CATEGORY").toString(); // 카테고리

                    // GeoJSON 데이터를 WKT로 변환하여 저장
                    Geometry geometry = reader.read(geometryJson.toString());
                    String polygonWkt = wktWriter.write(geometry);

                    // 기존에 Area가 DB에 있는지 확인
                    Optional<Area> existingArea = areaRepository.findByAreaNameAndDeletedAtIsNull(areaName);
                    if (existingArea.isPresent()) {
                        Area area = existingArea.get();
                        // polygonWkt가 빈 문자열이거나 null인 경우에만 업데이트
                        if (area.getPolygonWkt() == null || area.getPolygonWkt().trim().isEmpty()) {
                            area.setPolygonWkt(polygonWkt);
                            areaRepository.save(area);
                            log.info("Updated polygonWkt for existing area: {}", areaName);
                        } else {
                            log.info("polygonWkt already exists for area: {}, skipping update", areaName);
                        }
                    } else {
                        // DB에 없으면 새로 저장
                        Area newArea = new Area(category, areaCode, areaName, polygonWkt);
                        areaRepository.save(newArea);
                        log.info("Saved new area: {}", areaName);
                    }
                }

                log.info("서울시 주요 장소 {}개 저장 완료", features.size());

            } catch (Exception e) {
                log.error("데이터 초기화 실패: {}", e.getMessage(), e);
            }

        }
    }
}
