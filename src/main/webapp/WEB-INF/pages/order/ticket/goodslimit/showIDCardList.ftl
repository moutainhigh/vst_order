<!DOCTYPE html>
<html>
<head>
<#include "/base/head_meta.ftl"/>
</head>
<body>
<div class="iframe_content mt10">
<div class="tiptext tip-warning cc5">
	<#if categoryId!=43 && categoryId!=44 && categoryId!=45>
    	<p class="pl15">日期限制：该商品在指定游玩日期范围内的每一天，同一个手机号，最多允许订购的笔数。（已取消的订单不参与累加计算）</p>
	</#if>
    <p class="pl15">黑名单：该黑名单列表内，所有日期均不允许下单。</p>      
</div>
        <div class="p_box box_info p_line">
			<div id="price_tab" class="price_tab">
				<input type="hidden" value="${categoryId}" id="hCategoryId"/>
				<ul class="J_tab ui_tab">
				<#if categoryId!=43 && categoryId!=44 && categoryId!=45>
					<li id="show1" class="active"><a href="javascript:showPage('show1');">游玩日期范围限制</a></li>
				</#if>
					<li id="show2" ><a href="javascript:showPage('show2');">手机黑名单</a></li>
					<li id="show3" class="active"><a href="javascript:showPage('show3');">身份证黑名单</a></li>
				<#if categoryId!=43 && categoryId!=44 && categoryId!=45>
					<li id="show4" ><a href="javascript:showPage('show4');">限购信息</a></li>
				</#if>
				<#if categoryId==11 || categoryId==12 || categoryId==13>	
					<li id="show5" ><a href="javascript:showPage('show5');">适用限制</a></li>
				</#if>
				</ul>
			</div>            
		</div>
		<input type="hidden" id="goodId" name="goodId" value="${goodId}">
		<div class="fl operate"><a class="btn btn_cc1" id="addIDCARD">添加</a></div><br><br>
	<#if list?? && list?size &gt; 0>
    <table class="p_table table_center" style="width:500px">
        <thead>
        <tr>
            <th class="w18">身份证号</th>
            <th class="w18">操作</th>
        </tr>
        </thead>
        <tbody>
			<#list list as SuppGoodsBlackList>
            <tr>
                <td>${SuppGoodsBlackList.blacklistNum!''} </td>
                <td>
                    <input type="hidden" name="blacklistId" value="${SuppGoodsBlackList.blacklistId}">
                    <a href="javascript:void(0);" onclick='removeTr(this,${SuppGoodsBlackList.blacklistId});' style="color:red;">删除</a></td>
                </td>
            </tr>
			</#list>
        </tbody>
    </table>
</#if>
</div>
<#include "/base/foot.ftl"/>
</body>
</html>
<script>

    function showPage(obj){
	<#if promForbidBuyId?? && promForbidBuyId!=''>
        var promForbidBuyId=${promForbidBuyId}
	<#else>
        var promForbidBuyId='';
	</#if>
        var goodId = $("#goodId").val();
        if(obj=='show1'){
            location.href = '/vst_order/goods/goodsLimit/showBlackList.do?goodId='+goodId+'&promForbidBuyId='+promForbidBuyId;
        }else if(obj=='show2'){
            location.href = '/vst_order/goods/goodsLimit/showPhoneList.do?goodId='+goodId+'&promForbidBuyId='+promForbidBuyId;
        } else if(obj=='show4'){
            if(promForbidBuyId==''){
                alert("没有限购规则");
            }else {
                location.href='/vst_prom/prom/purchase/showForbidBuy.do?goodId='+goodId+'&isFromBack=true&promForbidBuyId='+promForbidBuyId;
            }
        }else if(obj=='show5'){
        	location.href = '/vst_order/goods/goodsLimit/showIDCardAgeLimitList.do?goodId='+goodId+'&promForbidBuyId='+promForbidBuyId+'&categoryId='+${categoryId};
        }
    }

    function removeTr(obj,id){
        if(confirm('确定要删除当前记录吗')){
            $(obj).parent().parent().remove();
            $.ajax({
                url : "/vst_order/goods/goodsLimit/deleteBlackList.do?blackId="+id,
                type : "post",
                dataType : 'json',
                data : $("#searchForm").serialize(),
                success : function(result) {
                    location.reload();
                }
            });
        }
    }

    $("#addIDCARD").click(function(){
        var goodId=$("#goodId").val();
        var blacklistType = 'IDCARD';
        addDialog = new xDialog("/vst_order/goods/goodsLimit/addBlackList.do",{"goodId":goodId,"blacklistType":blacklistType}, {title:"添加身份证",width:500,hight:400,scrolling:"yes"})
    });

</script>