<!DOCTYPE html>

<html>
<head>
    <title>显示审核审批</title>
</head>
<body>

<!-- 记录 -->
<#if applyType == 'TRANSFER'>
    <#include "/order/orderDepositRefund/inc/showTransferTable.ftl">
</#if>

<#if applyType == 'LOSSES'>
    <#include "/order/orderDepositRefund/inc/showLossesTable.ftl">
</#if>

<#--表单-->
<form id="processAuditFormId">
    <input id="refundAuditId" type="hidden" name="refundAuditId" value="${processAudit.refundAuditId}">
    <input id="orderId" type="hidden" name="orderId" value="${orderId}">
    <input id="applyType" type="hidden" name="applyType" value="${applyType}">
    <input id="aduitStatus" type="hidden" name="aduitStatus">
    <input id="retreatAmount" type="hidden" name="retreatAmount">
<#--资金转移-->
<#if applyType == 'TRANSFER'>
    <table style="width:100%;margin-top: 30px;" cellpadding="2" cellspacing="0" border="0" bordercolor="#000000"
           align="center">
        <tr>
            <td style="text-align:right;width:35%;height:50px;">需要转移至 :&nbsp; &nbsp; </td>
            <td>${processAudit.transferOrderId}</td>
        </tr>
        <tr>
            <td style="text-align:right;width:35%;height:50px;">转移原因 :&nbsp; &nbsp; </td>
            <td>${processAudit.applyInfo}</td>
        </tr>
        <tr>
            <td style="text-align:right;width:35%;height:50px;">处理意见 : &nbsp; &nbsp;</td>
            <td><textarea style="width: 286px; height: 108px; margin: 0px; resize: none" name="aduitReply"/></td>
        </tr>
    </table>
    <table style="width:50%;margin-top: 30px;" cellpadding="2" cellspacing="0" border="0" bordercolor="#000000"
           align="center">
        <tbody>

        <tr>
            <td style="text-align:right">
                <a class="btn JS_show_dialog_category" id="auditPass" style=";margin-right: 30px;">通过</a>


            </td>
            <td style="text-align:center"><a id="auditReject" class="btn JS_show_dialog_category" style=""
                                             value="">驳回</a></td>
            <td>
                <a id="auditNone" class="btn JS_show_dialog_category">暂不处理</a></td>
        </tr>
        </tbody>
    </table>
</#if>
<#--定金核损-->
<#if applyType == 'LOSSES'>
    <table style="width:100%;margin-top: 30px;" cellpadding="2" cellspacing="0" border="0" bordercolor="#000000"
           align="center">
        <tr>
            <td style="text-align:right;width:35%;height:50px;">已支付：</td>
            <td>${order.actualAmount/100} 元 (已退
                <#if order.refundedAmount??>
                ${order.refundedAmount/100}
                <#else >
                    0
                </#if>
                元 ， <b style="color:#FF0000;">剩余可退
                    <#if canRundAmount??>
                    ${canRundAmount/100}
                    <#else >
                        0
                    </#if>
                    元
                </b>)
            </td>
        </tr>
        <tr>
            <td style="text-align:right;width:35%;height:50px;">损失金额：</td>
            <td><input name="lossAmountShow" type="text"><b class="validate" style="color:#FF0000;"></td>
            <input type="hidden" name="lossAmount" value="0">
        </tr>
        <tr>
            <td style="text-align:right;width:35%;height:50px;">处理意见：</td>
            <td><textarea style="width: 286px; height: 108px; margin: 0px;" name="aduitReply"/></td>
        </tr>
    </table>
    <table style="width:50%;margin-top: 30px;" cellpadding="2" cellspacing="0" border="0" bordercolor="#000000"
           align="center">
        <tbody>

        <tr>
            <td style="text-align:right">
                <a class="btn JS_show_dialog_category" id="auditPass" style=";margin-right: 30px;">通过</a>


            </td>
            <td style="text-align:center"><a id="auditReject" class="btn JS_show_dialog_category" style=""
                                             value="">驳回</a></td>
            <td>
                <a id="auditNone" class="btn JS_show_dialog_category">暂不处理</a></td>
        </tr>
        </tbody>
    </table>
</#if>
</form>

</body>
</html>

<script>

    //损失金额校验
    $("input[name='lossAmountShow']").keyup(function () {
        var c = $(this);
        if(!isNaN(c.val())){
            $(".validate").html("");
        }else{
            $(".validate").html("输入正确损失金额");
        }
    });

    function check() {
        if($(".validate").html() == ""){
            return true;
        }else{
            return false;
        }
    }


    //审核通过
    $("#auditPass").bind("click", function () {
        $("#aduitStatus").attr("value", "PASS");
        var lossAmount = $("input[name='lossAmountShow']").val() * 100;
        $("input[name='lossAmount']").attr("value", lossAmount);
        commitProcess();
    });

    //驳回
    $("#auditReject").bind("click", function () {
        $("#aduitStatus").attr("value", "REJECT");
        $("#retreatAmount").attr("value", "0");
        commitProcess();
    });

    //暂不处理
    $("#auditNone").bind("click", function () {
        var applyType = $("#applyType").val();
        if (applyType == 'TRANSFER') {
            showProcessTransferDialog.close();
        } else {
            showProcessLossesDialog.close();
        }
    });

    //
    function commitProcess() {
        var applyType = $("#applyType").val();
        if ((applyType == "LOSSES") && !check()) {
            return;
        }

        //损失金额必须小于等于剩余可退金额提示
        if (applyType == "LOSSES") {
            var lossAmountShow = $("input[name='lossAmountShow']").val();
            var canRundAmount = ${canRundAmount!0};
            if (lossAmountShow > canRundAmount) {
                if (!confirm("损失金额大于可退金额，确定要继续操作？")) {
                    return;
                }
            }
        }

        /**
         * 提交申请
         */
        $.ajax({
            url: "/vst_order/order/depositRefund/processAudit.do",
            data: $("#processAuditFormId").serialize(),
            type: "POST",
            dataType: "JSON",
            success: function (result) {
                if (result.code == 200) {
                    alert("处理成功");
                    if (applyType == 'TRANSFER') {
                        showProcessTransferDialog.close();
                    } else {
                        showProcessLossesDialog.close();
                    }
                } else {
                    $.alert(result.msg);
                }
            }
        });
    };

</script>