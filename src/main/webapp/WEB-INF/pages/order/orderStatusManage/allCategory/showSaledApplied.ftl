<head>
    <meta charset="UTF-8">
    <meta name="renderer" content="webkit">
    <title>售后申请确认</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/backstage/v1/vst/base.css,/styles/lv/icons.css,/styles/lv/tips.css" rel="stylesheet">
    <link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/dialog.css"/>
    <link rel="stylesheet" href="http://pic.lvmama.com/min/index.php?f=/styles/v5/modules/button.css"/>
    <link rel="stylesheet" href="http://pic.lvmama.com/styles/backstage/vst/round/v1/vst-backstage-product.css"/></head>
<body>
<div class="saleApplication">
	    <dl class="clearfix">
	        <dt>已收款：</dt>
	        <dd>RMB<span class="receivables">${actualAmount?string("0.00") }</span>元<span class="refund">退款：<b>${refundedAmount?string("0.00") }</b></span></dd>
	    </dl>
	    <dl class="clearfix">
	        <dt><span class="red">*</span>退款金额：</dt>
	        <dd><input type="text" id="refundAmount" name="refundAmount" class="input refundInput JS-refundInput"></dd>
	    </dl>
	    <dl class="clearfix">
	        <dt>手续费：</dt>
	        <dd><input type="text" id="serviceAmount" name="serviceAmount" class="input serviceCharge JS-serviceCharge" value="0"></dd>
	    </dl>
	    <dl class="clearfix">
	        <dt>备注：</dt>
	        <dd><textarea id="remark" name="remark"></textarea></dd>
	    </dl>
	    <dl class="clearfix">
	        <dt>&nbsp;</dt>
	        <dd>
	            <label>
	                <input type="checkbox" id="cancelOrder" name="cancelOrder" checked="checked" class="js-cancel-order">取消订单
	            </label>
	            <label>
	                <input type="checkbox" id="createOrder" name="createOrder" checked="checked" class="js-create-order">生成售后单
	            </label>
	        </dd>
	    </dl>
	    <input type="hidden" name="orderId" value="${orderId }" />
	    <input type="hidden" name="actualAmount" value="${actualAmount }" />
	    <input type="hidden" name="refundedAmount" value="${refundedAmount }" />
	    <input type="hidden" name="operation" value="${operation }" />
	    <input type="hidden" name="cancelCode" value="${cancelCode }" />
	    <input type="hidden" name="cancleReasonText" value="${cancleReasonText }" />
	    <input type="hidden" id="orderRemark" name="orderRemark" value="${orderRemark }" />
    <dl class="clearfix">
        <dt>&nbsp;</dt>
        <dd>
            <a class="btn btn-orange js-sure">确定</a><a class="btn js-cancel">取消</a>
        </dd>
    </dl>
</div>
</body>
<!--页面结束-->
<!--此处js要引入页面-->
<script src="http://pic.lvmama.com/min/index.php?f=/js/new_v/jquery-1.7.min.js"></script>
<script src="http://pic.lvmama.com/min/index.php?f=/js/v5/modules/pandora-dialog.js"></script>
<script>
	var saledAppliedDialog = parent.saledAppliedDialog;
	$(".js-cancel").bind("click", function() {
		closeDialog();
	});
	function closeDialog() {
		saledAppliedDialog.close();
	}
	$('.js-sure').click(function(){
		var refundAmount = $('#refundAmount').val();
		var serviceAmount = $('#serviceAmount').val();
		var remark = $('#remark').val();
		var $input=$('input[type="checkbox"]');
		var orderRemark = $('#orderRemark').val();
		orderRemark = encodeURIComponent(encodeURIComponent(orderRemark));
		var createOrder,cancelOrder;
        if($input.eq(0).attr('checked')){
        	cancelOrder = 1;
        }else{
        	cancelOrder = 0;
        }
        if($input.eq(1).attr('checked')){
        	createOrder = 1;
        }else{
        	createOrder = 0;
        }
		var param="orderId="+${orderId }+"&cancelCode="+${cancelCode }
				+"&cancleReasonText="+encodeURIComponent(encodeURIComponent("${cancleReasonText }"))
				+"&orderRemark="+orderRemark+"&remark="+encodeURIComponent(encodeURIComponent(remark))
				+"&refundAmount="+refundAmount+"&serviceAmount="+serviceAmount+"&cancelOrder="+cancelOrder
				+"&createOrder="+createOrder;
		$.ajax({
        	url : "/vst_order/order/orderManage/refundAmount.do?"+param,
            type : 'POST',
            async: false,
            success : function(result){
            	console.log(result);
            	if(result.success == true){
            		pandora.dialog({
            			content:'操作成功',
            			ok:true
            		}, function() {
            			parent.saledAppliedDialog.close();
            		})
            	}else{
            		pandora.dialog({
            			content:'操作失败',
            			ok:true
            		})
            	}
            	
            },
            error : function(){
            	pandora.dialog({
        			content:'系统出现异常。',
        			ok:true
        		})
            }
         });
	});
    $(function(){
        var $document=$(document);
        console.log(1111);
//        退款金额：默认=已收款金额
        $document.find('.JS-refundInput').val(parseFloat($('.receivables').html()) - parseFloat($('.refund b').html()));
        $document.find('.JS-refundInput').on('blur',function(){
            var $this=$(this);
            var val=parseFloat($this.val());
            var serviceval=parseFloat($('.JS-serviceCharge').val());
            var refundHtml=parseFloat($('.refund b').html());
            var receivables=parseFloat($('.receivables').html());
            if(val < 0){
                $('.js-sure').addClass('disabled');
                $this.parents('dl').addClass('error');
            }else {
                $this.parents('dl').removeClass('error');
                $('.js-sure').removeClass('disabled');
                if(val+serviceval+refundHtml<receivables){
                    $('.js-cancel-order').removeAttr('checked');
                    if(!$('.js-create-order').attr('checked'))
                    {
                        $('.js-sure').addClass('disabled');
                    }
                }else if(val+serviceval+refundHtml>receivables)
                {
                    $this.val(receivables-(serviceval+refundHtml));
                }else{
                    $('.js-cancel-order').attr('checked',true);
                }
            }
        });
        $document.find('.JS-serviceCharge').on('blur',function(){
            var $this=$(this);
            var val=parseFloat($this.val());
            var refundval=parseFloat($('.JS-refundInput').val());
            var refundHtml=parseFloat($('.refund b').html());
            var receivables=parseFloat($('.receivables').html());
            if(val<0){
             val=0;
             $this.val('0');
            }
            if(val+refundval+refundHtml<receivables){
                $('.js-cancel-order').removeAttr('checked');
                if(!$('.js-create-order').attr('checked'))
                {
                    $('.js-sure').addClass('disabled');
                }
            }else if(val+refundval+refundHtml>receivables)
            {
                $this.val(receivables-(refundval+refundHtml));
            }else{
                $('.js-cancel-order').attr('checked',true);
                $('.js-sure').removeClass('disabled');
            }

        });
        $('input[type="checkbox"]').change(function(){
            var $input=$('input[type="checkbox"]');
            if(!$input.eq(0).attr('checked')&&!$input.eq(1).attr('checked'))
            {
                $('.js-sure').addClass('disabled');
            }else{
                $('.js-sure').removeClass('disabled');
            }
        });
        $document.find('.js-sure:not(.disabled)').on('click',function(){
            var $refundInput=$('.JS-refundInput');
            if(!$refundInput.val()){
                $refundInput.parents('dl').addClass('error');
                $('.js-sure').addClass('disabled');
            }else {
            //	$.alert("这个类似原生方法 alert");
            }
        });
    })
</script>