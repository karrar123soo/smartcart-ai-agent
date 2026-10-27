import React, { useState, useEffect, useRef } from 'react';
import {
  Sparkles,
  Bot,
  User,
  Send,
  X,
  RotateCcw,
  AlertCircle,
  HelpCircle,
  ExternalLink,
  ShoppingBag,
} from 'lucide-react';
import { sendChatMessage } from '../api';

const DEFAULT_SUGGESTIONS = [
  'Do you have wireless earbuds? Tell me their exact price from the database.',
  'What products do you have with a budget under 500?',
  'Recommend electronics available in the store.',
  'Do you have any gaming keyboards in stock?',
];

// Helper to render basic Markdown formatting from Gemini's response
function renderFormattedMessage(text) {
  if (!text) return null;

  // Split into lines
  const lines = text.split('\n');

  return lines.map((line, idx) => {
    const trimmed = line.trim();

    // Empty line -> spacing
    if (!trimmed) {
      return <div key={idx} className="msg-space" />;
    }

    // Bullet line
    if (trimmed.startsWith('* ') || trimmed.startsWith('- ')) {
      const content = trimmed.substring(2);
      return (
        <li key={idx} className="msg-bullet">
          {formatInlineText(content)}
        </li>
      );
    }

    // Header line (### or ##)
    if (trimmed.startsWith('### ')) {
      return (
        <h4 key={idx} className="msg-heading">
          {formatInlineText(trimmed.substring(4))}
        </h4>
      );
    }
    if (trimmed.startsWith('## ')) {
      return (
        <h3 key={idx} className="msg-heading">
          {formatInlineText(trimmed.substring(3))}
        </h3>
      );
    }

    // Standard paragraph
    return (
      <p key={idx} className="msg-paragraph">
        {formatInlineText(line)}
      </p>
    );
  });
}

// Format **bold** and `code` inline
function formatInlineText(text) {
  const parts = [];
  const regex = /(\*\*[^*]+\*\*|`[^`]+`)/g;
  let lastIndex = 0;
  let match;

  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      parts.push(text.substring(lastIndex, match.index));
    }

    const token = match[0];
    if (token.startsWith('**') && token.endsWith('**')) {
      parts.push(
        <strong key={match.index} className="msg-bold">
          {token.slice(2, -2)}
        </strong>
      );
    } else if (token.startsWith('`') && token.endsWith('`')) {
      parts.push(
        <code key={match.index} className="msg-code">
          {token.slice(1, -1)}
        </code>
      );
    }

    lastIndex = regex.lastIndex;
  }

  if (lastIndex < text.length) {
    parts.push(text.substring(lastIndex));
  }

  return parts;
}

export default function AIChatPanel({ isOpen, onClose, externalPrompt }) {
  const [messages, setMessages] = useState([
    {
      id: 'welcome',
      sender: 'ai',
      text: "👋 Hi! I'm your SmartCart AI Assistant. I use **Google Gemini** with Spring AI tool calling to query our live **MySQL catalog**. Ask me about prices, product specs, or budget recommendations!",
      timestamp: new Date(),
    },
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  // Auto-scroll to bottom on new message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  // Focus input when opened
  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 250);
    }
  }, [isOpen]);

  // Handle external prompt passed from Hero or Product Card
  useEffect(() => {
    if (externalPrompt && externalPrompt.trim()) {
      handleSendMessage(externalPrompt);
    }
  }, [externalPrompt]);

  // Handle Escape key to close drawer
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  const handleSendMessage = async (customMessage) => {
    const textToSend = customMessage || inputValue;
    if (!textToSend || !textToSend.trim() || isLoading) return;

    setErrorMsg(null);
    const userMsg = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: textToSend.trim(),
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMsg]);
    if (!customMessage) {
      setInputValue('');
    }
    setIsLoading(true);

    try {
      const response = await sendChatMessage(textToSend.trim());
      const aiMsg = {
        id: `ai-${Date.now()}`,
        sender: 'ai',
        text: response,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, aiMsg]);
    } catch (err) {
      console.error('Chat error:', err);
      setErrorMsg(
        err.message || 'Unable to communicate with the SmartCart AI backend. Please verify your internet connection or try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleClearHistory = () => {
    setMessages([
      {
        id: 'welcome',
        sender: 'ai',
        text: "👋 Chat cleared. What else can I help you find in our catalog today?",
        timestamp: new Date(),
      },
    ]);
    setErrorMsg(null);
  };

  if (!isOpen) return null;

  return (
    <div className="chat-drawer-overlay animate-fade-in" onClick={onClose} role="dialog" aria-modal="true">
      <div
        className="chat-drawer-container"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Chat Drawer Header */}
        <div className="chat-header">
          <div className="chat-header-identity">
            <div className="chat-bot-avatar">
              <Bot size={22} className="bot-icon" />
              <span className="bot-pulse-ring"></span>
            </div>
            <div className="chat-header-text">
              <div className="chat-title-row">
                <h3>SmartCart AI</h3>
                <span className="live-status-pill">
                  <span className="dot-green"></span> Live Agent
                </span>
              </div>
              <p className="chat-subtitle">Gemini 3.5 Flash • MySQL Tool Calling</p>
            </div>
          </div>

          <div className="chat-header-actions">
            <button
              type="button"
              className="chat-btn-action"
              onClick={handleClearHistory}
              title="Clear chat history"
              aria-label="Clear chat"
            >
              <RotateCcw size={16} />
            </button>
            <button
              type="button"
              className="chat-btn-action"
              onClick={onClose}
              title="Close assistant"
              aria-label="Close assistant"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Chat Messages Scrollable Body */}
        <div className="chat-messages-area">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`chat-bubble-row ${msg.sender === 'user' ? 'row-user' : 'row-ai'}`}
            >
              {msg.sender === 'ai' && (
                <div className="msg-avatar ai-avatar">
                  <Sparkles size={14} />
                </div>
              )}

              <div className={`chat-bubble ${msg.sender === 'user' ? 'bubble-user' : 'bubble-ai'}`}>
                <div className="bubble-content">{renderFormattedMessage(msg.text)}</div>
                <span className="bubble-time">
                  {msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>

              {msg.sender === 'user' && (
                <div className="msg-avatar user-avatar">
                  <User size={14} />
                </div>
              )}
            </div>
          ))}

          {/* Loading Indicator */}
          {isLoading && (
            <div className="chat-bubble-row row-ai">
              <div className="msg-avatar ai-avatar">
                <Sparkles size={14} />
              </div>
              <div className="chat-bubble bubble-ai loading-bubble">
                <div className="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
                <span className="loading-status-text">Querying store catalog tools...</span>
              </div>
            </div>
          )}

          {/* Error Banner */}
          {errorMsg && (
            <div className="chat-error-banner">
              <AlertCircle size={18} className="error-icon" />
              <div className="error-content">
                <strong>Error processing request</strong>
                <p>{errorMsg}</p>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Quick Suggestion Chips */}
        <div className="chat-suggestions-tray">
          <span className="tray-label">Quick Prompts:</span>
          <div className="tray-scroll">
            {DEFAULT_SUGGESTIONS.map((suggestion, idx) => (
              <button
                key={idx}
                type="button"
                className="suggestion-chip"
                onClick={() => handleSendMessage(suggestion)}
                disabled={isLoading}
              >
                {suggestion}
              </button>
            ))}
          </div>
        </div>

        {/* Chat Input Bar */}
        <form
          className="chat-input-bar"
          onSubmit={(e) => {
            e.preventDefault();
            handleSendMessage();
          }}
        >
          <input
            ref={inputRef}
            type="text"
            className="chat-text-input"
            placeholder="Ask about items, budget, categories..."
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            disabled={isLoading}
            aria-label="Message to SmartCart AI"
          />
          <button
            type="submit"
            className="chat-send-btn"
            disabled={!inputValue.trim() || isLoading}
            aria-label="Send message"
          >
            <Send size={16} />
          </button>
        </form>

        <div className="chat-footer-note">
          <span>Verified Catalog Data • Spring AI Tool Execution</span>
        </div>
      </div>
    </div>
  );
}
