<#macro trafficTrainInfo detail toStartDate backStartDate>
	<div class="trafficBox">
			<#if detail.prodProduct.prodTrafficVO.referFlag=="Y">参考车次<#else>精准车次</#if>
			<#assign prodTrafficGroup=detail.prodProduct.prodTrafficVO.prodTrafficGroupList[0] />
			<#if prodTrafficGroup??&&prodTrafficGroup.prodTrafficTrainList?size &gt; 0>
				<#list prodTrafficGroup.prodTrafficTrainList as prodTrafficTrain>
				<div class="trafficInfo">
                	<p class="trafficTab"><span><#if prodTrafficTrain.tripType=="TO">（去程）${toStartDate}<#else>（返程）${backStartDate}</#if></span><span>${prodTrafficTrain.bizTrain.startTime}</span><span>${prodTrafficTrain.bizTrain.startStationString}</span><span>${prodTrafficTrain.bizTrain.trainTypeString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficTrain.bizTrain.costTimeHour}小时${prodTrafficTrain.bizTrain.costTimeMinute}分）</span><span>${prodTrafficTrain.bizTrain.arriveTime}</span><span>${prodTrafficTrain.bizTrain.arriveStationString}</span><span>${prodTrafficTrain.trainNo}</span></p>
                </div>
				</#list>
			</#if>			
	</div>  
</#macro>