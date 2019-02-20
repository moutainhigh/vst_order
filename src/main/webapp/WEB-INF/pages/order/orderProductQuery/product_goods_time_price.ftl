<#macro showBreakFastType breakfast>
	<#switch breakfast>
		<#case 2>
			双早
		<#break>
		<#case 3>
			三早
		<#break>
		<#case 1>
			单早
		<#break>
		<#default>
			无早
	</#switch>
</#macro>
<#assign weekIndex=0/>
<#assign labelStr=""/>
<#macro dayWeekStr str>
	<#switch str>
		<#case "星期一">
		<#assign weekIndex=1/>
			<td></td>
		<#break>
		<#case "星期二">
		<#assign weekIndex=2/>
			<td></td>
			<td></td>
		<#break>
		<#case "星期三">
		<#assign weekIndex=3/>
			<td></td>
			<td></td>
			<td></td>
		<#break>
		<#case "星期四">
		<#assign weekIndex=4/>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		<#break>
		<#case "星期五">
		<#assign weekIndex=5/>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		<#break>
		<#case "星期六">
		<#assign weekIndex=6/>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		<#break>
		<#default>
		<#assign weekIndex=0/>
	</#switch>
</#macro>
<#macro dayWeekReverseStr str>
	<#switch str>
		<#case "星期一">
			<td></td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		<#break>
		<#case "星期二">
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		<#break>
		<#case "星期三">
			<td></td>
			<td></td>
			<td></td>
		<#break>
		<#case "星期四">
			<td></td>
			<td></td>
		<#break>
		<#case "星期五">
			<td></td>
		<#break>
		<#case "星期六">
		<#break>
		<#default>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
	</#switch>
</#macro>
<table id="timePriceTb" class="pg_d_table table_center co" style="width:50px;">
    <tbody>
    	<tr>
            <td nowrap="nowrap" style="width:14%;">
                <span class="co_info_line dis">周日</span>
            </td>
            <td nowrap="nowrap" style="width:14%;">
                <span class="co_info_line dis">周一</span>
            </td>
            <td nowrap="nowrap" style="width:14%;">
                <span class="co_info_line dis">周二</span>
            </td>
            <td nowrap="nowrap" style="width:14%;">
                <span class="co_info_line dis">周三</span>
            </td>
            <td nowrap="nowrap" style="width:14%;">
                <span class="co_info_line dis">周四</span>
            </td>
            <td nowrap="nowrap" style="width:14%;">
                <span class="co_info_line dis">周五</span>
            </td>
            <td nowrap="nowrap" style="width:14%;">
                <span class="co_info_line dis">周六</span>
            </td>
        </tr> 
        <tr>
		<#list timePriceList as timePrice>
				<#if timePrice_index==0>
		    		<@dayWeekStr timePrice.dayWeekStr/>
		    	</#if>
		    	 <td <#if timePrice.price != price || timePrice.breakfast != breakfast>style='background-color:#FEF300;'</#if>>
		            <span>${timePrice.specDateStr}</span><br>
		            <span>（${timePrice.stock!'0'}）</span><br>
		            <span><@showBreakFastType timePrice.breakfast/></span><br>
		            <span class="cc7">${timePrice.priceYuan}元</span>
		        </td>
		        <#assign index=timePrice_index+1/>
		        <#if timePriceList?size==index>
		    		<@dayWeekReverseStr timePrice.dayWeekStr/>
		    	</#if>
		        <#if weekIndex%7==6></tr><tr></#if>
		        <#assign weekIndex=weekIndex+1/>
		</#list>
		</tr>
    </tbody>
</table>
<script>
	if(typeof updatePriceTimePre==="function"){
        updatePriceTimePre('${suppGoods!''}');//携程促销修改时间价格
	}
</script>