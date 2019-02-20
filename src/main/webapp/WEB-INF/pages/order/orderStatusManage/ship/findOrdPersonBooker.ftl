<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
<div id="logResultList" class="divClass"><table >
		                <tbody>
		                    <tr>
		                        <td class="e_label">登录用户：</td>
		                        <td>${ordPersonBooker.fullName!''}</td>
		                    </tr>
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
		                    </tr>
		                    <tr>
		                        <td class="e_label">联系人手机：</td>
		                        <td>${ordPersonContact.mobile!''}</td>
		                    </tr>
		                    <tr>
		                        <td class="e_label">电子邮件：</td>
		                        <td>${ordPersonContact.email!''}</td>
		                        <#--
		                        <td><a href="javaScript:">[邮件列表]++</a></td>
		                        -->
		                    </tr>
		                    <tr>
				            	<td class="e_label">会员类型：</td>
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
		                </tbody>
		            </table>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>
<script type="text/javascript">

var updatePersonDialog;
        $("#personUpdate").bind("click",function(){
			updatePersonDialog = new xDialog("/vst_order/order/orderShipManage/showUpdatePerson.do",{"ordPersonId":${ordPersonContact.ordPersonId}},{title:"修改联系人",width:400});
		});  
         
          //定义发送短信弹出窗口变量
    var orderSendSmsDialog;
    
    
     $(function () {
	//绑定发送短信事件
	$("#orderSendSms").bind("click",function(){
		orderSendSmsDialog = new xDialog("/vst_order/ord/order/intoSendSmsPage.do",{"orderId":${order.orderId},"mobile":${ordPersonContact.mobile}},{title:"发送短信",width:600});
     });
 	});

    
 </script>
