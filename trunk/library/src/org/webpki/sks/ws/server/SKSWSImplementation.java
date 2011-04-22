/*
 *  Copyright 2006-2011 WebPKI.org (http://webpki.org).
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
package org.webpki.sks.ws.server;

        ///////////////////////////////////////////////
        // Generated by WSCreator 1.0 - Do not edit! //
        ///////////////////////////////////////////////

import java.io.IOException;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;

import java.security.cert.X509Certificate;

import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;

import java.security.spec.X509EncodedKeySpec;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.webpki.crypto.test.DemoKeyStore;

import org.webpki.sks.DeviceInfo;
import org.webpki.sks.EnumeratedKey;
import org.webpki.sks.ProvisioningSession;
import org.webpki.sks.SKSException;
import org.webpki.sks.SecureKeyStore;

/**
 * SKS Web Service Implementation.
 *
 * The purpose of the Web Service is creating a "singleton"
 * service that can concurrently be called by arbitrary
 * SKS-using applications.  The Web Service can be connected
 * to any conformant SKS implementation.
 */ 
@com.sun.xml.ws.developer.SchemaValidation
@WebService(serviceName="SKSWS",
            targetNamespace="http://xmlns.webpki.org/sks/v1.00",
            name="SKSWS.Interface",
            portName="SKSWS.Port",
            wsdlLocation="META-INF/SKSWS.wsdl")
public class SKSWSImplementation
  {

    static SecureKeyStore sks;
    
    static DeviceInfo device_info;
    
    static
      {
        try
          {
            Security.insertProviderAt (new BouncyCastleProvider(), 1);
            sks = (SecureKeyStore) Class.forName (System.getProperty ("sks.implementation")).newInstance ();
            device_info = sks.getDeviceInfo ();
            System.out.println ("Device: " + device_info.getVendorDescription ());
            System.out.println ("Vendor: " + device_info.getVendorName ());
            System.out.println ("API Version: " + device_info.getAPILevel () + "\n");
          }
        catch (ClassNotFoundException e)
          {
            throw new RuntimeException (e);
          }
        catch (InstantiationException e)
          {
            throw new RuntimeException (e);
          }
        catch (IllegalAccessException e)
          {
            throw new RuntimeException (e);
          }
        catch (SKSException e)
          {
            throw new RuntimeException (e);
          }
      }
    
    PublicKey createPublicKeyFromBlob (byte[] blob) throws SKSException
      {
        X509EncodedKeySpec ks = new X509EncodedKeySpec (blob);
        KeyFactory kf = null;
        try
          {
            kf = KeyFactory.getInstance ("RSA");
            return kf.generatePublic(ks);
          }
        catch (GeneralSecurityException e1)
          {
            try
              {
                kf = KeyFactory.getInstance ("EC");
                return kf.generatePublic(ks);
              }
            catch (GeneralSecurityException e2)
              {
                throw new SKSException (e2);
              }
          }
      }
      
    ECPublicKey getECPublicKey (byte[] blob) throws SKSException
      {
        PublicKey public_key = createPublicKeyFromBlob (blob);
        if (public_key instanceof ECPublicKey)
          {
            return (ECPublicKey) public_key;
          }
        throw new SKSException ("Expected EC key");
      }

    @WebMethod(operationName="createProvisioningSession")
    @RequestWrapper(localName="createProvisioningSession", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @ResponseWrapper(localName="createProvisioningSession.Response", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    public void createProvisioningSession (@WebParam(name="Algorithm", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                           String algorithm,
                                           @WebParam(name="ServerSessionID", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                           String server_session_id,
                                           @WebParam(name="ServerEphemeralKey", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                           byte[] server_ephemeral_key,
                                           @WebParam(name="IssuerURI", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                           String issuer_uri,
                                           @WebParam(name="KeyManagementKey", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                           byte[] key_management_key,
                                           @WebParam(name="ClientTime", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                           int client_time,
                                           @WebParam(name="SessionLifeTime", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                           int session_life_time,
                                           @WebParam(name="SessionKeyLimit", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                           short session_key_limit,
                                           @WebParam(name="ClientSessionID", targetNamespace="http://xmlns.webpki.org/sks/v1.00", mode=WebParam.Mode.OUT)
                                           Holder<String> client_session_id,
                                           @WebParam(name="ClientEphemeralKey", targetNamespace="http://xmlns.webpki.org/sks/v1.00", mode=WebParam.Mode.OUT)
                                           Holder<byte[]> client_ephemeral_key,
                                           @WebParam(name="Attestation", targetNamespace="http://xmlns.webpki.org/sks/v1.00", mode=WebParam.Mode.OUT)
                                           Holder<byte[]> attestation,
                                           @WebParam(name="ProvisioningHandle", targetNamespace="http://xmlns.webpki.org/sks/v1.00", mode=WebParam.Mode.OUT)
                                           Holder<Integer> provisioning_handle)
    throws SKSException
      {
        System.out.println ("SERV=" + (server_ephemeral_key == null ? "NULL" : server_ephemeral_key.length) + " KMK=" + (key_management_key == null ? "NULL" : key_management_key.length));
        try
          {
        org.webpki.asn1.BaseASN1Object o = org.webpki.asn1.DerDecoder.decode(server_ephemeral_key, 0);
        System.out.println(o.toString (true, true));
          }
        catch (IOException e)
          {
            System.out.println ("ASN1" + e.getMessage ());
          }
        ProvisioningSession sess = sks.createProvisioningSession (algorithm,
                                                                  server_session_id,
                                                                  getECPublicKey (server_ephemeral_key),
                                                                  issuer_uri,
                                                                  key_management_key == null ? null : createPublicKeyFromBlob (key_management_key),
                                                                  client_time,
                                                                  session_life_time,
                                                                  session_key_limit);
        client_session_id.value = sess.getClientSessionID ();
        client_ephemeral_key.value = sess.getClientEphemeralKey ().getEncoded ();
        attestation.value = sess.getAttestation ();
        provisioning_handle.value = sess.getProvisioningHandle ();
      }

    @WebMethod(operationName="abortProvisioningSession")
    @RequestWrapper(localName="abortProvisioningSession", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @ResponseWrapper(localName="abortProvisioningSession.Response", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    public void abortProvisioningSession (@WebParam(name="ProvisioningHandle", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                          int provisioning_handle)
    throws SKSException
      {
        if (provisioning_handle == 5)
          {
            throw new SKSException ("bad",4);
          }
        sks.abortProvisioningSession (provisioning_handle);
      }

    @WebMethod(operationName="enumerateKeys")
    @RequestWrapper(localName="enumerateKeys", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @ResponseWrapper(localName="enumerateKeys.Response", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    public void enumerateKeys (@WebParam(name="KeyHandle", targetNamespace="http://xmlns.webpki.org/sks/v1.00", mode=WebParam.Mode.INOUT)
                               Holder<Integer> key_handle,
                               @WebParam(name="ProvisioningHandle", targetNamespace="http://xmlns.webpki.org/sks/v1.00", mode=WebParam.Mode.OUT)
                               Holder<Integer> provisioning_handle)
    throws SKSException
      {
        EnumeratedKey ek = sks.enumerateKeys (new EnumeratedKey (key_handle.value, 0));
        key_handle.value = ek.getKeyHandle ();
        provisioning_handle.value = ek.getProvisioningHandle ();
      }

    @WebMethod(operationName="getKeyProtectionInfo")
    @RequestWrapper(localName="getKeyProtectionInfo", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @ResponseWrapper(localName="getKeyProtectionInfo.Response", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @WebResult(name="return", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    public int getKeyProtectionInfo (@WebParam(name="keyHandle", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                     int key_handle,
                                     @WebParam(name="ProtectionStatus", targetNamespace="http://xmlns.webpki.org/sks/v1.00", mode=WebParam.Mode.INOUT)
                                     Holder<String> protection_status,
                                     @WebParam(name="blah", targetNamespace="http://xmlns.webpki.org/sks/v1.00", mode=WebParam.Mode.OUT)
                                     Holder<Byte> blah,
                                     @WebParam(name="X509Certificate", targetNamespace="http://xmlns.webpki.org/sks/v1.00", mode=WebParam.Mode.OUT)
                                     Holder<List<byte[]>> certificate_path)
    throws SKSException
      {
        protection_status.value = protection_status.value + "@";
        blah.value = (byte)(key_handle + 2);
        List<byte[]> certs = new ArrayList<byte[]> ();
        try
          {
            certs.add (DemoKeyStore.getCAKeyStore ().getCertificate ("mykey").getEncoded ());
            certs.add (DemoKeyStore.getSubCAKeyStore ().getCertificate ("mykey").getEncoded ());
          }
        catch (GeneralSecurityException gse)
          {
            throw new SKSException (gse);
          }
        catch (IOException iox)
          {
            throw new SKSException (iox);
          }
        certificate_path.value = certs;
        return 800;
      }

    @WebMethod(operationName="setCertificatePath")
    @RequestWrapper(localName="setCertificatePath", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @ResponseWrapper(localName="setCertificatePath.Response", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    public void setCertificatePath (@WebParam(name="KeyHandle", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                    int key_handle,
                                    @WebParam(name="X509Certificate", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                    List<byte[]> certificate_path,
                                    @WebParam(name="MAC", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                    byte[] mac)
    throws SKSException
      {
        StringBuffer res = new StringBuffer ();
        if (certificate_path.size() == 0)
          {
            res.append ("'null'");
          }
        else
          {
            int i = 0;
            for (byte[] b_arr : certificate_path)
              {
                if (i != 0)
                  {
                     res.append ("\n      ");
                  }
                try
                  {
                    res.append ("C[" + (i++) + "]=" + new org.webpki.crypto.CertificateInfo(org.webpki.crypto.CertificateUtil.getCertificateFromBlob (b_arr)).getSubject ());
                  }
                catch (IOException iox)
                  {
                    throw new SKSException (iox);
                  }
              }
          }
        System.out.println ("Certs=" + res.toString () + " mac=" + mac.length);
      }

    @WebMethod(operationName="getVersion")
    @RequestWrapper(localName="getVersion", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @ResponseWrapper(localName="getVersion.Response", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @WebResult(name="return", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    public String getVersion ()
      {
        return "0.00001";
      }

    @WebMethod(operationName="getCertPath")
    @RequestWrapper(localName="getCertPath", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @ResponseWrapper(localName="getCertPath.Response", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    @WebResult(name="X509Certificate", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
    public List<byte[]> getCertPath (@WebParam(name="want", targetNamespace="http://xmlns.webpki.org/sks/v1.00")
                                     boolean want)
    throws SKSException
      {
        List<byte[]> certs = new ArrayList<byte[]> ();
        try
          {
            certs.add (DemoKeyStore.getCAKeyStore ().getCertificate ("mykey").getEncoded ());
            certs.add (DemoKeyStore.getSubCAKeyStore ().getCertificate ("mykey").getEncoded ());
          }
        catch (GeneralSecurityException gse)
          {
            throw new SKSException (gse);
          }
        catch (IOException iox)
          {
            throw new SKSException (iox);
          }
        return want ? certs : null;
      }

    public static void main (String[] args)
      {
        if (args.length != 1)
          {
            System.out.println ("Missing URL");
          }
        Endpoint endpoint = Endpoint.create (new SKSWSImplementation ());
        endpoint.publish (args[0]);
      }
  }
