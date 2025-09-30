import { useEffect, useState } from 'react';
import { getMenuItems } from '../services/menuService';
import type { MenuItem } from '../types/MenuItem';

export function MenuPage() {
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadMenu() {
      try {
        const items = await getMenuItems();
        setMenuItems(items);
      } catch (err) {
        setError('Failed to load menu. Please try again later.');
      } finally {
        setLoading(false);
      }
    }

    loadMenu();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-lg">Loading menu...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-lg text-red-600">Error: {error}</p>
      </div>
    );
  }

  if (menuItems.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-lg">No menu items available at the moment.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-3xl font-bold text-center mb-8 mt-4">
          El Chiringuito Menu
        </h1>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {menuItems.map((item) => (
            <div
              key={item.id}
              className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
            >
              <div className="w-full h-48 bg-gradient-to-br from-orange-50 to-yellow-50 flex items-center justify-center">
                <span className="text-8xl">{item.imageUrl}</span>
              </div>
              <div className="p-4">
                <h2 className="text-xl font-semibold mb-2">
                  {item.name}
                </h2>
                <p className="text-gray-600 mb-4">{item.description}</p>
                <p className="text-lg font-bold text-blue-600">
                  €{item.price.toFixed(2)}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}