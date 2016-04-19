/*
 * @(#)StandardCombiningAlgFactory.java
 *
 * Copyright 2004-2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package com.connexta.arbitro.combine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.connexta.arbitro.XACMLConstants;
import com.connexta.arbitro.combine.xacml2.DenyOverridesPolicyAlg;
import com.connexta.arbitro.combine.xacml2.FirstApplicablePolicyAlg;
import com.connexta.arbitro.combine.xacml2.FirstApplicableRuleAlg;
import com.connexta.arbitro.combine.xacml2.OnlyOneApplicablePolicyAlg;
import com.connexta.arbitro.combine.xacml2.OrderedDenyOverridesRuleAlg;
import com.connexta.arbitro.combine.xacml2.PermitOverridesPolicyAlg;
import com.connexta.arbitro.combine.xacml3.DenyOverridesRuleAlg;
import com.connexta.arbitro.combine.xacml3.OrderedDenyOverridesPolicyAlg;
import com.connexta.arbitro.combine.xacml3.OrderedPermitOverridesPolicyAlg;
import com.connexta.arbitro.combine.xacml3.OrderedPermitOverridesRuleAlg;
import com.connexta.arbitro.combine.xacml3.PermitOverridesRuleAlg;
import com.connexta.arbitro.UnknownIdentifierException;
import com.connexta.arbitro.combine.xacml3.DenyUnlessPermitPolicyAlg;
import com.connexta.arbitro.combine.xacml3.DenyUnlessPermitRuleAlg;
import com.connexta.arbitro.combine.xacml3.PermitUnlessDenyPolicyAlg;
import com.connexta.arbitro.combine.xacml3.PermitUnlessDenyRuleAlg;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This factory supports the standard set of algorithms specified in XACML 1.x and 2.0. It is the
 * default factory used by the system, and imposes a singleton pattern insuring that there is only
 * ever one instance of this class.
 * <p>
 * Note that because this supports only the standard algorithms, this factory does not allow the
 * addition of any other algorithms. If you call <code>addAlgorithm</code> on an instance of this
 * class, an exception will be thrown. If you need a standard factory that is modifiable, you should
 * create a new <code>BaseCombiningAlgFactory</code> (or some other <code>CombiningAlgFactory</code>
 * ) and configure it with the standard algorithms using <code>getStandardAlgorithms</code> (or, in
 * the case of <code>BaseAttributeFactory</code>, by providing the datatypes in the constructor).
 * 
 * @since 1.2
 * @author Seth Proctor
 */
public class StandardCombiningAlgFactory extends BaseCombiningAlgFactory {

    // the single factory instance
    private static volatile StandardCombiningAlgFactory factoryInstance = null;

    // the algorithms supported by this factory
    private static Set supportedAlgorithms = null;

    // identifiers for the supported algorithms
    private static Set supportedAlgIds;

    // the logger we'll use for all messages
    private static Log logger = LogFactory.getLog(StandardCombiningAlgFactory.class);

    /**
     * Default constructor.
     */
    private StandardCombiningAlgFactory() {
        super(supportedAlgorithms);
    }

    /**
     * Private initializer for the supported algorithms. This isn't called until something needs
     * these values, and is only called once.
     */
    private static void initAlgorithms() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing standard combining algorithms");
        }

        supportedAlgorithms = new HashSet();
        supportedAlgIds = new HashSet();

        supportedAlgorithms.add(new com.connexta.arbitro.combine.xacml2.DenyOverridesRuleAlg());
        supportedAlgIds.add(com.connexta.arbitro.combine.xacml2.DenyOverridesRuleAlg.algId);
        supportedAlgorithms.add(new DenyOverridesPolicyAlg());
        supportedAlgIds.add(DenyOverridesPolicyAlg.algId);

        supportedAlgorithms.add(new OrderedDenyOverridesRuleAlg());
        supportedAlgIds.add(OrderedDenyOverridesRuleAlg.algId);
        supportedAlgorithms.add(new com.connexta.arbitro.combine.xacml2.OrderedDenyOverridesPolicyAlg());
        supportedAlgIds.add(com.connexta.arbitro.combine.xacml2.OrderedDenyOverridesPolicyAlg.algId);

        supportedAlgorithms.add(new com.connexta.arbitro.combine.xacml2.PermitOverridesRuleAlg());
        supportedAlgIds.add(com.connexta.arbitro.combine.xacml2.PermitOverridesRuleAlg.algId);
        supportedAlgorithms.add(new PermitOverridesPolicyAlg());
        supportedAlgIds.add(PermitOverridesPolicyAlg.algId);

        supportedAlgorithms.add(new com.connexta.arbitro.combine.xacml2.OrderedPermitOverridesRuleAlg());
        supportedAlgIds.add(com.connexta.arbitro.combine.xacml2.OrderedPermitOverridesRuleAlg.algId);
        supportedAlgorithms.add(new com.connexta.arbitro.combine.xacml2.OrderedPermitOverridesPolicyAlg());
        supportedAlgIds.add(com.connexta.arbitro.combine.xacml2.OrderedPermitOverridesPolicyAlg.algId);

        supportedAlgorithms.add(new FirstApplicableRuleAlg());
        supportedAlgIds.add(FirstApplicableRuleAlg.algId);
        supportedAlgorithms.add(new FirstApplicablePolicyAlg());
        supportedAlgIds.add(FirstApplicablePolicyAlg.algId);

        supportedAlgorithms.add(new OnlyOneApplicablePolicyAlg());
        supportedAlgIds.add(OnlyOneApplicablePolicyAlg.algId);

        // XACML 3.0

        supportedAlgorithms.add(new DenyUnlessPermitRuleAlg());
        supportedAlgIds.add(DenyUnlessPermitRuleAlg.algId);
        supportedAlgorithms.add(new DenyUnlessPermitPolicyAlg());
        supportedAlgIds.add(DenyUnlessPermitPolicyAlg.algId);

        supportedAlgorithms.add(new PermitUnlessDenyRuleAlg());
        supportedAlgIds.add(PermitUnlessDenyRuleAlg.algId);
        supportedAlgorithms.add(new PermitUnlessDenyPolicyAlg());
        supportedAlgIds.add(PermitUnlessDenyPolicyAlg.algId);

        supportedAlgorithms.add(new DenyOverridesRuleAlg());
        supportedAlgIds.add(DenyOverridesRuleAlg.algId);
        supportedAlgorithms.add(new com.connexta.arbitro.combine.xacml3.DenyOverridesPolicyAlg());
        supportedAlgIds.add(com.connexta.arbitro.combine.xacml3.DenyOverridesPolicyAlg.algId);

        supportedAlgorithms.add(new com.connexta.arbitro.combine.xacml3.OrderedDenyOverridesRuleAlg());
        supportedAlgIds.add(com.connexta.arbitro.combine.xacml3.OrderedDenyOverridesRuleAlg.algId);
        supportedAlgorithms.add(new OrderedDenyOverridesPolicyAlg());
        supportedAlgIds.add(OrderedDenyOverridesPolicyAlg.algId);

        supportedAlgorithms.add(new PermitOverridesRuleAlg());
        supportedAlgIds.add(PermitOverridesRuleAlg.algId);
        supportedAlgorithms.add(new com.connexta.arbitro.combine.xacml3.PermitOverridesPolicyAlg());
        supportedAlgIds.add(com.connexta.arbitro.combine.xacml3.PermitOverridesPolicyAlg.algId);

        supportedAlgorithms.add(new OrderedPermitOverridesRuleAlg());
        supportedAlgIds.add(OrderedPermitOverridesRuleAlg.algId);
        supportedAlgorithms.add(new OrderedPermitOverridesPolicyAlg());
        supportedAlgIds.add(OrderedPermitOverridesPolicyAlg.algId);

        supportedAlgIds = Collections.unmodifiableSet(supportedAlgIds);
    }

    /**
     * Returns an instance of this factory. This method enforces a singleton model, meaning that
     * this always returns the same instance, creating the factory if it hasn't been requested
     * before. This is the default model used by the <code>CombiningAlgFactory</code>, ensuring
     * quick access to this factory.
     * 
     * @return the factory instance
     */
    public static StandardCombiningAlgFactory getFactory() {
        if (factoryInstance == null) {
            synchronized (StandardCombiningAlgFactory.class) {
                if (factoryInstance == null) {
                    initAlgorithms();
                    factoryInstance = new StandardCombiningAlgFactory();
                }
            }
        }

        return factoryInstance;
    }

    /**
     * A convenience method that returns a new instance of a <code>CombiningAlgFactory</code> that
     * supports all of the standard algorithms. The new factory allows adding support for new
     * algorithms. This method should only be used when you need a new, mutable instance (eg, when
     * you want to create a new factory that extends the set of supported algorithms). In general,
     * you should use <code>getFactory</code> which is more efficient and enforces a singleton
     * pattern.
     * 
     * @return a new factory supporting the standard algorithms
     */
    public static CombiningAlgFactory getNewFactory() {
        // first we make sure everything's been initialized...
        getFactory();

        // ...then we create the new instance
        return new BaseCombiningAlgFactory(supportedAlgorithms);
    }

    /**
     * Returns the identifiers supported for the given version of XACML. Because this factory
     * supports identifiers from all versions of the XACML specifications, this method is useful for
     * getting a list of which specific identifiers are supported by a given version of XACML.
     * 
     * @param xacmlVersion a standard XACML identifier string, as provided in
     *            <code>PolicyMetaData</code>
     * 
     * @return a <code>Set</code> of identifiers
     * 
     * @throws UnknownIdentifierException if the version string is unknown
     */
    public static Set getStandardAlgorithms(String xacmlVersion) throws UnknownIdentifierException {
        if (xacmlVersion.equals(XACMLConstants.XACML_1_0_IDENTIFIER)
                || xacmlVersion.equals(XACMLConstants.XACML_2_0_IDENTIFIER) ||
                xacmlVersion.equals(XACMLConstants.XACML_3_0_IDENTIFIER)){
            return supportedAlgIds;
        }

        throw new UnknownIdentifierException("Unknown XACML version: " + xacmlVersion);
    }

    /**
     * Throws an <code>UnsupportedOperationException</code> since you are not allowed to modify what
     * a standard factory supports.
     * 
     * @param alg the combining algorithm to add
     * 
     * @throws UnsupportedOperationException always
     */
    public void addAlgorithm(CombiningAlgorithm alg) {
        throw new UnsupportedOperationException("a standard factory cannot "
                + "support new algorithms");
    }

}
