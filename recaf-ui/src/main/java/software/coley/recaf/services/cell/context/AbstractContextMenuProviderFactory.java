package software.coley.recaf.services.cell.context;

import jakarta.annotation.Nonnull;
import javafx.scene.control.ContextMenu;
import org.slf4j.Logger;
import software.coley.recaf.analytics.logging.Logging;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.member.ClassMember;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.path.IncompletePathException;
import software.coley.recaf.path.PathNodes;
import software.coley.recaf.services.cell.icon.IconProvider;
import software.coley.recaf.services.cell.icon.IconProviderService;
import software.coley.recaf.services.cell.text.TextProvider;
import software.coley.recaf.services.cell.text.TextProviderService;
import software.coley.recaf.services.navigation.Actions;
import software.coley.recaf.services.search.match.StringPredicateProvider;
import software.coley.recaf.ui.contextmenu.ContextMenuBuilder;
import software.coley.recaf.ui.pane.search.MemberReferenceSearchPane;
import software.coley.recaf.util.ClipboardUtil;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.ClassBundle;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import static org.kordamp.ikonli.carbonicons.CarbonIcons.*;

/**
 * Common base to context menu provider factories.
 *
 * @author Matt Coley
 */
public abstract class AbstractContextMenuProviderFactory implements ContextMenuProviderFactory {
	private static final Logger logger = Logging.get(AbstractContextMenuProviderFactory.class);
	protected final TextProviderService textService;
	protected final IconProviderService iconService;
	protected final Actions actions;

	protected AbstractContextMenuProviderFactory(@Nonnull TextProviderService textService,
												 @Nonnull IconProviderService iconService,
												 @Nonnull Actions actions) {
		this.textService = textService;
		this.iconService = iconService;
		this.actions = actions;
	}

	/**
	 * Creates a context menu with header for a class member, and populates it with common actions.
	 *
	 * @param source         Context source.
	 * @param workspace      Containing workspace.
	 * @param resource       Containing resource.
	 * @param bundle         Containing bundle.
	 * @param declaringClass Containing class.
	 * @param member         The member to create a menu for.
	 * @param nameProvider   Text provider for the member.
	 * @param iconProvider   Icon provider for the member.
	 * @param gotoMenuKey    Translation key for the "go to" menu item.
	 * @param searchMenuKey  Translation key for the "search references" menu item.
	 * @param renameAction   Action to perform when renaming the member.
	 * @param editPopulator  Callback to populate JVM-specific edit menu items.
	 *
	 * @return Context menu for the member.
	 */
	protected ContextMenu createMemberContextMenu(@Nonnull ContextSource source,
	                                              @Nonnull Workspace workspace,
	                                              @Nonnull WorkspaceResource resource,
	                                              @Nonnull ClassBundle<? extends ClassInfo> bundle,
	                                              @Nonnull ClassInfo declaringClass,
	                                              @Nonnull ClassMember member,
	                                              @Nonnull TextProvider nameProvider,
	                                              @Nonnull IconProvider iconProvider,
	                                              @Nonnull String gotoMenuKey,
	                                              @Nonnull String searchMenuKey,
	                                              @Nonnull MemberRenameAction renameAction,
	                                              @Nonnull JvmEditMenuPopulator editPopulator) {
		ContextMenu menu = new ContextMenu();
		addHeader(menu, nameProvider.makeText(), iconProvider.makeIcon());
		var builder = new ContextMenuBuilder(menu, source).forMember(workspace, resource, bundle, declaringClass, member);

		if (source.isReference()) {
			builder.item(gotoMenuKey, ARROW_RIGHT, () -> {
				ClassPathNode classPath = PathNodes.classPath(workspace, resource, bundle, declaringClass);
				try {
					actions.gotoDeclaration(classPath)
							.requestFocus(member);
				} catch (IncompletePathException ex) {
					logger.error("Cannot go to member due to incomplete path", ex);
				}
			});
		} else {
			// Edit menu
			var edit = builder.submenu("menu.edit", EDIT);
			editPopulator.populateEditMenu(edit, workspace, resource, bundle, declaringClass, member);
		}

		// Search actions
		builder.item(searchMenuKey, CODE_REFERENCE, () -> {
			MemberReferenceSearchPane pane = actions.openNewMemberReferenceSearch();
			pane.ownerPredicateIdProperty().setValue(StringPredicateProvider.KEY_EQUALS);
			pane.namePredicateIdProperty().setValue(StringPredicateProvider.KEY_EQUALS);
			pane.descPredicateIdProperty().setValue(StringPredicateProvider.KEY_EQUALS);
			pane.ownerValueProperty().setValue(declaringClass.getName());
			pane.nameValueProperty().setValue(member.getName());
			pane.descValueProperty().setValue(member.getDescriptor());
		});

		// Copy path
		builder.item("menu.tab.copypath", COPY_LINK, () -> ClipboardUtil.copyString(declaringClass, member));

		// Documentation actions
		builder.memberItem("menu.analysis.comment", ADD_COMMENT, actions::openCommentEditing);

		// Refactor actions
		builder.memberItem("menu.refactor.rename", TAG_EDIT, renameAction::rename);

		return menu;
	}

	/**
	 * Functional interface for renaming a member.
	 */
	@FunctionalInterface
	protected interface MemberRenameAction {
		/**
		 * Performs the rename action.
		 *
		 * @param workspace      Containing workspace.
		 * @param resource       Containing resource.
		 * @param bundle         Containing bundle.
		 * @param declaringClass Containing class.
		 * @param member         The member to rename.
		 */
		void rename(@Nonnull Workspace workspace,
		            @Nonnull WorkspaceResource resource,
		            @Nonnull ClassBundle<? extends ClassInfo> bundle,
		            @Nonnull ClassInfo declaringClass,
		            @Nonnull ClassMember member);
	}

	/**
	 * Functional interface for populating JVM-specific edit menu items.
	 */
	@FunctionalInterface
	protected interface JvmEditMenuPopulator {
		/**
		 * Populates the edit menu with JVM-specific items.
		 *
		 * @param edit           The edit submenu builder.
		 * @param workspace      Containing workspace.
		 * @param resource       Containing resource.
		 * @param bundle         Containing bundle.
		 * @param declaringClass Containing class.
		 * @param member         The member being edited.
		 */
		void populateEditMenu(@Nonnull ContextMenuBuilder edit,
		                      @Nonnull Workspace workspace,
		                      @Nonnull WorkspaceResource resource,
		                      @Nonnull ClassBundle<? extends ClassInfo> bundle,
		                      @Nonnull ClassInfo declaringClass,
		                      @Nonnull ClassMember member);
	}
}
