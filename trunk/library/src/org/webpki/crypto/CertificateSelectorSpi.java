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
package org.webpki.crypto;

import java.io.IOException;


public interface CertificateSelectorSpi
  {

    /**
     * Filters PKI certificates.  This method is primarily designed for on-line signature and
     * authentication where they relying party provides a filter scheme such as with TLS.
     */
    CertificateSelection getCertificateSelection (CertificateFilter[] cfs, 
                                                  CertificateFilter.KeyUsage default_key_usage) throws IOException;

  }
