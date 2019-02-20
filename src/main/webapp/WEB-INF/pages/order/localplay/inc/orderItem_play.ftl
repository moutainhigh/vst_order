<table width="100%">
	<tbody>
		<tr>
			<td  colspan="2" style="font-size:16px;">
				<b>附加信息</b>：
			<td>
		</tr>
		<#if useTimeFlag=='Y'>
		<tr>
            <td  class="e_label td_top">使用时间：</td>
            <td>
				<select id="useTime" name="itemMap[${suppGoods.suppGoodsId}].useTime" required="true">
					<#if useTimeList ?? && useTimeList?size != 0>
						<option value="">请选择使用时间</option>
	            		<#list useTimeList as time>
	            			<option value="${time}">${time?if_exists}</option>
	            		</#list>
        			</#if>
	            </select>
		      </td> 
	    </tr>
		</#if>
		<#if localHotelAddressFlag=='Y'>
			 <tr>
		        <td width="100" class="e_label td_top"><i class="cc1"></i>酒店地址：</td>
		        <td>
					<input type="text" name="itemMap[${suppGoods.suppGoodsId}].localHotelAddress" value="${localHotelAddress}"  required="true"/>    			
		            <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>酒店地址不正确</span>
		        </td>
		    </tr>
		</#if>	

   </tbody>
</table>
