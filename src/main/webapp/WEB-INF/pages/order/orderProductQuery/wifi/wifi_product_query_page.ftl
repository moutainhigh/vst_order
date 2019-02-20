<#import "/base/pagination.ftl" as pagination>
<#if result??>
<#if result.items?? &&  result.items?size &gt; 0>
			<#list result.items as product> 
			<div class="p_box">
				<div class="p_box">   
                <div class="co_title">
	               <strong class="f14 cc8">
	               ${product.productName!''}
	                </strong>
                </div>
                <table class="co_p_table">
                    <tbody>
                    	<tr>
                        <td>
                            <table class="co_table">
                                <thead> 
                                    <tr class="noborder">
                                        <th nowrap="nowrap" style="width:20%">产品Id</th>
                                        <th nowrap="nowrap" style="width:20%">产品类型</th>
                                        <th nowrap="nowrap" style="width:20%">商品名称</th>
                                        <th nowrap="nowrap" style="width:20%">支付对象</th>
                                        <th nowrap="nowrap" style="width:5%">操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                		<#if product.recommendSuppGoodsList??>
                                		<#list product.recommendSuppGoodsList as suppGoods>
                                		<tr>
                                			<td>
                                				 ${suppGoods.productId!''}
                                			</td>
                                			<td>
                                				 <#if product.productType=='WIFI'>
                                				 WIFI
	                                        	<#elseif product.productType=='PHONE'>
	                                        	电话卡
	                                        	</#if>
                                			</td>
                                			<td class="co_unline">
                                			    
                                				<a class="J_tip" tip-content=""  href="javascript:void(0);">${suppGoods.goodsName!''}</a>
                                				<div style="display:none;">
                                				</div>
                                			</td>
                                			<td>
	                                        	<#if suppGoods.payTarget=='PREPAID'>预付
	                                        	<#elseif suppGoods.payTarget=='PAY'>现付
	                                        	</#if>
	                                        </td>
	                                         <td>
	                                        	<input type="button" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #000000;font-family: 'Applied Font';" 
	                                        	name="goodsId" value="预订" onclick="toBook('/vst_order/ord/book/wifi/infoFillIn.do?goodsId=${suppGoods.suppGoodsId}');">
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
		</div>
		</#list>
		</#if>
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
	<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关wifi产品，请重新输入相关条件查询！</div>
</#if>
