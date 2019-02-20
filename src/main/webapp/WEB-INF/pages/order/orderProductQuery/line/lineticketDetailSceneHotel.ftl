<#--景酒门票规格商品展示-->

<#if detail.prodProductBranch?? && detail.prodProductBranch.recommendSuppGoodsList??&& detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0 >
<table width="100%" class="updateChangeTable">
    <tbody>
    <tr>
        <td>
            <#assign prodProductBranch=detail.prodProductBranch />
            <table class="tab_nav" width="100%">
                <!-- 添加产品名称 -->
                <tr id="firstLineTicketShow${detail.groupId}_productNameLink">
                    <td colspan="3">
                        <#if detail.prodProduct.bizCategory.categoryCode=="category_other_ticket">
                            <a class="pro_tit"
                               href="http://ticket.lvmama.com/p-${detail.prodProduct.productId}"
                               target="_blank">${detail.prodProduct.productName}</a>
                        </#if>
                        <#if detail.prodProduct.bizCategory.categoryCode=="category_single_ticket">
                            <a class="pro_tit"
                               href="http://ticket.lvmama.com/scenic-${detail.prodProduct.urlId}"
                               target="_blank">${detail.prodProduct.productName}</a>
                        </#if>
                        <#if detail.prodProduct.bizCategory.categoryCode=="category_comb_ticket">
                            <span style="color:#0088CC;">${detail.prodProduct.productName}</span>
                        </#if>
                    </td>
                </tr>

                <tr id="firstLineTicketShow${detail.groupId}" groupId="${detail.groupId}"
                    productBranchId="${prodProductBranch.productBranchId}" adult="${adultNum}"
                    child="${childNum}" totalAmount="${prodProductBranch.dailyLowestPrice}"
                    class="table_nav">
                    <td width="50%">
                        <#list detail.prodProductBranch.recommendSuppGoodsList as goods>
                            <#if goods_index gt 0>
                                <br/>
                            </#if>
                        <#--商品显示样式： 规格：{规格名}+商品ID：{商品ID}+商品名：{商品名}-->
                            <a class="pro_tit showTicket" href="javascript:;"
                               desc="${prodProductBranch.branchName}"
                               prodBranchId="${prodProductBranch.productBranchId}">
                                &nbsp;&nbsp;规格：${prodProductBranch.branchName} &nbsp;&nbsp; 商品ID：${goods.suppGoodsId}
                                &nbsp; 商品名：${goods.goodsName}
                            </a>
                            <input type="hidden"
                                   name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId"
                                   value="${goods.suppGoodsId}" autocomplete="off"/>
                            <input type="hidden"
                                   name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId"
                                   value="${detail.detailId}" autocomplete="off"/>
                            <input type="hidden"
                                   name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation"
                                   value="PACK" autocomplete="off"/>
                            <input id="${goods.suppGoodsId}TotalNum" type="hidden"
                                   class="w5 numText"
                                   name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity"
                                   style="text-align:center" value="${goods.fitQuantity}"
                                   required=true number=true/>
                            <input class="itemLineSelectDate" type="hidden"
                                   name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime"
                                   value="${prodProductBranch.selectDateList[0]}"
                                   autocomplete="off"/>
                            <#assign productItemIdIndex=productItemIdIndex+1 />
                            <script>
                                productItemIdIndex = '${productItemIdIndex}';
                            </script>
                        </#list>
                    </td>
                    <td width="20%">
                    <#--期票不需要选择日期，不需要出现日期选择框-->
                    <#--期票没有一键下单-->
                        <#list detail.prodProductBranch.recommendSuppGoodsList as goods>
                            <#assign dateList = goods.selectPriceMap?keys>
                            <#if goods_index gt 0>
                                <br/>
                            </#if>
                            <#if goods.aperiodicFlag != "Y">
                                <select class="lineSelectDate" index = "${goods_index}" data-suppGoodsid="${goods.suppGoodsId}" data-other="true">
                                    <#if goods?? && goods.selectPriceMap??>
                                        <#list dateList as date>
                                            <#assign price =goods.selectPriceMap[date]/100 >
                                            <#assign auditPrice=price / goods.fitQuantity />
                                            <option value="#{price;M2}" auditPrice="#{auditPrice;M2}">${date}</option>
                                        </#list>
                                    </#if>
                                </select>
                                &nbsp;&nbsp;&nbsp;&nbsp;
                                <select childPrice="0" type="text" class="selectQunantityDropdownListTicket" data-groupId="${detail.groupId}"
									data-suppGoodsid="${goods.suppGoodsId}" data-goodsSpec="${goods.goodsSpec}" 
									data-reType="OPTIONAL"
									style="width:50px;"  style="text-align:center" required=true number=true>
										<option value="${goods.fitQuantity}" >${goods.fitQuantity}</option>
									</select>
									份
                            <#else>
                                <#--线路期票有效期检查信息-->
                                <#assign validMapKey=changeProdPackage.groupId+"_"+prodProductBranch.productBranchId+"_"+goods.suppGoodsId>
                                <div name="aperTicketTimeValidMsg" index = "${goods_index}"
                                     style="display:none">${lineTicketValidMsgMap[validMapKey]}</div>
                                <label>期票无需选择游玩日期</label>
                                <#--隐藏一个期票价格-->
                                <select class="moreLineSelectDate" index="${goods_index}" style=" display:none;">
                                    <#if goods?? && goods.selectPriceMap??>
                                        <#list dateList as date>
                                        <#--期票有且仅有一个date-->
                                            <#if date_index == 0>
                                                <#assign price =goods.selectPriceMap[date]/100 >
                                                <option value="#{price;M2}" selected="selected">${date}</option>
                                            </#if>
                                        </#list>
                                    </#if>
                                </select>
                            </#if>
                        </#list>
					 </td>
                    <td width="10%" class="orange" class="itemLineSelectDatePrice">
                        总价:￥${prodProductBranch.dailyLowestPriceYuan}元
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <p class="descript">规格描述：<textarea class="textarea"></textarea></p>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    </tbody>
</table>
</#if>