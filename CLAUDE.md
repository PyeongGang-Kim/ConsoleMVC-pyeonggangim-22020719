# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 프로젝트 개요

Java 17 콘솔 환경에서 순수 Java(외부 프레임워크 없음)로 MVC 아키텍처 패턴을 구현하는 주문 관리 시스템 POC.  
상세 요구사항은 `PRD.md`, 구현 단계 계획은 `PLAN.md`를 참조한다.

---

## 빌드 및 실행 명령

```bash
# 컴파일
./gradlew compileJava

# 전체 테스트 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.example.consolemvc.service.OrderServiceTest"

# 단일 테스트 메서드 실행
./gradlew test --tests "com.example.consolemvc.service.OrderServiceTest.createOrder_정상_생성"

# 애플리케이션 실행
./gradlew run

# 빌드 (컴파일 + 테스트 + JAR)
./gradlew build
```

> Windows 환경에서는 `./gradlew` 대신 `gradlew.bat` 또는 `gradlew` 사용

---

## 아키텍처

### 레이어 구조 및 의존성 방향

```
View → Controller → Service → Repository → Model
         ↑                        ↑
     (입력 전달)             (인터페이스만 참조)
```

- **허용**: Controller → Service, Controller → View, Service → Repository(인터페이스), Repository → Model
- **금지**: Model이 다른 레이어 import, View가 Service·Repository 직접 호출

### 패키지

```
com.example.consolemvc
├── model/         Order.java, OrderStatus.java
├── repository/    OrderRepository.java (interface), InMemoryOrderRepository.java
├── service/       OrderService.java
├── controller/    OrderController.java
├── view/          OrderView.java
├── exception/     OrderNotFoundException.java, InvalidOrderStateException.java
└── Application.java   (main — 수동 DI 조립 + 메뉴 루프)
```

### 핵심 설계 결정

- `Order.changeStatus()`에서 상태 전이 유효성 검증 → 도메인 규칙은 도메인 객체가 보호
- `OrderService`는 `OrderRepository` 인터페이스만 참조 → 구현체 교체 시 Service 무수정
- `Application.main()`에서 수동 DI 조립 → 외부 프레임워크 없이 의존성 주입 패턴 시연
- View는 `System.out` 출력과 raw 입력 반환만 담당; 예외 처리·비즈니스 판단 금지

---

## 개발 워크플로 (TDD)

이 프로젝트는 **TDD(Test-Driven Development)** 방식으로 진행한다.  
모든 구현은 아래 사이클을 반드시 준수한다.

```
RED → [사용자 검증] → GREEN → [사용자 검증] → Commit & Push
```

### RED 단계
1. 구현 전에 테스트 코드를 먼저 작성한다.
2. 테스트는 반드시 실패(`mvn test` 실패)하는 상태여야 한다.
3. **테스트 코드 작성 완료 후 사용자에게 검증을 요청하고, 승인을 받은 후에만 GREEN으로 진행한다.**

### GREEN 단계
1. RED 단계의 테스트를 통과시키는 최소한의 구현만 작성한다.
2. 테스트가 통과(`mvn test` 성공)하면 사용자에게 검증을 요청한다.
3. **사용자 검증 통과 후에만 커밋 & 푸시를 진행한다.**

### 단계별 체크포인트

| 단계 | Claude가 할 일 | 사용자 승인 필요 |
|------|---------------|-----------------|
| RED | 테스트 코드 작성 → `mvn test` 실패 확인 | ✅ 필수 |
| GREEN | 구현 코드 작성 → `mvn test` 통과 확인 | ✅ 필수 |
| Commit & Push | 커밋 메시지 작성 후 push | 사용자 승인 후 실행 |

> 사용자 승인 없이 GREEN 구현 진행, 커밋, 푸시를 단독으로 수행하지 않는다.

---

## 구현 단계 (PLAN.md 요약)

| Phase | 내용 | 주요 파일 |
|-------|------|-----------|
| 1 | 프로젝트 골격 | `pom.xml`, 패키지 구조 |
| 2 | 도메인 모델 | `Order`, `OrderStatus`, 예외 클래스 |
| 3 | 데이터 레이어 | `OrderRepository` (interface + InMemory 구현체) |
| 4 | 비즈니스 레이어 | `OrderService` |
| 5 | UI + 진입점 통합 | `OrderView`, `OrderController`, `Application` |

현재 진행 중인 Phase와 RED/GREEN 상태는 대화 맥락에서 추적한다.

---

## 테스트 작성 규칙

- 테스트 클래스는 `src/test/java/com/example/consolemvc/` 하위에 위치
- 메서드명은 `{행위}_{조건}_{기대결과}` 형식 (예: `createOrder_수량_0_이하_예외`)
- 각 테스트는 독립적으로 실행 가능해야 함 (공유 상태 금지)
- `OrderService` 테스트는 `InMemoryOrderRepository`를 직접 사용 (Mock 사용 안 함)
