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

package org.webpki.android.org.apache.xerces.impl.dtd.models;

import org.webpki.android.org.apache.xerces.xni.QName;

/**
 * @xerces.internal
 * 
 * @version $Id: ContentModelValidator.java 446752 2006-09-15 21:55:19Z mrglavas $
 */
public interface ContentModelValidator {

    //
    // Methods
    //

    /**
     * validate
     * 
     * @param children 
     * @param offset 
     * @param length 
     * 
     * @return The value -1 if fully valid, else the 0 based index of the child
     *         that first failed. If the value returned is equal to the number
     *         of children, then the specified children are valid but additional
     *         content is required to reach a valid ending state.
     */
    public int validate(QName[] children, int offset, int length);

} // interface ContentModelValidator
