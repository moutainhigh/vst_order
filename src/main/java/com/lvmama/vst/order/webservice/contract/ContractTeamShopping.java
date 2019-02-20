
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractTeamShopping complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractTeamShopping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="place" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shoppingplace" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="good" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="staytime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="memo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="signature" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractTeamShopping", propOrder = {
    "date",
    "place",
    "shoppingplace",
    "good",
    "staytime",
    "memo",
    "signature"
})
public class ContractTeamShopping {

    @XmlElement(required = true)
    protected String date;
    @XmlElement(required = true)
    protected String place;
    @XmlElement(required = true)
    protected String shoppingplace;
    @XmlElement(required = true)
    protected String good;
    @XmlElement(required = true)
    protected String staytime;
    protected String memo;
    @XmlElement(required = true)
    protected String signature;

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(String value) {
        this.date = value;
    }

    /**
     * Gets the value of the place property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlace() {
        return place;
    }

    /**
     * Sets the value of the place property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlace(String value) {
        this.place = value;
    }

    /**
     * Gets the value of the shoppingplace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShoppingplace() {
        return shoppingplace;
    }

    /**
     * Sets the value of the shoppingplace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShoppingplace(String value) {
        this.shoppingplace = value;
    }

    /**
     * Gets the value of the good property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGood() {
        return good;
    }

    /**
     * Sets the value of the good property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGood(String value) {
        this.good = value;
    }

    /**
     * Gets the value of the staytime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStaytime() {
        return staytime;
    }

    /**
     * Sets the value of the staytime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStaytime(String value) {
        this.staytime = value;
    }

    /**
     * Gets the value of the memo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMemo() {
        return memo;
    }

    /**
     * Sets the value of the memo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMemo(String value) {
        this.memo = value;
    }

    /**
     * Gets the value of the signature property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the value of the signature property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignature(String value) {
        this.signature = value;
    }

}
