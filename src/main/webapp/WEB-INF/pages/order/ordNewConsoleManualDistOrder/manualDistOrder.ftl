<#import "/base/spring.ftl" as spring/>

<form method="post" action='/vst_order/order/NewOrderConsole/manualDistOrder.do' id="dataForm">
<div class="iframe-content">   
    <div class="dialog1">
    
    			<input type="hidden" name="auditIdStatus" value="${RequestParameters.auditIdStatus!''}">
                <input type="hidden" name="oneData" value="${RequestParameters.oneData!''}">
                <input type="hidden" name="manualDistOrder" value="false">
				 <table class="p_table form-inline">
			            <tbody>
			                <tr> 
				                <td>
									<label for="range0">输入</label>
					                <input type="radio" id="range0" name="range" value="1" checked="checked" onclick="showRange(this);">
									<input type="text" id="groupMember0" name="groupMember" value="" style="width:150px;">
									<br/>
									<label for="range1">选择</label>	
									<input type="radio" id="range1" name="range" value="2" onclick="showRange(this);">
								</td>
							</tr>
										<tr>
										   <td>						               
											 <table class="p_table form-inline" id="selectInfoId">
										            <tbody>
										                <tr>
											                <td>
												                <label>一级部门：
													                <@spring.formSingleSelect   "ordAuditConfigInfo.firstDepartment"  firstDepMap  'class="w9"   required'/>
												                </label>
											                </td>
										                 </tr>
										                 <tr>
											                <td >
												                <label>二级部门：
												                 <@spring.formSingleSelect   "ordAuditConfigInfo.secondDepartment" secondDepMap 'class="w9"   required'/>
															　	</label>
											                </td>
										                 </tr>
										                 <tr>
											                <td >
												                <label>选择组　：
																<@spring.formSingleSelect "ordAuditConfigInfo.threeDepartment" threeDepMap 'class="w9"'/>
																</label>
											                </td>
										                 </tr>
										                 <tr>
											                <td >
												                <label>选择组员：
																	<@spring.formSingleSelect "ordAuditConfigInfo.groupMember" groupMemberMap 'class="w9" disabled="disabled">'/>
																</label>
											                </td>
										                </tr>
												  </tbody>
										     </table>
										   </td>
						                </tr>
								  </tbody>
						     </table>						     
    </div> 
</div>
</form>
<p align="center">

<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="saveManDistOrderButton">分单</button>	
&nbsp;&nbsp;
<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="cancelButton">取消</button>
<div id="txt" style="color:red;display:none" align ="center">  
    请等待结果....
</div>
</p>
<script src="/vst_order/js/vst_department_util.js"></script>
<script>
$("#txt").ajaxStart(function(){
  $("#txt").css("display","block");
});
$("#txt").ajaxComplete(function(){
  $("#txt").css("display","none");
});


 

	$("#cancelButton").bind("click",function(){
	
		updateDistOrderDialog.close();
						
	});
	
	var mustDistOrderDialog;
	$("#saveManDistOrderButton").bind("click",function(){
		//验证
		if(!$("#dataForm").validate().form()){
			return;
		}
		
		$("#saveManDistOrderButton").attr('disabled',true);
		var groupMember;
		var g11 = $("#groupMember0").attr("disabled");
		var g22 = $("#groupMember").attr("disabled");
		if(g11 == 'disabled'){
			groupMember = $("#groupMember").val();
		}else{
			groupMember = $("#groupMember0").val();
		}
		$.ajax({
		   url : "/vst_order/order/NewOrderConsole/manualDistOrder.do",
		   data : $("#dataForm").serialize(),
		   dataType:"JSON",
		   type:"POST",
		   success : function(result){
		   		if(result.code=="success"){
		   			$.alert(result.message);
		   			$("#saveManDistOrderButton").attr('disabled',false);
		
		   		}else {
		   			
		   			$("#saveManDistOrderButton").attr('disabled',false);
		   			//alert(groupMember);
		   			//$.alert(result.message);
		   			if(groupMember!='')
		   			{
		   				//$.alert(result.message);
		   				var param={"groupMember":groupMember,"auditIdStatus":"${RequestParameters.auditIdStatus!''}","oneData":"${RequestParameters.oneData!''}","resultMessage":result.message};
		   				//var param="groupMember="+groupMember;
		   				mustDistOrderDialog= new xDialog("/vst_order/order/NewOrderConsole/showMustDistOrder.do",param,{title:"强制分单",width:600});
		   			}else{
		   				$.alert(result.message);
		   			}
		   			
		   			
		   		}
		   }
		});						
	});
	
	
	var add_OrdFunctionDialog;
	function openFunction(){
		
		add_OrdFunctionDialog = new xDialog("/vst_order/order/ordFunction/findSelectOrdFunctionList.do",{},{title:"选择功能",iframe:true,width:"700",height:"400"});
		
	}
	function onOrdFunction(params){
		if(params!=null){
			$("#ordFunctionId").val(params.ordFunctionId);
			$("#ordFunctionName").val(params.ordFunctionName);
			
		}
		add_OrdFunctionDialog.close();
	}
	function showRange(obj){
		var range = $(obj).val();
		var groupMember0 = $("#groupMember0");
		var firstDepartment = $("#firstDepartment");
		var secondDepartment = $("#secondDepartment");
		var threeDepartment = $("#threeDepartment");
		var groupMember1 = $("#groupMember");		
		if(range=="2"){
			groupMember0.attr("disabled",true);
			firstDepartment.attr("disabled",false);
			secondDepartment.attr("disabled",false);
			threeDepartment.attr("disabled",false);
			groupMember1.attr("disabled",false);
		}else{
			groupMember0.attr("disabled",false);
			firstDepartment.attr("disabled",true);
			secondDepartment.attr("disabled",true);
			threeDepartment.attr("disabled",true);
			groupMember1.attr("disabled",true);
		}
	}
	$(function(){
		var groupMember0 = $("#groupMember0");
		showRange(groupMember0[0]);
	});	
</script>