<#import "/base/pagination.ftl" as pagination>

<!DOCTYPE html>
<html>
<head>

<#include "/base/head_meta.ftl"/>
</head>
<body>
<div class="iframe_header">
        <ul class="iframe_nav">
            <li><a href="#">首页</a> &gt;</li>
            <li><a href="#">功能管理</a> &gt;</li>
            <li class="active">订单状态与业务功能权限配置展示列表</li>
        </ul>
</div>


<div class="iframe_search">
<form method="post" action='/vst_order/order/ordFunction/findOrdFunctionList.do' id="searchForm">
        <table class="s_table">
            <tbody>
                <tr>
                <td class="s_label">功能编码：</td>
                    <td class="w18"><input type="text" name="functionCode" value="${ordFunction.functionCode!''}"></td>
                   
                    <td class="s_label">功能名称：</td>
                    <td class="w18"><input type="text" name="functionName" value="${ordFunction.functionName!''}"></td>
                   
                    
                    <td class=" operate mt10"><a class="btn btn_cc1" id="search_button">查询</a></td>
                    
                </tr>
            </tbody>
        </table>	
		</form>
    </div>
<!-- 主要内容显示区域\\ -->
<div class="iframe-content">   
    <div class="p_box">
	<table class="p_table table_center">
                <thead>
                    <tr>
                   <th> 功能编码</th>
                   <th>功能名字</th>
                    <th>操作</th>
                    </tr>
                </thead>
                <tbody>
					<#list pageParam.items as ordFunction> 
					<tr>
					<td>${ordFunction.functionCode!''} </td>
					<td>${ordFunction.functionName!''} </td>
					<td><a href="javascript:void(0);" class="editProp" data=${ordFunction.ordFunctionId},${ordFunction.functionName}>编辑配置关系</a> 
					 <a href="javascript:;" class="editCateGroup" data=${ordFunction.ordFunctionId},${ordFunction.functionName}>新增配置关系</a></td>
					</tr>
					</#list>
                </tbody>
            </table>
			<@pagination.paging pageParam/>
	</div><!-- div p_box -->
</div><!-- //主要内容显示区域 -->
<#include "/base/foot.ftl"/>
</body>
</html>
<script>
	var addOrdFuncRelationDialog,updateordFuncRelationDialog,ordStatusGroupDialog;
	//查询
	$("#search_button").click(function(){
		$("#searchForm").submit();
	});
	
	
	//编辑配置关系
	$("a.editProp").bind("click",function(){
		var dataArr=$(this).attr("data").split(",");
	    var ordFunctionId  = dataArr[0];
	    var functionName  = dataArr[1];
		updateordFuncRelationDialog = new xDialog("/vst_order/order/ordFuncRelation/showUpdateOrdFuncRelation.do",{"ordFunctionId":ordFunctionId,"functionName":functionName},{title:"修改订单状态与业务功能权限配置",width:800});
	});
	

	$("a.editCateGroup").bind("click",function(){
	   var dataArr=$(this).attr("data").split(",");
		
	    var ordFunctionId  = dataArr[0];
	    var functionName  = dataArr[1];
		addOrdFuncRelationDialog = new xDialog("/vst_order/order/ordFuncRelation/showAddOrdFuncRelation.do",{"ordFunctionId":ordFunctionId,"functionName":functionName},{title:"新增订单状态与业务功能权限配置",width:800});
		
	});

</script>
