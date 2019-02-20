<#import "/base/spring.ftl" as spring/>
<form method="post" action='/vst_order/order/ordAuditConfig/saveOrdAuditConfig.do' id="dataForm">
<input type="hidden" name="isAll" value="N">
<input type="hidden" name="threeDeptId" value="">
<input type="hidden" name="categoryId" value="">
<div class="iframe-content">
    <div class="p_box">
	 <table class="p_table form-inline">
            <tbody>
                <tr>
	                <td class="p_label">
	                	业务规则：
	                </td>
					<td colspan=1>
						<select name="businessRuleId" id="businessRuleId">
		            		<#list businessRuleList as businessRule>
		            			<option value="${businessRule.businessRuleId!''}" category="${businessRule.categoryId!''}">${businessRule.ruleName!''}</option>
		            		</#list>
		            	</select>	
	                </td>
	                <td class="p_label">
	                	销售渠道：
	                </td>
					<td colspan=1>
						<select name="distributionChannel" id="distributionChannel">
		            		<option value="taobao">分销(仅淘宝)</option>
		            		<option value="other">分销(不含淘宝,旅途,wap,微信,秒杀,团购)</option>
		            		<option value="neither">其它渠道</option>
		            	</select>	
	                </td>	                
                </tr>
               
                 <tr>
					<td class="p_label">
						选择组：
					</td>
					<td colspan=3>
							<label>一级部门：
							<@spring.formSingleSelect   "ordAuditConfigVo.firstDeptId"  firstDepMap  'class="w9"'/>
							</label>
				            <label>二级部门：
				            <@spring.formSingleSelect   "ordAuditConfigVo.secondDeptId" secondDepMap 'class="w9"'/>
				            　		</label>
				            <label>选择组：
				           <select id="selectThreeDeptId" class="w9"><option name="">请选择</option></select>
				            </label>
                   	</td>
                  </tr>
                  <tr>  	
                   	<td class="p_label">选择可接收的活动：</td>
					<td colspan=3>
						<table class="p_table form-inline">
				            <tbody>
				            		<tr>
				                	<#list ordFunctionList as ordFunction> 
					                	 <td>
					                		<input type="checkbox" name="ordFunctionIds" value="${ordFunction.ordFunctionId}">
											${ordFunction.functionName}
						                </td>
					                	 <#if ordFunction_index%2==1>
											 </tr><tr>
										 </#if>
									</#list>
									</tr>
	            			</tbody>
            			</table>	
                   	</td>
                   </tr>
            </tbody>
        </table>
		 
	</div><!-- div p_box -->
</div><!-- //主要内容显示区域 -->
</form>
 <p class="tc mt20 operate"><a class="btn btn_cc1" id="new_button">保存</a><p>
<script>
	$("#new_button").bind("click",function(){
		//验证
		var categoryId=$("#businessRuleId option:selected").attr("category");
		$("input[type=hidden][name=categoryId]").val(categoryId);
		var businessRuleId=$("#businessRuleId option:selected").val();
		if($.trim(businessRuleId)==''){
			$.alert("请选择业务规则");
			return;
		}
		var firstDeptId=$("#firstDeptId option:selected").val();
		if($.trim(firstDeptId)=='')
	    {
       		$.alert("请选择一级部门");
       		return;
	    }
		
		var secondDeptId=$("#secondDeptId option:selected").val();
		if($.trim(secondDeptId)=='')
	    {
       		$.alert("请选择二级部门");
       		return;
	    }
	    if($("#selectThreeDeptId option").size()>1){
		    var threeDeptId=$("#selectThreeDeptId option:selected").val();
			if($.trim(threeDeptId)=='')
		    {
	       		$.alert("请选择组");
	       		return;
		    }
		    $("input[name='threeDeptId']").val(threeDeptId);
	    }else{
	    	$("input[name='threeDeptId']").val(secondDeptId);
	    }
	    
	    var ordFunctionIdLength=$("input[type=checkbox][name=ordFunctionIds]:checked").length;
		if(ordFunctionIdLength<=0){
			$.alert("请选择可接收的活动");
			return;
		}
		var functionIdCount=$("input[type=checkbox][name=ordFunctionIds]").length;
		// 呼叫中心orgId值 55 
		if(ordFunctionIdLength==functionIdCount&&firstDeptId==55){
			$("input[type=hidden][name=isAll]").val("Y");
		}
		$.ajax({
		   url : "/vst_order/order/ordAuditConfig/saveOrdAuditConfig.do",
		   data : $("#dataForm").serialize(),
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		if(!result.success){
				 		$.alert(result.message);
				 	}else{
				 		$.alert(result.message);
				 		addOrdAuditCongigDialog.close();
				 	}
		   }
		});						
	});
	$("#firstDeptId").change(function(){
	
		$("#secondDeptId").html("");
		$("#selectThreeDeptId").html("");
		
		var firstDepartment=$("#firstDeptId option:selected").val();
		if(firstDepartment=='')
       {
       		$("#secondDeptId").html("");
       		$("#selectThreeDeptId").html("");
       		return;
       }
       
       var param="depId="+firstDepartment+"&nowLevel=first";
       $.ajax({
		   url : "/vst_order/order/ordCommon/findCascadDepartment.do",
		   data : param,
		   type:"POST",
		   dataType:"JSON",
		   success : function(data){
		   
		   		$("#secondDeptId").html("");
		   		$.each(data,function(){
						var departmentName=this.departmentName;
						var orgId=this.orgId;
						if(orgId==0){
							orgId="";
						}
						$("#secondDeptId").append("<option value="+orgId+">"+departmentName+"</option>");
		        })
		        
		   		/**
		   		  $.each(data[0],function(key,value){

		               //其中key相当于是JAVA中MAP中的KEY，VALUE就是KEY相对应的值
						$("#secondDepartment").append("<option value="+key+">"+value+"</option>");
		               //alert(key+"    "+value);
		        })
		        
		        */
		   		
		   }
		});	
});

$("#secondDeptId").change(function(){

       	$("#selectThreeDeptId").html("");
		
		var secondDepartment=$("#secondDeptId option:selected").val();
	  if(secondDepartment=='')
       {
       		$("#selectThreeDeptId").html("");
       		return;
       }
       var param="depId="+secondDepartment+"&nowLevel=second";
      
       $.ajax({
		   url : "/vst_order/order/ordCommon/findCascadDepartment.do",
		   data : param,
		   type:"POST",
		   dataType:"JSON",
		   success : function(data){
		   		//var html = '<option value="">请选择</option>'
		   		$("#selectThreeDeptId").html("");
		   		
		   		var html ="";
		   		$.each(data,function(){
						var departmentName=this.departmentName;
						var orgId=this.orgId;
						if(orgId==0){
							orgId="";
						}
						$("#selectThreeDeptId").append("<option value="+orgId+">"+departmentName+"</option>");
		        })
		        
		   		/**
		   		for(var i in data[0]){
		   			html+="<option value="+i+">"+data[0][i]+"</option>";
		   		}
		   		
		   		
		   		$("#threeDepartment").append(html);
		   		*/
		   		
		   		
		   }
		});	
});
</script>