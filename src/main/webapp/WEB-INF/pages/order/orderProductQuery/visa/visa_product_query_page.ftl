<#import "/base/pagination.ftl" as pagination>
<#import "/base/hotel_func.ftl" as hf>
<#if result??>
<#if result.items?? &&  result.items?size &gt; 0>
		<#list result.items as prodProduct>
			<div class="p_box">
				<div class="p_box">
                <div class="co_title"> 
	                <a title="查看签证前台详情" href="http://www.lvmama.com/visa/${prodProduct.bizDistrict.pinyin}" hidefocus="false" target="_blank">
	                	<strong class="f14 cc8">
	                		  ${prodProduct.productName!''}  <#--${prodProduct.recommendLevel!''}-->
	                	</strong>
	                </a>
	                <span class="lt100" style="color:blue;">产品编号：${prodProduct.productId!''}</span>
                </div>
                <table class="co_p_table">
                    <tbody>
                    	<tr>
                        <td>
                            <table class="co_table">
                                <thead> 
                                    <tr class="noborder">
                                        <th nowrap="nowrap" style="width:5%;">推荐级别</th>
                                        <th nowrap="nowrap" style="width:15%">规格名称</th>
                                        <th nowrap="nowrap" style="width:5%">所属领区</th>
                                        <th nowrap="nowrap" style="width:5%">签证类型</th>
                                        <th nowrap="nowrap" style="width:5%">使领馆办理工作日</th>
                                        <th nowrap="nowrap" style="width:5%">有效期</th>
                                        <th nowrap="nowrap" style="width:5%">停留时间</th>
                                        <th nowrap="nowrap" style="width:5%">入境次数</th>
                                        <th nowrap="nowrap" style="width:5%">价格</th>
                                        <th nowrap="nowrap" style="width:5%">操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <#if prodProduct.prodProductBranchList?? &&  prodProduct.prodProductBranchList?size &gt; 0>
                                	<#list prodProduct.prodProductBranchList as prodProductBranch> 
                                		<tr>
                                			<td>${prodProductBranch.recommendLevel!''}</td>
                                			<td>${prodProductBranch.branchName!''}</td>
                                			<td>
                                				<#list prodProductBranch.propValue['visa_range'] as propValue>
                                					 <#list visaRangeList as rangeCity>
                                					 	<#if rangeCity.dictId==propValue.id>
													   		${rangeCity.dictName}
													   		<#break />
													   	 </#if>
													   </#list>
											    </#list>
                                			</td>
	                                        <td>
	                                        	<#list prodProductBranch.propValue['visa_type'] as propValue>
												   ${propValue.name}
											    </#list>
											</td>
                                			<td>
												${prodProductBranch.propValue['visa_handle_length']}
											</td>
                                			<td>
                                				${prodProductBranch.propValue['visa_validity']}
											</td>
                                			<td>
                                				${prodProductBranch.propValue['visa_stay_days']}
											</td>
	                                        <td>
	                                        	${prodProductBranch.propValue['visa_arrival_count']}
	                                        </td>
	                                        <td>
	                                        	<#assign goodsId=0/>
	                                        	<#if prodProductBranch.suppGoodsList?? && prodProductBranch.suppGoodsList?size &gt; 0>
		                                        	<#list prodProductBranch.suppGoodsList as suppGoods>
													   ${suppGoods.suppGoodsBaseTimePrice.getYuanOfPrice(suppGoods.suppGoodsBaseTimePrice.price)}
													   <#assign goodsId=suppGoods.suppGoodsId/>
													   <#break />
												    </#list>
											    </#if>
											    
											</td>
	                                         <td>
	                                        	<input type="button" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #000000;font-family: 'Applied Font';" 
	                                        	name="goodsId" value="预订" onclick="toBook('/vst_order/ord/book/visa/infoFillIn.do?goodsId=${goodsId}');">
	                                        </td>
                                		</tr>
                                	</#list>
                                </#if>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </tbody>
                </table>
            </div>
		</div>
		</#list>
		<@pagination.paging result true "#productListDiv">
			<script src="/vst_order/js/tooltip/js/jtip.js"></script>
			<script type="text/javascript">
				function loadJtip(){
						$('.J_tip').lvtip({
					        templete: 3,
					        place: 'bottom-left',
					        offsetX: 0,
					        events: "live" 
					    });
				}
				loadJtip();
			</script>
		</@pagination.paging>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关签证，请重新输入相关条件查询！</div>
	</#if>
<#else>
	<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关签证，请重新输入相关条件查询！</div>
</#if>