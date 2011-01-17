
package org.webpki.sks.ws.common;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="keyHandle" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="x509Certificate" type="{http://www.w3.org/2001/XMLSchema}base64Binary" maxOccurs="unbounded"/>
 *         &lt;element name="mac" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"key_handle", "x509_certificate", "mac"})
@XmlRootElement(name = "setCertificatePath")
public class SetCertificatePath 
{
    @XmlElement(name="KeyHandle", required = true)
    public int key_handle;
    @XmlElement(name="X509Certificate", required = true)
    public List<byte[]> x509_certificate;
    @XmlElement(name="MAC", required = true)
    public byte[] mac;
}
