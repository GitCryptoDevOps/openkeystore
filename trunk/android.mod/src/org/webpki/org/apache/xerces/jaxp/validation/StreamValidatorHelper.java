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

package org.webpki.org.apache.xerces.jaxp.validation;

import java.lang.ref.SoftReference;
import java.util.Locale;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.webpki.org.apache.xerces.impl.Constants;
import org.webpki.org.apache.xerces.impl.XMLErrorReporter;
import org.webpki.org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.webpki.org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.webpki.org.apache.xerces.parsers.XML11Configuration;
import org.webpki.org.apache.xerces.xni.XNIException;
import org.webpki.org.apache.xerces.xni.parser.XMLInputSource;
import org.webpki.org.apache.xerces.xni.parser.XMLParseException;
import org.webpki.org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.xml.sax.SAXException;

/**
 * <p>A validator helper for <code>StreamSource</code>s.</p>
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id: StreamValidatorHelper.java 542521 2007-05-29 13:58:42Z mrglavas $
 */
final class StreamValidatorHelper implements ValidatorHelper {
    
    // feature identifiers
    
    /** Feature identifier: parser settings. */
    private static final String PARSER_SETTINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;    
    
    // property identifiers
    
    /** Property identifier: entity resolver. */
    private static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    
    /** Property identifier: error handler. */
    private static final String ERROR_HANDLER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    
    /** Property identifier: error reporter. */
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    
    /** Property identifier: XML Schema validator. */
    private static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    
    /** Property identifier: symbol table. */
    private static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    
    /** Property identifier: validation manager. */
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    
    //
    // Data
    //
    
    /** SoftReference to parser configuration. **/
    private SoftReference fConfiguration = new SoftReference(null);
    
    /** Schema validator. **/
    private final XMLSchemaValidator fSchemaValidator;
    
    /** Component manager. **/
    private final XMLSchemaValidatorComponentManager fComponentManager;

    public StreamValidatorHelper(XMLSchemaValidatorComponentManager componentManager) {
        fComponentManager = componentManager;
        fSchemaValidator = (XMLSchemaValidator) fComponentManager.getProperty(SCHEMA_VALIDATOR);
    }

    public void validate(Source source, Result result) 
        throws SAXException, IOException {
        if (result == null) {
            final StreamSource streamSource = (StreamSource) source;
            XMLInputSource input = new XMLInputSource(streamSource.getPublicId(), streamSource.getSystemId(), null);
            input.setByteStream(streamSource.getInputStream());
            input.setCharacterStream(streamSource.getReader());
            
            // Gets the parser configuration. We'll create and initialize a new one, if we 
            // haven't created one before or if the previous one was garbage collected.
            XMLParserConfiguration config = (XMLParserConfiguration) fConfiguration.get();
            if (config == null) {
                config = initialize();
            }
            // If settings have changed on the component manager, refresh the error handler and entity resolver.
            else if (fComponentManager.getFeature(PARSER_SETTINGS)) {
                config.setProperty(ENTITY_RESOLVER, fComponentManager.getProperty(ENTITY_RESOLVER));
                config.setProperty(ERROR_HANDLER, fComponentManager.getProperty(ERROR_HANDLER));
            }
            
            // prepare for parse
            fComponentManager.reset();
            fSchemaValidator.setDocumentHandler(null);
            
            try {
                config.parse(input);
            }
            catch (XMLParseException e) {
                throw Util.toSAXParseException(e);
            }
            catch (XNIException e) {
                throw Util.toSAXException(e);
            }
            return;
        }
        throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(Locale.getDefault(), 
                "SourceResultMismatch", 
                new Object [] {source.getClass().getName(), result.getClass().getName()}));
    }
    
    private XMLParserConfiguration initialize() {
        XML11Configuration config = new XML11Configuration();
        config.setProperty(ENTITY_RESOLVER, fComponentManager.getProperty(ENTITY_RESOLVER));
        config.setProperty(ERROR_HANDLER, fComponentManager.getProperty(ERROR_HANDLER));
        XMLErrorReporter errorReporter = (XMLErrorReporter) fComponentManager.getProperty(ERROR_REPORTER);
        config.setProperty(ERROR_REPORTER, errorReporter);
        // add message formatters
        if (errorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            errorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            errorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }
        config.setProperty(SYMBOL_TABLE, fComponentManager.getProperty(SYMBOL_TABLE));
        config.setProperty(VALIDATION_MANAGER, fComponentManager.getProperty(VALIDATION_MANAGER));
        config.setDocumentHandler(fSchemaValidator);
        config.setDTDHandler(null);
        config.setDTDContentModelHandler(null);
        fConfiguration = new SoftReference(config);
        return config;
    }

} // StreamValidatorHelper