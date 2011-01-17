
package org.webpki.sks.ws.client;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2-hudson-752-
 * Generated source version: 2.2
 * 
 */
@SuppressWarnings("serial")
@WebFault(name = "SKSException", targetNamespace = "http://xmlns.webpki.org/sks/v0.61")
public class SKSException_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private SKSExceptionBean faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     * @throws IOException 
     */
    public SKSException_Exception(String message, SKSExceptionBean faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
     }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     * @throws IOException 
     */
    public SKSException_Exception(String message, SKSExceptionBean faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: org.webpki.sks.ws.common.SKSException
     */
    public SKSExceptionBean getFaultInfo() {
        return faultInfo;
    }

}
