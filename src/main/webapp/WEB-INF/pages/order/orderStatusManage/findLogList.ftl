<#--页眉-->
<#import "/base/spring.ftl" as spring/>
<#include "/base/head_meta.ftl"/>
<#import "/base/pagination.ftl" as pagination>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-查看日志</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>
<#--页面导航-->
<div id="logResultList" class="divClass">

 <table class="p_table table_center"  style="word-break:break-all; word-wrap:break-all;">
    <thead>
        <tr>
              <TR>
				<th style="width:10%;" nowrap="nowrap">操作员</th>
				<th style="width:10%;" nowrap="nowrap">操作日期</th>
				<th style="width:30%;" nowrap="nowrap">系统说明</th>
				<th style="width:30%; word-break: break-all;">备注</th>
				<th style="width:20%;" nowrap="nowrap">相关操作</th>
			</TR>
        </tr>
    </thead>
    <tbody>
     
    	<#list logList  as log>
		    <TR>
			<TD>${log.operatorName!''}</TD>
			<TD>${log.createTime?string('yyyy-MM-dd HH:mm:ss')} </TD>
			<TD style="text-align:left;">${log.content!''}</TD>
			<TD style="text-align:left;">${log.content!''}</TD>
			<TD style="text-align:left;">${log.memo!''}</TD>
			</TR>
	   </#list>
   
                
              </tbody>
   <TR>
  <TD COLSPAN ="6">
   <@pagination.paging pageParam true "#logResultList"/>
  <#--分页标签
  <@pagination.paging pageParam>
 </@pagination.paging>
   
  -->

  </TD>
  </TR>
  <TR>
  <TD style="border-left:0;border-right:0;border-bottom:0;" COLSPAN ="6">
  	 <a class="btn btn_cc1" id="closeLog" >关闭</a>
  	
  </TD>
  </TR>
</table>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>
<script type="text/javascript">

         $("#closeLog").bind("click",function(){
         
         	showLogDialog.close();
         	/**
         	$("#logResult").css("display","none");
         	
         	$('html, body, .content').animate({scrollTop: 0}, 300);
         	*/ 
         });
 </script>
