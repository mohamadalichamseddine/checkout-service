# Checkout API-first Service

## About
A lightweight **checkout service** built with **Java (Spring Boot)**, following clean architecture principles and documented with an **OpenAPI Specification**.  
It demonstrates a modular approach to cart, checkout, payment, and order management with in-memory persistence for easy demo and testing.

---

## ✨ Features
- **Cart Management** – create carts, add items, view contents
- **Checkout Flow** – compute totals with coupons, tax, shipping
- **Order Lifecycle** – track order states (`AwaitingPayment`, `Paid`, `Failed`)
- **Payment Providers** – configurable providers (`MockPay`, `FailPay`, `CashOnDelivery`)
- **API-First** – documented with an **OpenAPI Specification (openapi.yaml)**
- **In-Memory Persistence** – quick demo setup, easily swappable for a database

---

## Project structure
```
checkout-service/
├─ src/main/
│  ├─ java/com/checkoutservice/
│  │  ├─ api/			# API Layer (Controllers / REST endpoints)
│  │  ├─ app/       	# Application Layer (Orchestration)
│  │  │  └─ beans/			# request/response beans
│  │  ├─ domain/		# Core business logic
│  │  │  ├─ cart/
│  │  │  ├─ order/
│  │  │  ├─ pricing/
│  │  │  └─ payment/
│  │  └─ persistence/	# Persistance Layer (Infra / Repository contracts)
│  │
│  ├─ resources/
│  │  ├─ openapi.yaml   # API contract
│  │  └─ application.properties
│  
├─ pom.xml
└─ README.md
```

## Build and Run 
```
mvn clean package
java -jar target/checkout-service.jar
```

Service will be available at http://localhost:8080


## API Quickstart (How to demo - curl)
Example requests using cURL:
```
# 1. Create a cart
curl -X POST http://localhost:8080/carts \
  -H "Content-Type: application/json" \
  -d '{"currency":"USD"}'

# responce: { "cartId": "c_123" }
 
# 2. Add item
curl -X POST http://localhost:8080/carts/c_123/items \
  -H "Content-Type: application/json" \
  -d '{"productId":"sku-101","qty":2,"unitPrice":10.0}'

# 3. View Cart
curl http://localhost:8080/carts/c_123

# 4. Start checkout
curl -X POST http://localhost:8080/checkouts \
  -H "Content-Type: application/json" \
  -d '{"cartId":"c_123","currency":"USD","couponCode":"WELCOME10","paymentProvider":"MockPay"}'

# 4. Get order
curl http://localhost:8080/orders/o_456
```

## Class Diagrams
Using Mermaid

### 1) Component / Layer Diagram
```mermaid
---
config:
  theme: default
  layout: dagre
---
flowchart LR
    subgraph API["API Layer"]
        API_Checkout["CheckoutController<br>+createCart(job: CreateCartJob): CreateCartResult<br>+addItem(id, job: AddCartItemJob): GetCartResult<br>+getCart(id): GetCartResult<br>+startCheckout(job: CheckoutJob): CheckoutResult<br>+getOrder(id): GetOrderResult"]
    end
    subgraph APP["Application Layer"]
        APP_Facade["CheckoutService<br>+createCart(job: CreateCartJob): CreateCartResult<br>+addItemToCart(id, job: AddCartItemJob): GetCartResult<br>+getCart(id): GetCartResult<br>+start(job: CheckoutJob): CheckoutResult<br>+getOrder(id): GetOrderResult"]
        BEAN_CreateCartResult["CreateCartResult"]
        BEAN_GetOrderResult["GetOrderResult"]
        BEAN_CreateCartJob["CreateCartJob"]
        BEAN_AddCartItemJob["AddCartItemJob"]
        BEAN_CartItemResult["CartItemResult"]
        BEAN_GetCartResult["GetCartResult"]
        BEAN_CheckoutJob["CheckoutJob"]
        BEAN_CheckoutResult["CheckoutResult"]
    end
    subgraph PRICING["Pricing Calculator (CoR)"]
        PR_Base["(AbstractPricingCalculator)"]
        PR_Coupon["CouponCalculator"]
        PR_Tax["TaxCalculator<br>-rate: double"]
        PR_Shipping["ShippingCalculator<br>-shipping: double<br>-freeThreshold: double"]
    end
    subgraph PAYMENT["Payment Strategy"]
        Pay_IF["(PaymentStrategy)"]
        Pay_Mock["MockPayStrategy"]
        Pay_Fail["FailPayStrategy"]
        Pay_COD["CashOnDeliveryStrategy"]
    end
    subgraph ORDER_STATE["State Pattern"]
        St_IF["(OrderState)"]
        St_Await["AwaitingPaymentState"]
        St_Paid["PaidState"]
        St_Failed["FailedState"]
    end
    subgraph DOMAIN["Domain"]
        PRICING
        PAYMENT
        ORDER_STATE
    end
    subgraph PERSIST["Persistence Layer"]
        Repo_Cart_IF["(CartRepository)"]
        Repo_Order_IF["(OrderRepository)"]
        Repo_Cart_Impl["InMemoryCartRepository"]
        Repo_Order_Impl["InMemoryOrderRepository"]
    end
    API_Checkout --> APP_Facade
    APP_Facade --> PRICING & PAYMENT & ORDER_STATE & Repo_Cart_IF & Repo_Order_IF
    Repo_Cart_IF --> Repo_Cart_Impl
    Repo_Order_IF --> Repo_Order_Impl
    Pay_IF --> Pay_Mock & Pay_Fail & Pay_COD
    St_IF --> St_Await & St_Paid & St_Failed
```

### 2) Class Diagram
```mermaid
classDiagram
%% ===================== API =====================
    class CheckoutController {
        +CheckoutController(service: CheckoutService)
        +createCart(job: CreateCartJob): ResponseEntity~CreateCartResult~
        +addItem(id: String, job: AddCartItemJob): ResponseEntity~GetCartResult~
        +getCart(id: String): ResponseEntity~GetCartResult~
        +startCheckout(job: CheckoutJob): ResponseEntity~CheckoutResult~
        +getOrder(id: String): ResponseEntity~GetOrderResult~
    }

%% ===================== Application (Service + Beans) =====================
    class CheckoutService {
        -carts: CartRepository
        -orders: OrderRepository
        -calculator: PricingCalculator
        +CheckoutService(carts: CartRepository, orders: OrderRepository)
        +createCart(job: CreateCartJob): CreateCartResult
        +addItemToCart(id: String, job: AddCartItemJob): GetCartResult
        +getCart(id: String): GetCartResult
        +start(job: CheckoutJob): CheckoutResult
        +getOrder(id: String): GetOrderResult
    }

%% ---- /app/beans (Requests/Responses) ----
    class CreateCartJob { +currency: String }
    class CreateCartResult { +cartId: String }

    class AddCartItemJob {
        +productId: String
        +qty: int
        +unitPrice: double }

    class CartItemResult {
        +productId: String
        +qty: int
        +unitPrice: double
        +lineTotal: double }
    class GetCartResult {
        +id: String
        +currency: String
        +items: List~CartItemResult~ }

    class CheckoutJob {
        +cartId: String
        +currency: String
        +couponCode: String
        +paymentProvider: String
    }
    class CheckoutResult {
        +orderId: String
        +state: String
        +total: double
        +provider: String }

    class GetOrderResult {
        +id: String
        +amount: double
        +currency: String
        +state: String }

%% ===================== Domain: Cart =====================
    class Money {
        -amount: double
        -currency: String
        +Money(amount: double, currency: String)
        +amount(): double
        +currency(): String
        +plus(o: Money): Money
        +minus(o: Money): Money
        +percent(p: double): Money
    }

    class CartItem {
        +productId: String
        +qty: int
        +unitPrice: Money
        +lineTotal(): Money
    }

    class Cart {
        -id: String
        -currency: String
        -items: List~CartItem~
        +Cart(id: String, currency: String)
        +id(): String
        +currency(): String
        +items(): List~CartItem~
        +add(item: CartItem): void
    }

    class CartRepository {
        <<interface>>
        +create(currency: String): String
        +get(id: String): Cart
        +addItem(id: String, item: CartItem): Cart
    }

%% Ownership
Cart "1" o-- "*" CartItem
CartItem *-- Money

%% ===================== Domain: Order (State) =====================
class Order {
-id: String
-amount: Money
-state: OrderState
+Order(amount: Money)
+getId(): String
+amount(): Money
+stateName(): String
+onPaymentSucceeded(): void
+onPaymentFailed(): void
}

class OrderState {
<<interface>>
+paymentSucceeded(): OrderState
+paymentFailed(): OrderState
+name(): String
}
class AwaitingPaymentState
class PaidState
class FailedState

Order *-- Money
Order *-- OrderState
OrderState <|.. AwaitingPaymentState
OrderState <|.. PaidState
OrderState <|.. FailedState

class OrderRepository {
<<interface>>
+save(order: Order): void
+get(id: String): Order
}

%% ===================== Domain: Pricing (CoR) =====================
class PricingContext {
+cart: Cart
+coupon: String
+subtotal: double
+discounts: double
+tax: double
+shipping: double
+total: double
+PricingContext(cart: Cart, coupon: String)
+finalizeTotals(): void
}

class PricingCalculator {
<<interface>>
+setNext(next: PricingCalculator): PricingCalculator
+calculate(ctx: PricingContext): PricingContext
}

class AbstractPricingCalculator {
#next: PricingCalculator
+setNext(next: PricingCalculator): PricingCalculator
+calculate(ctx: PricingContext): PricingContext
#apply(ctx: PricingContext): void
}

class BasePriceCalculator {
+apply(ctx: PricingContext): void
}
class CouponCalculator {
+apply(ctx: PricingContext): void
}
class TaxCalculator {
-rate: double
+TaxCalculator(rate: double)
+apply(ctx: PricingContext): void
}
class ShippingCalculator {
-shipping: double
-freeThreshold: double
+ShippingCalculator(shipping: double, freeThreshold: double)
+apply(ctx: PricingContext): void
}

PricingCalculator <|.. AbstractPricingCalculator
AbstractPricingCalculator <|-- BasePriceCalculator
AbstractPricingCalculator <|-- CouponCalculator
AbstractPricingCalculator <|-- TaxCalculator
AbstractPricingCalculator <|-- ShippingCalculator

%% ===================== Domain: Payment (Strategy) =====================
class PaymentStrategy {
<<interface>>
+pay(amount: double): boolean
+name(): String
}
class MockPayStrategy
class FailPayStrategy
class CashOnDeliveryStrategy

PaymentStrategy <|.. MockPayStrategy
PaymentStrategy <|.. FailPayStrategy
PaymentStrategy <|.. CashOnDeliveryStrategy

%% ===================== Persistence (In-Memory impls) =====================
class InMemoryCartRepository {
-db: Map~String, Cart~
+create(currency: String): String
+get(id: String): Cart
+addItem(id: String, item: CartItem): Cart
}

class InMemoryOrderRepository {
-db: Map~String, Order~
+save(order: Order): void
+get(id: String): Order
}

CartRepository <|.. InMemoryCartRepository
OrderRepository <|.. InMemoryOrderRepository

%% ===================== Key (minimal) relationships across layers =====================
CheckoutController --> CheckoutService
CheckoutService --> CartRepository
CheckoutService --> OrderRepository
CheckoutService --> PricingCalculator
CheckoutService --> PaymentStrategy

```

### 3) Sequence diagram (Start Checkout)
```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as CheckoutController
    participant S as CheckoutService
    participant CR as CartRepository
    participant PR as Pricing (CoR)
    participant PS as PaymentStrategy
    participant OR as OrderRepository

%% 0) HTTP request to controller with bean
    C->>API: POST /checkouts\nBody: CheckoutJob{cartId,currency?,couponCode?,paymentProvider?}
    API->>S: start(job: CheckoutJob)

%% 1) Load cart
    S->>CR: get(job.cartId)
    CR-->>S: Cart

%% 2) Run pricing pipeline (Base → Coupon → Tax → Shipping)
    S->>PR: calculate( PricingContext(cart, job.couponCode) )
    activate PR
    Note right of PR: BasePrice → Coupon → Tax → Shipping
    PR-->>S: {subtotal, discounts, tax, shipping, total}
    deactivate PR

%% 3) Create order (awaiting payment)
    S->>S: currency = (job.currency != null ? job.currency : cart.currency)
    S->>S: order = new Order( Money(total, currency) )

%% 4) Select payment strategy at runtime
    S->>S: provider = (job.paymentProvider != null ? job.paymentProvider : "MockPay")
    alt provider == "FailPay"
        S->>PS: instantiate FailPayStrategy
    else provider in {"COD","CashOnDelivery"}
        S->>PS: instantiate CashOnDeliveryStrategy
    else
        S->>PS: instantiate MockPayStrategy
    end

%% 5) Attempt payment
    S->>PS: pay(total)
    alt payment succeeded
        PS-->>S: true
        S->>S: order.onPaymentSucceeded()
    else payment failed
        PS-->>S: false
        S->>S: order.onPaymentFailed()
    end

%% 6) Persist order and respond
    S->>OR: save(order)
    OR-->>S: ok
    S-->>API: CheckoutResult{orderId,state,total,provider}
    API-->>C: 201 Created\nBody: CheckoutResult\nHeader: Location: /orders/{orderId}
```
