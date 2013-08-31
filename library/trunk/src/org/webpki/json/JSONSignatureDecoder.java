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
package org.webpki.json;

import java.io.IOException;

import java.math.BigInteger;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;

import java.security.cert.X509Certificate;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;

import java.util.Vector;

import org.webpki.crypto.CertificateUtil;
import org.webpki.crypto.SignatureAlgorithms;
import org.webpki.crypto.AsymSignatureAlgorithms;
import org.webpki.crypto.MACAlgorithms;
import org.webpki.crypto.KeyAlgorithms;

import org.bouncycastle.jce.ECNamedCurveTable;

import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;

/**
 * Decoder for JSON signatures.
 */
public class JSONSignatureDecoder extends JSONSignature
  {
    SignatureAlgorithms algorithm;
    
    String algorithm_string;
    
    byte[] canonicalized_data;
    
    byte[] signature_value;
    
    X509Certificate[] certificate_path;

    PublicKey public_key;

    String key_id;
    
    public JSONSignatureDecoder (JSONReaderHelper rd) throws IOException
      {
        JSONReaderHelper signature = rd.getObject (SIGNATURE_JSON);
        String version = signature.getStringConditional (VERSION_JSON, SIGNATURE_VERSION_ID);
        if (!version.equals (SIGNATURE_VERSION_ID))
          {
            throw new IOException ("Unknown \"" + SIGNATURE_JSON + "\" version: " + version);
          }
        algorithm_string = signature.getString (ALGORITHM_JSON);
        getKeyInfo (signature.getObject (KEY_INFO_JSON));
        signature_value = signature.getBinary (SIGNATURE_VALUE_JSON);
        JSONValue save = signature.current.properties.get (SIGNATURE_VALUE_JSON);
        signature.current.properties.remove (SIGNATURE_VALUE_JSON);
        canonicalized_data = JSONWriter.getCanonicalizedSubset (rd.current);
        signature.current.properties.put (SIGNATURE_VALUE_JSON, save);
        switch (getSignatureType ())
          {
            case X509_CERTIFICATE:
              asymmetricSignatureVerification (certificate_path[0].getPublicKey ());
              break;

            case ASYMMETRIC_KEY:
              asymmetricSignatureVerification (public_key);
              break;

            default:
              algorithm = MACAlgorithms.getAlgorithmFromURI (algorithm_string);
          }
      }

    void getKeyInfo (JSONReaderHelper rd) throws IOException
      {
        if (rd.hasProperty (SIGNATURE_CERTIFICATE_JSON))
          {
            getSignatureCertificate (rd, rd.getObject (SIGNATURE_CERTIFICATE_JSON));
          }
        else if (rd.hasProperty (X509_CERTIFICATE_PATH_JSON))
          {
            getX509CertificatePath (rd);
          }
        else if (rd.hasProperty (PUBLIC_KEY_JSON))
          {
            public_key = readPublicKey (rd);
          }
        else
          {
            key_id = rd.getString (KEY_ID_JSON);
          }
      }

    static BigInteger readCryptoBinary (JSONReaderHelper rd, String property) throws IOException
      {
        byte[] crypto_binary = rd.getBinary (property);
        if (crypto_binary[0] == 0x00)
          {
            throw new IOException ("Public key parameters must not contain leading zeroes");
          }
        return new BigInteger (1, crypto_binary);
      }

    public static PublicKey readPublicKey (JSONReaderHelper rd) throws IOException
      {
        rd = rd.getObject (PUBLIC_KEY_JSON);
        try
          {
            if (rd.hasProperty (RSA_JSON))
              {
                rd = rd.getObject (RSA_JSON);
                return KeyFactory.getInstance ("RSA").generatePublic (new RSAPublicKeySpec (readCryptoBinary (rd, MODULUS_JSON),
                                                                                            readCryptoBinary (rd, EXPONENT_JSON)));
              }
            rd = rd.getObject (EC_JSON);
              {
                String named_curve_uri = rd.getString (NAMED_CURVE_JSON);
                for (KeyAlgorithms named_curve : KeyAlgorithms.values ())
                  {
                    if (named_curve.getURI ().equals (named_curve_uri))
                      {
                        if (named_curve.getECDomainOID () == null)
                          {
                            throw new IOException ("Invalid EC curve: " + named_curve_uri);
                          }
                        ECNamedCurveParameterSpec curve_params = ECNamedCurveTable.getParameterSpec (named_curve.getJCEName ());
                        if (curve_params == null)
                          {
                            throw new IOException ("Provider doesn't support: " + named_curve_uri);
                          }
                        ECPoint w = new ECPoint (readCryptoBinary (rd, X_JSON), readCryptoBinary (rd, Y_JSON));
                        ECParameterSpec ec_params = new ECNamedCurveSpec (named_curve.getJCEName (),
                                                                          curve_params.getCurve(),
                                                                          curve_params.getG(),
                                                                          curve_params.getN());
                        return KeyFactory.getInstance ("EC").generatePublic (new ECPublicKeySpec (w, ec_params));
                      }
                  }
                throw new IOException ("Unknown named curve: " + named_curve_uri);
              }
          }
        catch (GeneralSecurityException e)
          {
            throw new IOException (e);
          }
      }

    public static X509Certificate[] readX509CertificatePath (JSONReaderHelper rd) throws IOException
      {
        X509Certificate last_certificate = null;
        Vector<X509Certificate> certificates = new Vector<X509Certificate> ();
        for (byte[] certificate_blob : rd.getBinaryArray (JSONSignature.X509_CERTIFICATE_PATH_JSON))
          {
            X509Certificate certificate = CertificateUtil.getCertificateFromBlob (certificate_blob);
            certificates.add (pathCheck (last_certificate, last_certificate = certificate));
          }
        return certificates.toArray (new X509Certificate[0]);
      }

    void getX509CertificatePath (JSONReaderHelper rd) throws IOException
      {
        certificate_path = readX509CertificatePath (rd);
      }

    void checkVerification (boolean success) throws IOException
      {
        if (!success)
          {
            String key;
            switch (getSignatureType ())
              {
                case X509_CERTIFICATE:
                  key = certificate_path[0].toString ();
                  break;
  
                case ASYMMETRIC_KEY:
                  key = public_key.toString ();
                  break;
  
                default:
                  key = getKeyID ();
               }
            throw new IOException ("Bad signed_object for key: " + key);
          }
      }

    void asymmetricSignatureVerification (PublicKey public_key) throws IOException
      {
        algorithm = AsymSignatureAlgorithms.getAlgorithmFromURI (algorithm_string);
        try
          {
            Signature sig = Signature.getInstance (algorithm.getJCEName ());
            sig.initVerify (public_key);
            sig.update (canonicalized_data);
            checkVerification (sig.verify (signature_value));
          }
        catch (GeneralSecurityException e)
          {
            throw new IOException (e);
          }
      }

    void getSignatureCertificate (JSONReaderHelper outer, JSONReaderHelper rd) throws IOException
      {
        String issuer = rd.getString (ISSUER_JSON);
        BigInteger serial_number = rd.getBigInteger (SERIAL_NUMBER_JSON);
        String subject = rd.getString (SUBJECT_JSON);
        getX509CertificatePath (outer);
        X509Certificate signature_certificate = certificate_path[0];
        if (!signature_certificate.getIssuerX500Principal ().getName ().equals (issuer) ||
            !signature_certificate.getSerialNumber ().equals (serial_number) ||
            !signature_certificate.getSubjectX500Principal ().getName ().equals (subject))
          {
            throw new IOException ("\"" + SIGNATURE_CERTIFICATE_JSON + "\" doesn't match actual certificate");
          }
      }

    public byte[] getSignatureValue ()
      {
        return signature_value;
      }

    public SignatureAlgorithms getSignatureAlgorithm ()
      {
        return algorithm;
      }

    public String getKeyID () throws IOException
      {
        if (getSignatureType () != JSONSignatureTypes.SYMMETRIC_KEY)
          {
            throw new IOException ("\"" + KEY_ID_JSON + "\" does not apply to: " + getSignatureType ().toString ());
          }
        return key_id;
      }

    public JSONSignatureTypes getSignatureType ()
      {
        if (certificate_path != null)
          {
            return JSONSignatureTypes.X509_CERTIFICATE;
          }
        return public_key == null ? JSONSignatureTypes.SYMMETRIC_KEY : JSONSignatureTypes.ASYMMETRIC_KEY;
      }

    public static JSONSignatureDecoder read (JSONReaderHelper rd) throws IOException
      {
        JSONSignatureDecoder verifier = new JSONSignatureDecoder (rd);
        return verifier;
      }

    public void verify (JSONVerifier verifier) throws IOException
      {
        if (verifier.getVerifierType () != getSignatureType ())
          {
            throw new IOException ("Verifier type doesn't match the received signature");
          }
        verifier.verify (this);
      }
  }