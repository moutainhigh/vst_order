<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<#import "func.html" as func/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>《预付款产品协议书》模板（驴妈妈2016版）</title>
    <style type="text/css">
        * {margin:0;padding:0;}
        body{font-family: SimSun;padding:0 40px;}
        .stamp{background:url(http://super.lvmama.com/vst_order/img/${(travelContractVO.stampImage)!''}) no-repeat;width:160px;height:160px;display:block;position:absolute;left:420px;top:0px;}
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
<div class="txt-hetong">
    <br/>
    <br/>
    <br/>
    <br/>
    <span style="float:right">合同编号：<em class="all-line" style="width:200px">${(travelContractVO.contractVersion)!''}　</em></span>
    <br/>
    <br/>
    <br/>
    <br/>
    <h1 class="h1-title" style="line-height:30px;">
        预付款产品协议书
    </h1>
    <br/>
    <p>
        <b>甲方（预订人）：</b><em class="all-line" style="width:173px" readonly="readonly">
        		<#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
	        		<#list travelContractVO.ordTravellerList  as person>
	                    ${person.fullName!''}
	                </#list>
                <#else>
                	${(travelContractVO.travellers)!''}
                </#if></em><b>联系电话：</b><em class="all-line" style="width:150px">${(order.contactPerson.mobile)!'/'}　</em><br>
        <b>乙方（接受预订人）：</b><em class="all-line" style="width:200px">${travelContractVO.filialeName}　</em><b>联系电话：</b><em class="all-line" style="width:150px">${(travelContractVO.lvMobile)!'1010-6060'}　</em><br>
        <br/>
        鉴于甲方向乙方预订的旅游产品资源特殊，故双方经友好协商，达成如下协议：<br/>
        一、甲方通过“驴妈妈旅游网”页面信息（产品重要提示中的预订须知描述）知晓并同意，其将预订的旅游产品资源需经
        一定时间方可确定。甲方预订的需进行确定的产品详情详见附件即订单信息，附件为本协议书一部分，与本协议书具有
        同等效力。<br/>
        二、旅游产品名称<em class="all-line" style="width:200px;"><@func.addSpace travelContractVO.productName 16/></em>；甲方在本协议项下向乙方申请先行支付预付款（相当于
        该产品全额款）人民币
        <em class="all-line" style="width:100px;">
            <#if travelContractVO.traveAmount ??>${travelContractVO.traveAmount!''}<#else>${(order.oughtAmountYuan)!''}</#if></em>元，
            该款在乙方确认资源后自动转为产品价款；乙方收款后进入预定流程，在规定日期内若乙方无该项产品资源的，则预付款
        将全额在
        <em class="all-line" style="width:20px;">
            <#if travelContractVO.copies ??>
            ${travelContractVO.copies!''}
            <#else>
                5
            </#if>
        </em> 个工作日内向甲方无息退还（原路径返还）；
        若因甲方原因申请撤销交易的按实际已产生的合理费用收取，扣除后的款项予以退还。<br/>
        三、甲乙双方一致确认，就本协议项下所述预订事宜双方无任何违约责任，仅按照前述条款约定办理。<br/>
        四、甲方已阅读并同意由乙方提供的旅游合同，在资源确认后，甲乙双方将正式签订旅游合同，相应的权利义务以合同
        内容为准，本协议履行结束，对双方均无法律约束力。<br/>
        五、甲方最终向乙方预订的具体产品信息详见甲乙双方正式签订的旅游合同及出团通知书。<br/>
        六、本协议自甲方在成功支付预付款后生效，将由“驴妈妈旅游网”发送至甲方指定邮箱。<br/>
        七、甲乙双方在履行本协议过程中发生争议的，甲乙双方应协商解决，协商不成提交本合同签订地上海市嘉定区人民法院
        诉讼解决。<br/>
        <span style="float: right"><b>【正文结束】</b></span><br/>
        <br/>
        <br/>
    </p>
    <p style="position: relative; padding-bottom: 60px;">
        <span style=" position: absolute; right: 60px; top: 0;" class="stamp"></span>
        <b>甲方 ： （签字或盖章）
        		<#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
	        		<#list travelContractVO.ordTravellerList  as person>
	                    ${person.fullName!''}
	                </#list>
                <#else>
                	${(travelContractVO.travellers)!''}
                </#if> </b>
        <span style="margin-left: 200px;"><b>乙方 ： （签字或盖章）</b></span>
        <br/>
        <br/>
        <br/>
        <br/>
        <span>
            <em class="all-line" style="width:220px">
            <#if travelContractVO.singnDate ??>
        		${travelContractVO.singnDate!''}
        	<#else>
                <#if order.createTime ??>${order.createTime?string('yyyy年MM月dd日') !''}</#if>
            </#if>
            </em>
        </span>

        <span style="margin-left: 130px;">
            <em class="all-line" style="width:220px">
            <#if travelContractVO.lvSingnDate ??>
                    ${travelContractVO.lvSingnDate!''}
                <#else>
                <#if order.createTime ??>${order.createTime?string('yyyy年MM月dd日') !''}</#if>
            </#if>
            </em>
        </span>
        <br/>
        <br/>
    </p>
</div>
</form>
</body>
</html>