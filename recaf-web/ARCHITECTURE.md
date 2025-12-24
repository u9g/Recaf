# Recaf Web Support Architecture

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Recaf Application                        │
│                                                                  │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐       │
│  │  recaf-core  │   │   recaf-ui   │   │  recaf-web   │       │
│  │              │   │              │   │              │       │
│  │ • Workspace  │   │ • JavaFX UI  │   │ • REST API   │       │
│  │ • Classes    │   │ • Desktop    │   │ • Web Server │       │
│  │ • Decompiler │   │ • Editor     │   │ • Web UI     │       │
│  │ • Services   │   │              │   │              │       │
│  └──────┬───────┘   └──────────────┘   └──────┬───────┘       │
│         │                                       │                │
│         └───────────────┬───────────────────────┘                │
│                         │                                        │
│                    Bootstrap                                     │
│                  (CDI Container)                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
            ┌─────────────────┴─────────────────┐
            │                                   │
            ▼                                   ▼
    ┌──────────────┐                   ┌──────────────┐
    │  Desktop UI  │                   │   Web API    │
    │   (JavaFX)   │                   │  (Javalin)   │
    └──────────────┘                   └──────┬───────┘
                                              │
                                              │ HTTP/REST
                                              │
                                    ┌─────────┴─────────┐
                                    │                   │
                                    ▼                   ▼
                            ┌──────────────┐    ┌──────────────┐
                            │  Web Browser │    │  API Clients │
                            │              │    │              │
                            │  • HTML      │    │  • Python    │
                            │  • CSS       │    │  • Node.js   │
                            │  • JavaScript│    │  • curl      │
                            └──────────────┘    └──────────────┘
```

## Module Dependencies

```
recaf-web
    │
    └── depends on ──> recaf-core
                          │
                          └── provides ──> Core Services
                                              │
                                              ├── WorkspaceManager
                                              ├── DecompilerManager
                                              ├── ClassInfo APIs
                                              └── Configuration
```

## Web Module Structure

```
recaf-web/
│
├── src/main/java/
│   └── software/coley/recaf/web/
│       │
│       ├── WebServer.java              # Main server entry point
│       │   ├── Initializes Javalin
│       │   ├── Configures CORS
│       │   ├── Serves static files
│       │   └── Registers API routes
│       │
│       └── api/                        # REST API Endpoints
│           ├── WorkspaceApi.java       # /api/workspace/*
│           │   ├── GET /status
│           │   └── GET /info
│           │
│           ├── ClassApi.java           # /api/classes/*
│           │   ├── GET /classes
│           │   └── GET /classes/{name}
│           │
│           └── DecompilerApi.java      # /api/decompile/*
│               └── GET /decompile/{name}
│
├── src/main/resources/web/
│   ├── index.html                      # Web UI
│   └── app.js                          # Frontend logic
│
└── src/test/java/
    └── software/coley/recaf/web/
        └── WebServerTest.java          # Unit tests
```

## Request Flow

### Example: Decompiling a Class

```
1. User clicks class in Web UI
   │
   ▼
2. JavaScript sends GET request
   GET /api/decompile/com/example/Main
   │
   ▼
3. Javalin routes to DecompilerApi
   DecompilerApi.decompileClass()
   │
   ▼
4. DecompilerApi gets services from Recaf
   - WorkspaceManager.getCurrent()
   - JvmDecompilerManager.getTargetJvmDecompiler()
   │
   ▼
5. Decompiler processes the class
   JvmDecompiler.decompile(workspace, classInfo)
   │
   ▼
6. Result converted to JSON
   {
     "className": "com/example/Main",
     "decompiler": "CFR",
     "success": true,
     "text": "package com.example;..."
   }
   │
   ▼
7. JavaScript receives response
   │
   ▼
8. UI displays decompiled code
   Syntax highlighted in <pre> tag
```

## API Endpoint Design

```
/api/
├── health                      # Health check
├── workspace/
│   ├── status                  # Quick status check
│   └── info                    # Detailed info
├── classes                     # List all classes
│   └── {className}             # Get specific class
└── decompile/
    └── {className}             # Decompile class
```

## Technology Stack

```
Backend (Java):
├── Javalin 6.3.0               # Web framework
├── Jetty                       # Embedded server
├── SLF4J/Logback              # Logging
├── Gson                        # JSON serialization
└── CDI (Weld)                  # Dependency injection

Frontend (Web):
├── Vanilla JavaScript          # No framework dependencies
├── HTML5                       # Semantic markup
├── CSS3                        # Modern styling
└── Fetch API                   # HTTP requests
```

## Data Flow

```
┌──────────────┐
│ Recaf loads  │
│  Workspace   │
│              │
│ (JAR/Class)  │
└──────┬───────┘
       │
       ▼
┌──────────────┐       ┌──────────────┐
│ Recaf Core   │◄──────┤  Web Server  │
│              │       │              │
│ • Classes    │       │ • REST API   │
│ • Decompiler │       │ • WebSocket  │
│ • Services   │       │              │
└──────┬───────┘       └──────┬───────┘
       │                      │
       │                      │ HTTP
       │                      │
       └──────────┬───────────┘
                  │
                  ▼
          ┌──────────────┐
          │  Web Client  │
          │              │
          │ • Browse     │
          │ • View       │
          │ • Search     │
          └──────────────┘
```

## Security Model

```
┌─────────────────────────────────────────┐
│            Security Layers               │
├─────────────────────────────────────────┤
│                                          │
│  Layer 1: Network                        │
│  • Localhost only (default)              │
│  • Optional: 0.0.0.0 binding            │
│  • Firewall rules                        │
│                                          │
├─────────────────────────────────────────┤
│                                          │
│  Layer 2: Application                    │
│  • CORS configuration                    │
│  • Input validation                      │
│  • XSS prevention                        │
│  • Rate limiting (TODO)                  │
│                                          │
├─────────────────────────────────────────┤
│                                          │
│  Layer 3: Authentication (TODO)          │
│  • API keys                              │
│  • OAuth/JWT                             │
│  • Session management                    │
│                                          │
├─────────────────────────────────────────┤
│                                          │
│  Layer 4: Authorization (TODO)           │
│  • Role-based access                     │
│  • Permission checks                     │
│  • Resource isolation                    │
│                                          │
└─────────────────────────────────────────┘

Note: Layers 3 and 4 are not implemented.
      Use reverse proxy (nginx) for production!
```

## Deployment Options

```
┌────────────────────────────────────────────────────────────┐
│                     Option 1: Standalone                    │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  java -jar recaf-web.jar [port]                            │
│                                                             │
│  • Independent process                                      │
│  • No desktop UI                                            │
│  • Headless server mode                                     │
│  • Ideal for remote/cloud deployment                        │
│                                                             │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                   Option 2: Integrated                      │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  java -jar recaf-ui.jar --web-server [--web-port=8080]     │
│                                                             │
│  • Runs alongside desktop UI                                │
│  • Shared workspace                                         │
│  • Best of both worlds                                      │
│  • Local development                                        │
│                                                             │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                   Option 3: Production                      │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  Internet                                                   │
│      │                                                      │
│      ▼                                                      │
│  ┌─────────┐                                               │
│  │  nginx  │  (Reverse Proxy + SSL)                        │
│  └────┬────┘                                               │
│       │                                                     │
│       ▼                                                     │
│  ┌─────────┐                                               │
│  │  Recaf  │  (Web Server)                                 │
│  │   Web   │                                               │
│  └─────────┘                                               │
│                                                             │
│  • SSL/TLS termination                                      │
│  • Authentication                                           │
│  • Rate limiting                                            │
│  • Load balancing                                           │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

## Extension Points

Future enhancements can be added through:

1. **New API Endpoints**: Add to `api/` package
2. **WebSocket Support**: Real-time updates
3. **Authentication**: Add auth middleware
4. **File Upload**: Workspace management
5. **Editing**: POST/PUT endpoints for modifications
6. **Plugin System**: Web-accessible plugins
7. **Multi-user**: Collaborative features

## Performance Considerations

```
Bottlenecks:
├── Decompilation (CPU intensive)
│   └── Solution: Cache results, async processing
│
├── Large workspaces (Memory)
│   └── Solution: Pagination, lazy loading
│
└── Concurrent requests
    └── Solution: Thread pool, rate limiting
```

