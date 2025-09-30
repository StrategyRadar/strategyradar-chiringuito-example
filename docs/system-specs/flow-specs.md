# Flow Specifications - El Chiringuito

**System**: El Chiringuito Restaurant Ordering System
**Version**: 1.0.0
**Last Updated**: 2025-09-30

## Overview

El Chiringuito is a restaurant ordering system enabling customers to order via QR code, kitchen staff to process orders, and waiters to manage pickups. The system handles the complete order lifecycle from placement through pickup.

## System Actors

- **CustomerActor**: Person ordering food via QR code
- **KitchenStaffActor**: Restaurant staff preparing orders
- **WaiterActor**: Restaurant staff managing order pickups
- **SystemActor**: Automated system actions (validation, SMS, refresh)

## Flow 1: PlaceOrderFlow

**Description**: Customer scans QR code, browses menu, adds items to order, provides phone number, completes payment via Stripe, and receives order confirmation with reference number.

| Aspect | Step 1 | Step 2 | Step 3 | Step 4 |
|--------|--------|--------|--------|--------|
| **Actor** | CustomerActor | CustomerActor | CustomerActor | CustomerActor |
| **Action** | BrowseMenuAction | AddItemToOrderAction | ProvideContactAction | ProcessPaymentAction |
| **Entities** | MenuItem [read-only] | Order [write], OrderLine [write] | Order [write] | Order [write], Payment [write] |
| **Metrics** | - | itemsAddedToCart | - | paymentsProcessed, failedPayments, ordersCreated |
| **Technical components and APIs** | MenuPage, MenuService (frontend), GET /api/menu, MenuController, BrowseMenuAction, MenuItemRepository | CartPage, OrderService (frontend), POST /api/order/add-item, OrderController, AddItemToOrderAction, OrderRepository | CheckoutPage, OrderService (frontend), PATCH /api/order/contact, OrderController, OrderRepository | CheckoutPage, PaymentService (frontend), POST /api/payment/process, PaymentController, ProcessPaymentAction, StripeClient, OrderRepository |
| **Produced events** | - | - | - | - |
| **Triggered by** | User accesses QR code URL | User clicks "Add to Order" | User enters phone number | User submits payment |

### Flow Details

**Step 1: BrowseMenuAction**
- Customer scans QR code and accesses the restaurant's ordering URL
- MenuPage component loads available menu items
- MenuService sends GET request to `/api/menu`
- MenuController receives request
- BrowseMenuAction retrieves all active menu items
- MenuItemRepository queries database for available items
- Returns menu with items (name, description, price, image)

**Step 2: AddItemToOrderAction**
- Customer selects menu item and quantity (max 50 items total per order)
- CartPage collects item details
- OrderService sends POST to `/api/order/add-item` with menuItemId and quantity
- OrderController creates CustomerActor
- AddItemToOrderAction validates:
  - Menu item exists and is available
  - Total items in order ≤ 50
  - Creates or updates Order entity (status: PENDING)
  - Creates OrderLine entity linking order to menu item
- OrderRepository persists order and order lines
- Publishes OrderItemAddedEvent
- Returns updated order summary

**Step 3: ProvideContactAction**
- Customer enters phone number on checkout page
- OrderService sends PATCH to `/api/order/contact` with phone number
- OrderController updates Order entity with phone number
- Returns confirmation

**Step 4: ProcessPaymentAction**
- Customer submits payment information via Stripe payment form
- PaymentService collects payment details and sends POST to `/api/payment/process`
- PaymentController creates CustomerActor
- ProcessPaymentAction performs all steps synchronously:
  - Validates order can be paid (status PENDING, has items, has phone)
  - Validates business rules:
    - Operating hours check (configurable, default 11:30-23:30)
    - Order has at least 1 item
    - Total items ≤ 50
    - Valid phone number format
  - Processes payment through Stripe API (test mode)
  - Creates Payment entity with transaction details and status
  - If payment successful:
    - Generates unique order reference number
    - Sets order timestamp
    - Updates Order status to PREPARING
  - Returns payment confirmation with order reference number or error

## Flow 2: ProcessOrderFlow

**Description**: Kitchen staff views order queue (auto-refreshes every 5 seconds), prepares food, marks order as ready, which triggers SMS notification to customer.

| Aspect | Step 1 | Step 2 |
|--------|--------|--------|
| **Actor** | KitchenStaffActor | KitchenStaffActor |
| **Action** | ViewOrderQueueAction | MarkOrderReadyAction |
| **Entities** | Order [read-only], OrderLine [read-only], MenuItem [read-only] | Order [write], SMSLog [write] |
| **Metrics** | - | ordersCompleted, smsNotificationsSent, smsNotificationsFailed |
| **Technical components and APIs** | KitchenDisplay, KitchenService (frontend polling every 5s), GET /api/kitchen/orders, KitchenController, ViewOrderQueueAction, OrderRepository | KitchenDisplay, KitchenService (frontend), POST /api/kitchen/mark-ready/{orderId}, KitchenController, MarkOrderReadyAction, TwilioClient, SMSLogRepository, OrderRepository |
| **Produced events** | - | - |
| **Triggered by** | Kitchen page auto-refresh (5s interval) | Kitchen staff clicks "READY" button |

### Flow Details

**Step 1: ViewOrderQueueAction**
- Kitchen display page polls every 5 seconds for updates
- KitchenService sends GET to `/api/kitchen/orders`
- KitchenController creates KitchenStaffActor
- ViewOrderQueueAction retrieves all orders with status PREPARING
- OrderRepository queries orders in FIFO order (oldest first)
- Returns list with order reference, items, quantities, timestamps
- Frontend displays orders in scrollable list

**Step 2: MarkOrderReadyAction**
- Kitchen staff clicks "READY" button next to order
- KitchenService sends POST to `/api/kitchen/mark-ready/{orderId}`
- KitchenController creates KitchenStaffActor
- MarkOrderReadyAction performs all steps synchronously:
  - Validates order exists and status is PREPARING
  - Updates Order status to READY
  - Sets readyAt timestamp
  - Composes SMS message: "Your order [reference] is ready for pickup at El Chiringuito!"
  - Sends SMS via Twilio API (test mode) directly
  - Creates SMSLog entity with status (SENT or FAILED) and timestamp
  - Persists order and SMS log changes
- Order disappears from kitchen display
- Returns success confirmation

## Flow 3: ManagePickupFlow

**Description**: Waiter views all ready orders with visual urgency indicators (turns red over time), marks orders as picked up when customer collects.

| Aspect | Step 1 | Step 2 |
|--------|--------|--------|
| **Actor** | WaiterActor | WaiterActor |
| **Action** | ViewReadyOrdersAction | MarkOrderPickedUpAction |
| **Entities** | Order [read-only], SMSLog [read-only] | Order [write] |
| **Metrics** | - | ordersPickedUp, averagePickupTime |
| **Technical components and APIs** | WaiterDisplay, WaiterService (frontend auto-refresh), GET /api/waiter/ready-orders, WaiterController, ViewReadyOrdersAction, OrderRepository, SMSLogRepository | WaiterDisplay, WaiterService (frontend), POST /api/waiter/mark-picked-up/{orderId}, WaiterController, MarkOrderPickedUpAction, OrderRepository |
| **Produced events** | - | - |
| **Triggered by** | Waiter page auto-refresh | Waiter clicks "PICKED UP" button |

### Flow Details

**Step 1: ViewReadyOrdersAction**
- Waiter display page auto-refreshes to show ready orders
- WaiterService sends GET to `/api/waiter/ready-orders`
- WaiterController creates WaiterActor
- ViewReadyOrdersAction:
  - Retrieves all orders with status READY
  - Includes order reference, phone number, ready timestamp
  - Retrieves SMS log to show notification status (SENT/FAILED)
  - Calculates time elapsed since ready
- Frontend displays with visual urgency:
  - Recent orders: white background
  - Waiting 5+ minutes: yellow background
  - Waiting 10+ minutes: red background
  - Shows "⚠️ SMS FAILED" indicator if notification failed
- Orders sorted by readyAt timestamp (oldest first)

**Step 2: MarkOrderPickedUpAction**
- Waiter clicks "PICKED UP" button when customer collects order
- WaiterService sends POST to `/api/waiter/mark-picked-up/{orderId}`
- WaiterController creates WaiterActor
- MarkOrderPickedUpAction:
  - Validates order exists and status is READY
  - Updates Order status to PICKED_UP
  - Sets pickedUpAt timestamp
  - Calculates pickup time (pickedUpAt - readyAt)
- OrderRepository persists changes
- Order disappears from waiter display
- Returns success confirmation

## Business Rules

### Operating Hours
- Orders only accepted during configured time window
- Default: 11:30 AM - 11:30 PM
- Configurable via application properties
- ValidateOrderAction enforces this rule

### Order Constraints
- Maximum 50 total items per order
- Minimum 1 item required
- Orders immutable after payment completes
- No cancellations or modifications after payment

### Payment Processing
- Stripe integration (test mode for development)
- Payment must succeed before order enters PREPARING status
- Failed payments keep order in PENDING status

### SMS Notifications
- Twilio integration (test mode for development)
- Sent when order status changes to READY
- Failure logged and visible in waiter interface
- No automatic retry on failure

### Kitchen Display
- Auto-refresh every 5 seconds
- Shows only PREPARING orders
- FIFO ordering (oldest first)
- Orders disappear when marked READY

### Waiter Display
- Shows only READY orders
- Visual urgency indicators based on wait time
- SMS failure warnings
- Orders disappear when marked PICKED_UP

## Implementation Notes

### Synchronous Processing
- All actions execute synchronously without event-driven architecture
- Payment processing includes validation, Stripe call, and order confirmation in single transaction
- SMS notification sent immediately when order marked ready (no async queue)
- Simpler implementation without message brokers or event handlers

## Integration Points

### External Systems
- **Stripe**: Payment processing (test mode with test cards)
- **Twilio**: SMS notifications (test mode with test numbers)

### Internal Systems
- **Menu Management**: SQL migration files for menu items
- **Configuration Service**: Operating hours and business rules

---

© 2025 Mosy Software Architecture SL. All rights reserved.

Licensed to AgentGuild customers for internal use only. Distribution, copying, or derivative works prohibited without written permission. Contact: legal@mosy.tech