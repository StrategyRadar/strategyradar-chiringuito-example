import { BrowserRouter, Routes, Route, useNavigate } from 'react-router-dom';
import { MenuPage } from './pages/MenuPage';
import { CartPage } from './pages/CartPage';
import { CartIcon } from './components/CartIcon';
import { CartProvider, useCart } from './contexts/CartContext';

function AppContent() {
  const navigate = useNavigate();
  const { cart } = useCart();

  const itemCount = cart?.itemCount || 0;

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
              <CartPage />
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
      <CartProvider>
        <AppContent />
      </CartProvider>
    </BrowserRouter>
  );
}

export default App;