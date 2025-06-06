# what_seoul
서울 실시간 도시데이터 조회 서비스 [왓서울(WhatSeoul)](https://github.com/WhatSEOUL/WhatSeoul)을 리팩토링합니다.
<br>

## 개요
- 개발 기간: 2025.03.03~ (진행중)
- 개발 인원: 1인

## 기술 Stack
- 주 언어 : Java 17
- 프레임워크 : Spring Boot 3.4.3
- DB : MySQL, Spring Data JPA
- 빌드 및 테스트 : Gradle, JUnit
- 템플릿 엔진 : Thymeleaf (+ HTML/CSS, Javascript)
- 배포 환경 : AWS RDS/EC2(예정), Github Actions(예정)

## 브랜치 전략
- main : 실제 서비스 운영 환경에 사용하기 위한 브랜치
- dev : 로컬 개발 환경에서 사용하기 위한 브랜치
  - 하위에 feat/#N과 같이 이슈 번호를 바탕으로 하위 브랜치를 생성하여 작업합니다.

## 주요 개선점
1. 장소별 도시데이터 조회 UI 가시성 개선
  - 기존 UI
    - 장소별 버튼을 클릭하는 방식으로 서울시 내 주요 장소를 사전에 직접 선택한 이후, 별도의 페이지에서 장소별 도시데이터를 조회 
  - 개선 UI
    - 서울시 주요 장소 116곳을 모두 폴리곤 형태로 지도에 표기한 후, 그 위에 유형별 도시데이터 (인구/날씨/문화행사)를 나타내어 주요 장소의 지리적 위치와 데이터 지표를 함께 조회할 수 있도록 개선
    - 폴리곤을 클릭한 후, 인구/날씨/문화행사로 구분되는 버튼을 클릭하여 지도 위 모달창에 유형별 도시데이터를 조회하도록 분리
   
2. 장소별 도시데이터 조회 방식 다변화
  - 기존 조회 방식
    - 장소 한 곳을 선택하여, 해당 장소의 인구/날씨/문화행사 데이터를 한 번에 모두 조회
  - 변경된 방식
    1. 장소 한 곳을 선택한 후, 인구/날씨/문화행사 데이터 중 한 유형의 데이터를 선택해 조회
    2. 전체 장소에 대한 인구 혼잡도를 지도상에서 조회 (장소별 폴리곤의 색상을 통해 혼잡도 표현)
    3. 전체 장소에 대한 날씨 현황을 지도상에서 조회 (장소별 폴리곤에 기온 표기)
    4. 전체 장소에 대한 도로명주소별 문화행사 정보를 지도상에서 조회 (장소별 폴리곤에 문화행사 주소마다의 마커 표기)
   



## 주요 기능

서울시 주요 장소 116곳에 대한 실시간 도시데이터 조회
- 장소별 인구 현황 조회
- 장소별 날씨 현황 조회
- 장소별 문화행사 정보 조회 
- 장소 전체 인구 혼잡도 조회
- 장소 전체 기온 조회
- 장소 전체 도로명주소별 문화행사 조회

장소 검색 및 조회 기능
- 현위치 인근 주요 장소 조회
- 장소 검색

문화행사 후기 작성 기능
- 문화행사 후기 작성/조회/수정/삭제 기능

회원 관련 기능
- 회원가입
- 로그인
- 회원정보 수정
- 회원 탈퇴
- 회원 정보 조회
