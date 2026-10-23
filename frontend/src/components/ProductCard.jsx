import React, { useState } from 'react';
import { ShoppingCart, Check, Sparkles, Tag } from 'lucide-react';

// Curated aesthetic fallback visuals based on category/name
function getProductVisual(category, name) {
  const lowerName = (name || '').toLowerCase();
  const lowerCat = (category || '').toLowerCase();

  if (lowerName.includes('earbud') || lowerName.includes('headphone')) {
    return {
      gradient: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 100%)',
      emoji: '🎧',
      accent: '#818cf8',
    };
  }
  if (lowerName.includes('keyboard')) {
    return {
      gradient: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)',
      emoji: '⌨️',
      accent: '#38bdf8',
    };
  }
  if (lowerName.includes('mug') || lowerCat.includes('kitchen') || lowerCat.includes('home')) {
    return {
      gradient: 'linear-gradient(135deg, #311b1b 0%, #451a03 100%)',
      emoji: '☕',
      accent: '#fb923c',
    };
  }
  if (lowerName.includes('shoe') || lowerCat.includes('footwear')) {
    return {
      gradient: 'linear-gradient(135deg, #064e3b 0%, #022c22 100%)',
      emoji: '👟',
      accent: '#34d399',
    };
  }

  return {
    gradient: 'linear-gradient(135deg, #2e1065 0%, #1e1b4b 100%)',
    emoji: '✨',
    accent: '#a78bfa',
  };
}

export default function ProductCard({ product, onAddToCart, onAskAI }) {
  const [isAdded, setIsAdded] = useState(false);
  const visual = getProductVisual(product.category, product.name);

  const handleAdd = () => {
    onAddToCart(product);
    setIsAdded(true);
    setTimeout(() => setIsAdded(false), 1600);
  };

  const formattedPrice = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(product.price);

  return (
    <article className="product-card">
      {/* Product Image / Visual Canvas */}
      <div className="product-card-visual" style={{ background: visual.gradient }}>
        <span className="product-visual-emoji">{visual.emoji}</span>
        <div className="product-category-tag">
          <Tag size={12} />
          <span>{product.category || 'General'}</span>
        </div>
      </div>

      {/* Product Content Details */}
      <div className="product-card-body">
        <div className="product-header">
          <h3 className="product-title" title={product.name}>{product.name}</h3>
          <div className="product-price">{formattedPrice}</div>
        </div>

        <p className="product-description">{product.description}</p>

        {/* Card Actions */}
        <div className="product-card-actions">
          <button
            type="button"
            className={`btn-add-cart ${isAdded ? 'added' : ''}`}
            onClick={handleAdd}
            aria-label={`Add ${product.name} to cart`}
          >
            {isAdded ? (
              <>
                <Check size={16} />
                <span>Added</span>
              </>
            ) : (
              <>
                <ShoppingCart size={16} />
                <span>Add to Cart</span>
              </>
            )}
          </button>

          <button
            type="button"
            className="btn-card-ai"
            onClick={() => onAskAI(`Tell me about the ${product.name} priced at ${formattedPrice} from your store.`)}
            title="Ask AI Shopping Assistant about this product"
          >
            <Sparkles size={14} />
            <span>Ask AI</span>
          </button>
        </div>
      </div>
    </article>
  );
}
