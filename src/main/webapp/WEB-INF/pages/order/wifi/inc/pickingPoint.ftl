	<#if suppGoods.goodsType =='NOTICETYPE_DISPLAY'>
    <div class="order_box border_1">
            <p class="wifiOrderTit">取还件地址</p>
            <dl class="wifiOrderDl">
                <dt>取还城市</dt>
                <dd>
                  <div class="wfDetailCity">
                    <span class="wfDetailSelect" style="width:120px;"><b><#if city??>${city.districtName}<#else>请选取取件城市</#if></b><i class="arrow"></i></span>
                    <div class="wfDetailOption">
                        <div class="wfDetailOptionSub">
                        	<#if cityList?? && cityList?size gt 0 >
                        	<#list cityList as city>
                            <span cityId="${city.districtId!''}" goodId="${suppGoods.suppGoodsId}">${city.districtName!''}</span>
                            </#list>
                            </#if>
                        </div>
                    </div>
                    	  <div class="wifiTips order_box_new wfQHCity cityVaild" style="display:none;">
                          <div class="poptip tip-light poptip-default poptip-twentyDay">
                              <div class="tip-content">
                                  <p><span class="tip-icon tip-icon-error"></span>请选择取件网点。</p>
                              </div>
                          </div>
                      </div>
                </div>
                </dd>
            </dl>
            <div id="pickingPointtoBox">
            <dl class="wifiOrderDl backPickingPointto">
                <dt>取件网点</dt>
                <#if wifiPickingPointList?? && wifiPickingPointList?size gt 0>
                <#list wifiPickingPointList as pickingPoint>
                <dd class="wifiOrderDd">
                <input type="radio" pickType="qu" <#if city??&& pickingPoint_index == 0>checked="checked"</#if> pickingPointId =${pickingPoint.pickingPointId!''} piontType="${pickingPoint.pickingType!''}" name="itemMap[${suppGoods.suppGoodsId}].wifiAdditation.tackePickingPointId" value="${pickingPoint.pickingPointId!''}"/><p>${pickingPoint.pickingAddr!''}</p>
                </dd>
                </#list>
                </#if>
                <dd>
                    <a href="javascript:" title="" class="wfOrderTake">查看全部取件地址<i class="arrow"></i></a>
                </dd>
            </dl>
            <dl class="wifiOrderDl takepickingPointto">
                <dt>还件网点</dt>
                <#if wifiPickingPointList?? && wifiPickingPointList?size gt 0>
                <#list wifiPickingPointList as pickingPoint>
                <dd class="wifiOrderDd">
                <input type="radio" pickType="huan" <#if city?? && pickingPoint_index == 0>checked="checked"</#if> pickingPointId =${pickingPoint.pickingPointId!''} piontType="${pickingPoint.pickingType!''}" name="itemMap[${suppGoods.suppGoodsId}].wifiAdditation.backPickingPointId" value="${pickingPoint.pickingPointId!''}"/><p>${pickingPoint.pickingAddr!''}</p>
                </dd>
                </#list>
                </#if>
                <dd>
                    <a href="javascript:" title="" class="wfOrderTake">查看全部还件地址<i class="arrow"></i></a>
                </dd>
            </dl>
            </div>
        </div>
        <#elseif suppGoods.goodsType =='EXPRESSTYPE_DISPLAY'>
            <p class="wifiOrderTit">还件地址</p>
            <div id="pickingPointtoBox">
            <p class="wxts wifiWxts">温馨提示：WIFI使用完毕后，可以邮寄给以下任意一地址哦，选择后我们会给您发确认短信。</p>
            <dl class="wifiOrderDl backPickingPointto">
                <dt>还件网点</dt>
                <#if wifiPickingPointList?? && wifiPickingPointList?size gt 0>
                <#list wifiPickingPointList as pickingPoint>
                <dd class="wifiOrderDd">
                <input type="radio" pickType="huan" <#if pickingPoint_index == 0>checked="checked"</#if> pickingPointId =${pickingPoint.pickingPointId!''} piontType="${pickingPoint.pickingType!''}" name="itemMap[${suppGoods.suppGoodsId}].wifiAdditation.backPickingPointId" value="${pickingPoint.pickingPointId!''}"/><p>${pickingPoint.districtName} ：${pickingPoint.pickingAddr!''}</p>
                </dd>
                </#list>
                </#if>
                <dd>
                    <a href="javascript:" title="" class="wfOrderTake">查看全部还件地址<i class="arrow"></i></a>
                </dd>
            </dl>
            </div>
        </#if>
        
        
        
            <#if pointType = 'quhuan'>
            <dl class="wifiOrderDl takepickingPointto">
                <dt>取件网点</dt>
                <#if wifiPickingPointList?? && wifiPickingPointList?size gt 0>
                <#list wifiPickingPointList as pickingPoint>
                <dd class="wifiOrderDd">
                <input type="radio" <#if pickingPoint_index == 0>checked="checked"</#if> pickType="qu" pickingPointId =${pickingPoint.pickingPointId!''} piontType="${pickingPoint.pickingType!''}" name="itemMap[${suppGoodsId}].wifiAdditation.tackePickingPointId" value="${pickingPoint.pickingPointId!''}"/><p>${pickingPoint.pickingAddr!''}</p>
                </dd>
                </#list>
                </#if>
                <dd>
                    <a href="javascript:" title="" class="wfOrderTake">查看全部取件地址<i class="arrow"></i></a>
                </dd>
            </dl>
            <dl class="wifiOrderDl backPickingPointto">
                <dt>还件网点</dt>
                <#if wifiPickingPointList?? && wifiPickingPointList?size gt 0>
                <#list wifiPickingPointList as pickingPoint>
                <dd class="wifiOrderDd">
                <input type="radio" <#if pickingPoint_index == 0>checked="checked"</#if> pickType="huan" pickingPointId =${pickingPoint.pickingPointId!''} piontType="${pickingPoint.pickingType!''}" name="itemMap[${suppGoodsId}].wifiAdditation.backPickingPointId" value="${pickingPoint.pickingPointId!''}"/><p>${pickingPoint.pickingAddr!''}</p>
                </dd>
                </#list>
                </#if>
                <dd>
                    <a href="javascript:" title="" class="wfOrderTake">查看全部还件地址<i class="arrow"></i></a>
                </dd>
            </dl>
            </div>
            <#elseif pointType = 'huan'>
            <p class="wxts wifiWxts">温馨提示：WIFI使用完毕后，可以邮寄给以下任意一地址哦，选择后我们会给您发确认短信。</p>
            <dl class="wifiOrderDl backPickingPointto">
                <dt>还件网点</dt>
                <#if wifiPickingPointList?? && wifiPickingPointList?size gt 0>
                <#list wifiPickingPointList as pickingPoint>
                <dd class="wifiOrderDd">
                <input type="radio" pickType="huan" <#if pickingPoint_index == 0>checked="checked"</#if> pickingPointId =${pickingPoint.pickingPointId!''} piontType="${pickingPoint.pickingType!''}" name="itemMap[${suppGoodsId}].wifiAdditation.backPickingPointId" value="${pickingPoint.pickingPointId!''}"/><p>${pickingPoint.pickingAddr!''}</p>
                </dd>
                </#list>
                </#if>
                <dd>
                    <a href="javascript:" title="" class="wfOrderTake">查看全部还件地址<i class="arrow"></i></a>
                </dd>
            </dl>
            </#if>
        
        
