<#import "/base/pagination.ftl" as pagination>
<#import "/base/hotel_func.ftl" as hf>
<#if result??>
<#if result.items?? &&  result.items?size &gt; 0>
			<div class="p_box">
				<#list result.items as ticketProductMap> 
				<div class="p_box">   
                <div class="co_title">
                	<#if ticketProductMap['product'].categoryId=="12">
		            	<a title="查看门票前台详情" href="http://ticket.lvmama.com/p-${ticketProductMap['product'].productId}" target="_blank">
		            <#else>
		            	<a title="查看门票前台详情" href="http://www.lvmama.com/vst_front/scenic-${ticketProductMap['product'].urlId}" target="_blank">
		            </#if>
	                	<strong class="f14 cc8">
	                		 [${ticketProductMap['product'].districtName!''}]${ticketProductMap['product'].productName!''}
	                	</strong>
	                </a>
	                 
	                <span class="lt50">地址：${ticketProductMap['product'].address!''}</span>
                </div>
                <table class="co_p_table">
                    <tbody>
                    	<tr>
                        <td>
                            <table class="co_table">
                                <thead> 
                                    <tr class="noborder">
                                        <th nowrap="nowrap" style="width:5%;">推荐级别</th>
                                        <th nowrap="nowrap" style="width:20%">产品Id</th>
                                        <th nowrap="nowrap" style="width:20%">名称</th>
                                        <th nowrap="nowrap" style="width:20%">支付对象</th>
                                        <th nowrap="nowrap" style="width:5%">当天订时间</th>
                                        <th nowrap="nowrap" style="width:5%">退改规则</th>
                                        <th nowrap="nowrap" style="width:5%">操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                	<#if ticketProductMap['goods']??>
                                	<#list ticketProductMap['goods'] as goodsIndexBean> 
                                		<tr>
                                			<td>
                                				${ticketProductMap['product'].recommendLevel!''}
                                			</td>
                                			<td>
                                				 ${goodsIndexBean.productId!''}
                                			</td>
                                			<td class="co_unline">
                                			    
                                				<a class="J_tip" tip-content="" onmouseover="goodsDetailMouseover(this,${goodsIndexBean.suppGoodsId});" href="javascript:void(0);">${ticketProductMap['product'].productName!''} ${goodsIndexBean.goodsName!''}</a>
                                				<div style="display:none;">
                                					取票方式：
                                					<#if goodsIndexBean.goodsType=="EXPRESSTYPE_DISPLAY">
                                						实体商品
                                						<#if goodsIndexBean.expressType=="LVMAMA">
	                                						驴妈妈-上海
	                                					<#elseif goodsIndexBean.expressType=="SUPPLIER">
	                                						供应商
	                                					<#elseif goodsIndexBean.expressType=="LVMAMA_BJ">
	                                						驴妈妈-北京
	                                					<#elseif goodsIndexBean.expressType=="LVMAMA_CD">
	                                						驴妈妈-成都
	                                					<#elseif goodsIndexBean.expressType=="LVMAMA_SY">
	                                						驴妈妈-三亚
	                                					<#elseif goodsIndexBean.expressType=="LVMAMA_GZ">
	                                						驴妈妈-广州
	                                					</#if>
                                					<#elseif goodsIndexBean.goodsType=="NOTICETYPE_DISPLAY">
                                						虚拟商品
                                					<#else>
                                						${goodsIndexBean.goodsType}
                                					</#if>
                                					<#if goodsIndexBean.goodsType=="NOTICETYPE_DISPLAY">
	                                					<#if goodsIndexBean.noticeType=="EMAIL">
	                                						/邮件
	                                					<#elseif goodsIndexBean.noticeType=="SMS">
	                                						/普通短信
	                                					<#elseif goodsIndexBean.noticeType=="QRCODE">
	                                						/二维码
	                                					<#else>
	                                						/${goodsIndexBean.noticeType}
	                                					</#if>
                                					</#if>
                                				</div>
                                			</td>
                                			<td>
	                                        	<#if goodsIndexBean.payTarget=='PREPAID'>预付
	                                        	<#elseif goodsIndexBean.payTarget=='PAY'>现付
	                                        	</#if>
	                                        </td>
	                                        <td>
	                                        	${goodsIndexBean.getAheadBookTimeToStr()}
	                                        </td>
	                                        <td>
	                                        	<a  class="J_tip" tip-content="" href="javascript:void(0);" onmouseover="initCancelStrategy(this,${goodsIndexBean.suppGoodsId},'${goodsIndexBean.aperiodicFlag}');">查看退改</a>
	                                        </td>
	                                         <td>
	                                        	<input type="button" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #000000;font-family: 'Applied Font';" 
	                                        	name="goodsId" value="预订" onclick="toBook('/vst_order/ord/book/ticket/infoFillIn.do?goodsId=${goodsIndexBean.suppGoodsId}');">
	                                        </td>
                                		</tr>
                                	</#list>
                                	</#if>
                                	<#if ticketProductMap['suppgoods']??>
                                	<#list ticketProductMap['suppgoods'] as goodsIndexBean> 
                                		<tr>
                                			<td>
                                				${ticketProductMap['product'].recommendLevel!''}
                                			</td>
                                			<td>
                                				 ${goodsIndexBean.productId!''}
                                			</td>
                                			<td class="co_unline">
                                				<a class="J_tip" tip-content="" onmouseover="goodsDetailMouseover(this,${goodsIndexBean.suppGoodsId});" href="javascript:void(0);">${goodsIndexBean.goodsName!''}</a>
                                				<div style="display:none;">
                                					<#--<#if (goodsIndexBean.goodsDescs.changeAddress)??>
		                                        	 	取票地点：${goodsIndexBean.goodsDescs.changeAddress}<br/>
		                                        	</#if>-->
                                					取票方式：
                                					<#if goodsIndexBean.goodsType=="EXPRESSTYPE_DISPLAY">
                                						实体商品
                                						<#if goodsIndexBean.expressType=="LVMAMA">
	                                						驴妈妈-上海
	                                					<#elseif goodsIndexBean.expressType=="SUPPLIER">
	                                						供应商
	                                					<#elseif goodsIndexBean.expressType=="LVMAMA_BJ">
	                                						驴妈妈-北京
	                                					<#elseif goodsIndexBean.expressType=="LVMAMA_CD">
	                                						驴妈妈-成都
	                                					<#elseif goodsIndexBean.expressType=="LVMAMA_SY">
	                                						驴妈妈-三亚
	                                					<#elseif goodsIndexBean.expressType=="LVMAMA_GZ">
	                                						驴妈妈-广州
	                                					</#if>
                                					<#elseif goodsIndexBean.goodsType=="NOTICETYPE_DISPLAY">
                                						虚拟商品
                                					<#else>
                                						${goodsIndexBean.goodsType}
                                					</#if>
                                					<#if goodsIndexBean.goodsType=="NOTICETYPE_DISPLAY">
	                                					<#if goodsIndexBean.noticeType=="EMAIL">
	                                						/邮件
	                                					<#elseif goodsIndexBean.noticeType=="SMS">
	                                						/普通短信
	                                					<#elseif goodsIndexBean.noticeType=="QRCODE">
	                                						/二维码
	                                					<#else>
	                                						/${goodsIndexBean.noticeType}
	                                					</#if>
                                					</#if>
                                				</div>
                                			</td>
                                			<td>
	                                        	<#if goodsIndexBean.payTarget=='PREPAID'>预付
	                                        	<#elseif goodsIndexBean.payTarget=='PAY'>现付
	                                        	</#if>
	                                        </td>
	                                        <td>
	                                        	${goodsIndexBean.getAheadBookTimeToStr()}
	                                        </td>
	                                        <td>
	                                        	<#--<#if goodsIndexBean.cancelStrategy=="RETREATANDCHANGE">
                            						可退改
                            					<#elseif goodsIndexBean.cancelStrategy=="UNRETREATANDCHANGE">
                            						不退不改
                            					<#elseif goodsIndexBean.cancelStrategy=="MANUALCHANGE">
                            						人工退改
                            					<#else>
                            						无 
                            					</#if>-->
                            					<a  class="J_tip" tip-content="" href="javascript:void(0);" onmouseover="initCancelStrategy(this,${goodsIndexBean.suppGoodsId},'${goodsIndexBean.aperiodicFlag}');">查看退改</a>
	                                        </td>
	                                         <td>
	                                        	<input type="button" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #000000;font-family: 'Applied Font';" 
	                                        	name="goodsId" value="预订" onclick="toBook('/vst_order/ord/book/ticket/infoFillIn.do?goodsId=${goodsIndexBean.suppGoodsId}');">
	                                        </td>
                                		</tr>
                                	</#list>
                                	</#if>
                                	<#if ticketProductMap['product']??>
                                	<#list ticketProductMap['product'] as ticketIndexBean> 
                                		<tr>
                                			<td>
                                				${ticketIndexBean.recommendLevel!''}
                                			</td>
                                			<td>
                                				 ${ticketIndexBean.productId!''}
                                			</td>
                                			<td class="co_unline">
                                				<a class="J_tip" tip-content="" href="javascript:void(0);">${ticketIndexBean.productName!''}</a>
                                			</td>
                                			<td>
	                                        	预付
	                                        </td>
	                                        <td>
	                                        	 ${ticketIndexBean.getAheadBookTimeToStr()}
	                                        </td>
	                                        <td>
	                                        	<a class="J_tip" tip-content="" href="javascript:void(0);" onmouseover="initCombCancelStrategy(this,${ticketIndexBean.productId!''});">查看退改</a>
	                                        </td>
	                                         <td>
	                                        	<input type="button" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #000000;font-family: 'Applied Font';" 
	                                        	name="goodsId" value="预订" onclick="toBook('/vst_order/ord/book/ticket/infoFillIn.do?productId=${ticketIndexBean.productId}');">
	                                        </td>
                                		</tr>
                                	</#list>
                                	</#if>
                                	<#if ticketProductMap['lvmamaProduct']??>
                                	<#list ticketProductMap['lvmamaProduct'] as ticketIndexBean> 
                                		<tr>
                                			<td>
                                				${ticketIndexBean.recommendLevel!''}
                                			</td>
                                			<td>
                                				 ${ticketIndexBean.productId!''}
                                			</td>
                                			<td class="co_unline">
                                				<a class="J_tip" tip-content="" href="javascript:void(0);">${ticketIndexBean.productName!''}</a>
                                			</td>
                                			<td>
	                                        	预付
	                                        </td>
	                                        <td>
	                                        	  ${ticketIndexBean.getAheadBookTimeToStr()}
	                                        </td>
	                                        <td>
	                                        	 <a class="J_tip" tip-content="" href="javascript:void(0);" onmouseover="initCombCancelStrategy(this,${ticketIndexBean.productId!''});">查看退改</a>
	                                        </td>
	                                         <td>
	                                        	<input type="button" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #000000;font-family: 'Applied Font';" 
	                                        	name="goodsId" value="预订" onclick="toBook('/vst_order/ord/book/ticket/infoFillIn.do?productId=${ticketIndexBean.productId}');">
	                                        </td>
                                		</tr>
                                	</#list>
                                	</#if>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </tbody>
                </table>
            </div>
			</#list>
		</div>
		<@pagination.paging result true "#productListDiv">
			<script src="/vst_order/js/tooltip/js/jtip.js"></script>
			<script type="text/javascript">
				function loadJtip(){
						$('.J_tip').lvtip({
					        templete: 3,
					        place: 'bottom-left',
					        offsetX: 0,
					        events: "live" 
					    });
				}
				loadJtip();
			</script>
		</@pagination.paging>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关门票，请重新输入相关条件查询！</div>
	</#if>
<#else>
	<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关门票，请重新输入相关条件查询！</div>
</#if>