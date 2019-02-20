$(function(){
	clicking();
	$(".hotel-tab").each(function(){
		refreshHotelGapPrice($(this), $(this).find(".hotel-info")[0]);
	});
});

function changeHotel(hotelSelect){
	var hotelTab = hotelSelect.parents(".hotel-tab");
	hotelTab.find(".btn-sm").each(function(){
		if ( $(this).hasClass("btn-default") ){
			$(this).addClass("btn-primary-hotel");
			$(this).removeClass("btn-default");
			$(this).html("选择");
		}
	});
	hotelSelect.parent().next().find(".btn-sm").addClass("btn-default").removeClass("btn-primary-hotel").html("已选");
	hotelTab.find(".jingjiuhotelSelect").each(function(){
		if ( $(this).hasClass("hotelSelect") ){
			$(this).removeClass("hotelSelect");
		}
	});
	hotelSelect.addClass("hotelSelect");
	clicking();
	
	var goodsPrice=hotelSelect.attr("goodsPrice");
	var totalPrice=parseFloat(goodsPrice)*parseInt(hotelSelect.val());
	hotelSelect.find("option[value="+hotelSelect.val()+"]").attr("selected",true);
	hotelTab.find(".priceYuan").html(totalPrice.toFixed(2)+"元");
	
	var suppGoodsId=hotelSelect.attr("data-suppgoodsid");
	var thisProductId = hotelSelect.attr("data-productId");
	var productItemIdIndex = hotelSelect.attr("data-productItemIdIndex");
	var detailId = hotelSelect.attr("data-detailid");
	hotelTab.find("[name='productMap[" + thisProductId + "].itemList[" + productItemIdIndex + "].quantity']").val(hotelSelect.val());
	hotelTab.find("[name='productMap[" + thisProductId + "].itemList[" + productItemIdIndex + "].adultQuantity']").val(hotelSelect.val());
	hotelTab.find("[name='productMap[" + thisProductId + "].itemList[" + productItemIdIndex + "].goodsId']").val(suppGoodsId);
    hotelTab.find("[name='productMap[" + thisProductId + "].itemList[" + productItemIdIndex + "].detailId']").val(detailId);
	hotelTab.find(".content-title-right").attr("data-selectedsuppgoodsid",suppGoodsId);
	hotelTab.find(".content-title-right").attr("data-totalAmount",hotelSelect.val());
	
	refreshHotelGapPrice(hotelTab, hotelSelect.parents(".hotel-info"));
	try{
		setTimeout(changeNextSelect(null,null),500);
	}catch(err){}
	countTotalPrice();
	
}

function clicking(){
	$(".btn-primary-hotel").click(function(){
        var hotelSelect = $(this).parent().parent().find(".jingjiuhotelSelect");
        changeHotel(hotelSelect);
        
	});
	$("#hotelSelect_jingjiu").live("change",function(){
		if ( $(this).hasClass("hotelSelect") ){
	  		var goodsPrice=$(this).attr("goodsPrice");
	  		var totalPrice=parseFloat(goodsPrice)*parseInt($(this).val());
	  		$(this).find("option[value="+$(this).val()+"]").attr("selected",true);
	  		
			$(this).parents(".hotel-tab").find(".priceYuan").html(totalPrice.toFixed(2)+"元");
			var thisProductId = $(this).attr("data-productId");
			var productItemIdIndex = $(this).attr("data-productItemIdIndex");
			$(this).parents(".hotel-tab").find("[name='productMap[" + thisProductId + "].itemList[" + productItemIdIndex + "].quantity']").val($(this).val());
			$(this).parents(".hotel-tab").find("[name='productMap[" + thisProductId + "].itemList[" + productItemIdIndex + "].adultQuantity']").val($(this).val());
			$(this).parents(".hotel-tab").find(".content-title-right").attr("data-totalAmount",$(this).val());
			refreshHotelGapPrice($(this).parents(".hotel-tab"), $(this).parents(".hotel-info"));
			try{
	  			setTimeout(changeNextSelect(null,null),500);
			}catch(err){}
	  		countTotalPrice();
		}else{
			changeHotel($(this)); 
		}
	});
}

//计算差价
function refreshHotelGapPrice(thisAdjustHotel, houseTypeItem){
    var selectObj = $(houseTypeItem).find(".jingjiuhotelSelect");
    //首先取到已选房型的价格
    var selectPrice = selectObj.attr("goodsPrice")*selectObj.val();
    var currentSuppGoodsId = selectObj.attr("data-suppgoodsid");
    //已选商品隐藏价格
    $(houseTypeItem).find('#gap-price').html("0");
    //取所有未选商品的价格
    thisAdjustHotel.find(".hotel-info").each(function(){
        var $thisSelect = $(this).find(".jingjiuhotelSelect");
        var goodsId = $thisSelect.attr("data-suppgoodsid");
        if(goodsId != currentSuppGoodsId){
            var doSelectPrice =  $thisSelect.val()*$thisSelect.attr("goodsPrice");
            var price = doSelectPrice-selectPrice;
            if(price%100 == 0){
                price = price.toFixed(0);
            }else{
                price = price.toFixed(2);
            }
            if(price==0){
                price = "0";
            }else if(price>0) {
                price = "+" + price;
            }
            $(this).find("#gap-price").html(price);
        }
    });
}

/**
 * 更换商品窗口操作
 * 
 * @param $this
 * @param pageFlag
 */
function jingjiuopenChangeDiv(tab){
	$this = $(tab);
	var url="/vst_order/ord/order/queryPackageMoreProduct.do";

	var productId=$("#productId").val();
	var outProductId=$this.attr("outProductId");
	var packageGroupId=$this.attr("packageGroupId");
	
	var groupId=$this.attr("packageGroupId");
	var packageProductId=$this.attr("packageProductId");
	var currentProductBranchId=$this.attr("packageProductBranchId");
	var currentSuppgoodsId=$this.attr("data-selectedsuppgoodsid");
	var selectedSuppGoodsId=$this.attr("selectedSuppGoodsId");
	var toPrice = $this.attr("toPrice");//交通去程价格
	
	var adultQuantity=$this.attr("adultNum");
	var childQuantity=$this.attr("childNum"); 
	var firstShowName=getFirstTrName("linehotel");
	var packageProductBranchId=currentProductBranchId;
	var quantity=$this.attr("quantity");
	var $firstShowTr = $(firstShowName+groupId);
	var totalAmount = $firstShowTr.attr("totalAmount");
	var haveChangeButton=$this.attr("haveChangeButton");
	var oldPrice=$this.attr("oldPrice");
	var backPrice = $this.attr("backPrice");//交通返程价格
	var specDate = $("#visitTime").val();
	if(!specDate) {
		specDate = $firstShowTr.find("input[name$='.visitTime']").val();
	}
	$("input[type=hidden][name=userId]").val(book_user_id);	
	var selectedDate = $firstShowTr.find("input[name$='.visitTime']").val();
	
	url=url+"?selectedDate=" + selectedDate + "&specDate="+specDate+"&productId="+productId
	+"&packageProductId="+packageProductId+"&groupId="+groupId
	+"&currentProductBranchId="+currentProductBranchId
	+"&currentSuppgoodsId="+currentSuppgoodsId
	+"&adultQuantity="+adultQuantity+"&childQuantity="+childQuantity
	+"&toPrice="+toPrice
	+"&backPrice="+backPrice
	+"&pageFlag=changeHotelJingjiu&productItemIdIndex="+productItemIdIndex+"&totalAmount="+$this.attr("data-totalAmount");
	
	var jStartDistrict = $("#queryStartDistrictId");
	if(jStartDistrict && jStartDistrict.length>0){
		url += "&startDistrictId="+jStartDistrict.attr("data-cityid");
	}

	var dialog;
	dialog = new xDialog(url,null,
		      {title:"更多可换酒店",width:1000});
	
	refreshHotelGapPrice($("#jingjiuhotelchangeTab"), $("#jingjiuhotelchangeTab").find(".btn-default").parents(".hotel-info"));
	
	$(".changehotelSelect").live("change",function(){
		refreshHotelGapPrice($("#jingjiuhotelchangeTab"), $("#jingjiuhotelchangeTab").find(".btn-default").parents(".hotel-info"));
	});
	
	$(".jingjiu-btn-hotel-change").click(function(){
		var hotelSelect = $(this).parent().prev().find(".jingjiuhotelSelect");
		var hotelTab = $(this).parents(".hotel-tab");
		hotelTab.find(".btn-sm").each(function(){
			if ( $(this).hasClass("btn-default") ){
				$(this).addClass("btn-primary-hotel");
				$(this).removeClass("btn-default");
				$(this).html("选择");
			}
		});
		$(this).addClass("btn-default").removeClass("btn-primary-hotel").removeClass("jingjiu-btn-hotel-change").html("已选");
		
		hotelTab.find(".jingjiuhotelSelect").each(function(){
			$(this).removeClass("changehotelSelect");
			$(this).attr("id","hotelSelect_jingjiu");
			$(this).find("option[selected]").attr("selected",false);
		});
		hotelSelect.addClass("hotelSelect");
		
		var suppGoodsId=hotelSelect.attr("data-suppgoodsid");
		var thisProductId = hotelSelect.attr("data-productId");
		var productItemIdIndex = hotelSelect.attr("data-productItemIdIndex");
		hotelTab.find("[name='productMap[" + thisProductId + "].itemList[" + productItemIdIndex + "].quantity']").val(hotelSelect.val());
		hotelTab.find("[name='productMap[" + thisProductId + "].itemList[" + productItemIdIndex + "].adultQuantity']").val(hotelSelect.val());
		hotelTab.find("[name='productMap[" + thisProductId + "].itemList[" + productItemIdIndex + "].goodsId']").val(suppGoodsId);
		
		var parentdetails = $("#jingjiuhotel-details"+groupId); 
		parentdetails.html(hotelTab.find(".content-details").html());
		parentdetails.find(".jingjiuhotelSelect[data-suppgoodsid="+suppGoodsId+"]").find("option[value="+hotelSelect.val()+"]").attr("selected",true);
		var parenthotelTab = parentdetails.parents(".hotel-tab");
		
		var hotelproductId = hotelSelect.attr("data-hotelproductId");
		var hotelproductName = hotelSelect.attr("hotelName");
		var packageProductBranchId = hotelSelect.attr("data-currentProductBranchId");
		parenthotelTab.find(".jingjiuhotelName").attr("href","http://hotels.lvmama.com/hotel/"+hotelproductId).html(hotelproductName);
		parenthotelTab.find(".content-title-right").attr("data-selectedsuppgoodsid",suppGoodsId).attr("packageProductId",hotelproductId).attr("packageProductBranchId",packageProductBranchId).attr("data-totalAmount",hotelSelect.val());
		
		var goodsPrice=hotelSelect.attr("goodsPrice");
		var totalPrice=parseFloat(goodsPrice)*parseInt(hotelSelect.val());
		hotelSelect.find("option[value="+hotelSelect.val()+"]").attr("selected",true);
		parenthotelTab.find(".priceYuan").html(totalPrice.toFixed(2)+"元");
		
		clicking();
        var more = $('.details-more');
        var moreContent = more.find('p');

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
        $('.content-details .room-type').on('click',function () {
            var tipBox = $(this).parent().find('.room-tip');
            if(tipBox.is(':visible')){
                tipBox.hide()
            }else {
                tipBox.show()
            }
        })
        
        $(".hotel-tab").each(function(){
			refreshHotelGapPrice($(this), $(this).find(".hotel-info")[0]);
		});
        
		dialog.close();
		dialog=null;
		try{
  			setTimeout(changeNextSelect(null,null),500);
		}catch(err){}
  		countTotalPrice();
	});
	
}

