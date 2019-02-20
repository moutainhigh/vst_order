<div>
    <div class="box_content p_line">
    	<form id="editAuditSortRuleForm" action="/vst_order/ord/order/confirm/editAuditSortRule.do">
    		<input type="hidden" name="sortRuleId" ="${auditSortRule.sortRuleName}" 
    				value="${auditSortRule.sortRuleId!''}">
	         <table class="e_table form-inline ">
	            <tbody>
	                <tr>
	                	<td class="w6 s_label">
	                		分组名称：
	                	</td>
	                    <td class="w15">
	                        <input type="text" name="sortRuleName" placeholder="分组名称" 
	                        		value="${auditSortRule.sortRuleName!''}">
	                    </td>
	                </tr>
					<tr>
	                	<td class="w6 s_label">
	                		到店时间：
	                	</td>
	                    <td class="w15">
	                    	<select name="arriveType">
								<#--<option value="ARRIVE_IMMEDIATELY" <#if auditSortRule.arriveType == 'ARRIVE_IMMEDIATELY'>selected="selected"</#if>>马上到店</option>-->
								<option value="ARRIVE_TODAY" <#if auditSortRule.arriveType == 'ARRIVE_TODAY'>selected="selected"</#if>>今日到店</option>
								<option value="ARRIVE_MORROW" <#if auditSortRule.arriveType == 'ARRIVE_MORROW'>selected="selected"</#if>>次日到店</option>
								<option value="ARRIVE_MORROW_AFTER" <#if auditSortRule.arriveType == 'ARRIVE_MORROW_AFTER'>selected="selected"</#if>>次日之后到店</option>
								<option value="" <#if auditSortRule.arriveType == ''>selected="selected"</#if>>全部</option>								
							</select>								
	                    </td>
	                </tr>
	                <tr>
	                	<td class="w6 s_label">
	                		所属BU：
	                	</td>
	                    <td class="w15">
	                    	<select name="bu" id="bu">
									<option value="LOCAL_BU" <#if auditSortRule.bu == 'LOCAL_BU'>selected="selected"</#if>>国内游事业部</option>
									<option value="OUTBOUND_BU" <#if auditSortRule.bu == 'OUTBOUND_BU'>selected="selected"</#if>>出境游事业部</option>
									<option value="DESTINATION_BU" <#if auditSortRule.bu == 'DESTINATION_BU'>selected="selected"</#if>>目的地事业部</option>
									<option value="TICKET_BU" <#if auditSortRule.bu == 'TICKET_BU'>selected="selected"</#if>>景区玩乐事业群</option>
									<option value="BUSINESS_BU" <#if auditSortRule.bu == 'BUSINESS_BU'>selected="selected"</#if>>商旅事业部</option>
									<option value="O2OWUXI_BU" <#if auditSortRule.bu == 'O2OWUXI_BU'>selected="selected"</#if>>O2O无锡子公司</option>									
									<option value="O2ONINGBO_BU" <#if auditSortRule.bu == 'O2ONINGBO_BU'>selected="selected"</#if>>O2O宁波子公司</option>
									<option value="OONEWSALE_BU" <#if auditSortRule.bu == 'OONEWSALE_BU'>selected="selected"</#if>>O+O新零售</option>
									<option value="" <#if auditSortRule.bu == ''>selected="selected"</#if>>全部</option>								
							</select>
	                    </td>
	                </tr>
	                <tr>
	                	<td class="w6 s_label">
	                		产品ID：
	                		全部<input type="radio" id="objectIdRadio1" name="objectIdRadio" 
	                				value="全部" onchange="javascript:objectIdChange()">
	                		指定<input type="radio" id="objectIdRadio2" name="objectIdRadio" 
	                				value="指定" onchange="javascript:objectIdChange()">
	                	</td>
	                    <td class="w15">
	                    	<textarea class="textarea" name="objectId"
								id="objectId_area" placeholder="产品ID" rows="5" cols="30" 
								onchange="javascript:if(this.value!='')this.value=(this.value.replace(/[，]/g,',')).replace(/[^0-9,]/g,'');"
								onkeyup="checkTextareaLength('objectId')">${auditSortRule.objectId!''}</textarea><br>
							<span class="fr" id="objectIdTip" style="float:left;max-width: 500px;">0/2000字</span>		
	                    </td>
	                </tr>
	                <tr>
	                	<td class="w6 s_label">
	                		供应商ID：
	                		全部<input type="radio" id="supplierIdRadio1" name="supplierIdRadio" 
	                				value="全部" onchange="javascript:supplierIdChange()">
	                		指定<input type="radio" id="supplierIdRadio2" name="supplierIdRadio" 
	                				value="指定" onchange="javascript:supplierIdChange()">
	                	</td>
	                    <td class="w15">
	                    	<textarea class="textarea" name="supplierId"
								id="supplierId_area" placeholder="供应商ID" rows="5" cols="30"
								onchange="javascript:if(this.value!='')this.value=(this.value.replace(/[，]/g,',')).replace(/[^0-9,]/g,'');"
								onkeyup="checkTextareaLength('supplierId')">${auditSortRule.supplierId!''}</textarea><br>
							<span class="fr" id="supplierIdTip" style="float:left;max-width: 500px;">0/2000字</span>		
	                    </td>
	                </tr>
	                <tr>
	                    <td class="s_label">渠道：</td>
                    	<td>
                        	<input type="checkbox" name="orderChannel" value="neither" <#if auditSortRule.orderChannel && auditSortRule.orderChannel?index_of('neither') !=-1>checked</#if>/>主站
                        	<input type="checkbox" name="orderChannel" value="other" <#if auditSortRule.orderChannel && auditSortRule.orderChannel?index_of('other') !=-1>checked</#if>/>分销(不含淘宝)
                        	<input type="checkbox" name="orderChannel" value="taobao" <#if auditSortRule.orderChannel && auditSortRule.orderChannel?index_of('taobao') !=-1>checked</#if>/>分销(淘宝)
                    	</td>
                    </tr>
                    <tr>
                    	<td class="w8 s_label">资源审核:</td>
                    	<td class="w15">
                        	<select name="orderResourceStatus">
                        			<option value="" <#if auditSortRule.orderResourceStatus == ''>selected="selected"</#if>>全部</option>
									<option value="UNVERIFIED" <#if auditSortRule.orderResourceStatus == 'UNVERIFIED'>selected="selected"</#if>>未审核</option>
									<option value="AMPLE" <#if auditSortRule.orderResourceStatus == 'AMPLE'>selected="selected"</#if>>资源满足</option>
									<option value="LOCK" <#if auditSortRule.orderResourceStatus == 'LOCK'>selected="selected"</#if>>资源不满足</option>
							</select>
                    	</td>
                    </tr>
	                <tr>
	                	<td class="w6 s_label">
	                		是否即时：
	                	</td>
	                    <td class="w15">
		                    <select name="immediatelyFlag">
									<option value="Y" <#if auditSortRule.immediatelyFlag == 'Y'>selected="selected"</#if>>是</option>
									<option value="N" <#if auditSortRule.immediatelyFlag == 'N'>selected="selected"</#if>>否</option>
									<option value="" <#if auditSortRule.immediatelyFlag == ''>selected="selected"</#if>>全部</option>
							</select>
	                    </td>
	                </tr>
	                <tr>
	                	<td class="w6 s_label">
	                		调配时间(分钟)：
	                	</td>
	                    <td class="w15">
							<#if auditSortRule.remindTime ??>
								<input type="text" name="remindTime" placeholder="调配时间" 
	                        		value="${auditSortRule.remindTime/60}">
							<#else>
							 	<input type="text" name="remindTime" placeholder="调配时间" 
	                        		value="">		
							</#if>
	                    </td>
	                </tr>
	                <tr>
	                	<td class="w6 s_label">
	                		优先级：
	                	</td>
	                    <td class="w15">
	                        <input type="text" name="seq" placeholder="优先级" 
	                        		value="${auditSortRule.seq!''}" >
	                    </td>
	                </tr>
	                <tr>
		                <td></td>
		                <td>
	                		<div class="fl operate" style="">
	                			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                			<a class="btn btn_cc1" id="saveButton" href="javascript:saveAuditSortRule()">保存</a>
	                		</div>
		                </td>
	                </tr>
	            </tbody>
	         </table>
         </form>
    </div>
</div>
<script type="text/javascript">
	$(function(){
		//产品ID选择效果
		var objectId='${auditSortRule.objectId!''}';
		if(objectId==''){
			$("#objectIdRadio1").prop("checked", true);
			$("#objectId_area").prop("disabled",true);
		}else{
			$("#objectIdRadio2").prop("checked", true);
			$("#objectId_area").prop("disabled",false);
		}
		
		//供应商ID选择效果
		var supplierId='${auditSortRule.supplierId!''}';
		if(supplierId==''){
			$("#supplierIdRadio1").prop("checked", true);
			$("#supplierId_area").prop("disabled",true);
		}else{
			$("#supplierIdRadio2").prop("checked", true);
			$("#supplierId_area").prop("disabled",false);
		}
	});
	
	//产品ID选择
	function objectIdChange(){
		var r1 = $("#objectIdRadio1").prop("checked");
		if(r1){
			$("#objectId_area").prop("disabled",true);
		}else{
			$("#objectId_area").prop("disabled",false);
		}
	}
	
	//供应商ID选择
	function supplierIdChange(){
		var r1 = $("#supplierIdRadio1").prop("checked");
		if(r1){
			$("#supplierId_area").prop("disabled",true);
		}else{
			$("#supplierId_area").prop("disabled",false);
		}
	}
	
	//保存审批排序规则
	function saveAuditSortRule(){
		var sortRuleName = $("input[name='sortRuleName']").val();
		var remindTime = $("input[name='remindTime']").val();
		var seq = $("input[name='seq']").val();
		var re = /^[1-9]+[0-9]*]*$/;
		if($.trim(sortRuleName)==''){
			$.alert("分组名称不能为空");
			return;
		}else if(sortRuleName.length>10){
			$.alert("分组名称长度不能超过10位");
			return;
		}
		if($.trim(remindTime)==''){
			$.alert("调配时间不能为空");
			return;
		}else if(remindTime.length>9){
			$.alert("调配时间不能超过9位");
			return;
		}else if(!re.test(remindTime)){
			$.alert("调配时间只能为整数");
			return;
		}
		if($.trim(seq)==''){
			$.alert("优先级不能为空");
			return;
		}else if(seq.length>11){
			$.alert("优先级不能超过11位");
			return;
		}else if(!re.test(seq)){
			$.alert("优先级只能为整数");
			return;
		}
		var r1 = $("#objectIdRadio1").attr("checked");
		var r2 = $("#supplierIdRadio1").attr("checked");
		if(r1){
			$("#objectId_area").val("");
		}else{
			if($.trim($("#objectId_area").val())==''){
				$.alert("产品ID不能为空");
				return;
			}
			
		}
		
		if(r2){
			$("#supplierId_area").val("");
		}else{
			if($.trim($("#supplierId_area").val())==''){
				$.alert("供应商ID不能为空");
				return;
			}
		}
		//遮罩层
		var loading = pandora.loading("正在努力保存中...");
		$.ajax({
			   url : "/vst_order/ord/order/confirm/editAuditSortRule.do",
			   data : $("#editAuditSortRuleForm").serialize(),
			   type:"POST",
			   dataType:"JSON",
			   success : function(result){
					if(result.code=="success" ){
						loading.close();
					  	alert(result.message);
					  	setInterval(function(){queryAuditSortRuleList()},1000);
					}else {
						loading.close();
						alert(result.message);
						setInterval(function(){queryAuditSortRuleList()},1000);
					}
			   },
			   error: function(XMLHttpRequest, textStatus, errorThrown) {
				   loading.close();
				   if(textStatus=='timeout'){
				　　　　　   alert("程序运行超时，3秒后自动刷新页面");
					    setInterval(function(){queryAuditSortRuleList()},3000);
				　　　}else{
						alert("程序运行出现异常，3秒后自动刷新页面");
						setInterval(function(){queryAuditSortRuleList()},3000);
				　　　}
				}
		});
	}
	
	//检测文本框内容长度
	function checkTextareaLength(Id){
		if(Id!=""&&(Id=="objectId"||Id=="supplierId")){
			if(Id=="objectId"){
				var objectId=textarea=document.getElementById('objectId_area');
				var objectIdLength=objectId.value.length;
				if(objectIdLength>2000)
		        {
			        $("#saveButton").attr("disabled",true);
			        $("#saveButton").hide();
			        $("#objectIdTip").css("color","red");
		        }else{
		        	$("#saveButton").removeAttr("disabled");
		        	$("#saveButton").show();
		        	$("#objectIdTip").css("color","");
		        }
		        $("#objectIdTip").html(objectIdLength+"/2000字");
			}
			if(Id=="supplierId"){
				var supplierId=document.getElementById('supplierId_area');
				var supplierIdLength=supplierId.value.length;
				if(supplierIdLength>2000)
		        {
			        $("#saveButton").attr("disabled",true);
			        $("#saveButton").hide();
			        $("#supplierIdTip").css("color","red");
		        }else{
		        	$("#saveButton").removeAttr("disabled");
		        	$("#saveButton").show();
		        	$("#supplierIdTip").css("color","");
		        }
		        $("#supplierIdTip").html(supplierIdLength+"/2000字");
			}
		}
    }
</script>