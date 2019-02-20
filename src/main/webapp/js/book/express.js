var Express;
if (!Express) {
	Express = {};
};
Express.isQuery=false;
//信息验证
Express.check=function(){
	if(!Express.isShow()){
		return true;
	}
	if(Express.isQuery){
		return false;
	}
	var isOk=true;	
	var city2Code = $("#js_city2").find('option:selected').attr("value");
	if (city2Code == "选择市") {
		return false;
	}
	if($("#user_name").val()==""){
		return false;
	}
	if($("#user_address").val()==""){
		return false;
	}
	if($("#user_phone").val()==""){
		return false;
	}
	return isOk;
	
};
// 初始化省市联动select
Express.initCity = function(path, parentId, destCitySelect) {
	$.ajax({
		url : path + "/vst_order/ord/book/ajax/expressDic.do?parentId=" + parentId,
		type : 'get',
		dataType : 'json',
		success : function(data) {
			 if(data.success){
				 $.each(data.attributes.provienceList, function(i, item){
						$(
								"<option value='" + item.districtId + "@"
										+ item.districtName + "'>" + item.districtName
										+ "</option>").appendTo(destCitySelect);
					});

                 //一键下单反填
                 Express.loadExpressValue();
			 }
		}
	});
};

//处理快递价格结果信息
Express.disposeExpressPriceResult = function(data) {
	if(!Express.isShow()){
		return;
	}
	var priceInfo = data.attributes.priceInfo;
	var expressGoodsInfo = data.attributes.expressGoodIds;
	// 快递总价显示控制
	var priceInfoHtml = "免邮费";
	var expressItemHtml="";
	if (priceInfo.expressPriceToYuan != '0.00') {
		priceInfoHtml=priceInfo.expressPriceToYuan+"元";
	}
	$("#expressTotalPrice").html(priceInfoHtml);
	// 创建快递收费项目隐藏域
	/*if (expressGoodsInfo&&expressGoodsInfo!="") {
		var ids=expressGoodsInfo.split(",");		
		for(var i=0;i<ids.length;i++){
			expressItemHtml += "<input type='hidden' name='itemMap["
				+ ids[i] + "].goodsId' value='" + ids[i]
				+ "' autocomplete='off'/>";
			expressItemHtml += "<input type='hidden' name='itemMap["
				+ ids[i]
				+ "].quantity' value='1' goodsId="+ids[i]+" adult='1' child='0' maxQuantity='8' minQuantity='0' mainItem='true' />";
		}	
		if(expressItemHtml&&expressItemHtml!=""){
			$("#expressItemDiv").html(expressItemHtml);
		}	
	}*/
};

// 计算快递费用
Express.countPrice = function() {
	/*var city2Code = $("#js_city2").find('option:selected').attr("value");
	if (city2Code == "选择市") {
		return;
	}
	var orderId=$("#orderId").val();
	var city1Code = $("#js_city1").find('option:selected').attr("value");
	alert("111111111"+city1Code);
	$.post("/vst_order/ord/book/ajax/countExpressPrice.do",{orderId:orderId,provinceCode:city1Code,cityCode:city2Code},function(data){
		if(data.success){
			var totalPrice = data.attributes.totalPrice;
			// 快递总价显示控制
			var priceInfoHtml = "免邮费";
			var expressItemHtml="";
			if (totalPrice!=""&&totalPrice != '0.00') {
				priceInfoHtml=totalPrice+"元";
				$("expressPriceSpan").html(priceInfoHtml);
			}else{
				$("expressPriceSpan").html(0);
			}
			$("expressTotalPrice").html(priceInfoHtml);
			
			$.each(data.attributes.goodsIdList, function(k, v){
				expressItemHtml += "<input type='hidden' name='itemMap["
					+ ids[i] + "].goodsId' value='" + ids[i]
					+ "' autocomplete='off'/>";
				expressItemHtml += "<input type='hidden' name='itemMap["
					+ ids[i]
					+ "].quantity' value='1' goodsId="+ids[i]+" adult='1' child='0' maxQuantity='8' minQuantity='0' mainItem='true' />";
			});
			if(expressItemHtml&&expressItemHtml!=""){
				$("#expressItemDiv").html(expressItemHtml);
			}	
		}else{
			 $.alert(data.message);
		}
	},"JSON");*/
	
	var city2Code = $("#js_city2").find('option:selected').attr("value");
	if (city2Code == "选择市") {
		var	priceInfoHtml = "<i class='tip-icon tip-icon-error'></i>请选择行政区域";
		$("span.error_text").html(priceInfoHtml);
	}
	if(typeof(current_product)!='undefined'&&current_product=='line'){
		countLineExpressPrice();
	}else{
		countExpressPrice();
	}
};
//省市联动事件定义
Express.initCitySelectEvent = function(path) {
	// 省份增加事件
	$("#js_city1").change(function() {
		var city1Code = $("#js_city1").find('option:selected').attr("value");
		$("#js_city2").empty();
		$("<option>选择市</option>").appendTo("#js_city2");
		Express.initCity(path, city1Code, $("#js_city2"));
	});
	// 城市增加事件
	$("#js_city2").change(function() {
		// 这里需要得到所有的快递元素列表
		Express.isQuery=true;
		Express.countPrice();
		Express.isQuery=false;
	});
};
//快递相关内容初始化
Express.init = function(path) {
	Express.isQuery=false;
	if(!Express.isShow()){
		return;
	}
	$("#js_city1").empty();
	$("<option>选择省</option>").appendTo("#js_city1");
	$("#js_city2").empty();
	$("<option>选择市</option>").appendTo("#js_city2");
	Express.initCity(path, 8, $("#js_city1"));
	Express.initCitySelectEvent(path);
};
//快递页面是否被显示
Express.isShow=function(){
	if(!$("#js_city1")){
		return false;
	}
	if(!$("#js_city1").find('option:selected').attr("value")){
		return false;
	}
	return true;
};

Express.showExpressageInfo=function(){
	var showFlag=showExpressCheck();
	if(showFlag){
		var htmlStr='<table width="100%">';
			htmlStr+='<tbody>';
			htmlStr+='<tr>';
				htmlStr+='<td style="font-size:16px;">';
				htmlStr+='<b>快递信息</b>：';
				htmlStr+='<div id="expressItemDiv"></div>';
				htmlStr+='<td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
				htmlStr+='<td class="e_label">收件人：</td>';
				htmlStr+='<td>';
				htmlStr+='<input type="text" placeholder="姓名" name="expressage.recipients" name_type="fullName" class="input" maxlength="25" id="user_name" required=true>';
				htmlStr+='<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>收件人不正确</span>';
				htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
			htmlStr+='<td class="e_label">联系电话：</td>';
			htmlStr+='<td>';
			htmlStr+='<input type="text" placeholder="手机号码" name="expressage.contactNumber" name_type="mobile" maxlength="11" id="user_phone" class="input" required=true>';
			htmlStr+='<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>联系电话不正确</span>';
			htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
			htmlStr+='<td class="e_label">邮寄地址：</td>';
			htmlStr+='<td>';
			htmlStr+='<select name="expressage.province" id="js_city1" class="select"><option>选择省</option></select>';
			htmlStr+='<select name="expressage.city" id="js_city2" class="select"><option>选择市</option></select>';
			htmlStr+='<br/>';
			htmlStr+='<input type="text" placeholder="详细地址" name="expressage.address" name_type="address" class="input w290" maxlength="100" id="user_address" required=true>';
			htmlStr+='<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>请选择行政区域</span>';
			htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
			htmlStr+='<td class="e_label">邮政编码：</td>';
			htmlStr+='<td>';
			htmlStr+='<input type="text" placeholder="邮政编码" name="expressage.postcode" name_type="youbian" id="user_code" class="input">';
			htmlStr+='<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮政编码错误</span>';
			htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
			htmlStr+='<td class="e_label">快递费用：</td>';
			htmlStr+='<td>';
			htmlStr+='<span id="expressTotalPrice">免邮费</span>';
			htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='</tbody>';
			htmlStr+='</table>';
		$("#expressageInfoDiv").html(htmlStr);
		Express.init("");
	}else{
		$("#expressageInfoDiv").html("");
	}
};

// 显示快递模块：id-元素ID、showFlag-显示/取消
Express.showExpressageConent=function(obj, showFlag){
	if(showFlag){
		var htmlStr='<table width="100%">';
			htmlStr+='<tbody>';
			htmlStr+='<tr>';
				htmlStr+='<td style="font-size:16px;">';
				htmlStr+='<b>快递信息</b>：';
				htmlStr+='<div id="expressItemDiv"></div>';
				htmlStr+='<td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
				htmlStr+='<td class="e_label">收件人：</td>';
				htmlStr+='<td>';
				htmlStr+='<input type="text" placeholder="姓名" name="expressage.recipients" name_type="fullName" class="input" maxlength="25" id="user_name" required=true>';
				htmlStr+='<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>收件人不正确</span>';
				htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
			htmlStr+='<td class="e_label">联系电话：</td>';
			htmlStr+='<td>';
			htmlStr+='<input type="text" placeholder="手机号码" name="expressage.contactNumber" name_type="mobile" maxlength="11" id="user_phone" class="input" required=true>';
			htmlStr+='<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>联系电话不正确</span>';
			htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
			htmlStr+='<td class="e_label">邮寄地址：</td>';
			htmlStr+='<td>';
			htmlStr+='<select name="expressage.province" id="js_city1" class="select"><option>选择省</option></select>';
			htmlStr+='<select name="expressage.city" id="js_city2" class="select"><option>选择市</option></select>';
			htmlStr+='<br/>';
			htmlStr+='<input type="text" placeholder="详细地址" name="expressage.address" name_type="address" class="input w290" maxlength="100" id="user_address" required=true>';
			htmlStr+='<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>请选择行政区域</span>';
			htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
			htmlStr+='<td class="e_label">邮政编码：</td>';
			htmlStr+='<td>';
			htmlStr+='<input type="text" placeholder="邮政编码" name="expressage.postcode" name_type="youbian" id="user_code" class="input">';
			htmlStr+='<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮政编码</span>';
			htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='<tr>';
			htmlStr+='<td class="e_label">快递费用：</td>';
			htmlStr+='<td>';
			htmlStr+='<span id="expressTotalPrice">免邮费</span>';
			htmlStr+='</td>';
			htmlStr+='</tr>';
			htmlStr+='</tbody>';
			htmlStr+='</table>';
		$(obj).html(htmlStr);
		Express.init("");
	}else{
		$(obj).html("");
	}
};

//缓存
Express.oneKeyOrderInfo = null;
Express.isFirstTime = true;

/**
 * 一键下单，自动加载旧订单上的快递信息，并设置
 */
Express.loadExpressValue = function () {
    var orderCreatingManner = $("#bookForm input[name=orderCreatingManner]").val();
    var originalOrderId = $("#bookForm input[name=originalOrderId]").val();
    if (orderCreatingManner == "2") {
        if (Express.oneKeyOrderInfo == null) {
            $.ajax({
                url: "/vst_order/ord/order/lineBackLoadExpress.do",
                async: true,
                data: {
                    originalOrderId: originalOrderId
                },
                type: "POST",
                success: function (data) {
                    if (data.success == true) {
                        //console.log(data);
                        Express.oneKeyOrderInfo = data;
                        Express.setExpressValue();
                    }
                }
            });
        } else {
            Express.setExpressValue();
        }
    }
};

/**
 * 设置快递数据
 */
Express.setExpressValue = function () {
    try {
        // if(window.console && console.log) {
        //     console.log(Express.oneKeyOrderInfo);
        // }

        if (Express.oneKeyOrderInfo != null) {
            var addressPerson = Express.oneKeyOrderInfo.attributes.addressPerson;
            var ordAddress = Express.oneKeyOrderInfo.attributes.ordAddress;
            var provinceSel = $("#js_city1").find('option:selected');
            if (Express.isFirstTime && provinceSel.attr("value") == "选择省") {
            	if(window){
    				window.console.log("一键下单反填快递信息");
    			}
                //收件人
                $("#user_name").val(addressPerson.fullName);
                //联系电话
                $("#user_phone").val(addressPerson.mobile);

                //邮寄地址
                $("#js_city1").find("option[value$='@" + ordAddress.province + "']").attr("selected", "true")
                $("#user_address").val(ordAddress.street);

                //邮政编码
                if(ordAddress.postalCode!=null && isNaN(ordAddress.postalCode)==false && Number(ordAddress.postalCode)!=0){
                	$("#user_code").val(ordAddress.postalCode);
                }
                $("#js_city1").change();
            } else if (Express.isFirstTime) {
                $("#js_city2").find("option[value$='@" + ordAddress.city + "']").attr("selected", "true");
                //Express.isFirstTime = false;
                $("#js_city2").change();
            }
        }
    } catch (e) {
        $.dialog("快递信息获取失败，请手工填写！");
    }
}