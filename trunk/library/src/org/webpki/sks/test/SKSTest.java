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
package org.webpki.sks.test;

import java.io.FileOutputStream;
import java.io.IOException;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.Signature;
import java.util.EnumSet;
import java.util.Set;

import javax.crypto.Cipher;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.rules.TestName;

import static org.junit.Assert.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.webpki.crypto.AsymEncryptionAlgorithms;
import org.webpki.crypto.HashAlgorithms;
import org.webpki.crypto.SignatureAlgorithms;

import org.webpki.keygen2.KeyUsage;
import org.webpki.keygen2.PINGrouping;
import org.webpki.keygen2.PassphraseFormat;
import org.webpki.keygen2.PatternRestriction;

import org.webpki.sks.EnumeratedProvisioningSession;
import org.webpki.sks.SKSException;
import org.webpki.sks.SecureKeyStore;
import org.webpki.util.ArrayUtil;

public class SKSTest
  {
    static final byte[] TEST_STRING = new byte[]{'S','u','c','c','e','s','s',' ','o','r',' ','n','o','t','?'};
  
    static FileOutputStream fos;
    
    static SecureKeyStore sks;
    
    Device device;
    
    private int sessionCount () throws Exception
      {
        EnumeratedProvisioningSession eps = new EnumeratedProvisioningSession ();
        int i = 0;
        while ((eps = sks.enumerateProvisioningSessions (eps, false)).isValid ())
          {
            i++;
          }
        return i;
      }
    
    private void edgeDeleteCase (boolean post) throws Exception
      {
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        GenKey key3 = sess2.createECKey ("Key.1",
                                         null /* pin_value */,
                                         null /* pin_policy */,
                                         KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        if (post)
          {
            sess2.postUpdateKey (key3, key1);
          }
        else
          {
            sks.deleteKey (key1.key_handle, new byte[0]);
          }
        try
          {
            if (post)
              {
                sks.deleteKey (key1.key_handle, new byte[0]);
              }
            else
              {
                sess2.postUpdateKey (key3, key1);
              }
            sess2.closeSession ();
            fail ("Multiple updates using the same key");
          }
        catch (SKSException e)
          {
          }
      }

    private void deleteKey (GenKey key) throws SKSException
      {
        sks.deleteKey (key.key_handle, new byte[0]);
      }
    
    private void updateReplace (boolean order) throws Exception
      {
        int q = sessionCount ();
        String ok_pin = "1563";
        ProvSess sess = new ProvSess (device);
        PINPol pin_policy = sess.createPINPolicy ("PIN",
                                                  PassphraseFormat.NUMERIC,
                                                  EnumSet.noneOf (PatternRestriction.class),
                                                  PINGrouping.SHARED,
                                                  4 /* min_length */, 
                                                  8 /* max_length */,
                                                  (short) 3 /* retry_limit*/, 
                                                  null /* puk_policy */);
        GenKey key1 = sess.createECKey ("Key.1",
                                        ok_pin /* pin_value */,
                                        pin_policy,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        GenKey key2 = sess2.createECKey ("Key.2",
                                         null /* pin_value */,
                                         null,
                                         KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        GenKey key3 = sess2.createRSAKey ("Key.1",
                                          2048,
                                          null /* pin_value */,
                                          null /* pin_policy */,
                                          KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST13");
        if (order) sess2.postCloneKey (key3, key1);
        sess2.postUpdateKey (key2, key1);
        if (!order) sess2.postCloneKey (key3, key1);
        sess2.closeSession ();
        assertTrue ("Old key should exist after update", key1.exists ());
        assertFalse ("New key should NOT exist after update", key2.exists ());
        assertTrue ("New key should exist after clone", key3.exists ());
        assertTrue ("Ownership error", key1.getUpdatedKeyInfo ().getProvisioningHandle () == sess2.provisioning_handle);
        assertTrue ("Ownership error", key3.getUpdatedKeyInfo ().getProvisioningHandle () == sess2.provisioning_handle);
        assertFalse ("Managed sessions MUST be deleted", sess.exists ());
        try
          {
            device.sks.signHashedData (key3.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
            assertTrue ("There should be an auth error", e.getError () == SKSException.ERROR_AUTHORIZATION);
          }
        try
          {
            byte[] result = device.sks.signHashedData (key3.key_handle, 
                                                      "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                                       ok_pin.getBytes ("UTF-8"), 
                                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            Signature verify = Signature.getInstance (SignatureAlgorithms.RSA_SHA256.getJCEName (), "BC");
            verify.initVerify (key3.cert_path[0]);
            verify.update (TEST_STRING);
            assertTrue ("Bad signature key3", verify.verify (result));
            result = device.sks.signHashedData (key1.key_handle, 
                                                "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", 
                                                ok_pin.getBytes ("UTF-8"), 
                                                HashAlgorithms.SHA256.digest (TEST_STRING));
            verify = Signature.getInstance (SignatureAlgorithms.ECDSA_SHA256.getJCEName (), "BC");
            verify.initVerify (key2.cert_path[0]);
            verify.update (TEST_STRING);
            assertTrue ("Bad signature key1", verify.verify (result));
          }
        catch (SKSException e)
          {
            fail ("Good PIN should work");
          }
        assertTrue ("Session count", ++q == sessionCount ());
      }

    
    private boolean nameCheck (String name) throws IOException, GeneralSecurityException
      {
        try
          {
            ProvSess sess = new ProvSess (device);
            sess.createPINPolicy (name,
                                  PassphraseFormat.NUMERIC,
                                  4 /* min_length */, 
                                  8 /* max_length */,
                                  (short) 3 /* retry_limit*/, 
                                  null /* puk_policy */);
            sess.abortSession ();
          }
        catch (SKSException e)
          {
            return false;
          }
        return true;
      }
  
    private boolean PINCheck (PassphraseFormat format,
                              PatternRestriction[] patterns,
                              String pin) throws IOException, GeneralSecurityException
      {
        try
          {
            Set<PatternRestriction> pattern_restrictions = EnumSet.noneOf (PatternRestriction.class);
            if (patterns != null)
              {
                for (PatternRestriction pattern : patterns)
                  {
                    pattern_restrictions.add (pattern);
                  }
              }
            ProvSess sess = new ProvSess (device);
            PINPol pin_pol = sess.createPINPolicy ("PIN",
                                                   format,
                                                   pattern_restrictions,
                                                   PINGrouping.NONE,
                                                   4 /* min_length */, 
                                                   8 /* max_length */,
                                                   (short) 3 /* retry_limit*/, 
                                                   null /* puk_policy */);
            sess.createECKey ("Key.1",
                              pin /* pin_value */,
                              pin_pol /* pin_policy */,
                              KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST8");
            sess.abortSession ();
          }
        catch (SKSException e)
          {
            return false;
          }
        return true;
      }

    private boolean PUKCheck (PassphraseFormat format,
                              String puk) throws IOException, GeneralSecurityException
      {
        try
          {
            ProvSess sess = new ProvSess (device);
            PUKPol pin_pol = sess.createPUKPolicy ("PUK",
                                                   format,
                                                   (short) 3 /* retry_limit*/, 
                                                   puk /* puk_policy */);
            sess.abortSession ();
          }
        catch (SKSException e)
          {
            return false;
          }
        return true;
      }

    private boolean PINGroupCheck (boolean same_pin, PINGrouping grouping) throws IOException, GeneralSecurityException
      {
        try
          {
            String pin1 = "1234";
            String pin2 = "4567";
            ProvSess sess = new ProvSess (device);
            PINPol pin_pol = sess.createPINPolicy ("PIN",
                                         PassphraseFormat.NUMERIC,
                                         EnumSet.noneOf (PatternRestriction.class),
                                         grouping,
                                         4 /* min_length */, 
                                         8 /* max_length */,
                                         (short) 3 /* retry_limit*/, 
                                         null /* puk_policy */);
            sess.createECKey ("Key.1",
                pin1 /* pin_value */,
                pin_pol /* pin_policy */,
                KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST");
            if (grouping == PINGrouping.SIGNATURE_PLUS_STANDARD)
              {
                sess.createECKey ("Key.1s",
                    pin1 /* pin_value */,
                    pin_pol /* pin_policy */,
                    KeyUsage.UNIVERSAL).setCertificate ("CN=TEST");
                sess.createECKey ("Key.2s",
                    same_pin ? pin1 : pin2 /* pin_value */,
                    pin_pol /* pin_policy */,
                    KeyUsage.SIGNATURE).setCertificate ("CN=TEST");
              }
            sess.createECKey ("Key.2",
                same_pin ? pin1 : pin2 /* pin_value */,
                pin_pol /* pin_policy */,
                KeyUsage.SIGNATURE).setCertificate ("CN=TEST");
            sess.abortSession ();
          }
        catch (SKSException e)
          {
              return false;
          }
        return true;
      }


    @BeforeClass
    public static void openFile () throws Exception
      {
        String dir = System.getProperty ("test.dir");
        if (dir.length () > 0)
          {
            fos = new FileOutputStream (dir + "/" + SKSTest.class.getCanonicalName () + ".txt");
          }
        Security.addProvider(new BouncyCastleProvider());
        sks = (SecureKeyStore) Class.forName (System.getProperty ("sks.implementation")).newInstance ();
      }

    @AfterClass
    public static void closeFile () throws Exception
      {
        if (fos != null)
          {
            fos.close ();
          }
      }
    
    @Before
    public void setup () throws Exception
      {
         device = new Device (sks);
         writeString ("Begin Test\n");
      }
        
    @After
    public void teardown () throws Exception
      {
         writeString ("End Test\n");
         EnumeratedProvisioningSession eps = new EnumeratedProvisioningSession ();
         while ((eps = sks.enumerateProvisioningSessions (eps, true)).isValid ())
           {
             writeString ("Deleted session: " + eps.getProvisioningHandle () + "\n");
             sks.abortProvisioningSession (eps.getProvisioningHandle ());
           }
      }

    @Rule 
    public TestName name = new TestName();
   
    private void write (byte[] data) throws Exception
      {
        if (fos != null)
          {
            fos.write (data);
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
    
      
    @Test
    public void test1 () throws Exception
      {
        int q = sessionCount ();
        new ProvSess (device).closeSession ();
        assertTrue ("Session count", q == sessionCount ());
      }
    @Test
    public void test2 () throws Exception
      {
        int q = sessionCount ();
        ProvSess sess = new ProvSess (device);
        sess.createPINPolicy ("PIN",
                              PassphraseFormat.NUMERIC,
                              4 /* min_length */, 
                              8 /* max_length */,
                              (short) 3 /* retry_limit*/, 
                              null /* puk_policy */);
        try
          {
            sess.closeSession ();
            fail ("Should have thrown an exception");
          }
        catch (SKSException e)
          {
          }
        assertTrue ("Session count", q == sessionCount ());
      }
    @Test
    public void test3 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        sess.createPINPolicy ("PIN",
                              PassphraseFormat.NUMERIC,
                              4 /* min_length */, 
                              8 /* max_length */,
                              (short) 3 /* retry_limit*/, 
                              null /* puk_policy */);
        sess.createPUKPolicy ("PUK",
                              PassphraseFormat.NUMERIC,
                              (short) 3 /* retry_limit*/, 
                              "012355" /* puk_policy */);
      }
    @Test(expected=SKSException.class)
    public void test4 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        PUKPol puk = sess.createPUKPolicy ("PUK",
                                              PassphraseFormat.NUMERIC,
                                              (short) 3 /* retry_limit*/, 
                                              "012355" /* puk_policy */);
        sess.createPINPolicy ("PIN",
                              PassphraseFormat.NUMERIC,
                              4 /* min_length */, 
                              8 /* max_length */,
                              (short) 3 /* retry_limit*/, 
                              puk /* puk_policy */);
        sess.closeSession ();
      }
    @Test(expected=SKSException.class)
    public void test5 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        sess.createRSAKey ("Key.1",
                           1024 /* rsa_size */,
                           null /* pin_value */,
                           null /* pin_policy */,
                           KeyUsage.AUTHENTICATION);
        sess.closeSession ();
      }
    @Test
    public void test6 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        int i = 1;
        for (short rsa_key_size : device.device_info.getRSAKeySizes ())
          {
            sess.createRSAKey ("Key." + i++,
                               rsa_key_size /* rsa_size */,
                               null /* pin_value */,
                               null /* pin_policy */,
                               KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST6");
          }
        if (i == 1) fail("Missing RSA");
        sess.closeSession ();
      }
    @Test
    public void test7 () throws Exception
      {
        assertTrue (nameCheck ("a"));
        assertTrue (nameCheck ("_"));
        assertTrue (nameCheck ("a."));
        assertTrue (nameCheck ("azAZ09-._"));
        assertTrue (nameCheck ("a123456789a123456789a12345678955"));
        assertFalse (nameCheck (".a"));
        assertFalse (nameCheck ("-"));
        assertFalse (nameCheck (" I_am_a_bad_name"));
        assertFalse (nameCheck (""));
        assertFalse (nameCheck ("a123456789a123456789a123456789555"));
      }
    @Test
    public void test8 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        sess.createECKey ("Key.1",
                           null /* pin_value */,
                           null /* pin_policy */,
                           KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();
        
      }
    @Test
    public void test9 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        GenKey key = sess.createECKey ("Key.1",
                                       null /* pin_value */,
                                       null /* pin_policy */,
                                       KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();
        byte[] result = device.sks.signHashedData (key.key_handle, 
                                                   "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", 
                                                   new byte[0], 
                                                   HashAlgorithms.SHA256.digest (TEST_STRING));
        Signature verify = Signature.getInstance (SignatureAlgorithms.ECDSA_SHA256.getJCEName (), "BC");
        verify.initVerify (key.cert_path[0]);
        verify.update (TEST_STRING);
        assertTrue ("Bad signature", verify.verify (result));
      }
    @Test
    public void test10 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        GenKey key = sess.createRSAKey ("Key.1",
                                        2048,
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();

        byte[] result = device.sks.signHashedData (key.key_handle, 
                                                   "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                                   new byte[0], 
                                                   HashAlgorithms.SHA256.digest (TEST_STRING));
        Signature verify = Signature.getInstance (SignatureAlgorithms.RSA_SHA256.getJCEName (), "BC");
        verify.initVerify (key.cert_path[0]);
        verify.update (TEST_STRING);
        assertTrue ("Bad signature", verify.verify (result));

        result = device.sks.signHashedData (key.key_handle, 
                                            "http://www.w3.org/2000/09/xmldsig#rsa-sha1", 
                                            new byte[0], 
                                            HashAlgorithms.SHA1.digest (TEST_STRING));
        verify = Signature.getInstance (SignatureAlgorithms.RSA_SHA1.getJCEName (), "BC");
        verify.initVerify (key.cert_path[0]);
        verify.update (TEST_STRING);
        assertTrue ("Bad signature", verify.verify (result));
      }
    @Test
    public void test11 () throws Exception
      {
        String ok_pin = "1563";
        ProvSess sess = new ProvSess (device);
        PINPol pin_policy = sess.createPINPolicy ("PIN",
                                                  PassphraseFormat.NUMERIC,
                                                  4 /* min_length */, 
                                                  8 /* max_length */,
                                                  (short) 3 /* retry_limit*/, 
                                                  null /* puk_policy */);

        GenKey key = sess.createRSAKey ("Key.1",
                                        1024,
                                        ok_pin /* pin_value */,
                                        pin_policy /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();

        try
          {
            device.sks.signHashedData (key.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
          }
        try
          {
            device.sks.signHashedData (key.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       ok_pin.getBytes ("UTF-8"), 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
          }
        catch (SKSException e)
          {
            fail ("Good PIN should work");
          }
        try
          {
            device.sks.signHashedData (key.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
          }
        try
          {
            device.sks.signHashedData (key.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
          }
        try
          {
            device.sks.signHashedData (key.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       ok_pin.getBytes ("UTF-8"), 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
          }
        catch (SKSException e)
          {
            fail ("Good PIN should work");
          }
        try
          {
            device.sks.signHashedData (key.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
          }
        try
          {
            device.sks.signHashedData (key.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
          }
        try
          {
            device.sks.signHashedData (key.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
          }
        try
          {
            device.sks.signHashedData (key.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       ok_pin.getBytes ("UTF-8"), 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Good PIN but too many errors should NOT work");
          }
        catch (SKSException e)
          {
          }
         
      }
    @Test
    public void test12 () throws Exception
      {
        assertTrue (PUKCheck (PassphraseFormat.ALPHANUMERIC, "AB123"));
        assertTrue (PUKCheck (PassphraseFormat.NUMERIC, "1234"));
        assertTrue (PUKCheck (PassphraseFormat.STRING, "azAB13.\n"));
        assertTrue (PUKCheck (PassphraseFormat.BINARY, "12300234FF"));

        assertFalse (PUKCheck (PassphraseFormat.ALPHANUMERIC, ""));  // too short 
        assertFalse (PUKCheck (PassphraseFormat.ALPHANUMERIC, "ab123"));  // Lowercase 
        assertFalse (PUKCheck (PassphraseFormat.NUMERIC, "AB1234"));      // Alpha

        assertTrue (PINCheck (PassphraseFormat.ALPHANUMERIC, null, "AB123"));
        assertTrue (PINCheck (PassphraseFormat.NUMERIC, null, "1234"));
        assertTrue (PINCheck (PassphraseFormat.STRING, null, "azAB13.\n"));
        assertTrue (PINCheck (PassphraseFormat.BINARY, null, "12300234FF"));

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
        
        assertTrue (PINGroupCheck (true, PINGrouping.NONE));
        assertTrue (PINGroupCheck (false, PINGrouping.NONE));
        assertTrue (PINGroupCheck (true, PINGrouping.SHARED));
        assertFalse (PINGroupCheck (false, PINGrouping.SHARED));
        assertFalse (PINGroupCheck (true, PINGrouping.UNIQUE));
        assertTrue (PINGroupCheck (false, PINGrouping.UNIQUE));
        assertFalse (PINGroupCheck (true, PINGrouping.SIGNATURE_PLUS_STANDARD));
        assertTrue (PINGroupCheck (false, PINGrouping.SIGNATURE_PLUS_STANDARD));
      }
    @Test
    public void test13 () throws Exception
      {
        int q = sessionCount ();
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        GenKey key2 = sess.createECKey ("Key.2",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        sess2.postDeleteKey (key1);
        assertTrue ("Ownership error", key2.getUpdatedKeyInfo ().getProvisioningHandle () == sess.provisioning_handle);
        assertTrue ("Missing key, deletes MUST only be performed during session close", key1.exists ());
        sess2.closeSession ();
        assertFalse ("Key was not deleted", key1.exists ());
        assertTrue ("Ownership error", key2.getUpdatedKeyInfo ().getProvisioningHandle () == sess2.provisioning_handle);
        assertFalse ("Managed sessions MUST be deleted", sess.exists ());
        assertTrue ("Session count", ++q == sessionCount ());
      }
    @Test
    public void test14 () throws Exception
      {
        int q = sessionCount ();
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        sess2.postDeleteKey (key1);
        assertTrue ("Missing key, deletes MUST only be performed during session close", key1.exists ());
        sess2.closeSession ();
        assertFalse ("Key was not deleted", key1.exists ());
        assertFalse ("Managed sessions MUST be deleted", sess.exists ());
        assertTrue ("Session count",q == sessionCount ());
      }
    @Test
    public void test15 () throws Exception
      {
        int q = sessionCount ();
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        GenKey key2 = sess.createECKey ("Key.2",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST16");
        sess.closeSession ();
        assertTrue (sess.exists ());
        deleteKey (key1);
        assertFalse ("Key was not deleted", key1.exists ());
        assertTrue ("Key did not exist", key2.exists ());
        assertTrue ("Session count", ++q == sessionCount ());
      }
    @Test
    public void test16 () throws Exception
      {
        int q = sessionCount ();
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();
        assertTrue (sess.exists ());
        deleteKey (key1);
        assertFalse ("Key was not deleted", key1.exists ());
        assertTrue ("Session count", q == sessionCount ());
      }
    @Test
    public void test17 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        GenKey key2 = sess2.createECKey ("Key.1",
                                         null /* pin_value */,
                                         null /* pin_policy */,
                                         KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess2.postUpdateKey (key2, key1);
        sess2.closeSession ();
        assertTrue ("Key should exist even after update", key1.exists ());
        assertFalse ("Key has been used and should be removed", key2.exists ());
        assertTrue ("Ownership error", key1.getUpdatedKeyInfo ().getProvisioningHandle () == sess2.provisioning_handle);
        assertFalse ("Managed sessions MUST be deleted", sess.exists ());
      }
    @Test
    public void test18 () throws Exception
      {
        String ok_pin = "1563";
        ProvSess sess = new ProvSess (device);
        PINPol pin_policy = sess.createPINPolicy ("PIN",
                                                  PassphraseFormat.NUMERIC,
                                                  4 /* min_length */, 
                                                  8 /* max_length */,
                                                  (short) 3 /* retry_limit*/, 
                                                  null /* puk_policy */);
        GenKey key1 = sess.createECKey ("Key.1",
                                        ok_pin /* pin_value */,
                                        pin_policy,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        GenKey key2 = sess2.createECKey ("Key.1",
                                         null /* pin_value */,
                                         null /* pin_policy */,
                                         KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess2.postUpdateKey (key2, key1);
        sess2.closeSession ();
        assertTrue ("Key should exist even after update", key1.exists ());
        assertFalse ("Key has been used and should be removed", key2.exists ());
        assertTrue ("Ownership error", key1.getUpdatedKeyInfo ().getProvisioningHandle () == sess2.provisioning_handle);
        assertFalse ("Managed sessions MUST be deleted", sess.exists ());
        try
          {
            device.sks.signHashedData (key1.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
          }
        try
          {
            byte[] result = device.sks.signHashedData (key1.key_handle, 
                                                       "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", 
                                                       ok_pin.getBytes ("UTF-8"), 
                                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            Signature verify = Signature.getInstance (SignatureAlgorithms.ECDSA_SHA256.getJCEName (), "BC");
            verify.initVerify (key2.cert_path[0]);
            verify.update (TEST_STRING);
            assertTrue ("Bad signature", verify.verify (result));
          }
        catch (SKSException e)
          {
            fail ("Good PIN should work");
          }
      }
    @Test
    public void test19 () throws Exception
      {
        String ok_pin = "1563";
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        PINPol pin_policy = sess2.createPINPolicy ("PIN",
                                                   PassphraseFormat.NUMERIC,
                                                   4 /* min_length */, 
                                                   8 /* max_length */,
                                                   (short) 3 /* retry_limit*/, 
                                                   null /* puk_policy */);
        GenKey key2 = sess2.createECKey ("Key.1",
                                         ok_pin /* pin_value */,
                                         pin_policy,
                                         KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        try
          {
            sess2.postUpdateKey (key2, key1);
            fail ("No PINs on update keys please");
          }
        catch (SKSException e)
          {
          }
      }
    @Test
    public void test20 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        GenKey key2 = sess2.createECKey ("Key.1",
                                         null /* pin_value */,
                                         null /* pin_policy */,
                                         KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        GenKey key3 = sess2.createECKey ("Key.2",
                                         null /* pin_value */,
                                         null /* pin_policy */,
                                         KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess2.postUpdateKey (key2, key1);
        try
          {
            sess2.postUpdateKey (key3, key1);
            fail ("Multiple updates of the same key");
          }
        catch (SKSException e)
          {
          }
      }
    @Test
    public void test21 () throws Exception
      {
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        GenKey key2 = sess.createECKey ("Key.2",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        GenKey key3 = sess2.createECKey ("Key.1",
                                         null /* pin_value */,
                                         null /* pin_policy */,
                                         KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess2.postUpdateKey (key3, key1);
        try
          {
            sess2.postUpdateKey (key3, key2);
            fail ("Multiple updates using the same key");
          }
        catch (SKSException e)
          {
          }
      }
    @Test
    public void test22 () throws Exception
      {
        String ok_pin = "1563";
        ProvSess sess = new ProvSess (device);
        PINPol pin_policy = sess.createPINPolicy ("PIN",
                                                  PassphraseFormat.NUMERIC,
                                                  EnumSet.noneOf (PatternRestriction.class),
                                                  PINGrouping.SHARED,
                                                  4 /* min_length */, 
                                                  8 /* max_length */,
                                                  (short) 3 /* retry_limit*/, 
                                                  null /* puk_policy */);
        GenKey key1 = sess.createECKey ("Key.1",
                                        ok_pin /* pin_value */,
                                        pin_policy,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        GenKey key2 = sess2.createRSAKey ("Key.1",
                                          2048,
                                          null /* pin_value */,
                                          null /* pin_policy */,
                                          KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST13");
        sess2.postCloneKey (key2, key1);
        sess2.closeSession ();
        assertTrue ("Old key should exist after clone", key1.exists ());
        assertTrue ("New key should exist after clone", key2.exists ());
        assertTrue ("Ownership error", key1.getUpdatedKeyInfo ().getProvisioningHandle () == sess2.provisioning_handle);
        assertFalse ("Managed sessions MUST be deleted", sess.exists ());
        try
          {
            device.sks.signHashedData (key2.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
            assertTrue ("There should be an auth error", e.getError () == SKSException.ERROR_AUTHORIZATION);
          }
        try
          {
            byte[] result = device.sks.signHashedData (key2.key_handle, 
                                                      "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                                       ok_pin.getBytes ("UTF-8"), 
                                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            Signature verify = Signature.getInstance (SignatureAlgorithms.RSA_SHA256.getJCEName (), "BC");
            verify.initVerify (key2.cert_path[0]);
            verify.update (TEST_STRING);
            assertTrue ("Bad signature key2", verify.verify (result));
            result = device.sks.signHashedData (key1.key_handle, 
                                                "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", 
                                                ok_pin.getBytes ("UTF-8"), 
                                                HashAlgorithms.SHA256.digest (TEST_STRING));
            verify = Signature.getInstance (SignatureAlgorithms.ECDSA_SHA256.getJCEName (), "BC");
            verify.initVerify (key1.cert_path[0]);
            verify.update (TEST_STRING);
            assertTrue ("Bad signature key1", verify.verify (result));
          }
        catch (SKSException e)
          {
            fail ("Good PIN should work");
          }
      }
    @Test
    public void test23 () throws Exception
      {
        String ok_pin = "1563";
        ProvSess sess = new ProvSess (device);
        PINPol pin_policy = sess.createPINPolicy ("PIN",
                                                  PassphraseFormat.NUMERIC,
                                                  EnumSet.noneOf (PatternRestriction.class),
                                                  PINGrouping.SHARED,
                                                  4 /* min_length */, 
                                                  8 /* max_length */,
                                                  (short) 3 /* retry_limit*/, 
                                                  null /* puk_policy */);
        GenKey key1 = sess.createECKey ("Key.1",
                                        ok_pin /* pin_value */,
                                        pin_policy,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        GenKey key2 = sess.createECKey ("Key.2",
                                        ok_pin /* pin_value */,
                                        pin_policy,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        GenKey key3 = sess2.createRSAKey ("Key.1",
                                          2048,
                                          null /* pin_value */,
                                          null /* pin_policy */,
                                          KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST13");
        sess2.postCloneKey (key3, key1);
        sess2.closeSession ();
        assertTrue ("Old key should exist after clone", key1.exists ());
        assertTrue ("New key should exist after clone", key2.exists ());
        assertTrue ("Ownership error", key1.getUpdatedKeyInfo ().getProvisioningHandle () == sess2.provisioning_handle);
        assertTrue ("Ownership error", key2.getUpdatedKeyInfo ().getProvisioningHandle () == sess2.provisioning_handle);
        assertFalse ("Managed sessions MUST be deleted", sess.exists ());
        try
          {
            device.sks.signHashedData (key3.key_handle, 
                                       "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                       new byte[0], 
                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            fail ("Bad PIN should not work");
          }
        catch (SKSException e)
          {
            assertTrue ("There should be an auth error", e.getError () == SKSException.ERROR_AUTHORIZATION);
          }
        try
          {
            byte[] result = device.sks.signHashedData (key3.key_handle, 
                                                      "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", 
                                                       ok_pin.getBytes ("UTF-8"), 
                                                       HashAlgorithms.SHA256.digest (TEST_STRING));
            Signature verify = Signature.getInstance (SignatureAlgorithms.RSA_SHA256.getJCEName (), "BC");
            verify.initVerify (key3.cert_path[0]);
            verify.update (TEST_STRING);
            assertTrue ("Bad signature key3", verify.verify (result));
            result = device.sks.signHashedData (key1.key_handle, 
                                                "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", 
                                                ok_pin.getBytes ("UTF-8"), 
                                                HashAlgorithms.SHA256.digest (TEST_STRING));
            verify = Signature.getInstance (SignatureAlgorithms.ECDSA_SHA256.getJCEName (), "BC");
            verify.initVerify (key1.cert_path[0]);
            verify.update (TEST_STRING);
            assertTrue ("Bad signature key1", verify.verify (result));
          }
        catch (SKSException e)
          {
            fail ("Good PIN should work");
          }
      }
    @Test
    public void test24 () throws Exception
      {
        updateReplace (true);
      }
    @Test
    public void test25 () throws Exception
      {
        updateReplace (false);
      }
    @Test
    public void test26 () throws Exception
      {
        edgeDeleteCase (true);
      }
    @Test
    public void test27 () throws Exception
      {
        edgeDeleteCase (false);
      }
    @Test
    public void test28 () throws Exception
      {
        int q = sessionCount ();
        ProvSess sess = new ProvSess (device);
        GenKey key1 = sess.createECKey ("Key.1",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        GenKey key2 = sess.createECKey ("Key.2",
                                        null /* pin_value */,
                                        null /* pin_policy */,
                                        KeyUsage.AUTHENTICATION).setCertificate ("CN=TEST18");
        sess.closeSession ();
        assertTrue (sess.exists ());
        ProvSess sess2 = new ProvSess (device);
        sess2.postDeleteKey (key2);
        sks.deleteKey (key1.key_handle, new byte[0]);
        sess2.closeSession ();
        assertTrue ("Session count", q == sessionCount ());
      }
    @Test
    public void test29 () throws Exception
      {
        String ok_pin = "1563";
        ProvSess sess = new ProvSess (device);
        PINPol pin_policy = sess.createPINPolicy ("PIN",
                                                  PassphraseFormat.NUMERIC,
                                                  4 /* min_length */, 
                                                  8 /* max_length */,
                                                  (short) 3 /* retry_limit*/, 
                                                  null /* puk_policy */);

        GenKey key = sess.createRSAKey ("Key.1",
                                        1024,
                                        ok_pin /* pin_value */,
                                        pin_policy /* pin_policy */,
                                        KeyUsage.ENCRYPTION).setCertificate ("CN=" + name.getMethodName());
        GenKey key2 = sess.createRSAKey ("Key.2",
                                         1024,
                                         ok_pin /* pin_value */,
                                         pin_policy /* pin_policy */,
                                         KeyUsage.AUTHENTICATION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();
        
        Cipher cipher = Cipher.getInstance (AsymEncryptionAlgorithms.RSA_PKCS_1.getJCEName (), "BC");
        cipher.init (Cipher.ENCRYPT_MODE, key.cert_path[0]);
        byte[] enc = cipher.doFinal (TEST_STRING);
        assertTrue ("Encryption error", ArrayUtil.compare (device.sks.asymmetricKeyDecrypt (key.key_handle,
                                                                                            new byte[0],
                                                                                            AsymEncryptionAlgorithms.RSA_PKCS_1.getURI (), 
                                                                                            ok_pin.getBytes ("UTF-8"), 
                                                                                            enc), TEST_STRING));
        try
          {
            device.sks.asymmetricKeyDecrypt (key.key_handle, 
                                             new byte[0], SignatureAlgorithms.RSA_SHA256.getURI (), 
                                             ok_pin.getBytes ("UTF-8"), 
                                             enc);
            fail ("Alg error");
          }
        catch (SKSException e)
          {
            
          }
        try
          {
            device.sks.asymmetricKeyDecrypt (key.key_handle, 
                                             new byte[]{6},
                                             AsymEncryptionAlgorithms.RSA_PKCS_1.getURI (), 
                                             ok_pin.getBytes ("UTF-8"), 
                                             enc);
            fail ("Parm error");
          }
        catch (SKSException e)
          {
            
          }
        try
          {
            device.sks.asymmetricKeyDecrypt (key.key_handle, 
                                             new byte[0],
                                             AsymEncryptionAlgorithms.RSA_PKCS_1.getURI (), 
                                             (ok_pin + "4").getBytes ("UTF-8"), 
                                             enc);
            fail ("PIN error");
          }
        catch (SKSException e)
          {
            
          }
        try
          {
            device.sks.asymmetricKeyDecrypt (key2.key_handle, 
                                             new byte[0],
                                             AsymEncryptionAlgorithms.RSA_PKCS_1.getURI (), 
                                             ok_pin.getBytes ("UTF-8"), 
                                             enc);
            fail ("Key usage error");
          }
        catch (SKSException e)
          {
            
          }
      }
    @Test
    public void test30 () throws Exception
      {
        String ok_pin = "1563";
        String puk_ok = "17644";
        ProvSess sess = new ProvSess (device);
        PUKPol puk = sess.createPUKPolicy ("PUK",
                                           PassphraseFormat.NUMERIC,
                                           (short) 3 /* retry_limit*/, 
                                           puk_ok /* puk_policy */);
        PINPol pin_policy = sess.createPINPolicy ("PIN",
                                                  PassphraseFormat.NUMERIC,
                                                  4 /* min_length */, 
                                                  8 /* max_length */,
                                                  (short) 3 /* retry_limit*/, 
                                                  puk /* puk_policy */);

        GenKey key = sess.createRSAKey ("Key.1",
                                        1024,
                                        ok_pin /* pin_value */,
                                        pin_policy /* pin_policy */,
                                        KeyUsage.ENCRYPTION).setCertificate ("CN=" + name.getMethodName());
        sess.closeSession ();
        
        Cipher cipher = Cipher.getInstance (AsymEncryptionAlgorithms.RSA_PKCS_1.getJCEName (), "BC");
        cipher.init (Cipher.ENCRYPT_MODE, key.cert_path[0]);
        byte[] enc = cipher.doFinal (TEST_STRING);
        assertTrue ("Encryption error", ArrayUtil.compare (device.sks.asymmetricKeyDecrypt (key.key_handle,
                                                                                            new byte[0],
                                                                                            AsymEncryptionAlgorithms.RSA_PKCS_1.getURI (), 
                                                                                            ok_pin.getBytes ("UTF-8"), 
                                                                                            enc), TEST_STRING));
        for (int i = 0; i < 4; i++)
          {
            try
              {
                device.sks.asymmetricKeyDecrypt (key.key_handle, 
                                                 new byte[0],
                                                 AsymEncryptionAlgorithms.RSA_PKCS_1.getURI (), 
                                                 (ok_pin + "4").getBytes ("UTF-8"), 
                                                 enc);
                fail ("PIN error");
              }
            catch (SKSException e)
              {
                
              }
          }
        try
          {
            device.sks.asymmetricKeyDecrypt (key.key_handle, 
                                             new byte[0],
                                             AsymEncryptionAlgorithms.RSA_PKCS_1.getURI (), 
                                             ok_pin.getBytes ("UTF-8"), 
                                             enc);
            fail ("PIN lock error");
          }
        catch (SKSException e)
          {
            
          }
        try
          {
            device.sks.unlockKey (key.key_handle, (puk_ok + "2").getBytes ("UTF-8"));
            fail ("PUK unlock error");
          }
        catch (SKSException e)
          {
            
          }
        device.sks.unlockKey (key.key_handle, puk_ok.getBytes ("UTF-8"));
        assertTrue ("Encryption error", ArrayUtil.compare (device.sks.asymmetricKeyDecrypt (key.key_handle,
                                                                                            new byte[0],
                                                                                            AsymEncryptionAlgorithms.RSA_PKCS_1.getURI (), 
                                                                                            ok_pin.getBytes ("UTF-8"), 
                                                                                            enc), TEST_STRING));
      }
  }