
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for submitStatus complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="submitStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="submitStatusRequest" type="{http://server.ws.api.contract.goldpalm.com/}submitStatusRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "submitStatus", propOrder = {
    "token",
    "submitStatusRequest"
})
public class SubmitStatus {

    protected String token;
    protected SubmitStatusRequest submitStatusRequest;

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
     * Gets the value of the submitStatusRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SubmitStatusRequest }
     *     
     */
    public SubmitStatusRequest getSubmitStatusRequest() {
        return submitStatusRequest;
    }

    /**
     * Sets the value of the submitStatusRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubmitStatusRequest }
     *     
     */
    public void setSubmitStatusRequest(SubmitStatusRequest value) {
        this.submitStatusRequest = value;
    }

}
