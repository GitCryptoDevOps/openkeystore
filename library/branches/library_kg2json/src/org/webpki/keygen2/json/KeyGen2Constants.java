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
package org.webpki.keygen2.json;

public interface KeyGen2Constants
  {
    String KEYGEN2_NS                                = "http://xmlns.webpki.org/keygen2/beta/20130813#";

    String KEYGEN2_SCHEMA_FILE                       = "keygen2.xsd";


    // JSON elements

    String ABORT_URL_JSON                            = "AbortURL";

    String ACTION_JSON                               = "Action";

    String ALGORITHMS_JSON                           = "Algorithms";

    String APP_USAGE_JSON                            = "AppUsage";

    String ATTESTATION_JSON                          = "Attestation";

    String AUTHORIZATION_JSON                        = "Authorization";

    String BIOMETRIC_PROTECTION_JSON                 = "BiometricProtection";

    String CERTIFICATE_FINGERPRINT_JSON              = "CertificateFingerprint";

    String CLIENT_SESSION_ID_JSON                    = "ClientSessionID";

    String CLIENT_TIME_JSON                          = "ClientTime";

    String DEFERRED_CERTIFICATION_JSON               = "DeferredCertification";

    String DELETE_PROTECTION_JSON                    = "DeleteProtection";

    String EMAIL_JSON                                = "Email";

    String ENDORSED_ALGORITHMS_JSON                  = "EndorsedAlgorithms";

    String ENABLE_PIN_CACHING_JSON                   = "EnablePINCaching";

    String END_ENTITY_CERTIFICATE_JSON               = "EndEntityCertificate";

    String ERROR_URL_JSON                            = "ErrorURL";

    String EXCLUDED_POLICIES_JSON                    = "ExcludedPolicies";

    String EXPIRES_JSON                              = "Expires";

    String EXPORT_PROTECTION_JSON                    = "ExportProtection";

    String EXTENSIONS_JSON                           = "Extensions";

    String CLIENT_JSONIBUTES_JSON                    = "ClientAttributes";

    String FORMAT_JSON                               = "Format";

    String FRIENDLY_NAME_JSON                        = "FriendlyName";

    String GROUPING_JSON                             = "Grouping";

    String HEIGHT_JSON                               = "Height";

    String ID_JSON                                   = "ID";

    String ISSUER_JSON                               = "Issuer";

    String INPUT_METHOD_JSON                         = "InputMethod";

    String ISSUED_BEFORE_JSON                        = "IssuedBefore";

    String ISSUED_AFTER_JSON                         = "IssuedAfter";

    String KEY_ALGORITHM_JSON                        = "KeyAlgorithm";

    String KEY_PARAMETERS_JSON                       = "KeyParameters";

    String KEY_SIZE_JSON                             = "KeySize";

    String KEY_SIZES_JSON                            = "KeySizes";

    String LANGUAGES_JSON                            = "Languages";

    String LOCKED_JSON                               = "Locked";

    String MAC_JSON                                  = "MAC";

    String MAX_LENGTH_JSON                           = "MaxLength";

    String MIME_TYPE_JSON                            = "MIMEType";

    String MIN_LENGTH_JSON                           = "MinLength";

    String NAME_JSON                                 = "Name";

    String NONCE_JSON                                = "Nonce";

    String PATTERN_RESTRICTIONS_JSON                 = "PatternRestrictions";

    String PRIVACY_ENABLED_JSON                      = "PrivacyEnabled";

    String POLICY_JSON                               = "Policy";

    String RETRY_LIMIT_JSON                          = "RetryLimit";

    String SERIAL_JSON                               = "Serial";

    String SERVER_CERT_FP_JSON                       = "ServerCertificateFingerprint";

    String SERVER_SEED_JSON                          = "ServerSeed";
    
    String SERVER_SESSION_ID_JSON                    = "ServerSessionID";

    String SERVER_TIME_JSON                          = "ServerTime";

    String SESSION_KEY_LIMIT_JSON                    = "SessionKeyLimit";

    String SESSION_LIFE_TIME_JSON                    = "SessionLifeTime";

    String SETTABLE_EXPONENT_JSON                    = "SettableExponent";

    String SUBJECT_JSON                              = "Subject";

    String SUBMIT_URL_JSON                           = "SubmitURL";

    String SUCCESS_URL_JSON                          = "SuccessURL";

    String TRUST_ANCHOR_JSON                         = "TrustAnchor";

    String TYPE_JSON                                 = "Type";

    String USER_MODIFIABLE_JSON                      = "UserModifiable";

    String VALUE_JSON                                = "Value";

    String WIDTH_JSON                                = "Width";

    String WRITABLE_JSON                             = "Writable";

    String CERTIFICATE_PATH_JSON                     = "CertificatePath";

    String CLIENT_ATTRIBUTE_JSON                     = "ClientAttribute";

    String CLIENT_EPHEMERAL_KEY_JSON                 = "ClientEphemeralKey";

    String CLONE_KEY_PROTECTION_JSON                 = "CloneKeyProtection";
    
    String CREDENTIAL_DISCOVERY_REQUEST_JSON         = "CredentialDiscoveryRequest";

    String CREDENTIAL_DISCOVERY_RESPONSE_JSON        = "CredentialDiscoveryResponse";
    
    String DELETE_KEY_JSON                           = "DeleteKey";
    
    String DEVICE_CERTIFICATE_PATH_JSON              = "DeviceCertificatePath";

    String DEVICE_PIN_PROTECTION_JSON                = "DevicePINProtection";

    String EXTENSION_JSON                            = "Extension";

    String ENCRYPTED_EXTENSION_JSON                  = "EncryptedExtension";

    String IMAGE_PREFERENCE_JSON                     = "ImagePreference";

    String KEY_CREATION_REQUEST_JSON                 = "KeyCreationRequest";
    
    String KEY_CREATION_RESPONSE_JSON                = "KeyCreationResponse";

    String KEY_ENTRY_JSON                            = "KeyEntry";       

    String KEY_MANAGEMENT_KEY_JSON                   = "KeyManagementKey";

    String MATCHING_CREDENTIAL_JSON                  = "MatchingCredential";

    String LOGOTYPE_JSON                             = "Logotype";

    String LOOKUP_RESULT_JSON                        = "LookupResult";

    String LOOKUP_SPECIFIER_JSON                     = "LookupSpecifier";

    String PIN_POLICY_JSON                           = "PINPolicy";       

    String PLATFORM_NEGOTIATION_REQUEST_JSON         = "PlatformNegotiationRequest";
    
    String PLATFORM_NEGOTIATION_RESPONSE_JSON        = "PlatformNegotiationResponse";
    
    String PRESET_PIN_JSON                           = "PresetPIN";
    
    String PRIVATE_KEY_JSON                          = "PrivateKey";

    String PROPERTY_BAG_JSON                         = "PropertyBag";       

    String PROPERTY_JSON                             = "Property";       

    String PROVISIONING_INITIALIZATION_REQUEST_JSON  = "ProvisioningInitializationRequest";
    
    String PROVISIONING_INITIALIZATION_RESPONSE_JSON = "ProvisioningInitializationResponse";

    String PROVISIONING_FINALIZATION_REQUEST_JSON    = "ProvisioningFinalizationRequest";
    
    String PROVISIONING_FINALIZATION_RESPONSE_JSON   = "ProvisioningFinalizationResponse";

    String PUBLIC_KEY_JSON                           = "PublicKey";

    String PUK_POLICY_JSON                           = "PUKPolicy";       

    String SERVER_EPHEMERAL_KEY_JSON                 = "ServerEphemeralKey";

    String SYMMETRIC_KEY_JSON                        = "SymmetricKey";

    String SEARCH_FILTER_JSON                        = "SearchFilter";

    String UNLOCK_KEY_JSON                           = "UnlockKey";

    String UPDATE_KEY_MANAGEMENT_KEY_JSON            = "UpdateKeyManagementKey";

    String UPDATE_KEY_JSON                           = "UpdateKey";

    String VIRTUAL_MACHINE_JSON                      = "VirtualMachine";
  }
