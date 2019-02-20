
<div class="order_box">
            <p class="wifiOrderTit">收件人信息</p>
             <#if personList?? && personList?size gt 0>
             	常用联系人:
				<div id="commonlyUsed" class="name_list_new">
						<#if personList?size &gt; 9>
							<a href="javascript:;" class="btn_qita js_name_shouqi" hidefocus="false">更多<i class="icon_arrow"></i></a>
						</#if>
						<#list personList as person>
							<label class="check" title=${person.fullName}>
								<input class="checkbox" type="checkbox" value="${person.receiverId}" name="contactsPersonCheckbox" personName="${person.fullName}"
									firstName="${person.firstName}" lastName="${person.lastName}" peopleType="${person.peopleType}"
									 expDate="<#if person.expDate??>${person.expDate?string("yyyy-MM-dd")}</#if>" issued="${person.issued}" birthday="${person.birthday}" gender="${person.gender}"
									mobile="${person.mobile}" idNo="${person.idNo}" idType="${person.idType}" email="${person.email}">
								${person.fullName}
							</label>
						</#list>
				    </div>
                </#if>
                <div class="user_info no_bd">
             		<div id="expressageInfoDiv">
		            	
		            </div>
		   
                </div>
                <#if product.productType =='WIFI'>
           		<#include "/order/wifi/inc/pickingPoint.ftl"/>
           		</#if>
        </div>