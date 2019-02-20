
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for uploadContract complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="uploadContract">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="submitContractRequest" type="{http://server.ws.api.contract.goldpalm.com/}submitContractRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uploadContract", propOrder = {
    "token",
    "submitContractRequest"
})
public class UploadContract {

    protected String token;
    protected SubmitContractRequest submitContractRequest;

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
     * Gets the value of the submitContractRequest property.
     * 
     * @return
     *     possible object is
     *     {@link SubmitContractRequest }
     *     
     */
    public SubmitContractRequest getSubmitContractRequest() {
        return submitContractRequest;
    }

    /**
     * Sets the value of the submitContractRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubmitContractRequest }
     *     
     */
    public void setSubmitContractRequest(SubmitContractRequest value) {
        this.submitContractRequest = value;
    }

}
