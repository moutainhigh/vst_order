<#--页眉-->
<!DOCTYPE html>
<html>
<head>
    <title>订单管理-订单处理</title>
</head>
<body>
<#--页面导航-->

<#if haveExpressOrder=='true' >
<div id="logResultList" class="divClass">
    <div class="order_msg clearfix">
        </br>

        <strong>
            快递单
                   <#if 'parent' == RequestParameters.orderType && order.orderStatus=='NORMAL' >
                    (<a class="btn btn_cc1" id="addOrdExpressButton" href="javaScript:" >新增</a>)
                   </#if>
        </strong>


    </div>
    <table class="p_table table_center mt20">
        <thead>

        <tr>
            <th>录入时间</th>
            <th>录入人</th>
            <th>寄送物品</th>
            <th>快递公司</th>
            <th>快递单号</th>
            <#if 'parent' == RequestParameters.orderType && order.orderStatus=='NORMAL' >
                <th>操作</th>
            </#if>
        </tr>
        </thead>
        <tbody>

            <#list expressOrderList as expressOrder>
            <tr class="tdCentre" style="height: 32px;">
                <td>${expressOrder.createTime?string('yyyy-MM-dd hh:mm:ss')}</td>
                <td>${expressOrder.writerId}</td>
                <td>${expressOrder.itemType}</td>
                <td>
                    <#switch expressOrder.company>
                        <#case "shunfeng">顺丰<#break>
                        <#case "yuantong">圆通<#break>
                        <#case "yunda">韵达<#break>
                        <#case "shentong">申通<#break>
                        <#case "1">游客自提<#break>
                        <#default>其他
                    </#switch>
                </td>
                <td>${expressOrder.expressCode}</td>

                <#if 'parent' == RequestParameters.orderType && order.orderStatus=='NORMAL' >
                <td>
                    <a href="javaScript:" class="showLogDetail" data="${expressOrder.ordExpressId}" name="editExpressOrderButton">编辑</a>
                    <#if expressOrder.company != "1" >
                        <a href="javaScript:" class="showLogDetail" data="${expressOrder.ordExpressId}" name="sendMessageButton">发送短信</a>
                    </#if>
                </td>
                </#if>

            </tr>
            </#list>

        </tbody>
    </table>
</div>



<#--页脚-->
</body>
</html>
<script type="text/javascript">

    /*新增快递信息*/
    var addOrdExpressButtonDialog;
    $("#addOrdExpressButton").bind("click",function(){
        if(${order??})
        {
            var param={"orderId":${order.orderId!''},"orderType":"parent"};
            addOrdExpressButtonDialog = new xDialog("/vst_order/order/orderManage/showInsertOrdExpress.do",param,{title:"新增快递信息",width:400, height:330});
        }
    });

    /*修改快递信息*/
    var editOrdExpressButtonDialog;
    $("a[name='editExpressOrderButton']").bind("click",function(){
        if(${order??})
        {
            var param={"ordOrderId":${order.orderId!''},"ordExpressId":$(this).attr("data"),"orderType":"parent"};
            editOrdExpressButtonDialog = new xDialog("/vst_order/order/orderManage/showUpdateOrdExpress.do",param,{title:"修改快递信息",width:400, height:330});
        }
    });


    /*短信发送*/
    $("a[name='sendMessageButton']").bind("click",function(){

        var param={"ordOrderId":${order.orderId!''},"ordPersonId":${addressPerson.ordPersonId!''},"ordExpressId":$(this).attr("data")};

        //遮罩层
        var loading = pandora.loading("正在发送中...");

        var url="/vst_order/order/orderManage/sendOrdExpressSMS.do";

        $.ajax({
            url : url,
            data : param,
            type:"POST",
            dataType:"JSON",
            success : function(result){
                //var message=result.message;
                if(result.code=="success" ){
                    alert(result.message);
                    loading.close();

                }else {
                    alert(result.message);
                }
            }
        });

    });


</script>
</#if>
