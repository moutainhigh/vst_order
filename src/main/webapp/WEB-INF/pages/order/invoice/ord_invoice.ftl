<html>
	<body>
		<strong>发票信息</strong>
		<table style="font-size: 12px" cellspacing="1" cellpadding="4" border="0" bgcolor="#B8C9D6" width="100%" class="newfont03">
				<tr bgcolor="#f4f4f4" align="center">
					<td height="30">
						发票抬头
					</td>
					<td>订单号列表</td>
					<td>
						发票内容
					</td>
					<td>
						发票金额
					</td>
					<td>
						发票备注
					</td>
					<td>
						状态
					</td>
					<td>
						送货方式
					</td>
					<td>
						快递号
					</td>
					<td>
						地址
					</td>
					<td>
						接收人
					</td>
					<td>
						电话
					</td>
					<td>
						操作
					</td>
				</tr>
				<#list invoiceList as invoice> 
					<tr bgcolor="#ffffff" align="center">
						<td height="25">
							${invoice.title!'' }
						</td>
						<td>
						    ${invoice.getOrderides()!''}
						</td>
						<td>
							${invoice.content!'' }
						</td>
						<td>
							<#if invoice.ordInvoiceId lt 0 >以订单金额为准<#else>${invoice.amountYuan!''}</#if>
						</td>
						<td>
							${invoice.memo!'' }
						</td>
						<td>
							${invoice.getZhStatus()!''}
						</td>
						<td>
							${invoice.getZhDeliveryType()!''}
						</td>
						<td>
							${invoice.expressNo!''}
						</td>
						<td>
							${invoice.deliveryAddress.addressList[0].province!''} ${invoice.deliveryAddress.addressList[0].city!''} ${invoice.deliveryAddress.addressList[0].street!''}
						</td>
						<td>
							${invoice.deliveryAddress.fullName!''}
						</td>
						<td>
							${invoice.deliveryAddress.mobile!''}
						</td>
						<td>
						    <#if invoice.status == 'UNBILL'>
						        <a href="javascript:cancelInvoice('${invoice.ordInvoiceId!''}')">取消</a>
						    </#if>
						    <#if invoice.status == 'APPROVE' || invoice.status == 'BILLED' && invoice.redFlag != 'true'>
						        <a href="javascript:reqRedInvoice('${invoice.ordInvoiceId!''}')">申请红冲</a>
						    </#if>
							<!--
							<a href="/vst_order/order/orderInvoice/ord/invoiceLong.do?ordInvoiceId=${invoice.ordInvoiceId!''}&parentType=ORD_INVOICE" target="_blank">查看日志</a>
							-->
							<a href="http://super.lvmama.com/lvmm_log/bizLog/find.do?objectId=${(invoice.ordInvoiceId)!''}&objectType=ORD_INVOICE&sysName=VST" target="_blank">查看日志</a>
						</td>
					</tr>
				</#list>
		</table>
	</body>
<script type="text/javascript">
  //取消
  function cancelInvoice(invoiceId){
	if(!confirm("确定需要取消当前发票")){
		return false;
	}
	var status="CANCEL";
	$.ajax({
		url : "/vst_order/order/orderInvoice/ord/invoiceChangeStatus.do",
		type : "post",
		dataType : 'json',
		data : {"status":status,"ordInvoiceId":invoiceId},
		success : function(result) {
			window.location.reload();
		}
	});
 }
 
  //申请红冲
  function reqRedInvoice(invoiceId){
	if(!confirm("确定要申请红冲吗?")){
		return false;
	}
	$.ajax({
		url : "/vst_order/order/orderInvoice/ord/doReqRedInvoice.do",
		type : "post",
		dataType : 'json',
		data : {"ordInvoiceId":invoiceId},
		success : function(result) {
			window.location.reload();
		}
	});
 }

</script>	
	
</html>
