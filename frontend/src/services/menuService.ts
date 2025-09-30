import type { MenuItem } from '../types/MenuItem';

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function getMenuItems(): Promise<MenuItem[]> {
  const response = await fetch(`${API_BASE_URL}/api/menu`);

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response.json();
}