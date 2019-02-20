<!DOCTYPE html>
<html>
<head>

<#import "/base/spring.ftl" as spring/>
<#include "/base/head_meta.ftl"/>
<#import "/base/pagination.ftl" as pagination>
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


<div class="iframe_search">
	<form id="searchForm" action="/vst_order/order/orderManage/findComMessageList.do" method="post">
         
         <input type="hidden" name="orderId" value="${RequestParameters.orderId}">
         <input type="hidden" name="orderItemId" value="${RequestParameters.orderItemId}">
		<input type="hidden" name="orderType" value="${RequestParameters.orderType}">
	   
         <table class="s_table">
            <tbody>
                <tr>
                <td class="s_label">通知分类：</td>
                    <td class="w18">
                   	<select name="auditSubType" id="auditSubType" >
                   		<option value="">全部</option>
	                	<#list auditSubTypeList as auditSubType>
	                		<option value="${auditSubType.code!''}" <#if auditSubType ==RequestParameters.auditSubType >selected</#if>>${auditSubType.cnName!''}</option>
	                	</#list>
	            	</select>
					</td>
                    <td class=" operate mt10"><a class="btn btn_cc1" id="search_button">查询</a></td>
                </tr>
            </tbody>
        </table>	
	</form>
</div>

<div class="iframe-content">  
    <div class="p_box">
<table class="p_table table_center" style="word-break:break-all; word-wrap:break-all;">
    <thead>
        <tr>
              <TR>
              <th   class="w1"></th>
				<th nowrap="nowrap" style="width:5%;"  >序号</th>
				<th nowrap="nowrap" style="width:5%;" >创建人</th>
				<th nowrap="nowrap" style="width:5%;" >分类</th>
				<th nowrap="nowrap" style="width:75%;" >通知内容</th>
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
			<#--
			<div style='width:90px;overflow: hidden;text-overflow:ellipsis;height:18px;' title='${message.messageContent!''}'  onmouseout='this.style.width="90px"'>
			
			</div>
-->
			</TD>
			<TD>${message.receiver!''}</TD>
			<TD>${message.createTime?string('yyyy-MM-dd HH:mm')} </TD>
			</TR>
	   </#list>
   
                
    </tbody>
 
<tr>
  <TD COLSPAN ="7">
  	<a  class="btn btn_cc1"  id="completeMessage" >完成通知</a>
  	&nbsp;&nbsp;
  	 <#--
  	 <a class="btn btn_cc1" id="closeButton" >取消</a>
  	 -->
  	
  </TD>
  </TR>
</table>
<table class="co_table">
        <tbody>
            <tr>
                 <td class="s_label">
                 	<#if pageParam.items?exists> 
						<div class="paging" > 
						${pageParam.getPagination()}
						</div> 
					</#if>
                 </td>
            </tr>
        </tbody>
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
//查询
	$("#search_button").click(function(){
		$("#searchForm").submit();
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
      
	   var data="messageIds="+messageIds+"&auditIds="+auditIds+"&orderId=${RequestParameters.orderId}&auditSubType=${RequestParameters.auditSubType}";
	   $.ajax({
			   url : "/vst_order/order/orderManage/updateMessage.do",
			   data : data,
			   type:"POST",
			   dataType:"JSON",
			   success : function(result){
			   		if( result.code=="success" || result.code=="error"){
			   		  alert(result.message);
					  //window.location.reload();
			   			$("#searchForm").submit();
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
