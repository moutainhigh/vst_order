
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for submitSign complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="submitSign">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="complementSignRequest" type="{http://server.ws.api.contract.goldpalm.com/}complementSignRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "submitSign", propOrder = {
    "token",
    "complementSignRequest"
})
public class SubmitSign {

    protected String token;
    protected ComplementSignRequest complementSignRequest;

    /**
     * Gets the value of the token property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the value of the token property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToken(String value) {
        this.token = value;
    }

    /**
     * Gets the value of the complementSignRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ComplementSignRequest }
     *     
     */
    public ComplementSignRequest getComplementSignRequest() {
        return complementSignRequest;
    }

    /**
     * Sets the value of the complementSignRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplementSignRequest }
     *     
     */
    public void setComplementSignRequest(ComplementSignRequest value) {
        this.complementSignRequest = value;
    }

}
