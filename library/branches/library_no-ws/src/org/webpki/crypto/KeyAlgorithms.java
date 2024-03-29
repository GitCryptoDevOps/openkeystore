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
package org.webpki.crypto;

import java.io.IOException;

import java.security.PublicKey;

import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;

import org.webpki.asn1.DerDecoder;
import org.webpki.asn1.ParseUtil;


public enum KeyAlgorithms implements SKSAlgorithms
  {
    RSA1024     (null,
                 "http://xmlns.webpki.org/sks/algorithm#rsa1024",
                 "RSA",
                 1024,
                 SignatureAlgorithms.RSA_SHA1,
                 false,
                 true),

    RSA2048     (null,
                 "http://xmlns.webpki.org/sks/algorithm#rsa2048",
                 "RSA",
                 2048,
                 SignatureAlgorithms.RSA_SHA256,
                 false,
                 true),

    RSA3072     (null,
                 "http://xmlns.webpki.org/sks/algorithm#rsa3072",
                 "RSA",
                 3072,
                 SignatureAlgorithms.RSA_SHA512,
                 false,
                 false),

    RSA4096     (null,
                 "http://xmlns.webpki.org/sks/algorithm#rsa4096",
                 "RSA",
                 4096,
                 SignatureAlgorithms.RSA_SHA512,
                 false,
                 false),

    RSA1024_EXP (null,
                 "http://xmlns.webpki.org/sks/algorithm#rsa1024.exp",
                 "RSA",
                 1024,
                 SignatureAlgorithms.RSA_SHA1,
                 true,
                 false),

    RSA2048_EXP (null,
                 "http://xmlns.webpki.org/sks/algorithm#rsa2048.exp",
                 "RSA",
                 2048,
                 SignatureAlgorithms.RSA_SHA256,
                 true,
                 false),

    RSA3072_EXP (null,
                 "http://xmlns.webpki.org/sks/algorithm#rsa3072.exp",
                 "RSA",
                 3072,
                 SignatureAlgorithms.RSA_SHA512,
                 true,
                 false),

    RSA4096_EXP (null,
                 "http://xmlns.webpki.org/sks/algorithm#rsa4096.exp",
                 "RSA",
                 4096,
                 SignatureAlgorithms.RSA_SHA512,
                 true,
                 false),

    B_163       ("1.3.132.0.15",
                 "http://xmlns.webpki.org/sks/algorithm#ec.b163",
                 "sect163r2",
                 163,
                 SignatureAlgorithms.ECDSA_SHA256,
                 false,
                 false),

    B_233       ("1.3.132.0.27",
                 "http://xmlns.webpki.org/sks/algorithm#ec.b233",
                 "sect233r1",
                 233,
                 SignatureAlgorithms.ECDSA_SHA512,
                 false,
                 false),

    B_283       ("1.3.132.0.17",
                 "http://xmlns.webpki.org/sks/algorithm#ec.b283",
                 "sect283r1",
                 283,
                 SignatureAlgorithms.ECDSA_SHA512,
                 false,
                 false),

    P_192       ("1.2.840.10045.3.1.1",
                 "http://xmlns.webpki.org/sks/algorithm#ec.p192",
                 "secp192r1",
                 192,
                 SignatureAlgorithms.ECDSA_SHA256,
                 false,
                 false),

    P_256       ("1.2.840.10045.3.1.7",
                 "http://xmlns.webpki.org/sks/algorithm#ec.p256",
                 "secp256r1",
                 256,
                 SignatureAlgorithms.ECDSA_SHA256,
                 false,
                 true),

    P_384       ("1.3.132.0.34",
                 "http://xmlns.webpki.org/sks/algorithm#ec.p384",
                 "secp384r1",
                 384,
                 SignatureAlgorithms.ECDSA_SHA512,
                 false,
                 false),

    P_521       ("1.3.132.0.35",
                 "http://xmlns.webpki.org/sks/algorithm#ec.p521",
                 "secp521r1",
                 521,
                 SignatureAlgorithms.ECDSA_SHA512,
                 false,
                 false);

    private final String ec_domain_oid;          // EC domain as expressed in ASN.1 messages, null for RSA
    private final String uri;                    // As expressed in XML messages
    private final String jcename;                // As expressed for JCE
    private final int length_in_bits;
    private final SignatureAlgorithms pref_alg;  // A sort of a "guide"
    private final boolean has_parameters;        // Parameter value required?
    private final boolean sks_mandatory;         // If required in SKS


    private KeyAlgorithms (String ec_domain_oid, 
                           String uri,
                           String jcename,
                           int length_in_bits,
                           SignatureAlgorithms pref_alg,
                           boolean has_parameters,
                           boolean sks_mandatory)
      {
        this.ec_domain_oid = ec_domain_oid;
        this.uri = uri;
        this.jcename = jcename;
        this.length_in_bits = length_in_bits;
        this.pref_alg = pref_alg;
        this.has_parameters = has_parameters;
        this.sks_mandatory = sks_mandatory;
      }


    @Override
    public String getURI ()
      {
        return uri;
      }

    
    public String getJCEName ()
      {
        return jcename;
      }


    public String getECDomainOID ()
      {
        return ec_domain_oid;
      }

    
    @Override
    public boolean isMandatorySKSAlgorithm ()
      {
        return sks_mandatory;
      }


    public boolean isECKey ()
      {
        return ec_domain_oid != null;
      }

    
    public boolean isRSAKey ()
      {
        return ec_domain_oid == null;
      }


    public int getPublicKeySizeInBits ()
      {
        return length_in_bits;
      }
 

    public SignatureAlgorithms getRecommendedSignatureAlgorithm ()
      {
        return pref_alg;
      }


    public boolean hasParameters ()
      {
        return has_parameters;
      }
 

    public static KeyAlgorithms getKeyAlgorithm (PublicKey public_key, Boolean key_parameters) throws IOException
      {
        if (public_key instanceof ECPublicKey)
          {
            String ec_domain_oid = ParseUtil.oid (
                                    ParseUtil.sequence (
                                      ParseUtil.sequence (
                                        DerDecoder.decode (public_key.getEncoded ()), 2).get(0), 2).get (1)
                                                 ).oid ();
            for (KeyAlgorithms alg : values ())
              {
                if (ec_domain_oid.equals (alg.ec_domain_oid) &&
                    (key_parameters == null || !alg.has_parameters))
                  {
                    return alg;
                  }
              }
            throw new IOException ("Unknown EC domain: " + ec_domain_oid);
          }
        byte[] modblob = ((RSAPublicKey)public_key).getModulus ().toByteArray ();
        int length_in_bits = (modblob[0] == 0 ? modblob.length - 1 : modblob.length) * 8;
        for (KeyAlgorithms alg : values ())
          {
            if (alg.ec_domain_oid == null && length_in_bits == alg.length_in_bits && 
                (key_parameters == null || alg.has_parameters == key_parameters))
              {
                return alg;
              }
          }
        throw new IOException ("Unsupported RSA key size: " + length_in_bits);
      }


    public static KeyAlgorithms getKeyAlgorithm (PublicKey public_key) throws IOException
      {
        return getKeyAlgorithm (public_key, null);
      }


    public static KeyAlgorithms getKeyAlgorithmFromURI (String uri) throws IOException
      {
        for (KeyAlgorithms alg : values ())
          {
            if (uri.equals (alg.uri))
              {
                return alg;
              }
          }
        throw new IOException ("Unknown algorithm: " + uri);
      }


    @Override
    public String getOID ()
      {
        return null;
      }
  }
