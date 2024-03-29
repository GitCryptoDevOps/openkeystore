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
package org.webpki.wasp;

import java.io.IOException;

import java.util.Vector;
import java.util.Date;

import java.security.SecureRandom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.webpki.util.MimeTypedObject;
import org.webpki.util.URLDereferencer;

import org.webpki.wasp.prof.xds.XDSProfileRequestEncoder;

import org.webpki.xml.DOMWriterHelper;
import org.webpki.xml.XMLObjectWrapper;

import org.webpki.xmldsig.XMLSigner;

import org.webpki.crypto.SignerInterface;
import org.webpki.crypto.CertificateFilter;
import org.webpki.crypto.KeyContainerTypes;
import org.webpki.crypto.KeyUsageBits;

import static org.webpki.wasp.WASPConstants.*;

public class SignatureRequestEncoder extends SignatureRequest
  {

    private int next_content_id = 0;

    private String domain_id;

    Vector<SignatureProfileEncoder> signature_profiles = new Vector<SignatureProfileEncoder> ();

    Vector<CertificateFilter> certificate_filters = new Vector<CertificateFilter> ();

    DocumentReferences document_references = new DocumentReferences ();

    DocumentData document_data = new DocumentData ();

    String id;

    String submit_url;

    String cancel_url;

    ClientPlatformFeature client_platform_request;

    Date server_time;

    private String signature_gui_policy;

    private String[] languages;

    boolean copy_data;

    private int expires;

    private String prefix;  // Default: no prefix

    static void writeOptionalStringAttribute (DOMWriterHelper wr, String attribute_name, String value) throws IOException
      {
        if (value != null)
          {
            wr.setStringAttribute (attribute_name, value);
          }
      }

    static void writeCertificateFilter (DOMWriterHelper wr, CertificateFilter cf) throws IOException
      {
        wr.addEmptyElement (CERTIFICATE_FILTER_ELEM);
        if (cf.getSha1 () != null)
          {
            wr.setBinaryAttribute (CF_SHA1_ATTR, cf.getSha1 ());
          }
        writeOptionalStringAttribute (wr, CF_ISSUER_ATTR, cf.getIssuerRegEx ());
        writeOptionalStringAttribute (wr, CF_SUBJECT_ATTR, cf.getSubjectRegEx ());
        writeOptionalStringAttribute (wr, CF_EMAIL_ATTR, cf.getEmailAddress ());
        if (cf.getSerial () != null)
          {
            wr.setBigIntegerAttribute (CF_SERIAL_ATTR, cf.getSerial ());
          }
        writeOptionalStringAttribute (wr, CF_POLICY_ATTR, cf.getPolicy ());
        if (cf.getContainers () != null)
          {
            KeyContainerTypes[] containers = cf.getContainers ();
            String[] scontainers = new String[containers.length];
            for (int q = 0; q < containers.length; q++)
              {
                for (int i = 0; i < KEYCONTAINER2NAME.length; i++)
                  {
                    if (KEYCONTAINER2NAME[i] == containers[q])
                      {
                        scontainers[q] = NAME2KEYCONTAINER[i];
                        break;
                      }
                  }
              }
            wr.setListAttribute (CF_CONTAINERS_ATTR, scontainers);
          }
        if (cf.getKeyUsage () != null)
          {
            StringBuffer coded_key_usage = new StringBuffer ();
            int i = 0;
            for (KeyUsageBits ku : KeyUsageBits.values ())
              {
                if (cf.getKeyUsage ().getRequiredBits ().contains (ku))
                  {
                    i = ku.ordinal ();
                    coded_key_usage.append ('1');
                  }
                else if (cf.getKeyUsage ().getDisAllowedBits ().contains (ku))
                  {
                    i = ku.ordinal ();
                    coded_key_usage.append ('0');
                  }
                else
                  {
                    coded_key_usage.append ('X');
                  }
              }
            wr.setStringAttribute (CF_KEY_USAGE_ATTR, coded_key_usage.toString ().substring (0, i + 1));
          }
        writeOptionalStringAttribute (wr, CF_EXT_KEY_USAGE_ATTR, cf.getExtKeyUsage ());
      }
    // Constructors

    public SignatureRequestEncoder (String domain_id, String submit_url, String cancel_url)
      {
        this.domain_id = domain_id;
        this.submit_url = submit_url;
        this.cancel_url = cancel_url;
      }


    public SignatureRequestEncoder (String domain_id, String submit_url)
      {
        this (domain_id, submit_url, null);
      }


    public SignatureProfileEncoder addSignatureProfile (SignatureProfileEncoder sp)
      {
        signature_profiles.add (sp);
        return sp;
      }


    public CertificateFilter addCertificateFilter (CertificateFilter cf)
      {
        certificate_filters.add (cf);
        return cf;
      }


    public void setID (String id)
      {
        this.id = id;
      }


    public void setServerTime (Date server_time)
      {
        this.server_time = server_time;
      }


    public ClientPlatformFeature createClientPlatformRequest (ClientPlatformFeature cli)
      {
// TODO
        return client_platform_request = cli;
      }


    public void setSignatureGUIPolicy (String signature_gui_policy)
      {
        this.signature_gui_policy = signature_gui_policy;
      }


    public void setLanguages (String[] languages)
      {
        this.languages = languages;
      }


    public void setCopyData ()
      {
        copy_data = true;
      }


    public void setPrefix (String prefix)
      {
        this.prefix = prefix;
      }


    public String getNextContentID ()
      {
        return "cid:d" + String.valueOf (next_content_id++) + "@" + domain_id;
      }


    private RootDocument createDocument (byte[] data, String mime_type)
      {
        int i = TEXT_TYPES.length;
        while (i-- > 0)
          {
            if (mime_type.equals (TEXT_TYPES[i]))
              {
                return new TextDocument (data, getNextContentID (), MARKUP_TYPES[i]);
              }
          }
        return new BinaryDocument (data, getNextContentID ());
      }


    private String addDocument (byte[] data, TargetContainer target, String mime_type, String meta_data) throws IOException
      {
        return addDocument (createDocument (data, mime_type), target, mime_type, meta_data);
      }


    private String addDocument (String data, TargetContainer target, String mime_type, String meta_data) throws IOException
      {
        return addDocument (data.getBytes ("UTF-8"), target, mime_type, meta_data);
      }


    public String addDocument (TargetContainer target, byte[] data, String mime_type, String meta_data) throws IOException
      {
        return addDocument (data, target, mime_type, meta_data);
      }


    public String addDocument (TargetContainer target, String data, String mime_type, String meta_data) throws IOException
      {
        return addDocument (data, target, mime_type, meta_data);
      }


    public String addHTMLDocument (TargetContainer target, String utf8_encoded_html, String meta_data) throws IOException
      {
        return addDocument (utf8_encoded_html, target, "text/html", meta_data);
      }


    public String addXMLDocument (TargetContainer target, String utf8_encoded_xml, String meta_data) throws IOException
      {
        return addDocument (utf8_encoded_xml, target, "text/xml", meta_data);
      }


    public String addTXTDocument (TargetContainer target, String utf8_encoded_text, String meta_data) throws IOException
      {
        return addDocument (utf8_encoded_text, target, "text/plain", meta_data);
      }


    public String addDocumentFromMTO (TargetContainer target, MimeTypedObject mto, String meta_data) throws IOException
      {
        return addDocument (mto.getData (), target, mto.getMimeType (), meta_data);
      }


    public String addDocumentFromURL (TargetContainer target, String url, String meta_data) throws IOException
      {
        return addDocumentFromMTO (target, new URLDereferencer (url), meta_data);
      }


    public String addDocument (RootDocument the_doc, TargetContainer target, String mime_type, String meta_data) throws IOException
      {
        document_data.addDocument (the_doc);
        String content_id = the_doc.getContentID ();
        switch (target)
          {
            case MAIN_DOCUMENT:
              if (document_references.main_document != null)
                {
                  throw new IOException ("MainDocument already defined!");
                }
              document_references.main_document = document_references.addReference (content_id, mime_type, meta_data);
              break;

            case DETAIL_DOCUMENT:
              if (document_references.detail_document != null)
                {
                  throw new IOException ("DetailDocument already defined!");
                }
              document_references.detail_document = document_references.addReference (content_id, mime_type, meta_data);
              break;

            case PROCESSING_DOCUMENT:
              if (document_references.processing_document != null)
                {
                  throw new IOException ("ProcessingDocument already defined!");
                }
              document_references.processing_document = document_references.addReference (content_id, mime_type, meta_data);
              break;

            case EMBEDDED_OBJECT:
              document_references.addEmbeddedObjectReference (content_id, mime_type, meta_data);
              break;

            default:
              throw new IOException ("Bad argument to addDocument");
          }
        return content_id;
      }


    public String addAttachment (RootDocument the_doc, String mime_type, String meta_data,
                                 boolean provider_originated, String description, String file, boolean must_access)
    throws IOException
      {
        document_data.addDocument (the_doc);
        String content_id = the_doc.getContentID ();
        document_references.addAttachmentReference (content_id, mime_type, meta_data, provider_originated, description, file, must_access);
        return content_id;
      }


    public String addAttachment (String data, String mime_type, String meta_data,
                                 boolean provider_originated, String description, String file, boolean must_access)
    throws IOException
      {
        return addAttachment (data.getBytes ("UTF-8"), mime_type, meta_data, 
                              provider_originated, description, file, must_access);
      }


    public String addAttachment (byte[] data, String mime_type, String meta_data,
                                 boolean provider_originated, String description, String file, boolean must_access)
    throws IOException
      {
        return addAttachment (createDocument (data, mime_type), mime_type, meta_data,
                              provider_originated, description, file, must_access);
      }


    // Debug method
    public DocumentSignatures getDocumentSignatures () throws IOException
      {
        return new DocumentSignatures (document_data);
      }


    protected void toXML (DOMWriterHelper wr) throws IOException
      {
        wr.initializeRootObject (prefix);

        //////////////////////////////////////////////////////////////////////////
        // Set top-level attributes
        //////////////////////////////////////////////////////////////////////////
        if (id == null)
          {
            id = "_" + Long.toHexString (new Date().getTime()) + Long.toHexString(new SecureRandom().nextLong());
          }
        wr.setStringAttribute (ID_ATTR, id);

        if (server_time == null)
          {
            server_time = new Date (); 
          }
        wr.setDateTimeAttribute (SERVER_TIME_ATTR, server_time);

        wr.setStringAttribute (SUBMIT_URL_ATTR, submit_url);

        if (cancel_url != null)
          {
            wr.setStringAttribute (ABORT_URL_ATTR, cancel_url);
          }

        if (signature_gui_policy != null)
          {
            wr.setStringAttribute (SIGNATURE_GUI_POLICY_ATTR, signature_gui_policy);
          }

        if (languages != null)
          {
            wr.setListAttribute (LANGUAGES_ATTR, languages);
          }

        if (copy_data)
          {
            wr.setBooleanAttribute (COPY_DATA_ATTR, true);
          }

        if (expires > 0)
          {
            wr.setIntAttribute (EXPIRES_ATTR, expires);
          }

        //////////////////////////////////////////////////////////////////////////
        // Signature request profiles
        //////////////////////////////////////////////////////////////////////////
        wr.addChildElement (SIGNATURE_PROFILES_ELEM);
        if (signature_profiles.isEmpty ())
          {
            signature_profiles.add (new XDSProfileRequestEncoder ());
          }
        for (SignatureProfileEncoder cp : signature_profiles)
          {
            wr.addWrapped ((XMLObjectWrapper) cp);
          }
        wr.getParent ();

        //////////////////////////////////////////////////////////////////////////
        // Certificate filters (optional)
        //////////////////////////////////////////////////////////////////////////
        for (CertificateFilter cf : certificate_filters)
          {
            writeCertificateFilter (wr, cf);
          }

        //////////////////////////////////////////////////////////////////////////
        // Document references
        //////////////////////////////////////////////////////////////////////////
        Element sorter = document_references.write (wr);

        //////////////////////////////////////////////////////////////////////////
        // Document data
        //////////////////////////////////////////////////////////////////////////
        document_data.write (wr, sorter);

        //////////////////////////////////////////////////////////////////////////
        // Optional "client platform request"
        //////////////////////////////////////////////////////////////////////////
        if (client_platform_request != null)
          {
            client_platform_request.write (wr);
          }
      }


    public void signRequest (SignerInterface signer) throws IOException
      {
        XMLSigner ds = new XMLSigner (signer);
        Document doc = getRootDocument ();
        ds.createEnvelopedSignature (doc, id);
      }
  }
