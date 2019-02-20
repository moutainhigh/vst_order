
package com.lvmama.vst.order.webservice.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contractTeamRoute complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contractTeamRoute">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="day" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="stop" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="departcity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="arrivecity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="arrivestate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arrivenation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="boardtime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="offtime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lineno" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="carriername" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="supplier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="memo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="trip" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="traffic" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dinner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="hotel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contractTeamRoute", propOrder = {
    "day",
    "stop",
    "title",
    "departcity",
    "arrivecity",
    "arrivestate",
    "arrivenation",
    "boardtime",
    "offtime",
    "lineno",
    "carriername",
    "supplier",
    "port",
    "memo",
    "trip",
    "traffic",
    "dinner",
    "hotel",
    "transit"
})
public class ContractTeamRoute {

    protected int day;
    protected int stop;
    protected String title;
    @XmlElement(required = true)
    protected String departcity;
    @XmlElement(required = true)
    protected String arrivecity;
    protected String arrivestate;
    @XmlElement(required = true)
    protected String arrivenation;
    protected String boardtime;
    protected String offtime;
    protected String lineno;
    protected String carriername;
    protected String supplier;
    protected String port;
    protected String memo;
    protected String trip;
    protected String traffic;
    protected String dinner;
    protected String hotel;
    protected String transit;

    /**
     * Gets the value of the day property.
     * 
     */
    public int getDay() {
        return day;
    }

    /**
     * Sets the value of the day property.
     * 
     */
    public void setDay(int value) {
        this.day = value;
    }

    /**
     * Gets the value of the stop property.
     * 
     */
    public int getStop() {
        return stop;
    }

    /**
     * Sets the value of the stop property.
     * 
     */
    public void setStop(int value) {
        this.stop = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the departcity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDepartcity() {
        return departcity;
    }

    /**
     * Sets the value of the departcity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDepartcity(String value) {
        this.departcity = value;
    }

    /**
     * Gets the value of the arrivecity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrivecity() {
        return arrivecity;
    }

    /**
     * Sets the value of the arrivecity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrivecity(String value) {
        this.arrivecity = value;
    }

    /**
     * Gets the value of the arrivestate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrivestate() {
        return arrivestate;
    }

    /**
     * Sets the value of the arrivestate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrivestate(String value) {
        this.arrivestate = value;
    }

    /**
     * Gets the value of the arrivenation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrivenation() {
        return arrivenation;
    }

    /**
     * Sets the value of the arrivenation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrivenation(String value) {
        this.arrivenation = value;
    }

    /**
     * Gets the value of the boardtime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBoardtime() {
        return boardtime;
    }

    /**
     * Sets the value of the boardtime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBoardtime(String value) {
        this.boardtime = value;
    }

    /**
     * Gets the value of the offtime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfftime() {
        return offtime;
    }

    /**
     * Sets the value of the offtime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfftime(String value) {
        this.offtime = value;
    }

    /**
     * Gets the value of the lineno property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineno() {
        return lineno;
    }

    /**
     * Sets the value of the lineno property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineno(String value) {
        this.lineno = value;
    }

    /**
     * Gets the value of the carriername property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCarriername() {
        return carriername;
    }

    /**
     * Sets the value of the carriername property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCarriername(String value) {
        this.carriername = value;
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
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPort(String value) {
        this.port = value;
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
     * Gets the value of the trip property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrip() {
        return trip;
    }

    /**
     * Sets the value of the trip property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrip(String value) {
        this.trip = value;
    }

    /**
     * Gets the value of the traffic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTraffic() {
        return traffic;
    }

    /**
     * Sets the value of the traffic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTraffic(String value) {
        this.traffic = value;
    }

    /**
     * Gets the value of the dinner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDinner() {
        return dinner;
    }

    /**
     * Sets the value of the dinner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDinner(String value) {
        this.dinner = value;
    }

    /**
     * Gets the value of the hotel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHotel() {
        return hotel;
    }

    /**
     * Sets the value of the hotel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHotel(String value) {
        this.hotel = value;
    }

    /**
     * Gets the value of the transit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransit() {
        return transit;
    }

    /**
     * Sets the value of the transit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransit(String value) {
        this.transit = value;
    }

}
