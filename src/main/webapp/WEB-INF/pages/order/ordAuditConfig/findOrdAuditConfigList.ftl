<#import "/base/spring.ftl" as spring/>
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
            <li><a href="#">订单管理</a> &gt;</li>
            <li class="active">员工活动组管理</li>
        </ul>
</div>

<div class="iframe_search">
<form method="post" action='/vst_order/order/ordAuditConfig/findOrdAuditConfigListNew.do' id="searchForm">
	<div class="iframe_search">
        <div class="xbox-form form-inline">
        	<label>订单品类：
        		<select name="categoryId">
            		<#list bizCategoryList as category>
            			<#if category.categoryId == ordAuditConfigInfo.categoryId>
            				<option value="${category.categoryId!''}" selected="selected">${category.categoryName!''}</option>
            			<#else>
            			<option value="${category.categoryId!''}">${category.categoryName!''}</option>
            			</#if>
            		</#list>
            	</select>
        	</label>
        	<label>销售渠道：
        		<select name="distributionChannel">
            		<option value="taobao" <#if ordAuditConfigInfo.distributionChannel == 'taobao'>selected="selected"</#if>>分销(仅淘宝)</option>
            		<option value="other" <#if ordAuditConfigInfo.distributionChannel == 'other'>selected="selected"</#if>>分销(不含淘宝,旅途,wap,微信,秒杀,团购)</option>
            		<option value="neither" <#if ordAuditConfigInfo.distributionChannel == 'neither'>selected="selected"</#if>>其它渠道</option>
            	</select>
        	</label>        	
            <label>一级部门：
			<@spring.formSingleSelect   "ordAuditConfigInfo.firstDepartment"  firstDepMap  'class="w9"'/>
			</label>
            <label>二级部门：
            <@spring.formSingleSelect   "ordAuditConfigInfo.secondDepartment" secondDepMap 'class="w9"'/>
            　		</label>
            <label>选择组：
            <@spring.formSingleSelect "ordAuditConfigInfo.threeDepartment" threeDepMap 'class="w9"'/>
            </label>
            <#--<label>员工工号：<@spring.formInput   "ordAuditConfigInfo.operatorName" 'class="w9"' /></label>--> 
        </div>
        <p class="tc mt20 operate"><a class="btn btn_cc1" id="search_button">查询</a>
        <a class="btn btn_cc1" id="new_button">新增</a>
         <#--<a class="btn btn_cc1" id="clear_button">清空</a>-->
        </p>
    </div>
</form>
    <div class="iframe_content">
    	<#if pageParam?? >
			<#if pageParam.items?size gt 0 >
		        <table class="p_table table_center">
		            <thead>
		                <tr>
		                    <#--<th class="w1"></th>-->
		                     <#--<th>员工工号</th>-->
		                     <#--<th>员工姓名</th>-->
		                    <#--<th>品类</th>-->
		                     <#--<th>查看活动组分单权限</th>-->
		                     <#--<th>调整活动组分单权限</th>-->
		                     <th>组</th>
		                     <th>订单品类</th>
		                     <th>销售渠道</th>
		                     <th>活动接收权限</th>
		                     <th>操作</th>
		                </tr>
		            </thead>
		            <tbody>
			            <#list pageParam.items as item> 
			                <tr>
			                     <#--<td><input type="checkbox" name="auditIds" value="${permUser.userName}"></td>
			                     <#--<td>${permUser.userName!''} </td>
			                     <#--<td>${permUser.realName!''} </td>-->
								<#--<td>${ordAuditConfig.categoryName!''} </td>-->
			                     <#--<td><a href="javascript:void(0);" class="editProp" data=${permUser.userName}>查看</a>
								</td>
								<td>
								<a href="javascript:;" class="editCateGroup" data=${permUser.userName}>修改</a></td>
								</td>-->
								<td>${item.deptStr}</td>
								<td>${item.businessRule.ruleName}</td>
								<td>
            						<#if item.distributionChannel == 'taobao'>分销(仅淘宝)</#if>
            						<#if item.distributionChannel == 'other'>分销(不含淘宝,旅途,wap,微信,秒杀,团购)</#if>
            						<#if item.distributionChannel == 'neither'>其它渠道</#if>
								</td>
								<td>
									<#list item.ordFunctionInfoList as ordFunctionInfo> 
										${ordFunctionInfo.functionName}(<#if ordFunctionInfo.checked=='false'>×<#else>√</#if>)
									</#list>
								</td>
								<td>
								<a href="javascript:newUpdateOrdAuditConfig(${item.ordAllocationId});">修改</a>&nbsp;&nbsp;
								<a href="javascript:delOrdAuditConfig(${item.ordAllocationId});">删除 </a>
								</td>
			                </tr>
			             </#list>   
		            </tbody>
		        </table>
				<@pagination.paging pageParam>
					<#--<a class="btn btn_cc1" id="checkAll">全选</a><a class="btn btn_cc1" data="batchUpdate" id="batchUpdate">批量修改</a>-->
				</@pagination.paging>
		<#else>
			<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关结果，请重新输入相关条件查询！</div>
		</#if>
	</#if> 
</div>
<#include "/base/foot.ftl"/>
</body>
</html>
<script src="/vst_order/js/vst_department_util.js"></script>
<script>
	var addOrdAuditCongigDialog,updateOrdAuditCongigDialog ,ordStatusGroupDialog;
	
	var queryOrdAuditCongigDialog=window;
	
	//清空
	$("#clear_button").bind("click",function(){
		window.location.href = "/vst_order/order/ordAuditConfig/showOrdAuditConfigList.do";
	});
	$("#checkAll").bind("click",function(){
			  var ischeckAll=$("input[name='auditIds']").attr("checked"); 
			  if(!ischeckAll)
			  {
			  	$("input[name='auditIds']").attr("checked",true)
			  }else{
			  	$("input[name='auditIds']").attr("checked",false)
			  }
			  
		      
		});
	//查询
	$("#search_button").click(function(){
	/**
		var operatorName=$("#operatorName").val();
		if(operatorName=='')
		{
		
		}
		$("#operatorName").attr("required","true");
	*/
		if(!$("#searchForm").validate().form()){
					return false;
		}
		$("#searchForm").submit();
	});
	$("#add_button").click(function(){
		addOrdAuditCongigDialog = new xDialog("/vst_order/order/ordAuditConfig/showAddOrdAuditConfig.do",{},{title:"新增任务分配人员配置",width:500});
	});
	
	//查看
	$("a.editProp").bind("click",function(){
	    var userName  = $(this).attr("data");
		updateOrdAuditCongigDialog = new xDialog("/vst_order/order/ordAuditConfig/viewOrdAuditConfig.do",{"updateOp":"false","userName":userName},{title:"权限查看",width:550});
	});
	
	
	function updateOrdAuditConfig()
	{
		//alert(this.id);
	    var data=$(this).attr("data");
	    var userName;
		var batchUpdate;
	    if("batchUpdate"==data)
	    {
	    	  batchUpdate="true";
	    	  var operatorName_value =[];    
	    	  var categoryId_value =[];    
		      $('input[name="auditIds"]:checked').each(function(){    
		      	
		       		var checkedValue=$(this).val();
		       		operatorName_value.push(checkedValue); 
		      });    
		      if(operatorName_value.length==0)
		      {
		      	$.alert('尚未选中任何记录活动'); 
		      	return;
		      }
		     userName  =operatorName_value;
		     
	    
	    }else{
	    	 batchUpdate="false";
	    	 userName  = data;
	    
	    }
	   userName=userName+"";
	   updateOrdAuditCongigDialog = new xDialog("/vst_order/order/ordAuditConfig/showUpdateOrdAuditConfig.do",{"updateOp":"true","batchUpdate":batchUpdate,"userName":userName},{title:"活动组权限修改",width:500});
	    
							
	
	}
	
	//修改
	function newUpdateOrdAuditConfig(ordAllocationId){
		 updateOrdAuditCongigDialog = 
		 new xDialog("/vst_order/order/ordAuditConfig/showUpdateOrdAuditConfig.do",
		 {"ordAllocationId":ordAllocationId},
		 {title:"修改任务分配人员配置",width:700});
	}
	
	//删除
	function delOrdAuditConfig(ordAllocationId){
		 if(confirm('确定要删除此记录吗?'))
		 	$.ajax({
					url : "/vst_order/order/ordAuditConfig/delOrdAuditConfig.do",
					type : "post",
					dataType : 'json',
					data : "ordAllocationId="+ordAllocationId,
					success : function(result) {
						if(!result.success){
					 		$.alert(result.message);
					 	}else{
					 		 $("#searchForm").submit();
					 	}
						
					}
				});
	}
	//新增
	$("#new_button").bind("click",function(){
		 addOrdAuditCongigDialog = 
		 new xDialog("/vst_order/order/ordAuditConfig/showAddOrdAuditConfig.do",
		 {},
		 {title:"新增任务分配人员配置",width:700});
	});

</script>
