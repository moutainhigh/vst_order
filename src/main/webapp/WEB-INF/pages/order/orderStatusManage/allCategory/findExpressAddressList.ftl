<#--页眉-->
<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单处理</title>
</head>
<body>
<#--页面导航-->
<#if haveExpress=='true' >
<div id="logResultList" class="divClass">
<div class="order_msg clearfix">
                	</br>
                	
                	<strong>
                	  快递地址
                	   <#if 'parent' == RequestParameters.orderType && order.orderStatus=='NORMAL' >
                	    (<a class="btn btn_cc1" id="editOrdAddressButton" href="javaScript:" >修改</a>)
                	  </#if>
                	  </strong>
                	
                	
</div>
 <table class="p_table table_center mt20">
                <thead>
                 
                    <tr>
                        <th>姓名</th>
                        <th>手机号</th>
                        <th>联系地址</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>
                     	${addressPerson.fullName!''}
                        </td>
                         <td>
                     	${addressPerson.mobile!''}
                        </td>
                        <td>
                        ${ordAddress.province}${ordAddress.city}${ordAddress.district}${ordAddress.street}${ordAddress.postalCode}
						</td>
                    </tr>
                </tbody>
</table>
</div>

<#--页脚-->
</body>
</html>
<script type="text/javascript">
		var editOrdAddressButtonDialog;
		$("#editOrdAddressButton").bind("click",function(){
			if(${order??} && ${addressPerson??} && ${ordAddress??})
			{
				var param={"orderId":${order.orderId!''},"ordPersonId":${addressPerson.ordPersonId!''},"ordAddressId":${ordAddress.ordAddressId!''},"orderType":"parent"};
				editOrdAddressButtonDialog = new xDialog("/vst_order/order/orderManage/showUpdateOrdAddress.do",param,{title:"修改快递地址",width:800});
		 
			}
			
		 });
         
         
 </script>
 </#if>
