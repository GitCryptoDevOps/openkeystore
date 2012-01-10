/*
 *  Copyright 2006-2012 WebPKI.org (http://webpki.org).
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
package org.webpki.securityproxy;

import java.io.Serializable;

/**
 * Security proxy object containing a serialized HTTP response.
 * Internal use only.  Data is tunneled through the proxy out to the requester.
 */
class ResponseObject extends ClientObject implements Serializable
  {
    private static final long serialVersionUID = 1L;
    
    ProxyResponseWrapper response_data;

    ////////////////////////////////////////////////////////
    // Due to the multi-channel proxy, calls need IDs
    ////////////////////////////////////////////////////////
    long caller_id;

    ResponseObject (ProxyResponseWrapper response_data, long caller_id, String client_id)
      {
        super (client_id);
        this.caller_id = caller_id;
        this.response_data = response_data;
      }

  }
