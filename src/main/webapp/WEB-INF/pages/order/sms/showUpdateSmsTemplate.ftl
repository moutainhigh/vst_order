<form id="dataForm">
<input type="hidden" name="templateId" value="${ordSmsTemplate.templateId}" >
        <table class="p_table form-inline">
            <tbody>
                <tr>
                    <td class="s_label"><span class="notnull">*</span>产品类型：</td>
                    <td>
                     	<select name="categoryId" required=true disabled>
                     				<option value="">请选择</option>
                     				<option value="0" <#if ordSmsTemplate.categoryId = 0> selected</#if>>全部</option>
                     		<#list bizCategoryList as bizCategory> 
                    	 			<option value="${bizCategory.categoryId!''}" <#if ordSmsTemplate.categoryId ==bizCategory.categoryId> selected</#if>>${bizCategory.categoryName!''}</option>
                    	 	</#list>		
			        	</select>
			        </td>
			        <td class="s_label"><span class="notnull">*</span>发送节点：</td>
                    <td>
                     	<select name="sendNode" required=true disabled style="width:300px;">
                    	 			<option value="">请选择</option>
                    	 	<#list sendNodeList as sendNode>
                    				<option value="${sendNode.code!''}" <#if ordSmsTemplate.sendNode = sendNode.code>selected</#if>>${sendNode.cnName!''}</option>
                    		</#list>
			        	</select>
			        </td>
			     </tr>
			     <tr>
			     	<td class="s_label"><span class="notnull">*</span>规则名称：</td>
                    <td class="w18"><input type="text" name="templateName" value="${ordSmsTemplate.templateName}" required=true disabled></td>
                    <td class="s_label">规则类型：</td>
                    <td>
                    	<span>发送规则</span>
                    </td>
                 </tr>
			     <tr>
			        <td class="s_label">供应商：</td>  
		            <td>
		            <input type="text" errorEle="searchValidate" class="searchInput" name="supplierName" id="supplierName" value="${supplierName}" disabled>
                	<input type="hidden" value="${ordSmsTemplate.suplierId}" name="suplierId" id="suplierId" required=true>
                	</td>
                 </tr>
                 <tr>
                   <td class="s_label">支付对象：</td>
                   <td class="w18">
                     	<select name="distributorId" id="distributorId" disabled>
                     				<option value="">请选择</option>
                     				<option value="0" <#if ordSmsTemplate.distributorId =0> selected</#if>>全部</option>
                    				<option value="1" <#if ordSmsTemplate.distributorId =1> selected</#if>>现付</option>
                    				<option value="2" <#if ordSmsTemplate.distributorId =2> selected</#if>>预付</option>
			        	</select>
			       </td> 
			        <td class="s_label">下单时间：</td>
                    <td>
                     	<select name="orderTime" disabled>
                     				<option value="">请选择</option>
                     		<#list orderTimeList as orderTime>
                    				<option value="${orderTime.code!''}" <#if ordSmsTemplate.orderTime == orderTime.code>selected</#if>>${orderTime.cnName!''}</option>
                    		</#list>
			        	</select>
			        </td>
                </tr>
                <tr>
                 	<td class="s_label">短信内容</td>
	   					<td colspan="3">
	   					<textarea class="smsContent" name="content" maxlength="350" style="width:600px;height:100px">${ordSmsTemplate.content}</textarea>
	   				</td>
                </tr>
            </tbody>
        </table>
</form>
<button class="pbtn pbtn-small btn-ok" style="float:right;margin-top:20px;" id="save">保存</button>

<script>
vst_pet_util.commListSuggest("#supplierName", "input[name=suplierId]",'/vst_back/supp/supplier/searchSupplierList.do');

var disflag = false;

		$("#save").bind("click",function(){
		      
		    if(disflag)
		    {
		       return;
		    }
			//验证
			if(!$("#dataForm").validate({
		          }).form()){
				return;
			}
	
			$.ajax({
			url : "/vst_order/order/ordSmsTemplate/updateOrdSmsTemplate.do",
			type : "post",
			dataType:"json",
			async: false,
			data : $("#dataForm").serialize(),
			success : function(result) {
			   if(result.code=="success"){
			            disflag = true;
						$.alert(result.message,function(){
				   				updateDialog.close();
				   				window.location.href="/vst_order/order/ordSmsTemplate/findOrdSmsTemplateList.do?rule=Y";
		   			});
					}else {
						$.alert(result.message);
			   		}
			   }
			});						
		});
	
	
	// 在光标处插入字符串
$(function() {  
   (function($) {  
       $.fn.extend({  
                   insertContent : function(myValue, t) {  
                       var $t = $(this)[0];  
                       if (document.selection) { // ie   
                           this.focus();  
                            var sel = document.selection.createRange();  
                            sel.text = myValue;  
                            this.focus();  
                            sel.moveStart('character', -l);  
                            var wee = sel.text.length;  
                            if (arguments.length == 2) {  
                                var l = $t.value.length;  
                                sel.moveEnd("character", wee + t);  
                                t <= 0 ? sel.moveStart("character", wee - 2 * t  
                                        - myValue.length) : sel.moveStart(  
                                        "character", wee - t - myValue.length);  
                                sel.select();  
                            }  
                        } else if ($t.selectionStart  
                                || $t.selectionStart == '0') {  
                            var startPos = $t.selectionStart;  
                            var endPos = $t.selectionEnd;  
                            var scrollTop = $t.scrollTop;  
                            $t.value = $t.value.substring(0, startPos)  
                                    + myValue  
                                    + $t.value.substring(endPos,  
                                            $t.value.length);  
                            this.focus();  
                            $t.selectionStart = startPos + myValue.length;  
                            $t.selectionEnd = startPos + myValue.length;  
                            $t.scrollTop = scrollTop;  
                            if (arguments.length == 2) {  
                                $t.setSelectionRange(startPos - t,  
                                        $t.selectionEnd + t);  
                                this.focus();  
                            }  
                        } else {  
                            this.value += myValue;  
                            this.focus();  
                        }  
                    }  
                })  
    })(jQuery);   
});
function insert()
{
    $(".smsContent").insertContent($("#choose").val()); 
}
			
</script>
