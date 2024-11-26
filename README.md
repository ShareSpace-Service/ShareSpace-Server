<div align="center">
  <h1 align="center">Share Space</h1>

![version](https://img.shields.io/badge/version-1.0.0-blue.svg?cacheSeconds=2592000)

  <p align="center">
    React.js + TypeScript + Vite
    <br>
    <h3 align="center">쉐어스페이스는 좁은 주거 공간, 계절 용품의 보관 문제, 취미 장비 관리 등</h3>
    <h3 align="center">공간 부족 문제를 해결해 줄 수 있는 이웃 간의 공간 공유 플랫폼입니다.</h3>
  </p>

[🔗 API 명세](https://jagged-tang-bdd.notion.site/API-3ea854a2962f4e6f95e5dde8d293b570?pvs=74) | [📋 기능 명세](https://jagged-tang-bdd.notion.site/add7338a14d44881ad5dc88ecdaeda69?pvs=73) | [☢️ 트러블슈팅](https://jagged-tang-bdd.notion.site/c463d2527b454ef4a706db03f4d0f4a8) | [🛠️ 기술 문서](https://jagged-tang-bdd.notion.site/0a620ee6ced74a9c88540ffe7e56b4e6)

</div>




## 팀원
|                                                                이용학                                                                |                                                                남윤형                                                                |                                                               전창민                                                                |                                                                                                               
|:---------------------------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------------------------------:| 
| <img width="160px" src="https://github.com/user-attachments/assets/ce42121f-3ddc-4710-b037-b366222720d5" /> | <img width="160px" src="https://github.com/user-attachments/assets/cb2bf7fa-691f-4de4-b65f-565c9fce0d1f" />| <img width="160px" src="https://github.com/user-attachments/assets/d9c3231e-6051-424b-8468-87cb3f8b747d"/> |
|                                             [@Dradradre](https://github.com/parkjiye)                                              |                                          [@ChaeyeonSeo](https://github.com/ChaeyeonSeo)                                           |                                         [@hyunjeong408](https://github.com/hyunjeong408)                                         |

## Tech Stacks

| **카테고리**        | **사용 기술**                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| 🛠 **프레임워크**   | Spring Boot 3.3.4, Java 17                                                 |
| 🗄️ **데이터베이스** | MySQL, Spring Data JPA, Hibernate ORM 6.0.1, QueryDSL 5.0.0               |
| 🔒 **보안**         | Spring Security, JWT (jjwt-impl 0.11.2, jjwt-api 0.11.2, jjwt-jackson 0.11.2)|
| ☁️ **클라우드 서비스**| AWS S3 (aws-java-sdk-s3 1.12.535)                                          |
| 🛠 **유틸리티**      | Lombok, Spring AOP, Spring Boot Mail, Validation, ANTLR 4.10.1             |
| 🧪 **테스트**        | JUnit 5.7.0, Mockito 3.9.0                                                |
| 🏗️ **빌드 도구**     | Gradle, Spring Dependency Management Plugin 1.1.6                        |



## 서버 아키텍처
###  배포 URL
https://sharespace.store
![쉐스아키v5](https://github.com/user-attachments/assets/4bc2ab2b-8762-43e6-8bbc-365813b7560f)



## 구현 사항

### 이용학
#### 🔄 매칭 시스템
- `REQUESTED` → `PENDING` → `STORED` → `COMPLETED` 상태 흐름 관리
- 각 상태 변경 시 관련 당사자들에게 실시간 알림 전송
- 매칭 취소 및 거절 시나리오 처리
- 완료된 Matching 건에 대한 History 관리
#### 🔐 권한 관리 시스템
- `@CheckPermission` 커스텀 어노테이션 구현
- Spring AOP를 활용하여 메서드 레벨에서 사용자 권한 검증
- 게스트/호스트 역할별 접근 제어 구현
#### 📨 실시간 알림 시스템
- SSE(Server-Sent Events) 기반 실시간 알림
- 서버에서 클라이언트로의 단방향 실시간 통신 구현
- `ConcurrentHashMap`을 사용한 사용자별 `SseEmitter` 관리
- 알림 스케줄러를 도입하여 매일 자정마다 실행되는 배치 작업 구현 
#### 📊 Response 구조 표준화
- BaseResponse 패턴 구현
- 일관된 API 응답 구조
- 성공/실패 여부 표준화
- HTTP 상태 코드 및 메시지 통합 관리
#### 🏗️ 인프라 개선
- 서버와 클라이언트 도메인을 동일하게 설정하여 쿠키 전송 문제 해결
- GitHub Actions를 활용하여 프론트엔드 빌드 및 배포 자동화
- React 정적 파일을 Nginx를 통해 서빙하여 SPA 환경 최적화
- 모든 API 요청에 `/api` 접두사를 추가하여 정적 파일과 구분

### 남윤형
### 전창민
