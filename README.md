# Student Data Processing Application

A full-stack application that generates student Excel files, processes them into CSV format, uploads CSV data to PostgreSQL, and provides reporting/export capabilities.

## Prerequisites

Before running this application, ensure you have the following installed:

- **Docker** (v20.10+) or **Podman** (v4.0+)
- **Docker Compose** (v2.0+) or **Podman Compose**
- **Make** (usually pre-installed on Linux/macOS)

### Installing Docker

**macOS:**
```bash
brew install --cask docker
# Or download from https://docs.docker.com/desktop/install/mac-install/
```

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install docker.io docker-compose-plugin
sudo systemctl start docker
sudo usermod -aG docker $USER
```

**Windows:**
Download and install Docker Desktop from https://docs.docker.com/desktop/install/windows-install/

### Verifying Installation

```bash
docker --version
docker compose version
make --version
```

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd student-data-processing-app
```

### 2. Configure Compose Tool (Optional)

The Makefile uses `podman compose` by default. To use Docker instead, edit the `Makefile`:

```makefile
# Change this line:
COMPOSE = podman compose
# To:
COMPOSE = docker compose
```

### 3. Build and Run

```bash
# Build all container images
make build

# Start all services in detached mode
make up
```

### 4. Access the Application

- **Frontend Dashboard:** http://localhost:4200
- **Backend API:** http://localhost:8080

## Makefile Commands

| Command | Description |
|---------|-------------|
| `make build` | Build Docker images for backend and frontend |
| `make up` | Start all services (database, backend, frontend) |
| `make down` | Stop all running services |
| `make logs` | Tail logs from all services (Ctrl+C to exit) |
| `make ps` | List running containers and their status |
| `make clean` | Stop services, remove volumes, and delete output data |

### Common Workflows

**Start fresh:**
```bash
make build && make up
```

**View logs while running:**
```bash
make logs
```

**Stop everything:**
```bash
make down
```

**Complete reset (removes all data):**
```bash
make clean
make build
make up
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| `frontend` | 4200 | Angular 18 app served via Nginx |
| `backend` | 8080 | Spring Boot 3.4.5 REST API |
| `db` | 5432 | PostgreSQL 16 database |

### Database Credentials

- **Host:** localhost (or `db` from within containers)
- **Port:** 5432
- **Database:** studentdb
- **Username:** postgres
- **Password:** postgres

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/generate?count=1000` | Generate Excel file with student data |
| `POST` | `/api/process` | Convert Excel to CSV (multipart file upload) |
| `POST` | `/api/upload` | Upload CSV to database (multipart file upload) |
| `GET` | `/api/students` | Paginated student list (`?page=0&size=20&studentId=&class=`) |
| `GET` | `/api/export/excel` | Export students to Excel |
| `GET` | `/api/export/csv` | Export students to CSV |
| `GET` | `/api/export/pdf` | Export students to PDF |

## Application Workflow

1. **Generate Data** → Creates Excel file with random student records (scores: 55-75)
2. **Process Data** → Converts Excel to CSV, adds +10 to scores (scores: 65-85)
3. **Upload Data** → Imports CSV to PostgreSQL, adds +5 to scores (scores: 70-90)
4. **Reports** → View, search, filter, and export student data

### Score Transformation Summary

```
Excel (55-75) → CSV (+10) → Database (+5)
Example: 60 → 70 → 75
```

## Project Structure

```
student-data-processing-app/
├── backend/                    # Spring Boot application
│   ├── src/
│   ├── migrations/             # Flyway SQL migrations
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # Angular application
│   ├── src/
│   ├── Dockerfile
│   └── nginx.conf
├── data-output/                # Generated/processed files (created at runtime)
├── compose.yml                 # Docker Compose configuration
├── Makefile                    # Build and run commands
└── README.md
```

## Volumes

| Host Path | Container Path | Purpose |
|-----------|----------------|---------|
| `./backend/migrations` | `/app/migrations` | Flyway database migrations |
| `./data-output` | `/var/log/applications/API/dataprocessing` | Generated Excel/CSV files |

## Database Schema

Table: `students`

| Column | Type | Description |
|--------|------|-------------|
| `student_id` | BIGINT | Primary key |
| `first_name` | VARCHAR | Student's first name |
| `last_name` | VARCHAR | Student's last name |
| `dob` | DATE | Date of birth |
| `student_class` | VARCHAR | Class assignment (Class1-Class5) |
| `score` | INTEGER | Student score |

## Troubleshooting

### Port Already in Use

```bash
# Check what's using the port
lsof -i :4200
lsof -i :8080

# Kill the process or change ports in compose.yml
```

### Container Won't Start

```bash
# Check logs for errors
make logs

# Rebuild from scratch
make clean
make build
make up
```

### Database Connection Issues

```bash
# Verify database is running
docker exec -it student-data-processing-app-db-1 psql -U postgres -d studentdb -c "SELECT 1"
```

### Frontend Shows Nginx Default Page

```bash
# Rebuild frontend with no cache
docker compose build --no-cache frontend
make down
make up
```

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.4.5, Hibernate, Flyway
- **Frontend:** Angular 18, Angular Material, TypeScript
- **Database:** PostgreSQL 16
- **Containerization:** Docker/Podman, Nginx