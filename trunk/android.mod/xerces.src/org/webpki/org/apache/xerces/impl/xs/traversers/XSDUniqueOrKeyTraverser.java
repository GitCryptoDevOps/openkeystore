/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webpki.org.apache.xerces.impl.xs.traversers;

import org.webpki.org.apache.xerces.impl.xs.SchemaGrammar;
import org.webpki.org.apache.xerces.impl.xs.SchemaSymbols;
import org.webpki.org.apache.xerces.impl.xs.XSElementDecl;
import org.webpki.org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.webpki.org.apache.xerces.impl.xs.identity.UniqueOrKey;
import org.webpki.org.apache.xerces.util.DOMUtil;
import org.w3c.dom.Element;

/**
 * This class contains code that is used to traverse both <key>s and
 * <unique>s.
 *
 * @xerces.internal 
 *
 * @author Neil Graham, IBM
 * @version $Id: XSDUniqueOrKeyTraverser.java 446725 2006-09-15 20:40:10Z mrglavas $
 */
class XSDUniqueOrKeyTraverser extends XSDAbstractIDConstraintTraverser {

    public XSDUniqueOrKeyTraverser (XSDHandler handler,
                                  XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }


    void traverse(Element uElem, XSElementDecl element,
            XSDocumentInfo schemaDoc, SchemaGrammar grammar) {

        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(uElem, false, schemaDoc);

        // create identity constraint
        String uName = (String)attrValues[XSAttributeChecker.ATTIDX_NAME];

        if(uName == null){
            reportSchemaError("s4s-att-must-appear", new Object [] {DOMUtil.getLocalName(uElem) , SchemaSymbols.ATT_NAME }, uElem);
            //return this array back to pool
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return;
        }

        UniqueOrKey uniqueOrKey = null;
        if(DOMUtil.getLocalName(uElem).equals(SchemaSymbols.ELT_UNIQUE)) {
            uniqueOrKey = new UniqueOrKey(schemaDoc.fTargetNamespace, uName, element.fName, IdentityConstraint.IC_UNIQUE);
        } else {
            uniqueOrKey = new UniqueOrKey(schemaDoc.fTargetNamespace, uName, element.fName, IdentityConstraint.IC_KEY);
        }
        // it's XSDElementTraverser's job to ensure that there's no
        // duplication (or if there is that restriction is involved
        // and there's identity).

        // get selector and fields
        traverseIdentityConstraint(uniqueOrKey, uElem, schemaDoc, attrValues);

        // and stuff this in the grammar
        grammar.addIDConstraintDecl(element, uniqueOrKey);

        // and fix up attributeChecker
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
    } // traverse(Element,XSDElementDecl,XSDocumentInfo, SchemaGrammar)
} // XSDUniqueOrKeyTraverser
