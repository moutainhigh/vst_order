<div id="result" class="iframe_content mt20">
	<#if resultPage?? >
	    <#if resultPage.items?size gt 0 >
	            <div class="p_box">
	                <table class="p_table table_center" id="taskListTable">
	                    <thead>
	                        <tr>
	                            <th>订单号</th>
	                            <th>商品信息</th>
	                            <th>入离时间</th>
	                            <th>客人</th>
                                <th>房价</th>
                                <th>客人备注</th>
                                <th>入库时长</th>
                                <th>通知</th>
                                <th>预订通知内容</th>
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
	                                    </td>
                                        <td>${result.comMessage.messageContent}</td>
                                        <td width="280">
                                            <form action="" method="post">
                                                <input type="hidden" name="orderItemId" value=${result.objectId}>
                                                <input type="hidden" name="orderId" value=${result.orderMonitorRst.orderId}>
                                                <table class="e_table form-inline ">
                                                    <tbody>
                                                    <div style="float: left;">
                                                        <textarea class="textarea" autocomplete="off" placeholder="备注" name="orderMemo" rows="8" cols="15" maxlength="2000" >${result.orderMonitorRst.orderItemMemo!''}</textarea>
                                                    </div>
                                                    <div style="float: left;">
                                                        <input name="confirmStatus" type="hidden" autocomplete="off" value="${result.orderMonitorRst.orderItemConfirmStatus!''}" />
                                                        <div class="fl operate">
                                                            &nbsp;&nbsp;&nbsp;<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:updateConfirmStatus(this, '${result.orderMonitorRst.orderItemConfirmStatus!''}')" style="text-decoration:none;cursor:pointer;">保存</a>
                                                        </div>
                                                    </div>
                                                    </tbody>
                                                </table>
                                            </form>
                                        </td>
                                        <td width="140">
                                        	<div class="fl operate"">
	                                      		<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:orderPassOtherAudit(this,'${result.auditId}');" style="text-decoration:none;cursor:pointer;">审核通过</a>
	                                      	</div>
                                			<br/><br/>
                                        	<div class="fl operate"">
	                                      		<a class="btn btn_cc1 <#if result.orderMonitorRst.categoryType == 'category_hotel'>hotel_sendOderFax<#else>sendOderFax</#if>" href="javascript:void(0)" style="text-decoration:none;cursor:pointer;">发送凭证</a>
	                                      	</div>
                            				</br>

                            				<div style="float: left;margin-top: 5px;">
                               					 <a class="btn btn_cc1" target="_blank" href="/vst_order/ord/order/confirm/querySameSupplierOrderList.do?checkedTab=${checkedTab}&isDelay=N&bespokeOrder=&mainCheckedTab=MYDESTTASK&mainTab=&operatorName=${operatorName!''}&supplierId=${result.orderMonitorRst.supplierId}">同供应商</a>
                           					 </div>
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