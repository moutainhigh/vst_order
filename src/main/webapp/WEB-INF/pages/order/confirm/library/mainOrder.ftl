<div id="result" class="iframe_content mt20">
	<#if resultPage?? >
	    <#if resultPage.items?size gt 0 >
	            <div class="p_box">
	                <table class="p_table table_center" id="taskListTable">
	                    <thead>
	                        <tr>
	                            <th>订单号</th>
	                            <th>产品信息</th>
	                            <th>入离时间</th>
	                            <th>客人</th>
                                <th>订单金额</th>
	                            <th>入库时长</th>
	                            <th>主单预订通知</th>
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
	                                    <td width="450">${result.orderMonitorRst.productName!''}</td>
	                                    <td>${result.orderMonitorRst.visitTime}</td>
	                                    <td>${result.orderMonitorRst.contactName}</td>
                                        <#if result.orderMonitorRst.settlementPrice?? && result.orderMonitorRst.settlementPrice !=''>
                                            <td width="150">应收款：￥${(result.orderMonitorRst.settlementPrice?number/100)?string('#0.00')}
                                            <br>已收款：￥${(result.orderMonitorRst.actualSettlementPrice?number/100)?string('#0.00')}
                                            </td>
                                        <#else>
                                            <td></td>
                                        </#if>
	                                    <td>${result.auditCreateTime}</td>
                                        <td><a href="javaScript:void();" title="">预订通知-订单超时取消且支付成功</a></td>
                                        <td width="280">
                                            <form action="" method="post">
                                                <input type="hidden" name="orderId" value=${result.orderMonitorRst.orderId}>
                                                <input type="hidden" name="orderItemId" value=${result.orderMonitorRst.orderItemId}>
                                                <table class="e_table form-inline ">
                                                    <tbody>
                                                    <div style="float: left;">
                                                        <textarea class="textarea" autocomplete="off" placeholder="备注" name="orderMemo" rows="8" cols="15" maxlength="2000" >${result.orderMonitorRst.orderMemo!''}</textarea>
                                                    </div>
                                                    <div style="float: left;">
                                                        <input name="confirmStatus" type="hidden" autocomplete="off" value="${result.orderMonitorRst.orderItemConfirmStatus!''}" />
                                                        <div class="fl operate">
                                                            &nbsp;&nbsp;&nbsp;<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:updateMainOrderMemo(this)" style="text-decoration:none;cursor:pointer;">保存</a>
                                                        </div>
                                                    </div>
                                                    </tbody>
                                                </table>
                                            </form>
                                        </td>
                                        <td width="140">
                                        	<div class="fl operate"">
	                                      		<a class="btn btn_cc1" href="javascript:void(0)" onclick="javascript:orderPassMainAudit(this,'${result.auditId}');" style="text-decoration:none;cursor:pointer;">审核通过</a>
	                                      	</div>
                                			<br/><br/>
                                      		<div style="float: left;margin-top: 5px;">
<#--
                                                <a class="btn btn_cc1" target="_blank" href="/super_back/ord/sale/OrdSaleAddJump.zul?orderId=${result.orderMonitorRst.orderId}&sysCode=VST" style="text-decoration:none;cursor:pointer;">售后服务</a>
-->
                                                <a class="btn btn_cc1" target="_blank" href="/sales_front/ord/sale/toOrdSale/${result.orderMonitorRst.orderId}" title="售后服务">售后</a>
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