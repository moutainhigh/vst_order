<!DOCTYPE html>
<html lang="en">
<head>
	<#include "/base/head_meta.ftl"/>
    <meta charset="UTF-8">
    <title>Title</title>
    <link rel="stylesheet" href="/vst_order/css/lineGroupRate.css?version=${version!}" type="text/css"/>
</head>
<body>
    <div class="guest-num-container">
    	<input id="productId" value="${productId}" type="hidden"/>  
    	<input id="startDate" value="${startDate}" type="hidden"/>
    	<input id="monthResult" value="${monthResult}" type="hidden"/>
        <div class="gn-main"> 
        	<div class="gn-choose-bar">
			    <span class="gn-choose-prev"></span>
			    <span class="gn-choose-date" id="month" data0="${currentYear}" data1="${currentMonth}">${currentYear}年${currentMonth+1}月</span>
			    <span class="gn-choose-next"><#if monthResult &gt; 1><a onclick="changeMonth(1);"> > </a></#if></span>
			</div> 
			<div id="ajaxContent" style="overflow:auto;height:500px">
			</div>           
        </div>
    </div>

<script> 
$(function () { 
    loadData();
    
    function loadData(){
    	var productId = $("#productId").val();
    	var year = $("#month").attr("data0");
    	var month = $("#month").attr("data1");    	
    	var ajaxData = "productId="+productId+"&currentYear="+year+"&currentMonth="+month+"&startDate="+$("#startDate").val()+"&searchFlag=Y&queryGroupRateFlag=Y";
    	setAjaxContent("/vst_order/ord/order/route/queryGroupRate.do", ajaxData, $("#ajaxContent"));
    }     
});

    var cacheDate = new Date(Date.parse($("#startDate").val().replace(/-/g,"/")));
	var mos = parseInt($("#monthResult").val());
	
    //切换月份
    function changeMonth(op){
    
    	var productId = $("#productId").val();
    	var year = parseInt($("#month").attr("data0"));
    	var month = parseInt($("#month").attr("data1"));
    	var date = new Date(year,month + op,1);
    	year = date.getFullYear();
    	month = date.getMonth();
    	
    	$("#month").html(year+"年"+(month+1)+"月");
    	$("#month").attr("data0",year);
    	$("#month").attr("data1",month);
    	
    	
    	// 设置换月按钮显示
        var showPrev = date.getFullYear() > cacheDate.getFullYear() ?
                true : date.getMonth() === cacheDate.getMonth() ? false : true;

        var showNext = mos <= 0 ? true : ((date.getFullYear() - cacheDate.getFullYear()) * 12 +
                date.getMonth() - cacheDate.getMonth() + 1) >= mos ? false : true;
        if(showPrev == true){
        	$(".gn-choose-prev").html('<a onclick="changeMonth(-1);"> < </a>');
        }else{
        	$(".gn-choose-prev").html('');
        }
        
        if(showNext == true){
        	$(".gn-choose-next").html('<a onclick="changeMonth(1);"> > </a>');
        }else{
        	$(".gn-choose-next").html('');
        }
            
    	var ajaxData = "productId="+productId+"&currentYear="+year+"&currentMonth="+month+"&startDate="+$("#startDate").val()+"&searchFlag=Y&queryGroupRateFlag=Y";
    	setAjaxContent("/vst_order/ord/order/route/queryGroupRate.do", ajaxData, $("#ajaxContent"));
    }
    
    var isloading=false;
    function setAjaxContent(url,data,content,callback){
	    var ajaxData="";
	    if(data){
	    	ajaxData+=data+"&";
	    }
		ajaxData+="v="+(new Date()).getTime();
		if(isloading){
			return;
		}
		isloading=true;
		$.ajax({
			url:url,
			cache:false,
			data:data,
			success:function(html){
				content.html(html);
				isloading=false;
				if(callback){callback();}},
			fail:function(){
				isloading=false;}
		});
	}   

</script>
</body>
</html>


