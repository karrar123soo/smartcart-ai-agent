import React, { useState, useMemo } from 'react';
import ProductCard from './ProductCard';
import { RefreshCw, Filter, AlertTriangle, Search, PackageOpen } from 'lucide-react';

export default function ProductGrid({
  products,
  loading,
  error,
  onRefresh,
  searchTerm,
  onAddToCart,
  onAskAI,
}) {
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [sortBy, setSortBy] = useState('default');

  // Extract unique categories dynamically
  const categories = useMemo(() => {
    const cats = new Set(products.map((p) => p.category).filter(Boolean));
    return ['All', ...Array.from(cats)];
  }, [products]);

  // Filter and sort products
  const filteredProducts = useMemo(() => {
    let result = [...products];

    // Category filter
    if (selectedCategory !== 'All') {
      result = result.filter(
        (p) => (p.category || '').toLowerCase() === selectedCategory.toLowerCase()
      );
    }

    // Search filter
    if (searchTerm && searchTerm.trim()) {
      const q = searchTerm.toLowerCase().trim();
      result = result.filter(
        (p) =>
          (p.name || '').toLowerCase().includes(q) ||
          (p.category || '').toLowerCase().includes(q) ||
          (p.description || '').toLowerCase().includes(q)
      );
    }

    // Sorting
    if (sortBy === 'price-low') {
      result.sort((a, b) => a.price - b.price);
    } else if (sortBy === 'price-high') {
      result.sort((a, b) => b.price - a.price);
    } else if (sortBy === 'name-asc') {
      result.sort((a, b) => a.name.localeCompare(b.name));
    }

    return result;
  }, [products, selectedCategory, searchTerm, sortBy]);

  return (
    <section className="catalog-section" id="catalog">
      <div className="catalog-header">
        <div className="catalog-title-group">
          <h2 className="catalog-title">Live Store Catalog</h2>
          <span className="catalog-badge">
            {loading ? 'Refreshing...' : `${filteredProducts.length} items`}
          </span>
        </div>

        {/* Action Controls & Filters */}
        <div className="catalog-controls">
          {/* Refresh Button */}
          <button
            type="button"
            className={`btn-refresh ${loading ? 'loading' : ''}`}
            onClick={onRefresh}
            title="Reload products from MySQL backend"
            disabled={loading}
          >
            <RefreshCw size={15} />
            <span>Refresh</span>
          </button>

          {/* Sort Dropdown */}
          <div className="select-wrapper">
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="select-sort"
              aria-label="Sort products"
            >
              <option value="default">Sort: Default</option>
              <option value="price-low">Price: Low to High</option>
              <option value="price-high">Price: High to Low</option>
              <option value="name-asc">Name: A to Z</option>
            </select>
          </div>
        </div>
      </div>

      {/* Category Pills Bar */}
      <div className="category-pills-bar">
        {categories.map((cat) => (
          <button
            key={cat}
            type="button"
            className={`category-pill ${selectedCategory === cat ? 'active' : ''}`}
            onClick={() => setSelectedCategory(cat)}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Error Notice */}
      {error && (
        <div className="catalog-error-box">
          <AlertTriangle size={24} className="error-icon" />
          <div className="error-text">
            <strong>Unable to connect to MySQL backend at http://localhost:8081</strong>
            <p>Ensure your Spring Boot backend application is running on port 8081.</p>
          </div>
          <button type="button" className="btn-retry" onClick={onRefresh}>
            Retry Now
          </button>
        </div>
      )}

      {/* Loading Skeletons */}
      {loading && (
        <div className="product-grid-layout">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="product-card skeleton-card">
              <div className="skeleton-visual"></div>
              <div className="skeleton-body">
                <div className="skeleton-line line-title"></div>
                <div className="skeleton-line line-desc"></div>
                <div className="skeleton-line line-btn"></div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Product Grid */}
      {!loading && !error && filteredProducts.length > 0 && (
        <div className="product-grid-layout">
          {filteredProducts.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onAddToCart={onAddToCart}
              onAskAI={onAskAI}
            />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!loading && !error && filteredProducts.length === 0 && (
        <div className="catalog-empty-state">
          <PackageOpen size={48} className="empty-icon" />
          <h3>No products found</h3>
          <p>
            {searchTerm
              ? `No catalog items matched "${searchTerm}". You can ask the AI Assistant to search related items or add a new product.`
              : 'There are currently no products available in the store.'}
          </p>
          {searchTerm && (
            <button
              type="button"
              className="btn-ai-trigger"
              style={{ marginTop: '1rem' }}
              onClick={() => onAskAI(`Search for ${searchTerm} or recommend similar alternatives.`)}
            >
              Ask AI about "{searchTerm}"
            </button>
          )}
        </div>
      )}
    </section>
  );
}
