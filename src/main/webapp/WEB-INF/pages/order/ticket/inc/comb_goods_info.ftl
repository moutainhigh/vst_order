<input type="hidden" name="productMap[${ticketCombProductVO.product.productId}].productId" value="${ticketCombProductVO.product.productId}" autocomplete="off"/>
<table width="100%">
	<tbody>
    	<tr>
    		<#--<#list ticketCombProductVO.suppGoodsList as suppGoods>
			<input type="hidden" name="productMap[${ticketCombProductVO.product.productId}].itemList[${suppGoods_index}].goodsId" value="${suppGoods.suppGoodsId}" autocomplete="off"/>
       		</#list>-->
        	<td width="35%">
        	<a href="javascript:void(0);" class="J_tip" tip-content=""  onmouseover="goodsDetailMouseover(${ticketCombProductVO.product.productId},this);">${ticketCombProductVO.product.productName}
        		<#list ticketCombProductVO.suppGoodsList as suppGoods>
            		<#if suppGoods.goodsType=="EXPRESSTYPE_DISPLAY">(实体票 )<#break/></#if>
            	</#list>
        	</a>
        		<div style="display: none;">
			    	<#list ticketCombProductVO.suppGoodsList as suppGoods>
			    		${suppGoods.goodsName}<br/>
			    		<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc??||suppGoods.suppGoodsExp??>
			    		<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.priceIncludes??>
				        	费用包含:
				           	 ${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.priceIncludes}<br/>
				        </#if>
				        <#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.changeTime?? || 
				        	suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.changeAddress?? ||
				        	suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.enterStyle?? ||
				        	suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.limitFlag??||
				        	suppGoods.suppGoodsExp.days??>
				        	入园须知<br/>
				        	<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.changeTime??>
				            ·取票时间：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.changeTime}<br/>
				            </#if>
				            <#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.changeAddress??>
				            ·取票地点：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.changeAddress}<br/>
				            </#if>
				            <#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.enterStyle??>
				            ·入园方式：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.enterStyle}<br/>
				            </#if>
				             <#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.limitFlag??>
				            ·入园限制：<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.limitFlag=="1">无限制<#else>请在入园当天的${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.limitTimeStr}分以前入园</#if><br/>
				            </#if>
				            <#if suppGoods.suppGoodsExp.days??>
				            ·有效期：指定游玩日${suppGoods.suppGoodsExp.days}天内有效<br/>
				            </#if>
				        </#if>
				        <#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.height?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.age?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.region?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.maxQuantity?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.express?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.entityTicket??||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.others??>
				        	重要提示 <br/>
				        	<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.height?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.age?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.region?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.maxQuantity?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.express?? ||
				        		suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.entityTicket??>
				            	·票种说明：<br/>
				            			<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.height??>·身高：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.height}<br/></#if>
			        					<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.age??>·年龄：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.age}<br/></#if>
			        					<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.region??>·地域：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.region}<br/></#if>
			        					<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.maxQuantity??>·预订数量：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.maxQuantity}<br/></#if>
			        					<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.express??>·快递：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.express}<br/></#if>
			        					<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.entityTicket??>·实体票：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.entityTicket}<br/></#if>
			            		</#if>
			            		<#if suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.others??>
			            		·其他：${suppGoods.suppGoodsTicketDetailVO.suppGoodsDesc.others}<br/>
			            		</#if>
				        </#if>
				        </#if>
				        	退改说明:<br/>
				            	 ${suppGoods.suppGoodsTicketDetailVO.getRefundStr(suppGoods.aperiodicFlag)}<br/>
			        </#list>
			    </div>
        	</td>
            <#if aperiodPackageFlag??&&aperiodPackageFlag=="Y" >
                <td width="20%"><p>游玩时间：${combVisitTime!}</p><p>最晚订单时间：${ticketCombProductVO.firstSaleTimePrice.endDate?string('yyyy-MM-dd')}</p></td>
            <#else >
                <td width="20%"><p>游玩时间：${ticketCombProductVO.firstSaleTimePrice.specDate?string('yyyy-MM-dd')}</p><p>最晚预订时间：<#if ticketCombProductVO.firstSaleTimePrice??>${ticketCombProductVO.firstSaleTimePrice.aheadBookDate}</#if></p></td>
            </#if>
            <td width="15%" class="orange">单价：￥${ticketCombProductVO.getTotalPriceToYuan()}</td>
            <td width="10%">
            	<select class="hotel_sum" name="productMap[${ticketCombProductVO.product.productId}].quantity" productId="${ticketCombProductVO.product.productId}"  
            		adult="${ticketCombProductVO.getAdultNumber()}" child="${ticketCombProductVO.getChildNumber()}" minQuantity="${ticketCombProductVO.getMinQuantity()}"  
            		maxQuantity="${ticketCombProductVO.getMaxQuantity()}" mainItem="true" goodsType=
	                 <#list ticketCombProductVO.suppGoodsList as suppGoods> 
		            	<#if suppGoods.goodsType=='EXPRESSTYPE_DISPLAY'> 
		            		"${suppGoods.goodsType}"
		            	<#break>
		            	</#if>
	            	</#list>
            		>
                	<#list ticketCombProductVO.getMinQuantity()..ticketCombProductVO.getMaxQuantity() as num>
						<option value="${num}" <#if num==quantity>selected="selected"</#if>>${num}</option>
					</#list>
                </select>
            </td>
            <td width="10%" class="orange" id="${ticketCombProductVO.product.productId}Td">总价：￥--</td>
            <td></td>
        </tr>
        </tr>
    </tbody>
</table>