<div style="text-align:left;color:#0000FF;font-size:12px;padding-left:20px;">
	<input type="radio" name="activityName" value="BOOKING_AUDIT" checked=checeked <#if monitorCnd?? && monitorCnd.activityName =="BOOKING_AUDIT">checked=checeked</#if> count= '${bookingAuditNum!0}'/>预订通知（${bookingAuditNum!0}）
	<span >&nbsp; &nbsp; </span>
	
	<#list subTypeList as subType>
	 <span >&nbsp; &nbsp; </span>
	 <input type="radio" name="activityName" value="${subType.code!''}" <#if monitorCnd?? && monitorCnd.activityDetail == "${subType.code!''}">checked=checeked</#if> count= '${subTypeMap[subType.code]!0}'/>预订通知-${subType.cnName!''}（${subTypeMap[subType.code]!0}）
	</#list>
</div>

<#--结果显示-->
<#if message?? && code == "success">
	<div id="result" class="iframe_content mt20">
		<div class='loading mt20'>${message!''}</div>
	</div>
</#if>
</div>