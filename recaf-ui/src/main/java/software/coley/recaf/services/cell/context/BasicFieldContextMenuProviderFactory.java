package software.coley.recaf.services.cell.context;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.coley.collections.Unchecked;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.path.PathNodes;
import software.coley.recaf.services.cell.icon.IconProvider;
import software.coley.recaf.services.cell.icon.IconProviderService;
import software.coley.recaf.services.cell.text.TextProvider;
import software.coley.recaf.services.cell.text.TextProviderService;
import software.coley.recaf.services.navigation.Actions;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.List;

import static org.kordamp.ikonli.carbonicons.CarbonIcons.*;

/**
 * Basic implementation for {@link FieldContextMenuProviderFactory}.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicFieldContextMenuProviderFactory extends AbstractContextMenuProviderFactory
		implements FieldContextMenuProviderFactory {

	@Inject
	public BasicFieldContextMenuProviderFactory(@Nonnull TextProviderService textService,
	                                            @Nonnull IconProviderService iconService,
	                                            @Nonnull Actions actions) {
		super(textService, iconService, actions);
	}

	@Nonnull
	@Override
	public ContextMenuProvider getFieldContextMenuProvider(@Nonnull ContextSource source,
	                                                       @Nonnull Workspace workspace,
	                                                       @Nonnull WorkspaceResource resource,
	                                                       @Nonnull ClassBundle<? extends ClassInfo> bundle,
	                                                       @Nonnull ClassInfo declaringClass,
	                                                       @Nonnull FieldMember field) {
		return () -> {
			TextProvider nameProvider = textService.getFieldMemberTextProvider(workspace, resource, bundle, declaringClass, field);
			IconProvider iconProvider = iconService.getClassMemberIconProvider(workspace, resource, bundle, declaringClass, field);

			return createMemberContextMenu(
					source, workspace, resource, bundle, declaringClass, field,
					nameProvider, iconProvider,
					"menu.goto.field",
					"menu.search.field-references",
					actions::renameField,
					(edit, ws, res, bndl, declClass, member) -> {
						FieldMember fld = (FieldMember) member;
						edit.item("menu.edit.assemble.field", EDIT, Unchecked.runnable(() ->
								actions.openAssembler(PathNodes.memberPath(ws, res, bndl, declClass, fld))));

						if (declClass.isJvmClass()) {
							JvmClassBundle jvmBundle = (JvmClassBundle) bndl;
							JvmClassInfo declaringJvmClass = declClass.asJvmClass();

							edit.item("menu.edit.copy", COPY_FILE, () ->
									actions.copyMember(ws, res, jvmBundle, declaringJvmClass, fld));
							edit.item("menu.edit.delete", TRASH_CAN, () ->
									actions.deleteClassFields(ws, res, jvmBundle, declaringJvmClass, List.of(fld)));
							edit.item("menu.edit.remove.annotation", CLOSE, () ->
									actions.deleteMemberAnnotations(ws, res, jvmBundle, declaringJvmClass, fld))
									.disableWhen(fld.getAnnotations().isEmpty());
						}

						// TODO: implement operations
						//  - Edit
						//    - Add annotation
					}
			);
		};
	}
}
