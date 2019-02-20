<#import "/base/order_line_func.ftl" as line_func/>
<!-- 182只针对 自由行 机酒 国内 -->
<#if prodProduct.getSubCategoryId()?? && prodProduct.getSubCategoryId() == 182 && prodProduct.getProductType() == "INNERLINE">
<#if hotelInfoList?exists && hotelInfoList?size gt 0>
<div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">酒店</div>
  			<!--这里显示主要自主打包酒店信息-->
  			<#list hotelInfoList as hotel>
  				<#if hotel_index == 0>
  					<p class="listName">${hotel['check_in']}${hotel['week_in']}入住${hotel['check_out']}${hotel['week_out']}退房（共${hotel['days']}晚）</p> 
  				</#if>
				<#assign count = 0/>
                <#assign defaultSuppGoodsId = ''/>
				<#list hotel.fangXinList as fangXin>
				<#list fangXin?keys as hotelType >
				<#assign suppGoodsList =  fangXin[hotelType]/>
				<#list suppGoodsList as suppGoodsObj>
				<#if hotelType_index == 0 && suppGoodsObj_index == 0>
				<#assign defaultSuppGoodsId = suppGoodsObj.suppGoodsId/>
				</#if>
                </#list>
                </#list>
				</#list>
				<table width="100%" class="updateChangeTable">
  				   <tbody>
  				   <tr id="firstLineHotelShow${hotel['detailId']}_productNameLink">
						<td width="85%">
						  <a class="pro_tit" href="http://hotels.lvmama.com/hotel/${hotel['productId']}" packageGroupId="${hotel['groupId']}" pid="${hotel['productId']}" target="_blank">${hotel['hotel_name']}</a>
						</td>
						<td>
                            <#if packageProductId == hotel['productId']>
                            <div class="operate" style="align:center;">
                            	<span class="btn w8" style="visibility:inherit;cursor:pointer;"
									>已选</span>
                            </div>
        	              <#else>
        	              	<div class="operate" style="align:center;">
        	              		<span class="btn w8" detailId-data="${hotel['detailId']}" pid-data="${hotel['productId']}" name="hotelSelected" style="background:#4D90FE;border1px solid #3F87FE;color:#fff;"
									>选择</span>
        	              	</div>
        	               </#if>
                        </td>
					  </tr>
  					<tr style="display: none;">
  					  <td>
  						<table class="tab_nav machineWineTable" width="100%">
						  <#assign suppGoodObjCount = 0/>
                                <#list hotel.fangXinList as fangXin>
                                <#assign hotelTypeCount = 0/>
                                <#list fangXin?keys as hotelType > 
                                <#assign hotelTypeCount = hotelTypeCount + 1/>
                                <tr class="booking_list_type" <#if hotelTypeCount != 1>style="display:none;"</#if>>
                                    <td colspan="8"><a class="blt-name js_hotel_title" href="javascript:">${hotelType}</a></td>
                                </tr>
                                <#assign suppGoodsList =  fangXin[hotelType]/>
                                <#assign suppGoodCount = 0/>
                                <#list suppGoodsList as suppGoodsObj>
                                <#assign suppGoodCount = suppGoodCount + 1/>
                                <#assign suppGoodObjCount = suppGoodObjCount + 1/>
                                <tr class="firstLineHotelShow${hotel['detailId']}"  totalAmount="${suppGoodsObj['data-adultPrice']}" <#if suppGoodObjCount != 1>style="display:none;"</#if>>
                                    <td width="20%">
                                        <em class="table_list_name ${(suppGoodsObj['suppGoodsPropDescriptionVo']?exists)?string('js-hover-fold table_fold_hover','')}">${suppGoodsObj['goodsName']}</em>
                                        <div class="fdiv" productId-data="${productId}" productIndex-data="${productItemIdIndex}">
                                            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].goodsId" value="${suppGoodsObj['suppGoodsId']}" autocomplete="off"/>
				        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].detailId" value="${hotel['detailId']}" autocomplete="off"/>
				        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
				        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].quantity" id="${suppGoodObjCount}${defaultSuppGoodsId}TotalNum" class="w5 numText" style="text-align:center" value="${quantity}" required=true number=true />
				        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].visitTime" value="${hotel['check_in']}" autocomplete="off"/>
				        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].hotelAdditation.arrivalTime" id="${suppGoodObjCount}${defaultSuppGoodsId}TotalNum" class="w5 numText" style="text-align:center" value="14:00" required=true number=true />
				        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].hotelAdditation.leaveTime" id="${suppGoodObjCount}${defaultSuppGoodsId}Total     Num" class="w5 numText" style="text-align:center" value="${hotel['check_out']}" required=true number=true />
                                         </div>
                                    </td>
                                    <td width="10%">${suppGoodsObj['bed_type']}</td>
                                    <td width="10%">${suppGoodsObj['isbreakfast']}</td>
                                    <td width="10%">${suppGoodsObj['internet']}</td>
                                    <td width="10%"><span style="cursor: pointer; border-bottom:#666 dashed 1px;" class="hotelPolicy js_tip" tip-content="${suppGoodsObj['content']}">${suppGoodsObj['type']}</span></td>
                                    <td width="15%">
                                    	<input type="hidden" id="${defaultSuppGoodsId}adultQuantity" goodsId="${defaultSuppGoodsId}"
                                    	childPrice="0" type="text" class="w5 hotelAdultNumText" name="productMap[${hotel['productId']}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="" required=true number=true />
	                															
                                        <select class="<#if hotelType_index == 0 && suppGoodsObj_index == 0>lvmama-price-flag </#if> room_num js_hotel_quantity_selector hotelSelect" data-type="hotel" 
                                        	   goodsprice="${suppGoodsObj['data-adultPrice']}" goodsid="${defaultSuppGoodsId}" id="hotelSelect"
                                               data-detailid-suppgoodsid="${hotel['detailId']}-${defaultSuppGoodsId}" ${suppGoodsObj['data-price']} data-adultPrice="${suppGoodsObj['data-adultPrice']}">
                                             ${suppGoodsObj['selectOption']}  
                                       </select> 间
                                    </td>
                                    <td width="10%" style="display:none;"><span class="orange hoteltotalPrice"><span class="orange hoteltotalPrice">--</span></span></td>
                                    <td width="10%"><span class="room_price orange"><span class="room_price">--</span></span></td>
                                    <td width="8%">
                                        <#if hotelType_index == 0 && suppGoodsObj_index == 0>
                                        <div class="operate" style="align:center;">
                                        	<span class="btn w8 hotelBtnOk" id="selectedHotel" style="visibility:inherit;cursor:pointer;"
												>已选</span>
                                        </div>
                    	              <#else>
                    	              	<div class="operate" style="align:center;">
                    	              		<span class="btn w8 hotelBtnOk" style="background:#4D90FE;border1px solid #3F87FE;color:#fff;"
												>选择</span>
                    	              	</div>
                    	               </#if>
                                    </td>
                                </tr>
                                
                                </#list>
                                </#list> 
                                </#list>
  					   </td>
					 </tr>	  
					 </table>
					 <#assign productItemIdIndex=productItemIdIndex+1 /> 
	        			<script>
							productItemIdIndex='${productItemIdIndex}';	       
						</script>		
					 </tr>	  			
				  </tbody>
				</table>
  			</#list>
  		</div>
  	</div>
</div>
</#if>
<#else>
<#if moreProductList?? && moreProductList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">酒店</div>   					
  					<#assign changeProdPackage=moreProductList[0] />
  						<p class="listName">${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p>
  						<#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0>
  							<#list changeProdPackage.prodPackageDetails as detail>
		  					<#if detail.prodProductBranch?? && detail.prodProductBranch.recommendSuppGoodsList??&& detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0 >
		  					<table width="100%" class="updateChangeTable">
			  				   <tbody>
			  					<tr>
        						 <td>	
        						 	<#assign prodProductBranch=detail.prodProductBranch />
        						 	 <#if detail.prodProductBranch.recommendSuppGoodsList?? &&detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0>
        						 	  <#assign goods=detail.prodProductBranch.recommendSuppGoodsList[0] />
        						 		<table class="tab_nav" width="100%">
						        			<tr id="${prodProductBranch.productBranchId}_${detail.groupId}" groupId="${detail.groupId}" productBranchId="${prodProductBranch.productBranchId}" adult="${adultQuantity}" child="${childQuantity}"  class="table_nav" >
						        				<td width="50%"><a class="pro_tit" href="javascript:;" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}">${detail.prodProduct.productName}(${prodProductBranch.branchName})</a>
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
						        					<input type="hidden" id="${goods.suppGoodsId}TotalNum" class="w5 numText" name="productMap[${productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].visitTime" value="<#if changeProdPackage.prodPackageGroupHotel.dateList??>${changeProdPackage.prodPackageGroupHotel.dateList[0]}</#if>" autocomplete="off"/>
						        					<input type="hidden" class="w5 numText" name="productMap[${productId}].itemList[${productItemIdIndex}].hotelAdditation.arrivalTime" style="text-align:center" value="14:00" required=true number=true />
						        					<input type="hidden" class="w5 numText" name="productMap[${productId}].itemList[${productItemIdIndex}].hotelAdditation.leaveTime" style="text-align:center" value="${changeProdPackage.prodPackageGroupHotel.leaveDate}" required=true number=true />
						        				</td>
						        				<td width="20%">
						        					份数		
						        						<input type="hidden" id="${goods.suppGoodsId}adultQuantity" goodsid="${goods.suppGoodsId}" auditprice="" childprice="0" class="w5 hotelAdultNumText" 
						        							name="productMap[${productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="1" required="true" number="true">
						        						<#if prodProductBranch.productBranchId==currentProductBranchId>
						        							<select id="hotelSelect" class="hotelSelect"  readOnly goodsId=${goods.suppGoodsId} goodsPrice=${goods.adultPrice} fitQuantity=${goods.fitQuantity}>
																		           <#list goods.fitQuantity..adultNum as counter>
																		              <option value="${counter}">${counter}</option>
																		           </#list>
	                															</select>
						        						<#else>
						        						<select id="hotelSelect" class="hotelSelect" goodsId=${goods.suppGoodsId} goodsPrice=${goods.adultPrice} fitQuantity=${goods.fitQuantity}>
																		           <#list goods.fitQuantity..adultNum as counter>
																		              <option value="${counter}">${counter}</option>
																		           </#list>
	                															</select>
						        						</#if>						
						        					
						        				</td>
						        				<td width="10%" class="orange hoteltotalPrice" totalAmount="${prodProductBranch.dailyLowestPrice}" totalAmountYuan="${prodProductBranch.dailyLowestPriceYuan}">
						        					<#if prodProductBranch.productBranchId==currentProductBranchId>
						        						--
						        					<#else>
						        					差价:￥<@line_func.showSpreadPrice sourceTotalAmount prodProductBranch/>元
						        				</#if>
						        				</td>
						        				
						        				<td><div class="operate" style="text-align:center;">
						        				<#if prodProductBranch.productBranchId==currentProductBranchId>
						        					<span class="btn w8 " style="margin:0;padding:5px 0;">已选择</span>
						        				<#else>
						        				<a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" groupId="${detail.groupId}" packageProductId="${detail.prodProduct.productId}"  productBranchId="${prodProductBranch.productBranchId}" adult="${adultQuantity}" child="${childQuantity}" name="xztcBtn" >选择</a>
						        				</#if>
						        				</div>
						        					<div class="productNameLink" style="display:none;">
								        			<a class="pro_tit" href="http://hotels.lvmama.com/hotel/${detail.prodProduct.productId}" target="_blank">${detail.prodProduct.productName}</a>
						        					</div>
						        				</td>
						        			</tr>
						        			<tr>
												<td colspan="4">
												<p class="descript">规格描述：<textarea class="textarea"></textarea></p>
												</td>
											</tr>
				        			</table> 
				        			<#assign productItemIdIndex=productItemIdIndex+1 />  
						        	</#if>			        			
				        		 </td>
        					   </tr>	  					
	  						  </tbody>
        					</table>
        				</#if>
        				</#list>        				
  					</#if> 
  		</div>
  	</div>
  </div>
 <script>
 	 productItemIdIndex='${productItemIdIndex}';
 </script>  
 </#if> 
</#if>
 