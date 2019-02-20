
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractJSON complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractJSON">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="traveler" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="groupcorp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="supplier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="line" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pay" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="insurance" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="group" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="goldenweek" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="controversy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="other" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractJSON", propOrder = {
    "traveler",
    "groupcorp",
    "supplier",
    "line",
    "pay",
    "insurance",
    "group",
    "goldenweek",
    "controversy",
    "other"
})
public class ContractJSON {

    protected String traveler;
    protected String groupcorp;
    protected String supplier;
    protected String line;
    protected String pay;
    protected String insurance;
    protected String group;
    protected String goldenweek;
    protected String controversy;
    protected String other;

    /**
     * Gets the value of the traveler property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTraveler() {
        return traveler;
    }

    /**
     * Sets the value of the traveler property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTraveler(String value) {
        this.traveler = value;
    }

    /**
     * Gets the value of the groupcorp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupcorp() {
        return groupcorp;
    }

    /**
     * Sets the value of the groupcorp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupcorp(String value) {
        this.groupcorp = value;
    }

    /**
     * Gets the value of the supplier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplier() {
        return supplier;
    }

    /**
     * Sets the value of the supplier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplier(String value) {
        this.supplier = value;
    }

    /**
     * Gets the value of the line property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLine() {
        return line;
    }

    /**
     * Sets the value of the line property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLine(String value) {
        this.line = value;
    }

    /**
     * Gets the value of the pay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPay() {
        return pay;
    }

    /**
     * Sets the value of the pay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPay(String value) {
        this.pay = value;
    }

    /**
     * Gets the value of the insurance property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInsurance() {
        return insurance;
    }

    /**
     * Sets the value of the insurance property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInsurance(String value) {
        this.insurance = value;
    }

    /**
     * Gets the value of the group property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the value of the group property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroup(String value) {
        this.group = value;
    }

    /**
     * Gets the value of the goldenweek property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGoldenweek() {
        return goldenweek;
    }

    /**
     * Sets the value of the goldenweek property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGoldenweek(String value) {
        this.goldenweek = value;
    }

    /**
     * Gets the value of the controversy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getControversy() {
        return controversy;
    }

    /**
     * Sets the value of the controversy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setControversy(String value) {
        this.controversy = value;
    }

    /**
     * Gets the value of the other property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOther() {
        return other;
    }

    /**
     * Sets the value of the other property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOther(String value) {
        this.other = value;
    }

}
