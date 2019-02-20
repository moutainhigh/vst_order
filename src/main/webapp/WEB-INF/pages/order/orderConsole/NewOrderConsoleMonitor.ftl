<#--页眉-->
<#import "/base/spring.ftl" as spring/>
<#import "/base/pagination.ftl" as pagination>

<!DOCTYPE html>
<html>
<head>
<title>订单管理-新工作台监控</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>

<#--页面导航-->
 <div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="#">首页</a> ></li>
            <li><a href="#">订单管理</a> ></li>
            <li class="active">新工作台监控</li>
        </ul>
        <br>
 </div>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" name="searchForm" action="/vst_order/ord/order/monitor/queryOrderConsole.do" method="post">
		<table class="s_table2 form-inline">
            <tbody>
                <tr>
                    <td width="" colspan="2">
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
                    </td>
                 </tr>
            </tbody>
        </table>


        <p class="tc mt20 operate">
        <a class="btn btn_cc1" id="search_button">查询</a>
        <a class="btn btn_cc1" id="clear_button">清空</a>
       
        </p>
    </div>
    
    
    </form>
<#if resultPage?? >
		<#if resultPage.items?size gt 0 >
<#--结果显示-->
			<div class="iframe_content">
		        <table class="p_table table_center">
		            <thead>
		                <tr>
		                    <th nowrap="nowrap">工号</th>
		                    <th nowrap="nowrap" >姓名</th>
		                    <th nowrap="nowrap" style="width:10%;">在线状态</th>
		                    <th nowrap="nowrap" >询位库</th>
		                    <th nowrap="nowrap">新单库</th>
		                    <th nowrap="nowrap">满房/变价/特殊满房库</th>
		                    <th nowrap="nowrap">已审/已回传库</th>
		                    <th nowrap="nowrap" style="width:10%;">已审/已回传库（非保留房）</th>
		                    <th nowrap="nowrap">取消确认库</th>
		                    <th nowrap="nowrap">其他预订通知</th>
		                    <th nowrap="nowrap">未处理总计</th>
		                    <th nowrap="nowrap">操作</th>
						</tr>
					</thead>
					<tbody>
	            	<#list resultPage.items as result>
		                <tr>
		                    <td>
		                    <a href="/vst_order/ord/order/intoManuDistPage.do?operatorName=${result.userName!''}&departmentId=${result.departmentId!''}" class="editProp" target="_blank" >${result.userName!''}</a>
		                    </td>
		                    <td>${result.realName!''}</td>
		                    
							<td>${result.workStatus!''}</td>
								<td>${result.inquiryAudit!''}</td>
		                    	<td>${result.newOrderAudit!''}</td>
								<td>${result.fullAudit!''}</td>
								<td>${result.inconfirmAudit!''}</td>
								<td>${result.inconfirmStockFlagIsN!''}</td>
								<td>${result.cancelConfirmAudit!''}</td>
								<td>${result.confirmBookingAudit!''}</td>
								<td>${result.sumNum!''}</td>
							<td>
								<#if result.workStatus?? && result.workStatus=='可接单'>
									<a href="javascript:void(0);" onclick="updateAuditUserWorkStatus('${result.userName!''}',false);">不可接单</a>
								<#else>	
									<a href="javascript:void(0);" onclick="updateAuditUserWorkStatus('${result.userName!''}',true);">可接单</a>
								</#if>
								<a id="showLog" target="_blank" href="http://super.lvmama.com/vst_order/ord/order/confirm/selectMainCheckedTab.do?mainCheckedTab=MYDESTTASK&operatorName=${result.userName!''}" title="查看详情">查看详情</a>
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
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>

<script src="/vst_order/js/vst_department_util.js"></script>

<#--js脚本-->
<script type="text/javascript">
	var updateDistOrderDialog;
	var findManualListDialog=window;
	
	/*$(function () {
       disabledSearchCondition("");
	})
	
	function  disabledSearchCondition(auditStatus){
			
	
			if(auditStatus=='POOL')
			{
				var url="/vst_order/order/ordManualDistOrder/findManualDistOrderList.do"+
					"?firstDepartment="+$("#firstDepartment").val()+"&secondDepartment="+$("#secondDepartment").val()+
					"&threeDepartment="+$("#threeDepartment").val()+"&distributionTimeBegin="+$("#distributionTimeBegin").val()+
					"&distributionTimeEnd="+$("#distributionTimeEnd").val();
				
				$("#searchForm").attr("action", url);
				
				$("#firstDepartment").attr("disabled",true);
				$("#secondDepartment").attr("disabled",true);
				$("#threeDepartment").attr("disabled",true);
				$("#operatorName").attr("readOnly",true);
				$("#distributionTimeBegin").attr("disabled",true);
				$("#distributionTimeEnd").attr("disabled",true);
				
			}else{
				var url = "/vst_order/order/ordManualDistOrder/findManualDistOrderList.do";
				$("#searchForm").attr("action", url);
				$("#firstDepartment").attr("disabled",false);
				$("#secondDepartment").attr("disabled",false);
				$("#threeDepartment").attr("disabled",false);
				$("#operatorName").attr("readOnly",false);
				$("#distributionTimeBegin").attr("disabled",false);
				$("#distributionTimeEnd").attr("disabled",false);
			}
	}
	
		$("#auditStatus").bind("change",function(){
				disabledSearchCondition($(this).val());
		});
	*/
	
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
					 		$("#searchForm").submit();
					 	}
						
					}
				});
	}
		//查询
		$("#search_button").bind("click",function(){
			
			var threeDepartment=$.trim($("#threeDepartment").val());
			var operatorName=$.trim($("#operatorName").val());
			
			var auditStatus=$("#auditStatus").val();
			if(auditStatus=='UNPROCESSED')
			{
			   //start Modify by xuehualing 2014/05/30 
				//if(threeDepartment=='' && operatorName=='')
				//{
				//	alert("选择组和员工工号至少二选一");
				//	return;
				//}else{
				
				//}
				var threeDepartmentSize =  $('#threeDepartment').find('option').size();//三级部门个数
				var secondDepartment=$.trim($("#secondDepartment").val());//二级部门
				if(operatorName==''){
			         if(secondDepartment==''){
			            alert('请选择二级部门');
			            return ;
			         }
			        if(threeDepartmentSize>1&&threeDepartment==''){
			          alert('员工号和选择组必选一个');
			              return;
			         }
			      }
				 //end  Modify by xuehualing 2014/05/30 
			}
			
			//alert($("#searchForm").attr("action"));
			
			$("#searchForm").submit();
		});
		
		//清空
		$("#clear_button").bind("click",function(){
			window.location.href = "/vst_order/ord/order/monitor/queryOrderConsoleMonitor.do";
		});
		
		function showLog(userId){
	 	var param="objectType=PERM_USER&objectId="+userId+"&sysName=VST";
	    /*showLogDialog = new xDialog("/vst_order/ord/order/workStatus/logList.do",data,{title:"查看日志",width:1200});*/
         showLogDialog=new xDialog("/lvmm_log/bizLog/showVersatileLogList.do?"+param,{},{title:"查看日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
	 }
</script>
