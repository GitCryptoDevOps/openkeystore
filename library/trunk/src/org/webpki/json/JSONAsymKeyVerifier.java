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
package org.webpki.json;

import java.io.IOException;

import java.security.PublicKey;

/**
 * Initiatiator object for asymmetric key signature verifiers.
 */
public class JSONAsymKeyVerifier extends JSONVerifier
  {
    private static final long serialVersionUID = 1L;

    PublicKey expected_public_key;

    /**
     * Verifier for asymmetric keys.
     * Note that you can access the received public key from {@link JSONSignatureDecoder}
     * which is useful if there are multiple keys possible.
     * @param expected_public_key Expected public key
     * @throws IOException
     */
    public JSONAsymKeyVerifier (PublicKey expected_public_key) throws IOException
      {
        this.expected_public_key = expected_public_key;
      }

    @Override
    void verify (JSONSignatureDecoder signature_decoder) throws IOException
      {
        if (!expected_public_key.equals (signature_decoder.public_key))
          {
            throw new IOException ("Provided public key differs from the signature key");
          }
       }

    @Override
    JSONSignatureTypes getVerifierType () throws IOException
      {
        return JSONSignatureTypes.ASYMMETRIC_KEY;
      }
  }
