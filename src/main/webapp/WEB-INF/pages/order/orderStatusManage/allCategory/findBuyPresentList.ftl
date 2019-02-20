<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#if buyPresentCouponVos??>
<#--页面导航-->
<div id="logResultList" class="divClass">
	<div class="order_msg clearfix">
                	</br>
                	<strong>
                	赠品
                	 </strong>
	</div>
 	<table class="p_table table_center mt20">
		<thead>
		    <tr>
		    	<th>赠品信息</th>
		    	<th>份数</th>
		        <th>有效期</th>
		        <th>预计发放日期</th>
		    </tr>
		</thead>
		
		<tbody>
			<#list buyPresentCouponVos as buyPresent> 
			<tr>
				<td>
                        ${buyPresent.name!''}
                </td>
                 <td>
                      ${buyPresent.quantity!''}
				</td>
                <td>
                		${(buyPresent.beginValidDate?string("yyyy.MM.dd"))!} - ${(buyPresent.endValidDate?string("yyyy.MM.dd"))!}
                </td>
               <td>
                       ${buyPresent.sendDate}
                </td>
			</tr>
            </#list>
        </tbody>
    </table>
</div>
<#--页脚-->
</#if>
</body>
</html>
