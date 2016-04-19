/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package com.connexta.arbitro.xacml3;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.connexta.arbitro.ParsingException;
import com.connexta.arbitro.attr.AttributeValue;
import com.connexta.arbitro.cond.Expression;
import com.connexta.arbitro.cond.ExpressionHandler;
import com.connexta.arbitro.ctx.AttributeAssignment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.connexta.arbitro.DOMHelper;
import com.connexta.arbitro.PolicyMetaData;
import com.connexta.arbitro.attr.BagAttribute;
import com.connexta.arbitro.cond.Evaluatable;
import com.connexta.arbitro.cond.EvaluationResult;
import com.connexta.arbitro.ctx.EvaluationCtx;

/**
 * Represents AttributeAssignmentExpressionType in the XACML 3.0 policy schema..
 */
public class AttributeAssignmentExpression {

    /**
     * attribute id of the AttributeAssignmentExpression element
     */
    private URI attributeId;

    /**
     * category of the AttributeAssignmentExpression element whether it is subject, action and etc
     */
    private URI category;

    /**
     *  issuer of the AttributeAssignment
     */
    private String issuer;

    /**
     *  <code>Expression</code> that contains in <code>AttributeAssignmentExpression</code>
     */
    private Expression expression;

    /**
     * Constructor that creates a new <code>AttributeAssignmentExpression</code> based on
     * the given elements.
     * @param attributeId   attribute id of the AttributeAssignmentExpression element
     * @param category category of the AttributeAssignmentExpression element whether it is subject, action and etc
     * @param expression  <code>Expression</code> that contains in <code>AttributeAssignmentExpression</code>
     * @param issuer issuer of the AttributeAssignment
     */
    public AttributeAssignmentExpression(URI attributeId, URI category, Expression expression,
                                         String issuer) {
        this.attributeId = attributeId;
        this.category = category;
        this.expression = expression;
        this.issuer = issuer;
    }

    /**
     *  creates a <code>AttributeAssignmentExpression</code> based on its DOM node.
     *
     * @param root  root the node to parse for the AttributeAssignment
     * @param metaData  meta-date associated with the policy
     * @return a new <code>AttributeAssignmentExpression</code> constructed by parsing
     * @throws ParsingException if the DOM node is invalid
     */
    public static AttributeAssignmentExpression getInstance(Node root, PolicyMetaData metaData)
            throws ParsingException {

        URI attributeId;
        URI category = null;
        String issuer= null;
        Expression expression = null;

        if (!DOMHelper.getLocalName(root).equals("AttributeAssignmentExpression")) {
            throw new ParsingException("ObligationExpression object cannot be created "
                    + "with root node of type: " + DOMHelper.getLocalName(root));
        }

        NamedNodeMap nodeAttributes = root.getAttributes();

        try {
            attributeId = new URI(nodeAttributes.getNamedItem("AttributeId").getNodeValue());
        } catch (Exception e) {
            throw new ParsingException("Error parsing required AttributeId in " +
                    "AttributeAssignmentExpressionType", e);
        }

        try {
            Node categoryNode = nodeAttributes.getNamedItem("Category");
            if(categoryNode != null){
                category = new URI(categoryNode.getNodeValue());
            }

            Node issuerNode = nodeAttributes.getNamedItem("Issuer");
            if(issuerNode != null){
                issuer = issuerNode.getNodeValue();
            }
        } catch (Exception e) {
            throw new ParsingException("Error parsing optional attributes in " +
                    "AttributeAssignmentExpressionType", e);
        }

        NodeList children = root.getChildNodes();

        // there can be only one expression  TODO  error when more than one expression
        for(int i = 0; i < children.getLength(); i ++){
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                expression = ExpressionHandler.parseExpression(children.item(i), metaData, null);
                break;
            }
        }

        if(expression == null){
            throw new ParsingException("AttributeAssignmentExpression must contain at least one " +
                    "Expression Type");
        }

        return new AttributeAssignmentExpression(attributeId, category, expression, issuer);
    }

    /**
     * evaluates <code>Expression</code> element and create new <code>Set</code> of
     * <code>AttributeAssignment</code>
     *
     * @param ctx  <code>EvaluationCtx</code>
     * @return <code>Set</code> of <code>AttributeAssignment</code>
     */
    public Set<AttributeAssignment> evaluate(EvaluationCtx ctx) {
        
        Set<AttributeAssignment> values = new HashSet<AttributeAssignment>();
        EvaluationResult result = ((Evaluatable)expression).evaluate(ctx);

        if(result == null || result.indeterminate()){
            return null;
        }
        // TODO when indetermine  policy also must be indetermine
        AttributeValue attributeValue = result.getAttributeValue();

        if(attributeValue != null){
            if(attributeValue.isBag()) {
                if(((BagAttribute)attributeValue).size() > 0 ){
                    Iterator iterator = ((BagAttribute)attributeValue).iterator();
                    while(iterator.hasNext()){
                        AttributeValue bagValue = (AttributeValue) iterator.next();
                        AttributeAssignment assignment =
                                new AttributeAssignment(attributeId, bagValue.getType(), category,
                                                                        bagValue.encode(), issuer);

                        values.add(assignment);
                    }
                } else {
                    return null;
                }
            } else {
                AttributeAssignment assignment =
                        new AttributeAssignment(attributeId, attributeValue.getType(),
                                                category, attributeValue.encode(), issuer);
                values.add(assignment);
            }
        }

        return values;
    }

    /**
     * Encodes this <code>AttributeAssignmentExpression</code> into its XML form and writes this out to the provided
     * <code>StringBuilder</code>
     *
     * @param builder string stream into which the XML-encoded data is written
     */
    public void encode(StringBuilder builder){

        builder.append("<AttributeAssignmentExpression AttributeId=\"" + attributeId + "\"");

        if(category != null){
            builder.append(" Category=\"" + category + "\"");
        }
        if(issuer != null){
            builder.append(" Issuer=\"" + issuer + "\"");
        }
        builder.append(" >\n");

        expression.encode(builder);

        builder.append("</AttributeAssignmentExpression>\n");
    }
}
