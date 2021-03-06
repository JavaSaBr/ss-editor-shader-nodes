package com.ss.editor.shader.nodes.ui.component.shader.nodes.main;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.shader.ShaderNode;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.global.InputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.OutputShaderNodeParameter;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of shader element to present fragment shader nodes.
 *
 * @author JavaSaBr
 */
public class FragmentShaderNodeElement extends MainShaderNodeElement {

    public FragmentShaderNodeElement(@NotNull final ShaderNodesContainer container, @NotNull final ShaderNode object) {
        super(container, object);
    }

    @Override
    @FXThread
    public boolean canAttach(@NotNull final InputShaderNodeParameter inputParameter,
                             @NotNull final OutputShaderNodeParameter outputParameter) {

        if(!super.canAttach(inputParameter, outputParameter)) {
            return false;
        }

        final ShaderNodeElement<?> sourceElement = outputParameter.getNodeElement();

        if (sourceElement instanceof InputGlobalShaderNodeElement) {
            final ShaderGenerationInfo info = ((InputGlobalShaderNodeElement) sourceElement).getObject();
            return outputParameter.getVariable() != info.getVertexGlobal();
        }

        return sourceElement instanceof MaterialShaderNodeElement ||
                sourceElement instanceof FragmentShaderNodeElement ||
                sourceElement instanceof VertexShaderNodeElement ||
                sourceElement instanceof WorldShaderNodeElement;
    }
}
