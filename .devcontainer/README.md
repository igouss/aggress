# 🚀 Aggress Dev Container Setup

This directory contains the complete Dev Container configuration for the Aggress Web Crawler with Kubernetes support.

## 📁 Structure

```
.devcontainer/
├── devcontainer.json          # Main Dev Container configuration
├── docker-compose.yml         # Development services
├── Dockerfile.dev             # Development environment image
├── kind-config.yaml           # Local Kubernetes cluster config
├── kubernetes/                # K8s manifests for development
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secrets.yaml
│   └── services.yaml
└── scripts/
    ├── post-create.sh         # Setup script
    └── post-start.sh          # Service startup script
```

## 🎯 Features

### Development Environment

- **Java 24** with Temurin JDK
- **Gradle 8.5** with wrapper support
- **Lombok** for code generation
- **VS Code** optimized with Java extensions

### Kubernetes Tools

- **kubectl** - Kubernetes CLI
- **helm** - Package manager
- **skaffold** - Development workflow
- **k9s** - Kubernetes TUI
- **stern** - Log tailing
- **kind** - Local clusters

### Services Stack

- **Redis** - Caching and storage
- **Elasticsearch** - Search engine
- **Kafka** - Message streaming
- **Zookeeper** - Kafka coordination
- **Tor Proxy** - Anonymous web crawling (for avoiding IP blocks)
- **Registry** - Local container registry

## 🚀 Quick Start

### With VS Code

1. **Open project in VS Code**
2. **Install Dev Containers extension**
3. **Reopen in Container** (Ctrl+Shift+P → "Dev Containers: Reopen in Container")
4. **Wait for setup** (5-10 minutes first time)

### With IntelliJ IDEA

1. **Open project in IntelliJ IDEA** (2021.3+ required)
2. **Accept Dev Container prompt** or go to File → Remote Development → Dev Containers
3. **Click "Open in Container"**
4. **Wait for setup** (5-10 minutes first time)
5. **Enjoy pre-configured features** - Run configurations, database connections, Kubernetes integration

📖 **See [README-INTELLIJ.md](README-INTELLIJ.md) for detailed IntelliJ features and usage**

## 🛠️ Available Commands

### Project Commands

```bash
agbuild          # Build all modules
agtest           # Run tests with crawler flag
agrun-crawler    # Run crawler application
agrun-frontend   # Run frontend (port 8080)
agrun-webadmin   # Run web admin (port 8081)
```

### Kubernetes Commands

```bash
agdev            # Start Skaffold development mode
agdebug          # Start Skaffold debug mode
agdeploy         # Deploy to Kubernetes
agstatus         # Show environment status
```

### Utility Commands

```bash
k                # kubectl alias
kns              # kubens (namespace switching)
kctx             # kubectx (context switching)
logs <pod>       # stern log tailing
k9s              # Kubernetes dashboard
```

## 🎓 IDE-Specific Tips

### IntelliJ IDEA

- **Enable Kubernetes plugin** for enhanced K8s support
- **Use built-in Docker integration** for container management
- **Configure remote debugger** for Java applications running in K8s
- **Use Database tools** to connect to Redis/Elasticsearch
- **Enable Lombok plugin** if not already active

### VS Code

- **Use integrated terminal** for command-line operations
- **Install Kubernetes extension** for cluster management
- **Use Dev Containers extension** for container lifecycle
- **Configure port forwarding** through VS Code UI

## 🔍 Troubleshooting

### Container Build Issues

```bash
# Rebuild container from scratch
docker system prune -a
# Then reopen in container
```

### Kubernetes Issues

```bash
# Recreate Kind cluster
kind delete cluster --name aggress-dev
kind create cluster --config .devcontainer/kind-config.yaml --name aggress-dev
```

### Service Health Issues

```bash
# Check service status
agstatus

# Restart services
docker-compose -f .devcontainer/docker-compose.yml restart
```

### Port Conflicts

```bash
# Check port usage
netstat -tuln | grep -E ':(8080|8081|9200|6379)'

# Kill processes using ports
sudo lsof -ti:8080 | xargs kill -9
```

## 🏗️ Customization

### Disabling Tor Proxy

If you don't need the Tor proxy for your development:

1. **Edit `docker-compose.yml`:**
   ```yaml
   # Comment out or remove this section:
   # tor-proxy:
   #   image: arulrajnet/torprivoxy
   #   restart: unless-stopped
   #   ports:
   #     - "8118:8118"
   #     - "9050:9050"
   #     - "9051:9051"
   #   networks:
   #     - aggress-dev
   ```

2. **Update dependencies:**
   ```yaml
   dev-environment:
     # Remove tor-proxy from depends_on:
     depends_on:
       - redis
       - elasticsearch
       # - tor-proxy  # <-- Remove this line
   ```

3. **Rebuild container:** Use "Dev Containers: Rebuild Container"

### Adding New Tools

Edit `.devcontainer/Dockerfile.dev` to add tools:

```dockerfile
RUN apt-get update && apt-get install -y \
    your-new-tool \
    && rm -rf /var/lib/apt/lists/*
```

### Environment Variables

Add to `.devcontainer/devcontainer.json`:

```json
"containerEnv": {
  "YOUR_VAR": "value"
}
```

### VS Code Extensions

Add to `.devcontainer/devcontainer.json`:

```json
"customizations": {
  "vscode": {
    "extensions": [
      "your.extension.id"
    ]
  }
}
```

## 📊 Performance Tips

1. **Use volume mounts** for large files (already configured)
2. **Limit container resources** if running on low-spec machines
3. **Use .dockerignore** to exclude unnecessary files
4. **Prune Docker regularly** to free space

## 🔐 Security Notes

- **Secrets** are stored in Kubernetes secrets (base64 encoded)
- **SSL certificates** are mounted as volumes
- **Development passwords** are hardcoded (change for production)
- **RBAC** is configured for proper access control

## 📝 Configuration Files

### Application Config

Location: `crawler/src/main/resources/config.properties`

```properties
# Auto-generated on container creation
redisHost=redis
redisPort=6379
elasticHost=elasticsearch
elasticPort=9200
```

### Kubernetes Config

Location: `.devcontainer/kubernetes/configmap.yaml`

- Service endpoints
- Application settings
- Development overrides

## 🎓 Learning Resources

- [Dev Containers Documentation](https://code.visualstudio.com/docs/devcontainers/containers)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Skaffold Documentation](https://skaffold.dev/docs/)
- [Helm Documentation](https://helm.sh/docs/)

## 🤝 Contributing

When modifying the Dev Container:

1. **Test changes** in a clean environment
2. **Update documentation** for new features
3. **Verify compatibility** across platforms
4. **Update version tags** appropriately

For questions, see the main project README.md or open an issue.