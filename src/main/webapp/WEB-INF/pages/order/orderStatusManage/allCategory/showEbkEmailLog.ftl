<#import "/base/spring.ftl" as s/>
 
<table  class="p_table table_center mt20">
    <thead>
        <tr> 
            <th>操作账号</th>
		    <th>发送时间</th>
		    <!--
		    <th>邮件内容</th>
		    -->
        </tr>
    </thead>
    <tbody>
        <#if bizLogPage.itemList?? && bizLogPage.itemList?size &gt; 0>
			<#list bizLogPage.itemList as log>
				<tr>
                    <td>${log.operatorName!''}</td>
                    <td>${log.createTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                    <!--
                    <td>
                        <#if (log.content)??>
                            ${(log.content)?html}
                        </#if>
                    </td>
                    -->
				</tr>
			</#list>
		<#else>
            <div class="no_data mt20"><i class="icon-warn32"></i>暂无操作日志  ！</div>
        </#if>

    </tbody>
</table>





