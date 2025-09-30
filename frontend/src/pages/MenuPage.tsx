import { useEffect, useState } from 'react';
import { getMenuItems } from '../services/menuService';
import { addItemToOrder, updateItemQuantity, removeItemFromOrder } from '../services/orderService';
import { QuantitySelector } from '../components/QuantitySelector';
import { useCart } from '../contexts/CartContext';
import type { MenuItem } from '../types/MenuItem';

export function MenuPage() {
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [quantities, setQuantities] = useState<Record<string, number>>({});
  const [addingToCart, setAddingToCart] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [cartError, setCartError] = useState<string | null>(null);
  const { cart, refreshCart } = useCart();

  // Sync quantities with cart contents
  useEffect(() => {
    if (cart) {
      const cartQuantities: Record<string, number> = {};
      cart.orderLines.forEach((line) => {
        cartQuantities[line.menuItemId] = line.quantity;
      });

      setQuantities((prev) => {
        const newQuantities = { ...prev };
        Object.keys(newQuantities).forEach((itemId) => {
          newQuantities[itemId] = cartQuantities[itemId] || 0;
        });
        return newQuantities;
      });
    }
  }, [cart]);

  useEffect(() => {
    async function loadMenu() {
      try {
        const items = await getMenuItems();
        setMenuItems(items);
        // Initialize quantities to 0 for each item
        const initialQuantities: Record<string, number> = {};
        items.forEach((item) => {
          initialQuantities[item.id] = 0;
        });
        setQuantities(initialQuantities);
      } catch (err) {
        setError('Failed to load menu. Please try again later.');
      } finally {
        setLoading(false);
      }
    }

    loadMenu();
  }, []);

  const handleQuantityChange = async (itemId: string, newQuantity: number) => {
    // Update local state immediately for responsive UI
    setQuantities((prev) => ({
      ...prev,
      [itemId]: newQuantity,
    }));

    // If item is already in cart, update quantity via API or remove if 0
    const existingLine = cart?.orderLines.find((line) => line.menuItemId === itemId);
    if (existingLine && cart) {
      try {
        if (newQuantity === 0) {
          // Remove item from cart when quantity reaches 0
          await removeItemFromOrder(itemId);
        } else {
          // Update quantity
          await updateItemQuantity(itemId, newQuantity);
        }
        await refreshCart();
      } catch (err) {
        setCartError('Failed to update quantity. Please try again.');
        setTimeout(() => {
          setCartError(null);
        }, 3000);
      }
    }
  };

  const handleAddToCart = async (item: MenuItem) => {
    setAddingToCart(item.id);
    setSuccessMessage(null);
    setCartError(null);

    try {
      // Always add 1 item, like the plus button
      await addItemToOrder({
        menuItemId: item.id,
        quantity: 1,
      });

      setSuccessMessage(`${item.name} added to cart!`);
      await refreshCart();

      // Clear success message after 3 seconds
      setTimeout(() => {
        setSuccessMessage(null);
      }, 3000);
    } catch (err) {
      setCartError('Failed to add item to cart. Please try again.');
      setTimeout(() => {
        setCartError(null);
      }, 3000);
    } finally {
      setAddingToCart(null);
    }
  };

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

        {/* Success/Error messages with fixed height to prevent layout shift */}
        <div className="mb-4 h-14 flex items-center justify-center">
          {successMessage && (
            <div className="w-full p-4 bg-green-100 text-green-800 rounded-lg text-center">
              {successMessage}
            </div>
          )}
          {cartError && (
            <div className="w-full p-4 bg-red-100 text-red-800 rounded-lg text-center">
              {cartError}
            </div>
          )}
        </div>

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
                <h2 className="text-xl font-semibold mb-2">{item.name}</h2>
                <p className="text-gray-600 mb-4">{item.description}</p>
                <p className="text-lg font-bold text-blue-600 mb-4">
                  €{item.price.toFixed(2)}
                </p>

                {/* Quantity Selector */}
                <div className="flex items-center justify-between mb-4">
                  <span className="text-sm text-gray-600">Quantity:</span>
                  <QuantitySelector
                    quantity={quantities[item.id] ?? 0}
                    onChange={(newQuantity) =>
                      handleQuantityChange(item.id, newQuantity)
                    }
                  />
                </div>

                {/* Add to Cart Button */}
                <button
                  type="button"
                  onClick={() => handleAddToCart(item)}
                  disabled={addingToCart === item.id}
                  className="w-full py-2 px-4 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                >
                  {addingToCart === item.id ? 'Adding...' : 'Add to Cart'}
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}