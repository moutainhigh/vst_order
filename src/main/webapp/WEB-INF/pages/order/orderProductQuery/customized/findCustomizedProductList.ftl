<#--页眉-->
<#import "/base/spring.ftl" as spring/>
<#import "/base/pagination.ftl" as pagination>
<#--页面导航-->
<div class="p_box" id="logResultList">
			<input type="hidden" name="userId" />
		
			<input type="hidden" id="selectedEarnestHid" value=""/>
            <table class="p_table table_center product">
                <thead> 
                    <tr class="noborder">
                        <th colspan="7" style=" text-align:left;">产品选择</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
                        <td class="text_center">产品ID</td>
                        <td class="text_center">产品名称</td>  
                        <td class="text_center">产品品类</td>
                        <td class="text_center">
                        			行程名称(游玩天数)
                        <td class="text_center">参考价格</td>  
                        <td class="text_center">预定</td>        
                    </tr>
                    <#if result.items?? &&  result.items?size &gt; 0>
	                    <#list result.items as prod>
		                    <tr class="table_nav">
		                    	<input type="hidden" id="${prod.productId}Hid" value="${prod.productId}"/>
		
		                       	<td class="text_center">${prod.productId!''}</td>
		                        <td class="text_center"><a target="_blank" href="/vst_back/prod/baseProduct/preview.do?categoryId=${prod.bizCategoryId!''}&previewId=${prod.urlId!''}" title="">${prod.productName!''}</a></td>  
		                        <td class="text_center">${prod.bizCategory.categoryName!''}</td>
		                        <td class="text_center">
			                        <#if  productPropMap?? && productPropMap[prod.productId?c]?? && productPropMap[prod.productId?c]?size &gt;0>
			                        	<table width="100%" textAlgin="center">
			                        		 <#list productPropMap[prod.productId?c]  as route>
				                        		<tr>
				                        			<td>${route.routeName}(${route.routeNum}天${route.stayNum}晚)</td>
				                        		</tr>
			                        		</#list>
			                        	</table>
		                        	<#else>
		                        	---
		                        	</#if>
		                        </td>
		                        <td class="text_center">
			                        <#if productMap?? && productMap[prod.productId?c]?? && productMap[prod.productId?c].productAddtional?? && productMap[prod.productId?c].productAddtional.lowestSaledPrice??>${productMap[prod.productId?c].productAddtional.lowestSaledPrice/100}${productMap[prod.productId?c].getLineRouteProductUnit(false,'')}
			                        <#else> 
			                        	暂无起价
			                        </#if>
		                        </td>
		                        <td class="w10"><div class="operate">
			                        <#if prodGroupDateMap?? && prodGroupDateMap[prod.productId?c]??>
			                        	<a data="${prod.productId}" startDistrictId="${startDistrictIdMap[prod.productId?c]}" categoryId="${prod.categoryId}" groupDate="${prodGroupDateMap[prod.productId?c].specDate?string("yyyy-MM-dd")}" id="${prod.productId!''}Btn" class="btn btn_cc1 xzcpBtn">选择</a>
			                        <#else>
			                        	无团期
			                        </#if>
		                        </div></td>       
		                    </tr>
	                    </#list>
                  <#else>
                    <tr class="table_nav"><td colspan="7"><div class="no_data mt20"><i class="icon-warn32"></i>暂无相关产品，重新输入相关条件查询！</div></td>	</tr>
                  </#if>
                </tbody>
            </table>
            <#if result.items?? &&  result.items?size &gt; 0>
            	<div class="pages darkstyle">	
            		<@pagination.paging result true "#logResultList"/>
			    </div>
			</#if>
  </div>
  <div id="filter" style="display:none">${filter}</div>
  <form id="bookForm" action="" method="post">
  	<input type="hidden" name="specDate" >
  	<input type="hidden" name="productId" >
  	<input type="hidden" name="distributionId" >
  	<input type="hidden" name="userId" >
  	<input type="hidden" name="startDistrictId" >
  </form>
  <script>
	$('.xzcpBtn').click(function(){
		var productId = $(this).attr("data");
		//品类id
		var categoryId=$(this).attr("categoryId");		
		//查询团期	
  		var specDate = $(this).attr("groupDate");
		var distributionId = 2;
		if($(this).text()=="已选择"){
			return ;
		}
		var startDistrictId=$(this).attr("startDistrictId");
		
		$("#bookForm input[name=specDate]").val(specDate);
		$("#bookForm input[name=productId]").val(productId);
		$("#bookForm input[name=distributionId]").val(distributionId);
		$("#bookForm input[name=userId]").val(book_user_id);
		$("#bookForm input[name=startDistrictId]").val(startDistrictId);
		 //设置选中效果
		xzBtnRefresh($(this).attr("data"));
		
		//判断用户是否登录
		if(book_user_id==""){
			tempSelectProduct = productId;
			showQueryUserIdDialog();
			return;
		 }else{
			submitFormCallback.invoke();
		 }	
	});
	 function infoBookInfo(){		
		$("#bookForm").attr("action","/vst_order/ord/productQuery/customized/queryCustomizedProductDetailList.do");
		$("#bookForm").submit();
	 }
	submitFormCallback.pushFun(infoBookInfo);
	
	 function xzBtnRefresh(productId){
  		$(".xzcpBtn").addClass("btn_cc1").removeAttr("disabled").text("选择");
   		$("#"+productId+"Btn").removeClass("btn_cc1").text("已选择");
   }
   
  </script>