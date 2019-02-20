<!DOCTYPE html>
<html>
<head>
<#include "/base/head_meta.ftl"/>
</head>
<body>
<!-- 主要内容显示区域\\ -->
<div class="iframe_content">
	<#if pageParam.items?? &&  pageParam.items?size &gt; 0 >
    <div class="p_box">
		<table class="p_table table_center">
            <thead>
            <tr>
            	 <th>操作员</th>
                 <th>日志名称</th>
      			 <th>日志内容</th>
      			 <th>产生时间</th>
      			 <th>备注</th>
            </tr>
            </thead>
            <tbody>
            <#list pageParam.items as item> 
      			<tr>
      				 <td>${item.operatorName}</td>
       				 <td>${item.logName}</td>
        			 <td>${item.content}</td>
        			 <td>${item.createTime?string("yyyy-MM-dd HH:mm:ss")}</td>
        			 <td>${item.memo}</td>
      		</tr>
      	</#list>
         </tbody>
        </table>
 
	    <#if pageParam.items?exists>
	        <div class="paging">
	        ${pageParam.getPagination()}
	        </div>
	    </#if>
    </div>
    <!-- div p_box -->
    <#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无日志  ！</div>
	</#if>
</div>
<!-- //主要内容显示区域 -->

</body>
</html>
