package com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_INPUT_PARAMETER;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.*;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.ui.PluginCssClasses;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach.AttachVarExpressionToShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.InputSocketElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.SocketElement;
import com.ss.editor.ui.css.CssClasses;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.StringUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of input shader nodes parameter.
 *
 * @author JavaSaBr
 */
public class InputShaderNodeParameter extends ShaderNodeParameter {

    /**
     * The check box to use expression.
     */
    @NotNull
    private CheckBox useExpression;

    /**
     * The label to use an expression.
     */
    @NotNull
    private Label useExpressionLabel;

    /**
     * The field with expression.
     */
    @NotNull
    private TextField expressionField;

    public InputShaderNodeParameter(
            @NotNull final ShaderNodeElement<?> nodeElement,
            @NotNull final ShaderNodeVariable variable
    ) {
        super(nodeElement, variable);
        FXUtils.addClassTo(this, SHADER_NODE_INPUT_PARAMETER);
    }

    /**
     * Get the checkbox of using expression.
     *
     * @return the checkbox of using expression.
     */
    @FxThread
    protected @NotNull CheckBox getUseExpression() {
        return useExpression;
    }

    /**
     * Get the use expression label.
     *
     * @return the use expression label.
     */
    @FxThread
    protected @NotNull Label getUseExpressionLabel() {
        return useExpressionLabel;
    }

    /**
     * Get the expression field.
     *
     * @return the expression field.
     */
    @FxThread
    protected @NotNull TextField getExpressionField() {
        return expressionField;
    }

    @Override
    @FxThread
    protected @NotNull SocketElement createSocket() {
        return new InputSocketElement(this);
    }

    /**
     * Return true if this parameter uses expression.
     *
     * @return true if this parameter uses expression.
     */
    @FxThread
    public boolean isUsedExpression() {
        return getUseExpression().isSelected();
    }

    @Override
    @FxThread
    protected void createContent() {
        super.createContent();

        this.useExpressionLabel = new Label(PluginMessages.NODE_ELEMENT_USE_EXPRESSION + ":");
        this.useExpression = new CheckBox();
        this.useExpression.setDisable(!canUseExpression(getNodeElement(), getVariable()));
        this.useExpression.setOnAction(this::handleChangeUseExpression);
        this.expressionField = new TextField();
        this.expressionField.prefWidthProperty().bind(widthProperty());
        this.expressionField.setOnKeyReleased(this::handleChangeExpression);
        this.expressionField.focusedProperty()
            .addListener((observable, oldValue, newValue) -> handleChangeExpression(null));

        var parameterContainer = new HBox(getSocket(), getNameLabel(), getTypeLabel());
        parameterContainer.prefWidthProperty().bind(widthProperty());

        var expressionContainer = new HBox(expressionField);
        expressionContainer.managedProperty().bind(useExpression.selectedProperty());
        expressionContainer.visibleProperty().bind(useExpression.selectedProperty());
        expressionContainer.prefWidthProperty().bind(widthProperty());

        add(parameterContainer, 0, 0);
        add(getUseExpressionLabel(), 1, 0);
        add(getUseExpression(), 2, 0);
        add(expressionContainer, 0, 1, 3, 1);

        FXUtils.addClassTo(parameterContainer, PluginCssClasses.SHADER_NODE_INPUT_PARAMETER_CONTAINER);
        FXUtils.addClassTo(expressionContainer, CssClasses.DEF_HBOX);
    }

    /**
     * Handle of changing expression.
     *
     * @param event the key event or null.
     */
    @FxThread
    private void handleChangeExpression(@Nullable final KeyEvent event) {

        if (isDisable() || event != null && event.getCode() != KeyCode.ENTER) {
            return;
        }

        var element = getNodeElement();
        var shaderNode = (ShaderNode) element.getObject();
        var container = element.getContainer();
        var expressionField = getExpressionField();
        var expr = expressionField.getText();
        var mapping = findInMappingByNLeftVar(shaderNode, getVariable());

        if (mapping == null && StringUtils.isEmpty(expr)) {
            return;
        } else if (mapping != null && StringUtils.equals(expr, mapping.getRightExpression())) {
            return;
        }

        var newMapping = makeExpressionMapping(this, expr);

        container.getChangeConsumer()
            .execute(new AttachVarExpressionToShaderNodeOperation(shaderNode, newMapping, mapping));

        setDisable(true);
    }

    @Override
    public void refresh() {
        super.refresh();

        var element = getNodeElement();
        var object = element.getObject();

        if (object instanceof ShaderNode) {

            var shaderNode = (ShaderNode) element.getObject();
            var mapping = findInMappingByNLeftVar(shaderNode, getVariable());
            var selected = mapping != null && mapping.getRightExpression() != null;
            var expression = mapping != null ? mapping.getRightExpression() : "";

            getUseExpression().setSelected(selected);
            getExpressionField().setText(expression);
        }

        setDisable(false);
    }

    /**
     * Handle of using expression.
     *
     * @param event the action event.
     */
    @FxThread
    private void handleChangeUseExpression(@NotNull final ActionEvent event) {

        var element = getNodeElement();
        var shaderNode = (ShaderNode) element.getObject();
        var useExpression = getUseExpression();
        var container = element.getContainer();
        var mapping = findInMappingByNLeftVar(shaderNode, getVariable());
        var expression = getExpressionField().getText();

        if (useExpression.isSelected()) {
            var newMapping = makeExpressionMapping(this, expression);
            container.getChangeConsumer()
                    .execute(new AttachVarExpressionToShaderNodeOperation(shaderNode, newMapping, mapping));
        } else {
            container.getChangeConsumer()
                .execute(new AttachVarExpressionToShaderNodeOperation(shaderNode, null, mapping));
        }
    }
}
