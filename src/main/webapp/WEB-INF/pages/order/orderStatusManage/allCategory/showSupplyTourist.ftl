<#--页眉-->
<#import "/base/spring.ftl" as s/>
<form method="POST" action="/vst_order/ord/book/ticket/saveOrderPerson.do" name='orderInfoForm' id="orderInfoForm" onsubmit="return false">
	<input type="hidden" id="orderId" name="orderId" value="${order.orderId}"/>
	<div id="dialogBody" class="dialog-body">
	  <table class="e_table form-inline mt10">
            <tbody>
            	<#include "/order/requiredItem/userInfo.ftl"/>
            	<#if orderItemList?? && orderItemList?size&gt;0>
	            	<#list orderItemList as orderItem>
	            		<#if orderItem.ordItemPersonRelationList?? && orderItem.ordItemPersonRelationList?size&gt;0>
		                	<tr>
		                     	<td colspan="2">
		                     		<b>${orderItem.productName}(${orderItem.suppGoodsName})(${orderItem.quantity}份)</b>
		                        </td>
		                    </tr>
		                     <tr>
		                     	<td class="e_label"></td>
		                        <td name="orderItemTd" goodsId="${orderItem.suppGoodsId}" quantity="${orderItem.quantity}">
		                         	 <#if tavellerList?? && tavellerList?size&gt;0>
		                         	 	<#list tavellerList as taveller>
		                         	 		<label class="checkbox mr10"><input <#if orderItem.categoryId=="3" && travellerDelayFlag!='Y'> onclick="this.checked=!this.checked" </#if>  type="checkbox" name="personRelationMap[ORDERITEM_${orderItem.orderItemId}].ordItemPersonRelationList[${taveller_index}].ordPersonId"
		                         	 			<#list orderItem.ordItemPersonRelationList as ordItemPersonRelation>
		                         	 				<#if ordItemPersonRelation.ordPersonId==taveller.ordPersonId>
		                         	 					checked
		                         	 				</#if>
		                         	 			</#list>
		                         	 		 value="${taveller.ordPersonId}"><span>${taveller.fullName}</span></label>
		                         	  	</#list>
	                				</#if>
		                        </td>
		                    </tr>
	                    </#if>
	                </#list>
                </#if>
            </tbody>
        </table>
	</div>
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
    <table class="p_table table_center mt5">
		<thead>
			<tr>
				<th>中文姓名</th>
				<th>英文姓</th>
				<th>英文名</th>
				<th>证件类型</th>
				<th>证件号码</th>
				<th>签发地</th>
				<th>有效期</th>
				<th>出生日期</th>
				<th>人群</th>
				<th>性别</th>
				<th>联系电话</th>
				<th>邮箱地址</th>
			</tr>
		</thead>
        <tbody>
        <#list personList as person> 
	        <input type="hidden" name="ordPersonList[${person_index}].ordPersonId" value="${person.ordPersonId}">
	        <input type="hidden" name="ordPersonList[${person_index}].orderItemId" value="${person.orderItemId}">
            <tr>
                <td>
                	<input type="text" name="ordPersonList[${person_index}].fullName" style="width:50px;" value="${person.fullName!''}" maxLength=10>
                </td>
                <td>
                	<input type="text" name="ordPersonList[${person_index}].lastName" style="width:50px;" value="${person.lastName!''}" maxLength=10>
				</td>
                <td>
                	<input type="text" name="ordPersonList[${person_index}].firstName" style="width:50px;" value="${person.firstName!''}" maxLength=10>
				</td>
        		<td> 
        			${person.idTypeName!''}
        		</td>
              	<td>
                	<input type="text" name="ordPersonList[${person_index}].idNo" style="width:190px;" value="<#if person.idTypeName!="客服联系我">${person.idNo!''}</#if>" maxLength=20>
      			</td>
          		<td>
               		<input type="text" name="ordPersonList[${person_index}].issued" style="width:190px;" value="${person.issued!''}"   maxLength=20>
          		</td>
          		<td>
	               	<input type="text" name="ordPersonList[${person_index}].expDate" style="width:80px;" class="Wdate"  onFocus="WdatePicker({readOnly:true, dateFmt:'yyyy-MM-dd'})" <#if person.expDate?exists> value="${person.expDate?string('yyyy-MM-dd')!''}"   </#if>>
          		</td>
               	<td>
               		<input type="text" name="ordPersonList[${person_index}].birthday" style="width:80px;" class="Wdate"  onFocus="WdatePicker({readOnly:true, dateFmt:'yyyy-MM-dd'})" <#if person.birthday?exists> value="${person.birthday?string('yyyy-MM-dd')!''}"   </#if>>
                </td>
				<td>  
	                <#if person.peopleType == "PEOPLE_TYPE_ADULT"> 
	              		成人
					<#elseif person.peopleType == "PEOPLE_TYPE_CHILD">
						儿童
					<#elseif person.peopleType == "PEOPLE_TYPE_OLDER">
						老人
					</#if>
                </td>
                <td>
	                <select name="ordPersonList[${person_index}].gender" style="width:50px;"  >
		            	<#list genderTypeList as  genderType>
		            		<option value="${genderType.code!''}" <#if genderType.code == person.gender>selected</#if>>${genderType.cnName!''}</option>
		            	</#list>
	            	</select>
          		</td>
                <td>
                	<input type="text" name="ordPersonList[${person_index}].mobile" style="width:100px;" value="${person.mobile!''}" number=true   maxLength=20>
                </td>
                <td>${person.email!''}</td>
            </tr>
        </#list>
        </tbody>
    </table>
				 
	<p align="center">
		<button class="pbtn pbtn-small btn-ok optInsButton" style="margin-top:20px;">手动投保</button>
		&nbsp;&nbsp;
		<button class="pbtn pbtn-small btn-ok editButton" style="margin-top:20px;">保存</button>
		&nbsp;&nbsp;
		<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
	</p>
</form>
<script type="text/javascript" src="/vst_order/js/book/order_travellers.js"></script>
<script>
var $form = null;
$("button.editButton").bind("click",function(){
	$form=$(this).parents("form");
	supplyTravPersonInfo();
});

//投保事件
$("button.optInsButton").bind("click",function(){
	$form=$(this).parents("form");
	
	$.confirm("确认投保吗 ？", function () {
		$.ajax({
		   url : "/vst_order/ord/order/apply/applyTouristIns.do",
		   data : $form.serialize(),
		   type:"POST",
		   dataType:"JSON",
		   success : function(result){
			   	if(result.code=="success"){
					alert(result.message);
					loading.close();
					supplyPersonButtonDialog.close();
			   		parent.window.location.reload();
				}else {
					loading.close();
				  	alert(result.message);
				}
		   }
		}); 
	});
});


$("#closeButton").bind("click", function() {
 	supplyPersonButtonDialog.close();
});

function supplyTravPersonInfo(){
	//验证
	if(!$form.validate().form()){
		return false;
	}
	var msg="";
	var checkFlag=true;
	$form.find('input[type=text]').each(function(){
		 var text=$(this).val();
		 var nameType=$(this).attr("name_type");
		 var requiredTpe=$(this).attr("required");
		 if(requiredTpe || text!=""){
			 if(nameType=="email"){
			 	 var patt1 = new RegExp("^([a-zA-Z0-9\.\_\-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$");
			 	 if(!patt1.test(text)){
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
			 }else if(nameType=="idNo"){
			 	var idType=$(this).prev("select").val();
			 	var num = $(this).prev("select").attr("num");
			 	if(idType!="CUSTOMER_SERVICE_ADVICE"){
				 	if(idType=="ID_CARD"){
				 		 var patt1 = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
					 	 if(!patt1.test(text)){
							//提示
							$(this).next("span").css("display","");
							checkFlag=false;
						}else{
							$(this).next("span").css("display","none");
						}
				 	}else{
				 		var $birthdayInput=$("input[name='travellers["+num+"].birthday']");
				 		if($.trim($birthdayInput.val())=="" && requiredTpe){
				 			$(this).parent().next("span").css("display","");
				 			checkFlag=false;
				 		}else{
				 			$(this).parent().next("span").css("display","none");
				 		}
				 	}
			 	}
			 }
		 }
	});
	
	var isIdCardSame = {};
	//默认是没有相同类型的证件和证件号
	var isIdCardSameB=true;
	$form.find('input[type=text]').each(function(){
		var text=$(this).val();
		var requiredTpe=$(this).attr("required");
		var nameType=$(this).attr("name_type");
		var idType=$(this).prev("select").val();
		if(requiredTpe &&  nameType== "idNo"){
			if(idType!="CUSTOMER_SERVICE_ADVICE"){
				var idType=$(this).prev("select").val();
				var num = $(this).prev("select").attr("num");
				 if(isIdCardSame.num && isIdCardSame.num==text){
					//如果证件类型一样并且证件号一样 不能提交
					isIdCardSameB=false;
				}else{
					isIdCardSame.num=text;
				}  
			}
		}
	});
	
	if(!checkFlag){
		return;
	}
	
	if(!isIdCardSameB){
		alert("相同证件类型，证件号不能一样");
		return;
	}
	
	//遮罩层
	var loading = pandora.loading("正在努力保存中...");
			
	$.ajax({
	   url : "/vst_order/ord/order/supply/supplyTourist.do",
	   data : $form.serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
		   	if(result.code=="success"){
				alert(result.message);
				loading.close();
				supplyPersonButtonDialog.close();
		   		parent.window.location.reload();
			}else {
				loading.close();
			  	alert(result.message);
			}
	   }
	}); 
}


//当游玩人fullName变动的时候，改当地游关联的人的名称
$('[name_type=fullName][name^=travellers]').live('blur',function(){
	var current = $(this);
	var ordPersonId = current.closest("table").siblings("[name$=ordPersonId]").val();
	$("[name=orderItemTd] [name^=personRelationMap]").each(function(index, e){
		var _ordPersonId = $(e).val();
		if(ordPersonId == _ordPersonId) {
			$(e).siblings("span").text(current.val());
		}
	});
});
</script>