<p>
    <strong>历史申请记录</strong>
</p>
<p>
<table style="width:100%;" cellpadding="2" cellspacing="0" border="1" bordercolor="#000000">
    <tbody>
    <tr>
        <td style="text-align:center;">
            申请人
        </td>
        <td style="text-align:center;">
            申请日期
        </td>
        <td style="text-align:center;">
            转移至订单
        </td>
        <td style="text-align:center;">
            转移原因
        </td>
        <td style="text-align:center;">
            处理状态
        </td>
        <td style="text-align:center;">
            产品经理回复
        </td>
    </tr>
    <#list refundAuditList as audit>
    <tr>
        <td style="text-align:center;">
        ${audit.applicantName}
        </td>
        <td style="text-align:center;">
        ${audit.createTime?string("yyyy-MM-dd HH:mm:ss")}
        </td>
        <td style="text-align:center;">
        ${audit.transferOrderId}
        </td>
        <td style="text-align:center;">
        ${audit.applyInfo}
        </td>
        <td style="text-align:center;">
            <#if audit.aduitStatus == 'PROCESSING'>
                未处理
            <#elseif audit.aduitStatus == 'PASS'>
                通过
            <#elseif audit.aduitStatus == 'REJECT'>
                驳回
            <#else >
            ${audit.aduitStatus}
            </#if>
        </td>
        <td style="text-align:center;">
        ${audit.aduitReply}
        </td>
    </tr>
    </#list>
    </tbody>
</table>
</p>