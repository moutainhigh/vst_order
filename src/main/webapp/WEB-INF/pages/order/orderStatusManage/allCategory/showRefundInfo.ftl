<!DOCTYPE html>
<html>
	<head>
		<#include "/base/head_meta.ftl"/>
	</head>
	<body>
		<div class="p_box">
			<table class="p_table table_center">
			    <thead>
			    	<tr>
				        <th>操作账号</th>
				        <th>申请时间</th>
				        <th>申请份数</th>
				        <th>退款状态</th>
				        <th>日志</th>
			        </tr>
			    </thead>
			    <tbody>
			    
			         <#if orderRefundBatchList?? && orderRefundBatchList?size&gt;0>
			         <#assign index = 0>
                                     <#list orderRefundBatchList as orderRefundBatch > 
                               <tr>
                                <td>${orderRefundBatch.operator!''}</td>
                                <td>${orderRefundBatch.createTime?string("yyyy-MM-dd HH:mm:ss")}</td>
                                <td>${orderRefundBatch.refundQuantity!''}</td>
                                <td>${orderRefundBatch.itemAuditStatusStr!''}</td>
                                <td><input id="batchId${index}" type="hidden" value="${orderRefundBatch.id}"/>
                                 <input id="ordItemId${index}" type="hidden" value="${orderRefundBatch.orderItemId}"/>
                                 <a  id="queryRefundDetailInfo${index}" href="javascript:void(0)">查看</a></td>
                                </tr>   
                                <#assign index = index+1>
                                </#list>
                          </#if>
						    
				</tbody>
			</table>
		</div>
	<#include "/base/foot.ftl"/>
	</body>
</html>
<script type="text/javascript">
    //子订单退款明细
      $("a[id^=queryRefundDetailInfo]").bind("click",function(){
            var batchId=$.trim($(this).parent().children().eq(0).val());
            var ordItemId=$.trim($(this).parent().children().eq(1).val());
            var param={"batchId":batchId,"ordItemId":ordItemId};
            new xDialog("/vst_order/order/orderManage/queryRefundDetailInfo.do",param,{title:"退款日志",width:650});
      });
     
</script>