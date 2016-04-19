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

package com.connexta.arbitro.ctx;

import java.util.List;

import com.connexta.arbitro.ObligationResult;
import com.connexta.arbitro.XACMLConstants;
import com.connexta.arbitro.ctx.xacml2.Result;
import com.connexta.arbitro.xacml3.Advice;

/**
 * Factory that creates the AbstractResult
 */
public class ResultFactory {

    private static volatile ResultFactory factoryInstance;

    /**
     * Returns instance of <code>AbstractResult</code> based one the XACML version.
     * Constructs a <code>AbstractResult</code> object with decision and evaluation ctx
     *
     * @param decision decision the decision effect to include in this result.
     * @param status the <code>Status</code> to include in this result
     * @param version XACML request version
     * @return <code>AbstractResult</code> object
     */
    public AbstractResult getResult(int decision, Status status, int version) {

        if(version == XACMLConstants.XACML_VERSION_3_0){
            return new com.connexta.arbitro.ctx.xacml3.Result(decision, status, null, null, null);
        } else {
            return new Result(decision, status);
        }
    }

    /**
     * Returns instance of <code>AbstractResult</code> based one the XACML version. 
     * Constructs a <code>AbstractResult</code> object with decision and evaluation ctx
     * 
     * @param decision decision the decision effect to include in this result.
     * @param evaluationCtx context of a single policy evaluation
     * @return <code>AbstractResult</code> object
     */
    public AbstractResult getResult(int decision, EvaluationCtx evaluationCtx) {

        if(evaluationCtx.getXacmlVersion() == XACMLConstants.XACML_VERSION_3_0){
            return new com.connexta.arbitro.ctx.xacml3.Result(decision, null, null, null,evaluationCtx);
        } else {
            return new Result(decision, null);
        }
    }

    /**
     * Returns instance of <code>AbstractResult</code> based one the XACML version.
     * Constructs a <code>AbstractResult</code> object with decision and evaluation ctx
     *
     * @param decision decision the decision effect to include in this result.
     * @param status the <code>Status</code> to include in this result
     * @param evaluationCtx context of a single policy evaluation
     * @return <code>AbstractResult</code> object
     */
    public AbstractResult getResult(int decision, Status status, EvaluationCtx evaluationCtx) {

        if(evaluationCtx.getXacmlVersion() == XACMLConstants.XACML_VERSION_3_0){
            return new com.connexta.arbitro.ctx.xacml3.Result(decision, status, null, null, evaluationCtx);
        } else {
            return new Result(decision, status);
        }
    }

    /**
     * Returns instance of <code>AbstractResult</code> based one the XACML version.
     * Constructs a <code>AbstractResult</code> object with decision and evaluation ctx
     *
     * @param decision decision the decision effect to include in this result.
     * @param obligationResults a list of <code>ObligationResult</code> objects
     * @param advices a list of <code>Advice</code> objects
     * @param evaluationCtx context of a single policy evaluation
     * @return <code>AbstractResult</code> object
     */
    public AbstractResult getResult(int decision, List<ObligationResult> obligationResults,
                                            List<Advice> advices,  EvaluationCtx evaluationCtx) {

        if(evaluationCtx.getXacmlVersion() == XACMLConstants.XACML_VERSION_3_0){
            return new com.connexta.arbitro.ctx.xacml3.Result(decision, null, obligationResults,
                                                                            advices, evaluationCtx);
        } else {
            return new Result(decision, null, obligationResults);
        }
    }

    /**
     * Returns instance of <code>AbstractResult</code> based one the XACML version.
     * Constructs a <code>AbstractResult</code> object with decision and evaluation ctx
     *
     * @param decision decision the decision effect to include in this result.
     * @param status the <code>Status</code> to include in this result
     * @param obligationResults a list of <code>ObligationResult</code> objects
     * @param advices a list of <code>Advice</code> objects
     * @param evaluationCtx context of a single policy evaluation
     * @return <code>AbstractResult</code> object
     */
    public AbstractResult getResult(int decision, Status status, List<ObligationResult> obligationResults,
                                            List<Advice> advices,  EvaluationCtx evaluationCtx) {

        if(evaluationCtx.getXacmlVersion() == XACMLConstants.XACML_VERSION_3_0){
            return new com.connexta.arbitro.ctx.xacml3.Result(decision, status,obligationResults,
                                                                            advices, evaluationCtx);
        } else {
            return new Result(decision, status, obligationResults);
        }
    }

    /**
     * Returns an instance of this factory. This method enforces a singleton model, meaning that
     * this always returns the same instance, creating the factory if it hasn't been requested
     * before.
     * 
     * @return the factory instance
     */
    public static ResultFactory getFactory() {
        if (factoryInstance == null) {
            synchronized (ResultFactory.class) {
                if (factoryInstance == null) {
                    factoryInstance = new ResultFactory();
                }
            }
        }
        return factoryInstance;
    }
}
