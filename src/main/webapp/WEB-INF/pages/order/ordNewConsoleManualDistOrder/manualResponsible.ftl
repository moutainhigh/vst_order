<#import "/base/spring.ftl" as spring/>
<#import "/base/pagination.ftl" as pagination>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-人工分单</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>
	<#--页面导航-->
	 <div class="iframe_header">
	        <i class="icon-home ihome"></i>
	        <ul class="iframe_nav">
	            <li><a href="#">首页</a> ></li>
	            <li><a href="#">订单管理</a> ></li>
	            <li class="active">人工分单</li>
	        </ul>
	        <br>
	 </div>
 	<#--查询条件表单区域 start-->
	<div class="iframe_search">
		<form method="post" action='/vst_order/order/NewOrderConsole/query.do' id="searchForm">
			<table class="s_table  form-inline">
	            <tbody>
	                <tr>
		                <td class="w6 s_label">
		                	 订单编号：
		                </td>
		                <td>
		                	<@spring.formInput "monitorCnd.orderId" 'class="w9" number="true"'/>
		                </td>
		                 <td  class="w10 s_label">
		                    	订单负责人[ID]：
		                </td>
		                <td>
		                	<@spring.formInput "monitorCnd.operatorName" 'class="w9"'/>
		                </td>
		               <td class="w6 s_label">
			                 	一级部门：
			            </td>
		                <td>
				                <@spring.formSingleSelect   "ordAuditConfigInfo.firstDepartment"  firstDepMap  'class="w9"'/>
		                </td>
		                <td class="w6 s_label">
			                	二级部门：
			             </td>
		               	 <td>	 
		               	 	<@spring.formSingleSelect   "ordAuditConfigInfo.secondDepartment" secondDepMap 'class="w9"'/>
		                </td>
		                <td class="w6 s_label">
			                	选择组　：
						</td>
		               	 <td>	 
		               	 	<@spring.formSingleSelect "ordAuditConfigInfo.threeDepartment" threeDepMap 'class="w9"'/>
		                </td>
		                <#--<td class="w6 s_label">
		                	 选择组员：
						 </td>
		               	 <td>
		               	 	<@spring.formSingleSelect "ordAuditConfigInfo.groupMember" groupMemberMap 'class="w9"'/>
		                </td>-->
	                </tr>
	                 <tr>
	                 	 <td class="w6 s_label">
		                	 订单类型：
		                 </td>
		               	 <td>
		                	<@spring.formSingleSelect "monitorCnd.orderType" orderTypeMap 'class="w9"'/>
		                 </td>
		                 <td class="w6 s_label">
		                 	 下单时间：
		                 </td>
		                 <td>
		                 	<input id="d4321" class="Wdate" type="text" value="${monitorCnd.createTimeBegin}" 
	                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00'})" errorele="selectDate" name="createTimeBegin" readonly="readonly">
		                    	 -
		                    	 <input id="d4322" class="Wdate" type="text" value="${monitorCnd.createTimeEnd}" 
		                    	 onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{y:2});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
		                    	  errorele="selectDate" name="createTimeEnd">
		                   </label>
		                 </td>
		                 <td class="w6 s_label">
		                 	 游玩时间：
		                 </td>
		                 <td>
		                 		<@spring.formInput "monitorCnd.visitTimeBegin" 'class="w9" onClick="WdatePicker({readOnly:true})"'/>-
		                 		<@spring.formInput "monitorCnd.visitTimeEnd" 'class="w9" onClick="WdatePicker({readOnly:true})"'/>
		                 </td>
	                 </tr>
	                 <tr>
	                 	<td class="w6 s_label">
	                 		 订单状态：
	                 	</td>
		                 <td>
	             				<@spring.formSingleSelect "monitorCnd.orderStatus" orderStatusMap 'class="w9"'/>
	                 	</td>
	                 	<td class="w6 s_label">
	                 		 信息审核：
	             		 </td>
		                 <td>
	             				<@spring.formSingleSelect "monitorCnd.infoStatus" infoStatusMap 'class="w9"'/>
	                 	</td>
	                 	<td class="w6 s_label">
	                 		 资源审核：
	             		 </td>
		                 <td>
	             				<@spring.formSingleSelect "monitorCnd.resourceStatus" resourceStatusMap 'class="w9"'/>
	                 	</td>
	                 	<td class="w6 s_label">
	                 		 支付状态：
	             		 </td>
		                 <td>
	             				<@spring.formSingleSelect "monitorCnd.paymentStatus" paymentStatusMap 'class="w9"'/>
	                 	</td>
	                 	<td class="w10 s_label">
	                 		 凭证确认状态：
	             		 </td>
		                 <td>
	             				<@spring.formSingleSelect "monitorCnd.certConfirmStatus" certConfirmStatusMap 'class="w9"'/>
	                 	</td>
	                 </tr>
	                 <tr>
		                <td class="w6 s_label">产品编号：
		                </td>
		                <td>
	             			 <@spring.formInput "monitorCnd.productId" 'class="w9"'/>
	                 	</td>
		                <td class="w10 s_label">供应商名称：
		                </td>
		                <td>
	             			<@spring.formHiddenInput "monitorCnd.supplierId"/>
                        	<@spring.formInput "monitorCnd.supplierName" 'class="search"'/>
	                 	</td>
		            </tr>
		            <tr>
		                <td class="w8 s_label">联系人姓名：
		                </td>
		                <td>
	             			 <@spring.formInput "monitorCnd.contactName" 'class="w10"'/>
	                 	</td>
		                <td class="w8 s_label">联系人手机：
		                </td>
		                <td>
	             			<@spring.formInput "monitorCnd.contactMobile" 'class="w10"'/>
	                 	</td>
		            </tr>
			  </tbody>
	     </table>
	     <div class="operate mt20" style="text-align:center">
				<a class="btn btn_cc1" id="search_button">查询</a>
				<a class="btn btn_cc1" id="clear_button">清空</a>
				<a class="btn btn_cc1" id="batch_button" onclick="distOrderUser();">批量分单</a>
        </div>
		</form>
		<div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请至少输入一个查询条件！</div>
	</div>
	<#--查询条件表单区域end-->
	
	<div id="result" class="iframe_content mt20">
		<#--结果显示-->
		<#if resultPage?? && resultPage.items?? &&  resultPage.items?size &gt; 0>
				 <div class="p_box">
		   			 	<table class="p_table table_center">
		               		<thead>
		                		<tr>
		                			<th><input type="checkbox" name="allCk" value="" ></th>
		                            <th>订单来源</th>
		                            <th>产品类型</th>
		                            <th>主订单号</th>
		                            <th>子订单号</th>
		                            <th>产品编号</th>
		                            <th>产品名称</th>
		                            <th>供应商名称</th>
		                            <th>下单时间</th>
		                            <th>游玩日期</th>
		                            <th>联系人姓名</th>
		                            <th>联系人手机</th>
		                            <th>订单状态</th>
		                            <th>订单负责人</th>
		                            <th>组别</th>
		                        </tr>
		                     </thead>
		   					 <tbody>
		                       <#list resultPage.items as item>
		                        	<tr>
		                        		<td><input type="checkbox" name="ckbOrder"  value="${item.objectId}"></td>
		                                <td>${item.distributorName}</td>
		                                <td>${item.productType}</td>
		                                <td>${item.orderId}</td>
		                                <td>${item.orderItemId}</td>
		                                <td>${item.productId}</td>
		                                <td>${item.productName}</td>
		                                <td>${item.supplierName}</td>
		                                <td>${item.createTime}</td>
		                                <td>${item.visitTime}</td>
		                                <td>${item.contactName}</td>
		                                <td>${item.contactMobile}</td>
		                                <td>${item.currentStatus}</td>
		                                <td>${item.principal}</td>
		                                <td>${item.department}</td>
		                            </tr>
		                       </#list>
		                        </tbody>
		                    </table>
		                   <@pagination.paging resultPage/>
				</div>
			<#else>
			<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关结果，重新输入相关条件查询！</div>
		   </#if>
	</div>
<#--<form method="post" action='/vst_order/order/NewOrderConsole/manualDistOrder.do' id="dataForm">
<div class="iframe-content">   
    <div class="dialog1">
		<input type="hidden" name="auditIdStatus" value="${RequestParameters.auditIdStatus!''}">
	    <input type="hidden" name="oneData" value="${RequestParameters.oneData!''}">
	    <input type="hidden" name="manualDistOrder" value="false">
		 
    </div> 
</div>
</form>-->
<#--页脚-->
<#include "/base/foot.ftl"/>
<script src="/vst_order/js/vst_department_util.js"></script>
<script>
	var showSelectEmployeeDialog
	function distOrderUser(){
		var orderIds="";
		var checkLength=$("[input[name='ckbOrder'][checked]").length;
		if(checkLength<=0){
			$.alert("请选择要处理的订单");
			return false;
		}
		$("[input[name='ckbOrder'][checked]").each(function(){     
				orderIds+=$(this).val()+",";
		 })
		var objectType=$("select[name=orderType]").val();
		showSelectEmployeeDialog = 
				new xDialog("/vst_order/ord/NewOrderConsole/showSelectEmployee.do",
				{"orderIds":orderIds,"objectType":objectType},
				{title:"选择订单负责人",width:1000,height:600});
	}
	$(function(){
		//查询
		$("#search_button").bind("click",function(){
			 //表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			
			//遍历所有查询条件的值
			var value = "";
			
			//input
			var textFields = $("input[type=text]");
			$.each(textFields,function(){
			 	value += this.value;
			});
			
			//select
			var selectFields = $("select");
			$.each(selectFields,function(){
			 	value += this.value;
			});
			
			//客户必须输入一个条件
			if(value == ""){
				$("#errorMessage").show();
				return;
			}else{
				$("#errorMessage").hide();
			}
			$("#searchForm").submit();
		});
		//清空
		$("#clear_button").bind("click",function(){
			window.location.href = "/vst_order/order/NewOrderConsole/responsible.do";
		});
		
		$("#supplierName").jsonSuggest({
			url:"${rc.contextPath}/ord/order/querySupplierList.do",
			maxResults: 20,
			minCharacters:1,
			onSelect:function(item){
				$("#supplierId").val(item.id);
			}
		});
		
	});
	
	$(document).ready(function(){
				//设置checkbox选择,全选 
				$("input[type=checkbox][name=allCk]").click(function(){ 
					if(this.checked){
						$("input[name='ckbOrder']").attr("checked",true);
					  }else{
						  $("input[name='ckbOrder']").attr("checked",false);
					  }
				});
				
				//设置checkbox选择,单个元素选择 
				$("input[type=checkbox][name=ckbOrder]").click(function(){ 
					var ckLength=$("input[type=checkbox][name=ckbOrder]").length;
					var allLength=$("input[type=checkbox][name=ckbOrder]:checked").length;
					if(ckLength==allLength){
						$("input[name='allCk']").attr("checked",true);
					}else{
						  $("input[name='allCk']").attr("checked",false);
					  }
				});
			});
</script>
</body>
</html>