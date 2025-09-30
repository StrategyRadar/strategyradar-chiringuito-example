import { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, useNavigate } from 'react-router-dom';
import { MenuPage } from './pages/MenuPage';
import { CartPage } from './pages/CartPage';
import { CartIcon } from './components/CartIcon';
import type { Order } from './types/Order';

function AppContent() {
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // In a real app, we would fetch the current order from the backend on mount
  // For now, we'll just use local state that gets updated when items are added
  useEffect(() => {
    // Future: Fetch current order from backend
    // This would call GET /api/order/current or similar
  }, []);

  const itemCount = order?.itemCount || 0;

  const handleCartClick = () => {
    navigate('/cart');
  };

  const handleBackToMenu = () => {
    navigate('/menu');
  };

  return (
    <div className="relative">
      {/* Cart Icon - fixed in top right corner */}
      <div className="fixed top-4 right-4 z-50">
        <CartIcon itemCount={itemCount} onClick={handleCartClick} />
      </div>

      <Routes>
        <Route path="/" element={<MenuPage />} />
        <Route path="/menu" element={<MenuPage />} />
        <Route
          path="/cart"
          element={
            <div>
              <button
                type="button"
                onClick={handleBackToMenu}
                className="fixed top-4 left-4 z-50 px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg font-semibold transition-colors"
              >
                ← Back to Menu
              </button>
              <CartPage order={order} loading={loading} error={error} />
            </div>
          }
        />
      </Routes>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

export default App;