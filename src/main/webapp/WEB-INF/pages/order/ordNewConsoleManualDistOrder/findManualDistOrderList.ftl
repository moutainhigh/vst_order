<#--页眉-->
<#import "/base/spring.ftl" as spring/>
<#import "/base/pagination.ftl" as pagination>

<!DOCTYPE html>
<html>
<head>
<title>订单管理-人工分单</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>

<#--页面导航-->
 <div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="#">首页</a> ></li>
            <li><a href="#">订单管理</a> ></li>
            <li class="active">人工分单</li>
        </ul>
        <br>
 </div>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" name="searchForm" action="/vst_order/order/NewOrderConsole/findManualDistOrderList.do" method="post">
		<table class="s_table2 form-inline">
            <tbody>
            	<tr>
            		<td>
            			<label>活动列表：<@spring.formSingleSelect "comAuditInfo.auditType" auditTypeMap 'class="w9"'/></label>
				        <label>活动状态：<@spring.formSingleSelect "comAuditInfo.auditStatus" auditStatusMap 'class="w9"'/></label>
                 	</td>
                 	<td>
            			<label>订单品类：<@spring.formSingleSelect "comAuditInfo.categoryId" categoryMap 'class="w9"'/></label>
				        <label>类型：<@spring.formSingleSelect "comAuditInfo.stockFlag" stockFlagMap 'class="w9"'/></label>
                 	</td>
                </tr>   
                <tr>
                    <td width="" colspan="2">
                    	<label>一级部门：
                    	<#--<select id="firstDepartment"><option value="1">呼叫中心</option><option value="2">001</option></select>　-->
						<@spring.formSingleSelect   "ordAuditConfigInfo.firstDepartment"  firstDepMap  'class="w9"'/>
						</label>
			            <label>二级部门：
			            <#--<select id="secondDepartment"><option value="1">呼叫中心</option><option value="2">001</option></select>-->　
			            <@spring.formSingleSelect   "ordAuditConfigInfo.secondDepartment" secondDepMap 'class="w9"'/>
			            </label>
			            <label>选择组：
			            <@spring.formSingleSelect "ordAuditConfigInfo.threeDepartment" threeDepMap 'class="w9"'/>
			            </label>
			            <label>员工工号：
			            <@spring.formInput   "comAuditInfo.operatorName" 'class="w9"'/>
			            </label> 
                    </td>
                 </tr>
                 <tr>
                    <td>
	                    <label>入库时间：
	                    <input id="d4321" class="Wdate" type="text" value="${monitorCnd.createTimeBegin}" 
                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00'})" errorele="selectDate" name="createTimeBegin" readonly="readonly">
	                    	 -- 
	                    	 <input id="d4322" class="Wdate" type="text" value="${monitorCnd.createTimeEnd}" 
	                    	 onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
	                    	  errorele="selectDate" name="createTimeEnd"></label>
	                   	<label style="color:red;">(*查询时间必填且范围小于1个月)</label>
	                </td>
	              </tr>
	              <tr>
	              	<td>
			            <label>入住日期：<@spring.formInput "monitorCnd.visitTimeBegin" 'class="w9" onClick="WdatePicker({readOnly:true})"'/>—<@spring.formInput "monitorCnd.visitTimeEnd" 'class="w9" onClick="WdatePicker({readOnly:true})"'/></label>
			            <label>人工分单时间：<@spring.formInput "monitorCnd.distributionTimeBegin" 'class="w9" onClick="WdatePicker({readOnly:true})"'/>— <@spring.formInput "monitorCnd.distributionTimeEnd" 'class="w9" onClick="WdatePicker({readOnly:true})"'/></label>
				     	
				     </td>
                </tr>
            </tbody>
        </table>
        <span  class="notnull" >        
        温馨提示：<br>
        1,活动状态为待分配时，灰掉的查询条件将不生效(因为此时订单未分配到人)<br>
        2,活动状态为未处理时，查询条件中的"选择组"和"员工工号"两者至少必填一项
        </span>

        <p class="tc mt20 operate">
        <a class="btn btn_cc1" id="search_button">查询</a>
        <a class="btn btn_cc1" id="clear_button">清空</a>
       
        </p>
    </div>
    
    
    </form>

<#--结果显示-->
			<div class="iframe_content">
		        <table class="p_table table_center">
		            <thead>
		                <tr>
		                	<th nowrap="nowrap">选择</th>
		                    <th nowrap="nowrap">订单来源</th>
		                    <th nowrap="nowrap" >订单号</th>
		                    <th nowrap="nowrap" style="width:10%;">商品信息</th>
		                    <th nowrap="nowrap">下单日期</th>
		                    <th nowrap="nowrap">离店日期</th>
		                    <th nowrap="nowrap">客人</th>
		                    <th nowrap="nowrap" >活动名称</th>
		                    <th nowrap="nowrap">当前处理人</th>
		                    <th  nowrap="nowrap">操作</th>
						</tr>
					</thead>
					<tbody>
						<#list resultList  as comAuditInfo>
							<tr>
							
								<td><input type="checkbox" name="auditIds" value="${comAuditInfo.auditId}_${comAuditInfo.auditStatus}"></td>
								
								<td>
								<#if comAuditInfo.orderMonitorRst.distributorName?exists>
								${comAuditInfo.orderMonitorRst.distributorName}
								<#else>
								</#if>
								</td>
								<td>
								<#if comAuditInfo.orderMonitorRst.orderId?exists>
								${comAuditInfo.orderMonitorRst.orderId}
								<#else>
								</#if>
									
								</td>
								<td>
								<#if comAuditInfo.orderMonitorRst.productName?exists>
								${comAuditInfo.orderMonitorRst.productName}
								<#else>
								</#if>
								</td>
								<td>
								<#if comAuditInfo.orderMonitorRst.createTime?exists>
								${comAuditInfo.orderMonitorRst.createTime}
								<#else>
								</#if>
								</td>
								<td>
								<#if comAuditInfo.orderMonitorRst.visitTime?exists>
								${comAuditInfo.orderMonitorRst.visitTime}
								<#else>
								</#if>
								</td>
								<td>
								<#if comAuditInfo.orderMonitorRst.contactName?exists>
								${comAuditInfo.orderMonitorRst.contactName}
								<#else>
								</#if>
								</td>
								<td>
								<#if comAuditInfo.auditTypeName?exists>
								${comAuditInfo.auditTypeName}
								<#else>
								</#if>
								</td>
								<td>
								<#if comAuditInfo.operatorName?exists>
								${comAuditInfo.operatorName}
								<#else>
								</#if>
								</td>
								<td>
									<a href="javascript:;" class="editProp" data=${comAuditInfo.auditId}_${comAuditInfo.auditStatus}>分单</a>
								</td>
							</tr>
						</#list>
					</tbody>
				</table>
	           	<@pagination.paging pageParam>
					<a class="btn btn_cc1" id="checkAll">全选</a><a class="btn btn_cc1" id="distOrder">批量分单</a>
				</@pagination.paging>
	            </div>
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>

<script src="/vst_order/js/vst_department_util.js"></script>

<#--js脚本-->
<script type="text/javascript">
	var updateDistOrderDialog;
	var findManualListDialog=window;
	
	$(function () {
       disabledSearchCondition("${comAuditInfo.auditStatus}");
	})
	
	function  disabledSearchCondition(auditStatus){
			
	
			if(auditStatus=='POOL')
			{
				var url="/vst_order/order/NewOrderConsole/findManualDistOrderList.do"+
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
				var url = "/vst_order/order/NewOrderConsole/findManualDistOrderList.do";
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
	
		//查询
		$("#search_button").bind("click",function(){
			var createTimeBegin = $("input[name='createTimeBegin']").val();
			var createTimeEnd = $("input[name='createTimeEnd']").val();
			if(createTimeBegin=='' || createTimeEnd=='' || createTimeBegin==null || createTimeEnd==null){
				alert("下单时间必选!");
				return;
			}else{
				var days = GetDateDiff(createTimeBegin, createTimeEnd, "day");
				if(days > 30){
					alert("下单时间范围必须1个月内!");
					return;
				}
			}
			
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
			window.location.href = "/vst_order/order/NewOrderConsole/showManualDistOrderList.do";
		});
		
		$("a.editProp").bind("click",function(){
			var auditIdStatus=$(this).attr("data");
			updateDistOrderDialog = new xDialog("/vst_order/order/NewOrderConsole/showManualDistOrder.do",{"auditIdStatus":auditIdStatus,"oneData":"true"},{title:"分单对象选择",width:300});
		});
		
		$("#distOrder").bind("click",function(){
			  var chk_value =[];    
		      $('input[name="auditIds"]:checked').each(function(){    
		       chk_value.push($(this).val());    
		      });    
		      if(chk_value.length==0)
		      {
		      	$.alert('尚未选中任何订单活动'); 
		      	return;
		      }
		      var auditIdStatus=chk_value+"";
			  updateDistOrderDialog = new xDialog("/vst_order/order/NewOrderConsole/showManualDistOrder.do",{"auditIdStatus":auditIdStatus,"oneData":"false"},{title:"分单对象选择",width:300});
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
		
		/*
		* 获得时间差,时间格式为 年-月-日 小时:分钟:秒 或者 年/月/日 小时：分钟：秒
		* 其中，年月日为全格式，例如 ： 2010-10-12 01:00:00
		* 返回精度为：秒，分，小时，天
		*/
		function GetDateDiff(startTime, endTime, diffType) {
			//将xxxx-xx-xx的时间格式，转换为 xxxx/xx/xx的格式
			startTime = startTime.replace(/-/g, "/");
			endTime = endTime.replace(/-/g, "/");
			//将计算间隔类性字符转换为小写
			diffType = diffType.toLowerCase();
			var sTime = new Date(startTime); //开始时间
			var eTime = new Date(endTime); //结束时间
			//作为除数的数字
			var divNum = 1;
			switch (diffType) {
			case "second":
			divNum = 1000;
			break;
			case "minute":
			divNum = 1000 * 60;
			break;
			case "hour":
			divNum = 1000 * 3600;
			break;
			case "day":
			divNum = 1000 * 3600 * 24;
			break;
			default:
			break;
			}
			return parseInt((eTime.getTime() - sTime.getTime()) / parseInt(divNum)); 
		} 
</script>
