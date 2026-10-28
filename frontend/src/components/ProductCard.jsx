import React, { useState } from 'react';
import { ShoppingCart, Check, Sparkles, Tag } from 'lucide-react';
import { getProductImage, handleImageError } from '../utils/productImages';

export default function ProductCard({ product, onAddToCart, onAskAI }) {
  const [isAdded, setIsAdded] = useState(false);
  const [imgLoaded, setImgLoaded] = useState(false);

  const handleAdd = () => {
    onAddToCart(product);
    setIsAdded(true);
    setTimeout(() => setIsAdded(false), 1600);
  };

  const formattedPrice = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(product.price);

  const imageUrl = getProductImage(product);

  return (
    <article className="product-card">
      {/* Product Image Canvas */}
      <div className={`product-card-visual ${imgLoaded ? 'loaded' : 'loading'}`}>
        <img
          src={imageUrl}
          alt={product.name}
          className="product-card-img"
          loading="lazy"
          decoding="async"
          onLoad={() => setImgLoaded(true)}
          onError={handleImageError}
        />
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
            aria-label={`Ask AI Shopping Assistant about ${product.name}`}
          >
            <Sparkles size={14} />
            <span>Ask AI</span>
          </button>
        </div>
      </div>
    </article>
  );
}
