<!DOCTYPE html>
<html>
<head>
    <title>订单管理-订单处理</title>

</head>
<body>
<form action="#" method="post" id="ordExpressOrderForm">
    <input type="hidden" name="ordOrderId" id="ordOrderId" value="${orderId}">


    <table class="p_table form-inline">
        <tbody>
        <tr>
            <td class="p_label" ><span class="notnull"></span>*寄送物品</td>
            <td>
                <select id="itemType" name="itemType" required="true">
                    <option value="门票" selected="selected">门票</option>
                    <option value="物料">物料</option>
                </select>
            </td>

        </tr>
        <tr>
            <td class="p_label" ><span class="notnull"></span>*快递公司</td>
            <td>
                <select id="company" name="company" required="true">
                    <option value="" >请选择</option>
                    <option value="shunfeng">顺丰</option>
                    <option value="yuantong">圆通</option>
                    <option value="yunda">韵达</option>
                    <option value="shentong">申通</option>
                    <option value="1">游客自提</option>
                </select>
            </td>

        </tr>
        <tr>
            <td class="p_label" ><span class="notnull"></span>快递单号</td>
            <td>
                <input  type="text" value=""  id="expressCode" name="expressCode" required="true">
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
        if(!$("#ordExpressOrderForm").validate().form()){
            return;
        }
        //遮罩层
        var loading = pandora.loading("正在努力新增中...");


        var url="/vst_order/order/orderManage/insertOrdExpress.do";


        $.ajax({
            url : url,
            data : $("#ordExpressOrderForm").serialize(),
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
        addOrdExpressButtonDialog.close();
    });
</script>

