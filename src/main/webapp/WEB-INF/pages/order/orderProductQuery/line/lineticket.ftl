 <#if ticketProdPackageList?? && ticketProdPackageList?size &gt; 0>
  <div class="hotel_ebk_wrap">
  	<div class="hotel_tab_box">
  		<div class="hotel_tab">
  			<div class="table_t">景点门票</div>
  					<#list ticketProdPackageList as changeProdPackage>
  						<#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0>
  							<#assign detail=changeProdPackage.prodPackageDetails[0] />
	  						<#if changeProdPackage.prodPackageDetails?size gt 1>
	  							 <p class="listName"><a href="#" class="fr moreCategoryLineTicket" adultNum="${adultNum}" childNum="${childNum}" outProductId="${productId}" packageGroupId="${detail.groupId}" packageProductId="${detail.prodProduct.productId}" packageProductBranchId="${detail.prodProductBranch.productBranchId}">更多门票</a>${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p>
	  						<#else>
	  							 <p class="listName">${changeProdPackage.startDay}日到${changeProdPackage.endDay}日行程</p> 
	  						</#if>

                            <#--门票规格及商品信息-->
                            <#if "181" == "${prodProduct.subCategoryId}">
                                <#include "/order/orderProductQuery/line/lineticketDetailSceneHotel.ftl"/>
                            <#else>
                                <#include "/order/orderProductQuery/line/lineticketDetail.ftl"/>
                            </#if>
  					</#if>
  				</#list>
  		</div>
  	</div>
  </div>
 </#if> 