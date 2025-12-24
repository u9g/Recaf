package software.coley.recaf.services.cell.context;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.control.ContextMenu;
import software.coley.collections.Unchecked;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.path.PathNodes;
import software.coley.recaf.services.cell.icon.IconProvider;
import software.coley.recaf.services.cell.icon.IconProviderService;
import software.coley.recaf.services.cell.text.TextProvider;
import software.coley.recaf.services.cell.text.TextProviderService;
import software.coley.recaf.services.navigation.Actions;
import software.coley.recaf.ui.contextmenu.ContextMenuBuilder;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.List;

import static org.kordamp.ikonli.carbonicons.CarbonIcons.*;

/**
 * Basic implementation for {@link MethodContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicMethodContextMenuProviderFactory extends AbstractContextMenuProviderFactory implements MethodContextMenuProviderFactory {

	@Inject
	public BasicMethodContextMenuProviderFactory(@Nonnull TextProviderService textService,
	                                             @Nonnull IconProviderService iconService,
	                                             @Nonnull Actions actions) {
		super(textService, iconService, actions);
	}

	@Nonnull
	@Override
	public ContextMenuProvider getMethodContextMenuProvider(@Nonnull ContextSource source,
	                                                        @Nonnull Workspace workspace,
	                                                        @Nonnull WorkspaceResource resource,
	                                                        @Nonnull ClassBundle<? extends ClassInfo> bundle,
	                                                        @Nonnull ClassInfo declaringClass,
	                                                        @Nonnull MethodMember method) {
		return () -> {
			TextProvider nameProvider = textService.getMethodMemberTextProvider(workspace, resource, bundle, declaringClass, method);
			IconProvider iconProvider = iconService.getClassMemberIconProvider(workspace, resource, bundle, declaringClass, method);

			ContextMenu menu = createMemberContextMenu(
					source, workspace, resource, bundle, declaringClass, method,
					nameProvider, iconProvider,
					"menu.goto.method",
					"menu.search.method-references",
					actions::renameMethod,
					(edit, ws, res, bndl, declClass, member) -> {
						MethodMember mtd = (MethodMember) member;
						edit.item("menu.edit.assemble.method", EDIT, Unchecked.runnable(() ->
								actions.openAssembler(PathNodes.memberPath(ws, res, bndl, declClass, mtd))));

						if (declClass.isJvmClass()) {
							JvmClassBundle jvmBundle = (JvmClassBundle) bndl;
							JvmClassInfo declaringJvmClass = declClass.asJvmClass();

							edit.item("menu.edit.copy", COPY_FILE, () ->
									actions.copyMember(ws, res, jvmBundle, declaringJvmClass, mtd));
							edit.item("menu.edit.removevars", CIRCLE_DASH, () ->
									actions.removeMethodVariables(ws, res, jvmBundle, declaringJvmClass, List.of(mtd)));
							// The conditions for optimally no-op'ing a constructor are a bit tricky, we'll just skip those for now.
							if (!mtd.getName().equals("<init>"))
								edit.item("menu.edit.noop", CIRCLE_DASH, () ->
										actions.makeMethodsNoop(ws, res, jvmBundle, declaringJvmClass, List.of(mtd)));
							edit.item("menu.edit.delete", TRASH_CAN, () ->
									actions.deleteClassMethods(ws, res, jvmBundle, declaringJvmClass, List.of(mtd)));
							edit.item("menu.edit.remove.annotation", CLOSE, () ->
									actions.deleteMemberAnnotations(ws, res, jvmBundle, declaringJvmClass, mtd))
									.disableWhen(mtd.getAnnotations().isEmpty());
						}

						// TODO: implement additional operations
						//  - Edit
						//    - Add annotation
					}
			);

			// Additional method-specific menu items
			// TODO: implement additional operations
			//  - View
			//    - Control flow graph
			//    - Application flow graph
			var builder = new ContextMenuBuilder(menu, source).forMember(workspace, resource, bundle, declaringClass, method);
			var view = builder.submenu("menu.view", VIEW);
			if (declaringClass.isJvmClass()) {
				JvmClassBundle jvmBundle = (JvmClassBundle) bundle;
				JvmClassInfo declaringJvmClass = declaringClass.asJvmClass();
				view.item("menu.view.methodcallgraph", FLOW, () ->
						actions.openMethodCallGraph(workspace, resource, jvmBundle, declaringJvmClass, method));
			}

			// TODO: implement additional operations
			//  - Deobfuscate
			//    - Regenerate variable names
			//    - Optimize with pattern matchers
			//    - Optimize with SSVM
			//  - Simulate with SSVM (Virtualize > Run)

			return menu;
		};
	}
}
