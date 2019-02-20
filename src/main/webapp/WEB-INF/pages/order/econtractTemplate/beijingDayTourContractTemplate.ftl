<html xmlns="http://www.w3.org/1999/xhtml"> 
 <head> 
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
 <title>北京市一日游合同</title> 
 <#include "/base/head_meta.ftl"/>
 <style type="text/css"> 
	* {margin:0;padding:0;}
	body{font-family: SimSun;padding:0 40px;}
	.stamp{background:url(../../img/${(travelContractVO.stampImage)!''}) no-repeat;width:160px;height:160px;display:block;position:absolute;left:420px;top:0px;}
	.txt-hetong {color:#666;width:635px;margin:10px auto;text-align:center;font:12px/1.5 arial,SimSun;}
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
	.tab1 {border-collapse:collapse;background-color:#ccc;width:100%;margin:10px auto;}
	.tab1 td{border:1px solid #ccc;}
	.tab1 td,.tab1 th {padding:5px;text-align:left;vertical-align:middle;background-color:#fff;lin-height:20px;}
	.tab2 {border-collapse:collapse;background-color:#ccc;width:100%;margin:10px auto;}
	.tab2 td{border:1px solid #ccc;}
	.tab2 td,.tab1 th {padding:5px;text-align:center;vertical-align:middle;background-color:#fff;lin-height:20px;}
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
<form action="#" method="post" id="dataForm">
 
 
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
   <input type="hidden" name="ordContractId" value="${RequestParameters.ordContractId}">
   
<div class="txt-hetong">
	<p>
        GF-2014-2402
    </p>
  	<p style="margin-top:10px;"><span style="font-size:14px;float:right;color:#000;">合同编号：<em class="all-line" style="width:150px">　${(travelContractVO.contractVersion)!''}</em></span></p>
  	<p style="margin-top:10px;"><span style="font-size:14px;float:right;color:#000;">订单编号：<em class="all-line" style="width:150px">　${(order.orderId)!''}</em></span></p>
    <br/>
  	<h1 class="h1-title">
       北京市“一日游”合同
    </h1>
    <table border="0" cellspacing="1" cellpadding="0" class="tab1">
    	<tr>
        	<td rowspan="3" width="16%">旅游者姓名共（ ${order.ordTravellerList?size}）人</td>
            <td rowspan="3" colspan="2" width="24%">${travelContractVO.travellers!''} </td>
            <td width="12%">团号</td>
            <td width="24%" colspan="2">/</td>
            <td width="12%">出发时间</td>
            <td width="12%">	${order.visitTime?string('yyyy-MM-dd') !''}</td>
        </tr>
        <tr>

            <td>出发地点</td>
            <td colspan="4">北京</td>
        </tr>
        <tr>


            <td>返回地点</td>
            <td colspan="4">北京</td>
        </tr>
        <tr>
        	<td rowspan="3">行程安排</td>
            <td width="12%">空调车安排</td>
            <td colspan="2" width="24%">详见行程说明 </td>
            <td width="12%">座位号</td>
            <td>详见行程说明</td>
            <td>行程景点及线路</td>
            <td>详见行程说明</td>
        </tr>
        <tr>
        	<td colspan="2">午（晚）餐标准</td>
            <td colspan="5">详见行程说明</td>
        </tr>
        <tr>
        	<td colspan="2">其他</td>
            <td colspan="5"></td>
        </tr>
        <tr>
        	<td width="16%">旅游费用</td>
            <td colspan="3" width="36%">
            	总金额：<em class="all-line" style="width:20px;text-align:center;">${(travelContractVO.traveAmount)!''} </em><br/>
                成人 /<em class="all-line" style="width:20px;text-align:center;">  ${(travelContractVO.aduitCount)!''}</em>人× /<em class="all-line" style="width:20px;text-align:center;">${(travelContractVO.priceAdult)!''}</em>元/人
                +儿童<em class="all-line" style="width:20px;text-align:center;">${(travelContractVO.childCount)!''}</em>/人×<em class="all-line" style="width:20px;text-align:center;">${(travelContractVO.priceChild)!''}</em>元/人
                <br/>
                费用包含:详见费用说明<br/>
                费用不含:详见费用说明<br/>		    
			</td>
            <td width="12%">保险</td>
            <td colspan="3" width="36%">自愿购买保险总金额：${(travelContractVO.insuranceAmount)!'/'} 元</td>
        </tr>
        <tr>
        	<td>补充约定</td>
            <td colspan="7">
            <#if travelContractVO.productDelegate == 'COMMISSIONED_TOUR'>
		           本产品由${(travelContractVO.filialeName)!'上海驴妈妈兴旅国际旅行社有限公司'}代理招徕，委托社为<em class="all-line" style="width:270px;">${(travelContractVO.productDelegateName)!''} </em>，具体旅游服务和操作由委托社提供。 <br/>                         
		     </#if>
		     <#if travelContractVO.productDelegate == 'SELF_TOUR'>
		           本产品由${(travelContractVO.filialeName)!'上海驴妈妈兴旅国际旅行社有限公司'}及具有合法资质的地接社提供相关服务。<br/>                         
		     </#if>
            	双方约定以下补充条款： <br/>
                1．签约人代理其他旅游者签约的或者签约人与旅游者（出游人）不一致的，签约人保证已得到全体旅游
                者的授权签署本合同（包括附件、补充协议），并保证将上述材 料约定事项向全体旅游者做出必要说明
                （特别是本合同涉及"温馨提醒"、"补充条款"等相关的内容），如签约人未履行上述义务或因没有代理
                权限导致旅游者与 旅行社发生纠纷的，由签约人承担全部赔偿责任。<br/>
                2．旅游者同意按照旅行社提供的参考行程签约，实际航班、车次以行前说明会或出团通知的行程为准。
                《补充条款》、《出团通知》及所附《行程单》、《健康证明》、《孕妇出游须知及健康申明》作为本
                合同附件。如有变化，旅行社需及时用传真、短信、电话或电子邮件方式告知旅游者，旅游者应准时到
                达集合地点。<br/>
                3．产品中描述的旅游行程安排可能因不可抗力等不可归责于旅行社的客观原因进行调整或者变更。在旅
                游行程中，当发生不可抗力、危及旅游者人身、财产安全，或者非旅行社责任造成的意外情形，旅行社
                不得不调整或者变更旅游合同约定的行程安排时,可以调整或变更行程安排，但是应当在事前向旅游者做
                出说明；确因客观情况无法在事前说明的，应当在事后做出说明。导致本次团队旅游行程变更或取消的
                ，产生的损失费用旅行社不承担任何责任，因此增加的旅游费用由旅游者承担，因此减少的旅游费用退
                还旅游者。<br/>
                4．不可抗力是指不能预见、不能避免并不能克服的客观情况。不可抗力等不可归责于旅行社的客观原因
                包括但不限于，恶劣天气、自然灾害、战争、罢工、骚乱、恐怖事件、政府行为、公共卫生事件等客观
                原因，造成旅游行程安排的交通服务延误、景区临时关闭、宾馆饭店临时被征用、出境管制、边境关闭
                、目的地入境政策临时变更、我国政府机构发布橙色及以上旅游预警信息等，均会导致旅游目的无法实
                现，旅行社不承担违约责任。<br/>
                5．请在付款前详细阅读《国内团队旅游合同》的内容和了解《协议条款》，以维护自身的合法权利和履
                行参团义务。<br/>
                6．在游览中请听从导游和当地相关管理人员安排，听从有关人员指导，否则责任由旅游者自行承担。旅
                游者在行程中发现自身权益受到侵害，应及时向领队、导游以及旅行者紧急联系人提出，因没有及时提
                出而造成的损失由旅游者自负。<br/>
                7. "旅游有风险"。建议旅游者根据个人意愿和需要自行投保个人旅游意外保险。根据保险公司的规定，
                未成年人、成年人、老年人承保、赔付保险金额是不同的，其中未成年人、老年 人保险赔付金额会比保
                单中载明的保险金额低一定比例，敬请留意。对于高风险娱乐项目，旅行社再次特别提醒，建议旅游者
                投保高风险意外险种。<br/>
                8．旅游者参加西藏等高原地区旅游或风险旅游项目（包括但不限于：游泳、浮潜、冲浪、漂流等水上活
                动以及骑马、攀岩、登山等高风险的活动）或患有不宜出行旅游的病情（包括但不限于：恶性肿瘤、心
                血管病、高血压、呼吸系统疾病、癫痫、怀孕、精神疾病、身体残疾、糖尿病、传染性疾病、慢性疾病
                健康受损），须在报名前自行前往医疗机构体检后，确保自身身体条件能够完成本次旅游活动，并向乙
                方提供体检报告副本；旅游者须保证提供的身体健康状况真实，如隐瞒由本人承担全部责任；旅游者系
                70岁以上（含70岁）参加旅游，应有亲属同意，且非单人出行，同时在出行前如实填写并提交《身体健
                康申报表》；旅行社已经给予旅游者出游安全提示（旅行社已经提示并劝阻，但如旅游者仍坚持参加旅
                游活动，由此造成任何人身意外及不良后果将由旅游者本人全部承担）。<br/>
                9．在自行安排活动期间，旅游者应在自己能够控制风险的范围内活动，旅游者应选择自己能够控制风险
                的活动项目，并对自己的安全负责。旅游者因违约、自身过错、自行安排活动期间内的行为或自身疾病
                引起的人身、财产损失由其自行承担；由此给旅行社造成损失的，旅游者应当承担赔偿责任。旅游者应
                确保自身身体条件适合外出旅游度假。如旅游者为孕妇或有心脏病、高血压、呼吸系统疾病等病史，请
                在征得医院专业医生同意后预订旅游产品。由于旅游者自身原因或其他方原因发生意外由游客自行负责
                ，为了获得更为全面的保障，乙方建议旅游者出游时根据个人意愿和需要自行投保个人险种。<br/>
                10. 赠送项目仅作为额外增值服务，不享有退换、折价等权利，另有规定的除外。<br/>
                11．因预订的产品为团队形式出游，若人数不足成团人数，乙方可有权取消发团并退还所付金额，同时
                合同自动终止履行，若需改期或拼团则另行通知。对于违约责任，旅游者和旅行社已有约定的，从其约
                定承担，没有约定的，按照国家相关法律法规承担。<br/>
                12. 上海市旅游局质量监督所电话：021-64393615
            </td>
        </tr>
        <tr>
        	<td colspan="8">请在签字前充分了解本次旅游有关事宜，认真填写表格内容，仔细阅读并认可背书合同条款。</td>
        </tr>
        <tr>
        	<td>旅游者（签约代表）签章:</td>
            <td colspan="3">${(travelContractVO.firstTravellerPerson.fullName)!''} 共（ ${order.ordTravellerList?size}）人 </td>
            <td>旅行社签章：</td>
            <td colspan="3">
            <span style="position:relative; display:block;">
             <#if travelContractVO.stampImage??>
            <em style=" position:absolute;right:120px;top:-30px;">
            <img src="../../img/${(travelContractVO.stampImage)!''}" width="120" height="120"/></em>
            </#if>
            </span></td>
        </tr>
        <tr>
        	<td>身份证号:</td>
            <td colspan="3">${(travelContractVO.firstTravellerPerson.idNo)!'/'}　</td>
            <td>北京分社地址：</td>
            <td colspan="3">北京市朝阳区左家庄曙光西里甲6号院时间国际大厦1号楼907-08室</td>
        </tr>
        <tr>
        	<td>住所:</td>
            <td colspan="3"></td>
            <td>法定代表人：</td>
            <td colspan="3"></td>
        </tr>
        <tr>
        	<td>电话:</td>
            <td colspan="3">${(travelContractVO.firstTravellerPerson.mobile)!'/'}　</td>
            <td>经办人:</td>
            <td colspan="3"></td>
        </tr>
        <tr>
        	<td>签约时间:</td>
            <td> ${order.createTime?string('yyyy-MM-dd') !''}</td>
            <td>电话：</td>
            <td colspan="2">1010-6060<br/>010-59762904*800</td>
            <td>传真：</td>
            <td colspan="2">010-59762904*801</td>
        </tr>
    </table><br/><br/><br/>
	<h1 class="h1-title">
       北京市“一日游”合同条款
    </h1><br/>
    <p>
    	1、旅行社义务<br/>
        （1）应当在签约前向旅游者出示《营业执照》和《旅行社业务经营许可证》，如实告知有关旅游行程、餐饮、车辆、购
        物等方面安排的真实情况。<br/>
        （2）应当按照约定为旅游者提供旅游服务，保证服务不低于《旅行社国内旅游服务质量要求》确定的标准，并 不得指定
        购物、安排自费项目或医疗咨询；除约定费用或为满足旅游者特殊需要外，不得另行收取其他任何费用。未经旅游者书
        面同意，不得将旅游者转至其他旅行社合并组团。<br/>
        （3）应当保证所提供的服务符合旅游者人身、财产安全的要求；对可能危及旅游者人身、财产安全的事宜，向旅游者做
        出真实的说明和明确的警示，并积极采取防止危害发生的措施。<br/>
        2、旅游者义务<br/>
        （1）应当确保自身身体条件能够完成旅游活动，并全额支付旅游费用。<br/>
        （2）应当与旅行社互相协助共同完成旅游活动，不得因个人原因强迫旅行社改变约定的团队行程或擅自离团活动，并应
        当遵守国家和北京的法律及有关规定，遵守公共秩序，尊重社会公德。<br/>
        （3）应当妥善保管自己的行李物品，贵重物品应当随身携带或采取其他保护措施。<br/>
		3、旅游者退团：旅游者在出发日前一日16：00前通知旅行社解除合同的，旅行社应当全额返还旅游费用；旅游者在上述
        时间之后通知解除合同的，或未能按照约定时间、地点集合出发又未能中途加入的，旅行社可以在扣除20％的违约金后
        返还剩余旅游费用。<br/>
        4、旅行社取消： 旅行社可以在出发日前一日16：00前通知旅游者解除合同，并应当全额返还旅游费用。旅行社在上述
        时间之后通知解除合同的，除全额返还旅游费用外，还应当按照20％的标准支付违约金。<br/>
        5、旅行社责任：<br/>
        （1）提供的旅游服务未达到约定或《旅行社国内旅游服务质量要求》确定的标准的，按照《旅行社质量保证金赔偿试行
        标准》或旅行社责任保险的有关规定进行赔偿。<br/>
        （2）擅自指定购物、安排自费项目或医疗咨询的，应当按照旅游费用的50%向旅游者支付违约金。<br/>
        （3）强迫或者变相强迫旅游者购物、参加自费项目或接受医疗咨询的，应当按照旅游费用的100%向旅游者支付违约金。<br/>
		（4）在行程中单方解除合同的，应当承担由此给旅游者造成的滞留期间食宿费、返回出发点交通费等实际损失，并按照
        旅游费用的100%向旅游者支付违约金。<br/>
        6、旅游者责任：<br/>
        （1）因自身过错、自由活动期间内的行为或自身疾病引起的人身、财产损失应当自行承担；由此给旅行社或第三方造成
        损失的，旅游者应当承担赔偿责任。<br/>
        （2）在行程中因自身原因单方要求解除合同或自愿放弃某项旅游项目的，旅行社可以不退还相应旅游费用。未按照约定
        及时参加旅游项目或搭乘交通工具的，视为自愿放弃。<br/>
        7、意外事件及第三方过错：<br/>
        对由于交通阻塞等意外事件以及第三方侵害等不可归责于旅行社的原因导致旅游者人身、财产权益受到损害的，旅行社
        不承担责任，但应当积极协助旅游者解决与责任方之间的纠纷。<br/>
        8、争议解决： 本合同项下发生争议，双方应当协商解决或向旅游质监所、消费者协会等有关部门投诉；协商、投诉解
        决不成的，可以向乙方所在地有管辖权的人民法院提起诉讼，或按照另行达成的仲裁协议申请仲裁。<br/>
    </p><br/><br/><br/>
    <h1 class="h1-title">
       游玩人信息
    </h1><br/>
    <table border="0" cellspacing="1" cellpadding="0" class="tab2">
     
    	<tr style="font-weight:bold;">
        	<td width="20%">姓名：</td>
            <td width="60%">身份证</td>
            <td width="20%">手机</td>
        </tr>
         <#list order.ordTravellerList  as person> 
        <tr>
        	<td>${(person.fullName)!''}</td>
            <td> <#if person.idType == "ID_CARD"> 
                      	 ${person.idNo!''}
									</#if></td>
            <td>${person.mobile!''}</td>
        </tr>
      </#list> 
    </table>
	<br/><br/><br/>
    
      <h1 class="h1-title">
       订购清单
    </h1><br/>
    <table border="0" cellspacing="1" cellpadding="0" class="tab1">
    	<tr style="font-weight:bold;">
    	
    			<td style="width:120px;">子订单号</td>
    			<td>类型</td>
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
			                       ${orderMonitorRst.productName!''}
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
    <br/><br/><br/>
</div>

</form>
<#include "/base/foot.ftl"/>
<p align="center">
 <button class="btn btn_cc1"  id="editButton">保存且发送</button>
</p>

 </body> 
 <script>

$("#editButton").bind("click",function(){
	
	
	//遮罩层
    var loading = top.pandora.loading("正在努力保存中...");	
	
	$.ajax({
	   url : "/vst_order/order/orderManage/updateTravelContract.do",
	   data : $("#dataForm").serialize(),
	   type:"POST",
	   dataType:"JSON",
	   success : function(result){
   		if(result.code=="success"){
   			loading.close();
   			alert(result.message);
	   		 //parent.window.location.reload();
   		}else {
   			loading.close();
   		  	alert(result.message);
   		}
	   }
	});	
});
$("#closeButton").bind("click", function() {
 	addMessageDialog.close();
});
</script>
</html>