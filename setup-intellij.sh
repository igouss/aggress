#!/bin/bash
set -e

echo "🧠 Setting up IntelliJ IDEA optimizations for Aggress Web Crawler..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if we're in the right directory
if [ ! -f "build.gradle" ] || [ ! -d ".idea" ]; then
    echo -e "${RED}❌ Error: This script must be run from the Aggress project root directory${NC}"
    echo "   Make sure you're in the directory containing build.gradle and .idea/"
    exit 1
fi

echo -e "${BLUE}📋 Verifying IntelliJ IDEA configurations...${NC}"

# Function to check if a file exists and report
check_file() {
    local file=$1
    local description=$2
    if [ -f "$file" ]; then
        echo -e "${GREEN}✅ $description${NC}"
        return 0
    else
        echo -e "${RED}❌ Missing: $description${NC}"
        return 1
    fi
}

# Function to check if a directory exists and report
check_dir() {
    local dir=$1
    local description=$2
    if [ -d "$dir" ]; then
        echo -e "${GREEN}✅ $description${NC}"
        return 0
    else
        echo -e "${RED}❌ Missing: $description${NC}"
        return 1
    fi
}

echo ""
echo "🏃 Run Configurations:"
check_file ".idea/runConfigurations/Crawler.xml" "Crawler run configuration"
check_file ".idea/runConfigurations/Frontend.xml" "Frontend run configuration"
check_file ".idea/runConfigurations/WebAdmin.xml" "WebAdmin run configuration"
check_file ".idea/runConfigurations/Crawler_Debug.xml" "Crawler debug configuration"
check_file ".idea/runConfigurations/Remote_Debug_Kubernetes_Crawler.xml" "Kubernetes remote debug configuration"

echo ""
echo "🗄️ Database & Services:"
check_file ".idea/dataSources.xml" "Database connections (Redis, Elasticsearch)"
check_file ".idea/kubernetes.xml" "Kubernetes integration"
check_file ".idea/docker-compose.xml" "Docker Compose integration"

echo ""
echo "⚙️ Development Settings:"
check_file ".idea/gradle.xml" "Gradle configuration"
check_file ".idea/codeStyles/Project.xml" "Code style settings"
check_file ".idea/liveTemplates/Aggress.xml" "Live templates"

echo ""
echo "📚 Documentation:"
check_file ".idea/PROJECT_SETUP.md" "Project setup guide"
check_file ".devcontainer/README-INTELLIJ.md" "IntelliJ-specific documentation"

# Check if configurations are being tracked by Git
echo ""
echo -e "${BLUE}🔍 Checking Git tracking...${NC}"

# Check gitignore
if grep -q "^\.idea/$" .gitignore 2>/dev/null; then
    echo -e "${YELLOW}⚠️  Warning: .idea/ is fully ignored in .gitignore${NC}"
    echo "   Consider updating .gitignore to track shared configurations"
else
    echo -e "${GREEN}✅ Git configuration looks good${NC}"
fi

# List tracked .idea files
tracked_files=$(git ls-files .idea/ 2>/dev/null | wc -l)
if [ "$tracked_files" -gt 0 ]; then
    echo -e "${GREEN}✅ $tracked_files IntelliJ configuration files are tracked by Git${NC}"
else
    echo -e "${YELLOW}⚠️  Warning: No .idea files are tracked by Git${NC}"
fi

echo ""
echo -e "${BLUE}🚀 Setup Instructions:${NC}"
echo ""
echo "1. 📂 Open Project:"
echo "   - File → Open → Select this directory"
echo "   - Or: idea . (from command line)"
echo ""
echo "2. 🐳 Enable Dev Container (Recommended):"
echo "   - Click 'Open in Container' when prompted"
echo "   - Or: File → Remote Development → Dev Containers"
echo ""
echo "3. ✅ Verify Setup:"
echo "   - Run → Edit Configurations (should see Crawler, Frontend, WebAdmin)"
echo "   - Database tool window (should see Redis, Elasticsearch)"
echo "   - Settings → Live Templates → Aggress (should see custom templates)"
echo ""
echo "4. 🎯 Quick Test:"
echo "   - Type 'lombokdata' + Tab in a Java file"
echo "   - Should expand to @Data class with @Builder"
echo ""

# Check if IntelliJ IDEA is available
if command -v idea >/dev/null 2>&1; then
    echo -e "${GREEN}💡 IntelliJ IDEA CLI detected!${NC}"
    echo "   You can open the project with: idea ."
    echo ""
    read -p "🚀 Would you like to open the project in IntelliJ IDEA now? (y/N): " open_idea
    if [[ $open_idea =~ ^[Yy]$ ]]; then
        echo "Opening project in IntelliJ IDEA..."
        idea . &
        echo -e "${GREEN}✅ Project opened! Check the configurations mentioned above.${NC}"
    fi
else
    echo -e "${YELLOW}💡 IntelliJ IDEA CLI not found${NC}"
    echo "   Open IntelliJ IDEA manually and use File → Open"
fi

echo ""
echo -e "${BLUE}📖 For detailed information, see:${NC}"
echo "   - .idea/PROJECT_SETUP.md"
echo "   - .devcontainer/README-INTELLIJ.md"
echo ""
echo -e "${GREEN}🎉 IntelliJ IDEA setup verification complete!${NC}"