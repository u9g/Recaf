package software.coley.recaf.web.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.services.workspace.WorkspaceManager;
import software.coley.recaf.workspace.model.Workspace;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for workspace operations.
 *
 * @author Recaf Contributors
 */
public class WorkspaceApi {
	private static final Logger logger = Logging.get(WorkspaceApi.class);
	private final WorkspaceManager workspaceManager;
	
	/**
	 * Creates a new workspace API.
	 *
	 * @param workspaceManager Workspace manager to use.
	 */
	public WorkspaceApi(@Nonnull WorkspaceManager workspaceManager) {
		this.workspaceManager = workspaceManager;
	}
	
	/**
	 * Registers workspace API routes.
	 *
	 * @param app Javalin app to register routes on.
	 */
	public void register(@Nonnull Javalin app) {
		app.get("/api/workspace/status", this::getWorkspaceStatus);
		app.get("/api/workspace/info", this::getWorkspaceInfo);
	}
	
	/**
	 * GET /api/workspace/status - Returns current workspace status.
	 *
	 * @param ctx Javalin context.
	 */
	private void getWorkspaceStatus(@Nonnull Context ctx) {
		Workspace current = workspaceManager.getCurrent();
		
		Map<String, Object> response = new HashMap<>();
		response.put("hasWorkspace", current != null);
		
		if (current != null) {
			response.put("primaryResourceName", 
				current.getPrimaryResource().getJvmClassBundle().size() + " classes");
		}
		
		ctx.json(response);
	}
	
	/**
	 * GET /api/workspace/info - Returns detailed workspace information.
	 *
	 * @param ctx Javalin context.
	 */
	private void getWorkspaceInfo(@Nonnull Context ctx) {
		Workspace current = workspaceManager.getCurrent();
		
		if (current == null) {
			ctx.status(404).json(Map.of("error", "No workspace loaded"));
			return;
		}
		
		Map<String, Object> response = new HashMap<>();
		response.put("hasWorkspace", true);
		
		// Primary resource info
		int classCount = current.getPrimaryResource().getJvmClassBundle().size();
		int fileCount = current.getPrimaryResource().getFileBundle().size();
		
		response.put("classCount", classCount);
		response.put("fileCount", fileCount);
		
		ctx.json(response);
	}
}
