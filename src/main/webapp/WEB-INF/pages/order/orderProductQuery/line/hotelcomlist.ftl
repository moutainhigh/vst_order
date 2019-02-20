  <div id="dialogShow" style="display:none">
  <#if comboDinnerList?? && comboDinnerList?size &gt; 0>
        <div class="hotel_ebk_wrap">     	
        		<table class="p_table table_enter hotelcomTable" branchType="addition">
        			<thead>
        				<tr class="noborder">
        					<th colspan="4" style="text-align:left;">必选服务</th>       					
        				</tr>
        			</thread>
        			<tbody>
        			 <tr>
        			   <td> 	
        				<#assign hotelcomIdIndex=0 />
        				<#list comboDinnerList as pb>
        					<#assign goods=pb.suppGoodsList[0] />
        						<table class="tab_nav" width="100%">
		        					<tr class="table_nav" adult="" child="" suppGoodsId="${goods.suppGoodsId}" id="${goods.suppGoodsId}">
		        						<td width="60%"><a class="pro_tit" href="javascript:;" desc="${pb.branchName}" prodBranchId="${pb.productBranchId}">${pb.branchName}</a>
		        							<input type="hidden" name="itemMap[${goods.suppGoodsId}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
		        						</td>
		        						<td width="10%" num="${goods.fitQuantity}">
		        							<input type="hidden" readOnly class="w5" name="itemMap[${goods.suppGoodsId}].quantity" style="text-align:center" value="0" required=true number=true />
		        							${goods.fitQuantity}份
		        						</td>
		        						<td width="10%" class="orange">总价：￥${goods.dailyLowestPriceYuan}元</td>
		        						<td><div class="operate" style="text-align:center;"><a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" suppGoodsId="${goods.suppGoodsId}" name="xztcBtn" >选择</a></div></td>
		        					</tr>
		        					<tr>
										<td colspan="4">
										<p class="descript">规格描述：<textarea class="textarea"></textarea></p>
										</td>
									</tr>
	        					</table>
	        				<#assign hotelcomIdIndex=hotelcomIdIndex+1 />        				
        				</#list>
        				</td>
        			  </tr>
        			</tbody>
        		</table>        	
        </div>
        </#if>
</div>