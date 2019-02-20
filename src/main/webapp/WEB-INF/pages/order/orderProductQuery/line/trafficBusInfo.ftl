<#macro trafficBusInfo detail toStartDate backStartDate>
	<div class="trafficBox">
			<#if detail.prodProduct.prodTrafficVO.referFlag=="Y">参考车次<#else>精准车次</#if>
			<#assign prodTrafficGroup=detail.prodProduct.prodTrafficVO.prodTrafficGroupList[0] />
			<#if prodTrafficGroup??&&prodTrafficGroup.prodTrafficBusList?size &gt; 0>
				<#list prodTrafficGroup.prodTrafficBusList as prodTrafficBus>
				<div class="trafficInfo">
                	<p class="trafficTab"><span><#if prodTrafficBus.tripType=="TO">（去程）${toStartDate}<#else>（返程）${backStartDate}</#if></span><span>${prodTrafficBus.startTime}</span><span>${prodTrafficBus.adress}</span><span></span></p>
                </div>
				</#list>
			</#if>			
	</div>  
</#macro>