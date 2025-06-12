# 🧠 IntelliJ IDEA Optimizations for Aggress Dev Container

This document details the IntelliJ IDEA-specific optimizations included in the Dev Container setup.

## 🎯 **Pre-Configured Features**

### ⚡ **Run Configurations**

Ready-to-use run configurations located in `.idea/runConfigurations/`:

| Configuration       | Purpose                      | Environment Variables                 |
|---------------------|------------------------------|---------------------------------------|
| **Crawler**         | Main crawler application     | Redis, Elasticsearch, Kafka endpoints |
| **Frontend**        | Web frontend (port 8080)     | Service connections pre-configured    |
| **WebAdmin**        | Admin interface (port 8081)  | Development settings enabled          |
| **Crawler (Debug)** | Crawler with debug port 5005 | All above + debug configuration       |

### 🐛 **Remote Debugging**

Pre-configured remote debugging for Kubernetes:

1. **Remote Debug: Kubernetes Crawler** - Connects to crawler pod
2. **Kubectl Port Forward: Crawler Debug** - Automatically forwards debug port

**Usage:**

1. Deploy to Kubernetes with debug enabled
2. Run "Kubectl Port Forward: Crawler Debug"
3. Run "Remote Debug: Kubernetes Crawler"
4. Set breakpoints and debug live pods!

### 🗄️ **Database Connections**

Pre-configured data sources in `.idea/dataSources.xml`:

- **Redis (Development)** - localhost:6379 with dev password
- **Elasticsearch (Development)** - localhost:9200 with query console

**Access:** View → Tool Windows → Database

### ☸️ **Kubernetes Integration**

Pre-configured Kubernetes settings in `.idea/kubernetes.xml`:

- **Default Context:** `kind-aggress-dev`
- **Default Namespace:** `aggress-dev`
- **Auto-refresh:** Every 30 seconds
- **Pre-configured namespaces:** dev, staging, prod

**Access:** View → Tool Windows → Kubernetes

### 🐳 **Docker Compose Integration**

Pre-configured Docker Compose profiles in `.idea/docker-compose.xml`:

- **Development Profile** - Uses `.devcontainer/docker-compose.yml`
- **Production Profile** - Uses `docker/docker-compose.yml`

**Access:** View → Tool Windows → Services → Docker

### 🎨 **Code Style**

Optimized code style in `.idea/codeStyles/Project.xml`:

- **120 character line limit** with soft limit at 80
- **Organized imports** with project-specific grouping
- **Aligned parameters** and method chains
- **Consistent spacing** and indentation

### 🚀 **Live Templates**

Custom live templates in `.idea/liveTemplates/Aggress.xml`:

| Trigger             | Expands To                       | Usage      |
|---------------------|----------------------------------|------------|
| `lombokdata`        | @Data class with @Builder        | Type + Tab |
| `lombokvalue`       | @Value immutable class           | Type + Tab |
| `slf4j`             | @Slf4j annotation                | Type + Tab |
| `service`           | Spring @Service class            | Type + Tab |
| `controller`        | Spring @RestController           | Type + Tab |
| `configprops`       | @ConfigurationProperties         | Type + Tab |
| `completablefuture` | Async CompletableFuture          | Type + Tab |
| `crawlertest`       | Test method with Given/When/Then | Type + Tab |

### ⚙️ **Gradle Integration**

Optimized Gradle settings in `.idea/gradle.xml`:

- **Delegated build** enabled for faster builds
- **Gradle test runner** for better test integration
- **Java 24** configured as project JVM
- **External annotations** resolution enabled
- **All modules** pre-configured

## 🛠️ **How to Use**

### **Opening the Project**

1. Open IntelliJ IDEA
2. File → Open → Select project directory
3. IntelliJ detects Dev Container configuration
4. Click "Open in Container" notification

### **Running Applications**

1. Go to Run → Edit Configurations
2. Select pre-configured run configuration
3. Click Run or Debug
4. Application starts with correct environment variables

### **Debugging Kubernetes Pods**

1. Deploy application: `kubectl apply -f k8s/overlays/dev/`
2. Run configuration: "Kubectl Port Forward: Crawler Debug"
3. Run configuration: "Remote Debug: Kubernetes Crawler"
4. Set breakpoints and debug live code!

### **Database Access**

1. View → Tool Windows → Database
2. Expand "Redis (Development)" or "Elasticsearch (Development)"
3. Run queries or browse data directly in IntelliJ

### **Docker Compose Management**

1. View → Tool Windows → Services
2. Expand Docker → Compose
3. Start/stop services with visual interface
4. View logs and container details

## 🎓 **Pro Tips**

### **Hot Reload Development**

```bash
# Terminal in IntelliJ
./gradlew build --continuous

# In another terminal
skaffold dev
```

### **Live Templates Usage**

```java
// Type 'service' + Tab
@Service
@RequiredArgsConstructor
@Slf4j
public class MyService {
    
}

// Type 'lombokvalue' + Tab  
@Value
@Builder
public class MyEntity {
    
}
```

### **Remote Debugging**

1. Set breakpoint in IntelliJ
2. Trigger the code path in Kubernetes
3. IntelliJ automatically stops at breakpoint
4. Inspect variables, step through code

### **Database Queries**

```sql
-- In Redis console
GET user:123
KEYS crawler:*

-- In Elasticsearch console  
GET /products/_search
{
  "query": { "match_all": {} }
}
```

## 🔧 **Customization**

### **Adding New Run Configurations**

1. Run → Edit Configurations
2. Add New → Application
3. Configure main class and environment variables
4. Save to `.idea/runConfigurations/`

### **Adding Database Connections**

1. Database tool window → New → Data Source
2. Configure connection details
3. Test connection
4. Save configuration

### **Custom Live Templates**

1. File → Settings → Editor → Live Templates
2. Select "Aggress" group
3. Add new template
4. Configure context and variables

This IntelliJ integration transforms the development experience, providing IDE-native access to all project services,
debugging capabilities, and productivity features while maintaining the benefits of the containerized environment.