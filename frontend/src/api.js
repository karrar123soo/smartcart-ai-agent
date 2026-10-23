/**
 * SmartCart AI API Client
 * Configured to communicate with the Spring Boot backend at http://localhost:8081
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

/**
 * Fetch all products from the catalog
 * @returns {Promise<Array<{id: number, name: string, category: string, price: number, description: string}>>}
 */
export async function getProducts() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/products`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch products: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Error fetching products:', error);
    throw error;
  }
}

/**
 * Add a new product to the catalog
 * @param {{name: string, category: string, price: number, description: string}} product
 * @returns {Promise<Object>}
 */
export async function addProduct(product) {
  try {
    const response = await fetch(`${API_BASE_URL}/api/products`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: JSON.stringify({
        name: product.name.trim(),
        category: product.category.trim(),
        price: parseFloat(product.price),
        description: product.description.trim(),
      }),
    });

    if (!response.ok) {
      throw new Error(`Failed to add product: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Error adding product:', error);
    throw error;
  }
}

/**
 * Send a chat message to the Gemini AI shopping assistant
 * @param {string} message
 * @returns {Promise<string>} Text response from Gemini AI
 */
export async function sendChatMessage(message) {
  try {
    const response = await fetch(`${API_BASE_URL}/api/ai/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/plain, application/json, */*',
      },
      body: JSON.stringify({ message: message.trim() }),
    });

    if (!response.ok) {
      const errorText = await response.text().catch(() => '');
      throw new Error(errorText || `AI Chat failed with status: ${response.status}`);
    }

    // Backend returns plain text String from AIChatController
    return await response.text();
  } catch (error) {
    console.error('Error sending chat message:', error);
    throw error;
  }
}

export { API_BASE_URL };
