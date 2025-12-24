# Quick Start Guide - Recaf Web Support

Get up and running with Recaf's web interface in 5 minutes!

## Prerequisites

- Java 17 or higher
- Gradle (included via wrapper)
- A JAR file or Java classes to analyze

## Step 1: Clone and Build

```bash
# Clone the repository
git clone https://github.com/Col-E/Recaf.git
cd Recaf

# Build the project
./gradlew build -x test
```

## Step 2: Start Recaf with a Workspace

Load a JAR file you want to analyze:

```bash
# Option A: Using the UI build
java -jar recaf-ui/build/libs/recaf-ui-*-all.jar /path/to/your/app.jar

# Option B: For headless/server mode
# This will be available in future versions
```

## Step 3: Start the Web Server

In a new terminal:

```bash
cd Recaf
./gradlew :recaf-web:run
```

You should see:
```
INFO  Recaf Web Server started on port 8080
INFO  Access the web interface at: http://localhost:8080
```

## Step 4: Access the Web Interface

Open your browser and navigate to:

```
http://localhost:8080
```

You should see:
- The Recaf web interface with a dark theme
- A list of classes in the sidebar
- Workspace status in the header

## Step 5: Explore the Interface

### Browsing Classes
1. Use the search box to filter classes by name
2. Click any class to view its details
3. See fields, methods, and interfaces in the "Class Info" tab

### Viewing Decompiled Code
1. Select a class from the list
2. Click the "Decompiled" tab
3. View the Java source code

### Using the API
Try these commands in a new terminal:

```bash
# Health check
curl http://localhost:8080/api/health

# Get workspace info
curl http://localhost:8080/api/workspace/info

# List all classes
curl http://localhost:8080/api/classes

# Get details for a specific class
curl http://localhost:8080/api/classes/com/example/Main

# Decompile a class
curl http://localhost:8080/api/decompile/com/example/Main
```

## Troubleshooting

### "No workspace loaded"
Make sure you started Recaf with a JAR file in Step 2.

### "Port 8080 already in use"
Use a different port:
```bash
./gradlew :recaf-web:run --args="9000"
```
Then access at `http://localhost:9000`

### "Connection refused"
Make sure the web server is running and you're using the correct port.

### Build errors
If you encounter dependency download issues:
1. Check your internet connection
2. Try again (JitPack can be slow)
3. Use a VPN if certain domains are blocked

## What's Next?

- Read the [full README](README.md) for detailed documentation
- Check out [EXAMPLES.md](EXAMPLES.md) for integration examples
- Review [ARCHITECTURE.md](ARCHITECTURE.md) to understand the design
- Contribute improvements via pull requests!

## Quick Tips

1. **Dark Theme**: The web UI uses a dark theme optimized for long viewing sessions
2. **Search**: Type in the search box to quickly filter classes
3. **Real-time**: Refresh the page to see updates if you modify the workspace
4. **API-First**: All UI features are built on the REST API
5. **Documentation**: Hover over elements to see tooltips (coming soon!)

## Common Use Cases

### Analyzing an Application
1. Load the JAR in Recaf
2. Start web server
3. Browse classes in the UI
4. Decompile interesting classes

### Batch Processing
Use the REST API with scripts:
```bash
# Export all decompiled classes
for class in $(curl -s http://localhost:8080/api/classes | jq -r '.classes[]'); do
    curl -s "http://localhost:8080/api/decompile/$class" | jq -r '.text' > "$class.java"
done
```

### Remote Access
Access from another device on your network:
1. Find your machine's IP address
2. Navigate to `http://YOUR_IP:8080`
3. ⚠️ Only on trusted networks!

## Getting Help

- **Issues**: Report bugs on GitHub
- **Discussions**: Ask questions in Discord
- **Documentation**: Check the docs/ folder
- **Examples**: See EXAMPLES.md for more

## Security Notice

⚠️ **Important**: The web server is designed for local use. Do not expose it to the internet without:
- Adding authentication
- Using HTTPS
- Implementing rate limiting
- Running behind a reverse proxy

See the README for security best practices.

---

**Enjoy analyzing bytecode with Recaf's web interface!** 🎉
