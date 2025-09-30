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