<form id="dataForm">
        <table class="p_table form-inline">
            <tbody>
                <tr>
                    <td class="s_label">产品类型：</td>
                    <td>
                    <span> ${categoryName!''}</span>
			        </td>
			        <td class="s_label">发送节点：</td>
                    <td>
                    	<#list sendNodeList as sendNode>
                    		<#if ordSmsTemplate.sendNode = sendNode.code>${sendNode.cnName!''}</#if>
                    	</#list>
			        </td>
			     </tr>
			     <tr>            
                 	<td class="s_label">规则名称：</td>
                    <td><span><#if rule == "Y">${ordSmsTemplate.templateName}<#else>${ordSmsTemplate.ruleName}</#if></span></td>
                    <td class="s_label">规则类型：</td>
                    <td>
                    <#if rule == "Y">
                    	<span>发送规则</span>
                    <#else>	 
                		<span>不发送规则</span>
                    </#if>
                    </td>
                 </tr>
                 <tr>
                	<td class="s_label">一级渠道：</td>
                    <td>
                    	<span>${distributorName!''}</span>
			      </td>
                 <td class="s_label">供应商：</td>  
		            <td>
		           		<span>${supplierName}</span>
                	</td>
                </tr>
                <#if rule == "Y">
                <tr>
                 <td class="s_label">下单时间：</td>
                 <td>
                     <#list orderTimeList as orderTime>
                    	 <#if ordSmsTemplate.orderTime == orderTime.code>${orderTime.cnName!''}</#if>
                    </#list>
			     </td>
			    </tr>
                <tr>    
                    <td class="s_label">短信内容：</td>
	   				<td  colspan="3">
	   					<textarea class="smsContent" name="content" style="width:600px;height:100px" disabled>${ordSmsTemplate.content!''}</textarea>
	   				</td>
                </tr>
               </#if>
            </tbody>
        </table>
</form>