import React from 'react';
import { Sparkles, Bot, Database, ShieldCheck, ArrowRight, Tag } from 'lucide-react';

const SUGGESTED_HERO_PROMPTS = [
  { label: '🎧 Wireless earbuds price', query: 'Do you have wireless earbuds? Tell me their exact price from the database.' },
  { label: '⌨️ Mechanical Keyboard specs', query: 'Tell me about the mechanical keyboard and its features.' },
  { label: '💰 Products under $500', query: 'What products do you have with a budget under 500?' },
  { label: '👟 Lightweight running shoes', query: 'Show me athletic footwear or running shoes available.' },
];

export default function HeroSection({ onSelectPrompt }) {
  return (
    <section className="hero-section">
      <div className="hero-glow-backdrop"></div>
      <div className="hero-content">
        <div className="hero-pill-badge">
          <Sparkles size={14} className="hero-badge-icon" />
          <span>Spring AI + Google Gemini + MySQL</span>
        </div>

        <h1 className="hero-title">
          Shop Smarter with <span className="gradient-text">Autonomous AI</span>
        </h1>

        <p className="hero-subtitle">
          Experience conversational e-commerce with real-time tool calling. The AI queries
          live database inventory, checks verified prices, and suggests budget-friendly
          recommendations without hallucinations.
        </p>

        {/* Quick Clickable AI Prompts */}
        <div className="hero-prompts-wrapper">
          <span className="hero-prompts-label">
            <Tag size={14} /> Try asking the AI Agent:
          </span>
          <div className="hero-prompts-list">
            {SUGGESTED_HERO_PROMPTS.map((prompt, idx) => (
              <button
                key={idx}
                type="button"
                className="hero-prompt-chip"
                onClick={() => onSelectPrompt(prompt.query)}
              >
                <span>{prompt.label}</span>
                <ArrowRight size={12} className="chip-arrow" />
              </button>
            ))}
          </div>
        </div>

        {/* Tech Stack Metric Cards */}
        <div className="hero-metrics-grid">
          <div className="metric-card">
            <div className="metric-icon-wrapper ai-color">
              <Bot size={22} />
            </div>
            <div>
              <div className="metric-heading">Gemini 3.5 Flash</div>
              <div className="metric-sub">Spring AI @Tool Integration</div>
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-icon-wrapper db-color">
              <Database size={22} />
            </div>
            <div>
              <div className="metric-heading">MySQL Database</div>
              <div className="metric-sub">Live Real-time Catalog Queries</div>
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-icon-wrapper guard-color">
              <ShieldCheck size={22} />
            </div>
            <div>
              <div className="metric-heading">Zero Hallucinations</div>
              <div className="metric-sub">Catalog-Constrained Responses</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
