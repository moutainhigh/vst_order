<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>发票列表</title>	
	</head>
	<#include "/base/head_meta.ftl"/>
	<body>
		<div class="orderpopmain">
			<table style="font-size: 12px" width="100%" border="0" id="orderTable"
				class="contactlist">
				<tr>
					<td colspan="3">购买方名称：<span id="td_title">${ordInvoice.title!''}</span></td>
				</tr>
				<tr >
					<td width="33%" id="td_invoice">
						购买方式：<#if ordInvoice.purchaseWay == 'personal'>个人<#else>公司</#if>
					</td>
					<td width="33%">
						纳税人识别号：${ordInvoice.taxNumber!''}
					</td>
					<td width="33%" >					
						购买方地址：${ordInvoice.buyerAddress!''}
					</td>
				</tr>
				<tr >
					<td width="33%" id="td_invoice">
						购买方电话：${ordInvoice.buyerTelephone!''}
					</td>
					<td width="33%">
						开户银行：${ordInvoice.bankAccount!''}
					</td>
					<td width="33%" >					
						开户银行账号：${ordInvoice.accountBankAccount!''}
					</td>
				</tr>
				<tr >
					<td width="33%" id="td_invoice">
						发票ID：${ordInvoice.ordInvoiceId!''}
					</td>
					<td width="33%">
						发票号：${ordInvoice.invoiceNo!''}
					</td>
					<td width="33%" >					
						发票金额：<span id="td_price">${ordInvoice.amountYuan!''}</span>
					</td>
				</tr>
				<tr>
					<td>发票内容：<span id="td_detail">${ordInvoice.content!''}</span></td>
					<td>发票状态：${ordInvoice.getZhStatus()!''}</td>
					<td>备注：${ordInvoice.memo!''}</td>
				</tr>
				<tr>
					<td>申请时间：${ordInvoice.createTime?string('yyyy-MM-dd')!''}</td>
					<td>出票时间：<#if ordInvoice.billDate?? >${ordInvoice.billDate?string('yyyy-MM-dd')}</#if> </td>
					<td colspan="3">送货方式：${ordInvoice.getZhDeliveryType()!''}</td>
				</tr>
				<tr>
					<td>快递单号：${ordInvoice.expressNo!''}</td>
					<td>快递状态：${ordInvoice.getZhDeliverStatus()!''}</td>
				</tr>
			</table>
			<div class="iframe_content">
			<table class="p_table table_center">
					<tbody>
						<tr>
							<td height="20" align="center" style="font-size: 14px;" colspan="6">
								订单列表
							</td>
						</tr>
						<tr>
							<th>订单号</th>
							<th>产品名称</th>
							<th>游玩时间</th>
							<th>应付金额</th>
							<th>奖金支付金额</th>
							<th>去除奖金部分实付金额</th>
						</tr>
						<#list orderList as ordOrder>
						<tr>
							<td>${ordOrder.orderId!''}</td>
							<td>
						         <#if orderItemMap??>
						         <#if orderItemMap["inv_"+ordOrder.orderId]??>
					                 <#list orderItemMap["inv_"+ordOrder.orderId] as Item>
                                        <div> ${Item.productName!''}&nbsp; x ${Item.quantity!''}</div> 
                                     </#list>
					             </#if>
						         </#if>
							</td>
							<td>${ordOrder.visitTime?string('yyyy-MM-dd')!''}</td>
							<td>${ordOrder.getOughtAmountYuan()!''}</td>
							<td>${ordOrder.BonusAmountYuan()!''}</td>
							<td>${ordOrder.getActualPayExcludeBonusPaidAmountYuan()!''}</td>
						</tr>
						</#list>
					</tbody>
			</table>
			<div style="margin-top:20px;">			 
			 <#if ordInvoice.deliveryType == 'SELF'>
			     <h3 style="color:red">未添加送货地址</h3>
			 <#else>
			 <table class="p_table table_center">
			 	<tr>
			 		<td bgcolor="#eeeeee" width="70">收件人:</td><td bgcolor="#ffffff">${ordInvoice.deliveryAddress.fullName!''}</td>
			 	</tr>
			 	<tr>
			 		<td bgcolor="#eeeeee">手机号码:</td><td bgcolor="#ffffff">${ordInvoice.deliveryAddress.mobile!''}</td>
			 	</tr>
			 	<tr>
			 		<td bgcolor="#eeeeee" >地址:</td><td bgcolor="#ffffff">${ordInvoice.deliveryAddress.addressList[0].province!''} ${ordInvoice.deliveryAddress.addressList[0].city!''} ${ordInvoice.deliveryAddress.addressList[0].street!''}</td>
			 	</tr>
			 	<tr>
			 		<td bgcolor="#eeeeee" >邮编:</td><td bgcolor="#ffffff">${ordInvoice.deliveryAddress.addressList[0].postalCode!''}</td>
			 	</tr>
			 </table>
			 </#if>
			 </div>
			</div>
			<#if changInvoiceNo == false>
			<div>
				变更发票号<input type="hidden" name="invoiceId" value="${ordInvoice.ordInvoiceId!''}"/>
					<table >
						<tr>
							<td>&nbsp;&nbsp;&nbsp;发票号:</td>
							<td><input type="text" name="invoiceNo" value="${ordInvoice.invoiceNo!''}"/></td>
							<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
							<td><input type="button" id="updateInvoiceNo" value="更新"/></td>
						</tr>
					</table>
				
			</div>
			</#if>
			<#if ordInvoice.status=='BILLED'>
			<div>
				<form onsubmit="return false" id="expressForm"><input type="hidden" name="invoiceId" value="${ordInvoice.ordInvoiceId!''}"/>
					<table>
						<tr>
							<td>快递单号:</td><td><input type="text" name="expressNo"/></td>
							<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
							<td><input type="button" class="express_input" value="更新"/></td>
						</tr>
					</table>
				</form>
			</div>
			</#if>
			<div style="margin:10px 20px;text-align: center;">
			<#if ordInvoice.status=='UNBILL'>
				<button class="change_curr_status" invoiceId="${ordInvoice.ordInvoiceId!''}" ok_status="APPROVE">审核通过</button>
		    </#if>
			<#if ordInvoice.status!='COMPLETE'&&ordInvoice.status!='CANCEL' && ordInvoice.status!='UNBILL'>
			    <!--#if ordInvoice.status!='UNBILL'&& ordInvoice.status!='POST'-->
                <#if ordInvoice.status='APPROVE'>
                     <button class="printInvoiceBtn" invoiceId="${ordInvoice.ordInvoiceId!''}">打印发票</button>
                </#if>
				<button class="change_curr_status" ok_status="COMPLETE" invoiceId="${ordInvoice.ordInvoiceId!''}">完成</button>
			</#if>
			
			<table class="p_table table_center">
					<tr>
						<th height="30">
							日志名称
						</th>
						<th>
							内容
						</th>
						<th>
							操作人
						</th>
						<th>
							创建时间
						</th>
						<th>
							备注
						</th>
					</tr>
					<#if comLogList??>
						<#list comLogList as log>
                            <tr align="center">
                                <td height="25">
								${log.logName!'' }
                                </td>
                                <td>
								${log.content!'' }
									<#if log.logType=='cancelToCreateNew_new'>
                                        老订单ID${log.parentId!''}
									</#if>
									<#if log.logType=='cancelToCreateNew_original'>
                                        新订单ID${log.parentId!''}
									</#if>
                                </td>
                                <td>
								${log.operatorName!'' }
                                </td>
                                <td>
								${log.createTime?string('yyyy-MM-dd')!''}
                                </td>
                                <td>
								${log.memo!'' }
                                </td>
                            </tr>
						</#list>
					</#if>
				</table>
			</div>
			<object id="invoiceActiveX" classid="clsid:B7ED28F2-4843-4A57-98B0-52045508D6BD" style="display:none"></object>
		</div>
	</body>
	<#include "/base/foot.ftl"/>
</html>