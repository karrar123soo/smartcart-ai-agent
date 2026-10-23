import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import HeroSection from './components/HeroSection';
import ProductGrid from './components/ProductGrid';
import AIChatPanel from './components/AIChatPanel';
import AddProductModal from './components/AddProductModal';
import CartDrawer from './components/CartDrawer';
import { getProducts } from './api';
import { Sparkles, Bot, Heart, Layers, Cpu, Database } from 'lucide-react';
import './App.css';

export default function App() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Search & Navigation
  const [searchTerm, setSearchTerm] = useState('');

  // Modals & Drawers
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [externalPrompt, setExternalPrompt] = useState('');
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isCartOpen, setIsCartOpen] = useState(false);

  // Cart State (stored in localStorage for convenience)
  const [cartItems, setCartItems] = useState(() => {
    try {
      const saved = localStorage.getItem('smartcart_cart');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem('smartcart_cart', JSON.stringify(cartItems));
    } catch (e) {
      console.warn('Could not persist cart:', e);
    }
  }, [cartItems]);

  // Load products from backend
  const loadProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getProducts();
      setProducts(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to load products:', err);
      setError(err.message || 'Error connecting to Spring Boot backend.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  // Cart operations
  const handleAddToCart = (product) => {
    setCartItems((prev) => {
      const existing = prev.find((item) => item.id === product.id);
      if (existing) {
        return prev.map((item) =>
          item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      return [...prev, { ...product, quantity: 1 }];
    });
  };

  const handleUpdateQuantity = (productId, newQty) => {
    if (newQty <= 0) {
      handleRemoveFromCart(productId);
      return;
    }
    setCartItems((prev) =>
      prev.map((item) => (item.id === productId ? { ...item, quantity: newQty } : item))
    );
  };

  const handleRemoveFromCart = (productId) => {
    setCartItems((prev) => prev.filter((item) => item.id !== productId));
  };

  const handleClearCart = () => {
    setCartItems([]);
  };

  // Open Chat with a specific prompt
  const handleOpenChatWithPrompt = (promptText) => {
    setExternalPrompt(promptText);
    setIsChatOpen(true);
  };

  const handleOpenEmptyChat = () => {
    setExternalPrompt('');
    setIsChatOpen(true);
  };

  // Callback when product is added from modal
  const handleProductAdded = (newProduct) => {
    setProducts((prev) => [newProduct, ...prev]);
  };

  const totalCartCount = cartItems.reduce((acc, item) => acc + item.quantity, 0);

  return (
    <div className="app-root">
      {/* Navigation Bar */}
      <Navbar
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
        onOpenChat={handleOpenEmptyChat}
        onOpenAddModal={() => setIsAddModalOpen(true)}
        onOpenCart={() => setIsCartOpen(true)}
        cartCount={totalCartCount}
      />

      {/* Main Content Area */}
      <main className="main-content">
        {/* Hero Section */}
        <HeroSection onSelectPrompt={handleOpenChatWithPrompt} />

        {/* Live Catalog Product Grid */}
        <ProductGrid
          products={products}
          loading={loading}
          error={error}
          onRefresh={loadProducts}
          searchTerm={searchTerm}
          onAddToCart={handleAddToCart}
          onAskAI={handleOpenChatWithPrompt}
        />
      </main>

      {/* Floating AI Assistant Trigger Button */}
      <div className="floating-ai-anchor">
        <button
          type="button"
          className="floating-ai-btn"
          onClick={handleOpenEmptyChat}
          aria-label="Open SmartCart AI Assistant"
        >
          <div className="floating-btn-glow"></div>
          <Sparkles size={20} className="floating-sparkle" />
          <span className="floating-label">Ask SmartCart AI</span>
          <span className="floating-online-dot"></span>
        </button>
      </div>

      {/* Slide-Over AI Chat Assistant Panel */}
      <AIChatPanel
        isOpen={isChatOpen}
        onClose={() => setIsChatOpen(false)}
        externalPrompt={externalPrompt}
      />

      {/* Cart Drawer */}
      <CartDrawer
        isOpen={isCartOpen}
        onClose={() => setIsCartOpen(false)}
        cartItems={cartItems}
        onUpdateQuantity={handleUpdateQuantity}
        onRemoveItem={handleRemoveFromCart}
        onClearCart={handleClearCart}
      />

      {/* Add Product Modal */}
      <AddProductModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onProductAdded={handleProductAdded}
      />

      {/* Developer Portfolio Footer */}
      <footer className="footer-wrapper">
        <div className="footer-container">
          <div className="footer-brand-column">
            <div className="footer-logo">
              <Bot size={22} className="gradient-icon" />
              <span>SmartCart AI</span>
            </div>
            <p className="footer-tagline">
              Autonomous AI Shopping Agent architecture utilizing Spring Boot, Spring AI Tool Calling,
              Google Gemini 3.5, and MySQL relational persistence.
            </p>
          </div>

          <div className="footer-tech-stack">
            <h4>Architecture Highlights</h4>
            <div className="tech-tags-list">
              <span className="tech-tag"><Cpu size={12} /> Java 17 + Spring Boot 4.1.1</span>
              <span className="tech-tag"><Sparkles size={12} /> Spring AI 2.0.1 @Tool</span>
              <span className="tech-tag"><Bot size={12} /> Google Gemini 3.5 Flash</span>
              <span className="tech-tag"><Database size={12} /> MySQL 8.0 Catalog Sync</span>
              <span className="tech-tag"><Layers size={12} /> React 19 + Vite</span>
            </div>
          </div>
        </div>

        <div className="footer-bottom">
          <p>SmartCart AI Agent • Developer Portfolio Project</p>
          <div className="footer-endpoints-pill">
            Backend Port: <code>8081</code> • APIs: <code>/api/products</code> & <code>/api/ai/chat</code>
          </div>
        </div>
      </footer>
    </div>
  );
}
