package com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo;

import java.util.List;

import com.lvmama.vst.comm.vo.ResultHandle;

public class CouponAmount {
	 //优惠金额金额
	
	private  Long amount ;
	//奖金
	private Long bonus ;
	//抵扣现金框
	private Long maxBonus ;
	//校验优惠信息结果集
	List<ResultHandle> couponResultHandles ;
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public Long getBonus() {
		return bonus;
	}
	public void setBonus(Long bonus) {
		this.bonus = bonus;
	}
	public Long getMaxBonus() {
		return maxBonus;
	}
	public void setMaxBonus(Long maxBonus) {
		this.maxBonus = maxBonus;
	}
	public List<ResultHandle> getCouponResultHandles() {
		return couponResultHandles;
	}
	public void setCouponResultHandles(List<ResultHandle> couponResultHandles) {
		this.couponResultHandles = couponResultHandles;
	}
}
