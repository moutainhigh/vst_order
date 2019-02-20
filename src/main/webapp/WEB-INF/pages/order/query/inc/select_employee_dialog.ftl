<div style="height:300px">
    <div class="box_content p_line">
    	<form id="employeeForm">
	         <table class="e_table form-inline ">
	            <tbody>
	                <tr>
	                	<td class="w6 s_label">
	                		员工工号
	                	</td>
	                    <td class="w15">
	                        <input type="text" value="" name="searchOperatorName" id="searchOperatorName" placeholder="员工工号"><div id="errorDiv" class="e_error" style="display:none"><i class="e_icon icon-error"></i><span>错误提示</span></div>
	                    </td>
	                    <td>
	                		<div class="fl operate">&nbsp;&nbsp;&nbsp;<a class="btn btn_cc1" href="javascript:searchUser()">查询</a></div>
	                	</td>
	                </tr>
	            </tbody>
	         </table>
         </form>
    </div>
    <div class="box_content">
        <div class="iframe_content pd0">
            <div id="employeeListDiv">
            </div>
        </div>
    </div>
</div>
<script>
	function searchUser(){
		var searchOperatorName = $("input[name='searchOperatorName']").val();
		if($.trim(searchOperatorName)==''){
			$("#errorDiv").find("span").html("请输入员工工号");
			$("#errorDiv").show();
			return;
		}
		$("#errorDiv").find("span").html("");
		$("#errorDiv").hide();
		$.post("/vst_order/ord/order/queryEmployeeList.do",{"operatorName":searchOperatorName},function(data){
			$("#employeeListDiv").html(data);
		});
	}
</script>