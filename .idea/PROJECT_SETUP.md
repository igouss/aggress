# 🚀 IntelliJ IDEA Project Setup

This project includes pre-configured IntelliJ IDEA settings that will be automatically imported when you open the project.

## ✅ **What Gets Imported Automatically**

When you open this project in IntelliJ IDEA, the following configurations are automatically available:

### 🏃 **Run Configurations**
- **Crawler** - Main application with all environment variables
- **Frontend** - Web interface (port 8080)
- **WebAdmin** - Admin interface (port 8081)  
- **Crawler (Debug)** - With remote debugging enabled
- **Remote Debug: Kubernetes Crawler** - For debugging K8s pods
- **Kubectl Port Forward** - Automatic port forwarding for debugging

### 🗄️ **Database Connections**
- **Redis (Development)** - localhost:6379 with dev credentials
- **Elasticsearch (Development)** - localhost:9200 with query console

### ☸️ **Kubernetes Integration**
- **Default context:** `kind-aggress-dev`
- **Namespaces:** aggress-dev, aggress-staging, aggress-prod
- **Auto-refresh** every 30 seconds

### 🐳 **Docker Compose**
- **Development profile** - `.devcontainer/docker-compose.yml`
- **Production profile** - `docker/docker-compose.yml`

### 🎨 **Code Style**
- **120 character line limit** with 80-char soft limit
- **Organized imports** with project-specific grouping
- **Consistent formatting** for Java code

### 🚀 **Live Templates**
Type these shortcuts + Tab:
- `lombokdata` → @Data class with @Builder
- `lombokvalue` → @Value immutable class
- `slf4j` → @Slf4j annotation
- `service` → Spring @Service class
- `controller` → Spring @RestController
- `configprops` → @ConfigurationProperties class
- `completablefuture` → Async CompletableFuture
- `crawlertest` → Test method with Given/When/Then

### ⚙️ **Gradle Settings**
- **Java 24** configured as project JVM
- **Delegated build** enabled for performance
- **Gradle test runner** for better test integration
- **All modules** pre-configured

## 🔧 **First-Time Setup**

### 1. **Open Project**
```bash
# Clone the repository
git clone <repository-url>
cd aggress

# Open in IntelliJ IDEA
idea . 
# or manually: File → Open → Select project directory
```

### 2. **Accept Dev Container (Recommended)**
- When prompted, click **"Open in Container"**
- Wait for container setup (5-10 minutes first time)
- All configurations will be automatically applied

### 3. **Verify Setup**
- Go to **Run → Edit Configurations** - Should see pre-configured run configs
- Open **Database** tool window - Should see Redis and Elasticsearch connections
- Check **File → Settings → Editor → Live Templates → Aggress** - Should see custom templates

## 🚀 **Quick Start Guide**

### **Running the Application**
1. Start Dev Container (if using)
2. Run configuration: **"Crawler"** to start the main application
3. Run configuration: **"Frontend"** for web interface
4. Run configuration: **"WebAdmin"** for admin interface

### **Debugging in Kubernetes**
1. Deploy to K8s: `kubectl apply -f k8s/overlays/dev/`
2. Run: **"Kubectl Port Forward: Crawler Debug"**
3. Run: **"Remote Debug: Kubernetes Crawler"**
4. Set breakpoints and debug live pods!

### **Database Access**
1. **View → Tool Windows → Database**
2. Expand **"Redis (Development)"** or **"Elasticsearch (Development)"**
3. Execute queries directly in IntelliJ

### **Using Live Templates**
```java
// Type 'service' + Tab
@Service
@RequiredArgsConstructor  
@Slf4j
public class MyService {
    
}
```

## 🔍 **Troubleshooting**

### **Configurations Not Loading**
1. Close IntelliJ IDEA
2. Delete `.idea/workspace.xml` (if exists)
3. Reopen project
4. Check that `.idea/` folder contains run configurations

### **Database Connections Failing**
1. Ensure Dev Container is running
2. Check that services are up: `docker ps`
3. Test connections in Database tool window
4. Verify ports are forwarded correctly

### **Gradle Not Working**
1. **File → Settings → Build → Gradle**
2. Verify **"Use Gradle from: wrapper task in build.gradle"**
3. Set **Gradle JVM** to **"Project SDK"** (Java 24)
4. Enable **"Delegate IDE build/run actions to Gradle"**

### **Live Templates Missing**
1. **File → Settings → Editor → Live Templates**
2. Check if **"Aggress"** group exists
3. If missing, restart IntelliJ IDEA
4. Verify `.idea/liveTemplates/Aggress.xml` exists

## 🎯 **Advanced Usage**

### **Custom Run Configurations**
- Copy existing configurations as templates
- Modify environment variables as needed
- Save new configurations to share with team

### **Database Query Development**
- Use built-in query consoles for Redis and Elasticsearch
- Save frequently used queries
- Export/import query results

### **Kubernetes Development**
- Use Kubernetes tool window for real-time monitoring
- Create custom kubectl commands as External Tools
- Monitor pod logs directly in IntelliJ

### **Docker Integration**
- Manage all services through Services tool window
- View container logs and metrics
- Build and deploy containers directly from IDE

This setup provides a complete, professional development environment that works immediately after cloning the project!