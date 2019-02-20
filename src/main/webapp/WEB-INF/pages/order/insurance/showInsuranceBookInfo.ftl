<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-后台下单</title>
<#include "/base/head_meta.ftl"/>
<link rel="stylesheet" href="/vst_order/js/book/calendar.css" type="text/css"/>
<link rel="stylesheet" href="/vst_order/js/tooltip/css/global.css" type="text/css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/pa-base.css,/styles/v4/modules/arrow.css,/styles/v4/modules/button.css,/styles/v4/modules/forms.css,/styles/v4/modules/selectbox.css,/styles/v4/modules/step.css,/styles/v4/modules/tags.css,/styles/v4/modules/tip.css,/styles/v4/modules/dialog.css,/styles/v4/modules/tables.css" />
<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/modules/dialog.css,/styles/v4/modules/calendar.css,/styles/v4/modules/tables.css,/styles/v4/modules/bank.css,/styles/v4/order-common.css,/styles/v4/order.css" />
<link href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/button.css" rel="stylesheet" type="text/css">
<link rel="stylesheet" href="/vst_order/css/ticketOrder.css" type="text/css" />
</head>
<body>
		<#--页面导航-->
		<div class="iframe_header">
		        <i class="icon-home ihome"></i>
		        <ul class="iframe_nav">
		            <li><a href="#">首页</a> &gt;</li>
		            <li><a href="#">订单管理</a> &gt;</li>
		            <li class="active">新建保险订单</li>
		        </ul>
		</div>
		<div id="userInfoDiv" style="text-align:center;position:fixed;margin-left:500px;margin-top:-30px;background:#F9FAFB;"> 
			<#if user.userId?? >
			    <span style="color: #EE3388;font-size:22px;font-weight: bold;">客人姓名：${user.userName}</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="accountLogout();">退出当前用户</a>
			<#else>
				<span style="color: #EE3388;font-size:22px;font-weight: bold;">尚未登陆会员信息</span>
			</#if>
		</div>
	
		<div class="iframe_content">
       		<div class="hotel_title">
	        	<div class="hotel_t">
		        	产品名称：${suppGoods.prodProduct.productName}
	            </div>
	            <div class="operate ml50 hotel_link">
			    	<a class="btn btn_cc1" href="/vst_order/ord/insurance/preQuery.do?userId=${user.userId}" id="backSearchA">返回搜索</a>
			    </div>
        	</div>
			<#--表单区域-->
			<form id="orderForm" name="orderForm" method="POST" action="/vst_order/ord/book/visa/createOrder.do">
				<input type="hidden" name="userId" value="${user.userId}"/>
				<input type="hidden" name="productId" value="${suppGoods.prodProduct.productId}"/>
				<input type="hidden" name="sameVisitTime" value="true"/>
				<h5 class="hotel_tab_title">游玩日期</h5>
             	<div class="hotel_tab">
					<table width="100%">
						<tbody>
					      <tr>
					            <td >
					                <div style="width: 345px;">
							            	<p>
							            		请选择游玩日期:
							            		<input type="text" id="visitTime" name="visitTime" 
							            			style="width:100px;" value="${bookOrderVisitTime?string('yyyy-MM-dd')}" class="iflt_date" 
							            			data-type="calendar" onblur="" readonly="readonly" required/>
								            </p>					                    	
					                </div>
					            </td>
					      </tr>
					    </tbody>
					</table>             		
             	</div>
				<div class="p_box mt20">
					<h5 class="hotel_tab_title">商品选择</h5>
           	 		<div id="visaDiv" class="hotel_tab">
            			<div class="table_t">商品选择</div>
	        			<#include "/order/insurance/goods_info.ftl"/>
            		</div>
      		  	</div>
  				<h5 class="hotel_tab_title">订单信息</h5>
             	<div class="hotel_tab">
             		<#assign isFaxBreakRemark=true/>
             		<#include "/order/orderRemark.ftl"/>
 					<br/>
             		<div class="hotel_submit" id="totalOrderPriceDiv">
                		<p>
                			<#--<b style="font-size:14px;font-weight: bold;">
                				订单总价：
                			</b>
                			产品费用0元-优惠券0元-促销活动0元=0元-->
                		</p>
                    	<a id="orderSubmitA" href="javascript:void(0);" class="btn btn-orange" submitFlag="true">提交并填写订单信息</a>
                	</div>
             		<div class="table_t">退改规则</div>
             		<table width="100%">
                		<tbody>
                    		<tr>
	                        	<td id="cancelStrategyTd">请在2014-10-20 18:00:00之前取消订单，逾期将收取违约金</td>
                       		</tr>
                    	</tbody>
                	</table>
             	</div>
        	</form>
		</div>
<#include "/order/orderProductQuery/member_dialog.ftl"/>  
<#include "/base/foot.ftl"/>
<script type="text/javascript">var basePath = '${rc.contextPath}';</script>
<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script><!-- 人员登陆 -->
<script src="/vst_order/js/book/pandora-calendar.js"></script><!-- 日历控件 -->
<script src="/vst_order/js/tooltip/js/jtip.js"></script><!-- 未知 -->
<script src="/vst_order/js/book/bookCheck.js"></script><!-- 库存检查  -->
<script src="/vst_order/js/book/visa/bookVisa.js"></script><!-- 价格刷新 -->
<script src="/vst_order/js/book/order_promotion.js"></script><!-- 促销 和 奖金抵扣券 -->
<script src="/vst_order/js/book/order_travellers.js"></script><!-- 游玩人 -->
<script src="/vst_order/js/book/coupon_allCategroy.js?version=2014091412345"></script><!-- 优惠券 -->
<script src="/vst_order/js/book/express.js?2014091512345"></script><!-- 快递 -->
<script>
	var orderInfoDialog;
	var selectUserDialog;
	var book_user_id='${user.userId}';
	//日历初始化
	$(function(){
	    pandora.calendar.init({ 
	    	area: true, 
		    input: '.iflt_date',
		    inputClass: 'iflt_date',
		    showWeek:true,
		    selectDateCallback: function(){
				calendarConfirmCallback.invoke();//点击选择日期后的回调函数 
			}
	     });
    });
	// 提交订单事件绑定
	$('#orderSubmitA').bind('click',function(){
		if(book_user_id==""){
	      	showQueryUserIdDialog();
	      	return;
	      }else{
	      	submitFormCallback.invoke();
	      }
		
	});
	//订单提交方法
	function submitOrder(){
		var submitFlag=true;
		 $('#visaDiv select.hotel_sum').each(function(i,v){
				 var num=$(v).val();
				 var mainItem=$(v).attr("mainItem");
				 var integerReg = "^[1-9]*[0-9]*$";
					if(!num.match(integerReg)){
						submitFlag=false;
					}
			});
			if(!submitFlag){
				$.alert("请选择商品数量");
			}
			if($('#orderSubmitA').attr('submitFlag')=="true"){
					//验证
			if(!$("form[name='orderForm']").validate().form()){
				return false;
			}
			var checkFlag=true;
			$('input[type=text]').each(function(){
				 var text=$(this).val();
				 var nameType=$(this).attr("name_type");
				 if(nameType=="fullName"){
				 	  var patt1 = new RegExp("^[a-zA-Z]{2,20}$");
				 	  var patt2 = new RegExp("^[\u4e00-\u9fa5]{1,10}$");
				 	 if(!patt1.test(text)&&!patt2.test(text)){
						//提示
						$(this).next("span").css("display","");
						checkFlag=false;
					}else{
						$(this).next("span").css("display","none");
					}
				 }else if(nameType=="mobile"){
				 	 var myreg = /^\d+$/;
    				if(!myreg.test(text)){
						//提示
						$(this).next("span").css("display","");
						checkFlag=false;
					}else{
						$(this).next("span").css("display","none");
					}
				 }else if(nameType=="youbian"){
			        var patt1= /^[1-9][0-9]{5}$/
			        if(!patt1.test(text)){
			           $(this).next("span").css("display","");
						checkFlag=false;
					}else
			        {
			          $(this).next("span").css("display","none");
			        }
				 }else if(nameType=="address"){
				 	var city2Code = $("#js_city2").find('option:selected').attr("value");
			        if($("#user_address").val().length>100||city2Code == "选择市"){
			           $(this).next("span").css("display","");
						checkFlag=false;
					}else
			        {
			          $(this).next("span").css("display","none");
			        }
				 }
			});
			if(!checkFlag){
					return;
			}
		      //生成订单开始
			  orderInfoDialog = new xDialog("/vst_order/ord/book/comm/createOrder.do",
								      $("form[name='orderForm']").serialize(),
								      {title:"订单信息填写",width:800});
			}
	}
	//库存不足
    BACK.BOOK.CHECK.checkStock();
    //商品数量改变 - 触发
	bookNumOnchangeCallback.pushFun(BACK.BOOK.CHECK.checkStock);
	//点击选择日期 - 触发
	calendarConfirmCallback.pushFun(VST.VISA.ORDER.refereshTimePrice);
	calendarConfirmCallback.pushFun(BACK.BOOK.CHECK.checkStock);	
	submitFormCallback.pushFun(submitOrder);
</script>
</body>
</html>