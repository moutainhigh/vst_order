<#import "/base/spring.ftl" as spring/>
<form method="post" action='/vst_order/order/ordAuditConfig/saveOrdAuditConfig.do' id="dataForm">
<input type="hidden" name="ordAllocationId" value="${ordAllocationId!''}">
<input type="hidden" name="isAll" value="N">
<div class="iframe-content">
    <div class="p_box">
	 <table class="p_table form-inline">
            <tbody>
                <tr>
	                <td class="p_label">
	                	订单品类：
	                </td>
					<td colspan=2>
						<input type="hidden" id="categoryId" name="categoryId" value="${ordAuditConfigVo.bizCategory.categoryId!''}">
						${ordAuditConfigVo.bizCategory.categoryName}
						<#--<select name="categoryId" id="categoryId">
		            		<#list bizCategoryList as category>
		            			<#if category.categoryId == ordAuditConfigVo.bizCategory.categoryId>
	            					<option value="${category.categoryId!''}" selected="selected">${category.categoryName!''}</option>
		            			<#else>
		            				<option value="${category.categoryId!''}">${category.categoryName!''}</option>
		            			</#if>
		            		</#list>
		            	</select>-->	
	                </td>
                </tr>
               
                 <tr>
					<td class="p_label">
						选择组：
					</td>
					<td colspan=2>
							<label>一级部门：
								<input type="hidden" id="firstDeptId" name="firstDeptId" value="${ordAuditConfigVo.firstDeptId!''}">
								<span class="cc6 f14">${ordAuditConfigVo.firstDeptName}</span>
							</label>
				            <label>二级部门：
				            <span class="cc6 f14">${ordAuditConfigVo.secondDeptName}</span>
				            　		</label>
				            <label>选择组：
				            	<input type="hidden" id="threeDeptId" name="threeDeptId" value="${ordAuditConfigVo.threeDeptId!''}">
				            	<span class="cc6 f14">${ordAuditConfigVo.threeDeptName}</span>
				            </label>
                   	</td>
                  </tr>
                  <tr>  	
                   	<td class="p_label">选择可接收的活动：</td>
					<td colspan=2>
						<table class="p_table form-inline">
				            <tbody>
				            		<tr>
				                	<#list ordAuditConfigVo.ordFunctionInfoList as ordFunctionInfo> 
					                	 <td>
					                		<input type="checkbox" name="ordFunctionIds" value="${ordFunctionInfo.ordFunctionId}" <#if ordFunctionInfo.checked=='true'>checked</#if>>
											${ordFunctionInfo.functionName}
						                </td>
					                	 <#if ordFunctionInfo_index%2==1>
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
		
		var categoryId=$("#categoryId").val();
		if($.trim(categoryId)==''){
			$.alert("请选择订单品类");
			return;
		}
		 
	    var threeDeptId=$("#threeDeptId").val();
		if($.trim(threeDeptId)=='')
	    {
       		$.alert("请选择组");
       		return;
	    }
	   
	    var ordFunctionIdLength=$("input[type=checkbox][name=ordFunctionIds]:checked").length;
		if(ordFunctionIdLength<=0){
			$.alert("请选择可接收的活动");
			return;
		}
		
		var functionIdCount=$("input[type=checkbox][name=ordFunctionIds]").length;
		var firstDeptId=$("#firstDeptId").val();
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
				 		$("#searchForm").submit();
				 		updateOrdAuditCongigDialog.close();
				 	}
		   }
		});						
	});
	$("#firstDeptId").change(function(){
	
		$("#secondDeptId").html("");
		$("#threeDeptId").html("");
		
		var firstDepartment=$("#firstDeptId option:selected").val();
		if(firstDepartment=='')
       {
       		$("#secondDeptId").html("");
       		$("#threeDeptId").html("");
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

       	$("#threeDeptId").html("");
		
		var secondDepartment=$("#secondDeptId option:selected").val();
	  if(secondDepartment=='')
       {
       		$("#threeDeptId").html("");
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
		   		$("#threeDeptId").html("");
		   		
		   		var html ="";
		   		$.each(data,function(){
						var departmentName=this.departmentName;
						var orgId=this.orgId;
						if(orgId==0){
							orgId="";
						}
						$("#threeDeptId").append("<option value="+orgId+">"+departmentName+"</option>");
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