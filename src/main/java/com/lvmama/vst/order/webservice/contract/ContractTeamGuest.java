
package com.lvmama.vst.order.webservice.contract;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for contractTeamGuest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractTeamGuest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="idtype" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="idcode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="idfrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="idstartdate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="idenddate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="familyname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="givenname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sex" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="birthday" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="nation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="folk" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mobile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="no" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractTeamGuest", propOrder = {
    "idtype",
    "idcode",
    "idfrom",
    "idstartdate",
    "idenddate",
    "name",
    "familyname",
    "givenname",
    "sex",
    "birthday",
    "nation",
    "folk",
    "mobile",
    "no"
})
public class ContractTeamGuest {

    protected int idtype;
    @XmlElement(required = true)
    protected String idcode;
    protected String idfrom;
    @XmlSchemaType(name = "dateTime")
    protected Date idstartdate;
    @XmlSchemaType(name = "dateTime")
    protected Date idenddate;
    @XmlElement(required = true)
    protected String name;
    protected String familyname;
    protected String givenname;
    @XmlElement(required = true)
    protected String sex;
    @XmlSchemaType(name = "dateTime")
    protected Date birthday;
    protected String nation;
    protected String folk;
    protected String mobile;
    protected int no;

    /**
     * Gets the value of the idtype property.
     * 
     */
    public int getIdtype() {
        return idtype;
    }

    /**
     * Sets the value of the idtype property.
     * 
     */
    public void setIdtype(int value) {
        this.idtype = value;
    }

    /**
     * Gets the value of the idcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdcode() {
        return idcode;
    }

    /**
     * Sets the value of the idcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdcode(String value) {
        this.idcode = value;
    }

    /**
     * Gets the value of the idfrom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdfrom() {
        return idfrom;
    }

    /**
     * Sets the value of the idfrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdfrom(String value) {
        this.idfrom = value;
    }

    /**
     * Gets the value of the idstartdate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Date getIdstartdate() {
        return idstartdate;
    }

    /**
     * Sets the value of the idstartdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setIdstartdate(Date value) {
        this.idstartdate = value;
    }

    /**
     * Gets the value of the idenddate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Date getIdenddate() {
        return idenddate;
    }

    /**
     * Sets the value of the idenddate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setIdenddate(Date value) {
        this.idenddate = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the familyname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFamilyname() {
        return familyname;
    }

    /**
     * Sets the value of the familyname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFamilyname(String value) {
        this.familyname = value;
    }

    /**
     * Gets the value of the givenname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGivenname() {
        return givenname;
    }

    /**
     * Sets the value of the givenname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGivenname(String value) {
        this.givenname = value;
    }

    /**
     * Gets the value of the sex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSex() {
        return sex;
    }

    /**
     * Sets the value of the sex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSex(String value) {
        this.sex = value;
    }

    /**
     * Gets the value of the birthday property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * Sets the value of the birthday property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBirthday(Date value) {
        this.birthday = value;
    }

    /**
     * Gets the value of the nation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNation() {
        return nation;
    }

    /**
     * Sets the value of the nation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNation(String value) {
        this.nation = value;
    }

    /**
     * Gets the value of the folk property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolk() {
        return folk;
    }

    /**
     * Sets the value of the folk property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolk(String value) {
        this.folk = value;
    }

    /**
     * Gets the value of the mobile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * Sets the value of the mobile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMobile(String value) {
        this.mobile = value;
    }

    /**
     * Gets the value of the no property.
     * 
     */
    public int getNo() {
        return no;
    }

    /**
     * Sets the value of the no property.
     * 
     */
    public void setNo(int value) {
        this.no = value;
    }

}
