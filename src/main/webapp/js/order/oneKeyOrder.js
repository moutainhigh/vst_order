//一键重下 
var oneKeyRecreateUser={};
$(document).ready(function(){
	$("div[div-type='backOneKeyRecreating']").hide();
	$.ajax({
		url:"/vst_order/order/orderManage/checkPrerequisite.do",
		type:"POST",
		cache:false,
		data:{
			orderId:$("#oneKeyOrderForm_originalOrderId").val()
		},
		dataType:"json",
		success : function(result){
			if(result.code=="success"){
				$("div[div-type='backOneKeyRecreating']").show();
				$("#showMobileRegButton").hide();
				$("#checkUserInfoButton").hide();
				var searchUserInput=$("#demo13H").find("input[name='searchUser']").eq(0);
				searchUserInput.attr("value",result.attributes.user.userName);
				searchUserInput.attr("readonly","readonly");
				oneKeyRecreateUser=result.attributes.user;
			}else{
				if(window.console){
					window.console.log(result.message);
				}
			}
	   }
	});
	$("#backOneKeyRecreatingButton").bind("click",function(){
		showQueryUserIdDialog();
		putUserToDialog(oneKeyRecreateUser);
	});
});


/**
** 显示会员查询对话框
**/
function showQueryUserIdDialog(){
	//遮罩层
	var loading = pandora.loading("");
	selectUserDialog = $.dialog({
        width: 600,
        title: "会员识别",
        content: $("#demo13H").html()
    });
	
	$('a.dialog-close').click(function(){
		$("div.searchUser").hide();
		$("div.userMobile").hide();
		$('.overlay,.dialog-loading').remove();
	});
}
function putUserToDialog(user){
	$("#mobileDiv").hide();
//	$.post("/vst_order/ord/book/queryUser.do",{"key":searchUser},function(data){
//		$("#userListDiv").html(data);
//		if($("#userListDiv").find("input[type='radio']").length==1){
//			$("#userListDiv").find("input[type='radio']").eq(0).attr("checked","checked");
//		}
//	});\
	var html=$("#userSelectBaseForm").html();
	var form=$("<form id='userSelectForm'></form>");
	form.append(html);
	$("#userListDiv").empty();
	$("#userListDiv").append(form);
	var userinput=$("#userSelectForm").find("input[name='user_id']").eq(0);
	userinput.attr("userName",user.userName);
	userinput.attr("value",user.userId);
	$("#userSelectForm").find("td[data-type='name']").html(user.userName);
	$("#userSelectForm").find("td[data-type='zhUserStatus']").html(user.zhUserStatus);
	$("#userSelectForm").find("td[data-type='email']").html(user.email);
	$("#userSelectForm").find("td[data-type='mobileNumber']").html(user.mobileNumber);
	$("#userSelectForm").find("td[data-type='memberShipCard']").html(user.memberShipCard);
	
}




function selectUser(){
	
	var $form=$("#userSelectForm");
	var user=$form.find("input[name='user_id']:checked").val();
	var userName=$form.find("input[name='user_id']:checked").attr("userName");
	if(typeof(user)=="undefined"){
		alert("请先选中会员");
		return;
	}
	//校验是否该账号已经被冻结。
	if(isUserFrozen("userId",user)){
		$("div.searchUser span").html("此账号已被驴妈妈冻结，不能下单，请更换账号下单。");
		$("div.searchUser").show();
		return;
	};

	closeUserDialogAndShowGoodsInfo(user,userName);
}

function isUserFrozen(type,param){
	var isFrozen=false;
	var reqUrl="";
	if('mobile'==type){
		//mobileVal=$("input[name='userMobile']").val();
		reqUrl="/vst_order/ord/book/validFrozenByUserMobile.do";
	}else{
		//mobileVal=$("input[name='user_id']:checked").parent().next().next().text();
		reqUrl="/vst_order/ord/book/validFrozenByUserId.do";
	}
	$.ajax({
		type:"POST",
		async: false,
		url: reqUrl,
		data: {param: param},
		success: function(response){
			if(response.isFrozen=="Y"){	//condition: 是冻结手机号
				//如果是冻结电话号码提示
				isFrozen=true;
			}
		}
	});
 return isFrozen;

}

function closeUserDialogAndShowGoodsInfo(user,userName){
	$("#oneKeyOrderForm").find("input[name='userId']").eq(0).val(user);
	selectUserDialog.close();
	$('.overlay,.dialog-loading').remove();
	$("#oneKeyOrderForm").submit();
}
