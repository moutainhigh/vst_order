<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
<div id="logResultList" class="divClass">
<div class="order_msg clearfix">
                	</br>
                	
                	
                	<strong>
                	游客信息
                       <#if 'parent' == RequestParameters.orderType && order.orderStatus=='NORMAL' >
                              <#if order.ordAccInsDelayInfo?? && order.ordAccInsDelayInfo.travDelayFlag=='Y' &&order.ordAccInsDelayInfo.travDelayStatus=='UNCOMPLETED'>
                               (<a class="btn btn_cc1" id="supplyPersonButton" href="javaScript:" >补全</a>)
                               </#if>
                               (<a class="btn btn_cc1" id="editPersonButton" href="javaScript:" >修改</a>)
                              
                      </#if>
                	  <#if order.travellerDelayFlag=='Y' && order.travellerLockFlag =='Y'>
                	  	<font color="red">游玩人信息已锁定</font>
                	  </#if>
                	  </strong>
                	  
                	  <#--
                	<strong>
                	  游客信息
                	  </strong>
                	   <#if 'parent' == RequestParameters.orderType>
                	    <p align="right">
							<button class="pbtn pbtn-small btn-ok" style="margin-top:20px;"  id="editPersonButton">修改游客信息</button>
						</p>
						 </#if>
						 
						 -->
</div>
 <table class="p_table table_center mt20">
                <thead>
                 
                 
                    <tr>
                    	<th>类型</th>
                        <th>中文姓名</th>
                        <th>英文姓</th>
                        <th>英文名</th>
                        <th>证件类型</th>
                        <th>证件号码</th>
                        <th>签发地</th>
                        <th>有效期</th>
                        <th>出生日期</th>
                        <th>人群</th>
                        <th>性别</th>
                        <!-- 如果是当地玩乐(交通接驳)，境内手机号和境外手机号 -->
                        <#if hasConnects == true || hasPlayOut==true>
                            <th>境内手机</th>
                            <th>境外手机</th>
                        <#else>
                            <th>联系电话</th>
                        </#if>
                        <th>邮箱地址</th>
                        <th>
                       	关联的商品
                        </th>
                    </tr>
                </thead>
                <tbody>
                    
                <#list personList as person> 
                    <tr>
                    
                    	 <td>
                          <#if person.personType == "CONTACT"> 
                      	取票人/联系人
						<#else>
						游客
						
						</#if>
                        
                        </td>
                        
                        <td>
                      ${person.fullName!''}
                        </td>
                        <td>
                        ${person.lastName!''}
						</td>
                        <td>
                         ${person.firstName!''}
						</td>
						
                        <td> ${person.idTypeName!''}</td>
                        
                        
                      <td>
                       <#if person.idTypeName!="客服联系我">
                       ${person.idNo!''}
                       </#if>
                  		</td>
                  		
                  		<td> ${person.issued!''}</td>
                        <td> 
                        <#if person.expDate?exists>
                        ${person.expDate?string('yyyy-MM-dd')}
                        </#if>
                        </td>
                        
                       <td>
                         <#if person.birthday?exists>
                         ${person.birthday?string('yyyy-MM-dd')}
                        </#if>
                        
                        </td>
                       
                       
                       <td>  
                        <#if person.peopleType == "PEOPLE_TYPE_ADULT"> 
                      	 成人
						<#elseif person.peopleType == "PEOPLE_TYPE_CHILD">
						儿童
						<#elseif person.peopleType == "PEOPLE_TYPE_OLDER">
						老人
						</#if>
                       </td>
                       
                       
                        <td>
                        <#if person.gender == "MAN"> 
                      	 男
                      	  <#elseif person.gender == "WOMAN">
						女
						</#if>
                  		</td>
                  		
                  		
                       <td>${(person.mobile)!''}</td>
                        <#if hasConnects == true || hasPlayOut==true>
                            <td>${(person.outboundPhone)!''}</td>
                        </#if>
                        <td>${(person.email)!''} </td>
                       <td> ${(person.checkInRoomName)!'无'}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
</div>
<#--页脚-->
</body>
</html>
<script type="text/javascript">


var editPersonButtonDialog;
var supplyPersonButtonDialog;

$("#supplyPersonButton").bind("click",function(){
	supplyPersonButtonDialog = new xDialog("/vst_order/ord/order/update/showUpdateTourist.do",{"orderId":${RequestParameters.orderId!''},"isSupplyFlag":'Y'},{title:"补全游客",width:800,dialogAutoStop: true});
 
 });

  
$("#editPersonButton").bind("click",function(){
	//editPersonButtonDialog = new xDialog("/vst_order/order/orderManage/showUpdateTourist.do",{"orderId":${RequestParameters.orderId!''}},{title:"修改游客",width:1300});
	editPersonButtonDialog = new xDialog("/vst_order/ord/order/update/showUpdateTourist.do",{"orderId":${RequestParameters.orderId!''}},{title:"修改游客",width:800,dialogAutoStop: true});
 
 });

 </script>
