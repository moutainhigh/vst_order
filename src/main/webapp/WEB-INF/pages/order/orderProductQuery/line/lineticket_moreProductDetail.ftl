<#--非景酒页面-->
<table class="tab_nav" width="100%">
    <tr id="${prodProductBranch.productBranchId}_${detail.groupId}" groupId="${detail.groupId}" productBranchId="${prodProductBranch.productBranchId}" adult="${adultQuantity}" child="${childQuantity}"  class="table_nav" >
        <td width="50%"><a class="pro_tit showTicket" href="javascript:;" desc="${prodProductBranch.branchName}" prodBranchId="${prodProductBranch.productBranchId}">${detail.prodProduct.productName}(${prodProductBranch.branchName})</a>
        <#list detail.prodProductBranch.recommendSuppGoodsList as goods>
            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
            <input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
            <input class="itemLineSelectDate" type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].visitTime" value="${prodProductBranch.selectDateList[0]}" autocomplete="off"/>
            <#assign productItemIdIndex=productItemIdIndex+1 />
        </#list>
        </td>
        <td width="20%">
        <#if prodProductBranch.productBranchId==currentProductBranchId>
            <select class="parentSelectedDateList" style="display:none;">
                <#if changeProdPackage.prodPackageGroupTicket?? && changeProdPackage.prodPackageGroupTicket.dateList??>
                    <#assign index=0 />
                    <#list changeProdPackage.prodPackageGroupTicket.dateList as date>
                        <#if selectedDate == date>
                            <option value="${prodProductBranch.selectPriceMap[date]}" selected="selected">${date}</option>
                        <#else>
                            <option value="${prodProductBranch.selectPriceMap[date]}">${date}</option>
                        </#if>
                        <#assign index=index+1 />
                    </#list>
                <#else>
                    <option value="${prodProductBranch.selectPriceMap[specDate]}">${specDate}</option>
                </#if>
            </select>
        </#if>
            <select class="moreLineSelectDate">
            <#if prodProductBranch?? && prodProductBranch.selectDateList??>
                <#assign index=0 />
                <#list prodProductBranch.selectDateList as date>
                    <#if selectedDate == date>
                        <option value="${prodProductBranch.selectPriceMap[date]}" selected="selected">${date}</option>
                    <#else>
                        <option value="${prodProductBranch.selectPriceMap[date]}">${date}</option>
                    </#if>
                    <#assign index=index+1 />
                </#list>
            <#else>
                <option value="${prodProductBranch.selectPriceMap[specDate]}">${specDate}</option>
            </#if>
            </select>
        </td>
        <td width="10%" class="itemLineSelectDatePrice" totalAmount="${prodProductBranch.dailyLowestPrice}" totalAmountYuan="${prodProductBranch.dailyLowestPriceYuan}">
        <#if prodProductBranch.productBranchId==currentProductBranchId>
            --
        <#else>
            差价:￥<@line_func.showSpreadPrice sourceTotalAmount prodProductBranch/>元
        </#if>
        </td>
        <td><div class="operate" style="text-align:center;">
        <#if prodProductBranch.productBranchId==currentProductBranchId>
            <span class="btn w8 " style="margin:0;padding:5px 0;">已选择</span>
        <#else>
            <a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" groupId="${detail.groupId}" productBranchId="${prodProductBranch.productBranchId}" adult="${adultQuantity}" child="${childQuantity}"  packageProductId="${detail.prodProduct.productId}" name="xztcBtn" >选择</a>
        </#if>
        </div>
            <div class="productNameLink" style="display:none;">
            <#if detail.prodProduct.bizCategory.categoryCode=="category_other_ticket">
                <a class="pro_tit" href="http://ticket.lvmama.com/p-${detail.prodProduct.productId}" target="_blank">${detail.prodProduct.productName}</a>
            </#if>
            <#if detail.prodProduct.bizCategory.categoryCode=="category_single_ticket">
                <a class="pro_tit" href="http://ticket.lvmama.com/scenic-${detail.prodProduct.urlId}" target="_blank">${detail.prodProduct.productName}</a>
            </#if>
            <#if detail.prodProduct.bizCategory.categoryCode=="category_comb_ticket">
                <span style="color:#0088CC;">${detail.prodProduct.productName}</span>
            </#if>
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="4">
            <p class="descript">规格描述：<textarea class="textarea"></textarea></p>
        </td>
    </tr>
</table>