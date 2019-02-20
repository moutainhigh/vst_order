<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单</title>
<style>
.pop_table th, .pop_table td {
    padding: 5px 0;
}
</style>
</head>
<body>
<form action="#" method="post" id="dataForm">
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
	<#if travellerDelayFlag=='Y' && travellerLockFlag=='Y'>
	<div style="color:#FF0000;text-align: center;">出游人信息前台已锁定</div>
	</#if>
    <table class="p_table table_center mt5 pop_table">
                <thead>
                    <tr>
                      	<th>入住房间</th>
                        <th >中文姓名</th>
                        <th>英文姓</th>
                        <th>英文名</th>
                        <th>性别</th>
                        <th>出生地</th>
                        <th>出生日期</th>
                        <th>人群</th>
                        <th>证件类型</th>
                        <th>证件号码</th>
                        <th>签发地</th>
                        <th>签发日期</th>
                        <th>有效日期</th>
                        <th>手机号</th>
                    </tr>
                </thead>
                <tbody>
                <#assign personIndex = 0 />
                <#if personMap?? && personMap?size &gt; 0>
                	<#list personMap?keys as cangfangkey>
                		<#assign roomMap = personMap[cangfangkey]>
                		<#if roomMap?? && roomMap?size &gt; 0>
                			<#list roomMap?keys as roomkey>
                				<#assign personList = roomMap[roomkey]>
                					<#if personList?? && personList?size &gt; 0>
                						<#assign orderRemark = 1>
                						<#list personList as person>
						                    <tr>
							                    <input type="hidden" name="ordPersonList[${personIndex}].ordPersonId" value="${person.ordPersonId}">
							                    <input type="hidden" name="ordPersonList[${personIndex}].orderItemId" value="${person.orderItemId}">
							                    <input type="hidden" name="ordPersonList[${personIndex}].checkInRoom" value="${person.orderItemId}">
							                     <input type="hidden" name="ordPersonList[${personIndex}].checkInRoomName" value="第${roomkey}间:${person.checkInRoomName}">
							                      <input type="hidden" name="ordPersonList[${personIndex}].orderRemark" value="${orderRemark}">
							                    <#if person_index == 0> 
							                      	<td rowspan="<#if personList?? && personList?size &gt; 0>${personList?size}</#if>">第${roomkey}间：<br/>${person.checkInRoomName!''}</td>
												</#if>	
						                        <td>
						                        	<input type="text" name="ordPersonList[${personIndex}].fullName" style="width:50px;" value="${person.fullName!''}" maxLength=10>
						                        </td>
						                        <td>
						                        	<input type="text" name="ordPersonList[${personIndex}].lastName"  style="width:40px;" value="${person.lastName!''}" maxLength=10>
												</td>
						                        <td>
						                        	<input type="text" name="ordPersonList[${personIndex}].firstName"  style="width:55px;" value="${person.firstName!''}" maxLength=20>
												</td>
												<td>
							                        <select name="ordPersonList[${personIndex}].gender"  style="width:45px;">
									                	<#list genderTypeList as  genderType>
									                		<option value="${genderType.code!''}" <#if genderType.code == person.gender>selected</#if>>${genderType.cnName!''}</option>
									                	</#list>
									            	</select>
						                  		</td>
						                  		<td>
						                        	<input type="text" name="ordPersonList[${personIndex}].birthPlace" style="width:50px;" value="${person.birthPlace!''}" maxLength=10>
						                        </td>
						                        <td>
						                       		<input type="text" name="ordPersonList[${personIndex}].birthday"   style="width:80px;" class="Wdate"  onFocus="WdatePicker({readOnly:true, dateFmt:'yyyy-MM-dd'})" <#if person.birthday?exists> value="${person.birthday?string('yyyy-MM-dd')!''}" </#if>>
						                        </td>
						                        <td>
							                        <select name="ordPersonList[${personIndex}].peopleType"  style="width:55px;"  >
										               <#if 'PEOPLE_TYPE_ADULT' == person.peopleType>
										                		<option value="PEOPLE_TYPE_ADULT" selected>成人</option>
										                		<option value="PEOPLE_TYPE_CHILD" >儿童</option>
										                </#if> 
										                <#if 'PEOPLE_TYPE_CHILD' == person.peopleType>
											                <option value="PEOPLE_TYPE_ADULT" >成人</option>
											                <option value="PEOPLE_TYPE_CHILD" selected>儿童</option>
										                </#if> 
									            	</select>
						                  		</td>
						                		<td>
							                        <select name="ordPersonList[${personIndex}].idType"  style="width:70px;"  >
									                	<#list idTypeList as  idType>
									                		<option value="${idType.code!''}" <#if idType.code == person.idType>selected</#if>>${idType.cnName!''}</option>
									                	</#list>
									            	</select>
						                  		</td>
						                      
						                      	<td>
						                      		<input type="text" name="ordPersonList[${personIndex}].idNo"  style="width:125px;" value="<#if person.idTypeName!="客服联系我">${person.idNo!''}</#if>"   maxLength=20>
						                       	</td>
						                        <td>
						                       		<input type="text" name="ordPersonList[${personIndex}].issued"  style="width:50px;" value="${person.issued!''}"   maxLength=10>
						                  		</td>
						                      	<td>
						                       		<input type="text" name="ordPersonList[${personIndex}].issueDate"   style="width:80px;" class="Wdate"  onFocus="WdatePicker({readOnly:true, dateFmt:'yyyy-MM-dd'})" <#if person.issueDate?exists> value="${person.issueDate?string('yyyy-MM-dd')!''}"   </#if>>
						                  		</td>
						                      	<td>
						                       		<input type="text" name="ordPersonList[${personIndex}].expDate"   style="width:80px;" class="Wdate"  onFocus="WdatePicker({readOnly:true, dateFmt:'yyyy-MM-dd'})" <#if person.expDate?exists> value="${person.expDate?string('yyyy-MM-dd')!''}"   </#if>>
						                  		</td>
						                        <td>
						                        	<input type="text" name="ordPersonList[${personIndex}].mobile"  style="width:80px;" value="${person.mobile!''}" number=true   maxLength=11>
						                        </td>
						                    </tr>
						                    <#assign personIndex =	personIndex + 1/>			
						                    <#assign orderRemark =	orderRemark + 1/>			
                						</#list>
                					</#if>
                			</#list>
                		</#if>
                	</#list>
                </#if>                						                    
                </tbody>
            </table>
<input type="hidden" id="travellerLockFlag" name="travellerLockFlag" value="false"/>				 
</form>
<p align="center">
<#if travellerDelayFlag=='Y'>
<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="editButton" islock="true">锁定出游人并更新发送合同</button>
</#if>
<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="editButtonSave" islock="false">保存</button>

&nbsp;&nbsp;
<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>
<#include "/base/foot.ftl"/>
</body>
</html>
<script>
var contactAddDialog,districtSelectDialog,selectSupplierDialog;
$("#editButton").bind("click",function(){
	
	$("#travellerLockFlag").val($(this).attr("islock"));
	//如果是锁定游玩人
	if($("#travellerLockFlag").val()=='true'){
		//if($("#paymentStatus").val()!='PAYED'){
		//	alert("订单付款后方能锁定出游人并更新发送合同");
		//	return;
		//}
		//是否锁定游玩人
		editTravellerLockFlagDialog = 
			new xDialog("/vst_order/order/orderShipManage/editPersonCountiune.do",{},{title:"系统提示",width:700,dialogAutoStop: true});
		return;
	}else{
		isTravellerLockFlagSubmit();
	}
	
});
$("#editButtonSave").bind("click",function(){
	$("#travellerLockFlag").val($(this).attr("islock"));
	isTravellerLockFlagSubmit();
});

function isTravellerLockFlagSubmit(){
	
	 //验证
	if(!$("#dataForm").validate().form()){
		return;
	}
	 
	var isIdCardSame = {};
	//默认是没有相同类型的证件和证件号
	var isIdCardSameB=true;
	var isMobile=true;
	var isIdNo = true;
	var $form =$("#dataForm");
	$form.find('tr').each(function(idIndex){
		
		if(idIndex>0){
			//校验第一个游玩人的手机号
			var mobileNum=13;//手机号的td位置
			if($(this).find('td').length==14){
				mobileNum=13;
			}else{
				mobileNum=12;
			}
			if(idIndex==1){
				
				if(!$(this).find('td').eq(mobileNum).children("input").eq(0).val()){
					alert("第一个游玩人必须填写手机号码");
					isMobile=false;
				}else{
					var mobileReg = /^\d+$/;
					if(!mobileReg.test($(this).find('td').eq(mobileNum).children("input").eq(0).val())){
						//提示
						alert("手机号码格式不正确");
						isMobile=false;
					}
				}  
			}else{
				if($(this).find('td').eq(mobileNum).children("input").eq(0).val()){
					var mobileReg = /^\d+$/;
					if(!mobileReg.test($(this).find('td').eq(mobileNum).children("input").eq(0).val())){
						//提示
						alert("手机号码格式不正确");
						isMobile=false;
					}
				}
			}
			
			//校验证件  CUSTOMER_SERVICE_ADVICE
			var typenum=8;//证件类型的td位置
			var nonum=9;//证件号码的td位置
			if($(this).find('td').length==14){
				 typenum=8;
				 nonum=9;
			}else{
				 typenum=7;
				 nonum=8;
			}
			var idtype=$(this).find('td').eq(typenum).children("select").eq(0).val();
			var idNo=$(this).find('td').eq(nonum).children("input").eq(0).val();
			var patt1 = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
			if(idtype!='CUSTOMER_SERVICE_ADVICE'){
				if(idNo){
					if(isIdCardSame.idtype==$(this).find('td').eq(nonum).children("input").eq(0).val()){
						isIdCardSameB=false;
					}else{
						isIdCardSame.idtype=$(this).find('td').eq(nonum).children("input").eq(0).val();
					}
				}else{
					isIdNo = false;
				}
			}
		}
		
	});
	
	if(!isMobile){
		return;
	}
	if(!isIdNo){
		alert("证件号不能为空");
		return;
	}
	if(!isIdCardSameB){
		alert("相同证件类型，证件号不能一样");
		return;
	}

	//遮罩层
    var loading = pandora.loading("正在努力保存中...");		
		
	$.ajax({
	   url : "/vst_order/order/orderShipManage/updateTourist.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			alert(result.message);
   			loading.close();
   			editPersonButtonDialog.close();
	   		 parent.window.location.reload();
   		}else {
   			loading.close();
   		  	alert(result.message);
   		}
	   }
	});	
}
$("#closeButton").bind("click", function() {
 	editPersonButtonDialog.close();
});
</script>