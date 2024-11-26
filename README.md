<div align="center">
  <h1 align="center">Share Space</h1>

  ![version](https://img.shields.io/badge/version-1.0.0-blue.svg?cacheSeconds=2592000)

  <p align="center">
    <h3 align="center">쉐어스페이스는 좁은 주거 공간, 계절 용품 보관 문제, 취미 장비 관리 등</h3>
    <h3 align="center">공간 부족 문제를 해결해 줄 수 있는 이웃 간의 공간 공유 플랫폼입니다.</h3>
  </p>

  [🔗 API 명세](https://jagged-tang-bdd.notion.site/API-3ea854a2962f4e6f95e5dde8d293b570?pvs=74) 
  | [📋 기능 명세](https://jagged-tang-bdd.notion.site/add7338a14d44881ad5dc88ecdaeda69?pvs=73) 
  | [☢️ 트러블슈팅](https://jagged-tang-bdd.notion.site/c463d2527b454ef4a706db03f4d0f4a8) 
  | [🛠️ 기술 문서](https://jagged-tang-bdd.notion.site/0a620ee6ced74a9c88540ffe7e56b4e6)
</div>

<br>



## 👥 팀원
|                                                                이용학                                                                |                                                                남윤형                                                                |                                                               전창민                                                                |                                                                                                               
|:---------------------------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------------------------------:| 
| <img width="160px" src="https://github.com/user-attachments/assets/ce42121f-3ddc-4710-b037-b366222720d5" /> | <img width="160px" src="https://github.com/user-attachments/assets/cb2bf7fa-691f-4de4-b65f-565c9fce0d1f" />| <img width="160px" src="https://github.com/user-attachments/assets/d9c3231e-6051-424b-8468-87cb3f8b747d"/> |
|                                             [@Dradradre](https://github.com/Dradradre)                                              |                                          [@yunhyungNAM](https://github.com/yunhyungNAM)                                           |                                         [@thereisname](https://github.com/thereisname)                                         |
<br>


## 🛠️ Tech Stacks

| 🏷️ **Category**     | 🚀 **Technologies**                                                                              |
|--------------------|-------------------------------------------------------------------------------------------------|
| **📚 Frameworks**    | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen) ![Java](https://img.shields.io/badge/Java-17-blue)    |
| **🗄️ Databases**      | ![MySQL](https://img.shields.io/badge/MySQL-5.7.0-orange) ![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-2.7.0-brightgreen) ![Hibernate ORM](https://img.shields.io/badge/Hibernate%20ORM-6.0.1-red) ![QueryDSL](https://img.shields.io/badge/QueryDSL-5.0.0-lightgrey) |
| **🔐 Security**       | ![Spring Security](https://img.shields.io/badge/Spring%20Security-blue) ![JWT](https://img.shields.io/badge/JWT-0.11.2-yellow)  |
| **☁️ Cloud Services** | ![AWS S3](https://img.shields.io/badge/AWS%20S3-1.12.535-ff69b4)                                                               |
| **🛠️ Utilities**      | ![Lombok](https://img.shields.io/badge/Lombok-1.18.22-brightgreen) ![Spring AOP](https://img.shields.io/badge/Spring%20AOP-blue) ![ANTLR](https://img.shields.io/badge/ANTLR-4.10.1-orange) |
| **🧪 Testing**        | ![JUnit](https://img.shields.io/badge/JUnit-5.7.0-brightgreen) ![Mockito](https://img.shields.io/badge/Mockito-3.9.0-yellow)    |
| **🏗️ Build Tools**    | ![Gradle](https://img.shields.io/badge/Gradle-7.3.3-brightgreen) ![Dependency Management](https://img.shields.io/badge/Dependency%20Management-1.1.6-orange) |


<br>


## 🏗️ 서버 아키텍처
### [배포 URL](https://sharespace.store)
<div align="">
  <a href="https://sharespace.store" target="_blank">
    <img src="https://img.shields.io/badge/%F0%9F%94%97%20ACCESS%20SERVER-sharespace.store-brightgreen" alt="Access Server">
  </a>
</div>

### 아키텍처
<p align="">
  <img width="80%" src="https://github.com/user-attachments/assets/4bc2ab2b-8762-43e6-8bbc-365813b7560f" alt="ShareSpace 서버 아키텍처">
</p>

<br>

## 🚀 구현 사항

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
#### 🏡 장소 관리 시스템
- 장소 등록 및 수정 기능 구현 (다중 이미지 처리 및 이미지 URL 데이터베이스 저장 포함)
- 특정 물품에 적합한 장소 리스트 조회 기능 개발 (매칭 로직 설계 및 거리 계산 알고리즘 구현)
- 장소 수정 시 이미지 삭제 및 추가 로직 최적화
- 거리 계산 로직 반올림 처리 및 정수 단위 반환
- `PENDING`, `STORED` 상태 물품만 장소와 매칭되도록 필터링 로직 설계

#### 📬 쪽지 관리 시스템
- 쪽지 전송 및 수신 로직 개발
- 읽지 않은 쪽지 개수 실시간 조회 기능 구현
- `PENDING`, `STORED` 상태 사용자에게만 쪽지 전송 가능하도록 권한 로직 설계
- 발신 및 수신 대상 사용자 리스트 동적 조회
- N+1 문제 해결을 위한 JPQL 최적화 및 효율적 데이터 조회 구현

#### 📷 이미지 처리
- AWS S3를 활용한 이미지 등록, 수정, 삭제 로직 개발
- 다중 파일 업로드 및 삭제 최적화
- 장소 수정 시 기존 이미지 유지 및 신규 이미지 추가 처리 로직 구현
- 이미지 확장자 제한 (JPEG, PNG)
- 비용 효율성을 고려한 다중 파일 삭제 로직 설계
- 장소 수정 시 변경된 이미지만 S3에 반영하는 로직 구현

#### 🏗️ 인프라 구축
- GitHub Actions를 사용한 CI/CD 자동 배포 파이프라인 구축
- Docker Compose 기반 컨테이너 배포 환경 설계
- AWS VPC, EC2, RDS를 활용한 배포 환경 구축
- Public/Private Subnet을 통한 네트워크 분리 및 보안 그룹 설정 최적화
- Docker Hub와 연동해 자동화된 이미지 빌드 및 푸시 프로세스 구현
- EC2 상에서 Docker Compose를 활용한 컨테이너 실행 및 관리
