/**供应商促销处理*/
/*
 后台下单处理数组类型促销，数据格式：List<CtripHotelPromVo>;
 1.生成酒店详情页促销描述
 2.设置住几送几规则，送的天数，需要把价格修改为0
 */
function buildSupplierPromotions(goodsId,promotions) {
    if(!promotions)return;
    var hotelPromotion="";
    try {
        var hotelPromotion = JSON.parse(promotions);
    }
    catch(err){
        console.error("数据格式化失败："+err);
        return;
    }
    if (!!hotelPromotion){
        buildSupplierPromotion(goodsId,hotelPromotion);
    }
}
/*
 处理单个商品促销
 后台下单处理数组类型促销，数据格式：List<CtripHotelPromVo>;
 1.生成酒店详情页促销描述
 2.设置住几送几规则，送的天数，需要把价格修改为0
 */
function buildSupplierPromotion(goodsId,promotions) {
    var $goods = $("#room-"+goodsId);
    var $price = $("#id"+goodsId);
    if(!!promotions&&!!goodsId&&$goods.length===1&&$price.length===1){
        var tipContent="";
        for(var i in promotions) {
            var ctripHotelPromVo=promotions[i];
            if (!ctripHotelPromVo)
                continue;
            tipContent+=getLvmmOfferDescription(ctripHotelPromVo);
            if("NO1"===ctripHotelPromVo.promoteType&&!!ctripHotelPromVo.discountPattern&&ctripHotelPromVo.meet==true){//如果是住几送几促销
                $price.attr("discountPattern",ctripHotelPromVo.discountPattern);
            }
        }
        $goods.attr("promContent",tipContent);
    }
}
/**
 * 订单监控页面生成促销描述
 * @param goodsId
 * @param promotions
 */
function buildOfferDescriptionForStr(promotions) {
    if(!promotions)return;
    var hotelPromotion="";
    try {
        var hotelPromotion = JSON.parse(promotions);
    }
    catch(err){
        console.error("数据格式化失败："+err);
        return;
    }
    if (!!hotelPromotion){
        buildOfferDescription(hotelPromotion);
    }
}
/**
 * 订单监控页面生产促销描述
 * @param promotions
 */
function buildOfferDescription(promotions) {
    var $branchName=$("#branchName");
    if(!!promotions&&!!$branchName&&$branchName.length===1){
        var tipContent="";
        for(var i in promotions) {
            var ctripHotelPromVo=promotions[i];
            if (!ctripHotelPromVo)
                continue;
            tipContent+=getLvmmOfferDescriptionOrder(ctripHotelPromVo);
        }
        if(!!tipContent){
            $branchName.after('<tr><td class="e_label">携程促销订单：</td><td>'+tipContent+'</td></tr>');
        }
    }
}
/**
 * 驴妈妈自定义拼接促销描述--下单页
 * @param promotion
 */
function getLvmmOfferDescriptionOrder(promotion) {
    if (!promotion||!promotion.promoteType)
        return false;
    var reason=false;
    switch (promotion.promoteType){
        case "NO1":
            reason= strFormat("此房型需连住{}晚方可享受继续入住免费{}晚的优惠，如因提前离店，使得入住天数不能满足最低入住天数要求，则不能享受优惠，并需按原价支付，如实际入住天数不能包括免费入住的天数，则视为自动放弃该优惠。不可部分退。",[promotion.nightsRequired-(!promotion.nightsDiscounted?0:promotion.nightsDiscounted),promotion.nightsDiscounted]);break;
        case "NO2":
            var minAdvancedBookingStr=strFormat("提前{}天",[promotion.minAdvancedBookingOffset]);
            var maxAdvancedBookingStr=strFormat("入住前{}天内",[promotion.maxAdvancedBookingOffset]);
            var lastStr="在此时间内";
            if(!!minAdvancedBookingStr&&!maxAdvancedBookingStr){
                lastStr=minAdvancedBookingStr;
            }else if(!minAdvancedBookingStr&&!!maxAdvancedBookingStr){
                lastStr=maxAdvancedBookingStr;
            }
            reason=strFormat("此优惠房型需{}{}预订，如延住，也需{}预订。不可部分退。",[strFormat("提前{}天",[promotion.minAdvancedBookingOffset]),strFormat("{}入住前{}天内",[!!promotion.minAdvancedBookingOffset&&!!promotion.maxAdvancedBookingOffset?"且":"",promotion.maxAdvancedBookingOffset]),lastStr]);break;
        case "NO3":
            reason= strFormat("此优惠房型需在{}到{}{}时间段内预订，如延住也需在此时间段内预订。不可部分退。",[promotion.bookingStartDate,promotion.bookingEndDate,strFormat("每天{}到{}",[promotion.bookingStartTime,promotion.bookingEndTime])]);break;
        case "NO4":
            reason= strFormat("此优惠房型需至少入住{}间{}方可预订。不可部分退。",[promotion.minTotalOccupancy,strFormat("，最多不超过{}间",[promotion.maxTotalOccupancy])]);break;
        case "NO5":
            if(promotion.maxLOSMustBeMultiple==="true"){
                reason= strFormat("此优惠房型需连住{}晚方可预订，如因提前离店，使得入住天数不能满足最低入住天数要求，则需按原价支付。不可部分退。",[promotion.maxLOS]);
            }else if(!!promotion.minLOS){
                reason= strFormat("此优惠房型需连住{}晚{}方可预订{}。如因提前离店，使得入住天数不能满足入住天数要求，则未达到要求部分需按原价支付。不可部分退。",[promotion.minLOS,strFormat("或{}晚的倍数",[promotion.minLOSMustBeMultiple==="true"?promotion.minLOS:""]),strFormat("，最多不可超过{}晚",[promotion.maxLOS])]);
            }else {
                reason="";
            }
            break;
        case "NO6":
            reason= strFormat("此优惠房型需在{}到{}{}时间段内预订，优惠仅限当晚。不可部分退。",[promotion.bookingStartDate,promotion.bookingEndDate,strFormat("每天{}到{}",[promotion.bookingStartTime,promotion.bookingEndTime])]);break;
        default:reason=false;
    }
    return reason;
}

/**
 * 驴妈妈自定义拼接促销描述
 * @param promotion
 */
function getLvmmOfferDescription(promotion) {
    if (!promotion||!promotion.promoteType)
        return false;
    var reason=false;
    switch (promotion.promoteType){
        case "NO1":
            reason= strFormat("此房型需连住{}晚方可享受继续入住免费{}晚的优惠{}。",[promotion.nightsRequired-(!promotion.nightsDiscounted?0:promotion.nightsDiscounted),promotion.nightsDiscounted,strFormat("。且入住晚数需为{}晚的倍数",[promotion.mustBeMultiple==="true"?promotion.nightsRequired:""])]);break;
        case "NO2":
            reason=strFormat("此优惠房型需{}{}预订。",[strFormat("提前{}天",[promotion.minAdvancedBookingOffset]),strFormat("{}入住前{}天内",[!!promotion.minAdvancedBookingOffset&&!!promotion.maxAdvancedBookingOffset?"且":"",promotion.maxAdvancedBookingOffset])]);break;
        case "NO3":
            reason= strFormat("此优惠房型需在{}到{}{}时间段内预订。",[promotion.bookingStartDate,promotion.bookingEndDate,strFormat("每天{}到{}",[promotion.bookingStartTime,promotion.bookingEndTime])]);break;
        case "NO4":
            reason= strFormat("此优惠房型需至少入住{}间{}方可预订。",[promotion.minTotalOccupancy,strFormat("，最多不超过{}间",[promotion.maxTotalOccupancy])]);break;
        case "NO5":
            if(promotion.maxLOSMustBeMultiple==="true"){
                reason= strFormat("此优惠房型需连住{}晚方可预订。",[promotion.maxLOS]);
            }else if(!!promotion.minLOS){
                reason= strFormat("此优惠房型需连住{}晚{}方可预订{}。",[promotion.minLOS,strFormat("或{}晚的倍数",[promotion.minLOSMustBeMultiple==="true"?promotion.minLOS:""]),strFormat("，最多不可超过{}晚",[promotion.maxLOS])]);
            }else {
                reason="";
            }
            break;
        case "NO6":
            reason= strFormat("此优惠房型需在{}到{}{}时间段内预订。",[promotion.bookingStartDate,promotion.bookingEndDate,strFormat("每天{}到{}",[promotion.bookingStartTime,promotion.bookingEndTime])]);break;
        default:reason=false;
    }
    return reason;
}
/**
 * <p>Description: 促销不可用原因</p>
 */
function getUnAccessReason(promotion){
    if (!promotion||!promotion.promoteType)
        return false;
    var reason=false;
    switch (promotion.promoteType){
        case "NO1":
            reason= strFormat("此优惠房型需连住{}晚{}方可预订。",[promotion.nightsRequired,strFormat("或{}晚的倍数",[promotion.mustBeMultiple==="true"?promotion.nightsRequired:""])]);break;
        case "NO2":
            reason=strFormat("此优惠房型需{}{}预订。",[strFormat("提前{}天",[promotion.minAdvancedBookingOffset]),strFormat("{}入住前{}天内",[!!promotion.minAdvancedBookingOffset&&!!promotion.maxAdvancedBookingOffset?"且":"",promotion.maxAdvancedBookingOffset])]);break;
        case "NO3":
            reason= strFormat("此优惠房型需在{}到{}{}时间段内预订。",[promotion.bookingStartDate,promotion.bookingEndDate,strFormat("每天{}到{}",[promotion.bookingStartTime,promotion.bookingEndTime])]);break;
        case "NO4":
            reason= strFormat("此优惠房型需至少入住{}间{}方可预订。",[promotion.minTotalOccupancy,strFormat("，最多不超过{}间",[promotion.maxTotalOccupancy])]);break;
        case "NO5":
            if(promotion.maxLOSMustBeMultiple==="true"){
                reason= strFormat("此优惠房型需连住{}晚方可预订。",[promotion.maxLOS]);
            }else if(!!promotion.minLOS){
                reason= strFormat("此优惠房型需连住{}晚{}方可预订{}。",[promotion.minLOS,strFormat("或{}晚的倍数",[promotion.minLOSMustBeMultiple==="true"?promotion.minLOS:""]),strFormat("，最多不可超过{}晚",[promotion.maxLOS])]);
            }else {
                reason="";
            }
            break;
        case "NO6":
            reason= strFormat("此优惠房型需在{}到{}{}时间段内预订。",[promotion.bookingStartDate,promotion.bookingEndDate,strFormat("每天{}到{}",[promotion.bookingStartTime,promotion.bookingEndTime])]);break;
        default:reason=false;
    }
    return reason;
}
/**
 * <p>Description: 促销不可用原因,多种原因合并</p>
 */
function getUnAccessReasonLine(promotions) {
    var unAccessReason=new Array();//不满足促销条件的原因
    for(var i in promotions) {
        if (!(promotions[i]))
            continue;
        if(!promotions[i].meet){
            //不可订原因
            var unAccessReasonOne=getUnAccessReason(promotions[i]);
            if(unAccessReason)
                unAccessReason.push(unAccessReasonOne);
        }
    }
    return unAccessReason.join("");
}
/*
1.获取优惠规则描述
2.假如有预订几间以上优惠，修改房间最小数量
3，假如有住几送几，修改价格
*/
function getOfferDescriptionAndSet(promotions){
    if(!!promotions){
        var obj= new Object();
        obj.offerDescriptionNO4="";
        obj.offerDescription="";
        for(var i in promotions) {
            var ctripHotelPromotionVo= promotions[i];
            if (!ctripHotelPromotionVo)
                continue;
            if("NO4"==ctripHotelPromotionVo.promoteType){
                obj.offerDescriptionNO4=getLvmmOfferDescriptionOrder(ctripHotelPromotionVo);
                setMinQuantity($("#roomQuantity"),ctripHotelPromotionVo.minTotalOccupancy);//设置房间数量下拉框最大最小值
            }else{
                obj.offerDescription+=getLvmmOfferDescriptionOrder(ctripHotelPromotionVo);
            }
        }
        return obj;
    }
    return false;
}
/*
 设置促销描述信息
 设置价格监听，如果是住几送几，把免费房价格修改为0
 */
function updateOrderPageForCtripProm(promotions) {
    //1.设置促销描述信息
    var offerDescription=getOfferDescriptionAndSet(promotions);
    if(offerDescription){
        var description="";
        if(!!offerDescription.offerDescription){
            description+=offerDescription.offerDescription;
        }
        if(!!offerDescription.offerDescriptionNO4){
            description+=offerDescription.offerDescriptionNO4;
        }
        $("#offerDescription").remove();
        $("#orderInfoTable >tbody").prepend("<tr id='offerDescription'><td class='w6 e_label'>促销规则：</td><td><i class='e_icon icon-warn'></i>"+description+"</td></tr>")
    }
}
/**时间价格修改*/
function updatePriceTimePre(goodsId) {
    if(!goodsId)return;
    var discountPattern=$("#id"+goodsId).attr("discountPattern");
    if(!discountPattern)return;
    updatePriceTime(discountPattern);

}
/**
 * 修改时间价格
 * @param discountPattern
 * @returns {boolean}
 */
function updatePriceTime(discountPattern) {
    if(!discountPattern)return false;
    var priceSpan=$("#timePriceTb .cc7");
    if(!priceSpan||priceSpan.length==0)return false;
    if(priceSpan.length>=discountPattern.length){
        var multiple=parseInt(priceSpan.length/discountPattern.length);//取整
        for (var i=1;i<multiple;i++){
            discountPattern+=discountPattern;
        }
        for (var i=0,l=discountPattern.length;i<l;i++){
            if(discountPattern.charAt(i)==='1'){//修改时间价格
                $("#timePriceTb .cc7:eq("+i+")").html("0.00元");
            }
        }
    }
}
/*设置房间数量最下下拉值*/
function setMinQuantity($select,minQuantity) {
    if(!minQuantity)
        return false;
    if(!$select||$select.length===0)return;
    minQuantity=Number(minQuantity);
    var max=Number($select.find("option:last").val());
    var min=Number($select.find("option:first").val());
    if(max&&min){
        if(min<minQuantity&&max>minQuantity){
            $('#itemMap_quantity').val(minQuantity);
            for(var i=min;i<minQuantity;i++){
                $select.find("option[value='"+i+"']").remove();
            }
        }
    }
}
/**字符串格式化{}分离*/
function strFormat(str,args) {
    if(args.length===1){//当一个参数，并且为空时，直接返回，这里使用频率高
        if(!args[0]){
            return  "";
        }
    }
    var strs=str.split("{}");
    var length=strs.length;
    if(strs.length>args.length+1){
        return "";
    }
    var res=new Array();
    res.push(strs[0]);
    var isBlank=true;
    for (var i=1;i<length;i++){
        var arg=args[i-1];
        if(!!arg){
            isBlank=false;
            res.push(arg);
        }
        res.push(strs[i]);
    }
    return isBlank?"":res.join("");
}