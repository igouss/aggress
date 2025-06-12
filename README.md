# Aggress Web Crawler

A multi-module Java/Kotlin web crawler system for scraping Canadian firearms and outdoor equipment retailers.

## 🚀 Quick Start with Dev Containers (Recommended)

The easiest way to get started is using Dev Containers with Kubernetes support, providing a complete cloud-native
development environment.

### Prerequisites

#### Windows Users

1. **Check if WSL is installed:**
   ```powershell
   wsl --version
   ```

2. **If WSL is not installed or you need WSL 2:**
   ```powershell
   # Run in PowerShell as Administrator
   wsl --install
   # Restart computer when prompted
   ```

3. **Install Docker Desktop:**
   - Download from [docker.com/products/docker-desktop](https://docker.com/products/docker-desktop)
   - Enable WSL 2 integration during installation
   - Configure to use WSL 2 backend
   - Enable Kubernetes in Docker Desktop settings

4. **Install IDE with Dev Containers support:**

   **Option A: VS Code**
   - Download [VS Code](https://code.visualstudio.com/)
   - Install "Dev Containers" extension (ms-vscode-remote.remote-containers)

   **Option B: IntelliJ IDEA**
   - Use IntelliJ IDEA 2021.3+ (Ultimate or Community)
   - Dev Containers support is built-in (no additional plugins needed)

#### macOS/Linux Users

1. **Install Docker Desktop** or Docker Engine with Kubernetes
2. **Install your preferred IDE:**
   - **VS Code** with Dev Containers extension, OR
   - **IntelliJ IDEA** 2021.3+ with built-in Dev Container support

### Using Dev Containers

#### **With VS Code:**

1. **Open project:**
   ```bash
   code .
   ```

2. **Start Dev Container:**
   - Press `Ctrl+Shift+P` (Windows/Linux) or `Cmd+Shift+P` (macOS)
   - Type "Dev Containers: Reopen in Container"
   - Wait for container to build (first time takes 5-10 minutes)

#### **With IntelliJ IDEA:**

1. **Clone and open project:**
   ```bash
   git clone <repository-url>
   cd aggress
   idea .  # or File → Open in IntelliJ
   ```

2. **Automatic configuration import:**
   - ✅ **Run configurations** automatically available
   - ✅ **Database connections** pre-configured
   - ✅ **Live templates** ready to use
   - ✅ **Code style** applied automatically
   - ✅ **Kubernetes/Docker integration** configured

3. **Start Dev Container:**
   - Click "Open in Container" notification, OR
   - File → Remote Development → Dev Containers → Open in Container
   - Wait for container build (5-10 minutes first time)

4. **Verify setup:**
   - Run `./setup-intellij.sh` to verify all configurations
   - Check Run → Edit Configurations for pre-built run configs
   - Open Database tool window for Redis/Elasticsearch connections

#### **What You Get (Both IDEs):**

- **Java 24** + Gradle + Lombok
- **Kubernetes cluster** (Kind) with full stack
- **Core services**: Redis, Elasticsearch, Kafka
- **Tor proxy** (for crawler anonymity - optional, can be disabled)
- **Development tools**: kubectl, helm, skaffold, k9s
- **Hot reload capabilities** with Skaffold

#### **IDE-Specific Features:**

**VS Code:**

- Pre-configured extensions for Java, Kubernetes, YAML
- Integrated terminal with custom aliases
- Built-in Kubernetes dashboard
- Port forwarding UI

**IntelliJ IDEA:**

- **Pre-configured run configurations** for all modules (Crawler, Frontend, WebAdmin)
- **Remote debugging** for Kubernetes pods with one-click setup
- **Database connections** pre-configured for Redis and Elasticsearch
- **Docker Compose integration** with development and production profiles
- **Kubernetes dashboard** with cluster and namespace management
- **Live templates** for Lombok, Spring Boot, and testing patterns
- **Code style** optimized for the project conventions
- **Gradle integration** with Java 24 and multi-module support

### 🎯 Development Commands Reference

Once in the Dev Container (works with both IDEs), these commands are available in the terminal:

#### 🏗️ **Build Commands**

```bash
# Build all modules
agbuild

# Run tests (with crawler test flag)
agtest

# Build Docker images
./gradlew jib

# Build specific modules
./gradlew :crawler:build
./gradlew :frontend:build
./gradlew :webadmin:build
```

#### 🚀 **Run Commands**

```bash
# Run applications directly
agrun-crawler    # Main crawler application
agrun-frontend   # Frontend service (port 8080)
agrun-webadmin   # WebAdmin interface (port 8081)

# Run with custom arguments
agrun-crawler -clean -populate -crawl -parse
```

#### ☸️ **Kubernetes Commands**

```bash
# Development workflow
agdev            # Start Skaffold development mode (hot reload)
agdebug          # Start Skaffold debug mode
agdeploy         # Deploy to Kubernetes cluster

# Cluster management
k get pods       # Show running pods (alias for kubectl)
k get services   # Show services
kns aggress-dev  # Switch to development namespace
kctx kind-aggress-dev  # Switch to development context
kdev get pods    # Direct access to dev cluster

# Monitoring and debugging
k9s              # Kubernetes dashboard (TUI)
logs frontend    # Tail logs for frontend pod
logs crawler     # Tail logs for crawler pod
k port-forward svc/frontend 8080:8080  # Forward ports
```

#### 🐳 **Docker Commands**

```bash
# Container management
dps              # List running containers (docker ps)
dlogs <name>     # Follow logs for container
dexec <name>     # Execute interactive shell in container

# Service control
docker-compose -f .devcontainer/docker-compose.yml up -d
docker-compose -f .devcontainer/docker-compose.yml down
```

#### 🔍 **Monitoring & Status**

```bash
# Environment status
agstatus         # Show complete development environment status

# Service health checks
curl http://localhost:9200/_cluster/health  # Elasticsearch
redis-cli -h localhost -p 6379 -a devpassword ping  # Redis
curl http://localhost:8080/health           # Frontend health
```

#### 🛠️ **Setup & Configuration**

```bash
# IntelliJ IDEA setup (IntelliJ users only)
./setup-intellij.sh

# Import SSL certificates (required for HTTPS crawling)
./crawler/src/main/resources/startssl-java-master/import-certs.sh

# Gradle wrapper
./gradlew --version
./gradlew tasks --all
```

#### 🔧 **Quick Start Workflow**

```bash
# 1. Verify setup
agstatus

# 2. Build project
agbuild

# 3. Start development mode
agdev

# 4. In another terminal, monitor with K9s
k9s

# 5. Make code changes and see live updates!
```

### 🔒 **About the Tor Proxy**

The Dev Container includes a Tor proxy service because:

- **It's part of the original project** for web crawler anonymity
- **Helps avoid IP blocking** when scraping multiple retailer websites
- **Provides request distribution** across different exit nodes

**If you don't need Tor proxy:**

1. Edit `.devcontainer/docker-compose.yml`
2. Comment out or remove the `tor-proxy` service
3. Remove Tor proxy from the `depends_on` section in `dev-environment`
4. Rebuild the Dev Container

## 🔧 Manual Setup (Alternative)

### 1. Configuration

Create `crawler/src/main/resources/config.properties`:

```properties
canadiangunnutzLogin=
canadiangunnutzPassword=
redisHost=localhost
redisPort=6379
```

### 2. SSL Certificates

Import StartSSL certificates:

```bash
# Linux/macOS
./crawler/src/main/resources/startssl-java-master/import-certs.sh

# Windows
crawler\src\main\resources\startssl-java-master\import-certs.bat
```

### 3. Dependencies

Start required services:

```bash
# Using Docker Compose
cd docker && docker-compose up -d

# Or using Vagrant (legacy)
vagrant up
```

## 🏗️ Building the Project

```bash
# Build all modules
./gradlew clean build

# Build specific module
./gradlew :crawler:build

# Build Docker images
./gradlew buildDockerImages
```

## 🚀 Running the Application

### 🎯 Kubernetes Development (Recommended)

**With Dev Containers:**

```bash
# Start hot reload development
agdev

# Deploy to local Kubernetes
agdeploy

# View Kubernetes dashboard
k9s

# Check pod status
kubectl get pods -n aggress-dev

# View logs
logs frontend
logs crawler
```

**With Helm:**

```bash
# Install Helm chart for development
helm install aggress charts/aggress -f charts/aggress/values-dev.yaml

# Upgrade deployment
helm upgrade aggress charts/aggress -f charts/aggress/values-dev.yaml

# Uninstall
helm uninstall aggress
```

### 🐳 Docker Development

```bash
# Run full stack with Docker Compose
cd docker && docker-compose up

# Access services:
# - Frontend: http://localhost:8080
# - WebAdmin: http://localhost:8081
# - Elasticsearch: http://localhost:9200
# - Redis: localhost:6379
```

### 🖥️ Traditional Java Execution

**Crawler Operations:**

```bash
# Full crawl cycle
java -jar crawler/build/libs/crawler-all-1.0-SNAPSHOT.jar -clean -populate -crawl -parse

# Individual operations
java -jar crawler-all-1.0-SNAPSHOT.jar -createESIndex
java -jar crawler-all-1.0-SNAPSHOT.jar -clean
java -jar crawler-all-1.0-SNAPSHOT.jar -populate
java -jar crawler-all-1.0-SNAPSHOT.jar -crawl
java -jar crawler-all-1.0-SNAPSHOT.jar -parse
```

**Web Services:**

```bash
# Frontend (port 8080)
java -jar frontend/build/libs/frontend-1.0-SNAPSHOT.jar

# WebAdmin (port 8081) 
java -jar webadmin/build/libs/webadmin-1.0-SNAPSHOT.jar
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run crawler tests (requires flag)
./gradlew -Dcrawler.test=true test

# Run integration tests
./gradlew :crawler:integrationTest
```

## 📁 Project Structure

- **`crawler/`** - Main crawling engine with site-specific parsers
- **`frontend/`** - Public search interface (Vert.x + Thymeleaf, port 8080)
- **`webadmin/`** - Admin interface (Kotlin, port 8081)
- **`crawler-admin/`** - Rich admin panel with AdminLTE UI
- **`storage/`** - Elasticsearch and Redis integration
- **`common/`** - Shared utilities and models

## 🛠️ Technology Stack

- **Java 8** (primary), **Kotlin** (webadmin)
- **Vert.x** (async web framework)
- **Elasticsearch** (search engine)
- **Redis** (caching/storage)
- **Dagger 2** (dependency injection)
- **RxJava 1.x** (reactive programming)
- **JSoup** (HTML parsing)

## 🔄 Migration to Spring Boot + Lombok

This project is being modernized with Spring Boot + Lombok. See `plan.md` for the comprehensive migration plan.

## ☸️ Kubernetes Deployment

### Local Development

- **Kind cluster** automatically created in Dev Container
- **Skaffold** for hot reload development
- **Helm charts** for parameterized deployments

### Production Deployment

```bash
# Create namespace
kubectl create namespace aggress-prod

# Deploy with Helm
helm install aggress charts/aggress \
  --namespace aggress-prod \
  --values charts/aggress/values-prod.yaml

# Check deployment
kubectl get pods -n aggress-prod
```

### Multi-Environment Support

- **Development**: `values-dev.yaml`
- **Staging**: `values-staging.yaml`
- **Production**: `values-prod.yaml`

### DevOps Features

- **GitOps ready** with ArgoCD integration
- **Monitoring** with Prometheus/Grafana
- **Logging** with ELK stack
- **Security** with RBAC and network policies

## 📊 Build Status

<a href='https://travis-ci.org/igouss/aggress/builds'><img src='https://travis-ci.org/igouss/aggress.svg?branch=master'></a>


