<!DOCTYPE html>
<html>
<head>
    <title>订单管理-订单处理</title>

</head>
<body>
<form action="#" method="post" id="updateOrdExpressOrderForm">
    <input type="hidden" name="ordOrderId" id="ordOrderId" value="${ordExpress.ordOrderId}">
    <input type="hidden" name="ordExpressId" id="ordExpressId" value="${ordExpress.ordExpressId}">

    <table class="p_table form-inline">
        <tbody>
            <tr>
                <td class="p_label" ><span class="notnull"></span>*寄送物品</td>
                <td>
                    <select id="itemType" name="itemType" required="true">
                        <option value="门票" <#if ordExpress.itemType == '门票'> selected="selected" </#if> >门票</option>
                        <option value="物料" <#if ordExpress.itemType == '物料'> selected="selected" </#if> >物料</option>
                    </select>
                </td>

            </tr>
            <tr>
                <td class="p_label" ><span class="notnull"></span>*快递公司</td>
                <td>
                    <select id="company" name="company" required="true">
                        <option value="" >请选择</option>
                        <option value="shunfeng" <#if ordExpress.company == 'shunfeng'> selected="selected" </#if> >顺丰</option>
                        <option value="yuantong" <#if ordExpress.company == 'yuantong'> selected="selected" </#if> >圆通</option>
                        <option value="yunda" <#if ordExpress.company == 'yunda'> selected="selected" </#if> >韵达</option>
                        <option value="shentong" <#if ordExpress.company == 'shentong'> selected="selected" </#if> >申通</option>
                        <option value="1" <#if ordExpress.company == '1'> selected="selected" </#if> >游客自提</option>
                    </select>
                </td>

            </tr>
            <tr>
                <td class="p_label" ><span class="notnull"></span>快递单号</td>
                <td>

                    <input  type="text" value="${ordExpress.expressCode}" id="expressCode" name="expressCode" required="true">

                </td>

            </tr>

        </tbody>
    </table>
</form>
</body>
</html>
<p align="center">
    <button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="sendButton">保存</button>
    &nbsp;&nbsp;
    <button class="pbtn pbtn-small btn-ok" style="margin-top:20px;" id="closeButton">取消</button>
</p>
<script type="text/javascript">


    $("#company").change(function(){

        if( $("#company").val() == "1"){
            $("#expressCode").attr("required",false);
        }else{
            $("#expressCode").attr("required",true);
        }
    });


    $("#sendButton").bind("click",function(){

        //验证
        if(!$("#updateOrdExpressOrderForm").validate().form()){
            return;
        }
        //遮罩层
        var loading = pandora.loading("正在努力保存中...");


        var url="/vst_order/order/orderManage/updateOrdExpress.do";


        $.ajax({
            url : url,
            data : $("#updateOrdExpressOrderForm").serialize(),
            type:"POST",
            dataType:"JSON",
            success : function(result){
                //var message=result.message;
                if(result.code=="success" ){
                    alert(result.message);

                    parent.window.location.reload();

                }else {
                    alert(result.message);
                }
            }
        });

    });



    //取消按钮事件
    $("#closeButton").bind("click", function() {
        editOrdExpressButtonDialog.close();
    });
</script>

