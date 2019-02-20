<div class="iframe_header"/>
<div class="iframe_search">
	<#list ebkFaxRecv.ebkFaxRecvItems as item> 
		<table border="0" cellpadding="0" cellpadding="0" style="width:1080px;">
			<tr>
				<td width="40%" height="350" style="border: 1px;">
					<table border="0" cellpadding="0" cellpadding="0"">
						<tr>
							<td>
								<#if fileType =="JPG">
									<a href="${ebkFaxRecv.fileUrl}"></a>
									<a href="${ebkFaxRecv.fullUrl}">下载回传件</a>
								<#elseif fileType=='PDF'>
									<object classid="clsid:CA8A9780-280D-11CF-A24D-444553540000"
										width="100%" height="100%" border="0">
										<param name="_Version" value="65539" />
										<param name="_ExtentX" value="20108" />
										<param name="_ExtentY" value="10866" />
										<param name="_StockProps" value="0" />
										<param name="SRC" value="${ebkFaxRecv.fileUrl}" />
										<embed src="${ebkFaxRecv.fileUrl}" width="100%" height="100%"></embed>
									</object>
									<a href="${ebkFaxRecv.fullUrl}">下载回传件</a>
								<#elseif fileType=='TIFF' || fileType=='TIF'>
									<span style="font-family:Arial;font-size:13px;font-weight:normal;font-style:normal;text-decoration:none;color:#333333;">
										<#if readUserId !=null>
											已查看,
										<#else>
											第1次查看,
										</#if>
										共${FaxRecvitemSize}页
									</span>
									<input type="button" onclick="rotates(-90,${item.recvItemId})" class="u10 btn btn-small" value="向左旋转90 ICON"/>
									<input type="button" onclick="rotates(90,${item.recvItemId})" class="u10 btn btn-small" value="向右旋转90 ICON"/>								
									<div id="selectImageDiv${item.recvItemId}" data="${item.recvItemId}" class="selectImageDiv" style="TEXT-ALIGN:center;max-width:524px;max-height:624px;overflow:scroll;">
										<img width='500' height='600' id='showimg${item.recvItemId}' src='${item.pageUrl}'/>
									</div>								
									<a href="${ebkFaxRecv.fullUrl}">下载回传件</a>
								<#else>
									未上传回传件
								</#if>					
							</td>
						</tr>
					</table>									
				</td>
				<td style="vertical-align: top;" width="50%" align="center">
					<input type="hidden" id="orderMemo" value="${orderMemo!''}">
					<form action="/vst_order/ord/order/confirm/updateFaxRecv.do" method="post" id="updateFaxRecvStatus${item.recvItemId}">
						<div id="relate_certificate_div">
							<div class="p_box">
								<table border="0" class="p_table table-center">
									<tr>
										<th>操作时间</th>
					                    <th>关联凭证</th>
					                    <th>操作人</th>
					                    <th>回传状态</th>
					                    <th>回传备注</th>
					                    <th>操作</th>
									</tr>
									<div id="faxRecvLink${item.recvItemId}">						
										<#list ebkFaxRecv.ebkFaxRecvLinks as link> 
											<tr>
												<td>
													<#if link.createTime??>${link.createTime?string('yyyy-MM-dd')}</#if>
												</td>
												<td>${link.certifId}</td>
												<td>
													${link.operator}
												</td>
												<td>
													<#if link.resultStatus=='INCONFIRM'>
														已审
													<#elseif link.resultStatus=='FULL'>
														订单满房
													<#elseif link.resultStatus=='PECULIAR_FULL'>
														特殊满房
													<#elseif link.resultStatus=='CHANGE_PRICE'>
														订单变价
													<#elseif link.resultStatus=='SUCCESS'>
														确认成功
													<#else>
													 	&nbsp;
													</#if>
												</td>
												<td>
													${link.ebkCertif.memo}
													<input type="hidden" name="ebkCertif.version" value="${link.ebkCertif.version}"/>
												</td>
												<td>
													<a href="javascript:void(0)" onclick="deleteFaxRecvLink(${link.linkId},${item.recvItemId})">删除&nbsp;</a>
													<a href="javascript:void(0)" onclick="loadUpdateData(${link.linkId},${item.recvItemId})">修改&nbsp;</a>
												</td>
										 	</tr>											
										</#list>
									</div>
								</table>
								<table class="p_table table-center" height="350">							
									<tr>
										<td class="p_label">凭证编号：</td>
										<td>
											<input type="text" id="callerId${item.recvItemId}" name="certifId" onblur="loadEbkFaxCertifInfo(${item.recvItemId},this.value);"/>												
										</td>
									</tr>								
									<tr>
										<td class="p_label">订单号：</td>
										<td>
											<div id="orderId${item.recvItemId}"></div>
											<input type="hidden" name="orderId" id="ord${item.recvItemId}"/>
											<input type="hidden" name="orderItemId" value="${orderItemId}"/>
										</td>
									</tr>
									<tr>
										<td class="p_label">备注：</td>
										<td>
											<input type="hidden" name="recvItemId" value="${item.recvItemId}"/>
											<input type="hidden" name="linkId" id="linkId${item.recvItemId}"/>
											<input type="hidden" name="recvId" value="${ebkFaxRecv.recvId}"/>
											<textarea rows="10" cols="30" id="memo${item.recvItemId}" name="orderMemo"></textarea>
										</td>
									</tr>
									<tr>
										<td class="p_label">回传状态：</td>
										<td>
											<select name="confirmStatus" class="w10" id="confirmStatus${item.recvItemId}">
												<option value="" selected="selected"></option>
												<option value="INCONFIRM">已审</option>
												<option value="FULL">订单满房</option>
												<option value="PECULIAR_FULL">特殊满房</option>
												<option value="CHANGE_PRICE">订单变价</option>
												<option value="SUCCESS">确认成功</option>
											</select>
										</td>
									</tr>
									<tr>
										<td class="p_label">&nbsp;</td>
										<td><input type="button" onclick="updateValidate(${item.recvItemId});" class="u10 btn btn-small" value="保存"></td>
									</tr>
								</table>
							</div>
						</div>
					</form>
				</td>
			</tr>
		</table>
	</#list>
</div>

<!-- //主要内容显示区域 -->
<script>

	$(function(){
		 var upcheck=1;
		 $('.selectImageDiv').each(function(){
	 		$(this).mousewheel(function(event, delta, deltaX, deltaY) {
			    if (delta > 0)
					upcheck=1;
			    else 
			    	upcheck=0;
				showimg("showimg"+$(this).attr("data"));
			    event.stopPropagation();
			    event.preventDefault();
			});
		 });
		function getStyleValue(objname,stylename){
		    if(objname.currentStyle){
		        return objname.currentStyle[stylename]
		    }else if(window.getComputedStyle){
		        stylename = stylename.replace(/([A-Z])/g, "-$1").toLowerCase();  
		        return window.getComputedStyle(objname, null).getPropertyValue(stylename);  
		    }
		}
		function showimg(id){
		    var tmpobj = document.getElementById(id);
		    var width = parseInt(getStyleValue(tmpobj,'width'));
		    var height = parseInt(getStyleValue(tmpobj,'height'));
		    var i = width/height;
		    if(upcheck){
		        if(width<=1024){
		            tmpobj.style.height = (height + 30) + 'px';
		            tmpobj.style.width = (width + 30*i) + 'px';
		        }
		    }else{
		        if(width>=35){
		            tmpobj.style.height = (height - 30) + 'px';
		            tmpobj.style.width = (width - 30*i) + 'px';
		        }
		    }
		}	
	});

	var init_angle=0;
	function rotates(num,recvItemId){
		init_angle=(init_angle+num)%360;
		$("#selectImageDiv"+recvItemId+">*").rotate(init_angle,1);
	}
	
	// 删除EBK传真回传关联信息
	function deleteFaxRecvLink(linkId,recvItemId){
		$.confirm("确认删除吗 ？", function () {
			$.ajax({
				url : "/vst_certif/ebooking/faxRecv/delEbkFaxRecvLink.do",
				type : "post",
				dataType:"json",
				async: false,
				data : {"linkId":linkId},
				success : function(result) {
			  		window.location.reload();
				}
			});
		});		
	}
	
	// 加载EBK传真回传管理信息
	function loadUpdateData(linkId,recvItemId){
		$.ajax({
			url : "/vst_certif/ebooking/faxRecv/loadEbkFaxRecvLink.do",
			type : "post",
			dataType:"json",
			async: false,
			data : {"linkId":linkId},
			success : function(data) {
				$('#linkId'+recvItemId).val(linkId);
				$('#callerId'+recvItemId).val(data.certifId);
				$('#memo'+recvItemId).val($("#orderMemo").val());
				$('#orderId'+recvItemId).html(data.orderId);
				$('#ord'+recvItemId).val(data.orderId);
				var resultStatus = data.resultStatus;
				if(typeof(resultStatus) != "undefined" && (resultStatus=='INCONFIRM'||resultStatus=='FULL'||
								resultStatus=='PECULIAR_FULL'||resultStatus=='CHANGE_PRICE'||resultStatus=='SUCCESS')){
					$('#confirmStatus'+recvItemId).val(resultStatus);
				}
			}
		});
	}
	
	// 加载凭证信息
	function loadEbkFaxCertifInfo(recvItemId,certifId){
		var callerId = $('#callerId'+recvItemId).val();
		var reg = new RegExp("^[0-9]*$");
		if(callerId.length>0){
	   	   if(!reg.test(callerId)){
	   	     $('#callerId'+recvItemId).focus();
			 alert("凭证编号请填写数字！");
		     return false;
		   }
		}else{
			return false;
		}	
		$.ajax({
			url : "/vst_certif/ebooking/faxRecv/loadEbkFaxCertifInfo.do",
			type : "post",
			dataType:"json",
			async: false,
			data : {"certifId":certifId},
			success : function(data) {
				if(data.result=='success'){
					$('#memo'+recvItemId).val($("#orderMemo").val());
					$('#orderId'+recvItemId).html(data.orderId);
					$('#ord'+recvItemId).val(data.orderId);
				}else{
					$('#callerId'+recvItemId).focus();
					alert(data.result);
				}
			}
		});
	}
	
	// 修改校验
	function updateValidate(recvItemId){
		var callerId = $('#callerId'+recvItemId).val();
		var reg = new RegExp("^[0-9]*$");
		var confirmStatus = $('#confirmStatus'+recvItemId).val();
		if(callerId.length>0){
	   	   if(!reg.test(callerId)){
	   	     $('#callerId'+recvItemId).focus();
			 alert("凭证编号请填写数字！");
		     return false;
		   }
			var ord = $('#ord'+recvItemId).val();
			if(ord.length<=0){
				alert("订单号必须存在！");
				return false;				
			}
			if(typeof(confirmStatus) == "undefined"|| confirmStatus==""){
				alert("回传状态不能为空！");
				return false;	
			}
			 
			$.ajax({
				url : "/vst_order/ord/order/confirm/updateFaxRecv.do",
				type : "post",
				dataType:"json",
				async: false,					
				data : $("#updateFaxRecvStatus"+recvItemId).serialize(),
				success : function(data) {
					if(data.code=='success'){
						window.location.reload();
					}else{
						alert(data.message);
					}
				}
			});	
		}else{
			$('#callerId'+recvItemId).focus();
			alert('凭证编号不能为空!!');
		}
	}
</script>
