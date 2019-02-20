<#--页眉
<#import "/base/spring.ftl" as s/>-->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<#include "/base/head_meta.ftl"/>
		<title>发票列表</title>
		<script type="text/javascript" src="/vst_order/js/invoice/ord_invoice.js"></script>
		<style type="text/css">
			input.date{width:80px;}
			.jsonSuggest li a img {
				float:left;
				margin-right:5px;
			}
			.jsonSuggest li a small {
				display:block;
				text-align:right;
			}
			.jsonSuggest { font-size:0.8em; }
			.checks{margin-right:5px;}
			.item_box{width:520px; text-align:left; overflow:hidden; zoom:1;}
			.item_box span{float:left; vertical-align:middle;}
			.repeat_item{display:inline-block; text-align:center;}
			.repeat_item_hasSub{display:inline-block; width:220px; text-align:center; overflow:hidden; zoom:1;}
			.col_repeat  i{float:left; font-style:normal;}
			.width100{ width:70px; overflow:hidden;}
			.width120{ width:120px;overflow:hidden;}
		</style>
	</head>
	<body>
	<#import "/base/pagination.ftl" as pagination> 
			<div class="iframe_search">
			  <form action="/vst_order/order/orderInvoice/ord/invoiceList.do" method="post" id="searchForm" onsubmit="return beforeSubmit();">
				<table class="s_table">
					<tr>
						<td width="6%"  class="s_label">订单号：</td>
						<td width="12%"><input type="text" name="orderId" id="orderId"  value="${RequestParameters.orderId!''}"/></td>
						<td width="6%">发票单号:</td>
						<td width="12%"><input type="text" name="invoiceNo" id="invoiceNo" value="${RequestParameters.invoiceNo!''}"/></td>						
						<td width="6%">发票ID:</td>
						<td width="12%"><input type="text" name="ordInvoiceId" id="ordInvoiceId" name="ordInvoiceId" value="${RequestParameters.ordInvoiceId!''}"/></td>
					</tr>
					<tr>
					     <td width="6%" class="s_label">所属公司:</td>
					     <td width="12%">
						     <select name="filialeName">
						        <option value="0">请选择</option>
						           <#list filialeName as filialename>
						              <option value="${filialename.code}" <#if order!=null && order.filialeName == filialename.code>selected="selected"</#if>>${filialename.cnName!''}</option>
						          </#list>
						    </select>
						</td>
					     
					 <#--<td width="6%" class="s_label">订票人:</td>
						<td width="12%">
						   <@s.formHiddenInput "order.userId" ""/>
                    	   <input type="text" class="search" id="orderUserId" name="orderUserId" value="${order.userId!''}"/>             
						</td> -->
						<td width="6%">状态:</td>
						<td width="12%">
						<select name="status">
						 <option value="0">请选择</option>
						<#list invoiceStatus as invoicestatus>
						    <option value="${invoicestatus.code}" <#if ordInvoice!=null && ordInvoice.status == invoicestatus.code>selected="selected"</#if>>${invoicestatus.cnName!''}</option>
						</#list>
						</select>
						</td>
						<td width="6%">快递状态:</td>
						<td width="12%">
						<select name="deliverStatus">
						<option value="0">请选择</option>
						<#list logistics as logistics>
						  <option value="${logistics.code}" <#if ordInvoice!=null && ordInvoice.deliverStatus == logistics.code>selected="selected"</#if>>${logistics.cnName!''}</option>
						</#list>
						</select>
						</td>
					</tr>
					<tr>
						<td width="6%" class="s_label">申请时间:</td>
						<td width="12%">
						   <input type="text"  value="${RequestParameters.startTime!''}" id="startTime" name="startTime" errorEle="selectDate" class="Wdate" id="d4321" onFocus="WdatePicker({readOnly:true})" />
					       <input type="text"  value="${RequestParameters.endTime!''}" id="endTime" name="endTime" errorEle="selectDate" class="Wdate" id="d4322" onFocus="WdatePicker({readOnly:true})" />
						</td>
						<td width="6%">开票时间:</td>
						<td width="12%">
						    <input type="text"  value="${RequestParameters.billStartTime!''}" id="billStartTime" name="billStartTime" errorEle="selectDate" class="Wdate" id="d4321" onFocus="WdatePicker({readOnly:true})" />
					        <input type="text"  value="${RequestParameters.billEndTime!''}" id="billEndTime" name="billEndTime" errorEle="selectDate" class="Wdate" id="d4322" onFocus="WdatePicker({readOnly:true})" />
						</td>
						<td width="6%" >送货分类:</td>
						<td width="12%">
						   <select name="deliveryType">
						      <option value="0">请选择</option>
						          <#list deliveryType as deliveryTypes>
						               <option value="${deliveryTypes.code}" <#if ordInvoice!=null && ordInvoice.deliveryType == deliveryTypes.code>selected="selected"</#if>>${deliveryTypes.cnName!''}</option>
						          </#list>
						   </select>
						</td>						
					</tr>
				</table>
				<div class="operate mt20" style="text-align: right;margin:5px">
					<input type="submit" value="查 询" class="btn btn_cc1"/>
                </div>
			</form>
			<div class="iframe_content">
			<table class="p_table table_center" id="invoice_list">
			<tr>
				<th width="5%">序号</th>		
				<th width="10%">发票单号</th>
				<th width="10%">状态</th>
				<th width="10%">配送方式</th>
				<th width="9%">快递单号</th>
				<th width="12%">是否有地址</th>	
				<th width="10%">备注</th>
				<th colspan="6" class="col_repeat">
				   <div class="item_box">
					<span class="repeat_item width100">订单号</span>
					<span class="repeat_item width100 border_l">可开票金额</span>
					<span class="repeat_item_hasSub">
						<i class="repeat_item width100 border_l">保险金额</i>
						<i class="repeat_item width120 border_l">保险产品名称</i>
					</span>				
					<span class="repeat_item width100 border_l">申请时间</span>
					<span class="repeat_item width100 border_l">游玩时间</span>
				   </div>	
				</th>				
				<th width="8%">发票金额</th>						
				<th width="8%">操作</th>
			</tr>
		   <#if resultPage?? >
			<#if resultPage.items?size gt 0>
			  <tbody>
			 <#list resultPage.items as invoice>
			  <tr>
				<td>
				<input type="checkbox" class="checks" id="invoiceId" name="checkBoxInvoiceId" value="${invoice.ordInvoiceId!''}"/>${invoice.ordInvoiceId!''}</td>
				<td>${invoice.invoiceNo!''}</td>
				<td  status="${invoice.getZhStatus()!''}" id="status_${invoice.ordInvoiceId!''}">${invoice.getZhStatus()!''}</td>
				<td>${invoice.getZhDeliveryType()!''}</td>
				<td>${invoice.expressNo!''}</td>
				<td>
				<#if invoice.deliveryType != 'SELF'>是<#else>否</#if>
				</td>
				<td>${invoice.memo!''}</td>
				<td colspan="6" class="col_repeat">
				<#list invoice.orderList as order>
				    <div class="item_box">
						<span class="repeat_item width100">${order.orderId!''}</span> 
						<span class="repeat_item width100 border_l">
						    <em style="color: #f00; font-weight: bold"> 
								<#if order.oughtAmount=='UNPAY' && order.oughtAmount =='TOLVMAMA'>
									0
								</#if> 
								<#if test="order.oughtAmount =='TOSUPPLIER'">  
									0
								<#else>
									${order.getOrderInvoiceAmountYuan()!''} 
								</#if> 
						   </em>
						</span>
						<span class="repeat_item_hasSub">
					  	  <i class="repeat_item width100 border_l">
					  	       <#if order.getInsuranceAmountYuan() == '0.00'>
					  	       <#else>
		                       ${order.getInsuranceAmountYuan()!''}
		                       </#if>
			              </i>
			              <i class="repeat_item width120 border_l">
						  <#list order.orderItemList as orderItem>
						  	<#if "category_insurance"==orderItem.getContentStringByKey("categoryCode")>
						          ${orderItem.productName!''}&nbsp;
						    </#if> 
						  </#list>
						   </i>
						</span>
					    <span class="repeat_item width100 border_l">
					      ${invoice.createTime?string('yyyy-MM-dd')!''}
					   </span>
					   <span class="repeat_item width100 border_l">${order.visitTime?string('yyyy-MM-dd')!''}</span>
					</div>
				</#list>
			   </td>	
				<td><input type="hidden" name="amountYuan" id="amountYuan" value="${invoice.amountYuan!''}">  ${invoice.amountYuan!''} </td>		
				<td>
					<a href="javaScript:showInvoiceDetail(${invoice.ordInvoiceId!''})" id="showInvoiceDetail" title="发票详情">查看</a>
					<#if invoice.status=='UNBILL'||invoice.status=='APPROVE'>
						<a href="javascript:void(0);" class="cancel" result="${invoice.ordInvoiceId!''}" t="list">取消</a>
					</#if>
				</td>
			</tr>
          </#list>
         </tbody>
         <#else>
		     <div id="div" class="no_data mt20"><i class="icon-warn32"></i>暂无相关信息，请重新输入相关条件查询！</div>
	     </#if>
	     </#if>
		</table>
		
		<div class="operate mt20" style="text-align: right;margin:5px">
		  <table width="100%" cellspacing="5">
			<tr bgcolor="#ffffff">
				<td style="text-align: left">
				   <input type="checkbox" id="allsel" name="allsel" title="全选" style="margin-right:20px;"/>
				   <input type="button" value="审核通过" class="approve"/>
				   <!--input type="button" value="已开票" class="bill_btn" title="只有在不需要打印机直接打印发票的情况下才直接使用些操作"/-->
                   <#if resultPage?? >
                       <#if resultPage.items?size gt 0>
                           <#if resultPage.items[0].status=='APPROVE'>
                               <input type="button" value="开具发票" onclick="issueInvoice()" title="状态为“已经审核”的发票才能操作此按钮"/>
                           </#if>
                           <#if resultPage.items[0].status=='COMPLETE'>
                               <input type="button" value="重新打印" onclick="reprintInvoice('COMPLETE')" title="状态为“已完成”的发票才能操作此按钮"/>
                           </#if>
                           <#if resultPage.items[0].status=='BILLED'>
                               <input type="button" value="重新打印" onclick="reprintInvoice('BILLED')" title="状态为“已开票”的发票才能操作此按钮"/>
                           </#if>
                        </#if>
                   </#if>
				</td>
				<td colspan="2" align="right">
					<input type="button" value="导出报表" class="export" result="invoiceData"/>
					<input type="button" value="导出地址" class="export" result="invoiceAddress"/>				
				</td>
			</tr>
			</table>
		 </div>
			<@pagination.paging resultPage/> 
		</div>
		</div>
       <script type="text/javascript">
          //订票人查询
          $(function(){
             $("#orderUserId").jsonSuggest({
                url:"/vst_order/order/orderInvoice/ord/searchInvoice.do",
                maxResults: 20,
                minCharacters:1,
                onSelect:function(item){
                    $("#userId").val(item.text);
                }
             });
          });
          
        
          function getHeight(){
             $(".col_repeat span").each(function(){
                $(this).css("height",$(this).parents(".item_box").height());
             })
             $(".repeat_item_hasSub i").each(function(){
                $(this).css("height",$(this).parents(".item_box").height());
             })
           } 
           getHeight();
           $(window).resize(function(){
               getHeight();
           })
           
           function issueInvoice(){
               var table = $("#invoice_list");
               var checks = table.find("input[id='invoiceId']:checked");
               var ids = "";
               var j = 0;
               $.each(checks, function(i){
                   var check = checks.eq(i);
                   var id = check.val();
                   ids += id + ",";
                   j = 1;    
               });
               
               if (j != 1){
                   alert("选中个数为0");
                   return false;
               } else {
               var dataString = {"selectedInvoiceIds" : checks.serialize()};
                $.ajax({
                    url: "/vst_order/order/orderInvoice/ord/issueInvoice.do",
                    type: "POST",
                    data: dataString,
                    success: function (result) {
                        if(result.success == true)
                        window.location.href = "/vst_order/order/orderInvoice/ord/invoiceList.do?status=COMPLETE";
                        if(result.success == false)
                        window.alert(result.msg);
                    }
                 });
               }
           }
           
           function reprintInvoice(statusValue){
               var table = $("#invoice_list");
               var checks = table.find("input[id='invoiceId']:checked");
               var ids = "";
               var j = 0;
               $.each(checks, function(i){
                   var check = checks.eq(i);
                   var id = check.val();
                   ids += id + ",";
                   j = 1;    
               });

               if (j != 1){
                   alert("选中个数为0");
                   return false;
               } else {
               var dataString = {"selectedInvoiceIds" : checks.serialize()};
                $.ajax({
                    url: "/vst_order/order/orderInvoice/ord/reprintInvoice.do",
                    dataType:"json",
                    type: "POST",
                    data: dataString,
                    success: function (result) {
                        console.log(result);
                        if(result.success == true)
                            window.location.href = "/vst_order/order/orderInvoice/ord/invoiceList.do?status=" + statusValue;
                        if(result.success == false)
                            window.alert(result.msg);
                    }
                 });
               }
           }
           <!--去除两边空格-->
            function trim(str){  
                return str.replace(/\s+/g,"");
            } 
           //查询
            function beforeSubmit(){
            	var orderId=$("input[name=orderId]").val();
            	var orderIdReg=/^[0-9]*$/;	
 				if(orderId != '' && orderId != null){	
					if(!orderIdReg.test(this.trim(orderId))){
					   alert('输入的订单号必须为数字！');
					return false;
					}
				}
				var ordInvoiceId=$("input[name=ordInvoiceId]").val();
            	var ordInvoiceIdReg=/^[0-9]*$/;	
 				if(ordInvoiceId != '' && ordInvoiceId != null){	
					if(!ordInvoiceIdReg.test(this.trim(ordInvoiceId))){
					   alert('输入的发票ID必须为数字！');
					return false;
					}
				}
				
				return true;
			}
        </script>
	</body>
<#include "/base/foot.ftl"/>
</html>