package software.coley.recaf.web;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import software.coley.recaf.Bootstrap;
import software.coley.recaf.Recaf;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.workspace.WorkspaceManager;
import software.coley.recaf.web.api.WorkspaceApi;
import software.coley.recaf.web.api.ClassApi;
import software.coley.recaf.web.api.DecompilerApi;

/**
 * Web server for Recaf that provides REST API access to workspace and bytecode operations.
 * This allows remote access to Recaf functionality via HTTP.
 *
 * @author Recaf Contributors
 */
public class WebServer {
	private static final Logger logger = Logging.get(WebServer.class);
	private static final int DEFAULT_PORT = 8080;
	
	private final Javalin app;
	private final Recaf recaf;
	private final int port;
	
	/**
	 * Creates a new web server instance.
	 *
	 * @param recaf Recaf instance to expose via REST API.
	 * @param port Port to bind the server to.
	 */
	public WebServer(@Nonnull Recaf recaf, int port) {
		this.recaf = recaf;
		this.port = port;
		this.app = createApp();
	}
	
	/**
	 * Creates and configures the Javalin application.
	 *
	 * @return Configured Javalin instance.
	 */
	private Javalin createApp() {
		Javalin app = Javalin.create(config -> {
			// Enable CORS for development
			config.bundledPlugins.enableCors(cors -> {
				cors.addRule(it -> {
					it.anyHost();
				});
			});
			
			// Serve static files from resources
			config.staticFiles.add("/web", Location.CLASSPATH);
		});
		
		// Register API routes
		registerRoutes(app);
		
		return app;
	}
	
	/**
	 * Registers all API routes.
	 *
	 * @param app Javalin app to register routes on.
	 */
	private void registerRoutes(@Nonnull Javalin app) {
		WorkspaceManager workspaceManager = recaf.get(WorkspaceManager.class);
		
		// Health check endpoint
		app.get("/api/health", ctx -> {
			ctx.json(new HealthResponse("ok", "Recaf Web API is running"));
		});
		
		// API endpoints
		WorkspaceApi workspaceApi = new WorkspaceApi(workspaceManager);
		ClassApi classApi = new ClassApi(workspaceManager);
		DecompilerApi decompilerApi = new DecompilerApi(recaf);
		
		workspaceApi.register(app);
		classApi.register(app);
		decompilerApi.register(app);
		
		// Root path serves the web UI
		app.get("/", ctx -> ctx.redirect("/index.html"));
	}
	
	/**
	 * Starts the web server.
	 */
	public void start() {
		try {
			app.start(port);
			logger.info("Recaf Web Server started on port {}", port);
			logger.info("Access the web interface at: http://localhost:{}", port);
		} catch (Exception e) {
			logger.error("Failed to start web server", e);
			throw new RuntimeException("Failed to start web server", e);
		}
	}
	
	/**
	 * Stops the web server.
	 */
	public void stop() {
		app.stop();
		logger.info("Recaf Web Server stopped");
	}
	
	/**
	 * Main entry point for standalone web server.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		
		// Parse port from arguments if provided
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				logger.error("Invalid port number: {}. Using default port {}", args[0], DEFAULT_PORT);
			}
		}
		
		// Initialize Recaf
		Recaf recaf = Bootstrap.get();
		
		// Start web server
		WebServer server = new WebServer(recaf, port);
		server.start();
		
		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}
	
	/**
	 * Health check response.
	 */
	public static class HealthResponse {
		public final String status;
		public final String message;
		
		public HealthResponse(String status, String message) {
			this.status = status;
			this.message = message;
		}
	}
}
