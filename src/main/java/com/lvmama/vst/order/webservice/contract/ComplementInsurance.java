
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for complementInsurance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="complementInsurance">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="complementInsuranceRequest" type="{http://server.ws.api.contract.goldpalm.com/}complementInsuranceRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "complementInsurance", propOrder = {
    "token",
    "complementInsuranceRequest"
})
public class ComplementInsurance {

    protected String token;
    protected ComplementInsuranceRequest complementInsuranceRequest;

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
     * Gets the value of the complementInsuranceRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ComplementInsuranceRequest }
     *     
     */
    public ComplementInsuranceRequest getComplementInsuranceRequest() {
        return complementInsuranceRequest;
    }

    /**
     * Sets the value of the complementInsuranceRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComplementInsuranceRequest }
     *     
     */
    public void setComplementInsuranceRequest(ComplementInsuranceRequest value) {
        this.complementInsuranceRequest = value;
    }

}
