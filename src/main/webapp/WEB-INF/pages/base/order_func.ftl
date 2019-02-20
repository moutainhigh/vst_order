<#macro showOrderDetail order>
<#if order.orderPackList?? && order.orderPackList?size gt 0>
/vst_order/order/orderStatusManage/showOrderStatusManage.do?orderId=${order.orderId}
<#else>
/vst_order/order/orderShipManage/showOrderStatusManage.do?orderId=${order.orderId}
</#if>
</#macro>