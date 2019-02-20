<#import "/base/spring.ftl" as s/>
<table  class="p_table table_center mt20">
		                <thead>
		                    <tr>
		                        <th>促销编号</th>
		                        <th>促销名称</th>
		                        <th>优惠金额</th>
		                    </tr>
		                </thead>
		                <tbody>
			                <#list ordPromotions  as ordPromotion> 
			               		<tr>
			                        <td>
			                        	<a title="点击查看促销详情" href="/vst_prom/prom/promotion/showPromotionMaintain.do?promPromotionId=${ordPromotion.promPromotionId!''}" target="_blank">
											${ordPromotion.promPromotionId!''}
										</a>
			                        </td>
			                        <td>
										${ordPromotion.promTitle!''}
									</td>
			                        <td>
			                            ${ordPromotion.favorableAmount/100}
									</td>
									
			                     
			                    </tr>
			                </#list>
	                
		                 </tbody>
	            		 </table>

<script type="text/javascript">

</script>



