 <div style="display:none">
  <#if adultChildDiffList?? && adultChildDiffList?size &gt; 0>
        <div class="p_box">
        	<div id="lineError"></div>        	
        		<table class="p_table table_enter mainGoodsTable" branchType="addition">
        			<thead>
        				<tr class="noborder">
        					<th colspan="4" style="text-align:left;">必选服务</th>        					
        				</tr>
        			</thread>
        			<tbody>
        				<tr class="table_nav">
        					<td class="text_left">商品名称</td>        					
        					<td class="text_left">单价</td>     					
        					<td class="text_left">份数</td>
        					<td class="text_left">总价</td>
        				</tr>
        				<#assign mainIdIndex=0 />
        				<#list adultChildDiffList as pb>
        					<#if pb.suppGoodsList?? && pb.suppGoodsList?size &gt; 0>
        					<#assign goods=pb.suppGoodsList[0] />
        						<#if goods??> 
	        					<tr class="table_nav" adult="" child="" suppGoodsId="${goods.suppGoodsId}">
	        						<td class="text_left">${goods.goodsName}
	        							<input type="hidden" name="itemMap[${goods.suppGoodsId}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
	        							<input type="hidden" name="itemMap[${goods.suppGoodsId}].routeRelation" value="MAIN" autocomplete="off"/>
	        							<input type="hidden" name="itemMap[${goods.suppGoodsId}].childQuantity" value="${goods.fitChildQuantity}" autocomplete="off"/>
	        							<input type="hidden" name="itemMap[${goods.suppGoodsId}].adultQuantity" value="${goods.fitAdultQuantity}" autocomplete="off"/>
	        						</td>
	        						<td>--</td>
	        						<td>
	        							<input type="text" readOnly class="w5" name="itemMap[${goods.suppGoodsId}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
	        						</td>
	        						<td>--</td>
	        					</tr>
	        					<#assign mainIdIndex=mainIdIndex+1 />  
	        					</#if>    
	        					</#if>    				
        				</#list>
        			</tbody>
        		</table>        	
        </div>
        </#if>
     </div>