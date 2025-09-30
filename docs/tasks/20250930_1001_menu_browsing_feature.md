# Task: Menu Browsing Feature Implementation

## Metadata
- **Task ID**: 20250930_1001_menu_browsing_feature
- **Created**: 2025-09-30 10:01 UTC
- **Status**: IMPLEMENTATION
- **Assignee**: Javier (tdd-java-spring-engineer), Rex (tdd-react-typescript-engineer)
- **Branch**: menu_browsing_feature

## Problem Statement

### Core Requirements
Implement the minimal viable feature to demonstrate the El Chiringuito system working end-to-end: menu browsing functionality.

**User Story**: As a customer, I want to scan a QR code and browse the restaurant menu, so I can see what food is available to order.

**Scope**:
1. **Backend (Spring Boot)**:
   - Set up Spring Boot project structure with PostgreSQL
   - Create MenuItem entity with JPA annotations
   - Implement MenuItemRepository
   - Create GET /api/menu endpoint returning all available menu items
   - Implement BrowseMenuAction service layer
   - Load sample menu data via SQL migration

2. **Frontend (React TypeScript)**:
   - Set up React TypeScript project structure
   - Create MenuPage component displaying menu items
   - Implement MenuService to call backend API
   - Display menu items with: name, description, price, image
   - Add basic responsive design for mobile viewing

3. **Data**:
   - Sample menu with 5-10 typical chiringuito items
   - Initial SQL migration to populate menu_items table

**Out of Scope for This Task**:
- Order creation and cart functionality
- Payment processing
- Kitchen/waiter interfaces
- SMS notifications
- Any other flows

### AI Agent Insights and Additions

**Technical Recommendations**:
- ✅ Use H2 in-memory database for local development, with PostgreSQL profile for production
- ✅ Include Spring Boot DevTools for hot reload during development
- ✅ Follow TDD approach: write tests first for both backend and frontend (Javier's standard workflow)
- ✅ Include basic error handling for API failures
- ✅ Add CORS configuration for local development
- ✅ Mobile-first responsive design (viewport width 320px+)

**Testing Strategy**:
- Backend: Unit tests for BrowseMenuAction, integration tests for API endpoint with H2
- Frontend: Component tests for MenuPage, API service tests with mock server
- End-to-end: Manual verification that menu loads in browser

**Simplified Approach** (per user feedback):
- ❌ No testcontainers (keeping it simple for now)
- ❌ No performance optimization focus initially (basic implementation first)

## System Impact

### Entities

**MenuItem** (from entity-model.md):
- Properties: id (UUID), name, description, price, imageUrl, available, createdAt, updatedAt
- This task implements the full MenuItem entity and basic CRUD operations (read-only for this task)
- Database table: `menu_items` with proper indexes on name field

### Flows

**PlaceOrderFlow - Step 1: BrowseMenuAction** (from flow-specs.md):
- **Actor**: CustomerActor
- **Action**: BrowseMenuAction
- **Entities**: MenuItem [read-only]
- **Technical Components**:
  - Frontend: MenuPage component, MenuService
  - Backend: GET /api/menu, MenuController, BrowseMenuAction service, MenuItemRepository
- **Flow Details**:
  1. Customer accesses ordering URL
  2. MenuPage component loads
  3. MenuService sends GET request to /api/menu
  4. MenuController receives request
  5. BrowseMenuAction retrieves all active menu items from database
  6. Returns menu items as JSON (name, description, price, imageUrl, available)
  7. Frontend displays items in responsive grid layout

### Service Layer Design

**BrowseMenuAction** (Query):
- **Input**: None (or optional filter parameter for future use)
- **Output**: List<MenuItemDTO>
  ```json
  [
    {
      "id": "uuid",
      "name": "Paella Valenciana",
      "description": "Traditional Spanish rice dish with seafood",
      "price": 12.50,
      "imageUrl": "https://example.com/paella.jpg",
      "available": true
    }
  ]
  ```
- **Business Rules**:
  - Only return items where available = true
  - Sort by name alphabetically
- **Validations**:
  - None required for read operation
- **Produces Events**: None (query operation)

### Integration Points

**Database**:
- PostgreSQL for production
- H2 for development/testing
- Table: menu_items
- Initial migration: V1__create_menu_items_table.sql
- Sample data: V2__insert_sample_menu_items.sql

**API Endpoint**:
- **GET /api/menu**
- **Response**: 200 OK with List<MenuItemDTO>
- **Headers**: Content-Type: application/json
- **Error Cases**:
  - 500 Internal Server Error if database unavailable

**Frontend-Backend Integration**:
- Base URL: http://localhost:8080 (backend), http://localhost:5173 (frontend)
- CORS enabled for local development
- API client with error handling and retry logic

## Architecture

### Project Structure

**Backend (Spring Boot)**:
```
src/
├── main/
│   ├── java/com/chiringuito/
│   │   ├── ChiringuitoApplication.java
│   │   ├── config/
│   │   │   └── CorsConfig.java
│   │   ├── domain/
│   │   │   ├── entity/
│   │   │   │   └── MenuItem.java
│   │   │   └── repository/
│   │   │       └── MenuItemRepository.java
│   │   ├── service/
│   │   │   ├── action/
│   │   │   │   └── BrowseMenuAction.java
│   │   │   └── dto/
│   │   │       └── MenuItemDTO.java
│   │   └── web/
│   │       └── controller/
│   │           └── MenuController.java
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_menu_items_table.sql
│           └── V2__insert_sample_menu_items.sql
└── test/
    └── java/com/chiringuito/
        ├── service/action/
        │   └── BrowseMenuActionTest.java
        └── web/controller/
            └── MenuControllerIntegrationTest.java
```

**Frontend (React TypeScript)**:
```
src/
├── components/
│   ├── menu/
│   │   ├── MenuPage.tsx
│   │   ├── MenuItem.tsx
│   │   └── MenuGrid.tsx
├── services/
│   ├── api/
│   │   ├── apiClient.ts
│   │   └── menuService.ts
│   └── types/
│       └── MenuItem.ts
├── App.tsx
└── main.tsx

tests/
├── components/
│   └── menu/
│       └── MenuPage.test.tsx
└── services/
    └── menuService.test.ts
```

### Technology Stack

**Backend**:
- Java 17
- Spring Boot 3.2+
- Spring Data JPA
- Spring Boot DevTools (hot reload)
- PostgreSQL 15+ (production)
- H2 (local development and testing)
- Flyway (database migrations)
- JUnit 5 + Mockito (testing)

**Frontend**:
- React 18+
- TypeScript 5+
- Vite (build tool)
- Axios (HTTP client)
- Vitest + React Testing Library (testing)

**Development Tools**:
- Maven (backend build)
- npm (frontend package management)
- Spring Boot DevTools (hot reload)
- Docker Compose (PostgreSQL local setup)

## Technical Solution
*To be completed during IMPLEMENTATION phase*

### Overview
- [ ] Backend setup with Spring Boot and PostgreSQL
- [ ] Frontend setup with React TypeScript and Vite
- [ ] API endpoint implementation following Mosy action pattern
- [ ] Database schema and migrations
- [ ] Component implementation with responsive design
- [ ] Integration and testing

### Implementation Plan
*To be defined by Javier and Rex during implementation*

### Testing Strategy
- [ ] Backend unit tests (BrowseMenuAction)
- [ ] Backend integration tests (MenuController + database)
- [ ] Frontend component tests (MenuPage, MenuItem)
- [ ] Frontend service tests (menuService with mocked API)
- [ ] Manual end-to-end verification

## Definition of Done

### Backend
- [ ] Spring Boot project initialized with correct dependencies
- [ ] MenuItem entity created with all fields from entity-model.md
- [ ] MenuItemRepository implemented (extends JpaRepository)
- [ ] BrowseMenuAction service implements query logic
- [ ] MenuController exposes GET /api/menu endpoint
- [ ] Database migrations created (schema + sample data)
- [ ] CORS configuration allows frontend origin
- [ ] Unit tests written and passing (BrowseMenuAction)
- [ ] Integration tests written and passing (MenuController)
- [ ] API returns correct JSON structure
- [ ] All tests pass (`mvn test`)

### Frontend
- [ ] React TypeScript project initialized with Vite
- [ ] MenuPage component displays menu items
- [ ] MenuItem component shows name, description, price, image
- [ ] MenuService calls backend API correctly
- [ ] Error handling for API failures (display friendly message)
- [ ] Responsive design works on mobile (320px+)
- [ ] Loading state shown while fetching data
- [ ] Component tests written and passing
- [ ] Service tests with mock API written and passing
- [ ] All tests pass (`npm test`)

### Integration
- [ ] Backend starts successfully on port 8080
- [ ] Frontend starts successfully on port 5173
- [ ] Frontend can fetch menu from backend
- [ ] Menu displays correctly in browser
- [ ] Manual testing completed on Chrome and Safari
- [ ] README updated with setup instructions

### Documentation
- [ ] Code includes inline comments for complex logic
- [ ] API endpoint documented (OpenAPI/Swagger optional)
- [ ] Database schema matches entity-model.md specification
- [ ] Setup instructions added to project README

### Quality Gates
- [ ] No TypeScript compilation errors
- [ ] No Java compilation errors
- [ ] Code follows existing formatting conventions
- [ ] Test coverage > 80% for new code
- [ ] All unit and integration tests pass

## Task Log
- 2025-09-30 10:01 UTC - Task created by task-coordinator for minimal viable feature implementation
- 2025-09-30 12:10 CEST - User reviewed and approved task with simplifications: H2 for local dev, DevTools enabled, no testcontainers, no performance focus initially, TDD approach confirmed, mobile-first design confirmed
- 2025-09-30 12:10 CEST - Task ready for IMPLEMENTATION phase, handing off to Javier (backend) and Rex (frontend)

## Notes
This task represents the minimal vertical slice to demonstrate the system working end-to-end. It focuses solely on menu browsing, which requires no external integrations (Stripe/Twilio) and has no complex business logic. This makes it ideal for:
1. Setting up the foundational project structure (both backend and frontend)
2. Establishing the development workflow and testing patterns
3. Verifying the basic integration between frontend and backend
4. Providing a working baseline for subsequent feature development

The implementation should follow TDD principles and the Mosy action-based architecture pattern established in the system specifications.