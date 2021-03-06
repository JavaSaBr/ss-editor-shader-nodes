package com.ss.editor.shader.nodes.ui.component.shader.nodes.main;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.*;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.*;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.remove.RemoveShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.global.InputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach.AttachAttributeToShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach.AttachGlobalToShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach.AttachUniformToShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach.AttachVarToShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.OutputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.StringUtils;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The implementation of shader element to present shader nodes.
 *
 * @author JavaSaBr
 */
public class MainShaderNodeElement extends ShaderNodeElement<ShaderNode> {

    public MainShaderNodeElement(@NotNull final ShaderNodesContainer container, @NotNull final ShaderNode object) {
        super(container, object);
    }

    @Override
    @FXThread
    protected @NotNull String getTitleText() {
        return getObject().getDefinition().getName();
    }


    @FXThread
    @Override
    public @Nullable ShaderNodeParameter parameterFor(@NotNull final ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping, final boolean input) {

        final ShaderNode shaderNode = getObject();
        if (!shaderNode.getName().equals(variable.getNameSpace())) {
            return null;
        }

        return super.parameterFor(variable, fromOutputMapping, input);
    }

    @FXThread
    @Override
    protected void fillParameters(@NotNull final VBox container) {
        super.fillParameters(container);

        final ShaderNode shaderNode = getObject();
        final ShaderNodeDefinition definition = shaderNode.getDefinition();
        final List<ShaderNodeVariable> inputs = definition.getInputs();
        final List<ShaderNodeVariable> outputs = definition.getOutputs();

        for (final ShaderNodeVariable variable : inputs) {
            FXUtils.addToPane(new InputShaderNodeParameter(this, variable), container);
        }

        for (final ShaderNodeVariable variable : outputs) {
            FXUtils.addToPane(new OutputShaderNodeParameter(this, variable), container);
        }
    }

    @Override
    @FXThread
    public void attach(@NotNull final InputShaderNodeParameter inputParameter,
                       @NotNull final OutputShaderNodeParameter outputParameter) {
        super.attach(inputParameter, outputParameter);

        final ShaderNodeElement<?> nodeElement = outputParameter.getNodeElement();

        final ShaderNodeVariable inVar = inputParameter.getVariable();
        final ShaderNodeVariable outVar = outputParameter.getVariable();
        final ShaderNode shaderNode = getObject();

        final VariableMapping currentMapping = findInMappingByNLeftVar(shaderNode, inVar);
        final VariableMapping newMapping = makeMapping(inputParameter, outputParameter);
        newMapping.setRightSwizzling(calculateRightSwizzling(inVar, outVar));

        if (StringUtils.isEmpty(newMapping.getRightSwizzling())) {
            newMapping.setLeftSwizzling(calculateLeftSwizzling(inVar, outputParameter.getVariable()));
        }

        if (newMapping.equals(currentMapping)) {
            return;
        }

        final ShaderNodesChangeConsumer changeConsumer = getContainer().getChangeConsumer();

        if (nodeElement instanceof InputGlobalShaderNodeElement) {
            changeConsumer.execute(new AttachGlobalToShaderNodeOperation(shaderNode, newMapping, currentMapping));
        } else if (nodeElement instanceof AttributeShaderNodeElement) {
            changeConsumer.execute(new AttachAttributeToShaderNodeOperation(shaderNode, newMapping, currentMapping));
            return;
        }

        final ShaderNodesContainer container = nodeElement.getContainer();
        final TechniqueDef techniqueDef = container.getTechniqueDef();

        if (nodeElement instanceof MainShaderNodeElement) {

            final ShaderNode outShaderNode = ((MainShaderNodeElement) nodeElement).getObject();

            changeConsumer.execute(new AttachVarToShaderNodeOperation(shaderNode, newMapping,
                    currentMapping, techniqueDef, outShaderNode));

        } else if (nodeElement instanceof MaterialShaderNodeElement || nodeElement instanceof WorldShaderNodeElement) {

            final Shader.ShaderType type = shaderNode.getDefinition().getType();

            if (type == Shader.ShaderType.Vertex) {

                final List<ShaderNode> fragmentNodes = container.findWithRightInputVar(newMapping.getLeftVariable(),
                        FragmentShaderNodeElement.class);

                newMapping.getLeftVariable().setShaderOutput(!fragmentNodes.isEmpty());
            }

            changeConsumer.execute(new AttachUniformToShaderNodeOperation(shaderNode, outVar, techniqueDef,
                    newMapping, currentMapping));
        }
    }

    @Override
    @FXThread
    public @Nullable ShaderNodeAction<?> getDeleteAction() {
        return new RemoveShaderNodeAction(getContainer(), getObject(),
                new Vector2f((float) getLayoutX(), (float) getLayoutY()));
    }
}
