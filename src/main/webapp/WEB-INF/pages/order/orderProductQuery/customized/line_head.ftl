<div class="iframe_header">
		<div id="userInfoDiv" style="text-align:center;position:fixed;margin-left:500px;background:#F9FAFB;"> 
			<#if user.userId?? >
			    <span style="color: #EE3388;font-size:22px;font-weight: bold;">客人姓名：${user.userName}</span><#--&nbsp;&nbsp;<a href="javascript:void(0);" onclick="accountLogout();">退出当前用户</a>-->
			<#else>
				<span style="color: #EE3388;font-size:22px;font-weight: bold;">尚未登陆会员信息</span>
			</#if>
		</div>
		<div class="hotel_line_title">
		<div class="">
			<#if packageTourProductVo?? >
				<#if packageTourProductVo.bizCategory.categoryCode=="category_route_hotelcomb">
	            	产品名称：<a target="_blank" href="http://dujia.lvmama.com/package/${packageTourProductVo.productId}">${packageTourProductVo.productName}</a>        产品Id：${packageTourProductVo.productId}
	            <#elseif packageTourProductVo.bizCategory.categoryCode=="category_route_local">
	            	产品名称：<a target="_blank" href="http://dujia.lvmama.com/local/${packageTourProductVo.productId}">${packageTourProductVo.productName}</a>        产品Id：${packageTourProductVo.productId}
	            <#elseif packageTourProductVo.bizCategory.categoryCode=="category_route_freedom">
	            	产品名称：<a target="_blank" href="http://dujia.lvmama.com/freetour/${packageTourProductVo.productId}">${packageTourProductVo.productName}</a>        产品Id：${packageTourProductVo.productId}
	            <#else>
	            	产品名称：<a target="_blank" href="http://dujia.lvmama.com/group/${packageTourProductVo.productId}">${packageTourProductVo.productName}</a>        产品Id：${packageTourProductVo.productId}
	            </#if>
            </#if>
			<div style="width:210px;float:right;">
			<a class="btn btn_cc1" id="notifyBt" onclick="getProductNoticeByCondition();">重要通知</a>&nbsp;&nbsp;
			<a class="btn btn_cc1" id="backSearchA"
			 <#if intentionOrderId?exists >
			  href="/vst_order/ord/order/intoIntention.do"
			 <#else>
			 href="/vst_order/ord/productQuery/customized/showCustomizedOrderProductQueryList.do?userId=${user.userId}"
			 </#if>
			 >
			 返回搜索</a>
			</div>
		    </div>
		</div>
</div>