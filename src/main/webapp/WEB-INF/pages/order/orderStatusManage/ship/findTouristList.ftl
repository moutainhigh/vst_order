<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#--页面导航-->
<div id="logResultList" class="divClass">
 <table class="p_table table_center mt20">
                <thead>
                    <tr>
                        <#if categoryCode== "category_visa"> 
                         <th width="30px">
							签证
                        <#else>
                          <th width="60px">
							入住房间
							 </th>
						</#if>
                        <th width="50px">中文姓名</th>
                        <th width="40px">英文姓</th>
                        <th width="80px">英文名</th>
                        <th width="30px">性别</th>
                        <th width="50px">出生地</th>
                        <th width="60px">出生日期</th>
                        <th width="80px">证件号码</th>
                        <th width="50px">签发地</th>
                        <th width="60px">签发日期</th>
                        <th width="60px">有效日期</th>
                        <th width="60px">手机号</th>
                    </tr>
                </thead>
                <tbody>
                <#if personMap?? && personMap?size &gt; 0>
                	<#list personMap?keys as cangfangkey>
                		<#assign roomMap = personMap[cangfangkey]>
                		<#if roomMap?? && roomMap?size &gt; 0>
                			<#list roomMap?keys as roomkey>
                				<#assign personList = roomMap[roomkey]>
                					<#if personList?? && personList?size &gt; 0>
                						<#list personList as person> 
						                    <tr>
						                        <#if person_index == 0> 
						                        	<td rowspan="<#if personList?? && personList?size &gt; 0>${personList?size}</#if>">第${roomkey}间：<br/>${person.checkInRoomName!''}</td>
												</#if>	
						                        <td>
						                      		${person.fullName!''}
						                        </td>
						                        <td>
						                        	${person.lastName!''}
												</td>
						                        <td>
						                        	${person.firstName!''}
												</td>
												<td>
							                        <#if person.gender == "MAN"> 
							                      	 	男
													<#elseif person.gender == "WOMAN">
														女
													</#if>
						                  		</td>
						                       <td> ${person.birthPlace!''}</td>
						                       <td>
						                         <#if person.birthday?exists>
						                         	${person.birthday?string('yyyy-MM-dd')}
						                         </#if>
						                       </td>
						                       <td>
							                       <#if person.idTypeName!="客服联系我">
							                      	 ${person.idNo!''}
							                       </#if>
						                  	   </td>
						                       <td> ${person.issued!''}</td>
						                       <td >
							                         <#if person.issueDate?exists>
							                          ${person.issueDate?string('yyyy-MM-dd')}
							                        </#if>
						                       </td>
						                       <td>
							                        <#if person.expDate?exists>
							                        	${person.expDate?string('yyyy-MM-dd')}
							                        </#if>
						                       </td>
						                       <td>${person.mobile!''} </td>
						                    </tr>                						
                						</#list>
                					</#if>
                			</#list>
                		</#if>
                	</#list>
                </#if>                
                </tbody>
            </table>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>
<script type="text/javascript">

         
 </script>
