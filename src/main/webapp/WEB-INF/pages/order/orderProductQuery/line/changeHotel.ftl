 <#if changeProdPackageList?? && changeProdPackageList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">可换酒店</div>
  					<#list changeProdPackageList as changeProdPackage>
  						<#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0>
  							<#assign detail=changeProdPackage.prodPackageDetails[0] />
	  						<#if changeProdPackage.prodPackageDetails?size gt 1>
	  							 <p class="listName"><a href="#" class="fr moreCategoryChangeHotel" adultNum="${adultNum}" childNum="${childNum}" outProductId="${productId}" packageGroupId="${detail.groupId}" packageProductId="${(detail.prodProductBranch.productId)!}" packageProductBranchId="${detail.prodProductBranch.productBranchId}" changeDate="<#if isLvmamaProduct==false>${specDate?string("yyyy-MM-dd")}<#elseif changeProdPackage.prodPackageGroupLine.dateList??>${changeProdPackage.prodPackageGroupLine.dateList[0]}</#if>">更多可换酒店</a>${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p>
	  						<#else>
	  							 <p class="listName">${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p> 
	  						</#if>              			 	
		  					<#if detail.prodProductBranch?? && detail.prodProductBranch.recommendSuppGoodsList??&& detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0 >
		  					<table width="100%" class="updateChangeTable">
			  				   <tbody>
			  					<tr>
        						 <td>
        						 	<#assign prodProductBranch=detail.prodProductBranch />
        						 		<#if detail.prodProductBranch.recommendSuppGoodsList?? &&detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0>
        						 	    <#assign goods=detail.prodProductBranch.recommendSuppGoodsList[0] />
        						 		<table class="tab_nav" width="100%">
						        			<tr id="firstChangeHotelShow${detail.groupId}" groupId="${detail.groupId}" totalAmount="${prodProductBranch.dailyLowestPrice}" productBranchId="${prodProductBranch.productBranchId}" adult="${adultNum}" child="${childNum}" class="table_nav" >
						        				<td width="50%"><a class="pro_tit" href="javascript:;" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}">${prodProductBranch.branchName}</a>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
						        					<input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        					<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="<#if isLvmamaProduct==false>${specDate?string("yyyy-MM-dd")}<#elseif changeProdPackage.prodPackageGroupLine.dateList??>${changeProdPackage.prodPackageGroupLine.dateList[0]}</#if>" autocomplete="off"/>
						        				</td>
						        				<td width="20%" class="orange">成人价:￥${goods.suppGoodsBaseTimePrice.auditPriceYuan}元/儿童价:￥${goods.suppGoodsBaseTimePrice.childPriceYuan}元
						        				</td>
						        				<td width="20%">
						        					成人数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 adultNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitAdultQuantity}" required=true number=true /><br/>
						        					儿童数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 childNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].childQuantity" style="text-align:center" value="${goods.fitChildQuantity}" required=true number=true />
						        					<input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
						        				</td>
						        				<td width="10%" class="orange" >总价:￥${prodProductBranch.dailyLowestPriceYuan}元</td>
						        			</tr>
						        			<tr>
												<td colspan="4">
												<p class="descript">规格描述：<textarea class="textarea"></textarea></p>
												</td>
											</tr>
				        				</table>  
				        			<#assign productItemIdIndex=productItemIdIndex+1 />  
				        				<script>
											productItemIdIndex='${productItemIdIndex}';	       
			    						</script>
						        	</#if>    			
				        		 </td>
        					   </tr>	  					
	  						  </tbody>
        					</table>
        					</#if>
  					</#if>   						
  				</#list>
  		</div>
  	</div>
  </div>
	 <script type="text/javascript"> 
		//改变升级和跟换酒店，则改变总的房差单价	
		function getTotalUnitPrice(selectedObj){
			var groupId = selectedObj.attr("groupId");//房差组
			var detailId = selectedObj.attr("detailId");//房差明细
			var selectedDivId = groupId + "-" + detailId + "gap";//被选择的div
			//组类型
			var currType;
			//房差状态
			var currStatus;
			//组编号
			var currGroupId 
			//房差值
			var currGapValue;
			//dom对象id
			var currDivId;
			//单房差总单价
			var totalGapPrice = parseFloat("0");
			
			//同一组中其它房差选择状态全部设置为N
			$(".lvmama-fangcha-price").each(function(){
				currType = $(this).attr("data-type");//取到当前组类型
				currGroupId = $(this).attr("groupId");//取到当前组编号
				currDivId = $(this).attr("id");//取到当前房差值
				if(currType == "update" || currType == "change"){//升级
					if(currGroupId == groupId){//同一组
						if(selectedDivId == currDivId){//同一个元素
							$(this).attr("data-status","Y"); 
						}else{
							$(this).attr("data-status","N"); 
						}
					}
				}
				currStatus = $(this).attr("data-status");//取到当前房差状态
				currGapValue = $(this).attr("data-fangcha");//取到当前房差值
				if(currStatus == "Y"){
					totalGapPrice = totalGapPrice + parseFloat(currGapValue);
				}
			});
			var fangChaQuantity = $("#fangchaNum option:selected").val();//当前选择的份数
			$("#unitGapPriceId").html("");
			$("#unitGapPriceId").html("单价:￥" + totalGapPrice.toFixed(2) + "元");//设置当前的总单价
			$("#fangChaPrice").val(totalGapPrice);//设置隐藏域中当前的总单价
			$("#fangchaTotalPrice").html("");//清空总的房差价
			var fangchaTotalPriceStr = "总价:￥" + (totalGapPrice * parseInt(fangChaQuantity)).toFixed(2) + "元";
			$("#fangchaTotalPrice").html(fangchaTotalPriceStr);
			$("#fangChaAllPrice").val((totalGapPrice * parseInt(fangChaQuantity)).toFixed(2)); //设置总的房差价格以提供后面的订单总价计算
			var oldGap = $("#showGapPriceId").attr("value");//旧的房差价
			var oldOughtPay = $("#showOughtPayPriceId").attr("value");//旧的总价
			if(oldGap != null && oldGap != "" && !isNaN(oldGap)){
				if(oldOughtPay != null && oldOughtPay != "" && !isNaN(oldOughtPay)){
					//设置现在总价显示=旧的总价-旧的房差总价+新的房差总价
					//$("#showOughtPayPriceId").html(((parseFloat(oldOughtPay) - parseFloat(oldGap)) + (totalGapPrice * parseInt(fangChaQuantity))));
					$("#showOughtPayPriceId").html(((parseFloat(oldOughtPay) - parseFloat(oldGap)) + (totalGapPrice * parseInt(0))));
					//设置现在的新房差显示总价
					//$("#showGapPriceId").html(" + 房差¥" + (totalGapPrice * parseInt(fangChaQuantity)).toFixed(2));
					$("#showGapPriceId").html("");
					$("#showGapPriceId").attr("value",(totalGapPrice * parseInt(fangChaQuantity)).toFixed(2));
					$("#showOughtPayPriceId").attr("value",((parseFloat(oldOughtPay) - parseFloat(oldGap)) + (totalGapPrice * parseInt(fangChaQuantity))));
				}
			}
		}
	</script> 
 </#if> 