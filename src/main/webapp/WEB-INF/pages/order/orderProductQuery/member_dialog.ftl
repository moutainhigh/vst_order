<div id="demo13H" style="display:none" >
    <div class="box_content p_line">
         <table class="e_table form-inline ">
            <tbody>
                <tr>
                    <td>
                        <input type="text" class="w160" placeholder="会员名称/手机/邮箱/会员卡号" name="searchUser"><div class="e_error searchUser" style="display:none"><i class="e_icon icon-error"></i><span>错误提示</span></div>
                        <div class="cc3">注：输入信息识别用户账号，无帐号则手机注册新用户</div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div>
                            <div style="float: left;padding-top:5px;">操作人员：</div>
                            <div style="float: left;width: 480px;">
                                <table class="e_table form-inline ">
                                <#if markChannels??>
                                    <#assign channel_index=0 />
                                    <#assign first_index=0 />
                                    <#list markChannels as markChannel>
                                    <#if first_index == 0 || channel_index == 3>
                                    <tr>
                                    </#if>
                                    <#if first_index == 0>
                                        <td><input name="channel_code" type="radio" value="no_o2o" checked="checked" />不属于门店</td>
                                        <#assign first_index=1 />
                                    </#if>
                                        <td><input name="channel_code" type="radio" value="${markChannel.channelCode}" />${markChannel.channelName}</td>
                                    <#if channel_index == 2>
                                    </tr>
                                    </#if>
                                    <#assign channel_index=channel_index+1 />
                                    <#if channel_index == 4>
                                        <#assign channel_index=0 />
                                    </#if>
                                    </#list>
                                    </tr>
                                <#else>
                                    <tr>
                                        <td><input name="channel_code" type="radio" value="no_o2o" checked="checked" />不属于门店</td>
                                    </tr>
                                </#if>
                                    <tr>
                                        <td colspan="4"><div class="cc3">注：该选项只需要新零售店员勾选，其他账号可以忽略该字段</div></td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div class="fl operate"><a class="btn btn_cc1" href="javascript:searchUser()" id="checkUserInfoButton">核实帐号</a><a class="btn" id="showMobileRegButton" href="javascript:showMobileReg()">手机注册</a></div>
                    </td>
                </tr>
            </tbody>
         </table>
    </div>
    <div class="box_content">
        <div class="iframe_content pd0">
            <div id="userListDiv">
            </div>
            <div style="display:none" id="mobileDiv"><form id="userMobileForm">
            <table class="e_table form-inline ">
                <tbody>
                    <tr>
                        <td>
                            <input type="text" class="" placeholder="输入11位的手机号码" name="userMobile"><div class="e_error userMobile" style="display:none"><i class="e_icon icon-error"></i><span>该手机号已被注册</span></div>
                        </td>
                    </tr>
                </tbody>
             </table>
            <div class="fl operate mt10"><a class="btn btn_cc1" href="javascript:regUserAccount()">确定</a></div>
            </form>
            </div>
        </div>
    </div>
</div>