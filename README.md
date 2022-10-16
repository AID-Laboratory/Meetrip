# Meetrip Android Application
 
제 6회 Mobius 개발자대회 안드로이드 어플리케이션
## 사용전 필수 사항
1. Project Clone 및 import 후 Android Studio의 드롭다운 메뉴에서  
1. File/Invalidate Caches   
1. Build/rebuild project 진행


## 할 일 목록

- [x]  [영우] 밴드 데이터 줄이기(메인브랜치)
- [x]  [영우] 밴드 연결 자동화(`HeartRateConsentTask`)
- [x]  [영우] UserID에 따라 CNT구분하기(메인브랜치)
- [x]  [영우] 파이어베이스 구독신청

- [ ]  [상우, 재현] 위치정보(안드로이드)
    - [ ]  위치정보 Mobius로 전달 => 브랜치 파서 사용
    - [ ]  weather, air pollution Mobius로 전달 => 위 브랜치랑 같은 브랜치 활용

- [ ]  [재원, 영우] 이미지 촬영(안드로이드)
    - [ ]  DCIM/Meetrip폴더 생성
    - [ ]  [재원] 사진촬영
    - [ ]  [재원] DCIM/Meetrip폴더에 사진 저장
    - [ ]  [재원] 저장위치 string으로 리턴
    - [x]  [영우] 이미지 firebase로 전달
    - [x]  [영우] 이미지 저장 위치 Mobius로 전달

- [ ]  [재현] 이미지 불러오기(Python Code)
    - [x]  [영우] 파이어베이스 샘플이미지 업로드
    - [x]  [영우] 파이어베이스에서 데이터 받기

- [ ]  [현범, 영우] 경로 이미지 표시(Python Code)
    - [ ]  특정시간의 +-X분의 경로데이터를 가져오기
    - [ ]  csv파일이나 json등을 활용하여 저장
    - [ ]  2번의 파일을 가져와서 지도에 표시(카카오맵)
    - [ ]  경로가 표시된 지도를 이미지로 저장
    - [ ]  지도를 제외한 경로만 이미지로 저장
