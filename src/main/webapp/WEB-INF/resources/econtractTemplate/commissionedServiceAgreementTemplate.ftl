<html xmlns="http://www.w3.org/1999/xhtml"> 
 <head> 
  <#import "func.html" as func/> 
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
 <title>《委托服务协议》模板（驴妈妈2016版）</title>
 <style type="text/css"> 
	* {margin:0;padding:0;}
	body{font-family: SimSun;padding:0 40px;}
	.txt-hetong {color:#666;width:635px;margin:10px auto;text-align:center;font:12px/1.5 arial,SimSun;}
	.stamp{background:url(http://super.lvmama.com/vst_order/img/${(travelContractVO.stampImage)!''}) no-repeat;width:160px;height:160px;display:block;position:absolute;left:420px;top:0px;}
	.txt-hetong h1,.st2 {font:700 18px/2 SimSun;color:#333;}
	.txt-hetong .st2 {display:block;}
	.txt-hetong .st3 {display:block;text-align:center;font:700 14px SimSun;padding-bottom:5px;}
	.txt-hetong h1.h1-title {font:700 24px/2 SimSun;}
	.txt-hetong strong.st1 {font:14px/1.5 SimSun;color:#333;display:block;}
	.txt-hetong b {color:#333;font-weight:700;}
	.txt-hetong p {text-align:left;overflow:hidden; zoom:1;}
	.txt-hetong h2 {text-align:left;font:700 14px/2 SimSun;color:#333;margin-top:10px;}
	.txt-hetong .txt-right {float:right;}
	.txt-hetong .txt-right2{float:right;color:#333;font:16px/2 SimSun; clear:both;}
	.txt-hetong .all-line {font-style:normal;display:inline-block;border-bottom:1px solid #666;padding:0 5px;margin:0 5px;color:#333}
	.txt-hetong .all-line1 {width:50px;}
	.txt-hetong .all-line2 {width:255px;}
	.txt-hetong .all-line3 {width:80%;}
	.txt-hetong .b1 {font-family:SimSun;font-size:15px;}
	.txt-hetong .b2 {font:15px/1.5 SimSun;}
	.txt-hetong .kaiti {font-family:SimSun;font-size:16px;}
	.txt-hetong .kaiti1 {font-family:SimSun;font-size:18px;}
	table {font-size:12px;color:#666}
	.tab1 {border-collapse:collapse;background-color:#ccc;width:90%;margin:10px auto;}
	.tab1 td{border:1px solid #ccc;}
	.tab1 td,.tab1 th {padding:5px;text-align:center;vertical-align:middle;background-color:#fff;}
	.ul1 {width:90%;border-left:1px solid #ccc;border-top:1px solid #ccc;margin:10px auto;list-style:none;}
	.ul1 li {border-right:1px solid #ccc;border-bottom:1px solid #ccc;height:18px;font:14px/18px SimSun;text-align:left;text-indent:3em;}
	.text1 {width:250px;margin:20px auto;height:80px;font:14px SimSun;color:#333;}
	.text1 span {display:block;text-align:left;}
	.text1 .textLeft {float:left;width:150px;line-height:30px;}
	.text1 .textRight {float:left;width:100px;line-height:60px;}
	.ul2 {width:635px;list-style:none;}
	.ul2 li {width:317px;float:left;text-align:left;}
	.div1 {background-color: #FFFFFF;width:15px; height:15px;display:block; border: #000000 inset 1px;}
	.buchongbox{padding:9px 9px 18px;border:2px solid #ddd;display:block;}
 </style> 
 </head> 

 <body> 
 <div class="txt-hetong">
     <br/>
     <br/>
    <h1 class="h2-title">
        委托服务协议书使用说明
    </h1>
    <p>
     1. 本协议所涉及的相关产品订单，均由 “驴妈妈旅游网” 合作的供应商提供，“驴妈妈旅游网”仅作为网站服务运营商。<br/>
     2. “驴妈妈旅游网”页面所显示的“费用说明”、“重要提示”作为本协议的有效法律条款，具有同等法律效力。<br/>
     3. 委托方在“驴妈妈旅游网”在线订购产品或委托其客服工作人员电话订购的，就本协议所需填写部分可以通过在线填写电子订单或口头委托填写等方式确认协议条款并依 “驴妈妈旅游网”的预订流程体现至在线本协议文本中，对双方均有法律上的强制约束力。
     <#if travelContractVO?? && travelContractVO.prodProduct?? && travelContractVO.prodProduct.bizCategoryId=="16" && travelContractVO.prodProduct.producTourtType=="ONEDAYTOUR">
     4、本产品委托社（供应方）为【${travelContractVO.suppSupplier.supplierName}】，具体的旅游服务及操作由委托社（供应方）提供。<br>
     </#if>
     </p>
     <#if travelContractVO?? && travelContractVO.prodProduct??>
     <#if travelContractVO.prodProduct.bizCategoryId=="16" && travelContractVO.prodProduct.producTourtType=="ONEDAYTOUR">
      <#if travelContractVO.lineRoute?? && travelContractVO.lineRoute.prodLineRouteDetailList??>
	   <#list  travelContractVO.lineRoute.prodLineRouteDetailList?sort_by("nDay")  as prodLineRouteDetail>
	  </br>
	  </br>
	   <b>行&nbsp;&nbsp;&nbsp;&nbsp;程</b>
       <P>第${prodLineRouteDetail.nDay!''}天 &nbsp;&nbsp;${prodLineRouteDetail.title!''}</p></br>
       <#list prodLineRouteDetail.content?split("\n") as content>
       <#if content?length lt 60>
       <p>${content}<p>
       <#else>
        <p>
        <#assign counts=content?length/60 />
        <#list 0..counts as i >
        <#assign end=0/>
        <#if 60*(i+1) lt content?length>
         <#assign end=60*(i+1)/>
        <#else>
          <#assign end=content?length/>
        </#if>
        ${content?substring(60*i,end)}</br>
       </#list>
        </p>
       </#if>
       </#list>
       <p>以上行程时间安排可能会因天气、路况等原因做相应调整，敬请谅解<p>
      </#list>
      </#if>
     </#if>
    </#if>
    <br/><br/>
     <span style="float:right">合同编号：<em class="all-line" style="width:200px">${(travelContractVO.contractVersion)!''}　</em></span>
    <br/>
    <br/>
    <br/>
    <br/>
    <br/>
    <h1 class="h1-title" style="line-height:30px;">
        委托服务协议书
    </h1>
    <br/>
    <p>
        <b>委托方（以下简称甲方）：</b><em class="all-line" style="width:220px">
            <#if 'true' == travelContractVO.parentageGroup>
                <#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
                    <#if order.contactPerson??>${(order.contactPerson.fullName)}</#if>
                    <#list travelContractVO.ordTravellerList  as person>
                        ${person.fullName!''}
                    </#list>
                <#else>
                    ${(travelContractVO.suitableName)!''}
                </#if>
            <#else>
                <#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
	        		<#list travelContractVO.ordTravellerList  as person>
	                    ${person.fullName!''}
	                </#list>
                <#else>
                	${(travelContractVO.travellers)!''}
                </#if>
            </#if></em>
        <b>联系电话：</b><em class="all-line" style="width:150px">　<#if travelContractVO.contractVersion != null>${(order.contactPerson.mobile)!'/'} </#if></em><br>
        <b>受托方（以下简称乙方）：</b><em class="all-line" style="width:220px">${travelContractVO.filialeName}　</em>
        <b>联系电话：</b><em class="all-line" style="width:150px">${'1010-6060'}　</em><br>
        <br/>
        本协议系乙方代为甲方办理门票、往返机票、签证及酒店预订、机场接送等各类委托预订服务，且为了明确双方的权利义<br/>
        务，本着平等协商的原则，就有关事项达成的如下协议：<br/>
        <b>第一条 承诺与保证</b><br/>
        （一）乙方保证是具有国家旅游局认可的具有旅游资质的旅行社。<br/>
        （二）乙方仅为甲方提供本协议约定的服务事项，其他甲方在境内或境外期间自行安排的内容（包括但不限于参加旅游<br/>
        项目、购物、出行等）或尚未安排的内容全部由甲方自行承担费用、责任及风险，与乙方无关。<br/>
        （三）甲方在“驴妈妈旅游网”订购完成后，因自身原因而未能实际实现所委托事项部分或全部时，将自行承担责任。<br/>
        <b>（四）乙方明确告知甲方，无论在境内或境外的自行安排活动期间，请甲方在自己能够控制风险的范围内活动，所产生的<br/>
        责任自负；甲方已明知并愿意自觉遵守。</b><br/>
        （五）甲方就本协议的委托事项已作详尽了解，并自愿接受本协议内容。<br/>
        <b>第二条 申请与交易</b><br/>
        （一）甲方以书面、电话、网络等方式（包括但不限于通过“驴妈妈旅游网”在线或电话预订等方式）向乙方表明委托事项 (以“驴妈妈旅游网”预订流程中的订单信息为准）；甲方提交的其它书面委托事项有同等法律效力。甲方在实际实现了<br/>
        乙方委托事项后（完成订购），乙方按照本协议的受托及服务义务即刻履行完毕。<br/>
        （二）乙方在签订本协议时，对甲方委托事项中涉及的目的地、日期、标准、项目、安全须知等已作如实详细说明和报价，<br/>
        甲方确认已知悉。<br/>
        （三）甲、乙双方对本协议委托事项及服务费用已达成共识。<br/>
        （四）甲方自愿按委托事项交齐费用后，由乙方出具发票等文件，如需快递，费用由甲方承担。<br/>
        <b>第三条 委托信息及事项要求</b><br/>
        （一）实际出行者信息<br/>
     <table border="0" cellspacing="1" cellpadding="0" class="tab1">
     <tr>
         <td>姓名</td>
         <td>性别</td>
         <td>证件类型</td>
         <td>证件号</td>
     </tr>
     <#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
     <#list travelContractVO.ordTravellerList as listOrdTraveller>
     <tr>
         <td style="height: 30px;">${listOrdTraveller.fullName}</td>
         <td><#if listOrdTraveller.gender == "MAN"> 男<#elseif listOrdTraveller.gender == "WOMAN">女</#if></td>
         <td>
                <#if listOrdTraveller.idType == "ID_CARD">身份证</#if>
			    <#if listOrdTraveller.idType == "ERTONG">儿童无证件</#if>
			    <#if listOrdTraveller.idType == "GANGAO">港澳通行证</#if>
				<#if listOrdTraveller.idType == "HUIXIANG">回乡证</#if>
				<#if listOrdTraveller.idType == "HUZHAO">护照</#if>
				<#if listOrdTraveller.idType == "JUNGUAN">军官证</#if>
				<#if listOrdTraveller.idType == "TAIBAO">台胞证</#if>
				<#if listOrdTraveller.idType == "OTHER">其它</#if>
         </td>
         <td>${listOrdTraveller.idNo!''}</td>
     </tr>
     </#list>
     <#else>
     <tr>
         <td style="height: 30px;"></td>
         <td></td>
         <td></td>
         <td></td>
     </tr>
     </#if>
     
     </table>
     （二）甲方委托乙方代订服务项目信息如下：<br/>
     （表内为订购清单）<br/>
     <table border="0" cellspacing="1" cellpadding="0" class="tab1">
         <tbody>
         <tr>
             <td colspan="5"><b>订购清单</b></td>
         </tr>
         <td>子订单号</td>
    	 <td>类型</td>
         <td>名称</td>
         <td width="15%">预订份数</td>
         <td width="15%">出游时间</td>
         
         <#list chidOrderMap?keys as testKey>  
         <#list chidOrderMap[testKey]  as orderMonitorRst> 
		<tr>			                        
		 <td>${orderMonitorRst.orderId!''}</td>
		 <td>${orderMonitorRst.childOrderTypeName!''}</td>			                       
	     <td><@func.addSpace orderMonitorRst.productName 20/></td>
	     <td> <#if orderMonitorRst.childOrderType == 'category_cruise'> 
			   ${orderMonitorRst.personCount!''} 人/ ${orderMonitorRst.buyCount!''} 间
				<#else>
				${orderMonitorRst.buyCount!''}份
			</#if>
		 </td>
		 <td> ${orderMonitorRst.visitTime!''} </td>
		</tr>
	 	</#list>
        </#list>
        <tr><td colspan="5">总订单支付金额￥${(travelContractVO.traveAmount)!''} （如购买保险则含保险金额，否则不包含保险金额）</td></tr>
         
         </tbody>
    </table>
    </p>
    <p style="text-aligh:left;">
     <b>第四条 双方权利和义务</b><br/>
     （一）甲方在线或电话委托提交订单后，支付全款或预付款时签订本服务协议。甲方对要求乙方办理各类委托预订服务中<br/>
                 提供资料及填写的信息等真实性负责。<br/>
     （二）本协议的委托事项以甲方以“驴妈妈旅游网”预订流程中的订单信息为准；双方确认：乙方不安排领队、导游服务<br/>
              （甲方需要陪同服务人员除外）。<br/>
     （三）甲方抵达目的地后将自行保管所有旅行证件；如甲方发生证件遗失，乙方可在能力范围内给予协助；但由此产生本<br/>
                协议以外的费用（包括但不限于补办证件费、交通、延住、机票、陪同或其他服务费等）全部由甲方承担。<br/>
     （四）乙方所代办的服务事项均为甲方直接指定，其代办行为即为甲方的真实意愿表示；甲方如对已代办服务项目有任何<br/>
               其他书面要求，乙方协助甲方联系相关部门。甲方在第三方服务期间遭受人身、财产损失的，乙方可予以协助处理。<br/>
     （五）鉴于乙方仅向甲方提供委托服务，甲、乙双方履行本协议时，除约定由乙方提供的服务内容外，其余部分由甲方自行<br/>
                负责。甲方需了解自身委托事项的具体情况，在签订本协议时或后向乙方索取的其它信息介绍或资料仅作为甲方参考，<br/>
                乙方对参考信息及资料不提供服务，也不承担责任。<br/>
     （六）甲方已知晓：如遇航空公司机票税费、邮轮港务费、签证费等费用调整时，同意乙方按调整后的价格结算。<br/>
     <b>第五条 乙方特别告知甲方：</b><br/>
     （一）如甲方为老人、未成年人、孕妇或有心脏病、高血压、呼吸系统等疾病病史，建议征得医生意见，或经家属同意，<br/>
                 或由家属陪伴为妥。甲方在本协议委托事项以外所产生的费用，由甲方自行承担。<br/>
     （二）甲方作为成年人，已明确知晓户外部分活动（包括但不限于潜水、游泳、高速摩托艇、攀岩、蹦极、滑雪、高原<br/>
                 旅行等）为高危娱乐，在充分考虑到自身条件后才自愿参加；并自愿承担因参加上述活动而发生任何事故可能造<br/>
                 成的任何后果。<br/>
     （三）凡持非中国大陆护照的游客或自备签证的旅游者，应自行办理本次旅行签证和再次回中国大陆的签证（包括港澳、<br/>
                 台湾居民往来大陆通行证）。如因签证等问题造成出入境受阻，由甲方自行负责。<br/>
     （四）甲方应遵守社会公共秩序和社会公德，尊重目的地国家（地区）的法律法规、风俗习惯、文化传统和宗教信仰，<br/>
                 爱护旅游资源，保护生态环境，遵守文明行为规范，不参与色情、赌博和涉毒等活动。若违反，后果自负。<br/>
     （五）乙方提示甲方购买个人旅游意外保险，甲方已明确知晓。<br/>
     （六）甲方同意，在乙方完成本协议的委托事项义务后，因不可抗力或天气、罢工、飞机、火车及轮船等一切非乙方原<br/>
                 因造成甲方行程时间变更的，由甲方自行和服务提供商协商解决，乙方不承担违约或侵权责任。<br/>
     （七）甲方知晓：签证服务是否给予签证或签注，是否准予出入境，是外国政府、使领馆及有关部门的权力，如因甲方<br/>
                的自身原因或因提供材料存在问题及其他不可归责于乙方的原因而不能及时办理签证或签注而影响行程的，以及<br/>
                被有关部门拒发签证或签注或不准出入境的，相关责任和乙方实际已发生的费用（机票、酒店定金等）由甲方承担；<br/>
                乙方不退还相应服务费。<br/>
     （八）乙方将根据甲方的本协议委托事项，对甲方递交的资料进行初步审核；甲方同意并配合乙方增补所需材料和提供必<br/>
                 要的保证金。<br/>
     （九）甲方与乙方办理本协议委托事项后，若因甲方自身原因被拒签或拒绝出入境，或在乙方完成本协议委托服务之前甲<br/>
                 方自行取消旅游等导致本协议提前解除，或国家旅游局未发出建议中国公民暂缓前往该目的地的旅游警告解除时，<br/>
                 由甲方支付乙方实际已发生费用（机票、酒店定金、签证费等） ，以及乙方服务费。<br/>
     （十）甲方有义务按使领馆要求在规定时间、地点前往使领馆进行签证面试，并在回国后必须按使领馆要求履行销签义务，<br/>
                 在规定时间内去使领馆面试销签，因此发生的路费和误工费以及产生的其他责任由甲方自理，与乙方无关。<br/>
     <b>（十一）甲方同意：</b>若因非乙方的原因，造成委托事项变更的，以乙方最终确认信息和实际收到的所有票<br/>
                 据、凭证为准。<br/>
     （十二）委托过程中，第三方所提供的赠送活动及相关赠品，甲方若不接受，则视为自愿放弃，费用不予另退或抵扣。<br/>
     （十三）如甲方选择了自由组合航班或单订船票，因网上数据更新有一定的延时，甲方所选定的航班、舱位和价格以乙方操<br/>
                    作人员回复为准。乙方将根据民航或邮轮公司订座系统实时信息与甲方确认。<br/>
     （十四）甲方已知晓乙方提供的机票必须按顺序使用,否则航空公司有权取消后续行程的机位。<br/>
     （十五）甲方已知晓乙方提供的特价机票或优惠价机票属于不可签转、更改、退票，不可累计里程的，甲方应当对此完全了<br/>
                     解，并愿意按照航空公司规定使用。<br/>
     <b>（十六）甲方确认，本人是在签订本协议前已仔细阅读并详细了解了全部条款内容，乙方人员亦向。甲方对本协<br/>
                议内容进行了详细说明后，本人才自愿签订本委托协议。签署本服务协议的甲方代表人，对自身的代理权真实性和合<br/>
                法性承担相应法律责任，并承担负责将本协议内容转告未签名同行者的责任。</b><br/>
     <b>第六条 退改说明</b><br/>
     详见附件 1《补充条款》<br/>
     <b>第七条 其他</b><br/>
     （一）本合同一式两份，双方各持一份，《委托服务协议书使用说明》、补充条款（即退改说明）均有正文条款法律效力，此<br/>
                协议自双方签章且乙方资源审核完毕同时甲方付清全部应付费用后生效。<br/>
     （二）甲乙双方在履行本协议过程中发生争议的，甲乙双方应协商解决，协商不成提交本合同依法向人民法院起诉。<br/>
                <br/><br/>
     <span style="float: right"><b>【正文结束】</b></span><br/>
     <br/>
     <br/>
   </p>
   <p style="position: relative;">
       <span class="stamp"><#if travelContractVO.stampImage??><img src="http://super.lvmama.com/vst_order/img/${(travelContractVO.stampImage)!''}" /></#if></span>
       <b>甲方 ： （签字或盖章）<em class="all-line" style="width:85px;">
       			<#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
	        		<#list travelContractVO.ordTravellerList  as person>
	                    ${person.fullName!''}
	                </#list>
                <#else>
                	${(travelContractVO.travellers)!''}
                </#if></em></b> 
       <span style="margin-left: 200px;"><b>乙方 ： （签字或盖章）</b><em class="all-line" style="width:85px;"> </em></span>
     <br/>
     <br/>
     <br/>
     <br/>
     <span style="float:right">
        <em class="all-line" style="width:70px;">　${travelContractVO.createTime!''} </em><b></b>
    </span>
     <span style="float:left">
        <em class="all-line" style="width:70px;">　${travelContractVO.createTime!''}  </em><b></b>
    </span>
     <br/>
     <br/>
     <br/>
     <br/>
    </p>
    <p>
         <b>附件1：</b></p>
         <b style="font-size: 16px;">补充条款</b><br>
     <p>
     <p>
     <table border="0" cellspacing="1" cellpadding="0" class="" style="text-aligh:left;">
         <tr>
             <td style="height: 600px;text-aligh:left;">
              <#if travelContractVO.priceIncludes??>
		            <strong>费用包含：</strong> <@func.addSpace travelContractVO.priceIncludes 50/><br>
	 	      </#if>
              
              <#if travelContractVO.priceNotIncludes??>
		           <strong>费用不包含：</strong> <@func.addSpace travelContractVO.priceNotIncludes 50/><br>
	 	      </#if>
	 	
		      <#if travelContractVO.travelWarnings??>
		           <strong>出行警示及说明：</strong><@func.addSpace travelContractVO.travelWarnings 50/><br>
	 	      </#if>
	 	
		      <#if travelContractVO.travelNotes??>
		           <strong>行前须知：</strong><@func.addSpace travelContractVO.travelNotes 50/><br>
	 	      </#if>
	 	
		      <#if travelContractVO.backToThat??>
		           <strong>退改说明：</strong><@func.addSpace travelContractVO.backToThat 50/><br>
	          </#if> 
	          <#if travelContractVO.insuranceOrderItemList?? && travelContractVO.insuranceOrderItemList?size gt 0>
	               <#list travelContractVO.insuranceOrderItemList as insuranceOrderItem>
                        <strong>保险名称：</strong>${insuranceOrderItem.getProductName()}<br/>
             
                        <strong>保险金额：</strong>${insuranceOrderItem.getPrice()/100} 元/人<br/>
                   </#list>
              </#if>
            </td>   
         </tr>
     </table>
     <br/><br/><br/><br/><br/>
     </p>


</div>
 </body> 
 </html>