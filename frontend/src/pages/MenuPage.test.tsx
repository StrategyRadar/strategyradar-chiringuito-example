import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MenuPage } from './MenuPage';
import * as menuService from '../services/menuService';
import type { MenuItem } from '../types/MenuItem';

// Mock the menu service
vi.mock('../services/menuService');

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
});