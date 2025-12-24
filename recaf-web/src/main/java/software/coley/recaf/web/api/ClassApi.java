package software.coley.recaf.web.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.workspace.WorkspaceManager;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API for class operations.
 *
 * @author Recaf Contributors
 */
public class ClassApi {
	private static final Logger logger = Logging.get(ClassApi.class);
	private final WorkspaceManager workspaceManager;
	
	/**
	 * Creates a new class API.
	 *
	 * @param workspaceManager Workspace manager to use.
	 */
	public ClassApi(@Nonnull WorkspaceManager workspaceManager) {
		this.workspaceManager = workspaceManager;
	}
	
	/**
	 * Registers class API routes.
	 *
	 * @param app Javalin app to register routes on.
	 */
	public void register(@Nonnull Javalin app) {
		app.get("/api/classes", this::listClasses);
		app.get("/api/classes/{name}", this::getClass);
	}
	
	/**
	 * GET /api/classes - Lists all classes in the workspace.
	 *
	 * @param ctx Javalin context.
	 */
	private void listClasses(@Nonnull Context ctx) {
		Workspace current = workspaceManager.getCurrent();
		
		if (current == null) {
			ctx.status(404).json(Map.of("error", "No workspace loaded"));
			return;
		}
		
		JvmClassBundle bundle = current.getPrimaryResource().getJvmClassBundle();
		
		// Get all class names
		List<String> classNames = bundle.keySet().stream()
			.sorted()
			.collect(Collectors.toList());
		
		Map<String, Object> response = new HashMap<>();
		response.put("classes", classNames);
		response.put("count", classNames.size());
		
		ctx.json(response);
	}
	
	/**
	 * GET /api/classes/{name} - Gets details about a specific class.
	 *
	 * @param ctx Javalin context.
	 */
	private void getClass(@Nonnull Context ctx) {
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
		
		Map<String, Object> response = new HashMap<>();
		response.put("name", classInfo.getName());
		response.put("packageName", classInfo.getPackageName());
		response.put("superName", classInfo.getSuperName());
		response.put("interfaces", classInfo.getInterfaces());
		response.put("access", classInfo.getAccess());
		
		// Method names
		List<String> methods = classInfo.getMethods().stream()
			.map(m -> m.getName() + m.getDescriptor())
			.collect(Collectors.toList());
		response.put("methods", methods);
		
		// Field names
		List<String> fields = classInfo.getFields().stream()
			.map(f -> f.getName() + ":" + f.getDescriptor())
			.collect(Collectors.toList());
		response.put("fields", fields);
		
		ctx.json(response);
	}
}
