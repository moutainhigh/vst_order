 <#assign isGroupLast = ( prodRouteDetailGroup_index==((prodLineRouteDetail.prodRouteDetailGroupList?size)-1) && detailIndex==(detailSize-1) )/>
 
 <li <#if isGroupLast>class="last"</#if>>
		<#if detailIndex == 0>
					                        
            <div class="instance-travel-xc-time">
                <b>${prodRouteDetailGroup.getTimeType()!''}</b>
                <#if prodRouteDetailGroup.localTimeFlag == "Y"><span>当地时间</span></#if>
            </div>
        </#if>

        <div class="instance-travel-xc-jt">
            <i class="instance-travel-xc-icon instance-travel-xc-icon-restaurant"></i>
        </div>
        
        <div class="instance-travel-xc-info">
            <p class="instance-travel-xc-info-single" >
            <#if detailIndex != 0 && item.logicRelation != "">
            	<em class="instance-travel-xc-and-or">（<#if item.logicRelation == "AND">和<#elseif item.logicRelation == "OR">或</#if>）</em>
            </#if>	
        	<#if item.templateText?? && item.templateText!=''>
                <@func.addSpace item.templateText 37/>
        	</#if>
            </p>
        </div>
</li>