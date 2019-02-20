<!DOCTYPE html>
<#import "/order/econtractTemplate/RouteTemplate.ftl" as routeTemplate />
<#import "/order/econtractTemplate/func.html" as func />
<html>
<head>
<meta charset="utf-8" />
<title></title>
<meta name="keywords" content="" />
<meta name="description" content=""/>
<base target="_blank">
<#include "/base/head_meta.ftl"/>
<style>
body,div,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,code,form,fieldset,legend,input,select,textarea,p,blockquote,th,td,hr,button,figure,menu
{margin:0;padding:0; font:12px/1.5 Arial,"\5b8b\4f53";}
h1,h2,h3,h4,h5,h6{font-size:100%;}
table{border-collapse:collapse;border-spacing:0;}
ol,ul{list-style:none outside;}
a img{border:none;}
img{ vertical-align:top;}
input{ font-family:Tahoma,Verdana,Geneva,sans-serif}
a{ text-decoration:none;outline:none; hide-focus:expression(this.hideFocus=true);}a:hover{ text-decoration: underline;}
i,samp,dfn,em{ font-style:normal;}



.ct_Notice{width:600px;margin:0 auto;overflow:hidden;}
.ct_Notice .titleBox{ text-align:center;line-height:40px;color:#000;font-family:"宋体";font-size:36px;font-weight:bold;margin:30px 0;}
.ct_Notice .ct_form_Mod{width:100%;height:auto;overflow:hidden;}
.ct_Notice .ct_form_List{}
.ct_Notice .ct_form_List li{width:100%;height:auto;overflow:hidden;color:#000;font-family:"宋体";font-size:14px;line-height:22px;margin-top:10px;}
.ct_Notice .ct_form_List li b{float:left;}
.ct_Notice .ct_form_List li b span {margin-left: 0;}
.ct_Notice .ct_form_List li span{float:left;margin-left:10px;max-width:500px;overflow:hidden;}
.ct_Notice .ct_fomr_ListLi_Lbox{float:left;margin-left:10px;}
.ct_Notice .ct_fomr_ListLi_Lbox p{clear:both;overflow:hidden;height:22px;line-height:22px;color:#000;font-family:"宋体";font-size:14px;}
.ct_Notice .ct_fomr_List_radio{width:20px;height:20px;margin:2px 0;float:left;}
.ct_Notice .ct_fomr_List_checkBox{width:20px;height:20px;margin:2px 0;float:left;}
.ct_Notice .ct_fomr_ListLi_Lbox label{float:left;margin:0 5px;}
.ct_Notice .ct_fomr_List_textBox{float:left;border:1px solid #ccc;height:20px;width:300px;background:#fff; text-indent:5px;}
.ct_Notice .ct_fomr_ListLi_Lbox p .ct_fomr_List_textBox{width:200px;}
.ct_Notice .ct_fomr_List_textArea{min-width:580px;height:80px;resize: none;width:90%; }
.ct_fomr_List_table{background-color: transparent;border-collapse: collapse;border-spacing: 0;width: 100%;min-width:600px;max-width:1920px;}
.ct_Notice .ct_fomr_List_table tr td{ text-align:center;color:#000;font-family:"宋体";font-size:14px;line-height:30px;}
.ct_Notice .ct_fomr_List_table tr td span{display:block; text-align:center;width:100%;margin:0;padding:0;}
.ct_Notice .ct_fomr_List_table .BBox{font-weight:bold;}
.ct_Notice .ct_js_tableTd_textBox{display:none;border:1px solid #ccc;height:20px;width:90%;background:#fff; text-indent:5px;}

.ct_tips{width:100%;line-height:22px;;color:#000;font-family:"宋体";font-size:14px;margin:10px 0;}
.ct_form_tjBtn{width:300px;height:30px;line-height:30px; text-align:center;color:#fff;font-family:"宋体";font-size:16px;font-weight:bold;background:#009dd9;margin:0 auto;margin-top:50px;margin-bottom:50px; cursor:pointer;}

</style>
</head>

<body >
 <form action="#" method="post" id="dataForm">
 
 
	<input type="hidden" name="orderId" value="${order.orderId}">
  
   
   
<div class="ct_Notice" >
	<h2 class="titleBox">出团通知书</h2>
    <div class="ct_form_Mod">
        <ul class="ct_form_List">
        	<li>
            	<b>旅游线路：</b><span>${(travelContractVO.productName)!''}</span>
            </li>
            <li>
            	<b>订单编号：</b><span>${order.orderId!''}</span>
            </li>
            <li>
            	<b>客人姓名：</b><span>${order.firstTravellerPerson.fullName!''} 等 ${order.ordTravellerList?size} 位</span>
            </li>
            <li>
            	<b>旅游日期：</b><span>${order.visitTime?string('yyyy-MM-dd')!''} </span>
            </li>
            <li>
            	<b><span style="color: red;">*</span>接团方式：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<p>
                    	<input type="radio" name="groupWay" value="guideSign"  class="ct_fomr_List_radio"/><label>导游举游客姓名牌子接团</label>
                    </p>
                    <p>
                    	<input type="radio" name="groupWay" value="other"  class="ct_fomr_List_radio"/>
                    	<label>其他</label>
                    	<input type="text" name="groupWayOtherContent" class="ct_fomr_List_textBox" maxLength="100" placeholder="输入具体接团方式"/>
                    </p>
                </div>
            </li>
            <li>
            	<b><span style="color: red;">*</span>接机导游电话：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<input type="text" class="ct_fomr_List_textBox" name="guideTelephone" placeholder="输入导游联系电话" required="true" number="true"  maxLength="15"/>
                </div>
            </li>
            <li>
            	<b><span style="color: red;">*</span>当地应急电话：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<input type="text" class="ct_fomr_List_textBox" name="emergencyTelephone" placeholder="输入当地应急电话" required="true" number="true"   maxLength="15"/>
                </div>
            </li>
            <li>
            	<b>驴妈妈应急联系电话：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<input type="text" class="ct_fomr_List_textBox" name="lvmamaEmergencyTelephone"  value="10106060" placeholder="10106060" style="background:#ccc"/>
                </div>
            </li>
            <li>
            	<b>驴妈妈质监电话：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<input type="text" class="ct_fomr_List_textBox" name="lvmamaQualityPhone" placeholder="10106060"   value="10106060" style="background:#ccc"/>
                </div>
            </li>
            <li>
            	<b>组团社：</b>
                <div class="ct_fomr_ListLi_Lbox">                	
			    <#if travelContractVO.productDelegate == 'COMMISSIONED_TOUR'>
			           <input type="text" class="ct_fomr_List_textBox" name="lvmamaCompany"  value="${(travelContractVO.productDelegateName)!''}"  placeholder="${(travelContractVO.productDelegateName)!''}" style="background:#ccc"/>                         
			     </#if>			
			     <#if travelContractVO.productDelegate == 'SELF_TOUR'>
			          <input type="text" class="ct_fomr_List_textBox" name="lvmamaCompany"  value="${travelContractVO.filialeName}"  placeholder="${travelContractVO.filialeName}" style="background:#ccc"/>                        
			     </#if>
                </div>
            </li>
            <#if travelContractVO.productDelegate == 'COMMISSIONED_TOUR'>
            <li>          
                 <b>代理方：</b>
                  <div class="ct_fomr_ListLi_Lbox">
                   <input type="text" class="ct_fomr_List_textBox" name="agent"  value="${travelContractVO.filialeName}"  placeholder="${travelContractVO.filialeName}" style="background:#ccc"/>
                  </div>             
            </li>
             </#if>
            <li>
            	<b>特别说明：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<textarea name="memo" class="ct_fomr_List_textArea" maxLength="500"></textarea>
                </div>
            </li>
            <li>
            	<b>客人信息：</b>
                <table border="1" bordercolor="#ccc" class="ct_fomr_List_table">
                
	                	<tr>
	                    	<td class="BBox" width="30%">姓名</td>
	                        <td class="BBox" width="30%">证件类型</td>
	                        <td class="BBox" width="40%">证件号码</td>
	                    </tr>
	                  <#list order.ordTravellerList  as person>    
	                    <tr>
	                    	<td ><input type="text"  name="personList[${person_index}].fullName"  value="${(person.fullName)!''}" /></td>
	                        <td >  
	                        <select name="personList[${person_index}].idTypeName"  style="width:100px;"  >
		                	<#list IDTypeList as  idType>
		                		<option value="${idType.cnName!''}" <#if idType.code == person.idType>selected</#if>>${idType.cnName!''}</option>
		                	</#list>
		            		</select>
		            		</td>
	                        <td ><input type="text"  name="personList[${person_index}].idNo"  value="${(person.idNo)!''}" /></td>
	                    </tr>
	                   
                    
                    </#list> 
                </table>
            </li>
           
              <li>
            	<b><span style="color: red;">*</span>交通信息：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<textarea class="ct_fomr_List_textArea"  style="width:285px; height:320px;" name="orderTraffic" maxLength="500"></textarea>
                </div>
            </li>
             <#if hotelOrderItemList?size!=0>
          
		            <li>
		            	<b>入住信息：</b>
		                <table border="1" bordercolor="#ccc" class="ct_fomr_List_table">
		                	<tr>
		                    	<td class="BBox" width="15%">日期</td>
		                        <td class="BBox" width="25%">酒店名称</td>
		                        <td class="BBox" width="35%">酒店地址</td>
		                        <td class="BBox" width="25%">备注</td>
		                    </tr>
		                   
		                    <#list hotelOrderItemList  as orderItem> 
			                    <tr>
			                    	<td ><input type="text" style="width:80px;height:17px" name="orderItemList[${orderItem_index}].visitTime"   value="${orderItem.visitTime?string('yyyy-MM-dd') !''}" /></td>
			                        <td><input type="text"  name="orderItemList[${orderItem_index}].productName"  value="${orderItem.productName!''}-${orderItem.contentMap['branchName']!''}" /></td>
			                        <td ><input type="text"  name="orderItemList[${orderItem_index}].deductType"  value="${orderItem.deductType!''}" /></td>
			                        <td ><input type="text" style="width:120px;height:17px"  name="orderItemList[${orderItem_index}].orderMemo" value="" /></td>
			                    </tr>
		                   </#list>    
		                    
		                   
		                </table>
		            </li>
            </#if>
            
            <#if travelContractVO.lineRoute?? && travelContractVO.lineRoute.prodLineRouteDetailList??>	
            	<li>
	            	<b>行程信息：</b>
	                <div style="width:100%;height:auto;overflow:hidden;">
				    	<#if travelContractVO.isNewRoute?? && travelContractVO.isNewRoute=="Y">
				    		<table border="0" cellspacing="1" cellpadding="0" class="tab1">
								<@routeTemplate.routeTemplate travelContractVO.lineRoute/>
							</table>
				        <#else>
					    	<table border="0" cellspacing="1" cellpadding="0" class="tab1">
					        	<#if travelContractVO.lineRoute?? && travelContractVO.lineRoute.prodLineRouteDetailList??>	
						    	 <#list travelContractVO.lineRoute.prodLineRouteDetailList  as prodLineRouteDetail> 	 
							        	<tr>
											<td>
							                	<p>
							                    	<span style="color:#c06;font-weight:700;font-size:14px;">第${prodLineRouteDetail.nDay!''}天</span>
							                    	 ${prodLineRouteDetail.title!''}
							                    	
							                    </p>
							                    <p style="margin-top:10px;padding:10px;word-wrap: break-word;">
							                    	${prodLineRouteDetail.content!''}
							                    </p>
							                    <p style="margin-top:10px;padding:10px;line-height:20px;">                  	
							                        <b style="margin-right:10px;">
							                                            用餐</b>
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
							                        <b style="margin-right:10px;">住宿</b>
							                        <#if prodLineRouteDetail.stayType??>
						                        	  含住宿  
							                          <#list travelContractVO.hotelStarList as hotel>
														     <#if hotel.dictId == prodLineRouteDetail.stayType>
														      ${hotel.dictName}  
														      </#if>            		
													 </#list> 
													${prodLineRouteDetail.stayDesc!''}
						                        <#else>
						                       		不含住宿
						                        </#if>
							                    </p>
							                </td>
							            </tr>
							            
							         </#list> 
						         </#if>   
					        </table>
				        </#if>	
	    			</div>
            	</li>
            </#if>
            
            <li>
            	<b>费用包含：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<textarea class="ct_fomr_List_textArea" style="width:285px; height:320px;" name="priceIncludes" maxLength="10000">${travelContractVO.priceIncludes!''}</textarea>
                </div>
            </li>
            <li>
            	<b>费用不含：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<textarea class="ct_fomr_List_textArea" style="width:285px; height:320px;" name="priceNotIncludes"  maxLength="10000">${travelContractVO.priceNotIncludes!''}</textarea>
                </div>
            </li>
            <li>
           		 
            	
            	
               
                <#if travelContractVO.prodProduct.bizCategoryId==17>
                	<b>行前须知：</b>
                	 <div class="ct_fomr_ListLi_Lbox">
                	<textarea class="ct_fomr_List_textArea" style="width:285px; height:320px;" name="travelWarnings" maxLength="10000">${travelContractVO.travelWarnings!''}</textarea>
                	 </div>
                <#else>
                	<b>出行警示及说明：</b>
                	<div class="ct_fomr_ListLi_Lbox">
                	<textarea  class="ct_fomr_List_textArea" style="width:285px; height:320px;" name="travelWarnings" maxLength="10000">${travelContractVO.travelWarnings!''}</textarea>
                	 </div>
                </#if>
                	
               
            </li>
            <li>
            	<b>退改说明：</b>
                <div class="ct_fomr_ListLi_Lbox">
                	<textarea class="ct_fomr_List_textArea" style="width:285px; height:320px;" name="backToThat"  maxLength="10000">${travelContractVO.backToThat!''}</textarea>
                </div>
            </li>
        </ul>
    </div>
</div>









</form>
<#include "/base/foot.ftl"/>
<p align="center">
<!--
 <button class="ct_form_tjBtn"  id="viewButton">预览出团通知书</button>
 &nbsp;&nbsp;&nbsp;&nbsp;
 -->
 <button class="ct_form_tjBtn"  id="editButton">生成出团通知书并发送</button>
</p>
</body>

<script src="http://pic.lvmama.com/min/index.php?f=/js/new_v/jquery-1.7.2.min.js,/js/ui/lvmamaUI/lvmamaUI.js" type="text/javascript"></script>
<script>
$('.ct_js_tableTd').click(function(){
	var _val = $(this).text();
	$(this).find('.ct_js_tableTd_textBox').val(_val);
	$(this).find('.ct_js_tableTd_textBox').show();
	$(this).children('span').hide();
	
})
// placeholder 功能扩展
window.onload = function(){
    var doc = document,
    inputs = doc.getElementsByTagName('input'),
    supportPlaceholder = 'placeholder' in doc.createElement('input'),
    placeholder = function(input){
        var text = input.getAttribute('placeholder'),
            defaultValue = input.defaultValue;
        if(input.value=="" || input.value==text){
            input.value=text;
            input.style.color = 'gray';
        }
        
        input.onfocus = function(){
            if(input.value === text){
                this.value = '';
                this.style.color = '';
            }
        }
        input.onblur = function(){
            if(input.value === ''){
                input.style.color = 'gray';
                this.value = text;
            }
        }
        input.onkeydown = function(){
            this.style.color = '';
        }
    };
    if(!supportPlaceholder){
        for(var i = 0, len = inputs.length; i < len; i++){
            var input = inputs[i], text = input.getAttribute('placeholder');
            if(input.type === 'text' && text){
                placeholder(input);
            }
        }    
    }
}

/**
$("#viewButton").bind("click",function(){
		
	window.open ("/vst_order/order/orderShipManage/viewNoticeRegimentTemplate.do?"+$("#dataForm").serialize(), "预览出团通知书","height=880, width=850");
         
  });
   */      

$("#editButton").bind("click",function(){
	
	var groupWay = $("input[name='groupWay']:checked").val();
	var groupWayOtherContent = $("input[name='groupWayOtherContent']").val();
	var guideTelephone = $("input[name='guideTelephone']").val();
	var emergencyTelephone = $("input[name='emergencyTelephone']").val();
	var orderTraffic = $("textarea[name='orderTraffic']").val();

	if (!groupWay) {
		alert("接团方式为空，请补充完整");
        $("input[name='groupWay'][value='guideSign']").focus();
		return;
	} /*else if (groupWay == 'other' && !groupWayOtherContent) {
        alert("接团方式为空，请补充完整");
        return;
	}*/

    if (!guideTelephone) {
        alert("接机导游电话为空，请补充完整");
        $("input[name='guideTelephone']").focus();
        return;
    }
    if (!emergencyTelephone) {
        alert("当地应急电话为空，请补充完整");
        $("input[name='emergencyTelephone']").focus();
        return;
    }
    if (!orderTraffic) {
        alert("交通信息为空，请补充完整");
        $("textarea[name='orderTraffic']").focus();
        return;
    }
	//遮罩层
    var loading = top.pandora.loading("正在努力生成中...");	
	
	$.ajax({
	   url : "/vst_order/order/orderShipManage/saveNoticeRegiment.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			loading.close();
   			alert(result.message);
	   		 //parent.window.location.reload();
   		}else {
   			loading.close();
   		  	alert(result.message);
   		}
	   }
	});	
});

</script>
</html>
