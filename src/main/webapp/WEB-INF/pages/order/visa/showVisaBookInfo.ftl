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

<link rel="stylesheet" href="/vst_order/css/order_pay.css" type="text/css"/>
<link rel="stylesheet" href="http://pic.lvmama.com/styles/v6/order.css">

</head>
<body>
	<#--页面导航-->
	<div class="iframe_header">
	        <i class="icon-home ihome"></i>
	        <ul class="iframe_nav">
	            <li><a href="#">首页</a> &gt;</li>
	            <li><a href="#">订单管理</a> &gt;</li>
	            <li class="active">新建签证订单</li>
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
        		<#if suppGoods.prodProduct.bizDistrict??>
        			<a href="http://www.lvmama.com/visa/${suppGoods.prodProduct.bizDistrict.pinyin}">
        		</#if>
	        	产品名称：${suppGoods.prodProduct.productName}
	        	<#if suppGoods.prodProduct.bizDistrict??>
	        	</a>
	        	</#if>
            </div>
            <div class="operate ml50 hotel_link">
		    	<#--<a class="btn btn_cc1" id="notifyBt">重要通知</a>-->
		    	<a id="backSearchA" class="btn btn_cc1" href="/vst_order/ord/productQuery/visa/showVisaQueryList.do?userId=${user.userId}">返回搜索</a>
		    </div>
        </div>
		<#--表单区域-->
		<form id="orderForm" name="orderForm" method="POST" action="/vst_order/ord/book/visa/createOrder.do">
		<input type="hidden" name="userId" value="${user.userId}"/>
		<input type="hidden" name="userNo" value="${user.id}"/>
		<input type="hidden" name="productId" value="${suppGoods.prodProduct.productId}"/>
		<input type="hidden" name="sameVisitTime" value="true" autocomplete="off"/>
		<div class="p_box mt20">
			<div class="hotel_wrap">
	        	<h5 class="hotel_tab_title">游玩日期</h5>
	            <div class="hotel_tab">
	            	请选择游玩日期：<input type="text" id="visitDate" name="visitTime" style="width:100px;" value="${suppGoodsSimpleTimePrice.specDate?string('yyyy-MM-dd')}"
	                    	 class="iflt_date" data-type="calendar" onblur="" readonly="readonly" required/>
	            </div>
	        </div>
		</div>
		<div class="p_box mt20">
			<h5 class="hotel_tab_title">商品选择</h5>
            <div id="visaDiv" class="hotel_tab">
            	<div class="table_t">主商品选择</div>
	        		 <#include "/order/visa/inc/goods_info.ftl"/>
             </div>
             
             <!--保险-->
             <#if existsInsurance>
             	<div id="baoxianDiv" class="hotel_tab">
            		<#include "/order/ticket/inc/insurance.ftl"/>
            	</div>
             </#if>
            
            <!--
      		<h5 class="hotel_tab_title">可选服务</h5>
			<div id="baoxianDiv" class="hotel_tab">
			<div class="table_t">保险</div>
			<table width="100%">
				<tbody>
				<#if suppGoodsSaleReList?? && (suppGoodsSaleReList?size) &gt; 0>
			      <#list suppGoodsSaleReList as suppGoodsSaleRe>
						<#if suppGoodsSaleRe.insSuppGoodsList??>
							<#list suppGoodsSaleRe.insSuppGoodsList as sg>
						    <tr suppGoodsId="${sg.suppGoodsId}">
							<input type="hidden" name="itemMap[${sg.suppGoodsId}].goodsId" value="${sg.suppGoodsId}" autocomplete="off"/>
							<td width="35%">
							<a href="javascript:void(0);" class="J_tip" tip-content=""  onmouseover="VST.VISA.ORDER.goodsDetailMouseover(${sg.suppGoodsId},this);">${sg.prodProduct.productName}(${sg.prodProductBranch.branchName}-${sg.goodsName})</a>
							<div style="display: none;">
							${sg.prodProductBranch.propValue['branch_desc']}
							</div>
							</td>
							<td width="25%"><p></p></td>
							<td width="10%" class="orange">单价：￥${sg.suppGoodsBaseTimePrice.priceYuanStr}</td>
										<td width="10%">
						<select class="hotel_sum" name="itemMap[${sg.suppGoodsId}].quantity" goodsId="${sg.suppGoodsId}" onchange="">
										<#list 0..sg.maxQuantity as num>
											<option value="${num}" <#if num==quantity>selected="selected"</#if>>${num}</option>
										</#list>
										</select>
										</td>
							<td width="10%" class="orange" id="${sg.suppGoodsId}Td">总价：￥--</td>
						<td></td>
						   </tr>
						  </#list>
					   </#if>
				     </#list>
				     </#if>
			    </tbody>
			</table>
			</div>
			-->
		 <!--优惠模块-->
      		<#--<#include "/order/coupon/coupon.ftl"/>-->
      		<#include "/order/payment/order_pay.ftl"/>
      		 
  			 <h5 class="hotel_tab_title">订单信息</h5>
             <div class="hotel_tab">
             	<#include "/order/orderRemark.ftl"/>
             	<#if suppGoods.goodsType=="EXPRESSTYPE_DISPLAY">
             	<#include "/order/express_info.ftl"/>
 				</#if>
 				<br/>
             	<div class="hotel_submit" id="totalOrderPriceDiv">
                	<p><b style="font-size:14px;font-weight: bold;">订单总价：</b>产品费用0元<#--+保险0元-->-优惠券0元-促销活动0元=0元</p>
                    <a id="orderSubmitA" href="javascript:void(0);" class="btn btn-orange" submitFlag="true">提交并填写订单信息</a>
                </div>
             	<div class="table_t">预订须知</div>
             	<table width="100%">
                	<tbody>
                    	<tr>
                        	<td width="55%">${suppGoods.prodProductBranch.propValue['visa_note']}</td>
                            <td width="5%"></td>
                            <td width="10%"class="orange"></td>
                            <td width="10%">
                            </td>
                            <td width="10%" class="orange"></td>
                            <td></td>
                        </tr>
                    </tbody>
                </table>
             </div>
        </div>
        </form>
	</div>
<#include "/order/orderProductQuery/member_dialog.ftl"/>  
<#include "/base/foot.ftl"/>
<script type="text/javascript">var basePath = '${rc.contextPath}';</script>
<script src="/vst_order/js/book/pandora-calendar.js"></script>
<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script>
<script src="/vst_order/js/tooltip/js/jtip.js"></script>
<script src="/vst_order/js/book/bookCheck.js"></script>
<script src="http://s3.lvjs.com.cn/min/index.php?f=js/v4/modules/pandora-calendar.js"></script>
<script src="/vst_order/js/book/order_promotion.js"></script>
<script src="/vst_order/js/book/order_travellers.js"></script>
<script src="/vst_order/js/book/coupon_allCategroy.js?version=2014091412345"></script>
<script src="/vst_order/js/book/visa/bookVisa.js"></script>
<script src="/vst_order/js/book/express.js?2014091512345"></script>

	<script type="text/javascript">
		var rootPath = '${rc.contextPath}';
		var thisProCategoryId = '${suppGoods.prodProduct.bizCategory.categoryId}'; 
	</script>
	<script src="http://s3.lvjs.com.cn/min/index.php?f=/js/ui/lvmamaUI/lvmamaUI.js,/js/new_v/top/header-air_new.js,/js/v4/modules/pandora-dialog.js,/js/v4/login/rapidLogin.js"></script>
	<script src="http://pic.lvmama.com/min/index.php?f=/js/v5/modules/pandora-poptip.js"></script>
	<script src="http://pic.lvmama.com/js/v6/order.js"></script>
    <script src="/vst_order/js/payment_page.js"></script>

<script type="text/javascript">
function loadJtip(){
		$('.J_tip').lvtip({
	        templete: 3,
	        place: 'bottom-left',
	        offsetX: 0,
	        events: "live" 
	    });
}
loadJtip();
</script>
<script>
var orderInfoDialog;
var selectUserDialog;
var book_user_id='${user.userId}';
	$(function(){
		//日历初始化
	    pandora.calendar.init({ area: true, 
	    input: '.iflt_date',
	    inputClass: 'iflt_date',
	    showWeek:true,
	    selectDateCallback: function(){
					calendarConfirmCallback.invoke();
				}//点击选择日期后的回调函数 
	     });
    });
    BACK.BOOK.CHECK.checkStock();
	bookNumOnchangeCallback.pushFun(BACK.BOOK.CHECK.checkStock);
	calendarConfirmCallback.pushFun(VST.VISA.ORDER.refereshTimePrice);
	calendarConfirmCallback.pushFun(VST.VISA.ORDER.refereshInsurance);
	calendarConfirmCallback.pushFun(BACK.BOOK.CHECK.checkStock);
	// 提交订单
	$('#orderSubmitA').bind('click',function(){
		if(book_user_id==""){
	      	showQueryUserIdDialog();
	      	return;
	      }else{
	      	submitFormCallback.invoke();
	      }
		
	});
	
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
		
			var visaCount=VST.VISA.ORDER.calVisaPersonCount();
		var baoxianCount=VST.VISA.ORDER.calbaoxianPersonCount();
		if(visaCount<baoxianCount){
			$.alert("选择保险的份数不能超过游玩人数.");
			
			//$('#baoxianDiv select.hotel_sum').val(visaCount);
			
			return;
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
	submitFormCallback.pushFun(submitOrder);
</script>
</body>
</html>