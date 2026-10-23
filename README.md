# SmartCart AI — Autonomous AI Shopping Agent

A full-stack, enterprise-grade e-commerce application powered by **Java 17**, **Spring Boot 4.1.1**, **Spring AI 2.0.1**, **Google Gemini**, and a modern **React 19 + Vite** frontend.

The AI assistant connects directly to a live **MySQL database** via Spring AI `@Tool` calling to answer product inquiries, check exact verified prices, and offer budget-friendly recommendations without hallucinations.

---

## Architecture Overview

- **Backend:** Spring Boot 4.1.1, Java 17
- **AI Framework:** Spring AI 2.0.1 (`spring-ai-starter-model-google-genai`)
- **LLM:** Google Gemini 3.5 Flash
- **AI Tools:** `ProductTools` (`searchProducts`, `getProductsByMaxPrice`)
- **Database:** MySQL 8.0 (`smartcart_db`)
- **Frontend:** React 19, Vite, Lucide Icons, Modern CSS Design System
- **Ports:**
  - Backend API: `http://localhost:8081`
  - Frontend Web UI: `http://localhost:5173`

---

## Prerequisites

1. **Java 17+** (JDK 17 installed and configured on `PATH`)
2. **Node.js 20+** and **npm**
3. **MySQL 8.0+** running locally on port 3306 with database `smartcart_db`
4. **Environment Variables**:
   Configure your Gemini API key and MySQL password:
   ```bash
   # Windows PowerShell
   $env:GEMINI_API_KEY="your-api-key-here"
   $env:DB_PASSWORD="your-mysql-password"

   # Linux/macOS
   export GEMINI_API_KEY="your-api-key-here"
   export DB_PASSWORD="your-mysql-password"
   ```

---

## 1. Running the Spring Boot Backend

From the project root (`smartcart-ai-agent`):

### Option A: Using Maven Wrapper (Terminal)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

### Option B: Build and Run JAR
```bash
.\mvnw.cmd clean package -DskipTests
java -jar target/smartcart-ai-agent-0.0.1-SNAPSHOT.jar
```

The backend server will start on **`http://localhost:8081`**.

### Verify Backend Endpoints
- **Product Catalog:** `GET http://localhost:8081/api/products`
- **Add Product:** `POST http://localhost:8081/api/products`
- **AI Chat Agent:** `POST http://localhost:8081/api/ai/chat` with body:
  ```json
  {
    "message": "Do you have wireless earbuds? Tell me their exact price from the database."
  }
  ```

---

## 2. Running the React Frontend

Open a new terminal window and navigate to the `frontend` folder:

```bash
cd frontend
```

### Install Dependencies
```bash
npm install --legacy-peer-deps
```

### Start Development Server
```bash
npm run dev
```

Open your browser at **`http://localhost:5173`**.

### Build for Production
```bash
npm run build
```

---

## Key Features

1. **Live Catalog Sync:** Products are loaded directly from MySQL (`/api/products`) with real-time filtering, search, and sorting.
2. **Interactive AI Shopping Assistant:** Slide-over assistant drawer with Google Gemini tool execution (`searchProducts` & `getProductsByMaxPrice`).
3. **Add Product Modal:** Add new items directly to MySQL and immediately query the AI about them to see real-time database discovery.
4. **Shopping Cart Drawer:** Add items to cart, adjust quantities, calculate totals, and simulate checkout.
5. **Zero Hallucination Guardrails:** System prompts restrict the model from inventing inventory or fabricating prices.
