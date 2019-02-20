  <#if updateProdPackageList?? && updateProdPackageList?size &gt; 0>
 <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">升级服务</div>
  			<input type="hidden" name="oneKeyOrderUpdate" id="oneKeyOrderUpdate" value="${oneKeyOrderUpdate}" />
  			<#list updateProdPackageList as packageGroupBo>
  			<#if packageGroupBo?? && packageGroupBo.prodPackageDetails?? && packageGroupBo.prodPackageDetails?size &gt; 0 >
  			<p class="listName">规格列表 <#if packageGroupBo.prodPackageGroupLine?? 
  			&& packageGroupBo.prodPackageGroupLine.startDay??>第 ${packageGroupBo.prodPackageGroupLine.startDay}天</#if> </p>
			<table width="100%" class="additionTable">
               <tbody>
                <tr>
        		 <td>
					<#assign updateIdIndex=0 />
        				<#list packageGroupBo.prodPackageDetails as detail>
        					<#if detail.prodProductBranch?? && detail.prodProductBranch.recommendSuppGoodsList??&& detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0 >
	        					<#assign goods=detail.prodProductBranch.recommendSuppGoodsList[0] />
		        				<table class="tab_nav"  width="100%">
		        					<tr class="table_nav" adult="" child="" suppGoodsId="${goods.suppGoodsId}">
		        					<td width="50%">
		        						<a class="pro_tit" href="javascript:;" desc="${detail.prodProductBranch.branchName}" prodBranchId="${detail.prodProductBranch.productBranchId}">${detail.prodProductBranch.branchName}  </a>
	        							<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
	        							<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
	        							<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
	        							<input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="<#if packageGroupBo.prodPackageGroupLine?? && packageGroupBo.prodPackageGroupLine.dateList??>${packageGroupBo.prodPackageGroupLine.dateList[0]}</#if>" autocomplete="off"/>
	        						</td>
	        						<td width="25%" class="orange">成人价:￥${goods.suppGoodsBaseTimePrice.auditPriceYuan}元/儿童价:￥${goods.suppGoodsBaseTimePrice.childPriceYuan}元
	        						</td>
	        						<td width="15%" display="displayed">
	        							<#--成人数--><input groupId="${detail['groupId']}" goodsId="${goods.suppGoodsId}"  auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="hidden" class="w5 adultNumText adultNumText33" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="0" required=true number=true /><br/>
	        							<#--儿童数--><input groupId="${detail['groupId']}" goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="hidden" class="w5 childNumText childNumText33" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].childQuantity" style="text-align:center" value="0" required=true number=true />
	        							<input groupId="${detail['groupId']}" id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText totalNumText11" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="0" required=true number=true />
	        						</td>
	        						<td width="15%" class="orange totalNumText22" groupId="${packageGroupBo.groupId}">
	        							总价:￥--元
	        						</td>
	        						<td width="10%" class="orange">
				        				<input type="button" name="updateSelect${updateIdIndex}" value="选择" class="lvmama-fangcha-btn" style="padding: 0 6px;height: 20px;line-height: 20px;font-weight: normal;margin-right: 6px;font-size: 12px;box-sizing: content-box;color: #666;" id="${detail['groupId']}-${detail.detailId}butn"
				        						groupId="${detail['groupId']}" detailId="${detail.detailId}" 
				        						onclick="javascript:getTotalUnitPrice($(this));" style="display: block;">
	        						</td>
		        					</tr>
		        					<tr>
										<td colspan="4">
										 <p class="descript">规格描述：<textarea class="textarea"></textarea></p>
										 </td>
									</tr>
	        					</table>
		        				<#assign updateIdIndex=updateIdIndex+1 />  
		        				<#assign productItemIdIndex=productItemIdIndex+1 /> 
				        			<script>
										productItemIdIndex='${productItemIdIndex}';	       
			    					</script>  		        				     
	        				</#if>   				
        				</#list>  
        		 </td>
        		</tr>
               </tbody>
            </table>
            </#if>
            </#list>				
  		</div>	
  	</div>			
 </div>
	 <script type="text/javascript">
	 $(document).ready(function(){
		var exp = $("#oneKeyOrderUpdate").val();
		if (exp == "true")
		{
		    $("input[name='updateSelect0']").click();
		}
	 });
	 
	 function cancleFun(selectedObj) {
	 		unSelectedUpdatePro(selectedObj);
		 	selectedObj.attr("value", "选择");
		 	selectedObj.attr("onclick", "javascript:getTotalUnitPrice($(this));");
			var groupId = selectedObj.attr("groupId");//房差组
			var detailId = selectedObj.attr("detailId");//房差明细
			
			/**单房差计算 */
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
			$("#fangChaAllPrice").val((totalGapPrice * parseInt(fangChaQuantity)).toFixed(2)); //设置总的房差价格以提供后面的订单总价计算 （新的房差总价）
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
	 
		//改变升级和跟换酒店，则改变总的房差单价	
		function getTotalUnitPrice(selectedObj){
			var groupId = selectedObj.attr("groupId");
			//选择前清空所有儿童数、成人数为0，只保留当前选择
			$(".childNumText33[groupId='"+groupId+"']").each(function(){
				$(this).val(0);
			});
			$(".adultNumText33[groupId='"+groupId+"']").each(function(){
				$(this).val(0);
			});
			$(".totalNumText11[groupId='"+groupId+"']").each(function(){
				$(this).val(0);
			});
			$(".totalNumText22[groupId='"+groupId+"']").each(function(){
				$(this).html("总价:￥"+0.00+"元");
			});
			selectedUpdatePro(selectedObj);
			var groupId = selectedObj.attr("groupId");//房差组
			var detailId = selectedObj.attr("detailId");//房差明细
			/**选择按钮控制*/	
			$(".lvmama-fangcha-btn").each(function(){
				var tempGroupId = $(this).attr("groupId");//取到当前组编号
				var tempDetailId = $(this).attr("detailId");//取到当前组编号
				if(tempGroupId == groupId){
					if(tempDetailId == detailId){
						$(this).attr("value", "取消");
						var fun = $(this).attr("onclick");
						if(fun !=null && fun.indexOf("getTotalUnitPrice") != -1){
							$(this).attr("onclick", "javascript:cancleFun($(this));");
						}
					}else{
						$(this).attr("onclick", "javascript:getTotalUnitPrice($(this));");
						$(this).attr("value", "选择");
					}
				}
			});
			
			/**单房差计算 */
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
			$("#fangchaTotalPrice").html("");//情况总的房差价
			var fangchaTotalPriceStr = "总价:￥" + (totalGapPrice * parseInt(fangChaQuantity)).toFixed(2) + "元";
			$("#fangchaTotalPrice").html(fangchaTotalPriceStr);
			$("#fangChaAllPrice").val((totalGapPrice * parseInt(fangChaQuantity)).toFixed(2)); //设置总的房差价格以提供后面的订单总价计算 （新的房差总价）
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