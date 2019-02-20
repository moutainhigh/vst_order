// JavaScript Document
var yingfuBak=0;
$(function(){
		
	
	
	
	/*添加礼品卡*/
	$('.js_addcard').live('click',function(){ 
		$(this).hide().next('.dikou_box').show();
	});
	
	/*取消礼品卡*/
	$('.js_lpk_cancel').live('click',function(){ 
		$(this).parents('.lipinka_box').find('li').find('.input').removeClass('input_red').val('');
		$(this).parents('.youhui_info').hide().siblings('.youhui_tit').removeClass('info_show');
		$('.js_nameFull').remove();
	});
	
	
	/*取消储值卡*/
	$('.js_czk_cancel').live('click',function(){ 
		$(this).hide().find('.input').val('');
		$(this).parents('.lipinka_box').siblings('.btn_addcard').show();
	});
	

	
	
	
	
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
	

	
	
	
	

	
	
	//填写储值卡后，点击使用
	$("#storeCardBtn").live("click",function(){
		
		if($('.czk_table tr').length == 101){
			var  obj = $("#storeCardInputId");
			var text = '最多只能添加100张储值卡';
			nameFull(obj,text,10,8,'warning');
			return false;
		}
		
		var that = $(this);
		var storeCardInputId = $("#storeCardInputId").val();
		var isContainsChinese = checkContainsChinese(storeCardInputId);
		if(isContainsChinese){
			var  obj = $("#storeCardInputId");
			var text = '卡号不能包含汉字';
			nameFull(obj,text,10,8,'warning');
			return false;
		}
		//如果已经存在于表格中
		if(!validateCardNo(storeCardInputId,"0")){
			var  obj = $("#storeCardInputId");
			var text = '该卡已添加';
			nameFull(obj,text,10,8,'warning');
			return false;
		}else{
			$("#storeCardInputId").removeClass("input_red");
		}
		var checkStoreCardUrl = "/vst_front/book/checkStoreCard.do";
		//cardinfo={data:"",cardNo:"",leftAmt:"",status:"",cardType:"",expiredDate:"",bakword:""}
		if(storeCardInputId !=null && storeCardInputId!="" ){
			$.ajax({
				url:checkStoreCardUrl,
				data:{
					cardNo:storeCardInputId,
					passwd:"",
					type:"0"
				},
				async:false,
				method:"GET",
				dataType:"json",
				success:function(retstr){
					var validateCard= (retstr!=null && parseFloat(retstr.leftAmt)>0);

					if(validateCard){
						$("#storeCardInputId").removeClass("input_red");
						addCard('储值卡',storeCardInputId,retstr);
					}else{
						var  obj = $("#storeCardInputId");
						var text = '卡已激活,已出库,未过期且有余额才可以使用,请核对卡状态';
						nameFull(obj,text,10,8,'warning');
					}
				},
				error:function(){
					//请求失败
				},complete:function(){
					reloadLpkPicCode();
				}
			});
		}else{
			var  obj = $("#storeCardInputId");
			var text = '请输入卡号';
			nameFull(obj,text,10,8,'warning');
		}
		
	});


	//使用储值卡事件
	$(".czk_table").delegate(".czk_conf_clazz","click",function(){
		//应付款
		var thisCardNo = $(this).attr("data-cardno");
		//如果有这个类，那么说明是修改，而不是第一次加，如果是修改那么应付的话就得先加上这个值。 
		var isnotfirst = $(this).hasClass("card_notfirst");
		var origiYingfuAmt = 0;
		origiYingfuAmt = isnotfirst ? yingfuBak : 0;
		var yingfuAmt=parseInt(getAmountPrice()) + parseInt(origiYingfuAmt);
		var shiyongInput = $("#czk_input_amt_"+thisCardNo);
		/*var shiyongAmt=shiyongInput.val();
		if(!checkLpkCzkUseAmt(0,thisCardNo,shiyongAmt)){
			return false;
		}
		//将使用金额转换成分
		shiyongAmt = shiyongAmt * 100;*/
		
		var leftAmt=$(this).attr("data-leftamt");
		var shiyongAmt=leftAmt;
		var sjAmt = useAmt(shiyongAmt,yingfuAmt,leftAmt);
		shiyongInput.val(sjAmt);
		
		
		if(false){}
		else{
			
			var cardno = $(this).attr("data-cardno");
			var leftamt = $(this).attr("data-leftamt");
			var status = $(this).attr("data-status");
			var cardtype = $(this).attr("data-cardtype");
			var expireddate = $(this).attr("data-expireddate");
			
			
			var cardinfostr = " data-cardno=\""+cardno+"\" data-leftAmt=\""+leftamt+"\""
							+" data-status=\""+status+"\""
							+" data-cardtype=\""+cardtype+"\""
							+" data-expireddate=\""+expireddate+"\""
							+" "; 
			
			var yong_box = $(this).parents('tr').find('.yong_price');
			var yong_price = sjAmt/100;
			yong_box.html('￥'+yong_price).attr('yong_price',yong_price);
			var hiddenUseAmtInput = $(this).parents('tr').find(':hidden').eq(2);
			hiddenUseAmtInput.val(sjAmt);
			$(this).parent().html('<a class="card_gai"  '+cardinfostr+'  href="javascript:;" hidefocus="false">已经使用</a> <a class="card_del"  href="javascript:;" hidefocus="false">取消</a>');
			
		}
		//计算应付金额
		calcShouldAmt(thisProCategoryId);
		
	});
	
	//修改使用金额
	/*$('.card_gai').live('click',function(){ 
		
		
		var cardno = $(this).attr("data-cardno");
		var leftamt = $(this).attr("data-leftamt");
		var status = $(this).attr("data-status");
		var cardtype = $(this).attr("data-cardtype");
		var expireddate = $(this).attr("data-expireddate");
		
		var cardinfostr = " data-cardno=\""+cardno+"\" data-leftAmt=\""+leftamt+"\""
						+" data-status=\""+status+"\""
						+" data-cardtype=\""+cardtype+"\""
						+" data-expireddate=\""+expireddate+"\""
						+" "; 
		var confBtnId = "";
		var cancBtnId = ""
		var confclazz = "";
		var canclazz="";
		var inputAmtId="";
		
		var cardNo = cardno;
		var cardName="";
		if(cardtype==0){
			cardName = "储值卡";
		}
		if(cardName=='储值卡'){
			confBtnId="czk_confid_"+cardNo;
			cancBtnId="czk_cancid_"+cardNo;
			confclazz="czk_conf_clazz";
			canclazz="czk_canc_clazz";
			inputAmtId="czk_input_amt_"+cardNo;
		}else{
			confBtnId="lpk_confid_"+cardNo;
			cancBtnId="lpk_cancid_"+cardNo;
			confclazz="lpk_clazz_conf";
			canclazz="lpk_clazz_canc";
			inputAmtId="lpk_input_amt_"+cardNo;
		}
		
		var yong_box = $(this).parents('tr').find('.yong_price');
		var yong_price = yong_box.attr('yong_price');
		var hiddenUseAmtInput = $(this).parents('tr').find(':hidden').eq(2);
		yingfuBak = yong_price * 100;
		hiddenUseAmtInput.val(yong_price*100);
		yong_box.html('<input class="input" id="'+inputAmtId+'" value="'+yong_price+'" type="text">元');

		var newConfEle = '<span  '
					+cardinfostr
					+'  class="btn btn-mini btn-orange js_card_ok card_notfirst '+confclazz+'" id="'+confBtnId+'">确认</span>'
					+' <span class="btn btn-mini btn-default js_card_cancel  '+canclazz+'" id="'+cancBtnId+'">取消</span>';
					
		$(this).parent().html(newConfEle);
	})*/
	
	//取消使用金额
	$('.js_card_cancel').live('click',function(){ 
		removeTips();
		var yong_box = $(this).parents('tr').find('.yong_price');
		var yong_price = yong_box.attr('yong_price');
		yong_box.html(yong_price+'元');
		
		var id = $(this).attr("id");
		var t = id.indexOf("czk")>=0 ? "0" :"1";
		var cardNum = "";
		var that = null;
		if(t==0){
			//储值卡
			cardNum = id.substring("czk_cancid_".length);
			that = $("#czk_confid_"+cardNum);
		}else{
			cardNum = id.substring("lpk_cancid_".length);
			that = $("#lpk_confid_"+cardNum);
		}
		var cardno = $(that).attr("data-cardno");
		var leftamt = $(that).attr("data-leftamt");
		var status = $(that).attr("data-status");
		var cardtype = $(that).attr("data-cardtype");
		var expireddate = $(that).attr("data-expireddate");
		
		var cardinfostr = " data-cardno=\""+cardno+"\" data-leftAmt=\""+leftamt+"\""
						+" data-status=\""+status+"\""
						+" data-cardtype=\""+cardtype+"\""
						+" data-expireddate=\""+expireddate+"\""
						+" "; 
		
		$(this).parent().html('<a class="card_gai"  '+cardinfostr+'  href="javascript:;" hidefocus="false">修改</a> <a class="card_del" href="javascript:;" hidefocus="false">删除</a>')
		//计算应付金额
		calcShouldAmt(thisProCategoryId);
	})
	
	//删除卡
	$('.card_del').live('click',function(){ 
		if($(this).parents('.youhui_table').find('tr').length<3){
			$(this).parents('.youhui_table').html('');
			//计算应付金额
			calcShouldAmt(thisProCategoryId);
			return false;
		}
		//将该行的 以下行重新排序
		var trParent = $(this).parents('tr');
		var trSublings = null;
		var firstHiddenInput = trParent.find(":hidden").eq(0);
		var firstHiddenInputName = firstHiddenInput.attr("name");
		var thisType = firstHiddenInputName.indexOf("store")>=0 ? "0":"1";
		var thisIndex = firstHiddenInputName.substring(firstHiddenInputName.indexOf("[")+1,firstHiddenInputName.lastIndexOf("]"));
		var subLength = thisIndex;
		if(thisType==0){
			trSublings = $(this).parents('.czk_table').find("tr"); 
			
		}else{
			trSublings = $(this).parents('.lpk_table').find("tr");
		}
		subLength = trSublings.length;
		//当该行下面还有行，则处理，否则直接将改行删除
		if((subLength-2) > thisIndex){
			for(var i=subLength-2 ;i>thisIndex;i--){
				var sublingsTr = trSublings.eq(i+1);
				var hiddenInputs = sublingsTr.find(":hidden");
				for(var j=0;j<hiddenInputs.length;j++){
					var hiddenInput = hiddenInputs[j];
					var oldName = $(hiddenInput).attr("name");
					var seq = oldName.substring(oldName.indexOf("[")+1,oldName.lastIndexOf("]"));
					var newName = oldName.substring(0,oldName.indexOf("[") +1 )+(seq-1)+oldName.substring(oldName.indexOf("]"));
					//console.log(oldName.substring(0,oldName.indexOf("[") +1 )+(seq-1)+oldName.substring(oldName.indexOf("]")) );
					$(hiddenInput).attr("name",newName);
					
				}
			}
		}
		
		trParent.remove();
		//计算应付金额
		calcShouldAmt(thisProCategoryId);
	});
	//20150713 add by wanghuiwu 不同优惠券类型不能复选 STA
	$('.js_check_tips').live('click',function(){ 
//			var text = '卡号输入有误';
//			nameFull($(this),text,10,8,'warning');
		//$('.fixedCouponTbody').find(':radio:checked,:checkbox:checked').attr('checked',false);
	});
	
	$('.fixedCouponTbody').find("input[name='youhui_name']").live("change",function(){		
		$('.freeCouponTbody').find("input[name='youhui_name']").attr('checked',false);
		if($(this).attr('type')!='checkbox'){
			$(this).attr("checked","checked");
		}
		calcShouldAmt(thisProCategoryId);
	});
	
	$('.freeCouponTbody').find("input[name='youhui_name']").live("change",function(){
		if($('.fixedCouponTbody').find("input[name='youhui_name']:checked").size()>0)
			{
			$('.fixedCouponTbody').find("input[name='youhui_name']:checked").attr('checked',false);
			}
		calcShouldAmt(thisProCategoryId);
	});
	//20150713 add by wanghuiwu 不同优惠券类型不能复选 END

	//填写礼品卡后，点击使用
	$("#lpstoreCardBtn").live("click",function(){
		
		var that = $(this);
		var storeCardInputId = $("#lpCardInputId").val();
		
		var isContainsChinese = checkContainsChinese(storeCardInputId);
		if(isContainsChinese){
			var  obj = $("#lpCardInputId");
			var text = '卡号不能包含汉字';
			nameFull(obj,text,10,8,'warning');
			return false;
		}
		
		//如果已经存在于表格中
		if(!validateCardNo(storeCardInputId,"1")){
			var  obj = $("#lpCardInputId");
			var text = '该卡已添加';
			nameFull(obj,text,10,8,'warning');
			return false;
		}else{
			$("#lpCardInputId").removeClass("input_red");
		}
		
		//如果还没有来的及验证图片的话，需要先验证图片
		var picHasChecked =  $("#lpk_pic_checkCode").attr("disabled");
		if(picHasChecked != "disabled"){
			checkPicCode();
		}
		
		var isPicCode = $("#lpk_checkCodeValid").val();
		if(isPicCode == 0){
			var  obj = $("#lpk_pic_checkCode");
			var text = '验证码验证失败';
			nameFull(obj,text,10,8,'warning');
			return false;
		}
		
		var checkStoreCardUrl = "/vst_front/book/checkStoreCard.do";
		//cardinfo={data:"",cardNo:"",leftAmt:"",status:"",cardType:"",expiredDate:"",bakword:""}
		var passwd = $("#lpCardInputpwd").val();
		if(storeCardInputId !=null && storeCardInputId!="" ){
			$.ajax({
				url:checkStoreCardUrl,
				data:{
					cardNo:storeCardInputId,
					passwd:passwd,
					type:"1"
				},
				async:false,
				method:"GET",
				dataType:"json",
				success:function(retstr){
					var validateCard= (retstr!=null && parseFloat(retstr.leftAmt)>0 && retstr.status+"" == "1");
					if(retstr!=null && parseFloat(retstr.leftAmt) <= 0){
						var  obj = $("#lpCardInputId");
						var text = '该卡内余额不足（小于等于0元）,请输入其他卡号';
						nameFull(obj,text,10,8,'warning');
						return false;
					}
					if(validateCard){
						$("#lpCardInputId").removeClass("input_red");
						addCard('礼品卡',storeCardInputId,retstr);
						$("#createCheckCodeLPKHref").trigger("click");
					}else{
						var  obj = $("#lpCardInputId");
						var text = '卡号或密码输入有误';
						if(retstr!=null && retstr.bakWord !="" && retstr.bakWord+"" != "null" ){
							text = retstr.bakWord;
						}
						nameFull(obj,text,10,8,'warning');
					}
				},
				error:function(){
					//请求失败
				},complete:function(){
					reloadLpkPicCode();
				}
			});
		}else{
			var  obj = $("#lpCardInputId");
			var text = '请输入卡号';
			nameFull(obj,text,10,8,'warning');
		}
	});
	
	//换一张图片
	$("#createCheckCodeLPKHref").live("click",reloadLpkPicCode);
	//验证码 验证事件
	$("#lpk_pic_checkCode").one("click",initLpkPic);
	$("#lpk_pic_checkCode").blur(checkPicCode);
	
	
	//使用礼品卡事件
	$(".lpk_table").delegate(".lpk_clazz_conf","click",function(){
		
		if($('.lpk_table tr').length == 101){
			var  obj = $("#lpCardInputId");
			var text = '最多只能添加100张礼品卡';
			nameFull(obj,text,10,8,'warning');
			return false;
		}


		//应付款
		var thisCardNo = $(this).attr("data-cardno");
		var isnotfirst = $(this).hasClass("card_notfirst");
		var origiYingfuAmt = 0;
		origiYingfuAmt = isnotfirst ? yingfuBak : 0;
		var yingfuAmt=parseInt(getAmountPrice()) + parseInt(origiYingfuAmt);
		var shiyongInput = $("#lpk_input_amt_"+thisCardNo);
	/*	var shiyongAmt=shiyongInput.val();
		
		if(!checkLpkCzkUseAmt(1,thisCardNo,shiyongAmt)){
			return false;
		}
		
		//将使用金额转换成分
		shiyongAmt = shiyongAmt * 100;*/
		
		var leftAmt=$(this).attr("data-leftamt");
		var shiyongAmt=leftAmt;
		var sjAmt = useAmt(shiyongAmt,yingfuAmt,leftAmt);
		shiyongInput.val(sjAmt);
		
		if(false){}
		else{
			
			var cardno = $(this).attr("data-cardno");
			var leftamt = $(this).attr("data-leftamt");
			var status = $(this).attr("data-status");
			var cardtype = $(this).attr("data-cardtype");
			var expireddate = $(this).attr("data-expireddate");
			
			
			var cardinfostr = " data-cardno=\""+cardno+"\" data-leftAmt=\""+leftamt+"\""
							+" data-status=\""+status+"\""
							+" data-cardtype=\""+cardtype+"\""
							+" data-expireddate=\""+expireddate+"\""
							+" "; 
			
			var yong_box = $(this).parents('tr').find('.yong_price');
			var yong_price = sjAmt/100;
			yong_box.html('￥'+yong_price).attr('yong_price',yong_price);
			var hiddenUseAmtInput = $(this).parents('tr').find(':hidden').eq(2);
			hiddenUseAmtInput.val(sjAmt);
			$(this).parent().html('<a class="card_gai"  '+cardinfostr+'  href="javascript:;" hidefocus="false">已经使用</a> <a class="card_del"  href="javascript:;" hidefocus="false">删除</a>');
			
		}
		//计算应付金额
		calcShouldAmt(thisProCategoryId);
		
	});



});

$(".BounsPayLi").show();

//获取用户奖金账户余额以及该用户绑定的优惠券
getLogionMessageInfo();

//输入奖金金额
$('#input_bonus').keyup(function(event){
	var text = $(this).val();
	if($.trim(text)==''){
		$(this).parent().siblings('.btn-orange').removeClass('btn_stop');
		return;
	}
	var re = /^-\d+\.?\d*$/;
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
	if(Number(key)>Number(zhanghu) || re.test(text)){
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
		//奖金代扣
		if(parseFloat(price)<0){
			var  obj = $("#input_bonus");
			var text = '使用的奖金不能为负数，请输入正确奖金';
			nameFull(obj,text,10,8,'warning');
			return false;
		}
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
	    quxiaoBonus(parseFloat(bonusAmt));
	    
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

	//卡号和类型  已经存在返回false
	function validateCardNo(cardNo,type){
		var ret = true;
	
		if(type == 0){
			//储值卡
			if($('.czk_table tr').length>1){
				var cardNos = [];
				$(".czk_table tr").each(function (i) { 
					if(i>0){ 
						var txt = $(this).find("td:first-child").text();
						cardNos.push(txt);
					} 
				});
				
				for(var i=0;i<cardNos.length;i++){
					if(cardNos[i] == cardNo){
						ret = false;
						break;
					}
				}
			}
		}else{
			//礼品卡
			if($('.lpk_table tr').length>1){
				var cardNos = [];
				$(".lpk_table tr").each(function (i) { 
					if(i>0){ 
						var txt = $(this).find("td:first-child").text();
						cardNos.push(txt);
					} 
				});
				
				for(var i=0;i<cardNos.length;i++){
					if(cardNos[i] == cardNo){
						ret = false;
						break;
					}
				}
			}
		}
		return ret ;
	}
	//所有校验都成功了才过来添加卡
	function addCard(cardName,cardNo,card){
		var cardPrice = card.leftAmt;
		
		var cardno =card.cardNo;
		var leftamt = card.leftAmt;
		var status = card.status;
		var cardtype = card.cardType;
		var expireddate = card.expiredDate;
		var p = card.data;
		
		var cardinfostr = " data-cardno=\""+cardno+"\" data-leftAmt=\""+leftamt+"\""
						+" data-status=\""+status+"\""
						+" data-cardtype=\""+cardtype+"\""
						+" data-expireddate=\""+expireddate+"\""
						+" ";  
		var confBtnId = "";
		var cancBtnId = ""
		var confclazz = "";
		var canclazz="";
		var inputAmtId="";
		
		
		var index = cardName=='储值卡' ? $('.czk_table tr').length: $('.lpk_table tr').length;
		
		//tr 个数 -1 因为有标题行  
		if(index ==0 ){
			index = 0;
		}else{
			index = index -1;
		}
		//czk:storeCardList lpk:giftCardList  下单参数
		var cardNumName = "";
		var cardLeftAmtName = "";
		var cardUseAmtName = "";
		var cardPasswdName = "";
		var clz = "";
		if(cardName=='储值卡'){
			confBtnId="czk_confid_"+cardNo;
			cancBtnId="czk_cancid_"+cardNo;
			confclazz="czk_conf_clazz";
			canclazz="czk_canc_clazz";
			inputAmtId="czk_input_amt_"+cardNo;
			
			cardNumName = "storeCardList["+index+"].cardNo";
			cardLeftAmtName = "storeCardList["+index+"].leftAmt";
			cardUseAmtName = "storeCardList["+index+"].useAmt";
			cardPasswdName = "storeCardList["+index+"].passWd";
			clz = "storeCardAmt";
		}else{
			confBtnId="lpk_confid_"+cardNo;
			cancBtnId="lpk_cancid_"+cardNo;
			confclazz="lpk_clazz_conf";
			canclazz="lpk_clazz_canc";
			inputAmtId="lpk_input_amt_"+cardNo;
			
			cardNumName = "giftCardList["+index+"].cardNo";
			cardLeftAmtName = "giftCardList["+index+"].leftAmt";
			cardUseAmtName = "giftCardList["+index+"].useAmt";
			cardPasswdName = "giftCardList["+index+"].passWd";
			clz = "giftCardAmt";
		}
		
		
		var cardTit = '<tr><th width="25%">'+cardName+'号</th><th width="25%">剩余金额</th><th width="25%">本次使用</th><th width="25%">　</th></tr>';
		var cardHtml ='<tr><td>'+cardNo+'</td><td>'+cardPrice/100+'元</td>'
		
					+'<input type="hidden" name="'+cardNumName+'" value="'+cardno+'" /> '
					+'<input type="hidden" name="'+cardLeftAmtName+'" value="'+leftamt+'" />'
					+'<input type="hidden" class="'+clz+'" name="'+cardUseAmtName+'" value="0" />'
					+'<input type="hidden" name="'+cardPasswdName+'" value="'+p+'" />'
					
					+'<td><div class="yong_price" yong_price="0"><span class="input"  id="'+inputAmtId+'" type="text" value="￥0.00"  disabled="disabled"/></div></td>'
					+'<td><span  '+cardinfostr
					+'  class="btn btn-mini btn-orange js_card_ok  '+confclazz+'" id="'+confBtnId+'">使用</span>'
					+' <span class="btn btn-mini btn-default card_del  '+canclazz+'" id="'+cancBtnId+'">删除</span></td></tr>';
		if(cardName=='储值卡'){
			if($('.czk_table tr').length==0){
				$('.czk_table').append(cardTit+cardHtml);
			}else{
				$('.czk_table').append(cardHtml);
			}
			$("#storeCardInputId").val("");
		}else if(cardName=='礼品卡'){
			if($('.lpk_table tr').length==0){
				$('.lpk_table').append(cardTit+cardHtml);
			}else{
				$('.lpk_table').append(cardHtml);
			}
			$("#lpCardInputId").val("");
			$("#lpCardInputpwd").val("");
			$("#lpk_pic_checkCode").val("");
		}
	}
	
	function reloadLpkPicCode() {  
		var page_picSrc = rootPath + "/pic/initPic";
		document.getElementById("lpk_createCheckCode").src = page_picSrc+"?nocache=" + new Date().getTime();
		$("#lpk_checkCodeValid").val(0);
		$("#lpk_pic_checkCode").removeClass("input_red");
		$("#lpk_pic_checkCode").removeAttr("disabled");
	}
	
	function checkPicCode(){
		var inputCode = $("#lpk_pic_checkCode").val();
		if(inputCode==""|| isNaN(inputCode)){
			$("#lpk_checkCodeValid").val(0);
			return false;
		}
		var page_picSrc = rootPath + "/pic/checkPic";
		$.ajax({
			url: page_picSrc,
			async:false,
			data: {pic_checkCode:inputCode},
			success: function(result){
				if(result=='success'){
					$("#lpk_pic_checkCode").removeClass("input_red");
					$("#lpk_pic_checkCode").attr("disabled", "true");
					$("#lpk_checkCodeValid").val(1);
				}else{
					$("#lpk_pic_checkCode").removeAttr("disabled");
					$("#lpk_checkCodeValid").val(0);
				}
			}
		});
	}

	
	
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
		var bonusLabelAmt = $(".bonusHotelAmt").text(_bonusAmt+"");
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
		
//		switch(categoryNo+""){
//			case '14':
//			case '15':
//			case '16':
//			case '17':
//			case '18':
//				//路
//				lineBackCountPrice();
//				break;
//			case '4':
//				//签证
//				VST.order.visa.calcPriceInfo();
//				break;
//			case '5':
//			case '11':
//			case '12':
//			case '13':
//				//门票
//				BACK.BOOK.CHECK.checkStock();
//				break;
//		}
		calCouponAmt();
		displayBonus();
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
			nameFull(obj,msg,10,8,'warning');
			return false;
		}
		var isload = true;
		//将优惠券的值和已经查询出来的值想比较，判断是否重复
		$('.freeCouponTbody tr').each(function(){
			var coupCode = $(this).find("td").eq(1).html();
			if($.trim(coupCode) == $.trim(objVal)){
	   			var msg = '该优惠券兑换码已经添加，请输入其他兑换码';
	   			isload = false;
       			nameFull(obj,msg,10,8,'warning');
			}
		});
		$('.fixedCouponTbody tr').each(function(){
			var coupCode = $(this).find("td").eq(1).html();
			if($.trim(coupCode) == $.trim(objVal)){
	   			var msg = '该优惠券兑换码已经添加，请输入其他兑换码';
	   			isload = false;
       			nameFull(obj,msg,10,8,'warning');
			}
		});
		 
		var $form = $("form[name='orderForm']");
		if($form.length==0){
			$form = $("form[name='holdBookInfoForm']");
		}
		var notInsertCouponFlag=$("#notInsertCouponFlag").val();//如果库存为空 则不请求优惠券的标志值为 Y 不去查询优惠券
		if(isload&&notInsertCouponFlag!="Y"){
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
		       		nameFull(obj,errMsg,10,8,'warning');
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
		       			  var couponNameTd=$("<td width='40'></td>");
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
		       			  	 // couponNameTdLable.append(item.couponNameStr);
		       			  	  couponNameTdLable.attr("title",item.couponName);
		       			  	  couponNameTd.append(couponNameTdLable);
		       			  	var couponNoTd=$("<td class='counpCode'  style='display:none;'></td>");
		       			  	  couponNoTd.html(item.couponCode);
		       			  	var couponPriceTd=$("<td width='120'></td>");
			       			  var couponPriceSpan=$("<span class='yh_price'></span>");
			       			  var couponPriceDfn=$("<dfn>￥</dfn>");
			       			  	  couponPriceSpan.append(couponPriceDfn);
			       			  	  couponPriceSpan.append((item.discountAmount/100).toFixed(2));
			       			  	  couponPriceTd.html(couponPriceSpan);
			       			   var couponCoupNameTd=$("<td width='260'></td>");
			       		         	couponCoupNameTd.append(item.couponNameStr+"<span class='tag_tips'>New</span>");
			       			 var ticketTd=$("<td width='110'></td>");
			       			 if(item.favorType=="0"){
			       			     ticketTd.append("现金券");
			       			     }else{
			       			     ticketTd.append("满减券") ;
			       			     }
			       			     
		       			  var couponExpTd=$("<td ></td>");
		       			   var itemDate="有效期"+item.expiredDate.substr(10,11);
	       			  	     couponExpTd.html(itemDate);
		       			  	 
		       			  	  couponTr.append(couponNameTd);
		       			  	couponTr.append(couponNoTd);
		       			    	couponTr.append(couponPriceTd);
		       				  
		       				  couponTr.append(couponCoupNameTd);
		       				couponTr.append(ticketTd);
		       				  couponTr.append(couponExpTd);
		       				  if("0"==item.favorType){
		       					 fixedCouponTbody.append(couponTr);
			        			}else{
			        			 freeCouponTbody.append(couponTr);
			        			 }
		       				  		        	  
		       			  }else{
		       			    text = item.validInfo;
		       				nameFull(obj,text,10,8,'warning');
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
		       			}	       			
		       			nameFull(obj,text,10,8,'warning');
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
			var visoQuantity = $(".mainSum").val();
			if(parseInt(checkNum)>parseInt(visoQuantity)){
				   $(this).attr("checked", false);
				   
				   var objthis = $(this);
				   textMsg ="使用优惠券数目不能大于购买数量"; 
				   nameFull(objthis,textMsg,10,8,'warning');
				   return;
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
		/** 查看商品是否是能够使用优惠券的商品* */
		var notInsertCouponFlag=$("#notInsertCouponFlag").val();//如果库存为空 则不请求优惠券的标志值为 Y 不去查询优惠券
		if(notInsertCouponFlag!="Y"){
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
					}
			   }
			});
		   /**
   	  	 * 自动匹配优惠券
   	  	 */
      var mul=[];
  	  var single=[];
      var mulNum=0;
      var singleNum=0;
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
                 var greaterThanMaxCouponAccount=data.attributes.greaterThanMaxCouponAccount;

		       	  //奖金余额
		       	  var bonusBalance=data.attributes.bonusBalance;
		       	  
//		       	  if(bonusBalance == null || bonusBalance == "" || bonusBalance <=0){
//		       		  bonusBalance = 9999;
//		       	  }
		       	  
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
			   	 /**清除已绑定的账户的信息**/
		 	   	clearPageCouponMessage();
		       	 //循环遍历从后台获取的账户绑定优惠券信息
		 	      var inum=0;
		       	  if(myCouponList){
		       		  $.each(myCouponList, function(k,item){
		       			  inum++;
		       			  var couponTr;
		       				  couponTr=$("<tr class='loginInsert'></tr>");
		       			  var couponNameTd=$("<td width='40' ></td>");
		       			  var couponNameTdLable=$("<label></label>");
		       			  var couponInput;
		       			  	  if(couponTypeFlag&&couponTypeFlag=="true" && "0"==item.favorType){
		       			  		couponInput=$("<input class='js_check_tips' type='checkbox' tips_L='true' name='youhui_name' />");
		       			  	    mul.push(item);
		       			  	  }else{
			        			couponInput=$("<input class='js_check_tips'  type='radio' tips_L='true' name='youhui_name'/>");
			        			single.push(item);
		       			  	  }
		       			  	  couponInput.val(item.discountAmount);
		       			  	  couponInput.attr("couponCode",item.couponCode);
		       			  	  couponNameTdLable.append(couponInput);
		       			  	 // couponNameTdLable.append(item.couponNameStr);
		       			  	  couponNameTdLable.attr("title",item.couponName);
		       			  	  couponNameTd.append(couponNameTdLable);
		       			  	var couponNoTd=$("<td class='counpCode'  style='display:none;'></td>");
		       			  	  couponNoTd.html(item.couponCode);
		       			  var couponPriceTd=$("<td width='120' ></td>");
		       			      
		       			  var couponPriceSpan=$("<span class='yh_price'></span>");
		       			  var couponPriceDfn=$("<dfn>￥</dfn>");
		       			  	  couponPriceSpan.append(couponPriceDfn);
		       			  	  couponPriceSpan.append((item.discountAmount/100).toFixed(2));
		       			  	  couponPriceTd.html(couponPriceSpan);
		       			 var couponCoupNameTd=$("<td width='260'></td>");
		       			     couponCoupNameTd.append(item.couponNameStr);
		       			 var ticketTd=$("<td width='110'></td>");
		       			 if(item.favorType=="0"){
		       			     ticketTd.append("现金券");
		       			     }else{
		       			     ticketTd.append("满减券") ;
		       			     }
		       			     
		       			  var couponExpTd=$("<td ></td>");
		       			  if(item.expiredDate!=""){
		       				  var itemDate="有效期"+item.expiredDate.substr(10,11);
		       			  	  couponExpTd.html(itemDate);
		       			  }
		       			  	  couponTr.append(couponNameTd);
		       				  couponTr.append(couponNoTd);
		       				  couponTr.append(couponPriceTd);
		       				  couponTr.append(couponCoupNameTd);
		       				  couponTr.append(ticketTd);
		       				  couponTr.append(couponExpTd);
		       				  if("0"==item.favorType){
		       					  	fixedCouponTbody.append(couponTr);
			        			  	  }else{
			        			  		freeCouponTbody.append(couponTr);
			        			  	  }
		       				  
			        	  }); 
		       		  
		       		 //计算最多可以使用多少张优惠券
			       		var quantity = $(".youhuiQuantity").val();
			       		var personNum =$(".youhuiperson").val();
			       		var saleType = $('.productSaleType').val();
			       		var useNum=0;
			       		if(thisProCategoryId =='4'){
			    			var visoQuantity =$(".mainSum").val();
			    			useNum=parseInt(visoQuantity);
			    			
			    		}
			    		//按份
			    		if(saleType == 'COPIES')
			    		{
			    		  if(thisProCategoryId !=4 ){			   
			    			useNum=parseInt(quantity);
			    		  }
			    		}else
			    		{
			    			if(thisProCategoryId !=4 ){
			    		    useNum=parseInt(personNum);
			    			}
			    		}
			       		  
			       		  
			       		  
			       		  
			    		
			       		  
			       		 if(single.length>0){
			       			  var singleObject=single[0];
			       			  singleNum=single[0].discountAmount;
			       		  }
			       		 if(mul.length>0){
			       			 for(var i=0;i<mul.length;i++){
			       				 mulNum+=mul[i].discountAmount;
			       				 for (var j = 0; j < mul.length - i - 1; j++) {
			       					 if(mul[j].discountAmount==mul[j + 1].discountAmount){
			       						 var date1=new Date(mul[j].expiredDate.substr(11,10));
			       						 var date2=new Date(mul[j+1].expiredDate.substr(11,10));
			       						 if(Date.parse(date1)-Date.parse(date2)>0){
			       							var temp= mul[j];  
				       						 mul[j] = mul[j+1];  
				       						 mul[j+1] = temp;
			       						 }
			       					 
			       					 }
			       					 if (mul[j].discountAmount>mul[j + 1].discountAmount) {  
			       						 var temp= mul[j];  
			       						 mul[j] = mul[j+1];  
			       						 mul[j+1] = temp;  
			       						 }  
			       					 }  
			       				 }
			       			 }
			       		 if(mul.length>useNum){
			       			mulNum=0;
			       			for(var i=mul.length-useNum;i<mul.length;i++){
			       				mulNum+=mul[i].discountAmount;
			       			}
			       		 }
			       		 
			       		 
			       		 if(single.length>1){
			       		  for(var i=0;i<single.length-1;i++){
			       			//singleNum=single[i].discountAmount;
			       			var test=single[i+1].discountAmount;
			       			if(singleNum==test){
			       			    var date1=new Date(single[i].expiredDate.substr(11,10));
								 var date2=new Date(single[i+1].expiredDate.substr(11,10));
								 if(Date.parse(date1)-Date.parse(date2)>0){
									var temp= single[i];  
									single[i] = single[i+1];  
									single[i+1] = temp;
								 }
			       				
			       			}
			       			if(singleNum<test){
			       				singleNum=test;
			       				singleObject=single[i+1];
			       			}
			       		  }
			       		 }
			       		  //多选优惠大
			       		  if(mulNum>singleNum){
			       			 if(mulNum>0){
			       		    if(mul.length<useNum){
			       			 for(var i=0;i<mul.length;i++){
			       				$("input[couponCode="+mul[i].couponCode+"]").attr("checked",'checked'); 
				       		  }
			       		    }else{
			       		    	for(var i=mul.length-useNum;i<mul.length;i++){
			       		    		$("input[couponCode="+mul[i].couponCode+"]").attr("checked",'checked');
				       			}
			       		    	
			       		    }
			       			 }
			       			 
			       		  }else{
			       			  if(singleNum>0){
			       			   $("input[couponCode="+singleObject.couponCode+"]").attr("checked",'checked');
			       			  }
			       		  }
		       		 var str="<span class='tag_tips'>"+inum+"张</span>";
		       		 $("#countNums").empty();
		       		 $("#countNums").append(str);
		       		  var fixedNum=fixedCouponTbody.find("input[name='youhui_name']").size();
		       		  var freeNum=freeCouponTbody.find("input[name='youhui_name']").size();
		       		  if(fixedNum>0 || freeNum>0){
		       			  no_youhuiquanTr.hide();
		       		  }else{
		       			no_youhuiquanTr.show();
		       		  }
		       	  }
		       	  if(greaterThanMaxCouponAccount){
                      $("#coupon_tips").text("当前用户今天优惠券已使用了6次，优惠券不可用!")
		       	  	  $("#couponLi_a").hide();
                      $("#countNums").hide();
				  }
	         }  
	    });
		}
	}
	
function showAmountInfoNew(data){
	
	Express.disposeExpressPriceResult(data);
	var priceInfo = data.attributes.priceInfo;
	console.log(priceInfo);
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
	//isCanBoundLipinkaPay = false;
	isCanBoundLipinkaPay = isCanBoundLipinkaPay+"";
	if(isCanBoundLipinkaPay=="true"){
		if(lpkDIV!=null){
			lpkDIV.removeAttr("style");
		}
		if(czkDIV!=null){
		    czkDIV.removeAttr("style");
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
	if (priceInfo.promotionAmountYuan != '0.00'  && ( data.attributes.couponExclusion!=true || (data.attributes.couponExclusion==true && data.attributes.choosePromotion!="coupon" 	)  ) ) {
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
	        if(couponAmtTotal&&couponAmtTotal>0  && ( data.attributes.couponExclusion!=true || (data.attributes.couponExclusion==true && data.attributes.choosePromotion=="coupon" 	)  ) ){
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
			if(data.attributes.couponExclusion==true &&  data.attributes.choosePromotion=="coupon" ){
				oughtPay=priceInfo.goodsTotalPrice-disTotleAmt+priceInfo.expressPrice+priceInfo.insurancePrice;
			}
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
    $("input[name='choosePromotion']").die("change");
    showPromPromtion(data);
    $("input[name='choosePromotion']").live("change",function(){
        lineBackCountPrice();
    });
	  
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
//	if(shiyongAmt-leftAmt<0 && shiyongAmt-yingfuAmt>0){
//		sjAmt = yingfuAmt;
//	}else if(shiyongAmt-leftAmt>0&&shiyongAmt-yingfuAmt<0){
//		sjAmt = leftAmt;
//	}else if(shiyongAmt-leftAmt>0&&shiyongAmt-yingfuAmt>0){
//		sjAmt = yingfuAmt;
//	}else if(shiyongAmt-leftAmt<0&&shiyongAmt-yingfuAmt<0){
//		sjAmt = shiyongAmt;
//	}
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
			couponLabelAmt.html(parseFloat(couponAmtTotal/100));
			couponLabelAmt.attr('value',parseFloat(price));
			text = "优惠金额已超过应付金额！";
			nameFull(obj,text,10,8,'warning');
			$("#showOughtPayPriceId").attr("value",0).text(0);
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
		setTimeout(1000);
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
//			 var coup = couponLabelAmt.attr("value");//酒店	  			  
//			  if(!$.isNumeric(coup)){
//				  coup = 0.00;
//			  }
			  if(!$.isNumeric(y)){
				  y = 0.00;
			  }
			  if(!$.isNumeric(couponAmtTotal)){
				 couponAmtTotal = 0.00;
			  }
			  
			  var price = (y*100);
			  //优惠金额大于应付金额
			  if(parseFloat(price)<parseFloat(couponAmtTotal)){			 
				couponLabelAmt.html(parseFloat(couponAmtTotal/100));
				couponLabelAmt.attr('value',parseFloat(couponAmtTotal));
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
	$('.hotel_sum').live("change",function(){
		$('.freeCouponTbody').html('');
		$('.fixedCouponTbody').html('');		
		getLogionMessageInfo();
	});
	
	

