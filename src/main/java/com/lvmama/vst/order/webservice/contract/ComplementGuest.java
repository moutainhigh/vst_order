
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for complementGuest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="complementGuest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="complementGuestRequest" type="{http://server.ws.api.contract.goldpalm.com/}complementGuestRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "complementGuest", propOrder = {
    "token",
    "complementGuestRequest"
})
public class ComplementGuest {

    protected String token;
    protected ComplementGuestRequest complementGuestRequest;

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
     * Gets the value of the complementGuestRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ComplementGuestRequest }
     *     
     */
    public ComplementGuestRequest getComplementGuestRequest() {
        return complementGuestRequest;
    }

    /**
     * Sets the value of the complementGuestRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplementGuestRequest }
     *     
     */
    public void setComplementGuestRequest(ComplementGuestRequest value) {
        this.complementGuestRequest = value;
    }

}
