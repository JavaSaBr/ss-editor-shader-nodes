package com.jme3.shader.glsl.parser.ast;

/**
 * The node to present a type.
 *
 * @author JavaSaBr
 */
public class TypeASTNode extends ASTNode {

    /**
     * THe type name.
     */
    private String name;

    /**
     * Gets the type name.
     *
     * @return the type name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the type name.
     *
     * @param name the type name.
     */
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    protected String getStringAttributes() {
        return getName();
    }
}
