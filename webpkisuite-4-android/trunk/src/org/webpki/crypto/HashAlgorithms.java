/*
 *  Copyright 2006-2015 WebPKI.org (http://webpki.org).
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
package org.webpki.crypto;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.GeneralSecurityException;


public enum HashAlgorithms
  {
    SHA1   ("http://www.w3.org/2000/09/xmldsig#sha1",        "1.3.14.3.2.26",          "SHA-1"),
    SHA256 ("http://www.w3.org/2001/04/xmlenc#sha256",       "2.16.840.1.101.3.4.2.1", "SHA-256"),
    SHA384 ("http://www.w3.org/2001/04/xmldsig-more#sha384", "2.16.840.1.101.3.4.2.2", "SHA-384"),
    SHA512 ("http://www.w3.org/2001/04/xmlenc#sha512",       "2.16.840.1.101.3.4.2.3", "SHA-512");

    private final String sks_id;    // As (typically) expressed in protocols
    private final String oid;       // As expressed in ASN.1 messages
    private final String jcename;   // As expressed for JCE

    private HashAlgorithms (String sks_id, String oid, String jcename)
      {
        this.sks_id = sks_id;
        this.oid = oid;
        this.jcename = jcename;
      }


    public String getURI ()
      {
        return sks_id;
      }


    public String getOID ()
      {
        return oid;
      }


    public String getJCEName ()
      {
        return jcename;
      }

    
    public static boolean testAlgorithmURI (String sks_id)
      {
        for (HashAlgorithms alg : HashAlgorithms.values ())
          {
            if (sks_id.equals (alg.sks_id))
              {
                return true;
              }
          }
        return false;
      }


    public byte[] digest (byte[] data) throws IOException
      {
        try
          {
            return MessageDigest.getInstance (getJCEName ()).digest (data);
          }
        catch (GeneralSecurityException gse)
          {
            throw new IOException (gse);
          }
      }


    public static HashAlgorithms getAlgorithmFromID (String algorithm_id) throws IOException
      {
        for (HashAlgorithms alg : values ())
          {
            if (algorithm_id.equals (alg.sks_id))
              {
                return alg;
              }
          }
        throw new IOException ("Unknown algorithm: " + algorithm_id);
      }


    public static HashAlgorithms getAlgorithmFromOID (String oid) throws IOException
      {
        for (HashAlgorithms alg : values ())
          {
            if (oid.equals (alg.oid))
              {
                return alg;
              }
          }
        throw new IOException ("Unknown algorithm: " + oid);
      }

  }
