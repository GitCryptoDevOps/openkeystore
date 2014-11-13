/*
 *  Copyright 2006-2014 WebPKI.org (http://webpki.org).
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
package org.webpki.webapps.wcppsignaturedemo;

import java.io.IOException;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import java.security.cert.X509Certificate;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.webpki.crypto.CertificateInfo;
import org.webpki.crypto.CertificateUtil;
import org.webpki.crypto.CustomCryptoProvider;
import org.webpki.crypto.KeyStoreReader;

import org.webpki.json.JSONSignatureDecoder;

import org.webpki.util.ArrayUtil;
import org.webpki.util.Base64;
import org.webpki.util.Base64URL;

import org.webpki.webutil.InitPropertyReader;

public class SignatureDemoService extends InitPropertyReader implements ServletContextListener
  {
    static Logger logger = Logger.getLogger (SignatureDemoService.class.getName ());
    
    static String issuer_url;
    static String relying_party_url;
    
    static String cross_data_uri;
    static String working_data_uri;
    static String mybank_data_uri;
    
    static String card_font;

    static String key_password;

    static KeyStore client_root;
    static String client_eecert;
    static String user_name;
    static JWK client_private_key;
    static String client_cert_data;
    
    static int reference_id = 1000000;
    
    public static String getDataURI (String mime_type, byte[] data) throws IOException
      {
        return "data:" + mime_type + ";base64," + new Base64 (false).getBase64StringFromBinary (data);
      }

    private String getDataURI (String main, String extension) throws IOException
      {
        return getDataURI ("image/" + (extension.equals ("svg") ? "svg+xml" : extension),
                           ArrayUtil.getByteArrayFromInputStream (SignatureDemoService.class.getResourceAsStream (main + "." + extension)));
      }
    
    private KeyStore getRootCertificate (String resource_name) throws IOException, GeneralSecurityException
      {
        KeyStore ks = KeyStore.getInstance ("JKS");
        ks.load (null, null);
        ks.setCertificateEntry ("mykey",
                                CertificateUtil.getCertificateFromBlob (
                                    ArrayUtil.getByteArrayFromInputStream ( 
                                        SignatureDemoService.class.getResourceAsStream (resource_name))));        
        return ks;
      }

    @Override
    public void contextDestroyed (ServletContextEvent event)
      {
      }

    @Override
    public void contextInitialized (ServletContextEvent event)
      {
        initProperties (event);
        try 
          {
            CustomCryptoProvider.forcedLoad (getPropertyBoolean ("bouncycastle_first"));
            issuer_url = getPropertyString ("issuer_url");
            relying_party_url = getPropertyString ("relying_party_url");
            cross_data_uri = getDataURI ("cross", "png");
            working_data_uri = getDataURI ("working", "gif");
            mybank_data_uri = getDataURI ("mybank", "svg");
            card_font = getPropertyString ("card_font");
            key_password = getPropertyString ("key_password");
            client_root = getRootCertificate (getPropertyString ("client_root"));
            KeyStore client = KeyStoreReader.loadKeyStore (SignatureDemoService.class.getResourceAsStream (getPropertyString ("client_eecert")), SignatureDemoService.key_password);
            X509Certificate cert = (X509Certificate) client.getCertificate ("mykey");
            user_name = new CertificateInfo (cert).getSubjectCommonName ();
            client_eecert = Base64URL.encode (cert.getEncoded ());
            client_cert_data = new StringBuffer ("{" + JSONSignatureDecoder.ISSUER_JSON + ":'")
              .append (cert.getIssuerX500Principal ().getName ())
              .append ("', " + JSONSignatureDecoder.SERIAL_NUMBER_JSON + ":'")
              .append (cert.getSerialNumber ().toString ())
              .append ("', " + JSONSignatureDecoder.SUBJECT_JSON + ":'")
              .append (cert.getSubjectX500Principal ().getName ())
              .append ("'}").toString ();
            client_private_key = cert.getPublicKey () instanceof RSAPublicKey ? 
                  new JWK (client.getKey ("mykey", SignatureDemoService.key_password.toCharArray ()))
                                    :
                  new JWK ((ECPublicKey)cert.getPublicKey (), (ECPrivateKey)client.getKey ("mykey", SignatureDemoService.key_password.toCharArray ()));
            logger.info ("WebCrypto++ Signature Demo ClientKey=" + client_private_key.getKeyType () + " Successfully Initiated");
          }
        catch (Exception e)
          {
            logger.log(Level.SEVERE, "********\n" + e.getMessage() + "\n********", e);
          }
      }
  }