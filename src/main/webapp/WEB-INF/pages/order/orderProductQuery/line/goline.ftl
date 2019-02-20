<!--航班筛选---开始-->
<#import "/order/orderProductQuery/line/goline_table.ftl" as golineTable>

<link rel="stylesheet" href="http://pic.lvmama.com/styles/v6/line/line_detail_ticket.css"/>

	<div class="plane_search_tit1 plane_search flightSelectedInfo">
		<#if data && data?size gt 0> 
	   		<b>去程</b><span>${data.from} → ${data.to} ${data.date} （${data.day}）</span><span class='go_info' goDate="${data.date}"></span></br>
	   	</#if> 
	    	<b>返程</b><span class="back_info_prev"></span><span class="back_info" backDate=""></span>
    </div>
    <#if data && data?size gt 0 && flights?exists && flights?size gt 0>    
    	<ul class="ldt_ccs_tabs">
	        <li class="ldt_ccs_active"><a id="cfhb${type}" href="javascript:;">出发航班</a> <i></i></li>
	        <li ><a id="hchb${type}" href="javascript:;">回程航班</a> <i></i></li>
   		</ul>
    	<div id="cfhbdiv" style="height: 413px;"><@golineTable.golineTable flights data 'TO' suppGoodsDJFlagMap/></div>
		<div id="hchbdiv" style="display:none;height: 413px;"></div>
	<#else>
		<ul class="ldt_ccs_tabs">
	        <li ><a id="cfhb${type}" href="javascript:;">出发航班</a> <i></i></li>
	        <li class="ldt_ccs_active"><a id="hchb${type}" href="javascript:;">回程航班</a> <i></i></li>
   		</ul>
		<div id="cfhbdiv" style="display:none;height: 413px;">没有去程航班可换</div>
		<div id="hchbdiv" style="height: 413px;"></div>
	</#if>
        
    
	<button class="btn btn-mini confirmChange">确定</button>
	<button class="hide confirmChange_go">确定</button>
	<button class="hide confirmChange_back">确定</button>

<script>


$(function(){

	$("#cfhb").click(function(){
		$("#cfhb").closest('li').addClass("ldt_ccs_active");
		$("#hchb").closest('li').removeClass("ldt_ccs_active");
		$("#cfhbdiv").show();
		$("#hchbdiv").hide();
	});
	
	$("#hchb").click(function(){
		$("#hchb").closest('li').addClass("ldt_ccs_active");
		$("#cfhb").closest('li').removeClass("ldt_ccs_active");
		$("#hchbdiv").show();
		$("#cfhbdiv").hide();
	});
	/**
	 * 更多交通切换确认
	 */
	$(".confirmChange").click(function(){
		//$.alert("test");
		//$.alert($(window.parent.document).find("input[name=hasApiFlight]").val());
		
		var groupSize = $('.moreCategoryLineTransport').attr('groupSize');
		var currentShow = $('.moreCategoryLineTransport').attr('currentShow');
		
		if(groupSize == '3' && currentShow=='TOBACK'){
			if( isEmpty($(".go_info").html()) && !isEmpty($(".back_info").html()) ){ 
				$.alert('请选择去程');
				return;
			}
			if(!isEmpty($(".go_info").html()) && isEmpty($(".back_info").html())){
				$.alert('请选择返程');
				return;
			}
			$('.moreCategoryLineTransport').attr('currentShow','TO');
		}
		$(".confirmChange_go").click();
		$(".confirmChange_back").click();
		countTotalPrice();//重新价格	
		
		//重新计算是否可后置的标志
		recalculateTravelerDelayFlag();
		
		var parent_hasApiFlag=$(window.parent.document).find("input[name=hasApiFlight]");
		//alert(isApiFlaght());
		parent_hasApiFlag.val(isApiFlaght());
		//alert(parent_hasApiFlag.val());
		$(".dialog-close").click();
		adultChildPrice();
	});
	
 	/**
	 * 自由组合是否对接机票
	 */
	function isApiFlaght(){
	// 1、去程是否对接机票
	var go_isApi="N"// 去程是否对接
	var back_isApi="N"// 回程是否对接
	$("#golinelist").find(".line-traffic-icon").each(function(){
		var ctl_select=$(this);// 选中标签
		if(ctl_select.is(":visible")){
			go_isApi=ctl_select.next("button").attr("data-jiPiaoDuiJieFlag")
			return;// 跳出each
		}
	});
	
	$("#backlinelist").find(".line-traffic-icon").each(function(){
		var ctl_select=$(this);// 选中标签
		if(ctl_select.is(":visible")){
			back_isApi=ctl_select.next("button").attr("data-jiPiaoDuiJieFlag")
			return;// 跳出each
		}
	});
	// 如果去程或者返程有一个对接机票的，标示为对接机票
	if("Y"==go_isApi||"Y"==back_isApi){
		return "Y";
	}else{
		return "N";
	}
	
	}
	
	//setTimeout(function(){
		var transType = "${transportType}";
		if(transType == 'TOBACK'){
			openChangeDivTransportBack($('.moreCategoryLineTransport'), "apilinetransport");
		}else{
			openChangeDivTransportBack($('.apiChangeTransprot'), "apilinetransport");
		}
	//},10000);
});

//重新计算是否可后置的标志
function recalculateTravelerDelayFlag() {
	var $travellerDelayFlag = $("[name=travellerDelayFlag]");
	var oldTravellerDelayFlag = $travellerDelayFlag.val();
	var toDJFlag = $travellerDelayFlag.attr("data-toDJFlag");
	var backDJFlag = $travellerDelayFlag.attr("data-backDJFlag");
	
	//
	var $changeBtn = $(".apiChangeTransprot");
	if(!toDJFlag && typeof toSuppGoodsDJFlagMap != "undefined"  && toSuppGoodsDJFlagMap) {
		toDJFlag = toSuppGoodsDJFlagMap[$changeBtn.attr("toselectedsuppgoodsid")];
	}
	
	if(!backDJFlag && typeof backSuppGoodsDJFlagMap != "undefined" && backSuppGoodsDJFlagMap ) {
		backDJFlag = backSuppGoodsDJFlagMap[$changeBtn.attr("backselectedsuppgoodsid")];
	}
	
	if(toDJFlag || backDJFlag) {
		var newTravellerDelayFlag = "N";
		if(toDJFlag == "Y" || backDJFlag == "Y" ) {
			var newTravellerDelayFlag = "N"
		} else {
			//没有对接机票，如果产品中有游玩人后置标记，那么就可以后置
			var prod_travellerDelayFlag = $("[name=prod_travellerDelayFlag]").val();
			if("Y" == prod_travellerDelayFlag) {
				newTravellerDelayFlag = "Y";
			}
		}
		if(oldTravellerDelayFlag != newTravellerDelayFlag ) {
			$travellerDelayFlag.val(newTravellerDelayFlag);
		}
	}
}
//@ sourceURL=goline_confirmChange.js
</script>
