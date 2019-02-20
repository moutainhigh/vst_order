<div>
    <div class="box_content p_line">
    	<form id="cancelOrderForm" action="/vst_order/ord/order/confirm/cancelOrder.do">
    		<input type="hidden" name="orderId" value="${order.orderId}" >
	         <table class="e_table form-inline ">
	            <tbody>
	            	<tr>
	            		<td>子订单号：</td>
	            		<td>${orderItemId}</td>
	            	</tr>
	            	<tr>
	            		<td>取消类型：</td>
	            		<td>
			            	<#list orderCancelTypeList as cancelType> 
							 <#if cancelType_index%2==0 >
								<br>
							 </#if>
							 	   <label class="radio">
							 	 	<#if order.orderStatus=="COMPLETE" || order.orderStatus=="CANCEL"> 
										<input type="radio" name="cancelCode" value="${cancelType.dictDefId}" data="cancelOP" disabled ="true"/> 
									<#else> 
									<#if cancelType.dictDefId==order.cancelCode > 
										<input type="radio" name="cancelCode" value="${cancelType.dictDefId}" data="cancelOP" onclick="orderCancelTypeChange(this)" checked="true"/> 
									<#else> 
										<input type="radio" name="cancelCode" value="${cancelType.dictDefId}" data="cancelOP" onclick="orderCancelTypeChange(this)" /> 
									</#if> 
									</#if>
							 	  	${cancelType.dictDefName}
							 	   </label>&nbsp;&nbsp;&nbsp;&nbsp;
							</#list>
						</td>
					</tr>
	                <tr>
	                    <td>取消原因：</td>
	                    <td>
		                    <#if order.orderStatus=="COMPLETE" || order.orderStatus=="CANCEL"> 
								<select id="cancleReason" disabled ="true" name="cancleReasonText"> 
									<option value="0">选择原因</option> 
								</select> 
							<#else> 
								<#if ''==order.cancelCode > 
									<select id="cancleReason" name="cancleReasonText"> 
										<option value="0">选择原因</option> 
									</select> 
								<#else> 
									<select id="cancleReason" name="cancleReasonText"> 
										<option value="0">${order.reason!''}</option> 
									</select> 
								</#if> 
							</#if>
		                   <#if isSupplierOrder=="true" >
		                     <span class="fr" style="color:red">此订单为供应商订单，取消操纵成功后，需要等待供应商确认后，才会真正把订单取消。</span>
		                   </#if>
	                   </td>  
	                </tr>
	                <tr>
	                	<td>订单备注：</td>
	                	<td>
	                		<textarea style="width:200px; height:120px;" id="orderRemark" name="orderRemark" onkeyup="checkRemarkLength()">${order.orderMemo!''}</textarea>
	                		<span class="fr" id="zsRemark">0/500字</span>
	                	</td>
	                </tr>
	                <tr>
	                	<td></td>
		                <td>
	                		<div class="fl operate" style="text-align: center;">
	                			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                			<a class="btn btn_cc1" id="saveButton" href="javascript:cancelOrder()">保存</a>
	                		</div>
		                </td>
	                </tr>
	            </tbody>
	         </table>
         </form>
    </div>
</div>
<script type="text/javascript">
	function cancelOrder(){
		var cancelCode=$('input:radio[name="cancelCode"]:checked').val();
		if(cancelCode==null){
		    alert("取消类型不能为空");
		    return;
		}
		var r=confirm("确定取消订单？");
		if (r==true){
		    //遮罩层
		    var loading = pandora.loading("正在努力保存中...");
			$.ajax({
				   url : "/vst_order/ord/order/confirm/cancelOrder.do",
				   data : $("#cancelOrderForm").serialize(),
				   type:"POST",
				   dataType:"JSON",
				   success : function(result){
						if(result.code=="success" ){
							loading.close();
						  	alert(result.message);
						  	document.location.reload();
						}else {
							loading.close();
							alert(result.message);
                            document.location.reload();
						}
				   },
				   error: function(XMLHttpRequest, textStatus, errorThrown) {
					   loading.close();
					   if(textStatus=='timeout'){
                           document.location.reload();
					　　}else{
                           document.location.reload();
					　　}
					}
			});
		}
		showCancelOrderDialog.close();
	}
	
	function orderCancelTypeChange(obj){  
        var cancelType=obj.value;
        var param="dictDefId="+cancelType+"&needSelect=true";
      	
      	$.ajax({
		   url : "/vst_order/order/ordCommon/findBizDictData.do",
		   data : param,
		   type:"POST",
		   dataType:"JSON",
		   success : function(data){
		   		  $("#cancleReason").html("");
		   		  $.each(data,function(i){
					var valueText=this.dictId;
					var text=this.dictName;
					if((i+1)==data.length  && (cancelType=="200" || cancelType=="201") )
					{
						$("#cancleReason").append("<option  value="+text+">"+text+"</option>");
					}else{
					
						$("#cancleReason").append("<option value="+text+">"+text+"</option>");
					}
		        })
		   },
		   error: function(XMLHttpRequest, textStatus, errorThrown) {
			   loading.close();
			   if(textStatus=='timeout'){
                   document.location.reload();
			　　}else{
                   document.location.reload();
			　　}
			}
		});	
    }
    
    $("#cancleReason").change(function(){
   		var cancleReasonText=$("#cancleReason").find("option:selected").text();
   		cancleReasonText=$.trim(cancleReasonText);
   		if(cancleReasonText=='其他')
   		{
   			var orderRemark=$.trim($("#orderRemark").html());
   			if(orderRemark!=''){
   				orderRemark+="  其他";
   				$("#orderRemark").html(orderRemark);
   			}else{
   				$("#orderRemark").html("其他");
   			}
   		}
    });
    
	function checkRemarkLength(){
        var orderRemark=document.getElementById('orderRemark');
        var remarkLength=orderRemark.value.length;
        if(remarkLength>500)
        {
	        $("#saveButton").attr("disabled",true);
	        $("#saveButton").hide();
	        $("#zsRemark").attr("style","color:red");
        }else{
        	$("#saveButton").removeAttr("disabled");
        	$("#saveButton").show();
        	$("#zsRemark").attr("style","");
        }
        $("#zsRemark").html(remarkLength+"/500字");
    }
</script>