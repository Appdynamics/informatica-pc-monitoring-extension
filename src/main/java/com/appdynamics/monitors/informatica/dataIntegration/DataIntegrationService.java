package com.appdynamics.monitors.informatica.dataIntegration;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 */
@WebServiceClient(name = "DataIntegrationService", targetNamespace = "http://www.informatica.com/wsh", wsdlLocation = "file:/Users/akshay.srivastava/AppDynamics/InformaticaExtension/DataIntegrationWSDL.xml")
public class DataIntegrationService
        extends Service {

    private final static URL DATAINTEGRATIONSERVICE_WSDL_LOCATION;
    private final static WebServiceException DATAINTEGRATIONSERVICE_EXCEPTION;
    private final static QName DATAINTEGRATIONSERVICE_QNAME = new QName("http://www.informatica.com/wsh", "DataIntegrationService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("file:/Users/akshay.srivastava/AppDynamics/InformaticaExtension/DataIntegrationWSDL.xml");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        DATAINTEGRATIONSERVICE_WSDL_LOCATION = url;
        DATAINTEGRATIONSERVICE_EXCEPTION = e;
    }

    public DataIntegrationService() {
        super(__getWsdlLocation(), DATAINTEGRATIONSERVICE_QNAME);
    }

    public DataIntegrationService(WebServiceFeature... features) {
        super(__getWsdlLocation(), DATAINTEGRATIONSERVICE_QNAME, features);
    }

    public DataIntegrationService(URL wsdlLocation) {
        super(wsdlLocation, DATAINTEGRATIONSERVICE_QNAME);
    }

    public DataIntegrationService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, DATAINTEGRATIONSERVICE_QNAME, features);
    }

    public DataIntegrationService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public DataIntegrationService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    private static URL __getWsdlLocation() {
        if (DATAINTEGRATIONSERVICE_EXCEPTION != null) {
            throw DATAINTEGRATIONSERVICE_EXCEPTION;
        }
        return DATAINTEGRATIONSERVICE_WSDL_LOCATION;
    }

    /**
     * @return returns DataIntegrationInterface
     */
    @WebEndpoint(name = "DataIntegration")
    public DataIntegrationInterface getDataIntegration() {
        return super.getPort(new QName("http://www.informatica.com/wsh", "DataIntegration"), DataIntegrationInterface.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns DataIntegrationInterface
     */
    @WebEndpoint(name = "DataIntegration")
    public DataIntegrationInterface getDataIntegration(WebServiceFeature... features) {
        return super.getPort(new QName("http://www.informatica.com/wsh", "DataIntegration"), DataIntegrationInterface.class, features);
    }

}