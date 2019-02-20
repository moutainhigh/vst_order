<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>《上海市邮轮旅游合同》模板（驴妈妈2016版）</title>
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
        .txt-hetong input[type="text"]{ height: 26px; margin: 3px;}
        .sendBtn{ height: 28px; line-height: 28px; text-align: center; display: inline-block; font-size: 12px; color: #666666; padding: 0 10px;
            border: 1px solid #cccccc; background: #e9e9e9; border-radius: 3px; text-decoration: none;}
    </style>
    <script>
        $("#shipEditButton").unbind("click");
        $("#shipEditButton").bind("click", function(){
            //遮罩层
            var loading = top.pandora.loading("正在努力保存中...");
            $.ajax({
                url:"/vst_order/order/orderManage/updateTravelContract.do",
                data:$("#dataForm").serialize(),
                type:"POST",
                dataType:"JSON",
                success:function(result) {
                    if(result.code == "success"){
                        loading.close();
                        alert(result.message);
                    } else {
                        loading.close();
                        alert(result.message);
                    }
                }
            });
        });
    </script>
</head>

<body>
<form action="#" method="post" id="dataForm">

    <input type="hidden" name="orderId" value="${RequestParameters.orderId}">
    <input type="hidden" name="ordContractId" value="${RequestParameters.ordContractId}">
    <div class="txt-hetong">
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <h1 class="h1-title">
            上海市邮轮旅游合同
        </h1>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <h1 class="h1-title" style="line-height:30px;">
            参照二〇一五年九月上海市旅游局示范文本制定
        </h1>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <h1 class="h1-title">
            使　用　说　明
        </h1>
        <br/>
        <br/>
        <p>
            一、本合同示范文本供旅游者参加邮轮旅游与旅行社签订包价旅游合同时使用。旅游者应选择具有经营旅游业务相应资格的旅行社。旅行社应具有旅游行政管理部门颁发的《旅行社业务经营许可证》和工商行政管理部门颁发的《营业执照》。经营出境旅游的旅行社应具有经营出境旅游业务资格；经营赴台湾地区旅游的旅行社除了应具有上述经营出境旅游业务资格外，还应具有组织大陆居民赴台湾地区旅游的经营资格。<br/>
            二、旅游前，旅行社应当与旅游者签订书面旅游合同，本合同及其附件均应使用中文文本。旅游者在交纳旅游费用后，旅行社应开具发票。<br/>
            三、旅游者在自行安排活动期间，应结合自身身体状况选择邮轮上的活动项目。旅游者应选择适合自身身体状况的岸上旅游产品及项目。<br/>
            四、旅行社委托组团，须事先告知旅游者并在本合同中载明。<br/>
            五、旅游者与旅行社也可使用本合同电子版。<br/>
            六、在填写本合同第二条“行程与标准”和“旅游行程单”时，旅行社应以准确、明晰的语言表述，不得出现“准 X 星级”、“相当于 X 星级”、“仅供参考”、“与××同级”等模糊不确定性用语。<br/>
            七、旅游者有权自主选择旅游产品和服务，有权拒绝旅行社的强制交易行为。<br/>
            八、在签订合同时，双方应当结合具体情况选择本合同协议条款中所提供的选择项，条款前有“□”符号的，甲乙双方应当协商选定。双方选定的条款，应当在“□”中划“√” ；双方不选的条款，应当在“□”中划“×” ；条款中有空格处的，供双方自行约定并填写完整，对双方不予约定的空格处，应当划“×”以示没有特别约定。<br/>
            九、旅行社制定补充条款等双方自行约定内容对本合同示范文本有关条款的内容进行补充、细化的，自行约定内容不得减轻或者免除应当由旅行社承担的责任。<br/>
            十、本合同示范文本自 2015 年 8 月 25 日起使用。今后凡未制定新的版本前，本版本延续使用。<br/>
            十一、旅游咨询与投诉机构：<br/>
            &emsp;&emsp;1．上海市旅游质量监督所<br/>
            &emsp;&emsp;地址：中山南二路 2419 号 B1 楼 邮编：200232<br/>
            &emsp;&emsp;投诉电话：64393615、962020<br/>
            &emsp;&emsp;2．上海市消费者申（投）诉举报中心<br/>
            &emsp;&emsp;举报投诉电话：12315<br/>
            &emsp;&emsp;3．上海市文化市场行政执法总队<br/>
            &emsp;&emsp;地址：永嘉路 383 号 邮编：200031<br/>
            &emsp;&emsp;旅游违法违规举报电话：12318<br/>
        </p>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>

        <span style="float:right">合同编号：<em class="all-line" style="width:200px">${travelContractVO.contractVersion!''}　</em></span> <br/>
        <br/>
        <br/>
        <h1 class="h1-title" style="line-height: 30px;">上海市邮轮旅游合同<br/>（ 2015 版）</h1> <br/>
        <br/>
        <p>
            甲方（旅游者或旅游团体） ：<input type="text" style="width: 260px;" name="firstTravellerName"  readonly="readonly" value="${(travelContractVO.travellers)!''}"/><br/>
            乙方（旅行社） ：<input type="text" style="width: 260px;" name="filialeName" value="${travelContractVO.filialeName!''}"/><br/>
            经营许可证编号：<input type="text" style="width: 260px;" name="permit" value="${travelContractVO.permit!''}"/><br/>
            经营范围：<textarea name="businessScope" style="width: 500px; height: 60px;">${travelContractVO.businessScope!''}</textarea><br/>
            <br/>
            根据《中华人民共和国合同法》、《中华人民共和国旅游法》、《旅行社条例》、《上海市旅游条例》及其它有关法律
            法规的规定，甲乙双方在平等自愿、协商一致的基础上，签订本合同。<br/>
            <br/>
            <b>第一条 合同标的</b><br/>
            邮轮产品名称<input type="text" style="width: 260px;" name="productName" value="${travelContractVO.productName!''}"/>。<br/>
            团号：<input type="text" style="width: 260px;" name="regimentNum" value="${travelContractVO.regimentNum!''}"/>。<br/>
            组团方式（二选一）<br/>
            <input type="radio" name="groupMode" <#if travelContractVO.productDelegate == 'SELF_TOUR'>checked="checked"</#if>/>
        <#if travelContractVO.productDelegate == 'COMMISSIONED_TOUR'>
            <input type="hidden" name="productDelegate" value="${travelContractVO.productDelegate!''}" />
        </#if>
        <#if travelContractVO.productDelegate == 'SELF_TOUR'>
            <input type="hidden" name="productDelegate" value="${travelContractVO.productDelegate!''}" />
        </#if>
            自行组团；<br/>
            <input type="radio" name="groupMode" <#if travelContractVO.productDelegate == 'COMMISSIONED_TOUR'>checked="checked"</#if>/>
            委托组团（委托社全称及经营许可证编号<em class="all-line" style="width:200px;">
        <#if travelContractVO.productDelegate == 'COMMISSIONED_TOUR'>
            <input type="text" style="width:210px;height:25px;" name="productDelegateName" value="${travelContractVO.productDelegateName}">、
            <input style="width:100px;height:25px;" type="text" name="suppSupplier.permit" value="<#if travelContractVO.suppSupplier??>${travelContractVO.suppSupplier.permit!''}</#if>">
        </#if>
            　</em>）。<br/>
            出发日期<input type="text" style="width: 150px;" name="vistDate" value="${travelContractVO.vistDate!''}"/>，出发地点<input type="text" style="width: 260px;" name="departurePlace" value="${travelContractVO.departurePlace!'详见行程单'}"/>。<br/>
            邮轮途中停靠港口<input type="text" style="width: 260px;" name="lineShipDesc" value="详见行程单"/>。<!--${travelContractVO.lineShipDesc}--><br/>
            岸上游览地点<input type="text" style="width: 260px;" name="shorePlayPlace"  value="${travelContractVO.shorePlayPlace!'详见行程单'}"/>。<br/>
            结束日期<input type="text" style="width: 150px;" name="overDate" value="${travelContractVO.overDate!''}"/>，
            返回地点<input type="text" style="width: 260px;" name="returnPlace" value="<#--${travelContractVO.returnPlace}-->详见行程单"/>。<br/>
            <b>第二条 行程与标准（乙方提供旅游行程单，须含下列要素）</b><br/>
            邮轮上舱位类型及标准和住宿天数<input type="text" style="width: 260px;" name="cruiseCabinTypeAndStayNum"  value="${travelContractVO.cruiseCabinTypeAndStayNum!'详见行程单'}"/>。<br/>
            邮轮上用餐次数<input type="text" style="width: 200px;" name="cruiseDinnerCount"  value="${travelContractVO.cruiseDinnerCount!'详见行程单'}"/>，标准<input type="text" style="width: 200px;" name="cruiseDinnerCountStan"  value="${travelContractVO.cruiseDinnerCountStan!'详见行程单'}"/>。<br/>
            岸上景点名称和游览时间<input type="text" style="width: 260px;" name="shoreScenicNameAndPlayTime"  value="${travelContractVO.shoreScenicNameAndPlayTime!'详见行程单'}"/>。<br/>
            岸上往返交通<input type="text" style="width: 100px;" name="shoreGoBackTraffic"  value="${travelContractVO.shoreGoBackTraffic!'详见行程单'}"/>，标准<input type="text" style="width: 200px;" name="shoreGoBackTrafficStan"  value="${travelContractVO.shoreGoBackTrafficStan!'详见行程单'}"/>。<br/>
            岸上游览交通<input type="text" style="width: 100px;" name="shoreVisitTraffic"   value="${travelContractVO.shoreVisitTraffic!'详见行程单'}"/>，标准<input type="text" style="width: 200px;" name="shoreVisitTrafficStan"  value="${travelContractVO.shoreVisitTrafficStan!'详见行程单'}"/>。<br/>
            岸上旅游者自由活动时间<input type="text" style="width: 200px;" name="shoreFreeTime"  value="${travelContractVO.shoreFreeTime!'详见行程单'}"/>，次数<input type="text" style="width: 200px;" name="shoreFreeTimeCount"  value="${travelContractVO.shoreFreeTimeCount!'详见行程单'}"/>。<br/>
            岸上住宿安排（名称）及标准和住宿天数<input type="text" style="width: 260px;" name="shoreStayAndDays"  value="${travelContractVO.shoreStayAndDays!'详见行程单'}"/>。<br/>
            岸上用餐次数<input type="text" style="width: 200px;" name="shoreDinnerCount"  value="${travelContractVO.shoreDinnerCount!'详见行程单'}"/>，标准<input type="text" style="width: 200px;" name="shoreDinnerCountStan"  value="${travelContractVO.shoreDinnerCountStan!'详见行程单'}"/>。<br/>
            岸上地接社名称<input type="text" style="width: 100px;" name="localTravelAgencyName"   value="${travelContractVO.localTravelAgencyName!'详见行程单'}"/>，地址<input type="text" style="width: 200px;" name="localTravelAgencyAddress"  value="${travelContractVO.localTravelAgencyAddress!'详见行程单'}"/>。<br/>
            岸上地接社联系人<input type="text" style="width: 190px;" name="localTravelAgencyContact"  value="${travelContractVO.localTravelAgencyContact!'详见行程单'}"/>，联系电话 <input type="text" style="width: 150px;" name="localTravelAgencyMobile"  value="${travelContractVO.localTravelAgencyMobile!'详见行程单'}"/>。<br/>
            <b>第三条 旅游者保险</b><br/>
            乙方提示<b>甲方购买人身意外伤害保险和邮轮旅游意外保险</b>。经乙方推荐，甲方已经阅读并明确知晓上述保险的保险条款及其保单内容。甲方<input type="text" style="width: 100px;" name="personAccidentInsurance"  value="${travelContractVO.personAccidentInsurance!'不同意'}"/>（应填同意或不同意，打勾无效）委托乙方办理个人投保的人身意外伤害保险；甲方<input type="text" style="width: 100px;" name="cruiseInsurance" type="text" value="${travelContractVO.cruiseInsurance!'不同意'}"/>（应填同意或不同意，打勾无效）委托乙方办理个人投保的邮轮旅游意外保险。<br/>
            保险公司及产品名称<input type="text" style="width: 260px;" name="insuranceCompanyAndProductName"   value="${travelContractVO.insuranceCompanyAndProductName!'/'}"/>
            保险费人民币<input type="text" style="width: 300px;" name="insuranceAmount"  value="详见产品信息描述说明的保险金额"/>元/人。<br/><!--${travelContractVO.insuranceRMB!'/'}--><!--${travelContractVO.insuranceAmount!'/'}-->
            相关投保信息和约定以保单及其保险条款为准。<br/>
            <b>第四条 旅游费用及其支付（以人民币为计算单位）</b><br/>
            旅游费用包括：<input type="checkbox" name="cost" id="checkbox"/>邮轮船票费（含邮轮上指定的舱位、餐饮、游览娱乐项目和设施等）；<input type="checkbox" name="cost" id="checkbox2"/>船上服务费（小费）；<input type="checkbox" name="cost" id="checkbox3"/>港务费；<input type="checkbox" name="cost" id="checkbox4"/>签证费；<input type="checkbox" name="cost" id="checkbox5"/>签注费；<input type="checkbox" name="cost" id="checkbox6"/>乙方统一安排岸上游览景区景点的门票费、<input type="checkbox" name="cost" id="checkbox7"/>交通费、<input type="checkbox" name="cost" id="checkbox8"/>住宿费、<input type="checkbox" name="cost" id="checkbox9"/>餐费；<input type="checkbox" name="cost" id="checkbox10" disabled/>其他费用<em class="all-line" style="width:200px;">　</em>。<br/>
            甲方应交纳旅游费用<input type="text" style="width: 80px;"  name="traveAmount" value="${travelContractVO.traveAmount}"/>元，大写<input type="text" style="width: 150px;"  name="chineseNumeralTraveAmount" value="${travelContractVO.chineseNumeralTraveAmount}"/>元。<br/>
            旅游费用交纳期限<input type="text" style="width: 260px;"  name="visitCostPayExpire"  value="${travelContractVO.visitCostPayExpire!'以实际支付时间为准'}"/>。<br/>
            旅游费用交纳方式：<input type="text" style="width: 150px;" name="visitCostPayType"   value="${travelContractVO.visitCostPayType!'以实际支付方式为准'}"/><input type="checkbox" name="payment"/>现金；<input type="checkbox" name="payment"/>支票；<input type="checkbox" name="payment"/>信用卡；<input type="checkbox" name="payment" disabled/>其他<em class="all-line" style="width:150px;">　</em>。<br/>
            <b>第五条 双方的权利义务</b><br/>
            <b>（一）甲方的权利义务</b><br/>
            1．甲方有权知悉其购买的邮轮及岸上旅游产品和服务的真实情况，有权要求乙方按照约定提供产品和服务；有权拒绝乙方未经协商一致指定具体购物场所、安排另行付费旅游项目的行为；有权拒绝乙方未经事先协商一致将旅游业务委托给其他旅行社。<br/>
            2．甲方应自觉遵守旅游文明行为规范，遵守邮轮旅游产品说明中的要求，尊重船上礼仪和岸上旅游目的地的风俗习惯、文化传统和宗教禁忌，爱护旅游资源，保护生态环境；遵守《中国公民出国（境）旅游文明行为指南》等文明行为规范。甲方在旅游活动中应遵守团队纪律，配合乙方完成合同约定的旅游行程。<br/>
            3．甲方在签订合同或者填写材料时，应当使用有效身份证件，提供家属或其他紧急联络人的联系方式等，并对填写信息的真实性、有效性负责。限制民事行为能力人单独或由非监护人陪同参加旅游的，须征得监护人的书面同意；监护人或者其他负有监护义务的人，应当保护随行未成年旅游者的安全。<br/>
            4．甲方应当遵守邮轮旅游产品说明及旅游活动中的安全警示要求，自觉参加并完成海上紧急救生演习，对有关部门、机构或乙方采取的安全防范和应急处置措施予以配合。<br/>
            5．甲方不得随身携带或者在行李中夹带法律、法规规定及邮轮旅游产品说明中禁止带上船的违禁品。甲方应遵守邮轮禁烟规定，除指定的吸烟区域外，其余场所均禁止吸烟。<br/>
            6．在邮轮旅游过程中，甲方应妥善保管随身携带的财物。<br/>
            7．在邮轮上自行安排活动期间，甲方应认真阅读并按照邮轮方《每日须知》和活动安排，自行选择邮轮上的用餐、游览、娱乐项目等。在自行安排活动期间，甲方应在自己能够控制风险的范围内活动，选择能够控制风险的活动项目，并对自己的安全负责。<br/>
            8．甲方参加邮轮旅游以及岸上游览必须遵守集合出发和返回邮轮时间，按时到达集合地点。<br/>
            9．行程中发生纠纷，甲方应按本合同第八条、第十一条约定的方式解决，不得损害乙方和其他旅游者及邮轮方的合法权益，不得以拒绝上、下邮轮（机、车、船）等行为拖延行程或者脱团，不得影响港口、码头的正常秩序，否则应当就扩大的损失承担赔偿责任。<br/>
            10．甲方向乙方提交的出入境证件应当符合相关规定。甲方不得在境外非法滞留，随团出游的，不得擅自分团、脱团。<br/>
            11．甲方不能成行的，可以让具备参加本次邮轮旅游条件的第三人代为履行合同，并及时通知乙方。因代为履行合同增加或减少的费用，双方应按实结算。<br/>
            <b>（二）乙方的权利义务</b><br/>
            1．乙方提供的邮轮船票或凭证、邮轮旅游产品说明、登船相关文件、已订购服务清单，应由甲方确认，作为本合同组成部分。<br/>
            2．乙方提供旅游行程单，经双方签字或者盖章确认后作为本合同组成部分。<br/>
            3．乙方不得以不合理的低价组织旅游活动，诱骗甲方，并通过安排购物或者另行付费旅游项目获取回扣等不正当利益。<br/>
            4．乙方应在出团前，以说明会等形式如实告知邮轮旅游服务项目和标准，提醒甲方遵守旅游文明行为规范、遵守邮轮旅游产品说明中的要求，尊重船上礼仪和岸上旅游目的地的风俗习惯、文化传统、宗教禁忌。在合同订立及履行中，乙方应对旅游中可能危及甲方人身、财产安全的情况，作出真实说明和明确警示，并采取防止危害发生的适当措施。<br/>
            5．当发生延误或不能靠港等情况时，乙方应当及时向甲方发布信息，告知具体解决方案。<br/>
            6．乙方应妥善保管甲方提交的各种证件，依法对甲方信息保密。<br/>
            7．因航空、港务费、燃油价格等费用遇政策性调价导致合同总价发生变更的，双方应按实结算。<br/>
            8．甲方有下列情形之一的，乙方可以解除合同：<br/>
            &emsp;（1）患有传染病等疾病，可能危害其他旅游者健康和安全的；<br/>
            &emsp;（2）携带危害公共安全的物品且不同意交有关部门处理的；<br/>
            &emsp;（3）从事违法或者违反社会公德的活动的；<br/>
            &emsp;（4）从事严重影响其他旅游者权益的活动，且不听劝阻、不能制止的；<br/>
            &emsp;（5）法律规定的其他情形。<br/>
            因前款情形解除合同的，乙方应当按本合同第七条扣除必要的费用后，将余款退还甲方；给乙方造成损失的，甲方应当
            依法承担赔偿责任。<br/>
            9．成团人数与不成团的约定（二选一）<br/>
            <input type="radio" name="num" checked="checked"/>最低成团人数<input type="text" style="width: 50px;"  name="minPersonCountOfGroup" value="${travelContractVO.minPersonCountOfGroup}"/>人；低于此人数不能成团时，乙方应当提前 30 日通知甲方，本合同解除，向甲方退还已收取的全部费用。<br/>
            <input type="radio" name="num"/>本团成团不受最低人数限制。<br/>
            <b>第六条 甲方不适合邮轮旅游的情形</b><br/>
            因邮轮上没有专科医师及医疗设施，邮轮离岸后无法及时进行急救和治疗，为防止途中发生意外，甲方购买邮轮旅游产品、接受旅游服务时，应当如实告知与邮轮旅游活动相关的个人健康信息，参加适合自身条件的邮轮旅游活动。如隐瞒有关个人健康信息参加邮轮旅游，由甲方承担相应责任。<br/>
            <b>第七条 甲方解除合同及承担必要费用</b><br/>
            因甲方自身原因导致合同解除，乙方按下列标准扣除必要费用后，将余款退还甲方：<br/>
            （一）甲方在行程前解除合同的，双方约定扣除必要费用的标准为：<br/>
            &emsp;&emsp;1．行程前 <em class="all-line" style="width:20px">90</em>日至 <em class="all-line" style="width:20px">60</em> 日，旅游费用 <em class="all-line" style="width:20px">30</em>％；<br/>
            &emsp;&emsp;2．行程前 <em class="all-line" style="width:20px">59</em>日至 <em class="all-line" style="width:20px">30</em>日，旅游费用 <em class="all-line" style="width:20px">50</em>％；<br/>
            &emsp;&emsp;3．行程前 <em class="all-line" style="width:20px">29</em>日至 <em class="all-line" style="width:20px">15</em>日，旅游费用 <em class="all-line" style="width:20px">80</em>％；<br/>
            &emsp;&emsp;4．行程前 <em class="all-line" style="width:20px">14</em>日至 <em class="all-line" style="width:20px">1</em>日，旅游费用 <em class="all-line" style="width:20px">100</em>％；<br/>
            &emsp;&emsp;5．行程开始当日，旅游费用 <em class="all-line" style="width:20px">100</em>％。<br/>
            甲方行程前逾期支付旅游费用超过<em class="all-line" style="width:20px">1</em>日的，或者甲方未按约定时间到达约定集合出发地点，也未能在中途加入旅游
            的，乙方有权解除合同，乙方可以按本款规定扣除必要的费用后，将余款退还甲方。<br/>
            （二）甲方因疾病等自身的特殊原因，导致在行程中解除合同的，必要的费用扣除标准为：（二选一）<br/>
            &emsp;&emsp;<input type="radio" name="lycost" disabled="disabled"/>1．双方可以进行约定并从其约定：<br/>
            &emsp;&emsp;旅游费用－（<em class="all-line" style="width:20px"> </em>）－（<em class="all-line" style="width:20px"> </em>）－（<em class="all-line" style="width:20px"> </em>）－（<em class="all-line" style="width:20px"> </em>）<br/>
            &emsp;&emsp;<input type="radio" name="lycost" checked="checked"/>2．双方未约定的，按照下列标准扣除必要的费用。<br/>
            旅游费用 X 行程开始当日扣除比例＋（旅游费用－旅游费用×行程开始当日扣除比例）÷旅游天数×已经出游的天数。<br/>
            如按上述（一）或（二）约定标准扣除的必要费用低于实际发生的费用，按照实际发生的费用扣除，但最高额不应当超过旅游费用总额。<br/>
            行程前解除合同的，乙方扣除必要费用后，应当在合同解除之日起 <em class="all-line" style="width:20px">30</em> 个工作日内向甲方退还剩余旅游费用。<br/>
            行程中解除合同的，乙方扣除必要费用后，应当在协助甲方返回出发地或者到达甲方指定的合理地点后 <em class="all-line" style="width:20px">45</em> 个工作日内向甲方退还剩余旅游费用。<br/>
            <b>第八条 责任减免及不可抗力情形的处理</b><br/>
            （一）<b>具有下列情形的旅行社免责</b><br/>
            &emsp;&emsp;1．因甲方原因造成自己人身损害、财产损失或造成他人损失的，由甲方承担相应责任，但乙方应协助处理。<br/>
            &emsp;&emsp;2．因不可抗力造成甲方人身损害、财产损失的，乙方不承担赔偿责任，但应积极采取救助措施。<br/>
            &emsp;&emsp;3．在自行安排活动期间甲方人身、财产权益受到损害的，乙方在事前已尽到必要警示说明义务且事后已尽到必要救助义务的，乙方不承担赔偿责任。<br/>
            &emsp;&emsp;4．甲方因参加非乙方安排或推荐的活动导致人身损害、财产损失的，乙方不承担赔偿责任。<br/>
            &emsp;&emsp;5．由于公共交通经营者的原因造成甲方人身损害、财产损失的，由公共交通经营者依法承担赔偿责任，乙方应当协助甲方向公共交通经营者索赔。因公共交通工具延误，导致合同不能按照约定履行的，乙方不承担违约责任，但应向甲方退还未实际发生的费用。<br/>
            （二）因发生不可抗力情形或者乙方、履行辅助人已尽合理注意义务仍不能避免的事件，可能导致邮轮行程变更或取消
            部分停靠港口等情况时，按以下约定方式处理。<br/>
            1．行程前发生的，甲方可以按（1）或（2）选择（二选一）<br/>
            <input type="radio" name="cltype" checked="checked"/>（1）甲方同意邮轮行程变更或取消部分停靠港口等，按以下约定处理：<br/>
            &emsp;&emsp;①在不减少行程自然天数的情况下，启航延迟、港口停靠时间缩短、返航延迟抵达：船方提供餐食和各项服务，乙方退还旅游费用总额的 <em class="all-line" style="width:20px">1</em> ％。<br/>
            &emsp;&emsp;②无法停靠目的地港口：退还该港口的港务费以及未发生的岸上观光费用。<br/>
            &emsp;&emsp;③行程自然天数减少：扣除已实际支付且不可退还的费用后，按照减少行程的自然天数所占计划行程的百分比退还旅游费用。<br/>
            <input type="radio" name="cltype" disabled="disabled"/>（2）甲方不同意邮轮行程变更或取消部分停靠港口等上述约定，解除本合同；乙方应当在扣除已实际支付且不可退还的费用后，将余款<em class="all-line" style="width:20px"> </em>元退还甲方。<br/>
            2．行程中发生的，按上述（1）的约定处理。<br/>
            <b>第九条 违约责任</b><br/>
            （一）乙方在行程前 30 日以内（含第 30 日，下同）提出解除合同的，向甲方退还全额旅游费用（不得扣除签证／签注等费用），并按下列标准向甲方支付违约金：<br/>
            &emsp;&emsp;1．行程前 <em class="all-line" style="width:20px">30</em> 日至 <em class="all-line" style="width:20px">15</em> 日，支付旅游费用总额 <em class="all-line" style="width:20px">2</em>％的违约金；<br/>
            &emsp;&emsp;2．行程前 <em class="all-line" style="width:20px">14</em> 日至 <em class="all-line" style="width:20px">7</em> 日，支付旅游费用总额 <em class="all-line" style="width:20px">5</em>％的违约金；<br/>
            &emsp;&emsp;3．行程前 <em class="all-line" style="width:20px">6</em> 日至 <em class="all-line" style="width:20px">4</em>日，支付旅游费用总额 <em class="all-line" style="width:20px">10</em>％的违约金；<br/>
            &emsp;&emsp;4．行程前 <em class="all-line" style="width:20px">3</em> 日至 <em class="all-line" style="width:20px">1</em> 日，支付旅游费用总额 <em class="all-line" style="width:20px">15</em>％的违约金；<br/>
            &emsp;&emsp;5．行程开始当日，支付旅游费用总额 <em class="all-line" style="width:20px">20</em>％的违约金。<br/>
            如上述违约金不足以赔偿甲方的实际损失，乙方应当按实际损失对甲方予以赔偿。<br/>
            乙方应当在解除合同通知到达日起 <em class="all-line" style="width:20px">30</em> 个工作日内，向甲方全额退还已收旅游费用并支付违约金。<br/>
            （二）甲方逾期支付旅游费用的，应当每日按照逾期支付部分的旅游费用的 <em class="all-line" style="width:20px">0.3</em> ％，向乙方支付违约金。<br/>
            （三）甲方提供的个人信息及相关材料不真实而造成的损失，由其自行承担；如给乙方造成损失的，甲方还应当承担赔偿责任。<br/>
            （四）甲方因不听从乙方的劝告、提示而影响旅游行程，给乙方造成损失的，应当承担相应的赔偿责任。<br/>
            （五）乙方未按合同约定标准提供交通、住宿、餐饮等服务，或者违反本合同约定擅自变更旅游行程，给甲方造成损失的，应当承担相应的赔偿责任。<br/>
            （六）乙方未经甲方同意，擅自将旅游业务委托给其他旅行社的，甲方在行程前（不含当日）得知的，有权解除合同，乙方全额退还已收旅游费用，并按旅游费用的15％支付违约金；甲方在行程开始当日或者行程开始后得知的，乙方应当按旅游费用的 25％支付违约金。如违约金不足以赔偿甲方的实际损失，乙方应当按实际损失对甲方予以赔偿。<br/>
            （七）乙方未经与甲方协商一致或者未经甲方要求，指定具体购物场所或安排另行付费旅游项目的，甲方有权在旅游行程结束后三十日内，要求乙方为其办理退货并先行垫付退货货款，或者退还另行付费旅游项目的费用。<br/>
            （八）乙方具备履行条件，经甲方要求仍拒绝履行合同，造成甲方人身损害、滞留等严重后果的，甲方除要求乙方承担相应的赔偿责任外，还可以要求乙方支付旅游费用 <em class="all-line" style="width:20px">壹</em> 倍（一倍以上三倍以下）的赔偿金。<br/>
            （九）其他违约责任：<input type="text" style="width: 260px;" name="otherBreakContractDuty"  value="${travelContractVO.otherBreakContractDuty!''}"/>。<br/>
            <b>第十条 自愿购物和参加另行付费旅游项目约定</b><br/>
            1．甲方可以自主决定是否参加乙方安排的购物活动、另行付费旅游项目。<br/>
            2．乙方可以在不以不合理的低价组织旅游活动、不诱骗甲方、不获取回扣等不正当利益，且不影响其他旅游者行程安排的前提下，按照平等自愿、诚实信用的原则，与甲方协商一致达成购物活动、另行付费旅游项目补充协议。<br/>
            3．购物活动、另行付费旅游项目安排应不与旅游行程单冲突。<br/>
            4．地接社及其从业人员在行程中安排购物活动、另行付费旅游项目的，责任由订立本合同的乙方承担。<br/>
            5．购物活动、另行付费旅游项目具体约定见《自愿购物活动补充协议》（附件 1）、《自愿参加另行付费旅游项目补充协议》（附件 2）。<br/>
            <b>第十一条 争议解决方式</b><br/>
            双方发生争议的，可协商解决，也可在旅游合同结束之日 90 天内向旅游质监机构申请调解，或提请上海仲裁委员会仲裁<b>（不愿意仲裁而选择向法院提起诉讼的，请双方在签署合同时将此仲裁条款划去）</b>。<br/>
            <b>第十二条 附则</b><br/>
            本合同自双方签字或盖章之日起生效，本合同附有的旅游行程单、邮轮旅游产品说明和补充条款、补充协议等均为合同的附件，与本合同具有同等法律效力。<br/>
            <em class="all-line" style="padding:0 10px;width:100%;">${'详见补充条款'}</em><br/>
            <em class="all-line" style="padding:0 10px;width:100%;">　</em><br/>
            <br/>

            甲方签字（盖章） ：<input type="text" style="width: 160px; margin-right: 10px;" value="${(travelContractVO.travellers)!''}"/> 乙方签字（盖章） ：<em class="all-line" style="width:150px;position:relative;"><span class="stamp" style="left:0px;top:-35px;"></span></em><br>
            住　　所：<em class="all-line" style="width:200px; margin-right: 10px;">${(travelContractVO.personAddress)!'/'}</em>营业场所：<input type="text" style="width: 200px;" name="address"  value="${(travelContractVO.address)!'/'}"/><br>
            甲方代表：<input type="text" style="width: 210px; margin-top: 6px; margin-right: 10px;" name="firstDelegatePersonName"/> 乙方代表（经办人） ：<em class="all-line" style="width:137px">　</em><br>
            联系电话：<em class="all-line" style="width:200px; margin-right: 10px;">${(travelContractVO.contactTelePhoneNo)!'/'}　</em>联系电话：<input type="text" style="width: 200px;" name="lvMobile"  value="${(travelContractVO.lvMobile)!'1010-6060'}"/><br>
            邮　　编：<em class="all-line" style="width:200px; margin-right: 10px;">${(travelContractVO.postcode)!'/'}　</em>邮　　编：<input type="text" style="width: 200px;" name="lvPostcode"  value="${(travelContractVO.lvPostcode)!'/'}"/><br>
            日　　期：<input type="text" style="width: 210px; margin-top: 6px; margin-right: 10px;" name="firstSignatrueDate" value="${travelContractVO.firstSignatrueDate!''}"/> 日　　期：<input type="text" style="width: 210px; margin-top: 6px;" name="secondSignatrueDate" value="${travelContractVO.secondSignatrueDate!''}"/> <br>
            <br>
            <br>
            <br>
            <br>
        </p>

        <p><b>附件1 </b></p>
        <b style="font-size: 16px;">自愿购物活动补充协议</b><br><br>
        <p>
            1．甲方可以自主决定是否参加乙方安排的购物活动；<br>
            2．乙方可以在不以不合理的低价组织旅游活动、不诱骗甲方、不获取回扣等不正当利益，且不影响其他旅游者行程安排的前提下，按照平等自愿、诚实信用的原则，与甲方协商一致达成购物活动的约定；<br>
            3．购物活动安排应不与《行程单》冲突；<br>
            4．具体购物场所应当同时面向其他社会公众开放；<br>
            5．地接社及其从业人员在行程中安排购物活动，责任由订立本合同的乙方承担；<br>
            6．购物活动具体约定如下:<br>
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
            <tbody>
            <tr>
                <td width="150px">具体时间</td>
                <td width="40px">地点</td>
                <td width="70px">购物场所名称</td>
                <td width="70px">主要商品信息</td>
                <td width="70px">最长停留时间（分钟）</td>
                <td width="70px">其他说明</td>
                <td width="200px">旅游者签名同意</td>
            </tr>
            <#if (travelContractVO.shopingDetailList)?? && (travelContractVO.shopingDetailList?size)!=0>
                <#list travelContractVO.shopingDetailList as prodContractDetail>
                <tr>
                    <td style="text-align:left;">
                        <em class="all-line" style="width:20px;">　</em>年
                        <em class="all-line" style="width:10px;">　</em>月
                        <em class="all-line" style="width:10px;">　</em>日
                    </td>
                    <td><@func.addSpace prodContractDetail.address 7/></td>
                    <td><@func.addSpace prodContractDetail.detailName 7/></td>
                    <td><@func.addSpace prodContractDetail.detailValue 6/></td>
                    <td>${prodContractDetail.stay!''}</td>
                    <td><@func.addSpace prodContractDetail.other 10/></td>
                    <td style="text-align:left;">签名：<em class="all-line" style="width:50px;">${(travelContractVO.travellers)!''}　</em></td>
                </tr>
                </#list>
            <#else>
            <tr>
                <td style="text-align:left;">
                    <em class="all-line" style="width:20px;">　</em>年
                    <em class="all-line" style="width:10px;">　</em>月
                    <em class="all-line" style="width:10px;">　</em>日
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：<em class="all-line" style="width:50px;">　</em></td>
            </tr>
            <tr>
                <td style="text-align:left;">
                    <em class="all-line" style="width:20px;">　</em>年
                    <em class="all-line" style="width:10px;">　</em>月
                    <em class="all-line" style="width:10px;">　</em>日
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：<em class="all-line" style="width:50px;">　</em></td>
            </tr>
            <tr>
                <td style="text-align:left;">
                    <em class="all-line" style="width:20px;">　</em>年
                    <em class="all-line" style="width:10px;">　</em>月
                    <em class="all-line" style="width:10px;">　</em>日
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：<em class="all-line" style="width:50px;">　</em></td>
            </tr>
            </#if>
            </tbody></table>
        <br/>
        甲方签名：<input type="text" style="width: 167px;" readonly="readonly" value="${(travelContractVO.travellers)!''}"/>
        &emsp;&emsp;乙方（经办人）签名：<em class="all-line" style="width:150px;position:relative;"><span class="stamp" style=" left:-46px;top:-66px;"></span>　</em><br>
        <span style="margin-left: 35px;">
        	<input type="text" style="width: 200px;" name="firstSignDateStr" value="<#if travelContractVO.firstSignDateStr??>${(travelContractVO.firstSignDateStr)!'/'}</#if>"/>
        </span>
        <span style="margin-left: 120px;">
        	<input type="text" style="width: 200px;" name="secondSignDateStr" value="<#if travelContractVO.secondSignDateStr??>${(travelContractVO.secondSignDateStr)!'/'}</#if>"/>
        </span>
        <br>
        <br>
        <br>
        <br>
        <br>
        </p>

        <p><b>附件２</b></p>
        <b style="font-size: 16px;">自愿参加另行付费旅游项目补充协议</b><br>
        <p>
            1．甲方可以自主决定是否参加乙方安排的另行付费旅游项目；<br>
            2．乙方可以在不以不合理的低价组织旅游活动、不诱骗甲方、不获取回扣等不正当利益，且不影响其他旅游者行程安排的前提下，按照平等自愿、诚实信用的原则，与甲方协商一致达成另行付费旅游项目的约定；<br>
            3．另行付费旅游项目安排应不与《行程单》冲突；<br>
            4．另行付费旅游项目经营场所应当同时面向其他社会公众开放；<br>
            5．地接社及其从业人员在行程中安排另行付费旅游项目的，责任由订立本合同的乙方承担；<br>
            6．另行付费旅游项目具体约定如下：<br>
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
            <tbody>
            <tr>
                <td width="150px">具体时间</td>
                <td width="40px">地点</td>
                <td width="70px">购物场所名称</td>
                <td width="70px">主要商品信息</td>
                <td width="70px">最长停留时间（分钟）</td>
                <td width="70px">其他说明</td>
                <td width="200px">旅游者签名同意</td>
            </tr>
            <#if  (travelContractVO.recommendDetailList)?? && (travelContractVO.recommendDetailList?size)!=0>

                <#list travelContractVO.recommendDetailList as prodContractDetail>
                <tr>
                    <td style="text-align:left;">
                        <em class="all-line" style="width:20px;">　</em>年
                        <em class="all-line" style="width:10px;">　</em>月
                        <em class="all-line" style="width:10px;">　</em>日
                    </td>
                    <td><@func.addSpace prodContractDetail.address 8/></td>
                    <td><@func.addSpace prodContractDetail.detailName 8/></td>
                    <td><@func.addSpace prodContractDetail.detailValue 8/></td>
                    <td>${prodContractDetail.stay!''}</td>
                    <td><@func.addSpace prodContractDetail.other 10/></td>
                    <td style="text-align:left;">签名：<em class="all-line" style="width:50px;"><!--${(travelContractVO.travellers)!''}-->　</em></td>
                </tr>
                </#list>
            <#else>
            <tr>
                <td style="text-align:left;">
                    <em class="all-line" style="width:20px;">　</em>年
                    <em class="all-line" style="width:10px;">　</em>月
                    <em class="all-line" style="width:10px;">　</em>日
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：<em class="all-line" style="width:50px;">　</em></td>
            </tr>
            <tr>
                <td style="text-align:left;">
                    <em class="all-line" style="width:20px;">　</em>年
                    <em class="all-line" style="width:10px;">　</em>月
                    <em class="all-line" style="width:10px;">　</em>日
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：<em class="all-line" style="width:50px;">　</em></td>
            </tr>
            <tr>
                <td style="text-align:left;">
                    <em class="all-line" style="width:20px;">　</em>年
                    <em class="all-line" style="width:10px;">　</em>月
                    <em class="all-line" style="width:10px;">　</em>日
                </td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td style="text-align:left;">签名：<em class="all-line" style="width:50px;">　</em></td>
            </tr>
            </#if>
            </tbody></table>
        <br/>
        甲方签名：<input type="text" style="width: 167px;" readonly="readonly" value=""/>
        &emsp;&emsp;乙方（经办人）签名：<em class="all-line" style="width:150px;position:relative;"><span class="stamp" style=" left:-66px;top:-46px;"></span>　</em><br>
        <span style="margin-left: 35px;">
        	<input type="text" style="width: 200px;" name="thirdSignDateStr" value="<#if travelContractVO.thirdSignDateStr??>${(travelContractVO.thirdSignDateStr)!'/'}</#if>"/>
        </span>
        <span style="margin-left: 120px;">
        	<input type="text" style="width: 200px;" name="fourthSignDateStr" value="<#if travelContractVO.fourthSignDateStr??>${(travelContractVO.fourthSignDateStr)!'/'}</#if>"/>
        </span>
        <br>
        <br>
        <br>
        <br>
        <br>
        </p>

        <p>
            <b>附件3：邮轮旅游报名表</b><br/>
            旅游线路及编号<em class="all-line" style="width:200px;">${travelContractVO.productName!''}    ${travelContractVO.productId!''}　</em>旅游者出团意向时间<em class="all-line" style="width:200px;">${travelContractVO.visitTime!''}　</em><br/>
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
        <#if travelContractVO.ordTravellerList?? && travelContractVO.ordTravellerList?size gt 0>
            <#list travelContractVO.ordTravellerList  as person>
                <tr>
                    <td width="100px">姓名</td>
                    <td width="150px">${person.fullName!''}</td>
                    <td width="80px">性别</td>
                    <td width="80px">${person.genderName!''}</td>
                    <td width="80px">民族</td>
                    <td width="80px"></td>
                    <td width="120px">出生日期</td>
                    <td width="200px"><#if (person.birthday)??>${person.birthday?string("yyyy-MM-dd")}</#if></td>
                </tr>
                <tr>
                    <td colspan="2">身份证件号码</td>
                    <td colspan="4"><#if person.idType == "ID_CARD">${person.idNo!''}</#if></td>
                    <td>联系电话</td>
                    <td>${person.mobile!''}</td>
                </tr>
                <tr>
                    <td>国籍</td>
                    <td>${person.nationality!''}</td>
                    <td colspan="2">出境证件号</td>
                    <td colspan="4"><#if person.idType != "ID_CARD">${person.idNo!''}</#if></td>
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
                <td colspan="2">身份证件号码</td>
                <td colspan="4"></td>
                <td>联系电话</td>
                <td></td>
            </tr>
            <tr>
                <td>国籍</td>
                <td></td>
                <td colspan="2">出境证件号</td>
                <td colspan="4"></td>
            </tr>
            <tr>
                <td colspan="2">身体状况</td>
                <td colspan="6" style="text-align:left;"><span></span><br/>（需注明是否有身体残疾、精神疾病、高血压、心脏病等健康受损病症、病史，是否为妊娠期妇女。）</td>
            </tr>
        </#if>
            <tr>
                <td colspan="8" style="text-align:left;">
                    其他补充约定：<br/>
                    <span></span><br/>
                    旅游者确认签名（盖章）：<em class="all-line" style="width:120px;">
                <#--<#list travelContractVO.ordTravellerList  as person>
                ${person.fullName!''}
                </#list>-->${(travelContractVO.travellers)!''}　</em>　　
                    <em class="all-line" style="width:200px;"><#if travelContractVO.createTime??>${(travelContractVO.createTime)!''}</#if>　</em>
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
        <br/><br/><br/><br/><br/>
        </p>
        <p>
            <b>附件4：</b><br/>
        <table border="0" cellspacing="1" cellpadding="0" class="tab1">
            <tr>
                <td><b style="font-size: 16px;">产品信息描述说明</b></td>
            </tr>
            <tr>
                <td style="height: 600px;">
            	<textarea name="supplementaryTerms" style="width: 620px; height: 600px;" >${(travelContractVO.supplementaryTerms)!''}</textarea>
                </td>
            </tr>
        </table>
        <br/><br/><br/><br/><br/>
        </p>
        <p>
            <b>附件5：</b><br/>
        </p>
        <b style="font-size: 16px;">补充条款</b><br/>
        <p>
            1、为办理甲方旅游的证件（含签证）、预留邮轮舱位等，如需甲方事先支付旅游预付款的，甲方应予以支付，乙方应出具该款项的收款凭证。<br/>
            2、甲方对向乙方提供的办理参加旅游活动的所需的所有资料真实性及完整性负责，有时效限制的资料应在规定的有效期限内；若签证审查中须增补材料的，甲方应及时提供所需的补充材料。如甲方因自身签证材料原因或领馆原因造成拒签的，所产生相关签证等损失费用由甲方承担；如甲方提供虚假材料的，所产生的损失费由甲方承担。<br/>
            3、甲方应根据自身健康状况报名参加邮轮上或港口岸上的旅游活动，甲方应在报名参团时向乙方提供个人正确信息（包括健康疾病状况等信息或证明）。若因甲方虚报、瞒报上述有关情况，一经发现或旅途中发生意外，由甲方承担全部责任和后果；且乙方有权单方面解除旅游合同，拒绝甲方继续参团；给乙方造成损失的，甲方应当承担赔偿责任。<br/>
            4、甲方若携带未成年人出游，其父母需携带孩子的出生证明。如果小孩由非直系亲属携带出游，则孩子的直系亲属（父母）需要填写邮轮公司版本的未成年人授权书；出游的非直系亲属需填写随行监护人承诺书。<br/>
            5、甲方为 70 周岁（含 70 周岁）以上的出游者，需携带或提供经政府认可的有关医疗卫生部门出具的健康证明，并由监护人或直系亲属填写其具备出行能力的承诺书。<br/>
            6、六个月以下婴儿及怀孕二十四周以上的孕妇不适合邮轮旅游，乙方不接受上述游客参团，由于游轮上的医院没有配备帮助孕妇分娩的医疗设施，因此到预定的航次结束之日前已经怀孕 24 周的游客若预定申请将不被接受。所有孕妇在登船时必需提供自己与婴儿能够参加预定航次的健康证明，健康证明必需注明孕妇的预产期。对孕妇在船上或运送途中可能产生的与怀孕有关的问题游轮公司和承运人、乙方将不负任何责任。年龄未满 6 个月的婴儿不得登船。对于 15 天或更长时间的巡游，此最低年龄限制可提高至 12 个月。<br/>
            7、甲方报名参团发生自然单间或加床时需另补差价，若因甲方原因解除合同的, 仍应承担单房差或加床费用损失。甲方若同意乙方可根据参团客源情况拼房的，应当对拼房后可能产生的不便等有充分认知。<br/>
            <b>8、甲方已清楚且愿意遵守：</b>甲方已清楚且愿意遵守 ： 乙方提供的船票系特价舱位船票或乙方与邮轮公司约定不签转、更改或退票的船票，不得签转、更改或退票。<br/>
            9、国家旅游局未发出中国公民暂停前往该目的地的旅游警告前，甲方自行取消本次旅游导致本合同提前解除时，由甲方根据本合同规定支付乙方全部费用。国家旅游局发出中国公民暂停前往该目的地的旅游警告时，甲乙双方签署的合同自动解除，乙方扣除已向境外地接社或者履行辅助人或邮轮公司支付且不可退还的费用（包括但不限于签证费等）外，剩余费用退还甲方。<br/>
            10、由公共交通经营者（包括但不限于航空公司、铁路公司、航运公司等）的原因造成甲方人身损害或财产损失的，由公共交通经营者依法承担赔偿责任，乙方应协助甲方向公共交通经营者索赔。<br/>
            <b>11、凡持非中国大陆护照的游客（包括港、澳、台客人），或者自备签证的旅游者已明确知晓：</b>凡持非中国大陆护照的游客（包括港、澳、台客人） ，或者自备签证的旅游者已明确知晓：应自行办理本次旅游签证和再次回中国内地大陆的签证。如因签证问题造成出入境受阻，由此产生的一切后果和相关费用由甲方全部自行承担。<br/>
            12、甲方在邮轮旅游中的前往国或地区入境内或在岸上观光期间，由于甲方自身原因而被当地移民局或海关拒绝入境或遣返或滞留不归，由此产生的一切后果及相关费用由甲方自行承担。<br/>
            13、甲方旅游行程中应遵守文明旅游、安全旅游规定。在邮轮上应按照邮轮标示或说明合理使用船上设施、不浪费食物，因甲方个人原因使用船上设施不当而产生的意外，甲方责任自负。<br/>
            14、甲方有不文明旅游行为且经乙方或邮轮人员劝阻屡次不听的，视为本合同第五条（二）第 8 项（3）从事违反社会公德的行为，所产生的不利后果，由甲方自行承担；如给乙方造成损失，甲方还应当承担赔偿责任。<br/>
            15、甲方应按使领馆要求于规定时间地点前往使领馆进行签证面试，以及旅游回国后须按使领馆要求履行“销签”义务，或在规定时间内去使领馆面试“销签” ，并自行承担因此产生的费用。<br/>
            16、甲方自行保管护照、证件时，如发生遗失、毁损等，由此产生的一切后果和相关费用由甲方全部自行承担。<br/>
            17、甲方同意向乙方支付出境旅游保证金<em class="all-line" style="width:20px;">　</em>万元人民币 / 人，或按照乙方要求增加保证金金额或承担连带责任。双方在保证金事宜上未达成一致的，乙方有权终止本合同；甲方同意乙方在扣除相应业务损失费后，退回剩余的费用。若甲方在境外擅自脱团或不按旅游行程入境或滞留不归时，自愿将该保证金作为违约金全部用于赔偿乙方外事信誉和经济损失。甲方按时随团入境的，乙方应按甲方保证金支付的同等方式全额退还该保证金本金。<br/>
            18、若甲方在邮轮或境外违反当地法律法规或由于甲方个人原因造成经济损失的， 由此产生的一切后果和相关费用应由甲方自行承担。若甲方拒绝承担或无力承担的，甲方同意乙方有权从甲方的出境旅游保证金中直接扣除该笔费用。<br/>
            <b>19、乙方明确告知甲方：</b>乙方不组织行程单以外任何活动；应甲方的要求，安排购物或者另付费旅游项目的，经双方协商一致且不影响其他旅游者行程安排的除外。甲方在购物时应知晓商品的质量和价格，并获取相关凭证。<br/>
            20、甲方同意乙方利用互联网等技术或服务手段，向甲方送达行前说明内容的电子版本、音、视频资料并取得乙方的接收确认；当甲方因故未能接受行前服务时，乙方可采取以下服务形式作为应急措施或补救手段：<br/>
            a) 行程开始当天，在机场、车站、码头等公共区域临时举行；<br/>
            b) 前往旅游目的地的交通工具上临时举行；<br/>
            c) 在旅游过程中，通过播放音频、视频资料或由履行辅助人宣讲等进行。<br/>
            <b>21、关于自愿购物和参加另行付费旅游项目约定如下：</b><br/>
            （1）甲方可以自主决定是否参加乙方安排的购物活动、另行付费旅游项目；<br/>
            （2） 乙方可以在不以不合理的低价组织旅游活动、不诱骗旅游者、不获取回扣等不正当利益，且不影响其他旅游者行程安排的前提下，按照平等自愿、诚实信用的原则，与甲方协商一致达成购物活动、另行付费旅游项目协议；<br/>
            （3）购物活动、另行付费旅游项目安排应不与《行程单》冲突；<br/>
            （4）购物活动、另行付费旅游项目具体约定见《自愿购物活动补充协议》（附件 1）、《自愿参加另行付费旅游项目补充协议》（附件 2），本补充协议关于自愿购物和另行付费旅游项目的约定与《自愿购物活动补充协议》、《自愿参加另行付费旅游项目补充协议》约定不一致的，以《自愿购物活动补充协议》、《自愿参加另行付费旅游项目补充协议》的约定为准。<br/>
            <b>22、甲乙双方一致同意：</b>本次行程若系“自由行”产品 ( 以乙方网站展示的产品性质为唯一标准 ) 的, 甲方不要求乙方安排领队人员，在无乙方人员陪同出行的情况下，行程中各项协助工作均由甲方自行完成。<br/>
            23、甲方违反本合同第 5.1.2 条约定的内容，造成乙方声誉和经济损失的，应负责赔礼道歉，并赔偿给乙方造成的经济损失。<br/>
            24、甲方违反本合同第 5.1.4 条约定的内容，甲方应对自身原因引发任何问题负责，乙方可给予协助配合。<br/>
            25、若甲方未委托乙方购买乙方推荐人身意外伤害保险和邮轮旅游意外保险，甲方自身也未购买任何意外保险，因此引发的任何问题由甲方自行承担，与乙方无关 。<br/>
            26、甲方承诺其签字代表在本合同及相关补充协议，附件或其他相关文件上的签字，代表附件《邮轮旅游报名表》所有游客的意思表示，并已取得所有游客的合法授权。一旦签字代表在本合同及相关补充协议，附件或其他相关文件上签字，乙方有理由相信签字代表是附件《邮轮旅游报名表》所有游客的代表。<br/>
            27、本合同第 11 条约定的争议解决方式，甲方双方协商一致，同意选择将争议事项向上海市嘉定区人民法院提起诉讼解决。<br/>
            <b>28、甲方同意：</b>甲乙双方签署的本合同及相关补充条款、附件且自甲方付清全额旅游费用之日起生效。<br/>
            29、若甲乙双方有违反本补充条款之约定的，均可参照本合同违约条款追究违约责任。<br/>
            <b>30、甲方同意：</b>《出团通知书》、《行程单》、以及补充协议、全部附件均作为本合同组成部分，与合同具有同等效力。<br/>
            <b>31、本合同签署的甲方已阅读上述《补充条款》，且经乙方人员详细解释说明，已完全知晓上述全部内容；并愿意遵守和同意将此补充条款作为本合同附件，与本合同具有同等效力。且本合同的签署代表对自身的代理权真实性和合法性承担法律责任，并承担负责转告未签名旅游者上述安全提示警示告知全部内 容的责任。</b><br/>
            <br/>
            <br/>
            <span style="float:right">签字：<em class="all-line" style="width:150px">${(travelContractVO.travellers)!''}　</em></span>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
        </p>
        <p>
            <b>附件6：</b><br/>
        </p>
        <b style="font-size: 16px;">邮轮旅游安全提示警示告知书</b><br/>
        <p>
            <b>尊敬的游客：</b><br/>
            <b>为了确保您的旅游行程安全顺利，我公司特就旅游行程中应注意的安全事项，向您提示、警示并明确告知，请您仔细阅读。</b><br/>
            一、旅游出行前建议购买旅游者人身意外伤害保险，并请您出行前告知家人参团旅行社名称及行程、紧急联系电话等。<br/>
            二、临行前确保身体健康，如有体质较弱或者曾患病者必须坚持治疗，防止旧病复发；平时需要用药治疗者，出游时请带足所需药品；旅行社不建议患有高血压、心脏病、糖尿病以及身体残疾等身体状况不适宜旅游的客人参团出游，如执意参加者须征得医生同意，自备药品，或者征得家属同意或由家属陪伴；如因自身原因发生任何意外情形，责任自负，公司按客人书面请求给予协助。<br/>
            三、70 岁以上老年人、18 周岁以下未成年人参团旅游的，须由家属或监护人陪同出游，并做好安全防护措施，保证前述人员的安全。否则，因其本人或家属或监护人的自身原因导致的任何意外情形，责任自负，公司根据客人的书面请求给予协助。未有家属或监护人同行的情况下，公司可根据实际情况决定是否接受前述人员报名参团；如获准参加，前述人员的安全及因其自身原因导致的任何意外情形，由前述人员责任自负。<br/>
            四、对于有身孕（5 个月以下）的客人（怀孕 5 个月以上，边检将限制出入境。若虚报或隐瞒怀孕月份，在出入境边检时受阻，其后果自负），因在旅途中存在诸多对孕妇不利因素，如长途乘机、坐车及颠簸震动、景点上下、气候变化等，故公司不建议参团出游，尤其是出境旅游；若客人隐瞒孕情或坚持参团，须对自身健康和孕情负责；若在旅游行程中发生任何意外情形，责任自负，公司按客人书面请求给予协助。<br/>
            <b>五、公司郑重提醒客人：</b>根据自身健康状况，谨慎参加赛车、骑马、攀岩、滑翔、探险、漂流、潜水、游泳、滑雪、滑冰、滑板、跳伞、热气球、蹦极、冲浪等高风险活动或不在公司旅游行程内的活动；游客在自由活动时间内须选择自身能够控制风险的活动及旅游项目，并在自己能够控制风险的范围内进行活动；<b>若客人违反本安全警示告知而所导致的人身伤害或财产损失，公司不承担赔偿责任。</b><br/>
            <b>六、公司郑重提示警示自由行旅游客人：</b>请您在自由行旅游过程中，务必充分了解拟参与的旅游活动或项目的内容及风险，并根据自身身体状况和健康状况慎重评估后选择，自觉遵守该旅游活动或项目的安全规定。 <b>公司对您违反本提示所发生的任何意外事故不承担任何责任。</b><br/>
            <b>七、公司郑重提示警示客人：</b>旅游中，客人在乘坐任何交通工具（包括车辆、游船、游艇以及任何游览项目中承载游客的设施或设备）时，不得将身体任何部位置于交通工具外；并正确使用所配备的所有安全设施和设备；客人未经驾驶人员或管理人员或导游及领队许可，在交通工具运行时且乘坐期间，客人的身体各部位不得擅自以任何方式脱离座位；<b>若客人违反本提示警示而造成人身或财产损害时，责任自负。</b><br/>
            <b>八、公司郑重提示告知客人：</b>凡参加涉及海边旅游时，不得在非指定区域游泳或嬉水、须根据自身水性和身体及健康状况掌握游泳距离及下水时间；禁止患高血压、心脏病、中耳炎、急性眼结膜炎、孕妇等客人游泳；禁止剧烈运动后游泳和长时间曝晒游泳及游泳后立即进食；<b>若客人不遵守本提示而导致意外发生，将自行承担后果。</b><br/>
            <b>九、公司郑重提示告知客人：</b>客人在享受或参加邮轮上服务或娱乐活动项目前（如在邮轮上的游泳池内游泳、互动游戏、餐厅用餐、运动、舞蹈、享受 SPA、甲板活动、以及参加邮轮上其他游乐设施娱乐活动等），务必充分了解拟享受的服务或参与的娱乐活动以及游乐工具设施的内容及风险，并根据自身身体状况和健康状况慎重评估后选择，自觉遵守该服务或娱乐活动或游乐工具设施的安全规定或警示或说明。并且在上下邮轮时或邮轮甲板上行走活动中，应当充分注意楼梯或地面情况，自行防止滑倒或摔伤，禁止奔跑或拥挤或有损他人的举止。<b>若客人违反本安全警示告知而所导致的人身伤害或财产损失，公司不承担赔偿责任。</b><br/>
            十、客人在旅游中须严防“病从口入”，特别在选择公司安排的餐饮以外的就餐或选购食品时，请注意饮食卫生，防止消化不良、病毒性肝炎、痢疾、伤寒等肠道传染病；品尝海鲜、风味小吃、土特产等食物时，应视本人肠胃状况酌量适用，并应事先了解不宜同时饮用的忌口。非公司安排就餐引起的疾病，公司不承担任何责任。<br/>
            <b>十一、公司提示客人：</b>客人在有义务保管好自身携带的货币，证件和物品；在离开船舱或酒店或交通工具、以及购物或游览期间，应将货币和贵重物品随身携带；并须自身做好防盗防窃安全措施。若客人携带财产遭受损失时，应立即报警报案，公司予以协助。<br/>
            <b>十二、公司提示客人：</b>客人在邮轮、游览、观光过程中应谨慎选择拍照、摄像地点，在拍照、摄像时应注意往来车辆、周遍人员及情形、所处位置进行拍照、摄像是否有危险或是否有禁拍标志，切忌在可能有危险或设有危险标志的地方停留。旅游者在行程中发现自身权益受到侵害，应及时告知领队、导游以及旅游者的紧急联系人，因没有及时提出而致损失扩大的，游客自负。<br/>
            <b>十三、旅游合同签署的客人以及代表人阅读上述《旅游安全提示警示告知书》后，经公司人员详细解释说明，已完全知晓上述安全提示警示告知全部内容；并愿意遵守和同意将此告知书作为旅游合同附件，与旅游合同具有同等效力。且旅游合同的签署代表 对自身的代理权真实性和合法性承担法律责任，并承担负责转告未签名旅游者上述安全提示警示告知全部内容的责任。</b><br/>
            <br/>
            <br/>
            <span style="float:right">签字：<em class="all-line" style="width:150px">${(travelContractVO.travellers)!''}　</em></span>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
        </p>

        <p>
            <b>附件7：</b><br/>
            <br/>
            <br/>
            <span style="text-align: center;display: block;">
             <b>中国公民出国（境）旅游文明行为指南</b><br/><br/>
                中国公民，出境旅游，注重礼仪，保持尊严。<br/><br/>
                讲究卫生，爱护环境；衣着得体，请勿喧哗。<br/><br/>
                尊老爱幼，助人为乐；女士优先，礼貌谦让。<br/><br/>
                出行办事，遵守时间；排队有序，不越黄线。<br/><br/>
                文明住宿，不损用品；安静用餐，请勿浪费。<br/><br/>
                健康娱乐，有益身心；赌博色情，坚决拒绝。<br/><br/>
                参观游览，遵守规定；习俗禁忌，切勿冒犯。<br/><br/>
                遇有疑难，咨询领馆；文明出行，一路平安。<br/><br/>
         </span>
            <br/>
            <br/>
            <span style="text-align:right;display: block;padding-right: 160px;">
             中央文明办 国家旅游局<br/><br/>
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



    </div>
</form>
<#include "/base/foot.ftl"/>
<p style="text-align:center">
    <button class="ct_form_tjBtn"  id="shipEditButton">保存且发送</button>
</p>
</body>
<script>

    $("#shipEditButton").unbind("click");
    $("#shipEditButton").bind("click", function(){
        //遮罩层
        var loading = top.pandora.loading("正在努力保存中...");
        $.ajax({
            url:"/vst_order/order/orderManage/updateTravelContract.do",
            data:$("#dataForm").serialize(),
            type:"POST",
            dataType:"JSON",
            success:function(result) {
                if(result.code == "success"){
                    loading.close();
                    alert(result.message);
                } else {
                    loading.close();
                    alert(result.message);
                }
            }
        });
    });
</script>
</html>