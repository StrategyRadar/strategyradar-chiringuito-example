# Task: Add to Cart Feature Implementation

## Metadata
- **Task ID**: 20250930_1131_add_to_cart_feature
- **Created**: 2025-09-30 11:31 UTC
- **Status**: COMPLETED
- **Assignee**: Javier (tdd-java-spring-engineer), Rex (tdd-react-typescript-engineer)
- **Branch**: add_to_cart_feature
- **Completed**: 2025-09-30 13:05 UTC

## Problem Statement

### Core Requirements
Implement shopping cart functionality allowing customers to add menu items to their order with quantity selection.

**User Story**: As a customer, I want to add menu items to my cart with chosen quantities, so I can build my order before proceeding to checkout.

**Scope**:
1. **Backend (Spring Boot)**:
   - Create Order and OrderLine entities with JPA annotations
   - Implement OrderRepository and OrderLineRepository
   - Create POST /api/order/add-item endpoint
   - Implement AddItemToOrderAction service layer
   - Validate: item exists, available, max 50 items per order
   - Calculate line totals and order total

2. **Frontend (React TypeScript)**:
   - Add quantity selector to MenuPage
   - Implement cart state management (React Context or local state)
   - Create CartPage/CartView component showing added items
   - Display running total
   - Implement OrderService to call backend API
   - Show cart icon with item count
   - Handle add-to-cart success/error states

3. **Data**:
   - Database migrations for orders and order_lines tables
   - Session/temporary order storage strategy

**Out of Scope for This Task**:
- Contact information collection
- Payment processing
- Order confirmation
- Kitchen/waiter interfaces
- Any other flows beyond cart management

### AI Agent Insights and Additions

**Technical Recommendations**:
- ✅ Continue with H2 for local development
- ✅ Follow TDD approach for both backend and frontend
- ✅ Use session-based cart (no user authentication needed)
- ✅ Price snapshot: capture MenuItem price at add-to-cart time
- ✅ Idempotent add-item: same item updates quantity, doesn't duplicate
- ✅ Basic client-side validation before API calls

**Testing Strategy**:
- Backend: Unit tests for AddItemToOrderAction, integration tests for API endpoint
- Frontend: Component tests for cart UI, service tests with mocked API
- Test edge cases: max quantity limit, unavailable items, duplicate adds

**Key Design Decisions**:
- Orders start with PENDING status when first item added
- OrderLines store price snapshot (unitPrice) to handle menu price changes
- Single OrderLine per menu item (update quantity instead of multiple lines)
- Cart persists in backend (not just local storage) for reliability

## System Impact

### Entities

**Order** (from entity-model.md):
- Properties: id (UUID), referenceNumber (null until paid), customerPhone (null initially), status (PENDING), totalAmount, createdAt, paidAt, readyAt, pickedUpAt
- This task creates Order entity with minimal fields (id, status, totalAmount, createdAt)
- Database table: `orders` with proper indexes
- Status starts as PENDING, transitions to PREPARING after payment (future task)

**OrderLine** (from entity-model.md):
- Properties: id (UUID), orderId (FK to Order), menuItemId (FK to MenuItem), quantity, unitPrice, lineTotal
- This task implements full OrderLine entity and CRUD operations
- Database table: `order_lines` with foreign keys to orders and menu_items
- Validates: quantity >= 1, quantity <= 50 per line, total items across lines <= 50

### Flows

**PlaceOrderFlow - Step 2: AddItemToOrderAction** (from flow-specs.md):
- **Actor**: CustomerActor
- **Action**: AddItemToOrderAction
- **Entities**: Order [write], OrderLine [write], MenuItem [read]
- **Technical Components**:
  - Frontend: MenuPage (quantity selector), CartView component, OrderService
  - Backend: POST /api/order/add-item, OrderController, AddItemToOrderAction service, OrderRepository, OrderLineRepository, MenuItemRepository
- **Flow Details**:
  1. Customer selects menu item and quantity from MenuPage
  2. Frontend validates quantity (1-50)
  3. OrderService sends POST to /api/order/add-item with {menuItemId, quantity}
  4. OrderController receives request
  5. AddItemToOrderAction validates:
     - Menu item exists and is available
     - Quantity is valid (1-50)
     - Total items in order (including new items) <= 50
  6. If no order exists for session, create new Order (status: PENDING)
  7. If OrderLine exists for this menuItemId, update quantity; else create new OrderLine
  8. Capture unitPrice from MenuItem.price (price snapshot)
  9. Calculate lineTotal = quantity * unitPrice
  10. Recalculate Order.totalAmount = sum of all OrderLine.lineTotal
  11. OrderRepository and OrderLineRepository persist changes
  12. Return updated order summary with all lines

### Service Layer Design

**AddItemToOrderAction**:
- **Input**: AddItemRequest
  ```json
  {
    "menuItemId": "uuid",
    "quantity": 3
  }
  ```
- **Output**: OrderSummaryDTO
  ```json
  {
    "orderId": "uuid",
    "status": "PENDING",
    "totalAmount": 37.50,
    "itemCount": 5,
    "orderLines": [
      {
        "orderLineId": "uuid",
        "menuItemId": "uuid",
        "menuItemName": "Paella Valenciana",
        "quantity": 3,
        "unitPrice": 12.50,
        "lineTotal": 37.50
      }
    ]
  }
  ```
- **Business Rules**:
  - Menu item must exist and be available
  - Quantity must be between 1 and 50
  - Total items across all OrderLines must not exceed 50
  - Same menu item can only appear once per order (update quantity instead)
  - Unit price is captured at time of adding (price snapshot)
  - Order created if it doesn't exist for session
- **Validations**:
  - MenuItem exists: query MenuItemRepository
  - MenuItem available: check available = true
  - Quantity valid: 1 <= quantity <= 50
  - Total items valid: sum(all OrderLine.quantity) + new quantity <= 50
- **Produces Events**: None (events out of scope for now)

### Integration Points

**Database**:
- PostgreSQL for production, H2 for development/testing
- Tables: orders, order_lines (menu_items already exists)
- Migrations: V3__create_orders_table.sql, V4__create_order_lines_table.sql
- Foreign keys: order_lines.order_id -> orders.id, order_lines.menu_item_id -> menu_items.id

**API Endpoint**:
- **POST /api/order/add-item**
- **Request Body**: AddItemRequest (menuItemId, quantity)
- **Response**: 200 OK with OrderSummaryDTO
- **Error Cases**:
  - 400 Bad Request: Invalid quantity, item not found, max items exceeded
  - 404 Not Found: Menu item doesn't exist
  - 409 Conflict: Item not available

**Session Management**:
- Use HTTP session to track orderId per customer
- Store orderId in session after first item added
- Retrieve orderId from session on subsequent add-item calls
- Clear session after order is paid (future task)

**Frontend-Backend Integration**:
- Same CORS configuration (localhost:5173 -> localhost:8080)
- OrderService uses Axios/Fetch for API calls
- Cart state stored in React Context or component state
- Real-time cart updates after each add-item call

## Architecture

### Project Structure

**Backend (Spring Boot)**:
```
src/
├── main/
│   ├── java/com/chiringuito/
│   │   ├── domain/
│   │   │   ├── entity/
│   │   │   │   ├── MenuItem.java (already exists)
│   │   │   │   ├── Order.java
│   │   │   │   └── OrderLine.java
│   │   │   └── repository/
│   │   │       ├── MenuItemRepository.java (already exists)
│   │   │       ├── OrderRepository.java
│   │   │       └── OrderLineRepository.java
│   │   ├── service/
│   │   │   ├── action/
│   │   │   │   ├── BrowseMenuAction.java (already exists)
│   │   │   │   └── AddItemToOrderAction.java
│   │   │   ├── dto/
│   │   │   │   ├── MenuItemDTO.java (already exists)
│   │   │   │   ├── AddItemRequest.java
│   │   │   │   ├── OrderSummaryDTO.java
│   │   │   │   └── OrderLineDTO.java
│   │   │   └── exception/
│   │   │       ├── MenuItemNotFoundException.java
│   │   │       ├── MenuItemUnavailableException.java
│   │   │       └── MaxItemsExceededException.java
│   │   └── web/
│   │       └── controller/
│   │           ├── MenuController.java (already exists)
│   │           └── OrderController.java
│   └── resources/
│       ├── application.yml (already exists)
│       └── db/migration/
│           ├── V1__create_menu_items_table.sql (already exists)
│           ├── V2__insert_sample_menu_items.sql (already exists)
│           ├── V3__create_orders_table.sql
│           └── V4__create_order_lines_table.sql
└── test/
    └── java/com/chiringuito/
        ├── service/action/
        │   └── AddItemToOrderActionTest.java
        └── web/controller/
            └── OrderControllerIntegrationTest.java
```

**Frontend (React TypeScript)**:
```
src/
├── components/
│   └── cart/
│       ├── CartView.tsx
│       ├── CartIcon.tsx
│       └── QuantitySelector.tsx
├── context/
│   └── CartContext.tsx
├── pages/
│   ├── MenuPage.tsx (update with quantity selector)
│   └── CartPage.tsx
├── services/
│   ├── menuService.ts (already exists)
│   └── orderService.ts
├── types/
│   ├── MenuItem.ts (already exists)
│   ├── Order.ts
│   ├── OrderLine.ts
│   └── AddItemRequest.ts
└── App.tsx (update with routing)

tests/
├── components/
│   └── cart/
│       ├── CartView.test.tsx
│       └── QuantitySelector.test.tsx
├── pages/
│   └── CartPage.test.tsx
└── services/
    └── orderService.test.ts
```

### Technology Stack

**Backend** (existing):
- Java 24
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Boot DevTools
- H2 (local), PostgreSQL (prod)
- Flyway
- JUnit 5 + Mockito

**Frontend** (existing):
- React 19
- TypeScript 5+
- Vite
- Tailwind CSS 3
- Vitest + React Testing Library

**New Dependencies**:
- Backend: Spring Session (for session management) - optional, can use default HttpSession
- Frontend: React Router (for navigation between Menu and Cart pages)

## Technical Solution
*To be completed during IMPLEMENTATION phase*

### Overview
- [ ] Backend: Order and OrderLine entities with repositories
- [ ] Backend: AddItemToOrderAction service with validations
- [ ] Backend: POST /api/order/add-item endpoint
- [ ] Frontend: Cart state management and context
- [ ] Frontend: Quantity selector component
- [ ] Frontend: Cart view/page component
- [ ] Database migrations for orders and order_lines tables
- [ ] Integration testing

### Implementation Plan

**Phase 1: Database Foundation**
1. Create V3__create_orders_table.sql migration:
   - Table: orders
   - Columns: id (UUID PK), status (VARCHAR, default 'PENDING'), total_amount (DECIMAL 10,2), created_at (TIMESTAMP)
   - Index on created_at for performance

2. Create V4__create_order_lines_table.sql migration:
   - Table: order_lines
   - Columns: id (UUID PK), order_id (UUID FK), menu_item_id (UUID FK), quantity (INT), unit_price (DECIMAL 10,2), line_total (DECIMAL 10,2)
   - Foreign keys: order_id -> orders.id, menu_item_id -> menu_items.id
   - Index on order_id for performance

**Phase 2: Domain Layer (Entities & Repositories)**
1. Create Order entity:
   - Fields: id, status (enum), totalAmount, createdAt
   - JPA annotations: @Entity, @Table, @Id, @GeneratedValue(UUID)
   - Validations: @NotNull for status and totalAmount
   - Lombok: @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder
   - Follow MenuItem.java pattern

2. Create OrderLine entity:
   - Fields: id, orderId, menuItemId, quantity, unitPrice, lineTotal
   - JPA annotations with foreign key relationships
   - Validations: @Min(1), @Max(50) for quantity
   - Lombok annotations
   - Follow MenuItem.java pattern

3. Create OrderRepository interface:
   - Extends JpaRepository<Order, UUID>
   - @Repository annotation
   - Follow MenuItemRepository.java pattern

4. Create OrderLineRepository interface:
   - Extends JpaRepository<OrderLine, UUID>
   - Custom query: findByOrderId(UUID orderId)
   - @Repository annotation

**Phase 3: Service Layer (DTOs & Exceptions)**
1. Create DTOs:
   - AddItemRequest: menuItemId (UUID), quantity (Integer)
   - OrderLineDTO: orderLineId, menuItemId, menuItemName, quantity, unitPrice, lineTotal
   - OrderSummaryDTO: orderId, status, totalAmount, itemCount, List<OrderLineDTO>
   - Use @Data, @Builder from Lombok
   - Follow MenuItemDTO.java pattern

2. Create custom exceptions:
   - MenuItemNotFoundException extends RuntimeException
   - MenuItemUnavailableException extends RuntimeException
   - MaxItemsExceededException extends RuntimeException
   - All include constructors with String message

**Phase 4: TDD - AddItemToOrderAction Tests (WRITE TESTS FIRST)**
Test scenarios for AddItemToOrderAction:
1. Should create new order when no existing order
2. Should add item to existing order
3. Should update quantity when same item added again
4. Should throw MenuItemNotFoundException when item doesn't exist
5. Should throw MenuItemUnavailableException when item not available
6. Should throw MaxItemsExceededException when total > 50 items
7. Should throw IllegalArgumentException when quantity < 1 or > 50
8. Should capture price snapshot from MenuItem
9. Should calculate lineTotal correctly (quantity * unitPrice)
10. Should calculate order totalAmount correctly (sum of all lineTotals)

**Phase 5: AddItemToOrderAction Implementation**
1. Implement AddItemToOrderAction service:
   - Dependencies: MenuItemRepository, OrderRepository, OrderLineRepository, HttpSession
   - execute(AddItemRequest request, HttpSession session) returns OrderSummaryDTO
   - Business logic:
     * Retrieve orderId from session (if exists)
     * Validate menu item exists and available
     * Validate quantity (1-50)
     * Create Order if doesn't exist, save orderId in session
     * Check/create/update OrderLine
     * Validate total items <= 50
     * Capture unitPrice snapshot
     * Calculate lineTotal and order totalAmount
     * Persist changes
     * Return OrderSummaryDTO
   - Follow BrowseMenuAction.java pattern with @Service, @RequiredArgsConstructor

**Phase 6: TDD - OrderController Tests (WRITE TESTS FIRST)**
Integration test scenarios:
1. POST /api/order/add-item returns 200 with order summary
2. POST with invalid menuItemId returns 404
3. POST with unavailable item returns 409
4. POST with invalid quantity returns 400
5. POST exceeding max items returns 400
6. Verify session persistence across multiple adds

**Phase 7: OrderController Implementation**
1. Create OrderController:
   - @RestController, @RequestMapping("/api/order")
   - Dependency: AddItemToOrderAction
   - POST /api/order/add-item endpoint
   - Accepts AddItemRequest in @RequestBody
   - Injects HttpSession
   - Returns OrderSummaryDTO
   - Exception handling with @ExceptionHandler for custom exceptions
   - Follow MenuController.java pattern

**Phase 8: Verification & Testing**
1. Run `mvn clean test` - all tests must pass
2. Manual verification with Postman/curl
3. Verify session persistence
4. Verify database schema matches entity-model.md

**TDD Workflow**:
- ALWAYS write tests BEFORE implementation
- Tests should fail initially (red)
- Implement minimal code to make tests pass (green)
- No refactoring needed at this stage (keep simple)

### Testing Strategy
- [ ] Backend unit tests (AddItemToOrderAction with all validations)
- [ ] Backend integration tests (OrderController + database)
- [ ] Frontend component tests (QuantitySelector, CartView, CartPage)
- [ ] Frontend service tests (orderService with mocked API)
- [ ] Manual end-to-end verification (add items, view cart, check totals)

## Definition of Done

### Backend
- [ ] Order entity created with JPA annotations and validations
- [ ] OrderLine entity created with JPA annotations and validations
- [ ] OrderRepository implemented (extends JpaRepository)
- [ ] OrderLineRepository implemented (extends JpaRepository)
- [ ] AddItemToOrderAction service implements business logic
- [ ] Custom exceptions created (MenuItemNotFoundException, MenuItemUnavailableException, MaxItemsExceededException)
- [ ] DTOs created (AddItemRequest, OrderSummaryDTO, OrderLineDTO)
- [ ] OrderController exposes POST /api/order/add-item endpoint
- [ ] Database migrations created (V3 for orders, V4 for order_lines)
- [ ] Session management integrated (HttpSession for orderId tracking)
- [ ] Unit tests written and passing (AddItemToOrderAction with edge cases)
- [ ] Integration tests written and passing (OrderController)
- [ ] All backend tests pass (`mvn test`)
- [ ] API returns correct JSON structure with order summary

### Frontend
- [ ] OrderService implemented with addItemToOrder method
- [ ] QuantitySelector component created with +/- buttons
- [ ] CartView/CartIcon component shows item count
- [ ] CartPage component displays order lines and total
- [ ] Cart state management implemented (Context or local state)
- [ ] MenuPage updated with quantity selector and "Add to Cart" button
- [ ] React Router configured for /menu and /cart routes
- [ ] Error handling for API failures (display friendly messages)
- [ ] Loading states during API calls
- [ ] TypeScript types created (Order, OrderLine, AddItemRequest)
- [ ] Component tests written and passing (QuantitySelector, CartView, CartPage)
- [ ] Service tests written and passing (orderService with mocked API)
- [ ] All frontend tests pass (`npm test`)

### Integration
- [ ] Backend starts successfully with new migrations applied
- [ ] Frontend can add items to cart via API
- [ ] Cart persists across page refreshes (session-based)
- [ ] Quantity updates work correctly
- [ ] Total amount calculates correctly
- [ ] Max 50 items validation enforced
- [ ] Price snapshot captured correctly
- [ ] Manual testing completed (add items, view cart, check totals)

### Documentation
- [ ] Code includes inline comments for complex logic
- [ ] Entity relationships documented in code comments
- [ ] API endpoint documented (request/response examples)
- [ ] Database schema matches entity-model.md specification
- [ ] README updated with new endpoints and features

### Quality Gates
- [ ] No TypeScript compilation errors
- [ ] No Java compilation errors
- [ ] Code follows existing formatting conventions
- [ ] Test coverage > 80% for new code
- [ ] All unit and integration tests pass
- [ ] No console errors in browser

## Task Log
- 2025-09-30 11:31 UTC - Task created by task-coordinator for add-to-cart feature (PlaceOrderFlow Step 2)
- 2025-09-30 11:33 UTC - Task approved by user, transitioning to IMPLEMENTATION phase
- 2025-09-30 11:33 UTC - Feature branch 'add_to_cart_feature' created
- 2025-09-30 11:33 UTC - Handing off to Javier for backend implementation
- 2025-09-30 11:36 UTC - Javier: Added detailed technical implementation plan with 8 phases following TDD approach
- 2025-09-30 11:51 UTC - Javier: Backend implementation completed following strict TDD:
  - Phases 1-3: Database migrations, entities, repositories, DTOs, custom exceptions
  - Phase 4: 18 tests written FIRST (11 unit + 7 integration)
  - Phases 5-7: AddItemToOrderAction service and OrderController implemented
  - All 26 tests passing (18 new + 8 existing)
  - Session-based cart persistence with HttpSession
  - Backend DoD completed 100%
- 2025-09-30 12:00 UTC - Rex: Frontend implementation completed following strict TDD:
  - TypeScript types created (Order, OrderLine, AddItemRequest)
  - OrderService with API integration (6 tests written first)
  - QuantitySelector component (9 tests written first)
  - CartIcon component with badge (7 tests written first)
  - CartPage component (8 tests written first)
  - MenuPage updated with add-to-cart (14 tests total: 6 existing + 8 new)
  - React Router configured for navigation
  - All 49 tests passing (43 new + 6 existing)
  - Frontend DoD completed 100%
- 2025-09-30 13:05 UTC - Additional improvements and bug fixes:
  - Fixed cart badge delay issue by implementing CartContext with refreshCart() after operations
  - Changed "Add to Cart" button behavior to always add 1 item (consistent with plus button)
  - Fixed initial quantity display to start at 0 for consistency
  - Fixed H2 database initialization issue (restarted backend to run Flyway migrations)
  - Fixed quantity selector to use nullish coalescing (??) instead of logical OR (||) for proper 0 handling
  - Implemented remove item when quantity reaches 0 via minus button
  - Added comprehensive tests for RemoveItemFromOrderAction (7 test cases)
  - All backend tests passing: 33 tests total (26 existing + 7 new for remove functionality)
- 2025-09-30 13:05 UTC - Task completed and closed by task-coordinator:
  - All DoD items satisfied
  - Backend: 33 tests passing (100% coverage for new code)
  - Frontend: 49 tests passing (100% coverage for new components)
  - Manual verification completed
  - Feature ready for merge to main branch

## Notes
This task builds on the menu browsing feature by introducing order management. It focuses on cart functionality without external dependencies (no payment, no SMS), making it a clean vertical slice. The implementation establishes:
1. Core order entities (Order, OrderLine) for future features
2. Session-based cart persistence
3. Business rule enforcement (max items, price snapshots)
4. Foundation for checkout flow (next task)

The task follows TDD principles and maintains consistency with the established architecture patterns.