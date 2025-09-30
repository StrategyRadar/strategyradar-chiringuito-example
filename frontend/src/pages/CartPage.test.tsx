import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { CartPage } from './CartPage';
import * as orderService from '../services/orderService';
import type { Order } from '../types/Order';

// Mock the orderService
vi.mock('../services/orderService');

describe('CartPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should display loading state initially', () => {
    vi.spyOn(orderService, 'getCart').mockImplementation(() =>
      new Promise(() => {}) // Never resolves to keep loading state
    );

    render(<CartPage />);

    expect(screen.getByText(/loading cart/i)).toBeInTheDocument();
  });

  it('should display empty cart message when order is null', async () => {
    vi.spyOn(orderService, 'getCart').mockResolvedValue(null);

    render(<CartPage />);

    await waitFor(() => {
      expect(screen.getByText(/your cart is empty/i)).toBeInTheDocument();
    });
  });

  it('should display error message on fetch failure', async () => {
    vi.spyOn(orderService, 'getCart').mockRejectedValue(new Error('Network error'));

    render(<CartPage />);

    await waitFor(() => {
      expect(screen.getByText(/failed to load cart/i)).toBeInTheDocument();
    });
  });

  it('should display order items with details', async () => {
    const mockOrder: Order = {
      orderId: 'order-123',
      status: 'PENDING',
      totalAmount: 37.5,
      itemCount: 3,
      orderLines: [
        {
          orderLineId: 'line-1',
          menuItemId: 'item-1',
          menuItemName: 'Paella Valenciana',
          quantity: 2,
          unitPrice: 12.5,
          lineTotal: 25.0,
        },
        {
          orderLineId: 'line-2',
          menuItemId: 'item-2',
          menuItemName: 'Gazpacho',
          quantity: 1,
          unitPrice: 12.5,
          lineTotal: 12.5,
        },
      ],
    };

    vi.spyOn(orderService, 'getCart').mockResolvedValue(mockOrder);

    render(<CartPage />);

    await waitFor(() => {
      expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
      expect(screen.getByText('Gazpacho')).toBeInTheDocument();
      expect(screen.getByText(/Quantity: 2/i)).toBeInTheDocument();
      expect(screen.getByText(/Quantity: 1/i)).toBeInTheDocument();
    });
  });

  it('should display unit prices and line totals', async () => {
    const mockOrder: Order = {
      orderId: 'order-123',
      status: 'PENDING',
      totalAmount: 25.0,
      itemCount: 2,
      orderLines: [
        {
          orderLineId: 'line-1',
          menuItemId: 'item-1',
          menuItemName: 'Paella Valenciana',
          quantity: 2,
          unitPrice: 12.5,
          lineTotal: 25.0,
        },
      ],
    };

    vi.spyOn(orderService, 'getCart').mockResolvedValue(mockOrder);

    render(<CartPage />);

    await waitFor(() => {
      expect(screen.getByText(/Quantity: 2 × €12.50/i)).toBeInTheDocument();
      const prices = screen.getAllByText('€25.00');
      expect(prices).toHaveLength(2); // Line total and order total
    });
  });

  it('should display order total amount', async () => {
    const mockOrder: Order = {
      orderId: 'order-123',
      status: 'PENDING',
      totalAmount: 37.5,
      itemCount: 3,
      orderLines: [
        {
          orderLineId: 'line-1',
          menuItemId: 'item-1',
          menuItemName: 'Paella Valenciana',
          quantity: 2,
          unitPrice: 12.5,
          lineTotal: 25.0,
        },
        {
          orderLineId: 'line-2',
          menuItemId: 'item-2',
          menuItemName: 'Gazpacho',
          quantity: 1,
          unitPrice: 12.5,
          lineTotal: 12.5,
        },
      ],
    };

    vi.spyOn(orderService, 'getCart').mockResolvedValue(mockOrder);

    render(<CartPage />);

    await waitFor(() => {
      expect(screen.getByText(/total/i)).toBeInTheDocument();
      expect(screen.getByText('€37.50')).toBeInTheDocument();
    });
  });

  it('should display item count', async () => {
    const mockOrder: Order = {
      orderId: 'order-123',
      status: 'PENDING',
      totalAmount: 37.5,
      itemCount: 3,
      orderLines: [
        {
          orderLineId: 'line-1',
          menuItemId: 'item-1',
          menuItemName: 'Paella Valenciana',
          quantity: 2,
          unitPrice: 12.5,
          lineTotal: 25.0,
        },
        {
          orderLineId: 'line-2',
          menuItemId: 'item-2',
          menuItemName: 'Gazpacho',
          quantity: 1,
          unitPrice: 12.5,
          lineTotal: 12.5,
        },
      ],
    };

    vi.spyOn(orderService, 'getCart').mockResolvedValue(mockOrder);

    render(<CartPage />);

    await waitFor(() => {
      expect(screen.getByText(/3.*item/i)).toBeInTheDocument();
    });
  });

  it('should display correct singular/plural for item count', async () => {
    const mockOrder: Order = {
      orderId: 'order-123',
      status: 'PENDING',
      totalAmount: 12.5,
      itemCount: 1,
      orderLines: [
        {
          orderLineId: 'line-1',
          menuItemId: 'item-1',
          menuItemName: 'Paella Valenciana',
          quantity: 1,
          unitPrice: 12.5,
          lineTotal: 12.5,
        },
      ],
    };

    vi.spyOn(orderService, 'getCart').mockResolvedValue(mockOrder);

    render(<CartPage />);

    await waitFor(() => {
      expect(screen.getByText('1 item')).toBeInTheDocument();
    });
  });
});