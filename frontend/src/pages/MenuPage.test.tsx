import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MenuPage } from './MenuPage';
import * as menuService from '../services/menuService';
import * as orderService from '../services/orderService';
import type { MenuItem } from '../types/MenuItem';
import type { Order } from '../types/Order';

// Mock the services
vi.mock('../services/menuService');
vi.mock('../services/orderService');

describe('MenuPage', () => {
  const mockMenuItems: MenuItem[] = [
    {
      id: '1',
      name: 'Paella Valenciana',
      description: 'Traditional Spanish rice dish with seafood',
      price: 12.5,
      imageUrl: 'https://example.com/paella.jpg',
      available: true,
    },
    {
      id: '2',
      name: 'Tortilla Española',
      description: 'Spanish potato omelette with onions',
      price: 6.0,
      imageUrl: 'https://example.com/tortilla.jpg',
      available: true,
    },
  ];

  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('should display loading state initially', () => {
    vi.mocked(menuService.getMenuItems).mockImplementation(
      () => new Promise(() => {})
    );

    render(<MenuPage />);

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('should display menu items after successful fetch', async () => {
    vi.mocked(menuService.getMenuItems).mockResolvedValueOnce(
      mockMenuItems
    );

    render(<MenuPage />);

    await waitFor(() => {
      expect(
        screen.queryByText(/loading/i)
      ).not.toBeInTheDocument();
    });

    expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
    expect(screen.getByText('Tortilla Española')).toBeInTheDocument();
    expect(
      screen.getByText(/Traditional Spanish rice dish/i)
    ).toBeInTheDocument();
    expect(
      screen.getByText(/Spanish potato omelette/i)
    ).toBeInTheDocument();
  });

  it('should display prices correctly formatted', async () => {
    vi.mocked(menuService.getMenuItems).mockResolvedValueOnce(
      mockMenuItems
    );

    render(<MenuPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText(/€12\.50/)).toBeInTheDocument();
    expect(screen.getByText(/€6\.00/)).toBeInTheDocument();
  });

  it('should display error message when fetch fails', async () => {
    vi.mocked(menuService.getMenuItems).mockRejectedValueOnce(
      new Error('Failed to fetch menu')
    );

    render(<MenuPage />);

    await waitFor(() => {
      expect(screen.getByText(/error/i)).toBeInTheDocument();
    });

    expect(
      screen.getByText(/failed to load menu/i)
    ).toBeInTheDocument();
  });

  it('should display message when no menu items available', async () => {
    vi.mocked(menuService.getMenuItems).mockResolvedValueOnce([]);

    render(<MenuPage />);

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    expect(screen.getByText(/no menu items/i)).toBeInTheDocument();
  });

  it('should display all menu item properties', async () => {
    vi.mocked(menuService.getMenuItems).mockResolvedValueOnce([
      mockMenuItems[0],
    ]);

    render(<MenuPage />);

    await waitFor(() => {
      expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
    });

    // Check that emoji is displayed
    expect(
      screen.getByText('https://example.com/paella.jpg')
    ).toBeInTheDocument();
  });

  describe('Add to Cart functionality', () => {
    const mockOrder: Order = {
      orderId: 'order-123',
      status: 'PENDING',
      totalAmount: 25.0,
      itemCount: 2,
      orderLines: [
        {
          orderLineId: 'line-1',
          menuItemId: '1',
          menuItemName: 'Paella Valenciana',
          quantity: 2,
          unitPrice: 12.5,
          lineTotal: 25.0,
        },
      ],
    };

    it('should display quantity selector for each menu item', async () => {
      vi.mocked(menuService.getMenuItems).mockResolvedValueOnce(
        mockMenuItems
      );

      render(<MenuPage />);

      await waitFor(() => {
        expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
      });

      const increaseButtons = screen.getAllByRole('button', {
        name: /increase quantity/i,
      });
      const decreaseButtons = screen.getAllByRole('button', {
        name: /decrease quantity/i,
      });

      expect(increaseButtons).toHaveLength(2);
      expect(decreaseButtons).toHaveLength(2);
    });

    it('should display "Add to Cart" button for each menu item', async () => {
      vi.mocked(menuService.getMenuItems).mockResolvedValueOnce(
        mockMenuItems
      );

      render(<MenuPage />);

      await waitFor(() => {
        expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
      });

      const addToCartButtons = screen.getAllByRole('button', {
        name: /add to cart/i,
      });

      expect(addToCartButtons).toHaveLength(2);
    });

    it('should increment quantity when + button clicked', async () => {
      const user = userEvent.setup();
      vi.mocked(menuService.getMenuItems).mockResolvedValueOnce([
        mockMenuItems[0],
      ]);

      render(<MenuPage />);

      await waitFor(() => {
        expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
      });

      const increaseButton = screen.getByRole('button', {
        name: /increase quantity/i,
      });

      expect(screen.getByText('1')).toBeInTheDocument();

      await user.click(increaseButton);

      expect(screen.getByText('2')).toBeInTheDocument();
    });

    it('should decrement quantity when - button clicked', async () => {
      const user = userEvent.setup();
      vi.mocked(menuService.getMenuItems).mockResolvedValueOnce([
        mockMenuItems[0],
      ]);

      render(<MenuPage />);

      await waitFor(() => {
        expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
      });

      const increaseButton = screen.getByRole('button', {
        name: /increase quantity/i,
      });
      const decreaseButton = screen.getByRole('button', {
        name: /decrease quantity/i,
      });

      await user.click(increaseButton);
      await user.click(increaseButton);
      expect(screen.getByText('3')).toBeInTheDocument();

      await user.click(decreaseButton);
      expect(screen.getByText('2')).toBeInTheDocument();
    });

    it('should call orderService when "Add to Cart" clicked', async () => {
      const user = userEvent.setup();
      vi.mocked(menuService.getMenuItems).mockResolvedValueOnce([
        mockMenuItems[0],
      ]);
      vi.mocked(orderService.addItemToOrder).mockResolvedValueOnce(
        mockOrder
      );

      render(<MenuPage />);

      await waitFor(() => {
        expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
      });

      const increaseButton = screen.getByRole('button', {
        name: /increase quantity/i,
      });
      await user.click(increaseButton);

      const addToCartButton = screen.getByRole('button', {
        name: /add to cart/i,
      });
      await user.click(addToCartButton);

      await waitFor(() => {
        expect(orderService.addItemToOrder).toHaveBeenCalledWith({
          menuItemId: '1',
          quantity: 2,
        });
      });
    });

    it('should display success message after adding to cart', async () => {
      const user = userEvent.setup();
      vi.mocked(menuService.getMenuItems).mockResolvedValueOnce([
        mockMenuItems[0],
      ]);
      vi.mocked(orderService.addItemToOrder).mockResolvedValueOnce(
        mockOrder
      );

      render(<MenuPage />);

      await waitFor(() => {
        expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
      });

      const addToCartButton = screen.getByRole('button', {
        name: /add to cart/i,
      });
      await user.click(addToCartButton);

      await waitFor(() => {
        expect(screen.getByText(/added to cart/i)).toBeInTheDocument();
      });
    });

    it('should display error message when add to cart fails', async () => {
      const user = userEvent.setup();
      vi.mocked(menuService.getMenuItems).mockResolvedValueOnce([
        mockMenuItems[0],
      ]);
      vi.mocked(orderService.addItemToOrder).mockRejectedValueOnce(
        new Error('Failed to add item')
      );

      render(<MenuPage />);

      await waitFor(() => {
        expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
      });

      const addToCartButton = screen.getByRole('button', {
        name: /add to cart/i,
      });
      await user.click(addToCartButton);

      await waitFor(() => {
        expect(
          screen.getByText(/failed to add item to cart/i)
        ).toBeInTheDocument();
      });
    });

    it('should reset quantity to 1 after successful add to cart', async () => {
      const user = userEvent.setup();
      vi.mocked(menuService.getMenuItems).mockResolvedValueOnce([
        mockMenuItems[0],
      ]);
      vi.mocked(orderService.addItemToOrder).mockResolvedValueOnce(
        mockOrder
      );

      render(<MenuPage />);

      await waitFor(() => {
        expect(screen.getByText('Paella Valenciana')).toBeInTheDocument();
      });

      const increaseButton = screen.getByRole('button', {
        name: /increase quantity/i,
      });
      await user.click(increaseButton);
      expect(screen.getByText('2')).toBeInTheDocument();

      const addToCartButton = screen.getByRole('button', {
        name: /add to cart/i,
      });
      await user.click(addToCartButton);

      await waitFor(() => {
        expect(screen.getByText(/added to cart/i)).toBeInTheDocument();
      });

      expect(screen.getByText('1')).toBeInTheDocument();
    });
  });
});