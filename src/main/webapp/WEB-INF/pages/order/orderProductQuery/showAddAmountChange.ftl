
<form id="addAmountChangeForm">
    	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
    	<input type="hidden" name="orderItemId" value="${RequestParameters.orderItemId}">
	<input type="hidden" name="orderType" value="${RequestParameters.orderType}">
	
         <table class="e_table form-inline ">
            <tbody>
            	<tr>
                    <td class="w6 s_label">修改类型：</td>
                    <td class="w6">
                    <#if RequestParameters.orderType== 'parent'>
                    	<select name="formulas" id="formulas" >
	                		<option value="SUBTRACT">减少</option>
	            		</select>
                    <#else>
		                <select name="formulas" id="formulas" >
	                    <#list orderFormulasList as orderFormulas>
	                		<option value="${orderFormulas.code!''}">${orderFormulas.cnName!''}</option>
	                	</#list>
	            		</select>
	               </#if>
	                     
                     

					</td>
                </tr>
                
                <tr>
                    <td class="w6 s_label">类型：</td>
                    <td class="w6">
                    <select class="w160" name="amountType" id="amountType" >
                    
	                    <#if RequestParameters.orderType == 'parent'>
							 <#list orderAmountChangeTypeList as orderAmountChangeType>
							 	<#if orderAmountChangeType.code!= 'COST_CHANGE'>
		                		<option value="${orderAmountChangeType.code!''}">${orderAmountChangeType.cnName!''}</option>
		                		</#if>
		                	</#list>
						<#else>
		                       <option value="COST_CHANGE">成本变化</option>  	 
	                     </#if>
                     
                    </select>
                    
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label"><i style="color:red">*</i>金额：</td>
                    <td class="w6">
                     <#if RequestParameters.orderType == 'parent'>
                   		 <input type="text" class="w160" name="amountChange" required="true" isAmountNum="true"/>
                    <#else>
		                   <#if ordMulPriceRateList?size!=0 >  
		                   		<#--子订单多价格类型的时候-->
			                     <#list ordMulPriceRateList as ordMulPriceRate>
									<input type="hidden" class="w160" name="priceType"   value="${ordMulPriceRate.priceType}"/>
									 <#list orderPriceRateTypeList as orderPriceRateType>
									 	 <#if orderPriceRateType.code == ordMulPriceRate.priceType>
									 	 ${orderPriceRateType.cnName!''}
									 	 </#if>
									 </#list> 
								 	 (单价)：
								 	 <input type="text" class="w160" name="amountChange"  id="${ordMulPriceRate.priceType}" isAmountNum="true"/>
			                		 </br>
			                	 </#list> 
		                    <#else>
		                    	<#--子订单非多价格类型的时候-->
		                   		<input type="text" class="w160" name="amountChange" required="true" isAmountNum="true"/>
		                   </#if>   
	                 </#if>
	                 <div class="cc3" style="color:red">注:金额为增加或者减少金额，仅支持正数(或2位小数)</div>
                    
                    
                    </td>
                </tr>
                <tr>
                    <td class="w6 s_label">原因：</td>
                    <td class="w6"><textarea style="width:285px; height:120px;" id="reason" name="reason"></textarea></td>
                </tr>
               
            </tbody>
         </table>
        </form>
        
        
<p align="center">
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="submitAddAmountChangeForm">保存</button>
	&nbsp;&nbsp;
	<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>

   <#include "/base/foot.ftl"/>
<script>
	
$("#submitAddAmountChangeForm").bind("click",function(){
 /**
		//验证金额
		var num = /^(([1-9]?\d*)(\.\d{2})?)$/;
		$('input[name="amount"]').each(function(i){
		   alert(${this});
		 });

		var amount = $("#amount").val();
		if(amount==""){
			alert("请输入金额!");
			return;
		}
		if(!num.test(amount)){
			alert("只能填写正数(或2位小数)");
			return;
		}
	*/	
		
			//验证
		if(!$("#addAmountChangeForm").validate().form()){
			return;
		}
		
		//遮罩层
	    var loading = pandora.loading("正在努力保存中...");	
		
		$.ajax({
			url : '/vst_order/order/orderAmountChange/addOrdAmountChange.do',
		  	data : $("#addAmountChangeForm").serialize(),
		  	type : 'GET',
		  	dataType : 'JSON',
		  	success : function(result){
		  	
		  		loading.close();
			  	if(result.code=="success"){
		   			alert(result.message);
		   			showAmountDialog.close();
			   		 //parent.window.location.reload();
		   		}else {
		   			
		   		  	alert(result.message);
		   		}
		   }		
		});
	
	});
	
	$("#closeButton").bind("click", function() {
	 	showAmountDialog.close();
	});
</script>