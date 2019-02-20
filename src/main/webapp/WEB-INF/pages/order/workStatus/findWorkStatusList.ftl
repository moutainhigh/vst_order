<#import "/base/spring.ftl" as spring/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-员工工作状态查询</title>
<#include "/base/head_meta.ftl"/>
<link href="/vst_order/js/tooltip/css/global.css" rel="stylesheet" type="text/css" />
</head>
<body>
<#import "/base/pagination.ftl" as pagination>

<div class="iframe_header">
    <i class="icon-home ihome"></i>
    <ul class="iframe_nav">
        <li><a href="#">首页</a> &gt;</li>
        <li><a href="#">订单管理</a> &gt;</li>
        <li class="active">员工工作状态查询</li>
    </ul>
</div>

<div class="iframe_search">
	<form method="post" action='/vst_order/ord/order/queryWorkStatus.do' id="searchForm">
	    <table class="s_table">
			<tbody>
				<tr>
                    <td class="w6 s_label">一级部门：</td>
                    <td class="w10">
                    	<@spring.formSingleSelect "ordAuditConfigInfo.firstDepartment" firstDepMap 'class="w9"'/>
                    </td>
                    <td class="s_label">二级部门：</td>
                    <td class="w10">
						<@spring.formSingleSelect "ordAuditConfigInfo.secondDepartment" secondDepMap 'class="w9"'/>
                    </td>
                    <td class="s_label">选择组：</td>
                    <td class="w10">
						<@spring.formSingleSelect "ordAuditConfigInfo.threeDepartment" threeDepMap 'class="w9"'/>
                    </td>
                    <td class="s_label">员工工号：</td>
                    <td class="w10">
						<@spring.formInput  "ordAuditConfigInfo.operatorName" 'class="w9"' />
                    </td>
                    <td class="s_label">工作状态：</td>
                    <td>
                    	<@spring.formSingleSelect "ordAuditConfigInfo.workStatus" workStatusMap 'class="w9"'/>
                    </td>
                </tr>
                <tr>
                	<td class="w6 s_label">时间：</td>
                    <td class="w10">
                        <input id="d4321" class="Wdate" type="text" value="${ordAuditConfigInfo.startDateTime}" onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00'})" errorele="selectDate" name="startDateTime" readonly="readonly">
                    </td>
                    <td class="w2">&nbsp;-&nbsp;</td>
                    <td class="w10">
                     	<input id="d4322" class="Wdate" type="text" value="${ordAuditConfigInfo.endDateTime}" onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})" errorele="selectDate" name="endDateTime">
                    </td>
                </tr>
                <tr>
                	<td class="w6 s_label">可选项：</td>
                	<td class="w10">
                		<#if ordAuditConfigInfo.processedAudit?? && ordAuditConfigInfo.processedAudit == "Y">
                			<input type="checkbox" id="processedAudit" name="processedAudit" value="Y" checked/>&nbsp;已处理活动
                		<#else>
                			<input type="checkbox" id="processedAudit" name="processedAudit" value="N"/>&nbsp;已处理活动
                		</#if>
                	</td>
                	<td class="w10">
                		<#if ordAuditConfigInfo.processedOrder?? && ordAuditConfigInfo.processedOrder == "Y">
							<input type="checkbox" id="processedOrder" name="processedOrder" value="Y" checked/>&nbsp;已处理订单
                		<#else>
                			<input type="checkbox" id="processedOrder" name="processedOrder" value="N"/>&nbsp;已处理订单
                		</#if>
					</td>
                	<td class="w10">
                		<#if ordAuditConfigInfo.unProcessedAudit?? && ordAuditConfigInfo.unProcessedAudit == "Y">
							<input type="checkbox" id="unProcessedAudit" name="unProcessedAudit" value="Y" checked/>&nbsp;未处理活动
						<#else>
							<input type="checkbox" id="unProcessedAudit" name="unProcessedAudit" value="N"/>&nbsp;未处理活动
						</#if>
					</td>
                	<td class="w10">
                		<#if ordAuditConfigInfo.unProcessedOrder?? && ordAuditConfigInfo.unProcessedOrder == "Y">
							<input type="checkbox" id="unProcessedOrder" name="unProcessedOrder" value="Y" checked/>&nbsp;未处理订单 
						<#else>
							<input type="checkbox" id="unProcessedOrder" name="unProcessedOrder" value="N"/>&nbsp;未处理订单 
						</#if>              	
                	</td>
                </tr>
			</tbody>
		</table>
		
		<div class="operate mt20" style="text-align:center">
        	<a class="btn btn_cc1" id="search_button">查询</a>
        	<a class="btn btn_cc1" id="clear_button">清空</a>
        </div>
	 </form>
    
  	<#if resultPage?? >
		<#if resultPage.items?size gt 0 >
	    <div class="iframe_content">
	    	<p class="tr cc3">共查询到 可接单人员：<b class="cc7">${onlineNum}</b><#--人 忙碌人员：<b class="cc7">${busyNum}</b>-->人 不可接单人员：<b class="cc7">${offlineNum}</b>人</p>
	        <table class="p_table table_center">
	            <thead>
	                <tr>
	                    <th>工号</th>
	                    <th>姓名</th>
	                    <th>部门/组</th>
	                    <th>在线状态</th>
	                    <#if ordAuditConfigInfo.processedAudit?? && ordAuditConfigInfo.processedAudit == "Y">
	                    	<th>已处理活动</th>
	                    </#if>
	                    <#if ordAuditConfigInfo.processedOrder?? && ordAuditConfigInfo.processedOrder == "Y">
	                    	<th>已处理订单</th>
	                    </#if>
	                    <#if ordAuditConfigInfo.unProcessedAudit?? && ordAuditConfigInfo.unProcessedAudit == "Y">
	                    	<th>未处理活动</th>
	                    </#if>
	                    <#if ordAuditConfigInfo.unProcessedOrder?? && ordAuditConfigInfo.unProcessedOrder == "Y">
	                    	<th>未处理订单</th>
	                    </#if>
	                    <th>操作</th>
	                </tr>
	            </thead>
	            <tbody>
	            	<#list resultPage.items as result>
		                <tr>
		                    <td>
		                    <a href="/vst_order/ord/order/intoManuDistPage.do?operatorName=${result.userName!''}&departmentId=${result.departmentId!''}" class="editProp" target="_blank" >${result.userName!''}</a>
		                    </td>
		                    <td>${result.realName!''}</td>
		                    <td>
		                    	${result.departmentName!''}
		                    	<span class="formInfo">
		                    		<a href="/vst_order/ord/order/queryDepartment.do?width=200&departmentId=${result.departmentId!''}" class="jTip" id="id${result.userName!''}" name="员工组织架构信息">?</a>
		                    	</span>
		                    </td>
							<td>${result.workStatus!''}</td>
							<#if ordAuditConfigInfo.processedAudit?? && ordAuditConfigInfo.processedAudit == "Y">
								<td>${result.processedActivityNum!''}</td>
							</#if>
							<#if ordAuditConfigInfo.processedOrder?? && ordAuditConfigInfo.processedOrder == "Y">
		                    	<td>${result.processedOrderNum!''}</td>
		                    </#if>
		                    <#if ordAuditConfigInfo.unProcessedAudit?? && ordAuditConfigInfo.unProcessedAudit == "Y">
								<td>${result.unprocessedActivityNum!''}</td>
							</#if>
							<#if ordAuditConfigInfo.unProcessedOrder?? && ordAuditConfigInfo.unProcessedOrder == "Y">
								<td>${result.unprocessedOrderNum!''}</td>
							</#if>
							<td>
								<#if result.workStatus?? && result.workStatus=='可接单'>
									<a href="javascript:void(0);" onclick="updateAuditUserWorkStatus('${result.userName!''}',false);">不可接单</a>
								<#else>	
									<a href="javascript:void(0);" onclick="updateAuditUserWorkStatus('${result.userName!''}',true);">可接单</a>
								</#if>
								<a id="showLog" href="javascript:void(0);" onclick="showLog('${result.userId!''}')" title="查看日志">查看日志</a>
							</td>
		                </tr> 
	                </#list> 
	            </tbody>
	        </table>	
			<@pagination.paging resultPage/>
	 	</div>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关人员，请重新输入相关条件查询！</div>
	</#if>
</#if>

<#include "/base/foot.ftl"/>
</body>
</html>

<script src="/vst_order/js/vst_department_util.js"></script>
<script src="/vst_order/js/tooltip/js/jtip.js"></script>
<script type="text/javascript">
	//设置需要防止重复点击页码
	var checkReSubmitFlag = true;
	//查询
	$("#search_button").click(function(){
		if($(this).attr("disabled")) {
			return;
		}
		formSubmit();
		$(this).attr("disabled", true);
		$(this).css("border","1px solid #aaa").css("background-color","#bbb").css("color", "#000").css("cursor", "default");
	});
	
	$("#processedAudit,#processedOrder,#unProcessedAudit,#unProcessedOrder").click(function() {
		if($(this).attr("checked")) {
			$(this).val("Y");
		} else {
			$(this).val("N");
		}
	});
	
	function formSubmit(){
		//if(!$("#searchForm").validate().form()){
		//	return false;
		//}
		$("#searchForm").submit();
	}
	//清空
	$("#clear_button").bind("click",function(){
		window.location.href = "/vst_order/ord/order/intoWorkStatus.do";
	});
	
	function updateAuditUserWorkStatus(operatorName,onlineFlag){
		$.ajax({
					url : "/vst_order/ord/order/updateAuditUserWorkStatus.do",
					type : "post",
					dataType : 'json',
					data : {"operatorName":operatorName,"onlineFlag":onlineFlag},
					success : function(result) {
						if(!result.success){
					 		$.alert(result.message);
					 	}else{
					 		formSubmit();
					 	}
						
					}
				});
	}
	
	 var showLogDialog;
	 function showLog(userId){
	 	var param="objectType=PERM_USER&objectId="+userId+"&sysName=VST";
	    /*showLogDialog = new xDialog("/vst_order/ord/order/workStatus/logList.do",data,{title:"查看日志",width:1200});*/
         showLogDialog=new xDialog("/lvmm_log/bizLog/showVersatileLogList.do?"+param,{},{title:"查看日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
	 }
</script>