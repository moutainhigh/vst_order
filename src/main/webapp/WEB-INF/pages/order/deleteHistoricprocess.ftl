<!DOCTYPE html>
<html>
<head>
<title>订单管理-流程清除</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>
<form method="POST" action="/vst_order/ord/order/delHistoricProcess.do" id="delOrderProcessForm">
<table width="80%"  class="s_table">
	<tbody>
	  <tr>
    		<td style="font-size:16px;" class="s_label">
    			<b>流程ID（用,分隔）</b>：
    		<td>
    	</tr>
         <tr>
         	<td ></td>
            <td style="padding-left:20px;">
                   <textarea style="width:600px; height:400px;" name="processIds"></textarea>
            </td>
        </tr>
        <tr>
         	<td ></td>
            <td class=" operate mt10" style="padding-left:20px;">
            	<a class="btn btn_cc1" id="submit_button" style="margin-left:450px;">清除流程历史记录</a>
            </td>
        </tr>
        
    </tbody>
</table>
</form>
<script>
	$(function(){
		$("delOrderProcessForm input[name='processIds']").focus();
		$("#submit_button").bind("click",function(){
			$("#delOrderProcessForm").submit();
		}); 
	});
</script>
</body>
</html>