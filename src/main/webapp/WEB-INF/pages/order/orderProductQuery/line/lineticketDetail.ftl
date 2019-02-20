<#--非景酒门票规格商品展示-->
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
                            <a class="pro_tit" href="http://ticket.lvmama.com/p-${detail.prodProduct.productId}" target="_blank">${detail.prodProduct.productName}</a>
                        </#if>
                        <#if detail.prodProduct.bizCategory.categoryCode=="category_single_ticket">
                            <a class="pro_tit" href="http://ticket.lvmama.com/scenic-${detail.prodProduct.urlId}" target="_blank">${detail.prodProduct.productName}</a>
                        </#if>
                        <#if detail.prodProduct.bizCategory.categoryCode=="category_comb_ticket">
                            <span style="color:#0088CC;">${detail.prodProduct.productName}</span>
                        </#if>
                    </td>
                </tr>

                <tr id="firstLineTicketShow${detail.groupId}" groupId="${detail.groupId}" productBranchId="${prodProductBranch.productBranchId}" adult="${adultNum}" child="${childNum}" totalAmount="${prodProductBranch.dailyLowestPrice}" class="table_nav" >
                    <td width="50%"><a class="pro_tit showTicket" href="javascript:;" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}">${detail.prodProduct.productName}(${prodProductBranch.branchName})</a>
                        <#list detail.prodProductBranch.recommendSuppGoodsList as goods>
                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
                            <input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
                            <input class="itemLineSelectDate" type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="${prodProductBranch.selectDateList[0]}" autocomplete="off"/>
                            <#assign productItemIdIndex=productItemIdIndex+1 />
                            <script>
                                productItemIdIndex='${productItemIdIndex}';
                            </script>
                        </#list>
                    </td>
                    <#assign firstGoods=detail.prodProductBranch.recommendSuppGoodsList[0]>
                    <td width="20%">
                        <select class="lineSelectDate">
                            <#if prodProductBranch?? && prodProductBranch.selectDateList??>
                                <#assign index=0 />
                                <#list prodProductBranch.selectDateList as date>
                                    <option value="${prodProductBranch.selectPriceMap[date]}"
                                        <#if oneKeyOrderTicketDateMap?? && oneKeyOrderTicketDateMap["${firstGoods.suppGoodsId}"]?? && oneKeyOrderTicketDateMap["${firstGoods.suppGoodsId}"]==date>
                                            selected="selected"
                                        </#if>
                                    >${date}</option>
                                    <#assign index=index+1 />
                                </#list>
                            <#else>
                                <option value="${prodProductBranch.selectPriceMap[specDate]}">${specDate}</option>
                            </#if>
                        </select>
                    </td>
                    <td width="10%" class="orange" class="itemLineSelectDatePrice">总价:￥${prodProductBranch.dailyLowestPriceYuan}元</td>
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