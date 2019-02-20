<#import "/base/spring.ftl" as spring/>
<#import "/base/pagination.ftl" as pagination>
<!DOCTYPE html>
<html>
	<head>
		<#include "/base/head_meta.ftl"/>
	</head>
	<body>
	    <div class="iframe_content">
	    	<div style="margin: 10px 0 0; text-align:left;">
	    		<div class="fl operate"><a class="btn btn_cc1" 
	    				href="javascript:editAuditSortRule('')">新建分组</a></div>
	    	</div>
	    	<#if pageData?? && pageData.items??>
					<#if pageData.items?size gt 0 >
				        <table class="p_table table_center">
				            <thead>
				                <tr>
									 <th>分组ID</th>
				                     <th>分组名称</th>
				                     <th>到店时间</th>
									 <th>所属BU</th>
				                     <th>产品ID</th>
				                     <th>供应商ID</th>
									 <th>是否即时</th>
				                     <th>调配时间（分钟）</th>
				                     <th>优先级</th>
				                      <th>渠道</th>
				                     <th>操作人</th>
				                     <th>操作时间</th>
				                     <th>操作</th>
				                </tr>
				            </thead>
				            <tbody>
					            <#list pageData.items as auditSortRule> 
					            	<#if auditSortRule ??>
						                <tr>
						                	<td>
												${auditSortRule.sortRuleId!''}
											</td>
											<td>
												${auditSortRule.sortRuleName!''}
											</td>
											<td>
												<#if <#--auditSortRule.arriveType == 'ARRIVE_IMMEDIATELY'>
													马上到店
												<#elseif--> auditSortRule.arriveType == 'ARRIVE_TODAY'>
													今日到店
												<#elseif auditSortRule.arriveType == 'ARRIVE_MORROW'>
													次日到店
												<#elseif auditSortRule.arriveType == 'ARRIVE_MORROW_AFTER'>
													次日之后到店
												<#else>
													全部			
												</#if>
											</td>
											<td>
												<#if auditSortRule.bu ??>
													${allBuMap[auditSortRule.bu]}
												<#else>
													全部
												</#if>
											</td>
											<td>
												<#if auditSortRule.objectId ??>
													${auditSortRule.objectId}
												<#else>
													全部
												</#if>
											</td>
											<td>
												<#if auditSortRule.supplierId ??>
													${auditSortRule.supplierId}
												<#else>
													全部
												</#if>
											</td>											
											<td>
												<#if auditSortRule.immediatelyFlag == 'Y'>
													是
												<#elseif auditSortRule.immediatelyFlag == 'N'>
													否
												<#else>
													全部	
												</#if>
											</td>
											<td>
												<#if auditSortRule.remindTime ??>
													${auditSortRule.remindTime/60}
												<#else>
												</#if>
											</td>
											<td>
												${auditSortRule.seq!''}
											</td>
											<td>
												<#if auditSortRule.orderChannel=='neither,other,taobao' || auditSortRule.orderChannel=='' >
													全部
												<#elseif auditSortRule.orderChannel=='other,taobao'>
													分销(淘宝)和分销(不含淘宝)
												<#elseif auditSortRule.orderChannel=='neither,other'>
													分销(不含淘宝)和主站
												<#elseif auditSortRule.orderChannel=='neither,taobao'>
													分销(淘宝)和主站
												<#elseif auditSortRule.orderChannel=='taobao'>
													分销(淘宝)
												<#elseif auditSortRule.orderChannel=='neither'>
													主站
												<#elseif auditSortRule.orderChannel=='other'>
													分销(不含淘宝)
												<#else>
													${auditSortRule.orderChannel!''}
												</#if>
											</td>
											<td>
												${auditSortRule.operateName!''}
											</td>
											<td>
												${(auditSortRule.updateTime?string("yyyy-MM-dd HH:mm:ss"))!''}
											</td>
											<td width="300">
												<div class="fl operate" style="float:none;">
													<a class="btn btn_cc1"  href="javascript:editAuditSortRule('${auditSortRule.sortRuleId}')">修改</a>
													<a class="btn btn_cc1" 
														href="javascript:updateValid('${auditSortRule.sortRuleId}','${auditSortRule.sortRuleName}','${auditSortRule.valid}','${auditSortRule.arriveType}','${auditSortRule.bu}',
														'${auditSortRule.objectId}','${auditSortRule.supplierId}','${auditSortRule.immediatelyFlag}')">删除</a>
													<a class="btn btn_cc1" id="showLogDialog"  href="javascript:showLogDialog('${auditSortRule.sortRuleId}')">查看日志</a>		
												</div>
											</td>																																																																																								
						                </tr>
					               </#if>
					             </#list>   
				            </tbody>
				        </table>
						<@pagination.paging pageData></@pagination.paging>
				<#else>
					<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关活动！</div>
				</#if>
			</#if>
			<#if message == '0'>
				<div id="errorMessage" class="no_data mt20"><i class="icon-warn32"></i>参数不能为空</div>
			<#elseif message == '2'>
				<div id="errorMessage" class="no_data mt20"><i class="icon-warn32"></i>保存出现异常</div>
			<#else>
			</#if>
		</div>
		<#include "/base/foot.ftl"/>
		<#--js脚本-->
		<script>
			//显示编辑审核排序规则对话框
			var showEditAuditSortRuleDialog;
			function editAuditSortRule(sortRuleId){
				var url="/vst_order/ord/order/confirm/showEditAuditSortRuleDialog.do";
				var titleName="新增订单排序";
				if(sortRuleId!=""){
					url="/vst_order/ord/order/confirm/showEditAuditSortRuleDialog.do?sortRuleId="+sortRuleId;
					titleName="修改订单排序";
				}
				showEditAuditSortRuleDialog = 
						new xDialog(url,
						{},
						{title:titleName,width:700,hight:800,scrolling:"yes"});
			}
			
			function queryAuditSortRuleList(){
				window.location.href = "/vst_order/ord/order/confirm/queryAuditSortRuleList.do";
			}
			
			//更新审核排序规则为无效
			function updateValid(sortRuleId, sortRuleName, valid, arriveType, bu, objectId, supplierId, immediatelyFlag){
				if(arriveType=="" && bu=="" && objectId=="" && supplierId=="" && immediatelyFlag==""){
					alert("默认分组不能删除！");
				}else{
					if(sortRuleId!=""){
						var r=confirm("确认删除: "+ sortRuleName +" 分组?");
						if (r==true){
							//遮罩层
							var loading = pandora.loading("正在努力保存中...");
							var newValue = "N";
							var formData="sortRuleId=" + sortRuleId + "&valid=" + newValue;
							$.ajax({
								   url : "/vst_order/ord/order/confirm/updateAuditSortRuleValid.do",
								   data : formData,
								   timeout : 30000,
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
					}else{
						alert("分组ID不能为空！");
					}
				}
			}
			
			//查看日志
			function showLogDialog(sortRuleId){
		        var param = "parentType=AUDIT_SORT_RULE&parentId="+sortRuleId+"&sysName=VST";
		        new xDialog("/lvmm_log/bizLog/showVersatileLogList?"+param,{},{title:"日志",iframe:true,width:1000,hight:500,iframeHeight:680,scrolling:"yes"});
			}
		</script>		    	
	</body>
</html>