<#--页眉-->
<#import "/base/spring.ftl" as spring/>
<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
<div id="logResultList" class="divClass">
<table>
                        <tbody>
                            <tr>
                                <td class="e_label w10">下单时间：</td>
                                <td>${order.createTime?string('yyyy-MM-dd HH:mm') !''} </td>
                            </tr>
                            <tr>
                                <td class="e_label">订单来源：</td>
                                <td>${distributorName !''} 
                                
                                  <#if order.distributorId=="2" > 
	                                <strong>[${order.backUserId!''}]
	                                </strong>
                                 <#else>
				                  	 
				                  </#if>
                                
                                </td>
                            </tr>
                            <tr>
                                <td class="e_label">付款方式：</td>
                                <td>
                                <#if order.paymentTarget=="PREPAID" > 
                            	 预付
				                  <#else>
				                  	 现付
				                  </#if>
				                  
                                </td>
                            </tr>
                           
                            <tr>
                                <td class="e_label">退改政策：</td>
                                <td>
                                 ${cancelStrategyTypeStr}
                                </td>
                            </tr>
                            
                            <tr>
                                <td class="e_label">所属公司：</td>
                                <td>${order.zhFilialeName !''}</td>
                            </tr>
                            <tr>
                                <td class="e_label">所属产品经理：</td>
                                <td>${productManager.realName!''}</td>
                            </tr>
                        </tbody>
                    </table>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>
<script type="text/javascript">

         
 </script>
