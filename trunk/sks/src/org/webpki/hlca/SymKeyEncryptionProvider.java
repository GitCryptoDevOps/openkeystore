package org.webpki.hlca;

import java.io.IOException;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import org.webpki.crypto.SymEncryptionAlgorithms;

import org.webpki.sks.SecureKeyStore;

import org.webpki.util.WrappedException;

import org.webpki.util.ArrayUtil;


/**
 * Symmetric key encryption/decryption support.
 */
public class SymKeyEncryptionProvider extends HighLevelKeyStore
  {

    public SymKeyEncryptionProvider (SecureKeyStore sks)
      {
        super (sks);
      }


    boolean wantAsymmetricKeys ()
      {
        return false;
      }


    byte[] localOp (boolean encrypt_flag, byte[] data, SymEncryptionAlgorithms algorithm, byte[] iv) throws IOException
      {
        return sks.symmetricKeyEncrypt (key_handle,
                                        encrypt_flag,
                                        iv,
                                        algorithm.getURI (),
                                        getAuthorization (),
                                        data);
      }


    public byte[] encrypt (byte[] data, SymEncryptionAlgorithms algorithm) throws IOException
      {
        if (algorithm.needsIV ())
          {
            byte[] iv = new byte[16];
            try
              {
                SecureRandom.getInstance ("SHA1PRNG").nextBytes (iv);
              }
            catch (GeneralSecurityException gse)
              {
                throw new WrappedException (gse);
              }
            return ArrayUtil.add (iv, localOp (true, data, algorithm, iv));
          }
        return localOp (true, data, algorithm, null);
      }
   

    public byte[] decrypt (byte[] data, SymEncryptionAlgorithms algorithm) throws IOException
      {
        if (algorithm.needsIV ())
          {
            byte[] iv = new byte[16];
            System.arraycopy (data, 0, iv, 0, 16);
            byte[] real_data = new byte[data.length - 16];
            System.arraycopy (data, 16, real_data, 0, real_data.length);
            return localOp (false, real_data, algorithm, iv);
          }
        return localOp (false, data, algorithm, null);
      }
   
  }