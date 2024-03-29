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
package org.webpki.crypto.test;

import java.math.BigInteger;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;

import java.security.interfaces.RSAPublicKey;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.KeyAgreement;

import org.webpki.crypto.KeyAlgorithms;
import org.webpki.crypto.SignatureAlgorithms;

public class KeyExperiments
  {

    // Simple test of keys and generation

    private static KeyPair gen (KeyAlgorithms key_alg) throws Exception
      {
        AlgorithmParameterSpec alg_par_spec = null;
        if (key_alg.isRSAKey ())
          {
            int rsa_key_size = key_alg.getPublicKeySizeInBits ();
            BigInteger exponent = RSAKeyGenParameterSpec.F4;
            if (key_alg.hasParameters ())
              {
                exponent = RSAKeyGenParameterSpec.F0;
              }
            alg_par_spec = new RSAKeyGenParameterSpec (rsa_key_size, exponent);
          }
        else
          {
            alg_par_spec = new ECGenParameterSpec (key_alg.getJCEName ());
          }
        KeyPairGenerator generator = KeyPairGenerator.getInstance (alg_par_spec instanceof RSAKeyGenParameterSpec ? "RSA" : "EC");
        generator.initialize (alg_par_spec, new SecureRandom ());
        KeyPair kp =  generator.generateKeyPair ();
        if (key_alg != KeyAlgorithms.getKeyAlgorithm (kp.getPublic (), key_alg.hasParameters ()))
          {
            throw new RuntimeException ("Key mismatch on: " + key_alg);
          }
        return kp;
      }

    static byte[] data = {4, 5, 6, 7, 8, 0};
 
    
    private static void signverify (KeyPair kp, SignatureAlgorithms optional) throws Exception
      {
        SignatureAlgorithms sign_alg = optional == null ?
                       kp.getPublic () instanceof RSAPublicKey ? 
                                SignatureAlgorithms.RSA_SHA256 : SignatureAlgorithms.ECDSA_SHA256
                                                        : 
                       optional;
 
        Signature signer = Signature.getInstance (sign_alg.getJCEName ());
        signer.initSign (kp.getPrivate ());
        signer.update (data);
        byte[] signature = signer.sign ();

        Signature verifier = Signature.getInstance (sign_alg.getJCEName ());
        verifier.initVerify (kp.getPublic ());
        verifier.update (data);

        if (!verifier.verify (signature))
          {
            throw new RuntimeException ("Bad sign for: " + kp.getPublic ().toString ());
          }
      }
    
    private static void execute (KeyAlgorithms key_alg) throws Exception
      {
        KeyPair kp1 = gen (key_alg);
        KeyPair kp2 = gen (key_alg);
        if (key_alg.isECKey ())
          {
            KeyAgreement ka1 = KeyAgreement.getInstance("ECDH");
    
            ka1.init(kp1.getPrivate());
    
            KeyAgreement ka2 = KeyAgreement.getInstance("ECDH");
    
            ka2.init(kp2.getPrivate());
    
            ka1.doPhase(kp2.getPublic(), true);
            ka2.doPhase(kp1.getPublic(), true);
    
            BigInteger  k1 = new BigInteger(ka1.generateSecret());
            BigInteger  k2 = new BigInteger(ka2.generateSecret());
    
            if (!k1.equals(k2))
              {
                throw new RuntimeException (key_alg + " 2-way test failed");
              }
            System.out.println ("ECDH worked for key algorithm: " + key_alg);
          }
        signverify (kp1, null);
        signverify (kp2, key_alg.getRecommendedSignatureAlgorithm ());
        System.out.println ("Signature worked for algorithm: " + key_alg);
      }

    public static void main (String[] argv) throws Exception
      {
        try
          {
            Class<?> clazz = Class.forName ("org.bouncycastle.jce.provider.BouncyCastleProvider");
            Security.insertProviderAt ((Provider) clazz.newInstance (), 1);
          }
        catch (Exception e)
          {
            System.out.println ("BC not found");
          }
        for (KeyAlgorithms key_alg : KeyAlgorithms.values ())
          {
            execute (key_alg);
          }
      }
  }
