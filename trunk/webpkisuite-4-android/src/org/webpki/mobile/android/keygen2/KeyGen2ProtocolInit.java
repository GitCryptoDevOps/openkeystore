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
package org.webpki.mobile.android.keygen2;

import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

import android.widget.Button;
import android.widget.TextView;

import android.view.View;

import org.webpki.mobile.android.R;

import org.webpki.android.keygen2.CredentialDiscoveryRequestDecoder;
import org.webpki.android.keygen2.KeyCreationRequestDecoder;
import org.webpki.android.keygen2.PlatformNegotiationRequestDecoder;
import org.webpki.android.keygen2.ProvisioningFinalizationRequestDecoder;
import org.webpki.android.keygen2.ProvisioningInitializationRequestDecoder;

public class KeyGen2ProtocolInit extends AsyncTask<Void, String, Boolean>
  {
    private KeyGen2Activity keygen2_activity;

    public KeyGen2ProtocolInit (KeyGen2Activity keygen2_activity)
      {
        this.keygen2_activity = keygen2_activity;
      }

    @Override
    protected Boolean doInBackground (Void... params)
      {
        try
          {
            keygen2_activity.getProtocolInvocationData ();
            keygen2_activity.addSchema (PlatformNegotiationRequestDecoder.class);
            keygen2_activity.addSchema (ProvisioningInitializationRequestDecoder.class);
            keygen2_activity.addSchema (KeyCreationRequestDecoder.class);
            keygen2_activity.addSchema (CredentialDiscoveryRequestDecoder.class);
            keygen2_activity.addSchema (ProvisioningFinalizationRequestDecoder.class);
            keygen2_activity.platform_request = (PlatformNegotiationRequestDecoder) keygen2_activity.parseXML (keygen2_activity.initial_request_data);
            keygen2_activity.setAbortURL (keygen2_activity.platform_request.getAbortURL ());
            return true;
          }
        catch (Exception e)
          {
            keygen2_activity.logException (e);
          }
        return false;
      }

    @Override
    protected void onPostExecute (Boolean success)
      {
        if (keygen2_activity.userHasAborted ())
          {
            return;
          }
        keygen2_activity.noMoreWorkToDo ();
        if (success)
          {
            try
              {
                ((TextView) keygen2_activity.findViewById (R.id.partyInfo)).setText (new URL (keygen2_activity.getInitializationURL ()).getHost ());
              }
            catch (MalformedURLException e)
              {
              }
            keygen2_activity.findViewById (R.id.primaryWindow).setVisibility (View.VISIBLE);
            final Button ok = (Button) keygen2_activity.findViewById (R.id.OKbutton);
            final Button cancel = (Button) keygen2_activity.findViewById (R.id.cancelButton);
            ok.requestFocus ();
            ok.setOnClickListener (new View.OnClickListener ()
              {
                @Override
                public void onClick (View v)
                  {
                    keygen2_activity.findViewById (R.id.primaryWindow).setVisibility (View.INVISIBLE);
                    keygen2_activity.logOK ("The user hit OK");
                    new KeyGen2SessionCreation (keygen2_activity).execute ();
                  }
              });
            cancel.setOnClickListener (new View.OnClickListener ()
              {
                @Override
                public void onClick (View v)
                  {
                    keygen2_activity.conditionalAbort (null);
                  }
              });
          }
        else
          {
            keygen2_activity.showFailLog ();
          }
      }
  }