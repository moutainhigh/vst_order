<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<link rel="stylesheet" href="http://pic.lvmama.com/styles/v6/header_new.css">
	<style type="text/css">
		.vstjj-info {
			background-color: #fffcf6;
		}
		.vstjj-info dt {
			float: left;
		}
		.vstjj-info dt {
			width: 70px;
			font-weight: bold;
		}
		.vstjj-info dd {
			margin-left: 70px;
			line-height: 20px;
			margin-bottom: 5px;
		}
		.vstjj-info dd span {
			display: block;
			float: left;
			width: 80px;
		}
		.clearfix {
			margin: 20px 70px 20px 20px;
		}
	</style>
</head>
<body style="min-width: 900px; margin: 10px 30px;">
	<!-- 景点门票 其他票 -->
	<#if categoryId == 11 || categoryId == 12 || categoryId == 13>
		<#if suppGoodsDesc??>
			<div class="vstjj-info">
				<#if suppGoodsDesc.priceIncludes??>
					<dl class="clearfix">
						<dt>费用包含</dt>
						<dd>${suppGoodsDesc.priceIncludes}</dd>
					</dl>
				</#if>
				<dl class="clearfix">
					<dt>入园须知</dt>
					<#if categoryId == 11 || (categoryId == 13 && isSUPPLIER)>
						<#if suppGoodsDesc.typeDesc??>
							<dd>票种说明：${suppGoodsDesc.typeDesc}</dd>
						</#if>
						<#if suppGoodsDesc.needTicket?exists && suppGoodsDesc.needTicket?length gt 0>
							<dd>是否需取票：
								<#if suppGoodsDesc.needTicket == 'Y'>需要
								<#else>不需要
								</#if>
							</dd>
						</#if>
						<#if formattedFlag>
							<#if suppGoodsDesc.limitTime?exists && suppGoodsDesc.limitTime?length gt 0>
								<dd>入园时间：${suppGoodsDesc.limitTime}</dd>
							</#if>
							<#if suppGoodsDesc.visitAddress?exists && suppGoodsDesc.visitAddress?length gt 0>
								<dd>入园地点：${suppGoodsDesc.visitAddress}</dd>
							</#if>
						</#if>
					</#if>
					<#if categoryId == 12>
						<#if !formattedFlag>
							<#if suppGoodsDesc && suppGoodsDesc.limitFlag??>
		                        <#if suppGoodsDesc.limitFlag=="0">
		                            <dd>入园限制：	请在入园当天的${suppGoodsDesc.getLimitTimeStr()}分以前入园</dd>
		                        <#else>
		                            <dd>入园限制：无限制</dd>
		                        </#if>
		                    </#if>
						</#if>
					</#if>
					<#if suppGoodsDesc.enterStyle??>
						<dd>入园方式：${suppGoodsDesc.enterStyle}</dd>
					</#if>
					<#if suppGoodsDesc.changeTime?? && suppGoodsDesc.changeTime?length gt 0>
						<dd>取票时间：${suppGoodsDesc.changeTime}</dd>
					<#else>
						<dd>取票时间：无限制</dd>
	                </#if>
	                
	                <#if suppGoodsDesc.changeAddress?? && suppGoodsDesc.changeAddress?length gt 0>
						<dd>取票地点：${suppGoodsDesc.changeAddress}</dd>
					</#if>
					<#if (!suppGoodsDesc.passLimitTime??) || (!suppGoodsDesc.passFlag??) || (suppGoodsDesc.passFlag == 'N')>
						<dd>入园时间说明：无限制</dd>
					<#else>
						<dd>入园时间说明：下单${suppGoodsDesc.passLimitTimeStr}后方可入园</dd>
					</#if>
				</dl>
				<!-- 景点门票 -->
				<#if categoryId == 11 || (categoryId == 13 && isSUPPLIER)>
					<#if suppGoodsDesc.others??>
						<dl class="clearfix">
							<dt>重要提示</dt>
		                  	<dd>
			                    ${suppGoodsDesc.others}
	                       </dd>
						</dl>
					</#if>
				</#if>
				<!-- 其他票 -->
				<#if categoryId == 12>
					<#if suppGoodsDesc.height?? ||
			            suppGoodsDesc.age?? ||
			            suppGoodsDesc.region?? ||
			            suppGoodsDesc.maxQuantity?? ||
			            suppGoodsDesc.express?? ||
			            suppGoodsDesc.entityTicket??||
			            suppGoodsDesc.others?? ||
			            suppGoodsDesc.describe??>
						<dl class="clearfix">
							<dt>重要提示</dt>
							<#if suppGoodsDesc.height?? ||
			                    suppGoodsDesc.age?? ||
			                    suppGoodsDesc.region?? ||
			                    suppGoodsDesc.maxQuantity?? ||
			                    suppGoodsDesc.express?? ||
			                    suppGoodsDesc.entityTicket??>
								<dd>票种说明：
									<#if suppGoodsDesc.height??>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;·身高：${suppGoodsDesc.height}</#if>
			                        <#if suppGoodsDesc.age??>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;·年龄：${suppGoodsDesc.age}</#if>
			                        <#if suppGoodsDesc.region??>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;·地域：${suppGoodsDesc.region}</#if>
			                        <#if suppGoodsDesc.maxQuantity??>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;·最大限购：${suppGoodsDesc.maxQuantity}</#if>
			                        <#if suppGoodsDesc.express??>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;·快递：${suppGoodsDesc.express}</#if>
			                        <#if suppGoodsDesc.entityTicket??>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;·实体票：${suppGoodsDesc.entityTicket}</#if>
								</dd>
							</#if>
							<#if suppGoodsDesc.others??>
								<dd>其他：${suppGoodsDesc.others}</dd>
							</#if>
							<#if suppGoodsDesc.describe??>
								<dd>描述：${suppGoodsDesc.describe}</dd>
							</#if>
						</dl>
					</#if>
				</#if>
			</div>
		</#if>
	<!-- 美食娱乐 -->
	<#elseif categoryId == 43 || categoryId == 44>
		<#if suppGoodsDesc??>
			<div class="vstjj-info">
				<#if suppGoodsDesc.priceIncludes??>
					<dl class="clearfix">
						<dt>费用包含</dt>
						<dd>${suppGoodsDesc.priceIncludes}</dd>
					</dl>
				</#if>
				<dl class="clearfix">
					<#if (suppGoodsDesc.bookingDesc??&& suppGoodsDesc.bookingDesc?length gt 0) || 
						(suppGoodsDesc.orderInfo??&& suppGoodsDesc.orderInfo?length gt 0)>
						<dt>重要提示</dt>
						<#if suppGoodsDesc.bookingDesc??>
							<dd>预定须知：${suppGoodsDesc.bookingDesc}</dd>
						</#if>
						<#if suppGoodsDesc.orderInfo??>
							<dd>预约说明：${suppGoodsDesc.orderInfo}</dd>
						</#if>
					</#if>
				</dl>
			</div>
		</#if>
	</#if>
</body>
</html>