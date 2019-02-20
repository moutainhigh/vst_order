/**
 * 后台下单-产品商品结果列表处理
 * 
 * @author wenzhengtao
 * @date 2014-01-02
 */
 //显示房型面积、楼层、是否无烟，旁边的礼包暂时不做           
function branchNameMouseover(productId,suppGoodsId,params){
		var that = $(params);
		$.ajax({
			url : "/vst_order/ord/productQuery/getAreaFloorSmokelessRoom.do",
			type : "post",
			dataType:"JSON",
			async:false,
			data : {"productId":productId,"suppGoodsId":suppGoodsId},
			success : function(result) {
				var tipContent = "";
				var promContent =$("#room-"+suppGoodsId).attr("promcontent");
				if(result.code=='success'){
					var temp = '';
					if(result.attributes.addValue != ''&&result.attributes.addValue!=null&&result.attributes.addValue!="null"){
						temp = "（共"+result.attributes.addValue+"层）";
					}
					if(result.attributes.area!=""&&result.attributes.area!=null&&result.attributes.area!="null"){
						tipContent += "面积："+result.attributes.area+"平米</br>";
					}
					if(result.attributes.floor!=""&&result.attributes.floor!=null&&result.attributes.floor!="null"){
						tipContent += "楼层："+result.attributes.floor+"楼"+temp+"</br>";
					}
					if(result.attributes.smokeless_room!=""&&result.attributes.smokeless_room!=null&&result.attributes.smokeless_room!="null"){
						tipContent += "是否无烟："+result.attributes.smokeless_room+"</br>";
					}
					if(!!promContent){
                        tipContent+=promContent;
					}
					if(tipContent == ""){
						tipContent = "无数据";
					}
					that.attr("tip-content",tipContent);
				}else{
					tipContent = "无数据";
					that.attr("tip-content",tipContent);
				}
				
			}
		});
}

//显示床宽、是否可加床、加床价格
function bedTypeMouseover(productId,suppGoodsId,params){
		var that = $(params);
		$.ajax({
			url : "/vst_order/ord/productQuery/getAddBedFlag.do",
			type : "post",
			dataType:"JSON",
			async:false,
			data : {"productId":productId,"suppGoodsId":suppGoodsId},
			success : function(result) {
				var tipContent = "";
				if(result.code=='success'){
					if(result.attributes.bed_size!=""&&result.attributes.bed_size!=null&&result.attributes.bed_size!="null"){
						tipContent += "床型描述："+result.attributes.bed_size+"</br>";
					}
					if(result.attributes.add_bed_flag!=""&&result.attributes.add_bed_flag!=null&&result.attributes.add_bed_flag!="null"){
						tipContent += "加床："+result.attributes.add_bed_flag+"</br>";
					}
					if(result.attributes.extra_bed_price!=""&&result.attributes.extra_bed_price!=null&&result.attributes.extra_bed_price!="null"){
						tipContent += "加床价格："+result.attributes.extra_bed_price;
					}
					if(tipContent == ""){
						tipContent = "无数据";
					}
					that.attr("tip-content",tipContent);
				}else{
					tipContent = "无数据";
					that.attr("tip-content",tipContent);
				}
			}
		});
}

//显示宽带收费标准
function internetMouseover(productId,suppGoodsId,params){
		var that = $(params);
		$.ajax({
			url : "/vst_order/ord/productQuery/getInternet.do",
			type : "post",
			dataType:"JSON",
			async:false,
			data : {"productId":productId,"suppGoodsId":suppGoodsId},
			success : function(result) {
				var tipContent = "";
				if(result.code=='success'){
					if(result.attributes.addValue!=""&&result.attributes.addValue!=null&&result.attributes.addValue!="null"){
						tipContent += "宽带价格："+result.attributes.addValue;
					}else{
						tipContent = "无数据";
					}
					that.attr("tip-content",tipContent);
				}else{
					tipContent = "无数据";
					that.attr("tip-content",tipContent);
				}
			}
		});
}

//显示早餐价格
function breakfastMouseover(productId,params,breakfast){
		var that = $(params);
		$.ajax({
			url : "/vst_order/ord/productQuery/getBreakfast.do",
			type : "post",
			dataType:"JSON",
			async:false,
			data : {"productId":productId},
			success : function(result) {
				var tipContent = "";
				if(result.code=='success' && breakfast>0){
					if(result.attributes.addValue!=""&&result.attributes.addValue!=null&&result.attributes.addValue!="null"){
						tipContent += "早餐价格:"+result.attributes.addValue;
					}else{
						tipContent = "无数据";
					}
					that.attr("tip-content",tipContent);
				}else{
					tipContent = "无数据";
					that.attr("tip-content",tipContent);
				}
			}
		});
}

//重要通知事件
function showProductNotice(productId){
	var startTime = $("#startTime").val();
	var endTime = $("#endDate").val();
	
	if(productId==""){
		alert("产品ID不能为空!");
		return;
	}
	if(startTime==""){
		alert("入住日期不能为空!");
		return;
	}
	if(endTime==""){
		alert("离店日期不能为空!");
		return;
	}
	getProductNoticeByCondition(productId,startTime,endTime);
}

/**
 * 查找某一个房子在该区间内的时间价格表
 * 1,入住日期大于等于2天时才显示
 * 2,和第一天的价格和早餐不一样则区分显示
 * 
 * @param visitTime
 * @param leaveTime
 * @param goodsId
 * @param quantity
 * @param days
 */
function showTimePrice(goodsId,obj){
	var visitTime = $("#startDate").val();
	var leaveTime = $("#endDate").val();
	var days = $("#days").val();
	
	if(visitTime==""){
		alert("入住日期不能为空!");
		return;
	}
	
	if(leaveTime==""){
		alert("离店日期不能为空!");
		return;
	}
	
	if(days==""){
		alert("入住天数不能为空!");
		return;
	}
	
	if(goodsId==""){
		alert("商品ID不能为空!");
		return;
	}
	
	$.post("/vst_order/ord/order/productAndGoods/getTimePrice.do",
		{
			visitTime:visitTime,
			leaveTime:leaveTime,
			suppGoods:goodsId,
			quantity:1
		},
		function(data){
			//alert(data);
			if(days>=2){
				$(obj).attr("tip-content",data);
			}
		}
	);
}

