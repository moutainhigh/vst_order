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
				var categoryId=$("select[name=categoryId]").val();
				var subCategoryId=$("select[name=subCategoryId]").val();
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
				var contactName=$("input[name=contactName]").val();
				var orderId=$("input[name=orderId]").val();
				var status=$("select.applyInvoice").val();
				var orderIdReg=/^[0-9]*$/;		
				if(!orderIdReg.test(this.trim(orderId))){
				   alert('输入的订单编号必须为数字！');
				   return false;
				}
				
				var pattern = new RegExp("[~'!@#$%^&*()-+_=:]");   
			    if(bookerName!= "" && bookerName!= null){   
			        if(pattern.test(bookerName)){   
			            alert("下单人姓名含有非法字符！");   
			            return false;   
			        }   
			    }  
				
				if(contactName!= "" && contactName!= null){   
			        if(pattern.test(contactName)){   
			            alert("联系人姓名含有非法字符！");   
			            return false;   
			        }   
			    }  
				
				if($.trim(bookerName)==''&&$.trim(bookerMobile)==''&&$.trim(contactName)==''&&$.trim(contactMobile)==''&&$.trim(orderId)==''&&$.trim(status)==''
					&&$.trim(categoryId)==''&&$.trim(subCategoryId)==''){
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
				
				//发票申请状态下拉框回显
				var status = '${RequestParameters.orderStatus!''}';
				if(status!= "" && status!= null){
				       $("select.applyInvoice").find("option[value='"+status+"']").attr("selected","true"); 
				}
				
				//显示子品类
			    $("#categoryId").bind("change", function(){
			    	var $categoryId = $(this).val();
			    	if($categoryId==18){
			    		$("select[name=subCategoryId]").show();
			    		/* $("#brandDiv").hide();
			    		$("#brandId").val("");
			    		$("#brandName").val(""); */
			    	}else if($categoryId==1){
			    		
			    		$("select[name=subCategoryId]").hide();
			    		$("select[name=subCategoryId]").val("");
			    	}else{
			    		
			    		$("select[name=subCategoryId]").hide();
			    		$("select[name=subCategoryId]").val("");
			    	}
			    });
	      })			
   </script>
</head>
		<body>
		<#import "/base/pagination.ftl" as pagination>
			<!--=========================主体内容==============================-->
				<div class="iframe_search">
					<form method="post" action="/vst_order/order/orderInvoice/ord/prepareApplyInvoiceInfo.do" id="searchForm"  onsubmit="return beforeSubmit();">
							<table  class="s_table">
								<tbody>
								<tr>
									<td width="5%"  class="s_label">
										下单人姓名：
									</td>
									<td width="5%">
									    <input type=text name="bookerName"  maxlength='20'  id="bookerName" value="${RequestParameters.bookerName!''}">
									</td>
									<td width="5%">
										下单人手机：
									</td>
									<td width="5%">
									    <input type=text name="bookerMobile" id="bookerMobile" value="${RequestParameters.bookerMobile!''}">
									</td>
									<td width="5%">
										订单编号：
									</td>
									<td width="5%">
									    <input type=text name="orderId" id="orderId" value="${RequestParameters.orderId!''}">
									</td>
									<td width="4%">
										产品品类 ：
									</td>	
									<td width="14%">
									  <select class="form-control w90" id="categoryId" name="categoryId">
			                                <option value="" selected>不限</option>
						    				<#list bizCategoryList as bizCategory> 
							                    <option value=${bizCategory.categoryId!''} <#if RequestParameters.categoryId!=null && RequestParameters.categoryId == bizCategory.categoryId>selected</#if> >${bizCategory.categoryName!''}</option>
							                </#list>
			                          </select>
			                          <select class="form-control w90" name="subCategoryId" <#if RequestParameters.categoryId==null || RequestParameters.categoryId != 18>style = "display:none;"</#if>>
			                   	 			<option value="">不限</option>
						    				<#list subCategoryList as bizCategory> 
						                    	<option value=${bizCategory.categoryId!''} <#if RequestParameters.subCategoryId == bizCategory.categoryId>selected</#if> >${bizCategory.categoryName!''}</option>
							                </#list>
							        	</select>
									</td>									
								</tr>
								<tr>
									<td width="5%" class="s_label">
										联系人姓名：
									</td>
									<td width="5%">
									    <input type=text name="contactName" maxlength='20'  id="contactName" value="${RequestParameters.contactName!''}">
									</td>
									<td width="5%">  
										联系人手机：
									</td>
									<td width="5%">
									   <input type=text name="contactMobile" id="contactMobile" value="${RequestParameters.contactMobile!''}">
									</td>
									<td width="5%">
										申请状态：
									</td>
									<td width="5%">
									  <select name="orderStatus" class="applyInvoice">
									  	<option value="">请选择</option>
										<option value="PENDING">待申请</option>
										<option value="MANUAL">人工申请</option>
										<option value="INVALID">无效</option>
										<option value="FAILURE">申请失败</option>
									  </select>
									</td>	
									<td width="5%">
                                                                                                            购买方式：
                                    </td>
                                    <td width="5%">
                                      <select name="purchaseWay" class="applyInvoice">
                                        <option value="">请选择</option>
                                        <option value="personal">个人</option>
                                        <option value="company">公司</option>
                                      </select>
                                    </td>
									<td width="8%">
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
									<th>订单号</th>																	
									<th>联系人姓名</th>
									<th>联系人电话</th>									
									<th>下单人</th>
									<th>购买方式</th>
									<th>发票抬头</th>
									<th>纳税人识别号</th>
									<th>申请失败原因</th>
									<th>处理状态</th>
									<th>申请时间</th>
									<th>操作</th>
								</tr>
							</thead>
						  <#if resultPage?? >
							<#if resultPage.items?size gt 0 >
							<tbody>
								<#list resultPage.items as order>
									<tr>
										<td>
											${order.orderId!''} 
										</td>	
										
										<td>
											${order.contactName!''}
										</td>
										<td>
											${order.contactMobile!''}
										</td>
										<td>
										    ${order.bookerName!''}
										</td>
										<td>
                                             <#if order.purchaseWay?exists>
                                              <#if order.purchaseWay == "personal">
                                                个人
                                              <#elseif order.purchaseWay == "company" >                                                     
                                                公司
                                              <#else>  
                                                未知
                                              </#if>
                                             </#if>
                                        </td>
										<td>
                                             ${order.title!''}              
                                        </td>
										<td>
                                             ${order.taxNumber!''}                
                                        </td>
										<td>
											 ${order.errormsg!''}				
										</td>
										<td>
										  <#if order.status?exists>
											  <#if order.status == "PENDING">
	                                                                                                                                              待申请
	                                          <#elseif order.status == "MANUAL" >                                                     
	                                                                                                                                              人工申请
	                                           <#elseif order.status == "INVALID" >                                                     
	                                                                                                                                              无效
	                                          <#elseif order.status == "FAILURE" >                                                     
	                                                                                                                                              申请失败       
	                                          <#elseif order.status == "APPLIED" > 
	                                       	   已申请       
	                                       	   <#elseif order.status == "CANCEL" > 
	                                       	   已取消                                                                                                                                                                                                              
	                                          <#else>  
	                                          	未知                                                                           
	                                         </#if> 
	                                      </#if> 				
										</td>
										<td>
											${order.updateTime?string("yyyy-MM-dd HH:mm:ss")}			
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
					</div>
					<!--=========================主体内容 end==============================-->
			<#include "/base/foot.ftl"/>
	
	</body>
</html>
