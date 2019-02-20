<#macro bookLimitType limitType>
	<#switch limitType>
		<#case "NONE">
			 
		<#break>
		<#case "TIMEOUTGUARANTEE">
			 
		<#break>
		<#case "ALLTIMEGUARANTEE">
			 
		<#break>
		<#case "ALLGUARANTEE">
			 
		<#break>
		<#case "PREAUTH">
			 
		<#break>
		<#default>
			 
	</#switch>
</#macro>
<#assign weekIndex=0/>
<#assign labelStr=""/>
<#macro deductType deductTypeStr>
	<#switch deductTypeStr>
		<#case "FULL">
			全额房费，
		<#break>
		<#case "FIRSTDAY">
			首日房费，
		<#break>
		<#case "PEAK">
		 	入住内最高房费，
		<#break>
		<#case "PERCENT">
			百分之几房费，
		<#break>
		<#case "MONEY">
			房费的，
		<#break>
		<#default>
	</#switch>
</#macro>

<#macro isTimeOut timeOut>
	<#switch timeOut>
		<#case "true">
			 <p>退改规则：您的订单已经提供担保，若需要修改与取消本次预订，请在 <span class="cc7 f14">${order.lastCancelTime?string('yyyy-MM-dd HH:mm')}</span>前联系我们，过时不可修改取消，未入住将会扣除您<@brf.deductType orderItem.deductType /> <span class="cc7 f14">${deductAmountToYuan}</span>元。</p>
		<#break>
		<#case "false">
			 <p>退改规则：您的订单已经提供担保，订单一经提交不得修改和取消，未入住将会扣除您<@brf.deductType orderItem.deductType /> <span class="cc7 f14">${deductAmountToYuan}</span>元。</p>
		<#break>
		<#default>
	</#switch>
</#macro>