<script type="text/javascript">
    //订单核对取消关闭按钮
    $('.dialog-close').removeAttr('href');
	$('.dialog-close').hide();
</script>
	<div class="cc3"><a style="float:right" id="backOrderUpdate">返回修改订单</a></div>
	<input type="hidden" name="orderId" value="${orderId}"/>
	<div class="">${contract.fullName}，再和您核对一下：</div>
	<div class="">您预订了 <span style="color:red"> ${specDate} </span> 出发的《${productName}》产品；<span style="color:red">订单号：${orderId}</span></div>
	<div class="">其中包含:</div>
    <div class="box_content ">
         <table class="p_table table_center">
				${ordResult!''}
         </table>
    </div>
    <#if cancelStr??>
    <div class="">
    	<span style="color:blue;font-size:14px;font-weight:bold;"> 本产品退改说明：</span><br/>
    	${cancelStr}
    </div>
    <br/>
    </#if>
   <div class="">您的联系手机是<span style="color:red"> ${contract.mobile} </span>。感谢您的预订，资源确认后给您发短信</div>
   <div class="">请问还有什么可以帮您？感谢您的来电，祝您旅途愉快！</div>
   <div class="operate mt20" style="text-align:center"><a class="btn btn_cc1" id="submitOrder1" data="${ordId}">提交订单</a></div>
   <script type="text/javascript" src="/vst_order/js/jquery.messager.js"></script>
   <script>
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
	                    //alert(timerArr[1]);
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
   		var isSubmitOrder = false;
   		$("#submitOrder1").click(function(){
   			if(isSubmitOrder){
   				return;
   			}
   			closeMsg();
   			isSubmitOrder = true;
   			var loading	= pandora.loading("正在提交订单...");
   			var orderId = $(this).attr("data");
   			$.ajax({
	   			url : '/vst_order/ord/order/completeOrder.do?',
	        	type: "POST",
	       	    dataType: "html",
	        	data : {"orderId":orderId},
	        	success: function(msg){
	        		isSubmitOrder = false;
	        		loading.close();
	        		if(msg=="success"){
	        	 		$.alert("订单提交成功",function(){
	        	 				location.reload();
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
   			//submitOrderDialog.close();
   		});
   		$("#backOrderUpdate").click(function(){
   			closeMsg();
   			$.alert("暂未提供");
   		});
   </script>