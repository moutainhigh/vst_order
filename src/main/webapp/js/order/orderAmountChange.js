$(function() {
	$("#changeAmount").bind("click",function(){
			var param="";
			if(ordType=='parent'){
				param="orderId="+orderId+"&orderType="+ordType;
			}else{
				param="orderId="+orderId+"&orderType="+ordType+"&orderItemId="+orderItemId;
			}
			
       		$.ajax({
				   url : "/vst_order/order/orderAmountChange/validateOrderAmountChange.do",
				   data : param,
				   type:"POST",
				   dataType:"JSON",
				   success : function(result)
				   {
				   		if(result.code=="success" )
				   		{
				   			showAmountDialog = new xDialog("/vst_order/order/orderAmountChange/showAddAmountChange.do",param,{title:"修改价格",width:500});
				   		}else {
				   			 alert(result.message);
				   		}
				   }
			});	   
	 });
});
