<div id="result" class="iframe_content mt20">
	<#if resultPage?? >
	    <#if resultPage.items?size gt 0 >
	            <div class="p_box">
	                <table class="p_table table_center" id="taskListTable">
	                    <thead>
	                        <tr>
	                            <th>订单号</th>
	                            <th>商品信息</th>
	                            <th>供应商</th>
	                            <th>入离时间</th>
	                            <th>客人</th>
	                            <th>房价</th>
                                <th>通知</th>
                                <th>备注信息</th>
	                            <th>操作</th>
	                        </tr>
	                    </thead>
	                    <tbody>
	                        <#list resultPage.items as result>
	                                <tr>
	                                    <td>
	                                    	<#if result.orderMonitorRst.stockFlag=='all'>
	                                            		保留<br>
	                                        <#elseif result.orderMonitorRst.stockFlag=='part'>
	                                        			部分保留<br>
	                                        <#else>
	                                            		非保留<br>
	                                        </#if>
	                                         <a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?objectId=${result.orderMonitorRst.orderId}&objectType=ORDER<#if result.auditTypeName=='催支付'>&isReminderPayment=cuizhifu</#if>" target="_blank">
	                                            ${result.orderMonitorRst.orderId}
	                                        </a>/
	                                        <a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?objectId=${result.objectId}&objectType=${result.objectType}<#if result.auditTypeName=='催支付'>&isReminderPayment=cuizhifu</#if>" target="_blank">
	                                            ${result.orderMonitorRst.orderItemId}
	                                        </a>
	                                        <#if result.orderMonitorRst.guarantee == 'GUARANTEE'>
	                                            <a title="该订单需要担保">#</a>
	                                        </#if>
	                                    </td>
	                                    <td>${result.orderMonitorRst.productName!''} ${result.orderMonitorRst.suppGoodsName!''}</td>
	                                    <td>
	                                    	${result.orderMonitorRst.belongBU!''} <br>
	                                    	<a  href="javaScript:void(0)" id="supplierName" onclick="viewSupplier('${result.orderMonitorRst.supplierId}','${result.orderMonitorRst.suppGoodsId}');">${result.orderMonitorRst.supplierName!''}</a>
	                                    </td>
	                                    <td>${result.orderMonitorRst.visitTime}</td>
	                                    <td>${result.orderMonitorRst.contactName}</td>
	                                    <#if result.orderMonitorRst.settlementPrice?? && result.orderMonitorRst.settlementPrice !=''>
	                                    <td>￥${(result.orderMonitorRst.settlementPrice?number/100)?string('#0.00')}
											<#if result.orderMonitorRst.categoryType == 'category_hotel'>
                                                <br/>${result.orderMonitorRst.quantity}间，${result.orderMonitorRst.arrivalDays}晚
											</#if>
                                        </td>
										</td>
	                                    <#else>
	                                    <td></td>
	                                    </#if>
	                                    <td>
                                            <form action="" method="post">
                                                <input type="hidden" name="orderItemId" value=${result.objectId}>
                                                <input type="hidden" name="orderMemo" value=${result.orderMonitorRst.orderItemMemo}>
                                            </form>
											<#if result.orderMonitorRst.isSupplierOrderItem=="Y">
                                                <a target="_blank" href="/lvmm_log/bizLog/showVersatileLogList?objectType=ORD_ORDER_ITEM&objectId=${result.orderMonitorRst.orderItemId}&sysName=VST", title="日志查看">查看</a>
											<#else> <a target="_blank" href="/vst_certif/ebooking/task/findEbkAndFaxList.do?orderId=${result.orderMonitorRst.orderId}&orderItemId=${result.orderMonitorRst.orderItemId}", title="凭证查询">查看</a>

											</#if>
	                                    	<#if result.orderMonitorRst.isSupplierOrderItem == "N" && result.orderMonitorRst.orderSubType != "STAMP" && result.orderMonitorRst.ebkFaxCount gt 0>
	                                    	<a href="javaScript:showFaxRecv('${result.orderMonitorRst.orderId}','${result.orderMonitorRst.orderItemId}','${result.orderMonitorRst.certifId}');" title="查看回传">查看回传(${result.orderMonitorRst.ebkFaxCount!0})</a>
	                                    	</#if>
	                                    </td>
                                        <td width="280">
                                            <form action="/vst_order/ord/order/updateOrderItemConfirmStatus.do" method="post">
                                                <input type="hidden" name="orderItemId" value=${result.objectId}>
                                                <table class="e_table form-inline ">
                                                    <tbody>
                                                    	<div style="float: left;">
                                                        <textarea class="textarea" autocomplete="off" placeholder="备注" name="orderMemo" rows="7" cols="15" maxlength="2000" >${result.orderMonitorRst.orderItemMemo!''}</textarea>
                                                    	</div>
														<div class="fl operate">
															&nbsp;&nbsp;&nbsp;<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:updateCancelMemo(this)" style="text-decoration:none;cursor:pointer;">保存</a>
														</div>
                                                    </tbody>
                                                </table>
                                            </form>
                                        </td>
                                        <td width="210">
                                        	<div class="fl operate"">
                                        		<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:orderCancelConfirm('${result.orderMonitorRst.orderItemId}','${result.auditId}')" style="text-decoration:none;cursor:pointer;">确认取消</a>
                                        	</div>
                                       		<div style="float: left;margin-top: 5px;">
<#--
                                        		<a class="btn btn_cc1" target="_blank" href="/super_back/ord/sale/OrdSaleAddJump.zul?orderId=${result.orderMonitorRst.orderId}&sysCode=VST" title="售后服务">售后</a>
-->
                                                <a class="btn btn_cc1" target="_blank" href="/sales_front/ord/sale/toOrdSale/${result.orderMonitorRst.orderId}" title="售后服务">售后</a>
												<a class="btn btn_cc1" href="javaScript:uploadOrderAttachment('${result.orderMonitorRst.orderId}','${result.orderMonitorRst.orderItemId}')" title="上传附件">上传</a>
                                        		<a class="btn btn_cc1" href="javaScript:showLog('${result.orderMonitorRst.orderItemId}')" title="查看日志">日志</a>
                                        	</div>
											<#if (result.orderMonitorRst.categoryType == 'category_hotel' || result.orderMonitorRst.categoryType == 'category_route_hotelcomb') && result.orderMonitorRst.stockFlag != 'all'>
												<div style="float: left;margin-top: 5px;">
													<a class="btn btn_cc1" title="电话确认" <#if result.orderMonitorRst.isHandleSupplier == 'Y'>disabled="true"<#else> onclick="javaScript:handleSupplier(this)"</#if>>电话确认</a>
												</div>
											</#if>

                                        </td>
	                                </tr>
	                        </#list>
	                    </tbody>
	                </table>
	                <#--分页标签-->
	                <@pagination.paging resultPage/>
			</div>
		<#else>
			<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关订单，请重新输入相关条件查询！</div>
		</#if>
	</#if>
</div>