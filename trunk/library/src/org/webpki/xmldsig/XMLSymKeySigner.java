package org.webpki.xmldsig;

import java.io.IOException;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.webpki.crypto.MacAlgorithms;
import org.webpki.crypto.SignatureAlgorithms;
import org.webpki.crypto.SymKeySignerInterface;


public class XMLSymKeySigner extends XMLSignerCore
  {
    SymKeySignerInterface sym_signer;

    PublicKey populateKeys (XMLSignatureWrapper r) throws GeneralSecurityException, IOException
      {
        return null;
      }

    byte[] getSignatureBlob (byte[] data, SignatureAlgorithms sig_alg) throws GeneralSecurityException, IOException
      {
        return sym_signer.signData (data);
      }


    /**
     * Creates an XMLSymKeySigner.
     */
    public XMLSymKeySigner (SymKeySignerInterface sym_signer)
      {
        this.sym_signer = sym_signer;
      }

  }
