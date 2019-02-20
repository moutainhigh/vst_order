/*
 * 订单详情里发送短信业务JS
 * 
 */
 //定义发送短信弹出窗口变量
    var orderSendSmsDialog;
 $(function () {
 	
	//绑定发送短信事件
	$("#orderSendSms").bind("click",function(){
		orderSendSmsDialog = new xDialog("/vst_order/ord/order/intoSendSmsPage.do",{"orderId":orderId,"mobile":mobile},{title:"发送短信",width:600});
     });
 });
