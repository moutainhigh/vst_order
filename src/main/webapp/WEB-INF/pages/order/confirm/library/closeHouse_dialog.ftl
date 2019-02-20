<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
<head>

<#include "/base/head_meta.ftl"/>
    <link rel="stylesheet" href="http://pic.lvmama.com/styles/backstage/v1/pandora-calendar.css"/>
    <link rel="stylesheet"  href="http://pic.lvmama.com/styles/backstage/v1/sales-information-iframe.css"/>
</head>
<body>
<div class="iframe_search">
    <#if ordOrderItem?? >
<form method="post" >
 <input type="hidden" name="sourceType" value="${checkedTab}"/>
 <input type="hidden" name="orderItemId" value="${ordOrderItem.orderItemId!''}"/>
 <input type="hidden" name="sourceType" value="${ordOrderItem.sourceType!''}"/>


    <table class="s_table">
        <tbody>
        <tr>
            <div width="25%">
                <td class="s_label">产品ID：</td>
                <td class="w18"><span  name="productId" value="${ ordOrderItem.productId!''}" >${ ordOrderItem.productId!''}</span></td>
            </div>
            <div width="35%">
                <td class="s_label">产品名称：</td>
                <td class="w18"><span  name="productName" value="${ordOrderItem.productName!''}">${ordOrderItem.productName!''}</span></td>
            </div>
            <div width="40%">
                <td class="s_label">供应商名称：</td>
                <td class="w18"><span  name="supplierName" value="${ordOrderItem.supplierName!''}">${ordOrderItem.supplierName!''}</span></td>
            </div>

        </tr>
        </tbody>
    </table>	
	</form>
</div>
	
<!-- 主要内容显示区域\\ -->
<div>该订单关房操作记录:</div>
<div>   
    <iframe  frameborder="0" scrolling="yes" style="width:100%;" src="/lvmm_log/bizLog/showVersatileLogList?objectType=ORD_ORDER_ITEM_COLSE_HOUSE&objectId=${ordOrderItem.orderItemId!''}&sysName=VST"></iframe>
</div><!-- //主要内容显示区域 -->
 <#if suppGoodsList?? && suppGoodsList?size gt 0>
 <div class="row mt10">
     <div class="col w460 ml10" style="float: left"><span>请选择关房商品：</span></div>
     <div >
         <table class="display-table">

             <thead>
             <tr>
                 <td >
                     <label>
                         <input class="JS_result_select_all" type="checkbox">全选 &nbsp;
                         <label>
                 </td>
             </tr>
             <#list  suppGoodsList  as suppGood>
                   <#if suppGood.suppGoodsId??>
                   		<#if suppGood_index%5==0><tr id=${suppGood_index}></#if>
                     		<#if "${ordOrderItem.suppGoodsId}" == "${suppGood.suppGoodsId}" >
                         		<td>
                             		<label>
                                 		<input class="JS_result_select" type="checkbox" checked="checked" data-goodsid="${suppGood.suppGoodsId}" data-goodsName="${suppGood.goodsName}<#if ordOrderItem.categoryId==1>_${suppGood.branchName}</#if>"><#if ordOrderItem.categoryId==1>${suppGood.branchName}_</#if>${suppGood.goodsName}  &nbsp;
                                 		
                             		</label>
                         		</td>
                        	<#else >
                            	<td>
                                	<label>
                                    	<input class="JS_result_select" type="checkbox" data-goodsid="${suppGood.suppGoodsId}" data-goodsName="${suppGood.goodsName}<#if ordOrderItem.categoryId==1>_${suppGood.branchName}</#if>"><#if ordOrderItem.categoryId==1>${suppGood.branchName}_</#if>${suppGood.goodsName}  &nbsp;
                                	</label>
                            	</td>
                     		</#if>
                   		</#if>
					<#if suppGood_index%5==5>
						</tr>
					</#if>
                 </#list>
             </thead>
         </table>
     </div>
 </div>
</#if>


<div class="row mt10" style="width: 100%;height:280px;">
    <div class="col w460 ml10" style="float: left"><span>请选择关房时间：</span></div>
    <div class="row" style="width: 450px;float: left">
        <div class="JS_select_date"></div>
        <select class="JS_select_date_hidden"  multiple="true" id="selDate"></select>

    </div>
    <div class="row" style="float: right; width: 220px;margin-top: 120px">
        <a class="btn btn_cc1"  href="javaScript:removeDate();" title="清除">清除</a>
    </div>

 </div>
<div class="row mt10"><span style="float: none;">&nbsp;&nbsp;&nbsp;&nbsp;备注：&nbsp;&nbsp;</span><textarea style="width: 420px" class="textarea" autocomplete="off" placeholder="备注" name="clsoeHouseMark" rows="8" cols="15" maxlength="2000" >${ordOrderItem.orderMemo!''}</textarea></div>

<div class="row mt10" style="float: right;padding-right: 80px;">
    <table>
        <tr>
            <td> <a class="btn btn_cc1" href="javaScript:confirmButton();" title="确认">确认</a>&nbsp;&nbsp;</td>
            <td> <a class="btn btn_cc1"  href="javaScript:cancelButton();" title="取消">取消</a></td>
        </tr>
    </table>
</div>
</div>
    <#else>
    <div >无该订单相关信息！</div>
    </#if>
<#include "/base/foot.ftl"/>
<script src="http://pic.lvmama.com/js/backstage/v1/pandora-calendar.js"></script>
<script src="http://pic.lvmama.com/min/index.php?f=/js/backstage/v1/common.js,/js/lv/dialog.js,/js/lv/calendar.js"></script>

<script>
    // JavaScript Document
    $(document).ready(function(){
        //文档就绪自动渲染日历层
        pandora.calendar({
            target:".JS_select_date",//日历容器. 使用jquery选择器语法
            selectDateCallback:selectDateCallBack,
            cancelDateCallback: cancelDateCallBack,
            completeCallback: reRendarSelectedDate,
            autoRender:true,
            allowMutiSelected: true,
            isTodayClick: true,
            mos: 24,
            template: "small"
        });

        $("#btnDel").bind("click",function(){removeDate();});
        initSeletedDate();
    });
     var goodsArray=new Array();
     function confirmButton(){
         var goodsNameStr='';
         var goodsIdStr='';
         var goodsIdStr="";
         var firstIndex=0;
         $(".JS_result_select").each(function (index, dom) {
             if ($(dom).is(":checked")) {
                 console.log($(this));
                 goodsArray.push($(this).attr("data-goodsid"));
                 if( firstIndex == 0){
                     goodsIdStr=$(this).attr("data-goodsid");
                     goodsNameStr=$(this).attr("data-goodsname");
                     firstIndex++;
                 }else {
                     goodsIdStr=goodsIdStr+","+$(this).attr("data-goodsid");
                     goodsNameStr=goodsNameStr+","+$(this).attr("data-goodsname");
                 }
             }
         });
         if(goodsArray.length == 0){
             alert("请选择商品！");
             return;
         }
         
         var target = $("#selDate option");
         if(target.length == 0){
             alert("请选择日期！");
             return;
         }
         if(target.length >= 10){
             alert("选择日期请小于10天！");
             return;
         }
         var dateStr="";
         $.each(target, function (index, items) {
             var value = $(items).val();
             if (value){
                 if(index == 0){
                     dateStr=value;
                 }else{
                     dateStr=dateStr+","+value;
                 }
             }
         });
         var content="关房商品："+goodsNameStr+" \n关房日期："+dateStr+"\n              是否确认关房？";
         var r=confirm(content);
         if (r==true){
             var tempSourceType=$("input[name='sourceType']").val();
             var sourceType='';
             if(tempSourceType=='PECULIAR_FULL_AUDIT'){
                 //特殊满房关房
                 sourceType='PECULIAR_FULL';
             }else if(tempSourceType=='CHANGE_PRICE_AUDIT'){
                 //变价关房
                 sourceType='CHANGE_PRICE';
             }else if(tempSourceType=='FULL_AUDIT'){
              	//满房关房
                 sourceType='CONSOLE_FULL';
             }
             //调取关房操作
             closeHouse(goodsIdStr,dateStr,sourceType);

         }else{
             //closeHouse(goodsIdStr,dateStr,sourceType);
         }
     }
     //关房操作
     function closeHouse(suppGoodsIdListStr,closeDateListStr,sourceType) {
            $.ajax({
                url : "/vst_order/ord/order/confirm/closeHouse.do",
                data : {
                    orderItemId:$("input[name='orderItemId']").val(),
                    suppGoodsIdListStr:suppGoodsIdListStr,
                    closeDateListStr:closeDateListStr,
                    sourceType:sourceType,
                    orderMemo:$("textarea[name='clsoeHouseMark']").val()
                },
                type:"POST",
                dataType:"JSON",
                success : function(result){

                    if(result.success==true ){
                        alert("关房成功！");
                        document.location.reload();
                    }else {
                        alert("关房失败！");

                    }
                },
                error: function(XMLHttpRequest, textStatus, errorThrown) {
                    loading.close();
                    if(textStatus=='timeout'){
                        alert("程序运行超时");
                        document.location.reload();
                    }else{
                        alert("关房失败，请重试，或请手动关房，");
                        document.location.reload();
                    }
                }
            });
     }
     //取消关房
     function cancelButton(){
         window.parent.closeHouseDialog.close();
     }
     //全选  //全不选
     $(".JS_result_select_all").die("change");
     $(".JS_result_select_all").live("change", function () {
         var $this = $(this);
         if ($this.is(":checked")) {
             $(".JS_result_select").prop("checked", true)
         } else {
             $(".JS_result_select").prop("checked", false)
         }
     });
     $(".JS_result_select").die("change");
     $(".JS_result_select").live("change", function () {
         var isAllChecked = true;
         $(".JS_result_select").each(function (index, dom) {
             if (!$(dom).is(":checked")) {
                 isAllChecked = false;
             }
         });
         if (isAllChecked) {
             $(".JS_result_select_all").prop("checked", true)
         } else {
             $(".JS_result_select_all").prop("checked", false)
         }
     });
    function removeDate(){
        var target = $("#selDate option");
        $.each(target, function (index, items) {
            var value = $(items).val();
            if (!value)
                return true;
            var next = $(items).next().val() ? $(items).next() : $(items).prev();//判断下一个被选中的目标
            $("#selDate option").remove();
            next.attr("selected", "selected");
            $("td.calSelected[date-map='" + value + "']").removeClass("calSelected");
        });
    }

    function selectDateCallBack(data){
        var date=data.selectedDate;
        if(!date)
            return;
        if($("#selDate option[value='"+date+"']").length!=0)//once allowed
            return;
        $("#selDate").append("<option value='"+date+"'>"+date+"</option>");
    }

    function cancelDateCallBack(data){
        var date=data.selectedDate;
        if(!date)
            return;
        $("#selDate option[value='"+date+"']").remove();
    }

    function reRendarSelectedDate() {
        var target = $("#selDate option");
        $.each(target, function (index, items) {
            var value = $(items).val();
            if (!value)
                return true;
            $("td[date-map='" + value + "']").addClass("calSelected");
        });
    }
    function initSeletedDate(){
        var visitTimeArray=$("input[name='orditemVisitTime']").val();
        if( visitTimeArray ){
            var dataObj=eval("("+visitTimeArray+")");//转换为json对象
            //遍历json数组
            $.each(dataObj, function(i, item) {
                console.log(item);
                $("td[date-map='" + item + "']").addClass("calSelected");
                $("#selDate").append("<option value='"+item+"'>"+item+"</option>");
            });
        }
    };

</script>
</body>
</html>




