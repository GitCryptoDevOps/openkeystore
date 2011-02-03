
package org.webpki.sks.ws.client;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;



/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2-hudson-752-
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "SKSWS", targetNamespace = "http://xmlns.webpki.org/sks/v0.61")
public class SKSWS
    extends Service
{


    public SKSWS() {
        super(SKSWS.class.getResource ("/META-INF/SKSWS.wsdl"), new QName("http://xmlns.webpki.org/sks/v0.61", "SKSWS"));
    }

    /**
     * 
     * @return
     *     returns SKSWSInterface
     */
    @WebEndpoint(name = "SKSWS.Port")
    public SKSWSProxy getSKSWSPort() {
        return super.getPort(new QName("http://xmlns.webpki.org/sks/v0.61", "SKSWS.Port"), SKSWSProxy.class);
    }
}
