
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for resendMsg complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resendMsg">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="guestname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="guestmobile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resendMsg", propOrder = {
    "token",
    "contractid",
    "guestname",
    "guestmobile"
})
public class ResendMsg {

    protected String token;
    protected String contractid;
    protected String guestname;
    protected String guestmobile;

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
     * Gets the value of the contractid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContractid() {
        return contractid;
    }

    /**
     * Sets the value of the contractid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContractid(String value) {
        this.contractid = value;
    }

    /**
     * Gets the value of the guestname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuestname() {
        return guestname;
    }

    /**
     * Sets the value of the guestname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuestname(String value) {
        this.guestname = value;
    }

    /**
     * Gets the value of the guestmobile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuestmobile() {
        return guestmobile;
    }

    /**
     * Sets the value of the guestmobile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuestmobile(String value) {
        this.guestmobile = value;
    }

}
