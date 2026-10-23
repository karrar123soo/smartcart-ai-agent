# SmartCart AI — React Frontend

Modern React application built with **Vite** and **JavaScript** for the SmartCart AI Shopping Agent.

## Quick Start

### 1. Install Dependencies
```bash
npm install --legacy-peer-deps
```

### 2. Start Vite Dev Server
```bash
npm run dev
```
The application will launch on **`http://localhost:5173`**.

### 3. Build for Production
```bash
npm run build
```

## Backend Configuration
The frontend communicates directly with the Spring Boot backend at **`http://localhost:8081`**:
- `GET /api/products` (Catalog listing)
- `POST /api/products` (Product creation)
- `POST /api/ai/chat` (Google Gemini AI Assistant with database tool calling)
