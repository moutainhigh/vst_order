<!DOCTYPE html>
<html>
<head>

<#include "/base/head_meta.ftl"/>
</head>
<body>
<table>
	<tr>
		<td rowspan="5" style="vertical-align:top">签约方式：</td>
		<td><input type="radio" name="signingType" id="signingType" <#if contract.signingType=='ONLINE'>checked</#if>  value="ONLINE">在线签约</td>
	</tr>
	
	<tr>
		<td><input type="radio" name="signingType" id="signingType" <#if contract.signingType=='FAX'>checked</#if>  value="FAX">传真签约</td>
	</tr>
	
	<tr>
		<td><input type="radio" name="signingType" id="signingType" <#if contract.signingType=='BRANCHES'>checked</#if>  value="BRANCHES">门市签约</td>
	</tr>
	
	<tr>
		<td><input type="radio" name="signingType" id="signingType" <#if contract.signingType=='VISIT_SIGN'>checked</#if>   value="VISIT_SIGN">上门签约</td>
	</tr>
</table>
 <br/>
        <div class="p_box box_info clearfix mb20" style="padding-left:10px;">
            <div class="fl operate"><a class="btn btn_cc1" onclick="change()" id="save">保存</a><a class="btn btn_cc1" data-dismiss="dialog" id="cancel">取消</a></div>
        </div>
</body>
<script>
	function change(){
	var contractId="${contract.ordContractId}";
	var type="";
	var radios = document.getElementsByName("signingType");
	for(var i=0;i<radios.length;i++){ 
	if(radios[i].checked){
		type=radios[i].value;
	}
	}
	if(type==""){
		alert("请选择签约方式");
		return;
	}
	$.ajax({
				url : "/vst_order/order/orderManage/changeContractSignType.do?contractId="+contractId+"&signType="+type,
				type : "get",
				dataType : 'json',
				success : function(result) {
					if(result.code=="success"){
						alert("操作成功");
						saveOrUpdateDialog.close();
						parent.location.reload(); 
					}else{
						alert("操作失败:"+result.message);
					}
				}
				});		
}
</script>
</html>
