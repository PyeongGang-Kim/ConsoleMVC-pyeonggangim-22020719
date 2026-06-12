# PLAN.md — 구현 계획: Java Console MVC 주문 관리 시스템

> PRD 기준: `PRD.md` | 목표 완료: POC 전 기능 동작 + MVC 레이어 분리 검증

---

## 구현 단계 개요

```
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5
프로젝트    도메인     데이터     비즈니스    UI·진입점
골격        모델       레이어     레이어      + 테스트
```

각 Phase는 이전 Phase가 완료된 후 진행한다.  
Phase 1~3은 콘솔 없이 단위 테스트로 검증 가능하며, Phase 4부터 실제 실행이 가능해진다.

---

## Phase 1 — 프로젝트 골격

**목표**: 빌드·테스트가 통과하는 빈 프로젝트 뼈대를 만든다.

### 작업 목록

| # | 파일 | 작업 내용 |
|---|------|-----------|
| 1-1 | `build.gradle.kts` | Gradle 프로젝트 설정 (Java 17, JUnit 5 의존성) |
| 1-2 | `Application.java` | `main()` 메서드만 있는 진입점 클래스 생성 |
| 1-3 | 패키지 폴더 | `model`, `repository`, `service`, `controller`, `view`, `exception` 패키지 생성 |

### build.gradle.kts 핵심 설정

```kotlin
plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
```

### 완료 기준
- `./gradlew compileJava` 성공
- `./gradlew test` 성공 (테스트 0개 상태)

---

## Phase 2 — 도메인 모델

**목표**: 비즈니스 개념을 코드로 정의한다. 이 레이어는 어떤 레이어도 import하지 않는다.

### 작업 목록

| # | 파일 | 작업 내용 |
|---|------|-----------|
| 2-1 | `model/OrderStatus.java` | enum 정의 + 상태 전이 허용 여부 메서드 |
| 2-2 | `model/Order.java` | 도메인 객체 (생성자, getter, 상태 변경 메서드) |
| 2-3 | `exception/OrderNotFoundException.java` | RuntimeException 상속 |
| 2-4 | `exception/InvalidOrderStateException.java` | RuntimeException 상속 |

### 상세 설계

**OrderStatus.java**
```java
public enum OrderStatus {
    PENDING, CONFIRMED, CANCELLED;

    // 허용된 전이인지 검증
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING    -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED  -> next == CANCELLED;
            case CANCELLED  -> false;
        };
    }
}
```

**Order.java**
```java
public class Order {
    private final long id;
    private final String customerName;
    private final String productName;
    private int quantity;
    private OrderStatus status;
    private final LocalDateTime createdAt;

    // 생성자: id, customerName, productName, quantity
    // 상태 변경: changeStatus(OrderStatus newStatus)
    //   → canTransitionTo 검사 후 InvalidOrderStateException
    // getter 전부, setter 없음 (불변 필드는 final)
}
```

### 완료 기준
- `Order`, `OrderStatus` 컴파일 성공
- `OrderStatus.canTransitionTo()` 로직을 직접 단위 테스트로 검증

---

## Phase 3 — 데이터 레이어 (Repository)

**목표**: 인메모리 CRUD를 구현하고 단위 테스트로 검증한다.

### 작업 목록

| # | 파일 | 작업 내용 |
|---|------|-----------|
| 3-1 | `repository/OrderRepository.java` | 인터페이스 정의 |
| 3-2 | `repository/InMemoryOrderRepository.java` | HashMap 기반 구현체 |
| 3-3 | `OrderRepositoryTest.java` | CRUD 단위 테스트 |

### 인터페이스 설계

```java
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(long id);
    List<Order> findAll();
    void update(Order order);      // 상태 변경 후 저장
}
```

### InMemoryOrderRepository 구현 포인트
- `HashMap<Long, Order>` 사용
- `AtomicLong`으로 ID 자동 증가 (스레드 안전 불필요하나 관례상 사용)
- `findAll()`은 ID 오름차순 정렬된 `List` 반환

### 테스트 케이스 (OrderRepositoryTest)
| 테스트 | 검증 내용 |
|--------|-----------|
| `save_저장후_id_부여` | save 후 반환된 Order에 id > 0 |
| `findById_존재하는_id` | 저장한 Order와 동일한 객체 반환 |
| `findById_없는_id` | `Optional.empty()` 반환 |
| `findAll_정렬순서` | 여러 Order 저장 후 ID 오름차순 확인 |
| `update_상태변경_반영` | 상태 변경 후 update 호출 → findById로 확인 |

### 완료 기준
- `./gradlew test` — `OrderRepositoryTest` 전체 통과

---

## Phase 4 — 비즈니스 레이어 (Service)

**목표**: 비즈니스 규칙을 Service에 집중시키고, Repository는 인터페이스로만 참조한다.

### 작업 목록

| # | 파일 | 작업 내용 |
|---|------|-----------|
| 4-1 | `service/OrderService.java` | 비즈니스 로직 구현 |
| 4-2 | `OrderServiceTest.java` | 비즈니스 규칙 단위 테스트 |

### OrderService 메서드 설계

```java
public class OrderService {
    private final OrderRepository repository;

    // 생성자 주입
    public OrderService(OrderRepository repository) { ... }

    // F-01: 주문 생성
    public Order createOrder(String customerName, String productName, int quantity);

    // F-02: 전체 조회
    public List<Order> getAllOrders();

    // F-03: 단건 조회
    public Order getOrderById(long id);   // 없으면 OrderNotFoundException

    // F-04: 상태 변경
    public Order changeOrderStatus(long id, OrderStatus newStatus);

    // F-05: 주문 취소 (상태를 CANCELLED로)
    public Order cancelOrder(long id);
}
```

### 테스트 케이스 (OrderServiceTest)

| 테스트 | 검증 내용 |
|--------|-----------|
| `createOrder_정상_생성` | 반환 Order의 status == PENDING |
| `createOrder_수량_0_이하_예외` | `IllegalArgumentException` |
| `createOrder_이름_공백_예외` | `IllegalArgumentException` |
| `getOrderById_없는_id_예외` | `OrderNotFoundException` |
| `changeStatus_PENDING_to_CONFIRMED` | status 변경 확인 |
| `changeStatus_CANCELLED_to_CONFIRMED_예외` | `InvalidOrderStateException` |
| `cancelOrder_이미_취소된_주문_예외` | `InvalidOrderStateException` |

### 완료 기준
- `./gradlew test` — `OrderServiceTest` 전체 통과
- Service가 `InMemoryOrderRepository`를 직접 참조하지 않고 인터페이스(`OrderRepository`)만 참조

---

## Phase 5 — UI 레이어 + 진입점 + 통합

**목표**: 콘솔 UI를 붙이고 전체 흐름을 연결한다.

### 작업 목록

| # | 파일 | 작업 내용 |
|---|------|-----------|
| 5-1 | `view/OrderView.java` | 입출력 담당 (메뉴, 프롬프트, 결과 출력) |
| 5-2 | `controller/OrderController.java` | 입력 파싱·검증, Service 호출, View로 출력 위임 |
| 5-3 | `Application.java` | DI 조립 + 메뉴 루프 실행 |
| 5-4 | 수동 시나리오 테스트 | PRD §11 완료 기준 체크리스트 확인 |

### View 책임 범위

```
OrderView가 하는 것:
  - 메뉴 출력
  - 입력 프롬프트 출력 및 raw String 반환
  - Order 객체를 받아 포맷팅 후 출력
  - 성공/오류 메시지 출력

OrderView가 하면 안 되는 것:
  - 비즈니스 로직 판단
  - Service/Repository 직접 호출
  - 예외 처리
```

### Controller 책임 범위

```
OrderController가 하는 것:
  - View로부터 raw 입력을 받아 파싱·타입 변환
  - 유효성 검사 (빈값, 범위)
  - Service 메서드 호출
  - 예외(OrderNotFoundException 등) catch → View로 오류 메시지 전달
  - 각 메뉴 번호에 대응하는 handle*() 메서드 보유

OrderController가 하면 안 되는 것:
  - 직접 System.out.println
  - Repository 직접 호출
```

### Application.java DI 조립 패턴

```java
public class Application {
    public static void main(String[] args) {
        OrderRepository repository = new InMemoryOrderRepository();
        OrderService service = new OrderService(repository);
        OrderView view = new OrderView();
        OrderController controller = new OrderController(service, view);

        // 메뉴 루프
        Scanner scanner = new Scanner(System.in);
        while (true) {
            view.printMenu();
            String input = scanner.nextLine().trim();
            if ("0".equals(input)) {
                view.printMessage("시스템을 종료합니다.");
                break;
            }
            controller.handleMenu(input);
        }
    }
}
```

### 수동 시나리오 체크리스트

| 시나리오 | 기대 결과 |
|----------|-----------|
| 1 → 주문 생성 (정상) | ID 출력 후 메뉴 복귀 |
| 1 → 수량에 `-1` 입력 | 오류 메시지 + 재입력 요청 |
| 1 → 이름에 공백만 입력 | 오류 메시지 + 재입력 요청 |
| 2 → 전체 조회 (빈 상태) | "등록된 주문이 없습니다." |
| 3 → 없는 ID 입력 | 오류 메시지 + 메뉴 복귀 |
| 4 → CANCELLED → CONFIRMED 시도 | 상태 전이 오류 메시지 |
| 5 → 이미 취소된 주문 취소 | 오류 메시지 + 메뉴 복귀 |
| 0 → 종료 | "시스템을 종료합니다." 출력 후 종료 |
| 메뉴에서 `abc` 입력 | "올바른 메뉴를 선택해 주세요." 재출력 |

### 완료 기준
- 위 시나리오 체크리스트 전부 통과
- `./gradlew test` 전체 통과 (Phase 2~4 테스트 포함)

---

## 전체 파일 생성 순서 요약

```
Phase 1: pom.xml, Application.java (shell)
Phase 2: OrderStatus.java, Order.java,
         OrderNotFoundException.java, InvalidOrderStateException.java
Phase 3: OrderRepository.java (interface),
         InMemoryOrderRepository.java,
         OrderRepositoryTest.java
Phase 4: OrderService.java,
         OrderServiceTest.java
Phase 5: OrderView.java,
         OrderController.java,
         Application.java (완성)
```

---

## 주요 설계 결정 및 근거

| 결정 | 근거 |
|------|------|
| Service가 Repository 인터페이스 참조 | 구현체 교체(인메모리 → DB) 시 Service 무수정 |
| `Order.changeStatus()`에서 전이 검증 | 도메인 규칙은 도메인 객체가 보호 (Service에서 검증하면 중복·누락 위험) |
| Controller에서 입력 파싱 예외 처리 | View는 출력만 담당; 파싱 로직이 View에 있으면 책임 혼재 |
| `Application.main()`에서 수동 DI 조립 | 외부 DI 프레임워크 없이 의존성 주입 패턴을 명시적으로 시연 |
| 테스트 시 `InMemoryOrderRepository` 직접 사용 | Mock 없이 실제 구현체로 Service 테스트 → POC 범위에 적합 |
