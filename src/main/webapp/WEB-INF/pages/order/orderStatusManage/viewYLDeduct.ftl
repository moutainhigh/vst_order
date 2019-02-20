
<form method="post" action='/vst_order/order/ordAuditConfig/updateOrdAuditConfig.do' id="dataForm">
<table class="p_table form-inline">
    <tbody>
         
        
         <tr>
         	<td colSpan ="2">
		<div align="center" style="font-size:12pt;font-weight:bolder;">
		
		 艺龙订单号：${YLMap['orderId']!''}

		</div>
		</td>
         </tr>
          <tr>
		<td colSpan ="2">
		<div align="center" style="font-size:12pt;font-weight:bolder;">
		
		 艺龙订单罚金

		</div>
		</td>
		</tr>
        <tr>
        	<td>订单产生的罚金：${YLMap['PenaltyToCustomer']!''}</td>
        	<td> 罚金货币类型：${YLMap['PenaltyCurrencyCode']!''}</td>
        </tr>
   
    </tbody>
</table>
<table class="p_table form-inline">
    <tbody>
    	<tr>
		<td colSpan ="2">
		<div align="center" style="font-size:12pt;font-weight:bolder;">
		
		艺龙信用卡扣款类型

		</div>
		</td>
		</tr>
        
          <tr> 
            <td>交易类型：${processType!''}</td>  
              
            <td>交易状态：${status!''}</td>  
        </tr> 
    </tbody>
</table>
</form>
<p align="center">
<button  class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="cancelButton">关闭</button>
</p>
<script>
	
	$("#cancelButton").bind("click",function(){
	
		yiLongDeductDialog.close();
						
	});
	
	
	
</script>