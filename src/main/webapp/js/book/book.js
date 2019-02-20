function calcPriceInfo(){
	//清空促销信息
	$("#promPromotionTb tbody").html("");
	var $form = $("form[name='holdBookInfoForm']");
	//$.post("/vst_order/ord/book/ajax/priceInfo.do",$form.serialize(),function(data){
	$.post("/vst_order/ord/book/ajax/hotelBackPriceInfo.do",$form.serialize(),function(data){
		showAmountInfo(data);
		showBuyPresentActivityInfo(data);
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

function showAmountInfo(data){
	
	var suppGoodsId=$("#suppGoods").val();
	if(data.success){
		var priceInfo = data.attributes.priceInfo;
		showPromPromtion(data);
		//设置最晚到店时间
		setArriveTime(priceInfo);
		
		var price=(priceInfo.price/100).toFixed(2);
		/*var price=0;
		$.each(priceInfo.itemPriceMap, function(k, v){
			if(k==suppGoodsId){
				price=v;
			}
		});*/
		//"="+priceInfo.mulPromotionAmountToYuan+"元"
		var oughtPay = (priceInfo.oughtPay/100).toFixed(2);
		var bounsAmt=0;
		var bonusHtml = "<span class='bonusHotelAmt' value=\"" +bounsAmt + "\">"+bounsAmt+" 元</span> ";
		var couponAmt=0;
		var couponHtml = "<span class='couponHotelAmt' value=\"" +couponAmt + "\">"+couponAmt+" 元</span>";
		var mulPromotionAmount ="<span class='mulPromotion' value=\"" + priceInfo.mulPromotionAmountToYuan + "\" id=\"showOughtPayPriceId\">"+priceInfo.mulPromotionAmountToYuan+" 元</span>";		
		$("#totalOrderPriceDiv").html("<b style='font-size:14px;font-weight: bold;'>订单总价：</b>房费"+price+"元"+
				"-促销活动"+priceInfo.promotionAmountToYuan+"元"+
				//"-奖金"+bonusHtml+"-优惠"+couponHtml+"="+mulPromotionAmount);
				"-奖金"+bonusHtml+"-优惠"+couponHtml+"="+oughtPay);
		$("#totalOrderPriceDiv").show();
		//退订政策
		refereshClaim(priceInfo.doBookPolicyStr);
		if(thisProCategoryId == 1){
			displayHotelBonus();
			calHotelCouponAmt();
		};
	}else{
		$("#totalOrderPriceDiv").html("<b style='font-size:14px;font-weight: bold;'>订单总价：</b>￥--");
		$("#totalOrderPriceDiv").show();
	}	
}

$(".js_check_tips").live('click',function(){
	var amount = parseFloat($(this).attr('value'));

	$(".couponHotelAmt").text(amount/100);
    $(".couponHotelAmt").html(amount/100);
    // var oughtPay = $(".oughtPayHotelAmt").attr("value")-amount;
    // $(".oughtPayHotelAmt").html(oughtPay).attr("value",oughtPay);
});

//设置到点时间
function setArriveTime(priceInfo){
	var result="";
	var hours=priceInfo.earliestArriveTime.split(":")[0];
	var minute=priceInfo.earliestArriveTime.split(":")[1];
	var lastArrivalTime=$("#arrivalTime").val();//最晚到店时间 
	
	for (var i = hours; i <= 23; i++) {
		if(i == hours){
			if(minute == 30){
				result+="<option value='"+i+":30'>"+i+":30</option>";
			}else{
				result+="<option value='"+i+":00'>"+i+":00</option>";
				result+="<option value='"+i+":30'>"+i+":30</option>";
			}
		}else{
			result+="<option value='"+i+":00'>"+i+":00</option>";
			result+="<option value='"+i+":30'>"+i+":30</option>";
		}
	}
	result+="<option value='23:59'>23:59</option>";
	$("#arrivalTime").html(result);
	var val = $("#arrivalTime").find("option[value='"+lastArrivalTime+"']").val();
	if(typeof(val)=="undefined"){
		$("#arrivalTime option:first").attr("selected","true");
		lastArrivalTime=$("#arrivalTime option:first").val();
	}
	$("#arrivalTime").val(lastArrivalTime);
}

//添加入住人
function addPerson(num){

    var str = "";
    if (foreignFlag != null && foreignFlag != "" && foreignFlag === "Y") {
        for (var i = 0; i < num; i++) {
            str += '<input type="text" name="travellers[' + i + '].lastName" placeholder="姓 例ZHANG" class="w8" style="margin-bottom: 5px;"/>&nbsp;';
            str += '<input type="text" name="travellers[' + i + '].firstName" placeholder="名 例SAN" class="w8" style="margin-bottom: 5px;"/>&nbsp;';
            str += '</br>';
        }
    } else {
        for (var i = 0; i < num; i++) {
            str += '<input type="text" name="travellers[' + i + '].fullName" class="w8" style="margin-bottom: 5px;"/>&nbsp;';
        }
    }
    str += '<div><i class="e_icon icon-warn"></i>请填写中文/英文姓名，英文姓名格式为：Lastname/Firstname。</div>';
    if ($("#Jtenantlist").length > 0) {
        $("#Jtenantlist").html(str);
    } else if ($("#Jtenantlist2").length > 0) {
        $("#Jtenantlist2").html(str);
    }
	$("input:checkbox").removeAttr("checked");
	//常用游客控制事件绑定
	travellersBindEvent();
}

function checkStock(){
	var $form = $("form[name='holdBookInfoForm']");
	//$.post("/vst_order/ord/book/ajax/checkStock.do",$form.serialize(),function(data){
	$.post("/vst_order/ord/book/ajax/checkHotelBackStock.do",$form.serialize(),function(data){
        if(!!data&&!!data.attributes.ctripHotelPromVoList){////如果是促销
            //设置促销描述信息
            //设置价格监听，如果是住几送几，把免费房价格修改为0
            updateOrderPageForCtripProm(data.attributes.ctripHotelPromVoList);
        }
        if(!!data&&!!data.attributes.ctripHotelPromVoList&&data.attributes.isMeetCtripProm===false){//如果是促销且不满足促销规则
            var errorMsg="";
            if (typeof getUnAccessReasonLine === "function"){
                errorMsg=getUnAccessReasonLine(data.attributes.ctripHotelPromVoList);//获取促销不满足原因#supplier_promotion.js
            }
            if (typeof getOfferDescriptionAndSet === "function"){
                getOfferDescriptionAndSet(data.attributes.ctripHotelPromVoList);
            }
            if(!!errorMsg){
                $.alert(errorMsg);
            }else {
                $.alert(data.message);
            }
            showAmountInfo({"success":false});
        }else {
            if(!data.success){
                //console.log("库存不足");
                if(data.message.indexOf('找不到艺龙返回的rooms记录')>-1){
                    $("#stockMsgDiv").html("<i class='e_icon icon-error'></i>"+'暂无库存，请选择其它产品！');
                }else{
                    $("#stockMsgDiv").html("<i class='e_icon icon-error'></i>"+data.message);
                }
                $("#stockMsgDiv").show();
                showAmountInfo({"success":false});
            }else{
                //条件改变去掉选中的促销
                $("#promPromotionTb tbody").find("input[type=checkbox][name=promotionIdList]:checked").change(function(){
                    $(this).removeAttr("checked");
                });
                calcPriceInfo();
                //促销信息
                //showPromPromtion();
                $("#stockMsgDiv").hide();
            }
		}
	},"JSON");
}
function holdBookInfo(){
	var $form = $("form[name='holdBookInfoForm']");
	$.post("/vst_order/ord/book/holdBookInfo.do",$form.serialize(),function(data){
		if(!data.success){
			 
		}else{
		 
		}
	},"JSON");
}
//退订政策
function refereshClaim(resultStr){
	if(null==resultStr||""==resultStr||"null"==resultStr){
		$("div.claim").closest("tr").hide();
		$("#guaranteeTb").closest("td").hide();
		$("#needGuarantee").val("");
	}else{
		var payTarget=$("#payTarget").val();
		if(payTarget=="PAY"){
			$("#guaranteeTb").closest("td").show();
			$("#needGuarantee").val("GUARANTEE");
		}
		$("div.claim").html(resultStr);
		$("div.claim").closest("tr").show();
	}
}

//绑定常用游客事件
function travellersBindEvent(){
//	$("#commonlyUsed").find("input[type=checkbox]").change(function(){
//		checkIocTxt();
//	});
	
	if($("#Jtenantlist").length>0){
		$("#commonlyUsed").find("input[type=checkbox]").change(function(){
			checkIocTxt('Jtenantlist');
		});
		$("#Jtenantlist").find("input[type=text]").change(function(){
			txtIocCheck();
	   });
	}else if($("#Jtenantlist2").length>0){
		$("#commonlyUsed").find("input[type=checkbox]").change(function(){
			checkIocTxt('Jtenantlist2');
		});
		$("#Jtenantlist2").find("input[type=text]").change(function(){
			txtIocCheckEng();
	   });
		
	}	
}

//常用游客checkbox控制客人姓名text
function checkIocTxt(customersid){
	//$("input[type=checkbox][name='commonlyUsed']:checked")
	$("#commonlyUsed").find("input[type=checkbox]:checked").each(function(){
	    var receiverId=$(this).val();
	    var fullName=$(this).attr("personName");
	    var checkedFlag=false;
	    $("#"+customersid).find("input[type=text]").each(function(){
	    	var travellersId=$(this).attr("travellersId");
	    		if(fullName==$(this).val()&&travellersId==receiverId){
	    				checkedFlag=true;
		    			return false;
		    	}
	    	});
	    if(!checkedFlag){
		    $("#"+customersid).find("input[type=text]").each(function(){
			    	if(this.value==''){
						checkedFlag=true;
						$(this).val(fullName);
						$(this).attr("travellersId",receiverId);
						return false;
					}
	    	});
	    }
	   	if(!checkedFlag){
	   		$(this).removeAttr("checked");
	   	}
	  });
	//$("input[type=checkbox][name='commonlyUsed']")
	$("#commonlyUsed").find("input[type=checkbox]").not("input:checked").each(function(){
		    var fullName=$(this).attr("personName");
		    var receiverId=$(this).val();
		    $("#"+customersid).find("input[type=text]").each(function(){
		    		var travellersId=$(this).attr("travellersId");
		    		if(fullName==$(this).val()&&travellersId==receiverId){
			    		//var name=$(this).attr("name").split('.');
			    		//var id=name[0]+".receiverId";
			    		//$("#Jtenantlist").find('input[type=hidden][name="'+id+'"]').val("");
		    			$(this).val("");
		    			$(this).attr("travellersId","");
		    			return false;
		    		}
		    	});
	  });
}

//客人姓名text控制常用游客checkbox
function txtIocCheck(){
	//$("input[type=checkbox][name='commonlyUsed']:checked")
	$("#commonlyUsed").find("input[type=checkbox]:checked").each(function(){
	    var fullName=$(this).attr("personName");
	    var receiverId=$(this).val();
	    var checkedFlag=false;
	    $("#Jtenantlist").find("input[type=text]").each(function(){
		    var travellersId=$(this).attr("travellersId");
	    		if(fullName==$(this).val()&&travellersId==receiverId){
	    			checkedFlag=true;
	    			return false;
		    	}
	    	});
	   	if(!checkedFlag){
	   		$(this).removeAttr("checked");
	   	}
  });
		$("#Jtenantlist").find("input[type=text]").each(function(){
		var checkedFlag=false;
		var fullName=$(this).val();
		 var travellersId=$(this).attr("travellersId");
		$("#commonlyUsed").find("input[type=checkbox]").each(function(){
				var receiverId=$(this).val();
		     	if(fullName==$(this).attr("personName")&&travellersId==receiverId){
	    			checkedFlag=true;
	    			$(this).attr("checked","checked");
	    			return false;
		    	}
			});
		
			if(!checkedFlag){
	   		 
			}
	});
}

//促销信息
function showPromPromtion(data){
	//var $form = $("form[name='holdBookInfoForm']");
	//$.post("/vst_order/ord/book/queryPromPromotion.do",$form.serialize(),function(data){
	$("#promPromotionTb tbody").html("");
		if(data.success){
			var promList=data.attributes.promList;
			//var price=data.attributes.price;
				if(promList!=null&&promList!=""){
					
					$.each(promList, function(k, v){
						var promStr="<dd style='padding-right:32px;'><input type='hidden' class='promotionMap' name='promotionMap["+v.promotionKey+"]' value='"+v.promotionId+"'>";
						promStr+="<span class='yhTag'><i class='tags101' tip-content=''>" + v.hotelOrderPromTitle+ "</i></span>";
						var promStr2="";
						promStr2+=v.hotelOrderPromScript;
						promStr+="<span class='ysText'>";
						promStr+=promStr2;
						promStr+="</span>";
						promStr+='<span style="float:right;">-￥' + (v.favorableAmount/100).toFixed(2)+"</span>";
						
						/*
						 var promStr="<dd><input type='hidden' class='promotionMap' name='promotionMap["+v.promotionKey+"]' value='"+v.promotionId+"'>";
						 promStr+="<span class='yhTag'><i class='tags101' tip-content=''>酒店优惠</i></span>";
	                    promStr2=v.startVisitTimeStr+"至"+v.endVisitTimeStr;
	                    var activityTemp = "";
	                    if (v.activityTimeDays != null && v.activityTimeDays.length > 0 && v.activityTimeDays.length < 7){
	                        $.each(v.activityTimeDays,function(x,y){
	                            if(y == "MONDAY"){
	                                activityTemp += "周一、";
	                            }else if(y == "TUESDAY"){
	                                activityTemp += "周二、";
	                            }else if(y == "WEDNESDAY"){
	                                activityTemp += "周三、";
	                            }else if(y == "THURSDAY"){
	                                activityTemp += "周四、";
	                            }else if(y == "FRIDAY"){
	                                activityTemp += "周五、";
	                            }else if(y == "SATURDAY"){
	                                activityTemp += "周六、";
	                            }else if(y == "SUNDAY"){
	                                activityTemp += "周日、";
	                            }
	                        });
	                        if(activityTemp != null && activityTemp != "" && activityTemp.charAt(activityTemp.length - 1) == "、"){
	                            activityTemp = activityTemp.substring(0,activityTemp.length - 1);
	                        }
	                    }

	                    var visitTemp = "";
	                    if (v.visitTimeDays != null && v.visitTimeDays.length > 0 && v.visitTimeDays.length < 7){
	                        $.each(v.visitTimeDays,function(x,y){
	                            if(y == "MONDAY"){
	                                visitTemp += "周一、";
	                            }else if(y == "TUESDAY"){
	                                visitTemp += "周二、";
	                            }else if(y == "WEDNESDAY"){
	                                visitTemp += "周三、";
	                            }else if(y == "THURSDAY"){
	                                visitTemp += "周四、";
	                            }else if(y == "FRIDAY"){
	                                visitTemp += "周五、";
	                            }else if(y == "SATURDAY"){
	                                visitTemp += "周六、";
	                            }else if(y == "SUNDAY"){
	                                visitTemp += "周日、";
	                            }
	                        });
	                        if(visitTemp != null && visitTemp != "" && visitTemp.charAt(visitTemp.length - 1) == "、"){
	                            visitTemp = visitTemp.substring(0,visitTemp.length - 1);
	                        }
	                    }
	                    if(v.timeType=="TIME_VISIT_DATE"){
	                        promStr2+= visitTemp == "" ? "入住，" : "中的" + visitTemp + "入住,";
	                    }else if(v.timeType=="TIME_AT_HOTEL"){
	                        promStr2+= visitTemp == "" ? "在店，" : "中的" + visitTemp + "在店,";
	                    }else if (v.timeType=="TIME_ACTIVITY"){
	                        var promStr2=v.startVisitTimeStr+"至"+v.endVisitTimeStr;
	                        promStr2+= activityTemp == "" ? "活动期间，" : "活动期间的" + activityTemp + ",";
	                    }
						// if(v.timeType=="TIME_VISIT_DATE"){
						// 	promStr2+="入住，";
						// }else if(v.timeType=="TIME_AT_HOTEL"){
						// 	promStr2+="在店，";
						// }else if (v.timeType=="TIME_ACTIVITY"){
	                     //    // var promStr2=v.beginTimeStr+"至"+v.endTimeStr;
	                     //    var promStr2=v.startVisitTimeStr+"至"+v.endVisitTimeStr;
	                     //    promStr2+="活动期间，";
						// }
						if(v.ruleType=="AHEAD_X_DAY"){
							promStr2+="提前"+v.ruleValue+"天预订，";
						}else if(v.ruleType=="CONTINUITY_X_DAY"){
							promStr2+="连住"+v.ruleValue+"晚，";
						}else if(v.ruleType=="PER_CONTINUITY_X_DAY"){
							promStr2+="每连住"+v.ruleValue+"晚，";
						}
						
						if(v.resultType=="REDUCE_PRICE"){ //降价类优惠方案
							if(v.resultSecondType=="PER_NIGHT"){ //每间晚优惠
								if(v.amountType=="AMOUNT_FIXED"){ //固定金额
									promStr2+="每间晚优惠"+v.ruleResultValueYuan+"元";
								}else if(v.amountType=="AMOUNT_PERCENT"){//百分比
									promStr2+="每间晚优惠"+v.ruleResultValue+"%";
								}
							}else if(v.resultSecondType=="LAST_X_NIGHT"){//最后X晚优惠
								if(v.amountType=="AMOUNT_FIXED"){ //固定金额
									promStr2+="最后"+v.hotelNights+"晚优惠"+v.ruleResultValueYuan+"元";
								}else if(v.amountType=="AMOUNT_PERCENT"){//百分比
									promStr2+="最后"+v.hotelNights+"晚优惠"+v.ruleResultValue+"%";
								}
							}else if(v.resultSecondType=="FROM_X_NIGHT"){ //第X晚及以后优惠
								if(v.amountType=="AMOUNT_FIXED"){ //固定金额
									promStr2+="第"+v.hotelNights+"晚及以后优惠"+v.ruleResultValueYuan+"元";
								}else if(v.amountType=="AMOUNT_PERCENT"){//百分比
									promStr2+="第"+v.hotelNights+"晚及以后优惠"+v.ruleResultValue+"%";
								}
							}
						}else if(v.resultType=="FREE_OFFER"){//赠品类优惠方案
							 
						}else if(v.resultType=="DISCOUNT_BUY_OTHER"){//优惠购买其他商品优惠方案
							 
						}
						
						promStr+="<span class='ysText'>";
						promStr+=promStr2;
						promStr+="</span>";
						*/
						
						//promStr+="</label></div>";
						$("#promPromotionTb tbody").append(promStr);
					});
					
					
				$("#promPromotionTb").closest("tr").show();
				 bindPromCkEvent();
			}else{
				$("#promPromotionTb").closest("tr").hide();
			}
		}else{
			$("#promPromotionTb").closest("tr").hide();
		}
	//},"JSON");
}

function bindPromCkEvent(){
	$("#promPromotionTb tbody").find("input[type=checkbox][name=promotionIdList]").change(function(){
		
		//选择促销后重新计算Price
		calcPriceInfo();
		
		var currentExclusive=$(this).attr("exclusive");
		 var currentId=$(this).val();
		 if(currentExclusive=="Y"&&$(this).attr("checked")=="checked"){
			 $("#promPromotionTb tbody").find("input[type=checkbox][name=promotionIdList]").each(function(){
			     	var itemId=$(this).val();
				 	if(currentId==itemId){
	    			 	$(this).removeAttr("disabled");
			    	}else{
			    		 $(this).attr("disabled","disabled");
			    		 $(this).removeAttr("checked");
			    	}
				}); 
		 }
		 var ckdLength=$("#promPromotionTb tbody").find("input[type=checkbox][name=promotionIdList]:checked").length;
		 if(ckdLength==0){
			 $("#promPromotionTb tbody").find("input[type=checkbox][name=promotionIdList]").each(function(){
				 	$(this).removeAttr("disabled");
				}); 
		 }
	});	
}

//重要通知
function getProductNoticeByCondition(productId,startTime,endTime){
	//alert(startTime+"  "+endTime);
	$.ajax({
		type: "POST",
		url: "/vst_order/prod/productNotice/getProductNoticeByCondition.do",
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

/**
 * 检查cvv
 */
function checkNeedCvv(cardNo){
	if($.trim(cardNo)!=''){
		$.ajax({
			type: "POST",
			url: "/vst_order/ord/book/checkNeedCvv.do",
			data:{"card":cardNo},
			success: function (data) {
				if(data.success){
					if(!data.attributes.valid){
						$("#card_error").html("信用卡卡号错误").show();
						$("#card_error").css("color","red");
					}else{
						$("#card_error").html("卡号正确").show();
						$("#card_error").css("color","");
						if(data.attributes.needCvv){
							$("input[type=text][name='guarantee.cvv']").closest("tr").show();
						}else{
							$("input[type=text][name='guarantee.cvv']").closest("tr").hide(200);
							$("input[type=text][name='guarantee.cvv']").val("");
						}
					}
				}else{
					$("#card_error").html("数据加载异常").show();
				}
			}
		});
	}
}

//客人姓名text控制常用游客checkbox
function txtIocCheckEng(){
	//$("input[type=checkbox][name='commonlyUsed']:checked")
	$("#commonlyUsed").find("input[type=checkbox]:checked").each(function(){
	    var fullName=$(this).attr("personName");
	    var receiverId=$(this).val();
	    var checkedFlag=false;
	    $("#Jtenantlist2").find("input[type=text]").each(function(){
		    var travellersId=$(this).attr("travellersId");
	    		if(fullName==$(this).val()&&travellersId==receiverId){
	    			checkedFlag=true;
	    			return false;
		    	}
	    	});
	   	if(!checkedFlag){
	   		$(this).removeAttr("checked");
	   	}
  });
		$("#Jtenantlist2").find("input[type=text]").each(function(){
		var fullName=$(this).val();
		 var travellersId=$(this).attr("travellersId");
		$("#commonlyUsed").find("input[type=checkbox]").each(function(){
				var receiverId=$(this).val();
		     	if(fullName==$(this).attr("personName")&&travellersId==receiverId){
	    			checkedFlag=true;
	    			$(this).attr("checked","checked");
	    			return false;
		    	}
			});
	});
}