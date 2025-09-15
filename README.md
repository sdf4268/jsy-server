## https://jeonsy.cloud
## 개인 IoT 데이터 대시보드 서버

Spring Boot를 기반으로 구축된 개인용 IoT 데이터 수집 및 시각화 대시보드 프로젝트입니다.
1. ATMega2560 Chip에서 마이크로컨트롤러에서 전송되는 공기질 데이터를 수신하여 데이터베이스에 저장하고,
사용자가 웹을 통해 실시간 및 일일 데이터를 모니터링할 수 있는 기능을 제공합니다.

2. 운행 기록 데이터를 지도와 차트로 시각화하는 페이지도 포함되어 있습니다.
3. 삼성 Smartthings의 OAuth 2.0을 위한 토큰을 생성한 후 갱신하기 위한 API가 포함되어 있습니다.
4. 삼성 Smartthings의 Bearer Token을 통한 에어컨 리스트 확인, 데이터 수집, 제어 기능을 포함하고 있습니다.

<img width="1175" height="769" alt="image" src="https://github.com/user-attachments/assets/717210ae-275d-4725-9d86-ca8b5d4266f9" />


### 주요 기능
- RESTful API 서버: 외부 IoT 디바이스로부터 센서 데이터를 수신하여 데이터베이스에 저장하는 API 엔드포인트 제공

- 실시간 공기질 대시보드:

-- 온도, 습도, 미세먼지(PM1.0, PM2.5), CO2 등 최신 데이터를 실시간으로 확인

-- 날짜를 선택하여 해당일의 시간대별 데이터 변화를 라인 차트로 시각화

- 운행 기록 시각화

-- JSON 형식의 주행 로그 데이터를 읽어 Leaflet.js 지도 위에 이동 경로를 표시

-- 시간에 따른 속도, RPM, 가속/브레이크 변화를 Chart.js를 이용해 동적으로 시각화

- Smartthings 에어컨
-- 에어컨 관리, 데이터 확인 및 제어 API 추가

### 기술 스택
Backend
- Java 17

- Spring Boot 3.x

- Spring Data JPA

- PostgreSQL

- Hibernate

- Lombok

Frontend
- HTML5 / CSS3

- JavaScript

- Bootstrap 5

- Chart.js

- Leaflet.js

- Thymeleaf

DevOps
- Git & GitHub

- Gradle: 빌드 자동화

시스템 아키텍처
본 프로젝트는 전형적인 3-Tier 아키텍처를 따릅니다.

#### 1. Client (IoT Device & Web Browser):

- IoT 디바이스: 주기적으로 수집한 센서 데이터를 Backend API 서버로 POST 요청 전송

- 웹 브라우저: 사용자가 대시보드에 접속하면, 비동기(fetch)로 API 서버에 데이터를 요청하여 화면을 동적으로 업데이트

#### 2. Server (Spring Boot Application):

- @RestController를 통해 외부 요청을 처리하는 API 계층

- @Service를 통해 비즈니스 로직을 처리하는 서비스 계층

- @Repository(Spring Data JPA)를 통해 데이터베이스와 통신하는 데이터 접근 계층

#### 3. Database (PostgreSQL):

- IoT 디바이스로부터 수집된 모든 시계열(Time-series) 데이터가 영구적으로 저장됩니다.

### 시작하기
- JDK 17 이상

- PostgreSQL 데이터베이스

- (선택) 데이터를 전송할 IoT 디바이스

1. 소스 코드 복제
git clone https://github.com/your-username/your-repository.git
cd your-repository

2. 데이터베이스 설정
src/main/resources/application.properties 파일의 spring.datasource 관련 설정을 본인의 PostgreSQL 환경에 맞게 수정합니다.
spring.datasource.url=jdbc:postgresql://localhost:5432/jeonsydb
spring.datasource.username=your_username
spring.datasource.password=your_password

3. 애플리케이션 실행
서버가 정상적으로 실행되면 http://localhost:80 (또는 application.properties에 설정된 포트)로 접속하여 대시보드를 확인할 수 있습니다.

### API 엔드포인트
AirSensor API (/api/airsensor)
- POST /: 새로운 센서 데이터를 저장합니다.

-- Request Body: AirSensorDto 형식의 JSON 데이터

- GET /latest/{deviceId}: 특정 디바이스의 가장 최근 센서 데이터를 조회합니다.

- GET /daily?deviceId={id}&date={yyyy-MM-dd}: 특정 디바이스의 지정된 날짜 데이터를 모두 조회합니다.
