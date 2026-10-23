import React, { useState } from 'react';
import { ShoppingBag, Sparkles, Search, PlusCircle, ShoppingCart, X } from 'lucide-react';

export default function Navbar({
  searchTerm,
  setSearchTerm,
  onOpenChat,
  onOpenAddModal,
  onOpenCart,
  cartCount,
}) {
  const [isSearchFocused, setIsSearchFocused] = useState(false);

  return (
    <header className="navbar-wrapper">
      <div className="navbar-container">
        {/* Brand Logo */}
        <div className="navbar-brand">
          <div className="brand-logo-icon">
            <ShoppingBag className="icon-bag" size={22} />
            <Sparkles className="icon-sparkle" size={14} />
          </div>
          <div className="brand-text-group">
            <span className="brand-title">SmartCart<span className="brand-ai-badge">AI</span></span>
            <span className="brand-subtitle">Gemini Agent Store</span>
          </div>
        </div>

        {/* Search Bar */}
        <div className={`navbar-search ${isSearchFocused ? 'focused' : ''}`}>
          <Search size={18} className="search-icon" aria-hidden="true" />
          <input
            type="text"
            placeholder="Search products by name, category, or specs..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onFocus={() => setIsSearchFocused(true)}
            onBlur={() => setIsSearchFocused(false)}
            aria-label="Search catalog products"
          />
          {searchTerm && (
            <button
              type="button"
              className="search-clear-btn"
              onClick={() => setSearchTerm('')}
              aria-label="Clear search"
            >
              <X size={15} />
            </button>
          )}
        </div>

        {/* Action Controls */}
        <div className="navbar-actions">
          {/* Add Product Modal Trigger */}
          <button
            type="button"
            className="btn-secondary"
            onClick={onOpenAddModal}
            title="Add a new product to MySQL database"
          >
            <PlusCircle size={17} />
            <span className="btn-label-desktop">Add Item</span>
          </button>

          {/* Cart Trigger */}
          <button
            type="button"
            className="btn-icon-badge"
            onClick={onOpenCart}
            aria-label={`View shopping cart with ${cartCount} items`}
            title="Shopping Cart"
          >
            <ShoppingCart size={20} />
            {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
          </button>

          {/* AI Assistant Trigger Button */}
          <button
            type="button"
            className="btn-ai-trigger"
            onClick={() => onOpenChat()}
            aria-label="Open AI Shopping Assistant chat"
          >
            <Sparkles size={17} className="sparkle-spin" />
            <span>Ask AI</span>
            <span className="status-dot-pulse"></span>
          </button>
        </div>
      </div>
    </header>
  );
}
