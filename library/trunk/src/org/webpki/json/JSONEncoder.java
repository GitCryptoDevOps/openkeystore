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
import java.io.Serializable;

/**
 * Base class for java classes which are used for creating specific JSON object types.
 */
public abstract class JSONEncoder implements Serializable
  {
    private static final long serialVersionUID = 1L;

    JSONObject root;  // Of written document

    /**
     * INTERNAL USE ONLY.     
     */
    protected JSONEncoder () {}

    /**
     * INTERNAL USE ONLY.    
     */
    protected abstract void writeJSONData (JSONObjectWriter wr) throws IOException;

    /**
     * Emulation of XML namespace     
     */
    public abstract String getContext ();

    /**
     * Optional type indicator for JSON objects belonging to the same <code>@context</code>.
     */
    public String getQualifier ()
      {
        return null;
      }

    /**
     * @return Document in JSON format
     * @throws IOException
     */
    public byte[] serializeJSONDocument (JSONOutputFormats output_format) throws IOException
      {
        JSONObjectWriter wr = new JSONObjectWriter ();
        root = wr.root;
        wr.setString (JSONDecoderCache.CONTEXT_JSON, getContext ());
        if (getQualifier () != null)
          {
            wr.setString (JSONDecoderCache.QUALIFIER_JSON, getQualifier ());
          }
        writeJSONData (wr);
        return wr.serializeJSONObject (output_format);
      }
  }
