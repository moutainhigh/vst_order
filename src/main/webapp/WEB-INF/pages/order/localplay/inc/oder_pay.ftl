<!--新增促销优惠券模块---开始----------------------->
        <div class="order_box">
          <h3 class="order_tit">优惠信息</h3>
		<p class="wxts">温馨提示：若您在下单后对出游时间或出游人数进行变更，将无法享受促销优惠。</p>
		<div class="user_info pt_10 no_bd">
			<dl class="user_dl">
		    	<dt style="display:none">可享促销：</dt>
		    	<div id="promPromotionDiv" class="promPromotionDiv">
		        	<div style="width:20px;height:30px"></div>
		        </div>
		    </dl>
</div>
			<div class="table_t">促销</div>
			<table width="100%" id="promotionTb">
				<tbody>
			    	<tr>
			        	<td width="65%"><a href=""></a></td>
			            <td width="5%"> </td>
			            <td width="5%" class="orange"></td>
			            <td width="5%"></td>
			            <td width="5%" class="orange"></td>
			            <td></td>
			        </tr>
			    </tbody>
			</table>
			<div style="margin:10px"></div>
            <div class="youhui_box">
            	<ul class="youhui_list2">
                	<li>
                        <a class="youhui_tit" href="javascript:;" hidefocus="false">使用优惠券<i class="icon_arrow"></i></a>
                        <div class="youhui_info">
                        	<!--可用优惠券列表-->
                        	<!----用于是否可以单选---->
                        	<input type="hidden" value="${canUseCoupons}" class="couponTypeFlag" />
                        	<!----用于奖金使用数额---->
                        	<input type="hidden" value="0" class="bonusAmountHidden" name="bonusAmountHidden"/>
                        	<!----用于现金使用数额---->
                        	<input type="hidden" value="0" class="cashAmountHidden" name="cashAmountHidden"/>    	
                        	<input type="hidden" value="<#if user && user.userId>${user.userId}</#if>" class="userIdHidden" name="payOderUserId"/> 
                            <input type="hidden" value="<#if productSaleType>${productSaleType}<#else>"PEOPLE"</#if>" class="productSaleType" name="productSaleType"/> 
                            <input type="hidden" value="<#if youhuiQuantity>${youhuiQuantity}<#else>1</#if>" class="youhuiQuantity" name="youhuiQuantity"/>	
                            <input type="hidden" value="<#if youhuiperson>${youhuiperson}<#else>1</#if>" class="youhuiperson" name="youhuiperson"/>	
                            <#if prepaidFalg>
                         	  <input type="hidden" value="<#if prepaidFalg>${prepaidFalg}</#if>" class="prepaidFalg" name="prepaidFalg_pay"/>	
                            </#if>
                            	<table class="youhui_table">
                            	<thead>
                                    <tr>
                                        <td class="counpName">可用优惠券</td>
                                        <td class="counpCode">兑换码</td>
                                        <td class="counpAmt">可优惠金额</td>
                                        <td class="counpExp">有效期</td>
                                    </tr>
                                </thead>
                                </table>
                                 <div class="youhui_table_box">
                                <table class="youhui_table">
                                <tbody class="fixedCouponTbody">
                                	<tr class="no_youhuiquanTr">
                                    	<td colspan="4">
                                        	<!--没有优惠券的时候提示-->
                                            <div class="no_youhuiquan">
                                                <i class="order_icon order_lvhead"></i>很遗憾，您暂无可用优惠券
                                            </div>
                                        </td>
                                    </tr>
                                
                               
                                </tbody>
                                <tbody class="border_t1_dotted freeCouponTbody">
                               
                                </tbody>
                            </table>
                            </div>
                            <!--添加其他优惠券-->
                            <div class="youhui_add"> 
                            	<b>添加其他优惠券</b><input class="input" type="text" yz_input="true" placeholder="输入优惠券兑换号码"><span class="btn btn-small btn-orange js_yhq_yz" id="addCoupon">添加</span>
                            	<span style="width:100px;" class="btn btn-small cancelYouHui">取消使用优惠券</span> 
                            </div>
                            
                            <!--展开后的箭头-->
                            <div class="info_arrow"><span>◆</span><i>◆</i></div>
                        </div>
                    </li>
                </ul>
            </div>
            
        </div>
        <!--新增促销优惠券模块---结束----------------------->