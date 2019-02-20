<form id="addAmountChangeForm">
    <input type="hidden" name="orderId" id="orderId" value="${orderId}">
    <input type="hidden" name="orderItemId" value="${ordOrderItemExtend.orderItemId}">
    <table class="e_table form-inline">
        <tbody>
        <tr>
            <td class="w6 s_label">结算价格：</td>
            <td class="w6">
                <input type="text" class="w160" name="newActPrice" required="true"
                       value="${ordOrderItemExtend.foreignActualSettlementPrice/100.0}"
                       oldData="${ordOrderItemExtend.foreignActualSettlementPrice/100.0}" maxlength="10" isAmountNum="true"/>
                ${ordOrderItemExtend.currencyName}
                <div class="cc3" style="color:red">注:仅支持正数(或2位小数)</div>
            </td>
        </tr>
        <tr>
            <td class="w6 s_label">修改原因：</td>
            <td class="w6">
                <select class="w160" name="reason">
                    <#list orderAmountChangeTypeList as orderAmountChangeType>
                     <option value="${orderAmountChangeType.code!''}">${orderAmountChangeType.cnName!''}</option>
                    </#list>
                </select>
            </td>
        </tr>
        <tr>
            <td class="w6 s_label">备注：</td>
            <td class="w6"><textarea style="width:285px; height:120px;" name="remark"></textarea></td>
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
    $("#submitAddAmountChangeForm").bind("click", function () {
        $("#submitAddAmountChangeForm").attr("disabled", true);

        var newActualSettlementPrice = $("[name^=newActPrice]").val();
        if (newActualSettlementPrice.length > 9) {
            alert("金额不可以超过9位数！");
            $("#submitAddAmountChangeForm").attr("disabled", false);
            return;
        }

        //验证
        if (!$("#addAmountChangeForm").validate().form()) {
            $("#submitAddAmountChangeForm").attr("disabled", false);
            return;
        }
        //遮罩层
        var loading = pandora.loading("正在努力保存中...");
        $.ajax({
            url: '/vst_order/order/orderSettlementChange/addCurrencySettlementChange.do',
            data: $("#addAmountChangeForm").serialize(),
            type: 'GET',
            dataType: 'JSON',
            success: function (result) {
                loading.close();
                if (result.code == "success") {
                    alert(result.message);
                    if ("外币结算价修改成功" == result.message) {
                        $("#searchForm").submit();
                    } else {
                        showAmountDialog.close();
                    }
                } else {
                    $("#submitAddAmountChangeForm").attr("disabled", false);
                    alert(result.message);
                }
            }
        });

    });

    $("#closeButton").bind("click", function () {
        showAmountDialog.close();
    });
</script>