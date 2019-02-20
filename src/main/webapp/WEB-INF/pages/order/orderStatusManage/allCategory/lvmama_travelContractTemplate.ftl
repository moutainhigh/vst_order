<html xmlns="http://www.w3.org/1999/xhtml"> 
 <head> 
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
 
 <title> 北京出境旅游合同示范文本（2009版） </title> 
 <#include "/base/head_meta.ftl"/>
 <style type="text/css"> 
	* {margin:0;padding:0;}
	body{font-family: SimSun;padding:0 40px;}
	.txt-hetong {color:#666;width:635px;margin:10px auto;text-align:center;font:12px/1.5 arial,SimSun;}
	.stamp{background:url(http://pic.lvmama.com/img/order/stamp/${(contractVO.stampImage)!''}) no-repeat;width:160px;height:160px;display:block;position:absolute;left:420px;top:0px;}
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
 
 <form action="#" method="post" id="dataForm">
 
	<input type="hidden" name="orderId" value="${RequestParameters.orderId}">
    <input type="hidden" name="hasMinPersonCount" value="${hasMinPersonCount!''}">
    <input type="hidden" name="delegateGroup" value="${delegateGroup!''}">
    <input type="hidden" name="hasInsurance" value="${hasInsurance!''}">
    
    
    
 <div class="txt-hetong">
     <p>
        <span class="txt-right2">
            合同编号:
            <em class="all-line" style="width:140px;">
                ${(contractVO.contractVersion)!''}
            </em>
        </span>
    </p>
    <h1 class="h1-title">
        出境旅游合同
    </h1>
    <h1 class="h1-title">
        ${(contractVO.filialeName)!'上海驴妈妈兴旅国际旅行社有限公司'}<br/>
        ${traveller}
    </h1>
    <p>
        <span class="txt-right2">
            合同编号:
            <em class="all-line" style="width:140px;">
                ${(contractVO.contractVersion)!''}
            </em>
        </span>
        <span class="txt-right2">
            订单编号:
            <em class="all-line" style="width:140px;">
                ${(contractVO.orderId)!''}
            </em>
        </span>
    </p>
    <h1 class="h1-title">
        出境旅游合同
    </h1>
    <p>
        <b class="b2">
            甲方（旅游者或旅游团体）：
        </b>
        <em class="all-line" style="width:268px">
        <input type="text" style="width:268px;height:25px" name="travellers" id="travellers" value="${(contractVO.travellers)!''}">
        </em>
        <br />
        <b class="b2">
            乙方：
            <em class="all-line" style="width:286px">
            ${(contractVO.filialeName)!'上海驴妈妈兴旅国际旅行社有限公司'}
            </em>
        </b>
        <br />
        <b class="b2">
            经营许可证编号：
            <em class="all-line" style="width:286px">
            L-SH-CJ00056
            </em>
        </b>
        <br />
        <b class="b2">
            经营范围：
            <em class="all-line" style="width:316px">
            入境旅游业务、国内旅游业务、出境旅游业务。 
            </em>
        </b>
        <br />
    </p>
    <br/>
    <p>
    	<strong>根据《中华人民共和国合同法》、《中华人民共和国旅游法》、《旅行社条例》及其它有关法律法规的规定，三方<br/>
    	在平等自愿、协商一致的基础上，签订本合同。</strong>
    </p>
    <br/>
    <p>
        第一条  合同标的
        <br/>
        旅游产品名称<em class="all-line" style="width:286px"> <input type="text" style="width:268px;height:25px" name="productName" id="productName" value="${(contractVO.productName)!''}">
       </em>
        <br />
        组团方式（二选一）
        <br />
        <#if (contractVO.delegateGroup)! false >
        <img src="../../img/uncheckedRadioButton.jpg" />
        <#else>
        <img src="../../img/checkedRadioButton.jpg" />
        </#if> 
        自行组团<br/>
        <#if (contractVO.delegateGroup)! false >
        <img src="../../img/checkedRadioButton.jpg" />
        <#else>
        <img src="../../img/uncheckedRadioButton.jpg" />
        </#if>
       委托组团（委托社全称<em class="all-line" style="width:286px"> <input type="text" style="width:268px;height:25px" name="delegateGroupName" id="delegateGroupName" value="${(contractVO.delegateGroupName)!''}"></em>）。
        <br />
        出发日期<em class="all-line" style="width:186px"> <input type="text" style="width:168px;height:25px" name="vistDate" id="vistDate" value="${(contractVO.vistDate)!''}"></em>，出发地点<em class="all-line" style="width:186px"> <input type="text" style="width:168px;height:25px" name="departurePlace" id="departurePlace" value="${(contractVO.departurePlace)!''}"></em>。
        <br/>
        途经地点（经停地点）<em class="all-line" style="width:286px">详见旅游行程单与本合同具有同等效力 </em>。      
        <br/>
        目的地点<em class="all-line" style="width:286px"> <input type="text" style="width:268px;height:25px" name="destination" id="destination" value="${(contractVO.destination)!''}"></em>
        <br/>
        结束日期<em class="all-line" style="width:186px"> <input type="text" style="width:168px;height:25px" name="overDate" id="overDate" value="${(contractVO.overDate)!''}"></em>，返回地点<em class="all-line" style="width:186px"> <input type="text" style="width:168px;height:25px" name="returnPlace" id="returnPlace" value="${(contractVO.returnPlace)!''}"></em>。
        <br/>
        第二条  行程与标准（乙方提供旅游行程单，须含下列要素）
        <br/>
        景点名称和游览时间<em class="all-line" style="width:286px">详见旅游行程单与本合同具有同等效力</em>。
        <br/>
        往返交通<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>，
        标准<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>。
        <br/>
        <br/>
        游览交通<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>，
        标准<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>。
        <br/>
        <br/>
        旅游者自由活动时间<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>，
        次数<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>。
        <br/>
        住宿安排（名称）及标准和住宿天数<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>。
        <br/>
        用餐次数<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>，
        标准<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>。
        <br/>
        <br/>
        地接社名称<em class="all-line" style="width:215px"> <input type="text" style="width:168px;height:25px" name="localTravelAgencyName" id="localTravelAgencyName" value="${(contractVO.localTravelAgencyName)!''}"></em>，
        地址<em class="all-line" style="width:215px"> <input type="text" style="width:168px;height:25px" name="localTravelAgencyAddress" id="localTravelAgencyAddress" value="${(contractVO.localTravelAgencyAddress)!''}"></em>，
        <br/>
        <br/>
        地接社联系人<em class="all-line" style="width:215px">/</em>，
        联系电话<em class="all-line" style="width:215px">/</em>。
        <br/>
        第三条  旅游者保险
        <br/>
        乙、丙方提示甲方购买旅游意外险。经乙、丙方推荐，甲方<em class="all-line" style="width:100px"> <input type="text" style="width:68px;height:25px" name="agreeInsurance" id="agreeInsurance" value="${(contractVO.agreeInsurance)!''}"> </em>（应填同意或不同意，打勾无效）委托乙方或丙方办理个人投保的旅游意外保险。
        <br/>
        保险公司及产品名称<em class="all-line" style="width:215px"> <input type="text" style="width:168px;height:25px" name="insuranceCompanyAndProductName" id="insuranceCompanyAndProductName" value="${(contractVO.insuranceCompanyAndProductName)!''}"> </em>。
        <br/>
        保险金额<em class="all-line" style="width:215px">详见旅游行程单与本合同具有同等效力</em>，
        保险费<em class="all-line" style="width:215px"> <input type="text" style="width:68px;height:25px" name="insuranceAmount" id="insuranceAmount" value="${(contractVO.insuranceAmount)!''}"> </em>。
        <br/>
        第四条  旅游费用及其支付
        <br/>
        甲方应交纳旅游费用<em class="all-line" style="width:315px"><input type="text" style="width:68px;height:25px" name="traveAmount" id="traveAmount" value="${(contractVO.traveAmount)!''}"></em>元，大写<em class="all-line" style="width:100px">
        ${(contractVO.chineseNumeralTraveAmount)!''}
        <#--
        <input type="text" style="width:98px;height:25px" name="chineseNumeralTraveAmount" id="chineseNumeralTraveAmount" value="${(contractVO.chineseNumeralTraveAmount)!''}">
        -->
         </em>元。
        <br/>
        旅游费用交纳期限<em class="all-line" style="width:215px">下单时付清全部款项</em>。
        <br/>
        旅游费用交纳方式 <img src="../../img/uncheckedCheckBox.jpg" />现金；<img src="../../img/uncheckedCheckBox.jpg" />支票；<img src="../../img/uncheckedCheckBox.jpg" />信用卡；<img src="../../img/uncheckedCheckBox.jpg" />其他
        <em class="all-line" style="width:270px"><input type="text" style="width:198px;height:25px" name="payWay" id="payWay" value="${(contractVO.payWay)!''}"></em>。
    	<br/>
        第五条 双方的权利义务 
        <br/>
        （一）甲方的权利与义务
        <br/>
		甲方应自觉遵守旅游文明行为规范，尊重旅游目的地的风俗习惯、文化传统和宗教禁忌，爱护旅游资<br/>
		源，保护生态环境。甲方在旅游活动中应遵守《中国公民出国（境）旅游文明行为指南》和安全警示<br/>
		规定，遵守团队纪律，配合乙方完成合同约定的旅游行程。        
        <br/>
        2．甲方有权知悉其购买的旅游产品和服务的真实情况，有权要求乙方按照约定提供产品和服务，拒绝<br/>
                              乙方未经协商一致指定具体购物场所，安排另行付费旅游项目的行为。
        <br/>
        3．在旅游过程中，甲方应自行保管好随身携带的财物。
        <br/>
        4．在自行安排活动期间，甲方应在自己能够控制风险的范围内活动，选择自己能够控制风险的活动项<br/>
                            目，并对自己的安全负责。
        <br/>
        5．行程中发生纠纷，甲方应与乙方平等协商解决，不得损害乙方的合法权益，不得以拒绝上、下机（<br/>
                              车、船、邮轮）等行为拖延行程或者脱团，否则应当就扩大的损失承担赔偿责任。
        <br/>
        6．甲方在签订合同或者填写各种材料时，应当使用有效身份证件，并对填写信息的真实性、有效性负<br/>
                            责。限制民事行为能力人单独出行的，须由监护人书面同意。
        <br/>
        7．甲方购买旅游产品、接受旅游服务时，应当如实告知与旅游活动相关的个人健康信息，参加适合自<br/>
                             身条件的旅游活动，遵守旅游活动中的安全警示要求，配合有关部门、机构或乙方采取的安全防范<br/>
                             和应急处理措施。
        <br/>
        8．甲方向乙方提交的出入境证件应当符合相关规定。甲方不得在境外非法滞留，不得擅自分团、脱团。
        <br/>
        9．甲方不能成行的，可以让具备参加本次旅游条件的第三人代为履行合同，并及时通知乙方；在不影<br/>
                            响总体团队安排及各项资料均完毕的情况下经乙方同意，可以代为履行，否则仍由甲方承担责任。<br/>
                            因代为履行合同增加或减少的费用，双方应按实结算。
        <br/>
        （二）乙方的权利与义务
        <br/>
        1．乙方不得以不合理的低价组织旅游活动，诱骗旅游者，并通过安排购物或者另行付费旅游项目获取<br/>
                             回扣等不正当利益。
        <br/>
        2．乙方应在出团前，以说明会等形式如实告知相关旅游服务项目和标准，提醒甲方遵守旅游文明行为<br/>
                            规范，尊重旅游目的地的风俗习惯、文化传统、宗教禁忌。在合同订立时及履行中，乙方应对旅游<br/>
                            中可能危及甲方人身、财产安全的情况，作出真实说明和明确警示，并采取防止危害发生的适当措<br/>
                            施（附件一为《旅游安全提示禁止告知书》，由甲方确认签字）。
        <br/>
        3．乙方应妥善保管甲方提交的各种证件，依法对甲方信息保密。
        <br/>
        4．因航空、轮船、铁路运输等费用遇政策性调价导致合同总价发生变更的，双方应按实结算。
        <br/>
        5．乙方为甲方安排符合法律法规规定的领队人员。
        <br/>
        6．甲方有下列情形之一的，乙方可以解除合同：
        <br/>
        （1）患有传染病等疾病，可能危害其他旅游者健康和安全的；
        <br/>
        （2）携带危害公共安全的物品且不同意交有关部门处理的；
        <br/>
        （3）从事违法或者违反社会公德的活动的；
		<br/>
        （4）从事严重影响其他旅游者权益的活动，且不听劝阻、不能制止的；
        <br/>
        （5）法律规定的其他情形。
        <br/>
        因前款情形解除合同的，乙方应当按本合同第六条扣除必要的费用后，将余款退还甲方；给乙方<br/>
        造成损失的，甲方应当依法承担赔偿责任。
        <br/>
        7．成团人数与不成团的约定（二选一）
        <br/>
        <#if (contractVO.hasMinPersonCount)! false >
         <img src="../../img/checkedRadioButton.jpg" />
        <#else>
         <img src="../../img/uncheckedRadioButton.jpg" />
        </#if>
                      最低成团人数<em class="all-line" style="width:100px"> <input type="text" style="width:68px;height:25px" name="minPersonCountOfGroup" id="minPersonCountOfGroup" value="${(contractVO.minPersonCountOfGroup)!''}"></em>人；低于此人数不能成团时，乙方应当提前30日通知甲方，<br />
                      本合同解除，向甲方退还已收取的全部费用。
        <br/>
        <#if (contractVO.hasMinPersonCount)! false >
         <img src="../../img/uncheckedRadioButton.jpg" />
        <#else>
         <img src="../../img/checkedRadioButton.jpg" />
        </#if>
                     本团成团不受最低人数限制。 
        <br/>
        8．在旅游行程中因不可抗力或者意外事件导致本合同无法履行或者继续履行合同的，乙方或实际旅<br/>
                            游产品提供方在征得团队50%以上旅游者书面同意后，对相应行程内容予以变更。因情况紧急无法<br/>
                            书面征求意见或者经书面征求意见无法满足50%以上旅游者同意时，组团社或地接社可以决定行程<br/>
                            内容的变更，但应当就所作出的变更内容决定提供必要的书面证明。
        <br/>
        第六条  甲方解除合同及承担必要费用
        <br/>
        因甲方自身原因导致合同解除，乙方委托丙方按下列标准扣除必要费用后，将余款退还甲方。
        <br/>
        1．在行程前解除合同的，机（车、船）票费用按实结算后，其余必要的费用扣除标准为：
        <br/>
        （1）行程前30日至15日，旅游费用5％；
        <br/>
        （2）行程前14日至7日，旅游费用10％；
        <br/>
        （3）行程前6日至4日，旅游费用15％；
        <br/>
        （4）行程前3日至1日，旅游费用25％；
        <br/>
        （5）行程开始当日，旅游费用30％。
        <br/>
		甲方行程前逾期支付旅游费用超过<em class="all-line" style="width:30px">五</em>日的，<br/>
		或者甲方未按约定时间到达约定集合出发地点，也未能在中途加入旅游团队的，乙方可以视为甲方<br/>
		解除合同，乙方委托丙方按本款规定扣除必要的费用后，将余款退还甲方。
        <br/>
        2．在行程中解除合同的，必要的费用扣除标准为：
        <br/>
        （旅游费用-旅游费用×行程开始当日扣除比例）÷旅游天数×已经出游的天数+旅游费用×行程开始当日扣除比例。
        <br/>
        如按上述1、2方式支付的必要费用低于实际发生的费用，按照实际发生的业务损失费用扣除，但最高额<br/>
        不应超过旅游费用总额。
        <br/>
        <strong>业务损失费，指乙方、丙方因甲方行前退团而产生的经济损失，包括但不限于乘坐飞机（车、船）<br/>
                                          等交通工具的费用（含预订金）、旅游签证/签注费用、饭店住宿费用（含预订金）、旅游观光汽<br/>
                                          车的人均车租等已发生的实际费用。（适用合同全部条款）</strong>
        <br/>
        行程前解除合同的，乙方委托丙方扣除必要费用后，应当在合同解除之日起<em class="all-line" style="width:30px">三十</em>个工作日内向甲方退还剩余旅游费用；
        <br/>
        行程中解除合同的，乙方委托丙方扣除必要费用后，应当在协助甲方返回出发地或者到达甲方<br/>
        指定的合理地点后<em class="all-line" style="width:30px">三十</em>个工作日内向甲<br/>
        方退还剩余旅游费用。
        <br/>
        第七条  特别说明
        <br/>
        （一）甲方通过"驴妈妈旅游网"订购乙方的相应旅游产品，无论系直接网络下单或通过驴妈妈旅游网<br/>
                      客服人员下单，就本合同所需填写内容通过在线填写或口头委托客服人员填写等方式予以确认，<br/>
                      并依据其网络预订流程体现在本合同中，具有法律约束力。
        <br/>
        （二）除自主组团产品外，乙方在"驴妈妈旅游网"上销售的代理旅游产品，承担或负责处理在线销售<br/>
                      预订、市场宣传和网络技术等问题引起的法律责任；实际提供旅游服务的旅行社承担旅游旅游<br/>
                      产品设计及具体实施过程中出现的包括但不限于服务、价格争议、导游、安全等旅游要素引起<br/>
                      的法律责任。
        <br/>
        （三）乙方通过"驴妈妈旅游网"预订甲方旅游产品时所获取的行程安排为参考行程，乙方在出行前会<br/>
                       提供确定的行程安排，若最终乙方未提供新的行程安排单的，前述行程安排作为双方约定的最终<br/>
                       行程。
        <br/>
        （四）甲方承诺已接受同行全体旅游者的委托，代理其与乙方签署本合同，若由此引发合同争议其他旅<br/>
                      游者作出否认的，甲方签约人应承担全部法律责任。 
        <br/>
        第八条  违约责任
        <br/>
        （一）乙方在行程前30日以内（不含第30日，下同）提出解除合同的，向甲方退还全额旅游费用（不得<br/>
                      扣除签证／签注等费用），并按下列标准向甲方支付违约金：
        <br/>
        （1）行程前30日至15日，支付旅游费用2％的违约金；
        <br/>
        （2）行程前14日至7日，支付旅游费用5％的违约金；
        <br/>
        （3）行程前6日至4日，支付旅游费用10％的违约金；
        <br/>
        （4）行程前3日至1日，支付旅游费用总额15％的违约金；
        <br/>
        （5）行程开始当日，支付旅游费用20％的违约金。
        <br/>
        如上述违约金不足以赔偿甲方的实际损失，乙方应当按实际业务损失费用对甲方予以赔偿。
        <br/>
        乙方应当在取消出团通知到达日起<em class="all-line" style="width:30px">三十</em>个工作日内委托丙方向甲方退还全额旅游费用并支付违约金。
        <br/>
        （二）甲方逾期支付旅游费用的，应当每日按照逾期支付的旅游费用的<em class="all-line" style="width:30px">0.1</em>％，向乙方支付违约金。
        <br/>
        （三）甲方提供的个人信息及相关材料不真实而造成损失，由其自行承担；如给乙方造成损失的，甲方<br/>
                       还应当承担赔偿责任。
        <br/>
        （四）甲方因不听从乙方的劝告、提示而影响团队行程，给乙方造成损失的，应当承担相应的赔偿责任。
        <br/>
        （五）乙方未按合同约定标准提供交通、住宿、餐饮等服务，或者未经甲方同意调整旅游行程，给甲方<br/>
                      造成损失的，责任由乙方承担。
        <br/>
        （六）乙方未经甲方同意，擅自将旅游者转团、拼团的，甲方在行程前（不含当日）得知的，有权解除<br/>
                      合同，乙方全额退还已交旅游费用，并按旅游费用的15％支付违约金；甲方在行程开始当日或者<br/>
                      行程开始后得知的，乙方应当按旅游费用的25％支付违约金。如违约金不足以赔偿甲方的实际损<br/>
                      失，乙方应当按实际损失对旅游者予以赔偿。
        <br/>
        （七）乙方未经与甲方协商一致或者未经甲方要求，指定具体购物场所或安排另行付费旅游项目的，甲<br/>
                       方有权在旅游行程结束后三十日内，要求乙方为其办理退货并先行垫付退货货款，或者退还另行<br/>
                       付费旅游项目的费用。
        <br/>
        （八）乙方具备履行条件，经甲方要求仍拒绝履行合同，造成甲方人身损害、滞留等严重后果的，甲方<br/>
                       除要求乙方承担相应的赔偿责任外，还可以要求乙方支付旅游费用<em class="all-line" style="width:30px">一</em>倍（一倍以上三倍以下）的赔偿金。
        <br/>
        第九条  投诉机构
        <br/>
        若甲方就其接受的旅游服务不满意,产生投诉的,可参考如下方式: 
        <br/>
        （一）	致电丙方"驴妈妈旅游网"服务热线<strong>1010-6060</strong> 
        <br/>
        （二）<em class="all-line" style="width:215px">旅游质量监督电话：021-64393615 </em>
        <br/>
        第十条  争议解决方式
        <br/>
        各方发生争议的，可协商解决，也可在旅游合同结束之日起90天内向旅游质监机构申请调解，也可向消费者<br/>
        协会等有关部门或者机构申请调解，无法协商一致的，各方均有权向上海嘉定区人民法院提起诉讼。
        <br/>
        第十一条  附则
        <br/>
        本合同自双方签字或盖章<em class="all-line" style="width:100px">并甲方付款</em>之日起生效，本合同附有《旅游安全提示禁止告知书》和补充条款均为合同的附件，与本合同具有同等的法律效力。
        <br/>
        <span  class="buchongbox" style="">
        	<span style="font-size: x-large; text-align:center; display:block;"> 补 充 条 款 </span>
            <br/>
            <#--
            ${(contractVO.supplementaryTerms)!''}
            -->
            <textarea  id="supplementaryTerms" name="supplementaryTerms" style="width: 620px; height: 198px;" >${(contractVO.supplementaryTerms)!''}</textarea>
        	
        </span>
        <br/>
        <strong>重要告知：请旅游者（签约人）在签约本合同前务必仔细阅读本合同全部条款和内容，在丙方及乙方工作人员<br/>
                                          就前述内容和行程中食、住、行、旅游项目、娱乐活动的具体内容、时间、服务安排、标准以及相关的安全提<br/>
                                          示、注意警示、各自责任、风险向旅游者详细说明后，且甲方明确知晓和完全理解本合同内容，并同意与遵守<br/>
                                          合同约定内容后，再签订本合同。</strong>
     </p>
     <br/>
     <br/>
     <br/>
     <div style=" position:relative;">
     	<span class="stamp"></span>
         <p> 甲方签字（签章）：<em class="all-line" style="width:160px"><input type="text" style="width:168px;height:25px" name="signaturePersonName" id="signaturePersonName" value="${(contractVO.signaturePersonName)!''}">  </em> 乙方签字（盖章）：<em class="all-line" style="width:200px">${(contractVO.filialeName)!'上海驴妈妈兴旅国际旅行社有限公司'}</em></p> 
         <p> 住所：<em class="all-line" style="width:232px">/</em> 营业场所：<em class="all-line" style="width:248px">上海市嘉定区景域大道88号驴妈妈科技园</em></p> 
         <p> 甲方代表：<em class="all-line" style="width:208px"><input type="text" style="width:168px;height:25px" name="firstDelegatePersonName" id="firstDelegatePersonName" value="${(contractVO.firstDelegatePersonName)!''}"></em> 乙方代表（经办人）：<em class="all-line" style="width:188px">/</em></p> 
         <p> 联系电话：<em class="all-line" style="width:208px"><input type="text" style="width:168px;height:25px" name="contactTelePhoneNo" id="contactTelePhoneNo" value="${(contractVO.contactTelePhoneNo)!''}"> </em> 联系电话：<em class="all-line" style="width:248px">1010-6060  </em></p> 
         <p> 邮编：<em class="all-line" style="width:232px">/</em> 邮编：<em class="all-line" style="width:272px">201803 </em></p> 
         <p> 日期：<em class="all-line" style="width:232px"><input type="text" style="width:168px;height:25px" name="firstSignatrueDate" id="firstSignatrueDate" value="${(contractVO.firstSignatrueDate)!''}"> </em> 日期：<em class="all-line" style="width:272px"><input type="text" style="width:168px;height:25px" name="secondSignatrueDate" id="secondSignatrueDate" value="${(contractVO.secondSignatrueDate)!''}"> </em></p>   
     </div>	
     <div style=" position:relative;">
   <#if contractVO.orderMonitorRstList?? && (contractVO.orderMonitorRstList?size > 0)>
       <h1 class="h1-title">
       订购清单
    </h1><br/>
     <table border="0" cellspacing="1" cellpadding="0" class="tab1">
       <thead>
	     <tr>
	      <th>子订单号</th>
	      <th>类型</th>
	      <th>名称</th>
	      <th>预订份数</th>
	      <th>出游时间</th>
        </tr>
        </thead>
        <#list contractVO.orderMonitorRstList  as orderItem>
        <tr>
        <td><#if orderItem.orderItemId??>${orderItem.orderItemId}</#if></td>
        <td>${orderItem.childOrderTypeName}</td>
        <td><#if orderItem.productName??>${orderItem.productName}</#if></td>
        <td>
            <#if orderItem.childOrderType == 'category_cruise'> 
			    ${orderItem.personCount!''} 人/ ${orderItem.buyCount!''} 间
				<#else>
			  ${orderItem.buyItemCount!''}
		  </#if>
        </td>
        <td><#if orderItem.visitTime??>${orderItem.visitTime}</#if></td>
        </tr>
        </#list>
     </table>
     </#if>
     </div>
	 <br/>
     <br/>
     <br/>
    
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