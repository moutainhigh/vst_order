<#include "/order/orderProductQuery/member_dialog.ftl"/>  
<#include "/base/foot.ftl"/>
<script type="text/javascript">var basePath = '${rc.contextPath}';</script>
<script src="/vst_order/js/book/pandora-calendar.js"></script>
<script src="/vst_order/js/tooltip/js/jtip.js"></script>
<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script>
<script src="/vst_order/js/book/bookCheck.js?version=2014091412345"></script>
<script src="/vst_order/js/book/order_promotion.js?version=2014091412345"></script>
<script src="/vst_order/js/book/coupon_allCategroy.js?version=2014091412345"></script>
<script src="/vst_order/js/book/ticket/bookTicket.js?version=2014091112345"></script>
<script src="/vst_order/js/book/express.js?2014091512345"></script>
<script type="text/javascript">
function loadJtip(){
		$('.J_tip').lvtip({
	        templete: 3,
	        place: 'bottom-left',
	        offsetX: 0,
	        trigger : "click",
	        events: "live" 
	    });
}
</script>
<script>
var orderInfoDialog;
var selectUserDialog;
var book_user_id='${user.userId}';
    BACK.BOOK.CHECK.checkStock();
    VST.TICKET.ORDER.refereshInsurance();
    VST.TICKET.ORDER.bindHotelSum();
    Express.showExpressageInfo();
    loadJtip();
	bookNumOnchangeCallback.pushFun(BACK.BOOK.CHECK.checkStock);
	bookNumOnchangeCallback.pushFun(Express.showExpressageInfo);
	calendarConfirmCallback.pushFun(VST.TICKET.ORDER.refereshTimePrice);
	calendarConfirmCallback.pushFun(VST.TICKET.ORDER.refereshInsurance);
	calendarConfirmCallback.pushFun(Express.showExpressageInfo);
	calendarConfirmCallback.pushFun(BACK.BOOK.CHECK.checkStock);
	// 提交订单
	$('#orderSubmitA').bind('click',function(){
	
		if($("[name='masterSuppGoodsId']").length > 0){
			var masterSuppGoodsId = $("[name='masterSuppGoodsId']").attr("value");
			if($("[goodsid = "+masterSuppGoodsId+"] option:selected").val() < 1){
				alert("该商品不能单独预订!");
				return;
			}
		}
		if(book_user_id==""){
	      	showQueryUserIdDialog();
	      	return;
	      }else{
	      	submitFormCallback.invoke();
	      }
	});
	
	function submitOrder(){
		if(VST.TICKET.ORDER.orderValidate()){
			var ticketCount=VST.TICKET.ORDER.calTicketPersonCount();
			var baoxianCount=VST.TICKET.ORDER.calbaoxianPersonCount();
			if(ticketCount<baoxianCount){
				$.alert("选择保险的份数不能超过游玩人数.");
				return;
			}
			//验证
			if(!$("form[name='orderForm']").validate().form()){
				return false;
			}
			var checkFlag=true;
			$('input[type=text]').each(function(){
				 var text=$(this).val();
				 var nameType=$(this).attr("name_type");
				 if(nameType=="fullName"){
				 	  var patt1 = new RegExp("^[a-zA-Z]{2,20}$");
				 	  var patt2 = new RegExp("^[\u4e00-\u9fa5]{1,10}$");
				 	 if(!patt1.test(text)&&!patt2.test(text)){
						//提示
						$(this).next("span").css("display","");
						checkFlag=false;
					}else{
						$(this).next("span").css("display","none");
					}
				 }else if(nameType=="mobile"){
				 	 var myreg = /^\d+$/;
    				if(!myreg.test(text)){
						//提示
						$(this).next("span").css("display","");
						checkFlag=false;
					}else{
						$(this).next("span").css("display","none");
					}
				 }else if(nameType=="youbian"){
			        var patt1= /^[1-9][0-9]{5}$/
			        if(text!=""){
				         if(!patt1.test(text)){
				           $(this).next("span").css("display","");
							checkFlag=false;
						}else
				        {
				          $(this).next("span").css("display","none");
				        }
			        }
				 }else if(nameType=="address"){
				 	var city2Code = $("#js_city2").find('option:selected').attr("value");
			        if($("#user_address").val().length>100||city2Code == "选择市"){
			           $(this).next("span").css("display","");
						checkFlag=false;
					}else
			        {
			          $(this).next("span").css("display","none");
			        }
				 }
			});
			if(!checkFlag){
					return;
			}
			if($('#orderSubmitA').attr('submitFlag')=="true"){
		      //生成订单开始
			  orderInfoDialog = new xDialog("/vst_order/ord/book/comm/createOrder.do",
							      $("form[name='orderForm']").serialize(),
							      {title:"订单信息填写",width:900});
			}
		}
	}
	submitFormCallback.pushFun(submitOrder);
</script>