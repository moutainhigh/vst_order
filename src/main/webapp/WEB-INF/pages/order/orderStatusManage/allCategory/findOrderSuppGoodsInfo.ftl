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
                                 <a href="/vst_order/order/orderManage/goodsInventoryChange.do?suppGoodsId=${orderItem.suppGoodsId}&productId=${orderItem.productId}&categoryId=${bizCategory.categoryId}" target="_blank">[商品库维护]</a>
                                 </td>
                            </tr>
                            
                            <#if bizCategory.categoryId == 1> 
                                <tr>
                                   <td class="e_label w10">酒店联系电话：</td>
                                   <td>${order.hotelTel}</td>
                                </tr>
                            </#if> 
                            
                            <tr>
                                <td class="e_label w10">供应商产品名称：</td>
                                <td>
                                 ${orderItem.suppProductName!''}
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">商品名称：</td>
                                <td>
                                 ${orderItem.suppGoodsName}
                                 <#if specialTicketType =="SHOW_TICKET">(${goodsSpecName})</#if>
                                 </td>
                            </tr>
                            <#if goodsExpiryDate??>
                            <tr>
                                <td class="e_label w10">有效期限：</td>
                                <td>
                                ${goodsExpiryDate}
                                </td>
                            </tr>
				            </#if>
				            <#if goodsUnvalidDate??>
				            <tr>
                                <td class="e_label w10">不适用日期：</td>
                                <td>
                                ${goodsUnvalidDate}
                                </td>
                            </tr>
				            </#if>
                            
                             <#if bizCategory.categoryId == 15 || bizCategory.categoryId == 16> 
		                            <tr>
		                                <td class="e_label w10">行程类型：</td>
		                                <td>
		                                 ${prodProductTourType}	                                
		                                </td>
                             </tr>
	                        </#if>
                            <#if specialTicketType =="DISNEY_SHOW"> 
	                           <tr>
	                                <td class="e_label">演出时间：</td>
	                                <td>${showTime}</td>
	                            </tr>
	                            <tr>
	                                <td class="e_label">区域详情：</td>
	                                <td>${sectionDetail}</td>
	                            </tr>
                                <tr>
                                    <td class="e_label">座位信息：</td>
                                    <td>${seatsDetail!'--'}</td>
                                </tr>
                            </#if> 
                            <tr>
                                <td class="e_label w10">是否对接：</td>
                                <td>
                                 <#if apiFlag=="Y">对接<#else>非对接</#if>
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">商品ID：</td>
                                <td>
                                 ${orderItem.suppGoodsId}
                                 </td>
                            </tr>
                            <tr>
                                <td class="e_label w10">产品ID：</td>
                                <td>
                                 ${orderItem.productId}
                                 </td>
                            </tr>
                            <#if order.categoryId == 15 && orderItem.mainItem == "true" && orderItem.contentMap['group_mode']?? >
                            <tr>
                                <td class="e_label w10">出团模式：</td>
                                <td>
                                <#if orderItem.contentMap['group_mode'] == 'NOLEADER_GROUP'>
                                    无领队小团
                                <#elseif orderItem.contentMap['group_mode'] == 'PARENTAGE_GROUP'>
                                    亲子游学
                                <#else>
                                    常规跟团游
                                </#if>
                                </td>
                            </tr>
                            </#if>

                            <#if orderItem.contentMap['big_traffic_flag']??> 
                            <tr>
                                <td class="e_label w10">是否含大交通：</td>
                                <td>
                                
                                  <#if   orderItem.contentMap['big_traffic_flag']=='Y'>
	                         		是
	                        	 <#elseif orderItem.contentMap['big_traffic_flag']=='N'>
		                        	否
		                     	 </#if>
		                     
                                 
                                 </td>
                            </tr>
                             </#if>
							<#if orderItem.contentMap['group_settle_flag']??>
                            <tr>
                                <td class="e_label w10">是否是团结算：</td>
                                <td>
									<#if  orderItem.contentMap['group_settle_flag']=='Y'>
                                        是
									<#else>
                                        否
									</#if>
                                </td>
                            </tr>
							</#if>
                           <#if orderItem.contentMap['is_from_foreign']??> 
                            <tr>
                                <td class="e_label w10">出发地是否出境：</td>
                                <td>
                                   <#if   orderItem.contentMap['is_from_foreign']=='Y'>
	                         		是
	                        	 <#elseif orderItem.contentMap['is_from_foreign']=='N'>
		                        	否
		                     	 </#if>
		                     
                                 </td>
                            </tr>
                            </#if> 
                            <#if orderItem.contentMap['is_to_foreign']??> 
                            <tr>
                                <td class="e_label w10">目的地是否出境：</td>
                                <td>
                                   <#if   orderItem.contentMap['is_to_foreign']=='Y'>
	                         		是
	                        	 <#elseif orderItem.contentMap['is_to_foreign']=='N'>
		                        	否
		                     	 </#if>
		                     
                                 </td>
                            </tr>
                            </#if>
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
                            
                            <tr>
                                <td class="e_label">客人姓名：</td>
                                <td><strong>[${travellerNum!'0'}人]</strong>${travellerName!''}</td>
                            </tr>
                            
                            <tr>
                            <#if specialTicketType =="SHOW_TICKET"> 
	                          <td class="e_label">演出日期：</td> 
	                        <#else>
	                           <td class="e_label">出发日期：</td> 
	                        </#if>
                            <td>
                            ${vistTime!''}
                            </td>
                            </tr>
                            
                            <#if specialTicketType =="SHOW_TICKET" > 
	                           <tr>
	                                <td class="e_label">演出时间：</td>
	                                <td>${showTime}</td>
	                            </tr>
	                            <tr>
	                                <td class="e_label">区域详情：</td>
	                                <td>${sectionDetail}</td>
	                            </tr>
                                <tr>
                                    <td class="e_label">座位信息：</td>
                                    <td>${seatsDetail!'--'}</td>
                                </tr>
                            </#if>
                            
                            <tr>
                                <td class="e_label">特殊要求：</td>
                                <td>${order.remark!''}</td>
                            </tr>
                             <tr>
                                <td class="e_label">确认方式：</td>
                                <td>短信确认</td>
                            </tr>
                             <tr>
                                <td class="e_label">相关订单：</td>
                                <td>
                                <#if otherOrder==true> 
                                <a href="${rc.contextPath}/ord/order/orderMonitorList.do?userId=${order.userId}&productId=${productId}&orderStatus=NORMAL" target="_blank">[另有订单]</a>
                                </#if> 
                                <a href="${rc.contextPath}/ord/order/orderMonitorList.do?userId=${order.userId}" target="_blank">[同用户订单]</a></td>
                            </tr>
                            <#if order.categoryId?? && (order.categoryId==15 || order.categoryId==16) && orderItem.categoryId?? && (orderItem.categoryId==15 || orderItem.categoryId==16)> 
                            <tr>
                                <td class="e_label">供应商收客人数：</td>
                                <td>
                                <a href="javaScript:void(0);" id="updateSuppTravellerNum">[修改]</a>
                                </td>
                            </tr>
                            </#if>
                            <#if performStatus!=""> 
	                           <tr>
	                                <td class="e_label">使用状态：</td>
	                                <td>${performStatus}  <a id="findPerformTimeDetail" href="javaScript:">[查看明细]</a></td>
	                                <input type="hidden" name="performStatus" id="performStatus" value="${performStatus!''}">
	                            </tr>
	                            
	                         <!--   <#if bizCategory.categoryCode == "category_single_ticket" || bizCategory.categoryCode == "category_other_ticket" || bizCategory.categoryCode == "category_comb_ticket"> 
		                            <tr>
		                                <td class="e_label">通关时间：</td>
		                                <td>${performTimeStr}</td>
		                            </tr>
	                            </#if>
	                            -->
	                            <tr>
	                                <td class="e_label">物流信息：</td>
	                                <td><a id="supplierPost" href="javascript:void(0)">[查看明细]</a></td>
	                            </tr>
                            </#if> 
                            <#if orderItem.ebkEmailFlag="Y">
                            <tr>
	                                <td class="e_label">EBK发送邮件：</td>
	                                <#if alreadySend=true>
	                                    <td>已发送  <a id="ebkEmailDetail" href="javaScript:">[查看明细]</a></td>
	                                    <input type="hidden" name="performStatus" id="performStatus" value="${performStatus!''}">
	                                <#else>
	                                    <td>未发送 
	                                </#if>
	                                
	                        </tr>
	                        </#if>
                            <tr>
                                <td class="e_label">佣金信息：</td>
                                <td>
                                    <a id="commissionInfo" href="javascript:void(0)">[查看明细]</a>
                                </td>
                            </tr>
                            <#if  isTicketOrder??> 
                            <tr>
                                <td class="e_label">退款明细：</td>
                                <td>
                                    <a id="queryRefundInfo" href="javascript:void(0)">[查看明细]</a>
                                 </td>
                            </tr>
                            </#if>
                            <#if  isExpired??>
                            <tr>
                                <td class="e_label">是否过期：</td>
                                <td>
                                    ${isExpired}
                                </td>
                            </tr>
                            </#if>
                            <#if itemCancelStatus??>
                            <tr>
                                <td class="e_label">出票状态：</td>
                                <td>
                                    ${itemCancelStatus}
                                </td>
                            </tr>
                            </#if>
                        </tbody>
                    </table>
                
                <#if bizCategory.categoryCode=="category_hotel">  
		                <table class="p_table table_center mt20">
                            <thead>
                            <tr>
                                <th>房型</th>
                                <th>日期</th>
                                <th>房价</th>
                                <th>结算价</th>
                                <th>早餐</th>
                                <th>担保时间</th>
                                <th>最晚预定</th>
                                <th>预订份数</th>
                                <#if singlePrice=="true" && isbuyoutFlag == "Y">
                                    <th width="120">买断结算单价</th>
                                    <th width="120">买断数量</th>
                                </#if>
                                <th>子订单实付金额</th>
                                <th>优惠分摊总金额</th>
                                <th>促销分摊总金额</th>
                                <th>分销渠道减少分摊总金额</th>
                                <th>订单金额减少分摊总金额</th>
                                <th>支付立减分摊总金额</th>
                                <th>退款金额</th>
                                <th>退款间数</th>
                            </tr>
                            </thead>
                            <tbody>

								<#list hotelTimeRateInfoList as hotelTimeRateInfo>
                                <tr>
                                    <td>
									${orderItem.contentMap['branchName']}( ${orderItem.suppGoodsName} )
                                    </td>
                                    <td>${hotelTimeRateInfo.visitTime?string('yyyy-MM-dd') !''}</td>
                                    <td>RMB ${((hotelTimeRateInfo.price)!0)/100!''}</td>
                                    <!-- 结算价 -->
                                    <td>RMB ${((hotelTimeRateInfo.settlementPrice)!0)/100}</td>
                                    <td>
                                        <#if hotelTimeRateInfo.breakfastTicket==0>
                                            无
                                        <#elseif hotelTimeRateInfo.breakfastTicket==1>
                                            单早
                                        <#elseif hotelTimeRateInfo.breakfastTicket==2>
                                            双早
                                        <#elseif hotelTimeRateInfo.breakfastTicket==3>
                                            三早
                                        <#else>
                                        ${hotelTimeRateInfo.breakfastTicket}早
                                        </#if>
                                    </td>
                                    <td>
                                        <#if hotelTimeRateInfo.guaranteeTime!=null && hotelTimeRateInfo.guaranteeTime!='null' >
                                        ${hotelTimeRateInfo.guaranteeTime!''}:00
                                        </#if>
                                    </td>
                                    <td>${hotelTimeRateInfo.lastTime!''}</td>
                                    <td>${priceQuantity!''}</td>
                                    <#if singlePrice=="true" && isbuyoutFlag == "Y">
                                        <td>${orderItem.buyoutPrice/100} 元</td>
                                        <td>${orderItem.buyoutQuantity}</td>
                                    </#if>
                                    <td>RMB ${((hotelTimeRateInfo.actualPaidAmount)!0)/100}</td>
                                    <td>RMB ${((hotelTimeRateInfo.couponApportionAmount)!0)/100}</td>
                                    <td>RMB ${((hotelTimeRateInfo.promotionApportionAmount)!0)/100}</td>
                                    <td>RMB ${((hotelTimeRateInfo.distributorApportionAmount)!0)/100}</td>
                                    <td>RMB ${((hotelTimeRateInfo.manualChangeApportionAmount)!0)/100}</td>
                                    <td>RMB ${((hotelTimeRateInfo.payAmountReductTotalAmount)!0)/100}</td>
                                    <td>RMB ${((hotelTimeRateInfo.refundAmount)!0)/100}</td>
                                    <td>${(hotelTimeRateInfo.refundQuantity)!0}间</td>
                                </tr>
								</#list>
                            </tbody>
		            </table>
               <#else>
	                <table class="p_table table_center mt5">
	                <thead>
	                    <tr>
	                        <th width="120">商品名称</th>
	                        
	                          <th width="120">出游日期</th>
	                       <#if productType=="WIFI">
	                       		<th width="120">出游截止日期</th>
	                       </#if>
	                        <th width="120">预订份数</th>
	                        
	                        <th width="120">驴妈妈单价</th>
	                         <th width="120">驴妈妈结算单价</th>
	                        <#if singlePrice=="true" && isbuyoutFlag == "Y">
	                        	<th width="120">买断结算单价</th>
	                        	<th width="120">买断数量</th>
	                        </#if>
							<th width="120">子订单实付总金额</th>
                            <th width="120">优惠分摊总金额</th>
                            <th width="120">促销分摊总金额</th>
                            <th width="120">分销渠道减少分摊总金额</th>
                            <th width="120">订单金额减少分摊总金额</th>
                             <th width="120">支付立减分摊总金额</th>
                            <th width="120">子订单退款总金额</th>
                            <th width="120">退款份数</th>
	                    </tr>
	                </thead>
	                <tbody>
	                    <tr>
	                        <td>
	                        	<#if bizCategory.categoryCode== "category_single_ticket"
	                        		|| bizCategory.categoryCode== "category_other_ticket"
	                        		|| bizCategory.categoryCode== "category_comb_ticket"
	                        		|| bizCategory.categoryCode== "category_food"
	                        		|| bizCategory.categoryCode== "category_sport">
			                        <a id="productInfo" href="javascript:">${orderItem.suppGoodsName}</a>
			                     <#else>
			                     	${orderItem.suppGoodsName}
			                    </#if>
	                        </td>
	                        <td> ${vistTime!''} </td>
	                        <#if productType=="WIFI">
	                       		<td>${endTime!''}</td>
	                       </#if>
	                         <td>
	                        ${priceQuantity!''}
	                        </td>
							<#if bizCategory.categoryCode== "category_cruise"> 
		                        <td>
		                         ${orderItem.quantity!''}
								</td>
							 </#if>
							 
	                      <td>
	                       ${lvmamaPrice!''}
	                  		</td>
	                  		  <td>
	                      <#if order.orderSubType!='STAMP'> ${settlementPrice!''}<#else>--</#if>
	                  		</td>
	                       	<#if singlePrice=="true" && isbuyoutFlag == "Y">
	                        	<td>${orderItem.buyoutPrice/100} 元</td>
	                        	<td>${orderItem.buyoutQuantity}</td>
	                        </#if>
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
                                <#if (orderItemApportionInfo.itemPromApportionPayReductionByPriceTypeMap)?? && orderItemApportionInfo.itemPromApportionPayReductionByPriceTypeMap?size gt 0>
                                    <#list orderItemApportionInfo.itemPromApportionPayReductionByPriceTypeMap?values as priceTypeVO>
                                    ${priceTypeVO.declaration!''}<#if (priceTypeVO.declaration)?? && priceTypeVO.declaration != "">:</#if>${priceTypeVO.price/100}<br/>
                                    </#list>
                                <#else>
                                    0元
                                </#if>
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
	            
	        </#if>    
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
      //佣金信息
      $("#commissionInfo").bind("click",function () {
        new xDialog("/vst_order/order/orderManage/commissionInfo.do",{"orderItemId":"${orderItem.orderItemId}"},{title:"佣金明细",width:650});
      });
      //退款明细
      $("#queryRefundInfo").bind("click",function(){
        new xDialog("/vst_order/order/orderManage/queryRefundInfo.do",{"orderItemId":"${orderItem.orderItemId}"},{title:"退款明细",width:650});
      });
      
      $("#ebkEmailDetail").click(function(){
		new xDialog("/vst_order/order/orderManage/showEbkEmailLog.do",{"objectId":"${orderItem.orderItemId}","objectType":"EBK_SEND_EMAIL_MEMO","sysName":"VST"},{title:"EBK邮件发送明细",iframe:false,width:1050,iframeHeight:680,height:500,scrolling:"yes"});
      });
 </script>
