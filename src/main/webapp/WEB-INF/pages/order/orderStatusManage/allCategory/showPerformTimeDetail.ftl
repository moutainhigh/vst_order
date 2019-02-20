<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
<div id="performTimeResultList" class="divClass">
         <table class="p_table table_center mt20">
                <thead>
                    <tr>
                        <th>操作账号</th>
                        <th>通关时间</th>
                        <th>使用状态</th>
                        <th>使用份数</th>
                    </tr>
                </thead>
                <tbody>
           <#if performTimeInfoList?? && performTimeInfoList?size &gt; 0>
                    <#list performTimeInfoList as performTimeInfo> 
                        <tr>
                            <td>${performTimeInfo.operator!''}</td>
                            <td>${performTimeInfo.performTime!''}</td>
                            <td>${performTimeInfo.performStatus!''} </td>
                            <td>${performTimeInfo.performNumber!''} ( ${performTimeInfo.adultNumber!''}成人,${performTimeInfo.childNumber!''}儿童)</td>
                        </tr>
                  </#list>
       </#if>
     </tbody>
            </table> 
</div>
</body>
</html>
