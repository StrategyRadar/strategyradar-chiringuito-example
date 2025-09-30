import { describe, it, expect, beforeEach, vi } from 'vitest';
import { getMenuItems } from './menuService';
import type { MenuItem } from '../types/MenuItem';

// Mock fetch globally
global.fetch = vi.fn();

describe('MenuService', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  describe('getMenuItems', () => {
    it('should fetch menu items successfully', async () => {
      const mockMenuItems: MenuItem[] = [
        {
          id: '1',
          name: 'Paella Valenciana',
          description: 'Traditional Spanish rice dish',
          price: 12.5,
          imageUrl: 'https://example.com/paella.jpg',
          available: true,
        },
        {
          id: '2',
          name: 'Tortilla Española',
          description: 'Spanish potato omelette',
          price: 6.0,
          imageUrl: 'https://example.com/tortilla.jpg',
          available: true,
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockMenuItems,
      });

      const result = await getMenuItems();

      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/menu'
      );
      expect(result).toEqual(mockMenuItems);
      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('Paella Valenciana');
    });

    it('should throw error when fetch fails', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 500,
      });

      await expect(getMenuItems()).rejects.toThrow(
        'HTTP error! status: 500'
      );
    });

    it('should throw error when network fails', async () => {
      (global.fetch as any).mockRejectedValueOnce(
        new Error('Network error')
      );

      await expect(getMenuItems()).rejects.toThrow('Network error');
    });

    it('should return empty array when no menu items', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      });

      const result = await getMenuItems();

      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });
  });
});