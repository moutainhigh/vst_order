package com.lvmama.vst.order.vo;

import java.io.Serializable;

/**
 * Created by alecyan on 2014/10/25.
 * 交通里边的详情
 */
public class TrafficForRouteDetailVO implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//所属公司
    private String belongToCompany;
    //航班号车次
    private String number;
    //开始时间 上车时间
    private String startTime;
    //结束时间
    private String endTime;
    //起飞的机场
    private String startAirport;
    //到达机场
    private String arriveAirport;
    //发车地
    private String fromDistrict;
    //到达地
    private String toDistrict;
    //中转站
    private String changeTrains;
    //运营的总的时间
    private String totalHour;
    //上车点
    private String address;
    //是否中转的标志
    private String changeflag;
    
    //舱位等级 
    private String cabin;
    
    //火车席别
    private String seatType;
    
    private String costTime;
	
	private String stopFlag;

    
    public String getCostTime() {
		return costTime;
	}
    public void setCostTime(String costTime) {
		this.costTime = costTime;
	}

    public String getCabin() {
		return cabin;
	}

	public void setCabin(String cabin) {
		this.cabin = cabin;
	}

	public String getSeatType() {
		return seatType;
	}

	public void setSeatType(String seatType) {
		this.seatType = seatType;
	}


	

    public String getChangeflag() {
        return changeflag;
    }

    public void setChangeflag(String changeflag) {
        this.changeflag = changeflag;
    }

    public String getBelongToCompany() {
        return belongToCompany;
    }

    public void setBelongToCompany(String belongToCompany) {
        this.belongToCompany = belongToCompany;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartAirport() {
        return startAirport;
    }

    public void setStartAirport(String startAirport) {
        this.startAirport = startAirport;
    }

    public String getArriveAirport() {
        return arriveAirport;
    }

    public void setArriveAirport(String arriveAirport) {
        this.arriveAirport = arriveAirport;
    }

    public String getFromDistrict() {
        return fromDistrict;
    }

    public void setFromDistrict(String fromDistrict) {
        this.fromDistrict = fromDistrict;
    }

    public String getToDistrict() {
        return toDistrict;
    }

    public void setToDistrict(String toDistrict) {
        this.toDistrict = toDistrict;
    }

    public String getChangeTrains() {
        return changeTrains;
    }

    public void setChangeTrains(String changeTrains) {
        this.changeTrains = changeTrains;
    }

    public String getTotalHour() {
        return totalHour;
    }

    public void setTotalHour(String totalHour) {
        this.totalHour = totalHour;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(String stopFlag) {
        this.stopFlag = stopFlag;
    }
}
