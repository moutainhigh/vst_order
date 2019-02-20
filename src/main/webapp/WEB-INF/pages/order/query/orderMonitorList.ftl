<#assign voa=JspTaglibs["/WEB-INF/pages//tld/vstOrgAuthentication-tags.tld"]>
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
	<form id="searchForm" action="/vst_order/ord/order/orderMonitorList.do" method="post">
	    <input type="hidden" name="callid" id="callid" value="${callid}" />
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                	<td class="s_table2_t" width="14" rowspan="4">订单信息</td>
                    <td width="" colspan="2">
                        <input type="hidden" id= "requiredFlg" value="${orderNoRequired}"/>
                    	<label>订单编号：<@s.formInput "monitorCnd.orderId" 'class="w9" number="true"'/></label>
                    	<label>子订单编号：<@s.formInput "monitorCnd.orderItemId" 'class="w9" number="true"'/></label>
                        <label>下单人工号：<@s.formInput "monitorCnd.backUserId" 'class="w9"'/></label>
                        <label>审核人工号：<@s.formInput "monitorCnd.responsiblePerson" 'class="w9"'/></label>
                        <label>下单时间：
                        	<input id="d4321" class="Wdate" type="text" value="${monitorCnd.createTimeBegin}" 
                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00'})" errorele="selectDate" name="createTimeBegin" readonly="readonly">
	                    	 -- 
	                    	 <input id="d4322" class="Wdate" type="text" value="${monitorCnd.createTimeEnd}" 
	                    	 onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
	                    	  errorele="selectDate" name="createTimeEnd">
	                    	<input type="hidden" id="previousDate" value="${previousDate}"/>
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
							<label class="checkbox mr10"><input id="" name="distributorIdForWepAndApp" value="Y" type="checkbox" <#if monitorCnd.distributorIdForWepAndApp == "Y"> checked="checked"</#if>>无线订单(App、Wap、不含线下推广)</label>
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
                                                                                                          产品经理:
                              <@s.formHiddenInput "monitorCnd.managerId"/>
                              <@s.formInput "monitorCnd.managerName" 'class="search"'/>
                              <input type="radio" name="whichManagerId" value="managerId" <#if monitorCnd.whichManagerId?? && monitorCnd.whichManagerId=="managerId">checked="checked"</#if>/>主订单
					    	  <input type="radio" name="whichManagerId" value="itemManagerId" <#if monitorCnd.whichManagerId?? && monitorCnd.whichManagerId=="itemManagerId">checked="checked"</#if>/>子订单
                        </label>
                        <label>所属BU：<@s.formSingleSelect "monitorCnd.belongBU" belongBUMap 'class="w14"'/></label>
                     </td>
                 </tr>
                 <tr>
                    　<td colspan="2">
                    	<label>产品类型：</label>
                    	<@s.formCheckboxes1 "monitorCnd.categoryIdList" productCategoryMap "" ""/></br>
                    	
						<div id='sel_children' style="display: none;">
							<#list freetourList as list>
								<br/><input type='checkbox' name='subCategoryIdList' value='${list.categoryId}' 
								<#list monitorCnd.subCategoryIdList as categoryId>
									<#if categoryId?? && list.categoryId == categoryId>checked</#if>
								</#list>
								/>${list.categoryName!''}
							</#list>
						</div>
						
                        <label>供应商名称：
                        	<@s.formHiddenInput "monitorCnd.supplierId"/>
                        	<@s.formInput "monitorCnd.supplierName" 'class="search"'/>
                        </label>
                        <label>商品支付方式：<@s.formSingleSelect "monitorCnd.payTarget" payTargetMap 'class="w14"'/></label>
                        <label>
                        	<div id="stockFlagDiv" style="display:none;">
                        	房间类型：<@s.formSingleSelect "monitorCnd.stockFlag" stockFlagMap 'class="w14"'/>
                        	</div>
                        </label>
                    </td>
            	</tr>
            	<tr>
                	
                    <td colspan="2">
                       <label>  所属公司：<@s.formSingleSelect "monitorCnd.filialeNames" filialeMap 'class="w14 filialeCombobox"'/></label>
                        <label>
                        	<div id="performStatusFlagDiv" style="display:none;">
                        	使用状态：<@s.formSingleSelect "monitorCnd.performStatus" performStatusMap 'class="w14"'/>
                        	</div>
                        </label>  
                    </td>
                </tr>
            </tbody>
        </table>
        <table class="s_table2 form-inline" style="margin-top:0;">
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
        <div>
        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
	    <div id="requiredErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>订单号或者子订单号至少要输入一个！</div>
	    <div id="timeConstrictionErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>未输入订单号、子订单号时，只能查询三个月之间的数据！</div>
	    <div id="createTimeErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>当输入产品名称或者商品名称时，必须输入下单时间！</div>
	    <div id="createTimeErrorMessageBoth" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>未输入订单号、子订单号时，订单创建开始不能为空!</div>
	    <div id="visitTimeErrorMessageBoth" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>未输入订单号、子订单号时且未选择订单创建时间时，入住开始或结束时间不能为空!</div>
	    <div id="requiredCreateTimeErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>未输入订单号、子订单号时，下单开始时间和结束时间(或入住开始时间和结束时间为必填)！</div>
		<div id="visitTimeNoCreateTimeErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>未输入订单号、子订单号且未选择订单创建时间时，只能查询入住时间三个月之间的数据！</div>
		<div id="managerIdTimeConstrictionErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>当输入产品经理时，只能查询7天内数据!</div>
		<div id="managerBuIdTimeConstrictionErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>当输入产品经理时且输入所属BU，只能查询两个月内数据!</div>
		</div>
	    
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
							<th nowrap="nowrap" style="width:10%;">是否测试订单</th>
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
								<td><#if result.isTestOrder=='Y'>是<#else>否</#if></td>
								<td>${result.distributorName}</td>
								<td>
									<!-- 订单权限 -->
									<#assign isPermission =false>
									<@voa.checkPerm managerIdPerm="${result.managerIdPerm!''}">
										<#assign isPermission =true>
									</@voa.checkPerm>

									<#if isPermission?? && isPermission=false>
										${result.orderId}
										<#else>	
										<a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?orderId=${result.orderId}&callid=${callid}" target="_blank">
											${result.orderId}
										</a>

									</#if>
									<#if result.orderSubType == 'STAMP'>
								         <br><p style="color:red">(预售券)</p>
								    </#if>	

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
			//var suppGoodsName=$("#suppGoodsName").val();
			//var productName=$("#productName").val();
		    //var createTimeBegin=$("input[name='createTimeBegin']").val();
		    //var createTimeEnd=$("input[name='createTimeEnd']").val();
			//if(suppGoodsName.length>0||productName.length>0){
			//	if(createTimeBegin.length==0&&createTimeEnd.length==0){
			//		$("#createTimeErrorMessage").show();
			//		return;
			//	}else{
			//		$("#createTimeErrorMessage").hide();
			//	}
			//}
			
			//未输入订单号、子订单号时，只能查询三个月之间的数据
			var previousNow = $("#previousDate").val();
			var createTimeBegin=$("input[name='createTimeBegin']").val();
			var createTimeEnd=$("input[name='createTimeEnd']").val();
		    var orderId=$("#orderId").val();
			var orderItemId=$("#orderItemId").val();
			var managerId=$("#managerId").val();
			var visitTimeBegin = $("input[name='visitTimeBegin']").val();
			var visitTimeEnd =   $("input[name='visitTimeEnd']").val();
			var visitTimeBeginDate = null;
			var visitTimeEndDate  = null;
			var belongBU  = $("#belongBU").val();
			
			if(orderId == "" && orderItemId == ""){
			
				if(createTimeBegin==""){
					if(!(visitTimeBegin !="" && visitTimeEnd!="")){
					$("#visitTimeErrorMessageBoth").siblings().hide();
					$("#visitTimeErrorMessageBoth").show();
						return;
					}
					if(!visitTimeCheck()){
						$("#visitTimeNoCreateTimeErrorMessage").siblings().hide();
						$("#visitTimeNoCreateTimeErrorMessage").show();	
						return;
					}
				}else{
					  var createTimeBeginDate = strToDate(createTimeBegin);
					  var createTimeEndDate=null;
					  if(createTimeEnd==""){
					  	 var timeBegin = strToDate(previousNow);
					  	 if(createTimeBeginDate<timeBegin){
					  	 	$("#timeConstrictionErrorMessage").siblings().hide();
							$("#timeConstrictionErrorMessage").show();
							return;
					  	 }else{
					  	 	$("#timeConstrictionErrorMessage").hide();
					  	 }
					  	 
					  	var today = new Date();
					  	var dayDiff = (today.getTime()-createTimeBeginDate.getTime())/(24*3600*1000);
					  	//产品经理不为空，所属BU为空，只能查询一个星期之内的数据
						//产品经理不为空，所属BU不为空，只能查询两个月之内的数据
						if(managerId!=""&&belongBU==""){
							if(dayDiff > 7){
								$("#timeConstrictionErrorMessage").hide();
								$("#managerIdTimeConstrictionErrorMessage").show();
								return;
							}else{
								$("#managerIdTimeConstrictionErrorMessage").hide();
							}

						
					  	}else if(managerId!=""&&belongBU!=""){
							if(dayDiff > 60){
								$("#timeConstrictionErrorMessage").hide();
								$("#managerBuIdTimeConstrictionErrorMessage").show();
								return;
							}else{
								$("#managerBuIdTimeConstrictionErrorMessage").hide();
							}
								
						}
						
					  }else{
					  	 createTimeEndDate = strToDate(createTimeEnd);
					  	 var dateDiff = (createTimeEndDate.getTime()-createTimeBeginDate.getTime())/(24*3600*1000);
						 if(dateDiff > 90){
								$("#timeConstrictionErrorMessage").siblings().hide();
								$("#timeConstrictionErrorMessage").show();
								return;
						}
						
						//产品经理不为空，所属BU为空，只能查询一个星期之内的数据
						//产品经理不为空，所属BU不为空，只能查询两个月之内的数据
						if(managerId!=""&&belongBU==""){
							if(dateDiff > 7){
								$("#timeConstrictionErrorMessage").hide();
								$("#managerIdTimeConstrictionErrorMessage").show();
								return;
							}else{
								$("#managerIdTimeConstrictionErrorMessage").hide();
							}

						
					  	}else if(managerId!=""&&belongBU!=""){
							if(dateDiff > 60){
								$("#timeConstrictionErrorMessage").hide();
								$("#managerBuIdTimeConstrictionErrorMessage").show();
								return;
							}else{
								$("#managerBuIdTimeConstrictionErrorMessage").hide();
							}
								
						}
						
					  }
				
					 
				}
			}
			
			//假加载效果
			$("#result").empty();
			$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
		
			$(this).attr("disabled", true);
			$(this).css("border","1px solid #aaa").css("background-color","#bbb").css("color", "#000").css("pointer","");
			if ($("#toES").val() == "true") {
				$("#searchForm").attr("action", "/vst_order/ord/order/esearchorder.do");
			}
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
		
		//当填写了订单编号、子订单编号
		$("#orderId,#orderItemId").blur(function(){
				var orderId=$("#orderId").val();
				var orderItemId=$("#orderItemId").val();
				if(orderId.length>0||orderItemId.length>0){
						var createTimeBegin=$("input[name='createTimeBegin']").val();
						if(globalTime==createTimeBegin){
							$("input[name='createTimeBegin']").attr("value","");
						}
				}
		});

		//为下拉框绑定事件，选择自由行时，显示酒+景下拉
		$("input[name='categoryIdList']").bind("click", function(){
			//选择自由行时
			if ($(this)[0].value == 18 && $(this)[0].checked) {
				$("#sel_children").css("position", "absolute")
				.css("top", $(this).offset().top+5).css("left", $(this).offset().left+5)
				.css("background-color", "#407FCC").css("z-index", 999).show();
				//默认勾选全部子品类 
				//董宁波 2016年1月15日 18:08:12 取消联动
				//$("#sel_children input[name='subCategoryIdList']").attr("checked", "checked");
			} else if ($(this)[0].value == 18 && !$(this)[0].checked) {
				//$("#sel_children").hide();
				$("#sel_children input").removeAttr("checked");
			}
		});
		
		//页面刷新后显示正在搜索的项
		/* var freetour = $("input[name='categoryIdList'][value='18']:checked");
		//console.info(freetour);
		if (freetour && freetour.length > 0) {
			$("#sel_children").css("position", "absolute")
				.css("top", freetour.offset().top+5).css("left", freetour.offset().left+5)
				.css("background-color", "#407FCC").show();
		} */
		$(".form-inline label.checkbox").hover(function(){
			var freetour = $(this).find("input[name='categoryIdList']");
			if (freetour[0].value == 18) {
				$("#sel_children").css("position", "absolute")
				.css("top", freetour.offset().top+5).css("left", freetour.offset().left+5)
				.css("background-color", "#407FCC").css("z-index", 999).show();
			}
		}, function(){
			var freetour = $(this).find("input[name='categoryIdList']");
			if (freetour[0].value != 18 && !$("#sel_children").is(":hover")) {
				$("#sel_children").hide();
			}
		});
		$("#sel_children").hover(null, function(){
			$("#sel_children").hide();
		});
		$("#sel_children input").bind("click", function(){
			//选择子品类时
			var freetour = $("input[name='categoryIdList'][value='18']");
			if (!$(freetour).checked && $(this)[0].checked) {
				$(freetour).attr("checked", "checked");
			} else if ($("#sel_children").find("input[name='subCategoryIdList']:checked").length == 0) {
				//董宁波 2016年1月15日 18:04:20 取消联动
				//$(freetour).removeAttr("checked");
			}
		});
		
		var checklist1=[];
		var checklist2=[];
		$("input[name='categoryIdList']").change(function() {
			roomTypeToggle();
			
			
			if($(this)[0].value=='11'||$(this)[0].value=='12'||$(this)[0].value=='13'){
				if($(this)[0].checked){
					checklist1.push($(this)[0].value);
				}else if(!$(this)[0].checked){
					for(var i=0;i<checklist1.length;i++){
						if(checklist1[i]==$(this)[0].value){
							checklist1.splice(i,1);
							break;
						}
					}
				}
			}else{
				if($(this)[0].checked){
					checklist2.push($(this)[0].value);
				}else if(!$(this)[0].checked){
					for(var i=0;i<checklist2.length;i++){
						if(checklist2[i]==$(this)[0].value){
							checklist2.splice(i,1);
							break;
						}
					}
				}
			}
			if(checklist1.length>0&&checklist2.length==0){
				$('#performStatusFlagDiv').show();
				$('#performStatus').attr("disabled", false);
			}else{
				$('#performStatusFlagDiv').hide();
				$('#performStatus').attr("disabled", true);
			
			}
			performStatusToggle();
		});
		
		$('#payTarget').live('change', function (e) {
			roomTypeToggle();
		});
		
		roomTypeToggle();
		performStatusToggle();
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

$("select.filialeCombobox").combobox({
    multiple:false,
    width: 170,
    filter:function(q,row){
		var opts=$(this).combobox("options");
		return row[opts.textField].indexOf(q) > -1;
	}
});

//房间是否保留房查询条件露出
function roomTypeToggle(){
	var payTargetVal = $('#payTarget').val();
	var categoryIdList = [];
	var obj = $("input[name='categoryIdList']");
	for(var i=0;i<obj.length;i++){  
        if(obj[i].checked) {  
           categoryIdList.push(obj[i].value);  
        }
    }  
    if(categoryIdList.length == 1 && categoryIdList[0] == 1 && "PREPAID" == payTargetVal){
    	$('#stockFlagDiv').show();
    }else{
    	$('#stockFlagDiv').hide();
    }
}

function performStatusToggle(){

	var c1=0;
	var c2=0;
	var obj = $("input[name='categoryIdList']");
	for(var i=0;i<obj.length;i++){
        if(obj[i].checked) { 
        	if(obj[i].value=='11'||obj[i].value=='12'||obj[i].value=='13'){
        		c1++;
        	}else{
        		c2++;
        	}
        }
    }
    if( c1>0&&c2==0){
    	$('#performStatusFlagDiv').show();
    	$('#performStatus').attr("disabled", false);
    	
    }else{
    	$('#performStatusFlagDiv').hide();
    	$('#performStatus').attr("disabled", true);
    	
    }
}

function strToDate(str) {
	 var tempStrs = str.split(" ");
	 var dateStrs = tempStrs[0].split("-");
	 var year = parseInt(dateStrs[0], 10);
	 var month = parseInt(dateStrs[1], 10) - 1;
	 var day = parseInt(dateStrs[2], 10);
	 var timeStrs = tempStrs[1].split(":");
	 var hour = parseInt(timeStrs [0], 10);
	 var minute = parseInt(timeStrs[1], 10) - 1;
	 var second = parseInt(timeStrs[2], 10);
	 var date = new Date(year, month, day, hour, minute, second);
	 return date;
}


function visitTimeCheck(){

	var visitTimeBegin = $("input[name='visitTimeBegin']").val();
	var visitTimeEnd =   $("input[name='visitTimeEnd']").val();
	
	if(visitTimeBegin!=""&&visitTimeEnd!=""){
	
	
		var visitTimeBeginDate =strToDateNoHour(visitTimeBegin);
	
		var visitTimeEndDate =strToDateNoHour(visitTimeEnd);
		
		var dateDiff = (visitTimeEndDate.getTime()-visitTimeBeginDate.getTime())/(24*3600*1000);
		
		if(dateDiff <=90){
			
			return true;
		}
	}	
	return false;
	
}


function strToDateNoHour(str){

	 var tempStrs = str.split(" ");
	 var dateStrs = tempStrs[0].split("-");
	 var year = parseInt(dateStrs[0], 10);
	 var month = parseInt(dateStrs[1], 10) - 1;
	 var day = parseInt(dateStrs[2], 10);
	 var date = new Date(year, month, day);
	 return date;
}
</script>
