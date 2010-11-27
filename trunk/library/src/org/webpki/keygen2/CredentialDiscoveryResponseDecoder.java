/*
 *  Copyright 2006-2010 WebPKI.org (http://webpki.org).
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
package org.webpki.keygen2;

import java.io.IOException;

import java.util.Vector;

import org.webpki.xml.DOMReaderHelper;
import org.webpki.xml.DOMAttributeReaderHelper;
import org.webpki.xml.ServerCookie;

import static org.webpki.keygen2.KeyGen2Constants.*;

public class CredentialDiscoveryResponseDecoder extends CredentialDiscoveryResponse
  {
    public class MatchingCredential
      {
        MatchingCredential () {}
        
        byte[] certificate_fingerprint;
        
        String client_session_id;
        
        String server_session_id;
        
        boolean locked;
        
        public String getClientSessionID ()
          {
            return client_session_id;
          }
        
        public String getServerSessionID ()
          {
            return server_session_id;
          }
        
        public byte[] getCertificateFingerprint ()
          {
            return certificate_fingerprint;
          }
        
        public boolean isLocked ()
          {
            return locked;
          }
      }

    public class LookupResult
      {
        String id;

        LookupResult () { }
        
        Vector<MatchingCredential> matching_credentials = new Vector<MatchingCredential> ();

        LookupResult (DOMReaderHelper rd) throws IOException
          {
            DOMAttributeReaderHelper ah = rd.getAttributeHelper ();
            rd.getNext (LOOKUP_RESULT_ELEM);
            id = ah.getString (ID_ATTR);
            rd.getChild ();
            while (rd.hasNext ())
              {
                rd.getNext (MATCHING_CREDENTIAL_ELEM);
                MatchingCredential mc = new MatchingCredential ();
                mc.client_session_id = ah.getString (CLIENT_SESSION_ID_ATTR);
                mc.server_session_id = ah.getString (SERVER_SESSION_ID_ATTR);
                mc.certificate_fingerprint = ah.getBinary (CERTIFICATE_FINGERPRINT_ATTR);
                mc.locked = ah.getBooleanConditional (LOCKED_ATTR);
                matching_credentials.add (mc);
              }
            rd.getParent ();
          }


        public String getID ()
          {
            return id;
          }
        
        public MatchingCredential[] getMatchingCredentials ()
          {
            return matching_credentials.toArray (new MatchingCredential[0]);
          }
      }

    private Vector<LookupResult> lookup_results = new Vector<LookupResult> ();
    
    private String client_session_id;

    private String server_session_id;

    private ServerCookie server_cookie;                     // Optional

    
    public String getServerSessionID ()
      {
        return server_session_id;
      }


    public String getClientSessionID ()
      {
        return client_session_id;
      }


    public ServerCookie getServerCookie ()
      {
        return server_cookie;
      }


    public LookupResult[] getLookupResults ()
      {
        return lookup_results.toArray (new LookupResult[0]);
      }
    
    
    protected void fromXML (DOMReaderHelper rd) throws IOException
      {
        DOMAttributeReaderHelper ah = rd.getAttributeHelper ();

        /////////////////////////////////////////////////////////////////////////////////////////
        // Read the top level attributes
        /////////////////////////////////////////////////////////////////////////////////////////

        server_session_id = ah.getString (SERVER_SESSION_ID_ATTR);

        client_session_id = ah.getString (ID_ATTR);

        rd.getChild ();

        /////////////////////////////////////////////////////////////////////////////////////////
        // Get the lookup_results [1..n]
        /////////////////////////////////////////////////////////////////////////////////////////
        do 
          {
            LookupResult o = new LookupResult (rd);
            lookup_results.add (o);
          }
        while (rd.hasNext (LOOKUP_RESULT_ELEM));

        /////////////////////////////////////////////////////////////////////////////////////////
        // Get optional server cookie
        /////////////////////////////////////////////////////////////////////////////////////////
        if (rd.hasNext ())
          {
            server_cookie = ServerCookie.read (rd);
          }
      }
  }