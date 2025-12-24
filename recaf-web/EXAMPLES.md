# Web Support Usage Examples

This document provides practical examples of using Recaf's web support features.

## Example 1: Analyzing a JAR File via Web Interface

1. Start Recaf with a JAR file:
```bash
java -jar recaf-ui.jar myapp.jar
```

2. In another terminal, start the web server:
```bash
cd recaf-web
./gradlew run
```

3. Open your browser to `http://localhost:8080`

4. You'll see:
   - List of all classes from myapp.jar in the sidebar
   - Search functionality to filter classes
   - Click any class to view its details
   - Switch to "Decompiled" tab to see Java source code

## Example 2: Using the REST API with curl

### Check Server Health
```bash
curl http://localhost:8080/api/health
```

Output:
```json
{
  "status": "ok",
  "message": "Recaf Web API is running"
}
```

### Get Workspace Status
```bash
curl http://localhost:8080/api/workspace/status
```

Output:
```json
{
  "hasWorkspace": true,
  "primaryResourceName": "250 classes"
}
```

### List All Classes
```bash
curl http://localhost:8080/api/classes
```

Output:
```json
{
  "classes": [
    "com/example/Main",
    "com/example/util/Helper",
    "com/example/model/User"
  ],
  "count": 3
}
```

### Get Class Details
```bash
curl http://localhost:8080/api/classes/com/example/Main
```

Output:
```json
{
  "name": "com/example/Main",
  "packageName": "com/example",
  "superName": "java/lang/Object",
  "interfaces": [],
  "access": 33,
  "methods": [
    "main([Ljava/lang/String;)V",
    "<init>()V"
  ],
  "fields": [
    "version:Ljava/lang/String;"
  ]
}
```

### Decompile a Class
```bash
curl http://localhost:8080/api/decompile/com/example/Main
```

Output:
```json
{
  "className": "com/example/Main",
  "decompiler": "CFR",
  "success": true,
  "text": "package com.example;\n\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World\");\n    }\n}"
}
```

## Example 3: Integrating with Python

Here's a simple Python script to interact with Recaf's REST API:

```python
import requests
import json

BASE_URL = "http://localhost:8080/api"

def check_health():
    """Check if the server is running."""
    response = requests.get(f"{BASE_URL}/health")
    return response.json()

def list_classes():
    """Get all classes in the workspace."""
    response = requests.get(f"{BASE_URL}/classes")
    return response.json()

def get_class_info(class_name):
    """Get detailed information about a class."""
    response = requests.get(f"{BASE_URL}/classes/{class_name}")
    return response.json()

def decompile_class(class_name):
    """Decompile a class to Java source."""
    response = requests.get(f"{BASE_URL}/decompile/{class_name}")
    return response.json()

# Example usage
if __name__ == "__main__":
    # Check server status
    health = check_health()
    print(f"Server status: {health['status']}")
    
    # List all classes
    classes = list_classes()
    print(f"Found {classes['count']} classes")
    
    # Analyze the first class
    if classes['classes']:
        first_class = classes['classes'][0]
        print(f"\nAnalyzing: {first_class}")
        
        # Get class details
        info = get_class_info(first_class)
        print(f"Methods: {len(info['methods'])}")
        print(f"Fields: {len(info['fields'])}")
        
        # Decompile
        decompiled = decompile_class(first_class)
        if decompiled['success']:
            print("\nDecompiled source:")
            print(decompiled['text'])
```

## Example 4: Integrating with JavaScript/Node.js

```javascript
const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';

async function analyzeWorkspace() {
    try {
        // Check health
        const health = await axios.get(`${BASE_URL}/health`);
        console.log('Server status:', health.data.status);
        
        // Get workspace info
        const workspace = await axios.get(`${BASE_URL}/workspace/info`);
        console.log(`Workspace has ${workspace.data.classCount} classes`);
        
        // List classes
        const classes = await axios.get(`${BASE_URL}/classes`);
        console.log(`\nClasses in workspace:`);
        
        // Analyze each class
        for (const className of classes.data.classes.slice(0, 5)) {
            const classInfo = await axios.get(
                `${BASE_URL}/classes/${encodeURIComponent(className)}`
            );
            console.log(`\n${className}:`);
            console.log(`  - ${classInfo.data.methods.length} methods`);
            console.log(`  - ${classInfo.data.fields.length} fields`);
        }
    } catch (error) {
        console.error('Error:', error.message);
    }
}

analyzeWorkspace();
```

## Example 5: Finding Classes with Specific Methods

Using the REST API to search for classes containing specific methods:

```bash
# Get all classes
curl -s http://localhost:8080/api/classes | jq -r '.classes[]' > /tmp/classes.txt

# Check each class for a specific method
while read class; do
    info=$(curl -s "http://localhost:8080/api/classes/$class")
    if echo "$info" | jq -r '.methods[]' | grep -q "main"; then
        echo "Found main method in: $class"
    fi
done < /tmp/classes.txt
```

## Example 6: Batch Decompiling Classes

Decompile all classes and save to files:

```bash
#!/bin/bash

OUTPUT_DIR="decompiled"
mkdir -p "$OUTPUT_DIR"

# Get all classes
classes=$(curl -s http://localhost:8080/api/classes | jq -r '.classes[]')

# Decompile each class
for class in $classes; do
    echo "Decompiling $class..."
    
    # Create directory structure
    dir=$(dirname "$class")
    mkdir -p "$OUTPUT_DIR/$dir"
    
    # Decompile and save
    curl -s "http://localhost:8080/api/decompile/$class" \
        | jq -r '.text' \
        > "$OUTPUT_DIR/${class}.java"
done

echo "Decompilation complete! Files saved to $OUTPUT_DIR/"
```

## Example 7: Remote Access

To access Recaf web interface from another machine on your network:

1. Start the server with host binding (requires code modification):
```java
// In WebServer.java, modify createApp() to:
app.start("0.0.0.0", port);
```

2. Find your machine's IP:
```bash
# Linux/Mac
ifconfig | grep "inet "

# Windows
ipconfig
```

3. Access from another machine:
```
http://YOUR_IP_ADDRESS:8080
```

⚠️ **Security Warning**: Only do this on trusted networks!

## Example 8: Custom Port Configuration

Run on a different port to avoid conflicts:

```bash
# Port 9000
java -jar recaf-web.jar 9000

# Or with Gradle
./gradlew :recaf-web:run --args="9000"
```

## Example 9: Monitoring with Health Checks

Set up automated health monitoring:

```bash
#!/bin/bash
# healthcheck.sh

while true; do
    status=$(curl -s http://localhost:8080/api/health | jq -r '.status')
    
    if [ "$status" = "ok" ]; then
        echo "[$(date)] Server is healthy"
    else
        echo "[$(date)] Server is down!"
        # Send alert, restart server, etc.
    fi
    
    sleep 60  # Check every minute
done
```

## Troubleshooting

### CORS Issues

If you're accessing the API from a web application on a different origin, ensure CORS is properly configured in `WebServer.java`.

### Connection Refused

Make sure:
1. The web server is running
2. You're using the correct port
3. No firewall is blocking the connection

### Empty Workspace

If APIs return "No workspace loaded", make sure to load a JAR/class file in Recaf before starting the web server, or integrate the web server with the main Recaf application.

## Best Practices

1. **Use HTTPS in Production**: Always use SSL/TLS when exposing the server to untrusted networks
2. **Implement Authentication**: Add API key or OAuth authentication
3. **Rate Limiting**: Prevent abuse with rate limiting
4. **Input Validation**: Validate all class names and parameters
5. **Logging**: Monitor all API access for security and debugging
6. **Error Handling**: Return appropriate HTTP status codes and error messages

## Next Steps

- Explore the [API documentation](README.md#api-documentation)
- Check the [recaf-web source code](src/main/java/software/coley/recaf/web/)
- Contribute improvements to the web interface
- Report issues or suggest features

