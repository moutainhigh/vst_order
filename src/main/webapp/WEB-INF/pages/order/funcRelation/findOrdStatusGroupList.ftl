<#import "/base/pagination.ftl" as pagination>

<!DOCTYPE html>
<html>
<head>

<#include "/base/head_meta.ftl"/>
</head>
<body>	
<!-- 主要内容显示区域\\ -->
<div class="iframe-content">   
    <div class="p_box">
	<table class="p_table table_center">
                <thead>
                    <tr>
					 <th>选择</th>
				
                   
                    <th>id</th>
                	<th>订单状态fields</th>
                    
                    </tr>
                </thead>
                <tbody>
					<#list ordStatusGroupList as ordStatusGroup> 
					<tr>
					<td>
						<input type="radio" name="ordStatusGroupRadio">
						<input type="hidden" name="statusGroupId" value="${ordStatusGroup.statusGroupId!''}">
						<input type="hidden" name="fileds" value="${ordStatusGroup.fileds!''}">
					 </td>
					<td>${ordStatusGroup.statusGroupId!''} </td>
					<td>${ordStatusGroup.fileds!''} </td>
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
	
	
	$("input[type='radio']").bind("click",function(){
		var obj = $(this).parent("td");
		var ordStatusGroup = {};
		ordStatusGroup.statusGroupId = $("input[name='statusGroupId']",obj).val();
	    ordStatusGroup.fileds = $("input[name='fileds']",obj).val();
		parent.onSelectStatusGroupId(ordStatusGroup);
	});
	
</script>


