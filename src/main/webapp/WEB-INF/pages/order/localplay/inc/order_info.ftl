<h5 class="hotel_tab_title">订单信息</h5>
 <div class="hotel_tab">
 	<#include "/order/orderRemark.ftl"/>
 	
 	<#if useTimeFlag?? || LocalHotelAddressFlag?? >
          	<#include "/order/localplay/inc/orderItem_play.ftl"/>
    </#if>
 	
 	<#--<#if productFlag??&&productFlag>
	 	<#list ticketCombProductVO.suppGoodsList as suppGoods> 
	    	<#if suppGoods.goodsType=='EXPRESSTYPE_DISPLAY'> 
	    		<#include "/order/express_info.ftl"/>
	    	<#break>
	    	</#if>
		</#list>
 	</#if>-->
 	<div id="expressageInfoDiv">
		            	
	</div>
 	<#--<#if suppGoodsFlag??&&suppGoodsFlag> 
    	<#if suppGoods.goodsType=='EXPRESSTYPE_DISPLAY'> 
    		<#include "/order/express_info.ftl"/>
    	</#if>
    </#if>-->
 	<br/>
 	<div class="hotel_submit" id="totalOrderPriceDiv">
    	<p><b style="font-size:14px;font-weight: bold;">订单总价：</b>产品费用0元+保险0元-优惠券0元=0元</p>
        <a id="orderSubmitA" href="javascript:void(0);" class="btn btn-orange" submitFlag="true">提交并填写订单信息</a>
    </div>
 </div>