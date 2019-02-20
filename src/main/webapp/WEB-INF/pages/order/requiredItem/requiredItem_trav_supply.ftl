<tr>
	<td  colspan="2">
	<#assign travellerPerson = tavellerList[number_index]/>
	 <input type="hidden" id="ordPersonId${number_index}" name="travellers[${number_index}].ordPersonId" value="${travellerPerson.ordPersonId}"/>
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
                <td class="e_label td_top"><span class="red">*</span>中文姓名：</td>
                <td>
					<input type="text" id="fullName${number_index}" name_type="fullName" name="travellers[${number_index}].fullName" 
					 placeholder="姓名" onchange="" travellersId="" value="${travellerPerson.fullName}" index="${number_index}" required="true" <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.userName=='N'>readonly='true'</#if><#else>readonly='true'</#if> />
					 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>中文姓名不正确</span>
                </td>
            </tr>
            <#if number==1>
            	<#if orderRequiredvO.ennameType=="TRAV_NUM_ONE" || orderRequiredvO.ennameType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top"><#if orderRequiredvO.expandMap['ennameType_OPTIONAL'] !='Y'><span class="red">*</span></#if>英文姓名：</td>
		        	<td>
		        		<input class="input js_yz" id="firstName${number_index}" name="travellers[${number_index}].firstName" value="${travellerPerson.firstName}" name_type="firstName" type="text" placeholder="firstName" <#if orderRequiredvO.expandMap['ennameType_OPTIONAL']!='Y'>required="true"</#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.enName=='N'>readonly='true'</#if><#else>readonly='true'</#if>/>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>firstName不正确</span>
		        		<input class="input js_yz" id="lastName${number_index}" name="travellers[${number_index}].lastName" value="${travellerPerson.lastName}" name_type="lastName" type="text" placeholder="lastName" <#if orderRequiredvO.expandMap['ennameType_OPTIONAL']!='Y'>required="true"</#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.enName=='N'>readonly='true'</#if><#else>readonly='true'</#if>/>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>lastName不正确</span>
		        	</td>
		          </tr>
		         <#else>
		         	<input type="hidden" id="firstName${number_index}" name="travellers[${number_index}].firstName" value="${travellerPerson.firstName}" name_type="firstName" />
		         	<input type="hidden" id="lastName${number_index}" name="travellers[${number_index}].lastName"  value="${travellerPerson.lastName}" name_type="lastName" />
		    	</#if>
		    	<#if orderRequiredvO.occupType=="TRAV_NUM_ONE" || orderRequiredvO.occupType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top"><span class="red">*</span>人群：</td>
		        	<td>
		        		 <select class="select" name="travellers[${number_index}].peopleType" value="${travellerPerson.peopleType}" id="peopleType${number_index}" <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.occup=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
						       	<option value="PEOPLE_TYPE_ADULT" <#if travellerPerson.peopleType=="PEOPLE_TYPE_ADULT">selected</#if>>成人</option>
					       		<option value="PEOPLE_TYPE_CHILD" <#if travellerPerson.peopleType=="PEOPLE_TYPE_CHILD">selected</#if>>儿童</option>
			           	</select>
		        	</td>
		          </tr>
		         <#else>
		        	<input type="hidden" name="travellers[${number_index}].peopleType" value="${travellerPerson.peopleType}" id="peopleType${number_index}" />
		    	</#if>
            	 <#if orderRequiredvO.phoneType=="TRAV_NUM_ONE" || orderRequiredvO.phoneType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top"><#if orderRequiredvO.expandMap['phoneType_OPTIONAL']!='Y'><span class="red">*</span></#if>手机号码：</td>
		        	<td>
		        		<input id="mobile${number_index}" name="travellers[${number_index}].mobile" value="${travellerPerson.mobile}" id="mobile${number_index}"
		        		 maxlength="11" type="text" placeholder="手机号码" name_type="mobile" <#if orderRequiredvO.expandMap['phoneType_OPTIONAL']!='Y'>required="true"</#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.phoneNum=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
		        		 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>手机号码不正确</span>
		        		<span class="ts_text">此手机号为接收短信所用，作为订购与取票凭证，请准确填写。</span>
		        	</td>
		          </tr>
		         <#else>
		         	<input  type="hidden" id="mobile${number_index}" name="travellers[${number_index}].mobile" value="${travellerPerson.mobile}"/>
		    	</#if>
		    	
		    	<#if orderRequiredvO.outboundPhoneType=="TRAV_NUM_ONE" || orderRequiredvO.outboundPhoneType=="TRAV_NUM_ALL">
			    	 <tr>
			        	<td class="e_label td_top">境外手机号码：</td>
			        	<td>
			        		<input id="outboundPhone${number_index}" name="travellers[${number_index}].outboundPhone" value="${travellerPerson.outboundPhone}" id="outboundPhone${number_index}"
			        		 maxlength="11" type="text" placeholder="境外手机号码" name_type="outboundPhone" >
			        		 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>境外手机号码不正确</span>
			        	</td>
			          </tr>
		    	</#if>
		    	
		    	<#if orderRequiredvO.emailType=="TRAV_NUM_ONE" || orderRequiredvO.emailType=="TRAV_NUM_ALL">
		    		<tr>
		        	<td class="e_label td_top"><#if orderRequiredvO.expandMap['emailType_OPTIONAL']!='Y'><span class="red">*</span></#if>邮箱地址：</td>
		        	<td>
		        		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" value="${travellerPerson.email}" type="text" 
		        			placeholder="邮箱" name_type="email" <#if orderRequiredvO.expandMap['emailType_OPTIONAL']!='Y'>required="true" </#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.email=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮箱地址不正确</span>
		        		<spid="email${number_index}" an class="ts_text">此邮箱地址为接收邮件所用，作为订购与取票凭证，请准确填写。</span>
		       		</td>
		       		</tr>
		       	<#else>
		       		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" value="${travellerPerson.email}" type="hidden" name_type="email" />
			    </#if>
			    <#if orderRequiredvO.idNumType=="TRAV_NUM_ONE" || orderRequiredvO.idNumType=="TRAV_NUM_ALL">
			    	<tr>
			    	<td class="e_label"><#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'><span class="red">*</span></#if>证件类型：</td>
			        <td>
			       <select class="select" name="travellers[${number_index}].idType" birthday="<#if travellerPerson.birthday??>${travellerPerson.birthday?string("yyyy-MM-dd")}</#if>"  gender="${travellerPerson.gender}"  expDate= "<#if travellerPerson.expDate??>${travellerPerson.expDate?string("yyyy-MM-dd")}</#if>" issued="${travellerPerson.issued}" id="idType${number_index}" num="${number_index}" old="${travellerPerson.idType}"
			        <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.idType=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
			        			<#if orderRequiredvO.idFlag=="Y">
							       <option value="ID_CARD" <#if travellerPerson.idType=="ID_CARD">selected</#if> >身份证</option>
							    </#if>
							    <#if orderRequiredvO.passportFlag=="Y">
							       <option value="HUZHAO" <#if travellerPerson.idType=="HUZHAO">selected</#if> >护照</option>
							    </#if>
							    <#if orderRequiredvO.passFlag=="Y">
							       <option value="GANGAO" <#if travellerPerson.idType=="GANGAO">selected</#if>>港澳通行证</option>
							    </#if>
							    <#if orderRequiredvO.twPassFlag=="Y">
							       <option value="TAIBAO" <#if travellerPerson.idType=="TAIBAO">selected</#if>>台湾通行证</option>
							    </#if>
							    <#if orderRequiredvO.twResidentFlag=="Y">
							       <option value="TAIBAOZHENG" <#if travellerPerson.idType=="TAIBAOZHENG">selected</#if>>台胞证</option>
							    </#if>
							    <#if orderRequiredvO.hkResidentFlag=="Y">
							       <option value="HUIXIANG" <#if travellerPerson.idType=="HUIXIANG">selected</#if>>回乡证</option>
							    </#if>
							    <#if orderRequiredvO.birthCertFlag=="Y">
							       <option value="CHUSHENGZHENGMING" <#if travellerPerson.idType=="CHUSHENGZHENGMING">selected</#if>>出生证明</option>
							    </#if>
							    <#if orderRequiredvO.householdRegFlag=="Y">
							       <option value="HUKOUBO" <#if travellerPerson.idType=="HUKOUBO">selected</#if>>户口簿</option>
							    </#if>
							    <#if orderRequiredvO.soldierFlag=="Y">
							       <option value="SHIBING" <#if travellerPerson.idType=="SHIBING">selected</#if>>士兵证</option>
							    </#if>
							    <#if orderRequiredvO.officerFlag=="Y">
							       <option value="JUNGUAN" <#if travellerPerson.idType=="JUNGUAN">selected</#if>>军官证</option>
							    </#if>
                                <#if orderRequiredvO.productType!="ticket">
							        <option value="CUSTOMER_SERVICE_ADVICE" <#if travellerPerson.idType=="CUSTOMER_SERVICE_ADVICE">selected</#if>>客服联系我</option>
                                </#if>
			           		</select>
			        		<input type_name="shenfenzheng" id="idNo${number_index}" name="travellers[${number_index}].idNo" value="${travellerPerson.idNo}" type="text" placeholder="证件号码" maxlength="25" name_type="idNo" <#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'>required="true"</#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.idType=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
			        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>证件错误</span>
			        		<span class="ts_text">取票时需出示身份证确认。</span>
			        </td>
			        </tr>
			        			        
			       <tr id="person_issued_${number_index}" <#if travellerPerson.idType!="HUIXIANG" && travellerPerson.idType!="TAIBAOZHENG">style="display:none"</#if> >
				    	<td class="e_label">有效期：</td>
				        <td class="data"  <#if travellerPerson.idType!="ID_CARD">style=""</#if>>
				       <input type='text' id='expDate${number_index}'  name='travellers[${number_index}].expDate' style='width:120px'  readonly='true' onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})" value="<#if travellerPerson.expDate??>${travellerPerson.expDate?string("yyyy-MM-dd")}</#if>"  <#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'>required="true"</#if>/>

				       	签发地：
				        <input type='text' id='issued${number_index}' name='travellers[${number_index}].issued' style='width:120px'  value="<#if travellerPerson.issued??>${travellerPerson.issued}</#if>"/>
				        </td>	
			        </tr>
			        <tr id="person_birthday_${number_index}" <#if travellerPerson.idType=="ID_CARD" || travellerPerson.idType=="CUSTOMER_SERVICE_ADVICE"||travellerPerson.idType==null>style="display:none"</#if> >
				    	<td class="e_label"><#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'><span class="red">*</span></#if>证件附加：</td>
				        <td class="data"  <#if travellerPerson.idType!="ID_CARD">style=""</#if>>
				        	
				        	出生日期<input type='text' id='birthday${number_index}' name='travellers[${number_index}].birthday' style='width:120px' <#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'>required="true"</#if> readonly='true' onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})" value="<#if travellerPerson.birthday??>${travellerPerson.birthday?string("yyyy-MM-dd")}</#if>" <#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'>required="true"</#if>/>
				        	性别<select name='travellers[${number_index}].gender' id='gender${number_index}' style='width:100px' required="true">
				        	<option value='MAN' <#if travellerPerson.gender=="MAN">selected</#if>>男</option><option value='WOMAN' <#if travellerPerson.gender=="WOMAN">selected</#if>>女</option>
				        	</select>
				        	
				        </td>
			        </tr>
			    <#else>
			    	<#-- 证件类型 -->
			    	<input type="hidden" name="travellers[${number_index}].idType" value="${travellerPerson.idType}" />
			    	<#-- 证件号码 -->
			    	<input type="hidden" name="travellers[${number_index}].idNo" value="${travellerPerson.idNo}" />
			    	<#-- 有效期 -->
			    	<input type='hidden' id='expDate${number_index}'  name='travellers[${number_index}].expDate' value="<#if travellerPerson.expDate??>${travellerPerson.expDate?string("yyyy-MM-dd")}</#if>"/>
			    	<#-- 签发地 -->
				     <input type='hidden' id='issued${number_index}' name='travellers[${number_index}].issued' value="<#if travellerPerson.issued??>${travellerPerson.issued}</#if>"/>
				     <#-- 出生日期 -->
				     <input type='hidden' id='birthday${number_index}' name='travellers[${number_index}].birthday' value="<#if travellerPerson.birthday??>${travellerPerson.birthday?string("yyyy-MM-dd")}</#if>"/>
				     <#-- 性别 -->
				    <input type='hidden' name='travellers[${number_index}].gender' value = "${travellerPerson.gender}" id='gender${number_index}' />
			    </#if>
            </#if>
            <#if number!=1>
            	<#if orderRequiredvO.ennameType=="TRAV_NUM_ALL">
            	   <tr>
		        	<td class="e_label td_top"><#if orderRequiredvO.expandMap['ennameType_OPTIONAL']!='Y'><span class="red">*</span></#if>英文姓名：</td>
		        	<td>
		        		<input class="input js_yz" id="firstName${number_index}" name="travellers[${number_index}].firstName" value="${travellerPerson.firstName}" name_type="firstName" type="text" placeholder="firstName" <#if orderRequiredvO.expandMap['ennameType_OPTIONAL']!='Y'>required="true"</#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.enName=='N'>readonly='true'</#if><#else>readonly='true'</#if>/>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>firstName不正确</span>
		        		<input class="input js_yz" id="lastName${number_index}" name="travellers[${number_index}].lastName" value="${travellerPerson.lastName}" name_type="lastName" type="text" placeholder="lastName" <#if orderRequiredvO.expandMap['ennameType_OPTIONAL']!='Y'>required="true"</#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.enName=='N'>readonly='true'</#if><#else>readonly='true'</#if>/>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>lastName不正确</span>
		        	</td>
		          </tr>
		        <#else>
		        	<input type="hidden" id="firstName${number_index}" name="travellers[${number_index}].firstName" value="${travellerPerson.firstName}" name_type="firstName" />
		         	<input type="hidden" id="lastName${number_index}" name="travellers[${number_index}].lastName"  value="${travellerPerson.lastName}" name_type="lastName" />
		    	</#if>
		    	<#if orderRequiredvO.occupType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top"><span class="red">*</span>人群：</td>
		        	<td>
		        		<select class="select" name="travellers[${number_index}].peopleType" id="peopleType${number_index}" <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.occup=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
						       	<option value="PEOPLE_TYPE_ADULT" <#if travellerPerson.peopleType=='PEOPLE_TYPE_ADULT'>selected</#if>>成人</option>
					       		<option value="PEOPLE_TYPE_CHILD" <#if travellerPerson.peopleType=='PEOPLE_TYPE_CHILD'>selected</#if>>儿童</option>
			           	</select>
		        	</td>
		          </tr>
		        <#else>
		        	<input type="hidden" name="travellers[${number_index}].peopleType" id="peopleType${number_index}" value="${travellerPerson.peopleType}" />
		    	</#if>
            	 <#if orderRequiredvO.phoneType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top"><#if orderRequiredvO.expandMap['phoneType_OPTIONAL']!='Y'><span class="red">*</span></#if>手机号码：</td>
		        	<td>
		        		<input id="mobile${number_index}" name="travellers[${number_index}].mobile" value="${travellerPerson.mobile}" id="mobile${number_index}"
		        		 maxlength="11" type="text" placeholder="手机号码" name_type="mobile" <#if orderRequiredvO.expandMap['phoneType_OPTIONAL']!='Y'>required="true"</#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.phoneNum=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
		        		 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>手机号码不正确</span>
		        		<span class="ts_text">此手机号为接收短信所用，作为订购与取票凭证，请准确填写。</span>
		        	</td>
		          </tr>
		        <#else>
		        	<input  type="hidden" id="mobile${number_index}" name="travellers[${number_index}].mobile" name_type="mobile" value="${travellerPerson.mobile}"/>
		    	</#if>
		    	
		    	<#if orderRequiredvO.outboundPhoneType=="TRAV_NUM_ALL">
			    	 <tr>
			        	<td class="e_label td_top">境外手机号码：</td>
			        	<td>
			        		<input id="outboundPhone${number_index}" name="travellers[${number_index}].outboundPhone" value="${travellerPerson.outboundPhone}" id="outboundPhone${number_index}"
			        		 maxlength="11" type="text" placeholder="境外手机号码" name_type="outboundPhone" >
			        		 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>境外手机号码不正确</span>
			        	</td>
			          </tr>
		    	</#if>
		    	
		    	<#if orderRequiredvO.emailType=="TRAV_NUM_ALL">
		    		<tr>
		        	<td class="e_label td_top"><#if orderRequiredvO.expandMap['emailType_OPTIONAL']!='Y'><span class="red">*</span></#if>邮箱地址：</td>
		        	<td>
		        		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" value="${travellerPerson.email}" type="text" 
		        			placeholder="邮箱" name_type="email" <#if orderRequiredvO.expandMap['emailType_OPTIONAL']!='Y'>required="true"</#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.email=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮箱地址不正确</span>
		        		<spid="email${number_index}" an class="ts_text">此邮箱地址为接收邮件所用，作为订购与取票凭证，请准确填写。</span>
		       		</td>
		       		</tr>
		       	<#else>
		       		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" value="${travellerPerson.email}" type="hidden" name_type="email" />
			    </#if>
			    <#if orderRequiredvO.idNumType=="TRAV_NUM_ALL">
			    	<tr>
			    	<td class="e_label"><#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'><span class="red">*</span></#if>证件类型：</td>
			        <td>
			       <select class="select" name="travellers[${number_index}].idType" birthday="<#if travellerPerson.birthday??>${travellerPerson.birthday?string("yyyy-MM-dd")}</#if>"  gender="${travellerPerson.gender}"  expDate= "<#if travellerPerson.expDate??>${travellerPerson.expDate?string("yyyy-MM-dd")}</#if>" issued="${travellerPerson.issued}" id="idType${number_index}" num="${number_index}" old="${travellerPerson.idType}"
			       <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.idType=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
			        			<#if orderRequiredvO.idFlag=="Y">
							       <option value="ID_CARD" <#if travellerPerson.idType=="ID_CARD">selected</#if> >身份证</option>
							    </#if>
							    <#if orderRequiredvO.passportFlag=="Y">
							       <option value="HUZHAO" <#if travellerPerson.idType=="HUZHAO">selected</#if> >护照</option>
							    </#if>
							    <#if orderRequiredvO.passFlag=="Y">
							       <option value="GANGAO" <#if travellerPerson.idType=="GANGAO">selected</#if>>港澳通行证</option>
							    </#if>
							    <#if orderRequiredvO.twPassFlag=="Y">
							       <option value="TAIBAO" <#if travellerPerson.idType=="TAIBAO">selected</#if>>台湾通行证</option>
							    </#if>
							    <#if orderRequiredvO.twResidentFlag=="Y">
							       <option value="TAIBAOZHENG" <#if travellerPerson.idType=="TAIBAOZHENG">selected</#if>>台胞证</option>
							    </#if>
							    <#if orderRequiredvO.hkResidentFlag=="Y">
							       <option value="HUIXIANG" <#if travellerPerson.idType=="HUIXIANG">selected</#if>>回乡证</option>
							    </#if>
							    <#if orderRequiredvO.birthCertFlag=="Y">
							       <option value="CHUSHENGZHENGMING" <#if travellerPerson.idType=="CHUSHENGZHENGMING">selected</#if>>出生证明</option>
							    </#if>
							    <#if orderRequiredvO.householdRegFlag=="Y">
							       <option value="HUKOUBO" <#if travellerPerson.idType=="HUKOUBO">selected</#if>>户口簿</option>
							    </#if>
							    <#if orderRequiredvO.soldierFlag=="Y">
							       <option value="SHIBING" <#if travellerPerson.idType=="SHIBING">selected</#if>>士兵证</option>
							    </#if>
							    <#if orderRequiredvO.officerFlag=="Y">
							       <option value="JUNGUAN" <#if travellerPerson.idType=="JUNGUAN">selected</#if>>军官证</option>
							    </#if>
                                <#if orderRequiredvO.productType!="ticket">
							        <option value="CUSTOMER_SERVICE_ADVICE" <#if travellerPerson.idType=="CUSTOMER_SERVICE_ADVICE">selected</#if>>客服联系我</option>
                                </#if>
			           		</select>
			        		<input type_name="shenfenzheng" id="idNo${number_index}" name="travellers[${number_index}].idNo" value="${travellerPerson.idNo}" type="text" placeholder="证件号码" maxlength="25" name_type="idNo" <#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'>required="true"</#if> <#if travellerPerson.ordTravAdditionConf??><#if travellerPerson.ordTravAdditionConf.idType=='N'>readonly='true'</#if><#else>readonly='true'</#if>>
			        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>证件错误</span>
			        		<span class="ts_text">取票时需出示身份证确认。</span>
			        </td>
			        </tr>
			        			        
			       <tr id="person_issued_${number_index}" <#if travellerPerson.idType!="HUIXIANG" && travellerPerson.idType!="TAIBAOZHENG">style="display:none"</#if> >
				    	<td class="e_label">有效期：</td>
				        <td class="data"  <#if travellerPerson.idType!="ID_CARD">style=""</#if>>
				       <input type='text' id='expDate${number_index}'  name='travellers[${number_index}].expDate' style='width:120px'  readonly='true' onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})" value="<#if travellerPerson.expDate??>${travellerPerson.expDate?string("yyyy-MM-dd")}</#if>"  <#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'>required="true"</#if>/>

				       	签发地：
				        <input type='text' id='issued${number_index}' name='travellers[${number_index}].issued' style='width:120px'  value="<#if travellerPerson.issued??>${travellerPerson.issued}</#if>"/>
				        </td>	
			        </tr>
			        <tr id="person_birthday_${number_index}" <#if travellerPerson.idType=="ID_CARD" || travellerPerson.idType=="CUSTOMER_SERVICE_ADVICE"||travellerPerson.idType==null>style="display:none"</#if> >
				    	<td class="e_label"><#if orderRequiredvO.expandMap['idNumType_OPTIONAL'] !='Y'><span class="red">*</span></#if>证件附加：</td>
				        <td class="data"  <#if travellerPerson.idType!="ID_CARD">style=""</#if>>
				        	
				        	出生日期<input type='text' id='birthday${number_index}' name='travellers[${number_index}].birthday' style='width:120px' <#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'>required="true"</#if> readonly='true' onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd'})" value="<#if travellerPerson.birthday??>${travellerPerson.birthday?string("yyyy-MM-dd")}</#if>" <#if orderRequiredvO.expandMap['idNumType_OPTIONAL']!='Y'>required="true"</#if>/>
				        	性别<select name='travellers[${number_index}].gender' id='gender${number_index}' style='width:100px' required="true">
				        	<option value='MAN' <#if travellerPerson.gender=="MAN">selected</#if>>男</option><option value='WOMAN' <#if travellerPerson.gender=="WOMAN">selected</#if>>女</option>
				        	</select>
				        	
				        </td>
			        </tr>
			    <#else>
			    	<#-- 证件类型 -->
			    	<input type="hidden" name="travellers[${number_index}].idType" value="${travellerPerson.idType}" />
			    	<#-- 证件号码 -->
			    	<input type="hidden" name="travellers[${number_index}].idNo" value="${travellerPerson.idNo}" />
			    	<#-- 有效期 -->
			    	<input type='hidden' id='expDate${number_index}'  name='travellers[${number_index}].expDate' value="<#if travellerPerson.expDate??>${travellerPerson.expDate?string("yyyy-MM-dd")}</#if>"/>
			    	<#-- 签发地 -->
				     <input type='hidden' id='issued${number_index}' name='travellers[${number_index}].issued' value="<#if travellerPerson.issued??>${travellerPerson.issued}</#if>"/>
				     <#-- 出生日期 -->
				     <input type='hidden' id='birthday${number_index}' name='travellers[${number_index}].birthday' value="<#if travellerPerson.birthday??>${travellerPerson.birthday?string("yyyy-MM-dd")}</#if>"/>
				     <#-- 性别 -->
				    <input type='hidden' name='travellers[${number_index}].gender' value = "${travellerPerson.gender}" id='gender${number_index}' />
			    </#if>
            </#if>
		</tbody>
	 </table>
    </td>
</tr>