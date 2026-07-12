![WalkingMate Banner](./assets/screens/banner_ALTools_AIUpscaler.png)

# WalkingMate

> 나의 일상 속 워킹 메이트  
> 한이음 드림업(ICT 멘토링) 프로젝트  
> 진행 기간: 2024.04.01 ~ 2024.10.31

WalkingMate는 걷기 기록, 메이트 모집, 피드, 채팅을 한 앱에서 제공하는 Android 서비스입니다.

- 진행 인원: 3명 (공모전 기간 공동 개발)
- 공모전 종료 후 리팩토링 및 기능 개선은 1인으로 지속 진행 중

## 주요 기능
- 산책 기록(거리/시간/경로)
- 메이트 모집 글 작성 및 신청/수락 관리
- 피드 업로드 및 조회
- 1:1 및 그룹 채팅
- 지도 기반 경로 표시
- 음악 추천/재생 기능

## 화면 및 기능 설명
### 인증
로그인과 회원가입, 네이버 간편 로그인으로 진입합니다.

| 로그인 | 회원가입 |
|---|---|
| <img src="./assets/screens/login-screen.jpg" width="180" /> | <img src="./assets/screens/signup-screen.jpg" width="180" /> |

### 메인 탭
하단 탭에서 홈/게시판/기록을 중심으로 이동합니다.

| 워킹 홈 | 게시판 | 기록 캘린더 |
|---|---|---|
| <img src="./assets/screens/screen01.jpg" width="170" /> | <img src="./assets/screens/screen02.jpg" width="170" /> | <img src="./assets/screens/screen03.jpg" width="170" /> |

- 홈: 오늘 걸음, 칼로리, 주간 요약 확인
- 게시판: 메이트 모집글 탐색/검색
- 기록: 날짜별 산책 기록 확인

### 트래킹/메이트
실시간 트래킹, 종료 처리, 메이트 신청/관리 흐름입니다.

| 트래킹 지도 | 종료 확인 | 신청 목록 관리 |
|---|---|---|
| <img src="./assets/screens/screen06.jpg" width="170" /> | <img src="./assets/screens/screen07.jpg" width="170" /> | <img src="./assets/screens/screen10.jpg" width="170" /> |

- 실시간 걸음/거리/시간 측정
- 기록 저장 여부를 종료 시점에 선택
- 신청자 수락/거절 및 채팅 연결

### 채팅/프로필
메이트 사용자와 채팅하고 프로필 정보를 확인합니다.

| 1:1 채팅 | 참가자 목록 | 사용자 프로필 |
|---|---|---|
| <img src="./assets/screens/screen11.jpg" width="170" /> | <img src="./assets/screens/screen12.jpg" width="170" /> | <img src="./assets/screens/screen13.jpg" width="170" /> |

### 음악 추천/템포 워킹
음악 유사도 추천과 템포 재생으로 걷기 페이스를 조절합니다.

| 음악 추천 메인 | 추천 상세 | 인터벌 트레이닝 |
|---|---|---|
| <img src="./assets/screens/screen15.jpg" width="170" /> | <img src="./assets/screens/screen16.jpg" width="170" /> | <img src="./assets/screens/screen17.jpg" width="170" /> |

| LOW 템포 | MAX 템포 |
|---|---|
| <img src="./assets/screens/screen18.jpg" width="180" /> | <img src="./assets/screens/screen21.jpg" width="180" /> |

## 기술 스택
### Android
<p>
  <img src="https://img.shields.io/badge/Android-34A853?style=flat-square&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Gradle-8.7-02303A?style=flat-square&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/AGP-8.5.2-3DDC84?style=flat-square&logo=androidstudio&logoColor=white" />
  <img src="https://img.shields.io/badge/compileSdk-33-0A66C2?style=flat-square" />
  <img src="https://img.shields.io/badge/targetSdk-33-0A66C2?style=flat-square" />
  <img src="https://img.shields.io/badge/minSdk-31-0A66C2?style=flat-square" />
</p>

### Backend (BaaS) / Database / Storage
<p>
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Cloud%20Firestore-FFCA28?style=flat-square&logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Realtime%20Database-FFCA28?style=flat-square&logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Firebase%20Storage-FFCA28?style=flat-square&logo=firebase&logoColor=black" />
</p>

### Network
<p>
  <img src="https://img.shields.io/badge/Retrofit-2.9.0-3E4348?style=flat-square" />
  <img src="https://img.shields.io/badge/Volley-1.2.1-3E4348?style=flat-square" />
</p>

### Map
<p>
  <img src="https://img.shields.io/badge/Naver%20Maps%20SDK-3.15.0-03C75A?style=flat-square" />
</p>

## 프로젝트 구조
```text
WalkingMate/
  app/
  assets/
    screens/
  gradle/
  build.gradle
  settings.gradle
  gradlew
```

## 수행 확인서

![alt text](image.png)