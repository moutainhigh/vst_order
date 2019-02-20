<#if hasRelation??&&hasRelation >
<div class="hotel_ebk_wrap">
  <div class="hotel_tab_box">
   <div class="hotel_tab">
     <#if additionList?? && additionList?size &gt; 0>
   	 <#else>
     <div class="table_t">关联销售</div>
     </#if>
     <!--这里显示关联销售-->
	 <#include "/order/orderProductQuery/line/relationTicket.ftl"/>
	 <#include "/order/orderProductQuery/line/relationLine.ftl"/> 
	 <#include "/order/orderProductQuery/line/relationTransport.ftl"/>
     </div>
   </div>
</div>
</#if>