        <div class="hotel_line_wrap" style="height:50px">
            <div class="hotel_line_tab" style="float: left;width:100%" >

				<#if prodProduct.muiltDpartureFlag?? && 'Y'==prodProduct.muiltDpartureFlag>
                    <span style="float:left"">出发地：</span>
                    <div class="selectSimu" style="float:left;margin:0 20px 0 0">
                        <p class="select-info like-input" style="margin:0 0 0 0;height:28px;">
                            <span class="select-arrow"><i class="ui-arrow-bottom dark-ui-arrow-bottom"></i></span>
                            <#if startDistrictVo?? && startDistrictVo.startDistrict>
                                <span id="queryStartDistrictId" class="select-value" data-cityid="${startDistrictVo.startDistrict.districtId}">${startDistrictVo.startDistrict.districtName}</span>
                            <#else>
                                <span id="queryStartDistrictId" class="select-value" data-cityid="">更多出发地</span>
                            </#if>
                        </p>
                        <div name="multi_startdistrict_opts" class="selectSimu-opt">
                            <div class="selectSimu-optTab">
                                <span class="active">ABCD<i class="lv_icon icon_TabArr"></i></span>
                                <span>EFGH<i class="lv_icon icon_TabArr"></i></span>
                                <span>IJKL<i class="lv_icon icon_TabArr"></i></span>
                                <span>MNOP<i class="lv_icon icon_TabArr"></i></span>
                                <span>QRST<i class="lv_icon icon_TabArr"></i></span>
                                <span style="margin:0;">UVWXYZ<i class="lv_icon icon_TabArr"></i></span>
                            </div>
                            <#if prodProduct.bizDistricts??>
                            <div class="selectSimu-optCon active">
                                <#list prodProduct.bizDistricts as oneDistrict>
                                    <#if oneDistrict?? && oneDistrict.shortPinyin??>
                                        <#if oneDistrict.shortPinyin?starts_with("a") || oneDistrict.shortPinyin?starts_with("b") || oneDistrict.shortPinyin?starts_with("c") || oneDistrict.shortPinyin?starts_with("d")>
                                            <#if oneDistrict.cancelFlag?? && oneDistrict.cancelFlag == 'N' >
												<span title="已售完">${oneDistrict.districtName}</span>
											<#else>
												<a data-cityid="${oneDistrict.districtId}" href="#">${oneDistrict.districtName}</a>
											</#if>
                                        </#if>
                                    </#if>
                                </#list>
                            </div>
                            <div class="selectSimu-optCon">
                                <#list prodProduct.bizDistricts as oneDistrict>
                                    <#if oneDistrict?? && oneDistrict.shortPinyin??>
				    					<#if oneDistrict.shortPinyin?starts_with("e") || oneDistrict.shortPinyin?starts_with("f") || oneDistrict.shortPinyin?starts_with("g") || oneDistrict.shortPinyin?starts_with("h")>
                                            <#if oneDistrict.cancelFlag?? && oneDistrict.cancelFlag == 'N' >
												<span title="已售完">${oneDistrict.districtName}</span>
											<#else>
												<a data-cityid="${oneDistrict.districtId}" href="#">${oneDistrict.districtName}</a>
											</#if>
										</#if>
                                    </#if>
                                </#list>
                            </div>
                            <div class="selectSimu-optCon">
                                <#list prodProduct.bizDistricts as oneDistrict>
                                    <#if oneDistrict?? && oneDistrict.shortPinyin??>
				     					<#if oneDistrict.shortPinyin?starts_with("i") || oneDistrict.shortPinyin?starts_with("j") || oneDistrict.shortPinyin?starts_with("k") || oneDistrict.shortPinyin?starts_with("l")>
                                            <#if oneDistrict.cancelFlag?? && oneDistrict.cancelFlag == 'N' >
												<span title="已售完">${oneDistrict.districtName}</span>
											<#else>
												<a data-cityid="${oneDistrict.districtId}" href="#">${oneDistrict.districtName}</a>
											</#if>
										</#if>
                                    </#if>
                                </#list>
                            </div>
                            <div class="selectSimu-optCon">
                                <#list prodProduct.bizDistricts as oneDistrict>
                                    <#if oneDistrict?? && oneDistrict.shortPinyin??>
				        				<#if oneDistrict.shortPinyin?starts_with("m") || oneDistrict.shortPinyin?starts_with("n") || oneDistrict.shortPinyin?starts_with("o") || oneDistrict.shortPinyin?starts_with("p")>
                                            <#if oneDistrict.cancelFlag?? && oneDistrict.cancelFlag == 'N' >
												<span title="已售完">${oneDistrict.districtName}</span>
											<#else>
												<a data-cityid="${oneDistrict.districtId}" href="#">${oneDistrict.districtName}</a>
											</#if>
										</#if>
                                    </#if>
                                </#list>
                            </div>
                            <div class="selectSimu-optCon">
                                <#list prodProduct.bizDistricts as oneDistrict>
                                    <#if oneDistrict?? && oneDistrict.shortPinyin??>
					  					<#if oneDistrict.shortPinyin?starts_with("q") || oneDistrict.shortPinyin?starts_with("r") || oneDistrict.shortPinyin?starts_with("s") || oneDistrict.shortPinyin?starts_with("t")>
                                            <#if oneDistrict.cancelFlag?? && oneDistrict.cancelFlag == 'N' >
												<span title="已售完">${oneDistrict.districtName}</span>
											<#else>
												<a data-cityid="${oneDistrict.districtId}" href="#">${oneDistrict.districtName}</a>
											</#if>
										</#if>
                                    </#if>
                                </#list>
                            </div>
                            <div class="selectSimu-optCon">
                                <#list prodProduct.bizDistricts as oneDistrict>
                                    <#if oneDistrict?? && oneDistrict.shortPinyin??>
                                        <#if oneDistrict.shortPinyin?starts_with("u") || oneDistrict.shortPinyin?starts_with("v") || oneDistrict.shortPinyin?starts_with("w") || oneDistrict.shortPinyin?starts_with("x") || oneDistrict.shortPinyin?starts_with("y") || oneDistrict.shortPinyin?starts_with("z")>
                                            <#if oneDistrict.cancelFlag?? && oneDistrict.cancelFlag == 'N' >
												<span title="已售完">${oneDistrict.districtName}</span>
											<#else>
												<a data-cityid="${oneDistrict.districtId}" href="#">${oneDistrict.districtName}</a>
											</#if>
										</#if>
                                    </#if>
                                </#list>
                            </div>
                            </#if>
                        </div>
                    </div>
				</#if>

		<input type="hidden" name="hasApiFlight" value="${hasApiFlight}" />
                    请选择游玩日期：
                    <input id="visitTime" class="input js_youwanTime" type="text" maxlength="10" name="visitTime"
                           value="${specDate?string("yyyy-MM-dd")}" readonly="readonly" placeholder="请选择游玩日期">
                <#if prodLineRoute && prodLineRoute.routeName >
                    &nbsp;&nbsp;${prodLineRoute.routeName}&nbsp;&nbsp;
                </#if>
                    <input type="hidden" id="adultNumValue" name="adultNumValue" value="${adultNum}">
                    <input type="hidden" id="childNumValue" name="childNumValue" value="${childNum}">
                    <input type="hidden" id="saleCopiesFlag"   value="${saleCopies}"/>
                    成人数：
                <#if adultSelect?? && adultSelect?length &gt; 0>
                    <select id="adultNum">
                    ${adultSelect}
                    </select>
                <#else>
                    <select id="adultNum">
                        <#list 1..100 as counter>
                            <option>${counter}</option>
                        </#list>
                    </select>
                </#if>
                    &nbsp;&nbsp;
                    儿童数：
                <#if childMinQuantity && childMinQuantity &lt; 0>
                    <select id="childNum" disabled="disabled">
                        <option value='0'>0</option>
                    </select>
                <#else>
                    <#if childSelect?? && childSelect?length &gt; 0>
                        <select id="childNum">
                        ${childSelect}
                        </select>
                    <#else>
                        <select id="childNum">
                            <#list 0..100 as counter>
                                <option>${counter}</option>
                            </#list>
                        </select>
                    </#if>
                </#if>
                    <span><a href="javascript:;" class="btn btn_ccproductdetail">查询</a></span>
            </div>            
        </div>