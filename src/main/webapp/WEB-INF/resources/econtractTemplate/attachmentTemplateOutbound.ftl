
 <p>
     	附件1：出境旅游报名表<br/>
        旅游线路及编号<em class="all-line" style="width:200px;">${travelContractVO.productName!''}    ${travelContractVO.productId!''}</em>旅游者出团意向时间	<em class="all-line" style="width:100px;">${travelContractVO.visitTime!''}</em><br/>
         
    <#list travelContractVO.ordTravellerList  as person> 
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
        	<tr>
            	<td width="100px">姓名</td>
                <td width="150px">${(person.fullName)!''}</td>
                <td width="80px">性别</td>
                <td width="80px">
                
                  <#if person.gender == "MAN"> 
                      	 男
									<#elseif person.gender == "WOMAN">
									女
									</#if>
                
                </td>
                <td width="80px">民族</td>
                <td width="80px"></td>
                <td width="120px">出生日期</td>
                <td width="200px">
                 <#if person.birthday?exists>
                         ${person.birthday?string('yyyy-MM-dd')}
                        </#if>
                        </td>
            </tr>
            <tr>
            	<td colspan="2">身份证件号码</td>
                <td colspan="4"> 
                
                
                  <#if person.idType == "ID_CARD"> 
                      	 ${person.idNo!''}
									</#if>
                
                
                </td>
                <td>联系电话</td>
                <td>${person.mobile!''}</td>
            </tr>
            <tr>

            	<td>国籍</td>
                <td></td>
                <td colspan="2">出境证件号</td>
                <td colspan="4">
                <#if person.idType != "ID_CARD"> 
                      	 ${person.idNo!''}
									</#if>
                
                </td>
            </tr>
            <tr>

            	<td colspan="2">身体状况</td>
                <td colspan="6" style="text-align:left;"><span>良好</span><br/></td>
            </tr>
            <tr>
            	<td colspan="8" style="text-align:left;">
                	旅游者全部同行人名单及分房要求（所列同行人均视为旅游者要求必须同时安排出团）：<br/>
                    <em class="all-line" style="width:50px;">　</em>与<em class="all-line" style="width:50px;">　</em>同住，
                    <em class="all-line" style="width:50px;">　</em>与<em class="all-line" style="width:50px;">　</em>同住，
                    <em class="all-line" style="width:50px;">　</em>与<em class="all-line" style="width:50px;">　</em>同住，<br/>
                    <em class="all-line" style="width:50px;">　</em>与<em class="all-line" style="width:50px;">　</em>同住，
                    <em class="all-line" style="width:50px;">　</em>与<em class="all-line" style="width:50px;">　</em>同住，
                    <em class="all-line" style="width:50px;">　</em>与<em class="all-line" style="width:50px;">　</em>同住，<br/>
                    <em class="all-line" style="width:50px;">　</em>为单男/单女需要安排与他人同住，<em class="all-line" style="width:50px;">　</em>不占床位，<br/>
                    <em class="all-line" style="width:120px;">　</em>全程要求入住单间（同意补交房费差额）。
                </td>
            </tr>
            <tr>
            	<td colspan="8" style="text-align:left;">
					其他补充约定：<br/>
                    <span></span><br/>
                    旅游者确认签名（盖章）：<em class="all-line" style="width:120px;">
                    ${person.fullName!''}
                    　</em>　　
                    <em class="all-line" style="width:70px;">${travelContractVO.createTime!''}</em>
                </td>
            </tr>
            <tr>
            	<td>备注</td>
                <td colspan="7" style="text-align:left">（年龄低于18周岁，需要提交监护人书面同意出行书）<br/><span></span></td>
            </tr>
            <tr>
                <td colspan="8" style="text-align:left">以　　下　　由　　旅       行　　社　　工　　作　　人　　员　　填　　写</td>
            </tr>
            <tr>
                <td colspan="2">服务网点名称</td>
                <td colspan="3"></td>
                <td colspan="2">出境社经办人</td>
                <td colspan="1"></td>
            </tr>
        </table>
     </#list>
      
              
              
              
     </p><br/><br/><br/><br/><br/>
     <p>
     	附件2：《旅游行程单》<br/>
     	行程单另附
        <span></span><br/>
        旅游者：（代表人签字）<em class="all-line" style="width:120px;">
         <#list travelContractVO.ordTravellerList  as person> ${person.fullName!''}</#list>
        </em>出境社：（盖章）<em class="all-line" style="width:120px;">　</em><br/>
        <span style="margin-left:272px;">经办人：（签字）<em class="all-line" style="width:120px;">　</em></span><br/>
        <span style="margin-right:212px;">
        	<em class="all-line" style="width:30px;">　</em>年
            <em class="all-line" style="width:30px;">　</em>月
            <em class="all-line" style="width:30px;">　</em>日
        </span>
     </p><br/><br/><br/><br/><br/>
      <p>
     	附件3：自愿购物活动补充协议<br/>
     	
     	
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
        	<tr>
            	<td colspan="7">自愿购物活动补充协议</td>
            </tr>
            <tr>
            	<td width="65px">具体时间</td>
                <td width="40px">地点</td>
                <td width="60px">购物场所名称</td>
                <td width="70px">主要商品信息</td>
                <td width="70px">最长停留</br>时间(分钟)</td>
                <td width="70px">其他说明</td>
                <td width="70px">旅游者</br>签名同意</td>
            </tr>
            <#if (travelContractVO.shopingDetailList)?? && (travelContractVO.shopingDetailList?size)!=0> 
            <#list travelContractVO.shopingDetailList as prodContractDetail> 
            <tr>
            	<td style="text-align:left;">
            	  <#if prodContractDetail.vistStartTime?exists>
                	${(prodContractDetail.vistStartTime?string('yyyy-MM-dd'))!''}
                  </#if>
                </td>
                <td><@func.addSpace prodContractDetail.address 8/></td>
                <td><@func.addSpace prodContractDetail.detailName 8/></td>
                <td><@func.addSpace prodContractDetail.detailValue 8/></td>
                <td><@func.addSpace prodContractDetail.stay 6/></td>
                <td><@func.addSpace prodContractDetail.other 10/></td>
                <td style="text-align:left;">签名：</td>
            </tr>
           </#list>
           
           <#else>
             <tr>
            	<td style="text-align:left;">
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：</td>
            </tr>
             <tr>
            	<td style="text-align:left;">
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：</td>
            </tr>
             <tr>
            	<td style="text-align:left;">
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：</td>
            </tr>
          
          </#if>
            
        </table><br/>
        出境社经办人签名：<em class="all-line" style="width:120px;">　</em><br/><br/><br/><br/><br/>
     </p><br/><br/><br/><br/><br/>
     <p>
     	附件4：自愿参加另行付费旅游项目补充协议<br/>
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
        	<tr>
            	<td colspan="7">自愿参加另行付费旅游项目补充协议</td>
            </tr>
            <tr>
            	<td width="65px">具体时间</td>
                <td width="40px">地点</td>
                <td width="70px">项目名称和内容</td>
                <td width="70px">费用（元）</td>
                <td width="70px">项目时长（分钟）</td>
                <td width="70px">其他说明</td>
                <td width="70px">旅游者<br/>签名同意</td>
            </tr>
            
             <#if  (travelContractVO.recommendDetailList)?? && (travelContractVO.recommendDetailList?size)!=0> 
             
	             <#list travelContractVO.recommendDetailList as prodContractDetail> 
		            <tr>
		            	<td style="text-align:left;">
		            		<#if prodContractDetail.vistStartTime?exists>
		                	${(prodContractDetail.vistStartTime?string('yyyy-MM-dd'))!''}
		                	</#if>
		                </td>
		                <td><@func.addSpace prodContractDetail.address 8/></td>
		                <td><@func.addSpace prodContractDetail.detailName 8/></td>
		                <td>${prodContractDetail.detailValue!''}</td>
		                <td>${prodContractDetail.stay!''}</td>
		                <td><@func.addSpace prodContractDetail.other 10/></td>
		                <td style="text-align:left;">签名：</td>
		            </tr>
	           </#list> 
           
           <#else>
             <tr>
            	<td style="text-align:left;">
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：</td>
            </tr>
             <tr>
            	<td style="text-align:left;">
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：</td>
            </tr>
             <tr>
            	<td style="text-align:left;">
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：</td>
            </tr>
          
          </#if>
        </table><br/>
        出境社经办人签名：<em class="all-line" style="width:120px;">　</em><br/><br/><br/><br/><br/>
     </p>
	 <p>
	附件5：中国公民出国（境）旅游文明行为指南<br/>
	 中国公民，出境旅游，注重礼仪，保持尊严。<br/>
	讲究卫生，爱护环境；衣着得体，请勿喧哗。<br/>
	尊老爱幼，助人为乐；女士优先，礼貌谦让。<br/>
	出行办事，遵守时间；排队有序，不越黄线。<br/>
	文明住宿，不损用品；安静用餐，请勿浪费。<br/>
	健康娱乐，有益身心；赌博色情，坚决拒绝。<br/>
	参观游览，遵守规定；习俗禁忌，切勿冒犯。<br/>
	遇有疑难，咨询领馆；文明出行，一路平安。<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	中央文明办 国家旅游局 
	 </p>	
	 
	  <h1 class="h1-title">
       订购清单
    </h1><br/>
    <table border="0" cellspacing="1" cellpadding="0" class="tab1">
    	<tr style="font-weight:bold;">
    	
    			<td style="width:100px;">子订单号</td>
    			<td style="width:100px;">类型</td>
        	<td >名称</td>
            <td width="15%">预订份数</td>
            <td width="15%">出游时间</td>
        </tr>
        <#list chidOrderMap?keys as testKey>  
        <#list chidOrderMap[testKey]  as orderMonitorRst> 
			               		<tr>
			                        
			              <td>
			              ${orderMonitorRst.orderId!''}        
						
									</td>
			                        <td>
			                         ${orderMonitorRst.childOrderTypeName!''}
									</td>
									
			                       
			                      <td>
			                      <@func.addSpace orderMonitorRst.productName 20/>
			                  		</td>
			                       <td> 
			                      
			                        <#if orderMonitorRst.childOrderType == 'category_cruise'> 
			                      	 ${orderMonitorRst.personCount!''} 人/ ${orderMonitorRst.buyCount!''} 间
														<#else>
														 ${orderMonitorRst.buyCount!''}份
														</#if>
														
									
			                      	
			                       </td>
			                       <td> ${orderMonitorRst.visitTime!''} </td>
			                    </tr>
			 </#list>
        </#list>
    </table>
    <br/><br/><br/><br/><br/>