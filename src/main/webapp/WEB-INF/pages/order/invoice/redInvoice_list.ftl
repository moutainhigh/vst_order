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
			.item_box{width:520px; text-align:left; overflow:hidden; zoom:1; }
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
			<form action="/vst_order/order/orderInvoice/ord/redInvoiceList.do" method="post" id="searchForm">
				<table class="s_table">
					<tr>
						<td width="6%" class="s_label">订单号：</td>
						<td width="12%"><input type="text" name="orderId" id="orderId" value="${RequestParameters.orderId!''}"/></td>
						<td width="6%">发票单号:</td>
						<td width="12%"><input type="text" name="invoiceNo" id="invoiceNo" value="${RequestParameters.invoiceNo!''}"/></td>						
						<td width="6%">发票ID:</td>
						<td width="12%"><input type="text" name="ordInvoiceId" id="ordInvoiceId" name="ordInvoiceId" value="${RequestParameters.ordInvoiceId!''}"/></td>
					</tr>					
				</table>
				  <div class="operate mt20" style="text-align: right;margin:5px">
					   <input type="submit" value="查询 " class="btn btn_cc1"/>
                  </div>
			</form>
		 <div class="iframe_content">
			<table class="p_table table_center">
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
				<input type="checkbox" class="checks" id="checkBoxInvoiceId" name="checkBoxInvoiceId" value="${invoice.ordInvoiceId!''}"/>${invoice.ordInvoiceId!''}</td>
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
					<#if invoice.status=='APPROVE'>
						<a href="javascript:void(0);" result="${invoice.ordInvoiceId!''}" class="cancel">取消</a>
						<a href="javascript:void(0);" result="${invoice.ordInvoiceId!''}" class="closeRed" >关闭</a>
					</#if>			
					<#if invoice.status=='BILLED'>
					    <a href="javascript:void(0);" result="${invoice.ordInvoiceId!''}" class="confirmRed">红冲</a>
					    <a href="javascript:void(0);" result="${invoice.ordInvoiceId!''}" class="closeRed" >关闭</a>
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
			<@pagination.paging resultPage/>
		</div>	
		</div>	
	   <script type="text/javascript">
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
	    </script>
	</body>
	<#include "/base/foot.ftl"/>
</html>