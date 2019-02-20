
<form id="addAmountChangeForm">
    	<input type="hidden" name="orderId" value="${orderItem.orderId}">
    	<input type="hidden" name="orderItemId" value="${orderItem.orderItemId}">
    	<input type="hidden" name="ordMulPriceRateListCount" id="ordMulPriceRateListCount" value="${ordMulPriceRateListCount}">
	<#if ordMulPriceRateList?size!=0 > 
	 <#list ordMulPriceRateMap?keys as key>
	    <table class="e_table form-inline " id="table${key}">
            <tbody>
                <tr>
                    <td class="w6 s_label">结算价格：</td>
                    <td class="w6">
		                <#--子订单多价格类型的时候-->
							<input type="checkbox" class="w160" name="checkPriceType${key}" id="checkPriceType${key}" value="${ordMulPriceRateList[key_index].priceType}"/>
							<input type="hidden" class="w160" name="priceType"   value="${ordMulPriceRateList[key_index].priceType}"/>
						   ${ordMulPriceRateMap[key]} 
						   <input type="text" class="w160"  name="newActualSettlementPrice${key}" value='${ordMulPriceRateList[key_index].price/100.0}' maxlength="10"  isAmountNum="true" /></br>
                   <div class="cc3" style="color:red">注:仅支持正数(或2位小数)</div>
                    </td> 
                </tr>
                 <tr>
                    <td class="w6 s_label">修改原因：</td>
                    <td class="w6">  
                    <select class="w160" name="form[${key}].reason" id="form[${key}].reason" >
	                     <#list orderAmountChangeTypeList as orderAmountChangeType>
		                		<option value="${orderAmountChangeType.code!''}">${orderAmountChangeType.cnName!''}</option>
		                </#list>
                    </select>
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label">备注：</td>
                    <td class="w6"><textarea style="width:263px; height:50px;" id="form[${key}].remark" name="form[${key}].remark">${ordMulPriceRateList[key_index].remark}</textarea></td>
                </tr>
                <tr>
                   <td colspan="2" style="color:gray"> -----------------------------------------------------------------------------------------</td>
                </tr>
            </tbody>
         </table>
          </#list>
	<#else>
	 <#list ordMulPriceRateMap?keys as key>
          <table class="e_table form-inline " id="table${key}">
            <tbody>
                <tr>
                    <td class="w6 s_label">结算价格：</td>
                    <td class="w6">
                    <#--子订单非多价格类型的时候-->
                   	<input type="text" class="w160" name="newActualSettlementPrice${key}" required="true"  value="${orderItem.actualSettlementPrice/100.0}"  oldData="${orderItem.actualSettlementPrice/100.0}" maxlength="10" isAmountNum="true" />
                   <div class="cc3" style="color:red">注:仅支持正数(或2位小数)</div>
                    </td>
                </tr>
                 <tr>
                    <td class="w6 s_label">修改原因：</td>
                    <td class="w6">
                    <select class="w160" name="form[${key}].reason" id="form[${key}].reason" >
	                     <#list orderAmountChangeTypeList as orderAmountChangeType>
		                		<option value="${orderAmountChangeType.code!''}">${orderAmountChangeType.cnName!''}</option>
		                </#list>
                    </select>
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label">备注：</td>
                    <td class="w6"><textarea style="width:285px; height:120px;" id="form[${key}].remark" name="form[${key}].remark"></textarea></td>
                </tr>
            </tbody>
         </table> 
          </#list>        		
    </#if>      
  </form>         
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="submitAddAmountChangeForm">保存</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>

<script>
$("#submitAddAmountChangeForm").bind("click",function(){
		$("#submitAddAmountChangeForm").attr("disabled",true);
        var ordMulPriceRateListCount = $("#ordMulPriceRateListCount").val();
        //alert(ordMulPriceRateListCount);
        if(ordMulPriceRateListCount >= 1){
            var flag = false;
            $("#[id^=checkPriceType]").each(function(i,e){  
			    if($(e).attr("checked")=="checked"){
				   flag = true;
				   $("#submitAddAmountChangeForm").attr("disabled",false);
				   return;
			    }						  					   
		     });
		     if(!flag){
			   alert("请至少选中一个价格类型修改");
			   $("#submitAddAmountChangeForm").attr("disabled",false);
			   return;
		     }
         }
         var newActualSettlementPrice = $("[name^=newActualSettlementPrice]").val();
         if(newActualSettlementPrice.length > 9){
		  alert("金额不可以超过9位数！");
		  $("#submitAddAmountChangeForm").attr("disabled",false);
		  return;
		}
         
     
		//验证
		if(!$("#addAmountChangeForm").validate().form()){
			$("#submitAddAmountChangeForm").attr("disabled",false);
		    return;
		}
		//遮罩层
	    var loading = pandora.loading("正在努力保存中...");	
		$.ajax({
			url : '/vst_order/order/orderSettlementChange/addOrderSettlementChange.do',
		  	data : $("#addAmountChangeForm").serialize(),
		  	type : 'GET',
		  	dataType : 'JSON',
		  	success : function(result){
		  		loading.close();
			  	if(result.code=="success"){
		   			alert(result.message);
		   			if("结算价修改成功"==result.message)
		   			{
		   				$("#searchForm").submit();
		   				//window.location.href = "/vst_order/order/orderSettlementChange/showOrderSettlementList.do";
		   			}else{
		   				showAmountDialog.close();
		   			}
		   			//
			   		 //parent.window.location.reload();
		   		}else {
		   			//showAmountDialog.close();
		   			$("#submitAddAmountChangeForm").attr("disabled",false);
		   		  	alert(result.message);
		   		}
		   }		
		});
	
	});
	
	$("#closeButton").bind("click", function() {
	 	showAmountDialog.close();
	});
</script>