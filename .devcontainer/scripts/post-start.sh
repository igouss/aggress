#!/bin/bash
set -e

echo "🔄 Starting Aggress development environment..."

# Wait for Docker daemon to be ready
while ! docker info >/dev/null 2>&1; do
    echo "⏳ Waiting for Docker daemon..."
    sleep 2
done

# Wait for services to be healthy
echo "🏥 Checking service health..."

# Check Redis
echo "📡 Checking Redis..."
timeout=60
counter=0
until docker exec -it $(docker ps -q -f "name=redis") redis-cli -a devpassword ping 2>/dev/null | grep -q PONG; do
    if [ $counter -eq $timeout ]; then
        echo "❌ Redis health check timeout"
        break
    fi
    echo "   Redis not ready, waiting..."
    sleep 2
    ((counter++))
done

# Check Elasticsearch
echo "🔍 Checking Elasticsearch..."
counter=0
until curl -s http://localhost:9200/_cluster/health >/dev/null 2>&1; do
    if [ $counter -eq $timeout ]; then
        echo "❌ Elasticsearch health check timeout"
        break
    fi
    echo "   Elasticsearch not ready, waiting..."
    sleep 2
    ((counter++))
done

# Check if Kubernetes cluster exists
if ! kind get clusters | grep -q "aggress-dev"; then
    echo "☸️  Creating local Kubernetes cluster..."
    if [ -f .devcontainer/kind-config.yaml ]; then
        kind create cluster --config .devcontainer/kind-config.yaml --name aggress-dev
        echo "✅ Kubernetes cluster 'aggress-dev' created"
    else
        echo "⚠️  Kind config not found, creating basic cluster..."
        kind create cluster --name aggress-dev
    fi
else
    echo "☸️  Kubernetes cluster 'aggress-dev' already exists"
fi

# Set kubectl context
kubectl config use-context kind-aggress-dev 2>/dev/null || echo "⚠️  Could not set kubectl context"

# Show environment status
echo ""
echo "🎉 Development environment ready!"
echo ""
echo "📊 Service Status:"
echo "==================="
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(redis|elasticsearch|kafka|tor-proxy|registry)"

echo ""
echo "☸️  Kubernetes Status:"
echo "======================"
if kubectl get nodes >/dev/null 2>&1; then
    kubectl get nodes
else
    echo "❌ Kubernetes cluster not accessible"
fi

echo ""
echo "🔗 Available Services:"
echo "======================"
echo "Frontend:       http://localhost:8080"
echo "WebAdmin:       http://localhost:8081"
echo "Elasticsearch:  http://localhost:9200"
echo "Redis:          localhost:6379"
echo "Kafka:          localhost:9092"
echo "Tor Proxy:      localhost:8118"
echo "Registry:       localhost:5000"

echo ""
echo "🛠️  Next Steps:"
echo "==============="
echo "1. Run 'agbuild' to build the project"
echo "2. Run 'agdev' to start Kubernetes development mode"
echo "3. Run 'aghelp' to see all available commands"
echo "4. Open another terminal and run 'k9s' for Kubernetes dashboard"