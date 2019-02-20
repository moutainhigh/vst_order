<#--景酒页面-->
<#--景酒直接展示到商品，之前是使用prodProductBranch中的信息，修改为使用recommendSuppGoodsList中的goods-->
<table class="tab_nav" width="100%">
    <tr id="${prodProductBranch.productBranchId}_${detail.groupId}" groupId="${detail.groupId}"
        productBranchId="${prodProductBranch.productBranchId}" adult="${adultQuantity}" child="${childQuantity}"
        class="table_nav">
        <td width="40%">
        <#list detail.prodProductBranch.recommendSuppGoodsList as goods>
            <#if goods_index gt 0>
                <br/>
            </#if>
            
            <#-- 景点名称  line.js-->
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
            </#if> </br>
            <p>
            <#--商品显示样式： 规格：{规格名}+商品ID：{商品ID}+商品名：{商品名}-->
            <a class="pro_tit showTicket" href="javascript:;"
               desc="${prodProductBranch.branchName}"
               prodBranchId="${prodProductBranch.productBranchId}">
                &nbsp;&nbsp;规格：${prodProductBranch.branchName} &nbsp;&nbsp; 商品ID：${goods.suppGoodsId}
                &nbsp; 商品名：${goods.goodsName}
            </a>
            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].goodsId"
                   value="${goods.suppGoodsId}" autocomplete="off"/>
            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].detailId"
                   value="${detail.detailId}" autocomplete="off"/>
            <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].routeRelation"
                   value="PACK" autocomplete="off"/>
            <input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText"
                   name="productMap[${productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center"
                   value="${goods.fitQuantity}" required=true number=true/>
            <#assign dateList = goods.selectPriceMap?keys>
            <input class="itemLineSelectDate" type="hidden"
                   name="productMap[${productId}].itemList[${productItemIdIndex}].visitTime"
                   value="${dateList[0]}" autocomplete="off"/></p>
            <#assign productItemIdIndex=productItemIdIndex+1 >
        </#list>
        </td>
        <td width="35%">
        <#list detail.prodProductBranch.recommendSuppGoodsList as goods>
            <#assign dateList = goods.selectPriceMap?keys>
            <#if goods_index gt 0>
                <br/>
            </#if>
            <#if goods.aperiodicFlag != "Y">
                <select class="moreLineSelectDate" index="${goods_index}" data-suppGoodsid="${goods.suppGoodsId}" data-other="true">
                    <#if goods?? && goods.selectPriceMap??>
                        <#assign index=0 >
                        <#list dateList as date>
                            <#assign price =goods.selectPriceMap[date]/100 >
                            <#assign auditPrice=price / goods.fitQuantity />
                            <#if selectedDate == date>
                                <option value="#{price;M2}"
                                        selected="selected" auditPrice="#{auditPrice;M2}">${date}</option>
                            <#else>
                                <option value="#{price;M2}" auditPrice="#{auditPrice;M2}">${date}</option>
                            </#if>
                            <#assign index=index+1 >
                        </#list>
                    </#if>
                </select>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <select childPrice="0" type="text" class="selectmoreQunantityDropdownListTicket" 
					data-suppGoodsid="${goods.suppGoodsId}"
					data-reType="OPTIONAL"
					style="width:50px;"  style="text-align:center" required=true number=true>
						<option value="${goods.fitQuantity}" >${goods.fitQuantity}</option>
					</select>
					份
            <#else>
                <#--期票不需要选择日期，不需要出现日期选择框-->
                <#--线路期票有效期检查信息-->
                <#assign validMapKey=changeProdPackage.groupId+"_"+prodProductBranch.productBranchId+"_"+goods.suppGoodsId>
                <div name="aperTicketTimeValidMsg" style="display:none">${(lineTicketValidMsgMap[validMapKey])!}</div>
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
        <td width="15%" class="itemLineSelectDatePrice" totalAmount="${prodProductBranch.dailyLowestPrice}"
            totalAmountYuan="${prodProductBranch.dailyLowestPriceYuan}">
        <#if prodProductBranch.productBranchId==currentProductBranchId>
            --
        <#else>
            差价:￥<@line_func.showSpreadPrice sourceTotalAmount prodProductBranch/>元
        </#if>
        </td>
        <td>
            <div class="operate" style="text-align:center;">
            <#if prodProductBranch.productBranchId==currentProductBranchId>
                <span class="btn w8 " style="margin:0;padding:5px 0;">已选择</span>
            <#else>
                <a class="btn btn_cc1 w8 " style="margin:0;padding:5px 0;" groupId="${detail.groupId}"
                   productBranchId="${prodProductBranch.productBranchId}" adult="${adultQuantity}"
                   child="${childQuantity}" packageProductId="${detail.prodProduct.productId}" name="xztcBtn">选择</a>
            </#if>
            </div>
            <div class="productNameLink" style="display:none;">
            <#if detail.prodProduct.bizCategory.categoryCode=="category_other_ticket">
                <a class="pro_tit" href="http://ticket.lvmama.com/p-${detail.prodProduct.productId}"
                   target="_blank">${detail.prodProduct.productName}</a>
            </#if>
            <#if detail.prodProduct.bizCategory.categoryCode=="category_single_ticket">
                <a class="pro_tit" href="http://ticket.lvmama.com/scenic-${detail.prodProduct.urlId}"
                   target="_blank">${detail.prodProduct.productName}</a>
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