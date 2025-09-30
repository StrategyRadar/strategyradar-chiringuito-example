# Task: Add to Cart Feature Implementation

## Metadata
- **Task ID**: 20250930_1131_add_to_cart_feature
- **Created**: 2025-09-30 11:31 UTC
- **Status**: IMPLEMENTATION
- **Assignee**: Javier (tdd-java-spring-engineer), Rex (tdd-react-typescript-engineer)
- **Branch**: add_to_cart_feature

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
*To be defined by Javier and Rex during implementation*

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

## Notes
This task builds on the menu browsing feature by introducing order management. It focuses on cart functionality without external dependencies (no payment, no SMS), making it a clean vertical slice. The implementation establishes:
1. Core order entities (Order, OrderLine) for future features
2. Session-based cart persistence
3. Business rule enforcement (max items, price snapshots)
4. Foundation for checkout flow (next task)

The task follows TDD principles and maintains consistency with the established architecture patterns.