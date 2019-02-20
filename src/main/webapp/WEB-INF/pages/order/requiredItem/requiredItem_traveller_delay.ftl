<tr>
	<td  colspan="2">
	 <input type="hidden" id="ordPersonId${number_index}" name="travellers[${number_index}].ordPersonId" value="${tavellerList[number_index].ordPersonId}"/>
     <table class="e_table" name="userInfoTb" index="${number_index}">
		<tbody>
			<tr>
				 <td class="e_label w12"><b>游玩人${number}</b>：</td>
				<td>
					<#if personList??>
					常用联系人：
                	<#list personList as person>
                    	<label class="checkbox mr10">
                    	<input class="checkbox" type="checkbox" name="travellers[${number_index}].receiverId" value="${person.receiverId}" personName="${person.fullName}"
								mobile="${person.mobile}" idNo="${person.idNo}" birthday="${person.birthday}" gender="${person.gender}" expDate="<#if person.expDate??>${person.expDate?string("yyyy-MM-dd")}</#if>" issued="${person.issued}" idType="${person.idType}" emails="${person.email}" firstName="${person.firstName}" 
								lastName="${person.lastName}" peopleType="${person.peopleType}">${person.fullName}</label>
                    </#list>
                    </#if>
				</td>
			</tr>
			 <tr>
                <td class="e_label td_top">中文姓名：</td>
                <td>
					<input type="text" id="fullName${number_index}" name_type="fullName" name="travellers[${number_index}].fullName" 
					 placeholder="姓名" onchange="" travellersId="" value="${tavellerList[number_index].fullName}" index="${number_index}" />
					 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>中文姓名不正确</span>
                </td>
            </tr>
            <#if number==1>
            	<#if orderRequiredvO.ennameType=="TRAV_NUM_ONE" || orderRequiredvO.ennameType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">英文姓名：</td>
		        	<td>
		        		<input class="input js_yz" id="firstName${number_index}" name="travellers[${number_index}].firstName" value="${tavellerList[number_index].firstName}" name_type="firstName" type="text" placeholder="firstName" >
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>firstName不正确</span>
		        		<input class="input js_yz" id="lastName${number_index}" name="travellers[${number_index}].lastName"  name_type="lastName" type="text" placeholder="lastName" value="${tavellerList[number_index].lastName}" >
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>lastName不正确</span>
		        	</td>
		          </tr>
		         <#else>
		         	<input type="hidden" id="firstName${number_index}" name="travellers[${number_index}].firstName" value="${tavellerList[number_index].firstName}" name_type="firstName" />
		         	<input type="hidden" id="lastName${number_index}" name="travellers[${number_index}].lastName"  value="${tavellerList[number_index].lastName}" name_type="lastName" />
		    	</#if>
		    	<#if orderRequiredvO.occupType=="TRAV_NUM_ONE" || orderRequiredvO.occupType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">人群：</td>
		        	<td>
		        		 <select class="select" name="travellers[${number_index}].peopleType" value="${tavellerList[number_index].peopleType}" id="peopleType${number_index}" >
						       	<option value="PEOPLE_TYPE_ADULT" <#if tavellerList[number_index].peopleType=="PEOPLE_TYPE_ADULT">selected</#if>>成人</option>
					       		<option value="PEOPLE_TYPE_CHILD" <#if tavellerList[number_index].peopleType=="PEOPLE_TYPE_CHILD">selected</#if>>儿童</option>
			           	</select>
		        	</td>
		          </tr>
		         <#else>
		        	<input type="hidden" name="travellers[${number_index}].peopleType" value="${tavellerList[number_index].peopleType}" id="peopleType${number_index}" />
		    	</#if>
            	 <#if orderRequiredvO.phoneType=="TRAV_NUM_ONE" || orderRequiredvO.phoneType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">手机号码：</td>
		        	<td>
		        		<input id="mobile${number_index}" name="travellers[${number_index}].mobile" value="${tavellerList[number_index].mobile}" id="mobile${number_index}"
		        		 maxlength="11" type="text" placeholder="手机号码" name_type="mobile" >
		        		 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>手机号码不正确</span>
		        		<span class="ts_text">此手机号为接收短信所用，作为订购与取票凭证，请准确填写。</span>
		        	</td>
		          </tr>
		         <#else>
		         	<input  type="hidden" id="mobile${number_index}" name="travellers[${number_index}].mobile" value="${tavellerList[number_index].mobile}"/>
		    	</#if>
		    	<#if orderRequiredvO.emailType=="TRAV_NUM_ONE" || orderRequiredvO.emailType=="TRAV_NUM_ALL">
		    		<tr>
		        	<td class="e_label td_top">邮箱地址：</td>
		        	<td>
		        		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" value="${tavellerList[number_index].email}" type="text" 
		        			placeholder="邮箱" name_type="email" >
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮箱地址不正确</span>
		        		<spid="email${number_index}" an class="ts_text">此邮箱地址为接收邮件所用，作为订购与取票凭证，请准确填写。</span>
		       		</td>
		       		</tr>
		       	<#else>
		       		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" value="${tavellerList[number_index].email}" type="hidden" name_type="email" />
			    </#if>
			    <#if orderRequiredvO.idNumType=="TRAV_NUM_ONE" || orderRequiredvO.idNumType=="TRAV_NUM_ALL">
			    	<tr>
			    	<td class="e_label">证件类型：</td>
			        <td>
			       <select class="select" name="travellers[${number_index}].idType" birthday="<#if tavellerList[number_index].birthday??>${tavellerList[number_index].birthday?string("yyyy-MM-dd")}</#if>"  gender="${tavellerList[number_index].gender}"  expDate= "<#if tavellerList[number_index].expDate??>${tavellerList[number_index].expDate?string("yyyy-MM-dd")}</#if>" issued="${tavellerList[number_index].issued}" id="idType${number_index}" num="${number_index}" old="${tavellerList[number_index].idType}">
			        			<#if orderRequiredvO.idFlag=="Y">
							       <option value="ID_CARD" <#if tavellerList[number_index].idType=="ID_CARD">selected</#if> >身份证</option>
							    </#if>
							    <#if orderRequiredvO.passportFlag=="Y">
							       <option value="HUZHAO" <#if tavellerList[number_index].idType=="HUZHAO">selected</#if> >护照</option>
							    </#if>
							    <#if orderRequiredvO.passFlag=="Y">
							       <option value="GANGAO" <#if tavellerList[number_index].idType=="GANGAO">selected</#if>>港澳通行证</option>
							    </#if>
							    <#if orderRequiredvO.twPassFlag=="Y">
							       <option value="TAIBAO" <#if tavellerList[number_index].idType=="TAIBAO">selected</#if>>台湾通行证</option>
							    </#if>
							    <#if orderRequiredvO.twResidentFlag=="Y">
							       <option value="TAIBAOZHENG" <#if tavellerList[number_index].idType=="TAIBAOZHENG">selected</#if>>台胞证</option>
							    </#if>
							    <#if orderRequiredvO.hkResidentFlag=="Y">
							       <option value="HUIXIANG" <#if tavellerList[number_index].idType=="HUIXIANG">selected</#if>>回乡证</option>
							    </#if>
							    <#if orderRequiredvO.birthCertFlag=="Y">
							       <option value="CHUSHENGZHENGMING" <#if tavellerList[number_index].idType=="CHUSHENGZHENGMING">selected</#if>>出生证明</option>
							    </#if>
							    <#if orderRequiredvO.householdRegFlag=="Y">
							       <option value="HUKOUBO" <#if tavellerList[number_index].idType=="HUKOUBO">selected</#if>>户口簿</option>
							    </#if>
							    <#if orderRequiredvO.soldierFlag=="Y">
							       <option value="SHIBING" <#if tavellerList[number_index].idType=="SHIBING">selected</#if>>士兵证</option>
							    </#if>
							    <#if orderRequiredvO.officerFlag=="Y">
							       <option value="JUNGUAN" <#if tavellerList[number_index].idType=="JUNGUAN">selected</#if>>军官证</option>
							    </#if>
                                <#if orderRequiredvO.productType!="ticket">
							        <option value="CUSTOMER_SERVICE_ADVICE" <#if tavellerList[number_index].idType=="CUSTOMER_SERVICE_ADVICE">selected</#if>>客服联系我</option>
                                </#if>
			           		</select>
			        		<input type_name="shenfenzheng" id="idNo${number_index}" name="travellers[${number_index}].idNo" value="${tavellerList[number_index].idNo}" type="text" placeholder="证件号码" maxlength="25" name_type="idNo" >
			        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>证件错误</span>
			        		<span class="ts_text">取票时需出示身份证确认。</span>
			        </td>
			        </tr>
			        			        
			       <tr id="person_issued_${number_index}" <#if tavellerList[number_index].idType!="HUIXIANG" && tavellerList[number_index].idType!="TAIBAOZHENG">style="display:none"</#if> >
				    	<td class="e_label">有效期：</td>
				        <td class="data"  <#if tavellerList[number_index].idType!="ID_CARD">style=""</#if>>
				       <input type='text' id='expDate${number_index}'  name='travellers[${number_index}].expDate' style='width:120px'  readonly='true' onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})" value="<#if tavellerList[number_index].expDate??>${tavellerList[number_index].expDate?string("yyyy-MM-dd")}</#if>"  />

				       	签发地：
				        <input type='text' id='issued${number_index}' name='travellers[${number_index}].issued' style='width:120px'  value="<#if tavellerList[number_index].issued??>${tavellerList[number_index].issued}</#if>"/>
				        </td>	
			        </tr>
			        <tr id="person_birthday_${number_index}" <#if tavellerList[number_index].idType=="ID_CARD" || tavellerList[number_index].idType=="CUSTOMER_SERVICE_ADVICE"||tavellerList[number_index].idType==null>style="display:none"</#if> >
				    	<td class="e_label">证件附加：</td>
				        <td class="data"  <#if tavellerList[number_index].idType!="ID_CARD">style=""</#if>>
				        	
				        	出生日期<input type='text' id='birthday${number_index}' name='travellers[${number_index}].birthday' style='width:120px'  readonly='true' onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})" value="<#if tavellerList[number_index].birthday??>${tavellerList[number_index].birthday?string("yyyy-MM-dd")}</#if>" />
				        	性别<select name='travellers[${number_index}].gender' id='gender${number_index}' style='width:100px' >
				        	<option value='MAN' <#if tavellerList[number_index].gender=="MAN">selected</#if>>男</option><option value='WOMAN' <#if tavellerList[number_index].gender=="WOMAN">selected</#if>>女</option>
				        	</select>
				        	
				        </td>
			        </tr>
			    <#else>
			    	<#-- 证件类型 -->
			    	<input type="hidden" name="travellers[${number_index}].idType" value="${tavellerList[number_index].idType}" />
			    	<#-- 证件号码 -->
			    	<input type="hidden" name="travellers[${number_index}].idNo" value="${tavellerList[number_index].idNo}" />
			    	<#-- 有效期 -->
			    	<input type='hidden' id='expDate${number_index}'  name='travellers[${number_index}].expDate' value="<#if tavellerList[number_index].expDate??>${tavellerList[number_index].expDate?string("yyyy-MM-dd")}</#if>"/>
			    	<#-- 签发地 -->
				     <input type='hidden' id='issued${number_index}' name='travellers[${number_index}].issued' value="<#if tavellerList[number_index].issued??>${tavellerList[number_index].issued}</#if>"/>
				     <#-- 出生日期 -->
				     <input type='hidden' id='birthday${number_index}' name='travellers[${number_index}].birthday' value="<#if tavellerList[number_index].birthday??>${tavellerList[number_index].birthday?string("yyyy-MM-dd")}</#if>"/>
				     <#-- 性别 -->
				    <input type='hidden' name='travellers[${number_index}].gender' value = "${tavellerList[number_index].gender}" id='gender${number_index}' />
			    </#if>
            </#if>
            <#if number!=1>
            	<#if orderRequiredvO.ennameType=="TRAV_NUM_ALL">
            	   <tr>
		        	<td class="e_label td_top">英文姓名：</td>
		        	<td>
		        		<input class="input js_yz" id="firstName${number_index}" name="travellers[${number_index}].firstName" value="${tavellerList[number_index].firstName}" name_type="firstName" type="text" placeholder="firstName" >
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>firstName不正确</span>
		        		<input class="input js_yz" id="lastName${number_index}" name="travellers[${number_index}].lastName"  name_type="lastName" type="text" placeholder="lastName" value="${tavellerList[number_index].lastName}" >
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>lastName不正确</span>
		        	</td>
		          </tr>
		        <#else>
		        	<input type="hidden" id="firstName${number_index}" name="travellers[${number_index}].firstName" value="${tavellerList[number_index].firstName}" name_type="firstName" />
		         	<input type="hidden" id="lastName${number_index}" name="travellers[${number_index}].lastName"  value="${tavellerList[number_index].lastName}" name_type="lastName" />
		    	</#if>
		    	<#if orderRequiredvO.occupType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">人群：</td>
		        	<td>
		        		<select class="select" name="travellers[${number_index}].peopleType" id="peopleType${number_index}" >
						       	<option value="PEOPLE_TYPE_ADULT" <#if tavellerList[number_index].peopleType=='PEOPLE_TYPE_ADULT'>selected</#if>>成人</option>
					       		<option value="PEOPLE_TYPE_CHILD" <#if tavellerList[number_index].peopleType=='PEOPLE_TYPE_CHILD'>selected</#if>>儿童</option>
			           	</select>
		        	</td>
		          </tr>
		        <#else>
		        	<input type="hidden" name="travellers[${number_index}].peopleType" id="peopleType${number_index}" value="${tavellerList[number_index].peopleType}" />
		    	</#if>
            	 <#if orderRequiredvO.phoneType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">手机号码：</td>
		        	<td>
		        		<input id="mobile${number_index}" name="travellers[${number_index}].mobile" value="${tavellerList[number_index].mobile}" id="mobile${number_index}"
		        		 maxlength="11" type="text" placeholder="手机号码" name_type="mobile"  >
		        		 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>手机号码不正确</span>
		        		<span class="ts_text">此手机号为接收短信所用，作为订购与取票凭证，请准确填写。</span>
		        	</td>
		          </tr>
		        <#else>
		        	<input  type="hidden" id="mobile${number_index}" name="travellers[${number_index}].mobile" name_type="mobile" value="${tavellerList[number_index].mobile}"/>
		    	</#if>
		    	<#if orderRequiredvO.emailType=="TRAV_NUM_ALL">
		    		<tr>
		        	<td class="e_label td_top">邮箱地址：</td>
		        	<td>
		        		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" value="${tavellerList[number_index].email}" type="text" 
		        			placeholder="邮箱" name_type="email" >
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮箱地址不正确</span>
		        		<spid="email${number_index}" an class="ts_text">此邮箱地址为接收邮件所用，作为订购与取票凭证，请准确填写。</span>
		       		</td>
		       		</tr>
		       	<#else>
		       		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" value="${tavellerList[number_index].email}" type="hidden" name_type="email" />
			    </#if>
			    <#if orderRequiredvO.idNumType=="TRAV_NUM_ALL">
			    	<tr>
			    	<td class="e_label">证件类型：</td>
			        <td>
			       <select class="select" name="travellers[${number_index}].idType" birthday="<#if tavellerList[number_index].birthday??>${tavellerList[number_index].birthday?string("yyyy-MM-dd")}</#if>"  gender="${tavellerList[number_index].gender}"  expDate= "<#if tavellerList[number_index].expDate??>${tavellerList[number_index].expDate?string("yyyy-MM-dd")}</#if>" issued="${tavellerList[number_index].issued}" id="idType${number_index}" num="${number_index}" old="${tavellerList[number_index].idType}">
			        			<#if orderRequiredvO.idFlag=="Y">
							       <option value="ID_CARD" <#if tavellerList[number_index].idType=="ID_CARD">selected</#if> >身份证</option>
							    </#if>
							    <#if orderRequiredvO.passportFlag=="Y">
							       <option value="HUZHAO" <#if tavellerList[number_index].idType=="HUZHAO">selected</#if> >护照</option>
							    </#if>
							    <#if orderRequiredvO.passFlag=="Y">
							       <option value="GANGAO" <#if tavellerList[number_index].idType=="GANGAO">selected</#if>>港澳通行证</option>
							    </#if>
							    <#if orderRequiredvO.twPassFlag=="Y">
							       <option value="TAIBAO" <#if tavellerList[number_index].idType=="TAIBAO">selected</#if>>台湾通行证</option>
							    </#if>
							    <#if orderRequiredvO.twResidentFlag=="Y">
							       <option value="TAIBAOZHENG" <#if tavellerList[number_index].idType=="TAIBAOZHENG">selected</#if>>台胞证</option>
							    </#if>
							    <#if orderRequiredvO.hkResidentFlag=="Y">
							       <option value="HUIXIANG" <#if tavellerList[number_index].idType=="HUIXIANG">selected</#if>>回乡证</option>
							    </#if>
							    <#if orderRequiredvO.birthCertFlag=="Y">
							       <option value="CHUSHENGZHENGMING" <#if tavellerList[number_index].idType=="CHUSHENGZHENGMING">selected</#if>>出生证明</option>
							    </#if>
							    <#if orderRequiredvO.householdRegFlag=="Y">
							       <option value="HUKOUBO" <#if tavellerList[number_index].idType=="HUKOUBO">selected</#if>>户口簿</option>
							    </#if>
							    <#if orderRequiredvO.soldierFlag=="Y">
							       <option value="SHIBING" <#if tavellerList[number_index].idType=="SHIBING">selected</#if>>士兵证</option>
							    </#if>
							    <#if orderRequiredvO.officerFlag=="Y">
							       <option value="JUNGUAN" <#if tavellerList[number_index].idType=="JUNGUAN">selected</#if>>军官证</option>
							    </#if>
                                <#if orderRequiredvO.productType!="ticket">
							        <option value="CUSTOMER_SERVICE_ADVICE" <#if tavellerList[number_index].idType=="CUSTOMER_SERVICE_ADVICE">selected</#if>>客服联系我</option>
                                </#if>
			           		</select>
			        		<input type_name="shenfenzheng" id="idNo${number_index}" name="travellers[${number_index}].idNo" value="${tavellerList[number_index].idNo}" type="text" placeholder="证件号码" maxlength="25" name_type="idNo" >
			        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>证件错误</span>
			        		<span class="ts_text">取票时需出示身份证确认。</span>
			        </td>
			        </tr>
			        			        
			       <tr id="person_issued_${number_index}" <#if tavellerList[number_index].idType!="HUIXIANG" && tavellerList[number_index].idType!="TAIBAOZHENG">style="display:none"</#if> >
				    	<td class="e_label">有效期：</td>
				        <td class="data"  <#if tavellerList[number_index].idType!="ID_CARD">style=""</#if>>
				       <input type='text' id='expDate${number_index}'  name='travellers[${number_index}].expDate' style='width:120px'  readonly='true' onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})" value="<#if tavellerList[number_index].expDate??>${tavellerList[number_index].expDate?string("yyyy-MM-dd")}</#if>"  />

				       	签发地：
				        <input type='text' id='issued${number_index}' name='travellers[${number_index}].issued' style='width:120px'  value="<#if tavellerList[number_index].issued??>${tavellerList[number_index].issued}</#if>"/>
				        </td>	
			        </tr>
			        <tr id="person_birthday_${number_index}" <#if tavellerList[number_index].idType=="ID_CARD" || tavellerList[number_index].idType=="CUSTOMER_SERVICE_ADVICE"||tavellerList[number_index].idType==null>style="display:none"</#if> >
				    	<td class="e_label">证件附加：</td>
				        <td class="data"  <#if tavellerList[number_index].idType!="ID_CARD">style=""</#if>>
				        	
				        	出生日期<input type='text' id='birthday${number_index}' name='travellers[${number_index}].birthday' style='width:120px'  readonly='true' onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})" value="<#if tavellerList[number_index].birthday??>${tavellerList[number_index].birthday?string("yyyy-MM-dd")}</#if>" />
				        	性别<select name='travellers[${number_index}].gender' id='gender${number_index}' style='width:100px'>
				        	<option value='MAN' <#if tavellerList[number_index].gender=="MAN">selected</#if>>男</option><option value='WOMAN' <#if tavellerList[number_index].gender=="WOMAN">selected</#if>>女</option>
				        	</select>
				        	
				        </td>
			        </tr>
			    <#else>
			    	<#-- 证件类型 -->
			    	<input type="hidden" name="travellers[${number_index}].idType" value="${tavellerList[number_index].idType}" />
			    	<#-- 证件号码 -->
			    	<input type="hidden" name="travellers[${number_index}].idNo" value="${tavellerList[number_index].idNo}" />
			    	<#-- 有效期 -->
			    	<input type='hidden' id='expDate${number_index}'  name='travellers[${number_index}].expDate' value="<#if tavellerList[number_index].expDate??>${tavellerList[number_index].expDate?string("yyyy-MM-dd")}</#if>"/>
			    	<#-- 签发地 -->
				     <input type='hidden' id='issued${number_index}' name='travellers[${number_index}].issued' value="<#if tavellerList[number_index].issued??>${tavellerList[number_index].issued}</#if>"/>
				     <#-- 出生日期 -->
				     <input type='hidden' id='birthday${number_index}' name='travellers[${number_index}].birthday' value="<#if tavellerList[number_index].birthday??>${tavellerList[number_index].birthday?string("yyyy-MM-dd")}</#if>"/>
				     <#-- 性别 -->
				    <input type='hidden' name='travellers[${number_index}].gender' value = "${tavellerList[number_index].gender}" id='gender${number_index}' />
			    </#if>
            </#if>
		</tbody>
	 </table>
    </td>
</tr>