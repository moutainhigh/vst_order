
package com.lvmama.vst.order.webservice.contract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for contractTeam complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractTeam">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="teamcode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="linename" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="days" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="nights" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="bgndate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="enddate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="qty" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="optype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="startcity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="longtraffic" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="localtraffic" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tripmemo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="routes" type="{http://server.ws.api.contract.goldpalm.com/}contractTeamRoute" maxOccurs="unbounded"/>
 *         &lt;element name="guests" type="{http://server.ws.api.contract.goldpalm.com/}contractTeamGuest" maxOccurs="unbounded"/>
 *         &lt;element name="activities" type="{http://server.ws.api.contract.goldpalm.com/}contractTeamActivity" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="shoppings" type="{http://server.ws.api.contract.goldpalm.com/}contractTeamShopping" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractTeam", propOrder = {
    "teamcode",
    "linename",
    "days",
    "nights",
    "bgndate",
    "enddate",
    "qty",
    "optype",
    "startcity",
    "longtraffic",
    "localtraffic",
    "tripmemo",
    "routes",
    "guests",
    "activities",
    "shoppings"
})
public class ContractTeam {

    protected String teamcode;
    @XmlElement(required = true)
    protected String linename;
    protected int days;
    protected Integer nights;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected Date bgndate;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected Date enddate;
    protected int qty;
    protected String optype;
    protected String startcity;
    protected String longtraffic;
    protected String localtraffic;
    protected String tripmemo;
    @XmlElement(required = true)
    protected List<ContractTeamRoute> routes;
	@XmlElement(required = true)
    protected List<ContractTeamGuest> guests;
    protected List<ContractTeamActivity> activities;
    protected List<ContractTeamShopping> shoppings;

    /**
     * Gets the value of the teamcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTeamcode() {
        return teamcode;
    }

    /**
     * Sets the value of the teamcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTeamcode(String value) {
        this.teamcode = value;
    }

    /**
     * Gets the value of the linename property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinename() {
        return linename;
    }

    /**
     * Sets the value of the linename property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinename(String value) {
        this.linename = value;
    }

    /**
     * Gets the value of the days property.
     * 
     */
    public int getDays() {
        return days;
    }

    /**
     * Sets the value of the days property.
     * 
     */
    public void setDays(int value) {
        this.days = value;
    }

    /**
     * Gets the value of the nights property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNights() {
        return nights;
    }

    /**
     * Sets the value of the nights property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNights(Integer value) {
        this.nights = value;
    }

    /**
     * Gets the value of the bgndate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Date getBgndate() {
        return bgndate;
    }

    /**
     * Sets the value of the bgndate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBgndate(Date value) {
        this.bgndate = value;
    }

    /**
     * Gets the value of the enddate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public Date getEnddate() {
        return enddate;
    }

    /**
     * Sets the value of the enddate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEnddate(Date value) {
        this.enddate = value;
    }

    /**
     * Gets the value of the qty property.
     * 
     */
    public int getQty() {
        return qty;
    }

    /**
     * Sets the value of the qty property.
     * 
     */
    public void setQty(int value) {
        this.qty = value;
    }

    /**
     * Gets the value of the optype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOptype() {
        return optype;
    }

    /**
     * Sets the value of the optype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOptype(String value) {
        this.optype = value;
    }

    /**
     * Gets the value of the startcity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartcity() {
        return startcity;
    }

    /**
     * Sets the value of the startcity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartcity(String value) {
        this.startcity = value;
    }

    /**
     * Gets the value of the longtraffic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLongtraffic() {
        return longtraffic;
    }

    /**
     * Sets the value of the longtraffic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLongtraffic(String value) {
        this.longtraffic = value;
    }

    /**
     * Gets the value of the localtraffic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocaltraffic() {
        return localtraffic;
    }

    /**
     * Sets the value of the localtraffic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocaltraffic(String value) {
        this.localtraffic = value;
    }

    /**
     * Gets the value of the tripmemo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTripmemo() {
        return tripmemo;
    }

    /**
     * Sets the value of the tripmemo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTripmemo(String value) {
        this.tripmemo = value;
    }

    /**
     * Gets the value of the routes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the routes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRoutes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContractTeamRoute }
     * 
     * 
     */
    public List<ContractTeamRoute> getRoutes() {
        if (routes == null) {
            routes = new ArrayList<ContractTeamRoute>();
        }
        return this.routes;
    }

    /**
     * Gets the value of the guests property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the guests property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGuests().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContractTeamGuest }
     * 
     * 
     */
    public List<ContractTeamGuest> getGuests() {
        if (guests == null) {
            guests = new ArrayList<ContractTeamGuest>();
        }
        return this.guests;
    }

    /**
     * Gets the value of the activities property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the activities property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActivities().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContractTeamActivity }
     * 
     * 
     */
    public List<ContractTeamActivity> getActivities() {
        if (activities == null) {
            activities = new ArrayList<ContractTeamActivity>();
        }
        return this.activities;
    }

    /**
     * Gets the value of the shoppings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shoppings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getShoppings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContractTeamShopping }
     * 
     * 
     */
    public List<ContractTeamShopping> getShoppings() {
        if (shoppings == null) {
            shoppings = new ArrayList<ContractTeamShopping>();
        }
        return this.shoppings;
    }

    public void setRoutes(List<ContractTeamRoute> routes) {
		this.routes = routes;
	}

	public void setGuests(List<ContractTeamGuest> guests) {
		this.guests = guests;
	}

	public void setActivities(List<ContractTeamActivity> activities) {
		this.activities = activities;
	}

	public void setShoppings(List<ContractTeamShopping> shoppings) {
		this.shoppings = shoppings;
	}
}
