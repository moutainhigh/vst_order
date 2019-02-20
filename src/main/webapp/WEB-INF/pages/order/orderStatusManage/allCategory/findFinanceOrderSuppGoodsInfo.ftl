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
	<#if (bizCategory.categoryId)?? && bizCategory.categoryId = 41>
                       【子订单】  &nbsp;&nbsp;&nbsp;&nbsp; ${(orderItem.productName)!''}-${(orderItem.suppGoodsName)!''}(${(orderItem.productId)!''})
	<#else>
                       【子订单】  &nbsp;&nbsp;&nbsp;&nbsp; ${(orderItem.productName)!''}-${orderItem.contentMap['branchName']}(${(orderItem.productId)!''})
	</#if>
</div>
<table>
                        <tbody>
                        	<tr>
                                <td class="e_label w10">产品品类：</td>
                                <td>
                                 ${bizCategory.categoryName}
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">供应商产品名称：</td>
                                <td>
                                 ${orderItem.suppProductName!''}
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">产品ID：</td>
                                <td>
                                 ${orderItem.productId}
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">商品ID：</td>
                                <td>
                                 ${orderItem.suppGoodsId}
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">商品名称：</td>
                                <td>
                                 ${orderItem.suppGoodsName}
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">有效期限：</td>
                                <td>
                                ${financeInterestsBonusVo.consumLimit}天
                                </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">结算状态：</td>
                                <td>
                                 ${settlementStatusStr}
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
                        </tbody>
                    </table>
                	<br/>
                	<br/>
                	<br/>
                	<br/>
	                <table class="p_table table_center mt5">
	                <thead>
	                    <tr>
	                        <th width="120">商品名称</th>
	                        <th width="120">预订份数</th>
	                        <th width="120">赠送权益金</th>
							<th width="120">子订单实付总金额</th>
                            <th width="120">优惠分摊总金额</th>
                            <th width="120">促销分摊总金额</th>
                            <th width="120">子订单退款总金额</th>
	                    </tr>
	                </thead>
	                <tbody>
	                    <tr>
	                        <td>
			                     ${orderItem.suppGoodsName}
	                        </td>
	                        <td>
	                         ${orderItem.quantity!''}份
							</td>
	                  		<td>
	                  			${orderItem.rightPrice}
	                  		</td>
                            <td>
                                ${buyItemTotalPrice}
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
	//重要信息提示
	var productInfoDialog;
	$('#productInfo').bind("click",function(){
		productInfoDialog = new xDialog("/vst_order/order/orderManage/showOrderProductDetail.do?orderId=" + ${order.orderId!''} + "&orderItemId=" + ${orderItem.orderItemId!''},{},{title:"重要提示",width:1200,iframe:true});
	});

var viewSupplierDialog;
        $("#supplierName").bind("click",function(){
			viewSupplierDialog = new xDialog("/vst_back/supp/supplier/showViewDistrict.do",{"supplierId":"${suppSupplier.supplierId}","suppGoodsId":"${orderItem.suppGoodsId}"},{title:"查看供应商",width:800});
		});
var viewUpdateSuppTravellerNumDialog;
var orderItemListSize = '${order.orderItemList?size}';       
        var idArray = [];
        var pidArray;
        if(orderItemListSize > 0)  
        {  
          <#list order.orderItemList as orderItem >  
          if( 15 == '${orderItem.categoryId}' || 16 == '${orderItem.categoryId}'){  
 				idArray.push(idArray, '${orderItem.productId}'); 
          }  
          </#list>  
          pidArray = idArray.toString();
        }
        $("#updateSuppTravellerNum").bind("click",function(){
			viewUpdateSuppTravellerNumDialog = new xDialog("/vst_back/prod/groupDateAddtional/findProdGroupDateAddtionalList.do?productId=${orderItem.productId}&date=${vistTime!''}&pageType=main&mainProductId=${order.productId}&pidArray="+pidArray,{},{title:"供应商收客人数",iframe:true,width:680,hight:650,scrolling:"yes"});
		});
var viewPerformTimeDetailDialog;
        $("#findPerformTimeDetail").bind("click",function(){
            viewPerformTimeDetailDialog = new xDialog("/vst_order/order/orderManage/showPerformTimeDetail.do",{"orderItemId":"${orderItem.orderItemId}","performStatus":"${performStatus}"},{title:"使用状态",width:650});
        });
        
      //供应商物流信息(门票-邮寄)
      $("#supplierPost").bind("click",function(){
		new xDialog("/vst_order/order/orderManage/showSupplierPost.do",{"orderItemId":"${orderItem.orderItemId}"},{title:"备注",width:650});
      });
      
      //退款明细
      $("#queryRefundInfo").bind("click",function(){
        new xDialog("/vst_order/order/orderManage/queryRefundInfo.do",{"orderItemId":"${orderItem.orderItemId}"},{title:"退款明细",width:650});
      });
      
      $("#ebkEmailDetail").click(function(){
		new xDialog("/vst_order/order/orderManage/showEbkEmailLog.do",{"objectId":"${orderItem.orderItemId}","objectType":"EBK_SEND_EMAIL_MEMO","sysName":"VST"},{title:"EBK邮件发送明细",iframe:false,width:1050,iframeHeight:680,height:500,scrolling:"yes"});
      });
 </script>
