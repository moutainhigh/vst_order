<!--线路详情页 行程介绍 之行程概要-->

 <ul class="instance_list">
 <#list product.prodLineRouteList as prodLineRouteFrontVo>

 				<li style="display:<#if prodLineRouteFrontVo_index == 0>block<#else>none</#if>;">
 				    <#if (prodLineRouteFrontVo.routeNum?? && prodLineRouteFrontVo.routeNum gt 0)||(prodLineRouteFrontVo.stayNum?? && prodLineRouteFrontVo.stayNum gt 0)>
                        <dl class="instance_list_text">
	                        <dt>行程天数：</dt>
	                        <dd>
	                        	<#if prodLineRouteFrontVo.routeNum?? && prodLineRouteFrontVo.routeNum gt 0 >${prodLineRouteFrontVo.routeNum}天</#if>
	                        	<#if prodLineRouteFrontVo.stayNum?? && prodLineRouteFrontVo.stayNum gt 0 >${prodLineRouteFrontVo.stayNum}晚</#if>
								<#if prodLineRouteFrontVo.trafficNum?? && prodLineRouteFrontVo.trafficNum != 0>
								    <#if prodLineRouteFrontVo.trafficNum gt 0>
                                                                                           （因航班原因可能造成到达日推迟${prodLineRouteFrontVo.trafficNum}天）
									<#else>
                                                                                           （因航班原因可能造成到达日提前${(prodLineRouteFrontVo.trafficNum*(-1))}天）
									</#if>
								</#if>
	                        </dd>
                        </dl>
                        </#if>
                        <#if prodLineRouteFrontVo.routeFeature?? && prodLineRouteFrontVo.routeFeature?trim!=''>
                        <dl class="instance_list_text">
                            <dt>行程特色：</dt>
                            <dd> <@func.addSpace prodLineRouteFrontVo.routeFeature 49/> </dd>
                        </dl>
                        </#if>
                        <#if (prodLineRouteFrontVo.traveTimeList?exists) && (prodLineRouteFrontVo.traveTimeList?size gt  0) >
                        <dl class="instance_list_text">
                            <dt>出行时间：</dt>
                            <dd>
                            	<#assign traveTimeListSize = (prodLineRouteFrontVo.traveTimeList?size) />
                                <#list prodLineRouteFrontVo.traveTimeList as traveTimeOne>
				        			<p>
					        			<#list traveTimeOne?split("&&") as value>
					        				<#if value_index==0>
					        					<em>【${value}】 </em>
					        				<#else>
                                                <#assign len = 50>
                                                <#assign total_length=value?length>
                                                <#assign count = total_length/len>
            
                                                <#if total_length%len gt 0>
                                                     <#assign count=count+1>
                                                </#if>
                                                <#assign start=0>
                                                <#assign end=0>
                                                <#list 1..count as c>
                                                       <#assign end=end+len>
                                                       <#if end gt total_length>
    	                                                    <#assign end=total_length/>
                                                       </#if>
	                                                   ${value?substring(start,end)} 
	                                                   <#assign start=end>
                                                </#list>

                                                  
					        				</#if>
					        			</#list>
				        			</p>
						         </#list>
                            </dd>
                        </dl>
                         </#if>
                        
                        <#if prodLineRouteFrontVo.routeInfos?? && prodLineRouteFrontVo.routeInfos?size gt 0 >
	                        <#list prodLineRouteFrontVo.routeInfos as routeInfo>
	                        	<#if routeInfo?? && routeInfo?size gt 0 >
	                        		<#assign hasRouteInfo=true />
	                        	</#if>
	                        </#list>
	                    <#if hasRouteInfo?? && hasRouteInfo>    
	                        <dl class="instance_list_text">
	                            <dt>线路概况：</dt>
	                            <dd>	
						        	<#list prodLineRouteFrontVo.routeInfos as routeInfo>
							   				<#if routeInfo?size gt 0 >
		                       					<p>【第${routeInfo_index+1}天】 
			                       					<#assign routeInfoSize = (routeInfo?size) />
			                       					<#list routeInfo as scenicOrActivity>
			                       						${scenicOrActivity}<#if scenicOrActivity_index!=routeInfoSize-1>></#if>
			                       					</#list>
		                       					</p>
	                       					</#if>  
						             </#list>
	                            </dd>
	                        </dl>
                        </#if>
                        </#if>
                        <span class="instance_jt" style="left: 29.5px;">◆<i>◆</i></span>
                </li>
</#list>        
</ul>



