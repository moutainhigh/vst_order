 <#assign isGroupLast = ( prodRouteDetailGroup_index==((prodLineRouteDetail.prodRouteDetailGroupList?size)-1) && detailIndex==(detailSize-1) )/>
 
 <li <#if isGroupLast>class="last"</#if>>
	<#if detailIndex == 0>
        <div class="instance-travel-xc-time">
            <b><#if prodRouteDetailGroup.getTimeType()!='全天'>${prodRouteDetailGroup.getTimeType()!''}</#if></b>
            <#if prodRouteDetailGroup.localTimeFlag == "Y"><span>当地时间</span></#if>
        </div>
  </#if> 
    <div class="instance-travel-xc-jt">
        <i class="instance-travel-xc-icon instance-travel-xc-icon-scene"></i>
    </div>
    <!-- 无图片样式-->	
	<div  <#if item.scenicNameId?? && item.scenicNameId!=''> class="instance-travel-xc-info instance-travel-xc-w630" <#else>class="instance-travel-xc-info"</#if>>
	    
	    <p class="instance-travel-xc-info-single">
        <#if detailIndex != 0 && item.logicRelateion != "">
			<em class="instance-travel-xc-and-or">（<#if item.logicRelateion == "AND">和<#elseif item.logicRelateion == "OR">或</#if>）</em>
		</#if>
 		<#if item.templateText?? && item.templateText!='' >
 			<#if item.scenicNameId!='' && item.scenicNameId!=''>
                  <@func.addSpace item.templateText 37/>
            </#if>
 		</#if>
        </p>
    </div>
     
     <#if routeShowType=='PAGE' && item.scenicNameId?? && item.scenicNameId!='' && item.scenicDesc!=''>
        <div class="instance-travel-xc-pic" style="display:none"  data_scenic_dest_id="${item.scenicNameId}" data_show_scenic_tag="show">
            <img src="http://placehold.it/180x120/CCCCCC" width="180" height="120"/>
        </div> 
    </#if>    
     
</li>
