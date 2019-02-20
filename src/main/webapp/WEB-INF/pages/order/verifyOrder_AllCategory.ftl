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
		            <p><strong><span class="cc6 f14"><#if order.getBookerPerson()??> </#if>${order.getBookerPerson().fullName} </span>您好，</strong>我再和您核对一下</p>
		            <p>您预订了<span class="cc6 f14"> ${order.mainOrderItem.visitTime?string('MM月dd日')}（${weekStr}）</span>的
		            <span class="cc6 f14">
		            	<#if order.getOrdOrderPack()??>
		            	${order.getOrdOrderPack().getProductName()}
		            	<#else>
		            	${order.mainOrderItem.productName}
		            	</#if>
		            </span>订单号：<span class="cc6 f14">${order.orderId}</span></p>
		            <p>特殊要求：<span class="cc6 f14">${order.remark}</span></p>
		           	<p> 入住/游玩人信息：</p>
		            <table class="p_table table_center">
               		<thead>
                		<tr>
                            <th>类型</th>
                            <th>中文姓名</th>
                            <th>英文姓名</th>
                            <th>证件类型</th>
                            <th>证件号码</th>
                            <th>有效期</th>
                            <th>签发地</th>
                            <th>证件明细</th>
                            <th>人群</th>
                            <th>性别</th>
                            <th>联系方式</th>
                        </tr>
                     </thead>
   					 <tbody>
	   					 	<tr>
		                        <td>取票人/联系人</td>
		                        <td>${order.contactPerson.fullName}</td>
		                        <td>${order.contactPerson.firstName}&nbsp;${order.contactPerson.lastName}</td>
		                        <td>${order.contactPerson.idTypeName}</td>
		                       
		                        <td>${order.contactPerson.idNo}</td>
		                        <td></td>
		                        <td></td>
		                        <td>
		                        <#if order.contactPerson?? && order.contactPerson.birthday??>
		                        	${order.contactPerson.birthday?string('yyyy-MM-dd')}
		                        </#if>
		                        <#--<hr/>有效期-->
		                        </td>
		                        <td>${order.contactPerson.peopleTypeName}</td>
		                        <td>${order.contactPerson.genderName}</td>
		                        <td>
		                        	${order.contactPerson.mobile}
			                        <#if order.contactPerson?? && order.contactPerson.mobile?? && order.contactPerson.email??>
			                        	<hr/>
			                        </#if>
		                        	${order.contactPerson.email}
		                        </td>
		                    </tr>
	                    <#list order.ordTravellerList as traveller>
		                    <tr>
		                        <td>游客</td>
		                        <td>${traveller.fullName}</td>
		                        <td>${traveller.firstName}&nbsp;${traveller.lastName}</td>
		                        <td>
		                        ${traveller.idTypeName}
		                        </td>
		                        <td>
		                        <#if traveller.idTypeName!="客服联系我">
		                        ${traveller.idNo}
		                        </#if></td>
		                        <td>
		                         <#if traveller?? && traveller.expDate??>
		                        ${traveller.expDate?string('yyyy-MM-dd')}
		                        </#if>
		                        </td>
		                        <td>${traveller.issued}</td>
		                        <td>
		                        	<#if traveller?? && traveller.birthday??>
			                        	${traveller.birthday?string('yyyy-MM-dd')}
			                        </#if>
			                         <#--<hr/>有效期-->
		                        </td>
		                        <td>${traveller.peopleTypeName}</td>
		                        <td>${traveller.genderName}</td>
		                        <td>
			                       	${traveller.mobile}
			                         <#if traveller?? && traveller.mobile?? && traveller.email??>
			                        	<hr/>
			                        </#if>
		                        	${traveller.email}
		                        </td>
		                    </tr>
	                    </#list>
                      </tbody>
                    </table>
                    <br/>
	                <p>订单总金额为<span class="cc6 f14">${order.oughtAmountYuan}</span>元。(产品费用${order.noContainsInsuranceProductAmountYuan}元+保险${order.getInsuranceAmountYuan()}元+快递${order.expressAmountYuan}元<#if order.categoryId =='28'>+押金${order.depositAmountYuan}元</#if>-优惠券<#if order.couponAmount??>${order.couponAmount/100}</#if>元-促销活动${order.getOrderAmountItemByType('PROMOTION_PRICE')}元=${order.oughtAmountYuan}元)</p>
		            <p>订单费用需要您：<span class="cc6 f14"><#if !order.hasNeedPay()>预付<#else>现付</#if></span></p>
		            <p>退改规则：<span class="cc6 f14">
		            		<#if order.mainOrderItem??&&order.mainOrderItem.cancelStrategy??>
		            			<#if order.cancelStrategy=='RETREATANDCHANGE'>
		            				请在${order.lastCancelTime?string('yyyy-MM-dd HH:mm:ss')}之前取消订单 逾期将收取违约金${order.getDeductAmountToYuan()}元
		            			<#elseif order.cancelStrategy=='UNRETREATANDCHANGE'>
		            				预订成功后，如修改或者取消订单，将收取订单的总金额作为违约金
		            			<#elseif order.cancelStrategy=='MANUALCHANGE'>
		            				人工退改
		            			<#else>
		            				无
		            			</#if>
		            		<#else>
		            			无
		            		</#if>
		            </span>
		            </p>
		            <p>您的联系手机是 <span class="cc6 f14">${order.contactPerson.mobile}</span> 。 感谢您的预订。</p>
		            <p>
		                <#if isDestBuOrder =='true'>
                          <!--  <#if order.hasNeedPrepaid() && order.paymentStatus == 'UNPAY'>
                                                                                                请于 ${order.paymentTime}前完成支付！
                            </#if>  -->
                            <#if !order.hasInfoAndResourcePass() && order.hasNeedPrepaid()>
                                                                                                 库存确认后，短信通知您！
                            </#if>              
                            <#if order.paymentTarget=='PREPAID'>
                                <p><span class="cc6 f14">请您在${order.waitPaymentTime?string('yyyy-MM-dd HH:mm')}时间之前完成支付，过期订单将自动取消</span></p>
                            </#if> 
                        </#if>
                        <#if isDestBuOrder =='false'>
		            	    <#if order.hasNeedPrepaid()&&order.isPayMentType()>
		            		              由于资源紧张，请到网站前台进行支付，订单才能确认是否有资源。
		            	    <#elseif order.hasNeedPrepaid()&&!order.isPayMentType()&&!order.hasInfoAndResourcePass()>
		            		              库存确认后，短息通知您！
		            	    <#elseif order.hasNeedPrepaid()&&!order.isPayMentType()&&order.hasInfoAndResourcePass()>
		            		              请于 ${order.paymentTime}前完成支付！
		            	    <#elseif order.hasNeedPay()&&!order.hasInfoAndResourcePass()>
		            		              库存确认后，短息通知您！
		            	    <#elseif order.hasNeedPay()&&order.hasInfoAndResourcePass()>
		            		              确认短信稍后通知您！
	            		    </#if>
	            		</#if>
		            </p>
		            <#if isDestBuOrder =='false'>
		            <h2 class="cc6" style="font-size:20px;">温馨提示请点确认或者确认并支付按钮否则订单30分钟内将会被自动废单。</h2>
		            </#if>
		            <div class="fl operate" style="text-align:right;width:100%">
		            <a id="backUpBt" class="btn" href="javascript:void(0);" onclick="cancelOrderBt();">取消订单</a>
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
		            <!--<#if !order.hasNeedPay()><a id="confirmAndPayBt" class="btn btn_cc1" href="javascript:void(0);" onclick="confirmAndPayBt();">确认并支付</a></#if>-->
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
	  				
	                var timer = setInterval(function() {
	                    step++;
	                    if (step==3) {step=1};
	                    if (step==1) {
	                    	$('title', window.parent.document).html('【　　　】'+_title);
	                    };
	                    if (step==2) {
	                    	$('title', window.parent.document).html('【新消息】'+_title);
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
			        //console.log("start"); 
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
var isSubmitOrder = false;
var isCancelOrder = false;
function confirmAndPayBt(){
	closeMsg();
 	 completeOrder("confirmAndPay");
	 //window.open("/vst_order/order/orderManage/showOrderStatusManage.do?orderType=parent&orderId="+'${order.orderId}');
	 //confirmOrderDialog.close();
}
function confirmBt(){
	 closeMsg();
	 completeOrder("confirm");
	 //confirmOrderDialog.close();
}

function completeOrder(type){
 	if(isSubmitOrder){
		return;
	}
	isSubmitOrder = true;
	var loading	= pandora.loading("正在提交订单...");
	var orderId = $("input[type=hidden][name=orderId]").val();
	$.ajax({
		url : '/vst_order/ord/book/completeOrder.do',
    	type: "POST",
   	    dataType: "html",
    	data : {"orderId":orderId},
    	success: function(msg){
    		isSubmitOrder = false;
    		loading.close();
    		if(msg=="success"){
    	 		$.alert("订单提交成功",function(){
    	 				if(type=="confirmAndPay"){
    	 					window.open("/vst_order/order/orderManage/showOrderStatusManage.do?orderType=parent&orderId="+'${order.orderId}');
    	 				}
    	 				var url=$("#backSearchA").attr("href"); 
    	 				window.location.href=url;
    	 				confirmOrderDialog.close();
    	 		});
    		}else {
    			$.alert("订单提交失败");
    		}
        },
        error: function() {
        	isSubmitOrder = false;
        	loading.close();
        }
	});
}
function cancelOrderBt(){
	 if(isCancelOrder){
		return;
	}
	isCancelOrder = true;
	closeMsg();
	var loading	= pandora.loading("正在取消订单...");
	var orderId = $("input[type=hidden][name=orderId]").val();
	$.ajax({
		url : '/vst_order/ord/book/cancelOrder.do',
    	type: "POST",
   	    dataType: "html",
    	data : {"orderId":orderId},
    	success: function(msg){
    		isSubmitOrder = false;
    		loading.close();
    		if(msg=="success"){
    	 		$.alert("订单取消成功",function(){
    	 				confirmOrderDialog.close();
    	 		});
    		}else {
    			$.alert("订单取消失败");
    		}
        },
        error: function() {
        	isCancelOrder = false;
        	loading.close();
        }
	});
}
function backUpBt(){
	 window.open("/vst_order/order/orderManage/showOrderStatusManage.do?orderType=parent&orderId="+'${order.orderId}');
	 //confirmOrderDialog.close();
}
</script>