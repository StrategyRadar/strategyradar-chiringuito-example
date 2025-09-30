import { useEffect, useState } from 'react';
import { removeItemFromOrder } from '../services/orderService';
import { useCart } from '../contexts/CartContext';

export function CartPage() {
  const { cart: order, refreshCart } = useCart();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [removing, setRemoving] = useState<string | null>(null);

  useEffect(() => {
    setLoading(false);
  }, []);

  const handleRemoveItem = async (menuItemId: string) => {
    setRemoving(menuItemId);
    try {
      await removeItemFromOrder(menuItemId);
      await refreshCart();
    } catch (err) {
      setError('Failed to remove item. Please try again.');
    } finally {
      setRemoving(null);
    }
  };
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-lg">Loading cart...</p>
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

  if (!order || order.orderLines.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-lg text-gray-600">Your cart is empty</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-center mb-8 mt-4">
          Your Cart
        </h1>

        <div className="bg-white rounded-lg shadow-md p-6">
          {/* Cart items */}
          <div className="space-y-4 mb-6">
            {order.orderLines.map((line) => (
              <div
                key={line.orderLineId}
                className="flex justify-between items-center border-b pb-4 last:border-b-0"
              >
                <div className="flex-1">
                  <h3 className="text-lg font-semibold">{line.menuItemName}</h3>
                  <p className="text-gray-600">
                    Quantity: {line.quantity} × €{line.unitPrice.toFixed(2)}
                  </p>
                </div>
                <div className="flex items-center gap-4">
                  <p className="text-lg font-bold text-blue-600">
                    €{line.lineTotal.toFixed(2)}
                  </p>
                  <button
                    type="button"
                    onClick={() => handleRemoveItem(line.menuItemId)}
                    disabled={removing === line.menuItemId}
                    className="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                  >
                    {removing === line.menuItemId ? 'Removing...' : 'Remove'}
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Order summary */}
          <div className="border-t pt-4">
            <div className="flex justify-between items-center mb-2">
              <p className="text-gray-600">
                {order.itemCount} {order.itemCount === 1 ? 'item' : 'items'}
              </p>
            </div>
            <div className="flex justify-between items-center">
              <p className="text-xl font-bold">Total</p>
              <p className="text-2xl font-bold text-blue-600">
                €{order.totalAmount.toFixed(2)}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}