import React from 'react';
import {
  Sparkles,
  Bot,
  Database,
  ShieldCheck,
  ArrowRight,
  Tag,
  Headphones,
  Keyboard,
  DollarSign,
  Activity,
} from 'lucide-react';

const SUGGESTED_HERO_PROMPTS = [
  {
    icon: Headphones,
    label: 'Wireless earbuds price',
    query: 'Do you have wireless earbuds? Tell me their exact price from the database.',
  },
  {
    icon: Keyboard,
    label: 'Mechanical Keyboard specs',
    query: 'Tell me about the mechanical keyboard and its features.',
  },
  {
    icon: DollarSign,
    label: 'Products under $500',
    query: 'What products do you have with a budget under 500?',
  },
  {
    icon: Activity,
    label: 'Lightweight running shoes',
    query: 'Show me athletic footwear or running shoes available.',
  },
];

export default function HeroSection({ onSelectPrompt }) {
  return (
    <section className="hero-section">
      <div className="hero-glow-backdrop"></div>
      <div className="hero-content">
        <div className="hero-pill-badge">
          <Sparkles size={13} className="hero-badge-icon" />
          <span>Spring AI + Google Gemini + MySQL</span>
        </div>

        <h1 className="hero-title">
          Shop Smarter with <span className="gradient-text">Autonomous AI</span>
        </h1>

        <p className="hero-subtitle">
          Conversational e-commerce with real-time tool calling. Browse live verified database inventory below or ask Gemini.
        </p>

        {/* Quick Clickable AI Prompts */}
        <div className="hero-prompts-wrapper">
          <span className="hero-prompts-label">
            <Tag size={12} /> Try asking the AI Agent:
          </span>
          <div className="hero-prompts-list">
            {SUGGESTED_HERO_PROMPTS.map((prompt, idx) => {
              const IconComponent = prompt.icon;
              return (
                <button
                  key={idx}
                  type="button"
                  className="hero-prompt-chip"
                  onClick={() => onSelectPrompt(prompt.query)}
                >
                  {IconComponent && <IconComponent size={12} className="chip-icon" />}
                  <span>{prompt.label}</span>
                  <ArrowRight size={11} className="chip-arrow" />
                </button>
              );
            })}
          </div>
        </div>

        {/* Tech Stack Metrics Strip (Compact) */}
        <div className="hero-metrics-strip">
          <div className="metric-chip">
            <Bot size={13} className="metric-chip-icon ai-color" />
            <span>Gemini 3.5 Flash</span>
          </div>
          <div className="metric-chip">
            <Database size={13} className="metric-chip-icon db-color" />
            <span>Live MySQL Sync</span>
          </div>
          <div className="metric-chip">
            <ShieldCheck size={13} className="metric-chip-icon guard-color" />
            <span>Zero Hallucinations</span>
          </div>
        </div>
      </div>
    </section>
  );
}
