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
					<li id="show2"><a href="javascript:showPage('show2');">手机黑名单</a></li>
					<li id="show3" ><a href="javascript:showPage('show3');">身份证黑名单</a></li>
				<#if categoryId!=43 && categoryId!=44 && categoryId!=45>
					<li id="show4" ><a href="javascript:showPage('show4');">限购信息</a></li>
				</#if>
				<#if categoryId==11 || categoryId==12 || categoryId==13>
					<li id="show5" class="active"><a href="javascript:showPage('show5');">适用限制</a></li>
				</#if>
			</ul>
		</div>            
	</div>
	<input type="hidden" id="goodId" name="goodId" value="${goodId}">
	<input type="hidden" id="hasIDCardAgeLimit" value="${hasIDCardAgeLimit}">
	
	<div class="limit" id="limit">
         <form id="dataForm">
		 	<table class="e_table form-inline">
                <tbody>
                <tr>
                    <td style="text-align:center" class="e_label td_top">适用人群</td>
                    <td class="w18" colspan='5'></td>
                </tr>
                <tr>
                    <td width="120" class="e_label td_top">年龄段限制：</td>
                    <td class="w18" colspan='5'>
                      	<table id="typeTable">
							<tbody>
							   <tr>
									<th>
										<div class="mb10 w61">
											<input type="radio" id="defaultRule" name="ruleFlag" value="N">默认规则&nbsp&nbsp注：无限制</input>
										</div>
									</th>
								</tr>
								<tr>
									<th>
										<div class="mb10 pull-left w61">
											<input type="radio" id="specialRule" name="ruleFlag" value="Y">特殊规则&nbsp&nbsp注：规则中的年龄段即可下单，否则提示：该年龄段不能购买该商品</input>
										</div>
									</th>
								</tr>
								<#if suppGoodsIDCardLimitList?? && suppGoodsIDCardLimitList?size &gt; 0>
									<#list suppGoodsIDCardLimitList as suppGoodsIDCardLimit>
										<tr>
											<td>
												<div class="info-tip-warning w78 pull-left">
												   <input type="text" name="startAge" value=${suppGoodsIDCardLimit.startAge} onkeyup='javascript:RepNumber(this)' /> — <input type="text" name="endAge" value=${suppGoodsIDCardLimit.endAge} onkeyup='javascript:RepNumber(this)' />
										           <a onclick="saveAge(this)" class="btn" data=${suppGoodsIDCardLimit.idCardLimitId}>保存</a>
										           <a onclick="deleteAge(this)" class="btn" data=${suppGoodsIDCardLimit.idCardLimitId}>删除</a>
												</div>
											</td>
		                                </tr>
	                                </#list>
								<#else>
									<tr>
										<td>
											<div class="info-tip-warning w78 pull-left">
											   <input type="text" name="startAge" onkeyup='javascript:RepNumber(this)' /> — <input type="text" name="endAge" onkeyup='javascript:RepNumber(this)' />
									           <a onclick="saveAge(this)" class="btn">保存</a>
									           <a onclick="deleteAge(this)" class="btn" id="deleteAgeBtn">删除</a>
											</div>
										</td>
	                                </tr>
								</#if>
							</tbody>
						</table> 
                    </td>
                </tr>
                <tr>
                    <td width="120" class="e_label td_top"></td>
                    <td><div class="fl operate"><a class="btn" id="addIDCardAgeRange">+ 新增规则</a></div></td>
                </tr>
                </tbody>
            
            </table>
        </form>
    </div>
</div>
<#include "/base/foot.ftl"/>
</body>
</html>
<script>
	$(document).ready(function(){
		var hasIDCardAgeLimit = $("#hasIDCardAgeLimit").val();
		if(hasIDCardAgeLimit == "Y"){
			$("#specialRule").attr("checked",true);
		}else{
			$("#defaultRule").attr("checked", true);
			$("input[name='startAge']").attr("disabled","disabled");
			$("input[name='endAge']").attr("disabled","disabled");
			$("#deleteAgeBtn").hide();
		}
	});

	$("input[name='ruleFlag']").change(function(){
		if($(this).val()=='Y'){
			$("input[name='startAge']").removeAttr("disabled");
			$("input[name='endAge']").removeAttr("disabled");
		}else{
			var isAgeLimit = $("#hasIDCardAgeLimit").val();
			if(isAgeLimit == 'Y'){
				$("#specialRule").attr("checked","true");
				$.alert("请先删除已保存的适用年龄再设为默认规则");
				return;
			}
			$("input[name='startAge']").attr("disabled","disabled");
			$("input[name='endAge']").attr("disabled","disabled");
		};
	});

    var str = '<tr><td><div class="info-tip-warning w78 pull-left"><input type="text" name="startAge" onkeyup="javascript:RepNumber(this)" /> — <input type="text" name="endAge" onkeyup="javascript:RepNumber(this)" />  <a onclick="saveAge(this)" class="btn">保存</a>  <a onclick="deleteAge(this)" class="btn">删除</a></div></td></tr>';
    $("#addIDCardAgeRange").bind("click",function(){
        if(!$("#specialRule").prop("checked")){
           return;
        }
    	$("#typeTable tbody").append(str);
    	$("#deleteAgeBtn").show();
    });

    //保存
    function saveAge(obj){
        if(!$("#specialRule").prop("checked")){
           return;
        }
    	var idCardLimitId = $(obj).attr("data");
    	var startAge =$(obj).parent().children().eq(0).val().trim() ;
	    var endAge = $(obj).parent().children().eq(1).val().trim() ;
	  	if(startAge == "" || endAge == ""){
	  		$.alert("请正确输入年龄！");return;
	  	}
	  	var startAgeInt = parseInt(startAge);
	  	var endAgeInt = parseInt(endAge);
	    if(startAgeInt > endAgeInt){
			$.alert("开始年龄要小于结束年龄！");return;
		}
		$("#deleteAgeBtn").show();
    	$.ajax({
			  url : "/vst_order/goods/goodsLimit/saveOrUpdateIDCardAgeLimit.do",
			  type : "post",
			  dataType:"JSON",
			  data : {"suppGoodsId":${goodId},"startAge":startAge,"endAge":endAge,"idCardLimitId":idCardLimitId},
			  async:false,
			  success : function(data) {
					var msg = data.message;
				    if(data.code=="success"){
				    	$.alert(msg, function() {
							window.location.reload();
						});
					}else {
						$.alert(result.message);
					}
			  }
		});
     }

    //删除
    function deleteAge(obj){
    	if(!$("#specialRule").prop("checked")){
           return;
        }
    	var idCardLimitId = $(obj).attr("data");
    	if(idCardLimitId == undefined || idCardLimitId == "" || idCardLimitId == null){
    		//删除空行
    		var deleteTr = $(obj).parent().parent().parent();
    		$(deleteTr).remove();
    		return;
    	}
    	$.ajax({
		  url : "/vst_order/goods/goodsLimit/deleteIDCardAgeLimit.do",
		  type : "post",
		  dataType:"JSON",
		  data : {"suppGoodsId":${goodId},"idCardLimitId":idCardLimitId},
		  async:false,
		  success : function(data) {
		 	    var msg = data.message;
			    if(data.code=="success"){
					$.alert(msg, function() {
						window.location.reload();
					});
				}else {
					$.alert(result.message);
				}
		  }
	   });
    }

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
        }else if(obj=='show3'){
            location.href = '/vst_order/goods/goodsLimit/showIDCardList.do?goodId='+goodId+'&promForbidBuyId='+promForbidBuyId;
        }else if(obj=='show4'){
            if(promForbidBuyId==''){
                alert("没有限购规则");
            }else {
                location.href='/vst_prom/prom/purchase/showForbidBuy.do?goodId='+goodId+'&isFromBack=true&promForbidBuyId='+promForbidBuyId;
            }
        }
    }

    //限制只能为数字
	function RepNumber(obj) {
		var reg = /^[\d]+$/g;
		if (!reg.test(obj.value)) {
			var txt = obj.value;
			txt.replace(/[^0-9]+/, function (char, index, val) {//匹配第一次非数字字符
			obj.value = val.replace(/\D/g, "");//将非数字字符替换成""
			var rtextRange = null;
		if (obj.setSelectionRange) {
			obj.setSelectionRange(index, index);
		} else {//支持ie
			rtextRange = obj.createTextRange();
			rtextRange.moveStart('character', index);
			rtextRange.collapse(true);
			rtextRange.select();
		}
		})}
	}

</script>