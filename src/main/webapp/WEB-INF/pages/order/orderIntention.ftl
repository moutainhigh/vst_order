<div>
    <b style="font-size:14px;font-weight: bold;color:red;">*</b>是否从驴途意向单产生的订单：
    <#if intentionOrderId?exists >
      <select name="intentionOrderFlag" id="intentionOrderFlag" >
        <option selected="selected" value="Y">是</option>
    </select>
    <#else>
    <select name="intentionOrderFlag" id="intentionOrderFlag">
        <option value="NONE">请选择</option>
        <#if intentionOrderFlag?? && intentionOrderFlag=="Y">
        	<option value="Y" selected="selected">是</option>
        	<option value="N">否</option>
        <#else>
        	<option value="Y">是</option>
        	<option value="N" selected="selected">否</option>
        </#if>
        
    </select>
    </#if>
    <span id="iOrderFlag"></span>
</div>