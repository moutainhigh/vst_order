//查看发票详情信息
var showInvoiceDetailog;
function showInvoiceDetail(invoiceId){
     showInvoiceDetailog = new xDialog("/vst_order/order/orderInvoice/ord/invoiceDetail.do",{"invoiceId":invoiceId},{title:"发票详情",width:950});
}

$(function(){
	//取消
	$("a.cancel").live("click",function(){
		if(!confirm("确定需要取消当前发票")){
			return false;
		}
		var $this=$(this);
		var result=$this.attr("result");
		var status="CANCEL";
		$.ajax({
			url : "/vst_order/order/orderInvoice/ord/invoiceChangeStatus.do",
			type : "post",
			dataType : 'json',
			data : {"status":status,"ordInvoiceId":result},
			success : function(result) {
//				$this.closest('td').parent().find('td:nth-child(3)').text("取消")
				window.location.reload();
			}
		});
	});
	
	/**确认红冲订单**/	
	$("a.confirmRed").live("click",function(){
		if(!confirm("确定需要将当前发票进行确认红冲操作？")){
			return false;
		}
		var $this=$(this);
		var result=$this.attr("result");
		var status = "RED";
		$.ajax({
			url : "/vst_order/order/orderInvoice/ord/invoiceChangeStatus.do",
			type : "post",
			dataType : 'json',
			data : {"status":status,ordInvoiceId:result},
			success : function(date) {
				window.location.reload();
			}
		});
	});

	//关闭
	$("a.closeRed").live("click",function(){
		if(!confirm("确定需要将当前发票进行关闭操作？")){
			return false;
		}
		var $this=$(this);
		var result=$this.attr("result");
		$.ajax({
			url : "/vst_order/order/orderInvoice/ord/doCloseRedInvoice.do",
			type : "post",
			dataType : 'json',
			data : {"ordInvoiceId":result},
			success : function(date) {
				window.location.reload();
			}
		});
	});
    //完成 ,审核
	$("button.change_curr_status").live("click",function(){
		if(!confirm("确定要变更当前状态")){
			return false;
		}
		var status=$(this).attr("ok_status");
		var invoiceId=$(this).attr("invoiceId");
		$.ajax({
			url : "/vst_order/order/orderInvoice/ord/invoiceChangeStatus.do",
			type : "post",
			dataType : 'json',
			data : {"status":status,"ordInvoiceId":invoiceId},
			success : function(result) {
				showInvoiceDetailog.reload();
			}
		});
	});
	
	/*全选*/
	$("#allsel").click(function(){
	    var $arr=$("input[name=allsel]:checked");
	    if($arr.size()!=0){
	        $("input[name=checkBoxInvoiceId]:checkbox").attr("checked",true);
	    }else{
	        $("input[name=checkBoxInvoiceId]:checkbox").attr("checked",false);
	    }
	});
	
	
	function getSelectedInvoiceList(){
		var $list=$("input[name=checkBoxInvoiceId].checks:checked");
		if($list.size()==0){
			throw "请先选中要操作的发票";
		}
		
		var invoice_ids="";
		var skip_flag=false;
		$.each($list,function(i,n){
			if(i>0){
				invoice_ids+=",";
			}
			var id=$(n).val();
			var status=$("#status_"+id).attr("status");
			if(status=='取消'||status=='已经开票'||status=='POST'||status=='完成'){
				throw "您选中的序号:"+id+"不可以变更到审核通过或已开票";				
			}
			invoice_ids+=id;
		});
		if($.trim(invoice_ids)===''){
			throw "请先选中要操作的发票";
		}
		return invoice_ids;
	}
	
	//已审核
	$("input.approve").click(function(){
		if(!confirm("确定要改成审核通过")){
			return false;
		}
		var amountYuan = $("input[name=amountYuan]").val();
		if(amountYuan<1){
			alert("发票金额低于1不能审核通过");
			return false;
		}
		var invoice_ids="";
		try{
			invoice_ids=getSelectedInvoiceList();			
		}catch(er){
			alert(er);
			return false;
		}
		$.ajax({
			url : "/vst_order/order/orderInvoice/ord/invoiceListApprove.do",
			type : "post",
			dataType : 'json',
			data : {"status":status,"invoices":invoice_ids},
			success : function(result) {
				var str="更新成功条数:"+result.attributes.length;
				if(result.attributes.length > 0){
					alert(str);
					window.location.reload();
				}else{
					alert(result.msg);
				}
				
			}
		});
	});
	
	//已开票
	$("input.bill_btn").click(function(){
		if(!confirm("确定要改成已开票")){
			return false;
		}
		var amountYuan = $("input[name=amountYuan]").val();
		if(amountYuan<1){
			alert("发票金额低于1不能开发票");
			return false;
		}
		var invoice_ids="";
		try{
			invoice_ids=getSelectedInvoiceList();			
		}catch(er){
			alert(er);
			return false;
		}
		$.ajax({
			url : "/vst_order/order/orderInvoice/ord/invoiceListBill.do",
			type : "post",
			dataType : 'json',
			data : {"invoices":invoice_ids},
			success : function(result) {
				var str="更新成功条数:"+result.attributes.length;
				if(result.attributes.length > 0){
					alert(str);
					window.location.reload();
				}else{
					alert(result.msg);
				}
			}
		});
	});
	
/**
 * 修改快递单号
 */
	$("input.express_input").live("click",function(){
		var $form=$("#expressForm");
		var invoiceId=$form.find("input[name=invoiceId]").val();
		if($.trim(invoiceId)==''){
			alert("信息不完整,重新刷新页面再操作");
			return false;
		}
		
		var expressNo=$form.find("input[name=expressNo]").val();
		if($.trim(expressNo)==''){
			alert("快递单号不可以为空！");
			return false;
		}
		
		$.ajax({
			url : "/vst_order/order/orderInvoice/ord/updateInvoiceExpress.do",
			type : "post",
			dataType : 'json',
			data : {"ordInvoiceId":invoiceId,"expressNo":expressNo},
			success : function(date) {
				showInvoiceDetailog.reload();
			}
		});
	});
	
	/**修改发票单号**/
	$("#updateInvoiceNo").live("click",function(){
		var invoiceId=$("input[name=invoiceId]").val();
		if($.trim(invoiceId)==''){
			alert("信息不完整，重新刷新页面再操作");
			return false;
		}
		var invoiceNo=$("input[name=invoiceNo]").val();
		if($.trim(invoiceNo)==''){
			alert("发票单号不可以为空");
			return false;
		}
		$.ajax({
			url : "/vst_order/order/orderInvoice/ord/updateInvoiceNo.do",
			type : "post",
			dataType : 'json',
			data : {"ordInvoiceId":invoiceId,"invoiceNo":invoiceNo},
			success : function(date) {
				showInvoiceDetailog.reload();
			}
		});
	});
	

	//导出信息
	$("input.export").click(function(){
		var res=$(this).attr("result");
		
		var url = "/vst_order/order/orderInvoice/ord/"+res+".do";
		$("#searchForm").attr("action",url);
	  	$("#searchForm").submit();
	  	
	  	var url = "/vst_order/order/orderInvoice/ord/invoiceList.do";
	  	$("#searchForm").attr("action",url);
	});
	
	//打印发票信息
	$("button.printInvoiceBtn").live("click",function(){
		var invoiceId=$(".printInvoiceBtn").attr("invoiceId");
		var title=$("#td_title").text();
		var price=$("#td_price").text();
		var detail=$("#td_detail").text();
		console.log(invoiceId);
		
		$.ajax({
			url : "/vst_order/order/orderInvoice/ord/issueInvoiceSingle.do",
			type : "post",
			dataType : 'json',
			data : {"invoiceId":invoiceId},
			success : function(result) {
				if(result.success == true)
					showInvoiceDetailog.reload();
                if(result.success == false)
                    window.alert(result.msg);
                    this.dialog.close();
					showInvoiceDetail(invoiceId);
			}
		});
		
		/*var invoiceActiveX=document.getElementById("invoiceActiveX");
		if(!invoiceActiveX){
			alert("打印控件没有安装");
			return false;
		}
		var res=invoiceActiveX.printInvoice(invoiceId,title,detail,price,"1","");		
		alert("操作完成，请查看发票号并自己添加");*/
	});
})