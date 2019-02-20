<#import "/base/pagination.ftl" as pagination>
<#import "/base/hotel_func.ftl" as hf>
<#--携程促销处理-->
<script src="${rc.contextPath}/js/book/supplier_promotion.js"></script>
<#if pageParam??>
<#if pageParam.items?? &&  pageParam.items?size &gt; 0>
			<div class="p_box">
				<#list pageParam.items as ordOrderProductVO> 
				<div class="p_box">
                <div class="co_title">
                	<#if ordOrderProductVO?? && ordOrderProductVO.hasNotice=="Y">
	                	<a class="fr" href="javascript:void(0);" onclick="showProductNotice('${ordOrderProductVO.productId}');">重要通知</a>
	                </#if>
	                <a title="查看酒店前台详情" href="http://hotels.lvmama.com/hotel/${ordOrderProductVO.productId}" target="_blank">
	                	<strong class="f14 cc8">
	                		[${ordOrderProductVO.districtName!''}]${ordOrderProductVO.productName!''}
	                	</strong>
	                </a>
	                <@hf.starlevel ordOrderProductVO.starRateName/>
	                <span>酒店地址：${ordOrderProductVO.address!''}</span>
	                <span>开业/装修：
	                	<#if ordOrderProductVO?? && ordOrderProductVO.establishmentDate?? && ordOrderProductVO.establishmentDate!''>
	                		${ordOrderProductVO.establishmentDate?substring(0,4)}
	                	</#if>
	                	/
	                	<#if ordOrderProductVO?? && ordOrderProductVO.renovationDate?? && ordOrderProductVO.renovationDate!''>
	                		${ordOrderProductVO.renovationDate?substring(0,4)}
	                	</#if>
	                </span>
                </div>
                <table class="co_p_table">
                    <tbody>
                    	<tr>
                        <td>
                            <table class="co_table">
                                <thead> 
                                    <tr class="noborder">
                                        <th nowrap="nowrap" style="width:5%;">推荐级别</th>
                                        <th nowrap="nowrap" style="width:20%">房型</th>
                                        <th nowrap="nowrap" style="width:20%">床型</th>
                                        <th nowrap="nowrap" style="width:5%">限住人数</th>
                                        <th nowrap="nowrap" style="width:5%">宽带</th>
                                        <th nowrap="nowrap" style="width:10%">付款方式</th>
                                        <th nowrap="nowrap" style="width:15%">限制</th>
                                        <th nowrap="nowrap" style="width:10%">房间价格</th>
                                        <th nowrap="nowrap" style="width:5%">早餐</th>
                                        <th nowrap="nowrap" style="width:5%">操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                	<#list ordOrderProductVO.ordOrderGoodsVOList as ordOrderGoodsVO> 
	                                	<tr <#if ordOrderGoodsVO_index+1 &gt; 3 || ordOrderGoodsVO_index+1 &gt; 20>style="display:none;"</#if>>
	                                        <td>${ordOrderGoodsVO.recommentLevel!''}</td>
	                                        <td class="co_unline" id="room-${ordOrderGoodsVO.suppGoodsId}"><a class="J_tip" tip-content="" href="javascript:void(0);"  onmouseover="branchNameMouseover('${ordOrderGoodsVO.productId!''}','${ordOrderGoodsVO.suppGoodsId!''}',this);">${ordOrderGoodsVO.branchName!''}(${ordOrderGoodsVO.goodsName!''})</a></td>
	                                        <td class="co_unline"><a class="J_tip" tip-content="" href="javascript:void(0);" onmouseover="bedTypeMouseover('${ordOrderGoodsVO.productId!''}','${ordOrderGoodsVO.suppGoodsId!''}',this);">${ordOrderGoodsVO.strBedType!''}</a></td>
	                                        <td>${ordOrderGoodsVO.maxVisitor!''}</td>
	                                        <td class="co_unline"><a class="J_tip" tip-content="" href="javascript:void(0);" onmouseover="internetMouseover('${ordOrderGoodsVO.productId!''}','${ordOrderGoodsVO.suppGoodsId!''}',this);">${ordOrderGoodsVO.strInternet!''}</a></td>
	                                        <td>
	                                        	<#if ordOrderGoodsVO.payTarget=='PREPAID'>预付
	                                        	<#elseif ordOrderGoodsVO.payTarget=='PAY'>现付
	                                        	</#if>
	                                        </td>
	                                        <td>
	                                        	${ordOrderGoodsVO.guarRuleStr!''}
	                                        </td>
	                                        <td class="com_unline">
		                                        <a class="jTip" id="id${ordOrderGoodsVO.suppGoodsId}" onclick="javascript:void(0);" 
		                                        href="/vst_order/ord/order/productAndGoods/getTimePrice.do?width=450&visitTime=${ordOrderProductQueryVO.startDate}&leaveTime=${ordOrderProductQueryVO.endDate}&suppGoods=${ordOrderGoodsVO.suppGoodsId}&quantity=1" name="时间价格表">RMB ${ordOrderGoodsVO.priceYuan!''}</a>
		                                       	<#if ordOrderGoodsVO.diffPriceFlag=='N'>
			                                        <i tip-content="此房型在预定时段&lt;/br&gt;内有不同价格" class="J_tip e_icon icon-warn"></i>
		                                        </#if>
	                                        </td>
	                                        <td class="co_unline"><a class="J_tip" tip-content="" href="javascript:void(0);" onmouseover="breakfastMouseover('${ordOrderGoodsVO.productId!''}',this,${ordOrderGoodsVO.breakfast!''});"><#if ordOrderGoodsVO.breakfast gt 0>有早<#else>无早</#if></a></td>
	                                        <#--是否有本地库存标志位-->
	                                        <#if ordOrderGoodsVO.isHasStock == 'Y'>
												<#if ordOrderGoodsVO.promFlag == 'Y'&&ordOrderGoodsVO.bookFlag == 'N'>
                                                    <td><input type="button" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #ccc;font-family: 'Applied Font';" name="goodsId" value="不可订" disabled="disabled" title="该商品不满足促销条件,不可售!"></td>
												<#else>
	                                        		<td><input type="button" id="bookBtn-${ordOrderGoodsVO.suppGoodsId}" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #000000;font-family: 'Applied Font';" name="goodsId" value="预订" onclick="openBookInfo(${ordOrderGoodsVO.suppGoodsId});"></td>
												</#if>
											<#else>
	                                    		<td><input type="button" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #ccc;font-family: 'Applied Font';" name="goodsId" value="已订完" onclick="openBookInfo(${ordOrderGoodsVO.suppGoodsId});" disabled="disabled" title="该商品在本地无库存,不可售!"></td>
	                                    	</#if>
											<#if ctripPromoJsonMap??&&ctripPromoJsonMap?size gt 0 &&ctripPromoJsonMap['Prom'+ordOrderGoodsVO.suppGoodsId]??>
                                                <script>buildSupplierPromotions('${ordOrderGoodsVO.suppGoodsId!''}','${ctripPromoJsonMap['Prom'+ordOrderGoodsVO.suppGoodsId]?js_string}');</script>
											</#if>
	                                    </tr>
                                	</#list>
                                	<#if ordOrderProductVO.ordOrderGoodsVOList?size &gt; 3>
	                                    <tr>
	                                        <td colspan="12" style="padding:5px 20px">
	                                           <a href="javascript:;" class="fr p_rel fb J_arrow active">其他房型<i class="ui-arrow-top blue-ui-arrow-top mt8"></i></a>
	                                        </td>
	                                    </tr>
                                    </#if>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </tbody>
                </table>
            </div>
			</#list>
		</div>
		<@pagination.paging pageParam true "#productListDiv">
			<#--时间价格表显示JS-->
			<script src="/vst_order/js/tooltip/js/jtip.js"></script>
			<#--其他鼠标悬停提示和房间收缩-->
			<script type="text/javascript">loadJtip();</script>
		</@pagination.paging>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关酒店，请重新输入相关条件查询！</div>
	</#if>
<#else>
	<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关酒店，请重新输入相关条件查询！</div>
</#if>