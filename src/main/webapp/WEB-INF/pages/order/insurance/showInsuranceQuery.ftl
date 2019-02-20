<#--页眉-->
<#import "/base/spring.ftl" as spring/>
<!DOCTYPE html>
<html>
	<head>
	<title>订单管理-后台下单</title>
	<#include "/base/head_meta.ftl"/>
	<link rel="stylesheet" href="/vst_order/js/book/calendar.css" type="text/css"/>
	<link rel="stylesheet" href="/vst_order/js/tooltip/css/global.css" type="text/css" />
	<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/pa-base.css,/styles/v4/modules/arrow.css,/styles/v4/modules/button.css,/styles/v4/modules/forms.css,/styles/v4/modules/selectbox.css,/styles/v4/modules/step.css,/styles/v4/modules/tags.css,/styles/v4/modules/tip.css,/styles/v4/modules/dialog.css,/styles/v4/modules/tables.css" />
	<link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v4/modules/dialog.css,/styles/v4/modules/calendar.css,/styles/v4/modules/tables.css,/styles/v4/modules/bank.css,/styles/v4/order-common.css,/styles/v4/order.css" />
	</head>
	<body>
		<#--页面导航-->
		<div class="iframe_header">
		        <i class="icon-home ihome"></i>
		        <ul class="iframe_nav">
		            <li><a href="#">首页</a> &gt;</li>
		            <li><a href="#">订单管理</a> &gt;</li>
		            <li class="active">保险订单产品查询</li>
		        </ul>
		</div>
	
		<div id="userInfoDiv" style="text-align:center;position:fixed;margin-left:500px;margin-top:-30px;background:#F9FAFB;"> 
			<#if user.userId?? >
			    <span style="color: #EE3388;font-size:22px;font-weight: bold;">客人姓名：${user.userName}</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="accountLogout();">退出当前用户</a>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="clearFindWhere();">清空查找条件</a>
			<#else>
				<span style="color: #EE3388;font-size:22px;font-weight: bold;">尚未登陆会员信息</span>&nbsp;&nbsp;<a href="javascript:void(0);" onclick="clearFindWhere();">清空查找条件</a>
			</#if>
		</div>
		
		<div>
			<#--表单区域-->
			<div class="iframe_search">
				<form id="searchForm" action="/vst_order/ord/insurance/query.do" method="post" onsubmit="return false">
					<#-- spring提供的FreeMarker隐藏控件实现-->
					<@spring.formHiddenInput "vo.recommendLevel" ""/>
					<table class="s_table  form-inline">
			            <tbody>
			                <tr>
			                	<td class="w12 s_label">产品编号：</td>
			                    <td class="w15">
                    				<input type="text" id="productId" name="productId" value="">
			                    </td>
			                	<td class="w12 s_label">产品名称：</td>
			                    <td class="w15">
                    				<input type="text" id="productName" name="productName" value="">
			                    </td>
				                <td class="s_label">被保天数：</td>
				               	<td class="w18">	
				               		<select name="daysType"> 
					               	 	<option value="">请选择</option>
				                    	<#list 1..60 as counter>
				                    		<option value="${counter}">${counter}天</option>
				                    	</#list>
			                    	</select>
				                </td>			                    
			                </tr>			                
			                <tr>
			                	<td class="w12 s_label">保险险种：</td>
			                    <td colspan="5">
					    			<#list insurTypeDictList as insurTypeDict>
					    				<input type="checkbox"  name="insurType" value="${insurTypeDict.dictId!''}"/>${insurTypeDict.dictName!''}
						             </#list>
			                    </td>
			                </tr>
			                <tr>
			                    <td class=" operate mt10">&nbsp;</td>
			                    <td class=" operate mt10">&nbsp;</td>
			                    <td class=" operate mt10">&nbsp;</td>
			                    <td class=" operate mt10">&nbsp;</td>
			                    <td class=" operate mt10">&nbsp;</td>
			                    <td class="operate mt10">
				                    <a class="btn btn_cc1" id="search_button">查询</a> 
				                    <a class="btn btn_cc1" id="clear_button">清空</a>
			                    </td>
		                    </tr>
			            </tbody>
			        </table>
				</form>
			    <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
			</div>		
			<#--结果显示-->
			<div id="result" class="iframe_content mt20">
				
			</div>
			<#include "/order/orderProductQuery/member_dialog.ftl"/>
			<#include "/base/foot.ftl"/> 
			<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script>
			<form id="bookForm" action="" method="post">
			 
			</form>
			<script>
				var basePath = '${rc.contextPath}';
				var book_user_id='${user.userId}';
				/**
				 * 绑定查询按钮事件
				 */
				$("#search_button").bind("click",function(){
					
					//加载效果
					$("#result").empty();
					$("#result").append("<div class='loading mt20'><img src='/vst_order/img/loading.gif' width='32' height='32' alt='加载中'>正在努力的加载数据中......</div>");
						 		//显示推荐级别区域
					//ajax加载查询结果
					var $form=$(this).parents("form");
					 
					$("#result").load($form.attr("action"),$form.serialize(),function(){});
					/**
					$.ajax({
						type:"post",
						url:$form.attr("action"),
						data:$form.serialize(),
						success:function(result){				
							$("#result").html(result);
						}
					});						
					*/				
				});
				
				//清空查找条件
				function clearFindWhere(){
					 $("#searchForm")[0].reset();
				}
				var urlStr="";
				function toBook(url){
					urlStr=url;
					 if(book_user_id==""){
				      	showQueryUserIdDialog();
				      	return;
				      }else{
				      	submitFormCallback.invoke();
				      }
				}
				function infoBookInfo(){
				 	$("#bookForm").attr("action",urlStr+"&userId="+book_user_id);
					 $("#bookForm").submit();
				}				
				submitFormCallback.pushFun(infoBookInfo);
			</script>
	</body>
</html>