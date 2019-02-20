var updateDistOrderDialog;
var confirmOrderDialog;
var orderInfoDialog;

//附加信息份数变化事件
$(".numText").bind("change",function(){
	countTotalPrice();
});


function selectedUpdatePro(selectedObj){
	var childPrice=selectedObj.parent().parent().find("td").eq(2).children(".childNumText").attr("childPrice");
	var auditPrice=selectedObj.parent().parent().find("td").eq(2).children(".adultNumText").attr("auditPrice");
	selectedObj.parent().parent().find("td").eq(2).children(".childNumText").val(getChildNum());
	selectedObj.parent().parent().find("td").eq(2).children(".adultNumText").val(getAdultNum());
	//设置商品总数
	var goodsId=selectedObj.parent().parent().find("td").eq(2).children(".adultNumText").attr("goodsId");
	var totalNum=parseInt(getChildNum())+parseInt(getAdultNum());
	$("#"+goodsId+"TotalNum").val(totalNum);
	//更新价格
	var totalPrice=parseFloat(childPrice)*parseInt(getChildNum())+parseFloat(auditPrice)*parseInt(getAdultNum());
	selectedObj.parent().parent().find("td").eq(3).html("总价:￥"+totalPrice.toFixed(2)+"元");
	countTotalPrice();	
}
function unSelectedUpdatePro(selectedObj){
	var childPrice=selectedObj.parent().parent().find("td").eq(2).children(".childNumText").attr("childPrice");
	var auditPrice=selectedObj.parent().parent().find("td").eq(2).children(".adultNumText").attr("auditPrice");
	selectedObj.parent().parent().find("td").eq(2).children(".childNumText").val(0);
	selectedObj.parent().parent().find("td").eq(2).children(".adultNumText").val(0);
	//设置商品总数
	var goodsId=selectedObj.parent().parent().find("td").eq(2).children(".adultNumText").attr("goodsId");
	var totalNum=0;
	$("#"+goodsId+"TotalNum").val(totalNum);
	//更新价格
	var totalPrice=parseFloat(childPrice)*parseInt(0)+parseFloat(auditPrice)*parseInt(0);
	selectedObj.parent().parent().find("td").eq(3).html("总价:￥"+totalPrice.toFixed(2)+"元");
	countTotalPrice();	
}
	$("#hotelSelect").live("change",function(){
		var hotelSubCategoryId = $("#hotelSubCategoryId").val();
  		var suppGoodsId="#"+$(this).attr("goodsId");
  		var goodsPrice=$(this).attr("goodsPrice");
  		var totalPrice=parseFloat(goodsPrice)*parseInt($(this).val())/100;
  		$(this).find("option[value="+$(this).val()+"]").attr("selected",true);
  		$(this).parent().parent().find(".hoteltotalPrice").attr("totalAmountYuan",totalPrice.toFixed(2));
		$(this).parent().parent().find(".hoteltotalPrice").html("总价:￥"+totalPrice.toFixed(2)+"元");
		
		//182只针对 自由行 机酒 国内
		if (hotelSubCategoryId != undefined && hotelSubCategoryId == "182" && $("input[name='productType']").val() == "INNERLINE") {
			$(this).closest("table").find(".hotelBtnOk").attr("style", "background:#4D90FE;border1px solid #3F87FE;color:#fff;");
			$(this).closest("table").find(".hotelBtnOk").html("选择");
			$(this).closest("table").find(".hotelBtnOk").removeAttr("id");
			$(this).closest("table").find(".hotelBtnOk").closest("tr").find("td select").removeClass("lvmama-price-flag");
			$(this).parent().parent().find(".hotelBtnOk").html("已选");
			$(this).parent().parent().find(".hotelBtnOk").attr("style", "");
			$(this).parent().parent().find(".hotelBtnOk").attr("id", "selectedHotel");
			$(this).closest("tr").find("td select").addClass("lvmama-price-flag");
		}
		if($(this).siblings(suppGoodsId+"adultQuantity")){
			var nameOfTotalNum = $(this).siblings(suppGoodsId+"adultQuantity").attr('name');
			if(nameOfTotalNum){
				nameOfTotalNum = nameOfTotalNum.replace('adultQuantity', 'quantity');
				$("[name='" + nameOfTotalNum + "']").val($(this).val());
				$(this).siblings(suppGoodsId+"adultQuantity").val($(this).val());
			}
		}
		
  		try{
  			setTimeout(changeNextSelect(null,$(this)),500);
		}catch(err){}
  		countTotalPrice();
  		//182只针对 自由行 机酒 国内
  		if (hotelSubCategoryId != undefined && hotelSubCategoryId == "182" && $("input[name='productType']").val() == "INNERLINE") {
  			calHotelCombPriceDiff(); //差价
  		}
  	});
	
	//后台线路下单 自由行机酒 酒店选择 只针对 自由行 机酒 
	$(".hotelBtnOk").live("click",function(){
		var table = $(this).closest("table");
		var selected = $(this).closest("tr").find("select");
		var suppGoodsId="#"+$(selected).attr("goodsId");
  		var goodsPrice=$(selected).attr("goodsPrice");
  		var totalPrice=parseFloat(goodsPrice)*parseInt($(selected).val())/100;
  		$(selected).find("option[value="+$(selected).val()+"]").attr("selected",true);
  		$(selected).parent().parent().find(".hoteltotalPrice").attr("totalAmountYuan",totalPrice.toFixed(2));
		$(selected).parent().parent().find(".hoteltotalPrice").html("总价:￥"+totalPrice.toFixed(2)+"元");
		$(table).find(".hotelBtnOk").attr("style", "background:#4D90FE;border1px solid #3F87FE;color:#fff;");
		$(table).find(".hotelBtnOk").html("选择");
		$(table).find(".hotelBtnOk").removeAttr("id");
		$(table).find(".hotelBtnOk").closest("tr").find("td select").removeClass("lvmama-price-flag");
		$(this).html("已选");
		$(this).attr("style", "");
		$(this).attr("id", "selectedHotel");
		$(this).closest("tr").find("td select").addClass("lvmama-price-flag");
		if($(selected).siblings(suppGoodsId+"adultQuantity")){
			var nameOfTotalNum = $(selected).siblings(suppGoodsId+"adultQuantity").attr('name');
			if(nameOfTotalNum){
				nameOfTotalNum = nameOfTotalNum.replace('adultQuantity', 'quantity');
				$(table).find("[name='" + nameOfTotalNum + "']").val($(selected).val());
				$(selected).siblings(suppGoodsId+"adultQuantity").val($(selected).val());
			}
		}
		
  		try{
  			setTimeout(changeNextSelect(null,$(selected)),500);
		}catch(err){}
  		countTotalPrice(); //总价
  		calHotelCombPriceDiff(); //差价
	});
//成人儿童份数变化事件
$(".childNumText").bind("change",function(){
	var childPrice=$(this).attr("childPrice");
	var auditPrice=$(this).attr("auditPrice");
	$(this).val(getChildNum());
	//设置商品总数
	var goodsId=$(this).attr("goodsId");
	var totalNum=parseInt(getChildNum())+parseInt(getAdultNum());
	$("#"+goodsId+"TotalNum").val(totalNum);
	
	//更新价格
	var totalPrice=parseFloat(childPrice)*parseInt(getChildNum())+parseFloat(auditPrice)*parseInt(getAdultNum());
	$(this).parent().parent().find("td").eq(3).html("总价:￥"+totalPrice+"元");
	countTotalPrice();	
});
//成人份数变化事件
$(".adultNumText").bind("change",function(){
	var childPrice=$(this).attr("childPrice");
	var auditPrice=$(this).attr("auditPrice");
	$(this).val(getAdultNum());
	//设置商品总数
	var goodsId=$(this).attr("goodsId");
	var totalNum=parseInt(getAdultNum())+parseInt(getChildNum());
	$("#"+goodsId+"TotalNum").val(totalNum);
	
	//更新价格
	var totalPrice=parseFloat(auditPrice)*parseInt(getAdultNum())+parseFloat(childPrice)*parseInt(getChildNum());
	$(this).parent().parent().find("td").eq(3).html("总价:￥"+totalPrice+"元");
	countTotalPrice();	
});
//酒店套餐份数变化事件
$(".hotelAdultNumText").bind("change",function(){
	var auditPrice=$(this).attr("auditPrice");
	if($(this).val().trim()==""||isNaN($(this).val())){
		$(this).val("0");
	}
	if($(this).attr("isHotel") != "true"){
		if(parseInt($(this).val())>(getAdultNum())){
			$(this).val(getAdultNum());
		}
	}
	//设置商品总数
	var goodsId=$(this).attr("goodsId");
	var totalNum=parseInt($(this).val());
	$("#"+goodsId+"TotalNum").val(totalNum);
	
	//更新价格
	var totalPrice=parseFloat(auditPrice)*parseInt($(this).val());
	$(this).parent().parent().find("td").eq(2).html("总价:￥"+changeTwoDecimal_f(totalPrice)+"元");
	try{
		setTimeout(changeNextSelect(null,null),500);
	}catch(err){}
	countTotalPrice();	
});
//***************************************************loading
var isFirstClick = true;
//查询商品详情绑定事件
$(".btn_ccproductdetail").bind("click",function(){
	if(isFirstClick){
		$(this).attr("disabled","disabled");
		$(this).removeAttr('href');//去掉a标签中的href属性
		$(this).removeAttr('onclick');//去掉a标签中的onclick事件
		requestProduct();
		isFirstClick = false;
	}
});
var isFirstClick_ = true;
//查询酒店套餐商品详情绑定事件
$(".btn_ccproductHoteldetail").bind("click",function(){
	if(isFirstClick_){
		$(this).attr("disabled","disabled");
		$(this).removeAttr('href');//去掉a标签中的href属性
		$(this).removeAttr('onclick');//去掉a标签中的onclick事件
		requestProduct();
		isFirstClick_ = false;
	}
});

//成人数变化事件	
	$("#adultNum").bind("change",function(){
		//requestProduct();
		resetChildMaxQuantity()
  	});  	
//儿童数变化事件	
  	$("#childNum").bind("change",function(){
  		
  		//requestProduct();
  	});
//份数变化事件	
  	$("#hotelNum").bind("change",function(){
		//requestProduct();
  	});
 


  //请求产品信息
  	function requestProduct(){
  		var isAdult=true;
  		var distributionId = 2;
  		var url;  		
  		var adultNum = $("#adultNum").find("option:selected").text();  
  		if(isNaN(adultNum)||adultNum==""){ 
  			isAdult=false;
  		}
  	
  		if(isAdult){
  			//成人儿童
  			var specDate=$("#visitTime").val();
  	  		var adult=getAdultNum();
	  	  	var child=getChildNum();
	  		var productId=$("#productId").val();
	  		//ajax请求成人儿童产品对象	
	  		$("#bookForm input[name=specDate]").val(specDate);
			$("#bookForm input[name=productId]").val(productId);
			$("#bookForm input[name=distributionId]").val(distributionId);
			$("#bookForm input[name=userId]").val(book_user_id);
			$("#bookForm input[name=adultNum]").val(adult);
			$("#bookForm input[name=childNum]").val(child);
	  		
	  	}else{
  			//份数
  			var specDate=$("#visitTime").val();
  	  		var copies=getCopies();
	  		var productId=$("#productId").val();
	  		//ajax请求酒店套餐产品对象			
	  		$("#bookForm input[name=specDate]").val(specDate);
			$("#bookForm input[name=productId]").val(productId);
			$("#bookForm input[name=distributionId]").val(distributionId);
			$("#bookForm input[name=userId]").val(book_user_id);
			$("#bookForm input[name=copies]").val(copies);
	  	}
		var startDistrict = $("#queryStartDistrictId");
		if(startDistrict && startDistrict.length>0){
			$("#bookForm input[name=startDistrictId]").val(startDistrict.attr("data-cityid"));
		}
		//设置选中效果
  		$("#bookForm").attr("action","/vst_order/ord/order/queryLineDetailList.do");  		
		$("#bookForm").submit();
  	}
  //初始化查询条件信息
  	function initPage(){  		
  		//日历初始化
  	   /* pandora.calendar.init({ area: true, input: '.iflt_date',inputClass: 'iflt_date',showWeek:true });*/
  		//设置成人数、儿童数、酒店套餐份数
  		var adultNum=$("#adultNumValue").val();
  		var childNum=$("#childNumValue").val();
  		var hotelNum=$("#hotelNumValue").val();
  		if(isNaN(hotelNum)){
  			$("#adultNum option[value=" + parseInt(adultNum) + "]").attr('selected', 'true');
  	  		$("#childNum option[value=" + parseInt(childNum) + "]").attr('selected', 'true');
  		}else{
  			$("#hotelNum option[value=" + parseInt(hotelNum) + "]").attr('selected', 'true');
  		}
  		insuranceMaxNum();
  	  	//初始化商品份数
  	 	countTotalPrice();
  	  	
  	};
  //***************************************************loading
  //得到成人数量
  	function getAdultNum(){
  		var adult = 0;
  		var saleCopiesFlag=$("#saleCopiesFlag").val();
   		if(saleCopiesFlag=="true"){
   			adult=$("#adultNumValue").val();
   		}else{
   			adult = $("#adultNum").find("option:selected").text();
   		}
  		if(adult&&!isNaN(adult)){  			
  			adult = parseInt(adult);
  		}else{
  			adult=$("#hotelNum").find("option:selected").text();
  			adult = parseInt(adult);
  		}
  		return adult;
  	};
  	//得到份数
  	function getCopies(){
  	
  		
  		var copies=$("#hotelNum").find("option:selected").text();
  		
  		return copies;
  	};
  	//得到儿童数量
  	function getChildNum(){  	
   		var child = 0;
   		var saleCopiesFlag=$("#saleCopiesFlag").val();
   		if(saleCopiesFlag=="true"){
   			child=$("#childNumValue").val()
   		}else{
   			child = $("#childNum").find("option:selected").text();
   		}
   		
  		if(child&&!isNaN(child)){  			
  			child = parseInt(child);
  		}else{
  			child=0;
  		}  		
  		return child;
  	};
/********************************** start供应商打包产品的更多选择 **********************************/
//更多酒店套餐
  	$('.moreCategoryHotelCom').click(function(){
		var dialog=$.dialog({
			title : '商品列表',
			content : $('#dialogShow').html(),
			width: 950,
			mask : true
		});
		$("a[name=xztcBtn]").bind("click",function(){
			var selectSuppGoodsId=$(this).attr("suppGoodsId");
						
			var selectHotleObj=$("#"+selectSuppGoodsId);			
			var firstHotelObj=$("#firstHotelInfoShow");
			
			var firstSuppGoodsId=firstHotelObj.attr("suppGoodsId");
			if(firstSuppGoodsId==selectSuppGoodsId){
				alert("当前商品已被选择");
				dialog.close();
				dialog=null;
				return;
			}				
			firstHotelObj.find("td").eq(2).find("input").val(0);
					
			firstHotelObj.attr("suppGoodsId",selectSuppGoodsId);
			firstHotelObj.find("td").eq(0).html(selectHotleObj.find("td").eq(0).html());
			firstHotelObj.find("td").eq(1).html(selectHotleObj.find("td").eq(1).html());
			firstHotelObj.find("td").eq(2).html(selectHotleObj.find("td").eq(2).html());        					
	        	
	        var counts=firstHotelObj.find("td").eq(1).attr("num");
		  	firstHotelObj.find("td").eq(1).find("input").val(counts);
		  	
			dialog.close();
			dialog=null;
			try{
	  			setTimeout(changeNextSelect(null,null),500);
			}catch(err){}
			countTotalPrice();
		});
	});	  	
/********************************** end供应商打包产品的更多选择 **********************************/  	

/************** start自主打包——线路组打包 则酒店套餐更多选择、当地游更多选择、跟团游更多选择、自由行更多选择以及跟团游和自由行下的可换酒店更多选择 **************/
	//更换商品时，请求附加信息更新事件  	
	function requestUpdateAddition(Object){		
		var groupId=Object.attr("groupId");
		var productId=Object.attr("productId");	
		var packageProductId=Object.attr("parentProductId");	
		var productBranchId=Object.attr("productBranchId");	
		var goodsId=Object.attr("goodsId");
		var adult=Object.attr("adult");	
		var child=Object.attr("child");
		var specDate=$("#visitTime").val();
		var realSpecDate=Object.attr("visitTime");
		var url="/vst_order/ord/order/queryPackageMoreAddition.do?";
		url=url+"specDate="+specDate+"&productId="+productId+"&packageProductId="+packageProductId+"&groupId="+groupId+"&productBranchId="+productBranchId+"&currentSuppGoodsId="+goodsId+"&adultQuantity="+adult+"&childQuantity="+child+"&productItemIdIndex="+productItemIdIndex+"&realSpecDate="+realSpecDate;
		
		var loading1	= pandora.loading("正在努力加载...");
		//查询附加信息	
		$.get(url,function(result){
			$("."+packageProductId).remove();
			$(".additionTableTd").append(result);
			loading1.close();
			//附加信息份数变化事件
			$(".numText").bind("change",function(){
				var auditPrice=$(this).attr("auditPrice");
				if($(this).val().trim()==""||isNaN($(this).val())){
					$(this).val("0");
				}
				if(parseInt($(this).val())>(getAdultNum()+getChildNum())){
					$(this).val(getAdultNum()+getChildNum());
				}
				var totalPrice=parseFloat(auditPrice)*parseInt($(this).val());
				$(this).parent().parent().find("td").eq(3).html("总价:￥"+totalPrice+"元");
				countTotalPrice();
			});
		});
	};

	//自主打包更多酒店套餐按钮事件	  	
  	$('.moreCategoryLineHotelCom').click(function(){
  		openChangeDiv($(this),"line_hotelcom");
	});	
  	//自主打包更多当地游按钮事件
	$('.moreCategoryLineLocal').click(function(){
		openChangeDiv($(this), "line_local");
	});	  	
	//自主打包更多跟团游
	$('.moreCategoryLineTour').click(function(){
		var url="/vst_order/ord/order/queryPackageMoreProduct.do";
		var productId=$("#productId").val();
		var packageProductId=$this.attr("packageProductId");
		var groupId=$(this).attr("packageGroupId");
		var currentProductBranchId=$(this).attr("packageProductBranchId");
		var specDate=$("#visitTime").val();
		var adultQuantity=$(this).attr("adultNum");
		var childQuantity=$(this).attr("childNum"); 
		var pageFlag="line_tour";
		
		$("input[type=hidden][name=userId]").val(book_user_id);		
		url=url+"?specDate="+specDate+"&productId="+productId+"&packageProductId="+packageProductId+"&groupId="+groupId+"&currentProductBranchId="+currentProductBranchId+"&adultQuantity="+adultQuantity+"&childQuantity="+childQuantity+"&pageFlag="+pageFlag+"&productItemIdIndex="+productItemIdIndex;
		
		//查询
		var dialog = new xDialog(url,null,
						      {title:"更多跟团游",width:800});
		
		$("a[name=xztcBtn]").bind("click",function(){
			var selectProductBranchId=$(this).attr("productBranchId");	
			var groupId=$(this).attr("groupId");	
			var selectHotleObj=$("#"+selectProductBranchId+"_"+groupId);			
			var name="firstLineTourShow"+groupId;	
			var firstHotelObj=$("#"+name);			
			var firstProductBranchId=firstHotelObj.attr("productBranchId");
			
			if(firstProductBranchId==selectProductBranchId){
				alert("当前商品规格已被选择");
				dialog.close();
				dialog=null;
				return;
			}	
			firstHotelObj.attr("groupId",groupId);
			firstHotelObj.attr("productBranchId",selectProductBranchId);
			firstHotelObj.find("td").eq(0).html(selectHotleObj.find("td").eq(0).html());
			firstHotelObj.find("td").eq(1).html(selectHotleObj.find("td").eq(1).html());
			firstHotelObj.find("td").eq(2).html(selectHotleObj.find("td").eq(2).html());
			firstHotelObj.find("td").eq(3).html(selectHotleObj.find("td").eq(3).html());
			
			requestUpdateAddition($(this));			
			
			dialog.close();
			dialog=null;
			countTotalPrice();
		});
	});	
	//自主打包更多自由行按钮事件
	$('.moreCategoryLineSelf').click(function(){
		var url="/vst_order/ord/order/queryPackageMoreProduct.do";
		var productId=$("#productId").val();
		var packageProductId=$this.attr("packageProductId");
		var groupId=$(this).attr("packageGroupId");
		var currentProductBranchId=$(this).attr("packageProductBranchId");
		var specDate=$("#visitTime").val();
		var adultQuantity=$(this).attr("adultNum");
		var childQuantity=$(this).attr("childNum"); 
		var pageFlag="line_self";
		
		$("input[type=hidden][name=userId]").val(book_user_id);		
		url=url+"?specDate="+specDate+"&productId="+productId+"&packageProductId="+packageProductId+"&groupId="+groupId+"&currentProductBranchId="+currentProductBranchId+"&adultQuantity="+adultQuantity+"&childQuantity="+childQuantity+"&pageFlag="+pageFlag+"&productItemIdIndex="+productItemIdIndex;
		
		//查询
		var dialog = new xDialog(url,null,
						      {title:"更多自由行",width:800});
		
		$("a[name=xztcBtn]").bind("click",function(){
			var selectProductBranchId=$(this).attr("productBranchId");	
			var groupId=$(this).attr("groupId");	
			var selectHotleObj=$("#"+selectProductBranchId+"_"+groupId);			
			var name="firstLineSelfShow"+groupId;	
			var firstHotelObj=$("#"+name);			
			var firstProductBranchId=firstHotelObj.attr("productBranchId");
			
			if(firstProductBranchId==selectProductBranchId){
				alert("当前商品规格已被选择");
				dialog.close();
				dialog=null;
				return;
			}	
			firstHotelObj.attr("groupId",groupId);
			firstHotelObj.attr("productBranchId",selectProductBranchId);
			firstHotelObj.find("td").eq(0).html(selectHotleObj.find("td").eq(0).html());
			firstHotelObj.find("td").eq(1).html(selectHotleObj.find("td").eq(1).html());
			firstHotelObj.find("td").eq(2).html(selectHotleObj.find("td").eq(2).html());
			firstHotelObj.find("td").eq(3).html(selectHotleObj.find("td").eq(3).html());
			
			
			/*****************************请求新的附加信息*************************/		
			requestUpdateAddition($(this));			
			/*****************************请求新的附加信息*************************/
			
			dialog.close();
			dialog=null;
			countTotalPrice();
		});
	});
	
	function getFirstTrName(pageFlag){
		var name="";
		if(pageFlag=="line_hotelcom"){
			name="#firstLineHotelComShow";
		}else if(pageFlag=="linehotel"){
			name="#firstLineHotelShow";
		}else if(pageFlag=="changeHotel"){
			name="#firstChangeHotelShow";
		}else if(pageFlag=="linetransport"){
			name="#firstLineTransportShow";
		}else if(pageFlag=="lineticket"){
			name="#firstLineTicketShow";
		}else if(pageFlag=="line_local"){
			name="#firstLineLocalShow";
		}
		return name;
	}

    function changeMoreSelectDate(currElement) {
        var subCategoryId = $("input[name=subCategoryId]").eq(0).val();
        if (subCategoryId != 181) {
            changeMoreSelectDateForOther(currElement);
        } else {
            changeMoreSelectDateForSceneHotel(currElement);
        }
    }

    function changeMoreSelectDateForOther(currElement) {
        var parentSelectPriceMap = {};
        var adultPriceMap = {};
        var childPriceMap = {};
        $(".parentSelectedDateList").eq(0).find("option").each(function() {
            parentSelectPriceMap[$(this).text()] = $(this).val();
        });
        currElement.parent().parent().find("select[class='adultPriceMapSelect']").eq(0).find("option").each(function() {
            adultPriceMap[$(this).text()] = $(this).val();
        });
        currElement.parent().parent().find("select[class='childPriceMapSelect']").eq(0).find("option").each(function() {
            childPriceMap[$(this).text()] = $(this).val();
        });
        var currDate = currElement.find("option:selected").text();
        var currPrice = currElement.find("option:selected").val();
        var parentPrice = parentSelectPriceMap[currDate];
        parentPrice = $("#sourceTotalAmount").val()/100;
        if(parentPrice == undefined || !parentPrice) {
            parentPrice = 0;
        }

        currElement.parent().parent().find("td").eq(0).find(".itemLineSelectDate").val(currDate);

        var gapPrice = currPrice - parentPrice;

        if(gapPrice > 0) {
            gapPrice = "+" + gapPrice;
        }

        var itemLineSelectDatePrice = currElement.parent().parent().find("td[class='itemLineSelectDatePrice']").eq(0);

        if(itemLineSelectDatePrice.text().indexOf("--") < 0) {
            itemLineSelectDatePrice.text("差价:￥" + gapPrice + "元");
        }

        itemLineSelectDatePrice.attr("totalAmount", currPrice * 100);
        itemLineSelectDatePrice.attr("totalAmountYuan", currPrice);

        currElement.parent().prev().find("span[class='adultChildPriceSpan']").eq(0)
            .html("成人价:￥" + adultPriceMap[currDate] + "元/儿童价:￥"
                + childPriceMap[currDate] + "元");
    }


/**
 * 景酒更多商品选择下拉框改变
 *
 * @param currElement
 */
function changeMoreSelectDateForSceneHotel(currElement) {
    var optionSelected = currElement.find("option:selected");
    var index = currElement.attr("index");

    //下面这一段代码只在部分情况下才会起作用，搜索了一下代码，只有当地游才有相关的数据 20170216
    var adultPriceMap = {};
    var childPriceMap = {};
    currElement.parent().parent().find("select[class='adultPriceMapSelect']").eq(0).find("option").each(function () {
        adultPriceMap[$(this).text()] = $(this).val();
    });
    currElement.parent().parent().find("select[class='childPriceMapSelect']").eq(0).find("option").each(function () {
        childPriceMap[$(this).text()] = $(this).val();
    });

    var currDate = optionSelected.text();
    var currPrice = 0;
    if(currElement.attr("data-other") == "true"){
    	var auditPrice = optionSelected.attr("auditPrice");
		var goodsId=currElement.attr("data-suppGoodsid");
		var fitQuantity =  currElement.parent().parent().find("#"+goodsId+"TotalNum").val();
		currElement.attr("currPrice",fitQuantity*auditPrice);
		currPrice = fitQuantity*auditPrice;
    }else{
    	currPrice = parseInt(optionSelected.val());
    }
    //获取同一规格下所有商品的价格总和
    currElement.siblings(".moreLineSelectDate").each(function(){
    	if($(this).attr("data-other") == "true"){
    		var option = $(this).find("option:selected");
    		var auditPrice = option.attr("auditPrice");
    		var goodsId=$(this).attr("data-suppGoodsid");
    		var fitQuantity =  $(this).parent().parent().find("#"+goodsId+"TotalNum").val();
    		$(this).attr("currPrice",fitQuantity*auditPrice);
    		currPrice += fitQuantity*auditPrice;
    	}else{
    		var option = $(this).find("option:selected");
    		currPrice += parseInt(option.val());
    	}
    });
    var parentPrice = 0;
    if(currElement.attr("data-other") == "true"){
    	var groupid_ = currElement.parent().parent().attr("groupid");
    	$('#firstLineTicketShow'+groupid_).find(".lineSelectDate").each(function(){
    		parentPrice += parseInt($(this).attr("currPrice"));
 		});
    }else{
    	parentPrice = $("#sourceTotalAmount").val() / 100;
    }
    if (parentPrice == undefined || !parentPrice) {
        parentPrice = 0;
    }

    //修改隐藏域中的选择时间值
    currElement.parent().parent().find("td").eq(0).find(".itemLineSelectDate").eq(index).val(currDate);

    var gapPrice = currPrice - parentPrice;

    if (gapPrice > 0) {
        gapPrice = "+" + gapPrice;
    }

    var itemLineSelectDatePrice = currElement.parent().parent().find("td[class='itemLineSelectDatePrice']").eq(0);

    if (itemLineSelectDatePrice.text().indexOf("--") < 0) {
        itemLineSelectDatePrice.text("差价:￥" + gapPrice + "元");
    }

    itemLineSelectDatePrice.attr("totalAmount", currPrice * 100);
    itemLineSelectDatePrice.attr("totalAmountYuan", currPrice);

    currElement.parent().prev().find("span[class='adultChildPriceSpan']").eq(0)
        .html("成人价:￥" + adultPriceMap[currDate] + "元/儿童价:￥"
            + childPriceMap[currDate] + "元");
}
	
	/**
	 * 
	 * @param $this 当前选择按钮
	 * @param $changeapi 更换交通按钮
	 * @param type 去程/返程标识
	 */
	function changeText($this,$changeapi,type,firstShowName)
	{	
		var json={} 
		var groupid=$this.attr("data-groupid");
		var goodsid=$this.attr("data-selectedsuppgoodsid");
		var tmp="."+groupid+"_"+goodsid;
		var tmp1="#"+groupid+"_"+goodsid;
		var tmpId="#"+goodsid;
		var zhongzhuanFlag = '';
		
		if(type == 'BACK'){
			$.extend(json,backDJJPgoflight);
			$('.confirmChange_back').unbind();
			$(".confirmChange_back").click(function(){
				changeTextComfirm($this,$changeapi,type,firstShowName,json);
			});
		}else{
			$.extend(json,DJJPgoflight);
			$('.confirmChange_go').unbind();
			$(".confirmChange_go").click(function(){
				changeTextComfirm($this,$changeapi,type,firstShowName,json);
			});
		}
		
		if(json.middle.length>0){
			zhongzhuanFlag = '中转'+"("+json.middle.length+")";
			$(tmp+"qidi_zhuan").html(zhongzhuanFlag);
		}else{
			zhongzhuanFlag = '直飞';
			$(tmp+"qidi_zhuan").html(zhongzhuanFlag);
		}
		
		var info = json.startFrom.starttime +"("+json.startFrom.airport+")-" + 
		json.endTo.datetime + "(" +json.endTo.airport +")" + "&nbsp;&nbsp" + json.airCompany + json.flightNo+
		"&nbsp;&nbsp" + zhongzhuanFlag + "&nbsp;&nbsp" + json.seatInfo[json.selected].seat;
		
		if(type=='BACK'){
			$('.back_info').html(info);
		}else{
			$('.go_info').html(info);
		}
		
	}
	/**
	 * 
	 * @param $this  选择按钮
	 * @param $changeapi 更换按钮
	 * @param type 去程/返程
	 * @param firstShowName 默认展示name节点
	 * @param json 填充使用的数据
	 */
	function changeTextComfirm($this,$changeapi,type,firstShowName,json){
		
		var type1 = $changeapi.attr("transportType");
		var groupid=$this.attr("data-groupid");
		var goodsid=$this.attr("data-selectedsuppgoodsid");
		var adultNum=$this.attr("data-adultQuantity");
		var childNum=$this.attr("data-childQuantity");
		var transProductName = $this.attr("data-transProductName");
		var transProductId = $this.attr("data-productId");
		var jipiaoduijieflag = $this.attr("data-jipiaoduijieflag");
		
		var $travellerDelayFlag = $("[name=travellerDelayFlag]");
		$travellerDelayFlag.attr(type == "BACK" ? "data-backDJFlag" : "data-toDJFlag", jipiaoduijieflag);
		
		var tmp="."+groupid+"_"+goodsid;
		var tmp1="#"+groupid+"_"+goodsid;
		var tmpId="#"+goodsid;
		var tmpNewId=$this.attr("data-suppgoodsid");
		
		var priceAdult=json.seatInfo[json.selected].price;
		var priceChild=json.seatInfo[json.selected].childPrice;
		
		var totalPrice = null;
		if(childNum){
			totalPrice = priceAdult*adultNum+priceChild*childNum;
			$changeapi.attr("oldPrice",totalPrice);
		}else{
			totalPrice = priceAdult*adultNum;
			$changeapi.attr("oldPrice",totalPrice);
		}
		var groupCount = 0;
		var goDate = $(".go_info").attr('goDate');
		groupCount = isEmpty(goDate)?0:groupCount+1;
		var backDate = $(".back_info").attr('backDate');
		groupCount = isEmpty(backDate)?0:groupCount+1;
		
		if(type1 == 'TOBACK'){//TODO 默认交融为往返的情况
			//选择按钮控件对象
			var selectProductBranchId=json.seatInfo[json.selected].branchId;	
			var groupId				=groupid;
			var detailId			=json.seatInfo[json.selected].detailId;
			var packageProductId	=json.seatInfo[json.selected].trafficId;	
			var adult				=adultNum;
			var child				=childNum;	
			
			//被选择的商品信息控件对象
			var selectHotleObj=$("#"+selectProductBranchId+"_"+groupId);
			//显示商品的控件对象
			var name=firstShowName+$changeapi.attr("packageGroupId");	
			var firstObj=$(name);//将要变更属性的节点
			var specDate = '';
			var productId		=$("#productId").val();
			var categaryName = '其他机票';
			
			if(type == 'BACK'){
				$(".back_startTime").html(json.startFrom.starttime);
				$(".back_flightNo").html(json.flightNo);
				specDate = backDJJPgoDate;
				firstObj.attr("backPrice",totalPrice);
				firstObj.attr("backadultPrice",priceAdult);
				firstObj.attr("backchildPrice",priceChild);
				$(".back_startTime").html(json.startFrom.starttime);
				$(".back_startAirportString").html(json.startFrom.airport);
				$(".back_airlineString").html(json.airCompany);
				$(".back_arriveTime").html(json.endTo.datetime);
				$(".back_arriveAirportString").html(json.endTo.airport);
				$(".back_flightNo").html(json.flightNo);
				$(".back_flightNo").closest('div').removeClass('transport_hide');
				
				//下单以及库存检查、计算价格需要传的参数赋值
				if(firstObj.find("td").length == 3){
					firstObj.append(
							'<td>'+
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].goodsId" 	value="' +$this.attr("data-suppgoodsid")+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].detailId" 		value="' +detailId+ '" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].routeRelation" 	value="PACK" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].quantity" 		value="' +$this.attr("data-quantity")+ '" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].visitTime" 		value="' +specDate+ '" ></input></td>'
					);
					firstObj.append(
							'<td><input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].adultQuantity" 	value="' +adult+ '" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].childQuantity" 	value="' +child+ '" ></input>'+
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].adultAmt" 	value="' +priceAdult+ '" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].childAmt" 	value="' +priceChild+ '" ></input>'+
							//<!--增加机票短信模板相关的信息--起-->
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].backDate" 	value="' +backDate+ '" ></input>'+
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].companyName" value="' +json.airCompany+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].flightNo" value="' +json.flightNo+ '" ></input>' +
							//飞机类型，
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].planeCode" value="' +json.planeCode+ '" ></input>' +
							//返程交通，交通类型为2
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].flightType" value="2" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].fromAirPort" value="' +json.startFrom.airport+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].toAirPort" value="' +json.endTo.airport+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].flyTime" value="' +json.flyTime+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].fromCityName" value="' +json.fromCityName+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].toCityName" value="' +json.toCityName+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].startTerminal" value="' +json.startTerminal+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].arriveTerminal" value="' +json.arriveTerminal+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].goTimeStr" value="' +json.startFrom.starttime+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].arriveTimeStr" value="' +json.endTo.datetime+ '" ></input>' +
							//<!--增加机票短信模板相关的信息--止-->
							'</td>'
					);
				}else if(firstObj.find("td").length == 5){
					firstObj.find("td").eq(3).html(
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].goodsId" 		value="' +$this.attr("data-suppgoodsid")+ '" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].detailId" 		value="' +detailId+ '" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].routeRelation" 	value="PACK" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].quantity" 		value="' +$this.attr("data-quantity")+ '" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].visitTime" 		value="' +specDate+ '" ></input>'
					);
					firstObj.find("td").eq(4).html(
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].adultQuantity" 	value="' +adult+ '" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].childQuantity" 	value="' +child+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].adultAmt" 	value="' +priceAdult+ '" ></input>' + 
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].childAmt" 	value="' +priceChild+ '" ></input>'+
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].backDate" 	value="' +backDate+ '" ></input>'+
							//<!--增加机票短信模板相关的信息--起-->
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].backDate" 	value="' +backDate+ '" ></input>'+
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].companyName" value="' +json.airCompany+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].flightNo" value="' +json.flightNo+ '" ></input>' +
							//飞机类型，
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].planeCode" value="' +json.planeCode+ '" ></input>' +
							//返程交通，交通类型为2
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].flightType" value="2" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].fromAirPort" value="' +json.startFrom.airport+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].toAirPort" value="' +json.endTo.airport+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].flyTime" value="' +json.flyTime+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].fromCityName" value="' +json.fromCityName+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].toCityName" value="' +json.toCityName+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].startTerminal" value="' +json.startTerminal+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].arriveTerminal" value="' +json.arriveTerminal+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].goTimeStr" value="' +json.startFrom.starttime+ '" ></input>' +
							'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].arriveTimeStr" value="' +json.endTo.datetime+ '" ></input>'
							//<!--增加机票短信模板相关的信息--止-->
					);
				}
				
				//设置的一个标志flag，主要是为了展示去程或返程交通产品的编辑链接
				var ifBack =  firstObj.find("td").eq(0).find('a[name=second]').attr('flag');
				if('BACK'==ifBack){
					firstObj.find("td").eq(0).find('a[name=second]').remove();
				}
				firstObj.find("td").eq(0).append('<a name="second"  flag="BACK" onclick =openProduct('+transProductId+',21,"'+categaryName+'")>'+transProductName+'</a>');
				
				var ifTo = firstObj.find("td").eq(0).find('a[name=first]').attr('flag');
				//如果ifTo为空，则表示往返更换为单返程，没有单去程，则把前两列置空
				if(isEmpty(ifTo) || ifTo!='TO'){
					firstObj.find("td").eq(0).html('');
					firstObj.find("td").eq(1).html('');
					$(".to_startTime").html('');
					$(".to_startAirportString").html('');
					$(".to_airlineString").html('');
					$(".to_arriveTime").html('');
					$(".to_arriveAirportString").html('');
					$(".to_flightNo").html('');
					$(".to_flightNo").closest('div').addClass('transport_hide');
				}
			}else{
				$(".to_startTime").html(json.startFrom.starttime);
				$(".to_flightNo").html(json.flightNo);
				specDate = DJJPgoDate;
				firstObj.attr("toPrice",totalPrice);
				firstObj.attr("toadultPrice",priceAdult);
				firstObj.attr("tochildPrice",priceChild);
				$(".to_startTime").html(json.startFrom.starttime);
				$(".to_startAirportString").html(json.startFrom.airport);
				$(".to_airlineString").html(json.airCompany);
				$(".to_arriveTime").html(json.endTo.datetime);
				$(".to_arriveAirportString").html(json.endTo.airport);
				$(".to_flightNo").html(json.flightNo);
				$(".to_flightNo").closest('div').removeClass('transport_hide');
				//长度为3表示此时还没加载返程数据,隐藏返程结构
				if(firstObj.find("td").length == 3){
					$(".back_startTime").html('');
					$(".back_startAirportString").html('');
					$(".back_airlineString").html('');
					$(".back_arriveTime").html('');
					$(".back_arriveAirportString").html('');
					$(".back_flightNo").html('');
					$(".back_flightNo").closest('div').addClass('transport_hide');
				}
				
				var ifTo = firstObj.find("td").eq(0).find('a[name=first]').attr('flag');
				var backHtml = firstObj.find("td").eq(0).find('a[name=second]');
				//下单以及库存检查、计算价格需要传的参数赋值
				firstObj.find("td").eq(0).html(
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].goodsId" 		value="' +$this.attr("data-suppgoodsid")+ '" ></input>' + 
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].detailId" 		value="' +detailId+ '" ></input>' + 
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].routeRelation" 	value="PACK" ></input>' + 
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].quantity" 		value="' +$this.attr("data-quantity")+ '" ></input>' + 
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].visitTime" 		value="' +specDate+ '" ></input>'
				);
				if('TO'==ifTo){
					firstObj.find("td").eq(0).find('a[name=first]').remove();
				}
				firstObj.find("td").eq(0).append('<a name="first" flag="TO" onclick =openProduct('+transProductId+',21,"'+categaryName+'")>'+transProductName+'</a><br/>');
				firstObj.find("td").eq(0).append(backHtml);
				
				firstObj.find("td").eq(1).html(
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].adultQuantity" 	value="' +adult+ '" ></input>' + 
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].childQuantity" 	value="' +child+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].adultAmt" 	value="' +priceAdult+ '" ></input>' + 
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].childAmt" 	value="' +priceChild+ '" ></input>'+
						//<!--增加机票短信模板相关的信息--起-->
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].toDate" 	value="' +goDate+ '" ></input>'+
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].companyName" value="' +json.airCompany+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].flightNo" value="' +json.flightNo+ '" ></input>' +
						//飞机类型，
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].planeCode" value="' +json.planeCode+ '" ></input>' +
						//去程交通，交通类型为1
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].flightType" value="1" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].fromAirPort" value="' +json.startFrom.airport+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].toAirPort" value="' +json.endTo.airport+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].flyTime" value="' +json.flyTime+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].fromCityName" value="' +json.fromCityName+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].toCityName" value="' +json.toCityName+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].startTerminal" value="' +json.startTerminal+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].arriveTerminal" value="' +json.arriveTerminal+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].goTimeStr" value="' +json.startFrom.starttime+ '" ></input>' +
						'<input type="hidden" name="productMap['+productId+'].itemList['+productItemIdIndex+'].additionalFlightNoVoList[0].arriveTimeStr" value="' +json.endTo.datetime+ '" ></input>'
					//<!--增加机票短信模板相关的信息--止-->
				);
			}
			var firstObjToPrice = isEmpty(firstObj.attr("toPrice"))?0:parseFloat(firstObj.attr("toPrice"))/100;
			var firstObjBackPrice = isEmpty(firstObj.attr("backPrice"))?0:parseFloat(firstObj.attr("backPrice"))/100;
			var totalAmount 	= firstObjToPrice+firstObjBackPrice;
			var firstObjAdultPrice = (isEmpty(firstObj.attr("toadultPrice"))?0:parseFloat(firstObj.attr("toadultPrice"))/100)
			                           +(isEmpty(firstObj.attr("backadultPrice"))?0:parseFloat(firstObj.attr("backadultPrice"))/100);
			var firstObjChildPrice = (isEmpty(firstObj.attr("tochildPrice"))?0:parseFloat(firstObj.attr("tochildPrice"))/100)
					                   +(isEmpty(firstObj.attr("backchildPrice"))?0:parseFloat(firstObj.attr("backchildPrice"))/100);
			
			//重新设置将要显示的商品对象属性
			if($("input[name='categoryId']").val() == "18" && $("input[name='subCategoryId']").val() == "182" && $("input[name='productType']").val() == "INNERLINE"){
				firstObj.find("td").eq(2).html("成人:￥"+firstObjAdultPrice
						+" X "+adultNum
						+"<br>儿童:￥"+firstObjChildPrice
						+" X "+childNum
						+"<br>"+"总价:￥"+totalAmount+"元");
			}else{
				firstObj.find("td").eq(2).html("总价:￥"+totalAmount+"元");//需要计算去程+返程价格  待做
			}
			firstObj.attr("totalAmount",totalAmount*100);
			firstObj.attr("groupId",groupId);
			firstObj.attr("detailId",detailId);
			firstObj.attr("productBranchId",selectProductBranchId);
			productItemIdIndex = parseInt(productItemIdIndex) + 1;
			$changeapi.attr("packageProductBranchId","-1");
			
		}else{//默认交通为单程的情况
			//返程则填充返程，去程则填充去程
			if(type == 'BACK'){
				$(".back_gotime").html(json.startFrom.starttime);
				$(".back_flightNo").html(json.flightNo);
			}else{
				$(".to_gotime").html(json.startFrom.starttime);
				$(".to_flightNo").html(json.flightNo);
			}


			$(tmp+"goTime").html(json.startFrom.starttime);
			$(tmp+"arriveTime").html(json.endTo.datetime);
			$(tmp+"companyName").html(json.airCompany);
			$(tmp+"seatName").html(json.seatInfo[json.selected].seat);
			$(tmp+"flyTime").html(json.totalTime);
			$(tmp+"fromAirPort").html(json.startFrom.airport);
			$(tmp+"toAirPort").html(json.endTo.airport);
			$(tmp+"flightNo").html(json.flightNo);
//			$(tmp+"qidi_zhuan").html();
			$(tmp1+"plane_type").html(json.planStyle);
			$(tmpId+"goodsId").attr("value",$this.attr("data-suppgoodsid"));
			$(tmpId+"detailId").attr("value",json.seatInfo[json.selected].detailId);
			$(tmpId+"adultAmt").attr("value",priceAdult);
			$(tmpId+"childAmt").attr("value",priceChild);

			//<!--增加机票短信模板相关的信息--起-->
			if(type=='BACK') {
				$(tmpId + "backDate").attr({"value":backDate,"id":tmpNewId + "backDate"});
				$(tmpId+"additionalFlightNoVoList-flightType").attr({"value":"2","id":tmpNewId+"additionalFlightNoVoList-flightType"});
			} else {
				$(tmpId + "toDate").attr({"value":goDate,"id":tmpNewId + "toDate"});
				$(tmpId+"additionalFlightNoVoList-flightType").attr({"value":"1","id":tmpNewId+"additionalFlightNoVoList-flightType"});
			}
			//设定更改后的交通信息，并修改控件的id，把id中包含的商品id设定为更改后的机票的商品id
			$(tmpId+"additionalFlightNoVoList-companyName").attr({"value":json.airCompany,"id":tmpNewId+"additionalFlightNoVoList-companyName"});
			$(tmpId+"additionalFlightNoVoList-flightNo").attr({"value":json.flightNo,"id":tmpNewId+"additionalFlightNoVoList-flightNo"});
			$(tmpId+"additionalFlightNoVoList-planeCode").attr({"value":json.planeCode,"id":tmpNewId+"additionalFlightNoVoList-planeCode"});
			$(tmpId+"additionalFlightNoVoList-fromAirPort").attr({"value":json.startFrom.airport,"id":tmpNewId+"additionalFlightNoVoList-fromAirPort"});
			$(tmpId+"additionalFlightNoVoList-toAirPort").attr({"value":json.endTo.airport,"id":tmpNewId+"additionalFlightNoVoList-toAirPort"});
			$(tmpId+"additionalFlightNoVoList-flyTime").attr({"value":json.flyTime,"id":tmpNewId+"additionalFlightNoVoList-flyTime"});
			$(tmpId+"additionalFlightNoVoList-fromCityName").attr({"value":json.fromCityName,"id":tmpNewId+"additionalFlightNoVoList-fromCityName"});
			$(tmpId+"additionalFlightNoVoList-toCityName").attr({"value":json.toCityName,"id":tmpNewId+"additionalFlightNoVoList-toCityName"});
			$(tmpId+"additionalFlightNoVoList-startTerminal").attr({"value":json.startTerminal,"id":tmpNewId+"additionalFlightNoVoList-startTerminal"});
			$(tmpId+"additionalFlightNoVoList-arriveTerminal").attr({"value":json.arriveTerminal,"id":tmpNewId+"additionalFlightNoVoList-arriveTerminal"});
			$(tmpId+"additionalFlightNoVoList-goTimeStr").attr({"value":json.startFrom.starttime,"id":tmpNewId+"additionalFlightNoVoList-goTimeStr"});
			$(tmpId+"additionalFlightNoVoList-arriveTimeStr").attr({"value":json.endTo.datetime,"id":tmpNewId+"additionalFlightNoVoList-arriveTimeStr"});
			//<!--增加机票短信模板相关的信息--止-->
			
			var tmpClass=groupid+"_"+tmpNewId;
			$(tmp+"goTime").attr("class",tmpClass+"goTime");
			$(tmp+"arriveTime").attr("class",tmpClass+"arriveTime");
			$(tmp+"companyName").attr("class",tmpClass+"companyName");
			$(tmp+"seatName").attr("class",tmpClass+"seatName");
			$(tmp+"flyTime").attr("class",tmpClass+"flyTime");
			$(tmp+"fromAirPort").attr("class",tmpClass+"fromAirPort");
			$(tmp+"toAirPort").attr("class",tmpClass+"toAirPort");
			$(tmp+"flightNo").attr("class",tmpClass+"flightNo");
			$(tmp+"qidi_zhuan").attr("class",tmpClass+"qidi_zhuan");
			$(tmp1+"plane_type").attr("id",tmpClass+"plane_type");
			$(tmpId+"goodsId").attr("id",tmpNewId+"goodsId");
			$(tmpId+"detailId").attr("id",tmpNewId+"detailId");
			$(tmpId+"adultAmt").attr("id",tmpNewId+"adultAmt");
			$(tmpId+"childAmt").attr("id",tmpNewId+"childAmt");
			
			var groupid=$this.attr("data-groupid");
			var goodsid=$this.attr("data-selectedsuppgoodsid");
			
		}
		//重新设置更多按钮属性
		if(type=='BACK'){
			$(tmp+"visitTime").html(backDJJPgoDate);
			$changeapi.attr("backPackageGroupId",groupid);
			$changeapi.attr("backPackageProductId",json.seatInfo[json.selected].trafficId);
			$changeapi.attr("backPackageProductBranchId",json.seatInfo[json.selected].branchId);
			$changeapi.attr("backSelectedSuppGoodsId",tmpNewId);
			$changeapi.attr("backPrice",totalPrice);
			$changeapi.closest(".old_plane").parent().find(".adultChildPrice_duijie").attr("backadultPrice",priceAdult);
			$changeapi.closest(".old_plane").parent().find(".adultChildPrice_duijie").attr("backchildPrice",priceChild);
		}else{
			$(tmp+"visitTime").html(DJJPgoDate);
			$changeapi.attr("toPackageGroupId",groupid);
			$changeapi.attr("toPackageProductId",json.seatInfo[json.selected].trafficId);
			$changeapi.attr("toPackageProductBranchId",json.seatInfo[json.selected].branchId);
			$changeapi.attr("toSelectedSuppGoodsId",tmpNewId);
			$changeapi.attr("toPrice",totalPrice);
			$changeapi.closest(".old_plane").parent().find(".adultChildPrice_duijie").attr("toadultPrice",priceAdult);
			$changeapi.closest(".old_plane").parent().find(".adultChildPrice_duijie").attr("tochildPrice",priceChild);
		}
		$changeapi.attr("adultNum",adultNum);
		$changeapi.attr("childNum",childNum);
		$changeapi.closest(".old_plane").parent().find(".adultChildPrice_duijie").attr("adultNum",adultNum);
		$changeapi.closest(".old_plane").parent().find(".adultChildPrice_duijie").attr("childNum",childNum);
	}
	
	/**
	 * 空校验
	 * @param param
	 * @returns {Boolean}
	 */
	function isEmpty(param){
		if(param == null || param == '' || typeof(param) == 'undefined'){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 更换商品窗口操作
	 * 
	 * @param $this
	 * @param pageFlag
	 */
	var upWindow;
	function openChangeDiv($this,pageFlag){
		var url="/vst_order/ord/order/queryPackageMoreProduct.do";

		var productId=$("#productId").val();
		var outProductId=$this.attr("outProductId");
		var packageGroupId=$this.attr("packageGroupId");
		
		var groupId=$this.attr("packageGroupId");
		var packageProductId=$this.attr("packageProductId");
		var currentProductBranchId=$this.attr("packageProductBranchId");
		var selectedSuppGoodsId=$this.attr("selectedSuppGoodsId");
		var toPrice = $this.attr("toPrice");//交通去程价格
		
		var adultQuantity=$this.attr("adultNum");
		var childQuantity=$this.attr("childNum"); 
		var firstShowName=getFirstTrName(pageFlag);
		var packageProductBranchId=currentProductBranchId;
		var quantity=$this.attr("quantity");
		var $firstShowTr = $(firstShowName+groupId);
		var totalAmount = $firstShowTr.attr("totalAmount");
		var haveChangeButton=$this.attr("haveChangeButton");
		var oldPrice=$this.attr("oldPrice");
		var backPrice = $this.attr("backPrice");//交通返程价格
		var specDate = $("#visitTime").val();
		if(!specDate) {
			specDate = $firstShowTr.find("input[name$='.visitTime']").val();
		}
		$("input[type=hidden][name=userId]").val(book_user_id);	
		var selectedDate = $firstShowTr.find("input[name$='.visitTime']").val();
		
		// 获取订单 品类id 182 自由行 机酒
		var hotelSubCategoryId = $("#hotelSubCategoryId").val();
		// 只针对 自由行 机酒 国内
		if (hotelSubCategoryId != undefined && hotelSubCategoryId == "182" && $("input[name='productType']").val() == "INNERLINE") {
			var thisSelected = $this.closest("p").next().find(".machineWineTable").find("#selectedHotel").closest("tr");
			upWindow = $this.closest("p").next().find(".machineWineTable");
			selectedDate = $(thisSelected).closest("tr").find("input[name$='.visitTime']").val();
			totalAmount = $(thisSelected).closest("tr").attr("totalAmount");
		}
		url=url+"?selectedDate=" + selectedDate + "&specDate="+specDate+"&productId="+productId
		+"&packageProductId="+packageProductId+"&groupId="+groupId+"&currentProductBranchId="+currentProductBranchId
		+"&adultQuantity="+adultQuantity+"&childQuantity="+childQuantity
		+"&toPrice="+toPrice
		+"&backPrice="+backPrice
		+"&pageFlag="+pageFlag+"&productItemIdIndex="+productItemIdIndex+"&totalAmount="+totalAmount;
		
		var jStartDistrict = $("#queryStartDistrictId");
		if(jStartDistrict && jStartDistrict.length>0){
			url += "&startDistrictId="+jStartDistrict.attr("data-cityid");
		}
		//查询
		var titleName = $this.text();
		if(!titleName) {
			titleName = "更多可换酒店";
		}

		var dialog;
		dialog = new xDialog(url,null,
			      {title:titleName,width:900});
		
		$(".selectmoreQunantityDropdownListTicket").bind("change",function(){
			var goodsId=$(this).attr("data-suppGoodsid");
			$(this).parent().parent().find("#"+goodsId+"TotalNum").val($(this).val());
			changeMoreSelectDateForSceneHotel($(this).prev(".moreLineSelectDate"));
		});
 		$('.moreLineSelectDate').each(function(){
 			var moregoodsId = $(this).attr("data-suppGoodsid");
			if(changeNextSelect($(this),null) == true){
			}
			setTimeout(function(){
				$firstShowTr.find(".selectQunantityDropdownListTicket").each(function() {
					var ticket_ = $(this);
					$(".selectmoreQunantityDropdownListTicket").each(function() {
						if(ticket_.attr("data-suppGoodsid") == $(this).attr("data-suppGoodsid")){
							var ticketnum_ = ticket_.val();
							var haveNum = false;
							$(this).find("option").each(function() {
								if(parseInt($(this).val()) == parseInt(ticketnum_)) {
									haveNum = true;
								}
							});
							if(haveNum){
								$(this).val(ticketnum_);
								var goodsId=$(this).attr("data-suppGoodsid");
								$(this).parent().parent().find("#"+goodsId+"TotalNum").val(ticketnum_);
							}
						}
					});
				});
			},500);
 			changeMoreSelectDate($(this));
 		});
 		setTimeout(function(){
				$firstShowTr.find(".selectQunantityDropdownListTicket").each(function() {
					var ticket_ = $(this);
					$(".selectmoreQunantityDropdownListTicket").each(function() {
						if(ticket_.attr("data-suppGoodsid") == $(this).attr("data-suppGoodsid")){
							var ticketnum_ = ticket_.val();
							var haveNum = false;
							$(this).find("option").each(function() {
								if(parseInt($(this).val()) == parseInt(ticketnum_)) {
									haveNum = true;
								}
							});
							if(haveNum){
								$(this).val(ticketnum_);
								var goodsId=$(this).attr("data-suppGoodsid");
								$(this).parent().parent().find("#"+goodsId+"TotalNum").val(ticketnum_);
							}
						}
					});
				});
			},1000);
 		// 只针对 自由行机酒 182 国内
 		$("span[name=hotelSelected]").bind("click", function(){
 			var thisTable = $(this).closest("table"),
 				firstLineHotel = $("#firstLineHotelShow"+ $(this).attr("detailId-data") +"_productNameLink"); //this
 				
 			$(firstLineHotel).find("td:eq(0)").attr("width", "");
 			$(firstLineHotel).find("td:eq(0)").attr("colspan", "3");
 			$(firstLineHotel).find("td:eq(1)").remove();
 			var upTable = $(upWindow).closest(".hotel_check_table");
 			$(upTable).html($(thisTable).html());
 			//获取房型数量 若果大于1 则展示 更多全部按钮
 			var hotelLength = $(thisTable).find(".machineWineTable").find(".hotelBtnOk").length;
 			if (hotelLength >= 2) {
 				$(upTable).next().attr("style", "cursor: pointer; margin:10px 0 0 10px; position:relative;height:20px; display:inline-block; line-height:20px;padding:0 10px 0 10px;border:#ddd solid 1px;");
 				$(upTable).next().removeClass("list_more_close");
 				$(upTable).next().html("展开全部");
 			} else {
 				$(upTable).next().attr("style", "cursor: pointer; margin:10px 0 0 10px; position:relative;height:20px; display:none; line-height:20px;padding:0 10px 0 10px;border:#ddd solid 1px;");
 			}
 			//linehotel.ftl
 			$(upTable).find("#selectedHotel").closest("table").closest("tr").attr("style", "display: table-row;")
 			//修改
 			$(upTable).prev().find("a").attr("packageproductid", $(this).attr("pid-data"));
 			//关闭窗口
 			dialog.close();
 			//点击已选 按钮重新计算价格
 			$(upTable).find("#selectedHotel").click();
 		});
		$("a[name=xztcBtn]").bind("click",function(){
			//选择按钮控件对象
			var selectProductBranchId=$(this).attr("productBranchId");	
			var groupId=$(this).attr("groupId");
			var detailId=$(this).attr("detailId");
			var packageProductId=$(this).attr("packageProductId");	
			var adult=$(this).attr("adult");
			var child=$(this).attr("child");	
			
			//被选择的商品信息控件对象
			var selectHotleObj=$("#"+selectProductBranchId+"_"+groupId);
			//显示商品的控件对象
			var name=firstShowName+groupId;	
			var firstObj=$(name);
			var firstObjPrdLink = $(name + "_productNameLink");
			
			//判断之前显示的商品是否和当前选择的商品是同一个
			var firstProductBranchId=firstObj.attr("productBranchId");
			if(firstProductBranchId==selectProductBranchId){
				alert("当前商品规格已被选择");
				dialog.close();
				dialog=null;
				return;
			}
			
			if(firstObjPrdLink) {
				var selectedProductLink = selectHotleObj.find("div[class='productNameLink']");
				if(selectedProductLink && selectedProductLink != undefined) {
					selectedProductLink = selectedProductLink.eq(0);
				}
				
				firstObjPrdLink.find("td").eq(0).html(selectedProductLink.html());
			}
			
			//重新设置将要显示的商品对象属性
			if (selectHotleObj.find("td p").eq(0).html() == undefined) {
				firstObj.find("td").eq(0).html(selectHotleObj.find("td").eq(0).html());
			} else {
				//为了fix 同一规格门票下既有儿童也有成人问题
				var $DestTD = firstObj.find("td");
				$DestTD.empty();
				selectHotleObj.find("td p").each(function(index, e){
					if(index != 0) {
						$DestTD.append("<br>");
					}
					$DestTD.append($(e).html());
				});
				//firstObj.find("td").eq(0).html(selectHotleObj.find("td p").eq(0).html());
			}
			firstObj.find("td").eq(1).html(selectHotleObj.find("td").eq(1).html());
			firstObj.find("td").eq(2).html(selectHotleObj.find("td").eq(2).html());
			var $td;
			if(pageFlag != "line_local"){
				$td = selectHotleObj.find("td").eq(2);//酒店组、交通组、门票组、线路酒店套餐
				firstObj.find("td").eq(2).html("总价:￥"+changeTwoDecimal_f($td.attr("totalAmountYuan"))+"元");
			}else{
				$td = selectHotleObj.find("td").eq(3);//线路当地游、线路跟团游、线路自由行、线路可换酒店
				firstObj.find("td").eq(3).html("总价:￥"+$td.attr("totalAmountYuan")+"元");
			}
			firstObj.attr("totalAmount",$td.attr("totalAmount"));
			firstObj.attr("groupId",groupId);
			firstObj.attr("detailId",detailId);
			firstObj.attr("productBranchId",selectProductBranchId);
			
			//重新设置更多按钮对象属性
			$this.attr("packageProductBranchId",selectProductBranchId);
			$this.attr("packageGroupId",groupId);
			$this.attr("packageProductId",packageProductId);
			$this.attr("adultNum",adult);
			$this.attr("childNum",child);	
			
			//门票和local使用
			var moreSelectedDate = selectHotleObj.find("select[class='moreLineSelectDate']").eq(0).find("option:selected").text();
			dialog.close();
			dialog=null;
			if(pageFlag == "line_local"){
				getTotalUnitPriceForLocal($(this), moreSelectedDate);//计算房差
			}
			//门票和local使用
			$('.moreLineSelectDate').each(function() {
				var currObj = $(this);
				currObj.attr("class", "lineSelectDate");
				currObj.bind("change",function(){
					try{
						setTimeout(changeNextSelect($(this),null),500);
					}catch(err){}
					lineSelectDateChange($(this));
				});
				currObj.find("option").each(function() {
					$(this).removeAttr("selected");
				});
				currObj.find("option").each(function() {
					if($(this).text() == moreSelectedDate) {
						$(this).attr("selected", true);
					}
				});
			});
			$('.selectmoreQunantityDropdownListTicket').each(function() {

				var thisDropdownList_ = $(this);
				thisDropdownList_.attr("class", "selectQunantityDropdownListTicket");
				var goodsId_=thisDropdownList_.attr("data-suppGoodsid");
				thisDropdownList_.val(thisDropdownList_.parent().parent().find("#"+goodsId_+"TotalNum").val());
				thisDropdownList_.bind("change",function(){
					var goodsId=$(this).attr("data-suppGoodsid");
					$(this).parent().parent().find("#"+goodsId+"TotalNum").val($(this).val());
					setTicketPersonNum();
					lineSelectDateChangeForSceneHotel($(this).prev(".lineSelectDate"));
				});
			});
			setTicketPersonNum();
			//往返推荐全部对接机票，父窗体设置为对接机票
			$(window.parent.document).find("input[name=hasApiFlight]").val("Y");
			try{
	  			setTimeout(changeNextSelect(null,null),500);
			}catch(err){}
			if(firstObj.attr("isroute") == "true"){
				firstObj.find(".hotelAdultNumText").bind("change",function(){
					var auditPrice=$(this).attr("auditPrice");
					if($(this).val().trim()==""||isNaN($(this).val())){
						$(this).val("0");
					}
					if($(this).attr("isHotel") != "true"){
						if(parseInt($(this).val())>(getAdultNum())){
							$(this).val(getAdultNum());
						}
					}
					//设置商品总数
					var goodsId=$(this).attr("goodsId");
					var totalNum=parseInt($(this).val());
					$(this).parent().parent().find("#"+goodsId+"TotalNum").val(totalNum);
					
					//更新价格
					var totalPrice=parseFloat(auditPrice)*parseInt($(this).val());
					$(this).parent().parent().find("td").eq(2).html("总价:￥"+changeTwoDecimal_f(totalPrice)+"元");
					try{
						setTimeout(changeNextSelect(null,null),500);
					}catch(err){}
					countTotalPrice();	
				});
			}
			countTotalPrice();
		});
	}
	
/**
 * start openChangeDivTransport add by zm
 * @param $this 更换按钮
 * @param pageFlag 交通标志
 */
function openChangeDivTransport($this,pageFlag){
		var url="/vst_order/ord/order/queryPackageMoreProduct.do";
		if( ("true" == $this.attr("isFlight")&& $this.attr("transportType") != "TOBACK") ||	pageFlag == "apilinetransport"){
			url="/vst_order/ord/order/getBasicJpData1.do";
		}
		var firstShowName		=getFirstTrName("linetransport");
		if(pageFlag=="apilinetransport"){
			$('.do-select-action_go').die();
			$(".do-select-action_go").live("click",function(){
				changeText($(this),$this,'TO',firstShowName);
			});
		}
		var productId				=$("#productId").val();
		var outProductId			=$this.attr("outProductId");
		
		var groupId					=$this.attr("packageGroupId");
		var packageProductId		=$this.attr("packageProductId");
		var currentProductBranchId	=$this.attr("packageProductBranchId");
		var selectedSuppGoodsId		=$this.attr("selectedSuppGoodsId");
		
		var toGroupId					=$this.attr("toPackageGroupId");
		var toPackageProductId			=$this.attr("toPackageProductId");
		var toCurrentProductBranchId	=$this.attr("toPackageProductBranchId");
		var toSelectedSuppGoodsId		=$this.attr("toSelectedSuppGoodsId");
		var toPrice 					=$this.attr("toPrice");//交通去程价格
		
		var backGroupId					=$this.attr("backPackageGroupId");
		var backPackageProductId		=$this.attr("backPackageProductId");
		var backCurrentProductBranchId	=$this.attr("backPackageProductBranchId");
		var backSelectedSuppGoodsId		=$this.attr("backSelectedSuppGoodsId");
		var backPrice 					=$this.attr("backPrice");//交通返程价格
		if(toGroupId==groupId){
			toGroupId = "";
		}
		var adultQuantity		=$this.attr("adultNum");
		var childQuantity		=$this.attr("childNum"); 
		var packageProductBranchId=currentProductBranchId;
		var quantity			=getCopies();//$this.attr("quantity");
		var $firstShowTr 		=$(firstShowName+groupId);
		var totalAmount 		=$firstShowTr.attr("totalAmount");
		var haveChangeButton	=$this.attr("haveChangeButton");
		var oldPrice			=$this.attr("oldPrice");
		var specDate 			=$("#visitTime").val();
		var selectedDate 		=$firstShowTr.find("input[name$='.visitTime']").val();
		
		var goTime = '';
		var flightNo = '';
		if($this.attr("transportType") != "TOBACK"){
			goTime = $(".to_gotime").html();
			flightNo = $(".to_flightNo").html();
		}else{
			goTime = $(".to_startTime").html();
			flightNo = $(".to_flightNo").html();
		}
		
		if(!specDate) {
			specDate = $firstShowTr.find("input[name$='.visitTime']").val();
		}
		$("input[type=hidden][name=userId]").val(book_user_id);	
		if(pageFlag=="apilinetransport" || ("true" == $this.attr("isFlight")&& $this.attr("transportType") != "TOBACK"))
		{	
			url=url+"?adultQuantity="	+adultQuantity
			+"&childQuantity="			+childQuantity
			+"&lvmamaProductId="		+productId
			+"&packageGroupId="			+toGroupId
			+"&packageProductId="		+toPackageProductId
			+"&packageProductBranchId="	+toCurrentProductBranchId
			+"&selectedSuppGoodsId="	+toSelectedSuppGoodsId
			+"&toPrice="				+toPrice
			+"&quantity="				+quantity
			+"&haveChangeButton="		+haveChangeButton
			+"&oldPrice="				+oldPrice
			+"&changeType=traffic"
			+"&selectDate="				+specDate
			+"&transportType="			+$this.attr("transportType")
			+"&selectedFlag="			+goTime + flightNo + toSelectedSuppGoodsId
		}else if(pageFlag=="linetransport"){
			url=url+"?selectedDate=" + specDate + "&specDate="+specDate+"&productId="+productId
			+"&packageProductId="+packageProductId+"&groupId="+groupId+"&currentProductBranchId="+currentProductBranchId
			+"&adultQuantity="+adultQuantity+"&childQuantity="+childQuantity
			+"&toPrice="+toPrice
			+"&backPrice="+backPrice
			+"&pageFlag="+pageFlag+"&productItemIdIndex="+productItemIdIndex+"&totalAmount="+totalAmount;
		}
		var jStartDistrict = $("#queryStartDistrictId");
		if(jStartDistrict && jStartDistrict.length>0){
			url += "&startDistrictId="+jStartDistrict.attr("data-cityid");
		}

		var titleName = $this.text();
		var dialog;
		if(pageFlag=="apilinetransport"){
			dialog = new xDialog(url,null,
				      {title:titleName,width:900});
		}else{
			dialog = new xDialog(url,null,
				      {title:titleName,width:800});
		}
		
 		$('.moreLineSelectDate').each(function(){
 			changeMoreSelectDate($(this));
 		});
		$("a[name=xztcBtn]").bind("click",function(){
				//选择按钮控件对象
				var selectProductBranchId=$(this).attr("productBranchId");	
				var groupId=$(this).attr("groupId");
				var detailId=$(this).attr("detailId");
				var packageProductId=$(this).attr("packageProductId");	
				var adult=$(this).attr("adult");
				var child=$(this).attr("child");	
				
				//被选择的商品信息控件对象
				var selectHotleObj=$("#"+selectProductBranchId+"_"+groupId);
				//显示商品的控件对象
				var name=firstShowName+groupId;	
				var firstObj=$(name);
				
				//判断之前显示的商品是否和当前选择的商品是同一个
				var firstProductBranchId=firstObj.attr("productBranchId");
				if(firstProductBranchId==selectProductBranchId){
					alert("当前商品规格已被选择");
					dialog.close();
					dialog=null;
					return;
				}
				
				//重新设置将要显示的商品对象属性
				firstObj.find("td").eq(0).html(selectHotleObj.find("td").eq(0).html());
				firstObj.find("td").eq(1).html(selectHotleObj.find("td").eq(1).html());
				firstObj.find("td").eq(2).html(selectHotleObj.find("td").eq(2).html());
				if(firstObj.find("td").length == 5){
					firstObj.find("td").eq(3).html('');
					firstObj.find("td").eq(4).html('');
				}
				var $td = selectHotleObj.find("td").eq(2);//酒店组、交通组、门票组、线路酒店套餐
				if($("input[name='categoryId']").val() == "18" && $("input[name='subCategoryId']").val() == "182" && $("input[name='productType']").val() == "INNERLINE"){
					firstObj.find("td").eq(2).html("成人:￥"+selectHotleObj.find("td").eq(1).find(".adultNumText").attr("auditPrice")
							+" X "+selectHotleObj.find("td").eq(1).find(".adultNumText").val()
							+"<br>儿童:￥"+selectHotleObj.find("td").eq(1).find(".childNumText").attr("childPrice")
							+" X "+selectHotleObj.find("td").eq(1).find(".childNumText").val()
							+"<br>"+"总价:￥"+$td.attr("totalAmountYuan")+"元");
				}else{
					firstObj.find("td").eq(2).html("总价:￥"+$td.attr("totalAmountYuan")+"元");
				}
				firstObj.attr("totalAmount",$td.attr("totalAmount"));
				firstObj.attr("groupId",groupId);
				firstObj.attr("detailId",detailId);
				firstObj.attr("productBranchId",selectProductBranchId);
				
				$this.attr("packageGroupId",groupId);
				$this.attr("packageProductId",packageProductId);
				$this.attr("selectedSuppGoodsId",selectedSuppGoodsId);
				$this.attr("packageProductBranchId",selectProductBranchId);
				
				//重新设置更多按钮对象属性
				$this.attr("toPackageGroupId",groupId);
				$this.attr("toPackageProductId",packageProductId);
				$this.attr("toPackageProductBranchId",selectProductBranchId);
				$this.attr("toPrice",$td.attr("totalAmount")/2);
				$this.attr('currentShow','TOBACK')
				
				$this.attr("backPackageGroupId",groupId);
				$this.attr("backPackageProductId",packageProductId);
				$this.attr("backPackageProductBranchId",selectProductBranchId);
				$this.attr("backPrice",$td.attr("totalAmount")/2);
				
				$this.attr("adultNum",adult);
				$this.attr("childNum",child);	
				
				if("21" == $(this).attr("categoryId")){
					$(".back_startTime").html($(this).attr("back_startTime"));
					$(".back_startAirportString").html($(this).attr("back_startAirportString"));
					$(".back_airlineString").html($(this).attr("back_airlineString"));
					$(".back_arriveTime").html($(this).attr("back_arriveTime"));
					$(".back_arriveAirportString").html($(this).attr("back_arriveAirportString"));
					$(".back_flightNo").html($(this).attr("back_flightNo"));
					$(".back_flightNo").closest('div').removeClass('transport_hide');
					
					$(".to_startTime").html($(this).attr("to_startTime"));
					$(".to_startAirportString").html($(this).attr("to_startAirportString"));
					$(".to_airlineString").html($(this).attr("to_airlineString"));
					$(".to_arriveTime").html($(this).attr("to_arriveTime"));
					$(".to_arriveAirportString").html($(this).attr("to_arriveAirportString"));
					$(".to_flightNo").html($(this).attr("to_flightNo"));
					$(".to_flightNo").closest('div').removeClass('transport_hide');
				}
				
				//判断是否是游玩人后置订单
				var $travellerDelayFlag = $("[name=travellerDelayFlag]");
				var oldTravellerDelayFlag = $travellerDelayFlag.val();
				if(oldTravellerDelayFlag != 'Y' ) {
					//$travellerDelayFlag.val('Y');
					$travellerDelayFlag.attr("data-toDJFlag", "N");
					$travellerDelayFlag.attr("data-backDJFlag", "N");
				}
				
				dialog.close();
				dialog=null;
				countTotalPrice();
		});
	}
	
	/**
	 * 为了提升用户体验，异步加载单程的返程数据
	 */
	function openChangeDivTransportBack($this,pageFlag){
		var url="/vst_order/ord/order/getBasicJpData1Back.do";
		var firstShowName		=getFirstTrName("linetransport");
		if(pageFlag=="apilinetransport"){
			$('.do-select-action_back').die();
			$(".do-select-action_back").live("click",function(){
				changeText($(this),$this,'BACK',firstShowName);
			});
		}
		var productId				=$("#productId").val();
		var outProductId			=$this.attr("outProductId");
		
		var groupId					=$this.attr("packageGroupId");
		var packageProductId		=$this.attr("packageProductId");
		var currentProductBranchId	=$this.attr("packageProductBranchId");
		var selectedSuppGoodsId		=$this.attr("selectedSuppGoodsId");
		
		var toGroupId					=$this.attr("toPackageGroupId");
		var toPackageProductId			=$this.attr("toPackageProductId");
		var toCurrentProductBranchId	=$this.attr("toPackageProductBranchId");
		var toSelectedSuppGoodsId		=$this.attr("toSelectedSuppGoodsId");
		var toPrice 					=$this.attr("toPrice");//交通去程价格
		
		var backGroupId					=$this.attr("backPackageGroupId");
		var backPackageProductId		=$this.attr("backPackageProductId");
		var backCurrentProductBranchId	=$this.attr("backPackageProductBranchId");
		var backSelectedSuppGoodsId		=$this.attr("backSelectedSuppGoodsId");
		var backPrice 					=$this.attr("backPrice");//交通返程价格
		
		var adultQuantity		=$this.attr("adultNum");
		var childQuantity		=$this.attr("childNum"); 
		var packageProductBranchId=currentProductBranchId;
		var quantity			=getCopies();//$this.attr("quantity");
		var $firstShowTr 		=$(firstShowName+groupId);
		var totalAmount 		=$firstShowTr.attr("totalAmount");
		var haveChangeButton	=$this.attr("haveChangeButton");
		var oldPrice			=$this.attr("oldPrice");
		var specDate 			=$("#visitTime").val();
		var selectedDate 		=$firstShowTr.find("input[name$='.visitTime']").val();
		
		var goTime = '';
		var flightNo = '';
		if($this.attr("transportType") != "TOBACK"){
			goTime = $(".back_gotime").html();
			flightNo = $(".back_flightNo").html();
		}else{
			goTime = $(".back_startTime").html();
			flightNo = $(".back_flightNo").html();
		}
		
		if(!specDate) {
			specDate = $firstShowTr.find("input[name$='.visitTime']").val();
		}
		if(groupId==backGroupId){
			backGroupId = "";
		}
		
		$("input[type=hidden][name=userId]").val(book_user_id);	
		url=url+"?adultQuantity="	+adultQuantity
		+"&childQuantity="			+childQuantity
		+"&lvmamaProductId="		+productId
		+"&backPackageGroupId="		+backGroupId
		+"&backPackageProductId="	+backPackageProductId
		+"&backPackageProductBranchId="+backCurrentProductBranchId
		+"&backSelectedSuppGoodsId="+backSelectedSuppGoodsId
		+"&backPrice="				+backPrice
		+"&quantity="				+quantity
		+"&haveChangeButton="		+haveChangeButton
		+"&oldPrice="				+oldPrice
		+"&changeType=traffic"
		+"&selectDate="				+specDate
		+"&transportType="			+$this.attr("transportType")
		+"&selectedFlag="			+goTime + flightNo + backSelectedSuppGoodsId
		var jStartDistrict = $("#queryStartDistrictId");
		if(jStartDistrict && jStartDistrict.length>0){
			url += "&startDistrictId="+jStartDistrict.attr("data-cityid");
		}
		$.ajax({
            url:url,
            type:"get",
            contentType:'application/json;charset=utf-8', //设置请求头信息
            success: function (data) {
                $("#hchbdiv").html(data);
            }
        });
	}
/**end openChangeDivTransport add by zm */	

	//自主打包的可更换酒店
	$('.moreCategoryChangeHotel').click(function(){
		var url="/vst_order/ord/order/queryPackageMoreProduct.do";
		var productId=$("#productId").val();
		var packageProductId=$(this).attr("packageProductId");
		var groupId=$(this).attr("packageGroupId");
		var currentProductBranchId=$(this).attr("packageProductBranchId");
		var specDate=$(this).attr("changeDate");
		var adultQuantity=$(this).attr("adultNum");
		var childQuantity=$(this).attr("childNum"); 
		var pageFlag="changeHotel";
		
		var totalAmount = $("#firstChangeHotelShow"+groupId+"").attr("totalAmount");
		$("input[type=hidden][name=userId]").val(book_user_id);		
		url=url+"?specDate="+specDate+"&productId="+productId+"&packageProductId="+packageProductId+"&groupId="+groupId+"&currentProductBranchId="+currentProductBranchId+"&adultQuantity="+adultQuantity+"&childQuantity="+childQuantity+"&pageFlag="+pageFlag+"&productItemIdIndex="+productItemIdIndex+"&totalAmount="+totalAmount;
		var $this=$(this);
		//查询
		var dialog = new xDialog(url,null,
						      {title:"更多可换酒店",width:800});
		
		$("a[name=xztcBtn]").bind("click",function(){
			//选择按钮控件对象
			var selectProductBranchId=$(this).attr("productBranchId");	
			var groupId=$(this).attr("groupId");
			var packageProductId=$(this).attr("packageProductId");	
			var adult=$(this).attr("adult");
			var child=$(this).attr("child");				
			
			//被选择的商品信息控件对象
			var selectHotleObj=$("#"+selectProductBranchId+"_"+groupId);
			
			//显示商品的控件对象
			var name="firstChangeHotelShow"+groupId;	
			var firstHotelObj=$("#"+name);			
			
			//判断之前显示的商品是否和当前选择的商品是同一个
			var firstProductBranchId=firstHotelObj.attr("productBranchId");
			if(firstProductBranchId==selectProductBranchId){
				alert("当前商品规格已被选择");
				dialog.close();
				dialog=null;
				return;
			}
			
			//重新设置将要显示的商品对象属性
			firstHotelObj.find("td").eq(0).html(selectHotleObj.find("td").eq(0).html());
			firstHotelObj.find("td").eq(1).html(selectHotleObj.find("td").eq(1).html());
			firstHotelObj.find("td").eq(2).html(selectHotleObj.find("td").eq(2).html());
			var $td = selectHotleObj.find("td").eq(3);
			firstHotelObj.find("td").eq(3).html("总价:￥"+$td.attr("totalAmountYuan")+"元");
			firstHotelObj.attr("totalAmount",$td.attr("totalAmount"));
			firstHotelObj.attr("groupId",groupId);
			firstHotelObj.attr("productBranchId",selectProductBranchId);
			
			//重新设置更多按钮对象属性
			$this.attr("packageProductBranchId",selectProductBranchId);
			$this.attr("packageGroupId",groupId);
			$this.attr("packageProductId",packageProductId);
			$this.attr("adultNum",adult);
			$this.attr("childNum",child);				
			
			dialog.close();
			dialog=null;
			getTotalUnitPrice($(this));//计算单房差
			try{
	  			setTimeout(changeNextSelect(null,null),500);
			}catch(err){}
			countTotalPrice();
		});
	});
/************** end自主打包——线路组打包 则酒店套餐更多选择、当地游更多选择、跟团游更多选择、自由行更多选择以及跟团游和自由行下的可换酒店更多选择 **************/	
	
/****************************************** start自主打包——酒店组打包 酒店更多选择 ******************************************/	
	//自主打包更多酒店按钮事件
	$('.moreCategoryLineHotel').click(function(){
		openChangeDiv($(this),"linehotel");
	});
/****************************************** end自主打包——酒店组打包 酒店更多选择 ******************************************/	

/****************************************** start自主打包——门票组打包 酒店更多选择 ******************************************/		
	//自主打包门票更多，按钮，hiddenDiv，trObj
	$('.moreCategoryLineTicket').click(function(){
		openChangeDiv($(this), "lineticket");
	});	
/****************************************** end自主打包——门票组打包 酒店更多选择 ******************************************/

/****************************************** start自主打包——交通组打包 交通更多选择 ******************************************/
	//大交通
	$('.moreCategoryLineTransport').click(function(){
		openChangeDivTransport($(this), "linetransport");
	});
	//对接机票
	$('.apiChangeTransprot').click(function(){
		openChangeDivTransport($(this), "apilinetransport");
	});
/****************************************** end自主打包——交通组打包 交通更多选择 ******************************************/	

/***********************************************************************************************/	
//门票和当地游选择日期事件
	$(".lineSelectDate").bind("change",function(){
		try{
			setTimeout(changeNextSelect($(this),null),500);
		}catch(err){}
		lineSelectDateChange($(this));
	});
	
	$(".selectQunantityDropdownListTicket").bind("change",function(){
		var goodsId=$(this).attr("data-suppGoodsid");
		$(this).parent().parent().find("#"+goodsId+"TotalNum").val($(this).val());
		setTicketPersonNum();
		lineSelectDateChangeForSceneHotel($(this).prev(".lineSelectDate"));
	});
	
	function setTicketPersonNum(){
		if($(".selectQunantityDropdownListTicket")){
			var thisSelectQunan = 0;
			$(".selectQunantityDropdownListTicket").each(function(){
				var aldutAndChild = 0;
				$(this).parent().find(".selectQunantityDropdownListTicket").each(function(){
					aldutAndChild +=parseInt($(this).val());
				});
				if(aldutAndChild > thisSelectQunan){
					thisSelectQunan =  aldutAndChild;
				}
			});
			if($("#ticketPersonNum")){
				$("#ticketPersonNum").val(thisSelectQunan);
			}
		}
	}
	

    function lineSelectDateChange(currElement) {
        var subCategoryId = $("input[name=subCategoryId]").eq(0).val();
        if (subCategoryId != 181) {
            lineSelectDateChangeForOther(currElement);
        } else {
            lineSelectDateChangeForSceneHotel(currElement);
        }
    }

    /**
     * 景酒之外的其他产品使用
     *
     * @param currElement
     */
    function lineSelectDateChangeForOther(currElement) {
        var date=currElement.find("option:selected").text();
        var price=currElement.find("option:selected").val();
        currElement.parent().parent().find("td").eq(0).find(".itemLineSelectDate").val(date);
        if(currElement.parent().parent().find("td").eq(3).html()==null){
            currElement.parent().parent().find("td").eq(2).html("总价:￥"+price+"元");
        }else{
            //处理当地游的附加信息联动
            getTotalUnitPriceForLocal(currElement.parent().parent(), date);
            currElement.parent().parent().find("td").eq(3).html("总价:￥"+price+"元");
            var parentProductId=currElement.find("option:selected").attr("parentProductId");
            changeAdditionInfo(parentProductId,date);
        }
        var adultPriceMap = {};
        var childPriceMap = {};
        currElement.parent().parent().find("select[class='adultPriceMapSelect']").eq(0).find("option").each(function() {
            adultPriceMap[$(this).text()] = $(this).val();
        });
        currElement.parent().parent().find("select[class='childPriceMapSelect']").eq(0).find("option").each(function() {
            childPriceMap[$(this).text()] = $(this).val();
        });

        currElement.parent().prev().find("span[class='adultChildPriceSpan']").eq(0)
            .html("成人价:￥" + adultPriceMap[date] + "元/儿童价:￥"
                + childPriceMap[date] + "元");

        countTotalPrice();
    }

	/**
	 * 景酒主页面上的日期选择框change事件
	 * 
	 * @param currElement
	 */
	function lineSelectDateChangeForSceneHotel(currElement) {
        var optionSelected = currElement.find("option:selected");
        var currDate=optionSelected.text();
        var index = currElement.attr("index");

        //修改隐藏的游玩时间
        currElement.parent().parent().find("td").eq(0).find(".itemLineSelectDate").eq(index).val(currDate);
        
        var currPrice = 0;
        if(currElement.attr("data-other") == "true"){
        	var auditPrice = optionSelected.attr("auditPrice");
    		var goodsId=currElement.attr("data-suppGoodsid");
    		var fitQuantity =  currElement.parent().parent().find("#"+goodsId+"TotalNum").val();
    		currElement.attr("currPrice",fitQuantity*auditPrice);
    		currPrice = fitQuantity*auditPrice;
        }else{
        	currPrice = parseInt(optionSelected.val());
        }
        //获取同一规格下所有商品的价格总和
        currElement.siblings(".lineSelectDate").each(function(){
        	if($(this).attr("data-other") == "true"){
        		var option = $(this).find("option:selected");
        		var auditPrice = option.attr("auditPrice");
        		var goodsId=$(this).attr("data-suppGoodsid");
        		var fitQuantity =  $(this).parent().parent().find("#"+goodsId+"TotalNum").val();
        		$(this).attr("currPrice",fitQuantity*auditPrice);
        		currPrice += fitQuantity*auditPrice;
        	}else{
        		var option = $(this).find("option:selected");
        		currPrice += parseInt(option.val());
        	}
        });

		if(currElement.parent().parent().find("td").eq(3).html()==null){
			currElement.parent().parent().find("td").eq(2).html("总价:￥"+currPrice+"元");
		}else{
			//处理当地游的附加信息联动
			getTotalUnitPriceForLocal(currElement.parent().parent(), currDate);
			currElement.parent().parent().find("td").eq(3).html("总价:￥"+currPrice+"元");
			var parentProductId=optionSelected.attr("parentProductId");
			changeAdditionInfo(parentProductId,currDate);
		}

		//下面这一段代码只在部分情况下才会起作用，搜索了一下代码，只有当地游才有相关的数据 20170216
		var adultPriceMap = {};
		var childPriceMap = {};
		currElement.parent().parent().find("select[class='adultPriceMapSelect']").eq(0).find("option").each(function() {
			adultPriceMap[$(this).text()] = $(this).val();
		});
		currElement.parent().parent().find("select[class='childPriceMapSelect']").eq(0).find("option").each(function() {
			childPriceMap[$(this).text()] = $(this).val();
		});
		currElement.parent().prev().find("span[class='adultChildPriceSpan']").eq(0)
			.html("成人价:￥" + adultPriceMap[currDate] + "元/儿童价:￥"
						+ childPriceMap[currDate] + "元");


		countTotalPrice();
	}

//附加信息价格和日期联动变化事件
function changeAdditionInfo(parentProductId,date){
	$("."+parentProductId).each(function(){
		var price=$(this).find("tr").find("td").eq(1).attr(date);
		var copies=$(this).find("tr").find("td").eq(2).find("input").val();
		$(this).find("tr").find("td").eq(0).find("."+parentProductId+"visitTime").val(date);
		$(this).find("tr").find("td").eq(1).html("单价:￥"+price+"元");
		$(this).find("tr").find("td").eq(3).html("总价:￥"+(price*copies)+"元");
	});
}

$(".showTicket").live({"mouseenter":function(){
	var $this = $(this);
    var str="";
    var $goodsId = $this.parents("tr").find("td:eq(0)").find("input[name$='.goodsId']");
    var subCategoryId = $("input[name=subCategoryId]").eq(0).val();
    if (subCategoryId != 181) {
        $goodsId.each(function () {
            var id = $(this).val();
            if (str.length > 0) {
                str += ",";
            }
            str += id;
        });
    } else {
        //景酒直接获取商品ID
        str = $this.next("input[name$='.goodsId']").eq(0).val();
    }
	$.post("/vst_order/ord/order/queryLineTicketCertValid.do",{"suppGoodsIds":str},function(data){
		if(data.success){
			if(!document.getElementById("showTicketCertValidDiv")){
				$("body").append($("<div id='showTicketCertValidDiv' style='position: absolute;z-index:1000;background-color:#fff;border:1px solid #333;margin:10px 0;padding:10px;'></div>"));
			}
			
			var $div = $("#showTicketCertValidDiv");
			var body="门票凭证有效期：<br/>";
			for(var key in data.attributes){
				if(key.indexOf("suppGoods")>-1){
					body+=key.replace("suppGoods","商品ID")+" "+data.attributes[key]+"<br/>";
				}
			}
			var offset = $this.offset();
			$div.html(body).css({"top":offset.top,"left":offset.left}).show();
		}
	},"JSON");
},mouseout:function(){
	$("#showTicketCertValidDiv").hide();
}});

/***********************************************************************************************/


//计算所有商品价格
function countTotalPrice(){
	lineBackCheckStock();
	//显示规格详细描述
	var showDescript=true;
	var descriptObj=null;
	$('.pro_tit').live('click',function(){
		if(!showDescript){
			return;
		}
		var prodBranchId=$(this).attr("prodBranchId");
		descriptObj=$(this).parents('.tab_nav').find('.descript');
		$.get("/vst_order/ord/order/queryProdBranchDesc.do?prodBranchId="+prodBranchId,null,function(data){
			if(data!=null&&data!=""){
				descriptObj.toggle();	
				descriptObj.find('.textarea').val(data);
			}
		});
		
	});	
};
function lineBackCheckStock(){
	var hotelSubCategoryId = $("#hotelSubCategoryId").val();
	$("#promotionTb tbody").html("");
	$("#promotionTb").prev("div").css("display","none");
	$('#orderSubmitA').attr('disabled',"true");
	var machineWineTable = hotelSubCategoryValuation(hotelSubCategoryId);
	var $form = $("form[name='orderForm']");
	var formData = $form.serialize();
	//自由行+机酒+国内
	if (hotelSubCategoryId != undefined && hotelSubCategoryId == "182" 
			&& machineWineTable != null && machineWineTable != "" 
				&& $("input[name='productType']").val() == "INNERLINE") {
		formData = formData + "&" + machineWineTable;
	}
	$.post("/vst_order/ord/order/lineBackCheckStore.do", formData,function(data){
		if(data==null||!data.success){
			$("#orderSubmitA").attr('submitFlag','false');
			 //需要提示库存不足
			BACK.BOOK.CHECK.showAmountInfo({"success":false,"resultInfo":data});
		}else{
			$("#orderSubmitA").attr('submitFlag','true');
			lineBackCountPrice();
		}
	},"JSON");
}

function formatDateStr(x,y) { 
	var z = {M:x.getMonth()+1,d:x.getDate(),h:x.getHours(),m:x.getMinutes(),s:x.getSeconds()}; 
	y = y.replace(/(M+|d+|h+|m+|s+)/g,function(v) {return ((v.length>1?"0":"")+eval('z.'+v.slice(-1))).slice(-2)}); 
	return y.replace(/(y+)/g,function(v) {return x.getFullYear().toString().slice(-v.length)}); 
} 
//显示买赠信息
function showBuyPresentActivityInfo(data){
	cleanBuyPresentActivityInfo();
	if(data.success){
		var buyPersent=data.attributes.buyPersentActivity;
		if(buyPersent && buyPersent.isFulfil=="Y"){
			var order_zp =$(".mz_info");
			//处理买赠是否提示不满足信息
			order_zp.find(".mz_name").html(buyPersent.activityName);
			order_zp.find(".mz_description").html(buyPersent.activityDescription);
			//循环处理赠品
			var presents=buyPersent.presents;
			if(presents){
				$.each(presents, function(k,item){
					var tmpTr=$("<tr></tr>");
					var tmpTdName=$("<td></td>");
					var tmpTdCount=$("<td style='text-align:left'></td>");
					var tmpTdExp=$("<td></td>");
					
					var zpIcon=$("<i class='zpIcon'></i>");
					tmpTdName.append(zpIcon);
					tmpTdName.append(item.presentName);
					
					if(item.presentExpireStart && item.presentExpireEnd){
						var expStr="有效期:&emsp;"+formatDateStr(new Date(item.presentExpireStart),"yyyy.MM.dd")+"&emsp;-&emsp;"
								  +formatDateStr(new Date(item.presentExpireEnd),"yyyy.MM.dd");
						tmpTdExp.append(expStr);
					}
					
					tmpTdCount.append(item.sendCount);
					tmpTr.append(tmpTdName);
					tmpTr.append(tmpTdCount);
					tmpTr.append(tmpTdExp);
					order_zp.find(".zp_info").append(tmpTr);
				});
					order_zp.show();
				}
			}
		}
	}

function cleanBuyPresentActivityInfo(){
	var order_zp =$(".mz_info");
	order_zp.find(".zp_info").html("");
	order_zp.hide();
	
}

function lineBackCountPrice(){
	var hotelSubCategoryId = $("#hotelSubCategoryId").val();
	var machineWineTable = hotelSubCategoryValuation(hotelSubCategoryId);
	var $form = $("form[name='orderForm']");
	$form.find(".promotionMap").remove();
	var formData = $form.serialize();
	//只针对自由行 机酒 182 国内
	if (hotelSubCategoryId != undefined && hotelSubCategoryId == "182" 
			&& machineWineTable != null && machineWineTable != ""
				&& $("input[name='productType']").val() == "INNERLINE") {
		formData = formData + "&" + machineWineTable;
	}
	
	var data = null ;
	$.post("/vst_order/ord/order/lineBackPriceInfo.do",formData,function(d){
		//console.log(data);
		data = d ; 
		BACK.BOOK.CHECK.showAmountInfo(data);
		showBuyPresentActivityInfo(data);
		$('#orderSubmitA').removeAttr('disabled');
	},"JSON");
}

/**
 *快递费用计算总价 
 */
function countLineExpressPrice(){
	// 查询快递商品
	$("#expressItemDiv").html("");
	$.post("/vst_order/ord/order/findOrderExpressGoods.do",$("form[name='orderForm']").serialize(),function(data){
		if(data.success){
			var expressItemHtml="";
			$(data.attributes.expressSuppGoodsVOList).each(function(index,item){
				var quantity=1;
				if(item.minQuantity>0){
					quantity=item.minQuantity;
				}
				expressItemHtml += "<input type='hidden' name='itemMap["
					+ item.suppGoodsId + "].goodsId' value='" +item.suppGoodsId
					+ "' autocomplete='off'/>";
				expressItemHtml += "<input type='hidden' name='itemMap["
					+ item.suppGoodsId + "].visitTime' value='" +data.attributes.visitTime
					+ "' autocomplete='off'/>";
				expressItemHtml += "<input type='hidden' name='itemMap["
					+ item.suppGoodsId
					+ "].quantity' value='"+quantity+"' goodsId="+item.suppGoodsId+" adult='"+item.adult+"' child='"+item.child+"' maxQuantity='"+item.maxQuantity+"' minQuantity='"+item.minQuantity+"' mainItem='false' />";
			});
			$("#expressItemDiv").html(expressItemHtml);
			
		}
		countTotalPrice();
		
	},"JSON");
};

/**
 * 保险选择重新计算总价
 */
$("select.hotel_sum").change(function(){
	countTotalPrice();
});	
/**
 * 保险下拉框份数生成
 */
function insuranceMaxNum(){
	var adultNum=$("input[type=hidden][name=adultNumValue]").val();
	var childNum=$("input[type=hidden][name=childNumValue]").val();
	var max=parseInt(adultNum)+parseInt(childNum);
	var str="";
	for ( var i = 0; i <= parseInt(max); i++) {
		str+="<option value='"+i+"'>"+i+"</option>";
	}
	$("#insuranceDiv").find("select.hotel_sum").html(str);
}

//查看收客人数
function showGroupRate(startDate,monthResult){
	
	var productId=$("#productId").val();
	var date= new Date(startDate);
	var year = date.getFullYear();
	var month = date.getMonth();
	var startDateStr=formatDateStr(date,"yyyy-MM-dd");
	new xDialog("/vst_order/ord/order/route/queryGroupRate.do",
			{"productId":productId,"currentYear":year,"currentMonth":month,"startDate":startDateStr,"monthResult":monthResult},
			{title:"本线路成团情况",width:700,height:700});
}


//重要通知
function getProductNoticeByCondition(){
	var productId=$("#productId").val();
	var startTime=$("#visitTime").val();
	var endTime=$("#visitTime").val();
	$.ajax({
		type: "POST",
		url: "/vst_back/prod/productNotice/getProductNoticeByCondition.do",
		data:{"productId":productId,"startTime":startTime,"endTime":endTime},
		success: function (data) {
			if(data.success){
				var productNoticeList = data.attributes.productNoticeList;
				if(data.attributes.productNoticeList==""||data.attributes.productNoticeList==null||data.attributes.productNoticeList=="null"){
					$.dialog({
		                width: 300,
		                title: "重要通知",
		                content: "<div>无重要通知</div>"
		            });
				}else{
					var noticeHtml="<div>";
					$.each(productNoticeList, function(k, productNotice){
						noticeHtml+=productNotice.fmtStartTime+"至 "+productNotice.fmtEndTime+"期间："+productNotice.content+"<br/>";
					});
					noticeHtml+="</div>";
					$.dialog({
		                width: 300,
		                title: "重要通知",
		                content: noticeHtml
		            });
				}
			}else{
				$.dialog({
	                width: 300,
	                title: "重要通知",
	                content: "<div>无重要通知</div>"
	            });
			}
		}
	});
}

$('#orderSubmitA').bind('click',function(){
    if(!Express.check()){
        alert("请正确输入快递信息");
        return;
    }
    /********************************意向单标记检查*********************************************/
    var flag = $("#intentionOrderFlag").val();
    if(flag=='NONE'){
        //alert(flag);
        $("#orderSubmitA").attr('submitFlag','false');
        $("#iOrderFlag").html("\<b style='color:red;font-size: 12px;font-weight: 300'>请确认订单是否从意向单产生。</b>");
    }
    /************************************意向单标记检查*****************************************/
    if($('#orderSubmitA').attr('submitFlag')=="false"){
        return;
    }
    var adultNum=$("input[type=hidden][name=adultNumValue]").val();
    var childNum=$("input[type=hidden][name=childNumValue]").val();
    var personCount=parseInt(adultNum)+parseInt(childNum);

    $("#insuranceDiv").find("select.hotel_sum").each(function(){
        var integerReg = "^[1-9][0-9]*$";
        var num=$(this).val();
        //对不同保险只检查该保险选购数量不超过总人数
        if(parseInt(num) > parseInt(personCount)){
            $.alert("购买的保险份数不能超过总人数.");
            return;
        }
    });
    //检查线路期票有效期，确定后提交订单
    submitCheckAperTicketChain();
});

/**
 * 下单检查链
 * 检查线路期票有效期及取票时间
 */
function submitCheckAperTicketChain() {
    var msg = "";
    $.each($("div[name=aperTicketTimeValidMsg]"), function (index, element) {
        if ($(element).html() != null && $(element).html().length > 0) {
            msg += $(element).html();
            return false; //跳出$.each，只需要一个提示
        }
    });

    if (msg.length > 0) {
        config = {
            okValue: "继续下单",
            content: msg
        };

        pandora.dialog(config, function () {
                //继续下单
                submitOrder();
            },
            function () {
            }
        );
    } else {
		submitOrder();
	}
}

/**
 * 提交订单
 */
function submitOrder () {
    //下单打点统计
    cmCreateElementTag("保存订单","国内后台下单页");

    //提交订单
    $("input[type=hidden][name=userId]").val(book_user_id);
    //生成订单开始
    orderInfoDialog = new xDialog("/vst_order/ord/order/beforeCreateOrder.do",
        $("form[name='orderForm']").serialize(),
        {title:"订单信息填写",width:800,dialogAutoStop:true});
}

/** ************************************** start关联销售门票添加 ************************************** **/
$(".selectDateDropdownList").bind("change",function(){
	var date = $(this).find("option:selected").text();
	
	
	var singlePrice = $(this).find("option:selected").val();
	$(this).parent().parent().find("td").eq(0).find(".selectDateInput").eq(0).val(date);
	$(this).parent().parent().find("td").eq(1).html("单价:￥"+ singlePrice);
	var copies = $(this).parent().next().find("select").eq(0).find("option:selected").val();
	$(this).parent().parent().find("td").eq(4).html("总价:￥"+(changeTwoDecimal_f(singlePrice * copies)));
	try{
		setTimeout(changeNextSelect($(this),null),500);
	}catch(err){}
	countTotalPrice();
});

//显示隐藏规格描述
$(".showBranchDesc").bind("click", function() {
	$(this).parents('.tab_nav').find('.descript').toggle();
});

$(".selectQunantityDropdownList").bind("change",function(){
	
	var copies =$(this).find("option:selected").val();
	var singlePrice = $(this).parent().prev().find("select").eq(0).find("option:selected").val();
	$(this).parent().parent().find("td").eq(4).html("总价:￥"+(changeTwoDecimal_f(singlePrice * copies)));
	
	
	// 原产品不包含快递信息
	var $expressInfoDiv = $("#expressInfoDiv");
	if ( $($expressInfoDiv).length > 0 && Number( $($expressInfoDiv).attr("hasExpress") ) == 0 ){
		
		var allCopies = 0;
		var selectQunantityDropdownListArr = $(".selectQunantityDropdownList");
		for ( var index = 0; index < selectQunantityDropdownListArr.length; index++) {
			var item = selectQunantityDropdownListArr[index];
			
			if ("EXPRESSTYPE_DISPLAY" == $(item).attr("goodsType")) {
				allCopies  = allCopies + $(item).find("option:selected").val();
			}
			
		}
		if (Number(allCopies) > 0) {
			Express.showExpressageConent($expressInfoDiv, true);
		} else {
			Express.showExpressageConent($expressInfoDiv, false);
		}
		
	}
	
	countTotalPrice();
});

$(".displayMoreGoods").bind("click", function() {
	var moreGoodsTable = $(this).parent().prev();
	if(moreGoodsTable.css("display") == 'none') {
		moreGoodsTable.css("display", "block");
		$(this).html("<<收起");
	} else {
		moreGoodsTable.css("display", "none");
		$(this).html("更多>>");
	}
});

function changeTwoDecimal_f(x) {
	  var f_x = parseFloat(x);
	  if (isNaN(f_x)) {
	      alert('function:changeTwoDecimal->parameter error');
	      return false;
	  }
	  var f_x = Math.round(x * 100) / 100;
	  var s_x = f_x.toString();
	  var pos_decimal = s_x.indexOf('.');
	  if (pos_decimal < 0) {
	      pos_decimal = s_x.length;
	      s_x += '.';
	  }
	  while (s_x.length <= pos_decimal + 2) {
	      s_x += '0';
	  }

	  return s_x;
}



//显示机型信息
function showJXinfo(e) {
	if ($(this).attr("table_td3") == "" && $(this).attr("table_td4") == ""&& $(this).attr("table_td2") == "") {
		var planeType = $(this);
		var isbackline = planeType.hasClass("JXT");
		$.ajax({
			url : "/vst_order/ord/order/getAirplaneJXinfo.do",
			type : "GET",
			async : false,
			data : {planStyle:encodeURI(planeType.attr("table_td1"))},
			dataType : "json",
			success : function(info) {
				//table_td1   table_td2  table_td3  table_td4  table_td5
				if(info=="" || info == null ){
					//ajax，调用失败，返回空字符串
				}else{
					planeType.attr("table_td1", info[0].planStyle);
					planeType.attr("table_td2", info[0].styleName);
					planeType.attr("table_td3", info[0].planeStyle);
					planeType.attr("table_td4", info[0].leastName);
					planeType.attr("table_td5", info[0].mostSeats);
				
				}
				
			},
			error : function() {
				alert("error");
			}
		});
		
	}
	var thisL = $(this).offset().left - 20, thisT = $(this).offset().top + 20, tanBox = $('.plane_type_box'), tdHtml = '';
	tanBox.show().css({
		'left' : thisL,
		'top' : thisT
	}).css("z-index", 9999);
	for ( var i = 0; i < tanBox.find('tr:first').find('th').length; i++) {
		tdHtml += '<td>' + $(this).attr('table_td' + (i + 1)) + '</td>'
	}
	tanBox.find('tr').eq(1).html(tdHtml);
}
//隐藏机型信息
function hideJXinfo() {
	$('.plane_type_box').hide();
}

function openProduct(productId, categoryId, categoryName){
	window.open("http://super.lvmama.com/vst_back/prod/baseProduct/toUpdateProduct.do?productId="+productId+"&categoryId="+categoryId+"&categoryName="+categoryName);
}

/** ************************************** end关联销售门票添加 ************************************** **/
/** ************************************** 意向单来源必选 ************************************** **/
$("#intentionOrderFlag").bind("change", function() {
    var flag = $(this).val();
    if(flag=='Y'||flag=='N'){
        //alert(flag);
        $("#orderSubmitA").attr('submitFlag','true');
        $("#iOrderFlag").html("");

    }
});
/** ************************************** 意向单来源必选 ************************************** **/

//后台线路下单 自由行 机酒 酒店 选中参数拼接 国内
function hotelSubCategoryValuation(hotelSubCategoryId) {
	if (hotelSubCategoryId != undefined && hotelSubCategoryId == "182"
			&& $("input[name='productType']").val() == "INNERLINE") {
		var machineWineTable = "";
		$(".machineWineTable").each(function(i){
			var v = $(this).find("#selectedHotel").closest("tr"); //获取已选的信息
			var productIdData = $(v).find("td div").attr("productId-data");
			var productIndexData = $(v).find("td div").attr("productIndex-data");
			var goodsId = $(v).find("input[name='productMap["+ productIdData +"].itemList["+ productIndexData +"].goodsId']").val();
			var detailId = $(v).find("input[name='productMap["+ productIdData +"].itemList["+ productIndexData +"].detailId']").val();
			var routeRelation = $(v).find("input[name='productMap["+ productIdData +"].itemList["+ productIndexData +"].routeRelation']").val();
			var quantity = $(v).find("input[name='productMap["+ productIdData +"].itemList["+ productIndexData +"].quantity']").val();
			var visitTime = $(v).find("input[name='productMap["+ productIdData +"].itemList["+ productIndexData +"].visitTime']").val();
			var arrivalTime = $(v).find("input[name='productMap["+ productIdData +"].itemList["+ productIndexData +"].hotelAdditation.arrivalTime']").val();
			var leaveTime = $(v).find("input[name='productMap["+ productIdData +"].itemList["+ productIndexData +"].hotelAdditation.leaveTime']").val();
			var adultQuantity = $(v).find("input[name='productMap["+ productIdData +"].itemList["+ productIndexData +"].adultQuantity']").val();
			if (quantity == "" || quantity == null) {
				var selectValue = $(v).find(".hotelSelect").val();
				quantity = selectValue;
				adultQuantity = selectValue;
			}
				
			machineWineTable += "productMap%5B"+ productIdData +"%5D.itemList%5B"+ productIndexData +"%5D.goodsId=" + goodsId + 
				"&productMap%5B"+ productIdData +"%5D.itemList%5B"+ productIndexData +"%5D.detailId=" + detailId +
				"&productMap%5B"+ productIdData +"%5D.itemList%5B"+ productIndexData +"%5D.routeRelation=" + routeRelation +
				"&productMap%5B"+ productIdData +"%5D.itemList%5B"+ productIndexData +"%5D.quantity=" + quantity +
				"&productMap%5B"+ productIdData +"%5D.itemList%5B"+ productIndexData +"%5D.visitTime=" + visitTime +
				"&productMap%5B"+ productIdData +"%5D.itemList%5B"+ productIndexData +"%5D.hotelAdditation.arrivalTime=" + arrivalTime +
				"&productMap%5B"+ productIdData +"%5D.itemList%5B"+ productIndexData +"%5D.hotelAdditation.leaveTime=" + leaveTime +
				"&productMap%5B"+ productIdData +"%5D.itemList%5B"+ productIndexData +"%5D.adultQuantity=" + adultQuantity + "&";
			$(this).find("input").attr("disabled", "false");
		});
		return machineWineTable;
	}
	return null;
}