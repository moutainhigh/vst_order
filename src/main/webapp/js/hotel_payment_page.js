	//账户抵扣
	$("input[name='accountMonery']").keyup(function(event){ 
		var text = $(this).val();
		if($.trim(text)==''){
			$(this).parent().siblings('.btn-orange').removeClass('btn_stop');
			return;
		}
		var re = /^-?\d+\.?\d*$/;		 
		var key = $(this).val(),
		zhanghu = $("#maxPayMoney").html();
		if (event.keyCode == "110" || event.keyCode == "190") {}
		if(Number(key)>Number(zhanghu) || !re.test(text)){
			$(this).parent().siblings('.btn-orange').addClass('btn_stop');
		}else if(Number(key)<Number(zhanghu)){			
			var yingfuAmt=getAmountPrice();//应付总额
			var yfAmt = (Number(yingfuAmt)/100).toFixed(2);
		    if(Number(key)>Number(yfAmt)){
				$(this).parent().siblings('.btn-orange').addClass('btn_stop');
			}else{
				$(this).parent().siblings('.btn-orange').removeClass('btn_stop');
			}
		    if(text.indexOf(".") > 0 ){
			  var length=text.toString().split(".")[1].length;
				if(length>2){
				  $(this).parent().siblings('.btn-orange').addClass('btn_stop');
				}
			}
		}
	});


$(".BounsPayLi").show();
$(".CashsPayLi").hide();

//获取用户奖金账户余额以及该用户绑定的优惠券
getLogionMessageInfo();

//输入奖金金额
$('#input_bonus').keyup(function(event){
	var text = $(this).val();
	if($.trim(text)==''){
		$(this).parent().siblings('.btn-orange').removeClass('btn_stop');
		return;
	}
	var re = /^-?\d+\.?\d*$/;
	var key = $(this).val();
	//超过使用次数返回-1
	if($('.maxpayBounsAmt').val() ==-1){
		var  obj = $("#input_bonus");
		var textmsg = '每月最多可使用10次奖金，每年最多可使用30次！';
		nameFull(obj,textmsg,10,8,'warning');
		$(this).parent().siblings('.btn-orange').addClass('btn_stop');
		return false;
	}
	zhanghu = parseFloat($(this).parents('.dikou_box').siblings('.dikou_b').find('samp').text());
	if (event.keyCode == "110" || event.keyCode == "190") {}
	if(Number(key)>Number(zhanghu) || !re.test(text)){
		$(this).parent().siblings('.btn-orange').addClass('btn_stop');
	}else if(Number(key)<Number(zhanghu)){
	
		var mostAmount = $(".maxCanPayBouns").html();//账户最多使用金额	
		var yingfuAmt=getAmountPrice();//应付总额
		var yfAmt = (Number(yingfuAmt)/100).toFixed(2);
		if(Number(key)>Number(mostAmount)){
			$(this).parent().siblings('.btn-orange').addClass('btn_stop');
		}else if(Number(key)>Number(yfAmt)){
			$(this).parent().siblings('.btn-orange').addClass('btn_stop');
		}else{
			$(this).parent().siblings('.btn-orange').removeClass('btn_stop');
		}
		if(text.indexOf(".") > 0 ){
		   var length=text.toString().split(".")[1].length;
		   if(length>2){
			 $(this).parent().siblings('.btn-orange').addClass('btn_stop');
		   }
		}
	}
});
/*使用奖金抵扣*/
$('.js_dikou_queren').live('click',function(){
	if(!$(this).hasClass('btn_stop')){
		var price = $(this).siblings('.yong_input').find('input').val(); 
		$(this).siblings('.yong_input').find('input').val(price);
		if(!price){
			price = '0';
		}
		$(this).hide().next('.dikou_box').show();
		$(this).siblings('.yong_input').hide();
		$(this).siblings('.yong_text').show().html(parseFloat(price)+' 元');
		
		//----------------------------------------------20150701 add by wanghuiwu  区分奖金现金礼品卡等---STA
		if($(this).hasClass('js_dikou_queren_bonus')){
			var maxCanPayBouns = $('.maxCanPayBouns').text();
			//奖金代扣
			if(parseFloat(price)>parseFloat(maxCanPayBouns)){
				var  obj = $(".js_dikou_quxiao");
				var text = '输入奖金超过最大可用额度！';
				nameFull(obj,text,10,8,'warning');
				return false;
			}				
			if($('.maxpayBounsAmt').val() ==-1){//超过使用次数返回-1
				var  obj = $(this).parents("div[class=dikou_box]");
				var text = '每月最多可使用10次奖金，每年最多可使用30次！';
				nameFull(obj,text,10,8,'warning');
				return false;
			}
			var jjprice = price*100;
			$('.bonusAmountHidden').val(jjprice);			
			$('#daikou_bonus').html("- ￥ "+price);
			var canPayBounsHidden = $('#CanPayBouns').html()*100;
			var CanPayBouns = (parseFloat(canPayBounsHidden)/100).toFixed(2);
			var lessPayBonus = parseFloat(CanPayBouns)-parseFloat(price);
			if(parseFloat(lessPayBonus)>0){
				$('.bonusAmountHidden').val();
				$('#CanPayBouns').html(lessPayBonus.toFixed(2));
				$('#canPayBounsHidden').val(lessPayBonus.toFixed(2)*100);
			}
		}else if($(this).hasClass('js_dikou_queren_cash')){
//			var price1 = $("#accountMonery").val();
			//现金存款代扣
			var xjprice = parseFloat(price)*100;
			$('.cashAmountHidden').val(parseFloat(xjprice).toFixed(0));
			$('#daikou_cash').html("- ￥ "+price);				
			var maxPayMoney=$('#maxPayMoneyHidden').val()/100;
			var lessPayMoney = parseFloat(maxPayMoney)-parseFloat(price);			
			if(parseFloat(lessPayMoney)>=0){
				$('#maxPayMoneyHidden').val(lessPayMoney.toFixed(2)*100);
				$('#maxPayMoney').html(lessPayMoney.toFixed(2));
			}else{
				var obj=$("#zhckPrice");
				var text = '余额不足，不能使用！';
				nameFull(obj,text,10,8,'warning');
				return false;
			}

		}
		//计算应付金额
		calcShouldAmt(thisProCategoryId,1);
		//----------------------------------------------20150701 add by wanghuiwu  区分奖金现金礼品卡等---END
		
	}
	
});

$('.js_dikou_quxiao').live('click',function(){
	var bonusAmt = $("div[class*=dikou_box]").find(".yong_text").text();
	$(this).siblings('.btn').show();
	$(this).siblings('.yong_input').show();
	$(this).siblings('.yong_text').hide().html('￥ 0');
	//$(this).parent().siblings('.btn-orange').removeClass('btn_stop');
	$("#sure_dikou_bonus").removeClass('btn_stop');
	//----------------------------------------------20150701 add by wanghuiwu  区分奖金现金礼品卡等---STA
	if($(this).hasClass('js_dikou_queren_bonus')){
		//奖金代扣
	    $(".bonusAmountHidden").val(0);
	    $("#input_bonus").val("");
	    var oldCanPay = $("#canPayBounsHidden_1").val();
	    $("#CanPayBouns").html(oldCanPay/100);
	    $("#canPayBounsHidden").val(oldCanPay);
		//价格计算
		//calcShouldAmt(thisProCategoryId);
	    //取消使用奖金
	    quxiaoHotelBonus(parseFloat(bonusAmt));
	    
	}else if($(this).hasClass('js_dikou_queren_cash')){
		//现金存款代扣
		$('.cashAmountHidden').val(0);
		var oldMax = $("#maxPayMoneyHidden_1").val();
		$('#maxPayMoneyHidden').val(oldMax);
		$('#maxPayMoney').html(oldMax/100);
		$("input[name='accountMonery']").val("");
		//价格计算
		calcShouldAmt(thisProCategoryId);
	}
	$(this).parents('.youhui_info').hide().siblings('.youhui_tit').removeClass('info_show');
	$('.js_nameFull').remove();
	//----------------------------------------------20150701 add by wanghuiwu  区分奖金现金礼品卡等---END
});
	
	
	function checkOrderForPayOther(){
		var flag=true;
		$.ajax({  
	         type : "post",  
	          url : "/vst_order/book/ajax/checkOrderForPayOther.do",  
	          data: $("form[name='orderForm']").serialize(),
	          dataType: "json",
	          async : false,  
	          success : function(data){  
	        	  if(data.success){
	        		  var checkResult=data.attributes.checkResult;
	        		  var bonusBalance=data.attributes.bonusBalance;
        	          var maxPayMoney=data.attributes.maxPayMoney;
	        		  if(checkResult!=""){
	        			  flag=false;
	        	         $(".dikou_price").find("#CanPayBouns").html(bonusBalance/100);
	        	         $(".dikou_price").find("#canPayBounsHidden").val(bonusBalance);
	        	         $(".dikou_price").find("#maxPayMoney").html(maxPayMoney/100);
	        	         $(".dikou_price").find("#maxPayMoneyHidden").val(maxPayMoney);
	        	         $.alert(checkResult);
	        		  }
	        	  }else{
	        		  $.alert("小驴抱歉的通知你服务器验证支付信息失败,请重新提交订单");
	        		  flag=false;
	        	  }
				
	          }  
	     });
		return flag;
	}
	
	function displayHotelBonus(){
		var _bonusAmt = $("#input_bonus").val();
		if(!$.isNumeric($.trim(_bonusAmt))){
			_bonusAmt = "0.00";
		}else{
			_bonusAmt = parseFloat(_bonusAmt).toFixed(2);
		}
		var bonusLabelAmt = $(".bonusHotelAmt");
		
		if(bonusLabelAmt.length>0){
			bonusLabelAmt.text(_bonusAmt+"");
		}
		var y = $("#showOughtPayPriceId").attr("value");
		if(!$.isNumeric(y)){
			y = 0.00;
		}
		if(!$.isNumeric(_bonusAmt)){
			_bonusAmt = 0.00;
		}
		var val = parseFloat(y )*100 - parseFloat(_bonusAmt)*100;
		val = val /100.00;
		val = val.toFixed(2);
		$("#showOughtPayPriceId").attr("value",val).text(val);
	}
	
	//将使用的奖金额度，放入统计，并显示
	function displayBonus(){
		var _bonusAmt = $("#input_bonus").val();
		if(!$.isNumeric($.trim(_bonusAmt))){
			_bonusAmt = "0.00";
		}else{
			_bonusAmt = parseFloat(_bonusAmt).toFixed(2);
		}
		var bonusLabelAmt = $("#totalOrderPriceDiv p").find(".bonusLabelAmt");
		//如果有这个label元素，则直接替换金额,如果没有那么将对字符串元素进行操作
		if(bonusLabelAmt.length > 0){
			bonusLabelAmt.text(_bonusAmt + " ");
		}else{
			var html = $("#totalOrderPriceDiv p").html();
			var start = html.indexOf("奖金抵扣");
			var end = html.indexOf("=");
			var f_html = html.substring(0,start);
			var l_html = html.substring(end);
			var _bonusHtml = "奖金抵扣¥<span class='bonusLabelAmt'>"+_bonusAmt+" </span> ";
			html = f_html + _bonusHtml + l_html;
			$("#totalOrderPriceDiv p").html(html);
		}
		
		var y = $("#showOughtPayPriceId").attr("value");
		if(!$.isNumeric(y)){
			y = 0.00;
		}
		if(!$.isNumeric(_bonusAmt)){
			_bonusAmt = 0.00;
		}
		var val = parseFloat(y )*100 - parseFloat(_bonusAmt)*100;
		val = val /100.00;
		val = val.toFixed(2);
		$("#showOughtPayPriceId").attr("value",val).text(val);
		
	}
	
	function quxiaoHotelBonus(bonusAmt){
		if(bonusAmt == null || bonusAmt == 'undefined' || !$.isNumeric(bonusAmt)){
			bonusAmt = "0.00 ";
		}
		var _bonusAmt = "0.00 ";
		
		var bonusLabelAmt = $(".bonusHotelAmt");
		
		if(bonusLabelAmt.length > 0){
			bonusLabelAmt.text(_bonusAmt + " " );
		}
		var y = $("#showOughtPayPriceId").attr("value");
		if(!$.isNumeric(y)){
			y = 0.00;
		}
		var val = parseFloat(y )*100+ parseFloat(bonusAmt)*100;
		val = val /100.00;
		val = val.toFixed(2);
		$("#showOughtPayPriceId").attr("value", val ).text(val);
		
	}
	
	
	function quxiaoBonus(bonusAmt){
		if(bonusAmt == null || bonusAmt == 'undefined' || !$.isNumeric(bonusAmt)){
			bonusAmt = "0.00 ";
		}
		var _bonusAmt = "0.00 ";
		var bonusLabelAmt = $("#totalOrderPriceDiv p").find(".bonusLabelAmt");
		//如果有这个label元素，则直接替换金额,如果没有那么将对字符串元素进行操作
		if(bonusLabelAmt.length > 0){
			bonusLabelAmt.text(_bonusAmt + " " );
		}else{
			var html = $("#totalOrderPriceDiv p").html();
			var start = html.indexOf("奖金抵扣");
			var end = html.indexOf("=");
			var f_html = html.substring(0,start);
			var l_html = html.substring(end);
			var _bonusHtml = "奖金抵扣¥<span class='bonusLabelAmt'>"+_bonusAmt+" </span> ";
			html = f_html + _bonusHtml + l_html;
			$("#totalOrderPriceDiv p").html(html);
		}
		
		var y = $("#showOughtPayPriceId").attr("value");
		if(!$.isNumeric(y)){
			y = 0.00;
		}
		var val = parseFloat(y )*100+ parseFloat(bonusAmt)*100;
		val = val /100.00;
		val = val.toFixed(2);
		$("#showOughtPayPriceId").attr("value", val ).text(val);
		
	}

	function calcShouldAmt(categoryNo,type){		
		if(thisProCategoryId ==1){
			calHotelCouponAmt();
			if(type ==1){
				displayHotelBonus();
			}
		}
		if(thisProCategoryId !=1){
			if(type ==1){
			 displayBonus();
			}			
			calCouponAmt();
		}
	}
	
	//优惠券提示，添加优惠券
	$('#addCoupon').live('click',function(){ 	
		var fixedCouponTbody=$(".fixedCouponTbody");
	   	var freeCouponTbody=$(".freeCouponTbody");
	   	var couponTypeFlag=$(".couponTypeFlag").val();
	   	var no_youhuiquanTr=$(".no_youhuiquanTr");
	   	var text = "";  
		var  obj = $(this).siblings('input');
		var objVal = $(this).siblings('input').val();
		if($.trim(objVal).length==0){
			var msg = '请输入优惠券！';
			//nameFull(obj,msg,10,8,'warning');
			alert(msg);
			return false;
		}
		var isload = true;
		//将优惠券的值和已经查询出来的值想比较，判断是否重复
		$('.freeCouponTbody tr').each(function(){
			var coupCode = $(this).find("td").eq(1).html();
			if($.trim(coupCode) == $.trim(objVal)){
	   			var msg = '该优惠券兑换码已经添加，请输入其他兑换码';
	   			isload = false;
       			//nameFull(obj,msg,10,8,'warning');
       			alert(msg);
       			return false;
			}
		});
		$('.fixedCouponTbody tr').each(function(){
			var coupCode = $(this).find("td").eq(1).html();
			if($.trim(coupCode) == $.trim(objVal)){
	   			var msg = '该优惠券兑换码已经添加，请输入其他兑换码';
	   			isload = false;
       			//nameFull(obj,msg,10,8,'warning');
       			alert(msg);
       			return false;
			}
		});
		 
		var $form = $("form[name='orderForm']");
		if($form.length==0){
			$form = $("form[name='holdBookInfoForm']");
		}
		if(isload){
			$.ajax({  
		        type : "post",  
		         url : "/vst_order/book/ajax/insertCoupon.do?coupon="+objVal,  
		         data: $form.serialize(),
		         dataType: "json",
		         async : false,  
		         success : function(data){
		       	  var myCouponList=data.myCouponList;
		       	  var errMsg = data.errMessage;
		       	  if($.trim(errMsg).length != 0){
		       		//nameFull(obj,errMsg,10,8,'warning');
		       		alert(errMsg);
		       		return false;
		       	  }
		       	  //循环遍历从后台获取的账户绑定优惠券信息
		       	  //判断有效
		       	  if(myCouponList){
		       		  $.each(myCouponList, function(k,item){
		       			if(item.couponCode==objVal && $.trim(errMsg).length > 0){
		       				return true;
		       			}
		       			  if($.trim(item.validInfo).length==0){
		       				  var couponTr;
		       				  couponTr=$("<tr class='loginInsert'></tr>");
		       			  var couponNameTd=$("<td class='counpName'></td>");
		       			  var couponNameTdLable=$("<label></label>");
		       			  var couponInput;	
		       			  //test
		       			  if(couponTypeFlag&&couponTypeFlag=="true" && "0"==item.favorType){
			       			  couponInput=$("<input class='js_check_tips' type='checkbox' tips_L='true' name='youhui_name' />");
			       			 }else{
				             couponInput=$("<input class='js_check_tips' type='radio' tips_L='true' name='youhui_name'/>");
			       			}
		       			  	  
		       			  	  couponInput.val(item.discountAmount);
		       			  	  couponInput.attr("couponCode",item.couponCode);
		       			  	  couponNameTdLable.append(couponInput);
		       			  	  couponNameTdLable.append(item.couponNameStr);
		       			  	  couponNameTdLable.attr("title",item.couponName);
		       			  	  couponNameTd.append(couponNameTdLable);
		       			  var couponNoTd=$("<td class='counpCode'></td>");
		       			  	  couponNoTd.html(item.couponCode);
		       			  var couponPriceTd=$("<td class='counpAmt'></td>");
		       			  var couponPriceSpan=$("<span class='yh_price'></span>");
		       			  var couponPriceDfn=$("<dfn>&yen;</dfn>");		       			  	  
		       			  	  couponPriceSpan.append(couponPriceDfn);
		       			  	  couponPriceSpan.append((item.discountAmount/100).toFixed(2));
		       			  	  couponPriceTd.html(couponPriceSpan);
		       			  var couponExpTd=$("<td class='counpExp'></td>");
		       			  	  couponExpTd.html(item.expiredDate);
		       			  	  couponTr.append(couponNameTd);
		       				  couponTr.append(couponNoTd);
		       				  couponTr.append(couponPriceTd);
		       				  couponTr.append(couponExpTd);
		       				  if("0"==item.favorType){
		       					 fixedCouponTbody.append(couponTr);
			        			}else{
			        			 freeCouponTbody.append(couponTr);
			        			 }
		       				  		        	  
		       			  }else{
		       			    text = item.validInfo;
		       				nameFull(obj,text,10,8,'warning');
		       				alert(text+'sadasd');
		       				return false;
		       			  }
		       		 }); 
		       		  
		       		  var fixedNum=fixedCouponTbody.find("input[name='youhui_name']").size();
		       		  var freeNum=freeCouponTbody.find("input[name='youhui_name']").size();
		       		  if(fixedNum>0 || freeNum>0){
		       			  no_youhuiquanTr.hide();
		       		  }else{
		       			no_youhuiquanTr.show();
		       			if($.trim(text).length==0){
		       				text = '优惠券兑换码输入错误，请重新输入';	
			       			alert(text);
			       			return false;
		       			}	       			
		       			//nameFull(obj,text,10,8,'warning');

		       		 }
		       	  }	       	  
		        }  
		    });
		}		
	});
	
	//取消使用的优惠券
	$(".cancelYouHui").live('click',function(){
		var $browsers = $("input[name=youhui_name]"); 
		$browsers.attr("checked",false);
		calcShouldAmt(thisProCategoryId);
	});
	
	
	
	$("input[name='youhui_name']").live('change',function(){
		var fixedCouponTbody=$(".fixedCouponTbody");
	   	var freeCouponTbody=$(".freeCouponTbody");
 		var fixedNum=fixedCouponTbody.find("input[name='youhui_name'][type='checkbox']:checked").size();
   		var freeNum=freeCouponTbody.find("input[name='youhui_name'][type='checkbox']:checked").size();
   		//被选中的数目
   		var checkNum = parseInt(fixedNum)+parseInt(freeNum);		
   		var quantity = $(".youhuiQuantity").val();
   		var personNum =$(".youhuiperson").val();
		var saleType = $('.productSaleType').val();
		var textMsg = '';		
		if(thisProCategoryId =='4'){
			var visoQuantity = $(".visoQuantity").val();
			if(parseInt(checkNum)>parseInt(visoQuantity)){
				   $(this).attr("checked", false);
				   
				   var objthis = $(this);
				   textMsg ="使用优惠券数目不能大于购买数量"; 
				   nameFull(objthis,textMsg,10,8,'warning');
				   return false;
			}
			
		}
		//按份
		if(saleType == 'COPIES')
		{
		  if(parseInt(checkNum)>parseInt(quantity) &&thisProCategoryId !=4 ){			   
			   $(this).attr("checked", false);
			   var objthis = $(this);
			   textMsg ="使用优惠券数目不能大于购买份数"; 
			   nameFull(objthis,textMsg,10,8,'warning');			   
				return false;
		  }
		}else
		{
			if(parseInt(checkNum)>parseInt(personNum)&&thisProCategoryId !=4 ){
				$(this).attr("checked", false);
				var objthis = $(this);
				textMsg ="使用优惠券数目不能大于下单购买人数"; 
				nameFull(objthis,textMsg,10,8,'warning');			
				return false;
			}
		}
		
		if($.trim(textMsg)==''){
			calcShouldAmt(thisProCategoryId);
		}
	});
	
	function checkDot(shiyongAmt,type,thisCardNo){
		var thisId = type==0?"#czk_input_amt_":"#lpk_input_amt_";
		var dotIndex = (""+shiyongAmt).indexOf(".");
		var amtLength = 0 ;
		if(dotIndex>0){
			amtLength = (""+shiyongAmt).substring(dotIndex).length;
		}
		if(amtLength > 3){
			var  obj = $(thisId+thisCardNo);
			var text = '请输入正确的金额[最多保存两位小数点]';
			nameFull(obj,text,-65,8,'warning');
			return false;
		}else{
			removeTips();
		}
		return true;
	}
	

	
	
	function clearPageCouponMessage(){
		  var fixedCouponTbody=$(".fixedCouponTbody");
	   	  var freeCouponTbody=$(".freeCouponTbody");
	   	  			/**清除已绑定的账户的信息**/
	   	  	if(fixedCouponTbody&&fixedCouponTbody.find(".loginInsert").size()>0){
	   	  	fixedCouponTbody.find(".loginInsert").remove();
	   	  	}
	   		if(freeCouponTbody&&freeCouponTbody.find(".loginInsert").size()>0){
	   			freeCouponTbody.find(".loginInsert").remove();
	   	   	 }
	}
	
	function getLogionMessageInfo(){
		  var fixedCouponTbody=$(".fixedCouponTbody");
	   	  var freeCouponTbody=$(".freeCouponTbody");
	   	  var couponTypeFlag=$(".couponTypeFlag").val();
	   	  var no_youhuiquanTr=$(".no_youhuiquanTr"); 
	  	var $form = $("form[name='orderForm']");
		if($form.length == 0){
			$form = $("form[name='holdBookInfoForm']");
		}
	   	 /**清除已绑定的账户的信息**/
		
		/** 查看商品是否是能够使用优惠券的商品* */
		$.ajax({
			   type: "POST",
			   url: "/vst_order/book/ajax/insertCoupon.do",
			   dataType : "json",
			   data:$form.serialize(),
			   success: function(data){
					var errMsg = data.errMessage;
					if ($.trim(errMsg).length != 0) {
					$("#houtai").attr("style","");
					$("#no_use").html("很遗憾，本产品不可用优惠券");
					$(".youhui_add").attr("style","display:none");
					$(".youhui_table_box").hide();
					}
			   }
		});
		//	   	clearPageCouponMessage();
	   	//获取用户绑定优惠券以及奖金账户现金账户情况
		$.ajax({
	        type : "post",  
	         url : "/vst_order/book/ajax/getLoginUserAccountInformation.do",  
	         data: $form.serialize(),
	         dataType: "json",
	         async : false,  
	         success : function(data){
	        	 if(data == null ){
	        		 alert("请求数据失败，后台错误");
	        		 return false;
	        	 }
	       	  	var myCouponList=data.attributes.userCouponVOList;
		       	  //奖金余额
		       	  var bonusBalance=data.attributes.bonusBalance;
		       	  
		          var maxPayMoney=data.attributes.maxPayMoney;
		          var loginUserId=data.attributes.loginUserId;
		          $("input[name='payOderUserId']").val(loginUserId);
		          if(bonusBalance>=0){
		        	  $(".dikou_price").find("#CanPayBouns").html((parseFloat(bonusBalance)/100).toFixed(2));
		          }else{
		        	  $(".dikou_price").find("#CanPayBouns").html(0.00);
		          }
		         //奖金余额
		         $(".dikou_price").find("#canPayBounsHidden").val(bonusBalance);
		         $("#canPayBounsHidden_1").val(bonusBalance);
		         $(".BounsPayLi").show();
		         
		         //账户存款
		         $(".dikou_price").find("#maxPayMoney").html((parseFloat(maxPayMoney)/100).toFixed(2)); 
		         $(".dikou_price").find("#maxPayMoneyHidden").val(maxPayMoney);
		         $("#maxPayMoneyHidden_1").val(maxPayMoney);
		         // $(".CashsPayLi").show();
		       	 //循环遍历从后台获取的账户绑定优惠券信息
		       	  if(myCouponList){
		       		  $.each(myCouponList, function(k,item){
		       			  var couponTr;
		       				  couponTr=$("<tr class='loginInsert'></tr>");
		       			  var couponNameTd=$("<td class='counpName'></td>");
		       			  var couponNameTdLable=$("<label></label>");
		       			  var couponInput;
		       			  	  if(couponTypeFlag&&couponTypeFlag=="true" && "0"==item.favorType){
		       			  		couponInput=$("<input class='js_check_tips' type='checkbox' tips_L='true' name='youhui_name' />");
		       			  	  }else{
			        			couponInput=$("<input class='js_check_tips'  type='radio' tips_L='true' name='youhui_name'/>");
		       			  	  }
		       			  	  couponInput.val(item.discountAmount);
		       			  	  couponInput.attr("couponCode",item.couponCode);
		       			  	  couponNameTdLable.append(couponInput);
		       			  	  couponNameTdLable.append(item.couponNameStr);
		       			  	  couponNameTdLable.attr("title",item.couponName);
		       			  	  couponNameTd.append(couponNameTdLable);
		       			  var couponNoTd=$("<td class='counpCode'></td>");
		       			  	  couponNoTd.html(item.couponCode);
		       			  var couponPriceTd=$("<td class='counpAmt'></td>");
		       			  var couponPriceSpan=$("<span class='yh_price'></span>");
		       			  var couponPriceDfn=$("<dfn>&yen;</dfn>");
		       			  	  couponPriceSpan.append(couponPriceDfn);
		       			  	  couponPriceSpan.append((item.discountAmount/100).toFixed(2));
		       			  	  couponPriceTd.html(couponPriceSpan);
		       			  var couponExpTd=$("<td class='counpExp'></td>");
		       			  if(item.expiredDate!=""){
		       			  	  couponExpTd.html(item.expiredDate);
		       			  }
		       			  	  couponTr.append(couponNameTd);
		       				  couponTr.append(couponNoTd);
		       				  couponTr.append(couponPriceTd);
		       				  couponTr.append(couponExpTd);
		       				  if("0"==item.favorType){
		       					  	fixedCouponTbody.append(couponTr);
			        			  	  }else{
			        			  		freeCouponTbody.append(couponTr);
			        			  	  }
		       				  
			        	  }); 
		       		  
		       		  var fixedNum=fixedCouponTbody.find("input[name='youhui_name']").size();
		       		  var freeNum=freeCouponTbody.find("input[name='youhui_name']").size();
		       		  if(fixedNum>0 || freeNum>0){
		       			  no_youhuiquanTr.hide();
		       		  }else{
		       			no_youhuiquanTr.show();
		       		  }
		       	  }
	         }  
	    });
	}
	
function showAmountInfoNew(data){
	
	Express.disposeExpressPriceResult(data);
	var priceInfo = data.attributes.priceInfo;
	//console.log(priceInfo);
	//console.log(1);
	//异步判断是否能够使用储值卡和礼品卡,false  不显示， true 显示
	//alert("开始判断0.03咯");
	var lpkDIV = $(".showLPK");
	var czkDIV = $(".showCZK");
	var isCanBoundLipinkaPay = false;
	isCanBoundLipinkaPay = priceInfo.canBoundLipinkaPay;
	//如果是签证，那么直接不显示，门票则判断这个0.03，其他直接显示
	//门票
	if(thisProCategoryId == 5 || thisProCategoryId == 12 || thisProCategoryId == 11 || thisProCategoryId == 13){
		//do nothing ,just do it
	}else
	//签证
	if(thisProCategoryId == 4){
		isCanBoundLipinkaPay = false;
	}else{
	//其他【这里只是线路了】
		isCanBoundLipinkaPay = true;
	}
	//先上优惠券，等邮轮过来再放开，如果邮轮来了，删掉下面这一行
	isCanBoundLipinkaPay = false;
	isCanBoundLipinkaPay = isCanBoundLipinkaPay+"";
	if(isCanBoundLipinkaPay=="true"){
		if(lpkDIV!=null){
			//lpkDIV.removeAttr("style");
		}
		if(czkDIV!=null){
			//czkDIV.removeAttr("style");
		}
	}else{
		if(lpkDIV!=null){
			lpkDIV.attr("style","display:none");
		}
		if(czkDIV!=null){
			czkDIV.attr("style","display:none");
		}
		$(".czk_table").empty();
		$(".lpk_table").empty();
	}
	//alert("开始判断0.03咯" + isCanBoundLipinkaPay);
	//console.log(2);
	//产品总价开始
	//奖金最大可用
	if(priceInfo){
		if(priceInfo.maxBonus<0){
			$(".maxCanPayBouns").html((0).toFixed(2));
		}else{
			$(".maxCanPayBouns").html((priceInfo.maxBonus/100).toFixed(2));
		}
		$(".maxpayBounsAmt").val(priceInfo.maxBonus);
		if(priceInfo.maxBonus>0){
			$(".BounsPayLi").show();
		}else{
			$(".BounsPayLi").hide();
		}
	}
	//console.log(4);
	if(priceInfo){
		$(".rebateAmountSpan").html((priceInfo.rebateAmount/100).toFixed(2));
		if(priceInfo.rebateAmount>0){
			$(".rebateAmountSpan").parent().show();
		}else{
			$(".rebateAmountSpan").parent().hide();
		}
	}
	//console.log(4);
	
	if (priceInfo.goodsTotalPriceToYuan) {
		$("#priceInfoDiv").find("p.fk_Amount span").html("&yen;"+ (parseFloat(priceInfo.goodsTotalPrice)/100).toFixed(2));
		var amoutPVal=$("input[name='amountProuctHidden']").val();
		var payOderUserId=$("input[name='payOderUserId']").val();
		if(payOderUserId!=""&&payOderUserId!=null){
			if(amoutPVal!="" && amoutPVal!=null){
				if(parseInt(amoutPVal)!=0){
					if(parseInt(amoutPVal)!=parseInt(priceInfo.goodsTotalPrice)){
						getLogionMessageInfo();
					}
					
				}
				
			}
		}
		$("input[name='amountProuctHidden']").val(priceInfo.goodsTotalPrice);
	}
	//门票总价显示
	if($("p.pro_jiage span ").size()>0){
		if(priceInfo.ticketGoodsPrice>0){
			$("p.pro_jiage span ").html("<small>¥</small>"+priceInfo.ticketGoodsPriceToYuan);
		}

	}
	
	
	// 快递总价显示控制 ---开始 
	if (priceInfo.expressPriceToYuan != '0.00') {
		$("#priceInfoDiv").find("p.fk_ExpressAmount span").html("&yen;"+ priceInfo.expressPriceToYuan);
		$("#priceInfoDiv").find("p.fk_ExpressAmount").show();
	}else{
		$("#priceInfoDiv").find("p.fk_ExpressAmount").hide();
	}
	
	// 保险显示控制 ---开始 
	if (priceInfo.insurancePrice >0) {
		$("#priceInfoDiv").find("p.fk_Insurance span").html("&yen;"+ (parseFloat(priceInfo.insurancePrice)/100).toFixed(2));
		$("#priceInfoDiv").find("p.fk_Insurance").show();
	}else{
		$("#priceInfoDiv").find("p.fk_Insurance").hide();
	}
	// 快递总价显示控制 ---结束
	//console.log(5);
	//促销金额开始
	if (priceInfo.promotionAmountYuan != '0.00') {
		$("#priceInfoDiv").find("p.fk_promotionAmount span").html("-&yen;"+ priceInfo.promotionAmountYuan);
		$("#priceInfoDiv").find("p.fk_promotionAmount").show();
	}else{
		$("#priceInfoDiv").find("p.fk_promotionAmount").hide();
	}
	//console.log(6);
	var disTotleAmt=0.0;
	//促销金额结束
	//奖金账户使用开始
	var bonusAmountHidden=$(".bonusAmountHidden");
		if(bonusAmountHidden)
		{
			var bonusAmount= bonusAmountHidden.val();
			if(bonusAmount&&bonusAmount>0){
				disTotleAmt+=parseFloat(bonusAmount);
			$("#priceInfoDiv").find("p.fk_bonusAmount span").html("-&yen;"
					+ (parseFloat(bonusAmount)/100).toFixed(2));
				$("#priceInfoDiv").find("p.fk_bonusAmount").show();
			}else{
				$("#priceInfoDiv").find("p.fk_bonusAmount").hide();
			}
		}
	//奖金使用结束
		//console.log(7);
		//现金账户使用开始
		var cashAmountHidden=$(".cashAmountHidden");
			if(cashAmountHidden)
			{
				var cashAmount= cashAmountHidden.val();
				if(cashAmount&&cashAmount>0){
					disTotleAmt+=parseFloat(cashAmount);
				$("#priceInfoDiv").find("p.fk_cashAmount span").html("-&yen;"
						+ (parseFloat(cashAmount)/100).toFixed(2));
					$("#priceInfoDiv").find("p.fk_cashAmount").show();
				}else{
					$("#priceInfoDiv").find("p.fk_cashAmount").hide();
				}
			}
		//现金使用结束
			//console.log(8);
	    //优惠券金额获取开始
			var couponAmtTotal=0;
			var index=0;
			$("input[name='youhui_name']").parent().find(".hiddenCouponList").remove();
	        $("input[name='youhui_name']:checked").each(function(){
	        	var hiddenInput=$("<input type='hidden' name='userCouponVoList["+index+"].couponCode' class='hiddenCouponList'>");
	        	hiddenInput.val($(this).attr("couponCode"));
	        	$(this).parent().append(hiddenInput);
	        	index++;
	    	  if($(this).val()!=""&&$(this).val()!=null)
	    		  {
	    			couponAmtTotal+=parseFloat($(this).val());
	    		  }
	    		  });
	        if(couponAmtTotal&&couponAmtTotal>0){
	        	disTotleAmt+=parseFloat(couponAmtTotal);
				$("#priceInfoDiv").find("p.fk_couponAmount span").html("-&yen;"
						+ (parseFloat(couponAmtTotal)/100).toFixed(2));
					$("#priceInfoDiv").find("p.fk_couponAmount").show();
				}else{
					$("#priceInfoDiv").find("p.fk_couponAmount").hide();
				}
	    //优惠券金额获取结束
	        
	        //礼品卡获取开始
			var giftAmtTotal=0;
	        $(".giftCardAmt").each(function(){
	    	  if($(this).val()!=""&&$(this).val()!=null)
	    		  {
	    		  giftAmtTotal+=parseFloat($(this).val());
	    		  }
	    		  });
	        if(giftAmtTotal&&giftAmtTotal>0){
	        	disTotleAmt+=parseFloat(giftAmtTotal);
				$("#priceInfoDiv").find("p.fk_giftCardAmount span").html("-&yen;"
						+ (parseFloat(giftAmtTotal)/100).toFixed(2));
					$("#priceInfoDiv").find("p.fk_giftCardAmount").show();
				}else{
					$("#priceInfoDiv").find("p.fk_giftCardAmount").hide();
				}
	    //礼品卡金额获取结束
	        //console.log(9);
	        //储值卡卡获取开始
			var storeCardAmtTotal=0;
	        $(".storeCardAmt").each(function(){
	    	  if($(this).val()!=""&&$(this).val()!=null)
	    		  {
	    		  storeCardAmtTotal+=parseFloat($(this).val());
	    		  }
	    		  });
	        if(storeCardAmtTotal&&storeCardAmtTotal>0){
	        	disTotleAmt+=parseFloat(storeCardAmtTotal);
				$("#priceInfoDiv").find("p.fk_paidAmount span").html("-&yen;"
						+ (parseFloat(storeCardAmtTotal)/100).toFixed(2));
					$("#priceInfoDiv").find("p.fk_paidAmount").show();
				}else{
					$("#priceInfoDiv").find("p.fk_paidAmount").hide();
				}
	    //储值卡金额获取结束
	        //console.log(10);
	        var oughtPay=priceInfo.goodsTotalPrice-priceInfo.promotionAmount-disTotleAmt+priceInfo.expressPrice+priceInfo.insurancePrice;
	        //原始金额赋值。不要更改
	        var leastPay = oughtPay;
	        if(oughtPay<=0){
	        	oughtPay=0;
	        }
	  var prepaidFalgInput=$("input[name='prepaidFalg_pay']");
	/*  if (prepaidFalgInput.size()>0) {//暂时注释掉
		  $(".BounsPayLi").hide();
		  $(".CashsPayLi").hide();
		  $(".showCZK").hide();
		  $(".showLPK").hide();
	  	}*/
	//订单价格
	$(".fk_box_free").find(".oughtPay").html((oughtPay/100).toFixed(2));
	$(".fk_box_free").find(".oughtPayHidden").val(oughtPay);
	$(".orderamountPage").html((oughtPay/100).toFixed(2));
	//$("#orderPriceDiv").find("p.fk_p2").html("（总价<span>￥ "+priceInfo.goodsTotalPriceToYuan+"</span> + 保险<span>￥ "+priceInfo.insurancePriceToYuan+"</span> +  快递<span>￥ "+priceInfo.expressPriceToYuan+"</span>  - 优惠<span>￥ "+priceInfo.couponToYuan+"</span> - 促销<span>￥<span defaultPromValue='"+priceInfo.promotionAmountYuan+"' id='promotionPrice'>"+priceInfo.promotionAmountYuan+"</span></span>  - 奖金抵扣<span>￥ "+priceInfo.bonusToYuan+"</span> ）");
	showPromPromtion(data);
	  
	if(parseFloat(leastPay)<0){
		pandora.dialog({
			content: "优惠金额已超过应付金额",
			ok: true
		});
      }
	//console.log(11);

}

function initLpkPic(){
	$("#lpk_createCheckCode").show();
	$("#createCheckCodeLPKHref").show();
	$("#lpk_pic_checkCode").removeAttr("placeholder");
	reloadLpkPicCode();
}

//包含则返回true ,否则false
function checkContainsChinese(str){
	return /.*[\u4e00-\u9fa5]+.*$/.test(str);
}

function useAmt(shiyongAmt,yingfuAmt,leftAmt){
	shiyongAmt = parseInt(shiyongAmt);
	yingfuAmt = parseInt(yingfuAmt);
	leftAmt = parseInt(leftAmt);
	
	
	var sjAmt = shiyongAmt;
	if(yingfuAmt == leftAmt){
		sjAmt = Math.min(shiyongAmt,leftAmt);
		return sjAmt;
	}
	
	if(yingfuAmt-leftAmt<0 && shiyongAmt-yingfuAmt>0){
		sjAmt = yingfuAmt;
	}else if(yingfuAmt-leftAmt<0&&shiyongAmt-yingfuAmt<=0){
		sjAmt = shiyongAmt;
	}else if(yingfuAmt-leftAmt>0&&shiyongAmt-leftAmt>0){
		sjAmt = leftAmt;
	}else if(yingfuAmt-leftAmt>0&&shiyongAmt-leftAmt<=0){
		sjAmt = shiyongAmt;
	}
	
	
	return sjAmt;
}
//通过 则返回 true
function checkLpkCzkUseAmt(type,thisCardNo,shiyongAmt){
	var _id = type == 0 ?"#czk_input_amt_":"#lpk_input_amt_";
	
	if(isNaN(shiyongAmt) || shiyongAmt=="" || shiyongAmt < 0 || shiyongAmt.indexOf(".")==0){
		var  obj = $(_id + thisCardNo);
		var text = '请输入正确的金额';
		nameFull(obj,text,-15,8,'warning');
		return false;
	}else{
		removeTips();
	}
	if(!checkDot(shiyongAmt,0,thisCardNo)){
		return false
	}
	
	if(shiyongAmt > 1000000){
		var  obj = $(_id + thisCardNo);
		var text = '一次不能使用超过1000000元';
		nameFull(obj,text,-35,8,'warning');
		return false;
	}
	return true;
}

function getAmountPrice(){
	var retrunPrice=0;
	$(".fk_box_free").find(".oughtPayHidden");
	if($(".fk_box_free") && $(".fk_box_free").find(".oughtPayHidden").size()>0){
		retrunPrice=$("#orderPriceDiv").find(".oughtPayHidden").val();
	}
	
	var yingfuAmtStr = $("#showOughtPayPriceId").text();
//	if(!$.isNumeric(yingfuAmtStr) || yingfuAmtStr == null || yingfuAmtStr == ""){
//		return 99999;
//	}
	//以分为单位
	return parseFloat(yingfuAmtStr*100);
}
	
	
	
	
	function checkPromotionList(){
		
		var flag=true;
		$.ajax({  
	         type : "post",  
	          url : "/vst_order/book/ajax/checkPromotionList.do",  
	          data: $("form[name='orderForm']").serialize(),
	          dataType: "json",
	          async : false,  
	          success : function(data){  
	        	  if(data.success){
	        			if(data.attributes.pomMessage!=""){
	    					VST.order.route.calcPriceInfo();
	    					pandora.dialog({
	    						content: "亲，您下手慢了<b>"+data.attributes.pomMessage+"</b>已抢光，如有用户未支付，您可继续参加此活动。",
	    						/*ok: function(){$('div.fk_box a.btn_fk').click();},
	    						cancel: true*/
	    						button: [{ 
	    							value: "继续", 
	    							className: "pbtn-orange", // 可选btn参数：默认白色 pbtn-pink btn-porange pbtn-blue 
	    							callback: function () {$('div.fk_box a.btn_fk').click();}
	    							},{ 
	    								value: "放弃", 
	    								className: "pbtn-orange", // 可选btn参数：默认白色 pbtn-pink btn-porange pbtn-blue 
	    								callback: function () {}
	    								}]
	    						});
	    					flag=false; 
	    				} 
	        	  }else{

						VST.order.route.calcPriceInfo();
						pandora.dialog({
							content: "亲，服务器走神了，促销校验失败，无法使用促销优惠，确认继续购买？",
							/*ok: function(){$('div.fk_box a.btn_fk').click();},
							cancel: true*/
							button: [{ 
								value: "继续", 
								className: "pbtn-orange", // 可选btn参数：默认白色 pbtn-pink btn-porange pbtn-blue 
								callback: function () {$('div.fk_box a.btn_fk').click();}
								},{ 
									value: "放弃", 
									className: "pbtn-orange", // 可选btn参数：默认白色 pbtn-pink btn-porange pbtn-blue 
									callback: function () {}
									}]
							});
						flag=false; 
					
	        	  }
				
	          }  
	     });
		return flag;
	}
	
	/**
	 * 计算优惠券使用金额
	 */
	function calCouponAmt(){
		var obj;
		var couponAmtTotal=0;
		var index=0;
		var couponLabelAmt = $("#totalOrderPriceDiv p").find(".couponLabelAmt");
		
		$("input[name='youhui_name']").parent().find(".hiddenCouponList").remove();
	    $("input[name='youhui_name']:checked").each(function(){
        	var hiddenInput=$("<input type='hidden' name='userCouponVoList["+index+"].couponCode' class='hiddenCouponList'>");
        	hiddenInput.val($(this).attr("couponCode"));
        	$(this).parent().append(hiddenInput);
        	index++;
		  if($(this).val()!=""&&$(this).val()!=null)
		  {
		   couponAmtTotal+=parseFloat($(this).val());
		  }
		  obj = $(this);
		});
		
		if(couponLabelAmt.length > 0){		  
		  var y = $("#showOughtPayPriceId").attr("value");
		  var coup = couponLabelAmt.attr("value");	  
		  
		  if(!$.isNumeric(coup)){
			  coup = 0.00;
		  }
		  if(!$.isNumeric(y)){
			  y = 0.00;
		  }
		  if(!$.isNumeric(couponAmtTotal)){
			 couponAmtTotal = 0.00;
		  }
		  
		  var price = (y*100)+parseFloat(coup);
		  
		 if(parseFloat(price)<parseFloat(couponAmtTotal)){			 
			 couponAmtTotal = 0.00;
			 couponLabelAmt.html(couponAmtTotal);//设置优惠券为0
			 $("#showOughtPayPriceId").attr("value",y).text(parseFloat(y).toFixed(2));
//			 var $browsers = $("input[name=youhui_name]"); 
//			 $browsers.attr("checked",false);
			 text = "优惠金额已超过应付金额！";
			 nameFull(obj,text,10,8,'warning');
			 $("#showOughtPayPriceId").attr("value",parseFloat(price/100).toFixed(2)).text(parseFloat(price/100).toFixed(2));//总价
			 return false;
		 }
		 
		var val = parseFloat((price/100).toFixed(2))-parseFloat((couponAmtTotal/100).toFixed(2));
		val = val.toFixed(2);
		$("#showOughtPayPriceId").attr("value",val).text(parseFloat(val).toFixed(2));//总价
		//优惠券
		couponLabelAmt.html(parseFloat((couponAmtTotal/100).toFixed(2)));
		couponLabelAmt.attr("value",couponAmtTotal);		
		};
	}
	
	/**
	 * 酒店使用优惠券
	 */
	function calHotelCouponAmt(){
		var obj;
		var couponAmtTotal=0;
		var index=0;
		var couponLabelAmt = $(".couponHotelAmt");		
		$("input[name='youhui_name']").parent().find(".hiddenCouponList").remove();
	    $("input[name='youhui_name']:checked").each(function(){
        	var hiddenInput=$("<input type='hidden' name='userCouponVoList["+index+"].couponCode' class='hiddenCouponList'>");
        	hiddenInput.val($(this).attr("couponCode"));
        	$(this).parent().append(hiddenInput);
        	index++;
		  if($(this).val()!=""&&$(this).val()!=null)
		  {
		   couponAmtTotal+=parseFloat($(this).val());
		  }
		  obj = $(this);
		});
	    
	    if(couponLabelAmt.length > 0){
	    	 var y = $(".mulPromotion").attr("value");//总金额
			  var coup = couponLabelAmt.attr("value");//酒店	  			  
			  if(!$.isNumeric(coup)){
				  coup = 0.00;
			  }
			  if(!$.isNumeric(y)){
				  y = 0.00;
			  }
			  if(!$.isNumeric(couponAmtTotal)){
				 couponAmtTotal = 0.00;
			  }
			  
			  var price = (y*100)+parseFloat(coup);
			if(parseFloat(price)<parseFloat(couponAmtTotal)){			 
				couponAmtTotal = y;
				couponLabelAmt.html(parseFloat(price/100));
			    couponLabelAmt.attr('value',parseFloat(price));			
			    text = "优惠金额已超过应付金额！";
			    nameFull(obj,text,10,8,'warning');
				 $("#showOughtPayPriceId").attr("value",0).text(0);
				return false;
			 }
		     
			var val = parseFloat((price/100).toFixed(2))-parseFloat((couponAmtTotal/100).toFixed(2));
			val = val.toFixed(2);
			$(".mulPromotion").attr("value",val).text(parseFloat(val).toFixed(2));//总价
			//优惠券
			couponLabelAmt.html(parseFloat((couponAmtTotal/100).toFixed(2)));
			couponLabelAmt.attr("value",couponAmtTotal);
		     
	    }
	    
	}
	
	
	//改变份数以后刷新优惠券
	$('#roomQuantity').live("change",function(){
		$('.freeCouponTbody').html('');
		$('.fixedCouponTbody').html('');		
		getLogionMessageInfo();
	});