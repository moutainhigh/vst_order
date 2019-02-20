<div id="result" class="iframe_content mt20">
	<#if resultPage?? >
	    <#if resultPage.items?size gt 0 >
	            <div class="p_box" style="float: left; width: 100%;">
	                <table class="p_table table_center" id="taskListTable">
	                    <thead>
	                        <tr>
	                            <th>订单号</th>
	                            <th>商品信息</th>
	                            <th>供应商</th>
	                            <th>入离时间</th>
	                            <th>客人</th>
	                            <th>房价</th>
	                            <th>客人备注</th>
	                            <th>入库时长</th>
	                            <th>通知</th>
	                            <th>备注信息</th>
							    <#if monitorCnd.bespokeOrder !='Y'>	                            
	                            <th>操作</th>
	                            </#if>
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
	                                        <#if result.orderMonitorRst.distributionChannel=='taobao'>
	                                        	<br>淘宝
	                                        </#if>
	                                        <#if result.orderMonitorRst.distributionChannel=='O2O'>
	                                        	<br>O2O
	                                        </#if>
	                                        <#if result.orderMonitorRst.distributionChannel=='other'>
	                                        	<br>分销（不含淘宝）
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
                                        <#if result.orderMonitorRst.categoryType == 'category_route_hotelcomb'>
										<br/>${result.orderMonitorRst.quantity}份
                                        </#if>
                                        </td>
	                                    <#else>
	                                    <td></td>
	                                    </#if>
	                                    <td>
	                                    	<#if result.orderMonitorRst.remark ??>
	                                            <a href="javaScript:showCustomRemark('${result.orderMonitorRst.remark}')" title="${result.orderMonitorRst.remark}">查看</a>
	                                        </#if>
	                                    </td>
	                                    <td>${result.auditCreateTime}</td>
	                                    <td>
											<#if result.orderMonitorRst.isSupplierOrderItem=="Y">
                                                <a target="_blank" href="/lvmm_log/bizLog/showVersatileLogList?objectType=ORD_ORDER_ITEM&objectId=${result.orderMonitorRst.orderItemId}&sysName=VST", title="日志查看">查看</a>
											<#else> <a target="_blank" href="/vst_certif/ebooking/task/findEbkAndFaxList.do?orderId=${result.orderMonitorRst.orderId}&orderItemId=${result.orderMonitorRst.orderItemId}", title="凭证查询">查看</a>

											</#if>
											<#if result.orderMonitorRst.isSupplierOrderItem == "N" && result.orderMonitorRst.orderSubType != "STAMP" && result.orderMonitorRst.ebkFaxCount gt 0>
	                                    	<a href="javaScript:showFaxRecvBynew('${result.orderMonitorRst.orderId}','${result.orderMonitorRst.orderItemId}','${result.orderMonitorRst.certifId}',${result.orderMonitorRst.ebkFaxCount!0});" title="查看回传">查看回传(${result.orderMonitorRst.ebkFaxCount!0})</a>
	                                    	</#if>
											<#if result.orderMonitorRst.isSupplierOrderItem == "N" && result.orderMonitorRst.orderSubType != "STAMP" && result.orderMonitorRst.ebkMailCount gt 0>
	                                    	<a href="javaScript:showMailRecv('${result.orderMonitorRst.orderId}','${result.orderMonitorRst.orderItemId}','${result.orderMonitorRst.certifId}');" title="查看邮件回传">查看邮件回传(${result.orderMonitorRst.ebkMailCount!0})</a>
	                                    	</#if>
	                                    </td>
	                                	<td width="280">
	                                    	<form action="/vst_order/ord/order/updateOrderItemConfirmStatus.do" method="post">
	                                    		<input type="hidden" name="orderItemId" value=${result.objectId}>
	                                    		<table class="e_table form-inline ">
										            <tbody>
			                                    		<div style="float: left;">
			                                    			<#if result.orderMonitorRst.orderItemConfirmStatus=='SUCCESS'>
			                                    			<div style="display: inline;">
			                                    			<#else>
			                                    			<div style="display: none;">
			                                    			</#if>
			                                    				<input type="text" autocomplete="off" name="confirmId" placeholder="确认号" style="margin: 0 0 3px;"><br>
			                                    			</div>
			                                    			<textarea class="textarea" autocomplete="off" placeholder="备注" name="orderMemo" rows="8" cols="15" maxlength="2000" >${result.orderMonitorRst.orderItemMemo!''}</textarea>
			                                    		</div>
		                                    			<div style="float: left;">
		                                    				<select name="confirmStatus" autocomplete="off" class="w10" style="margin: 0 0 5px;" value="${result.orderMonitorRst.orderItemConfirmStatus!''}" onchange="confirmIdDisplay(this);" >
																<option value="INCONFIRM" <#if result.orderMonitorRst.orderItemConfirmStatus=='INCONFIRM'>selected="selected"</#if>>已审</option>
																<option value="FULL" <#if result.orderMonitorRst.orderItemConfirmStatus=='FULL'>selected="selected"</#if>>订单满房</option>
																<option value="PECULIAR_FULL" <#if result.orderMonitorRst.orderItemConfirmStatus=='PECULIAR_FULL'>selected="selected"</#if>>特殊满房</option>
																<option value="CHANGE_PRICE" <#if result.orderMonitorRst.orderItemConfirmStatus=='CHANGE_PRICE'>selected="selected"</#if>>订单变价</option>
																<option value="SUCCESS" <#if result.orderMonitorRst.orderItemConfirmStatus=='SUCCESS'>selected="selected"</#if>>确认成功</option>
															</select><br>
															<div class="fl operate">
								                				&nbsp;&nbsp;&nbsp;<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:updateConfirmStatus(this, '${result.orderMonitorRst.orderItemConfirmStatus!''}')" style="text-decoration:none;cursor:pointer;">保存</a>
								                			</div>	
		                                    			</div>
										            </tbody>
										         </table>
	                                    	</form>
	                                    </td>
                                       	<#if monitorCnd.bespokeOrder !='Y'>
                                        	<td width="210">
	                                        	<div class="fl operate"">                                        		
	                                        		<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:showDelayRemindTime('${result.auditId}')" style="text-decoration:none;cursor:pointer;">暂缓</a>
	                                        		<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:showCancelOrder('${result.objectId}')" style="text-decoration:none;cursor:pointer;">取消订单</a>
	                                        	</div>
	                                        	<div style="float: left;margin-top: 5px;">
<#--
		                                        		<a class="btn btn_cc1" target="_blank" href="/super_back/ord/sale/OrdSaleAddJump.zul?orderId=${result.orderMonitorRst.orderId}&sysCode=VST" title="售后服务">售后</a>
-->
                                                    	<a class="btn btn_cc1" target="_blank" href="/sales_front/ord/sale/toOrdSale/${result.orderMonitorRst.orderId}" title="售后服务">售后</a>

                                                    	<a class="btn btn_cc1" href="javaScript:uploadOrderAttachment('${result.orderMonitorRst.orderId}','${result.orderMonitorRst.orderItemId}')" title="上传附件">上传</a>
		                                        		<a class="btn btn_cc1" href="javaScript:showLog('${result.orderMonitorRst.orderItemId}')" title="查看日志">日志</a>
	                                        	</div>
                                            <#if result.orderMonitorRst.categoryType == 'category_hotel' || result.orderMonitorRst.categoryType == 'category_route_hotelcomb'>
                                                <div class="" style="float: left;margin-top: 5px;margin-right: 5px;">
                                                    <a class="btn btn_cc1" title="发送满房邮件" onclick="javaScript:updateConfirmStatusAndSendEmail(this, 'FULL')">发送满房邮件</a>
                                                </div>
                                                <div class="" style="float: left;margin-top: 5px;margin-right: 5px;">
                                                    <a class="btn btn_cc1" title="发送变价邮件" onclick="javaScript:updateConfirmStatusAndSendEmail(this, 'CHANGE_PRICE')">发送变价邮件</a>
                                                </div>
                                            </#if>
                                            <#if (result.orderMonitorRst.categoryType == 'category_hotel' || result.orderMonitorRst.categoryType == 'category_route_hotelcomb') && result.orderMonitorRst.stockFlag != 'all'>
                                                <div style="float: left;margin-top: 5px;">
                                                    <a class="btn btn_cc1" title="电话确认" <#if result.orderMonitorRst.isHandleSupplier == 'Y'>disabled="true"<#else> onclick="javaScript:handleSupplier(this)"</#if>>电话确认</a>
                                                </div>
                                            </#if>
                                            </br>

                                            <div style="float: left;margin-top: 5px;">
                                                <a class="btn btn_cc1" target="_blank" href="/vst_order/ord/order/confirm/querySameSupplierOrderList.do?checkedTab=${checkedTab}&isDelay=N&bespokeOrder=&mainCheckedTab=MYDESTTASK&mainTab=&operatorName=${operatorName!''}&supplierId=${result.orderMonitorRst.supplierId}">同供应商</a>
                                            </div>
                                            </td>
										</#if>
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