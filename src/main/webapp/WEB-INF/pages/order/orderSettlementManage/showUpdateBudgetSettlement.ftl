<form id="addAmountChangeForm">
    <input type="hidden" name="orderItemId" id="orderItemId" value="${orderItem.orderItemId}">
    <input type="hidden" name="ordMulPriceRateListCount" id="ordMulPriceRateListCount" value="${ordMulPriceRateListCount}">
    <table class="e_table form-inline " id="table">
        <tbody>

        <input type="hidden" id="buyoutSettlementPrice" value="${orderItem.buyoutPrice/100.0}"/>
        <input type="hidden" id="buyoutTotalSettlementPrice" value="${orderItem.buyoutTotalPrice/100.0}"/>
        <input type="hidden" name="settlementPrice" id="settlementPrice" value="${orderItem.buyoutPrice/100.0}">

        <tr>
            <td class="w6 s_label">修改价格模式：</td>
            <td class="w6">
                <input type="radio"  name="priceModel" value='BUDGET_UNIT_PRICE' onclick="changePriceModel('BUDGET_UNIT_PRICE')" checked="true"/>修改单价
                <input type="radio"  name="priceModel" value='BUDGET_TOTAL_PRICE' onclick="changePriceModel('BUDGET_TOTAL_PRICE')"/>修改总价
            </td>
        </tr>

        <tr id="unitPriceTr">
            <td class="w6 s_label">买断单价：</td>
            <td class="w6">
                <input type="text" class="w160" id="buyoutPrice"  name="buyoutPrice" value='${orderItem.buyoutPrice/100.0}' maxlength="10"  isAmountNum="true" /></br>
                <div class="cc3" style="color:red">注:仅支持正数(或2位小数)</div>
            </td>

            <td>买断份数：</td>
            <td class="w6">
                <div class="cc3">${orderItem.buyoutQuantity}</div>
            </td>

        </tr>

        <tr id="totalPriceTr" style="display: none">
            <td class="w6 s_label">买断总价格：</td>
            <td class="w6">
                <input type="text" class="w160" id="buyoutTotalPrice"  name="buyoutTotalPrice" value='${orderItem.buyoutTotalPrice/100.0}' maxlength="10"  isAmountNum="true" /></br>
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

        $("#buyoutPrice").val($("#buyoutSettlementPrice").val());
        $("#buyoutTotalPrice").val($("#buyoutTotalSettlementPrice").val());

        if(priceModel == 'BUDGET_UNIT_PRICE'){
            $("#unitPriceTr").show();
            $("#totalPriceTr").hide();
        }else{
            $("#totalPriceTr").show();
            $("#unitPriceTr").hide();
        }
    }


    $("#submitAddAmountChangeForm").bind("click",function(){
        //验证
        if(!$("#addAmountChangeForm").validate().form()){
            return;
        }
        var priceModel = $(":radio:checked[name=priceModel]").val();
        var settlementPrice;
        if(priceModel == 'BUDGET_UNIT_PRICE'){
            settlementPrice = $("#buyoutPrice").val();
        }else{
            settlementPrice = $("#buyoutTotalPrice").val();
        }
        $("#settlementPrice").val(settlementPrice);

        if(settlementPrice.length > 9){
            alert("金额不可以超过9位数！");
            return;
        }

        //遮罩层
        var loading = pandora.loading("正在努力保存中...");
        $.ajax({
            url : '/vst_order/order/orderSettlementChange/addOrderBudgetSettlementChange.do',
            data : $("#addAmountChangeForm").serialize(),
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