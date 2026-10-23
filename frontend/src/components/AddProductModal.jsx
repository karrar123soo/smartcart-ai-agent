import React, { useState } from 'react';
import { X, Plus, AlertCircle, CheckCircle } from 'lucide-react';
import { addProduct } from '../api';

const SAMPLE_CATEGORIES = ['Electronics', 'Home & Kitchen', 'Footwear', 'Apparel', 'Accessories', 'Gaming'];

export default function AddProductModal({ isOpen, onClose, onProductAdded }) {
  const [formData, setFormData] = useState({
    name: '',
    category: 'Electronics',
    price: '',
    description: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!formData.name.trim()) {
      setError('Product name is required.');
      return;
    }
    const priceNum = parseFloat(formData.price);
    if (isNaN(priceNum) || priceNum <= 0) {
      setError('Please enter a valid price greater than 0.');
      return;
    }
    if (!formData.description.trim()) {
      setError('Product description is required.');
      return;
    }

    setIsSubmitting(true);
    try {
      const created = await addProduct(formData);
      setSuccess(true);
      setTimeout(() => {
        onProductAdded(created);
        onClose();
        setFormData({ name: '', category: 'Electronics', price: '', description: '' });
        setSuccess(false);
      }, 900);
    } catch (err) {
      setError(err.message || 'Failed to save product to MySQL database.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay animate-fade-in" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-header-text">
            <h3>Add New Product</h3>
            <p>Saves directly to MySQL database via Spring Boot <code>/api/products</code></p>
          </div>
          <button type="button" className="modal-close-btn" onClick={onClose} aria-label="Close dialog">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {error && (
            <div className="form-error-alert">
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="form-success-alert">
              <CheckCircle size={16} />
              <span>Product successfully saved to MySQL database!</span>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="prod-name">Product Name *</label>
            <input
              id="prod-name"
              type="text"
              placeholder="e.g. Ultra Noise-Canceling Headphones"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
            />
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label htmlFor="prod-category">Category *</label>
              <select
                id="prod-category"
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
              >
                {SAMPLE_CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="prod-price">Price ($) *</label>
              <input
                id="prod-price"
                type="number"
                step="0.01"
                min="0.01"
                placeholder="e.g. 79.99"
                value={formData.price}
                onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="prod-desc">Description *</label>
            <textarea
              id="prod-desc"
              rows={3}
              placeholder="Detailed description, key features, specifications..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              required
            />
          </div>

          <div className="modal-actions">
            <button type="button" className="btn-modal-cancel" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </button>
            <button type="submit" className="btn-modal-submit" disabled={isSubmitting}>
              {isSubmitting ? 'Saving to MySQL...' : (
                <>
                  <Plus size={16} />
                  <span>Save to Catalog</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
