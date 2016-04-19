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

package com.connexta.arbitro.ctx.xacml3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.connexta.arbitro.ObligationResult;
import com.connexta.arbitro.ParsingException;
import com.connexta.arbitro.PolicyReference;
import com.connexta.arbitro.XACMLConstants;
import com.connexta.arbitro.ctx.AbstractResult;
import com.connexta.arbitro.ctx.Attribute;
import com.connexta.arbitro.xacml3.Advice;
import com.connexta.arbitro.xacml3.Attributes;
import com.connexta.arbitro.xacml3.Obligation;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.connexta.arbitro.DOMHelper;
import com.connexta.arbitro.ctx.EvaluationCtx;
import com.connexta.arbitro.ctx.Status;

/**
 * XACML 3 implementation of  <code>AbstractResult</code>
 */
public class Result extends AbstractResult {

    /**
     * Set of applicable policy references
     */
    Set<PolicyReference> policyReferences;

    /**
     * Set of attributes that returns to PEP. mainly used in multiple decision profile 
     */
    Set<Attributes> attributes;
    
    public Result(int decision, Status status){
        super(decision, status, XACMLConstants.XACML_VERSION_3_0);
    }

    /**
     *
     * @param decision The decision contained within the result
     * @param status The status of the result
     * @param obligationResults the List of ObligationResults
     * @param advices the advices within the result
     * @param evaluationCtx The evaluationCtx to be used
     * @throws IllegalArgumentException if the arguments are malformed
     */
    public Result(int decision, Status status, List<ObligationResult> obligationResults,
                  List<Advice> advices, EvaluationCtx evaluationCtx) throws IllegalArgumentException {
        super(decision, status, obligationResults, advices, XACMLConstants.XACML_VERSION_3_0);
        if(evaluationCtx != null){
            XACML3EvaluationCtx ctx = (XACML3EvaluationCtx) evaluationCtx;
            this.policyReferences = ctx.getPolicyReferences();
            processAttributes(ctx.getAttributesSet());
        }
    }

    /**
     *
     * @param decision The decision contained within the result
     * @param status The status of the result
     * @param obligationResults the List of ObligationResults
     * @param advices the advices within the result
     * @param policyReferences The policy references
     * @param attributes the attributes to be used
     * @throws IllegalArgumentException if the arguments are malformed
     */
    public Result(int decision, Status status, List<ObligationResult> obligationResults,
                  List<Advice> advices, Set<PolicyReference> policyReferences, Set<Attributes> attributes)
                                                                throws IllegalArgumentException {
        super(decision, status, obligationResults, advices, XACMLConstants.XACML_VERSION_3_0);
        this.policyReferences = policyReferences;
        processAttributes(attributes);
    }
    /**
     * Creates a new instance of a <code>Result</code> based on the given
     * DOM root node. A <code>ParsingException</code> is thrown if the DOM
     * root doesn't represent a valid ResultType.
     *
     * @param root the DOM root of a ResultType
     *
     * @return a new <code>Result</code>
     *
     * @throws ParsingException if the node is invalid
     */
    public static AbstractResult getInstance(Node root) throws ParsingException {

        int decision = -1;
        Status status = null;
        List<ObligationResult> obligations = null;
        List<Advice> advices = null;
        Set<PolicyReference> policyReferences = null;
        Set<Attributes>  attributes = null;

        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String name = DOMHelper.getLocalName(node);

            if (name.equals("Decision")) {
                String type = node.getFirstChild().getNodeValue();
                for (int j = 0; j < DECISIONS.length; j++) {
                    if (DECISIONS[j].equals(type)) {
                        decision = j;
                        break;
                    }
                }

                if (decision == -1){
                    throw new ParsingException("Unknown Decision: " + type);
                }
            } else if (name.equals("Status")) {
                if(status == null){
                    status = Status.getInstance(node);
                } else {
                    throw new ParsingException("More than one StatusType defined");
                }
            } else if (name.equals("Obligations")) {
                if(obligations == null){
                    obligations = parseObligations(node);
                } else {
                    throw new ParsingException("More than one ObligationsType defined");
                }
            } else if (name.equals("AssociatedAdvice")) {
                if(advices == null){
                    advices = parseAdvices(node);
                } else {
                    throw new ParsingException("More than one AssociatedAdviceType defined"); 
                }
            } else if (name.equals("PolicyIdentifierList")){
                if(policyReferences == null){
                    policyReferences = parsePolicyReferences(node);
                } else {
                    throw new ParsingException("More than one PolicyIdentifierListType defined"); 
                }
            } else if(name.equals("Attributes")){
                if(attributes == null){
                    attributes = new HashSet<Attributes>();
                }
                attributes.add(Attributes.getInstance(node));    
            }
        }

        return new Result(decision, status, obligations, advices, policyReferences, attributes);
    }

    /**
     * Helper method that handles the obligations
     *
     * @param root the DOM root of the ObligationsType XML type
     * @return a <code>List</code> of <code>ObligationResult</code>
     * @throws ParsingException  if any issues in parsing DOM
     */
    private static List<ObligationResult> parseObligations(Node root) throws ParsingException {

        List<ObligationResult> list = new ArrayList<ObligationResult>();

        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (DOMHelper.getLocalName(node).equals("Obligation")){
                list.add(Obligation.getInstance(node));
            }
        }

        if (list.size() == 0) {
            throw new ParsingException("ObligationsType must not be empty");
        }
        return list;
    }

    /**
     * Helper method that handles the Advices
     * 
     * @param root the DOM root of the AssociatedAdviceType XML type
     * @return a <code>List</code> of <code>Advice</code>
     * @throws ParsingException  if any issues in parsing DOM
     */
    private static List<Advice> parseAdvices(Node root) throws ParsingException {

        List<Advice> list = new ArrayList<Advice>();

        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (DOMHelper.getLocalName(node).equals("Advice")){
                list.add(Advice.getInstance(node));
            }
        }

        if (list.size() == 0) {
            throw new ParsingException("AssociatedAdviceType must not be empty");
        }
        return list;
    }

    /**
     * Helper method that handles the Advices
     *
     * @param root the DOM root of the PolicyIdentifierListType XML type
     * @return a <code>Set</code> of <code>PolicyReference</code>
     * @throws ParsingException  if any issues in parsing DOM
     */
    private static Set<PolicyReference> parsePolicyReferences(Node root) throws ParsingException {

        Set<PolicyReference> set = new HashSet<PolicyReference>();

        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            set.add(PolicyReference.getInstance(node, null, null));
        }
        
        return set;
    }

    /**
     * Return set of attributes that is sent to PEP
     *
     * @return set of attributes
     */
    public Set<Attributes> getAttributes() {
        return attributes;
    }

    /**
     * Extract the attributes that must be included in the response
     *
     * @param attributesSet  a <code>Set</code> of <code>Attributes</code>
     */
    public void processAttributes(Set<Attributes> attributesSet){

        if(attributesSet == null){
            return;
        }

        Set<Attributes> newSet = new HashSet<Attributes>();

        for(Attributes attributes : attributesSet){
            Set<Attribute> attributeSet = attributes.getAttributes();
            if(attributeSet == null){
                continue;
            }
            Set<Attribute> newAttributeSet = new HashSet<Attribute>();
            for(Attribute attribute : attributeSet){
                if(attribute.isIncludeInResult()){
                    newAttributeSet.add(attribute);
                }
            }

            if(newAttributeSet.size() > 0){
                Attributes newAttributes = new Attributes(attributes.getCategory(),
                                    attributes.getContent(), newAttributeSet, attributes.getId());
                newSet.add(newAttributes);
            }
        }

        this.attributes = newSet;
    }

    /**
     * Encodes this <code>Result</code> into its XML form and writes this out to the provided
     * <code>StringBuilder</code>
     *
     * @param builder string stream into which the XML-encoded data is written
     */
    public void encode(StringBuilder builder) {

        builder.append("<Result>");
        // encode the decision
        //check whether decision is extended indeterminate values
        if(decision == 4 || decision == 5 || decision == 6){
            // if this is extended indeterminate values, we just return the "Indeterminate"
            builder.append("<Decision>").append(DECISIONS[2]).append("</Decision>");
        } else {
            builder.append("<Decision>").append(DECISIONS[decision]).append("</Decision>");
        }
        // encode the status
        if (status != null){
            status.encode(builder);
        }
        // encode the obligations
        if (obligations != null  && obligations.size() != 0) {

            builder.append("<Obligations>");

            Iterator it = obligations.iterator();
            while (it.hasNext()) {
                Obligation obligation = (Obligation) (it.next());
                obligation.encode(builder);
            }
            builder.append("</Obligations>");
        }

        // encode the advices
        if (advices != null  && advices.size() != 0) {

            builder.append("<AssociatedAdvice>");

            Iterator it = advices.iterator();

            while (it.hasNext()) {
                Advice advice = (Advice) (it.next());
                advice.encode(builder);
            }
            builder.append("</AssociatedAdvice>");
        }

        // encode the policy, policySet references
        if (policyReferences != null  && policyReferences.size() != 0) {
            builder.append("<PolicyIdentifierList>");

            for(PolicyReference reference : policyReferences){
                reference.encode(builder);
            }

            builder.append("</PolicyIdentifierList>");
        }

        // encode the attributes
        if (attributes != null  && attributes.size() != 0) {
            for(Attributes attribute : attributes){
                attribute.encode(builder);
            }
        }

        // finish it off
        builder.append("</Result>");
    }

}
