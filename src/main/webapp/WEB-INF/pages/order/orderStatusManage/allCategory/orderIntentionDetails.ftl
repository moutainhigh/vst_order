<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>意向单详细信息</title>
<style type="text/css">
.halfleft{
width:300px;float:left;color:#333333;
}
.halfright{
width:280px;float:left;color:#333333;
}
.allv{
width:580px;float:left;color:#333333;
}
</style>
<#include "/base/head_meta.ftl"/>
</head>
<body>

    <div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="javaScript:">首页</a>：</li>
            <li><a href="javaScript:">订单管理</a> ></li>
            <li class="active">意向单详情</li>
        </ul>
    </div>
    <form method="post" id="dataForm" onsubmit="return false;">
    <div class="order_main iframe_content mt10" id="iframeDiv">
        <span  class="f16 fl">
        <div class="halfleft">意向单号：<strong>${intentionOrder.intentionOrderId}</strong></div>
         <div class="halfleft">订单号：<strong>${intentionOrder.orderId}</strong></div>
        <div class="halfright">提交时间：${intentionOrder.createTimeStr}</div>
        </span>
        </br>
        <div class="solid_border"></div>
             <br>
        <span  class="f16 fl" style="font-size:14px;">
              <strong> 线路相关信息</strong></br></br>                    
           <div class="main_order_info" style="font-size:14px;color:#333333;">
              <div class="halfleft">线路ID:${intentionOrder.productId}</div>
              <div class="halfright">所属产品经理:${intentionOrder.productManager}</div></br></br>
              <div class="allv">线路名称:${intentionOrder.productName}</div></br></br>
              <div class="halfleft">出行日期：${intentionOrder.travelTimeStr}</div>
              <div class="halfright">出行人数：成人*${intentionOrder.adultCounts}    儿童*${intentionOrder.childrenCounts}</div></br></br> 
              <strong> 联系人相关信息</strong></br></br> 
              <div class="halfleft">登录用户:${intentionOrder.loginName!''}</div>
              <div class="halfright">联系人姓名:${intentionOrder.contactsName!''}</div></br></br>
              <div  class="halfleft">手机号码:${intentionOrder.tel}</div>
              <div class="halfright">电子邮箱:${intentionOrder.email}</div>
            </div></br>
             <div class="operate mt10"> 
             <#if intentionOrder.orderId?exists>
             <a class="btn btn_cc1"  disabled="true" >后台下单</a>
             <#else>
              <a class="btn btn_cc1" id="createButton" >后台下单</a>
              </#if>
             </div>
        </span>
        <div style="width:300px;float:left;background:#E6F4FA;color:#333333" >
            <div class="main_con clearfix" >
                <div class="main_order_msg"">
                    <div>备注信息:</div>
                      <textarea style="width:285px; height:120px;" id="intentionRemark" name="intentionRemark" onkeyup="checkRemarkLength()"></textarea>
                      <span class="fr" id="zsRemark">0/500字</span>
                    <div>修改订单状态:</div>
                    <div>
                    
                       <#-- <input type="radio" name="IntentionState" value="0"  onclick="hideReason()" />待&nbsp;跟&nbsp;进&nbsp;&nbsp;&nbsp;&nbsp;
                          <#-- <#if intentionOrder.orderId?exists >
	                         	 	<input type="radio" name="IntentionState"  disabled="true"/>已&nbsp;下&nbsp;单&nbsp;&nbsp;&nbsp;&nbsp;
	                     <#else>
	                         		 <input type="radio" name="IntentionState" value="1"  onclick="hideReason()" />已&nbsp;下&nbsp;单&nbsp;&nbsp;&nbsp;&nbsp;
	                    </#if> -->
	                   
	                    <input type="radio" name="IntentionState" value="2" <#if intentionOrder.state == '2'>checked="checked"</#if>  onclick="showReason()"/>已&nbsp;取&nbsp;消	
                    </div></br>
                    <div id="reasonDiv" hidden="true">
                         <strong>取消原因</strong></br>
                         <input type="radio" name="cancelReason" value="0"  onclick=" " />资&nbsp;源&nbsp;不&nbsp;确&nbsp;定&nbsp;&nbsp;
						 <input type="radio" name="cancelReason" value="1"  onclick=" " />用&nbsp;户&nbsp;通&nbsp;知&nbsp;取&nbsp;消&nbsp;&nbsp;</br>
						 <input type="radio" name="cancelReason" value="2"  onclick=" "/>信&nbsp;息&nbsp;不&nbsp;通&nbsp;过&nbsp;&nbsp;	
						 <input type="radio" name="cancelReason" value="3"  onclick=" "/>其&nbsp;它&nbsp;取&nbsp;消&nbsp;&nbsp;
                    </div>
                     <div class="operate mt10"><a class="btn btn_cc1" id="saveButton" >确认修改</a></div>
                     <input type="hidden" name="updateState"  value="detail">
                </div>                         
           </div>
         </div>
   </div>         
  </form>
     
    <#include "/base/foot.ftl"/>
    
    <form id="bookForm" action="" method="post">
    					<input type="hidden" name="specDate" value="${intentionOrder.travelTime?string("yyyy-MM-dd")}">
  						<input type="hidden" name="productId"  value="${intentionOrder.productId}">
  						<input type="hidden" name="distributionId"  value="2">
  						<input type="hidden" name="userId"  value="${intentionOrder.loginId!''}">
  						<input type="hidden" name="intentionOrderId"  value="${intentionOrder.intentionOrderId}">
  						<input type="hidden" name="childNum"  value="${intentionOrder.childrenCounts}">
  						<input type="hidden" name="adultNum"  value="${intentionOrder.adultCounts}">
  	
  </form>
</body>
</html>
<script type="text/javascript">
     function showReason(){
	   	var reason = $("#reasonDiv");
	   	reason.show();
	   	}
	function hideReason(){
		var reason = $("#reasonDiv");
			reason.hide();	
	}
	
	$("#saveButton").bind("click",function(){
		var intentionOrderId = ${intentionOrder.intentionOrderId};
		var intentionState= $('input[name="IntentionState"]:checked').val();
		var cancelReason = $('input[name="cancelReason"]:checked').val();	
		var intentionRemark = $("#intentionRemark").val();	
		var updateState =$('input[name="updateState"]').val(); 	
		$.ajax({
		   url : "/vst_order/ord/order/updateIntentionState.do",
		   data : { intentionOrderId:intentionOrderId,
		               intentionState:intentionState,
		               cancelReason:cancelReason,
		               intentionRemark:intentionRemark,
		               updateState:updateState
		   },
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
		   		if(result.code=="success"){   		
		   			$.alert(result.message);
		   		}else {
		   			$.alert(result.message);
		   		}
		   }
		});						
	});
	
	function checkRemarkLength(){
	        var orderRemark=document.getElementById('orderRemark');
	        var remarkLength=orderRemark.value.length;
	        if(remarkLength>500)
	        {
		        $("#saveButton").attr("disabled",true);
		        $("#saveButton").hide();
		         $("#zsRemark").attr("style","color:red");
	        }else{
	        	$("#saveButton").removeAttr("disabled");
	        	$("#saveButton").show();
	        	$("#zsRemark").attr("style","");
	        }
	        $("#zsRemark").html(remarkLength+"/500字");
        }
        
     $("#createButton").bind("click",function(){
     
		$("#bookForm").attr("action","/vst_order/ord/order/queryLineDetailList.do");
		
		$("#bookForm").submit();
		
	 });
</script>



