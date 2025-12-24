package software.coley.recaf.web.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import software.coley.recaf.Recaf;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.decompile.DecompileResult;
import software.coley.recaf.services.decompile.JvmDecompiler;
import software.coley.recaf.services.decompile.JvmDecompilerManager;
import software.coley.recaf.services.workspace.WorkspaceManager;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for decompiler operations.
 *
 * @author Recaf Contributors
 */
public class DecompilerApi {
	private static final Logger logger = Logging.get(DecompilerApi.class);
	private final Recaf recaf;
	
	/**
	 * Creates a new decompiler API.
	 *
	 * @param recaf Recaf instance.
	 */
	public DecompilerApi(@Nonnull Recaf recaf) {
		this.recaf = recaf;
	}
	
	/**
	 * Registers decompiler API routes.
	 *
	 * @param app Javalin app to register routes on.
	 */
	public void register(@Nonnull Javalin app) {
		app.get("/api/decompile/{name}", this::decompileClass);
	}
	
	/**
	 * GET /api/decompile/{name} - Decompiles a class.
	 *
	 * @param ctx Javalin context.
	 */
	private void decompileClass(@Nonnull Context ctx) {
		WorkspaceManager workspaceManager = recaf.get(WorkspaceManager.class);
		Workspace current = workspaceManager.getCurrent();
		
		if (current == null) {
			ctx.status(404).json(Map.of("error", "No workspace loaded"));
			return;
		}
		
		String className = ctx.pathParam("name");
		JvmClassBundle bundle = current.getPrimaryResource().getJvmClassBundle();
		JvmClassInfo classInfo = bundle.get(className);
		
		if (classInfo == null) {
			ctx.status(404).json(Map.of("error", "Class not found: " + className));
			return;
		}
		
		try {
			JvmDecompilerManager decompilerManager = recaf.get(JvmDecompilerManager.class);
			JvmDecompiler decompiler = decompilerManager.getTargetJvmDecompiler();
			
			DecompileResult result = decompiler.decompile(current, classInfo);
			
			Map<String, Object> response = new HashMap<>();
			response.put("className", className);
			response.put("decompiler", decompiler.getName());
			
			if (result.getException() != null) {
				response.put("success", false);
				response.put("error", result.getException().getMessage());
				response.put("text", result.getText());
			} else {
				response.put("success", true);
				response.put("text", result.getText());
			}
			
			ctx.json(response);
		} catch (Exception e) {
			logger.error("Failed to decompile class: " + className, e);
			ctx.status(500).json(Map.of("error", "Decompilation failed: " + e.getMessage()));
		}
	}
}
