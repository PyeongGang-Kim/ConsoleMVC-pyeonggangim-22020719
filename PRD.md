# PRD.md — Java Console MVC POC: 주문 관리 시스템

## 1. 프로젝트 개요

Java 콘솔 환경에서 MVC(Model-View-Controller) 아키텍처 패턴을 적용한 주문 관리 시스템 POC(Proof of Concept).  
외부 프레임워크 없이 순수 Java로 MVC의 역할 분리, 의존성 흐름, 레이어 간 인터페이스 설계를 검증하는 것이 목표다.

---

## 2. 목표 (Goals)

| # | 목표 | 성공 기준 |
|---|------|-----------|
| G1 | MVC 레이어 역할 분리 검증 | Model·View·Controller가 각자의 책임만 가지며 서로 직접 참조하지 않음 |
| G2 | 단방향 의존성 흐름 확인 | Controller → Model, Controller → View 방향만 허용; Model·View는 서로 모름 |
| G3 | 콘솔 UI와 비즈니스 로직 완전 분리 | View 코드 전체 교체 시 Model·Controller 무수정 |
| G4 | 주문 CRUD 전 흐름 동작 | 생성·조회·수정·취소가 오류 없이 동작 |

---

## 3. 범위 (Scope)

### In Scope
- 주문 생성, 전체 조회, 단건 조회, 상태 변경(수정), 취소(논리 삭제)
- 인메모리 저장소(Map 기반) — 프로세스 재시작 시 초기화됨
- 콘솔(stdin/stdout) 기반 텍스트 메뉴 UI
- 입력값 유효성 검사(Controller 레이어)

### Out of Scope
- DB·파일 연동, 네트워크 통신
- 인증/인가
- 동시성(멀티스레드) 처리
- GUI, 웹 UI

---

## 4. 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Java 17 |
| 빌드 | Gradle 8+ (Kotlin DSL) |
| 저장소 | 인메모리 (`HashMap`, `ArrayList`) |
| 외부 의존성 | 없음 (stdlib만 사용) |
| 테스트 | JUnit 5 (단위 테스트) |

---

## 5. 아키텍처

### 5.1 레이어 구조

```
┌─────────────────────────────────────────┐
│               View Layer                │
│  (콘솔 입출력 담당 — System.in/out)       │
│  OrderView                              │
└────────────────┬────────────────────────┘
                 │ 사용자 입력 전달 / 출력 위임
┌────────────────▼────────────────────────┐
│            Controller Layer             │
│  (입력 검증 · 흐름 제어 · 서비스 호출)    │
│  OrderController                        │
└────────────────┬────────────────────────┘
                 │ 비즈니스 로직 호출
┌────────────────▼────────────────────────┐
│             Service Layer               │
│  (비즈니스 규칙 · 도메인 로직)            │
│  OrderService                           │
└────────────────┬────────────────────────┘
                 │ 데이터 접근
┌────────────────▼────────────────────────┐
│           Repository Layer              │
│  (인메모리 CRUD — HashMap)              │
│  OrderRepository                        │
└─────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│             Model (Domain)              │
│  Order, OrderStatus(enum)               │
└─────────────────────────────────────────┘
```

### 5.2 의존성 규칙

- **허용**: Controller → Service → Repository → Model, Controller → View
- **금지**: Model이 다른 레이어를 알거나, View가 Service/Repository를 직접 호출하는 것

### 5.3 패키지 구조

```
com.example.consolemvc
├── model/
│   ├── Order.java          # 주문 도메인 객체
│   └── OrderStatus.java    # PENDING · CONFIRMED · CANCELLED (enum)
├── repository/
│   └── OrderRepository.java
├── service/
│   └── OrderService.java
├── controller/
│   └── OrderController.java
├── view/
│   └── OrderView.java
└── Application.java        # main() — DI 조립 · 메뉴 루프 진입
```

---

## 6. 도메인 모델

### Order

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | `long` | 자동 증가 ID |
| `customerName` | `String` | 주문자 이름 |
| `productName` | `String` | 상품명 |
| `quantity` | `int` | 수량 (1 이상) |
| `status` | `OrderStatus` | 주문 상태 |
| `createdAt` | `LocalDateTime` | 생성 시각 |

### OrderStatus (enum)

```
PENDING → CONFIRMED → (완료)
PENDING → CANCELLED
CONFIRMED → CANCELLED
```

---

## 7. 주요 기능 요구사항

### F-01 주문 생성
- 입력: 주문자 이름, 상품명, 수량
- 수량은 1 이상 정수; 이름·상품명은 공백 불가
- 생성 후 부여된 ID를 콘솔에 출력

### F-02 전체 주문 조회
- 모든 주문을 ID 오름차순으로 테이블 형태로 출력
- 주문이 없으면 "등록된 주문이 없습니다." 메시지 출력

### F-03 단건 주문 조회
- 입력: 주문 ID
- 존재하지 않는 ID 입력 시 오류 메시지 후 메뉴로 복귀

### F-04 주문 상태 변경
- 입력: 주문 ID, 변경할 상태
- 허용되지 않는 상태 전이 시 오류 메시지 출력 (예: CANCELLED → CONFIRMED 불가)

### F-05 주문 취소
- 입력: 주문 ID
- 이미 CANCELLED인 주문은 재취소 불가
- 인메모리에서 실제 삭제하지 않고 상태를 CANCELLED로 변경 (논리 삭제)

---

## 8. 콘솔 UI 흐름

```
========== 주문 관리 시스템 ==========
1. 주문 생성
2. 전체 주문 조회
3. 주문 상세 조회
4. 주문 상태 변경
5. 주문 취소
0. 종료
> _
```

- 잘못된 메뉴 번호 입력 시 재입력 요청
- 각 기능 완료 후 메인 메뉴로 자동 복귀
- `0` 입력 시 "시스템을 종료합니다." 출력 후 프로세스 종료

---

## 9. 에러 처리 원칙

| 상황 | 처리 |
|------|------|
| 존재하지 않는 주문 ID | `OrderNotFoundException` throw → Controller에서 catch → View로 메시지 전달 |
| 허용되지 않는 상태 전이 | `InvalidOrderStateException` throw → 동일 |
| 숫자 입력란에 문자 입력 | Controller에서 파싱 예외 처리 후 재입력 요청 |
| 공백/빈 문자열 입력 | Controller 유효성 검사 후 재입력 요청 |

예외 클래스는 `com.example.consolemvc.exception` 패키지에 위치.

---

## 10. 테스트 계획

| 대상 | 테스트 유형 | 도구 |
|------|-------------|------|
| `OrderService` | 단위 테스트 (상태 전이, 비즈니스 규칙) | JUnit 5 |
| `OrderRepository` | 단위 테스트 (CRUD 동작) | JUnit 5 |
| `OrderController` | 단위 테스트 (입력 유효성) | JUnit 5 |
| 전체 흐름 | 수동 콘솔 시나리오 테스트 | — |

---

## 11. POC 완료 기준

- [ ] 모든 F-01 ~ F-05 기능이 콘솔에서 오류 없이 동작
- [ ] Model 클래스가 View·Controller를 import하지 않음
- [ ] View 클래스가 Service·Repository를 import하지 않음
- [ ] `OrderService` 단위 테스트 통과율 100%
- [ ] 잘못된 입력(문자, 음수, 빈값)에 대해 프로세스가 종료되지 않고 재입력 요청

---

## 12. 디렉토리 구조 (Gradle 표준)

```
ConsoleMVC/
├── build.gradle.kts
├── settings.gradle.kts
└── src/
    ├── main/
    │   └── java/com/example/consolemvc/
    └── test/
        └── java/com/example/consolemvc/
```
