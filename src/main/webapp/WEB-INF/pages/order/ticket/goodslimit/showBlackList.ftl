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
				<input type="hidden" name="categoryId"  value="${categoryId}" id="hCategoryId"/>
				<ul class="J_tab ui_tab">
				<#if categoryId!=43 && categoryId!=44 && categoryId!=45>
					<li id="show1" class="active"><a href="javascript:showPage('show1');">游玩日期范围限制</a></li>
				</#if>	
					<li id="show2" ><a href="javascript:showPage('show2');">手机黑名单</a></li>
					<li id="show3" ><a href="javascript:showPage('show3');">身份证黑名单</a></li>
				<#if categoryId!=43 && categoryId!=44 && categoryId!=45>	
					<li id="show4" ><a href="javascript:showPage('show4');">限购信息</a></li>
				</#if>
				<#if categoryId==11 || categoryId==12 || categoryId==13>	
					<li id="show5" ><a href="javascript:showPage('show5');">适用限制</a></li>
				</#if>
				</ul>
			</div>            
		</div>
		<div class="limit" id="limit">
         <form id="searchForm" action="/supp/blacklist/saveLimit.do"  method="post">
         <input type="hidden" id="limitId" name="limitId" <#if limit??>value="${limit.limitId}"</#if>>
         <input type="hidden" id="goodId" name="goodId" value="${goodId}">
		 <table class="e_table form-inline">
			<#if isShowCircusFlag ="Y">
                <tbody>
                <tr>
                    <td width="120" class="e_label td_top">游玩日期范围限制:</td>
                    <td><label class="radio"><input type="radio" name="limitAble" <#if limit==null>checked=checked</#if>  value="N">不限</label><label class="radio"><input type="radio" name="limitAble" <#if limit!=null>checked=checked</#if> value="Y">限制</label></td>
                </tr>
                <tr>
                    <td width="120" class="e_label td_top"><i class="cc1">*</i>游玩日期范围：</td>
                    <td class="w18" colspan='5'><input type="text" <#if limit??> value="${limit.startTime?string('yyyy-MM-dd')}"</#if> id="startTime" name="startTime" errorEle="selectDate" class="Wdate" id="d4321" onFocus="WdatePicker({readOnly:true})" />
                        <input type="text" <#if limit??> value="${limit.endTime?string('yyyy-MM-dd')}"</#if> id="endTime" name="endTime" errorEle="selectDate" class="Wdate" id="d4322" onFocus="WdatePicker({readOnly:true})" /></td>
                </tr>
                <tr>
                    <td width="120" class="e_label td_top"><i class="cc1">*</i>同游玩日同手机最多订购笔数：</td>
                    <td>
                        <select id="limitNum" name="limitNum">
							<#list blacklistNumList as blackListNum>
								<#if limit??>
									<#if blackListNum == limit.limitNum>
                                        <option value="${blackListNum}"  selected="selected">${blackListNum}</option>
									<#else>
                                        <option value="${blackListNum}">${blackListNum}</option>
									</#if>
								<#else>
                                    <option value="${blackListNum}">${blackListNum}</option>
								</#if>
							</#list>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td width="120" class="e_label td_top"></td>
                    <td><div class="fl operate"><a class="btn btn_cc1" id="saveLimit">保存</a></div></td>
                </tr>
                </tbody>
			</#if>
			<#if categoryId!=43 && categoryId!=44 && categoryId!=45>
                <div class="tiptext tip-warning cc5">
                    <p class="pl15">商品限购可在VST-【限购管理】-【产品限购】里做相应设置。</p>
                </div>
             </#if>
            </table>
        </form>
    </div>
</div>
<#include "/base/foot.ftl"/>
</body>
</html>

<script>
    //初始化控件状态
    $("#limitNum").attr("disabled","disabled");
    $(".Wdate").attr("disabled","disabled");
    if($("input[name='limitAble']:checked").val()=="Y"){
        $("#limitNum").removeAttr("disabled");
        $(".Wdate").removeAttr("disabled");
    }
    $("input[name='limitAble']").click(function(){
        var val = $(this).val();
        if("Y"==val){
            $("#limitNum").removeAttr("disabled");
            $(".Wdate").removeAttr("disabled");
        }else {
            $("#limitNum").attr("disabled","disabled");
            $(".Wdate").attr("disabled","disabled");
        }
    });

    function showPage(obj){
		<#if promForbidBuyId?? && promForbidBuyId!=''>
        	var promForbidBuyId=${promForbidBuyId}
		<#else>
        	var promForbidBuyId='';
		</#if>

        <#if hasForbidBuyProductIds?? && hasForbidBuyProductIds!=''>
            var hasForbidBuyProductIds=${hasForbidBuyProductIds}
        <#else >
            var hasForbidBuyProductIds='';
        </#if >

        var goodId = $("#goodId").val();
		if(obj=='show1'){
            location.href = '/vst_order/goods/goodsLimit/showBlackList.do?goodId='+goodId+'&promForbidBuyId='+promForbidBuyId;
        }
        else if(obj=='show2'){
            location.href = '/vst_order/goods/goodsLimit/showPhoneList.do?goodId='+goodId+'&promForbidBuyId='+promForbidBuyId;
        }else if(obj=='show3'){
            location.href = '/vst_order/goods/goodsLimit/showIDCardList.do?goodId='+goodId+'&promForbidBuyId='+promForbidBuyId;
        }else if(obj=='show4'){
			if(promForbidBuyId==''&& hasForbidBuyProductIds==''){
				alert("没有限购规则");
			}else {
                if(hasForbidBuyProductIds=='' && promForbidBuyId!=''){
                    location.href='/vst_prom/prom/purchase/showForbidBuy.do?goodId='+goodId+'&isFromBack=true&promForbidBuyId='+promForbidBuyId;
                }else{
                    alert('该商品打包的产品。有限购规则的产品id是:' +hasForbidBuyProductIds );
                }

			}
        }else if(obj=='show5'){
        	location.href = '/vst_order/goods/goodsLimit/showIDCardAgeLimitList.do?goodId='+goodId+'&promForbidBuyId='+promForbidBuyId+'&categoryId='+${categoryId};
        }
    }

    $("#saveLimit").click(function(){
        if($("input[name='limitAble']:checked").val()=="Y"){
            var startTime = $("#startTime").val();
            var endTime = $("#endTime").val();
            var limitNum = $("#limitNum").val();

            if(startTime==''){
                $.alert("请选择开始时间");
                return;
            }
            if(endTime==''){
                $.alert("请选择结束时间");
                return;
            }

            var d1 = new Date(startTime.replace(/\-/g, "\/"));
            var d2 = new Date(endTime.replace(/\-/g, "\/"));
            if(d1 > d2)
            {
                $.alert("开始时间不能大于结束时间！");
                return false;
            }

            if(limitNum==''){
                $.alert("请填写份数");
                return;
            }
        }
        $.ajax({
            url : "/vst_order/goods/goodsLimit/saveLimit.do",
            type : "post",
            dataType : 'json',
            data : $("#searchForm").serialize(),
            success : function(result) {
                $.alert("保存成功");
            }
        });
    });

</script>