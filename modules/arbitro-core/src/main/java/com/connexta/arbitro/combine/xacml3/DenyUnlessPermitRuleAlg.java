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

package com.connexta.arbitro.combine.xacml3;

import com.connexta.arbitro.ObligationResult;
import com.connexta.arbitro.combine.RuleCombiningAlgorithm;
import com.connexta.arbitro.ctx.AbstractResult;
import com.connexta.arbitro.ctx.ResultFactory;
import com.connexta.arbitro.xacml3.Advice;
import com.connexta.arbitro.Rule;
import com.connexta.arbitro.combine.RuleCombinerElement;
import com.connexta.arbitro.ctx.EvaluationCtx;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the standard Deny unless permit rule combining algorithm. This algorithm is intended for
 * those cases where a permit decision should have priority over a deny decision, 
 * and an "Indeterminate" or "NotApplicable" must never be the result.
 * It is particularly useful at the top level in a policy structure to ensure that a
 * PDP will always return a definite "Permit" or "Deny"  result.
 */
public class DenyUnlessPermitRuleAlg extends RuleCombiningAlgorithm {

    /**
     * The standard URN used to identify this algorithm
     */
    public static final String algId = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:" +
            "deny-unless-permit";

    // a URI form of the identifier
    private static URI identifierURI;
    // exception if the URI was invalid, which should never be a problem
    private static RuntimeException earlyException;

    static {
        try {
            identifierURI = new URI(algId);
        } catch (URISyntaxException se) {
            earlyException = new IllegalArgumentException();
            earlyException.initCause(se);
        }
    }

    /**
     * Standard constructor.
     */
    public DenyUnlessPermitRuleAlg() {
        super(identifierURI);

        if (earlyException != null){
            throw earlyException;
        }
    }

    /**
     * Constructor that takes the algorithm's identifier.
     *
     * @param identifier the algorithm's identifier
     */
    public DenyUnlessPermitRuleAlg(URI identifier) {
        super(identifier);
    }


    @Override
    public AbstractResult combine(EvaluationCtx context, List parameters, List ruleElements) {

        List<ObligationResult> denyObligations = new ArrayList<ObligationResult>();
        List<Advice> denyAdvices = new ArrayList<Advice>();
        
        for (Object ruleElement : ruleElements) {
            Rule rule = ((RuleCombinerElement) (ruleElement)).getRule();
            AbstractResult result = rule.evaluate(context);
            int value = result.getDecision();

            // if there was a value of PERMIT, then regardless of what else
            // we've seen, we always return PERMIT
            if (value == AbstractResult.DECISION_PERMIT) {
                return result;
            } else if(value == AbstractResult.DECISION_DENY){
                denyObligations.addAll(result.getObligations());
                denyAdvices.addAll(result.getAdvices());
            }
        }

        // if there is not any value of PERMIT. The return DENY
        return ResultFactory.getFactory().getResult(AbstractResult.DECISION_DENY, denyObligations,
                                                                            denyAdvices, context);
    }
}
