# SaveMate: A Personal Finance Management Web Application

## 📖 프로젝트 소개

**SaveMate**는 사용자가 자신의 재무 상태를 효과적으로 관리하고, 건강한 소비 습관을 형성할 수 있도록 돕는 개인 재무 관리 웹 애플리케이션입니다. 이 프로젝트는 최신 기술 스택과 아키텍처 설계를 통해 확장성과 유지보수성을 고려하여 개발되었습니다.

### ✨ **Project Highlights**

*   **하이브리드 데이터베이스 아키텍처:** 관계형 데이터베이스(MariaDB)와 NoSQL 데이터베이스(MongoDB)의 장점을 결합하여 데이터의 특성에 따라 최적의 저장소를 선택하는 하이브리드 영속성 모델을 구현했습니다.
*   **관심사 분리 원칙 적용:** `ViewController`와 `APIController`를 명확히 분리하여, 서버 사이드 렌더링과 클라이언트 요청 처리를 독립적으로 관리함으로써 코드의 가독성과 유지보수성을 향상시켰습니다.
*   **인증 및 인가:** `Spring Security`를 도입하여 안전하고 신뢰할 수 있는 사용자 인증 및 인가 시스템을 구축했습니다.

## 🚀 주요 기능

- **지출 관리:** 지출 내역 기록, 카테고리별 분류 및 월별 조회
- **예산 관리:** 월별/카테고리별 예산 설정 및 지출과 연동된 실시간 분석
- **재정 목표 관리:** 목표 설정 및 달성 현황 추적
- **시각화 대시보드:** 월별 지출 현황, 예산 대비 지출 등 데이터를 시각적으로 제공
- **자동화된 알림:** 예산 초과 시 사용자에게 알림 전송
- **리포트 생성:** 월별/연도별 지출 리포트 생성 및 이메일 전송

## 🛠️ 기술 스택

| 구분 | 기술 |
| --- | --- |
| **Backend** | Java 17, Spring Boot 3, Spring Security, Spring Data JPA, Spring Data MongoDB, QueryDSL, OpenFeign |
| **Database** | MariaDB, MongoDB |
| **Frontend**| Thymeleaf, JavaScript, HTML5/CSS3 |
| **Build** | Gradle |
| **Etc** | Lombok |

## 🏗️ 아키텍처

SaveMate는 다음과 같은 아키텍처 원칙을 기반으로 설계되었습니다.

1.  **(Layered Architecture):**
    *   **Controller Layer:** `ViewController`와 `APIController`를 분리하여 각기 다른 요청 유형을 처리합니다.
    *   **Service Layer:** 비즈니스 로직을 담당하며, 트랜잭션을 관리합니다.
    *   **Repository Layer:** 데이터베이스와의 통신을 담당하며, JPA와 MongoRepository를 사용합니다.
2.  **(Hybrid Persistence):**
    *   **MariaDB (JPA):** 사용자 정보, 예산, 목표 등 정형적이고 트랜잭션이 중요한 데이터 저장
    *   **MongoDB:** 지출 내역과 같이 대용량이며 유연한 스키마가 필요한 데이터 저장
3.  **(Microservice Integration):**
    *   `OpenFeign`을 사용하여 외부 서비스와 선언적으로 통신함으로써, 서비스 간 결합도를 낮추고 유연한 확장을 가능하게 합니다.

## 💡 API Endpoints

주요 API 엔드포인트는 다음과 같습니다.

### Spending

- `GET /spendingAPI`: 지출 내역 조회
- `POST /spendingAPI`: 새 지출 내역 추가
- `PUT /spendingAPI/{id}`: 지출 내역 수정
- `DELETE /spendingAPI/{id}`: 지출 내역 삭제

### Budget

- `POST /budgetAPI`: 새 예산 추가
- `PUT /budgetAPI/{budgetId}`: 예산 수정
- `DELETE /budgetAPI/{budgetId}`: 예산 삭제

## 🏃 시작하기

### 요구 사항

- Java 17
- Gradle
- MariaDB
- MongoDB

### 설정 및 실행

1.  **데이터베이스 설정:**
    *   `src/main/resources` 경로에 `application.properties` 파일을 새로 생성합니다.
    *   생성된 `application.properties` 파일에 MariaDB 및 MongoDB 연결 정보를 올바르게 설정합니다. (예: 데이터베이스 URL, 사용자 이름, 비밀번호 등)
    *   **주의:** 민감한 정보(예: 데이터베이스 비밀번호)는 절대로 Git과 같은 버전 관리 시스템에 커밋하지 않도록 주의하십시오. 환경 변수 또는 별도의 설정 파일을 통해 관리하는 것을 권장합니다.
2.  **의존성 설치:** `./gradlew build`
3.  **실행:** `./gradlew bootRun`