<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
 <strong>
     下单人相关信息
</strong>
<table >
    <tbody>
        <tr>
            <td class="e_label">登录用户：</td>
            <td>
             <#if ordPersonBooker??>
				 <a href="../../../crm-srv-web/hotline/maintained/index/${order.userNo}" target="_blank">${ordPersonBooker.fullName!''}</a>
			 </#if>
            </td>
        </tr>
        
        <#if ordPersonContact??>
	        <tr>
	            <td class="e_label">联系人：</td>
	            <td>
	            
	            <#if order.distributorId==3 > 
	            	 <#if ordPersonContact.fullName!='' > 
	            	 	${ordPersonContact.fullName!''}
	            	 <#else>
	            	 	 ${ordPersonContact.mobile?substring(0,3)}****${ordPersonContact.mobile?substring(7,ordPersonContact.mobile?length)}
	            	 </#if>
	           
	          	 <#else>
	          	 ${ordPersonContact.fullName!''}
	          	</#if>
	            
	            </td>
	             <#if 'parent' == RequestParameters.orderType && order.mainOrderItem.categoryId==2>
	            <td>
	            <#--
	            
	            -->
	            <a href="javaScript:" id="personUpdate">[修改]
	            </a>
	            </td>
	             </#if>
	        </tr>
	        <tr>
	            <td class="e_label">联系人手机：</td>
	            <td>${ordPersonContact.mobile!''}</td>
	             <#if 'parent' == RequestParameters.orderType>
	            <td><a href="javaScript:" id="orderSendSms">[发送短信]</a> <a href="http://localhost:12366/ipcc/default.jsp?webcallout&webcalloutno=${ordPersonContact.mobile!''}">[呼叫]</a></td>
	             </#if>
	        </tr>
	        <tr>
	            <td class="e_label">电子邮件：</td>
	            <td>${ordPersonContact.email!''}</td>
	                 
	            <#if alreadySend==true>
	                  <td>           
	                       <a  id="sendEmail"  data0="${orderItemId}"  data1="${orderId}"  data2="${email}">[重发邮件]</a>
	                  </td>
	            </#if>
	        </tr>
	        <tr>
	            <td class="e_label"><!-- 是否VIP会员 -->会员类型：</td>
	            <!-- <td>
	            	<#if isCsVip==true>是
	            	<#else>否
	            	</#if>
	            </td> -->
           		<td>
					${userTypeStr!''}
	            </td>
	        </tr>
			<tr>
				<td class="e_label">是否为超级会员：</td>
				<td>
					<#if userSuperVip == true>
                        <span>是</span>
					<#else>
						<span>否</span>
					</#if>
				</td>
			</tr>
	        <#if hasPlayOut?? && hasPlayOut==true>
	         <tr>
	        	<td class="e_label">境外手机：</td>
	            <td>${(ordPersonContact.outboundPhone)!''}</td>
	         </tr>
	       </#if>
        </#if>
        
        
    </tbody>
</table>
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>
<script type="text/javascript">

	//重发邮件
	$("#sendEmail").bind("click",function(){
		var orderItemId = $(this).attr("data0");
		var orderId = $(this).attr("data1");
		var email = $(this).attr("data2");
		var param = "orderId=" + orderId + "&orderItemId=" + orderItemId + "&email=" + email;
		sendEmailDialog = new xDialog("/vst_order/order/orderManage/showResendEmail.do?" + param,{},{title:"重发邮件",width:700,height:350,iframe:true});
	});
 var updatePersonDialog;
$("#personUpdate").bind("click",function(){
	var ordPersonId;
	if(${(ordPersonContact.ordPersonId)??})
	{
		ordPersonId=${ordPersonContact.ordPersonId};
	}else{
		alert("没有联系人无法修改");
	}
	updatePersonDialog = new xDialog("/vst_order/order/orderManage/showUpdatePerson.do",{"orderId":${order.orderId!''},"ordPersonId":ordPersonId},{title:"修改联系人",width:400});
});  
     
       var mobile="";
       
      $(function () {
	   
	   if(${(ordPersonContact.ordPersonId)??} && ${(ordPersonContact.mobile)??})
		{
			mobile = ${ordPersonContact.mobile};
		}
	 });	
	

 </script>
 
 <script src="/vst_order/js/order/orderSendSms.js"></script>
 
 
 
