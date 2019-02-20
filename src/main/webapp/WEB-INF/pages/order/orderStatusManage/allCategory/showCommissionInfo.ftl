<!DOCTYPE html>
<html>
    <head>
    <#include "/base/head_meta.ftl"/>
    </head>
    <body>
        <div class="p_box">
            <table  class="p_table table_center mt20">
                <thead>
                    <tr>
                        <th>日期</th>
                        <th>佣金比例</th>
                        <th>佣金金额</th>
                    </tr>
                </thead>
                <tbody>
                    <#if ordOrderCommissions?? && ordOrderCommissions?size&gt;0>
                    <#assign index = 0>
                        <#list ordOrderCommissions as ordOrderCommission>
                            <tr>
                                <td>${ordOrderCommission.visitTime?string("yyyy-MM-dd")}</td>
                                <td>${(ordOrderCommission.commissionProportion!'')/100}%</td>
                                <td>${(ordOrderCommission.commissionAmount!'')/100}</td>
                            </tr>
                            <#assign index = index+1>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </div>
    <#include "/base/foot.ftl"/>
    </body>
</html>



