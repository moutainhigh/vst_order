
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cancelContract complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cancelContract">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cancelContractRequest" type="{http://server.ws.api.contract.goldpalm.com/}cancelContractRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cancelContract", propOrder = {
    "token",
    "cancelContractRequest"
})
public class CancelContract {

    protected String token;
    protected CancelContractRequest cancelContractRequest;

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
     * Gets the value of the cancelContractRequest property.
     * 
     * @return
     *     possible object is
     *     {@link CancelContractRequest }
     *     
     */
    public CancelContractRequest getCancelContractRequest() {
        return cancelContractRequest;
    }

    /**
     * Sets the value of the cancelContractRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancelContractRequest }
     *     
     */
    public void setCancelContractRequest(CancelContractRequest value) {
        this.cancelContractRequest = value;
    }

}
