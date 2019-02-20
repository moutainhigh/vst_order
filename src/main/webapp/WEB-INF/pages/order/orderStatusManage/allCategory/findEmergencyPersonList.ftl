<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
 <#if emergencyPersonList?size!=0 >
<div id="logResultList" class="divClass">
 <div class="order_msg clearfix">
                	</br>
                	
                	<strong>
                	  紧急联系人
                	   <#if 'parent' == RequestParameters.orderType && emergencyPersonList?? && emergencyPersonList?size!=0 && order.orderStatus=='NORMAL' >
                	    (<a class="btn btn_cc1" id="editEmergencyPersonButton" href="javaScript:" >修改</a>)
                	  </#if>
                	  </strong>
                	  <#--
                	   <#if 'parent' == RequestParameters.orderType>
                	    <p align="right">
							<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="editEmergencyPersonButton">修改</button>
						</p>
						
					</#if>
					-->
                	
</div>

                    
             <table class="p_table table_center mt20">
            	
                <thead>
                    <tr>
                        <th>中文姓名</th>
                        <th>联系手机</th>
                    </tr>
                </thead>
                <tbody>
                    
                <#list emergencyPersonList as person> 
                    <tr>
                        <td>
                     	 ${person.fullName!''}
                        </td>
                         <td>${person.mobile!''} </td>
                    </tr>
                </#list>
                </tbody>
            </table>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>

</#if>
</body>
</html>
<script type="text/javascript">
    
      	var editEmergencyPersonButtonDialog;
		$("#editEmergencyPersonButton").bind("click",function(){
		
			var param={"orderId":${RequestParameters.orderId!''},"orderType":"parent"};
			editEmergencyPersonButtonDialog = new xDialog("/vst_order/order/orderManage/showUpdateEmergencyPerson.do",param,{title:"修改紧急联系人",width:500});
		 
		 });
         
         
 </script>
