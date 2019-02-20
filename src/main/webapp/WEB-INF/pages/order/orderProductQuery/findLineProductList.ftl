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
                        <th colspan="9" style=" text-align:left;">产品选择</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
                        <td class="text_center">推荐级别</td>
                        <td class="text_center">产品ID</td>
                        <td class="text_center">产品名称</td>  
                        <td class="text_center">产品品类</td>
                        <td class="text_center">产品经理</td>
                        <td class="text_center">所属BU</td>
                        <td class="text_center">
                        			行程名称(游玩天数)
                        <td class="text_center">参考价格</td>  
                        <td class="text_center">预定</td>        
                    </tr>
                    <#if prodList?? && prodList?size &gt;  0>
                    <#list prodList  as prod>
                   <tr class="table_nav">
                    	<input type="hidden" id="${prod.productId}Hid" value="${prod.productId}"/>
						<input type="hidden" id="categoryId" value="${ordOrderProductQueryVO.categoryIds}"/>
						<input type="hidden" id="subCategoryId" value="${ordOrderProductQueryVO.subCategoryId}"/>
						
                        <td class="text_center">${prod.recommendLevel}</td>
                       	<td class="text_center">${prod.productId!''}</td>
                        <td class="text_center"><a target="_blank" href="/vst_back/prod/baseProduct/preview.do?categoryId=${prod.categoryId!''}&previewId=${prod.urlId!''}" title="">${prod.productName!''}</a></td>  
                        <td class="text_center">${prod.categoryName!''}</td>
                       <td class="text_center">${prod.managerName!''}</td>
                       <td class="text_center">
                           <#list buList as buEnum>
                               <#if prod.BU==buEnum.code>${buEnum.cnName}</#if>
                           </#list>
                       </td>
                        <td class="text_center">
                        <#if  productPropMap?? && productPropMap[prod.productId]??&&productPropMap[prod.productId]?size &gt;  0>
                        	<table width="100%" textAlgin="center">
                        		 <#list productPropMap[prod.productId]  as route>
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
                        <#if productMap[prod.productId] && productMap[prod.productId].productAddtional&&productMap[prod.productId].productAddtional.lowestSaledPrice>${productMap[prod.productId].productAddtional.lowestSaledPrice/100}${productMap[prod.productId].getLineRouteProductUnit(false,'')}
                        <#else> 暂无起价
                        </#if>
                        </td>
                        <td class="w10"><div class="operate">
                        <#if (prodGroupDateMap[prod.productId])??>
                        <a data="${prod.productId}" startDistrictId="${startDistrictIdMap[prod.productId]}" categoryId="${prod.categoryId}" groupDate="${prodGroupDateMap[prod.productId].specDate?string("yyyy-MM-dd")}" id="${prod.productId!''}Btn" class="btn btn_cc1 xzcpBtn">选择</a>
                        <#else>无团期</#if>
                        </div></td>       
                    </tr>
                    </#list>
                    <#else>
                    <tr class="table_nav"><td colspan="9"><div class="no_data mt20"><i class="icon-warn32"></i>暂无相关产品，重新输入相关条件查询！</div></td>	</tr>
                    </#if>
                </tbody>
            </table>
            <#if prodList?? && prodList?size &gt;  0>
            <div class="pages darkstyle">	
            	<@pagination.paging pageParam true "#logResultList"/>
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
  	<input type="hidden" name="copies" value="1">
    <input type="hidden" name="channel_code" />
  </form>
  <script>
  
  	function tanchu(){ 
		var _scrolltop = $(document).scrollTop()+150;
		var height_w =$(document).height();
		var width = $('.PopBox').width();
		$('.PopBox').css({'margin-left':-width/2});
		$('.pop_body_bg').css({'height':height_w,'width':$(document.body).width()}).show();
		$('.PopBox').show().css({'top':_scrolltop});
	};
	function guanbi(){ 
		$('.pop_body_bg,.PopBox').hide();
	};
	var newCategoryId = 32;
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
		newCategoryId = $(this).attr("categoryId");
		//判断用户是否登录
		if(book_user_id==""){
			tempSelectProduct = productId;
			showQueryUserIdDialog();
			return;
		 }else{
			submitFormCallback.invoke();
		 }	
	});
	
	function xzcpBtnClick(dom){
		var jDom = $(dom);
        var productId = jDom.attr("data");
        //品类id
        var categoryId=jDom.attr("categoryId");
        //查询团期
        var specDate = jDom.attr("groupDate");
        var distributionId = 2;
        if($(this).text()=="已选择"){
            return ;
        }
        var startDistrictId=jDom.attr("startDistrictId");

        $("#bookForm input[name=specDate]").val(specDate);
        $("#bookForm input[name=productId]").val(productId);
        $("#bookForm input[name=distributionId]").val(distributionId);
        $("#bookForm input[name=userId]").val(book_user_id);
        $("#bookForm input[name=startDistrictId]").val(startDistrictId);
		console.log($("#bookForm").serialize());
        //设置选中效果
        xzBtnRefresh(jDom.attr("data"));

        //判断用户是否登录
        if(book_user_id==""){
            tempSelectProduct = productId;
            showQueryUserIdDialog();
            return;
        }else{
            submitFormCallback.invoke();
        }
	}
	 function infoBookInfo(){
		if(newCategoryId == 32){
			$("#bookForm").attr("action","/lvmm_dest_back/ord/destbu/queryLineDetailList.do");
		}else{
			$("#bookForm").attr("action","/vst_order/ord/order/queryLineDetailList.do");
		}
		$("#bookForm").submit();
	 }
	 submitFormCallback.pushFun(infoBookInfo);
	
	function xzBtnRefresh(productId){
  		$(".xzcpBtn").addClass("btn_cc1").removeAttr("disabled").text("选择");
   		$("#"+productId+"Btn").removeClass("btn_cc1").text("已选择");
    }
   
  </script>