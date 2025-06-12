# 🧠 IntelliJ IDEA Auto-Import Configuration

This document explains how the Aggress Web Crawler project automatically configures IntelliJ IDEA for new developers.

## 🎯 **How Auto-Import Works**

When you clone this repository and open it in IntelliJ IDEA, the following configurations are **automatically imported
**:

### ✅ **What Gets Imported Automatically**

| Configuration Type         | Files                             | What You Get                                                            |
|----------------------------|-----------------------------------|-------------------------------------------------------------------------|
| **Run Configurations**     | `.idea/runConfigurations/*.xml`   | Ready-to-run Crawler, Frontend, WebAdmin with all environment variables |
| **Database Connections**   | `.idea/dataSources.xml`           | Pre-configured Redis and Elasticsearch connections                      |
| **Kubernetes Integration** | `.idea/kubernetes.xml`            | Cluster contexts, namespaces, auto-refresh settings                     |
| **Docker Compose**         | `.idea/docker-compose.xml`        | Development and production profiles                                     |
| **Code Style**             | `.idea/codeStyles/Project.xml`    | Project-specific formatting rules                                       |
| **Live Templates**         | `.idea/liveTemplates/Aggress.xml` | Custom code snippets for Lombok, Spring Boot, testing                   |
| **Gradle Settings**        | `.idea/gradle.xml`                | Java 24, delegated builds, multi-module setup                           |
| **Project Setup Guide**    | `.idea/PROJECT_SETUP.md`          | Comprehensive usage documentation                                       |

### 🔄 **Git Tracking Strategy**

The project uses a **selective Git tracking** approach for `.idea/` files:

#### **✅ Tracked (Shared Configurations)**

```bash
.idea/runConfigurations/     # Run configurations for all developers
.idea/codeStyles/           # Consistent code formatting
.idea/liveTemplates/        # Custom code snippets
.idea/dataSources.xml       # Database connection templates
.idea/gradle.xml            # Gradle integration settings
.idea/kubernetes.xml        # Kubernetes configurations
.idea/docker-compose.xml    # Docker Compose profiles
.idea/PROJECT_SETUP.md      # Setup documentation
```

#### **❌ Ignored (User-Specific Files)**

```bash
.idea/workspace.xml         # Personal workspace layout
.idea/tasks.xml             # Personal task configurations
.idea/dictionaries/         # Personal dictionaries
.idea/shelf/                # Personal shelved changes
.idea/usage.statistics.xml  # Personal usage statistics
```

## 🚀 **Developer Experience**

### **For New Team Members**

1. **Clone repository**: `git clone <repository-url>`
2. **Open in IntelliJ**: `idea .` or File → Open
3. **Everything works immediately**:
    - Run configurations ready
    - Database connections configured
    - Live templates available
    - Code style applied

### **For Existing Team Members**

- **Configurations stay in sync** with Git
- **Updates are automatic** when pulling changes
- **Personal settings preserved** (workspace, tasks, etc.)

## 🛠️ **Technical Implementation**

### **Modified .gitignore**

```gitignore
# Track shared IntelliJ configurations but ignore user-specific files
.idea/workspace.xml
.idea/tasks.xml
.idea/dictionaries/
.idea/shelf/
.idea/usage.statistics.xml

# Keep shared configurations (these are tracked):
# .idea/runConfigurations/
# .idea/codeStyles/
# .idea/liveTemplates/
# .idea/dataSources.xml
# .idea/gradle.xml
# .idea/kubernetes.xml
# .idea/docker-compose.xml
```

### **Verification Script**

The `./setup-intellij.sh` script verifies all configurations:

```bash
./setup-intellij.sh
# Checks all configuration files
# Verifies Git tracking
# Provides setup instructions
# Can optionally open IntelliJ
```

## 🎓 **Best Practices**

### **For Project Maintainers**

1. **Adding New Configurations**:
   ```bash
   # Create configuration in IntelliJ
   # Verify it's in .idea/
   git add .idea/newConfiguration.xml
   git commit -m "Add IntelliJ configuration for X"
   ```

2. **Updating Existing Configurations**:
   ```bash
   # Modify in IntelliJ
   # Changes automatically tracked by Git
   git commit -am "Update IntelliJ run configuration"
   ```

3. **Testing Auto-Import**:
   ```bash
   # Clone to new directory
   git clone <repo> test-intellij
   cd test-intellij
   ./setup-intellij.sh
   # Verify all configurations work
   ```

### **For Team Members**

1. **Getting Updates**:
   ```bash
   git pull
   # IntelliJ automatically detects .idea/ changes
   # Restart IntelliJ if configurations don't appear
   ```

2. **Personal Customizations**:
    - Keep personal settings in non-tracked files
    - Use `.idea/workspace.xml` for personal layouts
    - Create personal run configurations with different names

3. **Troubleshooting**:
   ```bash
   ./setup-intellij.sh  # Verify setup
   # If issues persist:
   # 1. Close IntelliJ
   # 2. Delete .idea/workspace.xml
   # 3. Reopen project
   ```

## 🎯 **Benefits**

### **Immediate Productivity**

- **Zero configuration time** for new developers
- **Consistent development environment** across team
- **Professional IDE setup** out of the box

### **Team Synchronization**

- **Shared run configurations** prevent "works on my machine"
- **Consistent code style** across all developers
- **Shared debugging setups** for complex scenarios

### **Maintenance**

- **One-time setup** by project maintainers
- **Automatic distribution** to all team members
- **Version controlled** like any other project asset

## 📊 **Compatibility**

### **IntelliJ IDEA Versions**

- **Minimum**: IntelliJ IDEA 2021.3+
- **Recommended**: Latest version for best Dev Container support
- **Editions**: Works with Community and Ultimate

### **Operating Systems**

- **Windows**: Full support with WSL 2
- **macOS**: Native support
- **Linux**: Native support

### **Dev Container Integration**

- Configurations work **both inside and outside** Dev Containers
- **Enhanced experience** when using Dev Container
- **Fallback support** for traditional development

This auto-import system ensures that every developer gets a **professional, fully-configured IntelliJ IDEA experience**
immediately after cloning the repository, dramatically reducing onboarding time and ensuring consistency across the
entire development team.