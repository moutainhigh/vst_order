<div>
    <div class="box_content p_line">
    	<form id="updateDelayRemindTimeForm" action="/vst_order/ord/order/confirm/updateDelayRemindTime.do">
    		<input type="hidden" name="auditId" value="${auditId}" >
	         <table class="e_table form-inline ">
	            <tbody>
	                <tr>
	                	<td class="w8 s_label">
	                		预约时间：
	                	</td>
	                    <td class="w20">
	                         <input class="Wdate" type="text" value="${remindTime!''}" 
                            onfocus="WdatePicker({readOnly:true,minDate:'%y-%M-%d %H:%m:%s',dateFmt:'yyyy-MM-dd HH:mm:ss'})" errorele="selectDate" name="remindTimeStr" readonly="readonly">
	                    </td>
	                </tr>
	                <tr>
	                	<td></td>
		                <td>
	                		<div class="fl operate" style="text-align: center;">
	                			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                			<a class="btn btn_cc1" href="javascript:updateDelayRemindTime()">保存</a>
	                		</div>
		                </td>
	                </tr>
	            </tbody>
	         </table>
         </form>
    </div>
</div>
<script type="text/javascript">
	function updateDelayRemindTime(auditId, remindTimeStr){
		var auditId = $("input[name='auditId']").val();
		var remindTimeStr = $("input[name='remindTimeStr']").val();
		if($.trim(auditId)==''){
			$.alert("审核ID不能为空");
			return;
		}
		if($.trim(remindTimeStr)==''){
			$.alert("预约时间不能为空");
			return;
		}
		//遮罩层
		var loading = pandora.loading("正在努力保存中...");
		var formData="auditId="+auditId+"&remindTimeStr="+remindTimeStr;
		$.ajax({
			   url : "/vst_order/ord/order/confirm/updateDelayRemindTime.do",
			   data : formData,
			   type:"POST",
			   dataType:"JSON",
			   success : function(result){
					if(result.code=="success" ){
						loading.close();
					  	alert(result.message);
					  	setInterval(function(){queryDestTaskList()},1000);
					}else {
						loading.close();
						alert(result.message);
						setInterval(function(){queryDestTaskList()},1000);
					}
			   },
			   error: function(XMLHttpRequest, textStatus, errorThrown) {
				   loading.close();
				   if(textStatus=='timeout'){
				　　　　　   alert("程序运行超时，3秒后自动刷新页面");
					    setInterval(function(){queryDestTaskList()},3000);
				　　　}else{
						alert("程序运行出现异常，3秒后自动刷新页面");
						setInterval(function(){queryDestTaskList()},3000);
				　　　}
				}
		});
		showDelayRemindTimeDialog.close();
	}
</script>