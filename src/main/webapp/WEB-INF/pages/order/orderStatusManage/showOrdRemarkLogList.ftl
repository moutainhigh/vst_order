<!DOCTYPE html>
<html>
<head>
	<#include "/base/head_meta.ftl"/>
</head>
<body>  
	<div class="iframe-content"> 
		<form action="/ord/ordRemarkLog/saveOrdRemarkLog.do" method="post" id="ordRemarkLogForm">
				<input type="hidden" name="orderId" value="${orderId}" required>
				<table  border="0" cellspacing="0" cellpadding="0">
		            <tbody>
		                <tr>
		                    <td>
		                    	<textarea class="w35 textWidth" name="content" maxlength=4000 style="width: 700px; height: 75px;" required></textarea>
		                    </td>
		                </tr>
		                <tr>
			            	<td>
			            		<div class="operate mt10">
				            		<a class="btn btn_cc1" id="btSaveordRemarkLog" href="javascript:void(0);">添加备注</a>
                    			</div>
			            	</td>
		                </tr>
		            </tbody>
		        </table>
		</form>
		<br/>
		<div id="listDiv">
			<#include "/order/orderStatusManage/ordRemarkLogList.ftl"/>		
		</div>
	</div>
	<#include "/base/foot.ftl"/>
	<script>
		var vst_util = {
				/**
				 * 判断输入字符长度
				 * @param id 保存ID的控件，一般是个隐藏域
				 */
				countLenth : function(id) {
					var realLength = 0;
				    var len = id.val().length;
				    var charCode = -1;
				    var maxlen = 0;
				    for(var i = 0; i < len; i++){
				    	if(realLength<=id.attr("maxlength")){
					        charCode = $(id).val().charCodeAt(i);
		                    if (charCode == 10) {
		                        //windows的换行符是两个byte
		                        realLength += 2;
		                    } else if (charCode >= 0 && charCode <= 128) {
					            realLength += 1;
					        }else{ 
					            // 如果是中文则长度加3
					            realLength += 2;
					        }
				        }
				    	if(realLength<=id.attr("maxlength")){
				    		maxlen = maxlen+1;
				    	}
				    } 
					var wordsLenth = id.attr("maxlength") - realLength;
					id.siblings("#textWidthTip").remove();
					if(wordsLenth<0){
						id.val(id.val().substring(0,maxlen));
						wordsLenth=0;
					}
					id.after("<span id = 'textWidthTip'>还能输入" + parseInt(wordsLenth/2) + "个汉字或者"+wordsLenth+"个字母</span>");
				}
		};	
		$(function(){
			$(".textWidth[maxlength]").each(function(){
					var	maxlen = $(this).attr("maxlength");
					if(maxlen != null && maxlen != ''){
						var l = maxlen*12;
						if(l >= 700) {
							l = 700;
						} else if (l <= 200){
							l = 200;
						} else {
							l = 400;
						}
						$(this).width(l);
					}
				$(this).keyup(function() {
					vst_util.countLenth($(this));
				});	
				$(this).mouseup(function() {
					vst_util.countLenth($(this));
				});			
			});
		}); 
		
		$("#btSaveordRemarkLog").click(function(){
				if(!$("#ordRemarkLogForm").validate().form()){
						return false;
				 }
				var loading = pandora.loading("正在努力保存中...");
				$.ajax({
						url : "/vst_order/ord/ordRemarkLog/saveOrdRemarkLog.do",
						type : "post",
						dataType : 'json',
						data : $("#ordRemarkLogForm").serialize(),
						success : function(result) {
							loading.close();
							if(!result.success){
						 		$.alert(result.message);
						 	}else{
						 		$("#listDiv").load('/vst_order/ord/ordRemarkLog/showOrdRemarkLogList.do?flag=Y&orderId=' + ${orderId},{},function(){
										$("#ordRemarkLogForm")[0].reset();
										$(window.parent.document).find(".dialog-body").find(".dialog-content").eq(0).height(300)
								});
						 	}
							
						}
					});
		});
	</script>
</body>
</html>