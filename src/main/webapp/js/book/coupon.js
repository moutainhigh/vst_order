$(function(){
	$("#couponVerify").click(function(){
         var code=$("#couponCode").val();
         if($.trim(code)==""){
        	 $("#couponInfoMsg").html("请输入优惠券代码.");
        	 $("#couponChecked").val("false");
        	 return;
         }
         /*if(!setAndsubmitOrder()){
           	return;
         }*/
         $("#couponInfoMsg").html("");
         $("#couponChecked").val("true");
         lineBackCheckStock();
	});
	$("#couponCode").change(function(){
        var code=$("#couponCode").val();
        if($.trim(code)==""){
       	 $("#couponChecked").val("false");
        }
	});
});

function couponVerify(){
	$.ajax(
	        {
	            type: "get",
	            async: false,
	            url: "/vst_order/ord/order/validateCoupon.do",
	            data: $("#baseDataForm,#ordForm,#touristForm,#contractForm,#remarkForm,#couponForm").serialize(),
	            dataType: "json",
	            success: function (data) {
	            	if(data.success){
	            		$("#couponInfoMsg").html("优惠券代码可用.");
	            		 lineBackCheckStock();
	            	}else{
	            		$("#couponInfoMsg").html(data.code);
	            		$("#couponChecked").val("false");
	            	}
	            }
	        }
	    );
}