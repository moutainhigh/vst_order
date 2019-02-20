<!DOCTYPE html>
<head>
<#include "/base/head_meta.ftl"/>
<link href="/vst_order/js/tooltip/css/global.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="/vst_order/js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="/vst_order/js/remoteUrlLoad.js"></script>
<script language="javascript" src="/vst_order/js/ord.js" type="text/javascript"></script>
</head>
         <div class="iframe_content">
           <form method="post" name="myform" id="formId" class="formc" action="/vst_order/order/orderInvoice/ord/goAddInvoiceInfo.do" onsubmit="return check();">
             <p class="tr cc3">添加的订单号列表</p>
			<table class="p_table table_center">
				<thead>
				<tr>
					<th>订单号</th><th>联系人</th><th>联系人电话</th><th>订单人</th><th>发票内容</th><th>开票单位</th><th>操作</th>
				</tr>
				</thead>
				<tbody>
				<#list orderList as ordOrder>
				   <tr id="tr_in_${ordOrder.orderId!''}">
					    <td>${ordOrder.orderId!''}</td>
						<td>${ordOrder.contactPerson.fullName!''}</td>
						<td>${ordOrder.contactPerson.mobile!''}</td>	
						<td>${ordOrder.bookerPerson.fullName!''}</td>	
					<td>
					     <select id="selectId" name="selectId">
					     <#if invoiceContentMap??>
					     <#if invoiceContentMap["inv_"+ordOrder.orderId]??>
					     <#list invoiceContentMap["inv_"+ordOrder.orderId] as code>
                             <option  value="${code!''}"> ${code!''} </option>
                         </#list>
					     </#if>
					     
					     </#if>
					     
                         </select>
					</td>
						<td>
							<#if ordOrder.companyType?exists>
								<#list companyTypeMap?keys as key >
									<#if ordOrder.companyType == key>
									    ${companyTypeMap[key]}
                                    </#if>												
								</#list>
							<#else>
							     ${companyTypeMap['XINGLV']}
							</#if>
						</td>
					<td><a href="#delete" result="${ordOrder.orderId!''}" class="delete" id="deleteOrder">删除</a></td>
				</tr>
				</#list>
				<tr>
					<td colspan="6" style="text-align: right;margin:5px">
					总金额：<input type="hidden" id="amountYuan" name="amountYuan" value="${amountYuan!''}"/><span id="amountYuanSpan">${amountYuan!''}<span></td>	
				</tr>
				<tr>
					<td colspan="6">
					开票数量：<input type="radio" id="anumber" name='number' checked value="1">1张发票
					  <input type="radio" id="manyNumber" name='number'/>
					       开具<input type="text" size='3' id="invoiceNumber" name="invoiceNumber" readonly="true"/>张发票					
					</td>
				</tr>
			  </tbody>
			</table>
			<input type="hidden" name="orderIds"  id="orderIds" value="${orderIds!''}"/> 
			<div class="operate mt20"  style="text-align:center" id="myDiv">
				<input type="submit" value="下一步" class="btn btn_cc1"/>
            </div>
     </form>
</div>

<script type="text/javascript">
  $(function(){
	  $(":radio[name='number']").click(function(){	 
	      if($(":radio[name='number']:checked").val()==1){
	    	  $("#invoiceNumber").val("");
	    	  $("#invoiceNumber").attr("readonly",true);
	      }else{
	    	  $("#invoiceNumber").attr("readonly",false);
	      }
	  });
  });
  
 
   function check(){
       var num = ${InvoiceNum};
	   var amountYuan=$("#amountYuan").val();
	   if(amountYuan<1){
		   alert("总金额小于1元不能开发票");
		   return false;
	   }
	   
	   if($("#manyNumber").attr("checked")){
		   if($("tr[id^=tr_in_]").length>1){
			   alert("多订单只可申请合并开票");
			   return false;
		   }
		   if($("tr[id^=tr_in_]").length==0){
		      alert("没有订单不能申请开发票！");
		      return false;
		   }
		   var numInv=parseInt($("#invoiceNumber").val());
		   if(numInv==NaN||numInv<1){
			   alert("开票张数必须大于1");
			   return false;
		   }
		  if(num<numInv){
			   alert("根据游玩人数开票张数不能大于" + num);
			   return false;
		   }
	   }
	   return true;
   }
</script>				
