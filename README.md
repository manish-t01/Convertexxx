# Convertexxx

Convertexxx is a modern, privacy-first document conversion platform. This repository establishes the application foundation; document processing, uploads, and APIs are intentionally not part of this initialization.

## Tech Stack

- Frontend: Next.js 15, React, TypeScript, Tailwind CSS, shadcn/ui foundations, and Lucide React
- Backend: Java 25, Spring Boot, and Maven
- Database: PostgreSQL configuration prepared for a future profile
- Storage: local temporary storage configuration prepared for future workflows

## Folder Structure

```text
Convertexxx/
├── frontend/       Next.js application
├── backend/        Spring Boot application
├── docs/           Project documentation
├── assets/         Shared design and media assets
├── docker/         Container resources
└── .github/        GitHub configuration
```

## Run the Frontend

Prerequisites: Node.js 20.9 or later and npm.

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000).

## Run the Backend

Prerequisites: Java 25 and Maven 3.9 or later.

```bash
cd backend
mvn spring-boot:run
```

The backend starts on [http://localhost:8080](http://localhost:8080). PostgreSQL remains opt-in until data access is introduced.

## Future Roadmap

- Image to PDF conversion
- PDF to image conversion
- PDF merge and split workflows
- PDF compression
- Privacy-focused file lifecycle controls
