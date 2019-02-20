<#macro trafficFlightInfo detail toStartDate backStartDate>
	<div class="trafficBox"><#if detail.prodProduct.prodTrafficVO.referFlag=="Y">参考车次<#else>精准车次</#if>
			<#assign prodTrafficGroup=detail.prodProduct.prodTrafficVO.prodTrafficGroupList[0] />
			<#if prodTrafficGroup??&&prodTrafficGroup.prodTrafficFlightList?size == 1>
            	<div class="trafficInfo">
                	<p class="trafficTab"><span>（去程）${toStartDate}</span><span>${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.startTime}</span><span class="to_startAirportString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.startAirportString}</span><span class="to_airlineString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span><#if prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightTime>（约${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightTimeMinute}分）</#if></span><span>${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.arriveTime}</span><span>${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.arriveAirportString}</span><span>${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightNo}</span></p>
                </div>
			</#if>	  							
			<#if prodTrafficGroup??&&prodTrafficGroup.prodTrafficFlightList?size == 2>
            	<div class="trafficInfo">
                	<p class="trafficTab"><span>（去程）${toStartDate}</span><span class="to_startTime">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.startTime}</span><span class="to_startAirportString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.startAirportString}</span><span class="to_airlineString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightTimeMinute}分）</span><span class="to_arriveTime">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.arriveTime}</span><span class="to_arriveAirportString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.arriveAirportString}</span><span class="to_flightNo">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightNo}</span></p>
                </div>
                <div class="trafficInfo">
                	<p class="trafficTab"><span>（返程）${backStartDate}</span><span class="back_startTime">${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.startTime}</span><span class="back_startAirportString">${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.startAirportString}</span><span class="back_airlineString">${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.flightTimeMinute}分）</span><span class="back_arriveTime">${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.arriveTime}</span><span class="back_arriveAirportString">${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.arriveAirportString}</span><span class="back_flightNo">${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.flightNo}</span></p>
           		 </div>
			</#if>
			<#if prodTrafficGroup??&&prodTrafficGroup.prodTrafficFlightList?size == 3>
				<div class="trafficInfo">
                	<p class="trafficTab"><span>（去程）${toStartDate}</span><span class="to_startTime">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.startTime}</span><span class="to_startAirportString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.startAirportString}</span><span class="to_airlineString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightTimeMinute}分）</span><span class="to_arriveTime">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.arriveTime}</span><span class="to_arriveAirportString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.arriveAirportString}</span><span class="to_flightNo">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightNo}</span></p>
                </div>
                <div class="trafficInfo">
                	<p class="trafficTab"><span>（经停）${toStartDate}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.startTime}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.startAirportString}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.flightTimeMinute}分）</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.arriveTime}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.arriveAirportString}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.flightNo}</span></p>
           		 </div>
           		 <div class="trafficInfo">
                	<p class="trafficTab"><span>（返程）${backStartDate}</span><span class="back_startTime">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.startTime}</span><span class="back_startAirportString">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.startAirportString}</span><span class="back_airlineString">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.flightTimeMinute}分）</span><span class="back_arriveTime">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.arriveTime}</span><span class="back_arriveAirportString">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.arriveAirportString}</span><span class="back_flightNo">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.flightNo}</span></p>
                </div>
			</#if>
			<#if prodTrafficGroup??&&prodTrafficGroup.prodTrafficFlightList?size == 4>
				<div class="trafficInfo">
                	<p class="trafficTab"><span>（去程）${toStartDate}</span><span class="to_startTime">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.startTime}</span><span class="to_startAirportString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.startAirportString}</span><span class="to_airlineString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightTimeMinute}分）</span><span class="to_arriveTime">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.arriveTime}</span><span class="to_arriveAirportString">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.arriveAirportString}</span><span class="to_flightNo">${prodTrafficGroup.prodTrafficFlightList[0].bizFlight.flightNo}</span></p>
                </div>
                <div class="trafficInfo">
                	<p class="trafficTab"><span>（经停）${toStartDate}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.startTime}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.startAirportString}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.flightTimeMinute}分）</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.arriveTime}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.arriveAirportString}</span><span>${prodTrafficGroup.prodTrafficFlightList[1].bizFlight.flightNo}</span></p>
           		 </div>
           		 <div class="trafficInfo">
                	<p class="trafficTab"><span>（返程）${backStartDate}</span><span class="back_startTime">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.startTime}</span><span class="back_startAirportString">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.startAirportString}</span><span class="back_airlineString">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.flightTimeMinute}分）</span><span class="back_arriveTime">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.arriveTime}</span><span class="back_arriveAirportString">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.arriveAirportString}</span><span class="back_flightNo">${prodTrafficGroup.prodTrafficFlightList[2].bizFlight.flightNo}</span></p>
                </div>
                <div class="trafficInfo">
                	<p class="trafficTab"><span>（经停）${backStartDate}</span><span>${prodTrafficGroup.prodTrafficFlightList[3].bizFlight.startTime}</span><span>${prodTrafficGroup.prodTrafficFlightList[3].bizFlight.startAirportString}</span><span>${prodTrafficGroup.prodTrafficFlightList[3].bizFlight.airlineString}</span></p>
                    <p class="trafficTab"><span>（约${prodTrafficGroup.prodTrafficFlightList[3].bizFlight.flightTimeHour}小时${prodTrafficGroup.prodTrafficFlightList[3].bizFlight.flightTimeMinute}分）</span><span>${prodTrafficGroup.prodTrafficFlightList[3].bizFlight.arriveTime}</span><span>${prodTrafficGroup.prodTrafficFlightList[3].bizFlight.arriveAirportString}</span><span>${prodTrafficGroup.prodTrafficFlightList[3].bizFlight.flightNo}</span></p>
           		 </div>
			</#if>
	</div>  
</#macro>