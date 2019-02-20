<html>
	<head>
		<title>提交表单</title>
	</head>
	<body>
		<form id="yeepayPhone" action="https://pay.lvmama.com/payment/pay/yeepayPhone.do"
			name="chinaprnorder" onsubmit="return checkSubmit();return false;"  method="post">
			<div style="position:relative; width:800px;">
			         <div style="position:absolute; width:300px; height:150px; right:40px; top:5px; font-size:13px;">
			         <p>支持的银行(信用卡)：招商银行、中国银行、工商银行、建设银行、广发银行、中信银行、农业银行、民生银行、兴业银行、光大银行、单笔限额5万，日限额 无。交通银行 单笔限额3万，日限额无。平安银行、邮储银行、华夏银行、上海银行、单笔限额2万 日限额无。</p>
                     <p>支持的银行(借记卡)：工商银行、农业银行、兴业银行、光大银行、邮储银行、上海银行、北京银行、深发银行、平安银行、中信银行、浦发银行</p>
                     <p>账户限制：<b style="color:red">信用卡：来电手机必须是银行卡开户时预留的手机号才可支付；储蓄卡： 储蓄卡支付必须开通无卡支付业务（无卡支付业务是向开户行申请）才能支付</b></p>
                     <p>号段限制：<b style="color:red">目前支持的手机号段:130~139、150~159、180~183、185~189、145、147</b></p>
                     </div>
			<table width="800" border="0" align="center" cellpadding="0"
				cellspacing="0" bgcolor="#e3effe" style="border: 1px solid #6f9dd9;"
				class="table">
				<tr>
					<td colspan="2" width="100%" height="35" align="center" valign="middle">
						易宝电话支付
					</td>
				</tr>
				<tr>
					<td width="151" height="35" align="right" valign="middle">
						订单号：
					</td>
					<td width="647" height="35" align="left" valign="middle"
						class="td1">
						
							<input type="text" name="objectId" id="objectId" readonly="true"
							value="${orderId}" />
							
							
							<input type="hidden" name="orderidsess" id="orderid" readonly="true"
							value="${orderId}" />
							<input type="hidden" name="orderid" id="orderid" readonly="true"
							value="${orderId}" />
					</td>
				</tr>
				
				<tr>
					<td height="35" align="right" valign="middle">
						支付金额(元)：
					</td>
					<td height="35" align="left" valign="middle" class="td1">
						<input type="text" name="paytotal" id="paytotal" value="${paytotal}" />
					</td>
				</tr>
				<#if (payDeposit?eval >0)>
				<tr>
					<td height="35" align="right" valign="middle">
						产品订金(元)：
					</td>
					<td height="35" align="left" valign="middle" class="td1">
						<input type="text" name="payDeposit" id="payDeposit" value="${payDeposit}" readonly="true" />
					</td>
				</tr>
				</#if>
				<tr>
					<td height="35" align="right" valign="middle">
						姓名<b style="color:red"> *</b>：
					</td>
					<td height="35" align="left" valign="middle" class="td1">
						<input type="text" name="cardusername" id="cardusername" value="" />
					</td>
				</tr>
				<tr>
					<td height="35" align="right" valign="middle">
						手机号码<b style="color:red"> *</b>：
					</td>
					<td height="35" align="left" valign="middle" class="td1">
						<input type="text" name="mobilenumber" id="mobilenumber" value=""/>
					</td>
				</tr>
				
				<tr>
					<td height="35" align="right" valign="middle">
						客服号码：
					</td>
					<td height="35" align="left" valign="middle" class="td1">
						<input type="text" name="csno" id="csno" readonly="true"
							value="${csno}" />
							
						<input type="hidden" name="signature" id="signature" readonly="true"
							value="${signature}" />
								
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input id="selectCheckBox" class="selectCheckBox" type="checkbox"> 启用手动输入卡信息
						<input type="hidden" name="ivrType" id="ivrType"  value="0"/>
					</td>
				</tr>
				<tr class="cardNo" style="display:none">
					<td height="35" align="right" valign="middle">
						卡号：
					</td>
					<td height="35" align="left" valign="middle" class="td1">
						<input type="text" name="cardNo" id="cardNo"  value="${cardNo}" onkeyup="this.value=this.value.replace(/[^\d]/g,'') " onafterpaste="this.value=this.value.replace(/[^\d]/g,'') "/>
						<input type="hidden" name="cardType" id="cardType"  value=""/>
					</td>
				</tr>
				
				<tr class="credNo" style="display:none">
					<td height="35" align="right" valign="middle">
						身份证号码：
					</td>
					<td height="35" align="left" valign="middle" class="td1">
						<input type="text" name="credNo" id="credNo"  value="${credNo}" />
						<input type="hidden" name="credType" id="credType" value="1" />
					</td>
				</tr>
				
				<input type="hidden" name="objectType" id="objectType" readonly="true" value="${objectType}" />
				
				<input type="hidden" name="paymentType" id="paymentType" readonly="true" value="${paymentType}" />
				
				<input type="hidden" name="bizType" id="bizType" readonly="true" value="${bizType}" />
			
				<tr>
					<td height="35" align="right" valign="middle">
						
					</td>
					<td height="35" align="left" valign="middle" class="td1">
						<input type="submit" name="button" id="button" value="提交表单" class="input2-button"/>
					</td>
				</tr>
				<tr>
					<td height="35" align="right" valign="middle"></td>
					<td height="35" align="left" valign="middle" class="td1"></td>
				</tr>
				<tr>
					<td height="35" align="right" valign="middle"></td>
					<td height="35" align="left" valign="middle" class="td1"></td>
				</tr>
				<tr>
					<td height="35" align="right" valign="middle"></td>
					<td height="35" align="left" valign="middle" class="td1"></td>
				</tr>
			</table>
			</div>
		</form>
		<#include "/base/head.ftl"/>
	</body>
<script type="text/javascript">
	$(function() {
		$.getScript("http://localhost:12366/ipcc/default.jsp?getphoneno&random="+Math.random(),
			function(){
				try{
					$("#mobilenumber").val(phoneNo);
					$("#mobilenumber").attr("readonly","readonly");
				}
				catch(e){
					$("#mobilenumber").removeAttr("readonly");
				}
			}
		);
	});
	
	 $("#selectCheckBox").click(function (){
    	if($('#selectCheckBox').attr("checked")){
    	   $(".cardNo").show();
    	   $(".credNo").show();
    	   $("#ivrType").val("1");
		}else{
		   $(".cardNo").hide();
    	   $(".credNo").hide();
    	   $("#ivrType").val("0");
		}
	});
	
	$("#cardNo").bind("keyup",function(){
		var cardNo=$("#cardNo").val();
		var paytotal="${paytotal}";
		var csno="${csno}";
		if($.trim(cardNo).length==9){
			 $.ajax({ 
		    	type: 'get', 
		        async: false,
		    	url: 'http://pay.lvmama.com/payment/pay/yeepay/getCardNoMessage.do', 
		    	dataType:'jsonp',
		    	data:{"cardNo":cardNo,"paytotal":paytotal,"csno":csno}, 
		    	jsonp:"callback",    
		    	jsonpCallback:"success_jsonp",
		    	success: function(jsonStr) { 
		    		if(jsonStr!=null&&jsonStr!=""){
		    			var cardType=jsonStr.cardType;
					    var isSupport=jsonStr.isSupport;
					    var bankName=jsonStr.bankName;
					    var bankLimitAmount=jsonStr.bankLimitAmount;
					    $("#cardType").val(cardType);
				    	if(isSupport=="0"){
				    		if(cardType=="0"){
				    			alert("该卡为"+bankName+"借记卡，易宝电话支付暂不支持该卡支付");
				    			return false;
				    		}else if(cardType=="1"){
				    			alert("该卡为"+bankName+"信用卡，易宝电话支付暂不支持该卡支付");
				    			return false;
				    		}else{
				    			alert("易宝电话支付暂不支持该卡支付");
				    			return false;
				    		}
				    	}else if(isSupport=="1"&&cardType=="1"){
				    		var paytotal=document.getElementById("paytotal").value;
				    		if(parseFloat(paytotal)>0&&parseFloat(bankLimitAmount)>0&&parseFloat(paytotal)>parseFloat(bankLimitAmount)){
				    			alert("该卡为"+bankName+"信用卡,限额为"+bankLimitAmount+",订单支付金额超出限额");
					    		return false;
					    	}	
				    	}
				     }  	
				},
			    error: function(){
			     }
			  
		    });
		  }
		
	 });
     
     
	function checkSubmit(){
		var paytotal=document.getElementById("paytotal").value;
		var mobilenumber=document.getElementById("mobilenumber").value;
		var username = document.getElementById('cardusername').value;
		var r = /^([1-9][\d]{0,7}|0)(\.[\d]{1,2})?$/;
		
		if(username==""){
			alert("请输入姓名!");
			return false;
		}	
		if(mobilenumber==null || mobilenumber==''){
			alert("手机号码不能为空!");
			return false;
		}
		if(!r.test(paytotal)){
			alert("支付金额格式不正确!");
	        return false;
        }
		if(paytotal<=0){
			alert("支付金额必须大于零!");
			return false;
		}
		if(paytotal>${paytotal}){
			alert("支付金额不能超过${paytotal}元");
			return false;
		}
		if(${payDeposit}>0 && paytotal<${payDeposit} && ${actualPayFloat}<${payDeposit}){
			alert("首次支付金额不能小于产品订金!");
			return false;
		}
		
		var ivrType=$("#ivrType").val();
		if(ivrType=="1"){
			var cardNoReg = /^[0-9]*$/;
			var cardNo=$("input[type=text][name='cardNo']").val();
			if(cardNo==""||!cardNoReg.test(cardNo)){
				alert("请输入正确的卡号");
				return false;
			}
			
			
			var credNo=$("input[type=text][name='credNo']").val();
			var patt1 = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
	 	    if(!patt1.test(credNo)){
				alert("请输入正确的身份证号");
				return false;    
			}
			
			
			
		}
	}
</script>
</html>
