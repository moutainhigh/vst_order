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
		            <li><a href="#">微信模板管理</a> &gt;</li>
		            <li class="active">微信模板查询</li>
		        </ul>
		</div>
		<div class="iframe_search">
			<form method="post" action="/vst_order/ord/ordWechatTemplate/findOrdWechatTemplateList.do" id="searchForm">
        		<table class="s_table form-inline">
          			<tbody>
	                	<tr>
		                    <td class="w6 s_label">发送节点：</td>
		                    <td class="w18">
		                     	<select name="sendNode" >
			                    	<option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;--- 请选择 ---</option>
		                     		<#list nodeList as node>
		                     			<option value="${node.code!''}" <#if ordWechatTemplate ?? && ordWechatTemplate.sendNode==node.code>selected="selected"</#if>>${node.cnName!''}</option>
			                  		</#list>
					        	</select>
					        </td>
					        <td class="w6 s_label">消息类型：</td>
		                    <td class="w18">
		                     	<select name="messageCode">
		                    	 	<option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;--- 请选择 ---</option>
		                     		<#list wechatInfoTypeList as wechatInfoType>
		                     			<option value="${wechatInfoType.code!''}" <#if ordWechatTemplate ?? && ordWechatTemplate.messageCode==wechatInfoType.code>selected="selected"</#if>>${wechatInfoType.cnName!''}</option>
			                  		</#list>		                    	 	
					        	</select>
					        </td>
					        <td class="w6 s_label">模板状态：</td>
		                    <td class="w18">
		                     	<select name="state">
		                    	 	<option value="">不限</option>
			                    	<option value='Y' <#if ordWechatTemplate ?? && ordWechatTemplate.state=='Y'>selected="selected"</#if>>有效</option>
			                    	<option value='N' <#if ordWechatTemplate ?? && ordWechatTemplate.state=='N'>selected="selected"</#if>>无效</option>		                    	 	
					        	</select>
					        </td>	
					        <td class=" operate mt10"><a class="btn btn_cc1" id="search_button">查询</a> </td>
					        <td class=" operate mt10"><a class="btn btn_cc1" id="new_button">新增</a> </td>				        
				     	</tr>
            		</tbody>
        		</table>	
			</form>
    	</div>
	    <div class="iframe_content">
	    	<#if pageData?? && pageData.items??>
					<#if pageData.items?size gt 0 >
				        <table class="p_table table_center">
				            <thead>
				                <tr>
				                     <th>发送节点</th>
				                     <th>消息类型</th>
				                     <th>模板状态</th>
				                     <th>操作</th>
				                </tr>
				            </thead>
				            <tbody>
					            <#list pageData.items as wechatTemplate> 
					                <tr>
										<td>
											<#list nodeList as node> 
												<#if node.code == wechatTemplate.sendNode>${node.cnName!''}</#if>
											</#list>
										</td>
										<td>
											<#list wechatInfoTypeList as wechatType> 
												<#if wechatType.code = wechatTemplate.messageCode>${wechatType.cnName!''}</#if>
											</#list>
										</td>
										<td>
											<#if wechatTemplate.state == "Y">
												有效
											<#else>
												无效
											</#if>
										</td>										
										<td>
											<a href="javascript:void(0);" class="edit" data=${wechatTemplate.id!''}>修改</a>
											<#if wechatTemplate.state == "Y">
												<a href="javascript:void(0);" class="updateState" data="N" data1=${wechatTemplate.id!''}>禁用</a>
											<#else>
												<a href="javascript:void(0);" class="updateState" data="Y" data1=${wechatTemplate.id!''}>启用</a>
											</#if>
										</td>
					                </tr>
					             </#list>   
				            </tbody>
				        </table>
						<@pagination.paging pageData></@pagination.paging>
				<#else>
					<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关结果，请重新输入相关条件查询！</div>
				</#if>
			</#if> 
		</div>
		<#include "/base/foot.ftl"/>    	
	</body>
</html>
<script type="text/javascript">
	var addDialog,editDialog;
	//新增
	$("#new_button").click(function(){
		 addDialog = new xDialog("/vst_order/ord/ordWechatTemplate/showAddOrdWechatTemplate.do", {}, {title:"新增微信消息模板",width:700});
	});
	//查询
	$("#search_button").click(function(){
		if(!$("#searchForm").validate().form()){
			return false;
		}
		$("#searchForm").submit();
	});
	//修改
	$("a.edit").bind("click",function(){
		var id = $(this).attr("data");
		editDialog = new xDialog("/vst_order/ord/ordWechatTemplate/showEditOrdWechatTemplate.do", {"id":id }, {title:"修改微信消息模板",width:700});
	});
	//启用和禁用
	$("a.updateState").bind("click",function(){
		var id = $(this).attr("data1");
		var flag = $(this).attr("data");
		var msg = (flag == "N" ? "确认禁用微信模板吗？" : "确认启用微信模板吗？");
		$.confirm(msg,function(){
			$.get("/vst_order/ord/ordWechatTemplate/updateOrdWechatTemplate.do?id="+id+"&state="+flag, function(data){
				if(data.code == "success"){
					pandora.dialog({wrapClass:"dialog-mini", content:data.message, okValue:"确定",ok:function(){
						if(!$("#searchForm").validate().form()){
							return false;
						}
						$("#searchForm").submit();
					}});
				}else{
					pandora.dialog({wrapClass:"dialog-mini", content:data.message, okValue:"确定",ok:function(){
					
					}});				
				}
			});
		});
	});		
</script>