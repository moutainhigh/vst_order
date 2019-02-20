<!DOCTYPE html>
<head>
<#include "/base/head_meta.ftl"/>
   <script type="text/javascript">
     $(function(){
          $("#provinceId").change(function(){
               var val=$(this).val();
			   var districtId = $("#provinceId").find('option:selected').attr("value");
			   var parentId = districtId.split("@")[0];
			   $("#cityId").empty();
			    var cityId = $("#cityId");
			       $.ajax({
		                 url : "/vst_order/order/orderInvoice/ord/selCityName.do?districtId="+parentId,
		                 type : 'post',
		                 dataType : 'json',
		                 success : function(data) {
		                	$("#cityId").html("");
		                	$("#cityId").append('<option value="0">请选择市 </option>');
			                $.each(eval(data), function(i, item) {
							      $("<option value='" + item.districtId + "@" + item.districtName + "'>" + item.districtName
										+ "</option>").appendTo(cityId);
		                    });
		                 }
	                });
	   });   
   });	
   </script>
   <script type="text/javascript">
        function  addAddress(){
            if($("#address").val()==""){
                alert('地址不可以为空！');
                return false;
            }
            if($("#receiverName").val()==""){
                alert('联系人不可以为空！');
                return false;
            }
            if($("#mobileNumber").val()==""){
                alert('联系电话不可以为空！');
                return false;
            }
            var userId =  $("#userId").val();
            var orderId =  $("#orderId").val();
            var pos = 0;
            $.ajax({
	            type: "post",
	            url: "/vst_order/order/orderInvoice/ord/saveAddress.do?userId="+userId,
	            data: $('#saveOrder').serialize(),
	            dataType: "json",
	            success: function (result) {
	                if(result.code=="success"){
	                   alert("保存成功！");
	                   showAmountDialog.close(); 
	                     $.post("/vst_order/order/orderInvoice/ord/loadAddresses.do",
							{
								hidePhysical:true,
								hideButton:true,
								orderId:orderId,
								index:pos++
							},
							function(data){
								$("#[id^=addressDiv]").html(data);
								$("#[id^=addressDiv]").each(function(){
									var $idx = $(this).attr("idx");
									$(this).find("input[name^=invoiceAddressId]").attr("name","invoiceAddressId"+$idx);
								});
								
								var userAddressListCount = $("#userAddressListCount").val();
								if(userAddressListCount < 20){
        	                        str = '<input type="button" value="新增地址" class="btn btn_cc1" name="editPassed" onclick="showAddAddressDialg()"/>';
		                        }else{
		   	                        str = '<input type="button" value="新增地址" class="btn btn_cc1" name="editPassed" onclick="showAddAddressDialg()" disabled="disabled"/>';
		                        }
		                        $("#[id^=operate_mt20]").html(str);
							}
						);
						
				       //window.location.reload();  
	                }else{
	                   alert("保存失败！");
	                   showAmountDialog.close();
	                }
	          }
	     });
     } 
        function closeMyDiv(){
            showAmountDialog.close();
        }
    </script>
</head>  
</head>
<body>
	<div class="iframe_search">
		<#list mapUserId as user>
			<input name="userId" type="hidden" id="userId" value="${user.userId!''}" />
			<input name="orderId" type="hidden" id="orderId" value="${user.orderId!''}" />
		</#list>
			<form theme="simple" id="saveOrder">
				<table >
					<tr>
						<td width="8%">
							地址：
						</td>
						<td width="12%">
						  <select class="select" name="province" id="provinceId" size="1">
						    <option value="0"> 请选择省 </option>
							<#list provinceNameList as proProvince>
							    <option value="${proProvince.districtId!''}@${proProvince.districtName!''}" > ${proProvince.districtName!''} </option>
						    </#list>
						 </select>
                         <select class="select" id="cityId" name="city"></select><br/>
                         <input name="address" type="text" id="address"/>
					   </td>
					   <td width="8%">
							联系人：
						</td>
						<td width="12%">
							<input name="receiverName" type="text" id="receiverName"/>
						</td>
					</tr>
					<tr>
						<td width="8%">
							联系电话：
						</td>
						<td width="12%">
							<input name="mobileNumber" type="text" id="mobileNumber"/>
						</td>
						<td width="8%">
							邮编：
						</td>
						<td width="12%">
							<input name="postCode" id="postCode" type="text" />
						</td>
					</tr>
				</table>
				<div class="operate mt20" style="text-align:center">
					<input type="reset" name="btnResetAddress" value="清空" class="btn btn_cc1">&nbsp;&nbsp;&nbsp;
					<input type="button" name="btnSaveAddress" value="保存" onclick="addAddress();"class="btn btn_cc1" id="address"> &nbsp;&nbsp;&nbsp;
					<input type="button" id="closebtn" onclick="closeMyDiv();"name="btnCloseAddress" value="关闭" class="btn btn_cc1">
				</div>
			</form>
			<#include "/base/foot.ftl"/>
		</div>
	</body>
</html>

