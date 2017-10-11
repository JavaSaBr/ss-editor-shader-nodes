package com.ss.editor.shader.nodes.tree.node.definition;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.model.shader.node.definition.*;
import com.ss.editor.shader.nodes.tree.action.DeleteShaderNodeDefinitionAction;
import com.ss.editor.shader.nodes.tree.operation.RenameShaderNodeDefinitionOperation;
import com.ss.editor.shader.nodes.ui.PluginIcons;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.util.StringUtils;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The node to present shader node definition.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionTreeNode extends TreeNode<ShaderNodeDefinition> {

    public ShaderNodeDefinitionTreeNode(@NotNull final ShaderNodeDefinition element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {

        final ShaderNodeDefinition element = getElement();

        switch (element.getType()) {
            case Vertex:
                return Icons.VERTEX_16;
            default:
                return PluginIcons.FRAGMENT_16;
        }
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return getElement().getName();
    }

    @Override
    @FXThread
    public void changeName(@NotNull final NodeTree<?> nodeTree, @NotNull final String newName) {
        if (StringUtils.equals(getName(), newName)) return;

        super.changeName(nodeTree, newName);

        final TreeNode<?> parent = notNull(getParent());
        final ShaderNodeDefinitionList definitionList = (ShaderNodeDefinitionList) parent.getElement();

        final ShaderNodeDefinition definition = getElement();
        final ChangeConsumer consumer = notNull(nodeTree.getChangeConsumer());
        consumer.execute(new RenameShaderNodeDefinitionOperation(definition.getName(), newName, definitionList, definition));
    }

    @Override
    @FXThread
    public boolean canEditName() {
        return true;
    }

    @Override
    @FXThread
    public boolean hasChildren(@NotNull final NodeTree<?> nodeTree) {
        return true;
    }

    @Override
    @FXThread
    public @NotNull Array<TreeNode<?>> getChildren(@NotNull final NodeTree<?> nodeTree) {

        final ShaderNodeDefinition definition = getElement();

        final Array<TreeNode<?>> children = ArrayFactory.newArray(TreeNode.class, 2);
        children.add(FACTORY_REGISTRY.createFor(new ShaderNodeDefinitionImports(definition)));
        children.add(FACTORY_REGISTRY.createFor(new ShaderNodeDefinitionDefines(definition)));
        children.add(FACTORY_REGISTRY.createFor(new ShaderNodeInputDefinitionParameters(definition)));
        children.add(FACTORY_REGISTRY.createFor(new ShaderNodeOutputDefinitionParameters(definition)));
        children.add(FACTORY_REGISTRY.createFor(new ShaderNodeDefinitionShaderSources(definition)));

        return children;
    }

    @Override
    @FXThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new DeleteShaderNodeDefinitionAction(nodeTree, this));
    }
}
