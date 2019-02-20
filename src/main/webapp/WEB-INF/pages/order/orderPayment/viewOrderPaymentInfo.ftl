<div class="iframe-content">   
	<div class="p_box">
		<table class="p_table table_center">
		    <thead>
		        <tr>
		              <tr>
						<th nowrap="nowrap">交易流水号</th>
						<th nowrap="nowrap">网关交易号</th>
						<th nowrap="nowrap">支付网关</th>
						<th nowrap="nowrap">交易金额</th>
						<th nowrap="nowrap">立減金额</th>
                    	<th nowrap="nowrap">金额类型</th>
						<th nowrap="nowrap">支付状态</th>
						<th nowrap="nowrap">支付信息</th>
						<th nowrap="nowrap">交易时间</th>
						<th nowrap="nowrap">创建时间</th>
						<th nowrap="nowrap">对账流水号</th>
						<th nowrap="nowrap">原订单号</th>
						<th nowrap="nowrap">预授权状态</th>
                    	<th nowrap="nowrap">杉德交易流水号</th>
					</tr>
		        </tr>
		    </thead>
		    <tbody>
		    	<#list payAndPreVOList as result>
				    <tr>
					    <td>${result.serial!''}</td>
						<td>${result.gatewayTradeNo!''}</td>
						<td>${result.payWayZh!''}</td>
						<td>${result.amountYuan!''}</td>
						<td>${result.promotAmountYuan!''}</td>
						<td>${result.equityAmountDesc!''}</td>
						<td>${result.statusZh!''}</td>
						<td>${result.callbackInfo!''}</td>
						<td><#if result.callbackTime??>${result.callbackTime?string('yyyy-MM-dd HH:mm') !''}</#if></td>
						<td>${result.createTime?string('yyyy-MM-dd HH:mm') !''}</td>
						<td>${result.paymentTradeNo!''}</td>
						<td>${result.oriObjectId!''}</td>
						<td>${result.payPreStatusZh!''}</td>
                        <td>${result.sandOrderNumber!''}</td>
					</tr>
			   </#list>
			</tbody>
		</table>
		<br/>
		<div class="p_box box_info clearfix mb20" style="padding-left:30px;">
		    <div class="fl operate">
		    	 <#if order.normal && !order.hasPayed() && order.hasNeedPrepaid() && (order.hasInfoAndResourcePass() || isDestBuFront == 'Y')>
		    	 	<#if paymentInfoFlag == 'N'>
			    	 		<#if orderCategoryId != '5' && orderCategoryId != '11'>
	                 			<a class="btn btn_cc1" id="payment" >百付电话支付</a>
	                 			<a class="btn btn_cc1" id="yeepay" >易宝电话支付</a>
	                 	    </#if>
	                 	<a class="btn btn_cc1" id="btnLakala" >线下拉卡拉支付</a>
				    	<a class="btn btn_cc1" id="btnPayPos" onClick="openPosWindow('COMM_POS');">Pos机支付</a>
				    	<a class="btn btn_cc1" id="btnSandPayPos" onClick="openPosWindow('SAND_POS');">杉德Pos机支付</a>
			    	</#if>
               		<form onsubmit="return false"><input type="hidden" name="orderId" value="${order.orderId}"/><input type="text" name="oriOrderId"/><input type="button" value="关联订单转移资金" onclick="transferOrderAmount(this.form)" class="btn"/></form>
                </#if>
		    </div>
		</div>
	</div>
</div>
<script language="javascript">
	var postPayDialog;
	var paymentDialog;
	function openPosWindow(posType){
		window.open("/vst_order/ord/order/initPosPaymentRecord.do?orderId=${orderId}&posType="+posType,"POS机支付","height=240,width=400,top=200,left=400");
		orderPaymentInfoDialog.close();
	}
	
	$("#payment").bind("click",function(){
			window.open ("/vst_order/order/byPostdatePay/byPay.do?orderId=${orderId}", "百付电话支付","height=480, width=850,top=200, left=200");
			orderPaymentInfoDialog.close();
     });
     
     $("#yeepay").bind("click",function(){
			window.open ("/vst_order/order/yeepay/yeepayPhone.do?orderId=${orderId}", "易宝电话支付","height=480, width=850,top=200, left=200");
			orderPaymentInfoDialog.close();
     });
     
     $("#btnLakala").bind("click",function(){
     		$.ajax({
				type:"POST", 
				url:'/vst_order/ord/order/createLakalaPaymentUrl.do', 
				data:{orderId:${orderId}}, 
				async: false, 
				success:function (result) {
					if(!result.success){
				 		$.alert(result.message);
				 	}else{
						window.open ("http://pay.lvmama.com/payment/pay/lakala.do?"+result.attributes.params);
				 	}
				}
			});
     });
     
     function transferOrderAmount(form){
     	if(!confirm("确定需要转移订单资金到当前订单")){return false;}

     	 var nowOrderId = $("input[name='orderId']").val();
         var transferedOrderId = $("input[name='oriOrderId']").val();

     	  if(isEmpty(transferedOrderId)){
     	     alert("请输入需要转移的订单号");
     	     return;
		 }

     	//校验是否是已审核通过的资产转移订单
         $.get("/vst_order/order/depositRefund/checkTransferOrder/" + transferedOrderId + ".do", function (data) {
             if (data.code == 500) {
                 alert(data.msg);
                 return;
             }

             if(data.code == 502){
                 if(data.msg != nowOrderId){
                     alert("您所输入的订单号资金不能转移至本订单，请核对~");
                     return;
				 }
			 }

             $.post("/vst_order/ord/order/transferOrderAmount.do",$(form).serialize(),function(data){
                 if(data.success){
                     alert("转移操作已经提交，10秒刷新查看");
                 }else{
                     alert(data.message);
                 }
             },"JSON");

         });

     }

    function isEmpty(obj){
        if(typeof obj == "undefined" || obj == null || obj == ""){
            return true;
        }else{
            return false;
        }
    }

</script>
