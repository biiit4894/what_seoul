package org.example.what_seoul.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.citydata.AreaRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final BCryptPasswordEncoder encoder;
//    private final CitydataScheduler cityDataScheduler;

    @Value("${seoul.open.api.hot-spot.file-path}")
    private String filePath;

    @Override
    public void run(String[] args) throws IOException {
//        if (userRepository.count() == 0) {
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

        if (areaRepository.count() == 0) {
//            // 서울시 116개 핫스팟 정보 저장
//            FileInputStream stream = new FileInputStream(filePath);
//            Workbook workbook = new XSSFWorkbook(stream);
//            Sheet sheet = workbook.getSheetAt(0);
//
//            List<Area> areas = new ArrayList<>();
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) {
//                    continue;
//                }
//
//                Cell categoryCell = row.getCell(0);
//                Cell areaCdCell = row.getCell(2);
//                Cell areaNmCell = row.getCell(3);
//                Cell engNmCell = row.getCell(4);
//
//                if (areaCdCell != null && areaNmCell != null && engNmCell != null) {
//                    String category = categoryCell.getStringCellValue();
//                    String areaCode = areaCdCell.getStringCellValue();
//                    String areaName = areaNmCell.getStringCellValue();
//                    String engName = engNmCell.getStringCellValue();
//
//                    // Area 객체 생성 및 리스트에 추가
//                    areas.add(new Area(category, areaCode, areaName, engName));
//                }
//
//            }
//
//            // 데이터베이스에 저장
//            areaRepository.saveAll(areas);
//
//            // 파일 스트림 닫기
//            workbook.close();
//            stream.close();

            try {
                // GeoJSON 파일 읽기
                InputStream inputStream = new ClassPathResource("data/seoul_zones.geojson").getInputStream();
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
\
                    // GeoJSON 데이터를 WKT로 변환하여 저장
                    Geometry geometry = reader.read(geometryJson.toString());
                    String polygonWkt = wktWriter.write(geometry);

                    Area area = new Area(category, areaCode, areaName, polygonWkt);
                    areaRepository.save(area);
                }

                log.info("서울시 주요 장소 {}개 저장 완료", features.size());

            } catch (Exception e) {
                log.error("데이터 초기화 실패: {}", e.getMessage(), e);
            }
        }

//        cityDataScheduler.call();


    }
}
