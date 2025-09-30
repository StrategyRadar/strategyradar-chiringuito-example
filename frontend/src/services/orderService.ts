import type { Order } from '../types/Order';
import type { AddItemRequest } from '../types/AddItemRequest';

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function addItemToOrder(request: AddItemRequest): Promise<Order> {
  const response = await fetch(`${API_BASE_URL}/api/order/add-item`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
    credentials: 'include', // Important: enables session cookies
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response.json();
}

export async function getCart(): Promise<Order | null> {
  const response = await fetch(`${API_BASE_URL}/api/order/cart`, {
    method: 'GET',
    credentials: 'include', // Important: enables session cookies
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const data = await response.text();
  return data ? JSON.parse(data) : null;
}

export async function removeItemFromOrder(menuItemId: string): Promise<void> {
  const response = await fetch(
    `${API_BASE_URL}/api/order/remove-item/${menuItemId}`,
    {
      method: 'DELETE',
      credentials: 'include', // Important: enables session cookies
    }
  );

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
}

export async function updateItemQuantity(
  menuItemId: string,
  quantity: number
): Promise<Order> {
  const response = await fetch(`${API_BASE_URL}/api/order/update-quantity`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ menuItemId, quantity }),
    credentials: 'include', // Important: enables session cookies
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response.json();
}