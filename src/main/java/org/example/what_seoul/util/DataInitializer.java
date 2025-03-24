package org.example.what_seoul.util;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.citydata.AreaRepository;
import org.example.what_seoul.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final CityDataScheduler cityDataScheduler;

    @Value("${seoul.open.api.hot-spot.file-path}")
    private String filePath;

    @Override
    public void run(String[] args) throws IOException {
        if (userRepository.count() == 0) {
            // 테스트용 유저 100인 생성
            List<User> testUsers = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                testUsers.add(
                        new User(
                                "test" + (i + 1),
                                "test" + (i + 1),
                                "test" + (i + 1) + "@test" + (i + 1) + ".com",
                                "닉네임" + (i + 1),
                                LocalDateTime.now().minusDays(i + 1)
                        )
                );
            }

            userRepository.saveAll(testUsers);
        }

        if (areaRepository.count() == 0) {
            // 서울시 116개 핫스팟 정보 저장
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
                Cell engNmCell = row.getCell(4);

                if (areaCdCell != null && areaNmCell != null && engNmCell != null) {
                    String category = categoryCell.getStringCellValue();
                    String areaCode = areaCdCell.getStringCellValue();
                    String areaName = areaNmCell.getStringCellValue();
                    String engName = engNmCell.getStringCellValue();

                    // Area 객체 생성 및 리스트에 추가
                    areas.add(new Area(category, areaCode, areaName, engName));
                }

            }

            // 데이터베이스에 저장
            areaRepository.saveAll(areas);

            // 파일 스트림 닫기
            workbook.close();
            stream.close();
        }

        cityDataScheduler.call();


    }
}
