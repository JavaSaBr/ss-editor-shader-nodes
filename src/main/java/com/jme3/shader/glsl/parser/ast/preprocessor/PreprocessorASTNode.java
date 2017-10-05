package com.jme3.shader.glsl.parser.ast.preprocessor;

import com.jme3.shader.glsl.parser.ast.ASTNode;

/**
 * The node to present preprocessor in the code.
 *
 * @author JavaSaBr
 */
public class PreprocessorASTNode extends ASTNode {

    /**
     * The type.
     */
    private String type;

    /**
     * Gets the type.
     *
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type.
     */
    public void setType(final String type) {
        this.type = type;
    }

    @Override
    protected String getStringAttributes() {
        return getType();
    }
}
