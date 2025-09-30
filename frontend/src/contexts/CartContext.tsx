import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { getCart } from '../services/orderService';
import type { Order } from '../types/Order';

interface CartContextType {
  cart: Order | null;
  refreshCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<Order | null>(null);

  const refreshCart = async () => {
    try {
      const cartData = await getCart();
      setCart(cartData);
    } catch (err) {
      console.error('Failed to load cart:', err);
    }
  };

  useEffect(() => {
    refreshCart();
  }, []);

  return (
    <CartContext.Provider value={{ cart, refreshCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
}