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
package org.webpki.keygen2.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import java.math.BigInteger;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.security.interfaces.ECPublicKey;

import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.crypto.KeyAgreement;

import javax.security.auth.x500.X500Principal;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.rules.TestName;

import static org.junit.Assert.*;

import org.webpki.asn1.cert.DistinguishedName;

import org.webpki.ca.CA;
import org.webpki.ca.CertSpec;

import org.webpki.crypto.AsymKeySignerInterface;
import org.webpki.crypto.CertificateUtil;
import org.webpki.crypto.KeyAlgorithms;
import org.webpki.crypto.HashAlgorithms;
import org.webpki.crypto.KeyContainerTypes;
import org.webpki.crypto.KeyStoreSigner;
import org.webpki.crypto.KeyUsageBits;
import org.webpki.crypto.MACAlgorithms;
import org.webpki.crypto.AsymSignatureAlgorithms;
import org.webpki.crypto.CustomCryptoProvider;
import org.webpki.crypto.SymEncryptionAlgorithms;
import org.webpki.crypto.SymKeySignerInterface;
import org.webpki.crypto.SignatureWrapper;

import org.webpki.crypto.test.DemoKeyStore;

import org.webpki.keygen2.Action;
import org.webpki.keygen2.KeyGen2Constants;
import org.webpki.keygen2.KeyGen2Messages;
import org.webpki.keygen2.KeySpecifier;
import org.webpki.keygen2.ProvisioningFinalizationRequestDecoder;
import org.webpki.keygen2.ProvisioningFinalizationRequestEncoder;
import org.webpki.keygen2.ProvisioningFinalizationResponseDecoder;
import org.webpki.keygen2.ProvisioningFinalizationResponseEncoder;
import org.webpki.keygen2.CredentialDiscoveryRequestDecoder;
import org.webpki.keygen2.CredentialDiscoveryRequestEncoder;
import org.webpki.keygen2.CredentialDiscoveryResponseDecoder;
import org.webpki.keygen2.CredentialDiscoveryResponseEncoder;
import org.webpki.keygen2.KeyGen2URIs;
import org.webpki.keygen2.KeyCreationResponseDecoder;
import org.webpki.keygen2.KeyCreationResponseEncoder;
import org.webpki.keygen2.KeyCreationRequestDecoder;
import org.webpki.keygen2.KeyCreationRequestEncoder;
import org.webpki.keygen2.InvocationRequestDecoder;
import org.webpki.keygen2.InvocationRequestEncoder;
import org.webpki.keygen2.InvocationResponseDecoder;
import org.webpki.keygen2.InvocationResponseEncoder;
import org.webpki.keygen2.ProvisioningInitializationRequestDecoder;
import org.webpki.keygen2.ProvisioningInitializationRequestEncoder;
import org.webpki.keygen2.ProvisioningInitializationResponseDecoder;
import org.webpki.keygen2.ProvisioningInitializationResponseEncoder;
import org.webpki.keygen2.ServerState;

import org.webpki.sks.AppUsage;
import org.webpki.sks.DeleteProtection;
import org.webpki.sks.DeviceInfo;
import org.webpki.sks.EnumeratedKey;
import org.webpki.sks.EnumeratedProvisioningSession;
import org.webpki.sks.ExportProtection;
import org.webpki.sks.InputMethod;
import org.webpki.sks.KeyAttributes;
import org.webpki.sks.KeyData;
import org.webpki.sks.Grouping;
import org.webpki.sks.KeyProtectionInfo;
import org.webpki.sks.PassphraseFormat;
import org.webpki.sks.PatternRestriction;
import org.webpki.sks.Property;
import org.webpki.sks.ProvisioningSession;
import org.webpki.sks.SKSException;
import org.webpki.sks.SecureKeyStore;

import org.webpki.sks.ws.WSSpecific;

import org.webpki.util.ArrayUtil;
import org.webpki.util.HTMLHeader;
import org.webpki.util.ImageData;

import org.webpki.json.JSONDecoder;
import org.webpki.json.JSONDecoderCache;
import org.webpki.json.JSONObjectWriter;
import org.webpki.json.JSONOutputFormats;

/*
 * KeyGen2 "Protocol Exerciser" / JUnit Test
 */
public class KeyGen2Test
  {
    static final byte[] TEST_STRING = {'S','u','c','c','e','s','s',' ','o','r',' ','n','o','t','?'};

    boolean pin_protection;
    
    boolean ecc_key;
    
    boolean two_keys;
    
    boolean custom_key_name;
    
    boolean key_agreement;
    
    boolean server_seed;
    
    boolean property_bag;
    
    boolean symmetric_key;
    
    boolean updatable;
    
    boolean ecc_kmk;
    
    boolean fixed_pin;
    
    boolean languages;
    
    boolean key_container_list;
    
    Server clone_key_protection;
    
    Server update_key;
    
    Server delete_key;
    
    Server plain_unlock_key;
    
    boolean device_pin_protection;
    
    boolean enable_pin_caching;
    
    boolean privacy_enabled;
    
    boolean pin_group_shared;
    
    boolean puk_protection;
    
    boolean add_pin_pattern;
    
    boolean preset_pin;
    
    boolean encryption_key;
    
    boolean set_private_key;
    
    boolean encrypted_extension;
    
    boolean standard_extension;
    
    boolean set_trust_anchor;
    
    boolean update_kmk;
    
    boolean brain_pool;
    
    boolean virtual_environment;
    
    boolean get_client_attributes;
    
    boolean https;  // Use server-cert
    
    boolean ask_for_4096;
    
    boolean ask_for_exponent;
    
    boolean set_abort_url;
    
    ExportProtection export_protection;
    
    DeleteProtection delete_protection;
    
    InputMethod input_method;
    
    boolean image_prefs;
    
    static FileOutputStream fos;
    
    static SecureKeyStore sks;
    
    static final byte[] OTP_SEED = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
    
    static final byte[] AES32BITKEY = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};
    
    static final byte[] USER_DEFINED_PIN = {'0','1','5','3','5','4'};

    static final byte[] PREDEF_SERVER_PIN = {'3','1','2','5','8','9'};
    
    static final byte[] BAD_PIN = {0x03, 0x33, 0x03, 0x04};

    static final String ABORT_URL = "http://issuer.example.com/abort";

    static final String INVOCATION_URL = "http://issuer.example.com/invocation";

    static final String ISSUER_URL = "http://issuer.example.com/provsess";
    
    static final String KEY_INIT_URL = "http://issuer.example.com/keyinit";

    static final String FIN_PROV_URL = "http://issuer.example.com/finalize";

    static final String CRE_DISC_URL = "http://issuer.example.com/credisc";
    
    static final String ACME_INDUSTRIES = "Acme Industries";
    
    static final String SPECIFIC_VM = "http://platforms.extreme-vm.com/type.3";
    static final byte[] VM_CONFIG_DATA = {0,1,2,3};  // In real file probably a bit bigger...
    
    static X509Certificate server_certificate;
    
    int round;
   
    @BeforeClass
    public static void openFile () throws Exception
      {
        String dir = System.getProperty ("test.dir");
        if (dir.length () > 0)
          {
            fos = new FileOutputStream (dir + "/keygen2.junit.run.html");
            fos.write (HTMLHeader.createHTMLHeader (false, true,"KeyGen2 JUinit test output", null).append ("<body><h3>KeyGen2 JUnit Test</h3><p>").toString ().getBytes ("UTF-8"));
          }
        CustomCryptoProvider.forcedLoad (true);
        server_certificate = (X509Certificate) CertificateFactory.getInstance ("X.509").generateCertificate (KeyGen2Test.class.getResourceAsStream ("server-certificate.der"));
        sks = (SecureKeyStore) Class.forName (System.getProperty ("sks.implementation")).newInstance ();
        if (fos != null)
          {
            DeviceInfo dev = sks.getDeviceInfo ();
            fos.write (("<b>SKS Description: " + dev.getVendorDescription () +
                        "<br>SKS Vendor: " + dev.getVendorName () +
                        "<br>SKS API Level: " + (dev.getApiLevel () / 100) + '.' + (dev.getApiLevel () % 100) +
                        "<br>SKS Interface: " + (sks instanceof WSSpecific ? "WebService" : "Direct") +
                        "<br>&nbsp<br></b>").getBytes ("UTF-8"));
          }
        if (sks instanceof WSSpecific)
          {
            String device_id = System.getProperty ("sks.device");
            if (device_id != null && device_id.length () != 0)
              {
                ((WSSpecific) sks).setDeviceID (device_id);
              }
          }
      }

    @AfterClass
    public static void closeFile () throws Exception
      {
        if (fos != null)
          {
            fos.write ("</body></html>".getBytes ("UTF-8"));
            fos.close ();
          }
      }
    
    @Before
    public void setup () throws Exception
      {
         if (sks instanceof WSSpecific)
           {
             ((WSSpecific)sks).logEvent ("Testing:" + _name.getMethodName ());
           }
      }
        
    @After
    public void teardown () throws Exception
      {
      }
    @Rule 
    public TestName _name = new TestName();
    
    static class KeyCreator
      {
        private static final String kg2keycre = 
        "{\n" +
            "\"@context\": \"" + KeyGen2Constants.KEYGEN2_NS + "\",\n" +
            "\"@qualifier\": \"" + KeyGen2Messages.KEY_CREATION_REQUEST.getName () + "\",\n" +
            "\"" + KeyGen2Constants.SERVER_SESSION_ID_JSON + "\": \"1417ace50e9IoDMto6NHlN1JWvysvZsC\",\n" +
            "\"" + KeyGen2Constants.CLIENT_SESSION_ID_JSON + "\": \"KzyjlYG3YurWzSr2d9O9X3y_1EUsadmE\",\n" +
            "\"" + KeyGen2Constants.SUBMIT_URL_JSON + "\": \"http://issuer.example.com/keyinit\",\n" +
            "\"" + KeyGen2Constants.KEY_ENTRY_ALGORITHM_JSON + "\": \"http://xmlns.webpki.org/sks/algorithm#key.1\",\n";     
        private static JSONDecoderCache json_cache;
        
        static
        {
          try
            {
              json_cache = new JSONDecoderCache ();
              json_cache.addToCache (KeyCreationRequestDecoder.class);
            }
          catch (IOException e)
            {
            }
        }
        
        private StringBuffer json = new StringBuffer (kg2keycre);
        
        private int key_id;
        
        private int pin_id;
        
        boolean key_spec = true;
        
        KeyCreator () throws IOException
          {
          }
        
        KeyCreator addPIN (PassphraseFormat format, Grouping grouping, PatternRestriction[] patterns)
          {
            if (grouping == null)
              {
                grouping = Grouping.NONE;
              }
            json.append ("\"" + KeyGen2Constants.PIN_POLICY_SPECIFIERS_JSON + "\":[{ \"" + KeyGen2Constants.FORMAT_JSON + "\" :\"")
               .append (format.getProtocolName ())
               .append ("\", \"" + KeyGen2Constants.ID_JSON + "\":\"PIN.")
               .append (++pin_id)
               .append ("\", \"" + KeyGen2Constants.GROUPING_JSON + "\" :\"")
               .append (grouping.getProtocolName ())
               .append ("\",");
            if (patterns != null)
              {
                json.append (" \"" + KeyGen2Constants.PATTERN_RESTRICTIONS_JSON + "\" : [");
                String blank="";
                for (PatternRestriction pattern : patterns)
                  {
                    json.append (blank);
                    blank = ",";
                    json.append ('"').append (pattern.getProtocolName ()).append ('"');
                  }
                json.append ("],");
              }
            json.append (" \"" + KeyGen2Constants.MAC_JSON + "\": \"3dGegeDJ1enpEzCgwdbXJirNZ95wooM6ordOGW_AJ-0\", \"" + KeyGen2Constants.MAX_LENGTH_JSON + "\":8, \"" + KeyGen2Constants.MIN_LENGTH_JSON + "\":4, \"" + KeyGen2Constants.RETRY_LIMIT_JSON + "\":3,");
            return this;
          }
        
        KeyCreator addKey (AppUsage app_usage)
          {
            if (key_spec)
              {
                json.append ('"').append (KeyGen2Constants.KEY_ENTRY_SPECIFIERS_JSON).append ("\" :[");
              }
            if (!key_spec) json.append (',');
            json.append ("{ \"" + KeyGen2Constants.APP_USAGE_JSON + "\":\"")
               .append (app_usage.getProtocolName ())
               .append ("\", \"" + KeyGen2Constants.ID_JSON + "\":\"Key.")
               .append (++key_id)
               .append ("\", \"" + KeyGen2Constants.KEY_ALGORITHM_JSON + "\":\"http://xmlns.webpki.org/sks/algorithm#rsa2048\", \"" + KeyGen2Constants.MAC_JSON + "\":\"Jrqigi79Yw6SoLobsBA5S8b74gTKrIJPh3tQRKci33Y\"}");
            key_spec = false;
            return this;
          }

        KeyCreationRequestDecoder parse () throws Exception
          {
            json.append ("]}]}");
            return (KeyCreationRequestDecoder)json_cache.parse (json.toString ().getBytes ("UTF-8"));
          }
      }

    boolean PINCheck (PassphraseFormat format, PatternRestriction[] patterns, String pin) throws Exception
      {
        KeyCreator kc = new KeyCreator ();
        kc.addPIN (format, null, patterns);
        kc.addKey (AppUsage.AUTHENTICATION);
        KeyCreationRequestDecoder.UserPINDescriptor upd = kc.parse ().getUserPINDescriptors ().elementAt (0);
        KeyCreationRequestDecoder.UserPINError pin_test = upd.setPIN (pin, false);
        KeyCreationRequestDecoder.UserPINError pin_set = upd.setPIN (pin, true);
        if ((pin_test == null) ^ (pin_set == null))
          {
            throw new IOException ("PIN test/set confusion");
          }
        return pin_set == null;
      }
    
    void PINGroupCheck (Grouping grouping, AppUsage[] keys, String[] pins, int[] index, boolean fail) throws Exception
      {
        KeyCreator kc = new KeyCreator ();
        kc.addPIN (PassphraseFormat.NUMERIC, grouping, null);
        for (AppUsage app_usage : keys)
          {
            kc.addKey (app_usage);
          }
        KeyCreationRequestDecoder decoder = kc.parse ();
        String error = null;
        if (decoder.getUserPINDescriptors ().size () != pins.length)
          {
            error = "Wrong number of PINs";
          }
        else
          {
            int i = 0;
            for (KeyCreationRequestDecoder.UserPINDescriptor upd : decoder.getUserPINDescriptors ())
              {
                if (upd.setPIN (pins[i++], true) != null)
                  {
                    error = "PIN return error";
                    break;
                  }
              }
            if (error == null)
              {
                i = 0;
                for (KeyCreationRequestDecoder.KeyObject ko : decoder.getKeyObjects ())
                  {
                    if (!ArrayUtil.compare (ko.getSKSPINValue (), pins[index[i++]].getBytes ("UTF-8")))
                      {
                        error = "Grouping problem";
                        break;
                      }
                  }
              }
          }
        if (error == null)
          {
            if (fail) throw new IOException ("Error expected");
          }
        else if (!fail)
          {
            throw new IOException ("Unexpected error: " + error);
          }
      }

    class Client
      {
        JSONDecoderCache client_xml_cache;
        
        int provisioning_handle;
        
        KeyCreationRequestDecoder key_creation_request;
        
        ProvisioningInitializationRequestDecoder prov_sess_req;
        
        InvocationRequestDecoder invocation_request;

        CredentialDiscoveryRequestDecoder cre_disc_req;
        
        DeviceInfo device_info;
        
        Client () throws IOException
          {
            client_xml_cache = new JSONDecoderCache ();
            client_xml_cache.addToCache (InvocationRequestDecoder.class);
            client_xml_cache.addToCache (ProvisioningInitializationRequestDecoder.class);
            client_xml_cache.addToCache (CredentialDiscoveryRequestDecoder.class);
            client_xml_cache.addToCache (KeyCreationRequestDecoder.class);
            client_xml_cache.addToCache (ProvisioningFinalizationRequestDecoder.class);
          }

        private void abort (String message) throws IOException, SKSException
          {
            sks.abortProvisioningSession (provisioning_handle);
            throw new IOException (message);
          }
        
        private void scanForKeyManagementKeyUpdates (ProvisioningInitializationRequestDecoder.KeyManagementKeyUpdateHolder kmk) throws SKSException
          {
            for (ProvisioningInitializationRequestDecoder.KeyManagementKeyUpdateHolder child : kmk.KeyManagementKeyUpdateHolders ())
              {
                scanForKeyManagementKeyUpdates (child);
                EnumeratedProvisioningSession old_provisioning_session = new EnumeratedProvisioningSession ();
                while ((old_provisioning_session = sks.enumerateProvisioningSessions (old_provisioning_session.getProvisioningHandle (), false)) != null)
                  {
                    if (child.getKeyManagementKey ().equals (old_provisioning_session.getKeyManagementKey ()))
                      {
                        sks.updateKeyManagementKey (old_provisioning_session.getProvisioningHandle (),
                                                    kmk.getKeyManagementKey (),
                                                    child.getAuthorization ());
                      }
                  }
              }
          }

        private void postProvisioning (ProvisioningFinalizationRequestDecoder.PostOperation post_operation, int handle) throws IOException, GeneralSecurityException
          {
            EnumeratedProvisioningSession old_provisioning_session = new EnumeratedProvisioningSession ();
            while (true)
              {
                if ((old_provisioning_session = sks.enumerateProvisioningSessions (old_provisioning_session.getProvisioningHandle (), false)) == null)
                  {
                    abort ("Old provisioning session not found:" + 
                           post_operation.getClientSessionId () + "/" +
                           post_operation.getServerSessionId ());
                  }
                if (old_provisioning_session.getClientSessionId ().equals(post_operation.getClientSessionId ()) &&
                    old_provisioning_session.getServerSessionId ().equals (post_operation.getServerSessionId ()))
                  {
                    break;
                  }
              }
            EnumeratedKey ek = new EnumeratedKey ();
            while (true)
              {
                if ((ek = sks.enumerateKeys (ek.getKeyHandle ())) == null)
                  {
                    abort ("Old key not found");
                  }
                if (ek.getProvisioningHandle () == old_provisioning_session.getProvisioningHandle ())
                  {
                    KeyAttributes ka = sks.getKeyAttributes (ek.getKeyHandle ());
                    if (ArrayUtil.compare (HashAlgorithms.SHA256.digest (ka.getCertificatePath ()[0].getEncoded ()), post_operation.getCertificateFingerprint ()))
                      {
                        switch (post_operation.getPostOperation ())
                          {
                            case ProvisioningFinalizationRequestDecoder.PostOperation.CLONE_KEY_PROTECTION:
                              sks.postCloneKeyProtection (handle, ek.getKeyHandle (), post_operation.getAuthorization (), post_operation.getMac ());
                              break;

                            case ProvisioningFinalizationRequestDecoder.PostOperation.UPDATE_KEY:
                              sks.postUpdateKey (handle, ek.getKeyHandle (),  post_operation.getAuthorization (), post_operation.getMac ());
                              break;

                            case ProvisioningFinalizationRequestDecoder.PostOperation.UNLOCK_KEY:
                              sks.postUnlockKey (handle, ek.getKeyHandle (),  post_operation.getAuthorization (), post_operation.getMac ());
                              break;

                            default:
                              sks.postDeleteKey (handle, ek.getKeyHandle (), post_operation.getAuthorization (), post_operation.getMac ());
                          }
                        return;
                      }
                  }
              }
          }

        ///////////////////////////////////////////////////////////////////////////////////
        // Get platform request and respond with SKS compatible data
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] invocationResponse (byte[] json_data) throws IOException
          {
            invocation_request = (InvocationRequestDecoder) client_xml_cache.parse (json_data);
            assertTrue ("Languages", invocation_request.getOptionalLanguageList () == null ^ languages);
            assertTrue ("Key containers", invocation_request.getOptionalKeyContainerList () == null ^ key_container_list);
            if (set_abort_url)
              {
                assertTrue ("Abort URL", invocation_request.getOptionalAbortUrl ().equals (ABORT_URL));
              }
            else
              {
                assertTrue ("Abort URL", invocation_request.getOptionalAbortUrl () == null);
              }
            device_info = sks.getDeviceInfo ();
            InvocationResponseEncoder invocation_response = new InvocationResponseEncoder (invocation_request);
            Vector<String> matches = new Vector<String> ();
            for (String want : invocation_request.getQueriedCapabilities ())
              {
                for (String have : device_info.getSupportedAlgorithms ())
                  {
                    if (have.equals (want))
                      {
                        matches.add (have);
                        break;
                      }
                  }
              }
            for (String algorithm : matches)
              {
                invocation_response.addSupportedFeature (algorithm);
              }
            if (invocation_request.getQueriedCapabilities ().contains (KeyGen2URIs.CLIENT_ATTRIBUTES.DEVICE_PIN_SUPPORT))
              {
                if (device_info.getDevicePinSupport ())
                  {
                    invocation_response.addSupportedFeature (KeyGen2URIs.CLIENT_ATTRIBUTES.DEVICE_PIN_SUPPORT);
                  }
              }
            if (invocation_request.getQueriedCapabilities ().contains (KeyGen2URIs.CLIENT_ATTRIBUTES.IMEI_NUMBER))
              {
                invocation_response.addClientValues (KeyGen2URIs.CLIENT_ATTRIBUTES.IMEI_NUMBER,
                                                     new String []{"490154203237518"});
              }
            if (invocation_request.getQueriedCapabilities ().contains (KeyGen2URIs.CLIENT_ATTRIBUTES.IP_ADDRESS))
              {
                invocation_response.addClientValues (KeyGen2URIs.CLIENT_ATTRIBUTES.IP_ADDRESS,
                                                     new String[]{"fe80::4465:62dc:5fa5:4766%10", "192.168.0.202"});
              }
            if (invocation_request.getQueriedCapabilities ().contains (KeyGen2URIs.LOGOTYPES.CARD))
              {
                invocation_response.addImagePreference (KeyGen2URIs.LOGOTYPES.CARD, "image/png", 200, 120);
              }
            if (invocation_request.getQueriedCapabilities ().contains (KeyGen2URIs.FEATURE.VIRTUAL_ENVIRONMENT))
              {
                invocation_response.addClientValues (KeyGen2URIs.FEATURE.VIRTUAL_ENVIRONMENT,
                                                     new String []{SPECIFIC_VM});
                byte[] nonce = new byte[16];
                new SecureRandom ().nextBytes (nonce);
                invocation_response.setNonce (nonce);
              }
            return invocation_response.serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }

        ///////////////////////////////////////////////////////////////////////////////////
        // Get provisioning session request and respond with ephemeral keys and and attest
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] provSessResponse (byte[] json_data) throws IOException
          {
            prov_sess_req = (ProvisioningInitializationRequestDecoder) client_xml_cache.parse (json_data);
            scanForKeyManagementKeyUpdates (prov_sess_req.getKeyManagementKeyUpdateHolderRoot ());
            assertTrue ("Submit URL", prov_sess_req.getSubmitUrl ().equals (ISSUER_URL));
            assertFalse ("VM", virtual_environment ^ ACME_INDUSTRIES.equals (prov_sess_req.getVirtualEnvironmentFriendlyName ()));
            Date client_time = new Date ();
            ProvisioningSession sess = 
                  sks.createProvisioningSession (prov_sess_req.getSessionKeyAlgorithm (),
                                                 invocation_request.getPrivacyEnabledFlag(),
                                                 prov_sess_req.getServerSessionId (),
                                                 prov_sess_req.getServerEphemeralKey (),
                                                 prov_sess_req.getSubmitUrl (), /* IssuerURI */
                                                 prov_sess_req.getKeyManagementKey (),
                                                 (int)(client_time.getTime () / 1000),
                                                 prov_sess_req.getSessionLifeTime (),
                                                 prov_sess_req.getSessionKeyLimit ());
            provisioning_handle = sess.getProvisioningHandle ();
            
            ProvisioningInitializationResponseEncoder prov_init_response = 
                  new ProvisioningInitializationResponseEncoder (prov_sess_req,
                                                                 sess.getClientEphemeralKey (),
                                                                 sess.getClientSessionId (),
                                                                 client_time,
                                                                 sess.getAttestation (),
                                                                 invocation_request.getPrivacyEnabledFlag () ? null : device_info.getCertificatePath ());
            if (https)
              {
                prov_init_response.setServerCertificate (server_certificate);
              }

            prov_init_response.setResponseSigner (new SymKeySignerInterface ()
              {
                public MACAlgorithms getMacAlgorithm () throws IOException
                  {
                    return MACAlgorithms.HMAC_SHA256;
                  }

                public byte[] signData (byte[] data) throws IOException
                  {
                    return sks.signProvisioningSessionData (provisioning_handle, data);
                  }
              });
            return prov_init_response.serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }

        ///////////////////////////////////////////////////////////////////////////////////
        // Get credential doscovery request
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] creDiscResponse (byte[] json_data) throws IOException, GeneralSecurityException
          {
            cre_disc_req = (CredentialDiscoveryRequestDecoder) client_xml_cache.parse (json_data);
            assertTrue ("Submit URL", cre_disc_req.getSubmitUrl ().equals (CRE_DISC_URL));
            CredentialDiscoveryResponseEncoder cdre = new CredentialDiscoveryResponseEncoder (cre_disc_req);
            for (CredentialDiscoveryRequestDecoder.LookupSpecifier ls : cre_disc_req.getLookupSpecifiers ())
              {
                CredentialDiscoveryResponseEncoder.LookupResult lr = cdre.addLookupResult (ls.getID ());
                EnumeratedProvisioningSession eps = new EnumeratedProvisioningSession ();
                while ((eps = sks.enumerateProvisioningSessions (eps.getProvisioningHandle (), false)) != null)
                  {
                    if (ls.getKeyManagementKey ().equals (eps.getKeyManagementKey ()))
                      {
                        EnumeratedKey ek = new EnumeratedKey ();
                        while ((ek = sks.enumerateKeys (ek.getKeyHandle ())) != null)
                          {
                            if (ek.getProvisioningHandle () == eps.getProvisioningHandle ())
                              {
                                KeyAttributes ka = sks.getKeyAttributes (ek.getKeyHandle ());
                                X509Certificate[] cert_path = ka.getCertificatePath ();
                                if (ls.matches (cert_path))
                                  {
                                    KeyProtectionInfo kpi = sks.getKeyProtectionInfo (ek.getKeyHandle ()); 
                                    if ((ls.getGrouping () == null || ls.getGrouping () == kpi.getPinGrouping ()) &&
                                        (ls.getAppUsage () == null || ls.getAppUsage () == ka.getAppUsage ()))
                                      {
                                        lr.addMatchingCredential (cert_path,
                                                                  eps.getClientSessionId (),
                                                                  eps.getServerSessionId (),
                                                                  kpi.isPinBlocked ());
                                      }
                                  }
                              }
                          }
                      }
                  }
              }
            return cdre.serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }

        ///////////////////////////////////////////////////////////////////////////////////
        // Get key initialization request and respond with freshly generated public keys
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] keyCreResponse (byte[] json_data) throws IOException
          {
            key_creation_request = (KeyCreationRequestDecoder) client_xml_cache.parse (json_data);
            assertTrue ("Submit URL", key_creation_request.getSubmitUrl ().equals (KEY_INIT_URL));
            KeyCreationResponseEncoder key_creation_response = new KeyCreationResponseEncoder (key_creation_request);
            for (KeyCreationRequestDecoder.UserPINDescriptor upd : key_creation_request.getUserPINDescriptors ())
              {
                upd.setPIN (new String (USER_DEFINED_PIN, "UTF-8"), true);
              }
            int pin_policy_handle = 0;
            int puk_policy_handle = 0;
            for (KeyCreationRequestDecoder.KeyObject key : key_creation_request.getKeyObjects ())
              {
                if (key.getPINPolicy () == null)
                  {
                    pin_policy_handle = 0;
                    puk_policy_handle = 0;
                  }
                else
                  {
                    if (key.isStartOfPINPolicy ())
                      {
                        if (key.isStartOfPUKPolicy ())
                          {
                            KeyCreationRequestDecoder.PUKPolicy puk_policy = key.getPINPolicy ().getPUKPolicy ();
                            puk_policy_handle = sks.createPukPolicy (provisioning_handle, 
                                                                     puk_policy.getID (),
                                                                     puk_policy.getEncryptedValue (),
                                                                     puk_policy.getFormat ().getSksValue (),
                                                                     puk_policy.getRetryLimit (),
                                                                     puk_policy.getMac());
                          }
                        KeyCreationRequestDecoder.PINPolicy pin_policy = key.getPINPolicy ();
                        pin_policy_handle = sks.createPinPolicy (provisioning_handle,
                                                                 pin_policy.getID (),
                                                                 puk_policy_handle,
                                                                 pin_policy.getUserDefinedFlag (),
                                                                 pin_policy.getUserModifiableFlag (),
                                                                 pin_policy.getFormat ().getSksValue (),
                                                                 pin_policy.getRetryLimit (),
                                                                 pin_policy.getGrouping ().getSksValue (),
                                                                 PatternRestriction.getSksValue (pin_policy.getPatternRestrictions ()),
                                                                 pin_policy.getMinLength (),
                                                                 pin_policy.getMaxLength (),
                                                                 pin_policy.getInputMethod ().getSksValue (),
                                                                 pin_policy.getMac ());
                      }
                  }
                KeyData key_data = sks.createKeyEntry (provisioning_handle,
                                                       key.getID (),
                                                       key_creation_request.getKeyEntryAlgorithm (),
                                                       key.getServerSeed (),
                                                       key.isDevicePINProtected (),
                                                       pin_policy_handle,
                                                       key.getSKSPINValue (),
                                                       key.getEnablePINCachingFlag (),
                                                       key.getBiometricProtection ().getSksValue (),
                                                       key.getExportProtection ().getSksValue (),
                                                       key.getDeleteProtection ().getSksValue (),
                                                       key.getAppUsage ().getSksValue (),
                                                       key.getFriendlyName (),
                                                       key.getKeySpecifier ().getKeyAlgorithm ().getURI (),
                                                       key.getKeySpecifier ().getKeyParameters (),
                                                       key.getEndorsedAlgorithms (),
                                                       key.getMac ());
                key_creation_response.addPublicKey (key_data.getPublicKey (),
                                                    key_data.getAttestation (),
                                                    key.getID ());
              }
            return key_creation_response.serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }

        ///////////////////////////////////////////////////////////////////////////////////
        // Get the certificates and attributes and return a success message
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] creFinalizeResponse (byte[] json_data) throws IOException, GeneralSecurityException
          {
            ProvisioningFinalizationRequestDecoder prov_final_request =
                           (ProvisioningFinalizationRequestDecoder) client_xml_cache.parse (json_data);
            assertTrue ("Submit URL", prov_final_request.getSubmitUrl ().equals (FIN_PROV_URL));
            /* 
               Note: we could have used the saved provisioning_handle but that would not
               work for certifications that are delayed.  The following code is working
               for fully interactive and delayed scenarios by using SKS as state-holder
            */
            EnumeratedProvisioningSession eps = new EnumeratedProvisioningSession ();
            while (true)
              {
                if ((eps = sks.enumerateProvisioningSessions (eps.getProvisioningHandle (), true)) == null)
                  {
                    abort ("Provisioning session not found:" + 
                           prov_final_request.getClientSessionId () + "/" +
                           prov_final_request.getServerSessionId ());
                  }
                if (eps.getClientSessionId ().equals(prov_final_request.getClientSessionId ()) &&
                    eps.getServerSessionId ().equals (prov_final_request.getServerSessionId ()))
                  {
                    break;
                  }
              }
            
            //////////////////////////////////////////////////////////////////////////
            // Final check, do these keys match the request?
            // Missing credentials will be found by SKS during session termination
            //////////////////////////////////////////////////////////////////////////
            for (ProvisioningFinalizationRequestDecoder.IssuedCredential key : prov_final_request.getIssuedCredentials ())
              {
                int key_handle = sks.getKeyHandle (eps.getProvisioningHandle (), key.getId ());
                sks.setCertificatePath (key_handle, key.getCertificatePath (), key.getMac ());

                //////////////////////////////////////////////////////////////////////////
                // There may be a symmetric key
                //////////////////////////////////////////////////////////////////////////
                if (key.getOptionalSymmetricKey () != null)
                  {
                    sks.importSymmetricKey (key_handle, 
                                            key.getOptionalSymmetricKey (),
                                            key.getSymmetricKeyMac ());
                  }

                //////////////////////////////////////////////////////////////////////////
                // There may be a private key
                //////////////////////////////////////////////////////////////////////////
                if (key.getOptionalPrivateKey () != null)
                  {
                    sks.importPrivateKey (key_handle, 
                                          key.getOptionalPrivateKey (),
                                          key.getPrivateKeyMac ());
                  }

                //////////////////////////////////////////////////////////////////////////
                // There may be extensions
                //////////////////////////////////////////////////////////////////////////
                for (ProvisioningFinalizationRequestDecoder.Extension extension : key.getExtensions ())
                  {
                    sks.addExtension (key_handle,
                                      extension.getExtensionType (),
                                      extension.getSubType (), 
                                      extension.getQualifier (),
                                      extension.getExtensionData (),
                                      extension.getMac ());
                  }

                //////////////////////////////////////////////////////////////////////////
                // There may be a postUpdateKey or postCloneKeyProtection
                //////////////////////////////////////////////////////////////////////////
                ProvisioningFinalizationRequestDecoder.PostOperation post_operation = key.getPostOperation ();
                if (post_operation != null)
                  {
                    postProvisioning (post_operation, key_handle);
                  }
              }

            //////////////////////////////////////////////////////////////////////////
            // There may be any number of postUnlockKey
            //////////////////////////////////////////////////////////////////////////
            for (ProvisioningFinalizationRequestDecoder.PostOperation post_unl : prov_final_request.getPostUnlockKeys ())
              {
                postProvisioning (post_unl, eps.getProvisioningHandle ());
              }

            //////////////////////////////////////////////////////////////////////////
            // There may be any number of postDeleteKey
            //////////////////////////////////////////////////////////////////////////
            for (ProvisioningFinalizationRequestDecoder.PostOperation post_del : prov_final_request.getPostDeleteKeys ())
              {
                postProvisioning (post_del, eps.getProvisioningHandle ());
              }

            //////////////////////////////////////////////////////////////////////////
            // Create final and attested message
            //////////////////////////////////////////////////////////////////////////
            ProvisioningFinalizationResponseEncoder fin_prov_response = 
                new ProvisioningFinalizationResponseEncoder (prov_final_request,
                                                             sks.closeProvisioningSession (eps.getProvisioningHandle (),
                                                                                           prov_final_request.getCloseSessionNonce (),
                                                                                           prov_final_request.getCloseSessionMac ()));
            return fin_prov_response.serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }
      }
    
    class Server
      {
        static final String LOGO_URL = "http://issuer.example.com/images/logo.png";
        static final String LOGO_MIME = "image/png";
        byte[] LOGO_SHA256 = {0,5,6,6,0,5,6,6,0,5,6,6,0,5,6,6,0,5,6,6,0,5,6,6,0,5,6,6,0,5,6,6}; 
        static final int LOGO_WIDTH = 200;
        static final int LOGO_HEIGHT = 150;
        
        JSONDecoderCache server_xml_cache;
        
        int pin_retry_limit = 3;

        ServerState server_state;
        
        PrivateKey gen_private_key;
        
        PublicKey gen_public_key;
        
        PublicKey server_km;
        
        SoftHSM server_crypto_interface = new SoftHSM ();
        
        String aborted;

        Server () throws Exception
          {
            server_xml_cache = new JSONDecoderCache ();
            server_xml_cache.addToCache (InvocationResponseDecoder.class);
            server_xml_cache.addToCache (ProvisioningInitializationResponseDecoder.class);
            server_xml_cache.addToCache (CredentialDiscoveryResponseDecoder.class);
            server_xml_cache.addToCache (KeyCreationResponseDecoder.class);
            server_xml_cache.addToCache (ProvisioningFinalizationResponseDecoder.class);
          }
        
        void getProvSess (JSONDecoder xml_object) throws IOException
          {
            ////////////////////////////////////////////////////////////////////////////////////
            // Begin by creating the "SessionKey" that holds the key to just about everything
            ////////////////////////////////////////////////////////////////////////////////////
            ProvisioningInitializationResponseDecoder prov_init_response = (ProvisioningInitializationResponseDecoder) xml_object;

            ////////////////////////////////////////////////////////////////////////////////////
            // Update the container state.  This is where the action is
            ////////////////////////////////////////////////////////////////////////////////////
            server_state.update (prov_init_response, https ? server_certificate : null);

            ////////////////////////////////////////////////////////////////////////////////////
            // Here we could/should introduce an SKS identity/brand check
            ////////////////////////////////////////////////////////////////////////////////////
            X509Certificate[] certificate_path = prov_init_response.getDeviceCertificatePath ();
          }
        
        //////////////////////////////////////////////////////////////////////////////////
        // Create platform negotiation request for the client
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] invocationRequest () throws IOException, GeneralSecurityException
          {
            ////////////////////////////////////////////////////////////////////////////////////
            // Create the state container
            ////////////////////////////////////////////////////////////////////////////////////
            server_state = new ServerState (server_crypto_interface);
            if (privacy_enabled)
              {
                server_state.setPrivacyEnabled (true);
              }
            if (brain_pool)
              {
                server_state.setEphemeralKeyAlgorithm (KeyAlgorithms.BRAINPOOL_P_256);
              }
            if (languages)
              {
                server_state.setPreferredLanguages (new String[]{"en","de","fr"});
              }
            if (key_container_list)
              {
                server_state.setTargetKeyContainerList (new KeyContainerTypes[]{KeyContainerTypes.SOFTWARE,KeyContainerTypes.EMBEDDED});
              }

            ////////////////////////////////////////////////////////////////////////////////////
            // First keygen2 request
            ////////////////////////////////////////////////////////////////////////////////////
//            String server_session_id = "S-" + Long.toHexString (new Date().getTime()) + Long.toHexString(new SecureRandom().nextLong());
            InvocationRequestEncoder invocation_request =  new InvocationRequestEncoder (server_state, INVOCATION_URL, null);
            if (set_abort_url)
              {
                invocation_request.setAbortUrl (ABORT_URL);
              }
            if (ask_for_4096)
              {
                server_state.addFeatureQuery (KeyAlgorithms.RSA4096.getURI ())
                            .addFeatureQuery (KeyAlgorithms.RSA2048.getURI ());
              }
            if (ask_for_exponent)
              {
                server_state.addFeatureQuery (KeyAlgorithms.RSA2048_EXP.getURI ());
              }
            if (device_pin_protection)
              {
                server_state.addFeatureQuery (KeyGen2URIs.CLIENT_ATTRIBUTES.DEVICE_PIN_SUPPORT);
              }
            if (image_prefs)
              {
                server_state.addImageAttributesQuery (KeyGen2URIs.LOGOTYPES.CARD)
                            .addImageAttributesQuery (KeyGen2URIs.LOGOTYPES.LIST);
                
              }
            if (get_client_attributes)
              {
                server_state.addValuesQuery (KeyGen2URIs.CLIENT_ATTRIBUTES.IMEI_NUMBER)
                            .addValuesQuery (KeyGen2URIs.CLIENT_ATTRIBUTES.IP_ADDRESS)
                            .addValuesQuery (KeyGen2URIs.CLIENT_ATTRIBUTES.MAC_ADDRESS);
              }
            if (plain_unlock_key != null)
              {
                invocation_request.setAction (Action.UNLOCK);
              }
            if (virtual_environment)
              {
                server_state.addValuesQuery (KeyGen2URIs.FEATURE.VIRTUAL_ENVIRONMENT);
                KeyStoreSigner signer = new KeyStoreSigner (DemoKeyStore.getExampleDotComKeyStore (), null);
                signer.setKey (null, DemoKeyStore.getSignerPassword ());
                invocation_request.setRequestSigner (signer);
              }
            return invocation_request.serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }

        //////////////////////////////////////////////////////////////////////////////////
        // Create a provisioning session request for the client
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] provSessRequest (byte[] json_data) throws IOException, GeneralSecurityException
          {
            InvocationResponseDecoder invocation_response = (InvocationResponseDecoder) server_xml_cache.parse (json_data);
            server_state.update (invocation_response);
            if (ask_for_exponent)
              {
                if (server_state.isFeatureSupported (KeyAlgorithms.RSA2048_EXP.getURI ()))
                  {
                    ask_for_exponent = true;
                  }
              }
            if (ask_for_4096)
              {
                ask_for_4096 = false;
                if (server_state.isFeatureSupported (KeyAlgorithms.RSA4096.getURI ()))
                  {
                    ask_for_4096 = true;
                  }
              }
            if (device_pin_protection)
              {
                if (!server_state.isFeatureSupported (KeyGen2URIs.CLIENT_ATTRIBUTES.DEVICE_PIN_SUPPORT))
                  {
                    aborted = "No device PIN support";
                    return null;
                  }
              }

            ProvisioningInitializationRequestEncoder prov_init_request = 
                 new ProvisioningInitializationRequestEncoder (server_state, ISSUER_URL, 10000, (short)50);
            if (updatable)
              {
                ProvisioningInitializationRequestEncoder.KeyManagementKeyUpdateHolder kmk = 
                     prov_init_request.setKeyManagementKey (server_km = server_crypto_interface.enumerateKeyManagementKeys ()[ecc_kmk ? 2 : 0]);
                if (update_kmk)
                  {
                    kmk.update (server_crypto_interface.enumerateKeyManagementKeys ()[1])
                       .update (update_key.server_km);
                  }
              }
            if (virtual_environment)
              {
                prov_init_request.setVirtualEnvironment (VM_CONFIG_DATA, SPECIFIC_VM, ACME_INDUSTRIES);
                KeyStoreSigner signer = new KeyStoreSigner (DemoKeyStore.getExampleDotComKeyStore (), null);
                signer.setKey (null, DemoKeyStore.getSignerPassword ());
                prov_init_request.setRequestSigner (signer);
              }
            return prov_init_request.serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }

        ///////////////////////////////////////////////////////////////////////////////////
        // Create credential discover request for the client
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] creDiscRequest (byte[] json_data) throws IOException, GeneralSecurityException
          {
            getProvSess (server_xml_cache.parse (json_data));
            CredentialDiscoveryRequestEncoder cdre = new CredentialDiscoveryRequestEncoder (server_state, CRE_DISC_URL);
            cdre.addLookupDescriptor (server_crypto_interface.enumerateKeyManagementKeys ()[0]);

            cdre.addLookupDescriptor (server_crypto_interface.enumerateKeyManagementKeys ()[2])
                          .setEmail ("john.doe@example.com");

            cdre.addLookupDescriptor (server_crypto_interface.enumerateKeyManagementKeys ()[2])
                          .setEmail ("jane.doe@example.com");

            cdre.addLookupDescriptor (server_crypto_interface.enumerateKeyManagementKeys ()[1])
                          .setIssuedBefore (new Date (new Date ().getTime () - 100000))
                          .setIssuedAfter (new Date ())
                          .setGrouping (Grouping.SHARED)
                          .setAppUsage (AppUsage.UNIVERSAL)
                          .setFingerPrint (HashAlgorithms.SHA256.digest (TEST_STRING))
                          .setIssuer (new X500Principal ("CN=Root CA"))
                          .setSerialNumber (new BigInteger ("123"))
                          .setSubject (new X500Principal ("CN=John,serialNumber=123"))
                          .setEmail ("john.doe@example.com")
                          .setPolicyRules (new String[]{"5.4.8","-5.4.9"})
                          .setKeyUsageRules (new KeyUsageBits[]{},new KeyUsageBits[]{KeyUsageBits.KEY_CERT_SIGN})
                          .setExtendedKeyUsageRules (new String[]{"1.3.6.1.5.5.7.3.2","1.3.6.1.5.5.7.3.4"});
            return cdre.serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }

        ///////////////////////////////////////////////////////////////////////////////////
        // Create a key creation request for the client
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] keyCreRequest (byte[] json_data) throws IOException, GeneralSecurityException
          {
            JSONDecoder xml_object = server_xml_cache.parse (json_data);
            if (xml_object instanceof ProvisioningInitializationResponseDecoder)
              {
                getProvSess (xml_object);
              }
            else
              {
                CredentialDiscoveryResponseDecoder cdrd = (CredentialDiscoveryResponseDecoder) xml_object;
                server_state.update (cdrd);
                CredentialDiscoveryResponseDecoder.LookupResult[] lres = cdrd.getLookupResults ();
// TODO verify
              }

            ServerState.PINPolicy pin_policy = null;

            ServerState.PUKPolicy puk_policy = null;
            
            if (puk_protection)
              {
                puk_policy =
                    server_state.createPUKPolicy (new byte[]{'0','1','2','3','4','5','6', '7','8','9'},
                                                  PassphraseFormat.NUMERIC,
                                                  3);
              }
            if (pin_protection)
              {
                pin_policy = server_state.createPINPolicy (PassphraseFormat.NUMERIC,
                                                           4,
                                                           8,
                                                           pin_retry_limit,
                                                           puk_policy);
                if (add_pin_pattern)
                  {
                    pin_policy.addPatternRestriction (PatternRestriction.THREE_IN_A_ROW);
                    pin_policy.addPatternRestriction (PatternRestriction.SEQUENCE);
                  }
                if (pin_group_shared)
                  {
                    pin_policy.setGrouping (Grouping.SHARED);
                  }
                if (input_method != null)
                  {
                    pin_policy.setInputMethod (input_method);
                  }
                if (fixed_pin)
                  {
                    pin_policy.setUserModifiable (false);
                  }
              }
            KeySpecifier key_alg = null;
            if (ecc_key)
              {
                key_alg = new KeySpecifier (KeyAlgorithms.NIST_P_256);
              }
            else if (ask_for_exponent)
              {
                key_alg = new KeySpecifier (KeyAlgorithms.RSA2048_EXP, 3);
              }
            else
              {
                key_alg = new KeySpecifier (ask_for_4096 ? KeyAlgorithms.RSA4096 : KeyAlgorithms.RSA2048);
              }

            ServerState.Key kp = device_pin_protection ?
                server_state.createDevicePINProtectedKey (AppUsage.AUTHENTICATION, key_alg) :
                  preset_pin ? server_state.createKeyWithPresetPIN (encryption_key ? AppUsage.ENCRYPTION : AppUsage.AUTHENTICATION,
                                                                               key_alg, pin_policy,
                                                                               PREDEF_SERVER_PIN)
                             :
            server_state.createKey (encryption_key || key_agreement? AppUsage.ENCRYPTION : AppUsage.AUTHENTICATION,
                                               key_alg,
                                               pin_policy);
            if (symmetric_key || encryption_key)
              {
                kp.setEndorsedAlgorithms (new String[]{encryption_key ? SymEncryptionAlgorithms.AES256_CBC.getURI () : MACAlgorithms.HMAC_SHA1.getURI ()});
                kp.setSymmetricKey (encryption_key ? AES32BITKEY : OTP_SEED);
              }
            if (key_agreement)
              {
                kp.setEndorsedAlgorithms (new String[]{SecureKeyStore.ALGORITHM_ECDH_RAW});
              }
            if (property_bag)
              {
                kp.addPropertyBag ("http://host/prop")
                  .addProperty ("main", "234", false)
                  .addProperty ("a", "fun", true);
              }
            if (encrypted_extension)
              {
                kp.addEncryptedExtension ("http://host/ee", new byte[]{0,5});
              }
            if (standard_extension)
              {
                kp.addExtension ("http://host/ee", new byte[]{0,5});
              }
            ServerState.ImagePreference im_pref = server_state.getImagePreference (KeyGen2URIs.LOGOTYPES.CARD); 
            if (im_pref != null)
              {
                kp.addLogotype (KeyGen2URIs.LOGOTYPES.CARD, new ImageData (new byte[]{8,6,4,4}, im_pref.getMimeType ()));
              }
            if (export_protection != null)
              {
                kp.setExportProtection (export_protection);
              }
            if (delete_protection != null)
              {
                kp.setDeleteProtection (delete_protection);
              }
            if (enable_pin_caching)
              {
                kp.setEnablePINCaching (true);
              }
            if (server_seed)
              {
                byte[] seed = new byte[32];
                new SecureRandom ().nextBytes (seed);
                kp.setServerSeed (seed);
              }
            if (clone_key_protection != null)
              {
                kp.setClonedKeyProtection (clone_key_protection.server_state.getClientSessionId (), 
                                           clone_key_protection.server_state.getServerSessionId (),
                                           clone_key_protection.server_state.getKeys ()[0].getCertificatePath ()[0],
                                           clone_key_protection.server_km);
              }
            if (update_key != null)
              {
                kp.setUpdatedKey (update_key.server_state.getClientSessionId (), 
                                  update_key.server_state.getServerSessionId (),
                                  update_key.server_state.getKeys ()[0].getCertificatePath ()[0],
                                  update_kmk ? server_km : update_key.server_km);
              }
            if (delete_key != null)
              {
                server_state.addPostDeleteKey (delete_key.server_state.getClientSessionId (), 
                                               delete_key.server_state.getServerSessionId (),
                                               delete_key.server_state.getKeys ()[0].getCertificatePath ()[0],
                                               delete_key.server_km);
              }
            if (two_keys)
              {
                ServerState.Key key = server_state.createKey (AppUsage.SIGNATURE, new KeySpecifier (KeyAlgorithms.NIST_P_256), pin_policy);
                if (custom_key_name)
                  {
                    key.setID ("MyKeyName");
                  }
              }

            return new KeyCreationRequestEncoder (server_state, KEY_INIT_URL).serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }


        ///////////////////////////////////////////////////////////////////////////////////
        // Get the key create response and respond with certified public keys and attributes
        ///////////////////////////////////////////////////////////////////////////////////
        byte[] creFinalizeRequest (byte[] json_data) throws IOException, GeneralSecurityException
          {
            if (plain_unlock_key == null)
              {
                boolean temp_set_private_key = set_private_key;
                boolean otp = symmetric_key && !encryption_key;
                KeyCreationResponseDecoder key_init_response = (KeyCreationResponseDecoder) server_xml_cache.parse (json_data);
                server_state.update (key_init_response);
                for (ServerState.Key key_prop : server_state.getKeys ())
                  {
                    boolean auth = key_prop.getAppUsage () == AppUsage.AUTHENTICATION;
                    CertSpec cert_spec = new CertSpec ();
                    if (!otp)
                      {
                        // OTP certificates are just for transport
                        cert_spec.setEndEntityConstraint ();
                        if (auth)
                          {
                            cert_spec.setKeyUsageBit (KeyUsageBits.DIGITAL_SIGNATURE);
                            cert_spec.setKeyUsageBit (KeyUsageBits.KEY_AGREEMENT);
                          }
                        else
                          {
                            cert_spec.setKeyUsageBit (KeyUsageBits.DATA_ENCIPHERMENT);
                            cert_spec.setKeyUsageBit (KeyUsageBits.KEY_ENCIPHERMENT);
                          }
                      }
                    String extra = get_client_attributes ? ", SerialNumber=" + server_state.getValuesCapability (KeyGen2URIs.CLIENT_ATTRIBUTES.IMEI_NUMBER)[0] : "";
                    cert_spec.setSubject ("CN=KeyGen2 " + _name.getMethodName() + ", E=john.doe@example.com" +
                                          (otp ? ", OU=OTP Key" : extra));
                    otp = false;
    
                    GregorianCalendar start = new GregorianCalendar ();
                    GregorianCalendar end = (GregorianCalendar) start.clone ();
                    end.set (GregorianCalendar.YEAR, end.get (GregorianCalendar.YEAR) + 25);
    
                    gen_public_key =  key_prop.getPublicKey ();
    
                    if (temp_set_private_key)
                      {
                        KeyPairGenerator generator = KeyPairGenerator.getInstance (ecc_key ? "EC" :"RSA");
                        if (ecc_key)
                          {
                            generator.initialize (new ECGenParameterSpec ("P-256"), new SecureRandom ());
                          }
                        else
                          {
                            generator.initialize (new RSAKeyGenParameterSpec (1024, RSAKeyGenParameterSpec.F4), new SecureRandom ());
                          }
                        KeyPair kp = generator.generateKeyPair();
                        gen_public_key = kp.getPublic ();
                        gen_private_key = kp.getPrivate ();
                      }
    
                    Vector<X509Certificate> cert_path = new Vector<X509Certificate> ();
                    cert_path.add (new CA ().createCert (cert_spec,
                                                         DistinguishedName.subjectDN ((X509Certificate)DemoKeyStore.getSubCAKeyStore ().getCertificate ("mykey")),
                                                         new BigInteger (String.valueOf (new Date ().getTime ())),
                                                         start.getTime (),
                                                         end.getTime (), 
                                                         AsymSignatureAlgorithms.RSA_SHA256,
                                                         new AsymKeySignerInterface ()
                        {
    
                          @Override
                          public PublicKey getPublicKey () throws IOException
                            {
                              try
                                {
                                  return ((X509Certificate)DemoKeyStore.getSubCAKeyStore ().getCertificate ("mykey")).getPublicKey ();
                                }
                              catch (KeyStoreException e)
                                {
                                  throw new IOException (e);
                                }
                            }
    
                          @Override
                          public byte[] signData (byte[] data, AsymSignatureAlgorithms algorithm) throws IOException
                            {
                              try
                                {
                                  return new SignatureWrapper (algorithm, (PrivateKey) DemoKeyStore.getSubCAKeyStore ().getKey ("mykey", DemoKeyStore.getSignerPassword ().toCharArray ()))
                                      .update (data)
                                      .sign ();
                                }
                              catch (GeneralSecurityException e)
                                {
                                  throw new IOException (e);
                                }
                            }
                          
                        }, gen_public_key));

                    if (set_trust_anchor)
                      {
                        for (Certificate certificate : DemoKeyStore.getSubCAKeyStore ().getCertificateChain ("mykey"))
                          {
                            cert_path.add ((X509Certificate) certificate);
                          }
                        key_prop.setTrustAnchor (true);
                      }

                    key_prop.setCertificatePath (cert_path.toArray (new X509Certificate[0]));
    
                    if (temp_set_private_key)
                      {
                        key_prop.setPrivateKey (gen_private_key.getEncoded ());
                        temp_set_private_key = false;
                      }
    
                  }
              }
            else
              {
                CredentialDiscoveryResponseDecoder cdrd = (CredentialDiscoveryResponseDecoder) server_xml_cache.parse (json_data);
                server_state.update (cdrd);
                CredentialDiscoveryResponseDecoder.LookupResult[] lres = cdrd.getLookupResults ();
// TODO verify
                server_state.addPostUnlockKey (plain_unlock_key.server_state.getClientSessionId (), 
                                               plain_unlock_key.server_state.getServerSessionId (),
                                               plain_unlock_key.server_state.getKeys ()[0].getCertificatePath ()[0],
                                               plain_unlock_key.server_km);
              }

            return new ProvisioningFinalizationRequestEncoder (server_state, FIN_PROV_URL).serializeJSONDocument (JSONOutputFormats.PRETTY_PRINT);
          }

        ///////////////////////////////////////////////////////////////////////////////////
        // Finally we get the attestested response
        ///////////////////////////////////////////////////////////////////////////////////
        void creFinalizeResponse (byte[] json_data) throws IOException
          {
            ProvisioningFinalizationResponseDecoder prov_final_response = (ProvisioningFinalizationResponseDecoder) server_xml_cache.parse (json_data);
            server_state.update (prov_final_response);

            ///////////////////////////////////////////////////////////////////////////////////
            // Just a small consistency check
            ///////////////////////////////////////////////////////////////////////////////////
            ByteArrayOutputStream baos = new ByteArrayOutputStream ();
            new ObjectOutputStream (baos).writeObject (server_state);
            byte[] serialized = baos.toByteArray ();
            try
              {
                ServerState scs = (ServerState) new ObjectInputStream (new ByteArrayInputStream (serialized)).readObject ();
              }
            catch (ClassNotFoundException e)
              {
                throw new IOException (e);
              }
          }
      }
    
    class Doer
      {
        Server server;
        Client client;
        JSONDecoderCache xmlschemas = new JSONDecoderCache ();
        int pass;
        
        private void write (byte[] data) throws Exception
          {
            if (fos != null)
              {
                for (int i = 0; i < data.length; i++)
                  {
                    byte b = data[i];
                    if (b == '\n')
                      {
                        fos.write ("<br>".getBytes ("UTF-8"));
                      }
                    else
                      {
                        fos.write (b);
                      }
                  }
              }
          }
        
        private void write (int b) throws Exception
          {
            write (new byte[]{(byte)b}); 
          }
        
        private void writeString (String message) throws Exception
          {
            write (message.getBytes ("UTF-8"));
          }
        
        private void writeOption (String option, boolean writeit) throws Exception
          {
            if (writeit)
              {
                writeString (option);
                write ('\n');
              }
          }
        
        byte[] fileLogger (byte[] json_data) throws Exception
          {
            if (json_data == null)
              {
                return new byte[0];
              }
            JSONDecoder xo = xmlschemas.parse (json_data);
            writeString ("&nbsp;<br><table><tr><td bgcolor=\"#F0F0F0\" style=\"border:solid;border-width:1px;padding:4px\">&nbsp;Pass #" + 
                         (++pass) +
                         ":&nbsp;" + 
                         xo.getQualifier () +
                         "<a id=\"" +
                         xo.getQualifier() +
                         "." +
                         _name.getMethodName() +
                         "." +
                         round +
                         "\"/>&nbsp;</td></tr></table><p style=\"padding-left:10pt\">");
            fos.write (xo.serializeJSONDecoder (JSONOutputFormats.PRETTY_HTML));
            writeString ("</p>");
            return json_data;
          }

        
        Doer () throws Exception
          {
            xmlschemas.addToCache (InvocationRequestDecoder.class);
            xmlschemas.addToCache (InvocationResponseDecoder.class);
            xmlschemas.addToCache (ProvisioningInitializationRequestDecoder.class);
            xmlschemas.addToCache (ProvisioningInitializationResponseDecoder.class);
            xmlschemas.addToCache (CredentialDiscoveryRequestDecoder.class);
            xmlschemas.addToCache (CredentialDiscoveryResponseDecoder.class);
            xmlschemas.addToCache (KeyCreationRequestDecoder.class);
            xmlschemas.addToCache (KeyCreationResponseDecoder.class);
            xmlschemas.addToCache (ProvisioningFinalizationRequestDecoder.class);
            xmlschemas.addToCache (ProvisioningFinalizationResponseDecoder.class);
          }
        
        void perform () throws Exception
          {
            writeString ("<b>");
            writeString ("Begin Test (" + _name.getMethodName() + ":" + (++round) + ")</b><br>");
            writeOption ("4096 over 2048 RSA key preference", ask_for_4096);
            writeOption ("RSA key with custom exponent", ask_for_exponent);
            writeOption ("Get client attributes", get_client_attributes);
            writeOption ("Client shows one image preference", image_prefs);
            writeOption ("PUK Protection", puk_protection);
            writeOption ("PIN Protection ", pin_protection);
            writeOption ("PIN Input Method ", input_method != null);
            writeOption ("Device PIN", device_pin_protection);
            writeOption ("Preset PIN", preset_pin);
            writeOption ("Enable PIN Caching", enable_pin_caching);
            writeOption ("PIN patterns", add_pin_pattern);
            writeOption ("Fixed PIN", fixed_pin);
            writeOption ("Privacy Enabled", privacy_enabled);
            writeOption ("ECC Key", ecc_key);
            writeOption ("Languages", languages);
            writeOption ("Server Seed", server_seed);
            writeOption ("PropertyBag", property_bag);
            writeOption ("Symmetric Key", symmetric_key);
            writeOption ("Encryption Key", encryption_key);
            writeOption ("Encrypted Extension", encrypted_extension);
            writeOption ("Standard Extension", standard_extension);
            writeOption ("Delete Protection", delete_protection != null);
            writeOption ("Export Protection", export_protection != null);
            writeOption ("Private Key Import", set_private_key);
            writeOption ("Updatable Session", updatable);
            writeOption ("CloneKeyProtection", clone_key_protection != null);
            writeOption ("UpdateKey", update_key != null);
            writeOption ("DeleteKey", delete_key != null);
            writeOption ("UnlockKey", plain_unlock_key != null);
            writeOption ("ECC KMK", ecc_kmk);
            writeOption ("Update KMK", update_kmk);
            writeOption ("Multiple Keys", two_keys);
            writeOption ("Custom Key Name", custom_key_name);
            writeOption ("Brainpool EC", brain_pool);
            writeOption ("HTTPS server certificate", https);
            writeOption ("TrustAnchor option", set_trust_anchor);
            writeOption ("Abort URL option", set_abort_url);
            writeOption ("Virtual Environment option", virtual_environment);
            server = new Server ();
            client = new Client ();
            byte[] json;
            json = fileLogger (server.invocationRequest ());
            json = fileLogger (client.invocationResponse (json));
            json = fileLogger (server.provSessRequest (json));
            if (server.aborted != null)
              {
                writeString ("<b>Test Aborted: " + server.aborted + "</b>\n&nbsp;\n");
                return;
              }
            json = fileLogger (client.provSessResponse (json));
            if (delete_key != null || clone_key_protection != null || update_key != null || plain_unlock_key != null)
              {
                json = fileLogger (server.creDiscRequest (json));
                json = fileLogger (client.creDiscResponse (json));
              }
            if (plain_unlock_key == null)
              {
                json = fileLogger (server.keyCreRequest (json));
                json = fileLogger (client.keyCreResponse (json));
              }
            json = fileLogger (server.creFinalizeRequest (json));
            json = fileLogger (client.creFinalizeResponse (json));
            server.creFinalizeResponse (json);
            writeString ("\n");
            EnumeratedKey ek = new EnumeratedKey ();
            while ((ek = sks.enumerateKeys (ek.getKeyHandle ())) != null)
              {
                if (ek.getProvisioningHandle () == client.provisioning_handle)
                  {
                    KeyAttributes ka = sks.getKeyAttributes (ek.getKeyHandle ());
                    writeString ("Deployed key[" + ek.getKeyHandle () + "] " + CertificateUtil.convertRFC2253ToLegacy (ka.getCertificatePath ()[0].getSubjectX500Principal ().getName ()) + "\n");
                  }
              }
            writeString ("\n\n");
         }
        
        int getFirstKey () throws Exception
          {
            EnumeratedKey ek = new EnumeratedKey ();
            while ((ek = sks.enumerateKeys (ek.getKeyHandle ())) != null)
              {
                if (ek.getProvisioningHandle () == client.provisioning_handle)
                  {
                    break;
                  }
              }
            assertTrue ("Missing keys", ek != null);
            return ek.getKeyHandle ();
          }

        
      }

    @Test
    public void StrongRSAPreferences () throws Exception
      {
        ask_for_4096 = true;
        new Doer ().perform ();
      }

    @Test
    public void RSAExponentPreferences () throws Exception
      {
        ask_for_exponent = true;
        new Doer ().perform ();
      }

    @Test
    public void BrainpoolOption () throws Exception
      {
        brain_pool = true;
        new Doer ().perform ();
      }

    @Test
    public void ClientAttributes () throws Exception
      {
        get_client_attributes = true;
        new Doer ().perform ();
      }

    @Test
    public void ImagePreferences () throws Exception
      {
        Doer doer = new Doer ();
        image_prefs = true;
        pin_protection = true;
        doer.perform ();
      }

    @Test
    public void PINPatterns () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        add_pin_pattern = true;
        ecc_key = true;
        https = true;
        doer.perform ();
      }

    @Test
    public void ServerCertificate () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        ecc_key = true;
        https = true;
        doer.perform ();
      }

    @Test
    public void ServerSeed () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        fixed_pin = true;
        server_seed = true;
        doer.perform ();
        assertFalse ("PIN Not User Modifiable", sks.getKeyProtectionInfo (doer.getFirstKey ()).getPinUserModifiableFlag ());
      }

    @Test
    public void MultipleKeys () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        two_keys = true;
        doer.perform ();
      }

    @Test
    public void CustomNamedKey () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        two_keys = true;
        custom_key_name = true;
        doer.perform ();
      }

    @Test
    public void EncryptedExtension () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        ecc_key = true;
        property_bag = true;
        encrypted_extension = true;
        server_seed = true;
        doer.perform ();
      }

    @Test
    public void StandardExtension () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        ecc_key = true;
        standard_extension = true;
        server_seed = true;
        doer.perform ();
      }

    @Test
    public void InputMethod () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        puk_protection = true;
        input_method = InputMethod.PROGRAMMATIC;
        doer.perform ();
      }

    @Test
    public void PropertyBag () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        symmetric_key = true;
        property_bag = true;
        doer.perform ();
        int key_handle = doer.getFirstKey ();
        ServerState.PropertyBag prop_bag = doer.server.server_state.getKeys ()[0].getPropertyBags ()[0];
        Property[] props1 = sks.getExtension (key_handle, prop_bag.getType ()).getProperties ();
        ServerState.Property[] props2 = prop_bag.getProperties ();
        assertTrue ("Prop len error", props1.length == props2.length);
        int w = 0;
        for (int i = 0; i < props1.length; i++)
          {
            if (props2[i].isWritable ())
              {
                w = i;
              }
            assertTrue ("Prop name error", props1[i].getName ().equals (props2[i].getName ()));
            assertTrue ("Prop value error", props1[i].getValue ().equals (props2[i].getValue ()));
          }
        sks.setProperty (key_handle, prop_bag.getType (), props2[w].getName (), props2[w].getValue () + "w2");
        props1 = sks.getExtension (key_handle, prop_bag.getType ()).getProperties ();
        for (int i = 0; i < props1.length; i++)
          {
            assertTrue ("Prop name error", props1[i].getName ().equals (props2[i].getName ()));
            assertTrue ("Prop value error", (i == w) ^ props1[i].getValue ().equals (props2[i].getValue ()));
          }
        sks.setProperty (key_handle, prop_bag.getType (), props2[w].getName (), props2[w].getValue ());
        props1 = sks.getExtension (key_handle, prop_bag.getType ()).getProperties ();
        for (int i = 0; i < props1.length; i++)
          {
            assertTrue ("Prop name error", props1[i].getName ().equals (props2[i].getName ()));
            assertTrue ("Prop value error", props1[i].getValue ().equals (props2[i].getValue ()));
          }
        assertTrue ("HMAC error", ArrayUtil.compare (sks.performHmac (key_handle,
                                                                      MACAlgorithms.HMAC_SHA1.getURI (),
                                                                      null,
                                                                      USER_DEFINED_PIN, TEST_STRING),
                                                     MACAlgorithms.HMAC_SHA1.digest (OTP_SEED, TEST_STRING)));
      }

   @Test
    public void ImportSymmetricKey () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        encryption_key = true;
        symmetric_key = true;
        doer.perform ();
        int key_handle = doer.getFirstKey ();
        byte[] iv = null;
        byte[] enc = sks.symmetricKeyEncrypt (key_handle,
                                              SymEncryptionAlgorithms.AES256_CBC.getURI (),
                                              true,
                                              iv,
                                              USER_DEFINED_PIN,
                                              TEST_STRING);
        assertTrue ("Encrypt/decrypt error", ArrayUtil.compare (sks.symmetricKeyEncrypt (key_handle,
                                                                                         SymEncryptionAlgorithms.AES256_CBC.getURI (),
                                                                                         false,
                                                                                         iv,
                                                                                         USER_DEFINED_PIN, 
                                                                                         enc),
                                                                                         TEST_STRING));
        assertFalse ("PIN Cached", sks.getKeyProtectionInfo (key_handle).getEnablePinCachingFlag ());
      }

    @Test
    public void DevicePIN () throws Exception
      {
        Doer doer = new Doer ();
        device_pin_protection = true;
        set_abort_url = true;
        doer.perform ();
      }

    @Test
    public void PresetPIN () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        preset_pin = true;
        enable_pin_caching = true;
        input_method = InputMethod.TRUSTED_GUI;
        doer.perform ();
        assertTrue ("PIN User Modifiable", sks.getKeyProtectionInfo (doer.getFirstKey ()).getPinUserModifiableFlag ());
        assertTrue ("PIN Not Cached", sks.getKeyProtectionInfo (doer.getFirstKey ()).getEnablePinCachingFlag ());
      }

    @Test
    public void CloneKeyProtection () throws Exception
      {
        Doer doer1 = new Doer ();
        updatable = true;
        ecc_kmk = true;
        pin_protection = true;
        pin_group_shared = true;
        doer1.perform ();
        updatable = false;
        pin_protection = false;
        clone_key_protection = doer1.server;
        Doer doer2 = new Doer ();
        doer2.perform ();
        EnumeratedKey ek = new EnumeratedKey ();
        int j = 0;
        while ((ek = sks.enumerateKeys (ek.getKeyHandle ())) != null)
          {
            if (ek.getProvisioningHandle () == doer2.client.provisioning_handle)
              {
                j++;
                KeyAttributes ka = sks.getKeyAttributes (ek.getKeyHandle ());
                byte[] result = sks.signHashedData (ek.getKeyHandle (),
                                                    AsymSignatureAlgorithms.RSA_SHA256.getURI (),
                                                    null,
                                                    USER_DEFINED_PIN,
                                                    HashAlgorithms.SHA256.digest (TEST_STRING));
                assertTrue ("Bad signature",
                            new SignatureWrapper (AsymSignatureAlgorithms.RSA_SHA256, ka.getCertificatePath ()[0].getPublicKey ())
                                 .update (TEST_STRING)
                                 .verify (result));
              }
          }
        assertTrue ("Missing keys", j == 2);
      }

    @Test
    public void UpdateKey () throws Exception
      {
        Doer doer1 = new Doer ();
        updatable = true;
        pin_protection = true;
        pin_group_shared = true;
        doer1.perform ();
        updatable = false;
        pin_protection = false;
        update_key= doer1.server;
        Doer doer2 = new Doer ();
        doer2.perform ();
        int key_handle = doer2.getFirstKey ();
        KeyAttributes ka = sks.getKeyAttributes (key_handle);
        byte[] result = sks.signHashedData (key_handle,
                                            AsymSignatureAlgorithms.RSA_SHA256.getURI (),
                                            null,
                                            USER_DEFINED_PIN,
                                            HashAlgorithms.SHA256.digest (TEST_STRING));
        assertTrue ("Bad signature",
                    new SignatureWrapper (AsymSignatureAlgorithms.RSA_SHA256, ka.getCertificatePath ()[0].getPublicKey ())
                        .update (TEST_STRING)
                        .verify (result));
      }

    @Test
    public void UpdateKeyManagementKey () throws Exception
      {
        Doer doer1 = new Doer ();
        updatable = true;
        pin_protection = true;
        pin_group_shared = true;
        doer1.perform ();
        updatable = true;
        pin_protection = false;
        update_kmk = true;
        update_key= doer1.server;
        ecc_kmk = true;
        Doer doer2 = new Doer ();
        doer2.perform ();
        int key_handle = doer2.getFirstKey ();
        KeyAttributes ka = sks.getKeyAttributes (key_handle);
        byte[] result = sks.signHashedData (key_handle,
                                            AsymSignatureAlgorithms.RSA_SHA256.getURI (),
                                            null,
                                            USER_DEFINED_PIN,
                                            HashAlgorithms.SHA256.digest (TEST_STRING));
        assertTrue ("Bad signature",
                    new SignatureWrapper (AsymSignatureAlgorithms.RSA_SHA256, ka.getCertificatePath ()[0].getPublicKey ())
                        .update (TEST_STRING)
                        .verify (result));
      }

    @Test
    public void DeleteKey () throws Exception
      {
        Doer doer1 = new Doer ();
        updatable = true;
        doer1.perform ();
        updatable = false;
        delete_key= doer1.server;
        Doer doer2 = new Doer ();
        doer2.perform ();
        EnumeratedKey ek = new EnumeratedKey ();
        int j = 0;
        while ((ek = sks.enumerateKeys (ek.getKeyHandle ())) != null)
          {
            if (ek.getProvisioningHandle () == doer1.client.provisioning_handle)
              {
                j++;
              }
          }
        assertTrue ("Too many keys", j == 0);
      }

    @Test
    public void ImportPrivateKey () throws Exception
      {
        Doer doer = new Doer ();
        set_private_key = true;
        pin_protection = true;
        doer.perform ();
        int key_handle = doer.getFirstKey ();
        byte[] result = sks.signHashedData (key_handle,
                                            AsymSignatureAlgorithms.RSA_SHA256.getURI (),
                                            null,
                                            USER_DEFINED_PIN,
                                            HashAlgorithms.SHA256.digest (TEST_STRING));
        SignatureWrapper sign = new SignatureWrapper (AsymSignatureAlgorithms.RSA_SHA256, doer.server.gen_private_key);
        sign.update (TEST_STRING);
        assertTrue ("Bad signature", ArrayUtil.compare (sign.sign (), result));
      }

    @Test
    public void ExportProtection () throws Exception
      {
        for (ExportProtection exp_pol : ExportProtection.values ())
          {
            Doer doer = new Doer ();
            export_protection = exp_pol;
            pin_protection = exp_pol == ExportProtection.PIN || exp_pol == ExportProtection.PUK;
            puk_protection = exp_pol == ExportProtection.PUK;
            ecc_key = true;
            doer.perform ();
            KeyProtectionInfo kpi = sks.getKeyProtectionInfo (doer.getFirstKey ());
            assertTrue ("Export prot", kpi.getExportProtection () == exp_pol);
          }
      }

    @Test
    public void DeleteProtection () throws Exception
      {
        for (DeleteProtection del_pol : DeleteProtection.values ())
          {
            Doer doer = new Doer ();
            pin_protection = del_pol == DeleteProtection.PIN || del_pol == DeleteProtection.PUK;
            puk_protection = del_pol == DeleteProtection.PUK;
            delete_protection = del_pol;
            ecc_key = true;
            doer.perform ();
            KeyProtectionInfo kpi = sks.getKeyProtectionInfo (doer.getFirstKey ());
            assertTrue ("Delete prot", kpi.getDeleteProtection () == del_pol);
          }
      }

    @Test
    public void KeyAgreement () throws Exception
      {
        Doer doer = new Doer ();
        pin_protection = true;
        ecc_key = true;
        key_agreement = true;
        doer.perform ();
        int key_handle = doer.getFirstKey ();
        KeyPairGenerator generator = KeyPairGenerator.getInstance ("EC");
        ECGenParameterSpec eccgen = new ECGenParameterSpec ("secp256r1");
        generator.initialize (eccgen, new SecureRandom ());
        KeyPair kp = generator.generateKeyPair ();
        KeyAttributes ka = sks.getKeyAttributes (key_handle);
        byte[] z = sks.keyAgreement (key_handle,
                                     SecureKeyStore.ALGORITHM_ECDH_RAW,
                                     null,
                                     USER_DEFINED_PIN, 
                                     (ECPublicKey)kp.getPublic ());
        KeyAgreement key_agreement = KeyAgreement.getInstance ("ECDH");
        key_agreement.init (kp.getPrivate ());
        key_agreement.doPhase (ka.getCertificatePath ()[0].getPublicKey (), true);
        byte[] Z = key_agreement.generateSecret ();
        assertTrue ("DH fail", ArrayUtil.compare (z, Z));
      }

    @Test
    public void UnlockKey () throws Exception
      {
        Doer doer1 = new Doer ();
        updatable = true;
        pin_protection = true;
        ecc_key = true;
        doer1.perform ();
        int key_handle = doer1.getFirstKey ();
        for (int i = 1; i <= doer1.server.pin_retry_limit; i++)
          {
            try
              {
                sks.signHashedData (key_handle,
                                    AsymSignatureAlgorithms.ECDSA_SHA256.getURI (),
                                    null,
                                    BAD_PIN,
                                    HashAlgorithms.SHA256.digest (TEST_STRING));
                fail ("Bad PIN should not work");
              }
            catch (SKSException e)
              {
                assertFalse ("Locked", sks.getKeyProtectionInfo (key_handle).isPinBlocked () ^ (i == doer1.server.pin_retry_limit));
              }
          }

        updatable = false;
        pin_protection = false;
        ecc_key = false;
        plain_unlock_key = doer1.server;
        Doer doer2 = new Doer ();
        doer2.perform ();
        assertFalse ("UnLocked", sks.getKeyProtectionInfo (key_handle).isPinBlocked ());
      }

    @Test
    public void PrivacyEnabled () throws Exception
      {
        Doer doer1 = new Doer ();
        privacy_enabled = true;
        updatable = true;
        pin_protection = true;
        ecc_key = true;
        doer1.perform ();
        int key_handle = doer1.getFirstKey ();
        for (int i = 1; i <= doer1.server.pin_retry_limit; i++)
          {
            try
              {
                sks.signHashedData (key_handle,
                                    AsymSignatureAlgorithms.ECDSA_SHA256.getURI (),
                                    null,
                                    BAD_PIN,
                                    HashAlgorithms.SHA256.digest (TEST_STRING));
                fail ("Bad PIN should not work");
              }
            catch (SKSException e)
              {
                assertFalse ("Locked", sks.getKeyProtectionInfo (key_handle).isPinBlocked () ^ (i == doer1.server.pin_retry_limit));
              }
          }

        updatable = false;
        pin_protection = false;
        ecc_key = false;
        plain_unlock_key= doer1.server;
        Doer doer2 = new Doer ();
        doer2.perform ();
        assertFalse ("UnLocked", sks.getKeyProtectionInfo (key_handle).isPinBlocked ());
        assertTrue ("PIN User Modifiable", sks.getKeyProtectionInfo (key_handle).getPinUserModifiableFlag ());
      }

    @Test
    public void TrustAnchor () throws Exception
      {
        Doer doer = new Doer ();
        set_trust_anchor = true;
        doer.perform ();
        X509Certificate[] cert_path = sks.getKeyAttributes (doer.getFirstKey ()).getCertificatePath ();
        assertTrue ("Path Length", CertificateUtil.isTrustAnchor (cert_path[cert_path.length - 1]));
      }

    @Test
    public void VirtualEnvironment () throws Exception
      {
        Doer doer = new Doer ();
        virtual_environment = true;
        doer.perform ();
      }

    @Test
    public void PreferredLanguages () throws Exception
      {
        Doer doer = new Doer ();
        languages = true;
        doer.perform ();
      }

    @Test
    public void TargetKeyContainers () throws Exception
      {
        Doer doer = new Doer ();
        key_container_list = true;
        doer.perform ();
      }

    @Test
    public void MassiveUserPINCollection () throws Exception
      {
        assertTrue (PINCheck (PassphraseFormat.ALPHANUMERIC, null, "AB123"));
        assertTrue (PINCheck (PassphraseFormat.NUMERIC, null, "1234"));
        assertTrue (PINCheck (PassphraseFormat.STRING, null, "azAB13.\n"));
        assertTrue (PINCheck (PassphraseFormat.BINARY, null, "12300234FF"));
        assertTrue (PINCheck (PassphraseFormat.BINARY, null, "12300234ff"));
        assertFalse (PINCheck (PassphraseFormat.BINARY, null, "3034ff"));
        assertFalse (PINCheck (PassphraseFormat.BINARY, null, "12300234fp"));

        assertFalse (PINCheck (PassphraseFormat.ALPHANUMERIC, null, "ab123"));  // Lowercase 
        assertFalse (PINCheck (PassphraseFormat.NUMERIC, null, "AB1234"));      // Alpha

        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.SEQUENCE}, "1234"));      // Up seq
        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.SEQUENCE}, "8765"));      // Down seq
        assertTrue (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.SEQUENCE}, "1235"));      // No seq
        assertTrue (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.SEQUENCE}, "1345"));      // No seq

        assertTrue (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.TWO_IN_A_ROW}, "1232"));      // No two in row
        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.TWO_IN_A_ROW}, "11345"));      // Two in a row
        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.TWO_IN_A_ROW}, "13455"));      // Two in a row

        assertTrue (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.THREE_IN_A_ROW}, "11232"));      // No two in row
        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.THREE_IN_A_ROW}, "111345"));      // Three in a row
        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.THREE_IN_A_ROW}, "134555"));      // Three in a row
        
        assertTrue (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.SEQUENCE, PatternRestriction.THREE_IN_A_ROW}, "1235"));      // No seq or three in a row
        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.SEQUENCE, PatternRestriction.THREE_IN_A_ROW}, "6789"));      // Seq
        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.SEQUENCE, PatternRestriction.THREE_IN_A_ROW}, "1115"));      // Three in a row

        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "1476"));      // Bad combo
        assertFalse (PINCheck (PassphraseFormat.BINARY, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "12300234FF"));      // Bad combo

        assertTrue (PINCheck (PassphraseFormat.STRING, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "2aZ."));
        assertTrue (PINCheck (PassphraseFormat.ALPHANUMERIC, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "AB34"));

        assertFalse (PINCheck (PassphraseFormat.STRING, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "2aZA"));  // Non alphanum missing
        assertFalse (PINCheck (PassphraseFormat.STRING, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "a.jZ"));  // Number missing
        assertFalse (PINCheck (PassphraseFormat.STRING, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "2 ZA"));  // Lowercase missing
        assertFalse (PINCheck (PassphraseFormat.STRING, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "2a 6"));  // Uppercase missing

        assertFalse (PINCheck (PassphraseFormat.ALPHANUMERIC, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "ABCK")); // Missing number
        assertFalse (PINCheck (PassphraseFormat.ALPHANUMERIC, new PatternRestriction[]{PatternRestriction.MISSING_GROUP}, "1235")); // Missing alpha
        
        assertTrue (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.REPEATED}, "1345"));
        assertFalse (PINCheck (PassphraseFormat.NUMERIC, new PatternRestriction[]{PatternRestriction.REPEATED}, "1315"));  // Two of same

        PINGroupCheck (Grouping.NONE, new AppUsage[] {AppUsage.AUTHENTICATION}, new String[] {"1234"}, new int[] {0}, false);
        PINGroupCheck (Grouping.NONE, new AppUsage[] {AppUsage.AUTHENTICATION, AppUsage.SIGNATURE}, new String[] {"1234", "1234"}, new int[] {0, 1}, false);
        PINGroupCheck (Grouping.NONE, new AppUsage[] {AppUsage.AUTHENTICATION, AppUsage.SIGNATURE}, new String[] {"1234", "1235"}, new int[] {0, 1}, false);
        PINGroupCheck (Grouping.SHARED, new AppUsage[] {AppUsage.AUTHENTICATION, AppUsage.SIGNATURE}, new String[] {"1234", "1234"}, new int[] {0, 1}, true);
        PINGroupCheck (Grouping.SHARED, new AppUsage[] {AppUsage.AUTHENTICATION, AppUsage.SIGNATURE, AppUsage.SIGNATURE}, new String[] {"1234"}, new int[] {0, 0, 0}, false);
        PINGroupCheck (Grouping.UNIQUE, new AppUsage[] {AppUsage.AUTHENTICATION, AppUsage.SIGNATURE}, new String[] {"1234"}, new int[] {0, 0}, true);
        PINGroupCheck (Grouping.UNIQUE, new AppUsage[] {AppUsage.AUTHENTICATION, AppUsage.SIGNATURE}, new String[] {"1234","2345"}, new int[] {0, 1}, false);
        PINGroupCheck (Grouping.UNIQUE, new AppUsage[] {AppUsage.AUTHENTICATION, AppUsage.SIGNATURE}, new String[] {"1234","1234"}, new int[] {0, 1}, true);
        PINGroupCheck (Grouping.UNIQUE, new AppUsage[] {AppUsage.SIGNATURE, AppUsage.AUTHENTICATION, AppUsage.SIGNATURE}, new String[] {"1234","2345"}, new int[] {0, 1, 0}, false);
        PINGroupCheck (Grouping.UNIQUE, new AppUsage[] {AppUsage.SIGNATURE, AppUsage.AUTHENTICATION, AppUsage.SIGNATURE, AppUsage.ENCRYPTION}, new String[] {"1234","2345","7777"}, new int[] {0, 1, 0, 2}, false);
        PINGroupCheck (Grouping.SIGNATURE_PLUS_STANDARD, new AppUsage[] {AppUsage.SIGNATURE, AppUsage.AUTHENTICATION, AppUsage.SIGNATURE, AppUsage.ENCRYPTION}, new String[] {"2345","1234"}, new int[] {0, 1, 0, 1}, false);
        PINGroupCheck (Grouping.SIGNATURE_PLUS_STANDARD, new AppUsage[] {AppUsage.SIGNATURE, AppUsage.AUTHENTICATION, AppUsage.SIGNATURE, AppUsage.ENCRYPTION}, new String[] {"2345","2345"}, new int[] {0, 1, 0, 1}, true);
      }
  }
