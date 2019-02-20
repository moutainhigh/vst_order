
您好！

一、以下是线上 2017-06-01 以后产生的子订单未绑定结算对象CODE的情况,请您查阅！

<table style='border-collapse: collapse;border-spacing: 0;'>
    <tr>
        <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;'> 子订单未绑定结算对象CODE邮件 内容</td>
    </tr>
    <tr>
        <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;color:#1F497D;'> 子订单表ORD_ORDER_ITEM 中 共有${orderItemNoCodeCount?if_exists}条 子订单未绑定结算CODE，请及时处理！</td>
    </tr>
</table>

二、以下是 线上推送失败的订单消息信息,最多显示100条，更多数据请找DBA查看数据库！
<table style='border-collapse: collapse;border-spacing: 0;'>
    <tr>
        <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;'> 推送失败的订单消息信息 邮件内容</td>
    </tr>
    <tr>
        <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;color:#1F497D;'> 推送失败消息表 SEND_FAILED_MESSAGE_INFO 中 共有${orderItemNoCodeCount?if_exists}条 推送失败的消息，请及时处理！</td>
    </tr>
</table>
<table style='border-collapse: collapse;border-spacing: 0;'>
    <tr>
        <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;'>订单ID (非空)</td>
        <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;'>子订单ID (可为空)</td>
        <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;'>消息类型</td>
        <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;'>消息状态</td>
        <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;'>异常内容</td>
    </tr>
    <#list failedMessageInfoList as item>
        <tr>
            <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;color:#1F497D;'>${item.orderId?if_exists}</td>
            <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;color:#1F497D;'>${item.orderItemId?if_exists}</td>
            <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;color:#1F497D;'>${item.messageType?if_exists}</td>
            <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;color:#1F497D;'>${item.messageStatus?if_exists}</td>
            <td style='border:#000 solid 1px;line-height: 18px;padding: 3px 5px;color:#1F497D;'>${item.exceptionContent?if_exists}</td>
        </tr>
    </#list>
</table>

