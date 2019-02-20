function travellersBindEvent(){ //绑定常用游客事件
	$("table [name=userInfoTb]").each(function(){
		$(this).find("tr:eq(0) input[type=checkbox]").change(function(){
			checkIocTxt(this);
		});
		$(this).find("tr:eq(1) input[type=text]").change(function(){
			txtIocCheck(this);
			nameTxtChange();
		});
	});
	/*$("table [name=userInfoTb] tr:eq(0)").find("input[type=checkbox]").change(function(){
		checkIocTxt(this);
	});

	$("table [name=userInfoTb] tr:eq(1)").find("input[type=text]").change(function(){
		txtIocCheck(this);
		nameTxtChange();
	});*/
}
function checkIocTxt(obj){ //常用游客checkbox控制客人姓名text
	$(obj).closest("td").find("input[type=checkbox]:checked").each(function(){
	    var receiverId=$(this).val();
	    var fullName=$(this).attr("personName");
	    var mobile=$(this).attr("mobile");
	    var email=$(this).attr("emails");
	    var idNo=$(this).attr("idNo");
	    var expDate=$(this).attr("expDate");
	    var issued=$(this).attr("issued");
	    var birthday = $(this).attr("birthday");
    	var gender =$(this).attr("gender");
	    var idType=$(this).attr("idType");
	    var firstName=$(this).attr("firstName");
	    var lastName=$(this).attr("lastName");
	    var peopleType=$(this).attr("peopleType");
	    var checkedFlag=false;

	    var fullNameIpt=$(obj).closest("tr").next("tr").find("input[type=text]");
	    var travellersId=$(fullNameIpt).attr("travellersId");
    	var name=$(fullNameIpt).val();
    	if(fullName==name&&travellersId==receiverId){
			checkedFlag=true;
    	}

	    if(!checkedFlag){
	    	var tb=$(obj).closest("table");
	    	var index=$(tb).attr("index");
	    	var travellersId=$("#fullName" + index).attr("travellersId");
	    	var name=$("#fullName" + index).val();
    		if(travellersId==''&&name==''){
				checkedFlag=true;
				$("#fullName" + index).val(fullName);
				$("#fullName" + index).next("input[type=hidden]").val(receiverId);
				$("#fullName" + index).attr("travellersId",receiverId);
				$("#firstName" + index).val(firstName);
				$("#lastName" + index).val(lastName);
				$("#peopleType" + index).val(peopleType);
				$("#mobile" + index).val(mobile);
				$("#email" + index).val(email);
				$("#idNo" + index).val(idNo);
				$("#idType" + index).val(idType);
				$("#idType" + index).trigger("change");
				if(idType!='ID_CARD'&& idType!='CUSTOMER_SERVICE_ADVICE'){
					//showBirthdayDiv(index);
					$("#birthday" + index).val(birthday);
					$("#gender" + index).val(gender);
					//alert("aaa"+birthday);
				}else{
					$("#person_birthday_"+index+" span.data").html("");
					$("#person_birthday_"+index).hide();
				}
				if(idType="HUIXIANG"||idType=="TAIBAOZHENG"){
					//showIssuedDiv(index);
					$("#expDate" + index).val(expDate);
					$("#issued" + index).val(issued);
				}else{
					$("#person_issued_"+num+" span.data").html("");
					$("#person_issued_"+num).hide();
				}
				nameTxtChange();
			}
	    }
	   	if(!checkedFlag){
	   		$(obj).removeAttr("checked");
	   	}
	  });

	$(obj).closest("td").find("input[type=checkbox]").not("input:checked").each(function(){
		    var receiverId=$(this).val();
		    var tb=$(obj).closest("table");
	    	var index=$(tb).attr("index");
	    	var travellersId=$("#fullName" + index).attr("travellersId");
	    	var name=$("#fullName" + index).val();
    		if(receiverId==travellersId){
				checkedFlag=true;
				$("#fullName" + index).val("");
				$("#fullName" + index).next("input[type=hidden]").val("");
				$("#fullName" + index).attr("travellersId","");
				$("#firstName" + index).val("");
				$("#lastName" + index).val("");
				$("#peopleType" + index).val("");
				$("#mobile" + index).val("");
				$("#email" + index).val("");
				$("#idNo" + index).val("");
				$("#idType" + index).val("");
				$("#expDate" + index).val("");
				$("#issued" + index).val("");
				$("#birthday" + index).val("");
				$("#gender" + index).val("");
				$("#idType" + index).trigger("change");
				nameTxtChange();
			}
	  });
}
function txtIocCheck(obj){//客人姓名text控制常用游客checkbox
	$(obj).closest("tr").prev("tr").find("td input[type=checkbox]:checked").each(function(){
	    var fullName=$(this).attr("personName");
	    var receiverId=$(this).val();
	    var checkedFlag=false;

	    var travellersId=$(obj).attr("travellersId");
    	var name=$(obj).val();
    	if(fullName==name&&travellersId==receiverId){
			checkedFlag=true;
    	}

	   	if(!checkedFlag){
	   		var tb=$(obj).closest("table");
	    	var index=$(tb).attr("index");
			$("#fullName" + index).val("");
			$("#fullName" + index).next("input[type=hidden]").val("");
			$("#fullName" + index).attr("travellersId","");
			$("#firstName" + index).val("");
			$("#lastName" + index).val("");
			$("#peopleType" + index).val("");
			$("#mobile" + index).val("");
			$("#email" + index).val("");
			$("#idNo" + index).val("");
			$("#idType" + index).val("");
			$("#expDate" + index).val("");
			$("#issued" + index).val("");
			$("#birthday" + index).val("");
			$("#gender" + index).val("");
			$(this).removeAttr("checked");
			nameTxtChange();
	   	}
  });

	 var personId=$(obj).attr("travellersId");
     var personName=$(obj).val();
     $(obj).closest("tr").prev("tr").find("td input[type=checkbox]").each(function(){
			var receiverId=$(this).val();
	     	if(personName==$(this).attr("personName")){
	     		 var receiverId=$(this).val();
			    var fullName=$(this).attr("personName");
			    var mobile=$(this).attr("mobile");
			    var email=$(this).attr("emails");
			    var idNo=$(this).attr("idNo");
			    var idType=$(this).attr("idType");
			    var firstName=$(this).attr("firstName");
			    var lastName=$(this).attr("lastName");
			    var peopleType=$(this).attr("peopleType");
			    var expDate=$(this).attr("expDate");
			    var issued=$(this).attr("issued");
		    	var birthday = $(this).attr("birthday");
		    	var gender =$(this).attr("gender");
			    //var num = $(this).attr("num");
			    var tb=$(obj).closest("table");
		    	var index=$(tb).attr("index");
			    $("#fullName" + index).val(fullName);
			    $("#fullName" + index).next("input[type=hidden]").val(receiverId);
				$("#fullName" + index).attr("travellersId",receiverId);
				$("#firstName" + index).val(firstName);
				$("#lastName" + index).val(lastName);
				$("#peopleType" + index).val(peopleType);
				$("#mobile" + index).val(mobile);
				$("#email" + index).val(email);
				$("#idNo" + index).val(idNo);
				$("#idType" + index).val(idType);
				if(idType!='ID_CARD'&& idType!='CUSTOMER_SERVICE_ADVICE'){
					showBirthdayDiv(index);
					$("#birthday" + index).val(birthday);
					$("#gender" + index).val(gender);
				}else{
					//alert($("#birthday" + index));
					$("#person_birthday_"+index+" span.data").html("");
					$("#person_birthday_"+index).hide();
				}
				if(idType="HUIXIANG"||idType=="TAIBAOZHENG"){
					showIssuedDiv(index);
					$("#expDate" + index).val(expDate);
					$("#issued" + index).val(issued);
				}else{
					$("#person_issued_"+index+" span.data").html("");
					$("#person_issued_"+index).hide();

				}
				//alert(index+"   "+"wfssdfsd");
    			$(this).attr("checked","checked");
    			return false;
	    	}
		});
}

function nameTxtChange(){
	baoxianRelatePerson();
}
function baoxianRelatePerson(){
	$("td[name=baoxianTd]").each(function(i,v){
		var goodsId=$(this).attr("goodsId");
		var personStr="";
		$("table [name=userInfoTb]").each(function(){
			$.each($(this).find("tr:eq(1) td input"),function(ii,v){
				var tb=$(this).closest("table");
		    	var index=$(tb).attr("index");
				personStr+="<label class='checkbox mr10'><input type='checkbox' name='personRelationMap[GOODS_"+goodsId+"].itemPersonRelationList["+index+"].seq' value='"+index+"'>"+$(v).val()+"</label>";
			});
		});
		$(this).html(personStr);
	});
}

$(document).ready(function(){
	$("select[id^='idType']").each(function(){
		var num = $(this).attr("num");
		var val = $(this).val();
		if(val!='ID_CARD'&& val!='CUSTOMER_SERVICE_ADVICE'){
			showBirthdayDiv(num);
			$(this).attr("old",val);
		}

		if(val=='HUIXIANG'|| val=='TAIBAOZHENG'){
			showIssuedDiv(num);
			$(this).attr("old",val);
		}else{
			$("#person_issued_"+num+" span.data").html("");
			$("#person_issued_"+num).hide();
		}

		if(val=='CUSTOMER_SERVICE_ADVICE'){
			$("#idNo"+num).hide();
		}else{
			$("#idNo"+num).show();
		}
	});

    loadOneKeyOrderPersons();
});

function showBirthdayDiv(num){
	var birthday = $("#idType"+num).attr("birthday");

	var gender = $("#idType"+num).attr("gender");
	var manSelected="";
	var womanSelected="";
	if(gender=="MAN"){
		manSelected="selected='selected'";
	}
	if(gender=="WOMAN"){
		womanSelected="selected='selected'";
	}
	var html ="出生日期<input type='text' value='"+birthday+"' id='birthday"+num+"' name='travellers["+num+"].birthday' style='width:120px' required=true readonly='true' onfocus=\"WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})\"/>";
	html+="性别<select name='travellers["+num+"].gender' id='gender"+num+"' style='width:100px' required=true><option "+manSelected+" value='MAN'>男</option><option "+womanSelected+" value='WOMAN'>女</option></select>";
	$("#person_birthday_"+num+" span.data").html(html);
	$("#person_birthday_"+num).show();

}

function showIssuedDiv(num){
	var expDate = $("#idType"+num).attr("expDate");
	var issued = $("#idType"+num).attr("issued");
	var html ="有效期:  <input type='text' value='"+expDate+"' id='expDate"+num+"'  name='travellers["+num+"].expDate' style='width:120px'  readonly='true' onfocus=\"WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})\"/>";
		html+="签发地:  <input type='text' value='"+issued+"' id='issued"+num+"'  name='travellers["+num+"].issued'  style='width:100px' />";
	$("#person_issued_"+num+" span.data").html(html);
	$("#person_issued_"+num).show();

}
function isNotShowBirthday(val){
	return val=="ID_CARD"|| val=='CUSTOMER_SERVICE_ADVICE';
}

$("select[id^='idType']").change(function(){
	var num = $(this).attr("num");
	var val =$(this).val();
	var oldVal=$(this).attr("old");

	if(isNotShowBirthday(val)){
		$("#person_birthday_"+num+" span.data").html("");
		$("#person_birthday_"+num).hide();
	}else{
		if(typeof(oldVal)=="undefined"||oldVal==""||isNotShowBirthday(oldVal)){//alert("eeee");
			showBirthdayDiv(num);
		}
	}
	if(val=='HUIXIANG'|| val=='TAIBAOZHENG'){
		showIssuedDiv(num);
	}else{
		$("#person_issued_"+num+" span.data").html("");
		$("#person_issued_"+num).hide();
	}
	if(val=='CUSTOMER_SERVICE_ADVICE'){
		$("#idNo"+num).val("");
		$("#idNo"+num).hide();
	}else{
		$("#idNo"+num).show();
	}

	$(this).attr("old",val);
});


// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
// 例子：
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1,                 //月份
        "d+": this.getDate(),                    //日
        "h+": this.getHours(),                   //小时
        "m+": this.getMinutes(),                 //分
        "s+": this.getSeconds(),                 //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds()             //毫秒
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

/**
 * 一键下单加载人员信息
 */
function loadOneKeyOrderPersons() {
    var orderCreatingManner = $("#bookForm input[name=orderCreatingManner]").val();
    var originalOrderId = $("#bookForm input[name=originalOrderId]").val();
    if (orderCreatingManner == "2") {
        $.ajax({
            url: "/vst_order/ord/order/lineBackLoadOriginalOrderPersons.do",
            async: true,
            data: {
                originalOrderId: originalOrderId
            },
            type: "POST",
            success: function (data) {
//                console.log(data);
                try {
                	
                    if (data.success == true) {
                        var persons = data.attributes.orderRelatedPersonsVO;
                        if (persons != null && persons != "") {
                            var contect = persons.contact;

                            //订单联系人
                            $("input[name='contact.fullName']").val(contect.fullName);
                            $("input[name='contact.mobile']").val(contect.mobile);
                            $("#contactEmail").val(contect.email);
                            $("#contactPersonId").val(contect.ordPersonId);

                            //紧急联系人
							if(persons.emergencyContact!=null){
								$("input[name='emergencyPerson.fullName']").val(persons.emergencyContact.fullName);
								$("#emergencyPersonId").val(persons.emergencyContact.ordPersonId);
								$("input[name='emergencyPerson.mobile']").val(persons.emergencyContact.mobile);
							}


                            var travellerList = persons.travellerList;

                            var personNum = parseInt($("#personNum").val());
                            if (isNaN(personNum)) {
                                personNum = 0;
                            }

                            //  截取前面N个游玩人填充
                            //  birthday、gender、expDate、issued这些可能出现在select的属性值上面，
                            // 在下单的时候这个用户是不能填写的，所以这里不做处理；
                            for (var i = 0; i < travellerList.length && i < personNum; i++) {
                                $.each(travellerList[i], function (name, value) {
                                    //日期格式化
                                    if ((name == "expDate" || name == "birthday") && value != null) {
                                        value = new Date(value).Format("yyyy-MM-dd");
                                    }

                                    var input = $("input[name='travellers[" + i + "]." + name + "']");
                                    var select = $("select[name='travellers[" + i + "]." + name + "']");

                                    if (value != null) {
                                        if (input != undefined && input.size() == 1) {
                                            input.val(value);
                                        }

                                        if (select != undefined && select.size() == 1) {
                                            select.val(value);
                                        }
                                        //程序修改的属性，不会自动触发change事件
                                        $("select[id^='idType']").change();
                                    }
                                });
                            }
                            nameTxtChange();

                            var relationMap = data.attributes.goodPersonRelationMap;
                            processRelatePerson(relationMap, travellerList);
                        }

                        //是否为测试订单：
                        var isTestOrder = $("#bookForm input[name='isTestOrder']").val();
                        if (isTestOrder == "Y") {
                            $("input[name=isTestOrder]:eq(0)").attr("checked", 'true');
                        }
                    } else {
                        $.dialog("人员信息获取失败，请手工填写！");
                    }
                } catch (e) {
                    $.dialog("人员信息获取失败，请手工填写！");
                }
                if(data.attributes!=null && data.attributes.comfirm!=null){
                	try{
                		var comfirm=data.attributes.comfirm;
                    	if(comfirm.containOldMan=="Y"){
                    		$("input[name='orderTravellerConfirm.containOldMan'][value='Y']").attr("checked","checked")
                		}
                    	if(comfirm.containForeign=="Y"){
                    		$("input[name='orderTravellerConfirm.containForeign'][value='Y']").attr("checked","checked")
                		}
                    	if(comfirm.containPregnantWomen=="Y"){
                    		$("input[name='orderTravellerConfirm.containPregnantWomen'][value='Y']").attr("checked","checked")
                		}
                	}catch (e){
                		
                	}
            	}
            }
        });
    }
}

/**
 * 处理保险关联人复选框选中
 * @param relationMap
 * @param travellerList
 */
function processRelatePerson(relationMap, travellerList) {
    if (relationMap != null && relationMap != "") {
        $.each(relationMap, function (name, value) { //遍历所有的商品
            for (var j = 0; j < value.length; j++) {    //遍历当前商品所关联的人员
                for (var i = 0; i < travellerList.length ; i++) { //判断人员所对应的CHECKBOX顺序
                    if(value[j]==travellerList[i].ordPersonId){
                        //value[j]被商品关联
                        $("input[name='personRelationMap[GOODS_"+name+"].itemPersonRelationList["+i+"].seq']").attr("checked",true);
                    }
                }
            }
        });
    }
}