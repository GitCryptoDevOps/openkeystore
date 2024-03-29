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
package org.webpki.crypto;

import java.io.IOException;

import java.security.KeyFactory;
import java.security.PublicKey;

import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;

import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.security.spec.X509EncodedKeySpec;

public enum KeyAlgorithms implements SKSAlgorithms
  {
    RSA1024     ("http://xmlns.webpki.org/sks/algorithm#rsa1024", null,
                 "RSA",
                 1024,
                 AsymSignatureAlgorithms.RSA_SHA1,
                 false,
                 true,
                 null,
                 null),

    RSA2048     ("http://xmlns.webpki.org/sks/algorithm#rsa2048", null,
                 "RSA",
                 2048,
                 AsymSignatureAlgorithms.RSA_SHA256,
                 false,
                 true,
                 null,
                 null),

    RSA3072     ("http://xmlns.webpki.org/sks/algorithm#rsa3072", null,
                 "RSA",
                 3072,
                 AsymSignatureAlgorithms.RSA_SHA512,
                 false,
                 false,
                 null,
                 null),

    RSA4096     ("http://xmlns.webpki.org/sks/algorithm#rsa4096", null,
                 "RSA",
                 4096,
                 AsymSignatureAlgorithms.RSA_SHA512,
                 false,
                 false,
                 null,
                 null),

    RSA1024_EXP ("http://xmlns.webpki.org/sks/algorithm#rsa1024.exp", null,
                 "RSA",
                 1024,
                 AsymSignatureAlgorithms.RSA_SHA1,
                 true,
                 false,
                 null,
                 null),

    RSA2048_EXP ("http://xmlns.webpki.org/sks/algorithm#rsa2048.exp", null,
                 "RSA",
                 2048,
                 AsymSignatureAlgorithms.RSA_SHA256,
                 true,
                 false,
                 null,
                 null),

    RSA3072_EXP ("http://xmlns.webpki.org/sks/algorithm#rsa3072.exp", null,
                 "RSA",
                 3072,
                 AsymSignatureAlgorithms.RSA_SHA512,
                 true,
                 false,
                 null,
                 null),

    RSA4096_EXP ("http://xmlns.webpki.org/sks/algorithm#rsa4096.exp", null,
                 "RSA",
                 4096,
                 AsymSignatureAlgorithms.RSA_SHA512,
                 true,
                 false,
                 null,
                 null),

    NIST_B_163  ("http://xmlns.webpki.org/sks/algorithm#ec.nist.b163", null,
                 "sect163r2",
                 163,
                 AsymSignatureAlgorithms.ECDSA_SHA256,
                 false,
                 false,
                 "1.3.132.0.15",
                 new byte[]
                    {(byte)0x30, (byte)0x40, (byte)0x30, (byte)0x10, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86,
                     (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x05, (byte)0x2B,
                     (byte)0x81, (byte)0x04, (byte)0x00, (byte)0x0F, (byte)0x03, (byte)0x2C, (byte)0x00, (byte)0x04,
                     (byte)0x03, (byte)0x9A, (byte)0xC6, (byte)0x0F, (byte)0x63, (byte)0x03, (byte)0xC3, (byte)0x77,
                     (byte)0x14, (byte)0xFD, (byte)0x80, (byte)0xC5, (byte)0xA8, (byte)0xEE, (byte)0x72, (byte)0x95,
                     (byte)0x72, (byte)0x10, (byte)0xAC, (byte)0x6C, (byte)0xA7, (byte)0x02, (byte)0xD7, (byte)0x30,
                     (byte)0x15, (byte)0xEC, (byte)0xE1, (byte)0x53, (byte)0xC1, (byte)0x50, (byte)0x74, (byte)0x47,
                     (byte)0x56, (byte)0x8A, (byte)0xBC, (byte)0x0F, (byte)0x05, (byte)0x53, (byte)0x78, (byte)0xB1,
                     (byte)0x30, (byte)0x8E}),

    NIST_B_233  ("http://xmlns.webpki.org/sks/algorithm#ec.nist.b233", null,
                 "sect233r1",
                 233,
                 AsymSignatureAlgorithms.ECDSA_SHA512,
                 false,
                 false,
                 "1.3.132.0.27",
                 new byte[]
                    {(byte)0x30, (byte)0x52, (byte)0x30, (byte)0x10, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86,
                     (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x05, (byte)0x2B,
                     (byte)0x81, (byte)0x04, (byte)0x00, (byte)0x1B, (byte)0x03, (byte)0x3E, (byte)0x00, (byte)0x04,
                     (byte)0x01, (byte)0x8D, (byte)0x7E, (byte)0x41, (byte)0xF5, (byte)0xE9, (byte)0xCE, (byte)0x74,
                     (byte)0x00, (byte)0x6C, (byte)0x4E, (byte)0xE9, (byte)0x9C, (byte)0xAB, (byte)0x12, (byte)0x4F,
                     (byte)0x67, (byte)0x58, (byte)0x5A, (byte)0x10, (byte)0x4C, (byte)0x9A, (byte)0xCE, (byte)0xAA,
                     (byte)0x45, (byte)0x01, (byte)0x50, (byte)0xB5, (byte)0x59, (byte)0x91, (byte)0x01, (byte)0x21,
                     (byte)0x9C, (byte)0x0B, (byte)0x90, (byte)0x24, (byte)0xA3, (byte)0x55, (byte)0x27, (byte)0x0D,
                     (byte)0xE4, (byte)0xC9, (byte)0xD2, (byte)0xCB, (byte)0x7A, (byte)0x86, (byte)0x79, (byte)0x33,
                     (byte)0xF6, (byte)0x18, (byte)0xB8, (byte)0x4D, (byte)0xB8, (byte)0xD0, (byte)0x9C, (byte)0x81,
                     (byte)0xB4, (byte)0x99, (byte)0x3B, (byte)0x94}),

    NIST_B_283  ("http://xmlns.webpki.org/sks/algorithm#ec.nist.b283", null,
                 "sect283r1",
                 283,
                 AsymSignatureAlgorithms.ECDSA_SHA512,
                 false,
                 false,
                 "1.3.132.0.17",
                 new byte[]
                    {(byte)0x30, (byte)0x5E, (byte)0x30, (byte)0x10, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86,
                     (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x05, (byte)0x2B,
                     (byte)0x81, (byte)0x04, (byte)0x00, (byte)0x11, (byte)0x03, (byte)0x4A, (byte)0x00, (byte)0x04,
                     (byte)0x05, (byte)0xE9, (byte)0x16, (byte)0xB8, (byte)0x17, (byte)0x2C, (byte)0xF3, (byte)0xDA,
                     (byte)0xDF, (byte)0x3D, (byte)0x9E, (byte)0xFB, (byte)0x0D, (byte)0xC3, (byte)0x24, (byte)0x20,
                     (byte)0x7E, (byte)0x4F, (byte)0x1E, (byte)0x74, (byte)0xAE, (byte)0xFB, (byte)0xB3, (byte)0x0F,
                     (byte)0xD7, (byte)0xEC, (byte)0x09, (byte)0x71, (byte)0xB3, (byte)0x49, (byte)0xE2, (byte)0xD1,
                     (byte)0xED, (byte)0xED, (byte)0x64, (byte)0xF7, (byte)0x07, (byte)0x0C, (byte)0xA7, (byte)0x5A,
                     (byte)0xCD, (byte)0xEC, (byte)0x73, (byte)0x4C, (byte)0xFD, (byte)0x2B, (byte)0x57, (byte)0xFF,
                     (byte)0xC9, (byte)0x44, (byte)0xDC, (byte)0x76, (byte)0x1B, (byte)0xDF, (byte)0x33, (byte)0x51,
                     (byte)0xCF, (byte)0x07, (byte)0x7D, (byte)0x84, (byte)0xEC, (byte)0x23, (byte)0xC6, (byte)0x2C,
                     (byte)0x1E, (byte)0x12, (byte)0x0D, (byte)0x95, (byte)0xFD, (byte)0xC7, (byte)0xC7, (byte)0x0C}),

    NIST_P_192  ("http://xmlns.webpki.org/sks/algorithm#ec.nist.p192", null,
                 "secp192r1",
                 192,
                 AsymSignatureAlgorithms.ECDSA_SHA256,
                 false,
                 false,
                 "1.2.840.10045.3.1.1",
                 new byte[]
                    {(byte)0x30, (byte)0x49, (byte)0x30, (byte)0x13, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86,
                     (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x08, (byte)0x2A,
                     (byte)0x86, (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x03, (byte)0x01, (byte)0x01, (byte)0x03,
                     (byte)0x32, (byte)0x00, (byte)0x04, (byte)0xCC, (byte)0xE7, (byte)0x03, (byte)0x5B, (byte)0xF6,
                     (byte)0xAB, (byte)0xA7, (byte)0xFD, (byte)0xF5, (byte)0x71, (byte)0xDB, (byte)0x98, (byte)0xF4,
                     (byte)0x97, (byte)0xEF, (byte)0x84, (byte)0x86, (byte)0x4C, (byte)0x45, (byte)0x2A, (byte)0xF8,
                     (byte)0x0F, (byte)0xD9, (byte)0x04, (byte)0x9C, (byte)0x5F, (byte)0xF7, (byte)0xE8, (byte)0xF3,
                     (byte)0xC3, (byte)0xBE, (byte)0xF7, (byte)0x06, (byte)0x6E, (byte)0xCC, (byte)0x33, (byte)0xA7,
                     (byte)0xAC, (byte)0x61, (byte)0x5C, (byte)0x58, (byte)0x7C, (byte)0x98, (byte)0x25, (byte)0x82,
                     (byte)0x17, (byte)0xD7, (byte)0x69}),

    NIST_P_256  ("http://xmlns.webpki.org/sks/algorithm#ec.nist.p256", "P-256",
                 "secp256r1",
                 256,
                 AsymSignatureAlgorithms.ECDSA_SHA256,
                 false,
                 true,
                 "1.2.840.10045.3.1.7",
                 new byte[]
                    {(byte)0x30, (byte)0x59, (byte)0x30, (byte)0x13, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86,
                     (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x08, (byte)0x2A,
                     (byte)0x86, (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x03, (byte)0x01, (byte)0x07, (byte)0x03,
                     (byte)0x42, (byte)0x00, (byte)0x04, (byte)0x8B, (byte)0xDF, (byte)0x5D, (byte)0xA2, (byte)0xBE,
                     (byte)0x57, (byte)0x73, (byte)0xAC, (byte)0x78, (byte)0x86, (byte)0xD3, (byte)0xE5, (byte)0xE6,
                     (byte)0xC4, (byte)0xA5, (byte)0x6C, (byte)0x32, (byte)0xE2, (byte)0x28, (byte)0xBE, (byte)0xA0,
                     (byte)0x0F, (byte)0x8F, (byte)0xBF, (byte)0x29, (byte)0x1E, (byte)0xC6, (byte)0x67, (byte)0xB3,
                     (byte)0x51, (byte)0x99, (byte)0xB7, (byte)0xAD, (byte)0x13, (byte)0x0C, (byte)0x5A, (byte)0x7C,
                     (byte)0x66, (byte)0x4B, (byte)0x47, (byte)0xF6, (byte)0x1F, (byte)0x41, (byte)0xE9, (byte)0xB3,
                     (byte)0xB2, (byte)0x40, (byte)0xC0, (byte)0x65, (byte)0xF8, (byte)0x8F, (byte)0x30, (byte)0x0A,
                     (byte)0xCA, (byte)0x5F, (byte)0xB5, (byte)0x09, (byte)0x6E, (byte)0x95, (byte)0xCF, (byte)0x78,
                     (byte)0x7C, (byte)0x0D, (byte)0xB2}),

    NIST_P_384  ("http://xmlns.webpki.org/sks/algorithm#ec.nist.p384", "P-384",
                 "secp384r1",
                 384,
                 AsymSignatureAlgorithms.ECDSA_SHA512,
                 false,
                 true,
                 "1.3.132.0.34",
                 new byte[]
                    {(byte)0x30, (byte)0x76, (byte)0x30, (byte)0x10, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86,
                     (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x05, (byte)0x2B,
                     (byte)0x81, (byte)0x04, (byte)0x00, (byte)0x22, (byte)0x03, (byte)0x62, (byte)0x00, (byte)0x04,
                     (byte)0x63, (byte)0x5C, (byte)0x35, (byte)0x5C, (byte)0xC0, (byte)0xDF, (byte)0x90, (byte)0x16,
                     (byte)0xA6, (byte)0x18, (byte)0xF1, (byte)0x50, (byte)0xA7, (byte)0x73, (byte)0xE7, (byte)0x05,
                     (byte)0x22, (byte)0x36, (byte)0xF7, (byte)0xDC, (byte)0x9F, (byte)0xD8, (byte)0xA5, (byte)0xAC,
                     (byte)0x71, (byte)0x9F, (byte)0x1C, (byte)0x9A, (byte)0x71, (byte)0x94, (byte)0x8B, (byte)0x81,
                     (byte)0x15, (byte)0x32, (byte)0x24, (byte)0x92, (byte)0x11, (byte)0x11, (byte)0xDC, (byte)0x7E,
                     (byte)0x9D, (byte)0x70, (byte)0x1A, (byte)0x9B, (byte)0x83, (byte)0x33, (byte)0x8B, (byte)0x59,
                     (byte)0xC1, (byte)0x93, (byte)0x34, (byte)0x7F, (byte)0x58, (byte)0x0D, (byte)0x91, (byte)0xC4,
                     (byte)0xD2, (byte)0x20, (byte)0x8F, (byte)0x64, (byte)0x16, (byte)0x16, (byte)0xEE, (byte)0x07,
                     (byte)0x51, (byte)0xC3, (byte)0xF8, (byte)0x56, (byte)0x5B, (byte)0xCD, (byte)0x49, (byte)0xFE,
                     (byte)0xE0, (byte)0xE2, (byte)0xD5, (byte)0xC5, (byte)0x79, (byte)0xD1, (byte)0xA6, (byte)0x18,
                     (byte)0x82, (byte)0xBD, (byte)0x65, (byte)0x83, (byte)0xB6, (byte)0x84, (byte)0x77, (byte)0xE8,
                     (byte)0x1F, (byte)0xB8, (byte)0xD7, (byte)0x3D, (byte)0x79, (byte)0x88, (byte)0x2E, (byte)0x98}),

    NIST_P_521  ("http://xmlns.webpki.org/sks/algorithm#ec.nist.p521", "P-521",
                 "secp521r1",
                 521,
                 AsymSignatureAlgorithms.ECDSA_SHA512,
                 false,
                 true,
                 "1.3.132.0.35",
                 new byte[]
                    {(byte)0x30, (byte)0x81, (byte)0x9B, (byte)0x30, (byte)0x10, (byte)0x06, (byte)0x07, (byte)0x2A,
                     (byte)0x86, (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x05,
                     (byte)0x2B, (byte)0x81, (byte)0x04, (byte)0x00, (byte)0x23, (byte)0x03, (byte)0x81, (byte)0x86,
                     (byte)0x00, (byte)0x04, (byte)0x01, (byte)0xFC, (byte)0xA0, (byte)0x56, (byte)0x27, (byte)0xB7,
                     (byte)0x68, (byte)0x25, (byte)0xC5, (byte)0x83, (byte)0xD1, (byte)0x34, (byte)0x0A, (byte)0xAE,
                     (byte)0x96, (byte)0x1D, (byte)0xDC, (byte)0xE0, (byte)0x95, (byte)0xC5, (byte)0xE0, (byte)0x25,
                     (byte)0x1F, (byte)0x46, (byte)0xF6, (byte)0x36, (byte)0xD7, (byte)0x3F, (byte)0xD9, (byte)0x5A,
                     (byte)0x15, (byte)0xE3, (byte)0x05, (byte)0xBA, (byte)0x14, (byte)0x06, (byte)0x1B, (byte)0xEB,
                     (byte)0xD4, (byte)0x88, (byte)0xFC, (byte)0x0D, (byte)0x87, (byte)0x02, (byte)0x15, (byte)0x4E,
                     (byte)0x7E, (byte)0xC0, (byte)0x9F, (byte)0xF6, (byte)0x1C, (byte)0x80, (byte)0x2C, (byte)0xE6,
                     (byte)0x0D, (byte)0xF5, (byte)0x0E, (byte)0x6C, (byte)0xD9, (byte)0x55, (byte)0xFA, (byte)0xBD,
                     (byte)0x6B, (byte)0x55, (byte)0xA1, (byte)0x0E, (byte)0x00, (byte)0x55, (byte)0x12, (byte)0x35,
                     (byte)0x8D, (byte)0xFC, (byte)0x0A, (byte)0x42, (byte)0xE5, (byte)0x78, (byte)0x09, (byte)0xD6,
                     (byte)0xF6, (byte)0x0C, (byte)0xBE, (byte)0x15, (byte)0x0A, (byte)0x7D, (byte)0xC2, (byte)0x2E,
                     (byte)0x98, (byte)0xA1, (byte)0xE1, (byte)0x6A, (byte)0xF1, (byte)0x1F, (byte)0xD2, (byte)0x9F,
                     (byte)0x9A, (byte)0x81, (byte)0x65, (byte)0x51, (byte)0x8F, (byte)0x6E, (byte)0xF1, (byte)0x3B,
                     (byte)0x95, (byte)0x6B, (byte)0xCE, (byte)0x51, (byte)0x09, (byte)0xFF, (byte)0x23, (byte)0xDC,
                     (byte)0xE8, (byte)0x71, (byte)0x1A, (byte)0x94, (byte)0xC7, (byte)0x8E, (byte)0x4A, (byte)0xA9,
                     (byte)0x22, (byte)0xA8, (byte)0x87, (byte)0x64, (byte)0xD0, (byte)0x36, (byte)0xAF, (byte)0xD3,
                     (byte)0x69, (byte)0xAC, (byte)0xCA, (byte)0xCB, (byte)0x1A, (byte)0x96}),
                     
     SECG_K_256 ("http://xmlns.webpki.org/sks/algorithm#ec.secg.p256k1", null,
                 "secp256k1",
                 256,
                 AsymSignatureAlgorithms.ECDSA_SHA256,
                 false,
                 false,
                 "1.3.132.0.10",
                 new byte[]
                    {(byte)0x30, (byte)0x56, (byte)0x30, (byte)0x10, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86,
                     (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x05, (byte)0x2B,
                     (byte)0x81, (byte)0x04, (byte)0x00, (byte)0x0A, (byte)0x03, (byte)0x42, (byte)0x00, (byte)0x04,
                     (byte)0xA9, (byte)0xF7, (byte)0xEF, (byte)0x65, (byte)0x26, (byte)0x2F, (byte)0xDB, (byte)0x11,
                     (byte)0xE3, (byte)0xDA, (byte)0x7C, (byte)0x9D, (byte)0xDF, (byte)0x1F, (byte)0x2E, (byte)0x32,
                     (byte)0x49, (byte)0x99, (byte)0x4B, (byte)0x02, (byte)0x07, (byte)0x02, (byte)0x78, (byte)0x94,
                     (byte)0xFF, (byte)0x1C, (byte)0x5A, (byte)0x30, (byte)0xB3, (byte)0x39, (byte)0x44, (byte)0xF4,
                     (byte)0x50, (byte)0xBE, (byte)0xC9, (byte)0x6C, (byte)0xAE, (byte)0xE6, (byte)0xA5, (byte)0xC0,
                     (byte)0x8B, (byte)0xF9, (byte)0x29, (byte)0x5B, (byte)0xA0, (byte)0x16, (byte)0xC5, (byte)0x36,
                     (byte)0xDD, (byte)0xE6, (byte)0xA1, (byte)0x21, (byte)0x6D, (byte)0x80, (byte)0x77, (byte)0xD7,
                     (byte)0x5B, (byte)0xC1, (byte)0x32, (byte)0x44, (byte)0xA6, (byte)0x32, (byte)0x06, (byte)0xA9}),                       

    BRAINPOOL_P_256 (
                 "http://xmlns.webpki.org/sks/algorithm#ec.brainpool.p256r1", null,
                 "brainpoolP256r1",
                 256,
                 AsymSignatureAlgorithms.ECDSA_SHA256,
                 false,
                 true,
                 "1.3.36.3.3.2.8.1.1.7",
                 new byte[]
                    {(byte)0x30, (byte)0x5A, (byte)0x30, (byte)0x14, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86,
                     (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x09, (byte)0x2B,
                     (byte)0x24, (byte)0x03, (byte)0x03, (byte)0x02, (byte)0x08, (byte)0x01, (byte)0x01, (byte)0x07,
                     (byte)0x03, (byte)0x42, (byte)0x00, (byte)0x04, (byte)0x26, (byte)0x3C, (byte)0x91, (byte)0x3F,
                     (byte)0x6B, (byte)0x91, (byte)0x10, (byte)0x6F, (byte)0xE4, (byte)0xA2, (byte)0x2D, (byte)0xA4,
                     (byte)0xBB, (byte)0xAB, (byte)0xCE, (byte)0x9E, (byte)0x41, (byte)0x01, (byte)0x0B, (byte)0xB0,
                     (byte)0xC3, (byte)0x84, (byte)0xEF, (byte)0x35, (byte)0x0D, (byte)0x66, (byte)0xEE, (byte)0x0C,
                     (byte)0xEC, (byte)0x60, (byte)0xB6, (byte)0xF5, (byte)0x54, (byte)0x54, (byte)0x27, (byte)0x2A,
                     (byte)0x1D, (byte)0x07, (byte)0x61, (byte)0xB0, (byte)0xC3, (byte)0x01, (byte)0xE8, (byte)0xCB,
                     (byte)0x52, (byte)0xF5, (byte)0x03, (byte)0xC1, (byte)0x0C, (byte)0x3F, (byte)0xF0, (byte)0x97,
                     (byte)0xCD, (byte)0xC9, (byte)0x45, (byte)0xF3, (byte)0x21, (byte)0xC5, (byte)0xCF, (byte)0x41,
                     (byte)0x17, (byte)0xF3, (byte)0x3A, (byte)0xB4});

    private final String sks_id;                     // As (typically) expressed in protocols
    private final String josename;                   // As expressed in JOSE.  Only applicable EC curves
    private final String jcename;                    // As expressed for JCE
    private final int length_in_bits;                // You guessed it :-)
    private final AsymSignatureAlgorithms pref_alg;  // A sort of a "guide"
    private final boolean has_parameters;            // Parameter value required?
    private final boolean sks_mandatory;             // If required in SKS
    private final String ec_domain_oid;              // EC domain as expressed in ASN.1 messages, null for RSA
    private final ECParameterSpec ec_parm_spec;      // EC for creating a BC/JDK compatible method

    public static final String XML_DSIG_CURVE_PREFIX = "urn:oid:";

    private KeyAlgorithms (String sks_id,
                           String josename,
                           String jcename,
                           int length_in_bits,
                           AsymSignatureAlgorithms pref_alg,
                           boolean has_parameters,
                           boolean sks_mandatory,
                           String ec_domain_oid,
                           byte[] sample_public_key)
      {
        this.sks_id = sks_id;
        this.josename = josename;
        this.jcename = jcename;
        this.length_in_bits = length_in_bits;
        this.pref_alg = pref_alg;
        this.has_parameters = has_parameters;
        this.sks_mandatory = sks_mandatory;
        this.ec_domain_oid = ec_domain_oid;
        ECParameterSpec temp_ec_parm_spec = null;
        if (sample_public_key != null)
          {
            try
              {
                temp_ec_parm_spec = ((ECPublicKey) KeyFactory.getInstance ("EC").generatePublic (
                    new X509EncodedKeySpec (sample_public_key))).getParams ();
              }
            catch (Exception e)
              {
                new RuntimeException (e);
              }
          }
        this.ec_parm_spec = temp_ec_parm_spec;
      }


    @Override
    public boolean isSymmetric ()
      {
        return false;
      }


    @Override
    public boolean isMandatorySKSAlgorithm ()
      {
        return sks_mandatory;
      }


    @Override
    public String getJCEName ()
      {
        return jcename;
      }


    @Override
    public String getURI ()
      {
        return sks_id;
      }


    @Override
    public String getOID ()
      {
        return null;
      }

    
    @Override
    public String getJOSEName ()
      {
         return josename;
      }

    
    public String getECDomainOID ()
      {
        return ec_domain_oid;
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
 

    public AsymSignatureAlgorithms getRecommendedSignatureAlgorithm ()
      {
        return pref_alg;
      }


    public boolean hasParameters ()
      {
        return has_parameters;
      }


    public ECParameterSpec getECParameterSpec ()
      {
        return ec_parm_spec;
      }


    public static KeyAlgorithms getECKeyAlgorithm (ECParameterSpec ec_parameters) throws IOException
      {
        EllipticCurve ec_curve = ec_parameters.getCurve ();
        for (KeyAlgorithms alg : values ())
          {
            if (alg.isECKey () && alg.ec_parm_spec.getCurve ().equals (ec_curve))
              {
                return alg;
              }
          }
        throw new IOException ("Unknown EC curve: " + ec_curve.toString ());
      }
 

    public static KeyAlgorithms getKeyAlgorithm (PublicKey public_key, Boolean key_parameters) throws IOException
      {
        if (public_key instanceof ECPublicKey)
          {
            return getECKeyAlgorithm (((ECPublicKey) public_key).getParams ());
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


    public static KeyAlgorithms getKeyAlgorithmFromID (String algorithm_id) throws IOException
      {
        for (KeyAlgorithms alg : values ())
          {
            if (algorithm_id.equals (alg.sks_id) || algorithm_id.equals (alg.josename))
              {
                return alg;
              }
          }
        throw new IOException ("Unknown algorithm: " + algorithm_id);
      }

  }
