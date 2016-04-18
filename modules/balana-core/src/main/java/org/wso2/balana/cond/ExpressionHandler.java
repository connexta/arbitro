/*
 * ExpressionHandler.java
 *
 * Created by: seth proctor (stp)
 * Created on: Wed Dec 29, 2004	 8:24:30 PM
 * Desc: 
 *
 */

package org.wso2.balana.cond;

import org.w3c.dom.Node;
import org.wso2.balana.Balana;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.UnknownIdentifierException;
import org.wso2.balana.attr.AttributeDesignatorFactory;
import org.wso2.balana.attr.AttributeSelectorFactory;

/**
 * This is a package-private utility class that handles parsing all the possible expression types.
 * It was added becuase in 2.0 multiple classes needed this. Note that this could also be added to
 * Expression and that interface could be made an abstract class, but that would require substantial
 * change.
 * 
 * @since 2.0
 * @author Seth Proctor
 */
public class ExpressionHandler {

    /**
     * Parses an expression, recursively handling any sub-elements. This is provided as a utility
     * class, but in practice is used only by <code>Apply</code>, <code>Condition</code>, and
     * <code>VariableDefinition</code>.
     * 
     * @param root the DOM root of an ExpressionType XML type
     * @param metaData the meta-data associated with the containing policy
     * @param manager <code>VariableManager</code> used to connect references and definitions while
     *            parsing
     * 
     * @return an <code>Expression</code> or null if the root node cannot be parsed as a valid
     *         Expression
     * @throws org.wso2.balana.ParsingException if the node cannot be parsed
     * @return returns an Expression parsed from a node
     */
    public static Expression parseExpression(Node root, PolicyMetaData metaData,
            VariableManager manager) throws ParsingException {
        String name = DOMHelper.getLocalName(root);

        if (name.equals("Apply")) {
            return Apply.getInstance(root, metaData, manager);
        } else if (name.equals("AttributeValue")) {
            try {
                return Balana.getInstance().getAttributeFactory().createValue(root);
            } catch (UnknownIdentifierException uie) {
                throw new ParsingException("Unknown DataType", uie);
            }
        } else if("AttributeDesignator".equals(name)){
            return AttributeDesignatorFactory.getFactory().getAbstractDesignator(root, metaData);
        } else if (name.equals("SubjectAttributeDesignator")) {
            return AttributeDesignatorFactory.getFactory().getAbstractDesignator(root, metaData);
        } else if (name.equals("ResourceAttributeDesignator")) {
            return AttributeDesignatorFactory.getFactory().getAbstractDesignator(root, metaData);
        } else if (name.equals("ActionAttributeDesignator")) {
            return AttributeDesignatorFactory.getFactory().getAbstractDesignator(root, metaData);
        } else if (name.equals("EnvironmentAttributeDesignator")) {
            return AttributeDesignatorFactory.getFactory().getAbstractDesignator(root, metaData);
        } else if (name.equals("AttributeSelector")) {
            return AttributeSelectorFactory.getFactory().getAbstractSelector(root, metaData);
        } else if (name.equals("Function")) {
            return getFunction(root, metaData, FunctionFactory.getGeneralInstance());
        } else if (name.equals("VariableReference")) {
            return VariableReference.getInstance(root, metaData, manager);
        }

        // return null if it was none of these
        return null;
    }

    /**
     * Helper method that tries to get a function instance

     * @param root the Node which contains the function
     * @param metaData The metadata used by the FunctionFactory to construct the function
     * @param factory {@link FunctionFactory} which is used to constuct the function
     * @return returns a Function parsed from the node
     * @throws ParsingException if the node value is unable to be parsed
     */
    public static Function getFunction(Node root, PolicyMetaData metaData, FunctionFactory factory)
            throws ParsingException {

        String functionName;

        try {
            Node functionNode = root.getAttributes().getNamedItem("FunctionId");
            functionName = functionNode.getNodeValue();
        } catch (Exception e) {
            throw new ParsingException("Error parsing required FunctionId in " +
                    "FunctionType", e);
        }

        try {
            // try to get an instance of the given function
            return factory.createFunction(functionName);
        } catch (UnknownIdentifierException uie) {
            throw new ParsingException("Unknown FunctionId", uie);
        } catch (FunctionTypeException fte) {
            // try creating as an abstract function
            try {
                FunctionFactory ff = FunctionFactory.getGeneralInstance();
                return ff.createAbstractFunction(functionName, root, metaData.getXPathIdentifier());
            } catch (Exception e) {
                // any exception at this point is a failure
                throw new ParsingException("failed to create abstract function" + " "
                        + functionName, e);
            }
        }
    }

}
