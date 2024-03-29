/*
 *  Copyright 2006-2013 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.webpki.wasp;

import java.io.IOException;

import java.util.GregorianCalendar;

import java.security.cert.X509Certificate;

import org.webpki.xml.DOMReaderHelper;
import org.webpki.xml.DOMWriterHelper;
import org.webpki.xml.DOMAttributeReaderHelper;

import org.webpki.util.ArrayUtil;

import org.webpki.xmldsig.XMLSignatureWrapper;
import org.webpki.xmldsig.XMLVerifier;
import org.webpki.xmldsig.SignedKeyInfoSpecifier;

import org.webpki.crypto.CertificateFilter;

import org.webpki.crypto.VerifierInterface;

import static org.webpki.wasp.WASPConstants.*;


public class AuthenticationResponseDecoder extends AuthenticationResponse
  {
    // Attributes
    GregorianCalendar server_time;

    private GregorianCalendar client_time;

    private XMLSignatureWrapper signature;

    X509Certificate[] signer_certpath;


    public String getSubmitURL ()
      {
        return submit_url;
      }
 
    
    public String getRequestURL ()
      {
        return request_url;
      }
 
    
    public GregorianCalendar getClientTime ()
      {
        return client_time;
      }


    public String getID ()
      {
        return id;
      }


    public ClientPlatformFeature[] getClientPlatformFeatures ()
      {
        return client_platform_features.toArray (new ClientPlatformFeature[0]);
      }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // XML Reader
    /////////////////////////////////////////////////////////////////////////////////////////////

    protected void fromXML (DOMReaderHelper rd) throws IOException
      {
        DOMAttributeReaderHelper ah = rd.getAttributeHelper ();
        //////////////////////////////////////////////////////////////////////////
        // Get the top-level attributes
        //////////////////////////////////////////////////////////////////////////
        id = ah.getString (ID_ATTR);

        server_time = ah.getDateTime (SERVER_TIME_ATTR);

        submit_url = ah.getString (SUBMIT_URL_ATTR);

        request_url = ah.getString (REQUEST_URL_ATTR);

        client_time = ah.getDateTime (CLIENT_TIME_ATTR);

        server_certificate_fingerprint = ah.getBinaryConditional (SERVER_CERT_FP_ATTR);

        rd.getChild();
        
        //////////////////////////////////////////////////////////////////////////
        // Get the optional clien platform features
        //////////////////////////////////////////////////////////////////////////
        while (rd.hasNext (ClientPlatformFeature.CLIENT_PLATFORM_FEATURE_ELEM))
          {
            client_platform_features.add (ClientPlatformFeature.read (rd));
          }

        //////////////////////////////////////////////////////////////////////////
        // Get the sole child element
        //////////////////////////////////////////////////////////////////////////
        signature = (XMLSignatureWrapper) wrap (rd.getNext ());
      }

    protected void toXML (DOMWriterHelper helper) throws IOException
      {
        throw new IOException ("Should NEVER be called");
      }

    public void verifySignature (VerifierInterface verifier) throws IOException
      {
        XMLVerifier ds = new XMLVerifier (verifier);
        ds.setSignedKeyInfo (SignedKeyInfoSpecifier.ALLOW_SIGNED_KEY_INFO);
        ds.validateEnvelopedSignature (this, null, signature, id);
        signer_certpath = verifier.getSignerCertificatePath ();
      }
  }
