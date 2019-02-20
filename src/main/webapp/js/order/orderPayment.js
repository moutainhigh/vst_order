/**
 * 
 * 订单支付业务
 * 
 * @author wenzhengtao
 * 
 */
$(function() {
	// 绑定查看支付记录事件
	$("#orderPaymentInfo").bind(
		"click",
		function() {
			$.post("/vst_order/ord/order/validateApprovingAmountChange.do",
					{orderId:orderId},
					function(data) {
						if(data.success) {
							if(data.attributes.valid) {
								orderPaymentInfoDialog = new xDialog(
										"/vst_order/ord/order/viewOrderPaymentInfo.do", {
											"orderId" : orderId
										}, {
											title : "支付信息列表",
											width : 1350
										});
							} else {
								alert("该订单或相关子订单存在尚未审核的价格修改申请，请处理完后再进行支付相关操作");
							}
						} else {
							alert(data.message);
						}
					});
		});
	
	$("#paymentTerm").click(function(){
		var paymentData = {"orderId":orderId,"oughtAmount":oughtAmount};
		paymentTermDialog = new xDialog("/vst_order/ord/order/editPaymentTerm.do",	paymentData,{title:"支付方式",width:"600"});
	});

});
