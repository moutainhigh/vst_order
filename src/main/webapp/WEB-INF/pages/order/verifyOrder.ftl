<#import "/order/back_rules.ftl" as brf>
<style type="text/css">
	p{
	 font-size:16px;
	 font-weight: bold;
	 color:black;
	}
</style>
<script type="text/javascript">
    //订单核对取消关闭按钮
    $('.dialog-close').removeAttr('href');
	$('.dialog-close').hide();
</script>
<div data-content="content" class="dialog-content clearfix">
			<input type="hidden" name="orderId" value="${order.orderId}"/>
		            <p><strong><span class="cc6 f14">${order.contactPerson.fullName} </span>您好，</strong>我再和您核对一下</p>
		            <p>您预订了<span class="cc6 f14"> ${order.mainOrderItem.visitTime?string('MM月dd日')}（${weekStr}）</span>入住的
		            	<span class="cc6 f14">${order.mainOrderItem.productName} ${order.mainOrderItem.suppGoodsName}
						${order.mainOrderItem.quantity} 
		            </span>间 住 <span class="cc6 f14">${order.mainOrderItem.orderHotelTimeRateList?size}</span>晚。订单号：<span class="cc6 f14">${order.orderId}</span></p>
		            <p>特殊要求：<span class="cc6 f14">${order.remark}</span></p>
		            <br/>
		            <p>入住游客姓名：<span class="cc6 f14"><#list order.ordPersonList as person>
                                		<#if person?? && person.fullName!='' && person.personType=='TRAVELLER'>
	                                		${person.fullName}
	                                	</#if>
	                                	 <#if person_has_next>
	                                	 	<#if person.fullName!='' && person.personType=='TRAVELLER'>
	                                	 	,
	                                	 	</#if>
	                                	 </#if>
	                               	</#list></span>
	                </p>
	                <p>订单总金额为<span class="cc6 f14">${order.oughtAmountYuan}</span>元。(产品费用${order.getOrderAmountItemByType('ORIGINAL_PRICE')}元+保险0元-优惠券${order.getOrderAmountItemByType('COUPON_AMOUNT')}元-促销活动${order.getOrderAmountItemByType('PROMOTION_PRICE')}元=${order.oughtAmountYuan}元)</p>
		            <table class="p_table table_center">
               		<thead>
                		<tr>
                            <th>房型</th>
                            <th>起始日期</th>
                            <th>截止日期</th>
                            <th>房价</th>
                            <th>早餐</th>
                            <th>担保时间</th>
                            <th>最晚预定</th>
                        </tr>
                     </thead>
   					 <tbody>
                        <#list hotelTimeRateInfoList as hotelTimeRateInfo> 
		                    <tr>
		                        <td>
		                        ${orderItem.contentMap['branchName']}( ${orderItem.suppGoodsName} )
		                        </td>
		                        <td>${hotelTimeRateInfo.startDate?string('yyyy-MM-dd') !''}</td>
		                        <td>${hotelTimeRateInfo.endDate?string('yyyy-MM-dd') !''}</td>
		                        <td>RMB ${hotelTimeRateInfo.housePrice/100!''}</td>
		                      <td>
		                       <#if hotelTimeRateInfo.breakfastTicket==0> 
			                  	无
								<#elseif hotelTimeRateInfo.breakfastTicket==1> 
			                  	单早
			                  	<#elseif hotelTimeRateInfo.breakfastTicket==2> 
			                  	双早
			                  	<#elseif hotelTimeRateInfo.breakfastTicket==3> 
			                  	三早
			                  	<#else> 
			                  	${hotelTimeRateInfo.breakfastTicket}早
		                  		</#if> 
		                  		</td>
		                       <td>
			                       <#if hotelTimeRateInfo.guaranteeTime!=null> 
	                         			${hotelTimeRateInfo.guaranteeTime!''}:00
	                        		</#if> 
	                        	</td>
		                       <td>${hotelTimeRateInfo.lastTime!''}</td>
		                    </tr>
		                </#list>
                        </tbody>
                    </table>
		            <p>订单费用需要您<span class="cc6 f14">${payStr}</span></p>
		            <#if order.paymentTarget=='PAY'> 
                  		<#if order.bookLimitType=='NONE'> 
                  			
						<#elseif order.bookLimitType=='TIMEOUTGUARANTEE'> 
							<@brf.isTimeOut isTimeOut />
	                  	<#elseif order.bookLimitType=='ALLTIMEGUARANTEE'> 
	                  		<@brf.isTimeOut isTimeOut />
	                  	<#elseif order.bookLimitType=='ALLGUARANTEE'> 
	                  		<@brf.isTimeOut isTimeOut />
	                  	<#else> 
	                  		 
	              		</#if> 
                  	<#else> 
                  		<#if orderItem.deductType??>
                  			<#if "UNRETREATANDCHANGE"==orderItem.cancelStrategy>
                  				订单一经预订成功，不可变更/取消，如未按时入住，将按订单全额扣款。
	                  		<#elseif order.paymentType=='PREAUTH'> 
	                  			<#if isTimeOut=='true'>
	                  				<p> 在预授权完成后，若您需要修改和取消本次预订，请在<span class="cc7 f14">${order.lastCancelTime?string('yyyy-MM-dd HH:mm')}</span>前联系我们，过时不可修改取消，未入住将会扣除您<@brf.deductType orderItem.deductType /> <span class="cc7 f14">${deductAmountToYuan}</span>元。</p>
		                  		<#else>
		                  			<p> 在预授权完成后，不得修改和取消，未入住将会扣除您<@brf.deductType orderItem.deductType /> <span class="cc7 f14">${deductAmount}</span>元。</p>
		                  		</#if> 
		                  	<#else> 
		                  		<p> 在支付完成后，若您需要修改和取消本次预订，请在<span class="cc7 f14">${order.lastCancelTime?string('yyyy-MM-dd HH:mm')}</span>前联系我们，过时不可修改取消，未入住将会扣除您<@brf.deductType orderItem.deductType /> <span class="cc7 f14">${deductAmountToYuan}</span>元。</p>
		              		</#if>
	              		</#if>
              		</#if> 
		            
		            <p>您的联系手机是 <span class="cc7 f14">${order.contactPerson.mobile}</span>。感谢您的预订，您的订单已经提交，房间确认后给您发送短信！</p>
		            <p>请问还有什么可以帮您？</p>
		            <p>祝您旅途愉快，感谢您的来电，再见。</p>
		            <p>酒店地址：<strong>${suppGoods.prodProduct.propValue['address'] } </strong>电话：<strong>${suppGoods.prodProduct.propValue['telephone'] }</strong></p>
                    <#if isDestBuOrder =='true'>
                        <#if order.paymentTarget=='PREPAID'>
                            <p><span class="cc6 f14">请您在${order.waitPaymentTime?string('yyyy-MM-dd HH:mm')}时间之前完成支付，过期订单将自动取消</span></p>
                        </#if>
                    </#if>    
                    <div class="fl operate" style="text-align:right;width:100%">
                    <#if isDestBuOrder =='true'>
                        <#if order.paymentTarget=='PREPAID'>
                            <#if !order.hasNeedPay()><a id="confirmAndPayBt" class="btn btn_cc1" href="javascript:void(0);" onclick="confirmAndPayBt();">确认并支付</a></#if>
                        <#else>
                            <a id="confirmBt" class="btn btn_cc1" href="javascript:void(0);" onclick="confirmBt();">确认</a>
                        </#if>
                    <#else>
                        <#if !order.hasNeedPay()><a id="confirmAndPayBt" class="btn btn_cc1" href="javascript:void(0);" onclick="confirmAndPayBt();">确认并支付</a></#if>
                        <a id="confirmBt" class="btn btn_cc1" href="javascript:void(0);" onclick="confirmBt();">确认</a>
                    </#if>
		            <!--<a id="confirmBt" class="btn btn_cc1" href="javascript:void(0);" onclick="confirmBt();">确认</a>-->
		            <a id="backUpBt" class="btn" href="javascript:void(0);" onclick="backUpBt();">返回修改</a>
		            </div>
		    </div>
	</div>
<script type="text/javascript" src="/vst_order/js/jquery.messager.js"></script>
<script type="text/javascript">
	(function($) {
  
	     $.extend({
	        /**
	         * 调用方法： var timerArr = $.blinkTitle.show();
	         *          $.blinkTitle.clear(timerArr);
	         */
	        blinkTitle : {
	            show : function() { //有新消息时在title处闪烁提示
	                var step=0, _title = $('title', window.parent.document).text(); //document.title;
	                //console.log("_title"+_title); 
	  				
	                var timer = setInterval(function() {
	                    step++;
	                    if (step==3) {step=1};
	                    if (step==1) {
	                    	//console.log("_title1");
	                    	$('title', window.parent.document).html('【　　　】'+_title);
	                    };
	                    if (step==2) {
	                    	$('title', window.parent.document).html('【新消息】'+_title);
	                    	//console.log("_title2");
	                    };
	                }, 500);
	  
	                return [timer, _title];
	            },
	  
	            /**
	             * @param timerArr[0], timer标记
	             * @param timerArr[1], 初始的title文本内容
	             */
	            clear : function(timerArr) {    //去除闪烁提示，恢复初始title文本
	                if(timerArr) {
	                    clearInterval(timerArr[0]); 
	                    //console.log("_title_2"+timerArr[1]); 
	                    $('title', window.parent.document).html(timerArr[1]);
	                };
	            }
	        }
	    });
	   })(jQuery);
	   var timerArr_ ;//全局变量，用于关闭消息
	   function win_title (){
			var timerArr = $.blinkTitle.show();
		  	timerArr_ = timerArr;
		    setTimeout(function() {     //此处是过一定时间后自动消失
		        $.blinkTitle.clear(timerArr);
		    }, 60000);
		}
		
	   //消息浮层
	    var orderId = $("input[type=hidden][name=orderId]").val();
	    var text = "订单"+orderId+"，未确认,请尽快完成操作";
		var flag = false;
		var s=120;
		function countsub(){
			if(s<=0){
				if(!flag){
					$.messager.lays(250, 150);
			        $.messager.show('信息提示', text, 0); 
			       // console.log("start"); 
			        win_title();
			        //console.log("end");
					flag = true;
				} 
			}else { 
				s--;
			}
		}  
   		var iCount = setInterval('countsub()',1000); 
   		//关闭消息
		function closeMsg(){
			//console.log(timerArr_);
			clearInterval(iCount);
			$.blinkTitle.clear(timerArr_);
		}
   		//end
function confirmAndPayBt(){
	closeMsg();
	 window.open("/vst_order/order/orderStatusManage/showOrderStatusManage.do?orderId="+'${order.orderId}');
	 confirmOrderDialog.close();
}
function confirmBt(){
	 closeMsg();
	 alert("提交成功");
	 confirmOrderDialog.close();
}
function backUpBt(){
	 closeMsg();
	 window.open("/vst_order/order/orderStatusManage/showOrderStatusManage.do?orderId="+'${order.orderId}');
	 confirmOrderDialog.close();
}
</script>