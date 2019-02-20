<#macro lineTransportdetail changeProdPackage transportType productItemIdIndex ifMore>
    <#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0 &&changeProdPackage.jiPiaoDuiJieFlag=='N'>
        <#assign detail=changeProdPackage.prodPackageDetails[0] />
        <#if detail.prodProductBranch?? && detail.prodProductBranch.suppGoodsList??&& detail.prodProductBranch.suppGoodsList?size &gt; 0 >
            <#assign prodProductBranch=detail.prodProductBranch />
            <#assign suppGoods=detail.prodProductBranch.suppGoodsList[0]/>

        </#if>
        <#if changeProdPackage.prodPackageDetails?size gt 1 || haveChangeButton=='Y'>
        <p class="listName">
            <a href="javascript:void(0)" class="fr moreCategoryLineTransport"
               adultNum="${adultNum}"
               childNum="${childNum}"
               outProductId="${productId}"
               packageGroupId = "${detail.groupId}"
               packageProductId="${detail.prodProduct.productId}"
               packageProductBranchId="${detail.prodProductBranch.productBranchId}"
               selectedSuppGoodsId="${suppGoods.suppGoodsId}"

               toPackageGroupId="${detail.groupId}"
               toPackageProductId="${detail.prodProduct.productId}"
               toPackageProductBranchId="${detail.prodProductBranch.productBranchId}"
               toSelectedSuppGoodsId="${suppGoods.suppGoodsId}"
               toPrice="${prodProductBranch.adultPrice/2*adultNum + prodProductBranch.childPrice/2*childNum}"
               backPackageGroupId="${detail.groupId}"
               backPackageProductId="${detail.prodProduct.productId}"
               backPackageProductBranchId="${detail.prodProductBranch.productBranchId}"
               backSelectedSuppGoodsId="${suppGoods.suppGoodsId}"
               backPrice="${prodProductBranch.adultPrice/2*adultNum + prodProductBranch.childPrice/2*childNum}"
               quantity="${suppGoods.fitQuantity}"
               oldPrice="${prodProductBranch.adultPrice*adultNum + prodProductBranch.childPrice*childNum}"
               haveChangeButton="Y"
               transportType="${transportType}"
               groupSize = "${groupSize}"
               currentShow = "TOBACK"
               isFlight = "${isFlight}"
               toDate="${changeProdPackage.prodPackageGroupTransport.toStartDate}"
               backDate="${changeProdPackage.prodPackageGroupTransport.backStartDate}"
            >
                更多交通
            </a>
            <#if changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.toStartDate??>去程(${changeProdPackage.prodPackageGroupTransport.toStartPointDistrict.districtName}-${changeProdPackage.prodPackageGroupTransport.toDestinationDistrict.districtName})${changeProdPackage.prodPackageGroupTransport.toStartDate}日</#if>
            <#if changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.backStartDate??>返程(${changeProdPackage.prodPackageGroupTransport.backStartPointDistrict.districtName}-${changeProdPackage.prodPackageGroupTransport.backDestinationDistrict.districtName})${changeProdPackage.prodPackageGroupTransport.backStartDate}日</#if>
        </p>
        <#else>
        <p class="listName">
            <#if changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.toStartDate??>去程(${changeProdPackage.prodPackageGroupTransport.toStartPointDistrict.districtName}-${changeProdPackage.prodPackageGroupTransport.toDestinationDistrict.districtName})${changeProdPackage.prodPackageGroupTransport.toStartDate}日</#if>
            <#if changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.backStartDate??>返程(${changeProdPackage.prodPackageGroupTransport.backStartPointDistrict.districtName}-${changeProdPackage.prodPackageGroupTransport.backDestinationDistrict.districtName})${changeProdPackage.prodPackageGroupTransport.backStartDate}日</#if>
        </p>
        </#if>
    <div id="trafficInfoDiv">
        <#if detail.prodProduct.prodTrafficVO??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType=="FLIGHT">
            <@trafficFlightInfo.trafficFlightInfo detail changeProdPackage.prodPackageGroupTransport.toStartDate!"" changeProdPackage.prodPackageGroupTransport.backStartDate!"" />
        </#if>
		<#if detail.prodProduct.prodTrafficVO??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType=="TRAIN">
            <@trafficTrainInfo.trafficTrainInfo detail changeProdPackage.prodPackageGroupTransport.toStartDate!"" changeProdPackage.prodPackageGroupTransport.backStartDate!"" />
        </#if>
		<#if detail.prodProduct.prodTrafficVO??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType=="BUS">
            <@trafficBusInfo.trafficBusInfo detail changeProdPackage.prodPackageGroupTransport.toStartDate!"" changeProdPackage.prodPackageGroupTransport.backStartDate!"" />
        </#if>
		<#if detail.prodProduct.prodTrafficVO??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType??&&detail.prodProduct.prodTrafficVO.prodTraffic.toType=="SHIP">
            <@trafficBusInfo.trafficBusInfo detail changeProdPackage.prodPackageGroupTransport.toStartDate!"" changeProdPackage.prodPackageGroupTransport.backStartDate!"" />
        </#if>
    </div>
        <#if detail.prodProductBranch?? && detail.prodProductBranch.recommendSuppGoodsList??&& detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0 >
        <table width="100%" class="updateChangeTable">
            <tbody>
            <tr>
                <td>
                    <#assign prodProductBranch=detail.prodProductBranch />
                    <#if detail.prodProductBranch.recommendSuppGoodsList?? &&detail.prodProductBranch.recommendSuppGoodsList?size &gt; 0>
                        <#assign goods=detail.prodProductBranch.recommendSuppGoodsList[0] />

                        <table class="tab_nav" width="100%" >
                            <tr id="firstLineTransportShow${detail.groupId}" groupId="${detail.groupId}" productBranchId="${prodProductBranch.productBranchId}" adult="${adultNum}" child="${childNum}" totalAmount="${prodProductBranch.dailyLowestPrice}" class="table_nav" >
                                <td width="50%">
                                    <a class="pro_tit" href="javascript:;" desc="${prodProductBranch.branchName}"
                                       prodBranchId="${prodProductBranch.productBranchId}"
                                       onclick="openProduct(${detail.prodProduct.productId!''},
                                       ${detail.prodProduct.bizCategory.categoryId!''},
                                               '${detail.prodProduct.bizCategory.categoryName!''}')">${detail.prodProduct.productName}</a>
                                    <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.suppGoodsId}" autocomplete="off"/>
                                    <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" autocomplete="off"/>
                                    <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
                                    <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true id="${goods.suppGoodsId}TotalNum" class="w5 numText" />
                                    <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime" value="<#if changeProdPackage.prodPackageGroupTransport.toStartDate??>${changeProdPackage.prodPackageGroupTransport.toStartDate}<#else><#if changeProdPackage.prodPackageGroupTransport.backStartDate??>${changeProdPackage.prodPackageGroupTransport.backStartDate}</#if></#if>" autocomplete="off"/>
                                    <!--增加机票短信模板相关的信息(往返)--起-->
                                    <#assign prodTrafficGroup=detail.prodProduct.prodTrafficVO.prodTrafficGroupList[0] />
                                    <#if prodTrafficGroup??&&prodTrafficGroup.prodTrafficFlightList?size &gt; 0>
                                        <!--设定交通的去程和返程信息-->
                                        <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].toDate" value="${changeProdPackage.prodPackageGroupTransport.toStartDate}"/>
                                        <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].backDate" value="${changeProdPackage.prodPackageGroupTransport.backStartDate}"/>
                                        <#list prodTrafficGroup.prodTrafficFlightList as prodTrafficFlight>
                                            <#assign bizFlight=prodTrafficFlight.bizFlight/>
                                        <#--1代表去程，2代表返程，刚好可以用索引来表示-->
                                            <#assign roundTripFlightType=prodTrafficFlight_index+1/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].companyName" value="${bizFlight.airlineString}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].flightNo" value="${bizFlight.flightNo}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].planeCode" value="${bizFlight.airplaneString}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].flightType" value="${roundTripFlightType}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].flightTime" value="${bizFlight.flightTime}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].fromAirPort" value="${bizFlight.startAirportString}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].toAirPort" value="${bizFlight.arriveAirportString}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].fromCityName" value="${bizFlight.startDistrictString}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].toCityName" value="${bizFlight.arriveDistrictString}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].startTerminal" value="${bizFlight.startTerminal}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].arriveTerminal" value="${bizFlight.arriveTerminal}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].goTimeStr" value="${bizFlight.startTime}"/>
                                            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${prodTrafficFlight_index}].arriveTimeStr" value="${bizFlight.arriveTime}"/>
                                        </#list>
                                    </#if>
                                    <!--增加机票短信模板相关的信息(非对接往返)--止-->
                                </td>

                                <td width="20%" style="display:none">
                                    成人数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 adultNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" style="text-align:center" value="${goods.fitAdultQuantity}" required=true number=true /><br/>
                                    儿童数<input readOnly goodsId="${goods.suppGoodsId}" auditPrice="${goods.suppGoodsBaseTimePrice.auditPriceYuan}" childPrice="${goods.suppGoodsBaseTimePrice.childPriceYuan}" type="text" class="w5 childNumText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].childQuantity" style="text-align:center" value="${goods.fitChildQuantity}" required=true number=true />
                                    <input id="${goods.suppGoodsId}TotalNum" type="hidden" class="w5 numText" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" style="text-align:center" value="${goods.fitQuantity}" required=true number=true />
                                </td>
                                <td width="10%" class="orange">
              <#if "${prodProduct.bizCategoryId}"=="18" && "${prodProduct.subCategoryId}"=="182" && "${prodProduct.productType}"=="INNERLINE">                  
                                成人:￥${goods.suppGoodsBaseTimePrice.auditPriceYuan} X ${goods.fitAdultQuantity} <br>
                                儿童:￥${goods.suppGoodsBaseTimePrice.childPriceYuan} X ${goods.fitChildQuantity} <br>
              </#if>            
                                总价:￥${prodProductBranch.dailyLowestPriceYuan}元                  
                                </td>
                            </tr>
                            <tr>
                                <td colspan="3">
                                    <p class="descript">规格描述：<textarea class="textarea"></textarea></p>
                                </td>
                            </tr>
                        </table>

                    </#if>
                </td>
            </tr>
            </tbody>
        </table>
        </#if>
    </#if>
    <#if changeProdPackage??&&changeProdPackage.prodPackageDetails?size &gt; 0 &&changeProdPackage.jiPiaoDuiJieFlag=='Y'>
        <#assign detail=changeProdPackage.prodPackageDetails[0] />
        <#if detail.prodProductBranch?? && detail.prodProductBranch.suppGoodsList??&& detail.prodProductBranch.suppGoodsList?size &gt; 0 >
            <#assign prodProductBranch=detail.prodProductBranch />
            <#assign suppGoods=detail.prodProductBranch.suppGoodsList[0]/>
            <#assign goods=detail.prodProductBranch.suppGoodsMap[suppGoods.suppGoodsId+""]/>
        <div>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].goodsId" value="${goods.goodsId}" id="${goods.goodsId}goodsId" />
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].detailId" value="${detail.detailId}" id="${goods.goodsId}detailId" />
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" />
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].quantity" value="${adultNum+childNum}" />
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].visitTime"
                   value="<#if changeProdPackage.prodPackageGroupTransport.toStartDate??>${changeProdPackage.prodPackageGroupTransport.toStartDate}<#else><#if changeProdPackage.prodPackageGroupTransport.backStartDate??>${changeProdPackage.prodPackageGroupTransport.backStartDate}</#if></#if>"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultAmt" value="${goods.adultAmt}" id="${goods.goodsId}adultAmt" />
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].childAmt" value="${goods.childAmt}" id="${goods.goodsId}childAmt" />
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].adultQuantity" value="${adultNum}" />
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].childQuantity" value="${childNum}"/>

            <!--增加机票短信模板相关的信息(非对接单程+对接)--起-->
            <#if changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.toStartDate??>
                <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].toDate" value="${changeProdPackage.prodPackageGroupTransport.toStartDate}" id="${goods.goodsId}toDate"/>
                <#assign oneWayFlightType="1"/>
            <#elseif changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.backStartDate??>
                <#assign oneWayFlightType="2"/>
                <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].backDate" value="${changeProdPackage.prodPackageGroupTransport.backStartDate}" id="${goods.goodsId}backDate"/>
            </#if>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].companyName" value="${goods.companyName}" id="${goods.goodsId}additionalFlightNoVoList-companyName"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].flightNo" value="${goods.flightNo}" id="${goods.goodsId}additionalFlightNoVoList-flightNo"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].planeCode" value="${goods.planeCode}" id="${goods.goodsId}additionalFlightNoVoList-planeCode"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].flightType" value="${oneWayFlightType}" id="${goods.goodsId}additionalFlightNoVoList-flightType"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].fromAirPort" value="${goods.fromAirPort}" id="${goods.goodsId}additionalFlightNoVoList-fromAirPort"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].toAirPort" value="${goods.toAirPort}" id="${goods.goodsId}additionalFlightNoVoList-toAirPort"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].flyTime" value="${goods.flyTime}" id="${goods.goodsId}additionalFlightNoVoList-flyTime"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].fromCityName" value="${goods.fromCityName}" id="${goods.goodsId}additionalFlightNoVoList-fromCityName"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].toCityName" value="${goods.toCityName}" id="${goods.goodsId}additionalFlightNoVoList-toCityName"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].startTerminal" value="${goods.startTerminal}" id="${goods.goodsId}additionalFlightNoVoList-startTerminal"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].arriveTerminal" value="${goods.arriveTerminal}" id="${goods.goodsId}additionalFlightNoVoList-arriveTerminal"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].goTimeStr" value="${goods.goTime?string("HH:mm")}" id="${goods.goodsId}additionalFlightNoVoList-goTimeStr"/>
            <input type="hidden" name="productMap[${packageTourProductVo.productId}].itemList[${productItemIdIndex}].additionalFlightNoVoList[${0}].arriveTimeStr" value="${goods.arriveTime?string("HH:mm")}" id="${goods.goodsId}additionalFlightNoVoList-arriveTimeStr"/>
            <!--增加机票短信模板相关的信息(非对接单程+对接)--止-->
        </div>
        <!--新增线路默认机票---开始-->
        <div class="old_plane">
         <!--航班表格---结束-->
                <#if (ifMore == 'true' && flightMoreGoods=='Y')||( onlyOneDuiJieJiPiao?exists &&  onlyOneDuiJieJiPiao =="Y" && flightMoreGoods=='Y')><!--更换按钮开始-->
                <div class="adjust-traffic-item-status status" style="margin-right:40px;margin-top:-5px;margin-bottom:5px">
                    <div class="replace">
                        <td rowspan="2">
						    	  <span class="btn btn-mini btn-orange apiChangeTransprot"
                                        adultNum="${adultNum}"
                                        childNum="${childNum}"
                                        outProductId="${productId}"
                                      <#if toFlightParam?exists>
                                        toPackageGroupId="${toFlightParam.packageGroupId}"
                                        toPackageProductId="${toFlightParam.packageProductId}"
                                        toPackageProductBranchId="${toFlightParam.packageProductBranchId}"
                                        toSelectedSuppGoodsId="${toFlightParam.selectedSuppGoodsId}"
                                        toPrice="${toFlightParam.adultPrice*adultNum + toFlightParam.childPrice*childNum}"
                                      <#else>
                                        toPackageGroupId=""
                                        toPackageProductId=""
                                        toPackageProductBranchId=""
                                        toSelectedSuppGoodsId=""
                                        toPrice=""
                                      </#if>
                                      <#if backFlightParam?exists>
                                        backPackageGroupId="${backFlightParam.packageGroupId}"
                                        backPackageProductId="${backFlightParam.packageProductId}"
                                        backPackageProductBranchId="${backFlightParam.packageProductBranchId}"
                                        backSelectedSuppGoodsId="${backFlightParam.selectedSuppGoodsId}"
                                        backPrice="${backFlightParam.adultPrice*adultNum + backFlightParam.childPrice*childNum}"
                                      <#else>
                                        backPackageGroupId=""
                                        backPackageProductId=""
                                        backPackageProductBranchId=""
                                        backSelectedSuppGoodsId=""
                                        backPrice=""
                                      </#if>
                                        quantity="${suppGoods.fitQuantity}"
                                        haveChangeButton="Y"
                                        oldPrice="${goods.adultAmt*adultNum + childNum*goods.childAmt}"
                                        transportType="${transportType}"
                                        toDate="${changeProdPackage.prodPackageGroupTransport.toStartDate}"
                                        backDate="${changeProdPackage.prodPackageGroupTransport.backStartDate}"
                                  >更换交通</span>
                        </td>
                    </div>
                </div>
            </#if><!--更换按钮结束-->
            <!--航班表格---开始-->
            <table class="plane_table">
                <tr>
                    <td>
                        <#if changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.toStartDate??>
                            <span class="traffic-go-text-icon fl">去</span>
                            <span class="to_flightNo" style="display:none">${goods.flightNo}</span>
                            <span class="to_gotime" style="display:none">${goods.goTime?string('HH:mm')}</span>
                            <span class="${detail.groupId}_${goods.goodsId}visitTime">${changeProdPackage.prodPackageGroupTransport.toStartDate}</span>
                        </#if>
                        <#if changeProdPackage.prodPackageGroupTransport??&&changeProdPackage.prodPackageGroupTransport.backStartDate??>
                            <span class="traffic-go-text-icon fl">返</span>
                            <span class="back_flightNo" style="display:none">${goods.flightNo}</span>
                            <span class="back_gotime" style="display:none">${goods.goTime?string('HH:mm')}</span>
                            <span class="${detail.groupId}_${goods.goodsId}visitTime">${changeProdPackage.prodPackageGroupTransport.backStartDate}</span>
                        </#if>

                    <td>
                    <td width="150">
                        <div class="plane_name"><span class="${detail.groupId}_${goods.goodsId}companyName">${goods.companyName}</span></div>
                        <div class="plane_ban">
                                	<span class="${detail.groupId}_${goods.goodsId}flightNo">
                                    ${goods.flightNo}
                                	</span>
                                	<span class="plane_type" id="${detail.groupId}_${goods.goodsId}plane_type" table_td1="<#if goods.planeCode>${goods.planeCode}</#if>" table_td2="" table_td3="" table_td4="" table_td5="">
                                        <#if goods.planeCode?exists && goods.planeCode != 'null'>(${goods.planeCode})</#if>
                                	</span></div>
                    </td>
                    <td>
                        <ul class="qidi_box">
                            <li><span class="${detail.groupId}_${goods.goodsId}goTime">${goods.goTime?string('HH:mm')}</span><br><span class="${detail.groupId}_${goods.goodsId}fromAirPort">${goods.fromAirPort}</span></li>
                            <li><p><span class="${detail.groupId}_${goods.goodsId}qidi_zhuan"
                                <#if goods.flightNodeVoList && goods.flightNodeVoList.size &gt; 0>
                                         tip-content="<#list goods.flightNodeVoList as flightNode>${flightNode.city}(${flightNode.airport})<br></#list>"
                                </#if>
                            >


                                <#if goods.throughFlag>直飞
                                <#else>
                                    经停
                                    <#if goods.flightNodeVoList && goods.flightNodeVoList.size &gt; 0>
                                        (${goods.flightNodeVoList.size()})
                                    </#if>
                                </#if>
                                    	</span></p></li>
                            <li><span class="${detail.groupId}_${goods.goodsId}arriveTime">${goods.arriveTime?string('HH:mm')}</span><br><span class="${detail.groupId}_${goods.goodsId}toAirPort">${goods.toAirPort}</span></li>
                        </ul>
                    </td>
                    <td><span class="${detail.groupId}_${goods.goodsId}flyTime"><#if goods.flyTimeStr>${goods.flyTimeStr}<#else>--</#if></span></td>
                    <td><span class="${detail.groupId}_${goods.goodsId}seatName">${goods.seatName}</span></td>
                </tr>
            </table>
            <#if ifMore == 'false' && "${prodProduct.bizCategoryId}"=="18" && "${prodProduct.subCategoryId}"=="182" && "${prodProduct.productType}"=="INNERLINE">
                 <div style="width:100%;height:40px;">
                 <span id="adultChildPrice_duijie" class="orange adultChildPrice_duijie" style="float:right;margin-right:250px;"
                       adultNum="${adultNum}"
                       childNum="${childNum}"
                       <#if toFlightParam?exists>
                       toadultPrice="${toFlightParam.adultPrice}"
                       tochildPrice="${toFlightParam.childPrice}"
                       <#else>
                       toadultPrice="0"
                       tochildPrice="0"
                       </#if>
                       <#if backFlightParam?exists>
                       backadultPrice="${backFlightParam.adultPrice}"
                       backchildPrice="${backFlightParam.childPrice}"
                       <#else>
                       backadultPrice="0"
                       backchildPrice="0"
                       </#if>
                       >
                 </span>
                 </div>
            </#if>
        </div>
        <!--新增线路默认机票---结束-->
        </#if>
    </#if>
</#macro>