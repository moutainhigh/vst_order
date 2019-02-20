<!DOCTYPE html>
<html>
<head>
    <style>
        ul {
            margin: auto;
            list-style-type: none;
            width: 40%;
        }
    </style>
</head>
<body>
<p >
    提示：改期只针对EBK可及时通关订单，只允许改期${suppChangeCount}次。<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;只能同价位改期不支持差价改期
</p>
<div id="orderRescheduleDiv">
		<#if ordRescheduleFlag=='Y'>
            <input type="hidden" name="orderId" value="${orderId}">
             <#if showType?? && "order" == showType>
                 <ul>
                     <#if orderItemList?? && mainItem??>
                         <#list orderItemList as orderItem>
                     <li class="items">
                         <input type="hidden" name="orderItemId" value="${orderItem.orderItemId}" >
                         <b>(${orderItem.orderItemId})${orderItem.productName+"-"+orderItem.suppGoodsName}</b>
                     </li>
                         </#list>
                     </#if>
                     <li>
                         <input type = "hidden" name = "visitTime" value="${mainItem.visitTime?string("yyyy-MM-dd")}">
                         改期:&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="changeVisitDate" class="Wdate" id="changeVisitDate"  defaultVisitTime="${mainItem.visitTime?string("yyyy-MM-dd")}"
                                                           value="${mainItem.visitTime?string("yyyy-MM-dd")}" required/>
                     </li>
                 </ul>
             <#elseif showType?? && "orderItem" == showType >
                 <#if orderItemList??>
                     <#list orderItemList as orderItem>
                         <ul>
                             <li><b>(${orderItem.orderItemId})${orderItem.productName+"-"+orderItem.suppGoodsName}</b></li>
                             <li class="items">
                                 <input type="hidden" name="orderItemId" value="${orderItem.orderItemId}" >
                                 改期:&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="changeVisitDate" class="Wdate" id="changeVisitDate" defaultVisitTime="${orderItem.visitTime?string("yyyy-MM-dd")}"
                                                                   value="${orderItem.visitTime?string("yyyy-MM-dd")}" required/>
                             </li>
                         </ul>
                     </#list>
                 </#if>
             <#else >
             </#if>
            <br>
        <#else>
            <p style="font-size: 16px;">不能再改期</p>
		</#if>
</div>
<div style="text-align: center">
    <button class="pbtn-small btn-ok disabled"  id="orderRescheduleSubmit">申请改期</button>
</div>
</body>

</html>
<script>
    $(function(){
        $(".Wdate").focus(function(){
            var $that = $(this);
            WdatePicker({readOnly:true,minDate:'%y-%M-{%d}',onpicked:function(){
                var defaultVisitTime = $that.attr("defaultVisitTime");
                var flag = false;
                $(".Wdate").not($that).each(function(){
                    if($(this).val()!=$(this).attr("defaultVisitTime")){
                        flag = true;
                    }
                });
                if(defaultVisitTime!=$that.val()){
                    $("#orderRescheduleSubmit").removeClass("disabled"); 
                }else if(flag){
                    $("#orderRescheduleSubmit").removeClass("disabled");
                }else{
                    $("#orderRescheduleSubmit").addClass("disabled");
                }
            }})
            
        });
        $("#orderRescheduleSubmit").bind("click", function () {
            var arr = [];
            var obj={};
            var orderId = $("input[name='orderId']").val();
            var showType = "${showType}";
            $("#orderRescheduleDiv .items").each(function(i,e){
                var item={};
                var orderItemId = $(this).find("input[name='orderItemId']").val();
                var changeVisitDate;
                if('order'==showType){
                    changeVisitDate = $("#orderRescheduleDiv").find("input[name='changeVisitDate']").val(); 
                }else{
                    changeVisitDate =$(this).find("input[name='changeVisitDate']").val();
                }
                if(orderItemId!="" && changeVisitDate!=""){
                    item.orderItemId = orderItemId;
                    item.changeVisitDate  = changeVisitDate;
                    arr.push(item);
                }
            });
            obj.items=arr;
            obj.orderId=orderId;
            if (!(arr.length>0)|| orderId=="") {
                return;
            }
            console.info(JSON.stringify(obj));
            var confirmDialog = pandora.dialog({
                content: "<div style='text-align: center'><p>确定修改?</p> <a class='btn btn_cc1 ok'>确定</a> <a class='btn btn_cc1 cancel'>返回修改</a></div>",
                zIndex: 3000,
                mask: true
            });
            var clicked = false;
            confirmDialog.wrap.find('a.ok').click(
                    function() {
                        if (clicked) {
                            alert('请勿重复点击');
                        } else {
                            clicked = true;
                            $.ajax({
                                url: "/vst_order/order/orderManage/ordReschedule.do",
                                data: JSON.stringify(obj),
                                type: "POST",
                                dataType: "json",
                                contentType: "application/json; charset=utf-8",
                                success: function (data) {
                                    var msg = data.message;
                                    clicked = false;
                                    confirmDialog.close();
                                    if (data.code == "success") {
                                        msg = "<div class='text-center'>" + msg + "</div>";
                                        ordRescheduleDialog.close();
                                        pandora.dialog({height: 200, width: 400, content: msg, mask: true});
                                    } else {
                                        msg = "<div class='text-center'><p>改期失败<br>原因：" + msg + "</p><a class='btn btn_cc1 cancel'>返回修改</a></div>";
                                        var cancelDialog = pandora.dialog({height: 200, width: 400, content: msg, mask: true});
                                        cancelDialog.wrap.find('a.cancel').click(function () {
                                            cancelDialog.close();
                                        })
                                    }
                                }
                            });
                        }
                    }
            );
            confirmDialog.wrap.find('a.cancel').click(
                    function() {confirmDialog.close();}
            );
            return false;
        });
        
    });
    //# sourceURL=orderReschedule.show
</script>
