package com.lvmama.vst.order.utils;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.comm.vo.pass.EbkTicketPassVO;
import com.lvmama.vst.comm.vo.pass.EbkTicketPostVO;
import com.lvmama.vst.comm.vo.pass.EbkTicketStatisVO;

/** 
 * @ImplementProject vst_order
 * @Description: TODO 
 * @author zhujingfeng
 * @date 2018年10月11日 下午12:42:10 
 */
public class EbkCurrencyUtils {
	/**
	 * 如果外币字段中没有值，且查询的是人民币的统计信息，则将外币的币值设置成人名币及币种是RMB
	 * @param ebkTicketStatisVOList
	 */
	public static void dealForeignRMBType(String currencyCode,List<EbkTicketStatisVO> ebkTicketStatisVOList){
		if(SuppGoods.CURRENCYTYPE.CNY.name().equalsIgnoreCase(currencyCode) && CollectionUtils.isNotEmpty(ebkTicketStatisVOList)){
			for (EbkTicketStatisVO ebkTicketStatisVO : ebkTicketStatisVOList) {
				if(ebkTicketStatisVO!=null && ebkTicketStatisVO.getForeignSumPrice()==null && ebkTicketStatisVO.getSumPrice()!=null){
					ebkTicketStatisVO.setForeignSumPrice(ebkTicketStatisVO.getSumPrice());
				}
				
				if(ebkTicketStatisVO!=null && ebkTicketStatisVO.getCurrencyCode()==null){
					ebkTicketStatisVO.setCurrencyCode(SuppGoods.CURRENCYTYPE.CNY.name());
				}
				
				if(ebkTicketStatisVO!=null && ebkTicketStatisVO.getCurrencyName()==null){
					ebkTicketStatisVO.setCurrencyName(SuppGoods.CURRENCYTYPE.CNY.getCnName());
				}
			}
		}
	}
	
	/**
	 * 处理外币价格，如果为空的，则默认拿人名币进行赋值
	 * @param ebkTicketPassVOList
	 */
	public static void dealForeignPriceIsNull(List<EbkTicketPassVO> ebkTicketPassVOList){
		if(CollectionUtils.isNotEmpty(ebkTicketPassVOList)){
			for (EbkTicketPassVO ebkTicketPassVO : ebkTicketPassVOList) {
				/**
				 * 如果币种为空的，外币中存人名币的值
				 */
				if(ebkTicketPassVO!=null && StringUtils.isBlank(ebkTicketPassVO.getCurrencyCode())){
					ebkTicketPassVO.setCurrencyCode(SuppGoods.CURRENCYTYPE.CNY.name());
					ebkTicketPassVO.setCurrencyName(SuppGoods.CURRENCYTYPE.CNY.getCnName());
					
					if(ebkTicketPassVO.getActualSettlementPrice()!=null){
						ebkTicketPassVO.setForeignActualSettlementPrice(ebkTicketPassVO.getActualSettlementPrice().longValue());
					}
					
					if(ebkTicketPassVO.getPrice()!=null){
						ebkTicketPassVO.setForeignPrice(ebkTicketPassVO.getPrice().longValue());
					}
					
					if(ebkTicketPassVO.getSalePrice()!=null){
						ebkTicketPassVO.setForeignSalePrice(ebkTicketPassVO.getSalePrice().longValue());
					}
					
					if(ebkTicketPassVO.getTotalPrice()!=null){
						ebkTicketPassVO.setForeignTotalPrice(ebkTicketPassVO.getTotalPrice().longValue());
					}
					
					/**
					 * totalSettlementPrice,settlementPrice,marketPrice 对象中暂无此属性，故暂不设置
					 */
				}
			}
		}
	}
	
	
	/**
	 * 处理外币价格，如果为空的，则默认拿人名币进行赋值
	 * @param ebkTicketPassVOList
	 */
	public static void dealForeignPriceIsNullForPost(List<EbkTicketPostVO> ebkTicketPostVOList){
		if(CollectionUtils.isNotEmpty(ebkTicketPostVOList)){
			for (EbkTicketPostVO ebkTicketPostVO : ebkTicketPostVOList) {
				/**
				 * 如果币种为空的，外币中存人名币的值
				 */
				if(ebkTicketPostVO!=null && StringUtils.isBlank(ebkTicketPostVO.getCurrencyCode())){
					ebkTicketPostVO.setCurrencyCode(SuppGoods.CURRENCYTYPE.CNY.name());
					ebkTicketPostVO.setCurrencyName(SuppGoods.CURRENCYTYPE.CNY.getCnName());
					
					if(ebkTicketPostVO.getActualSettlementPrice()!=null){
						ebkTicketPostVO.setForeignActualSettlementPrice(ebkTicketPostVO.getActualSettlementPrice().longValue());
					}
					
					if(ebkTicketPostVO.getPrice()!=null){
						ebkTicketPostVO.setForeignPrice(ebkTicketPostVO.getPrice().longValue());
					}
					
					if(ebkTicketPostVO.getSalePrice()!=null){
						ebkTicketPostVO.setForeignSalePrice(ebkTicketPostVO.getSalePrice().longValue());
					}
					
					if(ebkTicketPostVO.getTotalPrice()!=null){
						ebkTicketPostVO.setForeignTotalPrice(ebkTicketPostVO.getTotalPrice().longValue());
					}
					
					/**
					 * totalSettlementPrice,settlementPrice,marketPrice 对象中暂无此属性，故暂不设置
					 */
				}
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println(Long.valueOf(Float.toString(0.01F)));
	}
}
