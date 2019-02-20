       <#--页眉-->
<#import "/base/spring.ftl" as spring/>
<#import "/base/pagination.ftl" as pagination>
<link rel="stylesheet" type="text/css" href="http://pic.lvmama.com/styles/youlun/birthcalendar.css" />
<style>
.pop_table th, .pop_table td {
    padding: 5px 0;
}
</style>
<input type="hidden" name="specDate" id="specDate" value="${specDate?string("yyyy-MM-dd")}">
<#--提交订单的Form-->
<form id="ordForm">
	
</form>
		<#if shipProdBranchList?? && shipProdBranchList?size &gt; 0>
		<div class="p_box">
			<div id="shipError"></div>
			<form id="shipForm">
            <table class="p_table table_center cfTable" combType="${combOptionType['shipType']}">
                <thead> 
                    <tr class="noborder">
                        <th colspan="11" style=" text-align:left;">舱房信息</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
                    	<td class="text_left">舱型</td>
                        <td class="text_left">房型</td>
                        <td class="text_left">可住人数</td>
                        <td>第1、2人价格</td>
                        <td>第3、4人成人价</td>
                        <td>第3、4人儿童价</td>
                        <td>床位费</td>
						<td>成人数</td>
                        <td>儿童数</td>
                        <td>房间数</td>
                        <td>选择</td>
                    </tr>
                    <#assign idIndex = 0 />
                    <#assign shipType=combOptionType['shipType'] />
                    <#if shipProdBranchList?? && shipProdBranchList?size &gt; 0>
                    <#list shipProdBranchList as pb>
                    <#assign goods = null />
                	<#assign timePrice = null />
                    <#if pb.goodsList ?? && pb.goodsList?size &gt; 0 >
                    	<#assign goods = pb.goodsList[0] />
                    	<#if goods?? && goods.goodsMultiTimePriceList?size &gt; 0>
                    		<#assign timePrice = goods.goodsMultiTimePriceList[0] />
                    	</#if>
                    </#if>
                    <#if timePrice!=null && pb.minPriceOfGoodsList!=null && pb.minPriceOfGoodsList &gt; 0 >
                    <tr class="table_nav" productBranchId="<#if pb??>${pb.productBranchId}</#if>"  productBranchName="${pb.branchName}" suppGoodsId="<#if goods??>${goods.suppGoodsId}</#if>" stock="<#if timePrice.stockType=='CONTROLLABLE'>${timePrice.stock}<#else>-1</#if>" title="<#if timePrice.stockType=='CONTROLLABLE'>剩余库存:${timePrice.stock}<#else>库存类型:现询</#if>" >
                    	<td class="text_left">${pb.propValue['cabin_type'][0].name}</td>
                        <td class="text_left">
                        	<div id="${goods.suppGoodsId}Div" style="display:none;">
								舱房设施：
								<#if pb.propValue["window"]?? && pb.propValue["window"]?size gt 0>
								<#list pb.propValue["window"] as propValue>
									${propValue.name},
									</#list>
								</#if>
								<#if pb.propValue["balcony"]?? && pb.propValue["balcony"]?size gt 0>
								<#list pb.propValue["balcony"] as propValue>
									${propValue.name},
									</#list>
					            </#if>
					            <#if pb.propValue["landscape"]?? && pb.propValue["landscape"]?size gt 0>
	                             	<#list pb.propValue["landscape"] as propValue>
					                    ${propValue.name},
					                </#list>
					            </#if>
					            <#if pb.propValue["bed_type"]?? && pb.propValue["bed_type"]?size gt 0>
	                             	<#list pb.propValue["bed_type"] as propValue>
					                    ${propValue.name},
					                </#list>
					            </#if>
	                            ${pb.propValue["cabin_description"]}。&#13;
	                            	位于甲板：${pb.propValue["deck_floor"]!''}层
                        	</div>
                        	<a class="J_tip" tip-content="" href="javascript:void(0);" onmouseover="houseTypeMouseover('${goods.suppGoodsId}',this);">${pb.branchName}</a>
                        </td>
                        <td name="occupant_number" class="text_center" data="${pb.propValue['occupant_number']}">
                        <#if pb.propValue["max_occupant_number"][0].name != pb.propValue["min_occupant_number"][0].name>
                        	${pb.propValue["min_occupant_number"][0].name}~${pb.propValue["max_occupant_number"][0].name}
                        	<#else>
                        	${pb.propValue["max_occupant_number"][0].name}
                        </#if>
                        
                        </td>
                        <td name="fstPrice" data="<#if pb!=null && pb.minPriceOfGoodsList!=0>${pb.minPriceOfGoodsList/100}</#if>"><#if pb!=null && pb.minPriceOfGoodsList!=0>&yen;${pb.minPriceOfGoodsList/100}<#else>----</#if></td>
                        <td name="secPrice" data="<#if timePrice!=null &&  timePrice.secPrice && timePrice.secPrice!=0>${timePrice.secPrice/100}</#if>"><#if timePrice!=null && timePrice.secPrice &&timePrice.secPrice!=0>&yen;${timePrice.secPrice/100}<#else>----</#if></td>
                        <td name="childPrice" data="<#if timePrice!=null && timePrice.childPrice && timePrice.childPrice!=0>${timePrice.childPrice/100}</#if>"><#if timePrice!=null && timePrice.childPrice && timePrice.childPrice!=0>&yen;${timePrice.childPrice/100}<#else>----</#if></td>
                         <td name="gapPrice" data="<#if timePrice!=null && timePrice.gapPrice && timePrice.gapPrice!=0>${timePrice.gapPrice/100}</#if>"><#if timePrice!=null && timePrice.gapPrice &&timePrice.gapPrice!=0>&yen;${timePrice.gapPrice/100}<#else>&yen;${pb.minPriceOfGoodsList/100}</#if></td>
                        <td><input type="text" class="w5" errorEle="ship" data="adult_count"  maxNumber="${pb.propValue["max_occupant_number"][0].name}" minNumber="${pb.propValue["min_occupant_number"][0].name}" name="adult_count${idIndex}" style="text-align:center" value="0" required=true number=true /></td>
                        <td><input type="text" class="w5" errorEle="ship" data="child_count" maxNumber="${pb.propValue["max_occupant_number"][0].name}" minNumber="${pb.propValue["min_occupant_number"][0].name}" name="child_count${idIndex}" style="text-align:center" value="0" required=true number=true /></td>
                        <td>
                            <select class="w10" style="padding:0;margin:0;text-align:center" name="room_count">
                                <option>0</option>
                            </select>
                        </td>
                        <td><div class="operate" style="text-align:center;"><a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" name="xzcfBtn" data-type="cangfang">选择</a></div></td>
                    </tr>
                    </#if>
                    <#assign idIndex =	idIndex+1 />
                    </#list>
                    </#if>
                </tbody>
            </table>
            </form>
        </div>
        </#if>
        
        <#if visaProductBranchList?? && visaProductBranchList?size &gt; 0 >
        <div class="p_box">
            <table class="p_table table_center qzTable" combType="${combOptionType['visaType']}">
                <thead> 
                    <tr class="noborder">
                        <th colspan="7" style=" text-align:left;">签证</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
                        <td class="text_left">签证名称</td>
                        <td class="text_left">市场价</td>
                        <td class="text_left">驴妈妈价</td>
                        <td>计价单位</td>
                        <td>使用日期</td>
						<td>人数/份数</td>
                        <td>选择</td>
                    </tr>
                     <#list visaProductBranchList as productBranch>
                     	<#assign goods = null />
                    	<#assign timePrice = null />
                    	<#if productBranch.goodsList ?? && productBranch.goodsList?size &gt; 0 >
                    	<#assign goods = productBranch.goodsList[0] />
                    	<#if goods?? && goods.goodsSimpleTimePriceList?size &gt; 0>
                    		<#assign timePrice = goods.goodsSimpleTimePriceList[0] />
                    	</#if>
                   	    </#if>
               	    <#if timePrice!=null  && productBranch.minPriceOfGoodsList!=null && productBranch.minPriceOfGoodsList &gt; 0>
                    <tr class="table_nav" adult="" child="" useself=""  suppGoodsId="<#if goods!=null>${goods.suppGoodsId}</#if>">
                        <td class="text_left">${productBranch.branchName}</td>
                        <td class="text_left"><#if timprice!=null && timePrice.marketPrice!=null>${timePrice.marketPrice/100}元/人<#else>---- </#if></td>
                        <td class="text_left" price=<#if productBranch!=null && productBranch.minPriceOfGoodsList!=null>${productBranch.minPriceOfGoodsList/100}</#if> ><#if productBranch!=null && productBranch.minPriceOfGoodsList!=null>${productBranch.minPriceOfGoodsList/100}元/人<#else>---- </#if></td>
                        <td>人</td>
                        <td>${specDate?string("yyyy-MM-dd")}</td>
						<td></td>
                        <td><div class="operate" style="text-align:center;"><a class="btn  w8 " style="margin:0;padding:5px 0;" name="xzqzBtn">已选择</a><#if combOptionType['visaType']=="REQUIRED"><a class="syzbq">使用自备签</a></#if></div></td>
                    </tr>
                    </#if>
                    </#list>
                </tbody>
            </table>
        </div>
        </#if>
        
        <#if sightSeeingProductList?? && sightSeeingProductList?size &gt; 0>
        <div class="p_box">
            <table class="p_table table_center ggTable" combType="${combOptionType['sightSeeingType']}">
                <thead> 
                    <tr class="noborder">
                        <th colspan="7" style=" text-align:left;">岸上观光</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
                        <td class="text_left">岸上观光</td>
                        <td class="text_left">市场价</td>
                        <td class="text_left">驴妈妈价</td>
                        <td>计价单位</td>
                        <td>使用日期</td>
						<td>人数/份数</td>
                        <td>选择</td>
                    </tr>
                    <#list sightSeeingProductList as product>
                    	<#assign goods = null />
                    	<#assign timePrice = null />
                    	<#assign productBranch = null/>
                    <#if product.additionalProductBranchVOList?? && product.additionalProductBranchVOList?size &gt; 0 >
                    	<#assign productBranch = product.additionalProductBranchVOList[0]>
                    	<#if productBranch.goodsList ?? && productBranch.goodsList?size &gt; 0 >
                    	<#assign goods = productBranch.goodsList[0] />
                    	<#if goods?? && goods.goodsSingleTimePriceList?size &gt; 0>
                    		<#assign timePrice = goods.goodsSingleTimePriceList[0] />
                    	</#if>
                    	</#if>
                    </#if>
                    <#if timePrice!=null && timePrice.auditPrice!=null>
                    <tr class="table_nav" adult="" child=""  suppGoodsId="<#if goods!=null>${goods.suppGoodsId}</#if>">
                        <td class="text_left">${product.productName}</td>
                        <td class="text_left"><#if timePrice!=null&&timePrice.auditMarketPrice!=null>${timePrice.auditMarketPrice/100}元/成人<#else>无成人价</#if> <#if timePrice!=null&&timePrice.childMarketPrice!=null>${timePrice.childMarketPrice/100}元/儿童<#else>无儿童价</#if></td>
                        <td class="text_left" adult="<#if timePrice!=null&&timePrice.auditPrice!=null>${timePrice.auditPrice/100}</#if>" child="<#if timePrice!=null&&timePrice.childPrice!=null>${timePrice.childPrice/100}</#if>"><#if timePrice!=null&&timePrice.auditPrice!=null>${timePrice.auditPrice/100}元/成人<#else>无成人价</#if> <#if timePrice!=null&&timePrice.childPrice!=null>${timePrice.childPrice/100}元/儿童<#else>无儿童价</#if></td>
                        <td>${product.propValue['pricing_type'][0].name}</td>
                        <td>${specDate?string("yyyy-MM-dd")}</td>
						<td></td>
                        <td><div class="operate" style="text-align:center;"><a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" name="xzasggBtn">选择</a></div></td>
                   </tr>
                   </#if>
                    </#list>
                </tbody>
            </table>
        </div>
        </#if>
        
        <#if combAdditionProductList?? && combAdditionProductList?size &gt; 0>
        <div class="p_box">
            <table class="p_table table_center fjTable" combType="${combOptionType['additionType']}">
                <thead> 
                    <tr class="noborder">
                        <th colspan="7" style=" text-align:left;">邮轮附加项</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
                        <td class="text_left">邮轮附加项</td>
                        <td class="text_left">市场价</td>
                        <td class="text_left">驴妈妈价</td>
                        <td>计价单位</td>
                        <td>使用日期</td>
						<td>人数/份数</td>
                        <td>选择</td>
                    </tr>
                    <#list combAdditionProductList as product>
                    	<#assign goods = null />
                    	<#assign timePrice = null />
                    	<#assign productBranch = null/>
                    <#if product.additionalProductBranchVOList?? && product.additionalProductBranchVOList?size &gt; 0 >
                    	<#assign productBranch = product.additionalProductBranchVOList[0]>
                    	<#if productBranch.goodsList ?? && productBranch.goodsList?size &gt; 0 >
                    	<#assign goods = productBranch.goodsList[0] />
                    	<#if goods?? && goods.goodsSingleTimePriceList?size &gt; 0>
                    		<#assign timePrice = goods.goodsSingleTimePriceList[0] />
                    	</#if>
                    	</#if>
                    </#if>
                    <#if timePrice!=null && timePrice.auditPrice!=null>
                    <#assign additionTypeKey = 'additionType_'+product.productId />
                    <tr combType="${combOptionType[additionTypeKey]}" class="table_nav" adult="" child="" suppGoodsId="<#if goods!=null>${goods.suppGoodsId}</#if>">
                        <td class="text_left">${product.productName}</td>
                        <td class="text_left"><#if timePrice!=null&&timePrice.auditMarketPrice!=null>${timePrice.auditMarketPrice/100}元/成人<#else>无成人价</#if> <#if timePrice!=null&&timePrice.childMarketPrice!=null>${timePrice.childMarketPrice/100}元/儿童<#else>无儿童价</#if></td>
                        <td class="text_left" adult="<#if timePrice!=null &&timePrice.auditPrice!=null>${timePrice.auditPrice/100}</#if>" child="<#if timePrice!=null&&timePrice.childPrice!=null>${timePrice.childPrice/100}</#if>" ><#if timePrice!=null&&timePrice.auditPrice!=null>${timePrice.auditPrice/100}元/成人<#else>无成人价</#if> <#if timePrice!=null&&timePrice.childPrice!=null>${timePrice.childPrice/100}元/儿童<#else>无儿童价</#if></td>
                        <td>${product.propValue['pricing_type'][0].name}</td>
                        <td>${specDate?string("yyyy-MM-dd")}</td>
						<td></td>
                        <td><div class="operate" style="text-align:center;"><a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" name="xzylfjxBtn">选择</a></div></td>
                    </tr>
                    </#if>
                    </#list>
                    
                </tbody>
            </table>
        </div>
        </#if>
        
        <#--> 保险 </#-->
        <#if suppGoodsSaleReList?? && suppGoodsSaleReList?size &gt; 0 >
	        <div class="p_box">
	            <table class="p_table table_left bxTable" combType="OPTION">
	                <thead> 
	                    <tr class="noborder">
	                        <th colspan="4" style=" text-align:left;">保险</th>
	                    </tr>
	                </thead>
	                <tbody>
				      <#list suppGoodsSaleReList as suppGoodsSaleRe>
				        <#if suppGoodsSaleRe.insSuppGoodsList??>
					       	<#list suppGoodsSaleRe.insSuppGoodsList as sg>
				                <tr price="${sg.suppGoodsBaseTimePrice.price/100}" 
				                	goodsId="${sg.suppGoodsId}"
									adultNumber="0"
									childNumber="0"
									useFlag="N">
				                    <td width="35%">
				                    	<a href="javascript:void(0);">${sg.prodProduct.productName}(${sg.prodProductBranch.branchName}-${sg.goodsName})</a>
				                    </td>
				                    <td width="10%" class="orange">
				                    	单价：￥${sg.suppGoodsBaseTimePrice.priceYuanStr}
				                    </td>
									<td width="10%">
					            		<select class="baoxianclassid btn_cc1" name="baoxianmz" useFlag="N">
											<#list 0..0 as num>
												<option value="${num}">${num}</option>
											</#list>
										</select>
									</td>
				                    <td width="10%" class="orange">总价：￥--</td>
				               	</tr>
				              </#list>
				           </#if>
				     	</#list>
	                </tbody>
	            </table>
	        </div>
        </#if>        
        
        <div class="p_box">
        	<div id="touristError"></div>
        	<form id="touristForm">
            <table class="p_table table_center touristInfo pop_table">
                <thead> 
                    <tr class="noborder">
                        <th colspan="15" style=" text-align:left;">游客信息</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
					<td colspan="15" style=" text-align:left;"><label class="checkbox mr10 fleft";>常用游客：</label>
					<input type="hidden" name="travellerDelayFlag"  value="${isTravellerDelay!'N'}"/>
					<input type="hidden" name="saveAllFlag"  value="Y"/>	
					<#list personList as person > 
					<label class="checkbox mr10 fleft">
					<input type="checkbox" class="cyyk" style="margin-top:4px;" value="${person.receiverId}"/>${person.fullName}
					<input type="hidden" id="fullName${person.receiverId}" value="${person.fullName}"/>
					<input type="hidden" id="firstName${person.receiverId}" value="${person.firstName}"/>
					<input type="hidden" id="lastName${person.receiverId}" value="${person.lastName}"/>
					<input type="hidden" id="idType${person.receiverId}" value="${person.idType}"/>
					<input type="hidden" id="idNo${person.receiverId}" value="${person.idNo}"/>
					<input type="hidden" id="expDate${person.receiverId}" value="<#if person.expDate?exists>${person.expDate?string('yyyy-MM-dd')}</#if>"/>  
					<input type="hidden" id="issued${person.receiverId}" value="${person.issued}"/>
					<input type="hidden" id="birthday${person.receiverId}" value="${person.birthday}"/>
					<input type="hidden" id="peopleType${person.receiverId}" value="${person.peopleType}"/>
					<input type="hidden" id="gender${person.receiverId}" value="${person.gender}"/>
					<input type="hidden" id="mobile${person.receiverId}" value="${person.mobile}"/>
					<input type="hidden" id="birthPlace${person.receiverId}" value="${person.birthPlace}"/>
					<input type="hidden" id="issueDate${person.receiverId}" value="<#if person.issueDate?exists>${person.issueDate?string('yyyy-MM-dd')}</#if>"/>
					<input type="hidden" id="receiverId${person.receiverId}" value="${person.receiverId}"/>
					</label>
					</#list>
					</td>
                    </tr>
                    <tr style="margin-top:100px">
                        <td>联系人</td>
                        <td>入住房间</td>
                        <td>中文姓名</td>
                        <td>英文姓</td>
                        <td>英文名</td>
                        <td>性别</td>
                        <td>出生地</td>                        
                        <td>出生日期</td>
                        <td>人群</td>
                        <td>证件类型</td>
						<td>证件号码</td>
                        <td>签发地</td>
                        <td>签发日期</td>
                        <td>有效日期</td>
						<td>手机号</td>
                    </tr>
                    <#assign idIndex22 = 0 />
                    <#if shipProdBranchList?? && shipProdBranchList?size &gt; 0>
                    <#list shipProdBranchList as pb>
                    <#assign goods = null />
                	<#assign timePrice = null />
                    <#if pb.goodsList ?? && pb.goodsList?size &gt; 0 >
                    	<#assign goods = pb.goodsList[0] />
                    	<#if goods?? && goods.goodsMultiTimePriceList?size &gt; 0>
                    		<#assign timePrice = goods.goodsMultiTimePriceList[0] />
                    	</#if>
                    </#if>
                    <#if timePrice!=null && pb.minPriceOfGoodsList!=null && pb.minPriceOfGoodsList &gt; 0 >
	                    <tr id="<#if pb??>${pb.productBranchId}</#if>pbid"></tr>
                    </#if>
                    <#assign idIndex22 = idIndex22+1 />
                    </#list>
                    </#if>                   
                </tbody>
            </table>
            </form>
        </div>
        <#if isTravellerDelay == "Y">
        <div class="p_box">
        	<form id="orderTravellerConfirmForm">
            <table class="p_table touristInfo pop_table">
                <thead> 
                    <tr class="noborder">
                        <th colspan="15" style=" text-align:left;">游玩人确认</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td text-align="left" style="width:35%;border-right: #fff 0px solid;">
                        	<i class="red">*</i>
                        	请确认出游人是否包含70周岁（含）以上老人同行？
                       	</td>
                       	<td  style="border-left: #fff 0px solid;">
	                       	<input type="radio" style="vertical-align:middle;" name="orderTravellerConfirm.containOldMan" value="Y" />有
	                       	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                       	<input type="radio" style="vertical-align:middle;" name="orderTravellerConfirm.containOldMan" value="N" checked="checked"/>没有
                       	</td>
                    </tr>
                    <tr>
                        <td text-align="left" style="width:35%;border-right: #fff 0px solid;">
                        	<i class="red">*</i>
                        	请确认同行人中有不满6个月的婴儿？
                       	</td>
                       	<td  style="border-left: #fff 0px solid;">
	                       	<input type="radio" style="vertical-align:middle;" name="orderTravellerConfirm.containBaby" value="Y"/>有
	                       	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                       	<input type="radio" style="vertical-align:middle;" name="orderTravellerConfirm.containBaby" value="N" checked="checked"/>没有
                       	</td>
                    </tr> 
                     <tr>
                        <td text-align="left" style="width:35%;border-right: #fff 0px solid;">
                        	<i class="red">*</i>
                        	请确认同行中是否有孕妇？
                       	</td>
                       	<td  style="border-left: #fff 0px solid;">
	                       	<input type="radio" style="vertical-align:middle;" name="orderTravellerConfirm.containPregnantWomen" value="Y"/>有
	                       	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                       	<input type="radio" style="vertical-align:middle;" name="orderTravellerConfirm.containPregnantWomen" value="N"  checked="checked"/>没有
                       	</td>
                    </tr>
                </tbody>
            </table>
            </form>
        </div>
        </#if>
        
        <div class="p_box">
        	<form id="contractForm">
            <table class="p_table table_center">
                <thead> 
                    <tr class="noborder">
                        <th colspan="2" style=" text-align:left;">联系人信息</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;"><i class="red">*</i>联系人：</label>
                            <input type="text" class="w8 fleft" id="contact-fullName" name="contact.fullName" required maxlength=10/>
                        </td>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;"><i class="red">*</i>联系手机：</label>
                            <input type="text" class="w8 fleft" id="contact-mobile" name="contact.mobile" required=true number=true/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;">传真号码：</label>
                            <input type="text" class="w4 fleft" errorEle="fax" placeholder="区号" name="fax1" id="fax1" number=true />
                            <input type="text" class="w8 fleft" errorEle="fax" placeholder="传真号码" name="fax2" id="fax2" number=true />
                            <input type="text" class="w4 fleft" errorEle="fax" placeholder="分机号" name="fax3" id="fax3" number=true />
                            <input type="hidden" class="w4 fleft" name="contact.fax"/>
                            <div id="faxError" style="display:inline"></div>
                        </td>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;">固定电话：</label>
                            <input type="text" class="w4 fleft" errorEle="phone" placeholder="区号" name="phone1" id="phone1" number=true />
                            <input type="text" class="w8 fleft" errorEle="phone" placeholder="电话号码" name="phone2" id="phone2" number=true />
                            <input type="text" class="w4 fleft" errorEle="phone" placeholder="分机号" name="phone3" id="phone3" number=true />
                            <input type="hidden" class="w4 fleft" name="contact.phone"/>
                            <div id="phoneError" style="display:inline"></div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;"><i class="red">*</i>邮件地址：</label>
                            <input type="text" class="w8 fleft" placeholder="a@b.c" name="contact.email" required email=true/>
                        </td>
                        <td>
                        	
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;">紧急联系人：</label>
                            <input type="text" class="w8 fleft" name="emergencyPerson.fullName"  maxlength=10/>
							<label class="checkbox mr10 fleft w10" style=" text-align:right;">紧急联系电话：</label>
                            <input type="text" class="w8 fleft" name="emergencyPerson.mobile" />
                        </td>
                    </tr>
                </tbody>
            </table>
            </form>
        </div>

        <form id="couponForm">
        <div class="p_box">
            <table class="p_table table_center">
                <thead> 
                    <tr class="noborder">
                        <th style=" text-align:left;">价格信息</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>
                        	
                        		<input type="hidden" id="couponAmount" value="0"/>
	                        	<input type="hidden" id="couponChecked" name="couponList[0].checked" value="false"/>
	                        	<label class="ml10 fleft" style=" text-align:right;">优惠券：此类型订单的优惠券使用规则。如每张订单可使用一张邮轮类优惠券。优惠券代码：</label>
	                            <input type="text" id="couponCode" name="couponList[0].code" class="w8 fleft"/>
	                            <a class="btn w8 fleft" id="couponVerify"  style="margin:0;padding:2px 0;">验证</a>
	                            <span id="couponInfoMsg" style="color:red;"></span>
                           
                        </td>
                    </tr>
                    <tr>
                        <td>
                        <div id="promotionDiv" style="display:none">
                        	<label class="ml10 fleft" style=" text-align:left;">促销活动：</label>
                        	<div style='text-align:left;' id="promotionDetail"></div>
                        </div>
                        	<input type="hidden" id="promotionPrice" value="0">    
                            <label class="ml10 fleft mt10" style=" text-align:left;display:block;width:100%;"><i class="red"><b id="totalPrice"></b></i></label>
                        </td>
                    </tr>
                    <tr>
                    	<td style="text-align:left;color:red;">
                    		舱房计价流程： <br/>
								A、step1：计算房间数：房间数=总入住人数/房间最多可入住人数； <br/> 
								B、step2：分配床位： 将入住人根据先分配成人、再分配儿童的规则，先所有房间的第一个床位、再分配第二个床位，以此类推；
									确保每间房，至少有1个成人入住；即成人数一定大于或等于房间数；  <br/>
								C、step3：判断第三个、第四个及之后的床位，是成人入住还是儿童入住；若是成人，判定取第3、4人价格，若是儿童，取第3、4人儿童价格； <br/> 
								D、step4：确保每间房间的第1个床位和第2个床位的费用已收齐；即1个人住了2人间、或者3人间，其总价为第1、2人价格*2； <br/> 
								E、step5：根据上述规则，计算总价； 
									总价：第1、2人价格*2（入住第1、2床位的最少数量）*总房间数+第3、4人价格*入住第3、4床位的成人数 +第3、4人儿童价格*入住第3、4床位的儿童数
                    	</td>
                    </tr>
                </tbody>
            </table>
        </div>
         </form>
        <div class="p_box">
        	<table class="p_table table_center">
                <thead> 
                    <tr class="noborder">
                        <th style=" text-align:left;">特殊要求</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
						<td style=" text-align:left;">
                        	<span class="boxBox"><label class="checkbox mr10 fleft tsyq" data="房间相近"><input type="checkbox" style="margin-top:4px;*margin-top:0;"/>房间相近</label><label class="checkbox mr10 fleft tsyq" data="房间相连"><input type="checkbox" style="margin-top:4px;*margin-top:0;"/>房间相连</label><label class="checkbox mr10 fleft tsyq" data="残疾房"><input type="checkbox" style="margin-top:4px;*margin-top:0;"/>残疾房</label></span>
                            <form id="remarkForm"><textarea name="remark" class="mt10 w36" maxlength=200></textarea></form>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

       <div class="p_box">
        	<form id="isTestOrderForm">
	            <table class="p_table table_center" style="width:40%">
	                <thead> 
	                    <tr class="noborder">
	                        <th colspan="2" style=" text-align:left;">订单类型</th>
	                    </tr>
	                </thead>
	                <tbody>
	                    <tr>
	                    	<td>
	                    		<label class="checkbox mr10 fleft " style=" text-align:right;"><i class="red">*</i>测试订单：</label>
	                    	</td>
	                    	<td>
	                		 	<input type='radio' value='Y' name='isTestOrder'>是 </input>
								<input type='radio' value='N' name='isTestOrder' checked='checked'> 否</input> 
							</td>
	                    </tr>
	                 </tbody>
	            </table>
            </form>
                        
            <div class="operate mt20" style="text-align:center"><a class="syzbq">上一步</a><a class="btn btn_cc1" id="submitOrder">核对订单</a></div>
      </div>
      
        <script type="text/javascript" src="http://pic.lvmama.com/js/youlun/jquery.birthcalendar.js"></script>
        <script type="text/javascript" src="/vst_order/js/order-util.js"></script>
  <script>
  	var isSubmit = false;
  	//将没有子项的table 隐藏
  	 $(".qzTable,.ggTable,.fjTable").each(function(){
  	 	if($(this).find("tr").size()==2){
  	 		$(this).hide();
  	 	}
  	 });
  	 var submitOrderDialog;
  	 //初始化按钮状态
  	 
  
  	 function init(combType,name){
  	 	 //必选
	  	  if("REQUIR"==combType){
	   	  	$("a[name="+name+"]").removeClass("btn_cc1").text("已选择");
	   	  }else if("ONE"==combType){
	   	    $("a[name="+name+"]").eq(0).removeClass("btn_cc1").text("已选择");
	   	  }else if("MORE_ONE"==combType){
	   	   $("a[name="+name+"]").eq(0).removeClass("btn_cc1").text("取消选择");
	   	  }else if("NONE"==combType){
	   	   
	  	  }
  	 }
     
      function initSupp(object){
      
      	var combType = object.parents("tr").attr("combType");
	    var name = "xzylfjxBtn";
  	 	 //必选
	  	  if("REQUIR"==combType){
	   	  	object.removeClass("btn_cc1").text("已选择");
	   	  }
  	 }
     
     //初始化舱房按钮
     function initShipBtn(){
     	var combType = $("a[name=xzcfBtn]").parents("table").attr("combType");
     	var name = "xzcfBtn";
     	//init(combType,name);
     }
     
     //初始化签证按钮
     function initVisaBtn(){
     	var combType = $("a[name=xzqzBtn]").parents("table").attr("combType");
     	var name = "xzqzBtn";
     	init(combType,name);
     }
    
    //初始化岸上观光按钮
    function initSightBtn(){
     	var combType = $("a[name=xzasggBtn]").parents("table").attr("combType");
     	var name = "xzasggBtn";
     	init(combType,name);
     }
    
	     //初始化附加属性按钮
	    function initAdditionSuppBtn(){
		    $("a[name=xzylfjxBtn]").each(function(){
		     		initSupp($(this));
		  		});
		   }
	  	
	  	 function initAdditionBtn(){
			var combType = $(this).parents("table").attr("combType");
	     		var name = "xzylfjxBtn";
	     		init(combType,name);
	     }
	     
     
     
     initShipBtn();
     initVisaBtn();
     initSightBtn();
      <#if prodProduct && prodProduct.packageType=='SUPPLIER'>
      initAdditionSuppBtn();
    	 <#else>
     initAdditionBtn();
  		</#if>
  	//设置特殊需求
  	$(".tsyq").click(function(){
  		if($(this).find("input").is(":checked")){
  			var text = $(this).attr("data");
	  		var oldText = $("#remarkForm").find("textarea").val();
	  		if(oldText.indexOf(text)==-1){
	  			$("#remarkForm").find("textarea").val(oldText+" "+text);
	  		}
  		}
  	});  
  	//舱房被选中后改变房间信息
  	
  	$("select[name=room_count]").bind("change",function(){
  		var $tr = $(this).parents("tr");
  		var $btn=$tr.find("a[name=xzcfBtn]");
		$btn.click();
		$btn.click(); 		
  		if(!$btn.hasClass("btn_cc1")){
  			countPromotion();
  		}
  		
  	});
    //设置舱房选中
  	$("a[name=xzcfBtn]").bind("click",function(){
  	
  	 //得到所在行
   		var $tr = $(this).parents("tr");
   		//获得成人数量
   		var adultCount = $tr.find("input[data=adult_count]").val();
   		adultCount = adultCount == '' ? 0 : parseInt(adultCount);
   		//获得儿童数量
   		var childCount = $tr.find("input[data=child_count]").val();
   		childCount = childCount == '' ? 0 : parseInt(childCount);
   		
  		var roomCount= $tr.find("select[name=room_count]").val();
  			roomCount = roomCount == '' ? 0 : parseInt(roomCount);
  
	  	if(roomCount==0 &&(adultCount+childCount)>0){
	  			$.alert("当前成人儿童数无法匹配该房型");
	  			return;
	  		}
	  	if(roomCount==0 &&(adultCount+childCount)==0){
	  			$.alert("无入住成人儿童,无法进行选择操作");
	  			return;
	  		}
		if(roomCount>0 &&(adultCount+childCount)==0){
  			$.alert("请为房间选择入住成人和儿童");
  			return;
  		}
  		var combType = $(this).parents("table").attr("combType");
  		setBtnClick($(this),'xzcfBtn',combType);
  		refreshBaoxianCount();
  		//刷签证人数
  		refreshVisaCount();
  		refreshSigthCount();
   		refreshAdditionCount();
   		refreshTouristInfo($(this));
   		//游玩人后置设置默认属性
   		if(isTravellerDelay()){
   			var productBranchId =  $(this).parents("tr").attr("productBranchId")
   			setdTravellerdefaulInfo(false,productBranchId);
   		}
   		//刷新游玩人舱房
   		//refreshCombSelect();
   		//计算促销
   		countPromotion();
   		
  	});
  	
  	
  	//设置签证选中
  	$("a[name=xzqzBtn]").bind("click",function(){
  		var combType = $(this).parents("table").attr("combType");
  		setBtnClick($(this),'xzqzBtn','REQUIRED');
  		
  	});
  	//设置岸上观光
  	$("a[name=xzasggBtn]").bind("click",function(){
  		var combType = $(this).parents("table").attr("combType");
  		setBtnClick($(this),'xzasggBtn',combType);
  	});
  	//设置附加项目
  	$("a[name=xzylfjxBtn]").bind("click",function(){
  		var combType = $(this).parents("tr").attr("combType");
  		setBtnClick($(this),'xzylfjxBtn',combType);
  	});
  
   /**
    *obj 要设置的对象，一般为按钮
    *type 类型，ONE,REQUIRED 等
    *callBack 回调函数，执行完成后可以执行回调函数
   **/
   function setBtnClick($obj,name,type){
   	  //如果是必选
   	  if("REQUIR"==type){
   	  	 $obj.removeClass("btn_cc1").text("已选择");
   	  }else if("ONE"==type){
   	   //任选一项
   	  	$("a[name="+name+"]").addClass("btn_cc1").text("选择");
  		$obj.removeClass("btn_cc1").text("已选择");
   	  }else if("MORE_ONE"==type){
   	    //一项起选
		//判断选中数量是否于1
		var size = 0;
		$("a[name="+name+"]").each(function(){
			if(!$(this).is(".btn_cc1")){
				size++;
			}
		});
   	    if($obj.is(".btn_cc1")){
	   	    //得到所在行
	   		var $tr = $obj.parents("tr");
   	    	 $tr.find("input[data=adult_count]").attr("readOnly",true);
	  		$tr.find("input[data=child_count]").attr("readOnly",true);
  			$obj.removeClass("btn_cc1").text("取消选择");
  		}else {
  			if($obj.attr("data-type")){//仓房不限制至少选择一间
	  				//得到所在行
		   		var $tr = $obj.parents("tr");
		  		$tr.find("input[data=adult_count]").attr("readOnly",false);
		  		$tr.find("input[data=child_count]").attr("readOnly",false);
	  			$obj.addClass("btn_cc1").text("选择");
  			}else{
  				if(size > 1){
  				 $obj.addClass("btn_cc1").text("选择");
  				}
  					
  			}
	  	
  		}
   	  }else if("NONE"==type){
   	   //随便选
   	  	if($obj.is(".btn_cc1")){
  			$obj.removeClass("btn_cc1").text("取消选择");
  		}else {
  			$obj.addClass("btn_cc1").text("选择");
  		}
   	  }
   	  refreshBaoxianCount();
   	  //刷新价格
	  calculateTotalPrice();
   }
  
   //计算舱房的Table
   $("input[data=adult_count],input[data=child_count]").change(function(){
        //得到所在行
   		var $tr = $(this).parents("tr");
   		//获得成人数量
   		var adultCount = $tr.find("input[data=adult_count]").val();
   		adultCount = adultCount == '' ? 0 : parseInt(adultCount);
   		//获得儿童数量
   		var childCount = $tr.find("input[data=child_count]").val();
   		childCount = childCount == '' ? 0 : parseInt(childCount);
   		//获得可入住人数
   		var maxNumber=$(this).attr("maxNumber");
   		maxNumber = maxNumber == '' ? 0 : parseInt(maxNumber);
   		var minNumber=$(this).attr("minNumber");
   		minNumber = minNumber == '' ? 0 : parseInt(minNumber);
   		var occupantNumber = $tr.find("td[name=occupant_number]").attr("data");
   		occupantNumber = occupantNumber == '' ? 1 : parseInt(occupantNumber);

   		//计算儿童最大最小
   		var minChild=0;
   		if(adultCount>0){
	   		var maxChild=(maxNumber-1)*adultCount;
	   		if(childCount<minChild || childCount>maxChild){
	   			$tr.find("input[data=child_count]").val("0");
	   			childCount=0;
	   			$.alert("儿童可输入范围为"+minChild+"~"+maxChild+",儿童人数重置为0");
   			}
   		}else{
   			$.alert("成人数为0时,儿童可选范围为0,儿童人数重置为0");
   			$tr.find("input[data=child_count]").val("0");
   			childCount=0;
   		}
   		
   		var  totlePersonCount=adultCount + childCount;//总人数
   		
   		//计算最多的房间数
   		//var maxRoomCount = adultCount + childCount;
   		var maxRoomCount = parseInt(totlePersonCount/minNumber);
   		if(maxRoomCount>adultCount){
   			maxRoomCount=adultCount;
   		}
   		//计算最少的房间数
   		var minRoomCount = Math.ceil(totlePersonCount/maxNumber);
   		if(totlePersonCount/minRoomCount<minNumber)
   		{
   			maxRoomCount=0;
   			minRoomCount=0;
   			$.alert("当前人数不满足销售条件，无法匹配房间");
   		}
   		//判断最少间数和库存关系
   		var stock = $tr.attr("stock");
   		stock = parseInt(stock);
   		if(stock!=-1){
   			if(maxRoomCount > stock){
   				maxRoomCount = stock;
   			}
   			if(minRoomCount>stock){
   				$.alert("该商品只有: "+stock+" 间,现成人:"+adultCount+"儿童:"+childCount+"至少需要"+minRoomCount+"间");
   				minRoomCount=0;
   				maxRoomCount=0;
   			}
   			
   		}
   		
   		//刷新select
   		var $select = $tr.find("select[name=room_count]");
   		$select.empty();
   		for(var i=minRoomCount;i<=maxRoomCount;i++){
   			if(i==minRoomCount){
   				$select.append('<option value='+i+' selected="selected">'+i+'</option>');
   			}else {
   				$select.append('<option value='+i+'>'+i+'</option>');
   			}
   		}
  
   });
   
   //使用自备签
   $(".syzbq").click(function(){
   		//得到所在TR
   		var $tr = $(this).parents("tr");
   		//根据成人数和儿童数添加select
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		//创建选择成人和儿童的选择框
   		//成人
		var adultSelect = $("<select>").addClass("w10").appendTo($tr.find("td").eq(5).empty()).before("<span>成人:</span>");
   		if(adult > 0){
	   		for(var i = 0 ; i <= adult ; i++){
	   			if(adult == i){
	   				adultSelect.append("<option value="+i+" selected=selected>"+i+"</option>");
	   			}else {
	   				adultSelect.append("<option value="+i+">"+i+"</option>");
	   			}
	   		}
   		}
   		//儿童
   		var childSelect = $("<select>").addClass("w10").appendTo($tr.find("td").eq(5)).before("<span>儿童:</span>");
   		if(child > 0){
	   		for(var i = 0 ; i <= child ; i++){
	   			if(child == i){
	   				childSelect.append("<option value="+i+" selected=selected>"+i+"</option>");
	   			}else {
	   				childSelect.append("<option value="+i+">"+i+"</option>");
	   			}
	   			
	   		}
   		}
   		//按钮变为确认
   		$tr.find("a[name=xzqzBtn]").replaceWith('<a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" name="syzbq">确认</a>');
   		$tr.find("a[name=syzbq]").click(function(){
   				var $select = $(this).parents("tr").find("select");
   				if($select.size()!=0){
	   				var adult = $select.eq(0).val() == null ? 0 : $select.eq(0).val();
	   				var child = $select.eq(1).val() == null ? 0 : $select.eq(1).val();
	   				$tr.find("td").eq(5).empty().text("成人:"+ adult + "人      儿童:"+ child +"人");
	   				$tr.attr("adult",adult);
	   				$tr.attr("child",child);
	   				$tr.attr("useself","T");
   				}
   				$(this).replaceWith('<a class="btn  w8" style="margin:0;padding:5px 0;" name="xzqzBtn">已选择</a>');
   				//刷价格
   				calculateTotalPrice();
   		});
   });
   
   //刷签证的人数
   function refreshVisaCount(){
   		//获得已选的出游人数
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		$(".qzTable tr").each(function(i){
   			if(i>1){
	   			$(this).attr("adult",adult);
	   			$(this).attr("child",child);
	   			//重置使用自有签证
	   			$(this).attr("useself","");
	   			//
	   			$(this).find("td").eq(5).text("成人:"+ adult + "人      儿童:"+ child +"人");
   			}
   		})
   }
   
   //刷岸上观光的人数
   function refreshSigthCount(){
   		//获得已选的出游人数
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		$(".ggTable tr").each(function(i){
   			if(i>1){
	   			$(this).attr("adult",adult);
	   			$(this).attr("child",child);
	   			//
	   			$(this).find("td").eq(5).text("成人:"+ adult + "人      儿童:"+ child +"人");
   			}
   		})
   }
   
   //刷邮轮附加项的人数
   function refreshAdditionCount(){
   		//获得已选的出游人数
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		$(".fjTable tr").each(function(i){
   			if(i>1){
	   			$(this).attr("adult",adult);
	   			$(this).attr("child",child);
	   			//
	   			$(this).find("td").eq(5).text("成人:"+ adult + "人      儿童:"+ child +"人");
   			}
   		})
   }
   
   //刷新保险选择的人数
   function refreshBaoxianCount(){
   		//获得已选的出游人数
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		$(".bxTable tr").each(function(i){
   			if(i>0){
		        var optionStr="<select name=\"baoxianmz\" useFlag=\"N\" class=\"baoxianclassid btn_cc1\">";
		        optionStr += "<option value=\""+0+"\">"+0+"</option>";
		        if(adult+child > 0){
					optionStr += "<option value=\""+(adult+child)+"\">"+(adult+child)+"</option>";
		        }
			    optionStr += "</select>";
			    $(this).find("td").eq(2).html("");
			    $(this).find("td").eq(2).html(optionStr);
			    $(this).find("td").eq(3).text("");
			    $(this).find("td").eq(3).text("总价：￥--");
			    $(this).attr("useFlag",'N'); 
   			}
   		});
   }   

   
   //获得已选的成人数
   function getSelectAdult(){
   		var adult = 0;
   		$(".cfTable tr").each(function(i){
   			if(i>1){
   				if(!$(this).find("td").eq(10).find("a").is(".btn_cc1")){
	   				var tempAdult =  $(this).find("input[data=adult_count]").val();
	   				adult = adult + (tempAdult == '' ? 0 : parseInt(tempAdult));
   				}
   			}
   		});
   		return adult;
   }
   
   //获得已选的儿童数
   function getSelectChild(){
   		var child = 0;
   		$(".cfTable tr").each(function(i){
   			if(!$(this).find("td").eq(10).find("a").is(".btn_cc1") && i>1){
   				var tempChild =  $(this).find("input[data=child_count]").val();
   				child = child + (tempChild == '' ? 0 : parseInt(tempChild));
   			}
   		});
   		return child;
   }
   
   
   //刷新游客信息
   function refreshTouristInfo($obj){
   		reSetTouristIndex();
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		var counts = adult + child;
   		var existsTiCounts = $(".touristInfo").find(".ti").size();
   		//如果是增加
   		if(counts > existsTiCounts){
   			existsTiCounts = parseInt(existsTiCounts);
   			//当前选择舱房的房间数
			var $tr = $obj.parents("tr");
			var productBranchId = $tr.attr("productBranchId");
			//商品id
			var goodsId = $tr.attr("suppGoodsId");
			//舱房名字
			var productBranchName = $tr.attr("productBranchName");
   			//当前选择舱房的房间数
   			var adultCount = $tr.find("input[data=adult_count]").val();
   			adultCount = adultCount == '' ? 0 : parseInt(adultCount);
   			var childCount = $tr.find("input[data=child_count]").val();
   			childCount = childCount == '' ? 0 : parseInt(childCount);
   			var personCount = childCount + adultCount;
   			//当前选择舱房的总人数
  			var roomCount= $tr.find("select[name=room_count]").val();
  			var fangjianCount = (roomCount == '' ? 0 : parseInt(roomCount));
  			//舱房房间分配
  			var roomNum = 1;
  			var persons = parseInt(personCount);
  			var rooms = parseInt(fangjianCount);
			var fangjian = parseFloat(fangjianCount);
			
			//用于存放每个房间的人数的信息
			var temp = new Array();
			while(persons > 0 && rooms > 0){
				var person = Math.ceil(persons/fangjian);
				//alert(person);
				//alert(roomNum);
				for(var i=0; i < person; i++){
					var info = createTouristInfo(existsTiCounts+i, roomNum, productBranchName, person, i, productBranchId, goodsId);
					//插入位置productBranchId+pbid之后
					//$("#" + productBranchId + "pbid").before("fangjianCount-" + fangjianCount + "personCount-" + personCount + "roomNum-" + roomNum + "productBranchName-" + productBranchName);
					$("#" + productBranchId + "pbid").before(info);
					
					//每种舱房设置选择的成人儿童放置到标签中
					$("#" + productBranchId + "pbid").attr("adult",adultCount);
					$("#" + productBranchId + "pbid").attr("child",childCount);
					$("#" + productBranchId + "pbid").attr("rooms",fangjianCount);
					
					//对应房间总人数
					temp[roomNum-1] = person;
					
				}
				
				//缓存每种舱房的房间和对应人数信息
				dataMap[productBranchId+""] =  temp;
				
				existsTiCounts = existsTiCounts + person;
				persons = persons - person;
				rooms--;
				fangjian = parseFloat(rooms);
				roomNum++;
			}
   		}else if(counts < existsTiCounts){
   			//如果是减少，则从最后开始删除指定行数
   			//当前要取消的舱房房间
			var $tr = $obj.parents("tr");
			var productBranchId = $tr.attr("productBranchId");
			var classFlag = "branchcc_" +　productBranchId;
			//alert(classFlag);
			$(".touristInfo").find("." + classFlag).each(function(){
				$(this).remove();
			});
   		}
   		
   		//为游客信息绑定点击事件
   		$("input[name=person]").click(function(){
   			var $tr = $(this).parents("tr");
   			//解除其它文本框的绑定事件
   			$("input[name=person]").each(function(){
   				var $tr = $(this).parents("tr");
	   			//是否规格-房间-第一个
	   			var xfjFirst0 = $(this).attr("xfjFirst");  
	   			if(xfjFirst0 == "Y"){
	   				$tr.find("td").eq(2).find("input").unbind("change");
	   				$tr.find("td").eq(14).find("input").unbind("change");	   				
	   			}else{
	   				$tr.find("td").eq(1).find("input").unbind("change");
	   				$tr.find("td").eq(13).find("input").unbind("change");
	   			}
   			});   			
   			//是否规格-房间-第一个
   			var xfjFirst = $(this).attr("xfjFirst");
   			if(xfjFirst == "Y"){
   				var fullName = $tr.find("td").eq(2).find("input").val();
   				var mobile =  $tr.find("td").eq(14).find("input").val();
	   			$("#contact-fullName").val(fullName);
   				$("#contact-mobile").val(mobile);   				
	   			//绑定change事件
	   			$tr.find("td").eq(2).find("input").bind("change",function(){
	   				$("#contact-fullName").val($(this).val());
	   			});
	   			$tr.find("td").eq(14).find("input").bind("change",function(){
	   				$("#contact-mobile").val($(this).val());
	   			});   				
   			}else{
	   			var fullName = $tr.find("td").eq(1).find("input").val();
	   			var mobile =  $tr.find("td").eq(13).find("input").val();
	   			$("#contact-fullName").val(fullName);
   				$("#contact-mobile").val(mobile);
		   		//绑定change事件
		   		$tr.find("td").eq(1).find("input").bind("change",function(){
		   			$("#contact-fullName").val($(this).val());
		   		});
		   		$tr.find("td").eq(13).find("input").bind("change",function(){
		   			$("#contact-mobile").val($(this).val());
		   		});    			
   			}
   		});
   		
   		var nowDate = '';
   		function getNowDate(){
   			var dt = new Date();
   			var month = dt.getMonth()+1;
			var day = dt.getDate();
			var year = dt.getFullYear();
			if(month<10)
			  month = "0"+month;
			if(day<10)
			  day = "0"+day;  
			nowDate =  year+"-"+month+"-"+day;
   		}
   		getNowDate();
   		$('.birthdayJs').simpleDatepicker({ 
			chosendate: '1985-02-03', 
			startdate: '1950-01-01', 
			enddate: nowDate, 
			y:40 
		}); 
   }
   
   //设置常用游客
   function setCutourists(){
	$(".cyyk").click(function(){
		//如果是选中
		var receiverId = $(this).val();
		if($(this).is(":checked")){
			if(checkTouristInfo()){
				setTouristInfo(receiverId)
			}else {
				$.alert("没有可以空余的可以设置的联系人");
				$(this).removeAttr("checked");
			}
		}else {
			reSetTouristInfo(receiverId);
		}
	});
   }
   setCutourists();
   
   function checkTouristInfo(){
   		var flag = false;
   		$(".touristInfo").find(".ti").each(function(){
   				if($(this).attr("data")==""){
   					flag = true;
   				}
   		});
   		return flag;
   }
   
   //设置游玩人
   function setTouristInfo(receiverId){
	 $(".touristInfo").find(".ti").each(function(){
			var $tr = $(this);
			if($tr.attr("data")==""){
				$tr.attr("data",receiverId);
				var $td = $tr.find("td").eq(0).find("input[name=person]");
		   		//是否规格-房间-第一个
		   		var xfjFirst = $td.attr("xfjFirst");  
	   			if(xfjFirst == "Y"){
					$tr.find("td").eq(2).find("input").val($("#fullName"+receiverId).val());
					$tr.find("td").eq(3).find("input").val($("#lastName"+receiverId).val());
					$tr.find("td").eq(4).find("input").val($("#firstName"+receiverId).val());
					var gender = $("#gender"+receiverId).val()
					$tr.find("td").eq(5).find("input").find("#option[value="+gender+"]").attr("selected","selected");
					$tr.find("td").eq(6).find("input").val($("#birthPlace"+receiverId).val());
					$tr.find("td").eq(7).find("input").val($("#birthday"+receiverId).val());
					var peopleType = $("#peopleType"+receiverId).val();
					$tr.find("td").eq(8).find("select").find("#option[value="+peopleType+"]").attr("selected","selected");
					var idType = $("#idType"+receiverId).val();
					$tr.find("td").eq(9).find("select").find("option[value="+idType+"]").attr("selected","selected");
					$tr.find("td").eq(10).find("input").val($("#idNo"+receiverId).val());
					$tr.find("td").eq(11).find("input").val($("#issued"+receiverId).val());
					$tr.find("td").eq(12).find("input").val($("#issueDate"+receiverId).val());
					$tr.find("td").eq(13).find("input").val($("#expDate"+receiverId).val());
					$tr.find("td").eq(14).find("input").val($("#mobile"+receiverId).val());
					$tr.find("td").eq(15).find("input").val($("#receiverId"+receiverId).val()); 				
	   			}else{
					$tr.find("td").eq(1).find("input").val($("#fullName"+receiverId).val());
					$tr.find("td").eq(2).find("input").val($("#lastName"+receiverId).val());
					$tr.find("td").eq(3).find("input").val($("#firstName"+receiverId).val());
					var gender = $("#gender"+receiverId).val()
					$tr.find("td").eq(4).find("input").find("#option[value="+gender+"]").attr("selected","selected");
					$tr.find("td").eq(5).find("input").val($("#birthPlace"+receiverId).val());
					$tr.find("td").eq(6).find("input").val($("#birthday"+receiverId).val());
					var peopleType = $("#peopleType"+receiverId).val();
					$tr.find("td").eq(7).find("select").find("#option[value="+peopleType+"]").attr("selected","selected");
					var idType = $("#idType"+receiverId).val();
					$tr.find("td").eq(8).find("select").find("option[value="+idType+"]").attr("selected","selected");
					$tr.find("td").eq(9).find("input").val($("#idNo"+receiverId).val());
					$tr.find("td").eq(10).find("input").val($("#issued"+receiverId).val());
					$tr.find("td").eq(11).find("input").val($("#issueDate"+receiverId).val());
					$tr.find("td").eq(12).find("input").val($("#expDate"+receiverId).val());
					$tr.find("td").eq(13).find("input").val($("#mobile"+receiverId).val());
					$tr.find("td").eq(14).find("input").val($("#receiverId"+receiverId).val());
	   			}	
				return false;
			}
		});
   }
   
   //取消游玩人
   function reSetTouristInfo(receiverId){
	 $(".touristInfo").find(".ti").each(function(){
			var $tr = $(this);
			if($tr.attr("data")==receiverId){
				$tr.attr("data","");
				var $td = $tr.find("td").eq(0).find("input[name=person]");
		   		//是否规格-房间-第一个
		   		var xfjFirst = $td.attr("xfjFirst");			
				if(xfjFirst == "Y"){
					$tr.find("td").eq(2).find("input").val("");
					$tr.find("td").eq(3).find("input").val("");
					$tr.find("td").eq(4).find("input").val("");
					$tr.find("td").eq(6).find("input").val("");
					$tr.find("td").eq(7).find("input").val("");
					$tr.find("td").eq(10).find("input").val("");
					$tr.find("td").eq(11).find("input").val("");
					$tr.find("td").eq(12).find("input").val("");
					$tr.find("td").eq(13).find("input").val("");
					$tr.find("td").eq(14).find("input").val("");
					$tr.find("td").eq(15).find("input").val("");				
				}else {
					$tr.find("td").eq(1).find("input").val("");
					$tr.find("td").eq(2).find("input").val("");
					$tr.find("td").eq(3).find("input").val("");
					$tr.find("td").eq(5).find("input").val("");
					$tr.find("td").eq(6).find("input").val("");
					$tr.find("td").eq(9).find("input").val("");
					$tr.find("td").eq(10).find("input").val("");
					$tr.find("td").eq(11).find("input").val("");
					$tr.find("td").eq(12).find("input").val("");
					$tr.find("td").eq(13).find("input").val("");
					$tr.find("td").eq(14).find("input").val("");				
				}

			}
		});
   }
   
   //创建游客信息
   function createTouristInfo(index, roomNum, cangfangName, rowSpan, hbnum, productBranchId, goodsId){
   		var hebingHang = "";
   		var tdCont = "";
   		var classFlag = "branchcc_" +　productBranchId;
   		//某一间的1个
   		var xfjFirst = "N";
   		if(hbnum == 0){
   			hebingHang = 'rowspan="' + rowSpan + '"';
   			tdCont = '<td ' + hebingHang + ' >第'+ roomNum + '间：' + cangfangName + '</td>'
   			xfjFirst = "Y";
   		}
   		
   		
   		var touristInfo = '<tr class="ti ' + classFlag + '" data="" roomNum="'+(roomNum-1)+'" >'+
                        '<td><input type="radio" name="person" xfjFirst="' + xfjFirst + '"/></td>'+
   						tdCont+
                        '<td><input type="text" id='+index+' class="w5" name="travellers['+index+'].fullName" errorEle="tourist" required=true maxlength=10/></td>'+
                        '<td><input type="text" id='+index+' class="w4" name="travellers['+index+'].lastName" errorEle="tourist"  maxlength=10/></td>'+
                        '<td><input type="text" id='+index+' class="w7" name="travellers['+index+'].firstName" errorEle="tourist"  maxlength=20/></td>'+
                        '<td>'+
							'<select class="w4" id='+index+' name="travellers['+index+'].gender" style="padding:0;margin:0;text-align:center">'+
                                '<option value="MAN">男</option>'+
                                '<option value="WOMAN">女</option>'+
                           ' </select>'+
						'</td>'+
						'<td><input type="text" id='+index+' class="w5" errorEle="tourist" name="travellers['+index+'].birthPlace" name_type="birthPlace"  maxlength=10/></td>'+
                         '<td><input type="text" id='+index+' class="w6 birthdayJs" errorEle="tourist" name="travellers['+index+'].birthday" readonly=readonly /></td>'+
                        '<td>'+
                        	'<select class="w5" id='+index+' name="travellers['+index+'].peopleType" style="padding:0;margin:0;text-align:center">'+
                                '<option value="PEOPLE_TYPE_ADULT">成人</option>'+
                                '<option value="PEOPLE_TYPE_CHILD">儿童</option>'+
                           ' </select>'+
                        '</td>'+
                        '<td>'+
                        	'<select class="w6" id='+index+' name="travellers['+index+'].idType" style="padding:0;margin:0;text-align:center">'+
                                '<option value="HUZHAO">护照</option>'+
                                '<option value="ID_CARD">身份证</option>'+
                                '<option value="GANGAO">港澳通行证</option>'+
                                '<option value="TAIBAO">台湾通行证</option>'+
                                '<option value="JUNGUAN">军官证</option>'+
                                '<option value="SHIBING">士兵证 </option>'+
                                '<option value="TAIBAOZHENG">台胞证 </option>'+
                                '<option value="HUIXIANG">回乡证</option>'+
                                '<option value="HUKOUBO">户口薄 </option>'+
                                '<option value="CHUSHENGZHENGMING">出生证明 </option>'+
                           ' </select>'+
                        '</td>'+
						'<td><input type="text" id='+index+' class="w12" errorEle="tourist" name="travellers['+index+'].idNo" name_type="idNo"  maxlength=20/></td>'+
                        '<td><input type="text" id='+index+' class="w5" errorEle="tourist" name="travellers['+index+'].issued" name_type="issued" maxlength=10/></td>'+
                        '<td><input type="text" id='+index+' class="w6 birthdayJs" errorEle="tourist" name="travellers['+index+'].issueDate" name_type="issueDate" readonly=readonly /></td>'+
                       '<td><input type="text" id='+index+' class="w6 birthdayJs" errorEle="tourist" name="travellers['+index+'].expDate" name_type="expDate" readonly=readonly /></td>'+
						'<td><input type="text" id='+index+' class="w8" name="travellers['+index+'].mobile" maxlength=11/></td>'+
                        '<input type="hidden" id='+index+' class="w8" name="cabin" value="' + goodsId + '"/>'+
                        '<input type="hidden" id='+index+' class="w8" name="travellers['+index+'].receiverId"/>'+
                        '<input type="hidden" id='+index+' class="w8" name="travellers['+index+'].roomNo" value="' + roomNum + '"/>'+
                    '</tr>';
        return touristInfo;
   }
   
   //刷游玩人的数组下标
   function reSetTouristIndex(){
   		$(".touristInfo").find(".ti").each(function(index){
    		$(this).find("input,select").each(function(i){
    			if(i>0){
    				var name = $(this).attr("name");
    				var id = $(this).attr("id");
    				name = name.replace(id,index);
    				$(this).attr("name",name);
    			}
    		});
    	});
   }
   
   
   //计算总价格
   function calculateTotalPrice(){
     	//舱房价格
   		var combPrice = calculateCombPrice();
   		var bedPrice=calculatebedPrice();
   		var visaPrice = calculateVisaPrice();
   		var sightPrice = calculateSightPrice();
   		var additionPrice =  calculateAdditionPrice();
   		var baoxianPrice =  calculateBaoxianPrice();
   		var couponPrice = calCouponPrice();
   		var earnest=calearnestPrice();
   		var productPrice=combPrice+visaPrice+sightPrice+additionPrice
   		var promotionPrice =$("#promotionPrice").val();
   		promotionPrice=promotionPrice/100;
   		var	totalPrice=productPrice+baoxianPrice-couponPrice-promotionPrice;
   		if(totalPrice<0){
   			totalPrice=0;
   		}
   		var priceStr ="";
   		if(bedPrice>0){
   			priceStr= "订单总价：产品费用"+(productPrice).toFixed(2)+"元(含床位费:"+bedPrice+"元)+保险"+baoxianPrice.toFixed(2) +"元-优惠券"+couponPrice+"元-促销活动"+promotionPrice+"元="+(totalPrice).toFixed(2)+"元（需交定金："+earnest+"元）";
   		}else{
   			priceStr= "订单总价：产品费用"+(productPrice).toFixed(2)+"元+保险" + baoxianPrice.toFixed(2) +"元-优惠券"+couponPrice+"元-促销活动"+promotionPrice+"元="+(totalPrice).toFixed(2)+"元（需交定金："+earnest+"元）";
   		}
   		
   		$("input[name=orderTotalPrice]").val((totalPrice)*100);
   		$("#totalPrice").text(priceStr);
   }
   
   //计算舱房价格
   function calculateCombPrice(){
   		var totalMoney = 0;
   		$(".cfTable tr").each(function(){
   			 var $tr = $(this);
   			 if($tr.find(".operate").size()==1 && !$tr.find("a").is(".btn_cc1")){
   			 	//第一第二人价格
   			 	var fstPrice = $tr.find("td[name=fstPrice]").attr("data");
   			 	fstPrice = fstPrice == "" ? 0 : parseFloat(fstPrice);
   			    //第三第四人成人价
   			    var secPrice = $tr.find("td[name=secPrice]").attr("data");
   			    secPrice = secPrice == "" ? 0 : parseFloat(secPrice);
   			    //第三第四人儿童价
   			    var childPrice = $tr.find("td[name=childPrice]").attr("data");
   			    childPrice = childPrice == "" ? 0 : parseFloat(childPrice);
   			    //床位费
   			    var gapPrice = $tr.find("td[name=gapPrice]").attr("data");
   			    gapPrice = gapPrice == "" ? 0 : parseFloat(gapPrice);
   			    
   			    //成人数
   			    var adult = $tr.find("input[data=adult_count]").val();
   			    adult = adult == "" ? 0 : parseInt(adult);
   			    //儿童数
   			    var child = $tr.find("input[data=child_count]").val();
   			    child = child == "" ? 0 : parseInt(child);
   			    //房间数
   			    var roomCount = $tr.find("select[name=room_count]").val();
   			    roomCount = roomCount == "" ? 0 : parseInt(roomCount);
   			    // 房间最大入住人数 
   			    var maxPerson = $tr.find("input[data=adult_count]").attr("maxNumber");
   			    maxPerson = maxPerson == "" ? 0 : parseInt(maxPerson);
   			    
   			    var roomCount = $tr.find("select[name=room_count]").val();
   			    roomCount = roomCount == "" ? 0 : parseInt(roomCount);
   			    var totalCount = child + adult;
   			    if(totalCount==0){
   			    	return 0;
   			    }
   			      //如果没有三四人成人价，则三四成人价等同第一第二人成人价
   			    if(secPrice==0){
   			    	secPrice = fstPrice;
   			    }
   			      //如果没有儿童价，则儿童价等同第一第二人成人价
   			    if(childPrice==0){
   			    	childPrice = fstPrice;
   			    }
   			      //如果没有床位费，则儿童价等同第三第四人成人价
   			    if(gapPrice==0){
   			    	gapPrice = fstPrice;
   			    }
   			  
   			   var totalBedPrice = (roomCount*maxPerson - totalCount)*gapPrice;
   			     
			        //正好一个房间最多住二个人
			        
			        if(roomCount * 2 >= totalCount){
						// 总价计算=订购人数*第1、2人成人销售价+（间数*最大入住人数-订购人数）*床位费
			            totalMoney += totalCount*fstPrice + totalBedPrice;
			        }else{
			        	 if((totalCount - roomCount*2) <= child){
			                totalMoney += roomCount*2*fstPrice+totalBedPrice + (totalCount -roomCount*2)*childPrice;
			            }else{
			                totalMoney += roomCount*2*fstPrice+totalBedPrice + child*childPrice +
			                        (totalCount -roomCount*2 - child)*secPrice;
			            }
			        }
   			 }
   		});
   		return totalMoney;
   }
    //计算床位费
   function calculatebedPrice(){
   		 var totalBedPrice  = 0;
   		$(".cfTable tr").each(function(){
   			 var $tr = $(this);
   			 if($tr.find(".operate").size()==1 && !$tr.find("a").is(".btn_cc1")){
   			 	//第一第二人价格
   			 	var fstPrice = $tr.find("td[name=fstPrice]").attr("data");
   			 	fstPrice = fstPrice == "" ? 0 : parseFloat(fstPrice);
   			    //第三第四人成人价
   			    var secPrice = $tr.find("td[name=secPrice]").attr("data");
   			    secPrice = secPrice == "" ? 0 : parseFloat(secPrice);
   			    //第三第四人儿童价
   			    var childPrice = $tr.find("td[name=childPrice]").attr("data");
   			    childPrice = childPrice == "" ? 0 : parseFloat(childPrice);
   			    //床位费
   			    var gapPrice = $tr.find("td[name=gapPrice]").attr("data");
   			    gapPrice = gapPrice == "" ? 0 : parseFloat(gapPrice);
   			    
   			    //成人数
   			    var adult = $tr.find("input[data=adult_count]").val();
   			    adult = adult == "" ? 0 : parseInt(adult);
   			    //儿童数
   			    var child = $tr.find("input[data=child_count]").val();
   			    child = child == "" ? 0 : parseInt(child);
   			    //房间数
   			    var roomCount = $tr.find("select[name=room_count]").val();
   			    roomCount = roomCount == "" ? 0 : parseInt(roomCount);
   			    // 房间最大入住人数 
   			    var maxPerson = $tr.find("input[data=adult_count]").attr("maxNumber");
   			    maxPerson = maxPerson == "" ? 0 : parseInt(maxPerson);
   			    
   			    var roomCount = $tr.find("select[name=room_count]").val();
   			    roomCount = roomCount == "" ? 0 : parseInt(roomCount);
   			    var totalCount = child + adult;
   			    if(totalCount==0){
   			    	return 0;
   			    }
   			    
   			      //如果没有床位费，则儿童价等同第三第四人成人价
   			    if(gapPrice==0){
   			    	gapPrice = fstPrice;
   			    }
   			  totalBedPrice += (roomCount*maxPerson - totalCount)*gapPrice;    
   			 }
   		});
   		return totalBedPrice;
   }
   
   //计算签证价格
   function calculateVisaPrice(){
   		var price = 0;
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		//计算使用自签的价格
   		$(".qzTable tr").each(function(i){
   			if(!$(this).find("td").eq(6).find("a").is(".btn_cc1") && i>1){
   				var useself = $(this).attr("useself");
   				var visaPrice = $(this).find("td").eq(2).attr("price");
   				visaPrice = visaPrice == "" ? 0 : parseFloat(visaPrice);
   				//如果使用自有签证
   				if("T"==useself){
   					var adult_useself = $(this).attr("adult") == "" ? 0 : parseInt($(this).attr("adult"));
   					var child_useself = $(this).attr("child") == "" ? 0 : parseInt($(this).attr("child"));
   					price = price + (adult_useself + child_useself)*visaPrice;
   				}else {
   					price = price + (adult + child)*visaPrice;
   				}
   			}
   		});
   		return price;
   }
   
    //保险份数改变事件
  	$("select[name=baoxianmz]").live("change",function(){
  		var price = $(this).parents("tr").attr("price");
        var quantity = $(this).val();
        var totalPrice = (parseInt(quantity)*price).toFixed(2);
        if(quantity == 0){
	        $(this).parents("tr").find("td").eq(3).text("总价：￥--");
			$(this).attr("useFlag",'N');   
			$(this).parents("tr").attr("useFlag",'N');        
        }else{
         	$(this).parents("tr").find("td").eq(3).text("总价：" + totalPrice);
			$(this).attr("useFlag",'Y');
			$(this).parents("tr").attr("useFlag",'Y');
        } 
   		//计算促销
   		countPromotion();        
        calculateTotalPrice();
  	});
  	
   //计算保险价格
   function calculateBaoxianPrice(){
   		var price = 0;
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		$(".bxTable tr").each(function(i){
   			var useflag = $(this).attr("useFlag");
   			if(useflag == 'Y' && i>0){
		  		var unitPrice = $(this).attr("price");
   				unitPrice = (unitPrice == "") ? 0 : parseFloat(unitPrice);
   				price = price + unitPrice * (adult + child);
   			}
   		});
   		return price;
   }  	   
   //计算岸上观光价格
   function calculateSightPrice(){
   		var price = 0;
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		$(".ggTable tr").each(function(i){
   			if(!$(this).find("td").eq(6).find("a").is(".btn_cc1") && i>1){
	   		    var adultPrice = $(this).find("td").eq(2).attr("adult");
   				adultPrice = adultPrice == "" ? 0 : parseFloat(adultPrice);
   				var childPrice = $(this).find("td").eq(2).attr("child");
   				childPrice = childPrice == "" ? 0 : parseFloat(childPrice);
   				price = price + adultPrice * adult + childPrice * child;
   			}
   		});
   		return price;
   }
   //计算附加项价格
   function calculateAdditionPrice(){
   		var price = 0;
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		$(".fjTable tr").each(function(i){
   			if(!$(this).find("td").eq(6).find("a").is(".btn_cc1") && i>1){
	   		    var adultPrice = $(this).find("td").eq(2).attr("adult");
   				adultPrice = adultPrice == "" ? 0 : parseFloat(adultPrice);
   				var childPrice = $(this).find("td").eq(2).attr("child");
   				childPrice = childPrice == "" ? 0 : parseFloat(childPrice);
   				price = price + adultPrice * adult + childPrice * child;
   			}
   		});
   		return price;
   }
   
   //计算需交定金
   function calearnestPrice(){
   		var price = $("#selectedEarnestHid").val();
   		var adult = getSelectAdult();
   		var child = getSelectChild();
   		if($.trim(price)==""){
   			price=0;
   		}else{
   			price=(price*(adult+child))/100;
   		}
   		
   		return price;
   }
   
   //计算优惠券的金额
   function calCouponPrice(){
   		var price = 0;
   		var code=$("#couponCode").val();
   		var couponChecked=$("#couponChecked").val()
         if($.trim(code)!=""&&couponChecked=="true"){
          if(!setAndsubmitOrder()){
          	return price;
          }
	   		$.ajax(
		        {
		            type: "get",
		            async: false,
		            url: "/vst_order/ord/order/calCoupon.do",
		            data: $("#baseDataForm,#ordForm,#touristForm,#orderTravellerConfirmForm,#contractForm,#remarkForm,#couponForm").serialize(),
		            dataType: "json",
		            success: function (data) {
		            	if(data.success){
		            		if(data.attributes.favorStrategyInfo!=null){
		            			var discountAmount=data.attributes.favorStrategyInfo.discountAmountToYuan;
			            		if(discountAmount!=null||$.trim(discountAmount)!=""){
			            			price=discountAmount;
			            			$("#couponAmount").val(price);
			            		}
		            		}
		            	}else{
		            		$("#couponInfoMsg").html(data.code);
		            	}
		            }
		        }
		    );
		    
         }
         
        if(price==undefined){
         	price=0;
         }
	    return price;
   }
   
   //刷新出游人的舱房
   function refreshCombSelect(){
   	  var cabinSelect = $("select[name=cabin]");
   	  cabinSelect.empty();
   	  $(".cfTable tr").each(function(){
   			 var $tr = $(this);
   			 if($tr.find(".operate").size()==1 && !$tr.find("a").is(".btn_cc1")){
   			 	 var goodsId = $tr.attr("suppGoodsId");
   			 	 var name = $tr.find("td").eq(1).find("a").text();
   			 	 if(goodsId!=""){
   			 	 	cabinSelect.append('<option value='+goodsId+'>'+name+'</option>');
   			 	 }
   			 }
   	  });
   };
   
   //获得选中的产品名称
   function setSelectProductName(){
   		$("table.product").find('tr').each(function(){
   				if($(this).find(".operate").size()==1 && !$(this).find("a").is(".btn_cc1")){
   			 	 var name = $(this).find("td").eq(2).text();
   			 	 $("#combProductName").val(name);
   			 }
   		});
   }
   
   <#--构建商品的Item-->
   function createItem(index,goodsId,quantity,visitTime,mainItem,ownerQuantity,adultQuantity,childQuantity){
		$("#ordForm").append('<input type=hidden name=itemList['+index+'].goodsId value='+goodsId+'>');
	 	 <#--商品数量-->
	 	 $("#ordForm").append('<input type=hidden name=itemList['+index+'].quantity value='+quantity+'>');
	 	  <#--成人数-->
	 	 $("#ordForm").append('<input type=hidden name=itemList['+index+'].adultQuantity value='+adultQuantity+'>');
	 	  <#--儿童数-->
	 	 $("#ordForm").append('<input type=hidden name=itemList['+index+'].childQuantity value='+childQuantity+'>');
	 	 
	 	 <#--日期-->
	 	 $("#ordForm").append('<input type=hidden name=itemList['+index+'].visitTime value='+visitTime+'>');
	 	 <#--是否主订单-->
	 	 $("#ordForm").append('<input type=hidden name=itemList['+index+'].mainItem value='+mainItem+' >');
	 	 <#--是否主订单-->
	 	 $("#ordForm").append('<input type=hidden name=itemList['+index+'].ownerQuantity value='+ownerQuantity+' >');
   }
   
   
   <#--构建订单页面-->
   function createOrderResult(){
   		 $("input[name=ordResult]").val('');
   		//创建table
   		var $table = $("<table>");
   			$table.attr("class","p_table table_center");
   			$table.append("<tbody>");
   			
   		var adult = getSelectAdult();
   		var child = getSelectChild();	
   		
   		//添加舱房
   		$(".cfTable tr").each(function(){
   			 var $tr = $(this);
   			 if($tr.find(".operate").size()==1 && !$tr.find("a").is(".btn_cc1")){
   			 	var branchName = $tr.find("td").eq(1).html();
   			 	var tempAdult =  $(this).find("input[data=adult_count]").val();
   			 	tempAdult = tempAdult == "" ? 0 : parseFloat(tempAdult);
   			 	var tempchild =  $(this).find("input[data=child_count]").val();
   			 	tempchild = tempchild == "" ? 0 : parseFloat(tempchild);
   			 	var total = parseInt(tempAdult) + parseInt(tempchild);
   			 	var quantity = $tr.find("select[name=room_count]").val();
   			 	var $tr = createOrderResultTr('ship','舱房',branchName,total+'人/'+quantity+'间');
   			 	$table.find('tbody').append($tr);
   			 }
   	     });
   	     //添加签证
   	     $(".qzTable tr").each(function(i){
   	    	 var $tr = $(this);
   			if(!$(this).find("td").eq(6).find("a").is(".btn_cc1") && i>1){
   			 	var branchName = $tr.find("td").eq(0).html();
   			 	var adult_visa = adult;
   				var child_visa = child;
   				var useself = $tr.attr("useself");
   				if("T"==useself){
   					var adult_useself = $(this).attr("adult") == "" ? 0 : parseInt($(this).attr("adult"));
   					var child_useself = $(this).attr("child") == "" ? 0 : parseInt($(this).attr("child"));
   					adult_visa = adult_useself;
   					child_visa = child_useself;
   				}
   				var total = adult_visa + child_visa;
   			 	var $tr = createOrderResultTr('visa','签证',branchName,total + '份');
   			 	$table.find('tbody').append($tr);
   			}
   		});
   		//岸上观光
   		$(".ggTable tr").each(function(i){
   			var $tr = $(this);
   			if(!$(this).find("td").eq(6).find("a").is(".btn_cc1") && i>1){
	   		   var branchName = $tr.find("td").eq(0).html();
   				var total = adult + child;
   			 	var $tr = createOrderResultTr('sight','岸上观光',branchName,total + '份');
   			 	$table.find('tbody').append($tr);
   			}
   		});	
   		//保险
   		$(".bxTable tr").each(function(i){
   			var $tr = $(this);
   			var useflag = $tr.attr("useFlag");
   			if(useflag == 'Y' && i>0){
	   		   var branchName = $tr.find("td").eq(0).find("a").html();
   				var total = adult + child;
   			 	var $tr = createOrderResultTr('baoxian','保险',branchName, total + '份');
   			 	$table.find('tbody').append($tr);
   			}
   		});	   		
   		//附加项
   		$(".fjTable tr").each(function(i){
   			var $tr = $(this);
   			if(!$(this).find("td").eq(6).find("a").is(".btn_cc1") && i>1){
	   		   var branchName = $tr.find("td").eq(0).html();
   				var total = adult + child;
   			 	var $tr = createOrderResultTr('addition','邮轮附加项',branchName,total + '份');
   			 	$table.find('tbody').append($tr);
   			}
   		});
   		//各种合并单元格
   		//舱房
   		 if($table.find("tr.ship").size() > 1){
   		 	$table.find("tr.ship").each(function(i){
   		 		if(i==0){
   		 			$(this).find('td').eq(0).attr("rowspan",$table.find("tr.ship").size());
   		 		}else {
   		 			$(this).find('td').eq(0).remove();
   		 		}
   		 	});
   		 }
   		 //签证
   		 if($table.find("tr.visa").size() > 1){
   		 	$table.find("tr.visa").each(function(i){
   		 		if(i==0){
   		 			$(this).find('td').eq(0).attr("rowspan",$table.find("tr.visa").size());
   		 		}else {
   		 			$(this).find('td').eq(0).remove();
   		 		}
   		 	});
   		 }
   		 //岸上观光
   		 if($table.find("tr.sight").size() > 1){
   		 	$table.find("tr.sight").each(function(i){
   		 		if(i==0){
   		 			$(this).find('td').eq(0).attr("rowspan",$table.find("tr.sight").size());
   		 		}else {
   		 			$(this).find('td').eq(0).remove();
   		 		}
   		 	});
   		 }
   		 //附加项
   		 if($table.find("tr.addition").size() > 1){
   		 	$table.find("tr.addition").each(function(i){
   		 		if(i==0){
   		 			$(this).find('td').eq(0).attr("rowspan",$table.find("tr.addition").size());
   		 		}else {
   		 			$(this).find('td').eq(0).remove();
   		 		}
   		 	});
   		 }
   		 
   		 //保险
   		 if($table.find("tr.baoxian").size() > 1){
   		 	$table.find("tr.baoxian").each(function(i){
   		 		if(i==0){
   		 			$(this).find('td').eq(0).attr("rowspan",$table.find("tr.baoxian").size());
   		 		}else {
   		 			$(this).find('td').eq(0).remove();
   		 		}
   		 	});
   		 }   		 
   		 
   		 //优惠信息
   		 if($("#couponChecked").val()=="true"){
   		 	 var $tr = createOrderResultTr('coupon','优惠券',$('#couponAmount').val()+'元','1份');
   			 $table.find('tbody').append($tr);
   		 }
   		 //合并最后一个
   		 if($table.find('tr').size()>1){
   		 	$table.find('tr').each(function(i){
   		 		if(i==0){
   		 			$(this).find('td').eq(3).attr("rowspan",$table.find('tr').size());
   		 			var totalPrice = $("input[name=orderTotalPrice]").val();
   		 			$(this).find('td').eq(3).text("总价:"+(totalPrice/100).toFixed(2));
   		 		}else {
   		 			$(this).find('td').eq($(this).find('td').size()-1).remove();
   		 		}
   		 	});
   		 }else if($table.find('tr').size()==1){
   		 	var totalPrice = $("input[name=orderTotalPrice]").val();
   		 	$table.find('tr').find('td').eq(3).text("总价:"+(totalPrice/100).toFixed(2));
   		 }
   	     $("input[name=ordResult]").val($table.html());
   }
   
   function createOrderResultTr($class,categoryName,branchName,quantity){
   		var $tr = [];
   		$tr.push('<tr class='+$class+'>');
   		$tr.push('<td>'+categoryName+'</td>');
   		$tr.push('<td>'+branchName+'</td>');
   		$tr.push('<td>'+quantity+'</td>');
   		$tr.push('<td></td>');
   		$tr.push('</tr>');
   		return $tr.join('');
   }
   
   //提交订单方法
 function setAndsubmitOrder(){
   //验证是否有成人
   if(getSelectAdult()==0){
   	$.alert("请至少选择一个成人，儿童不能单卖!");
   	return false;
   }
 
   //验证人员数量
   if(getSelectAdult()+getSelectChild()==0){
   		$.alert("请设置游客数量");
   		return false;
   }
   
   //验证舱房
	if(!$("#shipForm,#touristForm").validate().form()){
		return false;
	}
	
	//游玩人后置设置默认属性
   	if(isTravellerDelay()){
   		if(!setdTravellerdefaulInfo(true)){
   			return false;
   		}
   	}
	
	//验证游客
	if(!$("#touristForm").validate().form()){
		return false;
	}
	
	//验证备注
	if(!$("#remarkForm").validate().form()){
		return false;
	}
	
	//验证联系人
	if(!$("#contractForm").validate({
			rules : {
				"contact.fullName" : {
					required : true,
					maxlength:10
				}
				,
				"contact.mobile" : {
					required : true,
					isMobile : true
				}
			},
			messages : {
				"contact.fullName" : '联系人不能为空'
			}
	
	}).form()){
		return false;
	}
	<#--其它验证-->
	//验证英文姓和英文名是否是字母
	try {
		$(".ti").each(function(){
			var $tr = $(this);
			var $td = $tr.find("td").eq(0).find("input[name=person]");
		   	//是否规格-房间-第一个
		   	var xfjFirst = $td.attr("xfjFirst");  
		   	if(xfjFirst == "Y"){
				var firstName  = $tr.find("td").eq(4).find("input").val();
				var lastName  = $tr.find("td").eq(3).find("input").val();
				var alphabet = /^[a-z A-Z]+$/;
				if(firstName!=""&&!alphabet.test(firstName)){
					throw "英文姓:"+firstName+" 只能包含英文字符";
				}
				if(lastName!=""&&!alphabet.test(lastName)){
				 	throw "英文名:"+lastName+" 只能包含英文字符";
				}							   	
		   	}else{
				var firstName  = $tr.find("td").eq(3).find("input").val();
				var lastName  = $tr.find("td").eq(2).find("input").val();
				var alphabet = /^[a-z A-Z]+$/;
				if(firstName!=""&&!alphabet.test(firstName)){
					throw "英文姓:"+firstName+" 只能包含英文字符";
				}
				if(lastName!=""&&!alphabet.test(lastName)){
				 	throw "英文名:"+lastName+" 只能包含英文字符";
				}		   	
		   	}
		});
	}catch(e){
		$.alert(e);
		return false;
	}
	
	//验证成人数和儿童数是否正确
	var tiAdult = 0;
	var tiChild = 0;
	$(".ti").each(function(){
		var $tr = $(this);
		var $td = $tr.find("td").eq(0).find("input[name=person]");
		//是否规格-房间-第一个
		var xfjFirst = $td.attr("xfjFirst");  
		var type = "";
		if(xfjFirst == "Y"){
			type  = $tr.find("td").eq(8).find("select").val();
		}else{
			type  = $tr.find("td").eq(7).find("select").val();
		}		
		if(type=="PEOPLE_TYPE_ADULT"){
			tiAdult++;
		}else if(type=="PEOPLE_TYPE_CHILD"){
			tiChild++;
		}
	});
	if(getSelectAdult()!=tiAdult || getSelectChild()!=tiChild){
		$.alert("游玩人中成人数和儿童数设置不正确!");
		return false;
	}
	
	//验证成人数和儿童数是否正确	
	try{
		$(".cfTable tr").each(function(){
   			 var $tr = $(this);
   			 if($tr.find(".operate").size()==1 && !$tr.find("a").is(".btn_cc1")){
   			 	var suppGoodsId = $tr.attr("suppGoodsId");
   			 	var tempAdult =  $(this).find("input[data=adult_count]").val();
   			 	tempAdult = tempAdult == "" ? 0 : parseFloat(tempAdult);
   			 	var tempchild =  $(this).find("input[data=child_count]").val();
   			 	tempchild = tempchild == "" ? 0 : parseFloat(tempchild);
   			 	var tiAdult = 0;
				var tiChild = 0;
				$(".ti").each(function(){
					var $tr = $(this);
					var $inputObj = $tr.find("input[name=cabin]"); 
					if($inputObj.val()==suppGoodsId){
						var $td = $tr.find("td").eq(0).find("input[name=person]");
						//是否规格-房间-第一个
						var xfjFirst = $td.attr("xfjFirst");  
						var type = "";
						if(xfjFirst == "Y"){
							type  = $tr.find("td").eq(8).find("select").val();
						}else{
							type  = $tr.find("td").eq(7).find("select").val();
						}						
						if(type=="PEOPLE_TYPE_ADULT"){
							tiAdult++;
						}else if(type=="PEOPLE_TYPE_CHILD"){
							tiChild++;
						}
					}
				});
				if(tempAdult!=tiAdult || tempchild!=tiChild){
					throw "游玩人中成人数和儿童数设置不正确!";
				}
   			 }
   	     });
	
	}catch(e){
		$.alert(e);
		return false;
	}
	
	
	//验证是否有一个手机号
	try {
		var hasLastOneMobile = false;
		$(".ti").each(function(){
			var $tr = $(this);
			var $td = $tr.find("td").eq(0).find("input[name=person]");
			//是否规格-房间-第一个
			var xfjFirst = $td.attr("xfjFirst");  
			var mobile;
			if(xfjFirst == "Y"){
				mobile  = $tr.find("td").eq(14).find("input").val();
			}else{
				mobile  = $tr.find("td").eq(13).find("input").val();
			}				
			 var ismobile = /^1[3|4|5|8|7|9][0-9]\d{8}$/;
			 if(ismobile.test(mobile)){
			 	hasLastOneMobile = true;
			 	return false;
			 }
		});
		//游玩人后置不检验
   		if(isTravellerDelay()){
   			hasLastOneMobile = true;
   		}
		if(!hasLastOneMobile){
			throw "游客中应该至少有一个出行游客填写联系方式";
		}
	}catch(e){
		$.alert(e);
		return false;
	}
	
	
	<#--检查舱房是否有游玩人-->
	try {
		$(".cfTable tr").each(function(){
   			 var $tr = $(this);
   			 if($tr.find(".operate").size()==1 && !$tr.find("a").is(".btn_cc1")){
   			 	var suppgoodsId = $tr.attr("suppGoodsId");
   			   	<#--检查舱房是否有游玩人-->
   			   	var hasPerson = false;
   			   	$("input[name=cabin]").each(function(){
   			   		if($(this).val()==suppgoodsId){
   			   			hasPerson = true;
   			   		}
   			   	});
   			   	if(hasPerson == false){
   			   		throw "请为已选舱房设置游玩人!";
   			   	}
   			   	//成人数
   			    var adult = $tr.find("input[data=adult_count]").val();
   			    adult = adult == "" ? 0 : parseInt(adult);
   			    //房间数
   			    var roomCount = $tr.find("select[name=room_count]").val();
   			   	 //判断成人数是否大于房间数
   			    if(adult<roomCount){
   			    	throw "每个房间至少要有一个成人!";
   			    }
   			 }
   		});
	}catch(e){
		$.alert(e);
		return false;
	}
	
	// 验证游客证件号不能相同
	var checkFlag=true;
	$.each($("input[name_type='idNo'][name^='travellers']"),function(i,n){
		var idNo1 = $(n).val();
		if(idNo1!='undefined'&&idNo1!=null&&idNo1!=""){
			$.each($("input[name_type='idNo'][name^='travellers']"),function(j,m){
				var idNo2 = $(m).val();
				if(idNo1 == idNo2 && n != m){
					$.alert("游玩人证件号不能一样！");
					checkFlag=false;
					return false;
				}
			});
		}
		if(!checkFlag){
			return false;
		}
	});
	if(!checkFlag){
		return false;
	}
	
	initData();
    return true;
   }
  function submitOrder(){
  	if(!setAndsubmitOrder()){
          	return;
          }
          
  		<#--7.验证数据-->
	    if(isSubmit){
	    	return;
	    }
	    var code=$("#couponCode").val();
   		var couponChecked=$("#couponChecked").val();
	    if( couponChecked == 'true' && $.trim(code)!=""){
	    	$("#couponForm").append("<input type='hidden' id='userCouponVoList' name='userCouponVoList[0].couponCode' value="+code+">");
	    }
	    isSubmit = true;
  		<#--8.提交数据-->
   	 	loading	= pandora.loading("正在核对订单数据...");
	    $.ajax({
	    	url : '/vst_order/ord/order/combBackCreateOrder.do?',
	        type: "POST",
	        dataType: "html",
	        data : $("#baseDataForm,#ordForm,#touristForm,#orderTravellerConfirmForm,#contractForm,#remarkForm,#couponForm, #isTestOrderForm").serialize(),
	        success: function(html){
	        	loading.close();
	          	 submitOrderDialog = pandora.dialog({
			        width: 700,
			        title: "核对订单",
			        mask : true,
			        content: html
	   			 });
	   			 isSubmit = false;
	        },
	        error: function() {
	        	isSubmit = false;
	        	loading.close();
	        }
	    })
   }
   
   
//查询促销
function countPromotion(){
	$("#promotionDetail").html("");
	$("#promotionDiv").css('display','none');
	$("#promotionPrice").val(0);
	
	initData();
   $.ajax({
   	url : '/vst_order/ord/book/ajax/queryPromotion.do?',
       type: "POST",
       dataType:'JSON',
       data : $("#baseDataForm,#ordForm,#touristForm,#orderTravellerConfirmForm,#contractForm,#remarkForm,#couponForm").serialize(),
       success: function(data){
       var totalPrice=0;
       var promList=data.attributes.promList;
		if(promList!=null){
			var promStr="";
			var selectChannel=$("input[paymentChannel]:checked").val();
			$.each(promList, function(k, item){
				if(item.promitionType!="ORDERCHANNELFAVORABLE"){
				totalPrice+=item.discountAmount;
				promStr+="<input type='hidden' id='promotionMap' name='promotionMap["+item.key+"]' value='"+item.promPromotionId+"'>";
				promStr+="<div ><span class='tags101'>";
				if(item.promitionType=="eraly_order_type"){
					promStr+="早订早惠</span>";
					promStr+="提前"+item.ruleValue+"天预订，销售单价优惠";
				}
				else if(item.promitionType=="more_order_more_favorable"){
					promStr+="多订多惠</span>";
					if(item.ruleType=="EACH_FULL"){
						promStr+="每满"+item.ruleValue+"份，销售总价优惠";
					}if(item.ruleType=="FULL"){
						promStr+="满"+item.ruleValue+"份起，销售单价优惠";
					}if(item.ruleType=="REDUCE_PRICE"){
						promStr+="满"+item.ruleValue+"份后，每增加"+item.promResult.addEach+"份，销售总价优惠";
					}
					
				}else if(item.promitionType=="IMMEDIATELY_FAVORABLE"){
					promStr+="立减</span>";
					if(item.ruleType=="EACH_FULL"){
						promStr+="满"+item.ruleValue/100+"元，销售总价优惠";
					}if(item.ruleType=="REDUCE_PRICE"){
						promStr+="满"+item.ruleValue/100+"元后，每增加"+item.promResult.addEach/100+"元，销售总价优惠";
					}
					
				}else if(item.promitionType=="LINERfAVORABLE"){
					promStr+="邮轮优惠</span>";
					if(item.promResult.amountType=="AMOUNT_FIXED"){
						if(item.promResult.fixedAmount!=''&&item.promResult.fixedAmount>0){
							promStr+="一二人价优惠"+item.promResult.fixedAmount/100+"元，";
						}
						if(item.promResult.linerSecFixedAmount!=''&&item.promResult.linerSecFixedAmount>0){
							promStr+="三四成人价优惠"+item.promResult.linerSecFixedAmount/100+"元，";
						}
						if(item.promResult.linerThiFixedAmount!=''&&item.promResult.linerThiFixedAmount>0){
							promStr+="三四儿童价优惠"+item.promResult.linerThiFixedAmount/100+"元，";
						}
						
					}
					
					if(item.promResult.amountType=="AMOUNT_PERCENT"){
						if(item.promResult.rateAmount!=''&&item.promResult.rateAmount>0){
							promStr+="一二人价优惠"+item.promResult.rateAmount+"%，";
						}
						if(item.promResult.linerSecRateAmount!=''&&item.promResult.linerSecRateAmount>0){
							promStr+="三四成人价优惠"+item.promResult.linerSecRateAmount+"%，";
						}
						if(item.promResult.linerThiRateAmount!=''&&item.promResult.linerThiRateAmount>0){
							promStr+="三四儿童价优惠"+item.promResult.linerThiRateAmount+"%，";
						}
					}
					
				}
				if(item.promitionType!="LINERfAVORABLE"){
				if(item.promResult.amountType=="AMOUNT_FIXED"){
						promStr+=item.promResult.fixedAmount/100+"元，";
					}
				if(item.promResult.amountType=="AMOUNT_PERCENT"){
					promStr+=item.promResult.rateAmount+"%，";
				}
				}
				promStr+="合计优惠"+item.discountAmount/100+"元</div>";
			}
				});
			$("#promotionDetail").html(promStr);
			$("#promotionDiv").css('display','');
		}
		if(promList.length==0){
			$("#promotionDetail").html("");
			$("#promotionDiv").css('display','none');
		}
		
		$("#promotionPrice").val(totalPrice);
		calculateTotalPrice();
       }
   });
}
   
   
   function initData(){
   
     $("#ordForm").empty();
   	var specDate = $("#specDate").val();
   	$("input[name=specDate]").val(specDate);
   	var index = 0;
    <#--收集待提交数据Form-->
    var adult = getSelectAdult();
	var child = getSelectChild();
    <#--1.收集舱房数据-->
    $(".cfTable tr").each(function(){
   			 var $tr = $(this);
   			 if($tr.find(".operate").size()==1 && !$tr.find("a").is(".btn_cc1")){
   			 	 var goodsId = $tr.attr("suppGoodsId");
   			 	 var quantity = $tr.find("select[name=room_count]").val();
   			 	 //成人数
   			    var adult = $tr.find("input[data=adult_count]").val();
   			    adult = adult == "" ? 0 : parseInt(adult);
   			    //儿童数
   			    var child = $tr.find("input[data=child_count]").val();
   			    child = child == "" ? 0 : parseInt(child);
   			 	 <#--创建商品Item-->
   			 	 createItem(index,goodsId,quantity,specDate,'true',0,adult,child);
   			 	 index++;
   			 }
   	  });
    
    <#--2.收集签证数据-->
    	$(".qzTable tr").each(function(i){
   			if(!$(this).find("td").eq(6).find("a").is(".btn_cc1") && i>1){
   				var goodsId = $(this).attr("suppGoodsId");
   				var useself = $(this).attr("useself");
   				var visaPrice = $(this).find("td").eq(2).attr("price");
   				visaPrice = visaPrice == "" ? 0 : parseFloat(visaPrice);
   				<#--计算使用签证的人数-->
   				var adult_visa = adult;
   				var child_visa = child;
   				if("T"==useself){
   					var adult_useself = $(this).attr("adult") == "" ? 0 : parseInt($(this).attr("adult"));
   					var child_useself = $(this).attr("child") == "" ? 0 : parseInt($(this).attr("child"));
   					adult_visa = parseInt(adult_useself);
   					child_visa = parseInt(child_useself);
   				}
   				 var quantity = adult + child;
   				 var ownerQuantity = (adult + child - adult_visa -child_visa);
   				 <#-- 创建商品Item -->
   				 if(goodsId=="")return;
   			 	 createItem(index,goodsId,quantity,specDate,'false',ownerQuantity,0,0);
   			 	 index++;
   			}
   		});
    
    <#--3.收集岸上观光数据-->
    	$(".ggTable tr").each(function(i){
    		//获得已选的出游人数
	   		var adult = getSelectAdult();
	   		var child = getSelectChild();
   			if(!$(this).find("td").eq(6).find("a").is(".btn_cc1") && i>1){
	   		    var goodsId = $(this).attr("suppGoodsId");
	   		    var quantity = adult + child;
	   		    if(goodsId=="")return;
   				createItem(index,goodsId,quantity,specDate,'false',0,adult,child);
   			 	index++;
   			}
   		});
   		 <#--5.收集保险数据-->
    	$(".bxTable tr").each(function(i){
    		//获得已选的出游人数
	   		var adult = getSelectAdult();
	   		var child = getSelectChild();
	   		var useflag = $(this).attr("useFlag");
   			if(useflag == 'Y' && i>0){
	   		    var goodsId = $(this).attr("goodsid");
	   		    var quantity = adult + child;
	   		    if(goodsId=="")return;
   				createItem(index,goodsId,quantity,specDate,'false',0,0,0);
   			 	index++;
   			}
   		});   		
    <#--4.收集附加项数据-->
    	$(".fjTable tr").each(function(i){
   			if(!$(this).find("td").eq(6).find("a").is(".btn_cc1") && i>1){
	   		    var goodsId = $(this).attr("suppGoodsId");
	   		    var quantity = adult + child;
	   		    if(goodsId=="")return;
   				createItem(index,goodsId,quantity,specDate,'false',0,0,0);
   			 	index++;
   			}
   		});
    <#--5.收集联系人数据-->
    	<#--设置游玩人的index-->
    	$(".touristInfo").find(".ti").each(function(index){
    		$(this).find("input,select").each(function(i){
    			if(i>0){
    				var name = $(this).attr("name");
    				name = name.replace("index",index);
    				$(this).attr("name",name);
    			}
    		});
    	});
    <#--6.收集其它数据-->
    	<#--6.1 邮轮产品ID-->
    	$("#ordForm").append($("#shipProductId").clone());
    	$("input[name=userId]").val(book_user_id);
    	
    	<#--6.2订单核对页面数据-->
    	createOrderResult();
    	
    	<#--6.3设置选中的产品名称-->
    	setSelectProductName();
    	
    	<#--6.4组装传真和固定电话-->
    	$("input[name='contact.fax']").val($("#fax1").val()+""+$("#fax2").val()+""+$("#fax3").val());
    	$("input[name='contact.phone']").val($("#phone1").val()+""+$("#phone2").val()+""+$("#phone3").val());
     
   }
   
   
   
   
   $("#submitOrder").bind("click",submitOrder);
   
   //舱房选择，鼠标悬浮在舱房上，呈现舱房的基础信息
	function houseTypeMouseover(goodsId,params){
			var that = $(params);
			var tipContent = "无数据";
			var id="#"+goodsId+"Div";
			tipContent=$(id).html();
			that.attr("title",tipContent.replace(/\s+/g,""));
	}
	
	
	
	
	
	$(function(){
	$("#couponVerify").click(function(){
         var code=$("#couponCode").val();
         if($.trim(code)==""){
        	 $("#couponInfoMsg").html("请输入优惠券代码.");
        	 $("#couponChecked").val("false");
        	 return;
         }
         /*if(!setAndsubmitOrder()){
           	return;
         }*/
         $("#couponInfoMsg").html("");
         $("#couponChecked").val("true");
         //lineBackCheckStock();
        var quantity = $("input[name='itemList[0].quantity']").val();
        if(!quantity||quantity=='0'){
            $.alert("请至少选择一个商品!");
            return false;
        }
         couponVerify();
	});
	$("#couponCode").change(function(){
        var code=$("#couponCode").val();
        if($.trim(code)==""){
       	 $("#couponChecked").val("false");
        }
	});
});

function couponVerify(){
	initData();
	$.ajax(
	        {
	            type: "get",
	            async: false,
	            url: "/vst_order/ord/order/validateCoupon.do",
	            data: $("#baseDataForm,#ordForm,#touristForm,#orderTravellerConfirmForm,#contractForm,#remarkForm,#couponForm").serialize(),
	            dataType: "json",
	            success: function (data) {
	            	if(data.success){
	            		$("#couponInfoMsg").html("优惠券代码可用.");
	            		 //lineBackCheckStock();
                        $("#couponChecked").val("true");
                         calculateTotalPrice();
	            	}else{
	            		$("#couponInfoMsg").html(data.code);
	            		$("#couponChecked").val("false");
	            	}
	            }
	        }
	    );
}



/*游玩人后置部分*/


/*
选中对应舱房的游玩人赋予默认值(游玩人后置使用)
isSaveOrder 是否是保存订单状态
*/
//缓存对应舱房对应房间的人数
var dataMap = {};
function setdTravellerdefaulInfo(isSaveOrder,pid){
	var sucessFlag = true;
	try{
	
	$("tr[id *= 'pbid']").each(function(){
   	var ad =  $(this).attr("adult");
   	var ch = $(this).attr("child");
   	var rooms = $(this).attr("rooms");
   	var prodBranchId = $(this).attr("id").replace("pbid","").trim();
   	if(!isNaN(ad) && !isNaN(ch) && !isNaN(rooms)){
   		var adult = parseInt(ad);
   		var child = parseInt(ch);
   		var romNum= parseInt(rooms);
   		if(adult>0 || child>0){
   		
   		//房间分成人儿童并选中
   		if(!isSaveOrder && (pid == prodBranchId)){
   		var cabinData = dataMap[prodBranchId];
   		var adultcount = Math.ceil(adult/romNum);
   		//分成人
   		while(adultcount>0){
   		$.each(cabinData,function(i,n){
   			var perosn = cabinData[i];
   			if(perosn>0 && ad>0){
   			if(cabinData["adult-"+i] == null){
   				var aultNum = 0;
   				cabinData["adult-"+i] = aultNum +1;
   				ad --;
   			}else{
   				cabinData["adult-"+i] = cabinData["adult-"+i] +1;
   				ad --;
   			}
   			perosn --;
   			}
   		});
   			adultcount --;
   		}
   		$.each(cabinData,function(i,n){
   			var perosn = cabinData[i];
   			cabinData["child-"+i] = perosn - cabinData["adult-"+i];
   		});
   		//分儿童
   		for(var i = 0;i<romNum;i++){
   			var anum = cabinData["adult-"+i];
   			var cnum = cabinData["child-"+i];
   			$(".branchcc_"+prodBranchId).filter("[roomnum='"+i+"' ]").each(function(i,n){
   				if(anum>0){
   					$(this).find("select[name *='peopleType']").find("option[value='PEOPLE_TYPE_ADULT']").attr("selected",true);
   					anum --;
   				}else if((cnum>0) && (anum <=0)){
   					$(this).find("select[name *='peopleType']").find("option[value='PEOPLE_TYPE_CHILD']").attr("selected",true);
   					cnum --;
   				}
   			
   			});
   		}
   		dataMap[prodBranchId] = null;
   		}
   		
   		//保存订单时游玩人姓名为空的设置为待填写
   		if(isSaveOrder){
   		var flag = false;
   		$(".branchcc_"+prodBranchId).each(function(){
   		if((adult<=0) && (child<=0)){
   			return;
   		}
   		var cardNum = $(this).find("input[name_type='idNo']").val();
   		if(cardNum!=""){
   		var cardType = $(this).find("select[name *='idType']").find("option:checked").val();
   		if("ID_CARD" == cardType){
   			if(!LVUTIL.ORDER.COMM.isIdCardNum(cardNum)){
   				$(this).find("input[name_type = 'idNo']").focus();
   				$.alert("请输入正确的身份件号码!");
   				sucessFlag = false;
   			}
   		}
   		
   		}
   		
   		var $travellerName = $(this).find("input[name *='fullName']");
   			if(($travellerName.val() == "") || ($travellerName.val() == "待填写")){
   				$travellerName.val("待填写");
   				flag = true;
   			}
   		
   		});
   	
   		//存在待填写游客姓名不全部保存游客信息
   		if(flag){
			$("input[name='saveAllFlag']").val("N");   		
   		}else{
   			$("input[name='saveAllFlag']").val("Y");
   		}
   		}
   		
   		}
   	} 
   	});
	
	}catch(err){
	
	}
	return sucessFlag;
}

//是否可游玩人后置
function isTravellerDelay(){
return "Y" == $("input[name='travellerDelayFlag']").val();
}
/*游玩人后置部分*/	
	
	
  </script>