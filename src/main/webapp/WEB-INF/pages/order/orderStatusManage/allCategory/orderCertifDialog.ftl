<div>
	<p>
		<#if cancelCertConfirmStatus=='CREATE'>
            EBK取消凭证未确认，无法发送新的凭证
		<#elseif cancelCertConfirmStatus=='ACCEPT'>
            EBK取消凭证已确认，无法发送新的凭证
		</#if>
	</p>
</div>