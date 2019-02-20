/**
*
*订单附件业务JS包
*
*@author wenzhengtao
*
*/

//页面加载后给链接或者按钮绑定事件
 $(function () {
 
 
 	var data;
 	if(orderType=="parent")
 	{
 		data={"orderId":orderId,"orderItemId":0,orderType:orderType};
 	
 	}else if(orderType=="child"){
 		
 		data={"orderId":orderId,"orderItemId":orderItemId,orderType:orderType};
 	}else{
 		data={"orderId":orderId};//酒店订单处理页面
 	
 	}
	//上传附件链接事件
	$("#uploadOrderAttachment").bind("click",function(){
		uploadOrderAttachmentDialog = new xDialog(
				"/vst_order/ord/order/intoUploadOrderAttachmentPage.do",//进入上传附件页面
				data,//传递订单ID
				{title:"上传订单普通附件",width:600}//设置弹出窗口样式
				);
     });
	
	//查看附件链接事件
	$("#viewOrderAttachment").bind("click",function(){
		viewOrderAttachmentDialog = new xDialog(
				"/vst_order/ord/order/queryOrderAttachment.do",//进入上传附件页面
				data,//传递订单ID
				{title:"订单附件列表",width:900}//设置弹出窗口样式
				);
     });
	
	//查看子订单附件链接事件
	$(".viewChildOrderAttachment").bind("click",function(){
		var childData = {"orderId":orderId,"orderItemId":$(this).parent().parent().find("td").eq(1).text(),orderType:"child"};
		viewOrderAttachmentDialog = new xDialog(
				"/vst_order/ord/order/queryOrderAttachment.do",//进入上传附件页面
				childData,//传递订单ID
				{title:"订单附件列表",width:900}//设置弹出窗口样式
				);
     });
	
	//查看主订单附件链接事件
	$("#viewParentOrderAttachment").bind("click",function(){
		var parentData = {"orderId":orderId,"orderItemId":0,orderType:"parent"};
		viewOrderAttachmentDialog = new xDialog(
				"/vst_order/ord/order/queryOrderAttachment.do",//进入上传附件页面
				parentData,//传递订单ID
				{title:"订单附件列表",width:900}//设置弹出窗口样式
				);
     });
	
 });
