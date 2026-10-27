# SmartCart AI — Autonomous AI Shopping Agent

[![Live Demo](https://img.shields.io/badge/Live_Demo-Vercel-black?style=for-the-badge&logo=vercel)](https://frontend-six-bay-21.vercel.app)
[![Backend API](https://img.shields.io/badge/Backend_API-Render-46E3B7?style=for-the-badge&logo=render)](https://smartcart-ai-agent.onrender.com/api/products)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring_AI-2.0.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-ai)
[![Google Gemini](https://img.shields.io/badge/Google_Gemini-3.5_Flash-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)

A full-stack e-commerce web application powered by **Java 17**, **Spring Boot 4.1.1**, **Spring AI 2.0.1**, **Google Gemini 3.5 Flash**, and a modern **React 19 + Vite** frontend.

The AI assistant connects directly to a live **MySQL / TiDB Cloud** relational database via Spring AI `@Tool` calling to answer product inquiries, check exact verified prices, and offer budget-friendly recommendations without hallucinations.

---

## 🌐 Live Demo & Endpoints

| Service | Platform | URL |
| :--- | :--- | :--- |
| **Frontend Web App** | **Vercel** | [https://frontend-six-bay-21.vercel.app](https://frontend-six-bay-21.vercel.app) |
| **Catalog API** | **Render** | [https://smartcart-ai-agent.onrender.com/api/products](https://smartcart-ai-agent.onrender.com/api/products) |
| **AI Chat Agent API** | **Render** | `POST https://smartcart-ai-agent.onrender.com/api/ai/chat` |
| **GitHub Repository** | **GitHub** | [https://github.com/karrar123soo/smartcart-ai-agent](https://github.com/karrar123soo/smartcart-ai-agent) |

---

## ✨ Key Features

1. **Autonomous AI Shopping Assistant**:
   * Slide-over conversational assistant powered by **Google Gemini 3.5 Flash**.
   * Integrates Spring AI function calling via `@Tool` (`searchProducts`, `getProductsByMaxPrice`).
   * **Zero Hallucination Guarantee**: Constrained strictly to verified database inventory and real-time prices.

2. **Live Store Catalog**:
   * Real-time product feed rendered directly from the relational database (`/api/products`).
   * Instant search by keyword matching across product name, category, and description.
   * Dynamic category filtering (Electronics, Footwear, Home & Kitchen, etc.) and price/name sorting.

3. **Dynamic Product Management**:
   * Interactive modal to insert new products directly into MySQL / TiDB Cloud.
   * New items are immediately queryable by the AI shopping assistant with zero restart required.

4. **Interactive Shopping Cart**:
   * Slide-out cart drawer with quantity adjustments, item removals, subtotal calculation, and checkout simulation.
   * Persisted locally in `localStorage` for a smooth user experience.

5. **Cloud-Native & Production Ready**:
   * **Backend**: Dockerized Spring Boot service hosted on **Render**.
   * **Frontend**: Optimized Single Page Application (SPA) hosted on **Vercel** with automatic rewrite routing.
   * **Database**: **TiDB Cloud Serverless** MySQL-compatible distributed SQL database with automated SSL/TLS encryption.
   * **Idempotent Seeder**: Automatically initializes default catalog items on first boot without duplicating records.

---

## 🛠️ Technology Stack

### Backend
* **Language & Runtime:** Java 17
* **Framework:** Spring Boot 4.1.1
* **AI Engine:** Spring AI 2.0.1 (`spring-ai-starter-model-google-genai`)
* **LLM Provider:** Google Gemini (`gemini-3.5-flash`)
* **Persistence & ORM:** Spring Data JPA, Hibernate ORM 7.x, HikariCP Connection Pool
* **Database Driver:** MySQL Connector/J 8.x / 9.x
* **Build Tool:** Apache Maven with Maven Wrapper (`mvnw`)

### Frontend
* **Library:** React 19
* **Tooling & Bundler:** Vite 8
* **Styling:** Vanilla CSS with custom modern design system (Glassmorphism, CSS variables, responsive grid)
* **Icons:** Lucide React

### Cloud Infrastructure
* **Frontend Hosting:** Vercel (Edge Network)
* **Backend Hosting:** Render (Web Service)
* **Database:** TiDB Cloud Serverless (MySQL Protocol, Port 4000, TLSv1.2/TLSv1.3)

---

## 🏗️ Architecture & AI Tool Calling

```
┌─────────────────────────────────┐
│     React 19 + Vite Frontend    │
│  (https://*.vercel.app)         │
└────────────────┬────────────────┘
                 │
                 │ HTTPS / REST (CORS enabled)
                 ▼
┌────────────────────────────────────────────────────────┐
│             Spring Boot Backend (Render)               │
│                                                        │
│  ┌───────────────────────┐   ┌──────────────────────┐  │
│  │   ProductController   │   │   AIChatController   │  │
│  └───────────┬───────────┘   └──────────┬───────────┘  │
│              │                          │              │
│              │               ┌──────────▼───────────┐  │
│              │               │    AIChatService     │  │
│              │               │  (Spring AI 2.0.1)   │  │
│              │               └──────────┬───────────┘  │
│              │                          │              │
│              │               Function   │ Gemini Tool  │
│              │               Calling    ▼ Execution    │
│              │               ┌──────────────────────┐  │
│              │               │     ProductTools     │  │
│              │               │   (@Tool callbacks)  │  │
│              │               └──────────┬───────────┘  │
│              ▼                          ▼              │
│        ┌──────────────────────────────────────┐        │
│        │            ProductService            │        │
│        └──────────────────┬───────────────────┘        │
│                           │                            │
│        ┌──────────────────▼───────────────────┐        │
│        │          ProductRepository           │        │
│        └──────────────────┬───────────────────┘        │
└───────────────────────────┼────────────────────────────┘
                            │
                            │ HikariCP / TLS Handshake
                            ▼
           ┌─────────────────────────────────┐
           │     MySQL 8.0 / TiDB Cloud      │
           │     `product` schema table      │
           └─────────────────────────────────┘
```

---

## 🚀 Local Development Setup

### 1. Prerequisites
* **Java 17+** (`java -version`)
* **Node.js 20+** and **npm** (`node -v`, `npm -v`)
* **MySQL 8.0+** running locally on port 3306 (or a TiDB Cloud connection)
* **Google Gemini API Key** ([Get a key from Google AI Studio](https://aistudio.google.com/))

### 2. Database Setup (Local MySQL)
Start your local MySQL service and create the database:
```sql
CREATE DATABASE smartcart_db;
```

### 3. Backend Configuration
Create an environment file or export required variables:
```bash
# Windows PowerShell
$env:GEMINI_API_KEY="your_gemini_api_key_here"
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/smartcart_db"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="your_mysql_password"

# Linux / macOS
export GEMINI_API_KEY="your_gemini_api_key_here"
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/smartcart_db"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="your_mysql_password"
```

Run the backend via Maven Wrapper:
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
The backend starts on **`http://localhost:8081`**.

Run automated tests:
```bash
.\mvnw.cmd test
```

### 4. Frontend Configuration
Navigate to the `frontend` folder:
```bash
cd frontend
```

Install dependencies:
```bash
npm install
```

Start the Vite development server:
```bash
npm run dev
```
Open **`http://localhost:5173`** in your browser.

---

## 📡 API Endpoints Reference

### 1. Catalog Endpoints
* **`GET /api/products`**: Fetch all available catalog items.
  ```json
  [
    {
      "id": 1,
      "name": "Wireless Earbuds",
      "category": "Electronics",
      "price": 1499.0,
      "description": "Bluetooth Wireless earbuds"
    }
  ]
  ```
* **`POST /api/products`**: Add a new item to the store catalog.
  ```json
  {
    "name": "Portable Bluetooth Speaker",
    "category": "Electronics",
    "price": 799.0,
    "description": "Waterproof portable speaker with 12h battery life"
  }
  ```

### 2. Autonomous AI Shopping Assistant
* **`POST /api/ai/chat`**: Send conversational shopping queries to the Gemini AI Agent.
  ```json
  {
    "message": "Do you have any products under 500 dollars?"
  }
  ```
  **Response:**
  > Here are the products currently available in the SmartCart store priced at or under 500:
  > 1. **Coffee Mug** ($299, Home & Kitchen)
  > 2. **Stainless Steel Water Bottle** ($499, Home & Kitchen)

---

## 📄 License

This project is licensed under the MIT License. Developed as an autonomous agentic e-commerce reference architecture.
