import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CartPage } from './CartPage';
import type { Order } from '../types/Order';

describe('CartPage', () => {
  it('should display empty cart message when order is null', () => {
    render(<CartPage order={null} loading={false} error={null} />);

    expect(screen.getByText(/your cart is empty/i)).toBeInTheDocument();
  });

  it('should display loading state', () => {
    render(<CartPage order={null} loading={true} error={null} />);

    expect(screen.getByText(/loading cart/i)).toBeInTheDocument();
  });

  it('should display error message', () => {
    render(
      <CartPage
        order={null}
        loading={false}
        error="Failed to load cart"
      />
    );

    expect(screen.getByText(/error.*failed to load cart/i)).toBeInTheDocument();
  });

  it('should display order items with details', () => {
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

    render(<CartPage order={mockOrder} loading={false} error={null} />);

    expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
    expect(screen.getByText('Gazpacho')).toBeInTheDocument();
    expect(screen.getByText(/Quantity: 2/i)).toBeInTheDocument();
    expect(screen.getByText(/Quantity: 1/i)).toBeInTheDocument();
  });

  it('should display unit prices and line totals', () => {
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

    render(<CartPage order={mockOrder} loading={false} error={null} />);

    expect(screen.getByText(/Quantity: 2 × €12.50/i)).toBeInTheDocument();
    const prices = screen.getAllByText('€25.00');
    expect(prices).toHaveLength(2); // Line total and order total
  });

  it('should display order total amount', () => {
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

    render(<CartPage order={mockOrder} loading={false} error={null} />);

    expect(screen.getByText(/total/i)).toBeInTheDocument();
    expect(screen.getByText('€37.50')).toBeInTheDocument();
  });

  it('should display item count', () => {
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

    render(<CartPage order={mockOrder} loading={false} error={null} />);

    expect(screen.getByText(/3.*item/i)).toBeInTheDocument();
  });

  it('should display correct singular/plural for item count', () => {
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

    render(<CartPage order={mockOrder} loading={false} error={null} />);

    expect(screen.getByText('1 item')).toBeInTheDocument();
  });
});