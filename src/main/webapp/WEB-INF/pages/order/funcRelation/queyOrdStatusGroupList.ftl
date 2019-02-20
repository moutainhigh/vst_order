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
            <li class="active">功能展示列表</li>
        </ul>
</div>


<div class="iframe_search">
<form method="post" action='/vst_order/order/ordStatusGroup/queyOrdStatusGroupList.do' id="searchForm">
        <table class="s_table">
            <tbody>
                <tr>
                
                    <td class="s_label">订单状态fields：</td>
                    <td class="w18"><input type="text" name="fileds" value="${ordStatusGroup.fileds!''}"></td>
                   <td class=" operate mt10"><a class="btn btn_cc1" id="search_button">查询</a></td>
                    <td class=" operate mt10"><a class="btn btn_cc1" id="add_button">新增</a></td>
                    
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
				
                   
                    <th>id</th>
                	<th>订单状态fields</th>
                    <th>操作</th>
                    </tr>
                </thead>
                <tbody>
					<#list ordStatusGroupList as ordStatusGroup> 
					<tr>
					<td>${ordStatusGroup.statusGroupId!''} </td>
					<td>${ordStatusGroup.fileds!''} </td>
					<td><a href="javascript:void(0);" class="editProp" data=${ordStatusGroup.statusGroupId}  data1=${ordStatusGroup.fileds}>编辑</a></td>
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
	var addOrdStatusGroupDialog,updateOrdStatusGroupDialog,ordStatusGroupDialog;
	var queryOrdStatuGroupDialog=window;
	//查询
	$("#search_button").click(function(){
		$("#searchForm").submit();
	});
	
	$("#add_button").click(function(){
		addOrdStatusGroupDialog = new xDialog("/vst_order/order/ordStatusGroup/showAddordStatusGroup.do",{},{title:"新增订单状态",width:800});
	});
	//编辑
	$("a.editProp").bind("click",function(){
		var statusGroupId=$(this).attr("data");
		var fileds=$(this).attr("data1");
		updateOrdStatusGroupDialog = new xDialog("/vst_order/order/ordStatusGroup/showUpdateOrdStatusGroup.do",{"statusGroupId":statusGroupId,"fileds":fileds},{title:"修改订单状态",width:800});
	});
	
	
	//alert(queryOrdStatuGroupDialog.location.href);
	function nowPageReload(){
		window.location.reload();
	
	}
</script>
