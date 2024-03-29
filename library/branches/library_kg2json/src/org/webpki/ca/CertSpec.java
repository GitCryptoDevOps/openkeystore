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
package org.webpki.ca;

import java.io.IOException;
import java.util.Vector;
import java.util.Set;
import java.util.EnumSet;

import java.net.InetAddress;

import org.webpki.asn1.ASN1OctetString;
import org.webpki.asn1.BaseASN1Object;
import org.webpki.asn1.ASN1IA5String;

import org.webpki.asn1.cert.RelativeDistinguishedName;
import org.webpki.asn1.cert.DistinguishedName;
import org.webpki.asn1.cert.SubjectAltNameTypes;

import org.webpki.util.ArrayUtil;

import org.webpki.crypto.CertificateUtil;
import org.webpki.crypto.HashAlgorithms;
import org.webpki.crypto.KeyUsageBits;

import org.webpki.util.URLDereferencer;


public class CertSpec
  {

    class NameValue
      {
        int name;
        BaseASN1Object value;

        NameValue (int name, BaseASN1Object value)
          {
            this.name = name;
            this.value = value;
          }
      }

    class Logotype
      {
        int index;
        byte[] hash_value;
        HashAlgorithms hash_alg;
        private byte[] value;
        String mime_type;
        Vector<String> uris = new Vector<String> ();
      }

    boolean end_entity;

    boolean ca_cert;

    boolean ski_extension;

    boolean aki_extension;

    Set<KeyUsageBits> key_usage_set = EnumSet.noneOf (KeyUsageBits.class);

    Vector<String> cert_policy_oids = new Vector<String> ();

    Vector<String[]> aia_locators = new Vector<String[]> ();

    Vector<String> crl_dist_points = new Vector<String> ();

    Vector<Logotype> logotypes = new Vector<Logotype> ();

    private boolean has_given_key_usage;

    private boolean default_key_usage;

    private Vector<RelativeDistinguishedName> subject = new Vector<RelativeDistinguishedName> ();
    
    /**
     * Components for the <code>subjectAltName</code>.
     * The names used in this list should be the integer constants defined in 
     * {@link org.webpki.asn1.cert.SubjectAltNameTypes SubjectAltNameTypes}.
     */
    Vector<NameValue> subjectAltName = new Vector<NameValue> ();
    

    DistinguishedName getSubjectDistinguishedName ()
      {
        return new DistinguishedName (subject.toArray (new RelativeDistinguishedName[0]));
      }


    private void setDefaultKeyUsage (KeyUsageBits[] kubits)
      {
        if (has_given_key_usage)
          {
            return;
          }
        for (KeyUsageBits kubit : kubits)
          {
            setKeyUsageBit (kubit);
          }
        default_key_usage = true;
      }


    public void setKeyUsageBit (KeyUsageBits kubit)
      {
        if (default_key_usage)
          {
            default_key_usage = false;
            key_usage_set = EnumSet.noneOf (KeyUsageBits.class);
          }
        key_usage_set.add (kubit);
        has_given_key_usage = true;
      }


    public void setEndEntityConstraint ()
      {
        ski_extension = true;
        aki_extension = true;
        end_entity = true;
        setDefaultKeyUsage (new KeyUsageBits[]{KeyUsageBits.digitalSignature,
                                               KeyUsageBits.nonRepudiation,
                                               KeyUsageBits.keyAgreement,
                                               KeyUsageBits.dataEncipherment,
                                               KeyUsageBits.keyEncipherment});
      }   


    public void setCACertificateConstraint ()
      {
        ski_extension = true;
        aki_extension = true;
        ca_cert = true;
        setDefaultKeyUsage (new KeyUsageBits[]{KeyUsageBits.keyCertSign,
                                               KeyUsageBits.cRLSign});
      }

    
    public void setSubjectKeyIdentifier ()
      {
        ski_extension = true;
      }
    

    public void setAuthorityKeyIdentifier ()
      {
        aki_extension = true;
      }
    

    public void addSubjectComponent (String name_or_oid, String value) throws IOException
      {
        subject.add (new RelativeDistinguishedName (name_or_oid, value));
      }


    private void bad (String err) throws IOException
      {
        throw new IOException ("Subject DN error: " + err);
      }

    private void addLogotype (int index, String uri) throws IOException
      {
        URLDereferencer dref = new URLDereferencer (uri);
        for (Logotype logo : logotypes)
          {
            if (logo.index == index && ArrayUtil.compare (logo.value, dref.getData ()))
              {
                logo.uris.add (uri);
                return;
              }
          }
        Logotype logo = new Logotype ();
        logo.hash_value = HashAlgorithms.SHA1.digest (logo.value = dref.getData ());
        logo.hash_alg = HashAlgorithms.SHA1;
        logo.uris.add (uri);
        logo.index = index;
        logo.mime_type = dref.getMimeType ();
        logotypes.add (logo);
      }


    public void setSubject (String subject) throws IOException
      {
        Vector<String> dns = new Vector<String> ();
        boolean quote = false;
        StringBuffer s = new StringBuffer ();
        int q = 0;
        while (q < subject.length ())
          {
            char c = subject.charAt (q++);
            if (c == ',' && !quote)
              {
                String attr = s.toString ().trim ();
                if (attr.length () > 0)
                  {
                    dns.add (attr);
                    s = new StringBuffer ();
                  }
              }
            else if (c == '"')
              {
                quote = !quote;
              }
            else
              {
                s.append (c);
              }
          }
        if (quote)
          {
            throw new IOException ("Bad quotes");
          }
        String attr = s.toString ().trim ();
        if (attr.length () > 0)
          {
            dns.add (attr);
          }
        String[] dn = dns.toArray (new String[0]);

        for (int i = dn.length; --i >= 0 ;)  // Reverse LDAP order
          {
            String nv = dn[i];
            int j = nv.indexOf ('=');
            if (j <= 0) bad ("= missing");
            String n = nv.substring (0, j).trim ().toUpperCase ();
            String v = nv.substring (j + 1).trim ();
            if (n.length () == 0 || v.length () == 0) bad ("zero length items");
                        
            if (n.startsWith ("OID"))
              {
                String t = n.substring (3);
                if (t.length () == 0)
                  {
                    bad ("malformed OID:\n\n  " + n);
                  }

                while(t.length() > 0)
                  {
                    if (t.charAt (0) != '.')
                      {
                        bad ("malformed OID:\n\n  " + n);
                      }
                    j = t.indexOf ('.', 1);
                    if (j == -1)
                      {
                        j = t.length ();
                      }
                                
                    try
                      {
                        Integer.parseInt (t.substring (1, j));
                      }
                    catch (NumberFormatException nfe)
                      {
                        bad ("malformed OID:\n\n  " + n);
                      }
                                
                    t = t.substring (j);
                  }
                n = n.substring (4);
              }
            addSubjectComponent (n, v);
          }
      }


    public void addSubjectAltNameElement (int name, BaseASN1Object value)
      {
        subjectAltName.add (new NameValue (name, value));
      }


    public void addEmailAddress (String address)
      {
        addSubjectAltNameElement (SubjectAltNameTypes.RFC822_NAME, new ASN1IA5String (address));
      }


    public void addDNSName (String name)
      {
        addSubjectAltNameElement (SubjectAltNameTypes.DNS_NAME, new ASN1IA5String (name));
      }

    
    public void addIPAddress (String ip_address) throws IOException
      {
        addSubjectAltNameElement (SubjectAltNameTypes.IP_ADDRESS, new ASN1OctetString (InetAddress.getByName (ip_address).getAddress ()));
      }


    public void addCertificatePolicyOID (String oid)
      {
        cert_policy_oids.add (oid);
      }


    public void addOCSPResponderURI (String uri)
      {
        aia_locators.add (new String[]{CertificateUtil.AIA_OCSP_RESPONDER, uri});
      }


    public void addCAIssuersURI (String uri)
      {
        aia_locators.add (new String[]{CertificateUtil.AIA_CA_ISSUERS, uri});
      }


    public void addIssuerLogotype (String uri) throws IOException
      {
        addLogotype (1, uri);
      }


    public void addSubjectLogotype (String uri) throws IOException
      {
        addLogotype (2, uri);
      }


    public void addCRLDistributionPointURI (String uri)
      {
        crl_dist_points.add (uri);
      }

  }
