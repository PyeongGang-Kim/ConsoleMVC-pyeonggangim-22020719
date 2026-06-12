# ConsoleMVC — Java 콘솔 MVC 주문 관리 시스템

Java 17 콘솔 환경에서 외부 프레임워크 없이 순수 Java로 MVC 아키텍처 패턴을 구현한 POC 프로젝트입니다.

---

## 목적

- MVC 레이어 역할 분리 실증 (Model · View · Controller)
- 단방향 의존성 흐름 설계 검증
- 수동 DI(Dependency Injection) 패턴 시연
- TDD(Red → Green) 기반 개발 프로세스 적용

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Java 17 |
| 빌드 | Gradle 8+ (Kotlin DSL) |
| 테스트 | JUnit 5 |
| 저장소 | InMemory (HashMap) |
| 외부 의존성 | 없음 |

---

## 아키텍처

```
View → Controller → Service → Repository → Model
```

| 레이어 | 클래스 | 책임 |
|--------|--------|------|
| Model | `Order`, `OrderStatus` | 도메인 상태 및 상태 전이 규칙 보유 |
| Repository | `OrderRepository` (interface), `InMemoryOrderRepository` | 인메모리 CRUD |
| Service | `OrderService` | 비즈니스 규칙, 입력 유효성 검증 |
| Controller | `OrderController` | 입력 파싱, Service 호출, 예외 처리 |
| View | `OrderView` | 콘솔 출력 및 raw 입력 반환 |

**의존성 규칙**
- 허용: `Controller → Service → Repository → Model`, `Controller → View`
- 금지: Model이 다른 레이어 참조, View가 Service·Repository 직접 호출

---

## 주문 상태 전이

```
PENDING ──→ CONFIRMED
   │              │
   └──→ CANCELLED ←┘
```

| 현재 상태 | 전이 가능 |
|-----------|-----------|
| PENDING | CONFIRMED, CANCELLED |
| CONFIRMED | CANCELLED |
| CANCELLED | 불가 |

---

## 빌드 및 실행

```bash
# 컴파일
gradlew compileJava

# 테스트 실행
gradlew test

# 단일 테스트 클래스 실행
gradlew test --tests "com.example.consolemvc.service.OrderServiceTest"

# 애플리케이션 실행
gradlew run --console=plain
```

---

## 패키지 구조

```
src/
├── main/java/com/example/consolemvc/
│   ├── Application.java              # 진입점 — 수동 DI 조립 + 메뉴 루프
│   ├── model/
│   │   ├── Order.java
│   │   └── OrderStatus.java
│   ├── repository/
│   │   ├── OrderRepository.java      # 인터페이스
│   │   └── InMemoryOrderRepository.java
│   ├── service/
│   │   └── OrderService.java
│   ├── controller/
│   │   └── OrderController.java
│   ├── view/
│   │   └── OrderView.java
│   └── exception/
│       ├── OrderNotFoundException.java
│       └── InvalidOrderStateException.java
└── test/java/com/example/consolemvc/
    ├── model/
    │   ├── OrderTest.java            # 6개
    │   └── OrderStatusTest.java      # 6개
    ├── repository/
    │   └── OrderRepositoryTest.java  # 5개
    ├── service/
    │   └── OrderServiceTest.java     # 7개
    └── controller/
        └── OrderControllerTest.java  # 4개
```

---

## 기능

| 메뉴 | 기능 | 설명 |
|------|------|------|
| 1 | 주문 생성 | 주문자·상품명·수량 입력, 자동 ID 부여 |
| 2 | 전체 조회 | ID 오름차순 테이블 출력 |
| 3 | 단건 조회 | ID로 주문 상세 조회 |
| 4 | 상태 변경 | 허용된 상태 전이만 가능 |
| 5 | 주문 취소 | 상태를 CANCELLED로 변경 (논리 삭제) |
| 0 | 종료 | 프로세스 종료 |

---

## 에러 처리

| 상황 | 처리 |
|------|------|
| 존재하지 않는 주문 ID | `OrderNotFoundException` → Controller catch → 오류 메시지 출력 |
| 허용되지 않는 상태 전이 | `InvalidOrderStateException` → 동일 |
| 숫자 입력란에 문자 입력 | Controller에서 파싱 예외 처리 후 재입력 요청 |
| 공백·빈 문자열 입력 | Controller 유효성 검사 후 재입력 요청 |

---

## 테스트 현황

```
model.OrderStatusTest      6 / 6  PASSED
model.OrderTest            6 / 6  PASSED
repository.OrderRepositoryTest  5 / 5  PASSED
service.OrderServiceTest   7 / 7  PASSED
controller.OrderControllerTest  4 / 4  PASSED
─────────────────────────────────────────
합계                      28 / 28 PASSED
```
