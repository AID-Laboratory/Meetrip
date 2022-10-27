# Meetrip Android Application
 
제 6회 Mobius 개발자대회 안드로이드 어플리케이션
## 사용전 필수 사항
1. Project Clone 및 import 후 Android Studio의 드롭다운 메뉴에서  
1. File/Invalidate Caches   
1. Build/rebuild project 진행


## 할 일 목록

### Android Application

- [x]  [영우] 밴드 데이터 줄이기
- [x]  [영우] 밴드 연결 자동화
- [x]  [영우] UserID에 따라 CNT구분하기
- [x]  [영우] 파이어베이스 구독신청

- [x]  [영우] 위치정보
    - [x]  [영우] 위치정보 Mobius로 전달
    - [x]  [영우] weather, air pollution Mobius로 전달

- [x]  [재현, 상우, 재원, 영우] 이미지 촬영
    - [x]  DCIM/Meetrip폴더 생성
    - [x]  [재원] 사진촬영
    - [x]  [재원] DCIM/Meetrip폴더에 사진 저장
    - [x]  [재원] 저장위치 및 이름 string으로 리턴
    - [x]  [영우] 이미지 firebase로 전달
    - [x]  [영우] 이미지 저장 위치 Mobius로 전달
    
- [ ]  앱 디자인 수정 및 사용성 증진

### Python Code On Server

- [x]  [영우] 이미지 불러오기(Python Code)
    - [x]  [영우] 파이어베이스 샘플이미지 업로드
    - [x]  [영우] 파이어베이스에서 데이터 받기

- [ ]  [현범, 영우] 경로 이미지 표시(Python Code)
    - [x]  [영우] 특정시간의 +-X분의 경로데이터를 가져오기
    - [x]  [영우] csv파일이나 json등을 활용하여 저장
    - [ ]  [현범] 2번의 파일을 가져와서 지도에 표시(카카오맵)
    - [ ]  [현범] 경로가 표시된 지도를 이미지로 저장
    - [x]  [영우] 지도를 제외한 경로만 이미지로 저장(Backup Plan)

### Metaverse Server

- [ ]  [재원] 메타버스 NPC
    - [ ]  NPC구현 및 코드로 데이터 전달 가능한 모듈이 있는지 확인
        - [ ]  구현 불가시 매크로(코드 노가다) 사용 검토

- [ ]  [재원] 메타버스 이미지 표시하기
    - 모듈이 지원하는 마인크래프트 서버 버전 확인 필수
    - [ ]  이미지를 표시하는 모듈 확인
        - [ ]  구현 불가시 매크로(코드 노가다) 사용 검토
    
- [ ]  [재현, 상우] 메타버스 텍스트 표시하기
    - [ ]  텍스트를 표시할 방법에 따라 모듈이 있는지 확인하기
    - 모듈이 지원하는 마인크래프트 서버 버전 확인 필수
        - [ ]  상자에 노트 넣기
        - [ ]  표지판으로 표시하기
        - [ ]  구현 불가시 매크로(코드 노가다) 사용 검토 => 코드로 안되면 이거 진행

#### 맵 디자인 노가다

- [ ]  [재현, 상우] 메타버스 월드 구현하기(맵 만들기)
    - [ ]  광장 => world 찾아보기
    - [ ]  새로운 Tag 확인시 추가 필드 생성 => setBlock 활용
    - [ ]  광장에서 필드로 텔레포트 => MCPI getPos, setPos 사용

- [ ]  메타버스 세계의 포스트 조회시간 구현하기
    - [ ]  포스팅 에리어 특정 구간 계산하기
    - [ ]  해당 구간에 들어오는 플레이어ID마다 시간 계산하여 data저장하기
    - [ ]  데이터 구조는 다음과 같음 post_id, player, time
