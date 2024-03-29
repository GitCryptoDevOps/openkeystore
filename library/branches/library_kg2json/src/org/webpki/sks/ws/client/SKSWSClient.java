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
package org.webpki.sks.ws.client;

import java.io.IOException;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;

import java.security.cert.X509Certificate;

import java.security.interfaces.ECPublicKey;

import java.security.spec.X509EncodedKeySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.ws.Holder;

import javax.xml.ws.BindingProvider;

import org.webpki.crypto.CertificateUtil;

import org.webpki.sks.DeviceInfo;
import org.webpki.sks.EnumeratedKey;
import org.webpki.sks.EnumeratedProvisioningSession;
import org.webpki.sks.Extension;
import org.webpki.sks.InputMethod;
import org.webpki.sks.KeyAttributes;
import org.webpki.sks.KeyData;
import org.webpki.sks.KeyProtectionInfo;
import org.webpki.sks.ProvisioningSession;
import org.webpki.sks.SKSException;
import org.webpki.sks.SecureKeyStore;

import org.webpki.sks.ws.TrustedGUIAuthorization;
import org.webpki.sks.ws.WSSpecific;

public class SKSWSClient implements SecureKeyStore, WSSpecific
  {
    public static final String DEFAULT_URL_PROPERTY = "org.webpki.sks.ws.client.url";
    
    private SKSWSProxy proxy;
    
    private String port;
    
    private TrustedGUIAuthorization tga_provider;
    
    private String device_id;
    
    private class AuthorizationHolder
      {
        byte[] value;
        
        AuthorizationHolder (byte[] authorization)
          {
            value = authorization;
          }
      }
    
    boolean getTrustedGUIAuthorization (int key_handle,
                                        AuthorizationHolder authorization_holder,
                                        boolean auth_error) throws SKSException
      {
        if (tga_provider == null)
          {
            return false;
          }
        
        KeyProtectionInfo kpi = getKeyProtectionInfo (key_handle);
        if (kpi.hasLocalPINProtection ())
          {
            if (kpi.getPINInputMethod () == InputMethod.TRUSTED_GUI)
              {
                if (authorization_holder.value != null)
                  {
                    throw new SKSException ("Redundant \"Authorization\"", SKSException.ERROR_AUTHORIZATION);
                  }
              }
            else if (kpi.getPINInputMethod () == InputMethod.PROGRAMMATIC || authorization_holder.value != null)
              {
                return false;
              }
            KeyAttributes ka = getKeyAttributes (key_handle);
            authorization_holder.value = tga_provider.getTrustedAuthorization (kpi.getPINFormat (),
                                                                               kpi.getPINGrouping (),
                                                                               ka.getAppUsage (),
                                                                               ka.getFriendlyName ());
            return authorization_holder.value != null;
          }
        return false;
      }
    
    
    public SKSWSClient (String port)
      {
        this.port = port;
      }

    public SKSWSClient ()
      {
        this (System.getProperty (DEFAULT_URL_PROPERTY));
      }

    private PublicKey createPublicKeyFromBlob (byte[] blob) throws GeneralSecurityException
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
            kf = KeyFactory.getInstance ("EC");
            return kf.generatePublic(ks);
          }
      }
    
    private X509Certificate[] getCertArrayFromBlobs (List<byte[]> blobs) throws IOException
      {
        Vector<X509Certificate> certs = new Vector<X509Certificate> ();
        for (byte[] bcert : blobs)
          {
            certs.add (CertificateUtil.getCertificateFromBlob (bcert));
          }
        return certs.toArray (new X509Certificate[0]);
      }
    
    private ECPublicKey getECPublicKey (byte[] blob) throws GeneralSecurityException
      {
        PublicKey public_key = createPublicKeyFromBlob (blob);
        if (public_key instanceof ECPublicKey)
          {
            return (ECPublicKey) public_key;
          }
        throw new GeneralSecurityException ("Expected EC key");
      }

    /**
     * Factory method. Each WS call should use this method.
     * 
     * @return A handle to a shared WS instance
     */
    private SKSWSProxy getSKSWS ()
      {
        if (proxy == null)
          {
            synchronized (this)
              {
                SKSWS service = new SKSWS ();
                SKSWSProxy temp_proxy = service.getSKSWSPort ();
                if (port != null && port.length () > 0)
                  {
                    Map<String,Object> request_object = ((BindingProvider) temp_proxy).getRequestContext ();
                    request_object.put (BindingProvider.ENDPOINT_ADDRESS_PROPERTY, port);
                  }
                proxy = temp_proxy;
              }
          }
        return proxy;
      }

    @Override
    public DeviceInfo getDeviceInfo () throws SKSException
      {
        try
          {
            Holder<Short> api_level = new Holder<Short> ();
            Holder<Byte> device_type = new Holder<Byte> ();
            Holder<String> update_url = new Holder<String> ();
            Holder<String> vendor_name = new Holder<String> ();
            Holder<String> vendor_description = new Holder<String> ();
            Holder<List<byte[]>> certificate_path = new Holder<List<byte[]>> ();
            Holder<List<String>> supported_algorithms = new Holder<List<String>> ();
            Holder<Integer> crypto_data_size = new Holder<Integer> ();
            Holder<Integer> extension_data_size = new Holder<Integer> ();
            Holder<Boolean> device_pin_support = new Holder<Boolean> ();
            Holder<Boolean> biometric_support = new Holder<Boolean> ();
            Holder<String> connection_port = new Holder<String> ();
            getSKSWS ().getDeviceInfo (device_id,
                                       api_level,
                                       device_type,
                                       update_url,
                                       vendor_name,
                                       vendor_description,
                                       certificate_path,
                                       supported_algorithms,
                                       crypto_data_size,
                                       extension_data_size,
                                       device_pin_support,
                                       biometric_support,
                                       connection_port);
            DeviceInfo device_info = new DeviceInfo (api_level.value,
                                                     device_type.value,
                                                     update_url.value,
                                                     vendor_name.value,
                                                     vendor_description.value,
                                                     getCertArrayFromBlobs (certificate_path.value),
                                                     supported_algorithms.value.toArray (new String[0]),
                                                     crypto_data_size.value,
                                                     extension_data_size.value,
                                                     device_pin_support.value,
                                                     biometric_support.value);
            device_info.setConnectionPort (connection_port.value);
            return device_info;
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
        catch (IOException e)
          {
            throw new SKSException (e);
          }
      }

    @Override
    public ProvisioningSession createProvisioningSession (String algorithm,
                                                          boolean privacy_enabled,
                                                          String server_session_id,
                                                          ECPublicKey server_ephemeral_key,
                                                          String issuer_uri,
                                                          PublicKey key_management_key,
                                                          int client_time,
                                                          int session_life_time,
                                                          short session_key_limit) throws SKSException
      {
        try
          {
            Holder<String> client_session_id = new Holder<String> ();
            Holder<byte[]>client_ephemeral_key = new Holder<byte[]> ();
            Holder<byte[]>attestation = new Holder<byte[]> ();
            int provisioning_handle = getSKSWS ().createProvisioningSession (device_id,
                                                                             algorithm,
                                                                             privacy_enabled,
                                                                             server_session_id,
                                                                             server_ephemeral_key.getEncoded (),
                                                                             issuer_uri,
                                                                             key_management_key == null ? null : key_management_key.getEncoded (),
                                                                             client_time,
                                                                             session_life_time,
                                                                             session_key_limit,
                                                                             client_session_id,
                                                                             client_ephemeral_key,
                                                                             attestation);
            return new ProvisioningSession (provisioning_handle, 
                                            client_session_id.value,
                                            attestation.value,
                                            getECPublicKey (client_ephemeral_key.value));
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
        catch (GeneralSecurityException e)
          {
            throw new SKSException (e);
          }
      }


    @Override
    public byte[] closeProvisioningSession (int provisioning_handle,
                                            byte[] nonce,
                                            byte[] mac) throws SKSException
      {
        try
          {
            return getSKSWS ().closeProvisioningSession (device_id,
                                                         provisioning_handle,
                                                         nonce,
                                                         mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public EnumeratedProvisioningSession enumerateProvisioningSessions (int provisioning_handle, boolean provisioning_state) throws SKSException
      {
        try
          {
            Holder<String> algorithm = new Holder<String> ();
            Holder<Boolean> privacy_enabled = new Holder<Boolean> ();
            Holder<byte[]> key_management_key = new Holder<byte[]> ();
            Holder<Integer> client_time = new Holder<Integer> ();
            Holder<Integer> session_life_time = new Holder<Integer> ();
            Holder<String> server_session_id = new Holder<String> ();
            Holder<String> client_session_id = new Holder<String> ();
            Holder<String> issuer_uri = new Holder<String> ();
            provisioning_handle = getSKSWS ().enumerateProvisioningSessions (device_id,
                                                                             provisioning_handle,
                                                                             provisioning_state,
                                                                             algorithm,
                                                                             privacy_enabled,
                                                                             key_management_key,
                                                                             client_time,
                                                                             session_life_time,
                                                                             server_session_id,
                                                                             client_session_id,
                                                                             issuer_uri);
            return provisioning_handle == EnumeratedProvisioningSession.INIT_ENUMERATION ? 
                       null : new EnumeratedProvisioningSession (provisioning_handle,
                                                                 algorithm.value,
                                                                 privacy_enabled.value,
                                                                 key_management_key.value == null ? null : createPublicKeyFromBlob (key_management_key.value),
                                                                 client_time.value,
                                                                 session_life_time.value,
                                                                 server_session_id.value,
                                                                 client_session_id.value,
                                                                 issuer_uri.value);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
        catch (GeneralSecurityException e)
          {
            throw new SKSException (e);
          }
      }

    @Override
    public void abortProvisioningSession (int provisioning_handle) throws SKSException
      {
        try
          {
            getSKSWS ().abortProvisioningSession (device_id,
                                                  provisioning_handle);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public byte[] signProvisioningSessionData (int provisioning_handle,
                                               byte[] data) throws SKSException
      {
        try
          {
            return getSKSWS ().signProvisioningSessionData (device_id,
                                                            provisioning_handle,
                                                            data);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public int createPUKPolicy (int provisioning_handle,
                                String id,
                                byte[] puk_value, 
                                byte format, 
                                short retry_limit, 
                                byte[] mac) throws SKSException
      {
        try
          {
            return getSKSWS().createPUKPolicy (device_id,
                                               provisioning_handle,
                                               id,
                                               puk_value,
                                               format,
                                               retry_limit,
                                               mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public int createPINPolicy (int provisioning_handle, 
                                String id,
                                int puk_policy_handle,
                                boolean user_defined,
                                boolean user_modifiable,
                                byte format,
                                short retry_limit,
                                byte grouping,
                                byte pattern_restrictions,
                                short min_length,
                                short max_length,
                                byte input_method,
                                byte[] mac) throws SKSException
      {
        try
          {
            return getSKSWS().createPINPolicy (device_id,
                                               provisioning_handle,
                                               id,
                                               puk_policy_handle,
                                               user_defined,
                                               user_modifiable,
                                               format,
                                               retry_limit,
                                               grouping,
                                               pattern_restrictions,
                                               min_length,
                                               max_length,
                                               input_method,
                                               mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public KeyData createKeyEntry (int provisioning_handle,
                                   String id,
                                   String algorithm,
                                   byte[] server_seed,
                                   boolean device_pin_protection,
                                   int pin_policy_handle,
                                   byte[] pin_value,
                                   boolean enable_pin_caching,
                                   byte biometric_protection,
                                   byte export_protection,
                                   byte delete_protection,
                                   byte app_usage,
                                   String friendly_name,
                                   String key_algorithm,
                                   byte[] key_parameters,
                                   String[] endorsed_algorithms,
                                   byte[] mac) throws SKSException
      {
        try
          {
            Holder<byte[]> public_key = new Holder<byte[]> ();
            Holder<byte[]> attestation = new Holder<byte[]> ();
            List<String> lalg = new ArrayList<String> ();
            for (String alg : endorsed_algorithms)
              {
                lalg.add (alg);
              }
            int key_handle = getSKSWS ().createKeyEntry (device_id,
                                                         provisioning_handle,
                                                         id,
                                                         algorithm,
                                                         server_seed,
                                                         device_pin_protection,
                                                         pin_policy_handle,
                                                         pin_value,
                                                         enable_pin_caching,
                                                         biometric_protection,
                                                         export_protection,
                                                         delete_protection,
                                                         app_usage,
                                                         friendly_name,
                                                         key_algorithm,
                                                         key_parameters,
                                                         lalg,
                                                         mac,
                                                         public_key,
                                                         attestation);
            return new KeyData (key_handle,
                                createPublicKeyFromBlob (public_key.value),
                                attestation.value);
          }
        catch (GeneralSecurityException e)
          {
            throw new SKSException (e);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public int getKeyHandle (int provisioning_handle,
                             String id) throws SKSException
      {
        try
          {
            return getSKSWS ().getKeyHandle (device_id,
                                             provisioning_handle,
                                             id);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void setCertificatePath (int key_handle,
                                    X509Certificate[] certificate_path,
                                    byte[] mac) throws SKSException
      {
        try
          {
            List<byte[]> lcert_path = new ArrayList<byte[]> ();
            for (X509Certificate cert : certificate_path)
              {
                lcert_path.add (cert.getEncoded ());
              }
            getSKSWS ().setCertificatePath (device_id,
                                            key_handle,
                                            lcert_path,
                                            mac);
          }
        catch (GeneralSecurityException e)
          {
            throw new SKSException (e);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void importSymmetricKey (int key_handle,
                                 byte[] symmetric_key,
                                 byte[] mac) throws SKSException
      {
        try
          {
            getSKSWS ().importSymmetricKey (device_id,
                                         key_handle,
                                         symmetric_key,
                                         mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void addExtension (int key_handle, 
                              String type,
                              byte sub_type,
                              String qualifier,
                              byte[] extension_data,
                              byte[] mac) throws SKSException
      {
        try
          {
            getSKSWS ().addExtension (device_id,
                                      key_handle,
                                      type,
                                      sub_type,
                                      qualifier,
                                      extension_data,
                                      mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void importPrivateKey (int key_handle,
                                   byte[] private_key,
                                   byte[] mac) throws SKSException
      {
        try
          {
            getSKSWS ().importPrivateKey (device_id,
                                          key_handle,
                                          private_key,
                                          mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void postDeleteKey (int provisioning_handle,
                               int target_key_handle,
                               byte[] authorization,
                               byte[] mac) throws SKSException
      {
        try
          {
            getSKSWS ().postDeleteKey (device_id,
                                       provisioning_handle,
                                       target_key_handle,
                                       authorization,
                                       mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void postUnlockKey (int provisioning_handle,
                               int target_key_handle,
                               byte[] authorization,
                               byte[] mac) throws SKSException
      {
        try
          {
            getSKSWS ().postUnlockKey (device_id,
                                       provisioning_handle,
                                       target_key_handle,
                                       authorization,
                                       mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void postUpdateKey (int key_handle,
                               int target_key_handle,
                               byte[] authorization,
                               byte[] mac) throws SKSException
      {
        try
          {
            getSKSWS ().postUpdateKey (device_id,
                                       key_handle,
                                       target_key_handle,
                                       authorization,
                                       mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void postCloneKeyProtection (int key_handle,
                                        int target_key_handle,
                                        byte[] authorization,
                                        byte[] mac) throws SKSException
      {
        try
          {
            getSKSWS ().postCloneKeyProtection (device_id,
                                                key_handle,
                                                target_key_handle,
                                                authorization,
                                                mac);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public EnumeratedKey enumerateKeys (int key_handle) throws SKSException
      {
        try
          {
            Holder<Integer> provisioning_handle = new Holder<Integer> ();
            key_handle = getSKSWS ().enumerateKeys (device_id, key_handle, provisioning_handle);
            return key_handle == EnumeratedKey.INIT_ENUMERATION ? null : new EnumeratedKey (key_handle, provisioning_handle.value);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public KeyAttributes getKeyAttributes (int key_handle) throws SKSException
      {
        try
          {
            Holder<Short> symmetric_key_length = new Holder<Short> ();
            Holder<List<byte[]>> certificate_path = new Holder<List<byte[]>> ();
            Holder<Byte> app_usage = new Holder<Byte> ();
            Holder<String> friendly_name = new Holder<String> ();
            Holder<List<String>> endorsed_algorithms = new Holder<List<String>> ();
            Holder<List<String>> extension_types= new Holder<List<String>> ();
            getSKSWS ().getKeyAttributes (device_id,
                                          key_handle,
                                          symmetric_key_length,
                                          certificate_path,
                                          app_usage,
                                          friendly_name,
                                          endorsed_algorithms,
                                          extension_types);
            return new KeyAttributes (symmetric_key_length.value,
                                      getCertArrayFromBlobs (certificate_path.value),
                                      app_usage.value,
                                      friendly_name.value,
                                      endorsed_algorithms.value.toArray (new String[0]),
                                      extension_types.value.toArray (new String[0]));
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
        catch (IOException e)
          {
            throw new SKSException (e);
          }
      }

    @Override
    public KeyProtectionInfo getKeyProtectionInfo (int key_handle) throws SKSException
      {
        try
          {
            Holder<Byte> protection_status = new Holder<Byte> ();
            Holder<Byte> puk_format = new Holder<Byte> ();
            Holder<Short> puk_retry_limit = new Holder<Short> ();
            Holder<Short> puk_error_count = new Holder<Short> ();
            Holder<Boolean> user_defined = new Holder<Boolean> ();
            Holder<Boolean> user_modifiable = new Holder<Boolean> ();
            Holder<Byte> format = new Holder<Byte> ();
            Holder<Short> retry_limit = new Holder<Short> ();
            Holder<Byte> grouping = new Holder<Byte> ();
            Holder<Byte> pattern_restrictions = new Holder<Byte> ();
            Holder<Short> min_length = new Holder<Short> ();
            Holder<Short> max_length = new Holder<Short> ();
            Holder<Byte> input_method = new Holder<Byte> ();
            Holder<Short> pin_error_count = new Holder<Short> ();
            Holder<Boolean> enable_pin_caching = new Holder<Boolean> ();
            Holder<Byte> biometric_protection = new Holder<Byte> ();
            Holder<Byte> export_protection = new Holder<Byte> ();
            Holder<Byte> delete_protection = new Holder<Byte> ();
            Holder<Byte> key_backup = new Holder<Byte> ();
            getSKSWS ().getKeyProtectionInfo (device_id,
                                              key_handle,
                                              protection_status,
                                              puk_format,
                                              puk_retry_limit,
                                              puk_error_count,
                                              user_defined,
                                              user_modifiable,
                                              format,
                                              retry_limit,
                                              grouping,
                                              pattern_restrictions,
                                              min_length,
                                              max_length,
                                              input_method,
                                              pin_error_count,
                                              enable_pin_caching,
                                              biometric_protection,
                                              export_protection,
                                              delete_protection,
                                              key_backup);
            return new KeyProtectionInfo (protection_status.value,
                                          puk_format.value,
                                          puk_retry_limit.value,
                                          puk_error_count.value,
                                          user_defined.value,
                                          user_modifiable.value,
                                          format.value,
                                          retry_limit.value,
                                          grouping.value,
                                          pattern_restrictions.value,
                                          min_length.value,
                                          max_length.value,
                                          input_method.value,
                                          pin_error_count.value,
                                          enable_pin_caching.value,
                                          biometric_protection.value,
                                          export_protection.value,
                                          delete_protection.value,
                                          key_backup.value);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void updateKeyManagementKey (int provisioning_handle,
                                        PublicKey key_managegent_key,
                                        byte[] authorization) throws SKSException
      {
        try
          {
            getSKSWS ().updateKeyManagementKey (device_id,
                                                provisioning_handle,
                                                key_managegent_key.getEncoded (),
                                                authorization);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public Extension getExtension (int key_handle,
                                   String type) throws SKSException
      {
        try
          {
            Holder<Byte> sub_type = new Holder<Byte> ();
            Holder<String> qualifier = new Holder<String> ();
            Holder<byte[]> extension_data = new Holder<byte[]> ();
            getSKSWS ().getExtension (device_id,
                                      key_handle,
                                      type,
                                      sub_type,
                                      qualifier,
                                      extension_data);
            return new Extension (sub_type.value,
                                  qualifier.value,
                                  extension_data.value);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void setProperty (int key_handle,
                             String type,
                             String name,
                             String value) throws SKSException
      {
        try
          {
            getSKSWS ().setProperty (device_id,
                                     key_handle,
                                     type,
                                     name,
                                     value);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void deleteKey (int key_handle,
                           byte[] authorization) throws SKSException
      {
        try
          {
            getSKSWS ().deleteKey (device_id,
                                   key_handle,
                                   authorization);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public byte[] exportKey (int key_handle,
                             byte[] authorization) throws SKSException
      {
        try
          {
            return getSKSWS ().exportKey (device_id,
                                          key_handle,
                                          authorization);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }    

    @Override
    public void unlockKey (int key_handle,
                           byte[] authorization) throws SKSException
      {
        try
          {
            getSKSWS ().unlockKey (device_id,
                                   key_handle,
                                   authorization);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void changePIN (int key_handle,
                           byte[] authorization,
                           byte[] new_pin) throws SKSException
      {
        try
          {
            getSKSWS ().changePIN (device_id,
                                   key_handle,
                                   authorization,
                                   new_pin);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public void setPIN (int key_handle,
                        byte[] authorization,
                        byte[] new_pin) throws SKSException
      {
        try
          {
            getSKSWS ().setPIN (device_id,
                                key_handle,
                                authorization,
                                new_pin);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public byte[] signHashedData (int key_handle,
                                  String algorithm,
                                  byte[] parameters,
                                  byte[] authorization,
                                  byte[] data) throws SKSException
      {
        boolean tga = false;
        while (true)
          {
            try
              {
                AuthorizationHolder auth = new AuthorizationHolder (authorization);
                tga = getTrustedGUIAuthorization (key_handle, auth, tga);
                return getSKSWS ().signHashedData (device_id,
                                                   key_handle,
                                                   algorithm,
                                                   parameters,
                                                   tga,
                                                   auth.value,
                                                   data);
              }
            catch (SKSException_Exception e)
              {
                if (!tga || (e.getFaultInfo ().getError () != SKSException.ERROR_AUTHORIZATION))
                  {
                    throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
                  }
                authorization = null;
              }
          } 
      }

    @Override
    public byte[] asymmetricKeyDecrypt (int key_handle, 
                                        String algorithm,
                                        byte[] parameters,
                                        byte[] authorization,
                                        byte[] data) throws SKSException
      {
        boolean tga = false;
        while (true)
          {
            try
              {
                AuthorizationHolder auth = new AuthorizationHolder (authorization);
                tga = getTrustedGUIAuthorization (key_handle, auth, tga);
                return getSKSWS ().asymmetricKeyDecrypt (device_id,
                                                         key_handle,
                                                         algorithm,
                                                         parameters,
                                                         tga,
                                                         auth.value,
                                                         data);
              }
            catch (SKSException_Exception e)
              {
                if (!tga || (e.getFaultInfo ().getError () != SKSException.ERROR_AUTHORIZATION))
                  {
                    throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
                  }
                authorization = null;
              }
          } 
      }

    @Override
    public byte[] keyAgreement (int key_handle,
                                String algorithm,
                                byte[] parameters,
                                byte[] authorization,
                                ECPublicKey public_key) throws SKSException
      {
        boolean tga = false;
        while (true)
          {
            try
              {
                AuthorizationHolder auth = new AuthorizationHolder (authorization);
                tga = getTrustedGUIAuthorization (key_handle, auth, tga);
                return getSKSWS ().keyAgreement (device_id,
                                                 key_handle,
                                                 algorithm,
                                                 parameters,
                                                 tga,
                                                 auth.value,
                                                 public_key.getEncoded ());
              }
            catch (SKSException_Exception e)
              {
                if (!tga || (e.getFaultInfo ().getError () != SKSException.ERROR_AUTHORIZATION))
                  {
                    throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
                  }
                authorization = null;
              }
          } 
      }

    @Override
    public byte[] performHMAC (int key_handle,
                               String algorithm,
                               byte[] parameters,
                               byte[] authorization,
                               byte[] data) throws SKSException
      {
        boolean tga = false;
        while (true)
          {
            try
              {
                AuthorizationHolder auth = new AuthorizationHolder (authorization);
                tga = getTrustedGUIAuthorization (key_handle, auth, tga);
                return getSKSWS ().performHMAC (device_id,
                                                key_handle, 
                                                algorithm,
                                                parameters,
                                                tga,
                                                auth.value,
                                                data);
              }
            catch (SKSException_Exception e)
              {
                if (!tga || (e.getFaultInfo ().getError () != SKSException.ERROR_AUTHORIZATION))
                  {
                    throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
                  }
                authorization = null;
              }
          }
      }

    @Override
    public byte[] symmetricKeyEncrypt (int key_handle,
                                       String algorithm,
                                       boolean mode,
                                       byte[] parameters,
                                       byte[] authorization,
                                       byte[] data) throws SKSException
      {
        boolean tga = false;
        while (true)
          {
            try
              {
                AuthorizationHolder auth = new AuthorizationHolder (authorization);
                tga = getTrustedGUIAuthorization (key_handle, auth, tga);
                return getSKSWS ().symmetricKeyEncrypt (device_id,
                                                        key_handle,
                                                        algorithm,
                                                        mode,
                                                        parameters,
                                                        tga,
                                                        auth.value,
                                                        data);
              }
            catch (SKSException_Exception e)
              {
                if (!tga || (e.getFaultInfo ().getError () != SKSException.ERROR_AUTHORIZATION))
                  {
                    throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
                  }
                authorization = null;
              }
          }
      }

    @Override
    public String updateFirmware (byte[] chunk) throws SKSException
      {
        try
          {
             return getSKSWS ().updateFirmware (device_id,
                                                chunk);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }
    
    @Override
    public String[] listDevices () throws SKSException
      {
        try
          {
             return getSKSWS ().listDevices ().toArray (new String[0]);
          }
        catch (SKSException_Exception e)
          {
            throw new SKSException (e.getFaultInfo ().getMessage (), e.getFaultInfo ().getError ());
          }
      }

    @Override
    public String getVersion ()
      {
        return getSKSWS ().getVersion ();
      }

    @Override
    public void logEvent (String event)
      {
        getSKSWS ().logEvent (event);
      }

    @Override
    public boolean setTrustedGUIAuthorizationProvider (TrustedGUIAuthorization tga_provider)
      {
        this.tga_provider = tga_provider;
        return true;
      }

    @Override
    public void setDeviceID (String device_id)
      {
        this.device_id = device_id;
      }

    /**
     * Test method. Use empty argument list for help.
     * 
     * @param args Command line arguments
     * @throws SKSException 
     */
    public static void main (String args[]) throws SKSException
      {
        if (args.length != 1)
          {
            System.out.println ("SKSWSClient port\n if port is set to \"default\" the WSDL value is used\n" +
                                "port may also be set with the JVM -D" + DEFAULT_URL_PROPERTY + "=port");
            System.exit (3);
          }
        SKSWSClient client = args[0].equals ("default") ? new SKSWSClient () : new SKSWSClient (args[0]);
        System.out.println ("Version=" + client.getVersion () + "\nDevice=" + client.getDeviceInfo ().getVendorDescription ());
      }
  }
