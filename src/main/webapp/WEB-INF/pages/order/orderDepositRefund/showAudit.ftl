<!DOCTYPE html>

<html>
<head>
    <title>显示审核</title>
</head>
<body>

<!-- 记录 -->
<#if applyType == 'TRANSFER'>
    <#include "/order/orderDepositRefund/inc/showTransferTable.ftl">
</#if>

<#if applyType == 'LOSSES'>
<p>
    <strong>订单金额 :</strong> ${order.oughtAmount/100} 元
</p>
<p>
    <strong>已支付&nbsp; &nbsp; :</strong> ${order.actualAmount/100} 元&nbsp;
</p>
<p>
    <strong>历史申请记录</strong>
</p>
<p>
    <#include "/order/orderDepositRefund/inc/showLossesTable.ftl">
</#if>

<#--表单-->
<form id="auditFormId">
    <input id="orderId" type="hidden" name="orderId" value="${orderId}">
    <input id="applyType" type="hidden" name="applyType" value="${applyType}">
    <#--资金转移-->
    <#if applyType == 'TRANSFER'>
        <table style="width:100%;margin-top: 30px;" cellpadding="2" cellspacing="0" border="0" bordercolor="#000000"
               align="center">
            <tr>
                <td style="text-align:right;width:35%;height:50px;">* 需要转移至</td>
                <td><input name="transferOrderId" placeholder="请输入订单号" type="text"/><b class="validate" style="color:#FF0000;"></b></td>
            </tr>
            <tr>
                <td style="text-align:right;width:35%;height:50px;">* 转移原因</td>
                <td><textarea style="width: 286px; height: 108px; margin: 0px;" name="applyInfo"/></td>
            </tr>
        </table>
        <table style="width:30%;margin-top: 30px;" cellpadding="2" cellspacing="0" border="0" bordercolor="#000000"
               align="center">
            <tbody>

            <tr>
                <td style="text-align:right">
                    <a class="btn JS_show_dialog_category" id="auditCommit" style=";margin-right: 30px;">提交</a>

                </td>
                <td style="text-align:center"><a id="auditCancel" class="btn JS_show_dialog_category" style=""
                                                 value="">取消</a></td>

            </tr>
            </tbody>
        </table>
    </#if>
    <#--定金核损-->
    <#if applyType == 'LOSSES'>
        <table style="width:100%;margin-top: 30px;" cellpadding="2" cellspacing="0" border="0" bordercolor="#000000"
               align="center">
            <tr>
                <td  style="text-align:right;width:35%;height:50px;">* 备注信息</td>
                <td><textarea style="width: 286px; height: 108px; margin: 0px;" name="applyInfo"/></td>
            </tr>
        </table>
        <table style="width:30%;margin-top: 30px;" cellpadding="2" cellspacing="0" border="0" bordercolor="#000000"
               align="center">
            <tbody>

            <tr>
                <td style="text-align:right">
                    <a class="btn JS_show_dialog_category" id="auditCommit" style=";margin-right: 30px;">提交核损申请</a>

                </td>
                <td style="text-align:center"><a id="auditCancel" class="btn JS_show_dialog_category" style=""
                                                 value="">取消</a></td>
            </tr>
            </tbody>
        </table>
    </#if>
</form>

</body>
</html>

<script>

    //订单号校验
    $("input[name='transferOrderId']").keyup(function () {
        var c = $(this);
        if(!isNaN(c.val())){
            $(".validate").html("");
        }else{
            $(".validate").html("输入正确的订单号");
        }
    });

    function check() {
        if($(".validate").html() == ""){
            return true;
        }else{
            return false;
        }
    }

    //提交申请
    $("#auditCommit").bind("click", function () {
        var orderId = $("#orderId").val();
        var applyType = $("#applyType").val();
        if ((applyType == "TRANSFER") && !check()) {
            return;
        }
        //校验是否可提交申请
        $.get("/vst_order/order/depositRefund/checkCommit/" + applyType + "/" + orderId + ".do", function (data) {
            if (data.code == 500) {
                alert(data.msg);
            } else {

                if ((data.code == 501)) {
                    if (confirm(data.msg)) {
                        // 驳回已有 【审核中】 或 【审核通过且未处理】的记录
                        $.get("/vst_order/order/depositRefund/revertBeforeApply/" + applyType + "/" + orderId + ".do?", function (data) {
                            if (data.code == 500) {
                                alert(data.msg);
                                return;
                            }
                        });
                    } else {
                        return;
                    }
                }

                /**
                 * 提交申请
                 */
                $.ajax({
                    url: "/vst_order/order/depositRefund/commitAudit/" + applyType + ".do",
                    data: $("#auditFormId").serialize(),
                    type: "POST",
                    dataType: "JSON",
                    success: function (result) {
                        if (result.code == 200) {
                            if (applyType == 'TRANSFER') {
                                showTransferDialog.reload();
                            } else {
                                showLossesDialog.reload();
                            }
                        } else {
                            $.alert(result.msg);
                        }
                    }
                });
            }
        });
    });

    //取消申请
    $("#auditCancel").bind("click", function () {
        var applyType = $("#applyType").val();
        if (applyType == 'TRANSFER') {
            showTransferDialog.close();
        } else {
            showLossesDialog.close();
        }
    });

</script>