<#if relateList??&&relateList?size&gt;0>
<div class="table_t">其他门票</div>
<table width="100%">
	<tbody>
			<#list relateList as relate>
             <tr>
             	<input type="hidden" name="itemMap[${relate.suppGoodsId}].goodsId" value="${relate.suppGoodsId}" autocomplete="off"/>
                <td width="35%">
	                <a href="javascript:void(0);" class="J_tip" tip-content=""  onmouseover="goodsDetailMouseover(${relate.suppGoodsId},this);">${relate.prodProductBranch.branchName}-${relate.goodsName}<#if relate.goodsType=="EXPRESSTYPE_DISPLAY">(实体票)</#if></a>
	                <div style="display: none;">
	        			 <#list relateDetailVOList as rdvl>
					        <#if rdvl??&&(rdvl.suppGoodsDesc??&&rdvl.suppGoodsDesc.suppGoodsId==relate.suppGoodsId||rdvl.suppGoodsRefund??&&rdvl.suppGoodsRefund.goodsId==relate.suppGoodsId)>
						    	 
							    	<#if rdvl.suppGoodsDesc??&&rdvl.suppGoodsDesc.priceIncludes??>
							    	 费用包含:${rdvl.suppGoodsDesc.priceIncludes}<br/>
							        </#if>
							        <#if rdvl.suppGoodsDesc??&&(rdvl.suppGoodsDesc.changeTime?? || 
							        	rdvl.suppGoodsDesc.changeAddress?? ||
							        	rdvl.suppGoodsDesc.enterStyle?? ||
							        	rdvl.suppGoodsDesc.limitFlag??)||relate.suppGoodsExp??&&(
							        	relate.suppGoodsExp.days?? ||
							        	relate.suppGoodsExp.startTime?? ||
							        	relate.suppGoodsExp.endTime??)>
							         	入园须知<br/>
							         	<#if rdvl.suppGoodsDesc??>
								        	<#if rdvl.suppGoodsDesc.changeTime??>
								            ·取票时间：${rdvl.suppGoodsDesc.changeTime}<br/>
								            </#if>
								            <#if rdvl.suppGoodsDesc.changeAddress??>
								            ·取票地点：${rdvl.suppGoodsDesc.changeAddress}<br/>
								            </#if>
								            <#if rdvl.suppGoodsDesc.enterStyle??>
								            ·入园方式：${rdvl.suppGoodsDesc.enterStyle}<br>
								            </#if>
								            <#if rdvl.suppGoodsDesc.limitFlag??>
								            ·入园限制：<#if rdvl.suppGoodsDesc.limitFlag=="1">无限制<#else>请在入园当天的${rdvl.suppGoodsDesc.limitTimeStr}分以前入园</#if><br/>
								            </#if>
							            </#if>
							            <#if relate.hasAperiodic()&&relate.suppGoodsExp??>
							            	<#if relate.suppGoodsExp.startTime?? &&
								            			relate.suppGoodsExp.endTime??>
								            ·有效期：${relate.suppGoodsExp.startTime?string('yyyy-MM-dd')} 至 ${relate.suppGoodsExp.endTime?string('yyyy-MM-dd')}有效
								            	<#if relate.suppGoodsExp.unvalid??>
								            	，期票商品不适用日期：${relate.suppGoodsExp.unvalid}<br/>
								            	 </#if>
								            </#if>
							            <#else>
							            	<#if relate.suppGoodsExp.days??>
								            ·有效期：指定游玩日${relate.suppGoodsExp.days}天内有效<br/>
								            </#if>
							            </#if>
							        </p>
							        </#if>
							        <#if rdvl.suppGoodsDesc??&&(rdvl.suppGoodsDesc.height?? ||
								        		rdvl.suppGoodsDesc.age?? ||
								        		rdvl.suppGoodsDesc.region?? ||
								        		rdvl.suppGoodsDesc.maxQuantity?? ||
								        		rdvl.suppGoodsDesc.express?? ||
								        		rdvl.suppGoodsDesc.entityTicket??)>
							        	重要提示<br/>
							        	 <#if rdvl.suppGoodsDesc.height?? ||
								        		rdvl.suppGoodsDesc.age?? ||
								        		rdvl.suppGoodsDesc.region?? ||
								        		rdvl.suppGoodsDesc.maxQuantity?? ||
								        		rdvl.suppGoodsDesc.express?? ||
								        		rdvl.suppGoodsDesc.entityTicket??>
							        		·票种说明：<br/>
							        					<#if rdvl.suppGoodsDesc.height??>·身高：${rdvl.suppGoodsDesc.height}<br/></#if>
							        					<#if rdvl.suppGoodsDesc.age??>·年龄：${rdvl.suppGoodsDesc.age}<br/></#if>
							        					<#if rdvl.suppGoodsDesc.region??>·地域：${rdvl.suppGoodsDesc.region}<br/></#if>
							        					<#if rdvl.suppGoodsDesc.maxQuantity??>·预订数量：${rdvl.suppGoodsDesc.maxQuantity}<br/></#if>
							        					<#if rdvl.suppGoodsDesc.express??>·快递：${rdvl.suppGoodsDesc.express}<br/></#if>
							        					<#if rdvl.suppGoodsDesc.entityTicket??>·实体票：${rdvl.suppGoodsDesc.entityTicket}<br/></#if>
							            	</#if>
							            	<#if rdvl.suppGoodsDesc.others??>
								            	·其他：${rdvl.suppGoodsDesc.others}<br/>
								            </#if>
							       	</#if>
							        	退改说明:${rdvl.getRefundStr(relate.aperiodicFlag)}
					        	</#if>
					        </#list>
				    </div>
                </td>
                <td width="20%"><p>游玩时间：${suppGoodsBaseTimePrice.specDate?string('yyyy-MM-dd')}</p>
                <p>最晚预订时间：<#list timePriceList as tp><#if tp??&&tp.suppGoodsId==relate.suppGoodsId><#if tp??>${tp.aheadBookDate}</#if><#break></#if></#list></p>
                </td>
                <td width="15%" class="orange">
                	<#list timePriceList as tp>
				        <#if tp??&&tp.suppGoodsId==relate.suppGoodsId>
			         	 单价：￥${tp.priceYuan}
			         	<#break>
				        </#if>
			        </#list>
                </td>
                <td width="10%">
                	<select class="hotel_sum" name="itemMap[${relate.suppGoodsId}].quantity" onchange="" adult="${relate.adult}" child="${relate.child}" goodsId="${relate.suppGoodsId}"  mainItem="false">
                    	<option value="0" selected="selected">0</option>
                    	<#list relate.minQuantity..relate.maxQuantity as num>
							<option value="${num}">${num}</option>
						</#list>
                    </select>
                </td>
               	<td width="10%" class="orange" id="${relate.suppGoodsId}Td">总价：￥--</td>
            	<td></td>
            </tr>
            </#list> 	 
    </tbody>
</table>
 </#if>