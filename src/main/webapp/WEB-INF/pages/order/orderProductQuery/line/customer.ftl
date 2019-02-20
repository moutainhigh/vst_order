<div class="p_box">
        	<div id="touristError"></div>
            <table class="p_table table_center touristInfo">
                <thead> 
                    <tr class="noborder">
                        <th colspan="10" style=" text-align:left;">游客信息</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
					<td colspan="10" style=" text-align:left;"><label class="checkbox mr10 fleft";>常用游客：</label>
					<#list personList as person > 
					<label class="checkbox mr10 fleft">
					<input type="checkbox" class="cyyk" style="margin-top:4px;" value="${person.receiverId}"/>${person.fullName}
					<input type="hidden" id="fullName${person.receiverId}" value="${person.fullName}"/>
					<input type="hidden" id="firstName${person.receiverId}" value="${person.firstName}"/>
					<input type="hidden" id="lastName${person.receiverId}" value="${person.lastName}"/>
					<input type="hidden" id="idType${person.receiverId}" value="${person.idType}"/>
					<input type="hidden" id="idNo${person.receiverId}" value="${person.idNo}"/>
					<input type="hidden" id="expDate${person.receiverId}" value="${person.expDate}"/>  
					<input type="hidden" id="issued${person.receiverId}" value="${person.issued}"/>
					<input type="hidden" id="birthday${person.receiverId}" value="${person.birthday}"/>
					<input type="hidden" id="peopleType${person.receiverId}" value="${person.peopleType}"/>
					<input type="hidden" id="gender${person.receiverId}" value="${person.gender}"/>
					<input type="hidden" id="mobile${person.receiverId}" value="${person.mobile}"/>
					<input type="hidden" id="receiverId${person.receiverId}" value="${person.receiverId}"/>
					</label>
					</#list>
					</td>
                    </tr>
                    <tr>
                        <td>联系人</td>
                        <td>中文姓名</td>
                        <td>英文姓</td>
                        <td>英文名</td>
                        <td>证件类型</td>
						<td>证件号码</td>
						<td>有效期</td>
                        <td>签发地</td>
                        <td>游客类型</td>
                        <td>性别</td>
						<td colspan="2">联系电话</td>
                    </tr>
                </tbody>
            </table>
        </div>
        <!--这里是游客信息 -->