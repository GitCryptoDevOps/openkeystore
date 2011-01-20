
package org.webpki.sks.ws.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "keyHandle"
})
@XmlRootElement(name = "getKeyProtectionInfo")
public class getKeyProtectionInfo {

  public getKeyProtectionInfo()
    {
      System.out.println ("KPI");
    }
    protected int keyHandle;

    /**
     * Gets the value of the keyHandle property.
     * 
     */
    public int getKeyHandle() {
        return keyHandle;
    }

    /**
     * Sets the value of the keyHandle property.
     * 
     */
    public void setKeyHandle(int value) {
        this.keyHandle = value;
    }

}
