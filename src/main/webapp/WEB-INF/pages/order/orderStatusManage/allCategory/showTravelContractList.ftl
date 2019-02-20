
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-合同管理</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>
    <div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="javaScript:">首页</a>：</li>
            <li><a href="javaScript:">订单管理</a> ></li>
            <li class="active">合同管理</li>
        </ul>
    </div>
<#--结果显示-->
<#if 1==1>
		 <div class="p_box">
   			 	<table class="p_table table_center">
               		<thead>
                		<tr>
                			<th width="4%">序号</th>
                            <th width="8%">合同编号</th>
                            <th width="10%">合同类型</th>
                            <th width="5%">合同状态</th>
                            <th width="5%">签约方式</th>
                            <th width="5%">联系人</th>
                            <th width="8%">电子邮箱</th>
                            <th width="8%">是否已发邮件</th>
                            <th width="6%">子订单号</th>
                            <th width="10%">商品名称</th>
                            <th width="6%">游玩时间</th>
                            <th width="6%">提交状态</th>
                            <th width="18%">操作栏</th>
                        </tr>
                     </thead>
   					 <tbody>
   					 	<#list travelContractVoList as travelContractVo>
                        	 <tr>
                                 <td>${travelContractVo_index+1}</td>
                                 <td>${travelContractVo.version}</td>
                                 <td>${travelContractVo.contractTemplateName}</td>
                                 <td>${travelContractVo.statusName}</td>
                                 <td>${travelContractVo.signingTypeName}</td>
                                 <td>${contacts}</td>
                                 <td><#if email ??>${email}<#else>/</#if></td>
                                 <td>
                                 	<#if travelContractVo.sendEmailFlag?? && travelContractVo.sendEmailFlag=='Y'>已发送<#else>未发送</#if>	
                                 </td>
                                 <td>
                                 	<table style="width:100%;border: none;border-collapse: collapse;">
	                                 	<#list travelContractVo.orderItemList as orderItem>
		                                 	<tr>
		                                 		<td style="border: none;">${orderItem.orderItemId}</td>
		                                 	</tr>
	                                 	</#list>
                                 	</table>
                                 </td>
                                 <td style="border-collapse: collapse;margin:0px;padding:0px;">
                                 	<table style="width:100%;border: none;border-collapse: collapse;">
	                                 	<#list travelContractVo.orderItemList as orderItem>
		                                 	<tr>
		                                 		<td style="border: none;">${orderItem.productName}</td>
		                                 	</tr>
	                                 	</#list>
                                 	</table>
                                 </td>
                                 <td style="border-collapse: collapse;margin:0px;padding:0px;">
                                 	<table style="width:100%;border: none;">
	                                 	<#list travelContractVo.orderItemList as orderItem>
			                                 <tr>
			                                 	<td style="border: none;">${orderItem.visitTime?string('yyyy-MM-dd')}</td>
			                                 </tr>
	                                 	</#list>
                                 	</table>
                                 </td>
                               	 <td>${travelContractVo.syncStatusName}</td>
                                 <td style="border-collapse: collapse;margin:0px;padding:0px;">
	                                 	<table style="width:100%;border: none;border-collapse: collapse;">
			                                 <tr>
			                                 	<td style="border: none;">
			                                 		<#if (travelContractVo.contractTemplate == 'FINANCE_CONTRACT' && order.categoryId == 33)>
				                                 		<a href="javascript:void(0);" style="color:#999999">签约方式</a>		                                 		
				                                 	<#else>
				                                 		<a onclick="changeSignType('${travelContractVo.ordContractId}')" href="javascript:retrun false;">
				                                 			签约方式
				                                 		</a>
				                                 	</#if>
			                                 	</td>
			                                 	<td style="border: none;">
			                                 		<a onclick="sendContractEmail('${travelContractVo.orderId}','${travelContractVo.ordContractId}')" href="javascript:retrun false;">
			                                 			<#if travelContractVo.status=='UNSIGNED'>发送合同<#else>重发合同</#if>
			                                 		</a>
			                                 	</td>
			                                 	
			                                 	<#if travelContractVo.contractTemplate=='FINANCE_CONTRACT'>
			                                 		<td style="border: none;">
			                                 			<a onclick="updateTravelContract('${travelContractVo.orderId}','${travelContractVo.ordContractId}')" href="javascript:retrun false;">修改合同</a>
			                                 		</td>
			                                 	<#else>
				                                 		<td style="border: none;">
						                                 	<#if travelContractVo.contractTemplate=='TEAM_OUTBOUND_TOURISM' 
						                                 		|| travelContractVo.contractTemplate=='TEAM_WITHIN_TERRITORY' 
						                                 		|| travelContractVo.contractTemplate=='DONGGANG_ZHEJIANG_CONTRACT'>
						                                 		<a target="_blank" href="/vst_order/order/orderManage/showUpdateTravelContract.do?ordContractId=${travelContractVo.ordContractId}&orderId=${travelContractVo.orderId}">修改合同</a>
						                                 	<#else>
						                                 		<a target="_blank" href="/vst_order/order/orderManage/showUpdateTravelContract.do?ordContractId=${travelContractVo.ordContractId}&orderId=${travelContractVo.orderId}">修改合同</a>
						                                 	</#if>
					                                 	</td>
				                                </#if>
			                                 	
			                                 	
			                                 </tr>
			                                 <tr>
			                                 	<td style="border: none;">
			                                 		<a target="_blank" href="/vst_back/pet/ajax/file/downLoad.do?fileId=${travelContractVo.fileId}">下载合同</a>
			                                 	</td>
			                                 	<td style="border: none;">
				                                 	<#if travelContractVo.contractTemplate !='COMMISSIONED_SERVICE_AGREEMENT' 
				                                 		&& travelContractVo.contractTemplate !='PREPAYMENTS'  
				                                 		&& travelContractVo.contractTemplate !='BEIJING_DAY_TOUR' 
				                                 		&& travelContractVo.contractTemplate !='PRESALE_AGREEMENT'
														|| (travelContractVo.contractTemplate == 'COMMISSIONED_SERVICE_AGREEMENT' && order.categoryId == 15)>
				                                 		<#if travelContractVo.additionFileId?? && travelContractVo.additionFileId?length gt 0>
				                                 			<a target="_blank" href="/vst_back/pet/ajax/file/downLoad.do?fileId=${travelContractVo.additionFileId}">下载行程单</a>
				                                 		<#else>
				                                 			<a href="javascript:void(0);" style="color:#999999">下载行程单</a>
				                                 		</#if>				                                 		
				                                 	<#else>
				                                 		<a href="javascript:void(0);" style="color:#999999">下载行程单</a>
				                                 	</#if>
			                                 	</td>
			                                 	<td style="border: none;">
				                                 	<#if travelContractVo.contractTemplate !='COMMISSIONED_SERVICE_AGREEMENT' 
					                                 	&& travelContractVo.contractTemplate !='PREPAYMENTS'  
					                                 	&& travelContractVo.contractTemplate !='BEIJING_DAY_TOUR'>
					                                 	<#if travelContractVo.attachementUrl??>
					                                 		<a target="_blank" href="/vst_back/pet/ajax/file/downLoad.do?fileId=${travelContractVo.attachementFileId}">下载补充条款</a>
					                                 	<#else>
					                                 		<a href="javascript:void(0);" style="color:#999999">下载补充条款</a>
					                                 	</#if>
				                                 	<#else>
				                                 		<a href="javascript:void(0);" style="color:#999999">下载补充条款</a>
				                                 	</#if>
			                                 	</td>
			                                 </tr>
			                                 <tr>
			                                 	<td style="border: none;">
			                                 		<#if (travelContractVo.contractTemplate == 'FINANCE_CONTRACT' && order.categoryId == 33)>
				                                 		<a href="javascript:void(0);" style="color:#999999">上传附件</a>		                                 		
				                                 	<#else>
				                                 		<a id="uploadOrderAttachment" onclick="uploadFile('${travelContractVo.orderId}')" href="javaScript:" title="上传附件">上传附件</a>
				                                 	</#if>
			                                 	</td>
			                                 	<td style="border: none;">
			                                 		<a href="javascript:void(0);" class="showLogDialog" param='objectId=${travelContractVo.ordContractId}&objectType=ORD_ORDER_ECONTRACT&sysName=VST'>
			                                 			查看日志
			                                 		</a>
			                                 	</td>
			                                 	<td style="border: none;"></td>
			                                 </tr>
	                                 </table>
                                 </td>
                            </tr>
                          </#list> 
                        </tbody>	
                    </table>
		</div>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关合同数据！</div>
    </#if>
    <#include "/base/foot.ftl"/>
 </body>
 
 <script>
	 //定义上传附件弹出窗口变量
	 var uploadOrderAttachmentDialog;
	  var orderType="";
	 function changeSignType(id){
	 	saveOrUpdateDialog = new xDialog("/vst_order/order/orderManage/toChangeContractSignTypePage.do?contractId="+id,{},{title:"签约方式",width:200,height:200});
	 }
 	
 	function sendContractEmail(orderId,contractId){
 	
 		if(confirm("确认发送合同吗?")){
	 		$.ajax({
					url : "/vst_order/order/orderManage/sendContractEmail.do?contractId="+contractId+"&orderId="+orderId,
					type : "get",
					dataType : 'json',
					success : function(result) {
						if(result.code=="success"){
							alert("发送成功");
							//saveOrUpdateDialog.close();
							//parent.location.reload();
							 location.reload(); 
						}else{
							alert("操作失败:"+result.message);
						}
					}
			});		
	 	}
 	
 	}
 	
 	function updateTravelContract(orderId,contractId){
        //遮罩层
        var loading = top.pandora.loading("正在努力保存中...");

        $.ajax({
            url : "/vst_order/order/orderManage/updateTravelContract.do?ordContractId="+contractId+"&orderId="+orderId,
            data : null,
            type:"POST",
            dataType:"JSON",
            success : function(result){
                if(result.code=="success"){
                    loading.close();
                    alert(result.message);
                    location.reload(); 
                    //parent.window.location.reload();
                }else {
                    loading.close();
                    alert(result.message);
                }
            }
        });
 	
 	}
 	
 	//上传附件链接事件
	function uploadFile(orderId){
		data={"orderId":orderId};
		uploadOrderAttachmentDialog = new xDialog(
				"/vst_order/ord/order/intoUploadOrderAttachmentPage.do",//进入上传附件页面
				data,//传递订单ID
				{title:"上传订单普通附件",width:600}//设置弹出窗口样式
				);
	}
		
 </script>

 
 
 
 