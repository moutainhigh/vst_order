<html xmlns="http://www.w3.org/1999/xhtml"> 
 <head> 
<#import "func.html" as func/> 
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
 <title>行程单</title> 
 <style type="text/css"> 
	* {margin:0;padding:0;}
	body{font-family: SimSun;padding:0 40px;}
	.txt-hetong {color:#666;width:635px;margin:10px auto;text-align:center;font:12px/1.5 arial,SimSun;}
	.txt-hetong b {color:#333;font-weight:700;}
	.txt-hetong p {text-align:left;overflow:hidden; zoom:1;}
	table {font-size:12px;color:#666}
	.tab1 {background-color:#fff;width:98%;margin:10px auto;}
	.tab1 td,.tab1 th {padding:5px;text-align:left;vertical-align:middle;background-color:#fff;lin-height:20px;}
 </style> 
 </head> 

<body> 
<div class="txt-hetong">
	<p style="font-size:18px;">
        <#if travelContractVO.productName?? && travelContractVO.productName?length gt 0>
        	<@func.addSpace travelContractVO.productName 30/>
        <#else>
        	/
        </#if>
    </p><br/>
	<p style="border-bottom:3px solid #df1078">
    	<span style="float:left;width:110px;height:30px;line-height:30px; text-align:center;background:#df1078;color:#fff;font-size:14px">行程说明</span>
        <span style="float:right;font-style:normal;width:100%;" >
             <#if travelContractVO.productDelegate == 'COMMISSIONED_TOUR'>
    	          <#if travelContractVO.taiwanFlag=='Y' && travelContractVO.prodProduct.productType=='FOREIGNLINE'>
	                                            具体的旅游服务和操作由:${(travelContractVO.productDelegateName)!''}提供，驴妈妈旅游网（上海景域文化传播股份有限公司）<br/>
	                                            仅提供技术平台服务，由该产品及服务造成的一切法律责任和后果均由${(travelContractVO.productDelegateName)!''}承担。
	              <#else>      
                                                     本产品由${(travelContractVO.filialeName)!'上海驴妈妈兴旅国际旅行社有限公司'}代理招徕，委托社为${(travelContractVO.productDelegateName)!''}，<br/>
                                                     具体旅游服务和操作由委托社提供。 <br/>                         
      	          </#if>
            </#if>
            <#if travelContractVO.productDelegate == 'SELF_TOUR'>
                                     本产品由${(travelContractVO.filialeName)!'上海驴妈妈兴旅国际旅行社有限公司'}及具有合法资质的地接社提供相关服务。<br/>                         
            </#if>
      </span>
    </p>
	
    <#if travelContractVO.isNewRoute?? && travelContractVO.isNewRoute=="Y">
    	<#import "RouteTemplate.ftl" as routeTemplate />
        <div style="width:100%;height:auto;background:#eee;overflow:hidden;">
            <table border="0" cellspacing="1" cellpadding="0" class="tab1">
                <tbody>
                <!--如果存在行程，且行程内行程明细列表不为空，引用行程说明模板-->
                <#if travelContractVO.lineRoute?? && travelContractVO.lineRoute.prodLineRouteDetailList??>	
		            <@routeTemplate.routeTemplate  travelContractVO.lineRoute/>
     			</#if>
                </tbody>
            </table>
        </div>
    <#else>
	    <div style="width:100%;height:auto;background:#eee;overflow:hidden;">
	    <#if order.categoryId == '8'>
	    		<table border="0" cellspacing="1" cellpadding="0" class="tab1">
		    	<#if travelContractVO.shipLineRoute?? && travelContractVO.shipLineRoute.lineRouteDetails??>	
			    	 <#list travelContractVO.shipLineRoute.lineRouteDetails  as prodLineRouteDetail> 
			    	 
			        	<tr>
							<td>
			                	<p>
			                    	<span style="color:#c06;font-weight:700;font-size:14px;">第${prodLineRouteDetail.nDay!''}天</span>
			                    	<em style="margin: 0 12px;font-style:normal;font-size:14px;">${prodLineRouteDetail.title!''}</em>
			                    </p>
			                    <p style="margin-top:10px;">
				                    <#if prodLineRouteDetail.lineRouteDescs??>
				                    	<#list prodLineRouteDetail.lineRouteDescs  as routeDesc> 
				                    		${routeDesc.title!''}<br/>
				                    		<@func.addSpace routeDesc.content 50/>
				                    	</#list> 
				                    </#if>
			                    </p>
			                    <p style="margin-top:10px;padding:10px;background:#ddd;line-height:20px;">    
				                    <b style="margin-right:10px;">
				                   		 用餐
				                   	</b>
			                      <#if prodLineRouteDetail.mealsDesc?? && prodLineRouteDetail.mealsDesc?contains("breakfast")>
		                         		早餐：含
		                          <#else>
		                          		早餐 敬请自理  
		                          </#if>    
		                          <#if prodLineRouteDetail.mealsDesc?? && prodLineRouteDetail.mealsDesc?contains("chinese")>
		                         		中餐：含 
		                          <#else>
		                          		中餐 敬请自理 
		                          </#if> 
		                           <#if prodLineRouteDetail.mealsDesc?? && prodLineRouteDetail.mealsDesc?contains("dinner")>
		                         		晚餐：含
		                          <#else>
		                          		晚餐 敬请自理 
		                          </#if>    
		                          <br/>
			                        <b style="margin-right:10px;">住宿</b>
									  <#if prodLineRouteDetail.stayDesc??>
									 	<@func.addSpace prodLineRouteDetail.stayDesc 50/><br/>
									 <#else>
									 	<br/>
			                         </#if>
			                        <b style="margin-right:10px;">交通</b>${prodLineRouteDetail.trafficTool!''}
			                    </p>
			                </td>
			            </tr>
			            
			         </#list> 
		         </#if>   
		        </table>
	    	<#else>
	    	<table border="0" cellspacing="1" cellpadding="0" class="tab1">
	    	<#if travelContractVO.lineRoute?? && travelContractVO.lineRoute.prodLineRouteDetailList??>	
		    	 <#list travelContractVO.lineRoute.prodLineRouteDetailList  as prodLineRouteDetail> 
		    	 
		        	<tr>
						<td>
		                	<p>
		                    	<span style="color:#c06;font-weight:700;font-size:14px;">第${prodLineRouteDetail.nDay!''}天</span>
		                    	<em style="margin: 0 12px;font-style:normal;font-size:14px;">${prodLineRouteDetail.title!''}</em>
		                    </p>
		                    <p style="margin-top:10px;">
		                     <#--${prodLineRouteDetail.content!''}-->
		                   
		                    	<@func.addSpace prodLineRouteDetail.content 50/>
		                    	
		                    </p>
		                    <p style="margin-top:10px;padding:10px;background:#ddd;line-height:20px;">    
			                    <b style="margin-right:10px;">
			                   		 用餐  
			                   	</b>
		                      <#if prodLineRouteDetail.breakfastFlag=="Y">
	                         		含早餐<#if prodLineRouteDetail.breakfastDesc?? && prodLineRouteDetail.breakfastDesc != '含' && prodLineRouteDetail.breakfastDesc != ''>（${prodLineRouteDetail.breakfastDesc!''}）</#if>    
	                          <#else>
	                          		早餐（敬请自理）     
	                          </#if>    
	                           <#if prodLineRouteDetail.lunchFlag=="Y">
	                         		含中餐<#if prodLineRouteDetail.lunchDesc?? && prodLineRouteDetail.lunchDesc != '含' && prodLineRouteDetail.lunchDesc != ''>（${prodLineRouteDetail.lunchDesc!''}）</#if>    
	                          <#else>
	                          		中餐（敬请自理）    
	                          </#if> 
	                           <#if prodLineRouteDetail.dinnerFlag=="Y">
	                         		含晚餐<#if prodLineRouteDetail.dinnerDesc?? && prodLineRouteDetail.dinnerDesc != '含' && prodLineRouteDetail.dinnerDesc != ''>（${prodLineRouteDetail.dinnerDesc!''}）</#if>    
	                          <#else>
	                          		晚餐（敬请自理）    
	                          </#if>     
	                          <br/>
	                            <#--            	
		                        <b style="margin-right:10px;">用餐</b>早餐：${prodLineRouteDetail.breakfastDesc!'敬请自理'} ，  中餐：${prodLineRouteDetail.lunchDesc!'敬请自理'} ，  晚餐：${prodLineRouteDetail.dinnerDesc!'敬请自理'}<br/>
		                        -->
		                        <b style="margin-right:10px;">住宿</b>
		                         <#if prodLineRouteDetail.stayType??>
	                        	  ${prodLineRouteDetail.stayType!''}  
		                          <#list travelContractVO.hotelStarList as hotel>
									     <#if hotel.dictId == prodLineRouteDetail.stayType>
									      ${hotel.dictName}  
									      </#if>            		
								 </#list> 
								  </#if>
								  <#if prodLineRouteDetail.stayDesc??>
								 	<@func.addSpace prodLineRouteDetail.stayDesc 50/><br/>
								 <#else>
								 	<br/>
		                         </#if>
		                        <b style="margin-right:10px;">交通</b>${prodLineRouteDetail.trafficType!''}  ${prodLineRouteDetail.trafficOther!''}
		                    </p>
		                </td>
		            </tr>
		            
		         </#list> 
	         </#if>   
	        </table>
	    </#if>
	    </div>
    </#if>
</div>
</body> 
</html>