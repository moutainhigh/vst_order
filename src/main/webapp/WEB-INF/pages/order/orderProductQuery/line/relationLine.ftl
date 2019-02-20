<#if relSaleLocalDetailList??  && relSaleLocalDetailList?size &gt; 0>
<!--/当地游模板-->
<div class="right-container clearfix">
    <div class="type gray">
         <p class="name gray">当地游</p>
    </div>
        <#list relSaleLocalDetailList as saleLocalDetail>
        <#assign currSuppGoods = saleLocalDetail['suppGoods']>
        <#assign suppGoodsId = currSuppGoods.suppGoodsId>
        
        <#assign auditPriceMap = saleLocalDetail['auditPriceMap']>
        <#assign childPriceMap = saleLocalDetail['childPriceMap']>
        <#assign gapPriceMap   = saleLocalDetail['gapPriceMap']>
        
        <#assign selectAdultQuantityRange = saleLocalDetail['selectAdultQuantityRange']>
        <#assign selectChildQuantityRange = saleLocalDetail['selectChildQuantityRange']>
        <#assign selectGapQuantityRange   = saleLocalDetail['selectGapQuantityRange']>
        
        <#assign selectDateList = saleLocalDetail['selectDateList']>
        
    	<#if selectDateList??>
	    	<#list selectDateList as selectDate>
				<#assign auditPrice = auditPriceMap[selectDate]>
				<#assign childPrice = childPriceMap[selectDate]>
				<#assign gapPrice   = gapPriceMap[selectDate]>
				<input type="hidden"  data="${suppGoodsId}_${selectDate}" suppGoodsId="${suppGoodsId}" auditPrice="${auditPrice}" childPrice="${childPrice}" gapPrice="${gapPrice}"/>
	       	</#list>
        </#if>
        
		<input type="hidden" name="itemMap[${currSuppGoods.suppGoodsId}].goodsId" value="${currSuppGoods.suppGoodsId}" autocomplete="off"/>                  
        <input type="hidden" name="itemMap[${currSuppGoods.suppGoodsId}].routeRelation" value="ADDITION" autocomplete="off"/>
        <input type="hidden" name="itemMap[${currSuppGoods.suppGoodsId}].goodType" value="localRoute" autocomplete="off"/>
     
        
        <div class="detail detail-item">
        
        	<div class="insurance-detail-item insurance-detail-item_insurance insurance-detail-item-localTour">
                <div class="detail-top clearfix">
                    <div class="spot">
                        <p class="name gray">
                          		<a href="http://dujia.lvmama.com/local/${saleLocalDetail['productId']}" target="_blank" rel="nofollow">${saleLocalDetail['productName']}</a>
                        </p>
                    </div>
                    <div class="spot">
                        <p class="name gray">
                              <a rel="nofollow">${currSuppGoods.goodsName}</a>
                        </p>
                    </div>
                    <div class="price_day">
                    	出发日期：
                    		<select class="selectDateClass" name="itemMap[${currSuppGoods.suppGoodsId}].visitTime" suppGoodsId="${suppGoodsId}"> 
                        	<#if selectDateList??>
                        	<#list selectDateList as selectDate>
                            	<option value="${selectDate}" selectDate="${suppGoodsId}_${selectDate}" 
                            		<#if saleLocalDetail['selectDefaultDate']?? && saleLocalDetail['selectDefaultDate']==selectDate >selected="selected"</#if> 
                            	>${selectDate}</option>
                           	</#list>
                            </#if>
                        </select>
                    </div>
                </div>
                
                <div class="default">
					
					<!-- 成人价div start-->
					 <div class="auditPrice_div_${suppGoodsId}">	                
	                     <div class="optional-insurance-item adjust-product-item">
	                        <div class="optional-insurance-item-name">
	                           	成人价
	                        </div>
	                        <div class="optional-insurance-single-price price">
	                            	￥<span class="auditPrice_${suppGoodsId}">0</span> /人
	                        </div>
	                        
	                        <div class="optional-insurance-quantity">
	                           <select class="addtion-change-quantity lvmama-price-addition lvmama-price-flag selectAdultQuantityClass" 
                           		   	   name="itemMap[${suppGoodsId}].adultQuantity" goodsType="${currSuppGoods.goodsType}"
                           		   	   goodsId="${suppGoodsId}" > 
	   							<#list selectAdultQuantityRange?split(",") as num>
									<option value="${num}"
										<#if saleLocalDetail['defaultAuditQuantity']?? && saleLocalDetail['defaultAuditQuantity']==num >selected="selected"</#if> 
									>${num}</option>
								</#list>
	                           </select>份
	                        </div>
	                        <div class="optional-insurance-total-price price">
	                            	￥<span class="price auditPriceTotal_${suppGoodsId}">0</span>
	                        </div>
	                        <div class="optional-item-status status product-upgrade-operator">
	                                <i class="product-item-checked-icon"></i>
	                        </div>
	                        <div class="clearfix"></div>
	                     </div>
	                   </div>
					<!-- 成人价div end-->                     
                     
                    <!-- 儿童价div start -->
                 	<div class="childPrice_div_${suppGoodsId}">
                     <div class="optional-insurance-item adjust-product-item">
                        <div class="optional-insurance-item-name">
                                                                                    儿童价
                        </div>
                        <div class="optional-insurance-single-price price">
                           	 ￥<span class="childPrice_${suppGoodsId}">0</span> /人
                        </div>
                        
                        <div class="optional-insurance-quantity">
                        	<select class="addtion-change-quantity lvmama-price-addition lvmama-price-flag selectChildQuantityClass"
                 		   	      	name="itemMap[${suppGoodsId}].childQuantity" goodsType="${currSuppGoods.goodsType}"
                       		   	    goodsId="${suppGoodsId}"> 
   	   							<#list selectChildQuantityRange?split(",") as num>
									<option value="${num}"
										<#if saleLocalDetail['defaultChildQuantity']?? && saleLocalDetail['defaultChildQuantity']==num >selected="selected"</#if> 
									>${num}</option>
								</#list>
							</select>份
                        </div>
                        <div class="optional-insurance-total-price price">
                        	￥<span class="childPriceTotal_${suppGoodsId}">0</span>
                        </div>
                        <div class="optional-item-status status product-upgrade-operator">
                                 
                        </div>
                        
                        <div class="clearfix"></div>
                     </div>
                  </div>
             	  <!-- 儿童价div end -->
                     
                  <!-- 房差div start-->
                  <div class="childPrice_div_${suppGoodsId}">
                     <div class="optional-insurance-item adjust-product-item">
                        <div class="optional-insurance-item-name"> 单补房差
                        </div>
                        <div class="optional-insurance-single-price price">
                        		￥<span class="gapPrice_${suppGoodsId}">0</span> /人
                        </div>
                        
                        <div class="optional-insurance-quantity">
                           <select class="addtion-change-quantity lvmama-price-addition lvmama-price-flag selectGapQuantityClass"
                 		   	       name="itemMap[${suppGoodsId}].gapQuantity" goodsType="${currSuppGoods.goodsType}"
                 		   	        id="gapQuantitySelectId_${suppGoodsId}"
                 		   	        <#if saleLocalDetail['defaultGapQuantity']??>defaultGapQuantity=${saleLocalDetail['defaultGapQuantity']}</#if>
                       		   	   goodsId="${suppGoodsId}">
									<option value="0">0</option>
                           </select>份
                        </div>
                        <div class="optional-insurance-total-price price">
                           	 ￥<span class="gapPriceTotal_${suppGoodsId}">0</span>
                        </div>
                        <div class="optional-item-status status product-upgrade-operator">
                                <i class="product-item-checked-icon"></i>
                        </div>
                        <div class="clearfix"></div>
                     </div>
                     <!-- 房差div start-->
                     
                </div>
                    
            </div>
        </div>
        </#list>
</div>
<!--/当地游模板END-->
</#if>

<!--当地游JS start-->
<script>

	$(function(){//初始化"成人/儿童/房差"价格 
		changePrice();
		$(".selectAdultQuantityClass").bind("change", function(){
			setGapQuantityOption(this);//添加房差价格option
			setAuditPriceTotal(this);//设置成人总价
			countTotalPrice();//计算总价
		});	
		$(".selectChildQuantityClass").bind("change", function(){
			setChildPriceTotal(this);//设置儿童价格
			countTotalPrice();//计算总价
		});
		$(".selectGapQuantityClass").bind("change", function(){
			setGapPriceTotal(this);//设置房差总价
			countTotalPrice();//计算总价
		});	
		$(".selectAdultQuantityClass").trigger("change");
		$(".selectChildQuantityClass").trigger("change");
		$(".selectGapQuantityClass").each(function(){
			var defaultGapQuantity=$(this).attr("defaultGapQuantity");
			if(defaultGapQuantity!=null){
				$(this).find("option[value='"+defaultGapQuantity+"']").attr("selected","selected");
			}
		});
		$(".selectGapQuantityClass").trigger("change");
	});
	
	$(".selectDateClass").bind("change", function(){
	
		changePrice();//改变单价
	
		
		$(".selectAdultQuantityClass").each(function(i,item){
			setAuditPriceTotal(item);//设置成人总价
		});
		
		$(".selectChildQuantityClass").each(function(i,item){
			setChildPriceTotal(item);//设置儿童总价
		});
		
		$(".selectGapQuantityClass").each(function(i,item){
		
			setGapPriceTotal(item);//设置房差总价

		});	
		
		countTotalPrice();//计算总价
		
	});
		
	function changePrice(){
		$(".selectDateClass").each(function(i,item){
		    var selectDate=$(item).find("option:selected").attr("selectDate"); //获取Select选择日期
		    //alert(selectDate);
		    var auditPrice = $("input[data="+selectDate+"]").attr("auditPrice");//成人价
		    var childPrice = $("input[data="+selectDate+"]").attr("childPrice");//成人价
		    var gapPrice   = $("input[data="+selectDate+"]").attr("gapPrice");//房差
		    //alert("auditPrice="+auditPrice+",childPrice="+childPrice+",gapPrice="+gapPrice);
		    
		    var suppGoodsId = $("input[data="+selectDate+"]").attr("suppGoodsId");
	 		//alert(suppGoodsId);
	 		
		    if(auditPrice != ""){
				$(".auditPrice_"+suppGoodsId).text(auditPrice);//设置成人价
				//alert($(".auditPrice_"+suppGoodsId).text());
			}else{
				$(".auditPrice_div_"+suppGoodsId).css("display", "none");
			}
			if(childPrice != ""){
				$(".childPrice_"+suppGoodsId).text(childPrice);//设置儿童价
			}else{
				$(".childPrice_div_"+suppGoodsId).css("display", "none");
			}
			if(gapPrice != ""){
				$(".gapPrice_"+suppGoodsId).text(gapPrice);//设置儿童价
			}else{
				$(".gapPrice_div_"+suppGoodsId).css("display", "none");
			}
		});

		
		countTotalPrice();//计算总价
	}
	
	
	
	function setAuditPriceTotal(item){
		var suppGoodsId = $(item).attr("goodsId")//商品ID
		var adultQuantity = $(item).find("option:selected").val();//成人数量
		var auditPrice = $(".auditPrice_"+suppGoodsId).text();//成人价
		//alert("suppGoodsId="+suppGoodsId+",adultQuantity="+adultQuantity+",auditPrice="+auditPrice+",共"+changeTwoDecimal_f(auditPrice * adultQuantity));
		$(".auditPriceTotal_"+suppGoodsId).text(changeTwoDecimal_f(auditPrice * adultQuantity));//设置总成人价
	}
	
	function setChildPriceTotal(item){
		var suppGoodsId = $(item).attr("goodsId")//商品ID
		var childQuantity = $(item).find("option:selected").val();//儿童数量
		var childPrice = $(".childPrice_"+suppGoodsId).text();//成人价
		//alert("suppGoodsId="+suppGoodsId+",childQuantity="+childQuantity+",childPrice="+childPrice+",共"+changeTwoDecimal_f(childQuantity * childQuantity));
		$(".childPriceTotal_"+suppGoodsId).text(changeTwoDecimal_f(childQuantity * childPrice));//设置总成人价
	}
	
	function setGapPriceTotal(item){
		var suppGoodsId = $(item).attr("goodsId")//商品ID
		var gapQuantity = $(item).find("option:selected").val();//儿童数量
		var gapPrice = $(".gapPrice_"+suppGoodsId).text();//成人价
		//alert("suppGoodsId="+suppGoodsId+",gapQuantity="+gapQuantity+",gapPrice="+gapPrice+",共"+changeTwoDecimal_f(gapQuantity * gapPrice));
		$(".gapPriceTotal_"+suppGoodsId).text(changeTwoDecimal_f(gapQuantity * gapPrice));//设置总成人价
	}
	
	function setGapQuantityOption(item){
		var suppGoodsId = $(item).attr("goodsId")//商品ID
		var adultQuantity = $(item).find("option:selected").val();//成人数量
		//alert("adultQuantity="+adultQuantity);
		//$(".selectGapQuantityClass").html("");
		selectId = "gapQuantitySelectId_"+suppGoodsId;
		$("#"+selectId+"").html("");
		for(var i=0;i<=adultQuantity;i++){
 			var optionHtml=$("<option></option>");
			optionHtml.val(i).html(i);
			$("#"+selectId+"").append(optionHtml);
		}
	}
			
	
</script> 
<!--当地游JS end--> 