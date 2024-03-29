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

package org.webpki.android.org.apache.xerces.impl.dv;

/**
 * Datatype exception for invalid facet. This exception is only used by
 * schema datatypes.
 *
 * @xerces.internal 
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id: InvalidDatatypeFacetException.java 446751 2006-09-15 21:54:06Z mrglavas $
 */
public class InvalidDatatypeFacetException extends DatatypeException {

    /** Serialization version. */
    static final long serialVersionUID = -4104066085909970654L;
    
    /**
     * Create a new datatype exception by providing an error code and a list
     * of error message substitution arguments.
     *
     * @param key  error code
     * @param args error arguments
     */
    public InvalidDatatypeFacetException(String key, Object[] args) {
        super(key, args);
    }

}
