
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cancelContractRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cancelContractRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="no" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nomsg" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cancelContractRequest", propOrder = {
    "id",
    "no",
    "nomsg"
})
public class CancelContractRequest {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String no;
    protected String nomsg;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the no property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNo() {
        return no;
    }

    /**
     * Sets the value of the no property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNo(String value) {
        this.no = value;
    }

    /**
     * Gets the value of the nomsg property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomsg() {
        return nomsg;
    }

    /**
     * Sets the value of the nomsg property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomsg(String value) {
        this.nomsg = value;
    }

}
