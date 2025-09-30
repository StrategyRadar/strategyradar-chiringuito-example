# El chiringuito app

## Description

The basic order flow is the customer accessing a URL (via QR code), then they're presented with the menu and they add items to the order. Then they enter a phone number, they make the payment and they're shown a message telling them they'll be notified by SMS when the order is ready to pickup. They get a number as a reference.

On the kitchen side, they see orders in a list with numbers and the items. this page is refreshed automatically every 5 seconds. when an order is ready, they click/tap a big button next to each order (READY) and that row goes away. That triggers a sms sent to the customer. 

The waiter has a similar page but they first all orders that are ready to pickup, since the moment they're ready, and the phone number of the customer. the more time it passes, the more red the row becomes. when the order has been picked up (they have a button to toggle that), the order dissapears from their screen.

## Tech Stack

I need to use Spring Boot and React since those are the ones my organization is familiar with.