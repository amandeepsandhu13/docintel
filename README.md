    
# DocIntel: AI-Powered Document Analyzer

## Overview
DocIntel is an AI-driven platform designed to extract, analyze, and interact with data from uploaded documents such as invoices, contracts, and unstructured files. It leverages Azure Form Recognizer and OpenAI services to provide detailed content extraction, key-value pair detection, table parsing, and interactive Q&A over document chunks.

## Features
- Upload documents and specify model type (Invoice / Document)
- Extract structured data: content, tables, key-value pairs
- Parse unstructured content for deeper insights
- Interactive Q&A interface to query document sections
- Backend built with Spring Boot; frontend with Angular
- Integration with Azure Form Recognizer and OpenAI API for AI-powered insights

## Tech Stack
- Frontend: Angular (standalone components, TypeScript, RxJS)
- Backend: Spring Boot (Java)
- AI Services: Azure Form Recognizer, OpenAI GPT
- Data Storage: MySQL (or configurable)
- Communication: REST APIs

## Setup Instructions

### Backend
1. Clone the repository.
2. Configure Azure Form Recognizer and OpenAI API keys in application.properties.
3. Run Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
