# Recaf Web Module

This module provides web-based access to Recaf's bytecode editing capabilities through a REST API and web interface.

## Features

- **REST API**: Access Recaf functionality programmatically via HTTP endpoints
- **Web UI**: Browser-based interface for viewing and analyzing bytecode
- **Cross-platform**: Access Recaf from any device with a web browser
- **Lightweight**: Uses Javalin for minimal overhead

## Quick Start

### Running the Web Server

You can run the web server in several ways:

#### Option 1: Standalone Mode

```bash
# Build the module
./gradlew :recaf-web:build

# Run on default port (8080)
java -jar recaf-web/build/libs/recaf-web-*-all.jar

# Or specify a custom port
java -jar recaf-web/build/libs/recaf-web-*-all.jar 9000
```

#### Option 2: From Gradle

```bash
# Run on default port
./gradlew :recaf-web:run

# With custom port
./gradlew :recaf-web:run --args="9000"
```

#### Option 3: Integrated with Main Application

You can also start the web server from the main Recaf application by adding a command line argument:

```bash
java -jar recaf-ui.jar --web-server --web-port=8080
```

### Accessing the Web Interface

Once the server is running, open your browser and navigate to:

```
http://localhost:8080
```

## API Documentation

### Health Check

Check if the server is running:

```bash
GET /api/health
```

Response:
```json
{
  "status": "ok",
  "message": "Recaf Web API is running"
}
```

### Workspace Status

Check current workspace status:

```bash
GET /api/workspace/status
```

Response:
```json
{
  "hasWorkspace": true,
  "primaryResourceName": "123 classes"
}
```

### Workspace Information

Get detailed workspace information:

```bash
GET /api/workspace/info
```

Response:
```json
{
  "hasWorkspace": true,
  "classCount": 123,
  "fileCount": 45
}
```

### List Classes

List all classes in the workspace:

```bash
GET /api/classes
```

Response:
```json
{
  "classes": [
    "com/example/Main",
    "com/example/util/Helper"
  ],
  "count": 2
}
```

### Get Class Details

Get information about a specific class:

```bash
GET /api/classes/{className}
```

Example:
```bash
GET /api/classes/com/example/Main
```

Response:
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
  "fields": []
}
```

### Decompile Class

Decompile a class to Java source:

```bash
GET /api/decompile/{className}
```

Example:
```bash
GET /api/decompile/com/example/Main
```

Response:
```json
{
  "className": "com/example/Main",
  "decompiler": "CFR",
  "success": true,
  "text": "package com.example;\n\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World\");\n    }\n}"
}
```

## Web Interface

The web interface provides:

1. **Class Browser**: Navigate through all classes in the workspace
2. **Search**: Filter classes by name
3. **Class Info Tab**: View class metadata (fields, methods, interfaces, etc.)
4. **Decompiled Tab**: View decompiled Java source code

### Features

- Dark theme for reduced eye strain
- Responsive layout
- Real-time search filtering
- Syntax highlighting for code
- Clean, modern interface

## Security Considerations

⚠️ **Important**: This web server is intended for local development and analysis. It should **not** be exposed to untrusted networks without proper security measures:

- No authentication is enabled by default
- CORS is enabled for all origins (development convenience)
- No rate limiting
- No input sanitization beyond basic XSS prevention

For production use, you should:

1. Add authentication (e.g., API tokens, OAuth)
2. Configure CORS to restrict allowed origins
3. Add rate limiting
4. Deploy behind a reverse proxy (e.g., nginx) with HTTPS
5. Implement proper logging and monitoring

## Development

### Project Structure

```
recaf-web/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── software/coley/recaf/web/
│   │   │       ├── WebServer.java          # Main server class
│   │   │       └── api/
│   │   │           ├── WorkspaceApi.java    # Workspace endpoints
│   │   │           ├── ClassApi.java        # Class endpoints
│   │   │           └── DecompilerApi.java   # Decompiler endpoints
│   │   └── resources/
│   │       └── web/
│   │           ├── index.html               # Main web page
│   │           └── app.js                   # Frontend JavaScript
│   └── test/
│       └── java/
└── build.gradle
```

### Adding New Endpoints

To add a new API endpoint:

1. Create a new API class in `software.coley.recaf.web.api`
2. Implement the endpoint handlers
3. Register the API in `WebServer.registerRoutes()`

Example:

```java
public class MyApi {
    public void register(Javalin app) {
        app.get("/api/my-endpoint", this::handleRequest);
    }
    
    private void handleRequest(Context ctx) {
        ctx.json(Map.of("result", "data"));
    }
}
```

### Frontend Development

The web UI is a single-page application using vanilla JavaScript. To modify:

1. Edit `index.html` for structure and styling
2. Edit `app.js` for functionality
3. No build step required - changes are immediate

## Dependencies

- **Javalin 6.3.0**: Lightweight web framework
- **Jetty WebSocket**: For future real-time features
- **SLF4J**: Logging (provided by recaf-core)
- **Gson**: JSON serialization (provided by recaf-core)

## Troubleshooting

### Port Already in Use

If port 8080 is already in use:

```bash
# Use a different port
java -jar recaf-web.jar 9000
```

### Cannot Access from Other Machines

By default, the server only accepts connections from localhost. To allow remote connections (⚠️ not recommended without proper security):

1. Modify `WebServer.java` to bind to `0.0.0.0`
2. Configure your firewall to allow connections
3. **Ensure proper security measures are in place**

### Classes Not Loading

Make sure a workspace is loaded in Recaf before accessing the web interface. You can load a workspace by:

1. Starting Recaf with a file: `java -jar recaf.jar myfile.jar`
2. Loading a file through the UI (if using integrated mode)

## Future Enhancements

Potential features for future development:

- [ ] WebSocket support for real-time updates
- [ ] Bytecode editing via web interface
- [ ] Assembly view with syntax highlighting
- [ ] Multi-user collaboration features
- [ ] Authentication and authorization
- [ ] File upload/download
- [ ] Search across all classes
- [ ] Hex view for binary resources
- [ ] Plugin management through web UI

## Contributing

Contributions are welcome! Please follow the project's contribution guidelines and maintain the existing code style.

## License

This module is part of Recaf and uses the same license as the main project.
