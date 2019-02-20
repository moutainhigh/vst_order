/**
 * 后台下单的产品、商品搜索重构js
 * 
 * @author wenzhengtao
 * @date 2013-12-30
 * 
 */

/***************判断两个日期大小***************/ 
function loadJtip(){
	$('.J_tip').lvtip({
        templete: 3,
        place: 'bottom-left',
        offsetX: 0,
        events: "live" 
    });
	
	//展开收起房间数据,默认显示3间，最多显示20间
    $(".J_arrow").click(function () {
        var $table = $(this).parents("table.co_table");
        if ($(this).hasClass("active")) {
            if ($table.find("tr").length > 5) {
                $(this).removeClass("active").html('收起房型<i class="ui-arrow-top blue-ui-arrow-top mt8"></i>');
                $table.find("tr").show();
            }
        } else {
            $table.find("tr").hide();
            $table.find("tr").eq(0).show();
            $table.find("tr").eq(1).show();
            $table.find("tr").eq(2).show();
            $table.find("tr").eq(3).show();
            $table.find("tr").last().show();
            $(this).addClass("active").html('其他房型<i class="ui-arrow-top blue-ui-arrow-top mt8"></i>');
        }
    });
}

//日期比较函数
function compareDay(a,b){  
	var a1 = a.split("-");
	var b1 = b.split("-");  
    var d1 = new Date(a1[0],a1[1],a1[2]);       
	var d2 = new Date(b1[0],b1[1],b1[2]);    
    if (Date.parse(d1) - Date.parse(d2) > 0) {
		return false;
	}else{
		return true;
	}
}

/***************判断日期是否为标准格式yyyy-MM-dd***************/ 
function isDateString(sDate){      
	var mp=/\d{4}-\d{2}-\d{2}/;   
    var matchArray = sDate.match(mp);     
    if (matchArray==null){
    	return false;  
    }
    var iaMonthDays = [31,28,31,30,31,30,31,31,30,31,30,31];     
    var iaDate = new Array(3);     
    var year, month, day;        
    iaDate = sDate.split("-");          
    year = parseFloat(iaDate[0]);     
    month = parseFloat(iaDate[1]);      
    day=parseFloat(iaDate[2]);  
    if (year < 1900 || year > 2100){
    	return false;
    }
    if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)){ 
    	iaMonthDays[1]=29;
    }
    if (month < 1 || month > 12){
    	return false;  
    }
    if (day < 1 || day > iaMonthDays[month - 1]){
    	return false;      
    }
    return true;
} 

/*****将日期对象转化为字符串********/
function convertDateToString(dateIn){
	var year=0;
    var month=0;
    var day=0;
    var dateStr="";
    
    year = dateIn.getFullYear();//获得完整年份
    month = dateIn.getMonth()+1;//获得显示的月份
    day = dateIn.getDate();//获得天
    
    dateStr = year + "-";
    if (month >= 10 ){
    	dateStr = dateStr + month + "-";
    }else{
    	dateStr = dateStr + "0" + month + "-";
    }
    
    if (day >= 10 ){
    	dateStr = dateStr + day ;
    }else{
    	dateStr = dateStr + "0" + day ;
    }
    
    return dateStr;
}

/******判断是否是正整数***********/
function isPositiveNumber(input){  
     var re = /^[1-9]+[0-9]*]*$/;  
     if (!re.test(input)){  
        alert("请输入正整数");
        return false;
     }else{
    	 return true;
     }
}  

/*******日期增加相应的天数后返回一个新的日期**********/
function dateAdd(dateObj,days){  
    var tempDate = dateObj.valueOf();  
    tempDate = tempDate + days * 24 * 60 * 60 * 1000;  
    tempDate = new Date(tempDate);  
    return tempDate;  
}  

/*****根据入住时间和离店时间计算入住天数 或者 根据入住时间和入住天数计算离店日期*******/
function checkDay(){
	try{
	   var strDateStart = $.trim($("#startDate").val());
	   var strDateEnd = $.trim($("#endDate").val());
	   var days = $.trim($("#days").val());
	
	   if(strDateStart == ""){
		   //alert("入住日期为必填项!");
		   $("#startDate").addClass("border_red");
		   return;
	   }
	   
	   if(strDateStart != ""){
		   if(!isDateString(strDateStart)){
			   $("#startDate").addClass("border_red");
			   return;
		   }
	   }
	   
	   if(strDateEnd != ""){
		   if(!isDateString(strDateEnd)){
			   $("#endDate").addClass("border_red");
			   return;
		   }
	   }
	   
	   if(days != "" && days != 0){
		   if(!isPositiveNumber(days)){
			   alert("入住天数必须是正整数!");
			   $("#days").addClass("border_red");
			   return;
		   }
	   }
	   
	   if(strDateEnd != ""){
		   var oDate1 = strDateStart.split("-");
		   var oDate2 = strDateEnd.split("-");
		   var strDateS = new Date(oDate1[0], oDate1[1]-1, oDate1[2]);
		   var strDateE = new Date(oDate2[0], oDate2[1]-1, oDate2[2]);
		   var iDays = parseInt(Math.abs(strDateS - strDateE ) / 1000 / 60 / 60 /24);// 把相差的毫秒数转换为天数
		   $("#days").val(iDays);
		   //重新取值
		   days = $("#days").val();
	   }
	   
	   if(days != ""){
		   var oDate1 = strDateStart.split("-");
		   var strDateS = new Date(oDate1[0],oDate1[1]-1, oDate1[2]);
		   //计算离店日期
		   var strDateE = dateAdd(strDateS,days);
		   var oDate2 = convertDateToString(strDateE);
		   $("#endDate").val(oDate2);
		   endDate = $("#endDate").val();
	   }
	   
	   $("#startDate").removeClass("border_red");
	   $("#endDate").removeClass("border_red");
	   $("#days").removeClass("border_red");
	}catch(e){
		alert(e.message);
	}
}


$(function(){
	//日历初始化
    pandora.calendar.init({ area: true, input: '.iflt_date',inputClass: 'iflt_date',showWeek:true });
    
    /**
	 * 绑定入住城市ajax事件
	 */
	$("#districtName").jsonSuggest({
		url:basePath+"/ord/order/queryDistrictList.do",
		maxResults: 20,
		minCharacters:1,
		onSelect:function(item){
			$("#districtId").val(item.id);
			//alert($("#districtId").val());
		}
	});
	$("#destDistrictName").jsonSuggest({
		url:basePath+"/ord/order/queryDistrictList.do",
		maxResults: 20,
		minCharacters:1,
		onSelect:function(item){
			$("#destDistrictId").val(item.id);
			//alert($("#districtId").val());
		}
	});	
	/**
	 * 绑定酒店名称ajax事件
	 * 如果入住城市有值，则根据城市过滤
	 */
	$("#productName").jsonSuggest({
		url:basePath+"/ord/order/queryProductList.do",
		maxResults: 20,
		minCharacters:1,
		param:["#districtId"],//扩展参数
		onSelect:function(item){
			$("#productIdBak").val(item.id);
			$("#productId").val(item.id);
		}
	});
	
	/**
	 * 绑定查询按钮事件
	 */
	$("#search_button").bind("click",function(){
		
		$("#packagesTypes").val("");
		$("#traffic").val("");
		
		//如果酒店ID不为空，则城市非必填
		if($.trim($("#productId").val())!=""){
			$("#districtName").nextAll(".e_error").remove();
			$("#districtName").removeAttr("required");
		}else{
			$("#districtName").nextAll(".e_error").remove();
		}
		
		if(!$("#searchForm").validate().form()){
				return false;
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
			return false;
		}else{
			$("#errorMessage").hide();
		}
			
		//加载效果
		$("#result").empty();
		if($("#detailResult")){
			$("#detailResult").empty();
		}		
		$("#result").empty();
		$("#result").append("<div class='loading mt20'><img src='../../img/loading.gif' width='32' height='32' alt='加载中'>正在努力的加载数据中......</div>");

		//给productId强制赋值
		var productId = $("#productIdBak").val();
		if(productId!=""){
			$("#productId").val(productId);
		}
		//显示推荐级别区域
		$("#recommendDiv").show();
		//ajax加载查询结果
		var $form=$(this).parents("form");
		innerHtml("",$('.js_procla'));
		$.ajax({
			type:"post",
			url:$form.attr("action"),
			data:$form.serialize(),
			success:function(result){				
				$("#result").html(result);
				var filterData=$("#filter").html();
				innerHtml(jQuery.parseJSON(filterData),$('.js_procla'));
			}
		});
		
	});
	
	/**
	 * 绑定清空按钮事件
	 */
	$("#clear_button").bind("click",function(){
		$("#searchForm")[0].reset();		
		//window.location.href = basePath+"/ord/productQuery/showOrderProductQueryList.do";
	});
	
});

/**
 * 根据推荐级别查询
 * @param level
 * @param obj
 */
function searchByRecommendLevel(level,id){
	try{
		//设置隐藏域的推荐级别值
		if(level=="all"){
			$("#recommendLevel").val("");
		}else{
			$("#recommendLevel").val(level);
		}
		
		//拿到tab父节点
		$("#"+id).parent().parent(".ui_tab_1").children().removeClass();
		//设置当前选项卡的选中样式
		$("#"+id).parent().addClass("active");
		
		$("#result").empty();
		$("#result").append("<div class='loading mt20'><img src='../../img/loading.gif' width='32' height='32' alt='加载中'>正在努力的加载数据中......</div>");
		
		//ajax加载结果
		var $form=$("#searchForm");
		//alert($form.serialize());
		$("#result").load($form.attr("action"),$form.serialize(),function(){
			//loadJtip();
		});
	}catch(e){
		alert(e.message);
	}
}

//显示自定义价格区间
function showCustomPriceRange(obj){
	var priceRange = $(obj).val();
	//alert(priceRange);
	var customPriceRange = $("#customPriceRange");
	if(priceRange=="7"){
		customPriceRange.show();
	}else{
		customPriceRange.hide();
	}
	$("#priceBegin").val("");
	$("#priceEnd").val("");
}
