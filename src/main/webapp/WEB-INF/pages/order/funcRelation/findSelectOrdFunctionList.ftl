<#import "/base/pagination.ftl" as pagination>

<!DOCTYPE html>
<html>
<head>

<#include "/base/head_meta.ftl"/>
</head>
<body>

<div class="iframe_search">
<form method="post" action='/vst_order/order/ordFunction/findSelectOrdFunctionList.do' id="searchForm">
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
                     <th>选择</th>
                   <th> 功能编码</th>
                   <th>功能名字</th>
                   
                    </tr>
                </thead>
                <tbody>
					<#list pageParam.items as ordFunction> 
					<tr>
					<td>
					<input type="radio" name="ordFunctionIdRadio">
					<input type="hidden" name="ordFunctionId" value="${ordFunction.ordFunctionId!''}">
					<input type="hidden" name="ordFunctionName" value="${ordFunction.functionName!''}">
					
					
						
					</td>
					<td>${ordFunction.functionCode!''} </td>
					<td>${ordFunction.functionName!''} </td>
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
	//查询
	$("#search_button").click(function(){
		$("#searchForm").submit();
	});
	
	$("input[type='radio']").bind("click",function(){
		var obj = $(this).parent("td");
		var ordFunction = {};
		ordFunction.ordFunctionId = $("input[name='ordFunctionId']",obj).val();
	    ordFunction.ordFunctionName = $("input[name='ordFunctionName']",obj).val();
		parent.onOrdFunction(ordFunction);
	});

</script>
