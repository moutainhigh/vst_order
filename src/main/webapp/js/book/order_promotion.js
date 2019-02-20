//促销信息
function showPromPromtion(data){
	
	$("#promotionTb tbody").html("");
	$("#promotionTb").prev("div").css("display","none");
	$("input[name=couponExclusion]").val(data.attributes.couponExclusion);
	var promList=data.attributes.promList;
	if(promList!=null&&promList!=""){
		var promStr="";
        if(data.attributes.couponExclusion==true){
            //与优惠券互斥
            if(data.attributes.choosePromotion==null || data.attributes.choosePromotion=="") {
                var tempCouponAmtTotal = 0;
                $("input[name='youhui_name']:checked").each(function () {
                    if ($(this).val() != "" && $(this).val() != null) {
                        tempCouponAmtTotal = tempCouponAmtTotal + parseInt($(this).val());
                    }
                });
                if (data.attributes.priceInfo.promotionAmount < tempCouponAmtTotal) {
                    data.attributes.choosePromotion = "coupon";
                } else {
                    data.attributes.choosePromotion = "promotion";
                }
            }
            if(data.attributes.choosePromotion == "coupon"){
                promStr+="<tr><td></td><td><input type=\"radio\" name=\"choosePromotion\" value=\"coupon\" data-tag='coupon'   class=\"choose-promotion\" style='display: none' >可享促销：</td></tr>";
			}else{
                promStr+="<tr><td></td><td><input type=\"radio\" name=\"choosePromotion\" value=\"promotion\" data-tag='promotion'   class=\"choose-promotion\" style='display: none' >可享促销：</td></tr>";
			}
            $("div.order_box").find("p.wxts").html("<span style='color:red'>该产品促销优惠和账户优惠不可共享。</span>");
        }else{
            promStr+="<tr><td></td><td><input type=\"radio\" name=\"choosePromotion\" value=\"promotion\" data-tag='promotion' class=\"choose-promotion\" style='display: none'>可享促销：</td></tr>";
        }
		
		$.each(promList, function(k, item){
			if(item.promitionType!="ORDERCHANNELFAVORABLE"){
			var branchs=item.branchs;
			var promType="";
			if(branchs.indexOf('MAN_TYPE')>-1){
				promType='成人';
			}
			if(branchs.indexOf('CHILD_TYPE')>-1){
				if(promType=='成人'){
					promType+="、";
				}
				promType+='儿童';
			}
			
			var promStr="<tr><td width='7%'></td><td width='65%'>";
			promStr+="<input type='hidden' class='promotionMap' name='promotionMap["+item.key+"]' value='"+item.promPromotionId+"'/><span class='tags101'>";
			if(item.promitionType=="eraly_order_type"){
				promStr+="早订早惠</span><span class='yh_price'>- <dfn>¥</dfn>"+item.discountAmount/100+"</span>";
				promStr+=promType;
				promStr+="提前"+item.ruleValue+"天预订，销售单价优惠";
			}else if(item.promitionType=="more_order_more_favorable"){
				promStr+="多订多惠</span><span class='yh_price'>- <dfn>¥</dfn>"+item.discountAmount/100+"</span>";
				promStr+=promType;
				if(item.ruleType=="EACH_FULL"){
					promStr+="每满"+item.ruleValue+"份，销售总价优惠";
				}if(item.ruleType=="FULL"){
					promStr+="满"+item.ruleValue+"份起，销售单价优惠";
				}if(item.ruleType=="REDUCE_PRICE"){
					promStr+="满"+item.ruleValue+"份后，每增加"+item.promResult.addEach+"份，销售总价优惠";
				}
			}else if(item.promitionType=="IMMEDIATELY_FAVORABLE"){
				promStr+="立减</span><span class='yh_price'>- <dfn>¥</dfn>"+item.discountAmount/100+"</span>";
				promStr+=promType;
				if(item.ruleType=="EACH_FULL"){
					promStr+="满"+item.ruleValue/100+"元，销售总价优惠";
				}if(item.ruleType=="REDUCE_PRICE"){
					promStr+="满"+item.ruleValue/100+"元后，每增加"+item.promResult.addEach/100+"元，销售总价优惠";
				}
			}else if(item.promitionType=="LINERfAVORABLE"){
				promStr+="油轮优惠</span><span class='yh_price'>- <dfn>¥</dfn>"+item.discountAmount/100+"</span>";
			}else if(item.promitionType=="ORDERCHANNELFAVORABLE"){
				promStr+="渠道优惠</span>";
				var dw = "";
				var ruleValue="";
				var addEch="";
				if(item.promResult.ruleCalcType=="order_AMOUNT"){
					dw="元";
					ruleValue=item.ruleValue/100;
					addEch =item.promResult.addEach/100;
				}
				if(item.promResult.ruleCalcType=="order_count"){
					dw="份";
					ruleValue=item.ruleValue;
					addEch =item.promResult.addEach;
				}
				
				if(item.ruleType=="FULL"){
					promStr+="满"+ruleValue+""+dw+"起，销售单价优惠";
				}
				if(item.ruleType=="EACH_FULL"){
					promStr+="满"+ruleValue+""+dw+"起，销售总价价优惠";
				}
				if(item.ruleType=="REDUCE_PRICE"){
					promStr+="满"+ruleValue+""+dw+"后，每增加"+addEch+""+dw+"，销售总价优惠";
				}
			}
			if(item.promitionType!="LINERfAVORABLE"){
			if(item.promResult.amountType=="AMOUNT_FIXED"){
					promStr+=item.promResult.fixedAmount/100+"元，";
				}
			if(item.promResult.amountType=="AMOUNT_PERCENT"){
				promStr+=item.promResult.rateAmount+"%，";
			}
			}
			promStr+="合计优惠"+item.discountAmount/100+"元";
				
			promStr+="</td><td width='5%'> </td><td width='5%'> </td><td width='5%'> </td><td></td></tr>";
			$("#promotionTb tbody").append(promStr);
		}
		});
		$("#promotionTb tbody").prepend(promStr);
        if(data.attributes.couponExclusion==true){
            $("input[name=couponExclusion]").val(true);
            $("input[name='choosePromotion']").show();
            if(data.attributes.choosePromotion=="promotion" || data.attributes.choosePromotion==null || data.attributes.choosePromotion==""){
                $("input[name='choosePromotion'][data-tag='promotion']").trigger("click").attr("checked","checked");
            }else{
                $("input[name=couponExclusion]").val(false);

                $("input[name='choosePromotion'][data-tag='coupon']").trigger("click");
                $("input[name='choosePromotion'][data-tag='coupon']").parent("a").trigger("click");
            }
        }else{
            $("input[name='choosePromotion']").hide();
            $("input[name='choosePromotion'][data-tag='coupon']").trigger("click");
        }
	}else{
        $("input[name='choosePromotion']").hide();
        $("input[name='choosePromotion'][data-tag='coupon']").trigger("click");

    }
}

//奖金抵扣
var maxBonus=0;
function bonusChange(){
	$("#target").val("bonus");
	if(ValidateBonus()){
		if (typeof lineBackCheckStock != 'undefined' && lineBackCheckStock instanceof Function) {
			lineBackCheckStock();
		 }else{
			 BACK.BOOK.CHECK.checkStock();
		 }
	}
}


function ValidateBonus(){
	$("#bonusInfoMsg").html('');
	$("#bonusInfoMsg").css("display","none");
	var youhui=$("input[type=radio][name=youhui]:checked").val();
	if(youhui!="bonus"){
		$("#bonusInfoMsg").html('<i class="tip-icon tip-icon-error"></i>请选择使用奖金返现');
		$("#bonusInfoMsg").css("display","block");
		return false;
	}
	var bonus = $("#bonus_number").val();
	var re = /^(\d+\.\d{1,2}|\d+)$/
	if(bonus==""){
		//$("#target").val("");
		$("#bonus_number").val(0.00);
		return true;
	}
	if(!re.test(bonus)){
		$("#bonusInfoMsg").html('<i class="tip-icon tip-icon-error"></i>输入奖金抵扣格式不正确');
		$("#bonusInfoMsg").css("display","block");
		$("#target").val("");
		return false;
	}
	if(parseFloat(bonus)>parseFloat(maxBonus)){
		$("#bonusInfoMsg").html('<i class="tip-icon tip-icon-error"></i>奖金抵扣额度不能超过最大抵扣额度');
		$("#bonusInfoMsg").css("display","block");
		$("#target").val("");
		return false;
	}
	return true;
}