
<#--页眉 -->
<#import "/base/spring.ftl" as s/>
<input type="hidden" id="personNum" name="personNum" value="${personNum}"/>
<input type="hidden" id="insuranceNum" name="insuranceNum" value="${insuranceNum}"/>
<form method="POST" action="/vst_order/ord/book/saveOrderPerson.do" name='orderInfoForm' id="orderInfoForm">
	<input type="hidden" id="orderId" name="orderId" value="${order.orderId}"/>
	<input type="hidden" id="userId" name="userId" value="${order.getUserId()}"/>
	<input type="hidden" id="travNumType0" name="travNumType" value="${orderRequiredvO.travNumType}"/>
	<input type="hidden" id="occupType" name="occupType" value="${orderRequiredvO.occupType}" adult="${adult!0}" child="${child!0}"/>
	<input type="hidden" name="travellerDelayFlag" value="${isTravellerDelay?string('Y','N')}" />
	<input type="hidden" name="hasApiFlight" value="${hasApiFlight}" />
	<div id="dialogBody" class="dialog-body">
		  <table class="e_table form-inline mt10">
                <tbody>
                	<#include "/order/requiredItem/userInfo.ftl"/>
                	<#if bxGoodsList?? && bxGoodsList?size&gt;0>
					<tr>
                		<td style="font-size:16px;">
                			<b>商品关联人</b>：
                		<td>
                	</tr>
                	<#list bxGoodsList as bx>
                	<tr>
                     	<td colspan="2" pname="${bx.suppGoodsId}">
                     		<b>${bx.prodProduct.productName}(${bx.prodProductBranch.branchName}-${bx.goodsName})(${bx.quantity}份)</b>
                        </td>
                    </tr>
                     <tr>
                     	<td class="e_label"></td>
                        <td name="baoxianTd" goodsId="${bx.suppGoodsId}" quantity="${bx.quantity}">
                         	 
                        </td>
                    </tr>
                    </#list>
                    </#if>
                    
                    
                	<#if LocalRouteSuppGoodsList?? && LocalRouteSuppGoodsList?size&gt;0>
						<tr>
	                		<td style="font-size:16px;">
	                			<b>关联当地游游玩人</b>：
	                		<td>
	                	</tr>
	                	<#list LocalRouteSuppGoodsList as suppGoods>
			            	<tr>
			                 	<td colspan="2" pname="${suppGoods.suppGoodsId}">
			                 		<b>${suppGoods.prodProduct.productName}(${suppGoods.prodProductBranch.branchName}-${suppGoods.goodsName})(${suppGoods.quantity}份)</b>
			                    </td>
			                </tr>
			                 <tr>
			                 	<td class="e_label"></td>
			                    <td name="baoxianTd" goodsId="${suppGoods.suppGoodsId}" quantity="${suppGoods.quantity}">
			                     	 
			                    </td>
			                </tr>
	                    </#list>
                    </#if>
                    
                    <tr>
                    	<td style="font-size:16px;">
                			<b>订单类型</b>：
                		<td>
                    </tr>
                    <tr>
							<td class="e_label td_top"><i class="cc1">*</i>测试订单：</td>
					        <td>
					        	<input type='radio' value='Y' name='isTestOrder'>是 </input>
								<input type='radio' value='N' name='isTestOrder' checked='checked'> 否</input> 
					        </td>
                    </tr>
                    <#if isTravellerDelay?? && isTravellerDelay>
                    	<#include "/order/route/traveller_confirm.ftl"/>
                    </#if>
                    
                     <tr>
                        <td colspan="2">
                        <div id="totalOrderPriceDiv" style="color:red;">
                        	<b style="font-size:14px;font-weight: bold;">订单总价：</b>产品费用 ${order.noContainsInsuranceProductAmountYuan}元+保险${order.getInsuranceAmountYuan()}元+快递${order.expressAmountYuan}元<#if order.categoryId =='28'>+押金${order.depositAmountYuan}元</#if>-优惠券<#if order.couponAmount??>${(order.couponAmount?number)/100}</#if>元-促销活动${order.getOrderAmountItemByType('PROMOTION_PRICE')}元=${order.oughtAmountYuan}元</div>
                        </td>
                    </tr>            
                    <tr>
                        <td class="e_label"></td>
                        <td><div class="fl operate"><a id="checkOrderA" class="btn btn_cc1">核对订单</a><a id="closeBookInfoPage" class="btn">取消</a></div></td>
                    </tr>
                </tbody>
            </table>
	</div>
</form>
<script src="/vst_order/js/book/order_travellers.js?2014091512345"></script>

<script type="text/javascript">

	 var checkOrderDialog;
	 var sumbitFlg=false;
	 nameTxtChange();
	 travellersBindEvent();
	 $(function(){

		
	});
	
	//判断是否对接机票
	function isApiFlight(){
		var hasApiFlight=$("input[name=hasApiFlight]");
		if(hasApiFlight){//如果机票控件存在
			if("Y"==hasApiFlight.val()){//对接的值为true
				return true;
			}
		}
		return false;
	}
	//如果对接机票，判断儿童是否在2-12周岁
	$('input[type_name=shenfenzheng]').live('blur',function(){ 
		var This = $(this);
		//如果对接机票
		if(isApiFlight()){
			var cardNum=This.val();//证件号
			if(cardNum==""){
				return;
			}
			var cardType=This.prev().val();//证件类型
			var cardTypeId=This.attr("id");//证件号控件ID
			var index=cardTypeId.replace("idNo","");//序列号
			var peopleType=$("#peopleType"+index).val();//人群

			//如果是儿童而且证件类型为身份证
			if("ID_CARD"==cardType){
				var regIDCard= /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
				//身份证错误
				if(!regIDCard.test(cardNum)){
					This.next(".error_text").html("证件错误");
					This.next(".error_text").css({color:"red"})
					This.next(".error_text").show();
					This.next(".error_text").next().hide();
				}else if("PEOPLE_TYPE_CHILD"==peopleType&&(!isAge2to12(cardNum))){
					//如果是儿童，年年不在2-12周岁
					This.next(".error_text").html("仅支持2-12周岁的儿童订购对接机票");
					This.next(".error_text").css({color:"red"})
					This.next(".error_text").show();
					This.next(".error_text").next().hide();
				}else{
					This.next(".error_text").html("证件错误");
					This.next(".error_text").hide();
					This.next(".error_text").next().show();
				}
			}
			}
		});
	
	//仅支持2-12周岁的儿童订购对接机票
	function validateTravellerforChild(){
		if(isApiFlight()){
			var hasErrorAgeChild=false;//是否有错误年龄的儿童
			$("select[name^=travellers][name$=peopleType]").each(function(){
				var travellerIndex=getTravellerNumberIndex($(this).attr("id"));
				if("PEOPLE_TYPE_CHILD"==$(this).val()&&"ID_CARD"==$("#idType"+travellerIndex).val()){
					var cardNum=$("#idNo"+travellerIndex).val();
					//如果不是2-12岁
					if(!isAge2to12(cardNum)){
						hasErrorAgeChild=true;
					}
				}
			});
		}
		if(hasErrorAgeChild==true){
			return false;
		}
		return true;
	}
	
	function isNullOrEmpty(strVal) {
		if (strVal == '' || strVal == null || strVal == undefined) {
			return true;
		} else {
			return false;
		}
	}
	//根据身份证号判断年龄是否是2-12岁
	function isAge2to12(sn){
		if(!sn){return false;}
		if(sn.length!=18){return false;}
		var bstr = sn.substring(6,14)
		var visitTime=$("#visitTime").val();
		var now;
		if(isNullOrEmpty(visitTime)){
			now=new Date();
		}else{
		 	now = new Date(visitTime.substring(0,4),visitTime.substring(5,7),visitTime.substring(8,10));
		}
		
		var birth = new Date(bstr.substring(0,4),bstr.substring(4,6),bstr.substring(6,8));
		var agen = now-birth;
		//向下去整
		var age = Math.floor(agen/(365*24*60*60*1000));
		return age>=2 && age<=11;
	}
	
	//根据用户类型的id,获得游玩人序号，从0开始，（0表示第1个游玩人）
	function getTravellerNumberIndex(peopleTypeId){
		if (typeof(peopleTypeId) == "undefined"||""==peopleTypeId) { 
		  return null;
		}
		return peopleTypeId.replace("peopleType","");
	}  
		
		
	//提交订单
		$('#checkOrderA').bind('click',function(){
				var travellerDelayFlag = $("#orderInfoForm [name=travellerDelayFlag]").val();
				//验证
				if(!$("form[name='orderInfoForm']").validate().form()){
					$.alert("表单校验不通过,请检查填写信息合法性");
					return false;
				}
				//校验儿童年是否在2-12岁
				if(!validateTravellerforChild()){
					  //如果不确认直接返回
					  if(!confirm("当前订单游玩人存在小于2周岁或大于12周岁的儿童，确认是否订购?")){
					   return false;
					  }
				}
				var msg="";
				var alertMsg=""
				var checkFlag=true;
				$('input[type=text]').each(function(){
					 var text=$(this).val();
					 var nameType=$(this).attr("name_type");
					 var requiredTpe=$(this).attr("required");
					 
					 if(requiredTpe || text!=""){
					 	 if(nameType=="fullName"){
					
					 	  var patt1 = new RegExp("^[a-zA-Z][a-z A-Z]{0,19}$");
					 	  var patt2 = new RegExp("^[\u4e00-\u9fa5]{1,10}$");
					 	  var flightName = new RegExp("^([a-zA-Z])+\/+([a-zA-Z])+$");
					 	 if(patt1.test(text)||patt2.test(text)||(flightName.test(text) && text.length<20)){
							$(this).next("span").css("display","none");
						}else{
							//提示
							$(this).next("span").css("display","");
							alertMsg="姓名填写有误";
							checkFlag=false;
							
						}
					 }else if(nameType=="firstName"){
					 	 var patt1 = new RegExp("^[a-zA-Z][a-z A-Z]{0,19}$");
					 	 if(!patt1.test(text)){
							//提示
							$(this).next("span").css("display","");
							alertMsg="firstName填写有误";
							checkFlag=false;
							
						}else{
							$(this).next("span").css("display","none");
						}
					 }else if(nameType=="lastName"){
					 	 var patt1 = new RegExp("^[a-zA-Z][a-z A-Z]{0,19}$");
					 	 if(!patt1.test(text)){
							//提示
							$(this).next("span").css("display","");
							alertMsg="lastName填写有误";
							checkFlag=false;
							
						}else{
							$(this).next("span").css("display","none");
						}
					 }else if(nameType=="email"){
					 	 var patt1 = new RegExp("^([a-zA-Z0-9\.\_\-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$");
					 	 if(!patt1.test(text)){
							//提示
							$(this).next("span").css("display","");
							alertMsg="邮箱填写有误";
							checkFlag=false;
							
						}else{
							$(this).next("span").css("display","none");
						}
					 }else if(nameType=="mobile"){
					 	 //var mobileReg = "^0?1[3|4|5|8][0-9]\d{8}$";
					 	 var myreg = /^\d+$/;
        				if(!myreg.test(text)){
							//提示
							$(this).next("span").css("display","");
							alertMsg="手机号填写有误";
							checkFlag=false;
							
						}else{
							$(this).next("span").css("display","none");
						}
					 }else if(nameType=="idNo"){
					 	var idType=$(this).prev("select").val();
					 	if(idType=="ID_CARD"){
					 		 //var patt1 = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;					 		 
					 		 //if(!patt1.test(text)){
						 	 if(!isIdCardNo(text)){
								//提示
								$(this).next("span").css("display","");
								alertMsg="身份证号填写有误";
								checkFlag=false;
							
								
							}else{
								$(this).next("span").css("display","none");
							}
					 	}
					 }else if(nameType=="flight_number"){
					 	if(text=='' || text.length>600){
					 		$(this).next("span").css("display","");
					 		checkFlag=false;
					 	}else{
					 		$(this).next("span").css("display","none");
					 	}
					 }else if(nameType=="dest_name"){
					 	if(text=='' || text.length>600){
					 		$(this).next("span").css("display","");
					 		checkFlag=false;
					 	}else{
					 		$(this).next("span").css("display","none");
					 	}
					 }else if(nameType=="dest_address"){
					 	if(text=='' || text.length>600){
					 		$(this).next("span").css("display","");
					 		checkFlag=false;
					 	}else{
					 		$(this).next("span").css("display","none");
					 	}
					 }else if(nameType=="outboundPhone"){
					 	if(text){
					 		var myreg = /^\d+$/;
	        				if(!myreg.test(text)){
								//提示
								$(this).next("span").css("display","");
								checkFlag=false;
							}else{
								$(this).next("span").css("display","none");
							}
					 	}
					 }
				   }
				});
				if(alertMsg!="")
				{
					$.alert(alertMsg);
				}
				if(!checkFlag){
					return checkFlag;
				}
				
				var travNumType = $('#travNumType0').val();
				if(travNumType=="TRAV_NUM_ALL"){
					var occupType = $("#occupType").val();
					if(occupType=="TRAV_NUM_ALL"){
						var adult = parseInt($("#occupType").attr("adult"));
						var child = parseInt($("#occupType").attr("child"));
						if(adult>0){
							var oucAdultCount =$("select[name$='.peopleType'] option[value='PEOPLE_TYPE_ADULT']:selected").size();
							if(oucAdultCount!=adult){
								$.alert("人群成人数为"+adult);
								return;
							}
						}
						
						if(child>0){
							var oucChildCount =$("select[name$='.peopleType'] option[value='PEOPLE_TYPE_CHILD']:selected").size();
							if(oucChildCount!=child){
                                $.alert("人群儿童数为"+child);
								return;
							}
						}
					}
				}
				
				var ecName=$("input[name='emergencyPerson.mobile']").val();
				var ecMobile = $("input[name='emergencyPerson.mobile']").val();
				if(ecName && typeof(ecName)!='undefined'){
					$.each($("input[name_type='fullName'][name^='travellers']"),function(i,n){
						var name = $(n).val();
						if(name && ecName==name){
							$.alert("游玩人与紧急联系人姓名一置");
							checkFlag=false;
							return false;
						}
						
					});
					if(!checkFlag){
						return checkFlag;
					}
				}
				if(typeof(ecMobile)!='undefined'){
					$.each($("input[name_type='mobile'][name^='travellers']"),function(i,n){
						var name = $(n).val();
						if(ecMobile==name && ecMobile!=""&&ecMobile!=null){
							$.alert("游玩人与紧急联系人手机号一置");
							checkFlag=false;
							return false;
						}
					});
					if(!checkFlag){
						return;
					}
				}
				
				$.each($("input[name_type='idNo'][name^='travellers']"),function(i,n){
					var idNo1 = $(n).val();
					if(idNo1!='undefined'&&idNo1!=null&&idNo1!=""){
						$.each($("input[name_type='idNo'][name^='travellers']"),function(j,m){
							var idNo2 = $(m).val();
							if(idNo1 == idNo2 && n != m){
								$.alert("游玩人证件号不能一样！");
								checkFlag=false;
								return false;
							}
						});
					}
					if(!checkFlag){
						return false;
					}
				});
				if(!checkFlag){
					return;
				}
				
				var $baoxian = $("td[name='baoxianTd']");
				if($baoxian.length>0){
					var baoxianFlag=false;
					$.each($baoxian,function(i,n){
						var quantity=parseInt($(this).attr("quantity"));
						var len=$(this).find("input[type=checkbox]:checked").length;
						var suppGoodsId=$(this).attr("goodsId");
						
						//对于后置订单，保险可以没有关联游玩人
						if((travellerDelayFlag == 'Y' && quantity < len) ||(travellerDelayFlag != 'Y' && quantity!=len)){
							$.alert($("td[pname='"+suppGoodsId+"']").text()+" 关联人数与购买份数不一置");
							baoxianFlag=true;
						}
					});
					if(baoxianFlag){
						
						checkFlag=false;
					}
				}
				
				if(!checkFlag){
					return;
				}
				
				if(travellerDelayFlag == 'Y' ) {
					localRoutePlaceHolder();
				} 
				
				
					if(sumbitFlg){
				return;
				}
				sumbitFlg = true;
				
				if(travellerDelayFlag == 'Y' ) {
					fillDefaultUserName();
				}
				
				if($("#otherRemark")){
					var remarkValueString ="";
					$("input[name='connectsRemark']").each(function(){
						if($(this).is(':checked')){
							remarkValueString=remarkValueString+$(this).val()+";";
						}
					});
					$("#otherRemark").attr("value",remarkValueString);
				}
				
				if($("#hournumber")){
					$("#connectshourtime").val($("#hournumber").val()+":"+$("#minitenumber").val());
				}
				
				
				
				$.post("/vst_order/ord/book/saveOrderPerson.do",$("form[name='orderInfoForm']").serialize(),function(data){
					if(data==null||!data.success){
						$.alert(data.message);
						sumbitFlg=false;
					}else{
						  //保存订单游玩人信息
						  confirmOrderDialog = new xDialog("/vst_order/ord/book/showVerifyOrder.do",
										      {orderId:$("#orderId").val()},
										      {title:"核对订单",width:950,dialogAuto:true,dialogAutoTop:0});
							orderInfoDialog.close();
					}
				},"JSON");
				
		});
	
	
	
	$("#closeBookInfoPage").bind('click',function(){
		orderInfoDialog.close();
	});

//校验身份信息的合法性
function isIdCardNo(num) {
    num = num.toUpperCase();
    if (!(/(^\d{15}$)|(^\d{17}([0-9]|X)$)/.test(num))) {
        return false;
    }
    //校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。
    var len, re;
    len = num.length;
    if (len == 15) {
        re = new RegExp(/^(\d{6})(\d{2})(\d{2})(\d{2})(\d{3})$/);
        var arrSplit = num.match(re);
        //检查生日日期是否正确
        var dtmBirth = new Date('19' + arrSplit[2] + '/' + arrSplit[3] + '/' + arrSplit[4]);
        var bGoodDay;
        bGoodDay = (dtmBirth.getYear() == Number(arrSplit[2])) && ((dtmBirth.getMonth() + 1) == Number(arrSplit[3])) && (dtmBirth.getDate() == Number(arrSplit[4]));
        if (!bGoodDay) {
            return false;
        } else {
            //将15位身份证转成18位
            //校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。
            var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
            var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
            var nTemp = 0, i;
            num = num.substr(0, 6) + '19' + num.substr(6, num.length - 6);
            for (i = 0; i < 17; i++) {
                nTemp += num.substr(i, 1) * arrInt[i];
            }
            num += arrCh[nTemp % 11];
            return num;
        }
    }
    if (len == 18) {
        re = new RegExp(/^(\d{6})(\d{4})(\d{2})(\d{2})(\d{3})([0-9]|X)$/);
        var arrSplit = num.match(re);
        //检查生日日期是否正确
        var dtmBirth = new Date(arrSplit[2] + "/" + arrSplit[3] + "/" + arrSplit[4]);
        var bGoodDay;
        bGoodDay = (dtmBirth.getFullYear() == Number(arrSplit[2])) && ((dtmBirth.getMonth() + 1) == Number(arrSplit[3])) && (dtmBirth.getDate() == Number(arrSplit[4]));
        if (!bGoodDay) {
            return false;
        }
        else {
            //检验18位身份证的校验码是否正确。
            //校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。
            var valnum;
            var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
            var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
            var nTemp = 0, i;
            for (i = 0; i < 17; i++) {
                nTemp += num.substr(i, 1) * arrInt[i];
            }
            valnum = arrCh[nTemp % 11];
            if (valnum != num.substr(17, 1)) {
                return false;
            }
            return num;
        }
    }
    return false;
}


function fillDefaultUserName() {
	$("#orderInfoForm [name_type=fullName][name^=travellers]").each(function(index, e){
		var fullName = $(e).val();
		if(!fullName) {
			$(e).val('待填写');
		}
	});
}

function localRoutePlaceHolder () {
	//对关联销售的当地游添加默认值，虽然在这个地方处理不是很好
	$("[name=baoxianTd]").each(function(i,v){
		var quantity=$(this).attr("quantity");
		var goodsid = $(this).attr("goodsid");
		
		$(this).find("[type=hidden][name^=personRelationMap]").remove();
		var len=$(this).find("input[type=checkbox]:checked").length;
		if(len < quantity ) {
			var fillCount = 0;
			for(var i=0; i< quantity && len + fillCount <  quantity; i ++ ) {
				var key = "personRelationMap[GOODS_" + goodsid + "].itemPersonRelationList[" + i + "].seq";
				if(!$('[name="' + key + '"]').attr('checked')) { 
					var html = " <input type='hidden' name='" + key + "' value='-520'/>";
					$(this).append(html);
					fillCount = fillCount + 1;
				}
			}
		}
	});
}

 //@ sourceURL=orderFormInfo.ftl
</script>