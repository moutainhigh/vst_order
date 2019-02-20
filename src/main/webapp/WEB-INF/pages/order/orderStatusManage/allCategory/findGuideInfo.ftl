<#--页眉-->
<#if flag?? && flag=='Y'>
<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
<div id="logResultList" class="divClass">
    <div class="order_msg clearfix" style="margin-top: 15px;">
        <strong>
           导游信息
        </strong>
    </div>
 <table class="p_table table_center mt20">
                <thead>
                    <tr>
                    	<th>类型</th>
                        <th>姓名</th>
                        <th>手机号码</th>
                        <th>导游证号</th>
                        <th>身份证号</th>
                    </tr>
                </thead>
                <tbody>
                <#if personList?? && personList?size&gt;0>
                <#list personList as person>
                    <tr>
                    	 <td>导游</td>
                        <td>${person.fullName!''}</td>
                        <td>${(person.mobile)!''}</td>
                        <td> ${person.guideCertificate!''}</td>
                        <td> ${person.idNo!''}</td>
                    </tr>
                </#list>
                </#if>
                </tbody>
            </table>
</div>
</body>
</html>
</#if>