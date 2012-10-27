package org.webpki.mobile.android.proxy.keygen2;

import java.io.IOException;
import java.math.BigInteger;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

import org.webpki.android.keygen2.PlatformNegotiationResponseEncoder;
import org.webpki.android.sks.DeviceInfo;
import org.webpki.mobile.android.proxy.BaseProxyActivity;
import org.webpki.mobile.android.proxy.InterruptedProtocolException;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * This part does the real work
 */
public class KeyGen2ProtocolRunner extends AsyncTask<Void, String, String> 
{
	private KeyGen2Activity keygen2_activity;
	
	public KeyGen2ProtocolRunner (KeyGen2Activity keygen2_activity)
	{
		this.keygen2_activity = keygen2_activity;
	}

	@Override
	protected String doInBackground(Void... params)
	{
		try
		{
			DeviceInfo dev_info = keygen2_activity.sks.getDeviceInfo();
			keygen2_activity.logOK ("Device Cert:\n" + dev_info.getCertificatePath()[0].toString());
        	PlatformNegotiationResponseEncoder platform_response = new PlatformNegotiationResponseEncoder (keygen2_activity.platform_request);
        	keygen2_activity.postXMLData(keygen2_activity.platform_request.getSubmitURL(), platform_response, true);
            keygen2_activity.logOK ("Sent \"PlatformNegotiationResponse\"");
            KeyPairGenerator generator = KeyPairGenerator.getInstance ("EC");
            ECGenParameterSpec eccgen = new ECGenParameterSpec ("secp256r1");
            generator.initialize (eccgen, new SecureRandom ());
            KeyPair kp = generator.generateKeyPair ();
            
            publishProgress (BaseProxyActivity.PROGRESS_KEYGEN);

            int rsa_key_size = 2048;
            BigInteger exponent = RSAKeyGenParameterSpec.F4;
            RSAKeyGenParameterSpec alg_par_spec = new RSAKeyGenParameterSpec (rsa_key_size, exponent);
            SecureRandom secure_random = new SecureRandom ();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance ("RSA");
            kpg.initialize (alg_par_spec, secure_random);
            KeyPair key_pair = kpg.generateKeyPair ();
			return keygen2_activity.getRedirectURL();
		}
		catch (GeneralSecurityException e)
		{
            keygen2_activity.logException (e);
		}
		catch (IOException e)
		{
            keygen2_activity.logException (e);
		}
		catch (InterruptedProtocolException e)
		{
			return keygen2_activity.getRedirectURL();
		}
		return null;
	}

	@Override
	public void onProgressUpdate(String... message)
	{
		keygen2_activity.updateWorkIndicator (message[0]);
	}

	@Override
    protected void onPostExecute(String result)
	{
		keygen2_activity.noMoreWorkToDo ();
		if (result != null)
		{
          	Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(result));
	        keygen2_activity.startActivity(intent);
	        keygen2_activity.finish ();
		}
		else
		{
			keygen2_activity.showFailLog ();
		}
	}
}
