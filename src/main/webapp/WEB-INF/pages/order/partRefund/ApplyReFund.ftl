<!DOCTYPE html>
<html>
<head>

<#include "/base/head_meta.ftl"/>
</head>
<body>
<div class="iframe_header">
        <ul class="iframe_nav">
            <li>订单管理&gt;</li>
            <li class="active">订单处理&gt;</li>
            <li class="active">退款申请</li>
        </ul>
</div>
<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/partRefundBackAction/toPartRefundPage.do" method="post" onsubmit="return false">
		<input type="hidden" id="orderId" name="orderId" value="${orderId}" />
		<#------------------------退款明细 ----------------------------->
	<#if refundDetailList?? && refundDetailList?size &gt;0 > 
		<div id="refundDetailDiv">
		<font style="font-weight:bold;">退款明细</font><br/><br/>
		<hr style="borber:1px gray solid;" /><br>
    		<table class="p_table table_center" id="refundDetailID">
                <tbody>
                   <thead>
	                	<th width="10%">操作账号</th>
	                	<th width="10%">申请时间</th>
	                	<th width="10%">子订单号</th>
	                    <th width="20%">包含商品</th>
	                    <th>预订份数</th>
	                    <th>申请份数</th>
	                    <th>退款状态</th>
	                    <th>日志</th>
                   </thead>
                  		 
					 <#list refundDetailList as detailItem> 
						<tr>
							<td>${detailItem.operator}</td>
							<td>
								<#if detailItem.createTime??>
									${detailItem.createTime?string("yyyy-MM-dd HH:mm:ss")}
								</#if>
							</td>
							<td>${detailItem.orderItemId}</td>
							<td>【${detailItem.suppGoodsId}】${detailItem.goodsName}</td>
							<td>${detailItem.quantity}</td>
							<td>${detailItem.refundQuantity}</td>
							<td>${detailItem.itemAuditStatusStr}</td>
							<td>
								<a class="showRefundLogDialog" href="javascript:void(0);" param="${detailItem.id}" tag="${detailItem.orderItemId}">查看</a>
							</td>
						</tr>
					</#list> 
                </tbody>
            </table> 
       </div> 
    </#if> 
        
        <#------------------------退款申请 ----------------------------->
        <br/>
        <div id="refundApplyData">
       	 <font style="font-weight:bold;">退款申请</font><br/><br/>
		<hr style="borber:1px gray solid;" /><br>
    		<table class="p_table table_center" id="refundApplyDataID">
                <tbody>
                   <thead>
                    <th width="10%"><input type="checkbox" name="cks" class="w6"  onclick="addAll(this);"/>子订单号</th>
                	<th width="20%">商品名称</th>
                	<th width="20%">退款类型</th>
                	<th width="10%">退款份数/人员</th>
                    <th width="40%">说明</th>
                </thead>
                <#if refundItemList ?? && refundItemList?size gt 0>
			 	<#list refundItemList as item> 
				 	<#if item.refundQuantity == 0 || item.refundType == 'UNRETREATANDCHANGE' || !item.canRefund>
						<tr style="background:#CCCCCC;">
					<#else>
						<tr>
					</#if>
							<td>

								<input type="checkbox" name="refundItemId" value="${item.orderItemId}" <#if item.refundQuantity == 0 || item.refundType == 'UNRETREATANDCHANGE' || !item.canRefund> disabled="true" </#if> class="w6" onclick='javascript:changeQuantity()'/>${item.orderItemId} 
	
								<input type="hidden" id="suppGoodsId_${item.orderItemId}" value="${item.suppGoodsId}" />
								<input type="hidden" id="suppGoodsName_${item.orderItemId}" value="${item.suppGoodsName}" />
								<input type="hidden" id="child_${item.orderItemId}" value="${item.child}" />
								<input type="hidden" id="adult_${item.orderItemId}" value="${item.adult}" />
								<input type="hidden" id="canRefund_${item.orderItemId}" value="${item.canRefund}" />
								<input type="hidden" id="refundType_${item.orderItemId}" value="${item.refundType}" />
								<input type="hidden" id="remainQuantity_${item.orderItemId}" value="${item.refundQuantity}" />
								<input type="hidden" id="quantity_${item.orderItemId}" value="${item.quantity}" />
								<input type="hidden" id="renfundWay_${item.orderItemId}" value="${item.renfundWay}" />
								<input type="hidden" id="categoryId_${item.orderItemId}" value="${item.categoryId}" />
								<input type="hidden" id="memo_${item.orderItemId}" value="${item.memo}" />
								
							</td>
							<td>
							【${item.suppGoodsId}】
								${item.suppGoodsName}
								<#if item.refundPersons?? && item.refundPersons?size&gt;0 >
									<#if item.categoryId?? && item.categoryId != 3>
										【儿童${item.child}人/成人${item.adult}人】
									</#if>
								</#if>
							</td>
							<td>
								<#if item.refundType?? && item.refundType == 'RETREATANDCHANGE'>
									<#if item.categoryId?? && item.categoryId == 3>
										可退
									<#else>
										整单退
									</#if>
								<#elseif item.refundType?? && item.refundType == 'UNRETREATANDCHANGE'>
									不可退
								<#elseif item.refundType?? && item.refundType == 'PARTRETREATANDCHANGE'>
									部分退
								<#else>
									人工退改
								</#if>
								
							</td>
							<td>
								<#if item.refundPersons?? && item.refundPersons?size&gt;0 >
									<#list item.refundPersons as itemPerson>
										<input type="checkbox" id="ordPersonId_${item.orderItemId}_${itemPerson.ordPersonId}" name="ordPersonId_${item.orderItemId}" value="${itemPerson.ordPersonId}" onclick='javascript:changeQuantity()'/>${itemPerson.fullName}</br>
									</#list>
								<#else>
									<select id="refundQuantity_${item.orderItemId}" name="refundQuantity_${item.orderItemId}" style="width:50px;" <#if item.refundType?? && item.refundType == 'RETREATANDCHANGE'> disabled="disabled" </#if> onchange='javascript:changeQuantity()';>
										<#if item.refundQuantity??>
										<#list 0..item.refundQuantity as i>
											<option value="${i}" <#if i==item.refundQuantity>selected</#if> >${i}</option>
										</#list>
										</#if>
									</select>
								</#if>
							</td>
							<td>
								<#if item.refundQuantity == 0>
									不支持申请，原因：无剩余可退份数
								<#elseif item.refundType == 'UNRETREATANDCHANGE'>
									不支持申请，原因：商品不可退
								<#else>
									${item.memo}
								</#if>
							</td>
						</tr>
			 		</#list>
			 		</#if>
                </tbody>
            </table> 
       </div> 
       
       <br/><br/>
       <#------------------------退款金额计算 ----------------------------->
       <div id="reFundMoneyCount">
       		<table width="100%" border="1" style="border:1px solid gray;text-align:center;" id="reFundMoneyCountID">
       			<tr>
	                <td style="background:#7bb7f2;color:#ffffff;height:50px;width:300px;">退款金额</td>
	            	<td style="text-align:left;">
	            		<div>
		            		&nbsp;￥<input type="number" id="refundAmountText" onkeyup="noAmountCheck()"/>
		            		&nbsp;&nbsp;<a id="countMoney" class="btn btn_cc1">&nbsp;计算&nbsp;</a>
	            			&nbsp;<a id="seachCountRule" href="javascript:void(0);" style="color:#06c;">查看计算规则</a>
	            		</div>
	            	</td>
				</tr>
				<tr>
					<td style="background:#7bb7f2;color:#ffffff;width:300px;">
						备注说明
					</td>
					<td style="text-align:left;height:120px">&nbsp;<textarea cols="50" style="width:90%;height:80px;" rows="2" id="reason" name="reason"></textarea></td>
				</tr>
            </table> 
            <br/><br/>
		   	<input type = "hidden" id="hasRefundApplyProcessing" value="${hasRefundApplyProcessing?string("true","false")}"/>
            <#if !hasRefundApplyProcessing>
            	<div style="width:100%;text-align:center;" id="applyRefundBlock"><a id="applyRefund" class="btn btn_cc1">&nbsp;&nbsp;申请退款&nbsp;&nbsp;</a></div>
            <#else>
            	<div style="width:100%;text-align:center;" id="applyRefundBlock"><button id="cantApply" class="btn btn_cc1" disabled="true">&nbsp;&nbsp;申请退款&nbsp;&nbsp;</button><span style="color:red">&nbsp;&nbsp;*含处理中的售后单</span></div>
            </#if>
       </div>
       <#------------------------退款金额计算过程展示内容 ----------------------------->
        <div id="refundFormulaDetailsDiv" style="display: none">
        </div>
	</form>
</div>

<#include "/base/foot.ftl"/>
</div>
</body>
</html>
<script>
//全选
function addAll(obj){
	if(obj.checked){
		$("input[name='refundItemId']").attr("checked",true);
		$("input[disabled=true]").attr("checked",false);
	}else{
	  $("input[name='refundItemId']").attr("checked",false);
	}
}

//查看日志
$("a.showRefundLogDialog").bind("click",function(){
	  var batchIdval = $(this).attr("param");
	  var ordItemIdval = $(this).attr("tag");
	  var param=""
	  if(batchIdval!="" && ordItemIdval!=""){
	      param="?batchId="+batchIdval+"&ordItemId="+ordItemIdval
	  }else{
	       return false;
	  }
      new xDialog("/vst_order/order/orderManage/queryRefundDetailInfo.do"+param,{},{title:"退款日志",iframe:true,width:1000,hight:300,iframeHeight:680,scrolling:"yes"});
})
function appendRefundRules(result){
    if(null!=result.attributes && result.attributes!=""){
        var obj = result.attributes; 
        var refundFormulaList= obj["refundFormulaDetails"];
        if(null!=refundFormulaList){
         var   tablehtml="<table width='100%' border='1' style='border:1px solid gray;text-align:center;table-layout:fixed;' id='refundFormulaTable'>";
       
         for(var refundFormula in refundFormulaList){
                if(" "==refundFormulaList[refundFormula]){
                     tablehtml+="<tr><td style='word-wrap:break-word;font-weight: bold;height: 20px;'>"+refundFormulaList[refundFormula]+"</td></tr>";
                }else{
                  tablehtml+="<tr><td style='word-wrap:break-word;font-weight: bold;'>"+refundFormulaList[refundFormula]+"</td></tr>";
                }
             
           }
        }
        tablehtml+="</table>"
         $("#refundFormulaDetailsDiv").append(tablehtml);
    }else if(null!=result.message){
      var  tablehtml="<table width='100%' border='1' style='border:1px solid gray;text-align:center;table-layout:fixed;' id='refundFormulaTable'>";
      tablehtml+="<tr><td style='word-wrap:break-word;font-weight: bold;'>"+result.message+"</td></tr>";
      tablehtml+="</table>"
       $("#refundFormulaDetailsDiv").append(tablehtml);
    }
}
function noAmountCheck(){
	var hasRefundApplyProcessing = $("#hasRefundApplyProcessing").val();
	if(hasRefundApplyProcessing == "false"){
		var calMoney=Number($("#refundAmountText").val());
		if(calMoney <= 0){
			$("#applyRefundBlock").html('<button id="cantApply" class="btn btn_cc1" disabled="true">&nbsp;&nbsp;申请退款&nbsp;&nbsp;</button><span style="color:red">&nbsp;&nbsp;*退款金额小于等于0</span>');
		} else {
			$("#applyRefundBlock").html('<a id="applyRefund" class="btn btn_cc1">&nbsp;&nbsp;申请退款&nbsp;&nbsp;</a>');
		}
    }
}
var refundItemInfoJson = '';
//计算金额
$("#countMoney").click(function(){
    $("#refundFormulaDetailsDiv").html('');
    $("#refundAmountText").val("");
	if(!getJsonString()){
		return;
	}
	$.ajax({
		url : "/vst_order/partRefundBackAction/calculationAmount.do",
		type : "POST",
		dataType : "JSON",
		data : {"refundItemInfoJson" : refundItemInfoJson,"orderId" : $("#orderId").val()},
		success : function(result){
			if(result.code == 'success'){
				$("#refundAmountText").val(result.message);
				var calMoney=Number($("#refundAmountText").val());
				if(calMoney <= 0){
					$("#applyRefundBlock").html('<button id="cantApply" class="btn btn_cc1" disabled="true">&nbsp;&nbsp;申请退款&nbsp;&nbsp;</button><span style="color:red">&nbsp;&nbsp;*退款金额小于等于0</span>');
				}
				appendRefundRules(result);
			}else{
			    appendRefundRules(result);
				alert(result.message);
			}
		}
	});
});

$("#seachCountRule").click(function(){
     pandora.dialog({title:"计算详情",height:400, width:600,content:$("#refundFormulaDetailsDiv").html()});
});
$("#applyRefund").live("click",function(){
	var applyReFundButtonCheck = "${applyReFundButtonCheck}";
	if(applyReFundButtonCheck != 'success'){
		$.alert('不支持申请，原因：' + applyReFundButtonCheck);
		return;
	}
	var chk_choice = $('input:checkbox[name=refundItemId]:checked').length;
	if(chk_choice == 0){
		$.alert('请选择退款子订单!');
		return;
	}
	var refundAmount = $("#refundAmountText").val();
	if(refundAmount == ''){
		$.alert('请计算退款金额!');
		return;
	}
	$.confirm("是否确认提交退款申请？", function(){
		$("#applyRefundBlock").html('<button id="cantApply" class="btn btn_cc1" disabled="true">&nbsp;&nbsp;申请退款&nbsp;&nbsp;</button>');
		var reason = $("#reason").val();
		var orderId = $("#orderId").val();
		var param = {"refundItemInfoJson" : refundItemInfoJson,"refundAmount" : refundAmount,"reason" : reason,"orderId" : orderId};
		$.ajax({
			url : "/vst_order/partRefundBackAction/applyRefundOperate.do",
			type : "POST",
			dataType : "json",
			data : param,
			contentType: "application/x-www-form-urlencoded; charset=utf-8",
			success : function(result){
				$.confirm(result.message, function(){
					//退款份数不满足条件或者申请退款成功刷新页面
					if(result.code == 'IS_CHANGE_QUANTITY' || result.code == 'true'){
						window.location.href = '/vst_order/partRefundBackAction/toPartRefundPage.do?orderId='+orderId;
					}else if(result.code == 'IS_CHANGE_AMOUNT'){ //退款金额发生改变重新计算金额
						$("#refundAmountText").val('');
					}
				}, function(){
					//退款份数不满足条件或者申请退款成功刷新页面
					if(result.code == 'IS_CHANGE_QUANTITY' || result.code == 'true'){
						window.location.href = '/vst_order/partRefundBackAction/toPartRefundPage.do?orderId='+orderId;
					}else if(result.code == 'IS_CHANGE_AMOUNT'){ //退款金额发生改变重新计算金额
						$("#refundAmountText").val('');
					}
				});
			}
		});
	}, {});
});

//改变份数清空退款金额
function changeQuantity(){
	$("#refundFormulaDetailsDiv").html('');
    $("#refundAmountText").val("");
}

//封装参数
function getJsonString(){
	var dataArray = new Array();
	var chk_length = $('input:checkbox[name=refundItemId]:checked').length;
	if(chk_length == 0){
		$.alert('请选择退款子订单!');
		return false;
	}
	var allPass = true;
  $('input:checkbox[name=refundItemId]:checked').each(function(){
  		var refundItemId = $(this).val();
		var suppGoodsId = $("#suppGoodsId_"+refundItemId).val();//商品id
		var suppGoodsName = $("#suppGoodsName_"+refundItemId).val();//商品名称
		var child = $("#child_"+refundItemId).val();//商品包含儿童数
		var adult = $("#adult_"+refundItemId).val();//商品包含成人数
		var quantity = $("#quantity_"+refundItemId).val();//预定份数
		var refundQuantity = $("#refundQuantity_"+refundItemId).val(); //退款数
		var remainQuantity = $("#remainQuantity_"+refundItemId).val(); //剩余可退份数
		var refundType = $("#refundType_"+refundItemId).val(); //退改策略类型
		var renfundWay = $("#renfundWay_"+refundItemId).val(); //退款方式：按分数退还是按人退
		var categoryId = $("#categoryId_"+refundItemId).val(); //品类ID
		var memo = $("#memo_"+refundItemId).val();//备注
		
		//按人退时的需要退的人
		var person_length = $('input:checkbox[name=ordPersonId_'+refundItemId+']').length;
		var person_length_checked = $('input:checkbox[name=ordPersonId_'+refundItemId+']:checked').length;
		
		if(person_length > 0 && person_length_checked == 0){
			$.alert('请选择要退款的游客！');
			allPass = false;
			return false;
		}else if(person_length > 0){
		    var refundQuantityMod;
			if(parseInt(categoryId) == 3){
			    refundQuantityMod=person_length_checked%1;
				refundQuantity = person_length_checked;
			}else{
			    refundQuantityMod=person_length_checked%(parseInt(child)+parseInt(adult));
				refundQuantity = person_length_checked/(parseInt(child)+parseInt(adult));
			}
			if(refundQuantityMod > 0){
				$.alert('如果按人退，退款份数必须是人的整数倍!');
				allPass = false;
				return false;
			}else if(refundQuantity > parseInt(remainQuantity)){
				$.alert('勾选游玩人数量超出剩余可退数量!');
				allPass = false;
				return false;
			}
		}
		
		var OrdRefundItemInfo = new Object();
		OrdRefundItemInfo.orderItemId = refundItemId;
		OrdRefundItemInfo.suppGoodsId = suppGoodsId;
		OrdRefundItemInfo.suppGoodsName = suppGoodsName;
		OrdRefundItemInfo.child = child;
		OrdRefundItemInfo.adult = adult;
		OrdRefundItemInfo.quantity = quantity;
		OrdRefundItemInfo.refundQuantity = refundQuantity;
		OrdRefundItemInfo.refundType = refundType;
		OrdRefundItemInfo.renfundWay = renfundWay;
		OrdRefundItemInfo.categoryId = categoryId;
		OrdRefundItemInfo.memo = memo;
		
		var refundPersonsArray = new Array();
		$('input:checkbox[name=ordPersonId_'+refundItemId+']:checked').each(function(){
			var OrdPerson = new Object();
			OrdPerson.ordPersonId = this.value;
			refundPersonsArray.push(OrdPerson);
		});
		
		OrdRefundItemInfo.refundPersons = refundPersonsArray;
		dataArray.push(OrdRefundItemInfo);
  });
  
  	if(allPass == true){
	  	if(dataArray != null && dataArray.length > 0){
			refundItemInfoJson = JSON.stringify(dataArray);
			return true;
		}
	}
	return false;
}
</script>
