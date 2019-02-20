$(document).ready(function(){
	loadOldInsurance();
	fireOldRemarkEvent();
});

function fireOldRemarkEvent(){
	$("textarea[name='remark']").trigger("change");
	$("textarea[name='faxMemo']").trigger("change");
	$("#expressInfoDiv").on("keydown",function(){
		if(typeof(Express)!="undefined" && Express!=null && Express.isFirstTime==true){
			if(window){
				window.console.log("取消一键下单反填事件！");
			}
			Express.isFirstTime=false;
		}
	});
}

/**
 * 加载保险
 */
function loadOldInsurance(){
	if($("table[data-type='insurance']").length==0){
		return;
	}
	if($("#originalOrderId").val()!=null && $("#originalOrderId").val()!=""){
		$.ajax({
			url:"/vst_order/order/orderManage/loadOrderInsurance.do",
			type:"POST",
			cache:false,
			data:{
				orderId:$("#originalOrderId").val()
			},
			dataType:"json",
			success : function(result){
				if(result.code=="success"){
					if(result.attributes!=null && result.attributes.insurance!=null){
						var indata=result.attributes.insurance;
						for (var int = 0; int < indata.length; int++) {
							var inselect=$("table[data-type='insurance']").find("select[name='itemMap["+indata[int].suppGoodsId+"].quantity']");
							if(inselect!=null){
								inselect.find("option[value='"+indata[int].quantity+"']").attr("selected","selected");
								inselect.change();
							}
						}
					}
				}
		   }
		});
	}
	if($("#originalOrderId").val()!=null && $("#originalOrderId").val()!=""){
		$(document).ajaxStop(function() {
			BACK.BOOK.CHECK.oneKeyOrderShowMessage=true;
			if(BACK.BOOK.CHECK.oneKeyOrderShowMessageData!=null){
				BACK.BOOK.CHECK.showAmountInfo(BACK.BOOK.CHECK.oneKeyOrderShowMessageData);
			}
		})
	}
}
