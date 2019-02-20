<#import "/base/pagination.ftl" as pagination>
<#if resultPage?? >
	<#if resultPage.items?size gt 0 >
			<table class="pg_d_table table_center">
			    <thead>
			        <tr>
			            <th>员工工号</th>
			            <th>姓名</th>
			            <th>部门/组</th>
			            <th>操作</th>
			        </tr>
			    </thead>
			    <tbody>
			    	<#list resultPage.items as result>
			        <tr>
			            <td>${result.userName!''}</td>
			            <td>${result.realName!''}</td>
			            <td>${result.departmentName!''}</td>
			            <td><a href="javascript:void(0);" onclick="selectUser('${result.userName}');">选择</a></td>
			        </tr>
			        </#list>
			    </tbody>
			</table>
			<#--分页标签-->
			<@pagination.paging resultPage true "#employeeListDiv"/>
	<#else>
			<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关人员，请重新输入相关条件查询！</div>
	</#if>
</#if>
<script>
	function selectUser(userName){
		$("#operatorName").val(userName);
		if($("#newoperatorName").length>0){
			$("#newoperatorName").val(userName);
		}
		$.ajax({
			url : "/vst_order/ord/order/queryEmployeeWorkStatus.do",
			type : "post",
			dataType : 'json',
			data : {"operatorName":userName},
			success : function(result) {
				if(!result.success){
			 		$.alert(result.message);
			 	}else{
			 		$("#workStatusSpan").html(result.attributes.workstatus);
			 	}
				//queryWorkBench();
				showSelectEmployeeDialog.close();
			}
		});
	}
</script>