<table width="100%">
	<tbody>
    	<tr>
    		<input type="hidden" name="itemMap[${suppGoods.suppGoodsId}].goodsId" value="${suppGoods.suppGoodsId}" autocomplete="off"/>
        	<td width="35%">
        		<a href="javascript:void(0);" class="J_tip" tip-content=""  onmouseover="goodsDetailMouseover(${suppGoods.suppGoodsId},this);">${suppGoods.prodProductBranch.branchName}-${suppGoods.goodsName}<#if suppGoods.goodsType=="EXPRESSTYPE_DISPLAY">(实体票)</#if></a>
        		<div style="display: none;">
        		<#if ticketDetailVO.suppGoodsDesc??||ticketDetailVO.suppGoodsExp??>
			    	<#if ticketDetailVO.suppGoodsDesc??&&ticketDetailVO.suppGoodsDesc.priceIncludes??>
			    	 费用包含:${ticketDetailVO.suppGoodsDesc.priceIncludes}<br/>
			        </#if>
			        <#if ticketDetailVO.suppGoodsDesc??&&
			        	(ticketDetailVO.suppGoodsDesc.changeTime?? || 
			        	ticketDetailVO.suppGoodsDesc.changeAddress?? ||
			        	ticketDetailVO.suppGoodsDesc.limitFlag?? ||
			        	ticketDetailVO.suppGoodsDesc.enterStyle??) ||
			        	ticketDetailVO.suppGoodsExp??&&
			        	(suppGoods.suppGoodsExp.days?? ||
			        	suppGoods.suppGoodsExp.startTime?? ||
			        	suppGoods.suppGoodsExp.endTime??)>
			        	入园须知<br/>
			        	<#if ticketDetailVO.suppGoodsDesc.changeTime??>
			            ·取票时间：${ticketDetailVO.suppGoodsDesc.changeTime}<br/>
			            </#if>
			            <#if ticketDetailVO.suppGoodsDesc.changeAddress??>
			            ·取票地点：${ticketDetailVO.suppGoodsDesc.changeAddress}<br/>
			            </#if>
			            <#if ticketDetailVO.suppGoodsDesc.enterStyle??>
			            ·入园方式：${ticketDetailVO.suppGoodsDesc.enterStyle}<br/>
			            </#if>
		              	<#if ticketDetailVO.suppGoodsDesc.limitFlag??>
			            ·入园限制：<#if ticketDetailVO.suppGoodsDesc.limitFlag=="1">无限制<#else>请在入园当天的${ticketDetailVO.suppGoodsDesc.limitTimeStr}分以前入园</#if><br/>
			            </#if>
			            <#if suppGoodsFlag??&&suppGoodsFlag&&suppGoods.hasAperiodic()>
			            	<#if suppGoods.suppGoodsExp.startTime?? &&
				            			suppGoods.suppGoodsExp.endTime??>
				            ·有效期：${suppGoods.suppGoodsExp.startTime?string('yyyy-MM-dd')} 至 ${suppGoods.suppGoodsExp.endTime?string('yyyy-MM-dd')}有效
				            	<#if suppGoods.suppGoodsExp.unvalid??>
				            	，期票商品不适用日期：${suppGoods.suppGoodsExp.unvalid}
				            	 </#if><br/>
				            </#if>
			            <#else>
			            	<#if suppGoods.suppGoodsExp.days??>
				            ·有效期：指定游玩日${suppGoods.suppGoodsExp.days}天内有效<br/>
				            </#if>
			            </#if>
			        </#if>
			        <#if ticketDetailVO.suppGoodsDesc??&&
			        			(ticketDetailVO.suppGoodsDesc.height?? ||
				        		ticketDetailVO.suppGoodsDesc.age?? ||
				        		ticketDetailVO.suppGoodsDesc.region?? ||
				        		ticketDetailVO.suppGoodsDesc.maxQuantity?? ||
				        		ticketDetailVO.suppGoodsDesc.express?? ||
				        		ticketDetailVO.suppGoodsDesc.entityTicket??||
				        		ticketDetailVO.suppGoodsDesc.others??)>
				        	重要提示<br/>
			        	 <#if ticketDetailVO.suppGoodsDesc.height?? ||
				        		ticketDetailVO.suppGoodsDesc.age?? ||
				        		ticketDetailVO.suppGoodsDesc.region?? ||
				        		ticketDetailVO.suppGoodsDesc.maxQuantity?? ||
				        		ticketDetailVO.suppGoodsDesc.express?? ||
				        		ticketDetailVO.suppGoodsDesc.entityTicket??>
			        		·票种说明： <br>
			        					<#if ticketDetailVO.suppGoodsDesc.height??>·身高：${ticketDetailVO.suppGoodsDesc.height}<br/></#if>
			        					<#if ticketDetailVO.suppGoodsDesc.age??>·年龄：${ticketDetailVO.suppGoodsDesc.age}<br/></#if>
			        					<#if ticketDetailVO.suppGoodsDesc.region??>·地域：${ticketDetailVO.suppGoodsDesc.region}<br></#if>
			        					<#if ticketDetailVO.suppGoodsDesc.maxQuantity??>·预订数量：${ticketDetailVO.suppGoodsDesc.maxQuantity}<br/></#if>
			        					<#if ticketDetailVO.suppGoodsDesc.express??>·快递：${ticketDetailVO.suppGoodsDesc.express}<br/></#if>
			        					<#if ticketDetailVO.suppGoodsDesc.entityTicket??>·实体票：${ticketDetailVO.suppGoodsDesc.entityTicket}<br/></#if>
			            	 </#if>
			            	 <#if ticketDetailVO.suppGoodsDesc.others??>
			            	·其他：${ticketDetailVO.suppGoodsDesc.others}
			            	</#if>
			        </#if>
			        <#if suppGoods.payTarget!="PAY">
			        <p>
			        	退改说明:
			            	 ${ticketDetailVO.getRefundStr(suppGoods.aperiodicFlag)}
			        </p>
			       </#if>
			      </#if>
			      </div>
        	</td>
            <td width="20%">
            <#if suppGoods.hasAperiodic()>
             <p>游玩时间：${suppGoods.suppGoodsExp.startTime?string('yyyy-MM-dd')} 至 ${suppGoods.suppGoodsExp.endTime?string('yyyy-MM-dd')}</p><p>最晚订单时间：${suppGoodsBaseTimePrice.endDate?string('yyyy-MM-dd')}</p>
            <#else>
             <p>游玩时间：${suppGoodsBaseTimePrice.specDate?string('yyyy-MM-dd')}</p><p>最晚预订时间：<#if suppGoodsBaseTimePrice??>${suppGoodsBaseTimePrice.aheadBookDate}</#if></p>
			</#if>
            </td>
            <td width="15%" class="orange">单价：￥${suppGoodsBaseTimePrice.priceYuanStr}</td>
            <td width="10%">
            	<select class="hotel_sum" name="itemMap[${suppGoods.suppGoodsId}].quantity" adult="${suppGoods.adult}" child="${suppGoods.child}" goodsId="${suppGoods.suppGoodsId}" goodsType="${suppGoods.goodsType}" mainItem="true">
                	<#list suppGoods.minQuantity..suppGoods.maxQuantity as num>
						<option value="${num}" >${num}</option>
					</#list>
                </select>
            </td>
            <td width="10%" class="orange" id="${suppGoods.suppGoodsId}Td">总价：￥--</td>
            <td></td>
        </tr>
        </tr>
    </tbody>
</table>