<!DOCTYPE html>
<html>
<head>

<#include "/base/head_meta.ftl"/>
</head>
<body>
<style>

td{
/*word-break:keep-all; 不换行 */
/*white-space:nowrap; 不换行 */
/*overflow:hidden; 内容超出宽度时隐藏超出部分的内容 */
/*text-overflow:ellipsis; 当对象内文本溢出时显示省略标记(...) ；需与overflow:hidden;一起使用。*/
}
</style> 
<div class="iframe-content">   
    <div class="p_box">
<table class="p_table table_center" style="word-break:break-all; word-wrap:break-all;">
    <thead>
        <tr>
              <TR>
              <th   class="w1"></th>
				<th nowrap="nowrap" style="width:5%;"  >序号</th>
				<th nowrap="nowrap" style="width:5%;" >创建人</th>
            	<th nowrap="nowrap" style="width:10%;" >分类</th>
				<th nowrap="nowrap" style="width:65%;" >通知内容</th>
				<th nowrap="nowrap" style="width:5%;" >处理人</th>
				<th nowrap="nowrap" style="width:10%;" >创建时间</th>
				
			</TR>
        </tr>
    </thead>
    <tbody>
    	<#list messageList  as message>
		    <TR>
		    <td><input type="checkbox" name="auditIds" value="${message.messageId!''},${message.auditId!''}"></td>
			<TD>${message_index+1}</TD>
			<TD>${message.sender!''}</TD>
			<TD>${message.auditSubTypeName!''}</TD>
			<TD style="text-align:left;">
			${message.messageContent!''}
			<div style='width:90px;overflow: hidden;text-overflow:ellipsis;height:18px;' title='${message.messageContent!''}'  onmouseout='this.style.width="90px"'>
			
			</div>

			</TD>
			<TD>${message.receiver!''}</TD>
			<TD>${message.createTime?string('yyyy-MM-dd HH:mm')} </TD>
			</TR>
	   </#list>
   
                
              </tbody>
  
  <TR>
  <TD COLSPAN ="7">
  	<a  class="btn btn_cc1"  id="completeMessage" >完成通知</a>
  	&nbsp;&nbsp;
  	 <a class="btn btn_cc1" id="closeButton" >取消</a>
  	 
  	
  </TD>
  </TR>
</table>
</div><!-- div p_box -->
</div><!-- //主要内容显示区域 -->
<#include "/base/foot.ftl"/>
</body>
</html>
<script type="text/javascript">
$("#closeButton").bind("click", function() {
 	messageDialog.close();
});

$("#completeMessage").bind("click", function() {

	var messageIds_value =[];    
	var auditIds_value =[]; 
	
		
	$('input[name="auditIds"]:checked').each(function(){    
	      	
	       		var checkedValue=$(this).val();
	       		var valueArray=checkedValue.split(',');
	       		
	       		messageIds_value.push(valueArray[0]); 
	       		auditIds_value.push(valueArray[1]); 
      });    
     
      if(messageIds_value.length==0)
      {
      	alert('尚未选中任何记录'); 
      	return;
      }
      var messageIds =messageIds_value+"";
      var auditIds=auditIds_value+"";
      
	   var data="messageIds="+messageIds+"&auditIds="+auditIds+"&orderId=${RequestParameters.orderId}";
	   $.ajax({
			   url : "/vst_order/order/orderStatusManage/updateMessage.do",
			   data : data,
			   type:"POST",
			   dataType:"JSON",
			   success : function(result){
			   		if( result.code=="success" || result.code=="error"){
			   		  alert(result.message);
					  window.location.reload();
			   			
			   		}else {
			   			alert(result.message);
			   		}
			   }
		});	
 	
});


$("#checkAll").bind("click",function(){
			  var ischeckAll=$("input[name='auditIds']").attr("checked"); 
			  if(!ischeckAll)
			  {
			  	$("input[name='auditIds']").attr("checked",true)
			  }else{
			  	$("input[name='auditIds']").attr("checked",false)
			  }
			  
		      
});	

 </script>
