<#import "/base/order_line_func.ftl" as line_func/> 
<#if additionList?? && additionList?size &gt; 0>
<div class="hotel_ebk_wrap">
            <div class="hotel_tab_box">
                    <div class="hotel_tab">
                        <div class="table_t">可选服务</div>
                        <p class="listName">附加规格</p>
                       <!-- 房差 -->
						<#if hiddenMain?? && hiddenMain?size &gt; 0>
							<#if hiddenMain['fangchaDiv']??>${hiddenMain['fangchaDiv']}
							<table width="100%" class="additionTable">
								<tbody>
									<tr>
										<td>
											<table class="tab_nav" width="100%">
												<tr class="table_nav">
													<td width="50%">
														<a class="pro_tit" href="javascript:void(0)" desc="" prodBranchId="">单房差</a> 
														<input type="hidden" id="fangChaPrice" name="fangChaPrice" value="${fanchaTotalPrice}"/></td>
														<input type="hidden" id="fangChaAllPrice" name="fangChaAllPrice" value="${fanchaTotalPrice}"/></td>
													<td width="20%" class="orange" id="unitGapPriceId">单价:￥${fanchaTotalPrice}元</td>
													<td width="20%">
														<select id="fangchaNum" name ="spreadQuantity" class="w5 numText hotel_sum" style="text-align:center">
															${fangchaQuantity}
														</select>
														份
													</td>
													<td width="10%" class="orange" id="fangchaTotalPrice">总价:￥0元</td>
												</tr>
											</table>
										</td>
									</tr>
								</tbody>
							</table>
							</#if>  
							<script>
	  							$(function(){ 
	  								var fangChaQuantity = $("#fangchaNum option:selected").val();
	  								var fangChaPrice = $("#fangChaPrice").val();
	  								if(fangChaPrice == null || fangChaPrice == "" || isNaN(fangChaPrice)){
	  									fangChaPrice = "0";
	  								}
	  								$("#fangchaTotalPrice").html("");
	  								var fangchaTotalPriceStr = "总价:￥" + (parseFloat(fangChaPrice) * parseInt(fangChaQuantity)).toFixed(2) + "元";
	  								$("#fangchaTotalPrice").html(fangchaTotalPriceStr);
	  								$("#fangChaAllPrice").val((parseFloat(fangChaPrice) * parseInt(fangChaQuantity)).toFixed(2));
	  								$('#fangchaNum').change(function(){
	  									var opt = $(this).children('option:selected').val();
	  									var fangChaPrice = $("#fangChaPrice").val();
		  								if(fangChaPrice == null && fangChaPrice == "" && isNaN(fangChaPrice)){
		  									fangChaPrice = "0";
		  								}	  									
		  								$("#fangchaTotalPrice").html("");
		  								var fangchaTotalPriceStr = "总价:￥" + (parseFloat(fangChaPrice) * parseInt(opt)).toFixed(2) + "元";
		  								$("#fangchaTotalPrice").html(fangchaTotalPriceStr);	
		  								$("#fangChaAllPrice").val(parseFloat(fangChaPrice) * parseInt(opt));
										var oldGap = $("#showGapPriceId").attr("value");//旧的房差价
										var oldOughtPay = $("#showOughtPayPriceId").attr("value");//旧的总价
										if(oldGap != null || oldGap != "" || !isNaN(oldGap)){
											if(oldOughtPay != null && oldOughtPay != "" && !isNaN(oldOughtPay)){
												//设置现在总价显示=旧的总价-旧的房差总价+新的房差总价
												//$("#showOughtPayPriceId").html(((parseFloat(oldOughtPay) - parseFloat(oldGap)) + (parseFloat(fangChaPrice) * parseInt(opt))));
												$("#showOughtPayPriceId").html(((parseFloat(oldOughtPay) - parseFloat(oldGap)) + (parseFloat(0) * parseInt(opt))));
												//设置现在的新房差显示总价
												//$("#showGapPriceId").html(" + 房差¥" + (parseFloat(fangChaPrice) * parseInt(opt)).toFixed(2));
												$("#showGapPriceId").html("");
												$("#showGapPriceId").attr("value",(parseFloat(fangChaPrice) * parseInt(opt)).toFixed(2));
												$("#showOughtPayPriceId").attr("value",((parseFloat(oldOughtPay) - parseFloat(oldGap)) + (parseFloat(fangChaPrice) * parseInt(opt))));
											}
										}		  								  									
	  								});
	  							});
  						  </script>							
						</#if>
                        <table width="100%" class="additionTable">
                            <tbody>
                            	<tr>
        						 <td class="additionTableTd">
                            	<#assign additionIdIndex=0 />
		        				<#list additionList as pb>
		        					<#assign goods=pb.suppGoodsList[0] />
		        						<table class="tab_nav ${goods.productId}" width="100%">
			        					<tr class="table_nav" parentProductId="${goods.productId}" adult="" child="" suppGoodsId="${goods.suppGoodsId}">
			        						<td width="50%"><a class="pro_tit" href="javascript:;" desc="${pb.branchName}" prodBranchId="${pb.productBranchId}">${pb.branchName}</a>
			        							<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
	        									<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="ADDITION" autocomplete="off"/>
	        									<input class="${goods.productId}visitTime" type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="${goods.suppGoodsBaseTimePrice.specDate?string('yyyy-MM-dd')}" autocomplete="off"/>
	        								</a></td>
			        						<td width="20%" class="orange"
			        						<#if pb.selectPriceMap??>
			        						<#assign selectPriceMap=pb.selectPriceMap />
			        						<#list selectPriceMap?keys as key>
			        						${key}="${selectPriceMap[key]}"
			        						</#list>
			        						</#if>
			        						>单价:￥${goods.suppGoodsBaseTimePrice.auditPriceYuan}元</td>
			        						
			        						<#if isLvmamaProduct??&&isLvmamaProduct >
			        							<td width="20%">
				        							<#if goods.relationType??&&goods.relationType='AMOUNT'>
				        								<input auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" goodsId="${goods.suppGoodsId}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 numText hotel_sum" readOnly name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center;width:45px" value="${goods.fitQuantity}" required=true number=true />
				        							<#else>
				        								<@line_func.showOptionSelect packageTourProductVo.productId pb goods productItemIdIndex/>
				        							</#if>	        							
			        							</td>
			        						<#else>
			        							<td width="20%">
				        							<#if goods.parentGoodsRelation??&&goods.parentGoodsRelation.relationType='AMOUNT'>
				        								<input auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" goodsId="${goods.suppGoodsId}"  childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 numText hotel_sum" readOnly name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center;width:45px" value="${goods.fitQuantity}" required=true number=true />
				        							<#else>
				        								<@line_func.showOptionSelect packageTourProductVo.productId pb goods productItemIdIndex/>
				        							</#if>	        							
			        							</td>
			        						</#if>			        						
			        						<td width="10%" class="orange" id="${goods.suppGoodsId}Td">总价:￥<#--${goods.dailyLowestPriceYuan}-->0元</td>
			        					</tr>
			        					<tr>
										 <td colspan="3">
										 <p class="descript">规格描述：<textarea class="textarea"></textarea></p>
										 </td>
										</tr>
			        					</table>
			        				<#assign additionIdIndex=additionIdIndex+1 /> 
			        				<#assign productItemIdIndex=productItemIdIndex+1 />
			        				               	<script>
														productItemIdIndex='${productItemIdIndex}';	       
			    									</script>
		        				</#list>
		        				</td>
        						</tr>
                            </tbody>
                        </table>
                    </div>
                  </div>
               </div>
		<#else>
			<#if hiddenMain?? && hiddenMain?size &gt; 0>
			<#if hiddenMain['fangchaDiv']??>${hiddenMain['fangchaDiv']}
			<div class="hotel_ebk_wrap">
            	<div class="hotel_tab_box">
                    <div class="hotel_tab">
                        <div class="table_t">可选服务</div>
                        <p class="listName">附加规格</p>
                       	<!-- 房差 -->
							<table width="100%" class="additionTable">
								<tbody>
									<tr>
										<td>
											<table class="tab_nav" width="100%">
												<tr class="table_nav">
													<td width="50%">
														<a class="pro_tit" href="javascript:void(0)" desc="" prodBranchId="">单房差</a> 
														<input type="hidden" id="fangChaPrice" name="fangChaPrice" value="${fanchaTotalPrice}"/></td>
														<input type="hidden" id="fangChaAllPrice" name="fangChaAllPrice" value="${fanchaTotalPrice}"/></td>
													<td width="20%" class="orange" id="unitGapPriceId">单价:￥${fanchaTotalPrice}元</td>
													<td width="20%">
														<select id="fangchaNum" name ="spreadQuantity" class="w5 numText hotel_sum" style="text-align:center">
															${fangchaQuantity}
														</select>
														份
													</td>
													<td width="10%" class="orange" id="fangchaTotalPrice">总价:￥0元</td>
												</tr>
											</table>
										</td>
									</tr>
								</tbody>
							</table>
							<script>
	  							$(function(){ 
	  								var fangChaQuantity = $("#fangchaNum option:selected").val();
	  								var fangChaPrice = $("#fangChaPrice").val();
	  								if(fangChaPrice == null || fangChaPrice == "" || isNaN(fangChaPrice)){
	  									fangChaPrice = "0";
	  								}	  								
	  								$("#fangchaTotalPrice").html("");
	  								var fangchaTotalPriceStr = "总价:￥" + (parseFloat(fangChaPrice) * parseInt(fangChaQuantity)).toFixed(2) + "元";
	  								$("#fangchaTotalPrice").html(fangchaTotalPriceStr);
	  								$("#fangChaAllPrice").val((parseFloat(fangChaPrice) * parseInt(fangChaQuantity)).toFixed(2));
	  								$('#fangchaNum').change(function(){
	  									var opt = $(this).children('option:selected').val();
	  									var fangChaPrice = $("#fangChaPrice").val();
		  								if(fangChaPrice == null || fangChaPrice == "" || isNaN(fangChaPrice)){
		  									fangChaPrice = "0";
		  								}	  	  									
		  								$("#fangchaTotalPrice").html("");
		  								var fangchaTotalPriceStr = "总价:￥" + (parseFloat(fangChaPrice) * parseInt(opt)).toFixed(2) + "元";
		  								$("#fangchaTotalPrice").html(fangchaTotalPriceStr);	
		  								$("#fangChaAllPrice").val(parseFloat(fangChaPrice) * parseInt(opt)); 
										var oldGap = $("#showGapPriceId").attr("value");//旧的房差价
										var oldOughtPay = $("#showOughtPayPriceId").attr("value");//旧的总价
										if(oldGap != null && oldGap != "" && !isNaN(oldGap)){
											if(oldOughtPay != null && oldOughtPay != "" && !isNaN(oldOughtPay)){
												//设置现在总价显示=旧的总价-旧的房差总价+新的房差总价
												//$("#showOughtPayPriceId").html(((parseFloat(oldOughtPay) - parseFloat(oldGap)) + (parseFloat(fangChaPrice) * parseInt(opt))));
												$("#showOughtPayPriceId").html(((parseFloat(oldOughtPay) - parseFloat(oldGap)) + (parseFloat(0) * parseInt(opt))));
												//设置现在的新房差显示总价
												//$("#showGapPriceId").html(" + 房差¥" + (parseFloat(fangChaPrice) * parseInt(opt)).toFixed(2));
												$("#showGapPriceId").html("");
												$("#showGapPriceId").attr("value",(parseFloat(fangChaPrice) * parseInt(opt)).toFixed(2));
												$("#showOughtPayPriceId").attr("value",((parseFloat(oldOughtPay) - parseFloat(oldGap)) + (parseFloat(fangChaPrice) * parseInt(opt))));
											}
										}		  								 									
	  								});
	  							});
  						  </script>							
					</div>
				</div>
			</div>
			</#if>	 
			</#if>                
        </#if>