<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#if stampCodeList??&&stampCodeList?size!=0>
<div id="findPreSaleList" class="divClass">
<div class="order_msg clearfix">
</br>
                	<strong>
                	预售券码信息
                	  </strong>
                	</div>
 <table class="p_table table_center mt20">
                <thead>
                 
                    <tr>
                        <th>券ID</th>
                        <th>劵码</th>
                        <th>使用状态</th>
                        <th>兑换订单号</th>
                    </tr>
                </thead>
                <tbody>
                    
                <#list stampCodeList as stampCode> 
                    <tr>
                        <td>
                          ${stampNo!''}
                        </td>
                        <td>
                      ${stampCode.serialNumber!''}
                        </td>
                        <td>
                        ${stampCode.stampStatus!''}
						</td>
						<td>
						<#if stampCode.useOrderHis??&&stampCode.useOrderHis?size!=0>
						<#list stampCode.useOrderHis as useOrder>
						${useOrder.useOrderId!''} (${useOrder.unbindStatus!''}) <br/>
						</#list>
						</#if>
						</td>
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

