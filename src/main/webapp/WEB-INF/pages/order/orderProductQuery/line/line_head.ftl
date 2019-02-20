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
			<div style="width:70%;float:left;">
			<#if prodProduct?? >
				<#if prodProduct.bizCategory.categoryCode=="category_route_hotelcomb">
	            	产品名称：<a target="_blank" href="http://dujia.lvmama.com/package/${prodProduct.productId}">${prodProduct.productName}</a>        产品Id：${prodProduct.productId}
	            <#elseif prodProduct.bizCategory.categoryCode=="category_route_local">
	            	产品名称：<a target="_blank" href="http://dujia.lvmama.com/local/${prodProduct.productId}">${prodProduct.productName}</a>        产品Id：${prodProduct.productId}
	            <#elseif prodProduct.bizCategory.categoryCode=="category_route_freedom">
	            	产品名称：<a target="_blank" href="http://dujia.lvmama.com/freetour/${prodProduct.productId}">${prodProduct.productName}</a>        产品Id：${prodProduct.productId}
	            <#else>
	            	产品名称：<a target="_blank" href="http://dujia.lvmama.com/group/${prodProduct.productId}">${prodProduct.productName}</a>        产品Id：${prodProduct.productId}
	            </#if>
            </#if>
			</div>
			<div style="width:30%;float:right;">
			<#if prodProduct.bizCategory.categoryCode=="category_route_group" || prodProduct.bizCategory.categoryCode=="category_route_local">
				<a class="btn btn_cc1" id="showGroupRate"  onclick="showGroupRate(${startDaeResult},${mothResult});">查看收客人数</a>&nbsp;&nbsp;
			</#if>
			<a class="btn btn_cc1" id="notifyBt" onclick="getProductNoticeByCondition();">重要通知</a>&nbsp;&nbsp;
			<a class="btn btn_cc1" id="backSearchA"
			 <#if intentionOrderId?exists >
			  href="/vst_order/ord/order/intoIntention.do"
			 <#else>
			 href="/vst_order/ord/productQuery/showLineOrderProductQueryList.do?userId=${user.userId}"
			 </#if>
			 >
			 返回搜索</a>
			<a class="btn btn_cc1" id="backSearchA"
			 <#if intentionOrderId?exists >
			  href="/vst_order/ord/order/intoIntention.do?isExit=true"
			 <#else>
			 href="/vst_order/ord/productQuery/showLineOrderProductQueryList.do?userId=${user.userId}&isExit=true"
			 </#if>
			 >
			 退出当前用户</a>
			</div>
		    </div>
		</div>
</div>