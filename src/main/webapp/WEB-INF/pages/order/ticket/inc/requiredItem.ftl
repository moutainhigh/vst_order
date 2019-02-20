<tr>
	<td  colspan="2">
     <table class="e_table" name="userInfoTb" index="${number_index}">
		<tbody>
			<tr>
				 <td class="e_label"><b>游玩人${number}</b>：</td>
				<td>
					常用联系人：
                	<#if personList??>
                	<#list personList as person>
                    	<label class="checkbox mr10">
                    	<input class="checkbox" type="checkbox" name="travellers[${number_index}].receiverId" value="${person.receiverId}" personName="${person.fullName}"
								mobile="${person.mobile}" idNo="${person.idNo}" expDate="<#if person.expDate??>${person.expDate?string("yyyy-MM-dd")}</#if>" issued="${person.issued}" idType="${person.idType}" email="${person.email}" firstName="${person.firstName}" 
								lastName="${person.lastName}" peopleType="${person.peopleType}">${person.fullName}</label>
                    </#list>
                    </#if>
				</td>
			</tr>
			 <tr>
                <td class="e_label td_top">中文姓名：</td>
                <td>
					<input type="text" id="fullName${number_index}" name_type="fullName" name="travellers[${number_index}].fullName" 
					 placeholder="姓名" onchange="" travellersId="" required=true/>
					 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>中文姓名不正确</span>
                </td>
            </tr>
            <#if number==1>
            	<#if orderRequiredvO.ennameType=="TRAV_NUM_ONE" || orderRequiredvO.ennameType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">英文姓名：</td>
		        	<td>
		        		<input class="input js_yz" name="travellers[${number_index}.firstName" name_type="firstName" type="text" placeholder="firstName" required=true>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>firstName不正确</span>
		        		<input class="input js_yz" name="travellers[${number_index}.lastName"  name_type="lastName" type="text" placeholder="lastName" required=true>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>lastName不正确</span>
		        	</td>
		          </tr>
		    	</#if>
		    	<#if orderRequiredvO.occupType=="TRAV_NUM_ONE" || orderRequiredvO.occupType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">人群：</td>
		        	<td>
		        		 <select class="select" name="travellers[${number_index}].peopleType" id="peopleType${number_index}" >
						       	<option value="PEOPLE_TYPE_ADULT">成人</option>
					       		<option value="PEOPLE_TYPE_CHILD">儿童</option>
			           	</select>
		        	</td>
		          </tr>
		    	</#if>
            	 <#if orderRequiredvO.phoneType=="TRAV_NUM_ONE" || orderRequiredvO.phoneType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">手机号码：</td>
		        	<td>
		        		<input id="mobile${number_index}"_name="mobile" name="travellers[${number_index}].mobile" id="mobile${number_index}"
		        		 maxlength="11" type="text" placeholder="手机号码" name_type="mobile" required=true>
		        		 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>手机号码不正确</span>
		        		<span class="ts_text">此手机号为接收短信所用，作为订购与取票凭证，请准确填写。</span>
		        	</td>
		          </tr>
		    	</#if>
		    	<#if orderRequiredvO.emailType=="TRAV_NUM_ONE" || orderRequiredvO.emailType=="TRAV_NUM_ALL">
		    		<tr>
		        	<td class="e_label td_top">邮箱地址：</td>
		        	<td>
		        		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" type="text" 
		        		id="email${number_index}" placeholder="邮箱" name_type="email" required=true>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮箱地址不正确</span>
		        		<span class="ts_text">此邮箱地址为接收邮件所用，作为订购与取票凭证，请准确填写。</span>
		       		</td>
		       		</tr>
			    </#if>
			    <#if orderRequiredvO.idNumType=="TRAV_NUM_ONE" || orderRequiredvO.idNumType=="TRAV_NUM_ALL">
			    	<tr>
			    	<td class="e_label">证件类型：</td>
			        <td>
			        		<select class="select" id="idType${number_index}" name="travellers[${number_index}].idType" id="idType${number_index}">
			        			<#if orderRequiredvO.idFlag=="Y">
							       <option value="ID_CARD">身份证</option>
							    </#if>
							    <#if orderRequiredvO.passportFlag=="Y">
							       <option value="HUZHAO">护照</option>
							    </#if>
							    <#if orderRequiredvO.passFlag=="Y">
							       <option value="GANGAO">港澳通行证</option>
							    </#if>
								 <#if orderRequiredvO.twPassFlag=="Y">
							       <option value="TAIBAO">台湾通行证</option>
							    </#if>	    
					       		 <#if orderRequiredvO.twResidentFlag=="Y">
							       <option value="TAIBAOZHENG">台胞证</option>
							    </#if>
							    <#if orderRequiredvO.hkResidentFlag=="Y">
							       <option value="HUIXIANG" >回乡证</option>
							    </#if>
							    <#if orderRequiredvO.birthCertFlag=="Y">
							       <option value="CHUSHENGZHENGMING">出生证明</option>
							    </#if>
							    <#if orderRequiredvO.householdRegFlag=="Y">
							       <option value="HUKOUBO">户口簿</option>
							    </#if>
							    <#if orderRequiredvO.soldierFlag=="Y">
							       <option value="SHIBING">士兵证</option>
							    </#if>
							    <#if orderRequiredvO.officerFlag=="Y">
							       <option value="JUNGUAN">军官证</option>
							    </#if>
			           		</select>
			        		<input type_name="shenfenzheng" id="idNo${number_index}" name="travellers[${number_index}].idNo" type="text" maxlength="25" placeholder="证件号码" name_type="idNo" required=true>
			        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>证件错误</span>
			        		<span class="ts_text">取票时需出示身份证确认。</span>
			        </td>
			        </tr>
			    </#if>
            </#if>
            <#if number!=1>
            	<#if orderRequiredvO.ennameType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">英文姓名：</td>
		        	<td>
		        		<input class="input js_yz" name="travellers[${number_index}.firstName" type="text" placeholder="firstName" required=true name_type="firstName">
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>firstName不正确</span>
		        		<input class="input js_yz" name="travellers[${number_index}.lastName" type="text" placeholder="lastName" required=true name_type="lastName">
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>lastName不正确</span>
		        	</td>
		          </tr>
		    	</#if>
		    	<#if orderRequiredvO.occupType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">人群：</td>
		        	<td>
		        		<select class="select" name="travellers[${number_index}].peopleType" id="peopleType${number_index}" >
						       	<option value="PEOPLE_TYPE_ADULT">成人</option>
					       		<option value="PEOPLE_TYPE_CHILD">儿童</option>
			           	</select>
		        	</td>
		          </tr>
		    	</#if>
            	 <#if orderRequiredvO.phoneType=="TRAV_NUM_ALL">
            	  <tr>
		        	<td class="e_label td_top">手机号码：</td>
		        	<td>
		        		<input id="mobile${number_index}"_name="mobile" name="travellers[${number_index}].mobile" id="mobile${number_index}"
		        		 maxlength="11" type="text" placeholder="手机号码" required=true name_type="mobile">
		        		 <span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>手机号码不正确</span>
		        		<span class="ts_text">此手机号为接收短信所用，作为订购与取票凭证，请准确填写。</span>
		        	</td>
		          </tr>
		    	</#if>
		    	<#if orderRequiredvO.emailType=="TRAV_NUM_ALL">
		    		<tr>
		        	<td class="e_label td_top">邮箱地址：</td>
		        	<td>
		        		<input type_name="email" id="email${number_index}" name="travellers[${number_index}].email" type="text" id="email${number_index}" placeholder="邮箱" name_type="email" required=true>
		        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>邮箱地址不正确</span>
		        		<span class="ts_text">此邮箱地址为接收邮件所用，作为订购与取票凭证，请准确填写。</span>
		       		</td>
		       		</tr>
			    </#if>
			    <#if orderRequiredvO.idNumType=="TRAV_NUM_ALL">
			    	<tr>
			    	<td class="e_label td_top"><span class="red">*</span>证件类型：</td>
			        <td>
			        		select class="select" id="idType${number_index}" name="travellers[${number_index}].idType" id="idType${number_index}">
			        			<#if orderRequiredvO.idFlag=="Y">
							       <option value="ID_CARD">身份证</option>
							    </#if>
							    <#if orderRequiredvO.passportFlag=="Y">
							       <option value="HUZHAO">护照</option>
							    </#if>
							    <#if orderRequiredvO.passFlag=="Y">
							       <option value="GANGAO">港澳通行证</option>
							    </#if>
								 <#if orderRequiredvO.twPassFlag=="Y">
							       <option value="TAIBAO">台湾通行证</option>
							    </#if>	    
					       		 <#if orderRequiredvO.twResidentFlag=="Y">
							       <option value="TAIBAOZHENG">台胞证</option>
							    </#if>
							    <#if orderRequiredvO.hkResidentFlag=="Y">
							       <option value="HUIXIANG" >回乡证</option>
							    </#if>
							    <#if orderRequiredvO.birthCertFlag=="Y">
							       <option value="CHUSHENGZHENGMING">出生证明</option>
							    </#if>
							    <#if orderRequiredvO.householdRegFlag=="Y">
							       <option value="HUKOUBO">户口簿</option>
							    </#if>
							    <#if orderRequiredvO.soldierFlag=="Y">
							       <option value="SHIBING">士兵证</option>
							    </#if>
							    <#if orderRequiredvO.officerFlag=="Y">
							       <option value="JUNGUAN">军官证</option>
							    </#if>
			           		</select>
			        		<input type_name="shenfenzheng" id="idNo${number_index}" name="travellers[${number_index}].idNo" maxlength="25" type="text" placeholder="证件号码" required=true name_type="idNo">
			        		<span class="error_text" style="display:none;"><i class="tip-icon tip-icon-error"></i>证件错误</span>
			        		<span class="ts_text">取票时需出示身份证确认。</span>
			        </td>
			        </tr>
			    </#if>
            </#if>
		</tbody>
	 </table>
    </td>
</tr>