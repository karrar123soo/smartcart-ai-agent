import React, { useState } from 'react';
import { X, Trash2, Plus, Minus, ShoppingBag, CheckCircle, ArrowRight } from 'lucide-react';
import { getProductImage, handleImageError } from '../utils/productImages';

export default function CartDrawer({
  isOpen,
  onClose,
  cartItems,
  onUpdateQuantity,
  onRemoveItem,
  onClearCart,
}) {
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [checkoutSuccess, setCheckoutSuccess] = useState(false);

  if (!isOpen) return null;

  const subtotal = cartItems.reduce(
    (acc, item) => acc + (item.price || 0) * (item.quantity || 1),
    0
  );

  const handleCheckout = () => {
    setIsCheckingOut(true);
    setTimeout(() => {
      setIsCheckingOut(false);
      setCheckoutSuccess(true);
      setTimeout(() => {
        onClearCart();
        setCheckoutSuccess(false);
        onClose();
      }, 1500);
    }, 1000);
  };

  return (
    <div className="cart-drawer-overlay animate-fade-in" onClick={onClose} role="dialog" aria-modal="true">
      <div className="cart-drawer-container" onClick={(e) => e.stopPropagation()}>
        <div className="cart-header">
          <div className="cart-header-title">
            <ShoppingBag size={20} />
            <h3>Your Shopping Cart</h3>
            <span className="cart-count-pill">{cartItems.length}</span>
          </div>
          <button type="button" className="cart-close-btn" onClick={onClose} aria-label="Close cart">
            <X size={18} />
          </button>
        </div>

        {checkoutSuccess ? (
          <div className="checkout-success-view">
            <CheckCircle size={56} className="success-icon" />
            <h3>Order Confirmed!</h3>
            <p>Thank you for shopping with SmartCart AI. Your simulated order has been placed.</p>
          </div>
        ) : (
          <>
            <div className="cart-items-list">
              {cartItems.length === 0 ? (
                <div className="cart-empty-state">
                  <ShoppingBag size={44} className="empty-bag-icon" />
                  <p>Your cart is empty.</p>
                  <span>Explore our live store catalog and add items!</span>
                </div>
              ) : (
                cartItems.map((item) => (
                  <div key={item.id} className="cart-item-card">
                    <div className="cart-item-thumb">
                      <img
                        src={getProductImage(item)}
                        alt={item.name}
                        loading="lazy"
                        onError={handleImageError}
                      />
                    </div>
                    <div className="cart-item-info">
                      <h4>{item.name}</h4>
                      <span className="cart-item-cat">{item.category}</span>
                      <div className="cart-item-price">
                        ${(item.price * item.quantity).toFixed(2)}
                      </div>
                    </div>

                    <div className="cart-item-controls">
                      <div className="qty-picker">
                        <button
                          type="button"
                          onClick={() => onUpdateQuantity(item.id, item.quantity - 1)}
                          aria-label="Decrease quantity"
                        >
                          <Minus size={13} />
                        </button>
                        <span>{item.quantity}</span>
                        <button
                          type="button"
                          onClick={() => onUpdateQuantity(item.id, item.quantity + 1)}
                          aria-label="Increase quantity"
                        >
                          <Plus size={13} />
                        </button>
                      </div>

                      <button
                        type="button"
                        className="btn-remove-item"
                        onClick={() => onRemoveItem(item.id)}
                        title="Remove item"
                      >
                        <Trash2 size={15} />
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>

            {cartItems.length > 0 && (
              <div className="cart-footer">
                <div className="cart-subtotal-row">
                  <span>Subtotal:</span>
                  <strong>${subtotal.toFixed(2)}</strong>
                </div>

                <div className="cart-actions-group">
                  <button
                    type="button"
                    className="btn-checkout"
                    onClick={handleCheckout}
                    disabled={isCheckingOut}
                  >
                    {isCheckingOut ? (
                      <span>Processing...</span>
                    ) : (
                      <>
                        <span>Proceed to Checkout</span>
                        <ArrowRight size={16} />
                      </>
                    )}
                  </button>

                  <button
                    type="button"
                    className="btn-clear-cart"
                    onClick={onClearCart}
                    disabled={isCheckingOut}
                  >
                    Clear Cart
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
