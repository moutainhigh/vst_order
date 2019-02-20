<div class="PopBox_p">
    	<h3>确认日期</h3>
    	<div class="daterlBox">
            <div class="hd">
                <div class="switch">
                    <div class="mainHd">
                        <ul class="dateList">
                        	<#if groupDateList?? && groupDateList?size &gt; 0>
                        	<#list groupDateList as gd>
                            <li data="${productId}">
                                <span class="specDate">${gd.date?string("yyyy-MM-dd")}</span>
                                <span>${gd.week}</span>
                                <span class="priceBox" data="<#if gd.minPrice??>hasPrice</#if>"><#if gd.minPrice??><em>&yen;${gd.minPrice/100}</em>起<#else>----</#if></span>
                            </li>
                            </#list>
                            </#if>
                            <li>暂无团期</li>
                        </ul>
                    </div>
                </div>
            </div>
        <div class="leftBtn">
            <
        </div>
        <div class="rightBtn">
            >
        </div>
</div>
   
        <div class="close_Btn">X</div>
 <script>
	var _box  = $('.dateList');
	var _jz_num =$('.dateList').length;
	
	$('.dateList').each(function(){ 
		var _list_num = $(this).find('li').length;
		if(_list_num>5){
		$(this).parents('.daterlBox').find('.rightBtn').addClass('rightBtn2').removeClass('rightBtn');
		};
	});
	
	
		$('.rightBtn2').live('click',function(){
			var small_box = $(this).parents('.daterlBox').find('.dateList');
			var _left = small_box.position().left;	
			var _geshu= small_box.find('li').length;
			var _width=-(_geshu-5)*131+10;
			small_box.animate({'left':_left-131},300,function(){
				if(small_box.stop(true).position().left<_width)
				{
					$(this).parents('.daterlBox').find('.rightBtn2').addClass('rightBtn').removeClass('rightBtn2');
				}										 
			 });
			$(this).parents('.daterlBox').find('.leftBtn').addClass('leftBtn2').removeClass('leftBtn');
		
		});
				
				
	 $('.leftBtn2').live('click',function(){
		 var small_box = $(this).parents('.daterlBox').find('.dateList');
		 var _left = small_box.position().left;							  
		small_box.animate({'left':_left+131},300,function(){
		if(small_box.stop(true).position().left>-1){$(this).parents('.daterlBox').find('.leftBtn2').addClass('leftBtn').removeClass('leftBtn2');}											
													 });
		$(this).parents('.daterlBox').find('.rightBtn').addClass('rightBtn2').removeClass('rightBtn')
	});
	
	$('.dateList li').click(function(){
		var productId = $(this).attr("data");
		var specDate = $(this).find(".specDate").text();
		var hasPrice = $(this).find("span").eq(2).attr("data");
		var distributionId = $("input[name=distributionId]").val();
		if(hasPrice!="hasPrice"){
			$.alert("该团期暂不可售!");
			return;
		}
		guanbi();
		//设置选中效果
		xzBtnRefresh($(this).attr("data"));
		loading	= pandora.loading("正在努力加载...");
		$.get("/vst_order/ord/order/queryCombDetailList.do?specDate="+specDate+"&productId="+productId+"&distributionId="+distributionId+"&userId="+book_user_id,function(result){
			$(".mod_xx").html(result);
			$('.mod_xx').show();
			loading.close();
		});
	});
 
   function xzBtnRefresh(productId){
   		$("#combProductId").val(productId);
  		$(".xzBtn").addClass("btn_cc1").removeAttr("disabled").bind("click",selectGroupDate).text("选择");
   		$("#"+productId+"Btn").removeClass("btn_cc1").unbind("click",selectGroupDate).text("已选择");
   }
 </script>       