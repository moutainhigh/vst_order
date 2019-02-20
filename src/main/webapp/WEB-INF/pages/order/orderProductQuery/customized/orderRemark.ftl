<table width="100%">
	<tbody>
	  <tr>
    		<td style="font-size:16px;">
    			<b>订单备注</b>：
    		<td>
    	</tr>
         <tr>
         	<td class="e_label"></td>
            <td >
                <div style="width: 345px;">
                    <textarea class="w37" name="remark" maxlength="100"></textarea>
                    <div class="cc3 tr"><span class="cc1">0</span>/100字（中文(50个)/英文(100个)）</div>
                </div>
            </td>
        </tr>
        <#if isFaxBreakRemark?? && isFaxBreakRemark>
        <#else>
        <tr>
        	<td class="e_label"></td>
        	<td style="color:blue;height:10px;"><a href="javascript:void(0);" onclick="copyRemarkToFaxMemo();">复制粘贴订单备注</a></td>
        </tr>
         <#--<tr>
    		<td colspan="2" style="font-size:16px;">
    			<b>传真/ebk备注</b>：
    		<td>
    	</tr>
        <tr>
        	<td class="e_label"></td>
            <td>
            	<div style="width: 345px;">
                    <textarea class="w37" name="faxMemo"  maxlength="100"></textarea>
                    <div class="cc3 tr"><span class="cc1">0</span>/100字（中文(50个)/英文(100个)）</div>
                </div>
            </td>
        </tr>-->
        </#if>
        <tr>
    		<td colspan="2" style="font-size:16px;">
    			<b>当地接送信息：</b>
    		<td>
    	</tr>
    	<#if trafficInfoList??&&trafficInfoList?size&gt;0> <!-- 当地游产品去程和返程上下车选择项 -->
        <tr>
        	<td class="e_label">去程上车点：</td>
            <td>
            	
            		<#list trafficInfoList as traffic>
            			<#if traffic['trafficMap']['toBuses']??&&traffic['trafficMap']['toBuses']?size&gt;0>
					        <div class="no_bd pt_10">
					        	<dl class="user_dl name_check">
					            	<dt>选择上车地点：</dt>
					                <#list traffic['trafficMap']['toBuses'] as tobus>
					                <dd>
					                	<label class="check"><input class="radio" name="additionMap[frontBusStop]" value="上车地点：${tobus['address']};发车时间：${tobus['startTime']};备注：${tobus['memo']}" <#if bus_index==0>checked="checked"</#if> type="radio">${tobus['address']}</label><span class="ts_text">发车时间：${tobus['startTime']}</span>
					                    <#if tobus['memo'] != null && tobus['memo'] != "">
                                        	<span class="ts_text">备注：${tobus['memo']}</span><br>
                                        </#if>
					                </dd>
					                </#list>
					            </dl>
					        </div>
            			</#if>
            		</#list>
            </td>
        </tr>
        <tr>
        	<td class="e_label">返程上车点：</td>
            <td>
            		<#list trafficInfoList as traffic>
            			<#if traffic['trafficMap']['backBuses']??&&traffic['trafficMap']['backBuses']?size&gt;0>
					        <div class="no_bd pt_10">
					        	<dl class="user_dl name_check">
					            	<dt>选择上车地点：</dt>
					                <#list traffic['trafficMap']['backBuses'] as backbus>
					                <dd>
					                	<label class="check"><input class="radio" name="additionMap[backBusStop]" value="上车地点：${backbus['address']};发车时间：${backbus['startTime']};备注：${backbus['memo']}" <#if bus_index==0>checked="checked"</#if> type="radio">${backbus['address']}</label><span class="ts_text">发车时间：${backbus['startTime']}</span>
					                    <#if backbus['memo'] != null && backbus['memo'] != "">
                                        	<span class="ts_text">备注：${backbus['memo']}</span><br>
                                        </#if>
					                </dd>
					                </#list>
					            </dl>
					        </div>
            			</#if>
            		</#list>
            	
            </td>
        </tr>
        </#if>
    </tbody>
</table>
<script>
	$(function(){
	    $("textarea[name=remark]").change(function(){
			$(this).closest("div").find("div span").html(getStrLength(this.value));
		});
		$("textarea[name=faxMemo]").change(function(){
			$(this).closest("div").find("div span").html(getStrLength(this.value));
		});
    });
    function getStrLength(str){   
	    var cArr = str.match(/[^\x00-\xff]/ig);   
	    return str.length + (cArr == null ? 0 : cArr.length);   
	}
	function copyRemarkToFaxMemo(){
		 $("textarea[name=faxMemo]").val($("textarea[name=remark]").val());
		 $("textarea[name=faxMemo]").closest("div").find("div span").html(getStrLength($("textarea[name=faxMemo]").val()));
	}
</script>