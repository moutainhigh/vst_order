<div class="plane_box">

	
    <div class="operate">
		<#if transportType=='TOBACK'>
	    	<a id="wftjTransport" class="btn w8"
	    		style=" margin:0;padding:5px 0;">往返推荐 </a>
	    	<a id="zyzhTransport" class="btn btn_cc1 w8" 
	    		style="margin:0;padding:5px 0;">自由组合</a>
		<#else>
	    	<a id="zyzhTransport" class="btn btn_cc1 w8" style="margin:0;padding:5px 0;">自由组合</a>
		</#if>
	</div>
    <div class="plane_list">
        
        <!--选择去程---开始-->
    	<div class="plane_list_qu" id="goline">
        	<#include "/order/orderProductQuery/line/goline.ftl">
        </div><!--选择去程---结束-->
        
        
        <!--去程选择后--开始-->
        <div class="plane_ok  backLineQu hide">
        	<table class="plane_table">
                <tbody>
                    <tr>
                        <td>
                        	<div class="plane_info"><b>去程</b><span id="go_city">上海 → 三亚</span></div>
                            <div class="plane_info" id="go_date">2015-03-26 （周一）</div>
                        </td>
                        <td>
                        	<div class="plane_name" id="go_aircompany">上海航空</div>
                        	<div class="plane_ban" id="go_flightNo">FM9566
                        		<span id="go_jxinfo" class="plane_type" table_td1="73H" table_td2="波音737-800" table_td3="窄体" 
                        			table_td4="156" table_td5="156">(73H)
                        		</span>
                        	</div>
                        </td>
                        <td>
                            <ul class="qidi_box">
                                <li>08:30<br>虹桥国际机场</li>
                                <li><p><span>直飞</span></p></li>
                                <li>10:30<br>地狱国际机场</li>
                            </ul>
                        </td>
                        <td id="go_totalTime">3小时19分</td>
                        <td id="go_seat"><p>经济舱</p></td>
                        <td id="go_food">含餐食</td>
                        <td>
                            <div class="adjust-traffic-item-status status">
                                <div class="replace">
                                    <button class="btn btn-mini js_fancheng">选择返程</button>
                                </div>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div><!--去程选择后--结束-->
        
        
        <!--选择去程---开始-->
    	<div class="plane_list_fan" id="backline">
        </div><!--选择去程---结束-->
        
    </div><!--交通弹窗切换tab内容---结束-->
</div>
<script>
 $(function(){
 	var transType = "${transportType}";
 	//往返推荐
	$('#wftjTransport').click(function(){
		$(".dialog-close").click();
		if(transType == 'TOBACK'){
			openChangeDivTransport($('.moreCategoryLineTransport'), "linetransport");
 		}else{
 			openChangeDivTransport($('.apiChangeTransprot'), "linetransport");
 		}
	});
	//自由组合
	$('#zyzhTransport').click(function(){
		$(".dialog-close").click();
		if(transType == 'TOBACK'){
			openChangeDivTransport($('.moreCategoryLineTransport'), "apilinetransport");
 		}else{
 			openChangeDivTransport($('.apiChangeTransprot'), "apilinetransport");
 		}
	});
 });
</script>


