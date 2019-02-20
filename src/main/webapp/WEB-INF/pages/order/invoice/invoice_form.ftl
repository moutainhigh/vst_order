<!DOCTYPE html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<head>
<title>订单处理后台_订单监控</title>
<#include "/base/head_meta.ftl"/>
<link href="/vst_order/js/tooltip/css/global.css" rel="stylesheet" type="text/css" />
<script type="text/javascript">
	       <!--去除两边空格-->
            function trim(str){  
                return str.replace(/\s+/g,"");
            } 
             //查询
            function beforeSubmit(){
				var bookerName=$("input[name=bookerName]").val();
				var bookerMobile=$("input[name=bookerMobile]").val();
				var contactMobile=$("input[name=contactMobile]").val();
				var mobile =/^1[0-9]{10}/;
				if(contactMobile != '' && contactMobile != null){
				   if(!mobile.test(this.trim(contactMobile))){
				       alert('请输入合法的手机号！');
				   return false;
				  }
				}
				if(bookerMobile != '' && bookerMobile != null){
				   if(!mobile.test(this.trim(bookerMobile))){
				       alert('请输入合法的手机号！');
				   return false;
				  }
				}
				var bookerEmail=$("input[name=bookerEmail]").val();
				var email = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*/;
				if(bookerEmail != '' && bookerEmail != null){
				   if(!email.test(this.trim(bookerEmail))){
				       alert('请输入合法的邮件！');
				   return false;
				  }
				}
				var contactName=$("input[name=contactName]").val();
				var orderId=$("input[name=orderId]").val();
				var orderIdReg=/^[0-9]*$/;		
				if(!orderIdReg.test(this.trim(orderId))){
				   alert('输入的订单编号必须为数字！');
				   return false;
				}
				if($.trim(bookerName)==''&&$.trim(bookerMobile)==''&&$.trim(bookerEmail)==''&&
						$.trim(contactName)==''&&$.trim(contactMobile)==''&&$.trim(orderId)==''){
					alert("查询条件不可以全为空");
					return false;
				}
				return true;
			}
   </script> 
   <script type="text/javascript">
            $(function(){
				$("#checkall").click(function(){
				    var $arr=$("input[name=checkall]:checked");
				    if($arr.size()!=0){
				        $("input[name=checkBoxName]:checkbox").attr("checked",true);
				    }else{
				        $("input[name=checkBoxName]:checkbox").attr("checked",false);
				    }
				});
				
				$("#addToWait").click(function(){
					var $arr=$("input[name=checkBoxName]:checked");
					if($arr.size()==0){
						alert("没有选中要操作的订单");
						return false;
					}
					var ids="";
					$.each($arr,function(i,n){
						if(i>0){
							ids+=",";
						}
						ids+=$(n).val();
					});
					if($.trim(ids)==''){
						alert("没有选中订单号");
						return false;
					}
					$.ajax({
	                  url : "/vst_order/order/orderInvoice/ord/invoiceAddReq.do?orderids="+ids,
	                  data : $("#dataForm").serialize(),
	                   type:"POST",
	                   dataType:"JSON",
	                   success : function(result){
	                       if(result.code=="success"){
	                           $("#waitToAddDiv").load("/vst_order/order/orderInvoice/ord/waitToAddList.do?orderIds="+ids);
	                           var tr_in_=$("tr[id^=tr_in_]").size();	
							   var totalYuan=$("#totalYuan").val();							
							   if(tr_in_>1 || totalYuan<=1){
								   $("#manyNumber").attr("disabled",true);
								   $("#invoiceNumber").attr("readonly",true);
							   }
	                       }
	                      if(result.code=="error"){
		     		           alert(result.attributes.error.message);
		     	          } 
	                    }
	                });
				});
		        $("a.delete").live("click",function(){
					var result=$(this).attr("result");
					if($.trim(result)==''){
						alert("订单不存在");
						return false;
					}
					
			   $.ajax({
		            url : "/vst_order/order/orderInvoice/ord/removeOrderInInvoice.do?orderId="+result,
		            type : 'post',
		            dataType : 'json',
		            success : function(data) {
		            	if(data.code=="success"){
		            	   $("#tr_in_"+result).remove();
						   $("#amountYuan").val(data.attributes.amountYuan);
						   $("#amountYuanSpan").html(data.attributes.amountYuan);
						   if(data.attributes.amountYuan == 0){
						      document.getElementById("myDiv").style.display="none";
						   }
		            	}
		           }
	          });
		  });
	})			
		</script>
</head>
		<body>
		<#import "/base/pagination.ftl" as pagination>
			<!--=========================主体内容==============================-->
				<div class="iframe_search">
					<form method="post" action="/vst_order/order/orderInvoice/ord/waitInvoceOrder.do" id="searchForm"  onsubmit="return beforeSubmit();">
							<table  class="s_table">
								<tbody>
								<tr>
									<td width="6%"  class="s_label">
										下单人姓名：
									</td>
									<td width="12%">
									    <input type=text name="bookerName" id="bookerName" value="${RequestParameters.bookerName!''}">
									</td>
									<td width="6%">
										下单人手机：
									</td>
									<td width="12%">
									    <input type=text name="bookerMobile" id="bookerMobile" value="${RequestParameters.bookerMobile!''}">
									</td>
									<td width="6%">
										电子邮件：
									</td>
									<td width="12%">
									    <input type=text name="bookerEmail" id="bookerEmail" value="${RequestParameters.bookerEmail!''}">
									</td>									
								</tr>
								<tr>
									<td width="6%" class="s_label">
										联系人姓名：
									</td>
									<td width="12%">
									    <input type=text name="contactName" id="contactName" value="${RequestParameters.contactName!''}">
									</td>
									<td width="6%">
										联系人手机：
									</td>
									<td width="12%">
									   <input type=text name="contactMobile" id="contactMobile" value="${RequestParameters.contactMobile!''}">
									</td>
									<td width="6%">
										订单编号：
									</td>
									<td width="12%">
									  <input type=text name="orderId" id="orderId" value="${RequestParameters.orderId!''}">
									</td>									
								</tr>
							</tbody>
						</table>
						<div class="operate mt20" style="text-align: right;margin:5px">
						    <input type="submit" value="查 询" class="btn btn_cc1"/>
                        </div>
					</form>
					
					 <div class="iframe_content">
						<table class="p_table table_center">
							<thead>
								<tr>
									<th><input type="checkbox" name="checkall" value="1" id="checkall" /></th>
									<th>订单号</th>	
									<th>订单类型</th>																	
									<th>联系人姓名</th>
									<th>联系人电话</th>									
									<th>下单人</th>
									<th>开票单位</th>
									<th>操作</th>
								</tr>
							</thead>
						  <#if resultPage?? >
							<#if resultPage.items?size gt 0 >
							<tbody>
								<#list resultPage.items as order>
									<tr>
										<td>
											<input type="checkbox" name="checkBoxName" value="${order.orderId!''}"></input>
										</td>
										<td>
											${order.orderId!''} 
										</td>	
										<td>
										    ${order.codeType!''}
										</td>
										<td>
											${order.contactPerson.fullName!''}
										</td>
										<td>
											${order.contactPerson.mobile!''}
										</td>
										<td>
										    ${order.bookerPerson.fullName!''}
										</td>
										<td>
											<#if order.companyType?exists>
												<#list companyTypeMap?keys as key >
													<#if order.companyType == key>${companyTypeMap[key]}
													</#if>												
												</#list>
											<#else>${companyTypeMap['XINGLV']}
											</#if>					
										</td>	
										<td>
											<a href="/vst_order/order/ordCommon/showOrderDetails.do?orderId=${order.orderId!''}" target="_blank">查看</a>
										</td>			
									</tr>
								</#list>
						  </tbody>
                         <#else>
		                      <div id="div" class="no_data mt20"><i class="icon-warn32"></i>暂无相关信息，请重新输入相关条件查询！</div>
	                    </#if>
	                    </#if>
						</table>
					<@pagination.paging resultPage/>
						</div>
						<div style="text-align: right;margin:5px" class="operate mt20" >
							<input type="button"   class="btn btn_cc1" value="添加到待开票" name="addToWait" id="addToWait"/>
						</div>
					</div>
					<!--=========================主体内容 end==============================-->
			<#include "/base/foot.ftl"/>
		<!--<div class="iframe_search" >
                                              兴旅：境外产品（自由行/门票/酒店/签证）；国内线路类产品；分社（除三亚外分社）国内/出境线路类产品。 <br/> 
                                             景域：国内自由行/单门票/单酒店；分社自由行/单门票/单酒店；实体票业务。 <br/> 
                                            国旅：三亚分社国内线路类产品。 <br/> 
		</div>-->
		<div id="waitToAddDiv" href="/vst_order/order/orderInvoice/ord/waitToAddList.do"></div>
	</body>
</html>
