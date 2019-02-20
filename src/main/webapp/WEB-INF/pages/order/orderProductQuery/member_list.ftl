<#if userList??>
<form id="userSelectForm">
<table class="pg_d_table table_center">
    <thead>
        <tr>
            <th style="width:30px;">操作</th>
            <th>会员名称</th>
            <th>手机号码</th>
            <th>邮箱</th>
            <th>会员卡号</th>
            <th>是否VIP会员</th>
            <th>会员状态</th>
        </tr>
    </thead>
    <tbody>
    	<#list userList as user>
        <tr>
            <#if user.userStatus?? && (user.userStatus == 'BLOCK' || user.userStatus == 'DISABLED')>
                <td></td>
            <#else>
                <td><input type="radio" name="user_id" userName="${user.userName}" value="${user.userId}"/></td>
            </#if>
            <td>${user.userName}</td>
            <td>${user.mobileNumber}</td>
            <td>${user.email}</td>
            <td>${user.memberShipCard}</td>
            <td>
            	<#if isVipMap['${user.id}']==true>是
            	<#else>否
            	</#if>
            </td>
            <td>
                <#if user.userStatus?? && user.userStatus == 'NORMAL'>正常
                <#elseif user.userStatus?? && user.userStatus == 'DISABLED'>注销
                <#elseif user.userStatus?? && user.userStatus == 'BLOCK'>冻结
                </#if>
            </td>
        </tr>
        </#list>
    </tbody>
</table>
<div class="fl operate mt10"><a class="btn btn_cc1" href="javascript:selectUser()">确定</a></div>
</form>
<#else>
没有搜索到相关的会员信息
</#if>