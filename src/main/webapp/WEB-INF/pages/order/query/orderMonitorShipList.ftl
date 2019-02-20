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
	<input type="hidden" value="${monitorCnd.subCategoryId}" id='subCategoryIdHide' />
	<select id='sel_children' name ="subCategoryId" style="display: none;" id="childrenIds">
		<option value="">请选${categoryIds }择</option>
	 	<#if bizFreedomList?? >
	   		<#list bizFreedomList as bizCategory>
	   	 		<option <#if bizCategory.categoryId == monitorCnd.subCategoryId>selected ='selected'</#if> value=${bizCategory.categoryId}>${bizCategory.categoryName!''}</option>
	   		</#list>
	 	 </#if>
	</select>
	<form id="searchForm" action="/vst_order/ord/order/orderMonitorShipList.do" method="post">
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                	<td class="s_table2_t" width="14" rowspan="3">订单信息</td>
                    <td width="" colspan="2">
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
	                       <!--
	                        <label>资源审核：<@s.formSingleSelect "monitorCnd.resourceStatus" resourceStatusMap 'class="w10"'/></label>
	                        <label>支付状态：<@s.formSingleSelect "monitorCnd.paymentStatus" paymentStatusMap 'class="w10"'/></label>
	                        -->
	                        
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
                     </td>
                 </tr>
                 <tr>
                    　<td colspan="2">
                    	<label class='category_raido'>产品类型：<@s.formSingleSelect "monitorCnd.categoryIds" productCategoryMap 'style="width:115px;" '/></label>
                        <label>供应商名称：
                        	<@s.formHiddenInput "monitorCnd.supplierId"/>
                        	<@s.formInput "monitorCnd.supplierName" 'class="search"'/>
                        </label>
                        
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
                        <label>出游时间：
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
		<div id="createTimeErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>当输入产品名称或者商品名称时，必须输入下单时间！</div>
		<div id="timeConstrictionErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>未输入订单号、子订单号时，只能查询三个月之间的数据！</div>
		<div id="requiredCreateTimeErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>未输入订单号、子订单号时，下单开始时间必填！</div>
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
						<div class="operate mt20" >
						<a class="btn btn_cc1" id="checkAll">全选</a>
							<a class="btn btn_cc1" id="uploadNotice">批量上传出团通知书</a>
							<a class="btn btn_cc1" id="sendNotice">批量发送出团通知书</a>
						</tr>
						 </div>
				</thead>
					<thead>
						<tr>
							<th nowrap="nowrap" style="width:5%;"></th>
							<th nowrap="nowrap" style="width:7%;">下单渠道</th>
							<th nowrap="nowrap" style="width:5%;">订单号</th>
							<th nowrap="nowrap" style="width:15%;">产品名称</th>
							<th nowrap="nowrap" style="width:5%;">出游人数</th>
							<th nowrap="nowrap" style="width:10%;">下单时间</th>
							<th nowrap="nowrap" style="width:10%;">出游时间</th>
							<th nowrap="nowrap" style="width:5%;">联系人</th>
							<th nowrap="nowrap" style="width:10%;">当前状态</th>
							<th nowrap="nowrap" style="width:10%;">出团通知书状态</th>
							<th nowrap="nowrap" style="width:10%;">操作</th>
						</tr>
					</thead>
					<tbody>
						<#list resultPage.items as result>
							<tr>
								<td>
								<#if result.orderStatus == 'NORMAL'>
									<input type="checkbox" name="orderIds" value="${result.orderId}_${result.contactNameEmail!''}">
									
								<#else> 
									<#--
									<input type="checkbox" name="orderIds" disabled ="disabled" value="${result.orderId}_${result.contactNameEmail!''}">
									-->
								</#if>
								</td>
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
								<td> ${result.tourisCount} 人</td>
								<td>${result.createTime}</td>
								<td>${result.visitTime}</td>
								<td>${result.contactName}</td>
								<td>${result.currentStatus!'无'}</td>
								<td>${result.noticeRegimentStatusName!'无'}</td>
								
								
								<td class="oper">
								
								<#if result.orderStatus == 'NORMAL'>
									<a href="javascript:void(0);" class="showUploadNotice" data="${result.orderId}_${result.contactNameEmail!''}">上传出团通知书</a>
									  </br>
									 
									 <#if result.noticeRegimentStatus!= "NO_UPLOAD"> 
										  
										  <a href="javascript:void(0);" class="showSendNotice" data="${result.orderId}_${result.contactNameEmail!''}">发送出团通知书</a>
										  </br>
									  </#if>	  
								</#if>
							  	<#if result.noticeRegimentStatus!= "NO_UPLOAD"> 
							  		<!--
							  		<a href="/vst_order/order/orderShipManage/fileDownLoad.do?orderId=${result.orderId}" data="${result.orderId}">查看</a>
							  		-->
							  		 <a href="javascript:void(0);" class="viewSendNoticeList" data="${result.orderId}" contactEmail="${result.contactNameEmail!''}">查看</a>
										 
								</#if>	  
		                        </td>
		                        
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
			
			//表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			
			//遍历所有查询条件的值
			var value = "";
			
			//input
			var textFields = $("input[type=text]");
			$.each(textFields,function(){
			 	value += this.value;
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
			if(orderId == "" && orderItemId == ""){
				if(createTimeBegin == ""){
					$("#timeConstrictionErrorMessage").hide();
					$("#requiredCreateTimeErrorMessage").show();
						return;
				}else{
					$("#requiredCreateTimeErrorMessage").hide();
				}
			    var createTimeBeginDate = strToDate(createTimeBegin);
			    var createTimeEndDate = null;
			    var terminalDate = strToDate(previousNow);
				if(createTimeEnd != ""){
					createTimeEndDate =strToDate(createTimeEnd);
				}
				if(createTimeEndDate == null){
					if(createTimeBeginDate < terminalDate){
						$("#timeConstrictionErrorMessage").show();
						return;
					}else{
						$("#timeConstrictionErrorMessage").hide();
					}
				}else{
					var dateDiff = (createTimeEndDate.getTime()-createTimeBeginDate.getTime())/(24*3600*1000);
					if(dateDiff > 90){
						$("#timeConstrictionErrorMessage").show();
						return;
					}else{
						$("#timeConstrictionErrorMessage").hide();
					}
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
			window.location.href = "/vst_order/ord/order/intoOrderMonitorShip.do";
		});
		
		$("#supplierName").jsonSuggest({
			url:"${rc.contextPath}/ord/order/querySupplierList.do",
			maxResults: 20,
			minCharacters:1,
			onSelect:function(item){
				$("#supplierId").val(item.id);
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
		
		showChildrenCategory();
	});
	
	var monitorShipListDialog =this.window;
	var uploadNoticeDialog ;
	$("#uploadNotice").bind("click",function(){
			  var chk_value =[];    
		      $('input[name="orderIds"]:checked').each(function(){    
		       chk_value.push($(this).val());    
		      });    
		      if(chk_value.length==0)
		      {
		      	$.alert('尚未选中任何订单'); 
		      	return;
		      }
		      var orderIds=chk_value+"";
			  uploadNoticeDialog = new xDialog("/vst_order/order/orderShipManage/showUploadNoticeRegiment.do",{"orderIds":orderIds,"oneData":"false"},{title:"上传出团通知书",width:600});
		});
	
	$("a.showUploadNotice").bind("click",function(){
		
		
		var orderIds=$(this).attr("data");
		uploadNoticeDialog = new xDialog("/vst_order/order/orderShipManage/showUploadNoticeRegiment.do",{"orderIds":orderIds,"oneData":"true"},{title:"上传出团通知书",width:600});
		
	});
	
		
	$("#sendNotice").bind("click",function(){
		 var chk_value =[];    
	      $('input[name="orderIds"]:checked').each(function(){    
	       chk_value.push($(this).val());    
	      });    
	      if(chk_value.length==0)
	      {
	      	$.alert('尚未选中任何订单'); 
	      	return;
	      }
	      var orderIds=chk_value+"";
		//遮罩层
    	var loading = pandora.loading("正在努力发送中...");		
		  $.ajax({
				url : "/vst_order/order/orderShipManage/sendNoticeRegiment.do",
				type : "post",
				dataType:"JSON",
				data : {"orderIds":orderIds,"oneData":"false"},
				success : function(result) {
					if (result.code == "success") {
						loading.close();
						alert(result.message);
						$("#searchForm").submit();
					}else {
						loading.close();
						$.alert(result.message);
					}
				}
			});
			
			
	});	
	
	$("a.showSendNotice").bind("click",function(){
		
		var orderIds=$(this).attr("data");
		
		//遮罩层
    	var loading = pandora.loading("正在努力发送中...");		
		$.ajax({
				url : "/vst_order/order/orderShipManage/sendNoticeRegiment.do",
				type : "post",
				dataType:"JSON",
				data : {"orderIds":orderIds,"oneData":"true"},
				success : function(result) {
					if (result.code == "success") {
						loading.close();
						alert(result.message);
						$("#searchForm").submit();
					}else {
						loading.close();
						$.alert(result.message);
					}
				}
			});
		
	});
	
	
	
	var viewSendNoticeListDialog;

	$("a.viewSendNoticeList").bind("click",function(){
		
		var orderId=$(this).attr("data");
		var contactEmail = $(this).attr("contactEmail");
		viewSendNoticeListDialog = new xDialog("/vst_order/order/orderShipManage/viewSendNoticeList.do",{"orderId":orderId,"sourceType":"notice","contactEmail":contactEmail},{title:"查看出团通知",width:900});
		
	});
	
	
		$("#checkAll").bind("click",function(){
			  var ischeckAll=$("input[name='orderIds']").attr("checked"); 
			  if(!ischeckAll)
			  {
			  	$("input[name='orderIds']").attr("checked",true)
			  }else{
			  	$("input[name='orderIds']").attr("checked",false)
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

	$("#categoryIds").bind("change",function(){
		var radio = $(this);
		//console.info(radio);
		if (radio[0].value=='18') {
			$(".category_raido").append($("#sel_children").clone().attr("id", "sel_children_clone"));
			$("#sel_children_clone").show();
		} else {
			$("#sel_children_clone").remove();
		}
	});
	function showChildrenCategory() {
		var selCategoryId = $("#categoryIds").val();
		if (selCategoryId == '18') {
			$(".category_raido").append($("#sel_children").clone().attr("id", "sel_children_clone"));
			$("#sel_children_clone").show();
			$("#sel_children_clone").val($("#subCategoryIdHide").val());
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
</script>
