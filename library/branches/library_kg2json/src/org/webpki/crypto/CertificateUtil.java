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
package org.webpki.crypto;

import java.io.IOException;
import java.io.ByteArrayInputStream;

import java.util.Vector;
import java.util.List;
import java.util.HashSet;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;

import java.security.GeneralSecurityException;

import javax.security.auth.x500.X500Principal;

import org.webpki.util.ArrayUtil;
import org.webpki.util.DebugFormatter;

import org.webpki.asn1.DerDecoder;
import org.webpki.asn1.ASN1Sequence;
import org.webpki.asn1.ASN1String;
import org.webpki.asn1.CompositeContextSpecific;
import org.webpki.asn1.ParseUtil;

import org.webpki.asn1.cert.SubjectAltNameTypes;


public class CertificateUtil
  {

    private CertificateUtil () {}  // No instantiation please


    public static final String AIA_CA_ISSUERS        = "1.3.6.1.5.5.7.48.2";
    public static final String AIA_OCSP_RESPONDER    = "1.3.6.1.5.5.7.48.1";

    private static ASN1Sequence getExtension (X509Certificate certificate, CertificateExtensions extension) throws IOException
      {
        byte[] extension_bytes = certificate.getExtensionValue (extension.getOID ());
        if (extension_bytes == null)
          {
            return null;
          }
        return ParseUtil.sequence (DerDecoder.decode (ParseUtil.octet (DerDecoder.decode (extension_bytes))));
      }


    public static X509Certificate getCertificateFromBlob (byte[] encoded) throws IOException
      {
        try
          {
            CertificateFactory cf = CertificateFactory.getInstance ("X.509");
            return (X509Certificate)cf.generateCertificate (new ByteArrayInputStream (encoded));
          }
        catch (GeneralSecurityException gse)
          {
            throw new IOException (gse);
          }
      }


    public static X509Certificate[] getSortedPathFromPKCS7Bag (byte[] bag) throws IOException
      {
        Vector<byte[]> certs = new Vector<byte[]> ();
      
        ASN1Sequence outer = ParseUtil.sequence (DerDecoder.decode (bag));
        for (int i = 0; i < outer.size (); i++)
          {
            if (ParseUtil.isCompositeContext (outer.get (i), 0))
              {
                ASN1Sequence inner = ParseUtil.sequence (ParseUtil.singleContext (outer.get (i), 0));
                for (int j = 0; j < inner.size (); j++)
                  {
                    if (ParseUtil.isCompositeContext (inner.get (j), 0))
                      {
                        CompositeContextSpecific cert_entries = ParseUtil.compositeContext (inner.get (j));
                        for (int k = 0; k < cert_entries.size (); k++)
                          {
                            certs.add (cert_entries.get (k).encode ());
                          }
                        return getSortedPathFromBlobs (certs);
                      }
                  }
              }
          }
        throw new IOException ("PKCS7 bag error");
      }


    private static CertificateLogotypeDescriptor getLogotypeDescriptor (ASN1Sequence logo) throws IOException
      {
        if (logo.size () == 3 && logo.get(0) instanceof ASN1String &&
            logo.get(1) instanceof ASN1Sequence && logo.get(2) instanceof ASN1Sequence)
          {
            String mime_type = ParseUtil.string (logo.get (0)).value ();
            ASN1Sequence hashes = ParseUtil.sequence (logo.get (1));
            for (int i = 0; i < hashes.size (); i++)
              {
                ASN1Sequence hash_value_pair = ParseUtil.sequence (hashes.get (i));
                String oid = ParseUtil.oid (ParseUtil.sequence (hash_value_pair.get (0)).get (0)).oid ();
                byte[] sha1_data = ParseUtil.octet (hash_value_pair.get (1));
                ASN1Sequence uris = ParseUtil.sequence (logo.get (2));
                for (int j = 0; j < uris.size (); j++)
                  {
                    if (uris.get(j) instanceof ASN1String)
                      {
                        String uri = ParseUtil.string (uris.get (j)).value ();
                        return new CertificateLogotypeDescriptor (uri, mime_type, oid, sha1_data);
                      }
                  }
              }
          }
        return null;
      }


    public static String[] getKeyUsages (X509Certificate certificate) throws IOException
      {
        boolean[] key_usage = certificate.getKeyUsage ();
        if (key_usage == null)
          {
            return null;
          }
        Vector<String> key_usage_set = new Vector<String> ();
        int i = 0;
        for (KeyUsageBits kubit : KeyUsageBits.values ())
          {
            if (i < key_usage.length)
              {
                if (key_usage[i++])
                  {
                    key_usage_set.add (kubit.toString ());
                  }
              }
          }
        return key_usage_set.toArray (new String[0]);
      }


    private static CertificateLogotypeDescriptor[] getLogotypeDescriptors (X509Certificate certificate,
                                                                           int index) throws IOException
      {
        ASN1Sequence outer = getExtension (certificate, CertificateExtensions.LOGOTYPES);
        if (outer == null)
          {
            return null;
          }
        for (int i = 0; i < outer.size (); i++)
          {
            if (ParseUtil.isCompositeContext (outer.get (i), index))
              {
                CompositeContextSpecific logotype_entries = ParseUtil.compositeContext (outer.get (i));
                if (ParseUtil.isCompositeContext (logotype_entries.get (0), 0))
                  {
                    Vector<CertificateLogotypeDescriptor> logotypes = new Vector<CertificateLogotypeDescriptor> ();
                    logotype_entries = ParseUtil.compositeContext (logotype_entries.get (0));
                    for (int j = 0; j < logotype_entries.size (); j++)
                      {
                        if (logotype_entries.get (j) instanceof ASN1Sequence)
                          {
                            outer = ParseUtil.sequence (logotype_entries.get (j));
                            if (outer.size () >= 1)
                              {
                                if (outer.get (0) instanceof ASN1Sequence)
                                  {
                                    outer = ParseUtil.sequence (outer.get (0));
                                    if (outer.size () >= 1)
                                      {
                                        if (outer.get (0) instanceof ASN1Sequence)
                                          {
                                            CertificateLogotypeDescriptor logotype = getLogotypeDescriptor (ParseUtil.sequence (outer.get (0)));
                                            if (logotype != null)
                                              {
                                                logotypes.add (logotype);
                                              }
                                          }
                                      }
                                  }
                              }
                          }
                      }
                    if (!logotypes.isEmpty ())
                      {
                        return logotypes.toArray (new CertificateLogotypeDescriptor[0]);
                      }
                  }
              }
          }
        return null;
      }


    public static CertificateLogotypeDescriptor[] getSubjectLogotypeDescriptors (X509Certificate certificate) throws IOException
      {
        return getLogotypeDescriptors (certificate, 2);
      }


    public static CertificateLogotypeDescriptor[] getIssuerLogotypeDescriptors (X509Certificate certificate) throws IOException
      {
        return getLogotypeDescriptors (certificate, 1);
      }


    public static X509Certificate[] getSortedPath (X509Certificate[] inpath) throws IOException
      {
        try
          {
            // Build/check path
            int n = 0;
            int[] idx = new int[inpath.length];
            int[] jidx = new int[inpath.length];
            boolean[] done = new boolean[inpath.length];
            for (int i = 0; i < inpath.length; i++)
              {
                X500Principal p = inpath[i].getIssuerX500Principal ();
                idx[i] = -1;
                for (int j = 0; j < inpath.length; j++)
                  {
                    if (j == i || done[j]) continue;
                    if (p.equals (inpath[j].getSubjectX500Principal ()))  // J is certifying I
                      {
                        n++;
                        idx[i] = j;
                        jidx[j] = i;
                        done[j] = true;
                        inpath[i].verify (inpath[j].getPublicKey ());
                        break;
                      }
                  }
              }
            if (n != (inpath.length - 1))
              {
                throw new IOException ("X509Certificate elements contain multiple or broken cert paths");
              }

            // Path OK, now sort it
            X509Certificate[] certpath = new X509Certificate[inpath.length];
            for (int i = 0; i < inpath.length; i++)
              {
                if (idx[i] < 0) // Must be the highest
                  {
                    certpath[n] = inpath[i];
                    while (--n >= 0)
                      {
                        certpath[n] = inpath[i = jidx[i]];
                      }
                    break;
                  }
              }
            return certpath;
          }
        catch (GeneralSecurityException gse)
          {
            throw new IOException (gse);
          }
      }


    public static X509Certificate[] getSortedPathFromBlobs (List<byte[]> blobvector) throws IOException
      {
        X509Certificate[] inpath = new X509Certificate[blobvector.size()]; 
        for (int i = 0; i < inpath.length; i++)
          {
            inpath[i] = getCertificateFromBlob (blobvector.get(i));
          }
        return getSortedPath (inpath);
      }


    public static String[] getPolicyOIDs (X509Certificate certificate) throws IOException
      {
        ASN1Sequence outer = getExtension (certificate, CertificateExtensions.CERTIFICATE_POLICIES);
        if (outer == null)
          {
            return null;
          }
        String[] oids = new String[outer.size ()];
        for (int q = 0; q < outer.size (); q++)
          {
            oids[q] = ParseUtil.oid (ParseUtil.sequence (outer.get (q)).get (0)).oid ();
          }
        return oids;
      }


    public static String[] getSubjectEmailAddresses (X509Certificate certificate) throws IOException
      {
        HashSet<String> email_addresses = new HashSet<String> ();

        Pattern pattern = Pattern.compile ("(^|,)(1\\.2\\.840\\.113549\\.1\\.9\\.1=#)([a-f0-9]+)(,.*|$)");
        Matcher matcher = pattern.matcher (certificate.getSubjectX500Principal ().getName ());
        if (matcher.find ())
          {
            email_addresses.add (getHexASN1String (matcher.group (3)));
          }

        ASN1Sequence outer = getExtension (certificate, CertificateExtensions.SUBJECT_ALT_NAME);
        if (outer != null)
          {
            for (int q = 0; q < outer.size (); q++)
              {
                if (ParseUtil.isSimpleContext (outer.get (q), SubjectAltNameTypes.RFC822_NAME))
                  {
                    email_addresses.add (new String (ParseUtil.simpleContext (outer.get (q), SubjectAltNameTypes.RFC822_NAME).value (), "UTF-8"));
                  }
              }
          }
        return email_addresses.isEmpty () ? null : email_addresses.toArray (new String[0]);
      }


    private static String[] getAIAURIs (X509Certificate certificate, String sub_oid) throws IOException
      {
        ASN1Sequence outer = getExtension (certificate, CertificateExtensions.AUTHORITY_INFO_ACCESS);
        if (outer == null)
          {
            return null;
          }
        Vector<String> uris = new Vector<String> ();
        for (int q = 0; q < outer.size (); q++)
          {
            ASN1Sequence inner = ParseUtil.sequence (ParseUtil.sequence (outer.get (q)));
            if (inner.size () != 2)
              {
                throw new IOException ("AIA extension size error");
              }
            if (ParseUtil.oid (inner.get (0)).oid ().equals (sub_oid))
              {
                if (ParseUtil.isSimpleContext (inner.get (1), 6))
                  {
                    String uri = new String (ParseUtil.simpleContext (inner.get (1), 6).value (), "UTF-8");
                    if (uri.startsWith ("http"))
                      {
                        // Sorry, we don't do LDAP [yet]
                        uris.add (uri);
                      }
                  }
              }
          }
        return uris.isEmpty () ? null : uris.toArray (new String[0]);
      }


    public static String[] getAIAOCSPResponders (X509Certificate certificate) throws IOException
      {
        return getAIAURIs (certificate, AIA_OCSP_RESPONDER);
      }


    public static String[] getAIACAIssuers (X509Certificate certificate) throws IOException
      {
        return getAIAURIs (certificate, AIA_CA_ISSUERS);
      }


    public static String[] getExtendedKeyUsage (X509Certificate certificate) throws IOException
      {
        try
          {
            List<String> eku = certificate.getExtendedKeyUsage ();
            if (eku == null)
              {
                return null;
              }
            return eku.toArray (new String[0]);
          }
        catch (GeneralSecurityException gse)
          {
            throw new IOException (gse);
          }
      }


    private static String getHexASN1String (String ascii_hex) throws IOException
      {
        return  ParseUtil.string (DerDecoder.decode (DebugFormatter.getByteArrayFromHex (ascii_hex))).value ();
      }


    private static String trycut (String olddn, String pattern, String replacement) throws IOException
      {
        int i = olddn.indexOf (pattern);
        if (i >= 0)
          {
            int k = i + pattern.length ();
            int j = k;
            while (j < olddn.length ())
              {
                char c = olddn.charAt (j);
                if (c == ',' || c == ' ')
                  {
                    break;
                  }
                j++;
              }
            try
              {
                return olddn.substring (0, i) + replacement + 
                       getHexASN1String (olddn.substring (k, j)) +
                       olddn.substring (j);
              }
            catch (IOException e)
              {
              }
          }
        return olddn;
      }

    
    public static String convertRFC2253ToLegacy (String dn) throws IOException
      {
        dn = trycut (dn, "1.2.840.113549.1.9.1=#", "E=");
        dn = trycut (dn, "2.5.4.5=#", "SerialNumber=");
        dn = trycut (dn, "2.5.4.4=#", "SurName=");
        dn = trycut (dn, "2.5.4.7=#", "Locality=");
        dn = trycut (dn, "2.5.4.42=#", "GivenName=");
        dn = trycut (dn, "2.5.4.41=#", "Name=");
        StringBuffer s = new StringBuffer ();
        boolean quoted = false;
        int i = 0;
        while (i < dn.length ())
          {
            char c = dn.charAt (i++);
            s.append (c);
            if (c == ',')
              {
                if (!quoted)
                  {
                    s.append (' ');
                  }
              }
            else if (c == '"')
              {
                quoted = !quoted;
              }
          }
        return s.toString ();
      }
    

    public static String convertLegacyToRFC2253 (String dn)
      {
        int i = dn.toLowerCase ().indexOf (" e=");
        if (i < 0) i = dn.toLowerCase ().indexOf (",e=");
        if (i > 0)
          {
            dn = dn.substring (0, ++i) + "EMAILADDRESS" + dn.substring (++i);
          }
        return new X500Principal (dn).getName (X500Principal.RFC2253);
      }


    public static byte[] getCertificateSHA1 (X509Certificate certificate) throws IOException
      {
        try
          {
            return HashAlgorithms.SHA1.digest (certificate.getEncoded ());
          }
        catch (GeneralSecurityException gse)
          {
            throw new IOException (gse);
          }
      }


    public static byte[] getCertificateSHA256 (X509Certificate certificate) throws IOException
      {
        try
          {
            return HashAlgorithms.SHA256.digest (certificate.getEncoded ());
          }
        catch (GeneralSecurityException gse)
          {
            throw new IOException (gse);
          }
      }


    public static boolean isTrustAnchor (X509Certificate certificate) throws IOException
      {
        boolean trust_anchor = certificate.getSubjectX500Principal ().equals (certificate.getIssuerX500Principal ()) && certificate.getBasicConstraints () >= 0;
        if (trust_anchor)
          {
            try
              {
                certificate.verify (certificate.getPublicKey ());
              }
            catch (Exception e)
              {
                throw new IOException (e);
              }
            return true;
          }
        return false;
      }

    public static void main (String[] args)
      {
        if (args.length == 0)
          {
            System.out.println ("\nCheck path for:\n   {certificate-in-der-format}...");
          }
        else
          {
            try
              {
                Vector<byte[]> cert_path = new Vector<byte[]> ();
                for (String file : args)
                  {
                    cert_path.add (ArrayUtil.readFile (file));
                  }
                for (X509Certificate cert : getSortedPathFromBlobs (cert_path))
                  {
                    System.out.println ("\nCertificate:\n" + new CertificateInfo (cert));
                  }
              }
            catch (Exception e)
              {
                e.printStackTrace ();
              }
          }
      }
  }
