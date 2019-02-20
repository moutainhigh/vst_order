<#macro golineTable flights data type suppGoodsDJFlagMap>
	<div>
    
    
    <div class="plane_search">

    <dl class="plane_kind planecondition planecondition_to">
        <dt>航空公司</dt>
        <dd class="active" id="nonAirCompany" ><label style="cursor:pointer">不限</label></dd>
        <#list data.airCompanies as company>
            
            <dd><label><input class="check" type="checkbox" value="${(company!'')?trim}" name="companyname">${company}</label></dd>
            
        </#list>
    </dl>
    <dl class="plane_kind planecondition planecondition_to">
        <dt>起飞时段</dt>
        <dd class="active" id="nonAirTime" ><label style="cursor:pointer">不限</label></dd>
        <dd><label><input class="check" type="checkbox" value="上午" name="takeofftime">上午（6-12点）</label></dd>
        <dd><label><input class="check" type="checkbox" value="中午" name="takeofftime">中午（12-13点）</label></dd>
        <dd><label><input class="check" type="checkbox" value="下午" name="takeofftime">下午（13-18点）</label></dd>
        <dd><label><input class="check" type="checkbox" value="晚上" name="takeofftime">晚上（18-24点）</label></dd>
    </dl>
    <dl class="plane_kind planecondition planecondition_to">
        <dt>条件筛选</dt>
        <#list data.spaces as space>
        	
        	<dd><label><input class="check" type="checkbox" value="${(space!'')?trim}" name="spacetype">${space}</label></dd>
        	
        </#list>
    </dl>
    </div>
</div><!-- 航班筛选---结束-->

	<!--航班总条数和排序按钮--开始-->
    <div class="plane_search_b">
        <span class="btn_cha goline">差价从低到高</span>
        <span class="btn_yu goline">剩余从多到少</span>
        <p>共${(flights)?size}条航班信息</p>
    </div><!--航班总条数和排序按钮--结束-->
    
    <!--航班表格---开始-->
    <div class="plane_table_box scroll_y">
    <table id="golinelist" class="plane_table plane_table_to">
        <tbody >
            <tr>
                <th width="110">航空公司</th>
                <th width="280">起抵时间</th>
                <th width="90">总时长</th>
                <th width="75">舱位</th>
                <th width="45">差价</th>
                <th width="55">备注</th>
                <th width="55">剩余</th>
                <th width="55">选择</th>
            </tr>
        </tbody>
        <#list flights as flight>
        <tbody>
        	<#list flight.seatInfo as seat>
        		<#if seat_index == 0>
                <tr>
                    <td><div class="plane_name">${flight.airCompany}</div><div class="plane_ban">${flight.flightNo!'未知'}<span class="plane_type jxinfo" table_td1="${flight.planStyle}" table_td2="" table_td3="" table_td4="" table_td5="">(${flight.planStyle!'未知'})</span></div></td>
                    <td>
                        <ul class="qidi_box">
                            <li style='width: 70px;overflow: hidden;'>${flight.startFrom.starttime}<br>${flight.startFrom.airport}</li>
                            <li>
                            	<p>
                            	<#if flight.middle?size gt 0>
                            		<span class="qidi_zhuan" tip-content222='
                            		<#list flight.middle as port>
                            			${port.city}(${port.airport}) <#if (port_index+1) lt flight.middle?size> <br></#if>	
                            		</#list>
                            		'>
                            		经停(${flight.middle?size})
                            		
                            		</span>
                            	<#else>
                            		<span>直飞</span>
                            	</#if>
                            	</p>
                            </li>
                            <li style='width: 70px;overflow: hidden;'>${flight.endTo.datetime}<br>${flight.endTo.airport}</li>
                        </ul>
                    </td>
                 <#else>
                 	<tr class="hide">
                 		<td></td>
                 		<td></td>
				</#if>		                     		
                    <td>${flight.totalTime}</td>
                    <td>
                    	<p>${seat.seat}</p>
                        <#if seat_index == 0 && flight.seatInfo?size gt 1 >
                        	<!--<span class="btn_cw morespace">更多舱位<i class="icon_arrow"></i></span>-->
                        </#if>
                    </td>
                    <td><span class="c_red">
                    	<#if seat.priceDiff?number gt 0>+</#if>${seat.priceDiff}
                    	</span>
                    </td>
                    <td>
                    	<#if seat.food == 'Y'>
							含餐食
						<#else>
                    		不含餐食
                    	</#if>
                    </td>
                    <#if seat.ticketLeft=='-1'>
                    	<td><span class="c_red">余票现询</span></td>
                    <#else>
                    	<td><span class="c_red">余${seat.ticketLeft}张</span></td>
                    </#if>
                    <td>
                        <div class="adjust-traffic-item-status status">
                            <#if flight.selected gt -1 && seat_index == flight.selected>
                                <div class="replace line-traffic-icon">
                                    <i class="product-item-checked-icon"></i>
                                </div>
                            	<button class="btn btn-mini do-select-action do-select-action_go selectflight hide" id=btn_${flight.flightNo}_${seat_index}
                            		<#if seat.ticketLeft!='-1' && seat.ticketLeft?number - (adultQuantity?number * quantity?number) lt 0 >
                            			disabled="disabled"
                            		</#if>
	                                data-suppgoodsid="${seat['goodId']}"
									data-groupid="${seat['groupId']}" 
									data-currentproductbranchid="${seat['branchId']}"
									data-productid="${seat['trafficId']}" 
									data-detailid="${seat['detailId']}"
									data-transProductName="${flight.productName}"
									data-quantity="${quantity}" 
									data-adultQuantity="${adultQuantity}"
									data-childQuantity="${childQuantity}"
									data-selectDate="${selectDate}"
									data-havechangebutton="${haveChangeButton}"
									data-selectedcurrentproductbranchid="${selectedCurrentProductBranchId}"
									data-selectedsuppgoodsid="${selectedSuppGoodsId}"
									data-price=${seat.price}
									data-ticketLeft=${seat.ticketLeft} 
									data-jiPiaoDuiJieFlag="${suppGoodsDJFlagMap[seat['goodId']]}"
	                                data-changeType="traffic">选择</button>
                            <#else>
                            	<div class="replace line-traffic-icon hide">
                                    <i class="product-item-checked-icon"></i>
                                </div>
                            	<button class="btn btn-mini do-select-action do-select-action_go selectflight" id=btn_${flight.flightNo}_${seat_index}
                            		<#if seat.ticketLeft!='-1' && seat.ticketLeft?number - (adultQuantity?number * quantity?number) lt 0 >
                            			disabled="disabled"
                            		</#if>
                            		
                            		data-suppgoodsid="${seat['goodId']}"
									data-groupid="${seat['groupId']}" 
									data-currentproductbranchid="${seat['branchId']}"
									data-productid="${seat['trafficId']}" 
									data-transProductName="${flight.productName}"
									data-quantity="${quantity}" 
									data-adultQuantity="${adultQuantity}"
									data-childQuantity="${childQuantity}"
									data-selectDate="${selectDate}"
									data-selectedcurrentproductbranchid="${selectedCurrentProductBranchId}"
									data-selectedsuppgoodsid="${selectedSuppGoodsId}"
									data-havechangebutton="${haveChangeButton}"
									data-price=${seat.price}
									data-ticketLeft=${seat.ticketLeft}
									data-jiPiaoDuiJieFlag="${suppGoodsDJFlagMap[seat['goodId']]}"
	                                data-changeType="traffic">选择</button>
                            </#if>
                        </div>
                    </td>
                </tr>
            </#list>
            
            
        	</tbody>
        </#list>
    </table><!--航班表格---结束-->
    </div>
<script>
	//获取飞机航班数组
	var toSuppGoodsDJFlagMap = jQuery.parseJSON('${suppGoodsDJFlagMapStr}');
	var DJJPdata= jQuery.parseJSON('${flightJsonStr}');
	var DJJPbackdata = jQuery.parseJSON('${flightJsonStrback}');//flightJsonStrback不存在
	var DJJPfrom = '${data.from}' ;
	var DJJPto = '${data.to}' ;
	var DJJPgoDate = '${data.date}';
	var DJJPgoDay = '${data.day}';
	var DJJPhasBackLine = '${DJJPhasBackLine}';
	
	var DJJPquantity='${quantity}';
	var DJJPadultQuantity='${adultQuantity}';
	var DJJPchildQuantity='${childQuantity}';
	var DJJPselectDate='${selectDate}';
	var DJJPselectedCurrentProductBranchId='${selectedCurrentProductBranchId}';
	var DJJPhaveChangeButton='${haveChangeButton}';
	var DJJPselectedSuppGoodsId = '${selectedSuppGoodsId}';
	var peoNum = '${(adultQuantity?number) }';
	var copies = '${ quantity?number}';
	if(parseInt(copies) > 0 ){
		peoNum = parseInt(peoNum) * parseInt(copies);
	}	
</script>
<script>
		
	/**
 * 
 */

$(function() {
	//已选去程隐藏
	$(".backLineQu").hide();
	/*返回去程*/
	
	$(".plane_search_b .goline").live("click",{backline:false},sortlines);
	
	/*机型参数*/
	$('.plane_table_to').delegate(".plane_type", "mouseenter", showJXinfo);
	$('.plane_table_to').delegate(".plane_type", "mouseout", hideJXinfo);

	//点击去程选择,如果没有返程，那么无需绑定了
	$(".plane_list_qu").delegate(".selectflight", "click", selectGoLine);

	$(".plane_list_qu .planecondition_to :checkbox").live("click", {backline : false}, searchFlight);
	
	$('td').delegate(".morespace", 'click', showMoreSpace);
	
	$(".qidi_zhuan").poptip();
	
	$(".planecondition_to").delegate("#nonAirCompany","click",clearAndSearchAirCompany);
	$(".planecondition_to").delegate("#nonAirTime","click",clearAndSearchAirTime);
});

function clearAndSearchAirTime(){
	$(":checkbox[name='takeofftime']").attr("checked",false);
	var ob = {data:{backline : false}};
	searchFlight(ob);
}

function clearAndSearchAirCompany(){
	$(":checkbox[name='companyname']").attr("checked",false);
	var ob = {data:{backline : false}};
	searchFlight(ob);
}

//点击，去程选择按钮
function selectGoLine() {
	var that = this;
	//之前选择的隐藏
	$(".plane_list_qu .line-traffic-icon").each(function(i) {
		$(".plane_list_qu .line-traffic-icon").eq(i).addClass("hide");
	});
	$(".plane_list_qu .selectflight").each(function(i) {
		$(".plane_list_qu .selectflight").eq(i).removeClass("hide");
	});

	//选择隐藏，勾选显示
	var sibling = $(that).siblings().eq(0);
	sibling.removeClass("hide");
	var id = $(that).attr("id");
	$(that).addClass("hide");
	//记录去程
	recgoflight.call(window, id);
}

//显示机型信息
function showJXinfo(e) {
	if ($(this).attr("table_td3") == "" && $(this).attr("table_td4") == ""&& $(this).attr("table_td2") == "") {
		var planeType = $(this);
		var isbackline = planeType.hasClass("JXT");
		$.ajax({
			url : "/vst_order/ord/order/getAirplaneJXinfo.do",
			type : "GET",
			async : false,
			data : {planStyle:encodeURI(planeType.attr("table_td1"))},
			dataType : "json",
			success : function(info) {
				//table_td1   table_td2  table_td3  table_td4  table_td5
				if(info=="" || info == null ){
					//ajax，调用失败，返回空字符串
				}else{
					planeType.attr("table_td1", info[0].planStyle);
					planeType.attr("table_td2", info[0].styleName);
					planeType.attr("table_td3", info[0].planeStyle);
					planeType.attr("table_td4", info[0].leastName);
					planeType.attr("table_td5", info[0].mostSeats);
					jxInfoToData.call(window, info,isbackline);
				}
				
			},
			error : function() {
				//alert("error");
			}
		});
		
	}
	var thisL = $(this).offset().left - 20, thisT = $(this).offset().top + 20, tanBox = $('.plane_type_box'), tdHtml = '';
	tanBox.show().css({
		'left' : thisL,
		'top' : thisT
	}).css("z-index", 9999);
	for ( var i = 0; i < tanBox.find('tr:first').find('th').length; i++) {
		tdHtml += '<td>' + $(this).attr('table_td' + (i + 1)) + '</td>'
	}
	tanBox.find('tr').eq(1).html(tdHtml);
}
//隐藏机型信息
function hideJXinfo() {
	$('.plane_type_box').hide();
}



var DJJPshowBackData = null;
var DJJPshowData = null; //列表中的数据    复制对象SOS
var DJJPsortType = null; //升序，降序      （1,0）
var DJJPsortValue = null; //按差价还是剩票   （priceDiff,ticketLeft）
var DJJPtype; //头部查询类别     // 1 公司，2 时间，3舱位  4直飞[]  5是否含餐食 ['']
var DJJPparams = []; //头部查询参数     //（1,['南方航空','上海航空']）    4,5 是传'Y'  否 传'N'
var DJJPgoflight = null; //记录去程航班
var DJJPbackflight = null; //记录返程航班
function showMoreSpace() {
	if (!$(this).hasClass('btn_up')) {
		$(this).addClass('btn_up').parents('tr').nextAll().show();
	} else {
		$(this).removeClass('btn_up').parents('tr').nextAll().hide();
	}
}
//机型信息填充到数据模型中
function jxInfoToData(info, isbackline) {
	var planStyle = info[0].planStyle;
	var flight = null;
	if(!isbackline){
		for ( var row in DJJPdata) {
			flight = DJJPdata[row]
			if (flight.planStyle == planStyle) {
				break;
			} else {
				flight = null;
			}
		}
	}else{
		//如果在去程中找不到，则在返程中查找
		for ( var d in DJJPbackdata) {
			flight = DJJPbackdata[d];
			//console.log("--"+flight.planStle+"--");
			//console.log(flight);
			if (flight.planStyle == planStyle) {
				break;
			} else {
				flight = null;
			}
		}
	}
	if (flight != null) {
		flight.styleName = info[0].styleName;
		flight.planeStyle = info[0].planeStyle;
		flight.leastSeats = info[0].leastSeats;
		flight.mostSeats = info[0].mostSeats;
	}
}
//记录去程
function recgoflight(btnId) {
	//获取航班号
	var fliNo = btnId.split("_")[1];
	var seatNo = btnId.split("_")[2];
	var datalist = DJJPshowData!=null  ? DJJPshowData : DJJPdata;
	//获取对应航班
	for ( var a in datalist) {
		var thisflight = datalist[a];
		if (thisflight.flightNo == fliNo) {
			DJJPgoflight = thisflight;
			DJJPgoflight.selected = parseInt(seatNo);
		} else {
			thisflight.selected = -1;
		}
	}
	//console.log('DJJPgoflight: ' + DJJPgoflight);
}

//根据条件查询相应的记录，并渲染到表格
function searchFlight(back) {
	var gocompany = [];
	var gotime = [];
	var goseat = [];
	var go_checkbox;
	var backline = back.data.backline;
	go_checkbox = $(".plane_list_qu .planecondition_to").find(".check");
	for ( var i = 0; i < go_checkbox.length; i++) {
		var chkbox = go_checkbox.eq(i);
		//if(i==0)console.log(chkbox.attr("name"));
		var text = chkbox.attr("value");
		var name = chkbox.attr("name");
		if (chkbox.attr("checked") == "checked") {
			if ('companyname'.indexOf(name) > -1) {
				gocompany.push(text);
			} else if ('takeofftime'.indexOf(name) > -1) {
				gotime.push(text);
			} else if ('spacetype'.indexOf(name) > -1) {
				goseat.push(text);
			}
		}
	}
	getRows(DJJPdata, 1, gocompany, backline);
	if (gotime.length > 0) {
		getRows(DJJPshowData, 2, gotime, backline);
	}
	if (goseat.length > 0) {
		getRows(DJJPshowData, 3, goseat, backline);
	}
	renderDataToTable(DJJPshowData, backline);
}
//data 头部筛选
// 1 公司，2 时间，3舱位  4直飞
//（1,['南方航空','上海航空']）
function getRows(flights, type, params, backline) {
	var datas = flights;

	if (params.length <= 0) {
		DJJPshowData = datas;
		return DJJPshowData;
	}
	var airflights = [];
	
	//舱位以及该舱位余票
	if (type == 3) {
		var copydatas = $.extend(true,{},datas);
		airflights = copydatas;
		for ( var row in airflights) {
			var seatsTmp = [];
			for ( var param in params) {
				var space = params[param];
				var seatinfos = airflights[row].seatInfo;
				for ( var spa in seatinfos) {
					var seat = seatinfos[spa];
					if (seat.seat.trim()== space.trim()) {
						seatsTmp.push(seat);
					}
				}
			}
			airflights[row].seatInfo = seatsTmp;
			seatsTmp=[];
		}
		
	}else{
		for ( var row in datas) {
			if (type == 1) {
				for ( var param in params) {
					if (datas[row].airCompany == params[param]) {
						airflights.push(datas[row]);
					}
				}
			}
			if (type == 2) {
				for ( var param in params) {
					//console.log(datas[row].startFrom.starttype  + "  " + params[param]);
					if (datas[row].startFrom.starttype == params[param]) {
						airflights.push(datas[row]);
					}
				}
			}

			if (type == 4) {
				//是 - 'Y'
				if (datas[row].middle.length == 0) {
					airflights.push(datas[row]);
				}
			}

		}
	}
	DJJPshowData = airflights;
	return DJJPshowData;
}
function sortlines(back){
	var datalist = [] ;
    var backline = back.data.backline;
    DJJPsortValue = $(this).hasClass("btn_yu") ? "ticketLeft":"priceDiff";
    datalist = DJJPshowData!=null  ? DJJPshowData : DJJPdata;
    //
    var goodId = null;
    var selectedFlight = null;
    var selectedFlag = null;
    if(DJJPgoflight!=null && DJJPgoflight.selected >= 0){
    	goodId = DJJPgoflight.seatInfo[DJJPgoflight.selected].goodId;
    	selectedFlag = DJJPgoflight.seatInfo[DJJPgoflight.selected].selectedFlag;
    	selectedFlight = DJJPgoflight;
    }
    for(var c in datalist){
    	var flightrow = datalist[c];
    	flightrow.seatInfo.sort(sortSeats);
    }
    if(selectedFlight != null){
	    for(var d in selectedFlight.seatInfo ){
	    	var r = selectedFlight.seatInfo[d];
	    	if(r.selectedFlag == selectedFlag){
	    		selectedFlight.selected = d;
	    	}
	    }
    }
    renderDataToTable(datalist,backline);
}
//data 排序- 这里默认是升序
function sortSeats(oA, oB) {
	//按差价  由少到多
	var type = DJJPsortValue;
	if (type == 'priceDiff') {
		if (parseInt(oA.priceDiff) - parseInt(oB.priceDiff) >= 0 ) {
			return 1;
		} else {
			return -1;
		}
	}
	//按航班的剩余票排序 从多到少
	if (type == 'ticketLeft') {
		if (parseInt(oA.ticketLeft) - parseInt(oB.ticketLeft) >= 0 ) {
			return -1;
		} else {
			return 1;
		}

	}
}

//data 渲染param:航班信息，去0还是返1
function renderDataToTable(airflights, backline) {

	$(".plane_list_qu .plane_table_to tbody:gt(0)").remove();
	
	var rows = [];
	for ( var a in airflights) {
		var flight = airflights[a];
		var row = "";
		for ( var b in flight.seatInfo) {

			var seat = flight.seatInfo[b];
			//之前的座位和当前的座位Id是否一样
			var isSameGoodId = null ; 
			if(flight.selectedFlag == "${selectedFlag}"){
				isSameGoodId = 'true';
			}
			
			//前两列
			var rowA = "";
			//选择咧
			var rowB = "";
			//舱位更多选项
			var rowC = "";
			//是否含餐食
			var food = '';
			//直飞
			var middle  = '';
			//机型上是返回标志
			var jxinfoCls = "";
			//如果剩余票不够，要置灰
			var disablestr = "";
			if(seat.ticketLeft!='-1'){
				disablestr = seat.ticketLeft - peoNum >=0 ? "" : "disabled=disabled";
			}
			
			if(flight.middle.length > 0 ){
				for(var m in flight.middle){
					var port = flight.middle[m];
					middle = middle + port.city +"("+port.airport+")";
					if(m != flight.middle.length -1){
					    middle = middle +"<br>";
					}
				}
				middle = "<span class ='qidi_zhuan' tip-content222='"+middle+"'>经停("+flight.middle.length+")</span>";
			}else{
			    middle = "<span>直飞</span>";
			}
			if (parseInt(b) == 0) {
				rowA = "<tr>" + "<td><div class='plane_name'>"
						+ flight.airCompany + "</div><div class='plane_ban'>"
						+ flight.flightNo
						+ "<span class='plane_type "+jxinfoCls+" info' table_td1='"
						+ flight.planStyle + "' table_td2='" + flight.styleName
						+ "' table_td3='" + flight.planeStyle + "' table_td4='"
						+ flight.leastSeats + "' table_td5='"
						+ flight.mostSeats + "'>(" + flight.planStyle
						+ ")</span></div></td>" + "<td>"
						+ "<ul class='qidi_box'>" + "<li style='width: 70px;overflow: hidden;'>"
						+ flight.startFrom.starttime + "<br>"
						+ flight.startFrom.airport + "</li>"
						+ "<li><p>"+middle+"</p></li>" + "<li style='width: 70px;overflow: hidden;'>"
						+ flight.endTo.datetime + "<br>" + flight.endTo.airport
						+ "</li>" + "</ul>" + "</td>";
			} else {
				rowA = "<tr class='hide'>" + "<td></td>" + "<td></td>";

			}
			var btnClass = '';
			var btnd = '';
			//去程
			btnClass = "selectflight";
			btnd = "btn_";
			if(DJJPhasBackLine!=true && DJJPhasBackLine !="true"){
				btnClass = btnClass + " do-select-action do-select-action_go";
			}
			if(seat.ticketLeft!='-1' && seat.ticketLeft - peoNum < 0){
				btnClass = btnClass + " disable";
			}
			if (isSameGoodId == 'true') {
				rowB = "<div class='replace line-traffic-icon'>"
						+ "<i class='product-item-checked-icon'></i>"
						+ "</div>" + "<button "+disablestr+" class='btn btn-mini hide "
						+ btnClass + "' id=" + btnd + flight.flightNo + "_" + b;
						
			} else {
				rowB = "<div class='replace line-traffic-icon hide'>"
						+ "<i class='product-item-checked-icon '></i>"
						+ "</div>" + "<button  "+disablestr+"  class='btn btn-mini " + btnClass
						+ "' id=" + btnd + flight.flightNo + "_" + b;

			}
			
			var jiPiaoDuiJieFlag = toSuppGoodsDJFlagMap[seat.goodId];
			if(!jiPiaoDuiJieFlag ) { //默认为对接，让其不可后置
				jiPiaoDuiJieFlag = "Y"
			}
			
			rowB = rowB + " data-suppgoodsid=" + seat.goodId 
						+ 	" data-groupid=" + seat.groupId
						+	" data-currentproductbranchid=" + seat.branchId 
						+	" data-productid=" + seat.trafficId  
						+	" data-detailid=" + seat.detailId 
						+	" data-transProductName=" + flight.productName
						+	" data-quantity="+DJJPquantity
						+	" data-adultQuantity="+DJJPadultQuantity
						+	" data-childQuantity="+DJJPchildQuantity
						+	" data-selectDate="+DJJPselectDate
						+	" data-selectedcurrentproductbranchid="+DJJPselectedCurrentProductBranchId
						+	" data-havechangebutton="+DJJPhaveChangeButton

						+	" data-selectedsuppgoodsid="+DJJPselectedSuppGoodsId 

						+	" data-price="+seat.price
						+	" data-ticketLeft="+seat.ticketLeft 
						+   " data-jiPiaoDuiJieFlag='" + jiPiaoDuiJieFlag + "'"
						+	" data-changeType=traffic"
						
						+ ">选择</button>";
			
			if (b == 0 && flight.seatInfo.length > 1) {
				//rowC = "<span class='btn_cw morespace'>更多舱位<i class='icon_arrow'></i></span>";
			}
			if (seat.food == 'Y') {
				food = "含餐食";
			} else {
				food = "不含餐食";
			}
			if(seat.ticketLeft != '-1'){
				row = row + rowA + "<td>" + flight.totalTime + "</td>" + "<td><p>"
					+ seat.seat + "</p>" + rowC + "</td>"
					+ "<td><span class='c_red'>" + (seat.priceDiff - 0 > 0 ? "+" +seat.priceDiff :seat.priceDiff)
					+ "</span></td>" + "<td>" + food + "</td>"
					+ "<td><span class='c_red'>余" + seat.ticketLeft
					+ "张</span></td>" + "<td>"
					+ "    <div class='adjust-traffic-item-status status'>"
					+ rowB + "</div>" + "</td>" + "</tr>"
			}else{
				row = row + rowA + "<td>" + flight.totalTime + "</td>" + "<td><p>"
					+ seat.seat + "</p>" + rowC + "</td>"
					+ "<td><span class='c_red'>" + (seat.priceDiff - 0 > 0 ? "+" +seat.priceDiff :seat.priceDiff)
					+ "</span></td>" + "<td>" + food + "</td>"
					+ "<td><span class='c_red'>余票现询</span></td>" + "<td>"
					+ "    <div class='adjust-traffic-item-status status'>"
					+ rowB + "</div>" + "</td>" + "</tr>"
			}
			

		}
		rows.push("<tbody>" + row + "</tbody>");
	}
	$(".plane_list_qu .plane_table_to").append(rows.join(""));
	$('td').delegate(".morespace", 'click', showMoreSpace);
}

function getDayByDate(d){
	return new Array("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")[d.getDay()];
}
</script>
    
</#macro>