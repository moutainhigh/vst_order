var batchUpdateMessageDialog = '';

$(function(){
	try{
		 
		//查询 
		$('#search_button').unbind("click"); 
		$("#search_button").bind("click",function(){
			if($(this).attr("disabled")) {
				return;
			}
			
			//表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			
			
			var createTimeBegin=$("input[name='createTimeBegin']").val();
			if(!createTimeBegin || createTimeBegin == ""){
				$("#createTimeRequiredErrorMessage").show();
				return;
			}
			
			var createTimeEnd=$("input[name='createTimeEnd']").val();
			if(!createTimeEnd || createTimeEnd == ""){
				var myDate=(new Date()).Format("yyyy-MM-dd hh:mm:ss");
				createTimeEnd =myDate;
			}
			
			var days = GetDateDiff(createTimeBegin, createTimeEnd, "day");
			if(days<0){
				$("#createTimeErrorErrorMessage").show();
				return;
			}
			else if(days > 186){
				$("#createTime166ErrorMessage").show();
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
			
			//客户必须输入一个条件
			if(value == ""){
				$("#errorMessage").show();
				return;
			}else{
				$("#errorMessage").hide();
			}		
			
			//checkBox
			var distributorIdsStr_front = "";
			var superChannelIdsStr_front = "";
			var allSuperChannelIdsStr_front = "";
			var distributorIds = $("#distributorIds").find("input[type=checkbox]");
			$.each(distributorIds,function(){
				if(this.checked){
					distributorIdsStr_front += this.value+",";
				}
			});
			var superChannel = $("#superChannel").find("input[type=checkbox]");
			$.each(superChannel,function(){
				allSuperChannelIdsStr_front += this.value+",";
				if(this.checked){
					superChannelIdsStr_front += this.value+",";
				}
			});
			$("#allSuperChannelIdsStr").val(allSuperChannelIdsStr_front);
			$("#distributorIdsStr").val(distributorIdsStr_front);
			$("#superChannelIdsStr").val(superChannelIdsStr_front);
			//假加载效果
			$("#result").empty();
			$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
			$(this).attr("disabled", true);
			$(this).css("border","1px solid #aaa").css("background-color","#bbb").css("color", "#000").css("pointer","");
			$("#searchForm").submit();
		});
		
		//清空
		$('#clear_button').unbind("click"); 
		$("#clear_button").bind("click",function(){
			window.location.href = "/vst_order/ord/order/intoOrderQuery.do";
		});
		
		//打开批量处理预定通知页面
		$('#batch_update_message_button').unbind("click");
		$("#batch_update_message_button").bind("click",function(){
			batchUpdateMessageDialog = new xDialog("/vst_order/ord/order/queryBatchMessage.do",{},{title:"批量处理预定通知窗口",width:1000,height:500});
		});
		
		//批量处理预定通知窗口查询 
		$('#search_batch_button').unbind("click"); 
		$("#search_batch_button").bind("click",function(){
			ajaxQueryWorkBench();
		});
		
		//批量处理预定通知窗口清空
		$("#clear_batch_button").unbind("click");
		$("#clear_batch_button").bind("click",function(){
			resetQueryWorkBench();
		});
		
		//批量处理预定通知
		$('#batchUpdateMessage').unbind("click"); 
		$("#batchUpdateMessage").bind("click",function(){
			var auditTypeCount = $("input[type='radio'][name='activityName']:checked").attr("count");
			if(auditTypeCount <= 0){
				$.alert("请确认您选择的预订通知数是否大于0,如果没有预订通知将不进行进一步的处理操作!");
				return ;
			}
	        $.confirm("确认要进行批量处理预定通知吗?",function(){
	        	batchUpdateMessage();
	        });
		});
		
		//检测当前用户的状态
		//checkUserStatus();
		
		//查询后重新生成下拉框
		//createActivity();
		
		//加载当前处理人的接单状态
		if($("#workStatusSpan").length > 0){
			loadUserWorkStatus();
 		}
	}catch(e){
		alert(e.message);
	}
});

function queryByAudit(auditType,subType){

	
	if(auditType=='BOOKING_AUDIT'){
		$("#activityDetail").val(subType);
	}
	$("#activityName").val(auditType);
	queryWorkBench();
	
}
function queryWorkBench(){
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
	
	//客户必须输入一个条件
	if(value == ""){
		$("#errorMessage").show();
		return;
	}else{
		$("#errorMessage").hide();
	}		
	
	//假加载效果
	$("#result").empty();
	$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
	$("#searchForm").submit();
	
}

//检测当前登录用户的接单状态，然后页面选中
function checkUserStatus(){
	//先清掉选中状态
	$("#user_status_online").attr("checked","");
	$("#user_status_busy").attr("checked","");
	$("#user_status_offline").attr("checked","");
	
	var url = "/vst_order/ord/order/checkUserStatus.do?callback=?";
	$.getJSON(url,
			function(res){
				if(typeof(res)!=='undefined'){
					var data=res.result;
					if(data == "ONLINE"){
						$("#user_status_online").attr("checked","checked");
						$("#onlineTd").css("background-color","#4D90FE");
						$("#busyTd").css("background-color","white");
						$("#offlineTd").css("background-color","white");
					}else if(data == "BUSY"){
						$("#user_status_busy").attr("checked","checked");
						$("#onlineTd").css("background-color","white");
						$("#busyTd").css("background-color","#4D90FE");
						$("#offlineTd").css("background-color","white");
					}else{
						$("#user_status_offline").attr("checked","checked");
						$("#onlineTd").css("background-color","white");
						$("#busyTd").css("background-color","white");
						$("#offlineTd").css("background-color","#4D90FE");
					}
				}
			});
}

function loadUserWorkStatus(){
	$.ajax({
		url : "/vst_order/ord/order/queryEmployeeWorkStatus.do",
		type : "post",
		dataType : 'json',
		data : {"operatorName":$("#operatorName").val()},
		success : function(result) {
			if(!result.success){
		 		$.alert(result.message);
		 	}else{
		 		$("#workStatusSpan").html(result.attributes.workstatus);
		 	}
		}
	});
}

function updateRemindTime(auditId, remindTimeStr){
	$.ajax({
		url : "/vst_order/ord/order/updateRemindTime.do",
		type : "post",
		dataType : 'json',
		data : {"auditId":auditId, "remindTimeStr":remindTimeStr},
		success : function(result) {
			if(!result.success){
		 		$.alert(result.message);
		 	}else{
		 		$.alert(result.message);
		 		//延迟3秒刷新当前页面
		 		setInterval(function(){queryWorkBench()},3000);
		 	}
		}
	});
}

//更新用户状态
function updateUserStatus(obj){
	var userStatus = $(obj).val();
	var url = "/vst_order/ord/order/updateUserStatus.do?callback=?";
	$.getJSON(url,
			{"userStatus":userStatus},
			function(res){
				if(typeof(res)!=='undefined'){
					var data=res.result;
					if(data == "success"){
						if(userStatus=="ONLINE"){
							alert("您现在可以接单!");
							$("#onlineTd").css("background-color","#4D90FE");
							$("#busyTd").css("background-color","white");
							$("#offlineTd").css("background-color","white");
						}else if(userStatus=="BUSY"){
							alert("您现在可以接少量的单!");
							$("#onlineTd").css("background-color","white");
							$("#busyTd").css("background-color","#4D90FE");
							$("#offlineTd").css("background-color","white");
						}else{
							alert("您现在不可接单!");
							$("#onlineTd").css("background-color","white");
							$("#busyTd").css("background-color","white");
							$("#offlineTd").css("background-color","#4D90FE");
						}
					}else{
						alert("网络连接异常,请重试!");
					}
				}else{
					alert("网络连接异常,请重试!");
				}
			});
}

/**
 * 活动和活动细分联动字典
 */
function createActivity(){
	try{
		var activityName = $("#activityName").val();
		//alert(activityName);
		var activityDetail = $("#activityDetail");
		//清空所有选项
		activityDetail.empty();
		if(activityName == "SALE_AUDIT"){
			var op0 = document.createElement("option");
			op0.value = "";
			op0.innerHTML = "全部";
			activityDetail.append(op0);
			
			var op1 = document.createElement("option");
			op1.value = "EMERGENCY";
			op1.innerHTML = "紧急入园";
			activityDetail.append(op1);
			
			var op2 = document.createElement("option");
			op2.value = "REFUND";
			op2.innerHTML = "退款";
			activityDetail.append(op2);
		}else{
			var op0 = document.createElement("option");
			op0.value = "";
			op0.innerHTML = "全部";
			activityDetail.append(op0);
		}
		
		//alert(currentActivityDetail);
		
		//选中当前的查询条件
		activityDetail.find("option[value="+currentActivityDetail+"]").attr("selected",true);
	}catch(e){
		alert(e.message);
	}
	
	
}
/**
 * ajax查询统计预订通知数量
 */
function ajaxQueryWorkBench(){
	
	var auditType = $("input[type='radio'][name='activityName']:checked").val();
	$("#activityName").val("BOOKING_AUDIT");
	if("BOOKING_AUDIT" != auditType){
		$("#activityDetail").val(auditType);
	}
	
	//表单验证
	if(!$("#searchForm").validate().form()){
		return;
	}
	//遍历所有查询条件的值
	var value = "";
	var textFields = $("input[type=text]");
	$.each(textFields,function(){
	 	value += this.value;
	});
	//客户必须输入一个条件
	if(value == ""){
		$("#errorMessage").show();
		return;
	}else{
		$("#errorMessage").hide();
	}		
	//假加载效果
	$("#result").empty();
	var activityNameRadios = $("input[type='radio'][name='activityName']");
	if(activityNameRadios.size() > 0){
		activityNameRadios.each(function(i, element) {
			$(element).attr("disabled",true);
		});
	}
	$("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
	$.ajax({
		url : "/vst_order/ord/order/queryResultMessage.do",
		type : "post",
		data : $("#searchForm").serialize() ,
		success : function(result) {
			$("#result").empty();
			$("#resultMessageDiv").html(result);
			$("#activityDetail").val("");
		},
		error : function(result) {
			$("#result").empty();
			$("#result").append("<div class='loading mt20'>无法连接到服务器,请稍后重试......</div>");
			$("#activityDetail").val("");
		}
	});
}

/**
 * 批量处理预定通知
 */
function batchUpdateMessage(){
	
	var auditType = $("input[type='radio'][name='activityName']:checked").val();
	
	$("#activityName").val("BOOKING_AUDIT");
	if("BOOKING_AUDIT" != auditType){
		$("#activityDetail").val(auditType);
	}
	
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
	
	//客户必须输入一个条件
	if(value == ""){
		$("#errorMessage").show();
		return;
	}else{
		$("#errorMessage").hide();
	}		
	
	//假加载效果
	$("#result").empty();
	var activityNameRadios = $("input[type='radio'][name='activityName']");
	if(activityNameRadios.size() > 0){
		activityNameRadios.each(function(i, element) {
			$(element).attr("disabled",true);
		});
	}
	$("#result").append("<div class='loading mt20'>正在努力批量执行预订通知处理中......</div>");
	$.ajax({
		url : "/vst_order/ord/order/batchUpdateMessage.do",
		type : "post",
		data : $("#searchForm").serialize() ,
		success : function(result) {
			$("#result").empty();
			$("#resultMessageDiv").html(result);
			$("#activityDetail").val("");
		},
		error : function(result) {
			$("#result").empty();
			$("#result").append("<div class='loading mt20'>无法连接到服务器,请稍后重试......</div>");
			$("#activityDetail").val("");
		}
	});
}

/**
 *重置批量处理预订通知窗口
 */
function resetQueryWorkBench(){
	$("#searchForm")[0].reset();
	$.ajax({
		url : "/vst_order/ord/order/queryResultMessage.do",
		type : "post",
		data : {"resetFlag":true},
		success : function(result) {
			$("#result").empty();
			$("#resultMessageDiv").html(result);
			$("#activityDetail").val("");
		},
		error : function(result) {
			$("#result").empty();
			$("#result").append("<div class='loading mt20'>无法连接到服务器,请稍后重试......</div>");
			$("#activityDetail").val("");
		}
	});
}

// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s) 可以用 1-2 个占位符， 
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
// 例子： 
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04
Date.prototype.Format = function (fmt) { //author: meizz 
    var o = {
        "M+": this.getMonth() + 1, //月份 
        "d+": this.getDate(), //日 
        "h+": this.getHours(), //小时 
        "m+": this.getMinutes(), //分 
        "s+": this.getSeconds()//秒 
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

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
