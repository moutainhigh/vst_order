
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for submitContractRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="submitContractRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="no" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="travelname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="travelmobile" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transactor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="price" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="platsource" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nomsg" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractTeam" type="{http://server.ws.api.contract.goldpalm.com/}contractTeam"/>
 *         &lt;element name="contractJSON" type="{http://server.ws.api.contract.goldpalm.com/}contractJSON"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "submitContractRequest", propOrder = {
    "version",
    "no",
    "travelname",
    "travelmobile",
    "transactor",
    "price",
    "platsource",
    "nomsg",
    "contractTeam",
    "contractJSON"
})
public class SubmitContractRequest {

    @XmlElement(required = true)
    protected String version;
    protected String no;
    @XmlElement(required = true)
    protected String travelname;
    @XmlElement(required = true)
    protected String travelmobile;
    @XmlElement(required = true)
    protected String transactor;
    protected double price;
    protected String platsource;
    protected String nomsg;
    @XmlElement(required = true)
    protected ContractTeam contractTeam;
    @XmlElement(required = true)
    protected ContractJSON contractJSON;

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
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
     * Gets the value of the travelname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTravelname() {
        return travelname;
    }

    /**
     * Sets the value of the travelname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTravelname(String value) {
        this.travelname = value;
    }

    /**
     * Gets the value of the travelmobile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTravelmobile() {
        return travelmobile;
    }

    /**
     * Sets the value of the travelmobile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTravelmobile(String value) {
        this.travelmobile = value;
    }

    /**
     * Gets the value of the transactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactor() {
        return transactor;
    }

    /**
     * Sets the value of the transactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactor(String value) {
        this.transactor = value;
    }

    /**
     * Gets the value of the price property.
     * 
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     */
    public void setPrice(double value) {
        this.price = value;
    }

    /**
     * Gets the value of the platsource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlatsource() {
        return platsource;
    }

    /**
     * Sets the value of the platsource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlatsource(String value) {
        this.platsource = value;
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

    /**
     * Gets the value of the contractTeam property.
     * 
     * @return
     *     possible object is
     *     {@link ContractTeam }
     *     
     */
    public ContractTeam getContractTeam() {
        return contractTeam;
    }

    /**
     * Sets the value of the contractTeam property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContractTeam }
     *     
     */
    public void setContractTeam(ContractTeam value) {
        this.contractTeam = value;
    }

    /**
     * Gets the value of the contractJSON property.
     * 
     * @return
     *     possible object is
     *     {@link ContractJSON }
     *     
     */
    public ContractJSON getContractJSON() {
        return contractJSON;
    }

    /**
     * Sets the value of the contractJSON property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContractJSON }
     *     
     */
    public void setContractJSON(ContractJSON value) {
        this.contractJSON = value;
    }

}
