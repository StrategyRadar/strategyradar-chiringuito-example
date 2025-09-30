import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { addItemToOrder } from './orderService';
import type { Order } from '../types/Order';
import type { AddItemRequest } from '../types/AddItemRequest';

// Mock fetch globally
const mockFetch = vi.fn();
global.fetch = mockFetch;

describe('orderService', () => {
  const API_BASE_URL = 'http://localhost:8080';

  beforeEach(() => {
    mockFetch.mockClear();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('addItemToOrder', () => {
    it('should successfully add item to order', async () => {
      const request: AddItemRequest = {
        menuItemId: 'item-123',
        quantity: 2,
      };

      const mockResponse: Order = {
        orderId: 'order-456',
        status: 'PENDING',
        totalAmount: 25.0,
        itemCount: 2,
        orderLines: [
          {
            orderLineId: 'line-789',
            menuItemId: 'item-123',
            menuItemName: 'Paella Valenciana',
            quantity: 2,
            unitPrice: 12.5,
            lineTotal: 25.0,
          },
        ],
      };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => mockResponse,
      });

      const result = await addItemToOrder(request);

      expect(result).toEqual(mockResponse);
      expect(mockFetch).toHaveBeenCalledWith(`${API_BASE_URL}/api/order/add-item`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
        credentials: 'include',
      });
    });

    it('should throw error on 400 Bad Request', async () => {
      const request: AddItemRequest = {
        menuItemId: 'item-123',
        quantity: 51, // Invalid quantity
      };

      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        statusText: 'Bad Request',
      });

      await expect(addItemToOrder(request)).rejects.toThrow(
        'HTTP error! status: 400'
      );
    });

    it('should throw error on 404 Not Found', async () => {
      const request: AddItemRequest = {
        menuItemId: 'non-existent-item',
        quantity: 1,
      };

      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        statusText: 'Not Found',
      });

      await expect(addItemToOrder(request)).rejects.toThrow(
        'HTTP error! status: 404'
      );
    });

    it('should throw error on 409 Conflict (item unavailable)', async () => {
      const request: AddItemRequest = {
        menuItemId: 'unavailable-item',
        quantity: 1,
      };

      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 409,
        statusText: 'Conflict',
      });

      await expect(addItemToOrder(request)).rejects.toThrow(
        'HTTP error! status: 409'
      );
    });

    it('should throw error on network failure', async () => {
      const request: AddItemRequest = {
        menuItemId: 'item-123',
        quantity: 1,
      };

      mockFetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(addItemToOrder(request)).rejects.toThrow('Network error');
    });

    it('should include credentials for session management', async () => {
      const request: AddItemRequest = {
        menuItemId: 'item-123',
        quantity: 1,
      };

      const mockResponse: Order = {
        orderId: 'order-456',
        status: 'PENDING',
        totalAmount: 12.5,
        itemCount: 1,
        orderLines: [
          {
            orderLineId: 'line-789',
            menuItemId: 'item-123',
            menuItemName: 'Paella Valenciana',
            quantity: 1,
            unitPrice: 12.5,
            lineTotal: 12.5,
          },
        ],
      };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => mockResponse,
      });

      await addItemToOrder(request);

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          credentials: 'include',
        })
      );
    });
  });
});