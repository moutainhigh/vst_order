<#if bizOrderConnectsPropList??>
	<tr>
		<td style="font-size:16px;">
			<b>服务信息填写</b>：
		<td>
	</tr>
	<#list bizOrderConnectsPropList as list>
			<tr>
		        <td width="100" class="e_label td_top">
		        	<#if list.propCode == 'car_using_person_number'>
		        		<#if  maxPeopleNum??>
		        			<input type="hidden" name="orderConnectsServicePropList[${list_index}].propId"  value="${list.propId}">
		        			<#if list.require == 'Y'>
				        		<i class="cc1">*</i>
				        	</#if>
					        ${list.propName}：
					     </#if>
					<#else>
							<input type="hidden" name="orderConnectsServicePropList[${list_index}].propId"  value="${list.propId}">
					 		<#if list.require == 'Y'>
				        		<i class="cc1">*</i>
				        	</#if>
					        ${list.propName}：
		        	</#if>
		        </td>
		        <td>
			        <#if list.textType == 'TEXT'>
			            <input type="text" name="orderConnectsServicePropList[${list_index}].propValue"  value="" name_type ="${list.propCode}" <#if list.require == 'Y'>required="Y"</#if>>
			        </#if>  
			        <#if list.textType == 'SELECT'>
			        	<#if list.propCode == 'baggage_number'>
				            <select name="orderConnectsServicePropList[${list_index}].propValue">
					            <#list 0..10 as number>
									<option value="${number}">${number}</option>	
								</#list>
				            </select>
				        <#elseif list.propCode == 'car_using_person_number'>
				        	<#if maxPeopleNum??>  
					            <select name="orderConnectsServicePropList[${list_index}].propValue">
						            <#list 0..maxPeopleNum as number>
										<option value="${number}">${number}</option>	
									</#list>
					            </select>
				           </#if>
			            </#if> 
			        </#if>  
			        <#if list.textType == 'CHECK' && list.propCode=='other_remark'>
			        		<input type="hidden" id="otherRemark" name="orderConnectsServicePropList[${list_index}].propValue" value="">
			        		<#if  suppGoodsConnectsAdditional.remark?contains("HAS_VISA")>
			            	<input type="checkbox"  name="connectsRemark" value="在当地机场办理落地签" >在当地机场办理落地签</br>
			            	</#if>
			            	<#if  suppGoodsConnectsAdditional.remark?contains("HAS_CHILDREN")>
			            	<input type="checkbox"  name="connectsRemark" value="需携带婴儿或儿童">需携带婴儿或儿童
			            	</#if>
			        </#if>
			        <#if list.textType == 'DATE'>
				        <select id="hournumber">
				        	<#list 0..23 as hournumber>
				        		<option value="${hournumber}">${hournumber}</option>
				        	</#list>
				        </select>点  
				        	&nbsp;&nbsp;&nbsp;&nbsp;
				        <select id="minitenumber">
				        		<option value="00">00</option>
				        		<option value="15">15</option>
				        		<option value="30">30</option>
				        		<option value="45">45</option>
				        </select>分
				        <input type="hidden" id="connectshourtime" name="orderConnectsServicePropList[${list_index}].propValue">
			        </#if>
		            <#if list.require == 'Y'>
		            	<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>${list.propName}填写不正确</span>
		            </#if>
		        </td>
		    </tr>
	</#list>
</#if>
<#if orderRequiredvO??>
	 <#if orderRequiredvO.ecFlag=="Y">
	 <tr>
		<td style="font-size:16px;">
			<b>紧急联系人</b>：
		<td>
	</tr>
     <tr>
        <td width="100" class="e_label td_top">联系人：</td>
        <td>
        	<input type="hidden" id="emergencyPersonId" name="emergencyPerson.ordPersonId" value="<#if emergencyPerson>${emergencyPerson.ordPersonId}</#if>"/>
            <input type="text" name="emergencyPerson.fullName"  value="<#if emergencyPerson>${emergencyPerson.fullName}</#if>">
            <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>联系人填写不正确</span>
        </td>
    </tr>
    <tr>
        <td class="e_label td_top">联系人手机：</td>
        <td>
        <input type="text" id="contactMobile" name="emergencyPerson.mobile"   value="${emergencyPerson.mobile}" onchange="" >
        <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>联系人手机填写不正确</span>
        </td>
    </tr>
    </#if>
    <#if orderRequiredvO.tpFlag=="Y">
    <tr>
		<td  colspan="2" style="font-size:16px;">
			<b>订单联系人</b>：
		<td>
	</tr>
	 <tr>
        <td width="100" class="e_label td_top"><i class="cc1">*</i>联系人：</td>
        <td>
        	<input type="hidden" id="contactPersonId" name="contact.ordPersonId" value="<#if contactPerson>${contactPerson.ordPersonId}</#if>"/>
        	<input type="hidden" id="contactdType" name="contact.idType" value="<#if contactPerson>${contactPerson.idType}</#if>"/>
        	<input type="hidden" id="contactdNo" name="contact.idNo" value="<#if contactPerson>${contactPerson.idNo}</#if>"/>
            <input type="text" name="contact.fullName" value="<#if contactPerson>${contactPerson.fullName}</#if>" name_type="fullName" required=true>
            <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>联系人不正确</span>
        </td>
    </tr>
    <tr>
        <td class="e_label td_top"><i class="cc1">*</i>联系人手机：</td>
        <td>
            <input type="text" id="contactMobile" name="contact.mobile" value="<#if contactPerson>${contactPerson.mobile}</#if>" name_type="mobile" onchange="" required=true> 
            <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>联系人手机不正确</span>
        </td>
    </tr>
      <#if isPlayOutType?? && isPlayOutType==true>
        <tr>
    	  <td class="e_label td_top">境外手机号：</td>
          <td>
            <input type="text" id="contactMobile" name="contact.outboundPhone" value="<#if contactPerson>${contactPerson.outboundPhone}</#if>" name_type="mobile"> 
            <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>境外手机号不正确</span>
          </td>
       </tr>
      </#if>
    </#if> 	
  
    
    <#if hasContractOrder>
    <tr>
    	<td class="e_label td_top"><i class="cc1">*</i>邮箱地址：</td>
    	<td>
    		<input id="contactEmail" name="contact.email" value="<#if contactPerson>${contactPerson.email}</#if>" type="text" 
    			placeholder="邮箱" name_type="email" required=true>
    		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮箱地址不正确</span>
   		</td>
   	</tr>
   	<#else>
   		<input id="contactEmail" name="contact.email" value="<#if contactPerson>${contactPerson.email}</#if>" type="hidden" />
   	</#if>
    <tr>
		<td style="font-size:16px;">
			<b>游玩人信息</b>
		<td>
	</tr>
	<#else>
		<input type="hidden" id="contactPersonId" name="contact.ordPersonId" value="<#if contactPerson>${contactPerson.ordPersonId}</#if>"/>
		<input type="hidden" name="contact.fullName" value="<#if contactPerson>${contactPerson.fullName}</#if>" name_type="fullName" />
		<input type="hidden" id="contactEmail" name="contact.email" value="<#if contactPerson>${contactPerson.email}</#if>" />
    </#if>
    <#if isTravellerDelay?? && isTravellerDelay == true>
		<#if orderRequiredvO.travNumType=="TRAV_NUM_ONE">
			<#list 1..1 as number>
			 	<#include "/order/requiredItem/requiredItem_traveller_delay.ftl"/>
			</#list>
		</#if>
		<#if orderRequiredvO.travNumType=="TRAV_NUM_ALL">
			<#list 1..personNum as number>
				<#include "/order/requiredItem/requiredItem_traveller_delay.ftl"/>
			</#list>
		</#if>
	<#elseif isSupplyFlag?? && isSupplyFlag == 'Y'>
		<#if orderRequiredvO.travNumType=="TRAV_NUM_ONE">
			<#list 1..1 as number>
			 	<#include "/order/requiredItem/requiredItem_trav_supply.ftl"/>
			</#list>
		</#if>
		<#if orderRequiredvO.travNumType=="TRAV_NUM_ALL">
			<#list 1..personNum as number>
				<#include "/order/requiredItem/requiredItem_trav_supply.ftl"/>
			</#list>
		</#if>
	<#else>
		<#if orderRequiredvO.travNumType=="TRAV_NUM_ONE">
			<#list 1..1 as number>
			 	<#include "/order/requiredItem/requiredItem.ftl"/>
			</#list>
		</#if>
		<#if orderRequiredvO.travNumType=="TRAV_NUM_ALL">
			<#list 1..personNum as number>
				<#include "/order/requiredItem/requiredItem.ftl"/>
			</#list>
		</#if>
	</#if>
	