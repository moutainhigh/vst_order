 <!--公告列表 开始-->
    <#if productNoticList?? && productNoticList?size &gt; 0> 
	<div class="product" style="margin-left:20px;">
	    <table class="table table-border">
	        <tbody>
	            <#assign index=1 />
		           <tr>产品公告：</tr>
		           <br>
				<#list productNoticList as Notice>
				  <#if Notice.noticeType != 'PRODUCT_GIFT' && Notice.noticeType != 'PRODUCT_RECOMMEND'>
				     <tr>
				        <span class="text-center">${index}<i>.<i/></span> 
			            <span class="text-center">${Notice.content}</span>
		             </tr>
		              <#if index != productNoticList?size>
		              <br> 
		             </#if> 
		           </#if>    
		          <#assign index = index+1 />    
	            </#list> 
	        </tbody>
	    </table>      
	</div>            	            
</#if> 	