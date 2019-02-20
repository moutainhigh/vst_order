<!DOCTYPE html>
<html>
	<head>
		<#include "/base/head_meta.ftl"/>
	</head>
	<body>
		<div class="p_box">
			<table class="p_table table_center">
			    <thead>
			    	<tr>
				        <th>类型</th>
				        <th>内容</th>
			        </tr>
			    </thead>
			    <tbody>
						    <tr>
						       <td>分销商名称</td>
						    	<td>${tntUserInfoVo.companyName!''}</td>
							</tr>	
							<tr>
                               <td>用户名</td>
                                <td>${tntUserInfoVo.userName!''}</td>
                            </tr>   
                            <tr>
                               <td>手机</td>
                                <td>${tntUserInfoVo.mobilePhone!''}</td>
                            </tr>   
                            <tr>
                               <td>邮箱</td>
                                <td>${tntUserInfoVo.email!''}</td>
                            </tr>   	
                            <tr>
                                <td colspan ="2" align="center">
                                <div id="closeButton"  style="text-align:center; "><a class="btn btn_cc1">取消</a></div>
                                </td>       
                            </tr>        			
				</tbody>
			</table>
		</div>
	<#include "/base/foot.ftl"/>
	</body>
</html>
<script>

$("#closeButton").bind("click", function() {
    window.parent.showDistributorDialog.close();
});
</script>