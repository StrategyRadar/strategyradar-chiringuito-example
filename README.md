# El Chiringuito - StrategyRadar Example Project

A real-world example of **Task-Driven and Spec-Driven Development** with AI, demonstrating how to escape the [Vibe Coding Trap](https://www.strategyradar.ai/blog/vibe-coding-trap) and build production-ready applications with structured AI assistance.

> This project is featured in the [Workflow Essentials Pack](https://www.strategyradar.ai/packs/workflow-essentials) from [StrategyRadar.ai](https://www.strategyradar.ai)

## What is El Chiringuito?

El Chiringuito is a complete ordering system for a beach bar (chiringuito), built using Spring Boot and React. The system demonstrates how to coordinate multiple AI agents working from detailed specifications to build a cohesive application.

### Key Features

**Customer Interface (QR Code Access)**
- Browse menu and add items to cart
- Enter phone number for SMS notifications
- Complete payment
- Receive order number and pickup notification via SMS

**Kitchen Interface**
- Real-time order dashboard (auto-refresh every 5 seconds)
- Mark orders as ready with one tap
- Automatic SMS notification to customers

**Waiter Interface**
- View all ready-to-pickup orders
- Visual time indicators (orders turn red as they wait)
- Mark orders as picked up to remove from queue

## Why This Example Matters

This project demonstrates the alternative to **Vibe Coding** - a structured approach where:

1. **Clear specifications** define what needs to be built
2. **Task breakdowns** organize the work into manageable pieces
3. **AI agents** work within defined boundaries and contracts
4. **Human oversight** ensures quality and coherence

Instead of asking AI to "just build it" and hoping for the best, this project shows how to:
- Create visual specifications (UML diagrams, sequence diagrams, entity models)
- Break down features into discrete tasks
- Coordinate multiple specialized AI agents
- Maintain code quality and consistency throughout

## Tech Stack

- **Backend**: Spring Boot (Java)
- **Frontend**: React + TypeScript

## Project Structure

```
strategyradar-chiringuito-example/
├── backend/           # Spring Boot application
├── frontend/          # React + TypeScript application
├── docs/
│   ├── system-specs/  # Architecture, entity models, sequence diagrams
│   └── tasks/         # Task breakdowns and specifications
└── .claude/
    └── agents/        # AI agent definitions
```

## Documentation

All specifications and architectural decisions are documented in the `/docs` folder:

- **Architecture**: System design and component interactions
- **Entity Model**: Database schema and relationships
- **Flow Specifications**: Detailed user flows for each interface
- **Sequence Diagrams**: Step-by-step interaction flows
- **OpenAPI Spec**: API contract definitions

## Learn More About This Approach

This project implements the methodology described in:

- 📦 [Workflow Essentials Pack](https://www.strategyradar.ai/packs/workflow-essentials) - Complete methodology and templates
- 📕 [Task-Driven and Spec-Driven Development Guide](https://www.strategyradar.ai/help/task-specs-driven-development) - Comprehensive methodology
- 📝 [The Vibe Coding Trap](https://www.strategyradar.ai/blog/vibe-coding-trap) - Why unstructured AI coding fails
- 🎯 [Understanding Context for AI](https://www.strategyradar.ai/help/understanding-context) - How to provide effective context

## About StrategyRadar.ai

[StrategyRadar.ai](https://www.strategyradar.ai) helps teams plan and implement AI integration across the entire software development lifecycle. Visit the site to:

- Explore AI capabilities across different SDLC stages
- Download the FREE AI Context Starter Pack
- Access implementation guides and best practices
- Learn structured approaches to AI-assisted development

## License

[Creative Commons Attribution 4.0 International (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/)
