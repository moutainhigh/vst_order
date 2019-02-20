<!DOCTYPE html>
<html>
<head>
<#include "/base/head_meta.ftl"/>

</head>
<body>
<form id="dataForm" autocomplete="off">

    <input name="id" type="hidden" value="${invoiceInfo.id}">
    <input name="orderId" type="hidden" value="${invoiceInfo.orderId}">
    <table class="p_table form-inline">
        <tbody>
        <tr>
            <td class="p_label" width="100px;">发票类型：</td>
            <td>
                <#if invoiceInfo.elecInvoice == 0>
                    纸质发票
                <#else>
                    电子发票
                </#if>
            </td>
        </tr>

        <tr>
            <td class="p_label" width="100px;"><i class="cc1">*</i>开票内容：</td>
            <td>
                <div>
                    <select id="content" name="content" required>
                        <#list contents as content>
                            <option value="${content}" <#if invoiceInfo.content == content>selected</#if>>${content}</option>
                        </#list>
                    </select>
                </div>
            </td>
        </tr>

        <tr>
            <td class="p_label" width="100px;"><i class="cc1">*</i>发票抬头：</td>
            <td>
                <#if invoiceInfo.purchaseWay == 'personal'>
                    <input type="radio" name="purchaseWay" value="personal" checked="checked">个人&nbsp;&nbsp;
                    <input type="radio" name="purchaseWay" value="company">公司
                <#elseif invoiceInfo.purchaseWay == 'company'>
                    <input type="radio" name="purchaseWay" value="personal" >个人&nbsp;&nbsp;
                    <input type="radio" name="purchaseWay" value="company" checked="checked">公司
                </#if>
            </td>
        </tr>

        <tr>
            <td class="p_label" width="100px;"><i class="cc1">*</i>抬头内容：</td>
            <td>
                <input class="w110" type="text" name="title" id="title" value="${invoiceInfo.title}">
            </td>
        </tr>

        <tr class="company_content">
            <td class="p_label" width="100px;"><i class="cc1">*</i>税号：</td>
            <td>
                <input class="w110" type="text" name="taxNumber" id="taxNumber" value="${invoiceInfo.taxNumber}">
            </td>
        </tr>

        <tr class="company_content">
            <td class="p_label" width="100px;">注册地址：</td>
            <td>
                <input class="w110" type="text" name="buyerAddress" id="buyerAddress" value="${invoiceInfo.buyerAddress}" maxlength="150">
            </td>
        </tr>

        <tr class="company_content">
            <td class="p_label" width="100px;">注册电话：</td>
            <td>
                <input class="w110" type="text" name="buyerTelephone" id="buyerTelephone" value="${invoiceInfo.buyerTelephone}" maxlength="150">
            </td>
        </tr>

        <tr class="company_content">
            <td class="p_label" width="100px;">开户银行：</td>
            <td>
                <input class="w110" type="text" name="bankAccount" id="bankAccount" value="${invoiceInfo.buyerTelephone}" maxlength="150">
            </td>
        </tr>

        <tr class="company_content">
            <td class="p_label" width="100px;">银行帐号：</td>
            <td>
                <input class="w110" type="text" name="accountBankAccount" id="accountBankAccount" value="${invoiceInfo.accountBankAccount}" maxlength="150">
            </td>
        </tr>

        <#if invoiceInfo.elecInvoice == 0>
            <tr>
                <td class="p_label" width="100px;"><i class="cc1">*</i>邮寄地址：</td>
                <td>
                    <div class="address_add_box" style="display: block;">
                        <!-- 送货方式 开始 -->
                        <dl class="orderDl">
                            <dt><i class="cc1">*</i>收件人：</dt>
                            <dd>
                                <input id="fullName" class="input address_yz" name="fullName" maxlength="20" type="text" placeholder="请输入联系人姓名" value="${invoiceInfo.fullName}">
                                <span class="error_text" style="display: none;"></span>
                            </dd>
                        </dl>
                        <dl class="orderDl">
                            <dt><i class="cc1">*</i>联系手机：</dt>
                            <dd>
                                <input id="mobile" class="input address_yz js_textBig" name="mobile" maxlength="11" type="text" placeholder="请输入联系方式" value="${invoiceInfo.mobile}">
                                <span class="error_text" style="display: none;"></span>
                            </dd>
                        </dl>
                        <dl class="orderDl">
                            <dt><i class="cc1">*</i>所在地区：</dt>
                            <dd class="clearfix">
                                <input name="province" type="hidden" value="${invoiceInfo.province}">
                                <input name="city" type="hidden" value="${invoiceInfo.city}">
                                <select id="province" class="select orderDl-addressSelect address_yz" name="provinceSelect">
                                    <option value="请选择省份">请选择省份</option>
                                    <#list provinceList as province>
                                        <option value="${province.districtId}" <#if invoiceInfo.province == province.districtName>selected</#if>>${province.districtName}</option>
                                    </#list>
                                </select>
                                <select id="city" class="select orderDl-addressSelect address_yz" name="citySelect"><option value="请选择城市">请选择城市</option></select>
                            </dd>
                        </dl>
                        <dl class="orderDl">
                            <dt><i class="cc1">*</i>详细地址：</dt>
                            <dd class="mt15">
                                <textarea rows="3" id="street" class="textarea address_yz" name="street" placeholder="请输入详细收件地址" value="" maxlength="200">${invoiceInfo.street}</textarea>
                                <span class="error_text" style="display: none;"></span>
                            </dd>
                        </dl>
                        <dl class="orderDl">
                            <dt>邮政编码：</dt>
                            <dd>
                                <input id="postcode" class="input address_yz" name="postcode" maxlength="6" type="text" placeholder="请输入邮政编码" value="${invoiceInfo.postcode}">
                                <span class="error_text" style="display: none;"></span>
                            </dd>
                        </dl>

                    </div>
                </td>
            </tr>
        <#elseif invoiceInfo.elecInvoice == 1 && invoiceInfo.deliveryType == 'EXPRESS'>
            <tr>
                <td class="p_label" width="100px;"><i class="cc1">*</i>邮寄地址：</td>
                <td>
                    <div class="address_add_box" style="display: block;">
                        <!-- 送货方式 开始 -->
                        <dl class="orderDl">
                            <dt><i class="cc1">*</i>收件人：</dt>
                            <dd>
                                <input id="fullName" class="input address_yz" name="fullName" maxlength="20" type="text" placeholder="请输入联系人姓名" value="${invoiceInfo.fullName}">
                                <span class="error_text" style="display: none;"></span>
                            </dd>
                        </dl>
                        <dl class="orderDl">
                            <dt><i class="cc1">*</i>联系手机：</dt>
                            <dd>
                                <input id="mobile" class="input address_yz js_textBig" name="mobile" maxlength="11" type="text" placeholder="请输入联系方式" value="${invoiceInfo.mobile}">
                                <span class="error_text" style="display: none;"></span>
                            </dd>
                        </dl>
                        <dl class="orderDl">
                            <dt><i class="cc1">*</i>所在地区：</dt>
                            <dd class="clearfix">
                                <input name="province" type="hidden" value="${invoiceInfo.province}">
                                <input name="city" type="hidden" value="${invoiceInfo.city}">
                                <select id="province" class="select orderDl-addressSelect address_yz" name="provinceSelect">
                                    <option value="请选择省份">请选择省份</option>
                                    <#list provinceList as province>
                                        <option value="${province.districtId}" <#if invoiceInfo.province == province.districtName>selected</#if>>${province.districtName}</option>
                                    </#list>
                                </select>
                                <select id="city" class="select orderDl-addressSelect address_yz" name="citySelect">
                                    <option value="请选择城市">请选择城市</option>
                                </select>
                            </dd>
                        </dl>
                        <dl class="orderDl">
                            <dt><i class="cc1">*</i>详细地址：</dt>
                            <dd class="mt15">
                                <textarea rows="3" id="street" class="textarea address_yz" name="street" placeholder="请输入详细收件地址" value="" maxlength="200">${invoiceInfo.street}</textarea>
                                <span class="error_text" style="display: none;"></span>
                            </dd>
                        </dl>
                        <dl class="orderDl">
                            <dt>邮政编码：</dt>
                            <dd>
                                <input id="postcode" class="input address_yz" name="postcode" maxlength="6" type="text" placeholder="请输入邮政编码" value="${invoiceInfo.postcode}">
                                <span class="error_text" style="display: none;"></span>
                            </dd>
                        </dl>

                    </div>
                </td>
            </tr>

            <tr>
                <td class="p_label" width="100px;"><i class="cc1">*</i>接受人邮箱：</td>
                <td>
                    <input type="text" name="receiverEmail" id="receiverEmail" value="${invoiceInfo.receiverEmail}"/>
                </td>
            </tr>

        <#else>
            <tr>
                <td class="p_label" width="100px;"><i class="cc1">*</i>接受人邮箱：</td>
                <td>
                    <input type="text" name="receiverEmail" id="receiverEmail" value="${invoiceInfo.receiverEmail}"/>
                </td>
            </tr>
        </#if>

        </tbody>
    </table>

    <div class="fl operate" style="margin:20px;width: 700px;" align="center">
        <a class="btn btn_cc1" id="saveOrUpdate">提交</a>
        <a class="btn btn_cc1" id="cancel">取消</a>
    </div>

</form>

</body>

</html>
<script type="text/javascript" src="/vst_order/js/pandora-dialog.js"></script>
<script>

    /*
    * 初始化方法
    * */
    function init(){

        //根据开票类型显示
        var purchaseWay = '${invoiceInfo.purchaseWay}';
        if (purchaseWay && purchaseWay == 'personal') {
            $('.company_content').hide();
        } else {
            $('.company_content').show();
        }

        //加载城市
        //寄送方式
        var deliveryType = '${invoiceInfo.deliveryType}';
        //发票类型，0纸质，1电子
        var elecInvoice = '${invoiceInfo.elecInvoice}';
        if (elecInvoice == 0 || (elecInvoice == 1 && deliveryType =='EXPRESS')) {
            initCity();
        }

    }

    function initCity(){
        //根据选中的地址省份加载城市信息
        var parentId = $('#province').val();
        $.ajax({
            url : '/vst_order/ord/book/ajax/expressDic.do',
            type : "post",
            data : {'parentId' : parentId},
            success : function(result) {
                if(result.code=='success') {
                    var citys = result.attributes.provienceList;
                    var cityName = '${invoiceInfo.city}';
                    //清空city下拉选
                    $('#city').html('');
                    if (citys && citys.length > 0) {
                        $.each(citys,function(index,value){
                            if (cityName == value.districtName) {
                                var option = '<option value='+ value.districtId +' selected>'+ value.districtName +'</option>';
                                $('input[name=city]').val(value.districtName);
                            }else {
                                if (index == 0) {
                                    var option = '<option value='+ value.districtId +' selected>'+ value.districtName +'</option>';
                                    $('input[name=city]').val(value.districtName);
                                } else {
                                    var option = '<option value='+ value.districtId +' >'+ value.districtName +'</option>';
                                }
                            }
                            $('#city').append(option);
                        });
                    }

                } else {
                    $.alert(result.message);
                }
            }
        });
    }

    //提交验证
    function validate() {
        //开票类型
        var purchaseWay = '${invoiceInfo.purchaseWay}';
        //寄送方式
        var deliveryType = '${invoiceInfo.deliveryType}';
        //发票类型，0纸质，1电子
        var elecInvoice = '${invoiceInfo.elecInvoice}';



        var content = $('#content').val();
        var title = $('input[name=title]').val();
        var taxNumber = $('input[name=taxNumber]').val();
        var buyerTelephone = $("#buyerTelephone").val();
        var fullName = $('input[name=fullName]').val();
        var mobile = $('input[name=mobile]').val();
        var province = $('input[name=province]').val();
        var city = $('input[name=city]').val();
        var street = $('#street').val();
        var receiverEmail = $('#receiverEmail').val();

        var regex = /^\d{15}|\d{18}|\d{20}$/;
        var emailReg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,8})$/;

        if (content == null || content == '') {
            alert('请选择开票内容');
            return false;
        }
        if (title == null || title == '') {
            alert('请输入抬头内容');
            return false;
        }

        if (purchaseWay == 'company') {

            if (taxNumber == null || taxNumber == '') {
                alert('请输入纳税人识别号');
                return false;
            }

            if (!regex.test(taxNumber)) {
                alert('请输入正确的纳税人识别号');
                return false;
            }

            if (buyerTelephone != null && buyerTelephone != '') {
                if (!/^[0-9]*$/.test(taxNumber)) {
                    alert('请输入正确的注册电话');
                    return false;
                }
            }

        }

        if (elecInvoice == 0 || (elecInvoice == 1 && deliveryType =='EXPRESS')) {
            if (fullName == null || fullName == '') {
                alert('请输入收件人姓名');
                return false;
            }
            if (mobile == null || mobile == '') {
                alert('请输入联系人手机号');
                return false;
            }
            if (!/^1[0-9]{10}$/.test(mobile)) {
                alert('请输入正确的联系人11位手机号');
                return false;
            }
            if (province == null || province == '') {
                alert('请选择省份');
                return false;
            }
            if (city == null || city == '') {
                alert('请选择城市');
                return false;
            }
            if (street == null || street == '') {
                alert('请输入详细地址');
                return false;
            }
        } else {
            if (receiverEmail == null || receiverEmail == '') {
                alert('请输入联系人邮箱');
                return false;
            }
            if (!emailReg.test(receiverEmail)) {
                alert('请输入正确联系人邮箱');
                return false;
            }
        }
        return true;
    }


    $(function() {

        init();

        $('input[type=radio][name=purchaseWay]').change(function() {
            if (this.value == 'personal') {
                $('.company_content').hide();
            }
            else if (this.value == 'company') {
                $('.company_content').show();
            }
        });

        //省份下拉选变动方法
        $('#province').change(function() {
            var provinceName = $('#province').find("option:selected").text();
            $('input[name=province]').val(provinceName);
            initCity();
        });

        //城市下拉选变动方法
        $('#city').change(function() {
            var cityName = $('#city').find("option:selected").text();
            $('input[name=city]').val(cityName);
        });




        // 保存
        $('#saveOrUpdate').bind('click',function() {

            // 验证表单
            if (!validate()) {
                return false;
            }

            var msg = '确定修改？';
            var url = '/vst_order/order/orderInvoice/updateInvoiceInfo.do';
            $.confirm(msg, function(){
                $.ajax({
                    url : url,
                    type : "post",
                    data : $("#dataForm").serialize(),
                    success : function(result) {
                        if(result.code=='success') {
                            $.alert(result.message,function(){
                                window.parent.location.reload();
                                window.parent.updateInvoiceDialog.close();
                            });
                        } else {
                            $.alert(result.message);
                        }
                    }
                });
            });
        });



        // 取消
        $('#cancel').bind('click',function(){
            window.parent.updateInvoiceDialog.close();
        });

    });
</script>

