<table width="100%">
	<tbody>
	  	<tr>
			<td style="font-size:16px;">
				<b>快递信息</b>：
				<div id="expressItemDiv"></div>
			<td>
		</tr>
	    <tr>
	    	<td class="e_label">收件人：</td>
	        <td>
	        	 <input type="text" placeholder="姓名" name="expressage.recipients" name_type="fullName" class="input" maxlength="25" id="user_name" required=true>
	        	 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>收件人不正确</span>
	        </td>
	    </tr>
	    <tr>
	    	<td class="e_label">联系电话：</td>
	        <td>
	        	 <input type="text" placeholder="手机号码" name="expressage.contactNumber" name_type="mobile" maxlength="11" id="user_phone" class="input" required=true>
	        	 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>联系电话不正确</span>
	        </td>
	    </tr>
	    <tr>
	    	<td class="e_label">邮寄地址：</td>
	        <td>
	        	<select name="expressage.province" id="js_city1" class="select"><option>选择省</option></select>
	            <select name="expressage.city" id="js_city2" class="select"><option>选择市</option></select>
	            <br/>
	            <input type="text" placeholder="详细地址" name="expressage.address" name_type="address" class="input w290" maxlength="100" id="user_address" required=true>
	            <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>请选择行政区域</span>
	        </td>
	    </tr>
	    <tr>
	    	<td class="e_label">邮政编码：</td>
	        <td>
	        	 <input type="text" placeholder="邮政编码" name="expressage.postcode" name_type="youbian" id="user_code" class="input" required=true>
	        	 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮政编码</span>
	        </td>
	    </tr>
	    <tr>
	    	<td class="e_label">快递费用：</td>
	        <td>
	        	 <span id="expressTotalPrice">免邮费</span>
	        </td>
	    </tr>
    </tbody>
</table>
<script src="/vst_order/js/book/express.js?2014091512345"></script>
<script>
	$(function(){
        Express.init("");
    });
</script>