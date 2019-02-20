
<#if hotelList?exists && hotelList?size gt 0>
<#list hotelList as hotel>
<#if hotel['default_selected']??>
  							
  							<div class="hotel-tab" id="firstLineHotelShow${hotel['default_selected']['groupId']}">
						        <div class="hotel-tab-title">
						            <p class="hotel-title-left">
						                酒店 ———— <span >${hotel['default_selected']['check_in_year']}-${hotel['default_selected']['check_in_month']}-${hotel['default_selected']['check_in_day']}日</span>
						                到<span>${hotel['default_selected']['check_out_year']}-${hotel['default_selected']['check_out_month']}-${hotel['default_selected']['check_out_day']}日行程</span>
						            </p>
						            <p class="hotel-title-right">
						                总价：¥ <span class="priceYuan">${hotel['default_selected']['priceYuanFirst']}元</span>
						            </p>
						        </div>
						        <div class="hotel-content">
						            <div class="content-title">
						                <p class="content-title-left">
											<a class="pro_tit jingjiuhotelName" href="http://hotels.lvmama.com/hotel/${hotel['default_selected']['productId']}" target="_blank">${hotel['default_selected']['hotel_name']}</a>
										</p>
						                <#if hotel['haveChangeButton']?exists && hotel['haveChangeButton'] =="Y">
						                <p class="content-title-right" onclick="jingjiuopenChangeDiv(this)" adultNum="${adultNum}" childNum="${childNum}" outProductId="${productId}" packageGroupId="${hotel['default_selected']['groupId']}" packageProductId="${hotel['default_selected']['productId']}" packageProductBranchId="${hotel['default_selected']['currentProductBranchId']}" data-totalAmount="${hotel['default_selected']['defaultQuantity']}" data-selectedsuppgoodsid="${hotel['default_selected']['suppGoodsId']}"> 更换酒店&gt; </p>
						                </#if>
						            </div>
						            <div class="content-details" id="jingjiuhotel-details${hotel['default_selected']['groupId']}">
						                <ul class="details-title">
						                    <li class="col-xs-5">房型</li>
						                    <li class="col-xs-1">退改规则</li>
						                    <li class="col-xs-1">床型</li>
						                    <li class="col-xs-1">是否含早</li>
						                    <li class="col-xs-1">宽带</li>
						                    <li class="col-xs-1">差价</li>
						                    <li class="col-xs-1">间数</li>
						                    <li class="col-xs-1">选择</li>
						                </ul>
						                <input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].goodsId" value="${hotel['default_selected']['suppGoodsId']}" autocomplete="off"/>
			        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].detailId" value="${hotel['default_selected']['detailId']}" autocomplete="off"/>
			        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].routeRelation" value="PACK" autocomplete="off"/>
			        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].quantity" class="w5 numText"  style="text-align:center" value="${hotel['default_selected']['defaultQuantity']}" required=true number=true />
			        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].visitTime" value="${hotel['default_selected']['check_in']}" autocomplete="off"/>
			        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].hotelAdditation.arrivalTime" class="w5 numText"  style="text-align:center" value="14:00" required=true number=true />
			        					<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].hotelAdditation.leaveTime" class="w5 numText"  style="text-align:center" value="${hotel['default_selected']['check_out']}" required=true number=true />
				        				<input type="hidden" name="productMap[${productId}].itemList[${productItemIdIndex}].adultQuantity" goodsId="${hotel['default_selected']['suppGoodsId']}" auditPrice="${hotel['default_selected']['auditPriceYuan']}" childPrice="0" type="text" class="w5 hotelAdultNumText"  style="text-align:center" value="${hotel['default_selected']['defaultQuantity']}" required=true number=true />
						        				
						                <ul class="details-pro hotel-info">
						                    <li class="col-xs-5 room-type">
						                    <a href="javascript:;" title="${hotel['default_selected']['name']}"> ${hotel['default_selected']['name']}</i></a>
						                    </li>
						                    <li class="col-xs-1" title="${hotel['default_selected']['content']}">${hotel['default_selected']['type']}</li>
						                    <li class="col-xs-1" title="${hotel['default_selected']['bed_type_no_desc']}">${hotel['default_selected']['bed_type_no_desc']}</li>
						                    <li class="col-xs-1"><@showBreakFastType hotel['default_selected']['breakfast']/></li>
						                    <li class="col-xs-1">
						                    <#if hotel['default_selected']['internet_no_desc']>
									              <#if hotel['default_selected']['internet_no_desc'] == "收费">
										              <em tip-content="${hotel['default_selected']['internet_desc']}" class="room_td4_tag">
										                  ${hotel['default_selected']['internet_no_desc']}
										              </em>
									              <#else>
									                  ${hotel['default_selected']['internet_no_desc']}
									              </#if> 
								           </#if>
						                    </li>
						                    <li class="col-xs-1" id="gap-price">0</li>
						                    <li class="col-xs-1">
												<select id="hotelSelect_jingjiu" class="hotelSelect jingjiuhotelSelect" 
													goodsId=${hotel['default_selected']['suppGoodsId']}  
													data-class="selectbox-mini" tips="" 
													showGoodsName="${hotel['default_selected']['hotel_name']}-${hotel['default_selected']['name']}"
									                data-currentProductBranchId="${hotel['default_selected']['currentProductBranchId']}" 
									                data-type="hotel" 
									                data-groupId="${hotel['default_selected']['groupId']}"
									              	data-detailid="${hotel['default_selected']['detailId']}" 
									              	data-suppgoodsid="${hotel['default_selected']['suppGoodsId']}"
									              	goodsPrice=${hotel['default_selected']['priceYuan']} 
									              	data-productItemIdIndex=${productItemIdIndex} 
									              	data-productId=${productId}
									              	style="width:50px;">
									              	 <#if hotel['default_selected']['selectNumList']?exists && hotel['default_selected']['selectNumList']?size gt 0>
													 <#list hotel['default_selected']['selectNumList'] as selectNum>
									                 <option value="${selectNum}">${selectNum}</option>
									                 </#list>
									                 <#else>
									                 <option value='0'>0</option>
									                 </#if>
									              </select>
						                    </li>
						                    <li class="col-xs-1">
						                        <p class="btn btn-default btn-sm">已选</p>
						                    </li>
						                    <li class="col-xs-12">
						                        <div class="room-tip">
						                            <div class="tip-arrow tip-arrow-11">
						                                <em>◆</em>
						                                <i>◆</i>
						                            </div>
						                            <p>
						                              <#if hotel['default_selected']['area']>面积：${hotel['default_selected']['area']}㎡<br/></#if>
										              <#if hotel['default_selected']['floor']>楼层：${hotel['default_selected']['floor']}<br/></#if> 
										              <#if hotel['default_selected']['add_bed_flag']>是否可加床：${hotel['default_selected']['add_bed_flag']}<br/></#if>
										              <#if hotel['default_selected']['window']>是否有窗：${hotel['default_selected']['window']}<br/></#if>
										              <#if hotel['default_selected']['smokeless_room']>无烟房：${hotel['default_selected']['smokeless_room']}<br/></#if>
										              <#if hotel['default_selected']['maxVisitor']>最多入住人数：${hotel['default_selected']['maxVisitor']}人<br/></#if>    
						                            </p>
						                        </div>
						                    </li>
						                </ul>
				    					<#if hotel['others']?exists && hotel['others']?size gt 0>
    									<#list hotel['others'] as otherHotel>       
						                <ul class="details-pro hotel-info" <#if otherHotel_index gt 1>style=" display:none;"</#if> >
						                   <li class="col-xs-5 room-type">
						                    <a href="javascript:;" title="${otherHotel['name']}"> ${otherHotel['name']}</i></a>
						                    </li>
						                    <li class="col-xs-1" title="${otherHotel['content']}">${otherHotel['type']}</li>
						                    <li class="col-xs-1" title="${otherHotel['bed_type_no_desc']}">${otherHotel['bed_type_no_desc']}</li>
						                    <li class="col-xs-1"><@showBreakFastType otherHotel['breakfast']/></li>
						                    <li class="col-xs-1">
						                    <#if otherHotel['internet_no_desc']>
									              <#if otherHotel['internet_no_desc'] == "收费">
										              <em tip-content="${otherHotel['internet_desc']}" class="room_td4_tag">
										                  ${otherHotel['internet_no_desc']}
										              </em>
									              <#else>
									                  ${otherHotel['internet_no_desc']}
									              </#if> 
								           </#if>
						                    </li>
						                    <li class="col-xs-1" id="gap-price">0</li>
						                    <li class="col-xs-1">
												<select id="hotelSelect_jingjiu" class="jingjiuhotelSelect"
													goodsId=${otherHotel['suppGoodsId']}  data-class="selectbox-mini" tips="" showGoodsName="${otherHotel['hotel_name']}-${otherHotel['name']}"
									                data-currentProductBranchId="${otherHotel['currentProductBranchId']}" data-type="hotel" data-groupId="${otherHotel['groupId']}"
									              	data-detailid="${otherHotel['detailId']}" data-suppgoodsid="${otherHotel['suppGoodsId']}"
									              	 goodsPrice=${otherHotel['priceYuan']} data-productItemIdIndex=${productItemIdIndex} data-productId=${productId}  style="width:50px;">
									              	 <#if otherHotel['selectNumList']?exists && otherHotel['selectNumList']?size gt 0>
													 <#list otherHotel['selectNumList'] as selectNum>
									                 <option value="${selectNum}">${selectNum}</option>
									                 </#list>
									                 <#else>
									                 <option value='0'>0</option>
									                 </#if>
									              </select>
						                    </li>
						                    <li class="col-xs-1">
						                        <p class="btn btn-primary-hotel btn-sm">选择</p>
						                    </li>
						                    <li class="col-xs-12">
						                        <div class="room-tip">
						                            <div class="tip-arrow tip-arrow-11">
						                                <em>◆</em>
						                                <i>◆</i>
						                            </div>
						                            <p>
						                              <#if otherHotel['area']>面积：${otherHotel['area']}㎡<br/></#if>
										              <#if otherHotel['floor']>楼层：${otherHotel['floor']}<br/></#if> 
										              <#if otherHotel['add_bed_flag']>是否可加床：${otherHotel['add_bed_flag']}<br/></#if>
										              <#if otherHotel['window']>是否有窗：${otherHotel['window']}<br/></#if>
										              <#if otherHotel['smokeless_room']>无烟房：${otherHotel['smokeless_room']}<br/></#if>
													  <#if otherHotel['maxVisitor']>最多入住人数：${otherHotel['maxVisitor']}人</#if> 
						                            </p>
						                        </div>
						                    </li>
						                </ul>
						                </#list>
										   <#if hotel['others']?size gt 2>                           
								                <div class="details-more">
								                    <p>展开其他房型<span></span></p>
								                </div>
										   </#if>
									   </#if>
						            </div>
						        </div>
						    </div>
						    <#assign productItemIdIndex=productItemIdIndex+1 /> 
		        			<script>
								productItemIdIndex='${productItemIdIndex}';	       
	    					</script>
</#if>
</#list>
</#if>
    <script src="http://pic.lvmama.com/min/index.php?f=/js/v5/modules/pandora-poptip.js"></script>
    <script src="/vst_order/js/book/linehotel_jingjiu.js?version=2017082212345"></script>  
    <script>
        $(function () {
            var uls = $('.content-details ul');
            var more = $('.details-more');
            var moreContent = more.find('p');
            var roomLi = uls.find('.room-type');

            more.click(function () {
                if(!$(this).hasClass('spread')){
	            	$(this).parent().find('ul:gt(3)').show();
	                moreContent.html('收起其他房型<span></span>');
	                $(this).addClass('spread')
	                more.find('span').addClass('active')
	            }else {
	            	$(this).parent().find('ul:gt(3)').hide()
	                moreContent.html('展开其他房型<span></span>');
	                more.find('span').removeClass('active');
	                $(this).removeClass('spread')
	            }
            });
            roomLi.on('click',function () {
                var tipBox = $(this).parent().find('.room-tip');
                if(tipBox.is(':visible')){
                    tipBox.hide()
                }else {
                    tipBox.show()
                }
            })
        })
    </script>
    <link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/tip.css">
    <style>
        .tip-arrow {
            top: -6px; }
        .tip-arrow i {
            color: #fff; }

        .hotel-tab {
            font-size: 14px; }
        .hotel-tab p {
            margin: 0; }
        .hotel-tab li {
            list-style: none; }
        .hotel-tab li .btn {
            padding: 0px 10px; }
        .hotel-tab ul {
            margin: 0; }
        .hotel-tab .hotel-tab-title {
            overflow: hidden;
            height: 35px;
            line-height: 35px;
            padding: 0 8px;
            color: #fff;
            background: #538ed7; }
        .hotel-tab .hotel-tab-title .hotel-title-left {
            float: left; }
        .hotel-tab .hotel-tab-title .hotel-title-right {
            float: right; }
        .hotel-tab .hotel-content {
            padding: 20px;
            min-width: 900px; }
        .hotel-tab .hotel-content .content-title {
            overflow: hidden;
            margin-bottom: 10px; }
        .hotel-tab .hotel-content .content-title .content-title-left {
            float: left; }
        .hotel-tab .hotel-content .content-title .content-title-right {
            float: right;
            color: #08c; }
        .hotel-tab .hotel-content .content-title .content-title-right:hover {
            color: #f60;
            cursor: pointer; }
        .hotel-tab .hotel-content .content-details .details-title {
            background-color: #f0f0f0;
            color: #666;
            overflow: hidden;
            padding: 10px; }
        .hotel-tab .hotel-content .content-details .details-title li {
            float: left; }
        .hotel-tab .hotel-content .content-details .details-pro {
            border-bottom: 1px dashed #ccc;
            overflow: hidden;
            padding: 10px; }
        .hotel-tab .hotel-content .content-details .details-pro .room-type {
            padding-right: 20px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            cursor: pointer; }
        .hotel-tab .hotel-content .content-details .details-pro .room-type:hover {
            color: #f90; }
        .hotel-tab .hotel-content .content-details .details-pro .room-tip {
            display: none;
            position: relative;
            background-color: #fff;
            min-height: 50px;
            padding: 10px;
            border: 1px solid #ccc;
            margin-top: 10px; }
        .hotel-tab .hotel-content .content-details li {
            padding: 0; }
        .hotel-tab .hotel-content .content-details .details-more {
            overflow: hidden;
            padding: 10px; }
        .hotel-tab .hotel-content .content-details .details-more p {
            float: right;
            cursor: pointer;
            transition: all 0.3s; }
        .hotel-tab .hotel-content .content-details .details-more p:hover {
            color: #f60; }
        .hotel-tab .hotel-content .content-details .details-more p:hover span {
            background: url("imgs/icon-arrow-hover.png"); }
        .hotel-tab .hotel-content .content-details .details-more span {
            float: right;
            background: url("imgs/icon-arrow.png");
            height: 6px;
            width: 9px;
            margin-top: 8px;
            margin-left: 4px; }
        .hotel-tab .hotel-content .content-details .details-more span.active {
            transform: rotateX(180deg); }
         .btn-primary-hotel {
		    color: #fff;
		    background-color: #337ab7;
		    border-color: #2e6da4;
			}

        /*# sourceMappingURL=index.css.map */

    </style>

<#macro showBreakFastType breakfast>
    <#switch breakfast>
        <#case 1>
        单早
            <#break>
        <#case 2>
        双早
            <#break>
        <#case 3>
        三早
            <#break>
        <#case 4>
        四早
            <#break>
        <#case 5>
        五早
            <#break>
        <#case 6>
        六早
            <#break>
        <#case 7>
        七早
            <#break>
        <#case 8>
        八早
            <#break>
        <#case 9>
        九早
            <#break>
        <#case 10>
        十早
            <#break>
        <#case 11>
        十一早
            <#break>
        <#case 12>
        十二早
            <#break>
        <#case 13>
        十三早
            <#break>
        <#case 14>
        十四早
            <#break>
        <#case 15>
        十五早
            <#break>
        <#case 16>
        十六早
            <#break>
        <#case 17>
        十七早
            <#break>
        <#case 18>
        十八早
            <#break>
        <#case 19>
        十九早
            <#break>
        <#case 20>
        二十早
            <#break>
        <#default>
        无早
    </#switch>
</#macro>