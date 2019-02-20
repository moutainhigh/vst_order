<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
<div id="logResultList" class="divClass">
<div class="order_msg clearfix">
	            	 【子订单】  &nbsp;&nbsp;&nbsp;&nbsp; ${orderItem.productName}-${orderItem.contentMap['branchName']}
</div>
<table>
                        <tbody>
                        	
                            <tr>
                                <td class="e_label w10">商品名称：</td>
                                <td>
                                 ${orderItem.suppGoodsName}
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label">供应商：</td>
                                <td><a  href="javaScript:" id="supplierName" >[  ${suppSupplier.supplierName}  ]</a></td>
                            </tr>
                            <tr>
                            	<td class="e_label">供应商电话：</td>
                                <td><a href="http://localhost:12366/ipcc/default.jsp?webcallout&webcalloutno=${suppSupplier.tel}" title="点击号码外呼">[  ${suppSupplier.tel}  ]</a></td>
                                
                            </tr>
                            
                            <tr>
                                <td class="e_label">客人姓名：</td>
                                <td><strong>[${travellerNum!'0'}人]</strong>${travellerName!''}</td>
                            </tr>
                            
                            <tr>
                                <td class="e_label">出游日期：</td>  
                                <td><strong>[${arrivalDays}天]</strong> ${order.visitTime?string('yyyy-MM-dd') !''} 至 ${endSailingDate !''} </td>
                            </tr>
                            
                            <tr>
                                <td class="e_label">特殊要求：</td>
                                <td>${order.remark!''}</td>
                            </tr>
                           
                        </tbody>
                    </table> 
                    
                <table class="p_table table_center mt5" style="margin-top: 200px">
                <thead>
                    <tr>
                        <th width="50">商品名称</th>
                        
                        <#if categoryCode== "category_cruise"> 
                          <th width="50">入住人数</th>
                          <th width="50">房间数</th>
                        <#else>
                           <th width="50">出游人数</th>
                        </#if>
                        <th width="90">出游日期</th>
                        <th width="100">驴妈妈单价</th>
                        <th width="100">子订单实付总金额</th>
                        <th width="100">优惠分摊总金额</th>
                        <th width="100">促销分摊总金额</th>
                        <th width="100">分销渠道减少分摊总金额</th>
                        <th width="100">订单金额减少分摊总金额</th>
                        <th width="100">支付立减分摊总金额</th>
                        <th width="100">子订单退款总金额</th>
                        <th width="100">退款份数</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>
                        ${orderItem.suppGoodsName}
                        </td>
                        <td>
                        ${travellerNum!'0'} 人
                        </td>
						
						<#if categoryCode== "category_cruise">
                            <td>
	                         ${orderItem.quantity!''}
							</td>
						 </#if>
                        <td>
                            ${order.visitTime?string('yyyy-MM-dd') !''}</br>${endSailingDate !''}
                        </td>
                        <td>
                        ${lvmamaPrice!''}
                  		</td>
                        <td>
                        <#if (orderItemApportionInfo.itemActualPaidApportionByPriceTypeMap)?? && orderItemApportionInfo.itemActualPaidApportionByPriceTypeMap?size gt 0>
                            <#list orderItemApportionInfo.itemActualPaidApportionByPriceTypeMap?values as priceTypeVO>
                            ${priceTypeVO.declaration!''}<#if (priceTypeVO.declaration)?? && priceTypeVO.declaration != "">:</#if>${priceTypeVO.price/100}<br/>
                            </#list>
                        <#else>
                            0元
                        </#if>
                        </td>
                        <td>
                        <#if (orderItemApportionInfo.itemCouponApportionByPriceTypeMap)?? && orderItemApportionInfo.itemCouponApportionByPriceTypeMap?size gt 0>
                            <#list orderItemApportionInfo.itemCouponApportionByPriceTypeMap?values as priceTypeVO>
                            ${priceTypeVO.declaration!''}<#if (priceTypeVO.declaration)?? && priceTypeVO.declaration != "">:</#if>${priceTypeVO.price/100}<br/>
                            </#list>
                        <#else>
                            0元
                        </#if>
                        </td>
                        <td>
                        <#if (orderItemApportionInfo.itemPromotionApportionByPriceTypeMap)?? && orderItemApportionInfo.itemPromotionApportionByPriceTypeMap?size gt 0>
                            <#list orderItemApportionInfo.itemPromotionApportionByPriceTypeMap?values as priceTypeVO>
                            ${priceTypeVO.declaration!''}<#if (priceTypeVO.declaration)?? && priceTypeVO.declaration != "">:</#if>${priceTypeVO.price/100}<br/>
                            </#list>
                        <#else>
                            0
                        </#if>
                        </td>
                        <td>
                        <#if (orderItemApportionInfo.itemDistributorApportionByPriceTypeMap)?? && orderItemApportionInfo.itemDistributorApportionByPriceTypeMap?size gt 0>
                            <#list orderItemApportionInfo.itemDistributorApportionByPriceTypeMap?values as priceTypeVO>
                            ${priceTypeVO.declaration!''}<#if (priceTypeVO.declaration)?? && priceTypeVO.declaration != "">:</#if>${priceTypeVO.price/100}<br/>
                            </#list>
                        <#else>
                            0元
                        </#if>
                        </td>
                        <td>
                        <#if (orderItemApportionInfo.itemManualChangeApportionByPriceTypeMap)?? && orderItemApportionInfo.itemManualChangeApportionByPriceTypeMap?size gt 0>
                            <#list orderItemApportionInfo.itemManualChangeApportionByPriceTypeMap?values as priceTypeVO>
                            ${priceTypeVO.declaration!''}<#if (priceTypeVO.declaration)?? && priceTypeVO.declaration != "">:</#if>${priceTypeVO.price/100}<br/>
                            </#list>
                        <#else>
                            0元
                        </#if>
                        </td>
                         <td>	
                        	销售价:${((orderItemApportionInfo.itemPayReductionAmount)!0)/100}
                        </td>
                        <td>
                        <#if (orderItemApportionInfo.itemRefundByPriceTypeMap)?? && orderItemApportionInfo.itemRefundByPriceTypeMap?size gt 0>
                            <#list orderItemApportionInfo.itemRefundByPriceTypeMap?values as priceTypeNode>
                            ${priceTypeNode.declaration!''}<#if (priceTypeNode.declaration)?? && priceTypeNode.declaration != "">:</#if>${priceTypeNode.amount/100}<br/>
                            </#list>
                        <#else>
                            0元
                        </#if>
                        </td>
                        <td>
                        <#if (orderItemApportionInfo.itemRefundByPriceTypeMap)?? && orderItemApportionInfo.itemRefundByPriceTypeMap?size gt 0>
                            <#list orderItemApportionInfo.itemRefundByPriceTypeMap?values as priceTypeNode>
                            ${priceTypeNode.declaration!''}<#if (priceTypeNode.declaration)?? && priceTypeNode.declaration != "">:</#if>${priceTypeNode.quantity}<br/>
                            </#list>
                        <#else>
                            0份
                        </#if>
                        </td>
                    </tr>
                </tbody>
            </table>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>
<script type="text/javascript">

var viewSupplierDialog;
        $("#supplierName").bind("click",function(){
			viewSupplierDialog = new xDialog("/vst_back/supp/supplier/showViewDistrict.do",{"supplierId":"${suppSupplier.supplierId}","suppGoodsId":"${orderItem.suppGoodsId}"},{title:"查看供应商",width:800});
		});
         
 </script>
