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
package org.webpki.keygen2;

import java.io.IOException;
import java.io.Serializable;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

import java.security.cert.X509Certificate;

import java.security.interfaces.ECPublicKey;

public interface ServerCryptoInterface extends Serializable
  {
    ECPublicKey generateEphemeralKey () throws IOException, GeneralSecurityException;
    
    void generateAndVerifySessionKey (ECPublicKey client_ephemeral_key,
                                      byte[] kdf_data,
                                      byte[] session_key_mac_data,
                                      X509Certificate device_certificate,
                                      byte[] session_attestation) throws IOException, GeneralSecurityException;;

    public byte[] mac (byte[] data, byte[] key_modifier) throws IOException, GeneralSecurityException;
    
    public byte[] encrypt (byte[] data) throws IOException, GeneralSecurityException;

    public byte[] generateNonce () throws IOException, GeneralSecurityException;

    public byte[] generateKeyManagementAuthorization (PublicKey key_management_key, byte[] data) throws IOException, GeneralSecurityException;
    
    public PublicKey[] enumerateKeyManagementKeys () throws IOException, GeneralSecurityException;
  }
