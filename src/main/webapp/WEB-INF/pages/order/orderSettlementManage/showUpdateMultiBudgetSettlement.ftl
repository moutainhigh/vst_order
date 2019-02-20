<form id="addAmountChangeForm">
    <input type="hidden" name="orderItemId" id="orderItemId" value="${orderItem.orderItemId}">
    <input type="hidden" name="ordMulPriceRateListCount" id="ordMulPriceRateListCount" value="${ordMulPriceRateListCount}">
    <table class="e_table form-inline " id="table">
        <tbody>

        <tr>
            <td class="w6 s_label">修改价格模式：</td>
            <td class="w6">
                <input type="radio"  name="priceModel" value='BUDGET_UNIT_PRICE' onclick="changePriceModel('BUDGET_UNIT_PRICE')" checked="true"/>修改单价
                <input type="radio"  name="priceModel" value='BUDGET_TOTAL_PRICE' onclick="changePriceModel('BUDGET_TOTAL_PRICE')"/>修改总价
            </td>
        </tr>
		<#list priceList as price>
	        <tr class="unitPriceTr">
	            <td class="w6 s_label">${price.priceCnName}：</td>
	            <td class="w6">
	            	<input type="checkbox" class="w160 multickb"  value="${price.priceType}"/>
	                <input type="text" class="w160 multi_price"  ori-value="${price.price/100.0}"   name="${price.priceType}" value='${price.price/100.0}' maxlength="10"  isAmountNum="true" /></br>
	                <div class="cc3" style="color:red">注:仅支持正数(或2位小数)</div>
	            </td>
	        </tr>
		</#list>
        <tr class="totalPriceTr" style="display: none">
            <td class="w6 s_label">买断总价格：</td>
            <td class="w6">
                <input type="text" class="w160" id="buyoutTotalPrice" ori-value="${orderItem.buyoutTotalPrice/100.0}" name="buyoutTotalPrice" value='${orderItem.buyoutTotalPrice/100.0}' maxlength="10"  isAmountNum="true" /></br>
                <div class="cc3" style="color:red">注:仅支持正数(或2位小数)</div>
            </td>
        </tr>


        <tr>
            <td class="w6 s_label">修改原因：</td>
            <td class="w6">
                <select class="w160" name="reason" id="reason" >
                <#list orderAmountChangeTypeList as orderAmountChangeType>
                    <option value="${orderAmountChangeType.code!''}">${orderAmountChangeType.cnName!''}</option>
                </#list>
                </select>
            </td>
        </tr>
        <tr>
            <td class="w6 s_label">备注：</td>
            <td class="w6"><textarea style="width:263px; height:50px;" id="remark" name="remark"></textarea></td>
        </tr>
        </tbody>
    </table>
</form>
<p align="center">
    <button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="submitAddAmountChangeForm">保存</button>
    &nbsp;&nbsp;
    <button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>

<script>

    function changePriceModel(priceModel){
        if(priceModel == 'BUDGET_UNIT_PRICE'){
	        $(".multi_price").each(function(){
	        	$(this).val($(this).attr("ori-value"));
	        });
            $(".unitPriceTr").show();
            $(".totalPriceTr").hide();
        }else{
	        $("#buyoutTotalPrice").val($("#buyoutTotalPrice").attr("ori-value"));
            $(".totalPriceTr").show();
            $(".unitPriceTr").hide();
        }
    }


    $("#submitAddAmountChangeForm").bind("click",function(){
        //验证
        if(!$("#addAmountChangeForm").validate().form()){
            return;
        }
        var priceModel = $(":radio:checked[name=priceModel]").val();
        
        var params={};
        params.remark = $("#remark").val();
        params.reson = $("#reason").val();
        
        params.orderItemId = $("#orderItemId").val();
        if(priceModel == 'BUDGET_UNIT_PRICE'){
	        params.priceModel = 'BUDGET_UNIT_PRICE';
        	//单价
        	$(".multi_price").each(function(){
	        	//$(this).val($(this).attr("ori-value"));
	        	var box = $(this).parents("td").find(":checkbox");
	        	if(box.attr("checked") == "checked"){
	        		var type = $(this).attr("name").toLowerCase();
	        		var val = $(this).val();
		        	switch(type){
		        		case 'settlement_adult_pre':
		        			params.settlementAdultPrice = $(this).val() * 100;
		        			break;
		        		
		        		case 'settlement_child_pre':
		        			params.settlementChildPrice = $(this).val() * 100;
		        			break;
		        		default :
		        			break;
		        	}
	        	}
	        	
	        })
        	
        	if(params.settlementAdultPrice==null && params.settlementChildPrice==null){
        		alert("请勾选你要修改的结算价");
        		return false;
        	}
        }else{
        	//总价
        	params.priceModel = 'BUDGET_TOTAL_PRICE';
            params.buyoutTotalPrice = $("#buyoutTotalPrice").val() * 100;
        }
	

        //遮罩层
        var loading = pandora.loading("正在努力保存中...");
        $.ajax({
            url : '/vst_order/order/orderSettlementChange/addOrderMultiBudgetSettlementChange.do',
            data : params,
            type : 'GET',
            dataType : 'JSON',
            success : function(result){
                loading.close();
                if(result.code=="success"){
                    if("结算价修改成功"==result.message){
                        $("#searchForm").submit();
                        //window.location.href = "/vst_order/order/orderSettlementChange/showOrderSettlementList.do";
                    }else{
                        showAmountDialog.close();
                    }
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