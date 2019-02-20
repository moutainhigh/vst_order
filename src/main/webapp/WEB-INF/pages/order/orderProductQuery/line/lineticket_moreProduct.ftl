 <#import "/base/order_line_func.ftl" as line_func/>
 <script>
 	$(".moreLineSelectDate").bind("change",function(){
 		try{
			setTimeout(changeNextSelect($(this),null),500);
		}catch(err){}
		changeMoreSelectDate($(this));
	});
</script>
 <#if moreProductList?? && moreProductList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">景点门票</div>   					
  					<#assign changeProdPackage=moreProductList[0] />
  						<p class="listName">${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p>
						<input type="hidden" id="sourceTotalAmount" value="${sourceTotalAmount}" />
  						<#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0>
  							<#list changeProdPackage.prodPackageDetails as detail>
		  					<#if detail.prodProductBranch?? && detail.prodProductBranch.recommendSuppGoodsList??&& detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0 >
		  					<table width="100%" class="updateChangeTable">
			  				   <tbody>
			  					<tr>
        						 <td>
									<#assign prodProductBranch=detail.prodProductBranch />
									<#--门票规格及商品信息-->
                            		<#if "181" == "${prodProduct.subCategoryId}">
										<#include "/order/orderProductQuery/line/lineticket_moreProductDetailSceneHotel.ftl"/>
								 	<#else>
									 	<#include "/order/orderProductQuery/line/lineticket_moreProductDetail.ftl"/>
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