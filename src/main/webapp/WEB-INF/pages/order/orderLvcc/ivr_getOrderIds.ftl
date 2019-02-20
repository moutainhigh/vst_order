<?xml version="1.0" encoding="GB2312"?>
<vxml version="1.0">
    <var name="orderTotalCount" expr="${ivrOrderTotalCount }" />
    <var name="orderIds" expr="'${ivrOrders }'" />
	<form>	
		<block>
			<return namelist="orderTotalCount orderIds"/>
		</block>
	</form>
</vxml>