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
            可退金额
        </td>
        <td style="text-align:center;">
            备注
        </td>
        <td style="text-align:center;">
            处理状态
        </td>
        <td style="text-align:center;">
            处理回复
        </td>
        <td style="text-align:center;">
            是否已退款
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
            <#if audit.aduitStatus = 'PASS' || audit.refundFlag == 'Y'>
            ${audit.retreatAmount/100} 元
            <#else>
                0 元
            </#if>
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
        <td style="text-align:center;">
            <#if audit.refundFlag = 'N'>
                否
            <#else>
                是
            </#if>
        </td>
    </tr>
    </#list>
    </tbody>
</table>
</p>