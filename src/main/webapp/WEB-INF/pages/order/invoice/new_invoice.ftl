<html>
<head>
    <title>发票信息查询</title>
<#include "/base/head_meta.ftl"/>
</head>
	<body>
		<div>
            <strong>发票信息</strong>
            <table style="font-size: 12px" cellspacing="1" cellpadding="4" border="0" bgcolor="#B8C9D6" width="100%" class="">
                <tr bgcolor="#f4f4f4" align="center">
                    <td>订单号</td>
                    <td>
                        开票内容
                    </td>
                    <td>
                        发票抬头
                    </td>
                    <td>
                        纳税人识别号
                    </td>
                    <td>
                        开户银行
                    </td>
                    <td>
                        开户银行帐号
                    </td>
                    <td>
                        发票申请时间
                    </td>
                    <td>
                        发票类型
                    </td>
                    <td>
                        寄送地址
                    </td>
                    <td>
                        寄送接收人
                    </td>
                    <td>
                        寄送电话
                    </td>
                    <td>
                        接受邮箱
                    </td>
                    <td>
                        状态
                    </td>
                    <td>
                        操作
                    </td>
                </tr>
				<#list invoiceList as invoice>
					<tr bgcolor="#ffffff" align="center">
                        <td>
							${invoice.orderId!''}
                        </td>
                        <td>
							${invoice.content!'' }
                        </td>
                        <td>
							${invoice.title!'' }
                        </td>
                        <td>
							${invoice.taxNumber!'' }
                        </td>
                        <td>
							${invoice.bankAccount!''}
                        </td>
                        <td>
							${invoice.accountBankAccount!''}
                        </td>
                        <td>
							${invoice.createTime?string('yyyy−MM−dd hh:mm:ss')!''}
                        </td>
                        <td>
                            <#if invoice.elecInvoice == 0>
                                纸质发票
							<#else >
                                电子发票
							</#if>
                        </td>
                        <td>
							${invoice.province!''} ${invoice.city!''} ${invoice.street!''}
                        </td>
                        <td>
							${invoice.fullName!''}
                        </td>
                        <td>
							${invoice.mobile!''}
                        </td>
                        <td>
							${invoice.receiverEmail!''}
                        </td>
                        <td>
                            <#if invoice.status == 'PENDING'>
                                待申请
                            <#elseif invoice.status == 'APPLIED'>
                                已申请
                            <#elseif invoice.status == 'CANCEL'>
                                取消
                            <#elseif invoice.status == 'REVOKE'>
                                取消
                            <#elseif invoice.status == 'MANUAL'>
                                需人工申请
                            <#elseif invoice.status == 'INVALID'>
                                无效
                            <#elseif invoice.status == 'UNPAY'>
                                待支付
                            <#elseif invoice.status == 'FAILURE'>
                                申请失败
                            <#else>
                                ${invoice.status!''}
                            </#if>
                        </td>
                        <td>
                            <#if invoice.status == 'UNPAY' || invoice.status == 'PENDING'>
                                <a href="javascript:showInvoice('${invoice.id!''}')">修改</a>
                                <a href="javascript:cancelInvoice('${invoice.id!''}','${invoice.orderId!''}')">取消</a>
                            </#if>
                        </td>
                    </tr>
				</#list>
            </table>
		</div>

        <#--页脚-->
        <#include "/base/foot.ftl"/>
	</body>

</html>

<script type="text/javascript">
    //取消
    function cancelInvoice(invoiceId,orderId) {
        var msg = '确定取消发票申请？';
        var url = '/vst_order/order/orderInvoice/cancelInvoiceInfo.do';
        var param = 'invoiceId=' + invoiceId + '&orderId=' + orderId;
        $.confirm(msg, function(){
            $.ajax({
                url : url,
                type : "post",
                data : param,
                success : function(result) {
                    if(result.code=='success') {
                        $.alert(result.message,function(){
                            window.location.reload();
                        });
                    } else {
                        $.alert(result.message);
                    }
                }
            });
        });
    }

    function showInvoice(invoiceId) {
        var url = "/vst_order/order/orderInvoice/showUpdateInvoiceInfo.do";
        url += "?invoiceId=" + invoiceId;
        updateInvoiceDialog = new xDialog(url,{},{title:"发票申请信息修改",iframe:true,width:800,height:650});
    }

</script>