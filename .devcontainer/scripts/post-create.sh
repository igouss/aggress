#!/bin/bash
set -e

echo "🚀 Setting up Aggress Web Crawler development environment..."

# Make scripts executable
chmod +x .devcontainer/scripts/*.sh
chmod +x gradlew 2>/dev/null || true

# Create gradle cache directory
mkdir -p /home/aggress/.gradle

# Create config file if it doesn't exist
if [ ! -f crawler/src/main/resources/config.properties ]; then
    echo "📝 Creating default config.properties..."
    mkdir -p crawler/src/main/resources
    cat > crawler/src/main/resources/config.properties << 'EOF'
# Aggress Web Crawler Configuration (Development)
canadiangunnutzLogin=
canadiangunnutzPassword=

# Redis Configuration (Dev Container)
redisHost=redis
redisPort=6379
redisPassword=devpassword

# Elasticsearch Configuration (Dev Container)
elasticHost=elasticsearch
elasticPort=9200

# Kafka Configuration (Dev Container)
kafkaBootstrapServers=kafka:29092

# Tor Proxy Configuration (Dev Container)
torProxyHost=tor-proxy
torProxyPort=8118

# Development Settings
deployment.env=development
logging.level=DEBUG
EOF
fi

# Make import-certs script executable
if [ -f crawler/src/main/resources/startssl-java-master/import-certs.sh ]; then
    chmod +x crawler/src/main/resources/startssl-java-master/import-certs.sh
    echo "📜 SSL certificates script made executable"
fi

# Create .gradle directory and set permissions
if [ ! -d "/home/aggress/.gradle" ]; then
    mkdir -p /home/aggress/.gradle
    chown -R aggress:aggress /home/aggress/.gradle
fi

# Create basic development aliases  
cat >> /home/aggress/.bashrc << 'EOF'

# Aggress Web Crawler Development Aliases
alias agbuild="./gradlew clean build"
alias agtest="./gradlew -Dcrawler.test=true test"
alias agrun-crawler="java -jar crawler/build/libs/crawler-all-1.0-SNAPSHOT.jar"
alias agrun-frontend="java -jar frontend/build/libs/frontend-1.0-SNAPSHOT.jar"
alias agrun-webadmin="java -jar webadmin/build/libs/webadmin-1.0-SNAPSHOT.jar"

# Kubernetes Development Aliases  
alias k="kubectl"
alias kns="kubens"
alias kctx="kubectx"
alias logs="stern"
alias kdev="kubectl --context=kind-aggress-dev"

# Docker Development Aliases
alias dps="docker ps"
alias dlogs="docker logs -f"
alias dexec="docker exec -it"

# Skaffold Development
alias agdev="skaffold dev"
alias agdebug="skaffold debug"
alias agdeploy="skaffold run"

# Status function
agstatus() {
    echo "📊 Aggress Development Status"
    echo "=============================="
    echo "🐳 Docker Services:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo ""
    if command -v kubectl &> /dev/null && kubectl cluster-info &> /dev/null; then
        echo "☸️  Kubernetes Cluster:"
        kubectl get nodes
        echo ""
        echo "📦 Pods:"
        kubectl get pods -A
    else
        echo "☸️  Kubernetes: Not running (use 'kind create cluster --config .devcontainer/kind-config.yaml --name aggress-dev')"
    fi
}

EOF

echo "✅ Post-create setup completed!"
echo ""
echo "🎯 Quick Start Commands:"
echo "  agstatus    - Check development environment status"
echo "  agbuild     - Build the project"
echo "  agdev       - Start Kubernetes development mode"
echo ""
echo "📖 See README.md 'Development Commands Reference' section for all available commands"