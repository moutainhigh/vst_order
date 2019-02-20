<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->


<div id="logResultList" class="divClass">
			<div class="order_msg clearfix">
              		</br>
	         	<strong>
	         		游玩人须知
	         		<a class="btn btn_cc1" id="saveConfirm" href="javaScript:" >保存</a>
	         	</strong>
			</div>
			<form id="subForm" action="/ord/order/update/updateTravellerConfirm.do" method="POST">
			<input type="hidden" name="orderId" value="${ordOrderTravellerConfirm.orderId}"/>
	 		<table class="p_table table_center mt20">
	                <thead>
	                    <tr>
	                    
	                    </tr>
	                </thead>
	                <tbody>
		            <tr>
		            	<td style="border-right:#fff 0px solid;text-align:left">
		            		请确认出游人是否包含60周岁（含）以上老人同行
		            	</td>
		            	<td style="border-left:#fff 0px solid;">
			            	<input type="radio" name="containOldMan" value="Y"  <#if ordOrderTravellerConfirm.containOldMan== 'Y'>checked="checked"</#if>/>有
			            	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			            	<input type="radio" name="containOldMan" value="N" <#if ordOrderTravellerConfirm.containOldMan== 'N'>checked="checked"</#if>/>没有
		            	</td>
		            </tr>
		            <tr>
		            	<td style="border-right:#fff 0px solid;text-align:left">
		            		请确认是否有外籍人士一同出游
		            	</td>
		            	<td style="border-left:#fff 0px solid;">
			            	<input type="radio" name="containForeign" value="Y" <#if ordOrderTravellerConfirm.containForeign== 'Y'>checked="checked"</#if>/>有
			            	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			            	<input type="radio" name="containForeign" value="N" <#if ordOrderTravellerConfirm.containForeign== 'N'>checked="checked"</#if>/>没有
		            	</td>
		            </tr>
		            <tr>
		            	<td style="border-right:#fff 0px solid;text-align:left">
		            		请确认同行中是否有孕妇
		            	</td>
		            	<td style="border-left:#fff 0px solid;">
			            	<input type="radio" name="containPregnantWomen" value="Y" <#if ordOrderTravellerConfirm.containPregnantWomen== 'Y'>checked="checked"</#if>/>有
			            	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			            	<input type="radio" name="containPregnantWomen" value="N" <#if ordOrderTravellerConfirm.containPregnantWomen== 'N'>checked="checked"</#if>/>没有
		            	</td>
		            </tr>
		         </tbody>
	            </table>
	            </form>
</div>
<#--页脚-->
</body>
</html>
<script type="text/javascript">

$("#saveConfirm").bind("click",function(){

	var loading = pandora.loading("正在努力保存中...");
	var formData=$("#subForm").serialize();
	 $.ajax({
		   url : "/vst_order/ord/order/update/updateTravellerConfirm.do",
		   data : formData,
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		if(result.code=="success" ){
		   			loading.close();
		   		  alert(result.message);
		   		  window.location.reload();
		   		}else {
		   			loading.close();
		   			alert(result.message);
		   		  	window.location.reload();
		   		}
		   }
		});	 
 });

</script>
