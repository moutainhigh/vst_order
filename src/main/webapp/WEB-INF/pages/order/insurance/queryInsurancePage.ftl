<#import "/base/pagination.ftl" as pagination>
<#if result??>
<#if result.items?? &&  result.items?size &gt; 0>
		<#list result.items as prodProduct>
			<div class="p_box">
				<div class="p_box">
	                <div class="co_title"> 
		                <strong class="f14 cc8">
		                	  ${prodProduct.productName!''}
		               	</strong>
	                </div>
	                <table class="co_p_table">
	                    <tbody>
	                    	<tr>
		                        <td>
		                            <table class="co_table">
		                                <thead> 
		                                    <tr class="noborder">
		                                        <th nowrap="nowrap" style="width:25%;">商品编号</th>
		                                        <th nowrap="nowrap" style="width:25%">产品名称(商品名称)</th>
		                                        <th nowrap="nowrap" style="width:15%">状态</th>
		                                        <th nowrap="nowrap" style="width:10%">被保天数</th>
		                                        <th nowrap="nowrap" style="width:25%">操作</th>
		                                    </tr>
		                                </thead>
		                                <tbody>
		                                	<#if sgntMap ??>
		                                		<#list sgntMap?keys as key>
		                                			<#if (key ??) && (key == prodProduct.productId) && (sgntMap[key] ??) && (sgntMap[key]?size &gt; 0)>
		                                					<#list sgntMap[key] as item>
							                                	<tr>
							                                		<td>${item.suppGoodsId!''}</td>
							                                		<td>${prodProduct.productName!''}(${item.goodsName!''})</td>
							                                		<td>
								                                		<#if prodProduct.cancelFlag ?? &&  prodProduct.cancelFlag =='Y' >
								                                			有效
								                                		</#if>
									                                	<#if prodProduct.cancelFlag ?? &&  prodProduct.cancelFlag !='Y' >
									                                		无效
									                                	</#if>
							                                		</td>
							                                		<td>
							                                		   <#if prodProduct?? && prodProduct.prodProductPropList?? && prodProduct.prodProductPropList?size &gt; 0>
							                                		       <#list prodProduct.prodProductPropList as prop>
							                                		           <#if prop?? && prop.bizCategoryProp?? && prop.bizCategoryProp.propCode == 'days_of_insurance'>
							                                		               ${prop.propValue!''}
							                                		           </#if>
							                                		       </#list>
							                                		   </#if>
                                                                    </td>
								                                    <td>
								                                   		<input type="button" style="width: 53px;height: 25px; font-size: 13px;font-style: normal; color: #000000;font-family: 'Applied Font';" 
								                                        	name="goodsId" value="预订" onclick="toBook('/vst_order/ord/insurance/book/authentication.do?goodsId=${item.suppGoodsId}');">
								                                    </td>
							                                	</tr>
		                                					</#list>
		                                			</#if>
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
		<@pagination.paging result true "#insuranceListDiv"></@pagination.paging>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关保险，请重新输入相关条件查询！</div>
	</#if>
<#else>
	<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关保险，请重新输入相关条件查询！</div>
</#if>