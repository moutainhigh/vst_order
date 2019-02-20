<#--页眉-->
<#import "/base/spring.ftl" as s/>
<#import "/base/pagination.ftl" as pagination>
<!DOCTYPE html>
<html>
<head>
	<title>订单管理-我的工作台</title>
</head>
<body>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/ord/order/queryMyOrderList.do" method="post">
		<input type="hidden" name="checkedTab" value="MYORDER"/>
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                    <td>
                    	<label>订单处理人：
                    		<@s.formInput "monitorCnd.operatorName" 'class="w10" readonly="true"  required="true"'/>
                    	</label>
                    	<a href="javascript:updateOrderUser()">修改订单处理人</a> 
                    	<label>下单时间：
                    		<input id="d4321" class="Wdate" type="text" value="${monitorCnd.createTimeBegin}" 
                        				onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00'})" 
                        				errorele="selectDate" 
                        				name="createTimeBegin" 
                        				readonly="readonly">
                    	 		- 
                    		<input id="d4322" class="Wdate" type="text" value="${monitorCnd.createTimeEnd}" 
                    	 				onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
                    	  				errorele="selectDate" 
                    	  				name="createTimeEnd">
                    	</label>
                        <label>游玩/入住时间：
                        	  <@s.formInput "monitorCnd.visitTimeBegin" 'class="w10" onClick="WdatePicker({readOnly:true})"'/>
                        	  	-
		                	  <@s.formInput "monitorCnd.visitTimeEnd" 'class="w10" onClick="WdatePicker({readOnly:true})"'/>
                        </label>
                    </td>
                </tr>
                <tr>
                	<td>
                        <label>处理方式：
                        	  <@s.formSingleSelect "monitorCnd.handlingMode" handlingModeMap 'class="w10"'/>
                        </label>
                        <label>活动列表：
                        	  <@s.formCheckboxes1 "monitorCnd.activityCodeList" allAuditTypeMap '' ''/>
                        </label>                        
                     </td>
                 </tr>
                 <tr>
                    <td>
                        <label>订单状态：
                        	<@s.formSingleSelect "monitorCnd.orderStatus" orderStatusMap 'class="w10"'/>
                        </label>
                        <label>出团通知状态：
                        	<@s.formSingleSelect "monitorCnd.noticeRegimentStatus" noticeRegimentStatusMap 'class="w10"'/>
                        </label>
                        <label>供应商名称：
		                	<input type="hidden" name="supplierId" id="supplierId" value="${monitorCnd.supplierId!''}" />
		               	 	<input type="text" class="searchInput" name="supplierName" id="supplierName" value="${monitorCnd.supplierName!''}" />                        
                        </label>
                    </td>
                </tr>
                 <tr>
                    <td>
                        <label>订单后置状态：
                        	<@s.formSingleSelect "monitorCnd.orderPostStatus" orderPostMap 'class="w10"'/>
                        </label>
                        <label>订单锁定状态：
                        	<@s.formSingleSelect "monitorCnd.orderLockStatus" orderLockMap 'class="w10"'/>
                        </label>
   
                    </td>
                </tr>
            </tbody>
        </table>  		
		<input type="hidden" name="activityName" id="activityName" value="" />
		<input type="hidden" name="activityDetail" id="activityDetail" value="" />
		
		<div class="operate mt20" style="text-align:center">
        	<a class="btn btn_cc1" id="search_button">查询</a>
        	<a class="btn btn_cc1" id="clear_button">清空</a>
        </div>
        
        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
        <div id="createTimeRequiredErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请输入下单开始时间！</div>
	</form>
</div>
<br/>

<#--结果显示-->
<div id="result" class="iframe_content mt20">
<#if resultPage?? >
	<#if resultPage.items?size gt 0 >
			<div class="p_box">
				<table class="p_table table_center">
					<thead>
						<tr>
							<th nowrap="nowrap">是否主订单</th>
							<th nowrap="nowrap">订单号</th>
							<th nowrap="nowrap">订单处理人</th>
							<th nowrap="nowrap">客人姓名及电话</th>
							<th nowrap="nowrap">订购数量</th>
							<th nowrap="nowrap">销售价</th>
							<th nowrap="nowrap">结算价</th>
							<th nowrap="nowrap">产品ID</th>
							<th nowrap="nowrap">产品名称</th>
							<th nowrap="nowrap">供应商名称</th>
							<th nowrap="nowrap">下单时间</th>
							<th nowrap="nowrap">游玩时间</th>
							<th nowrap="nowrap">订单状态</th>
							<th nowrap="nowrap">支付状态</th>
							<th nowrap="nowrap">出团通知书状态</th>
                            <th nowrap="nowrap">是否后置</th>
                            <th nowrap="nowrap">是否锁定</th>
							<th nowrap="nowrap">操作</th>
						</tr>
					</thead>
					<tbody>
						<#list resultPage.items as orderId>
							<#if myOrderMap[orderId] ??>
								<#assign orderMonitorRst = myOrderMap[orderId] >
								<tr>
								<td>
									<#if orderMonitorRst.isMainOrder == "Y">
										是
									<#else>
										否
									</#if>
								</td>
								<td>
									<a title="点击查看订单详情" href='/vst_order/order/ordCommon/showOrderDetails.do?objectId=<#if orderMonitorRst.isMainOrder == "Y">${orderMonitorRst.orderId}<#else>${orderMonitorRst.orderItemId}</#if>&objectType=<#if orderMonitorRst.isMainOrder == "Y">ORDER<#else>ORDER_ITEM</#if>' target="_blank">
										<#if orderMonitorRst.isMainOrder == "Y">
											${orderMonitorRst.orderId}
										<#else>
											${orderMonitorRst.orderItemId}
										</#if>
									</a>
								</td>
								<td>${orderMonitorRst.responsibleName ! ''}</td>
								<td>${orderMonitorRst.contactName}<#if orderMonitorRst.contactMobile?? && orderMonitorRst.contactMobile != ''>(${orderMonitorRst.contactMobile})</#if></td>
								<td>${orderMonitorRst.buyCount ! ''}</td>
								<td>${orderMonitorRst.price ! ''}</td>
								<td>${orderMonitorRst.actualTotalSettlementPrice ! ''}</td>
								<td>${orderMonitorRst.productId}</td>
								<td>${orderMonitorRst.productName}</td>
								<td>${orderMonitorRst.supplierName ! ''}</td>
								<td>${orderMonitorRst.createTime ! ''}</td>
								<td>${orderMonitorRst.visitTime ! ''}</td>
								<td>${orderMonitorRst.orderStatus ! ''}</td>
								<td>${orderMonitorRst.paymentStatus ! ''}</td>
								<td>${orderMonitorRst.noticeRegimentStatusName ! ''}</td>
								<td>
									<#if orderMonitorRst.travellerDelayFlag == 'Y'>
										是
									<#else>
										否
									</#if>
								</td>
								<td>
									<#if orderMonitorRst.travellerLockFlag == 'Y'>
										<font color="red">是</font>
									<#else>
										否
									</#if>
								</td>
								<td>
									<a title="活动详情" href='/vst_order/ord/order/showComAuditDetails.do?objectId=<#if orderMonitorRst.isMainOrder == "Y">${orderMonitorRst.orderId}<#else>${orderMonitorRst.orderItemId}</#if>&objectType=<#if orderMonitorRst.isMainOrder == "Y">ORDER<#else>ORDER_ITEM</#if>' target="_blank">
										<#if orderMonitorRst.isMainOrder == "Y">
											活动详情
										<#else>
											活动详情
										</#if>
									</a>
								</td>
								</tr>
							</#if>
						</#list>
					</tbody>
				</table>
				<#--分页标签-->
				<@pagination.paging resultPage/>
		</div>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关订单，请重新输入相关条件查询！</div>
	</#if>
</#if>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>
<#--js脚本-->
<script type="text/javascript">var currentActivityDetail = '${monitorCnd.activityDetail}';</script>
<script src="/vst_order/js/workbench/workbench.js"></script>
<script>
	vst_pet_util.commListSuggest("#supplierName", "#supplierId",'/vst_order/ord/order/querySupplierList.do');
	var showSelectEmployeeDialog
	function updateOrderUser(){
		showSelectEmployeeDialog = 
				new xDialog("/vst_order/ord/order/showSelectEmployee.do",
				{},
				{title:"选择订单负责人",width:1000,height:600});
	}
</script>
</body>
</html>