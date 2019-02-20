$(function(){
	
});

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

function countExpressPrice(){
	$("#expressItemDiv").html("");
	$.post("/vst_order/ord/book/ajax/findOrderExpressGoods.do",$("form[name='orderForm']").serialize(),function(data){
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
		BACK.BOOK.CHECK.checkStock();
	},"JSON");
}
var BACK={
	BOOK:{
		CHECK:{
			 checkStock:function(){
				$("#promotionTb tbody").html("");
				$("#promotionTb").prev("div").css("display","none");
				var $form = $("form[name='orderForm']");
				$.post("/vst_order/ord/book/ajax/checkStock.do",$form.serialize(),function(data){
					if(data==null||!data.success){
						$("#orderSubmitA").attr('submitFlag','false');
						 //需要提示库存不足
						BACK.BOOK.CHECK.showAmountInfo({"success":false});
					}else{
						$("#orderSubmitA").attr('submitFlag','true');
						BACK.BOOK.CHECK.calcPriceInfo();
					}
				},"JSON");
			},
			calcPriceInfo:function(){
				var $form = $("form[name='orderForm']");
				var data = null;
				$.post("/vst_order/ord/book/ajax/priceInfo.do",$form.serialize(),function(d){
					data = d;
					BACK.BOOK.CHECK.showAmountInfo(data);
					showBuyPresentActivityInfo(data);
				},"JSON");
				//刷新优惠券
				if($('.freeCouponTbody').length>0||$('.fixedCouponTbody').length>0){					
					$('.freeCouponTbody').html('');
					$('.fixedCouponTbody').html('');		
					getLogionMessageInfo();
				}
				
				/*BACK.BOOK.CHECK.showAmountInfo(data);
				showBuyPresentActivityInfo(data);
				
				$.ajax({
					url : "/vst_order/ord/book/ajax/priceInfo.do",
					type:"POST",
					data:$form.serialize(),
					async:false,
					success:function(d){
						data = d;
					},
					error:function(){
						alert("服务器错误");
					}
				});*/
				
			},
			showAmountInfo:function(data){
				
				if(data.success){
					var priceInfo = data.attributes.priceInfo;
					showPromPromtion(data);
					//奖金抵扣
					youhuiType = $("#youhui:checked").val();
					if(youhuiType=="bonus"){
						$("#bonus_number").val(priceInfo.bonusToYuan);
					}
					maxBonus=priceInfo.maxBonusToYuan;
					//最高奖金抵扣额度
					$("#max_bonus_lable").html(priceInfo.maxBonusToYuan);
					//后台下单begin
					
					if(priceInfo && priceInfo.maxBonus){
						var maxbo = priceInfo.maxBonus;
//						if(maxbo==null || maxbo=="" || maxbo <=0){
//							maxbo = 9999;
//						}
						if(maxbo<0){
							$(".maxCanPayBouns").html((0).toFixed(2));
						}else{
							$(".maxCanPayBouns").html((maxbo/100).toFixed(2));
						}
						$(".maxpayBounsAmt").val(maxbo);
					}
					
					
					//后台下单end
					
					
					if(typeof(Express)!="undefined"){
						//处理快递信息
						Express.disposeExpressPriceResult(data);
					}
					
					//首先判断订单是否有房差
					var hasGap = false;
					var num = $(".lvmama-fangcha-price").length;
					var gap = "";
					var numGap = parseFloat("0.00");
					if(num > 0){//该产品存在房差
						hasGap = true;
						//取到总的房差值
						gap = $("#fangChaAllPrice").val();
						if(gap != null && gap != "" && !isNaN(gap)){
							//numGap = parseFloat(gap);
							numGap = parseFloat("0.00");
							//gap = " + 房差¥" + parseFloat(gap).toFixed(2);
							gap = "";
						}else{
							numGap = parseFloat("0.00");
							//gap = " + 房差¥" + "0.00";
							gap = "";
						}
					}
					
					//总价
					var totalPrice1 = priceInfo.oughtPayToYuan;
					if(totalPrice1 != null && totalPrice1 != "" && !isNaN(totalPrice1)){
						totalPrice1 = (parseFloat(totalPrice1) + numGap).toFixed(2);
					}else {
						totalPrice1 = numGap.toFixed(2);
					}
					//定义一个元素 用于总价处理控件
					var oughtPaySpan = "<span priceValue=\"" + totalPrice1 + "\" value=\"" + totalPrice1 + "\" id=\"showOughtPayPriceId\">" + totalPrice1 + "</span>";
					
					//begin coupon--
					var couponHtml = "<span class='couponLabelAmt' couponValue=\"" + priceInfo.couponToYuan + "\">"+priceInfo.couponToYuan+" </span>";
					//end coupon
					//订单价格
					$("#totalOrderPriceDiv p").html("<b style='font-size:14px;font-weight: bold;'>订单总价：</b> 产品费用¥"+priceInfo.goodsTotalPriceToYuan+"+"+"押金¥"+priceInfo.depositPriceToYuan+"+"+"快递¥"+priceInfo.expressPriceToYuan+" - 优惠¥"+couponHtml +"="+oughtPaySpan);
					$("#cancelStrategyTd").html(priceInfo.doBookPolicyStr);
					 $(".hotel_sum").each(function(){
						 var goodsId=$(this).attr("goodsId");
						 $("#"+goodsId+"goodPrice").html("商品总价：￥"+priceInfo.goodsTotalPriceToYuan);
						 $("#"+goodsId+"deposit").html("押金总价：￥"+priceInfo.depositPriceToYuan);
					});
					$(priceInfo.couponResutHandles).each(function(k,v){
							if(v.msg!=null){
								$("#orderSubmitA").attr('submitFlag','false');
								$("#couponInfoMsg").html(v.msg);
							}
					});
					if(parseInt(priceInfo.coupon)>parseInt(0)){
						$("#couponInfoMsg").html('<i class="tip-icon tip-icon-warning"></i>优惠金额：'+priceInfo.couponToYuan+'元');
					}
					calCouponAmt();
					
				}else{
					$(".hotel_sum").each(function(){
						 var goodsId=$(this).attr("goodsId");
						 $("#"+goodsId+"goodPrice").html("商品总价：￥--");
						 $("#"+goodsId+"deposit").html("押金总价：￥--");
						 //$("#"+goodsId+"singePrice").html("单价：￥--");
						 
					});
					//总价
					var totalPrice1 = "";
					//定义一个元素 用于总价处理控件
					var oughtPaySpan = "= <span value=\"" + totalPrice1 + "\" id=\"showOughtPayPriceId\">" + totalPrice1 + "</span>";
					/** end房差添加 */					
					
					$("#totalOrderPriceDiv p").html("<b style='font-size:14px;font-weight: bold;'>订单总价：</b> 产品费用¥--"+"押金¥--"+"+ 快递¥-- - 优惠¥-- <span>¥"+ oughtPaySpan);//+  快递¥--
					$("#cancelStrategyTd").html("");
					
					//去除使用的奖金
					
				}
			}
		}
	}
}

function goodsDetailMouseover(goodsId,obj){
	$(obj).attr("tip-content",$(obj).next("div").html());
}