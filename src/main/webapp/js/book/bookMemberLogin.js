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
		var $channelInput = $("input[name='channel_code']:checked");
		if($channelInput.length > 0){
			var channel_code = $channelInput.val();
			if(channel_code != 'no_o2o'){
                $("#bookForm input[name='channel_code']").val(channel_code);
			}
		}
	
		closeUserDialogAndShowGoodsInfo(user,userName);
	}
	
	function closeUserDialogAndShowGoodsInfo(user,userName){
		book_user_id = user;
		$("input[type=hidden][name=userId]").val(user);
		$("#userInfoDiv").html(' <span style="color: #EE3388;font-size:22px;font-weight: bold;">客人姓名：'+userName+'</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="accountLogout();">退出当前用户</a>');
		selectUserDialog.close();
		$('.overlay,.dialog-loading').remove();
		submitFormCallback.invoke();
	}
	
	function searchUser(){
		var searchUser = $("input[name='searchUser']").val();
		if($.trim(searchUser)==''){
			return;
		}
		
		if($("#checkUserInfoButton").attr("disabled")) {
			return;
		}
		$("#checkUserInfoButton").attr("disabled", true);
		$("#checkUserInfoButton").css("border","1px solid #aaa").css("background-color","#bbb").css("color", "#000").css("cursor", "default");
		
		$("#mobileDiv").hide();
		$.post("/vst_order/ord/book/queryUser.do",{"key":searchUser},function(data){
			$("#userListDiv").html(data);
			$("#checkUserInfoButton").attr("disabled", false);
			$("#checkUserInfoButton").css("border","1px solid #3f87fe").css("background-color","#4d90fe").css("color", "#fff").css("cursor", "pointer");
		});
	}
	function showMobileReg(){
		$("#userListDiv").empty();
		$("#mobileDiv").show();
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
	
	function regUserAccount(){
		//校验是否该账号已经被冻结。
		if(isUserFrozen("mobile",$("input[name='userMobile']").val())){
			$("div.userMobile span").html("此手机号已被驴妈妈冻结，不能下单，请更换手机号下单。");
			$("div.userMobile").show();
			return;
		};
		$("div.userMobile").hide();
		var $form = $("#userMobileForm");
		var reqData = $form.serialize();
        var $channelInput = $("input[name='channel_code']:checked");
        if($channelInput.length > 0){
            var channel_code = $channelInput.val();
            if(channel_code != 'no_o2o'){
                $("#bookForm input[name='channel_code']").val(channel_code);
                reqData = reqData + '&channel_code=' + channel_code;
            }
        }

		$.post("/vst_order/ord/book/regUser.do",
			reqData,
			function(data){
				if(data.success){
					closeUserDialogAndShowGoodsInfo(data.attributes.userId);
				}else{
					$("div.userMobile span").html(data.message);
					$("div.userMobile").show();
				}
		},"JSON");
	}
	//注销
	function accountLogout(){
		book_user_id = "";
		//$("#userInfoDiv").hide();
		$("#userInfoDiv").html('  <span style="color: #EE3388;font-size:22px;font-weight: bold;">尚未登陆会员信息</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="clearFindWhere();">清空查找条件</a>');
	}
	function loginCallback(){//回调接口
		this.funs = [];
		this.pushFun = function(fun) {
			this.funs.push(fun);
		};
		this.invoke = function() {
			for (var i = 0; i < this.funs.length; i++) {
				try {
					this.funs[i]();
				} catch (err) {
					alert(err);
					return false;
				}
			}
		};
	}
	//提交表单回调接口
	var submitFormCallback = new loginCallback();