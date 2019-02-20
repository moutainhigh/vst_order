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
		            <li class="active">签证订单产品查询</li>
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
				<form id="searchForm" action="/vst_order/ord/productQuery/visa/findVisaList.do" method="post" onsubmit="return false">
					<@s.formHiddenInput "vo.recommendLevel" ""/>
					<table class="s_table  form-inline">
			            <tbody>
			                <tr>
			                	<td class="w12 s_label">签证产品名称/ID：</td>
			                    <td class="w15">
			                    	 <@s.formHiddenInput "vo.product" ""/>
                    				<input type="text" class="search" id="productName" name="productName" value="<#if ordOrderProductQueryVO??>${ordOrderProductQueryVO.product}</#if>" required>
			                    </td>
			                </tr>
			                <tr>
			                	<td class="w12 s_label">签证国家/地区：</td>
			                    <td class="w15">
			                    	<@s.formHiddenInput "vo.visaCountry" ""/>
                    				<input type="text" class="search" id="visaCountryName" name="visaCountryName" value="<#if ordOrderProductQueryVO??>${ordOrderProductQueryVO.visaCountry}</#if>" required>
			                    </td>
			                </tr>
			                <tr>
			                	<td class="w12 s_label">签证类型：</td>
			                    <td >
			                    	<label class="checkbox mr10"><input type="checkbox" name="vistType" value="">全部</label>
			                    	<#if vistTypeList?? && vistTypeList?size gt 0>
				                    	<#list vistTypeList as dictExtend>
					            			<label class="checkbox mr10">
					            			<#if dictExtend.dictName =='个人旅游签证' || dictExtend.dictName == '商务签证' || dictExtend.dictName == '探亲访友签证'>
					            			<input type="checkbox" name="visaType" value="${dictExtend.dictId!''}"
					            				<#if vo??>
				            							<#if vo.visaType == dictExtend.dictId>checked</#if>
					            				</#if>
					            			>${dictExtend.dictName!''}</label>
					            			</#if>
					            			
					            		</#list>
				                    </#if>
			                    </td>
			                </tr>
			                <tr>
			                	<td class="w12 s_label">所属领区：</td>
			                    <td >
			                    	<label class="checkbox mr10"><input type="checkbox" name="visaRange" value="">全部</label>
			                    	<#if visaRangeList?? && visaRangeList?size gt 0>
				                    	<#list visaRangeList as visaRange>
					            			<label class="checkbox mr10"><input type="checkbox" name="visaRange" value="${visaRange.dictId!''}"
					            				<#if vo??>
				            							<#if vo.visaRange == visaRange.dictId>checked</#if>
					            				</#if>
					            			>${visaRange.dictName!''}</label>
					            		</#list>
				                    </#if>
			                    </td>
			                </tr>
			            </tbody>
			        </table>
			        <div class="operate mt20" style="text-align:center">
			        	<a class="btn btn_cc1" id="search_button">查询</a>
			        	<a class="btn btn_cc1" id="clear_button">清空</a>
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
		<script type="text/javascript">var basePath = '${rc.contextPath}';</script>
		<form id="bookForm" action="" method="post">
			 
		</form>
		<script>
			var book_user_id='${user.userId}';
			
			$(function(){
				   /**
					 * 绑定签证国家地区ajax事件
					 */
					$("#visaCountryName").jsonSuggest({
						url:"/vst_back/visa/visaDoc/searchVisaCountry.do",
						maxResults: 20,
						minCharacters:1,
						onSelect:function(item){
							$("#visaCountry").val(item.text);
							//alert($("#districtId").val());
						}
					});
					 
					/**
					 *
					 */
					$("#productName").jsonSuggest({
						url:basePath+"/ord/order/visa/queryVisaProductList.do",
						maxResults: 20,
						minCharacters:1,
						onSelect:function(item){
							$("#product").val(item.id);
						}
					});
			});
			
			
			
			//清空查找条件
			function clearFindWhere(){
				 $("#searchForm")[0].reset();
			}
			
			/**
			 * 绑定查询按钮事件
			 */
			$("#search_button").bind("click",function(){
				
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
			
			function goodsDetailMouseover(obj){
					$(obj).attr("tip-content",$(obj).next("div").html());
				}
		</script>
	</body>
</html>
