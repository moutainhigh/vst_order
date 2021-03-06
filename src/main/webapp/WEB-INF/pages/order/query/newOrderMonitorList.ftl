<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单监控</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>
<#import "/base/paginationMonitor.ftl" as pagination>
<#--页面导航-->
<div class="iframe_header">
	<i class="icon-home ihome"></i>
	<ul class="iframe_nav">
		<li><a href="#">首页</a> &gt;</li>
		<li><a href="#">订单管理</a> &gt;</li>
		<li class="active">订单列表</li>
	</ul>
</div>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/ord/order/newOrderMonitorList.do" method="post">
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                	<td class="s_table2_t" width="14" rowspan="4">订单信息</td>
                    <td width="" colspan="2">
                        <input type="hidden" id= "requiredFlg" value="${orderNoRequired}"/>
                    	<label>订单编号：<@s.formInput "monitorCnd.orderId" 'class="w9" number="true"'/></label>
                    	<label>子订单编号：<@s.formInput "monitorCnd.orderItemId" 'class="w9" number="true"'/></label>
                        <label>下单人工号：<@s.formInput "monitorCnd.backUserId" 'class="w9"'/></label>
                        <label>下单时间：
                        	<input id="d4321" class="Wdate" type="text" value="${monitorCnd.createTimeBegin}" 
                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00'})" errorele="selectDate" name="createTimeBegin" readonly="readonly">
	                    	 -- 
	                    	 <input id="d4322" class="Wdate" type="text" value="${monitorCnd.createTimeEnd}" 
	                    	 onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
	                    	  errorele="selectDate" name="createTimeEnd">
	                    </label> 
                    </td>
                 </tr>
                 <tr>
                    　<td colspan="2">
	                        <label>订单状态：<@s.formSingleSelect "monitorCnd.orderStatus" orderStatusMap 'class="w10"'/></label>
	                        <label>信息审核：<@s.formSingleSelect "monitorCnd.infoStatus" infoStatusMap 'class="w10"'/></label>
	                        <label>资源审核：<@s.formSingleSelect "monitorCnd.resourceStatus" resourceStatusMap 'class="w10"'/></label>
	                        <label>支付状态：<@s.formSingleSelect "monitorCnd.paymentStatus" paymentStatusMap 'class="w10"'/></label>
	                        <label>支付时间：
	                        	<@s.formInput "monitorCnd.paymentTimeBegin" 'class="w7" onClick="WdatePicker({readOnly:true})"'/>
		                    	-- 
		                    	<@s.formInput "monitorCnd.paymentTimeEnd" 'class="w7" onClick="WdatePicker({readOnly:true})"'/>
	                        </label>
	                        <label>凭证确认状态：<@s.formSingleSelect "monitorCnd.certConfirmStatus" certConfirmStatusMap 'class="w10"'/></label>
	                        
	                        
	                         <label>合同状态：<@s.formSingleSelect "monitorCnd.contractStatus" contractStatusMap 'class="w10"'/></label>
	                        <label>出团通知书状态：<@s.formSingleSelect "monitorCnd.noticeRegimentStatus" noticeRegimentStatusMap 'class="w10"'/></label>
                    
                    </td>
                </tr>
                <tr>
                    <td class="qudao_t">下单渠道：</td>
                    <td>
                    	<input type="hidden" id="moreFlag" value="1">
                    	<span id="moreInfoSpan" class="qudao_r"><a href="javascript:moreInfo();"><b>更多</b><i></i></a></span>
                    	<!--去掉qudao_list_up这个class，箭头就向下指-->
                        <div id="moreInfoDiv" class="qudao_list">
                        <!--展开只需去掉h22这个class-->
                            <@s.formCheckboxes1 "monitorCnd.distributorIds" distributorMap "" ""/>
                        </div>
                    </td>
                </tr>
                <tr>
                	<td class="qudao_t" style="width:90px;">显示测试订单：</td>
                	<td>
                		<label><@s.formSingleSelect "monitorCnd.isTestOrder" isTestOrderMap 'class="w10"'/></label>
                	</td>
                </tr>
            </tbody>
        </table>
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                	<td class="s_table2_t" width="14" rowspan="3">产品信息</td>
                    <td colspan="2">
                    	<label>产品编号：<@s.formInput "monitorCnd.productId" 'class="w10" number="true"'/></label>
                    	<label>产品名称：<@s.formInput "monitorCnd.productName" 'class="14"'/></label>
                        <label>商品名称：<@s.formInput "monitorCnd.suppGoodsName" 'class="14"'/></label>
                        <label>商品ID：	<@s.formInput "monitorCnd.suppGoodsId" 'class="14" number="true"'/></label>
                        <label>
                                                                                                          所属产品经理:
                              <@s.formHiddenInput "monitorCnd.managerId"/>
                              <@s.formInput "monitorCnd.managerName" 'class="search"'/>
                        </label>
                        <label>所属BU：<@s.formSingleSelect "monitorCnd.belongBU" belongBUMap 'class="w14"'/></label>
                     </td>
                 </tr>
                 <tr>
                    　<td colspan="2">
                    	<label>产品类型：</label>
                    	<@s.formCheckboxes1 "monitorCnd.categoryIdList" productCategoryMap "" ""/></br>
                        <label>供应商名称：
                        	<@s.formHiddenInput "monitorCnd.supplierId"/>
                        	<@s.formInput "monitorCnd.supplierName" 'class="search"'/>
                        </label>
                        <label>商品支付方式：<@s.formSingleSelect "monitorCnd.payTarget" payTargetMap 'class="w14"'/></label>
                    </td>
            	</tr>
            	<tr>
                	<td class="qudao_t">所属公司：</td>
                    <td>
                        <@s.formCheckboxes1 "monitorCnd.filialeNames" filialeMap "" ""/>
                    </td>
                </tr>
                <tr><td></td></tr>
                <tr><td></td></tr>
            </tbody>
        </table>
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                	<td class="s_table2_t" width="14" rowspan="2">游客信息</td>
                    <td>
                    	<label>驴妈妈账号：<@s.formInput "monitorCnd.bookerName" 'class="w10"'/></label>
                    	<label>已绑定手机号：<@s.formInput "monitorCnd.bookerMobile" 'class="w10"'/></label>
                        <label>出游人姓名：<@s.formInput "monitorCnd.travellerName" 'class="w10"'/></label>
                        <label>入住时间：
                        	<@s.formInput "monitorCnd.visitTimeBegin" 'class="w10" onClick="WdatePicker({readOnly:true})"'/>
	                    	-- 
	                    	<@s.formInput "monitorCnd.visitTimeEnd" 'class="w10" onClick="WdatePicker({readOnly:true})"'/>
                        </label>
                     </td>
                 </tr>
                 <tr>
                    <td>
                        <label>联系人姓名：<@s.formInput "monitorCnd.contactName" 'class="w10"'/></label>
                        <label>联系人邮箱：<@s.formInput "monitorCnd.contactEmail" 'class="w10"'/></label>
                        <label>联系人固话：<@s.formInput "monitorCnd.contactPhone" 'class="w10"'/></label>
                        <label>联系人手机：<@s.formInput "monitorCnd.contactMobile" 'class="w10"'/></label>
                    </td>
                </tr>
            </tbody>
        </table>        
        <div class="operate mt20" style="text-align:center">
				<a class="btn btn_cc1" id="search_button">查询</a>
				<a class="btn btn_cc1" id="clear_button">清空</a>
        </div>
        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
	    <div id="requiredErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>订单号或者子订单号至少要输入一个！</div>
	     <div id="createTimeErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>当输入产品名称或者商品名称时，必须输入下单时间！</div>
	</form>
</div>

<#--结果显示-->
<div id="result" class="iframe_content mt20">
<#if resultPage?? >
	<#if resultPage.items?size gt 0 >
			<div class="p_box">
				<table class="p_table table_center">
					<thead>
						<tr>
							<th nowrap="nowrap" style="width:10%;">下单渠道</th>
							<th nowrap="nowrap" style="width:10%;">订单号</th>
							<th nowrap="nowrap" style="width:25%;">产品名称</th>
							<th nowrap="nowrap" style="width:10%;">支付方式</th>
							<th nowrap="nowrap" style="width:5%;">订购数量</th>
							<th nowrap="nowrap" style="width:10%;">下单时间</th>
							<th nowrap="nowrap" style="width:10%;">游玩日期</th>
							<th nowrap="nowrap" style="width:5%;">联系人</th>
							<th nowrap="nowrap" style="width:10%;">当前状态</th>
							<th nowrap="nowrap" style="width:5%;">所属BU</th>
						</tr>
					</thead>
					<tbody>
						<#list resultPage.items as result>
							<tr>
								<td>${result.distributorName}</td>
								<td>
									<a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?orderId=${result.orderId}" target="_blank">
										${result.orderId}
									</a>
									<#if result.guarantee == 'GUARANTEE'>
									<a title="该订单需要担保">#</a>
									</#if>
								</td>
								<td>${result.productName}</td>
								<td>${result.payTarget}</td>
								<td>${result.buyCount}</td>
								<td>${result.createTime}</td>
								<td>${result.visitTime}</td>
								<td>${result.contactName}</td>
								<td>${result.currentStatus}</td>
								<td>${result.belongBU}</td>
							</tr>
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
</body>
</html>

<#--js脚本-->
<script type="text/javascript">
	$(function(){
	
		var globalTime=$("input[name='createTimeBegin']").val();
		//查询
		$("#search_button").bind("click",function(){
			if($(this).attr("disabled")) {
				return;
			}
			
			$("#result").empty();
			
			// 去除input空格
			var tempTextFields = $("input[type=text]");
			$.each(tempTextFields,function(){
			 	this.value = $.trim(this.value);
			});
			
			//表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			////
			var requiredFlg=$("#requiredFlg").val();
			var orderNo=$("#orderId").val();
			var orderItemNo=$("#orderItemId").val();
			if(requiredFlg=="Y"){
				//订单号或者子订单号必须输入一个
				if(orderNo == ""&& orderItemNo == ""){
				$("#requiredErrorMessage").show();
				return;
				}else{
				$("#requiredErrorMessage").hide();
				}
			}
			////			
			
			
			//遍历所有查询条件的值
			var value = "";
			
			//input
			var textFields = $("input[type=text]");
			$.each(textFields,function(){
				var cs = $(this).attr("class");
				if(cs.indexOf("search") < 0){
					value += this.value;
				}
			});
			
			//select
			var selectFields = $("select");
			$.each(selectFields,function(){
			 	value += this.value;
			});
			
			//checkbox
			var checkboxFileds = $("input[type=checkbox]");
			$.each(checkboxFileds,function(){
				if(this.checked){
			 		value += this.value;
				}
			});
			var supplierId = $("#supplierId").val();
			var managerId  = $("#managerId").val();
			if(supplierId != ""){
				value = supplierId;
			}
			if(managerId != ""){
				value = managerId;
			}
			//客户必须输入一个条件
			if(value == ""){
				$("#errorMessage").show();
				return;
			}else{
				$("#errorMessage").hide();
			}
			
			//当填写产品名称或者商品名称时，必须填写下单时间
			var suppGoodsName=$("#suppGoodsName").val();
			var productName=$("#productName").val();
		    var createTimeBegin=$("input[name='createTimeBegin']").val();
		    var createTimeEnd=$("input[name='createTimeEnd']").val();
			if(suppGoodsName.length>0||productName.length>0){
				if(createTimeBegin.length==0&&createTimeEnd.length==0){
					$("#createTimeErrorMessage").show();
					return;
				}else{
					$("#createTimeErrorMessage").hide();
				}
			}
			
			//假加载效果
			$("#result").empty();
			$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
		
			$(this).attr("disabled", true);
			$(this).css("border","1px solid #aaa").css("background-color","#bbb").css("color", "#000").css("pointer","");
			$("#searchForm").submit();
		});
		
		//清空
		$("#clear_button").bind("click",function(){
			window.location.href = "/vst_order/ord/order/intoOrderMonitor.do";
		});
		
		$("#supplierName").jsonSuggest({
			url:"${rc.contextPath}/ord/order/querySupplierList.do",
			maxResults: 20,
			minCharacters:1,
			onSelect:function(item){
				$("#supplierId").val(item.id);
			}
		});
		
		$("#managerName").jsonSuggest({
			url:"${rc.contextPath}/ord/order/queryPermUserList.do",
			maxResults: 20,
			minCharacters:1,
			onSelect:function(item){
				$("#managerId").val(item.id);
			}
		});
		
		//当填写了订单编号、子订单编号、人工下单号、游客信息等字段时，清空下单开始时间
		$("#orderId,#orderItemId,#backUserId,#bookerName,#bookerMobile,#travellerName,#contactName,#contactEmail,#contactPhone,#contactMobile").blur(function(){
				var orderId=$("#orderId").val();
				var orderItemId=$("#orderItemId").val();
				var backUserId=$("#backUserId").val();
				var bookerName=$("#bookerName").val();
				var bookerMobile=$("#bookerMobile").val();
				var travellerName=$("#travellerName").val();
				var contactName=$("#contactName").val();
				var contactEmail=$("#contactEmail").val();
				var contactPhone=$("#contactPhone").val();
				var contactMobile=$("#contactMobile").val();
				if(orderId.length>0||orderItemId.length>0||
					backUserId.length>0||bookerName.length>0||
					bookerMobile.length>0||travellerName.length>0||
					contactName.length>0||contactEmail.length>0||
					contactPhone.length>0||contactMobile.length>0){
						var createTimeBegin=$("input[name='createTimeBegin']").val();
						if(globalTime==createTimeBegin){
							$("input[name='createTimeBegin']").attr("value","");
						}
				}
		});
		
	});
	
	// 按enter键提交查询
	$(document).keyup(function(event){
	  if(event.keyCode ==13){
	    $("#search_button").trigger("click");
	  }
	});
	
	//更多按钮事件
	function moreInfo(){
		try{
			var moreFlag = $("#moreFlag").val();
			if(moreFlag=="1"){
				$("#moreInfoSpan").removeClass();
				$("#moreInfoSpan").addClass("qudao_r qudao_list_up");
				
				$("#moreInfoDiv").removeClass();
				$("#moreInfoDiv").addClass("qudao_list");
				
				$("#moreFlag").val("2");
			}else{
				$("#moreInfoSpan").removeClass();
				$("#moreInfoSpan").addClass("qudao_r");
				
				$("#moreInfoDiv").removeClass();
				$("#moreInfoDiv").addClass("qudao_list h22");
				
				$("#moreFlag").val("1");
			}
		}catch(e){
			alert(e.message);
		}
	}
</script>
