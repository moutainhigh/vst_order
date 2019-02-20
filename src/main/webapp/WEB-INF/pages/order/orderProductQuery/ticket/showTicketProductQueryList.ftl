<#--页眉-->
<#import "/base/spring.ftl" as s/>
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
		            <li class="active">门票订单产品查询</li>
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
				<form id="searchForm" action="/vst_order/ord/productQuery/ticket/searchTicketList.do" method="post" onsubmit="return false">
					<@s.formHiddenInput "vo.recommendLevel" ""/>
					<table class="s_table  form-inline">
			            <tbody>
			                <tr>
			                	<td class="w18 s_label">目的地城市/产品名称/产品ID：</td>
			                    <td class="">
			                    	<#--<@s.formHiddenInput "ordOrderProductQueryVO.districtId" ""/>-->
			                    	<input class="w15" type="text" class="search" id="keywordTxt" name="keyword" value="" required>
			                    </td>
			                </tr>
			                <tr>
			                	<td class="w12 s_label">是否支持当天预订：</td>
			                    <td class="w18">
			                    	  <input class="radio" checked="checked" name="todayTicketFlag" value="" type="radio">&nbsp;&nbsp;不限&nbsp;&nbsp;&nbsp;&nbsp;
			                    	  <input class="radio" name="todayTicketFlag" value="1" type="radio">&nbsp;&nbsp;是&nbsp;&nbsp;&nbsp;&nbsp;
			                    	  <input class="radio" name="todayTicketFlag" value="0" type="radio">&nbsp;&nbsp;否
			                    </td>
			                </tr>
			            </tbody>
			        </table>
			        <div class="operate mt20" style="text-align:center">
			        	<a class="btn btn_cc1" id="search_button">查询</a>
			        	<a class="btn btn_cc1" id="clear_button" onclick="clearFindWhere();">清空</a>
			        </div>
			        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
				</form>
			</div>
		 	<#--结果显示-->
			<div class="f14" id="recommendDiv" style="display:none;margin-left:20px;margin-right:20px;">
				<ul class="ui_tab_1">
					<li><a id="level1" href="javascript:searchByRecommendLevel('1','level1');">不建议(1)</a></li>
					<li><a id="level2" href="javascript:searchByRecommendLevel('2','level2');">不推(2)</a></li>
					<li><a id="level3" href="javascript:searchByRecommendLevel('3','level3');">可订(3)</a></li>
					<li><a id="level4" href="javascript:searchByRecommendLevel('4','level4');">可推(4)</a></li>
					<li><a id="level5" href="javascript:searchByRecommendLevel('5','level5');">特推(5)</a></li>
					<li class="active"><a id="levelAll" href="javascript:searchByRecommendLevel('all','levelAll');">全部</a></li>
			    </ul>
			</div>
			<div id="result" class="iframe_content mt20">
				
			</div>
		</div>
		<#include "/order/orderProductQuery/member_dialog.ftl"/>
		<#include "/base/foot.ftl"/>
		<script src="/vst_order/js/book/bookMemberLogin.js?version=2014081112345"></script>
		<form id="bookForm" action="" method="post">
			 
		</form>
		<script>
			var book_user_id='${user.userId}';
			
			//清空查找条件
			function clearFindWhere(){
				 $("#searchForm")[0].reset();
			}
			
			/**
			 * 绑定查询按钮事件
			 */
			$("#search_button").bind("click",function(){
				//验证
				if(!$("#searchForm").validate().form()){
					return false;
				}
				//加载效果
				$("#result").empty();
				$("#result").append("<div class='loading mt20'><img src='/vst_order/img/loading.gif' width='32' height='32' alt='加载中'>正在努力的加载数据中......</div>");
		 		//显示推荐级别区域
				$("#recommendDiv").show();
				//ajax加载查询结果
				var $form=$(this).parents("form");
				 
				$("#result").load($form.attr("action"),$form.serialize(),function(){
					 
				});
			});
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
			
			/**
			 * 根据推荐级别查询
			 * @param level
			 * @param obj
			 */
			function searchByRecommendLevel(level,id){
				try{
					//设置隐藏域的推荐级别值
					if(level=="all"){
						$("#recommendLevel").val("");
					}else{
						$("#recommendLevel").val(level);
					}
					
					//拿到tab父节点
					$("#"+id).parent().parent(".ui_tab_1").children().removeClass();
					//设置当前选项卡的选中样式
					$("#"+id).parent().addClass("active");
					
					$("#result").empty();
					$("#result").append("<div class='loading mt20'><img src='../../img/loading.gif' width='32' height='32' alt='加载中'>正在努力的加载数据中......</div>");
					
					//ajax加载结果
					var $form=$("#searchForm");
					
					$("#result").load($form.attr("action"),$form.serialize(),function(){
						 
					});
				}catch(e){
					alert(e.message);
				}
			}
			submitFormCallback.pushFun(infoBookInfo);
			
			function goodsDetailMouseover(obj,suppGoodsId){
				$.ajax({
					url : "/vst_order/ord/book/ticket/getTicketGoodsDetail.do",
					type : "post",
					dataType:"JSON",
					async:false,
					data : {"suppGoodsId":suppGoodsId},
					success : function(data) {
						var tipContent = "";
						if(data.success){
							var detail=data.attributes.suppGoodsTicketDetailVO;
							 if(detail.suppGoodsDesc!=null&&detail.suppGoodsDesc.changeAddress!=""){
							  	tipContent = "取票地点："+detail.suppGoodsDesc.changeAddress+"<br/>";
							 }
						}else{
							tipContent = "无数据";
						}
						$(obj).attr("tip-content",tipContent+$(obj).next("div").html());
					}
				});
			}
			
			function initCancelStrategy(obj,suppGoodsId,aperiodicFlag){
					$.ajax({
						url : "/vst_order/ord/book/ticket/getTicketGoodsDetail.do",
						type : "post",
						dataType:"JSON",
						async:false,
						data : {"suppGoodsId":suppGoodsId,"aperiodicFlag":aperiodicFlag},
						success : function(data) {
							if(data.success){
								 var str=data.attributes.suppGoodsRefundStr;
								 if(str!=null&&str!=""){
								  	  $(obj).attr("tip-content",str);
								 }else{
									 $(obj).attr("tip-content","无");
								 }
							}else{
								 $(obj).attr("tip-content","无");
							}
						}
					});
			}
			function initCombCancelStrategy(obj,productId){
					$.ajax({
						url : "/vst_order/ord/book/ticket/getCombTicketGoodsDetail.do",
						type : "post",
						dataType:"JSON",
						async:false,
						data : {"productId":productId},
						success : function(data) {
							if(data.success){
								 var str=data.attributes.suppGoodsRefundStr;
								 if(str!=null&&str!=""){
								  	  $(obj).attr("tip-content",str);
								 }else{
									 $(obj).attr("tip-content","无");
								 }
							}else{
								 $(obj).attr("tip-content","无");
							}
						}
					});
			}
		</script>
	</body>
</html>
