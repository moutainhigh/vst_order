<p>
     	附件1：旅游报名表<br/>
        旅游线路及编号<em class="all-line" style="width:520px;">　${travelContractVO.productName!''}${travelContractVO.productId!''}</em>
        旅游者出团意向时间<em class="all-line" style="width:200px;">　${travelContractVO.visitTime!''}</em><br/>
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
          <#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0> 
          <#list travelContractVO.ordTravellerList  as person> 
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
                <td colspan="2">证件号码</td>
                <td colspan="4">${person.idNo!''}</td>
                <td>联系电话</td>
                <td>${person.mobile!''}</td>
            </tr>
            <tr>
                <td colspan="2">身体状况</td>
                <td colspan="6" style="text-align:left;"><span>良好</span><br/>（需注明是否有身体残疾、精神疾病、高血压、心脏病等健康受损病症、病史，是否为妊娠期妇女。）</td>
            </tr>
         </#list>
         <#else>
         <tr>
                <td width="100px">姓名</td>
                <td width="150px"></td>
                <td width="80px">性别</td>
                <td width="80px"></td>
                <td width="80px">民族</td>
                <td width="80px"></td>
                <td width="120px">出生日期</td>
                <td width="200px"></td>
            </tr>
            <tr>
                <td colspan="2">证件号码</td>
                <td colspan="4"></td>
                <td>联系电话</td>
                <td></td>
            </tr>
            <tr>
                <td colspan="2">身体状况</td>
                <td colspan="6" style="text-align:left;"><span></span><br/>（需注明是否有身体残疾、精神疾病、高血压、心脏病等健康受损病症、病史，是否为妊娠期妇女。）</td>
            </tr>
         </#if>
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
            	<td colspan="8" valign="bototm" style="text-align:left;">
					其他补充约定：<br/>
                    <span></span><br/>
                                                             旅游者确认签名（盖章）：<em class="all-line" style="width:70px;">
                 <#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
	        		<#list travelContractVO.ordTravellerList  as person>
	                    ${person.fullName!''}
	                </#list>
                <#else>
                	${(travelContractVO.travellers)!''}
                </#if>　</em>　　
                   <em class="all-line" style="width:70px;margin-left:300px;">　${travelContractVO.createTime!''}</em>
                </td>
                
            </tr>
            <tr>
            	<td>备注</td>
                <td colspan="7" style="text-align:left">（年龄低于18周岁，需要提交监护人书面同意出行书）<br/>(如游客众多，不足填写可附页)<span></span></td>
            </tr>
            <tr>
                <td colspan="8" style="text-align:left">以　　下　　由　　旅　　行　　社　　工　　作　　人　　员　　填　　写</td>
            </tr>
            <tr>
                <td colspan="2">服务网点名称</td>
                <td colspan="3"></td>
                <td colspan="2">旅行社经办人</td>
                <td colspan="1"></td>
            </tr>
        </table>  
       
           
     </p><br/><br/><br/><br/><br/>
     <p>
     	附件2：《旅游行程单》<br/>
        <span>行程单另附</span><br/>
        旅游者：（代表人签字）<em class="all-line" style="width:120px;">
                <#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
	        		<#list travelContractVO.ordTravellerList  as person>
	                    ${person.fullName!''}
	                </#list>
                <#else>
                	${(travelContractVO.travellers)!''}
                </#if></em>
        旅行社：（盖章）<em class="all-line" style="width:120px;"><#if travelContractVO.stampImage??> <img src="http://super.lvmama.com/vst_order/img/${(travelContractVO.stampImage)!''}" style="no-repeat;width:160px;height:160px;display:block;left:787px;padding-bottom：7px;"/></#if> </em><br/>
        <span style="margin-left:272px;">经办人：（签字）<em class="all-line" style="width:120px;">　</em></span><br/>
        <span style="float:right">
        	<em class="all-line" style="width:120px;">　${travelContractVO.createTime!''} </em>
        </span>
     </p><br/><br/><br/><br/><br/>
     <p>
     	附件3：<br/>
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
        	<tr>
            	<td colspan="7">自愿购物活动补充协议</td>
            </tr>
            <tr>
            	<td width="150px">具体时间</td>
                <td width="40px">地点</td>
                <td width="70px">购物场所名称</td>
                <td width="70px">主要商品信息</td>
                <td width="70px">最长停留时间（分钟）</td>
                <td width="70px">其他说明</td>
                <td width="200px">旅游者签名同意</td>
            </tr>
            
            <#if travelContractVO.shopingDetailList?? && travelContractVO.shopingDetailList?size gt 0> 
            <#list travelContractVO.shopingDetailList as prodContractDetail>
            <tr>
            	<td style="text-align:left;">
                	<em class="all-line" style="width:20px;">　</em>年
                    <em class="all-line" style="width:10px;">　</em>月
                    <em class="all-line" style="width:10px;">　</em>日
                </td>
                <td><@func.addSpace prodContractDetail.address 8/></td>
                <td><@func.addSpace prodContractDetail.detailName 8/></td>
                <td><@func.addSpace prodContractDetail.detailValue 8/></td>
                <td><@func.addSpace prodContractDetail.stay 6/></td>
                <td><@func.addSpace prodContractDetail.other 10/></td>
                <td>签字： <#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
	        		<#list travelContractVO.ordTravellerList  as person>
	                    ${person.fullName!''}
	                </#list>
                <#else>
                	${(travelContractVO.travellers)!''}
                </#if> </td>
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
        旅行社经办人签名：<em class="all-line" style="width:120px;">　</em><br/><br/><br/><br/><br/>
</p>
     <p>
     	附件4：<br/>
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
        	<tr>
            	<td colspan="7">自愿参加另行付费旅游项目补充协议</td>
            </tr>
            <tr>
            	<td width="150px">具体时间</td>
                <td width="40px">地点</td>
                <td width="70px">项目名称和内容</td>
                <td width="70px">费用（元）</td>
                <td width="70px">项目时长（分钟）</td>
                <td width="70px">其他说明</td>
                <td width="200px">旅游者签名同意</td>
            </tr>
            <#if travelContractVO.recommendDetailList?? && travelContractVO.recommendDetailList?size gt 0> 
            <#list travelContractVO.recommendDetailList as prodContractDetail> 
            <tr>
            	<td style="text-align:left;">
                	<em class="all-line" style="width:20px;">　</em>年
                    <em class="all-line" style="width:10px;">　</em>月
                    <em class="all-line" style="width:10px;">　</em>日
                </td>
                <td><@func.addSpace prodContractDetail.address 8/></td>
		        <td><@func.addSpace prodContractDetail.detailName 8/></td>
		        <td>${prodContractDetail.detailValue!''}</td>
		        <td>${prodContractDetail.stay!''}</td>
		        <td><@func.addSpace prodContractDetail.other 10/></td>
                <td >签名：</td>
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
         </table>
     <br/>
        旅行社经办人签名：<em class="all-line" style="width:120px;">　</em><br/><br/><br/><br/><br/>
     </p>
     <p>
         附件5：<br/>
     <table border="0" cellspacing="1" cellpadding="0" class="tab1">
         <tr>
             <td style="text-align:center;">产品信息描述说明</td>
         </tr>
         <tr>
             <td style="height: 600px;">
                <p> 
                  <@func.addSpace travelContractVO.supplementaryTerms 52/><br/>
                  
                </p>
             </td>
         </tr>
     </table>
     <br/>
     <br/><br/><br/><br/><br/>
     </p>
     <p>
         附件6：<br/>
     </p>
     <p style="text-align: center;color: #333;">补充条款<br/></p>
     <p>
         1、为办理甲方旅游的证件、预留机位、酒店及行程中交通等，甲方签订旅游合同时，应支付乙方旅游预付款，乙方应出<br/>
                                  具该款项的收款凭证。<br/>
         2、甲方对向乙方提供的办理参加旅游活动的所有资料真实性及完整性负责，并对签证审查中须增补材料的情形给予充分<br/>
                                  的理解和配合。<br/>
         3、甲方应根据自身健康状况报名参加旅游活动，甲方应在报名参团时向乙方提供个人正确信息（包括健康状况等信息或<br/>
                                  证明） 。若因甲方虚报、瞒报上述有关情况，一经发现或旅途中发生意外，由甲方承担全部责任和后果；且乙方有权<br/>
                                  单方面解除旅游合同，拒绝甲方继续参团；给乙方造成损失的，甲方应当承担赔偿责任。<br/>
         4、甲方报名参团发生自然单间或加床时需另补差价；甲方同意乙方可根据参团客源情况拼房；旅游行程中，甲方同意乙<br/>
                                   方 的拼房方案后，应对自身拼房风险和结果以及对拼房后的生活习惯差异等情况负责，若因甲方原因不成行的，应<br/>
                                   自行承担单房差的实际损失。<br/>
         5、甲方已清楚旅游行程中关于酒店等级标准的说明，在不使用国际统一标准的国家和地区以当地行业标准为准。<br/>
         6、<em style="font-style: normal;color: #333;">甲方已清楚且愿意遵守 ：</em>乙方提供的<br/>
                                    机票系特价舱位机票或乙方与航空公司约定不签转、 更改或退票的机票， 不得签转、 更改或退票。<br/>
         7、由公共交通经营者（包括但不限于航空公司，铁路公司、航运公司等）的原因造成甲方人身损害或财产损失的，由公<br/>
                                  共交 通经营者依法承担赔偿责任，乙方不承担责任，乙方应协助甲方向公共交通经营者索赔。<br/>
         8、<em style="font-style: normal;color: #333;">乙方 明确告知甲方：</em>乙方不组织行程<br/>
                                  单以外任何活动，应甲方的要求，安排购物或者另付费旅游项目的，经双方协商一致且不影响其他旅游者行程安排的除外。<br/>
                                  甲方在购物时应知晓商品的质量和价格，并获取相关凭证。<br/>
         9、甲方同意：乙方可在不影响和不降低行程单旅游景点内容及服务标准的情况下，对旅游行程作适当调整。<br/>
         10 、<em style="font-style: normal;color: #333;">乙方已提示甲方购买人身意外伤害保险和 <br/>
                                  旅游意外保险。经乙方推荐的保险，甲方已经阅读并明确知晓保险的保险条款及其保单内容，相关投保信息和约定以保单<br/>
                                  及其保险条款为准。</em><br/>
         11 、<em style="font-style: normal;color: #333;">具有下列情形的旅行社免责：</em><br/>
         （1）因甲方原因造成自己人身损害、财产损失或造成他人损失的，由甲方承担相应责任，但乙方应协助处理。<br/>
         （2）因不可抗力造成甲方人身损害、财产损失的，乙方不承担赔偿责任，但应积极采取救助措施。<br/>
         （3）在自行安排活动期间甲方人身、财产权益受到损害的，乙方在事前已尽到必要警示说明义务且事后已尽到必要救助<br/>
         义 务的，乙方不承担赔偿责任。<br/>
         （4）甲方因参加非乙方安排或推荐的活动导致人身损害、财产损失的，乙方不承担赔偿责任。<br/>
         （5）由于公共交通经营者的原因造成甲方人身损害、财产损失的，由公共交通经营者依法承担赔偿责任，乙方应当协助<br/>
                   甲  方向公共交通经营者索赔。因公共交通工具延误，导致合同不能按照约定履行的，乙方不承担违约责任，但应向<br/>
                   甲方退还未实际发生的费用。<br/>
         12 、<em style="font-style: normal;color: #333;">关于自愿购物和参加另行付费旅游项目约定如下：</em><br/>
         （1）甲方可以自主决定是否参加乙方安排的购物活动、另行付费旅游项目；<br/>
         （2） 乙方可以在不以不合理的低价组织旅游活动、 不诱骗旅游者、 不获取回扣等不正当利益， 且不影响其他旅游者行
         程安排的前提下，按照平等自愿、诚实信用的原则，与甲方协商一致达成购物活动、另行付费旅游项目协议；<br/>
         （3）购物活动、另行付费旅游项目安排应不与《行程单》冲突；<br/>
         （4） 购物活动、另行付费旅游项目具体约定见《自愿购物活动补充协议》 （附件 3） 、 《自愿参加另行付费旅游项目补充协
         议》 （附件 4） ，本补充协议关于自愿购物和另行付费旅游项目的约定与《自愿购物活动补充协议》 、 《自愿参加另行付费旅游
         项目补充协议》约定不一致的，以《自愿购物活动补充协议》 、 《自愿参加另行付费旅游项目补充协议》的约定为准。<br/>
         13 、<em style="font-style: normal;color: #333;">甲乙 双方 一致同意：</em>本次行程若系“自由行”产品 ( 以乙方网站展示的产品性质为唯一标准 ) 的 , 甲方要求乙方不
         再安排导游人员 ,因无乙方人员陪同出行的各项协助工作均由甲方自行完成。<br/>
         14、若甲乙双方有违反本补充条款之约定的，均可参照主合同违约条款追究违约责任。<br/>
         15 、 <em style="font-style: normal;color: #333;">甲方</em> 同意：甲方签收的《出团通知书》及其行程附件作为本合同组成部分，与合同具有同等效力。<br/>
         16 、 <em style="font-style: normal;color: #333;">甲乙双方一致同意 </em>：电子版旅游合同自旅行社明确告知旅游者其所预订的旅游产品 / 服务可提供且旅游者已付清全部费用时生效。<br/>
         17 、<em style="font-style: normal;color: #333;">旅游合同签署的客人以及代表人阅读上述《补充条款》后，经公司人员详细解释说明，已完全知晓上述补充条款的全部内容；并愿意遵守和同意将此《补充条款》作为旅游合同附件，与旅游合同具有同等效力。且旅游合同的签署代表对自身的代理权真实性和合法性承担法律责任，并承担负责转告未签名旅游者上述补充条款全部内容的责任。</em><br/>
         <br/>
         <br/>
         <br/>
         <br/>
         <span style="float:right">签字：<em class="all-line" style="width:150px">
         		<#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
	        		<#list travelContractVO.ordTravellerList  as person>
	                    ${person.fullName!''}
	                </#list>
                <#else>
                	${(travelContractVO.travellers)!''}
                </#if></em></span>
         <br/>
         <br/>
         <br/>
         <br/>
     </p>
     <p>
         附件7：<br/>
     </p>
     <p style="text-align: center;color: #333;">旅游安全提示警示告知书<br/></p>
     <p>
         <em style="font-style: normal;color: #333;">尊敬的游客：</em><br/>
         为了确保您的旅游行程安全顺利，我公司特就旅游行程中应注意的安全事项，向您提示、警示并明确告知，请您仔细阅读。<br/>
         一、旅游出行前建议购买旅游者人身意外伤害保险，并请您出行前告知家人参团旅行社名称及行程、紧急联系电话等。<br/>
         二、临行前确保身体健康，如有体质较弱或者曾患病者必须坚持治疗，防止旧病复发；平时需要用药治疗者，出游时请带足所需药品；旅行 社不建议患有高血压、心脏病、糖尿病以及身体残疾等身体状况不适宜旅游的客人参团出游，如执意参加者须征得医生同意，自备药品，或 者征得家属同意或由家属陪伴；如因自身原因发生任何意外情形，责任自负，公司按客人书面请求给予协助。<br/>
         三、70 岁以上老年人、18 周岁以下未成年人参团旅游的，须由家属陪同出游，并做好安全防护措施，保证前述人员的安全。否则，因其自 身原因导致的任何意外情形，责任自负，公司根据客人的书面请求给予协助。未有家人同行的情况下，公司可根据实际情况决定是否接受前 述人员报名参团； 如获准参加， 前述人员的安全及因其自身原因导致的任何意外情形，责任自负。<br/>
         四、对于有身孕（5 个月以下）的客人（怀孕 5 个月以上，边检将限制出入境。若虚报或隐瞒怀孕月份，在出入境边检时受阻，其后果自负） ， 因在旅途中存在诸多对孕妇不利因素，如长途乘机、坐车及颠簸震动、景点上下、气候变化等，故公司不建议参团出游，尤其是出境旅游； 若客人隐瞒孕情或坚持参团，须对自身健康和孕情负责；若在旅游行程中发生任何意外情形，责任自负，公司按客人书面请求给予协助。<br/>
         五、<em style="font-style: normal;color: #333;">公司郑重提醒客人：</em>根据自身健康状况，谨慎参加赛车、骑马、攀岩、滑翔、探险、漂流、潜水、游泳、滑雪、滑冰、滑板、跳伞、热气球、 蹦极、冲浪等高风险活动或不在公司旅游行程内的活动；游客在自由活动时间内须选择自身能够控制风险的活动及旅游项目， 并在自己能够 控制风险的范围内进行活动； 若客人违反本安全警示告知而所导致的人身伤害或财产损失，公司不承担赔偿责任。<br/>
         六、<em style="font-style: normal;color: #333;">公司郑重提示警示自由行旅游客人：</em>请您在自由行旅游过程中，务必充分了解拟参与的旅游活动或项目的内容及风险，并根据自身身体状况和健康状况慎重评估后选择，自觉遵守该旅游活动或项目的安全规定。公司对您违反本提示所发生的任何意外事故不承担任何责任。<br/>
         七、<em style="font-style: normal;color: #333;">公司郑重提示警示客人：</em>旅游中，客人在乘坐任何交通工具（包括车辆、游船、游艇以及任何游览项目中承载游客的设施或设备）时， 不得将身体任何部位置于交通工具外；并正确使用所配备的所有安全设施和设备；客人未经驾驶人员或管理人员或导游及领队许可，在交通 工具运行时且乘坐期间，客人的身体各部位不得擅自以任何方式脱离座位；若客人违反本提示警示而造成人身或财产损害时，责任自负。<br/>
         八、<em style="font-style: normal;color: #333;">公司郑重提示告知客人：</em>凡参加涉及海边旅游时，不得在非指定区域游泳或嬉水、须根据自身水性和身体及健康状况掌握游泳距离及下 水时间；禁止患高血压、心脏病、中耳炎、急性眼结膜炎、孕妇等客人游泳；禁止剧烈运动后游泳和长时间曝晒游泳及游泳后立即进食；若客人不遵守本提示而导致意外发生,将自行承担后果。<br/>
         九、客人在旅游中须严防“病从口入” ，特别在选择公司安排的餐饮以外的就餐或选购食品时，请注意饮食卫生，防止消化不良、病毒性肝炎、痢疾、伤寒等肠道传染病；品尝海鲜、风味小吃、土特产等食物时，应视本人肠胃状况酌量适用，并应事先了解不宜同时饮用的忌口。非公司安排就餐引起的疾病，公司不承担任何责任。<br/>
         十、<em style="font-style: normal;color: #333;">公司提示客人：</em>客人有义务保管好自身携带的货币，证件和物品；在离开酒店或交通工具、以及购物或游览期间，应将货币和贵重物品随身携带；并须自身做好防盗防窃安全措施。若客人携带财产遭受损失时，应立即报警报案，公司予以协助。<br/>
         十一、<em style="font-style: normal;color: #333;">公司提示客人：</em>客人在游览、观光过程中应谨慎拍照、摄像，在拍照、摄像时应注意往来车辆、所处位置进行拍照、摄像是否有危险 或是否有禁拍标志， 切忌在可能有危险或设有危险标志的地方停留。 旅游者在行程中发现自身权益受到侵害，应及时告知领队、导游以及旅 游者的紧急联系人，因没有及时提出而致损失扩大的，游客自负。<br/>
         十二、<em style="font-style: normal;color: #333;">旅游合同签署的客人以及代表人阅读上述 《旅游安全提示警示告知书》后，经公司人员详细解释说明，已完全知晓上述安全提示警示 告知的全部内容；并愿意遵守和同意将此告知书作为旅游合同附件，与旅游合同具有同等效力。且旅游合同的签署代表对自身的代理权真实 性和合法性承担法律责任，并承担负责转告未签名旅游者上述安全提示警示告知全部内容的责任。</em><br/>
         <br/>
         <br/>
         <br/>
         <br/>
         <span style="float:right">签字：<em class="all-line" style="width:150px">
         		<#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
	        		<#list travelContractVO.ordTravellerList  as person>
	                    ${person.fullName!''}
	                </#list>
                <#else>
                	${(travelContractVO.travellers)!''}
                </#if></em></span>
         <br/>
         <br/>
         <br/>
         <br/>
     </p>
     <p>
         附件8：<br/>
         <br/>
         <br/>
         <br/>
         <span style="text-align: center;display: block;">
             <em style="font-style: normal;color: #333;">中国公民文明旅游公约</em><br/><br/>
             重安全，讲礼仪；<br/><br/>
             不喧哗，杜陋习；<br/><br/>
             守良俗，明事理；<br/><br/>
             爱环境，护古迹；<br/><br/>
             文明行，最得体。<br/><br/>
         </span>
         <br/>
         <br/>
         <span style="text-align:right;display: block;padding-right: 160px;">
             中央文明办 国家旅游局<br/><br/>
             二〇一六年八月三日<br/>
         </span>
         <br/>
         <br/>
         <br/>
         <br/>
         <br/>
         <br/>
         <br/>
         <br/>
     </p>