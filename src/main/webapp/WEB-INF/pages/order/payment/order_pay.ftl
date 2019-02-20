<!--新增促销优惠券模块---开始----------------------->
        <div class="order_box">
          <h3 class="order_tit">优惠信息</h3>
		<p class="wxts">温馨提示：若您在下单后对出游时间或出游人数进行变更，将无法享受促销优惠。</p>
		<div class="user_info pt_10 no_bd">
			<dl class="user_dl">
		    	<dt style="display:none">可享促销：</dt>
		    	<div id="promPromotionDiv" class="promPromotionDiv">
		        	<div style="width:20px;height:0px"></div>
		        </div>
		    </dl>
        </div>
            <#if !(user && user.userId)>
            <div class="tiptext tip-info order_login">
            	<span class="tip-icon tip-icon-info"></span> <a id="order_login" href="javascript:;"></a>
            </div>
            </#if>
			<div class="table_t">促销</div>
			<table width="100%" id="promotionTb">
				<tbody>
			    	<tr>
			        	<td width="65%"><a href=""></a></td>
			            <td width="5%"> </td>
			            <td width="5%" class="orange"></td>
			            <td width="5%"></td>
			            <td width="5%" class="orange"></td>
			            <td></td>
			        </tr>
			    </tbody>
			</table>
			<div style="margin:10px"></div>
            <div class="youhui_box">
            	<ul class="youhui_list2">

                    <input type="hidden" name="couponExclusion" value="false">
                    <!--应产品经理李琦要求,后台下单过滤掉优惠卷和奖金模块-->
                	<li class="couponPayLi">
                        <a class="youhui_tit" id="couponLi_a" href="javascript:;" hidefocus="false">使用优惠券<i class="icon_arrow"></i></a>
                        <span id="countNums"></span>
                        <span id="coupon_tips"></span>
                        <div class="youhui_info">
                        	<!--可用优惠券列表-->
                        	<!----用于是否可以单选---->
                        	<input type="hidden" value="${canUseCoupons}" class="couponTypeFlag" />
                        	<!----用于奖金使用数额---->
                        	<input type="hidden" value="0" class="bonusAmountHidden" name="bonusAmountHidden"/>
                        	<!----用于现金使用数额---->
                        	<input type="hidden" value="0" class="cashAmountHidden" name="cashAmountHidden"/>
                        	<input type="hidden" value="<#if user && user.userId>${user.userId}</#if>" class="userIdHidden" name="payOderUserId"/>
                            <input type="hidden" value="<#if productSaleType>${productSaleType}<#else>"PEOPLE"</#if>" class="productSaleType" name="productSaleType"/>
                            <input type="hidden" value="<#if youhuiQuantity>${youhuiQuantity}<#else>1</#if>" class="youhuiQuantity" name="youhuiQuantity"/>
                            <input type="hidden" value="<#if youhuiperson>${youhuiperson}<#else>1</#if>" class="youhuiperson" name="youhuiperson"/>
                            <#if prepaidFalg>
                         	  <input type="hidden" value="<#if prepaidFalg>${prepaidFalg}</#if>" class="prepaidFalg" name="prepaidFalg_pay"/>
                            </#if>
                            	<table class="youhui_table youhui_table_new">

                                </table>
                                 <div class="youhui_table_box">
                                <table class="youhui_table youhui_table_new">
                                <tbody class="fixedCouponTbody">
                                	<tr class="no_youhuiquanTr">
                                    	<td colspan="5">
                                        	<!--没有优惠券的时候提示-->
                                            <div class="no_youhuiquan">
                                                <i class="order_icon order_lvhead"></i><span id="no_use">很遗憾，您暂无可用优惠券</span>
                                            </div>
                                        </td>
                                    </tr>


                                </tbody>
                                <tbody class="border_t1_dotted freeCouponTbody">

                                </tbody>
                            </table>
                            </div>
                            <!--添加其他优惠券-->
                            <div class="youhui_add">
                            	<b>添加其他优惠券</b><input class="input" type="text" yz_input="true" placeholder="输入优惠券兑换号码"><span class="btn btn-small btn-orange js_yhq_yz" id="addCoupon">添加</span>

                            </div>
                             <!--后台产品不可以使用优惠券的提示-->
                             <div  id="houtai"  style="display:none" >
                                 <span>很遗憾，本产品不可使用优惠券</span>
                             </div>

                            <!--展开后的箭头-->
                            <div class="info_arrow"><span>◆</span><i>◆</i></div>
                        </div>
                    </li>

                    <li class="BounsPayLi">
                        <a class="youhui_tit" href="javascript:;" hidefocus="false">使用奖金抵扣<i class="icon_arrow"></i></a>
                        <div class="youhui_info">
                        	<!--使用奖金输入框-->
                            <div class="dikou_box">
                            	<b>本次使用</b><span class="yong_input"><input class="input js_dikou_input" type="text" id="input_bonus">元</span>
                                <span class="yong_text"></span>
                                <span class="btn btn-small btn-orange js_dikou_queren js_dikou_queren_bonus" id="sure_dikou_bonus">确认</span>
                                <span class="btn btn-small btn-default js_dikou_quxiao js_dikou_queren_bonus">取消</span>
                            </div>

                            <!--奖金账户，可使用奖金-->
                            <div class="dikou_b">您的奖金账户余额：<span class="dikou_price"><samp><lable id="CanPayBouns">1000</lable><input type="hidden" id="canPayBounsHidden" value="100000" name="bounsAmt"></samp><dfn> 元</dfn></span>(本次最多可使用:<lable class="maxCanPayBouns">99.00</lable><input type="hidden" value="1" id="maxpayBounsAmt" class="maxpayBounsAmt" name="maxpayBounsAmt"/>元)</div>
                            <!--不变的奖金余额-->
                            <input type="hidden" id="canPayBounsHidden_1" value="0">
                            <div class="info_arrow"><span>◆</span><i>◆</i></div>
                        </div>
                    </li>
                    <li class="CashsPayLi">
                        <a class="youhui_tit" href="javascript:;" hidefocus="false">使用账户存款抵扣<i class="icon_arrow"></i></a>
                        <div class="youhui_info">
                            <div class="dikou_box">
                            	<!--使用账户存款输入框-->
                            	<b>本次使用</b>
                                <span class="yong_input"><input class="input js_dikou_input" name="accountMonery" id="accountMonery" type="text">元</span>
                                <span class="yong_text"></span>
                                <span class="btn btn-small btn-orange js_dikou_queren js_dikou_queren_cash">确认</span>
                                <span class="btn btn-small btn-default js_dikou_quxiao js_dikou_queren_cash">取消</span>
                            </div>

                            <!--存款账户，可使用奖金-->
                            <div class="dikou_b">
                            	您的账户存款余额：<span class="dikou_price" id="zhckPrice"><samp><lable id="maxPayMoney">0</lable><input type="hidden" id="maxPayMoneyHidden" value="0" name="maxPayMoney"></samp> 元</span>
                            <input type="hidden" id="maxPayMoneyHidden_1" value="0">
                            </div>
                            <div class="info_arrow"><span>◆</span><i>◆</i></div>
                        </div>
                    </li>
                    <li class="showCZK" style="display:none">
                        <a class="youhui_tit" href="javascript:;" hidefocus="false">使用储值卡<i class="icon_arrow"></i></a>
                        <span class="c_999">　（可添加多个储值卡）</span>
                        <div class="youhui_info">
                        	<!--使用储值卡输入框-->
                            <ul class="dikou_box lipinka_box">
                            	<li>
                                	<b>储值卡号</b><input id="storeCardInputId" class="input input_card" yz_input="true" type="text">
                                </li>
                                <li>
                                	<span class="btn btn-small btn-orange js_card_yz" id="storeCardBtn">使用</span>
                                    <span class="btn btn-small btn-default js_lpk_cancel" id="storeCardCalBtn">取消</span>
                                </li>
                            </ul>

                        	<table class="youhui_table czk_table"></table>



                            <!--展开后，向上的箭头-->
                            <div class="info_arrow"><span>◆</span><i>◆</i></div>
                        </div>
                    </li>
                    <li class="showLPK" style="display:none">
                        <a class="youhui_tit" href="javascript:;" hidefocus="false">使用礼品卡<i class="icon_arrow"></i></a>
                        <span class="c_999">　（可添加多个礼品卡）</span>
                        <div class="youhui_info">
                        	<!--添加礼品卡输入框模块-->
                            <ul class="dikou_box lipinka_box">
                            	<li>
                                	<b>礼品卡号</b><input id="lpCardInputId" class="input input_card" yz_input="true" type="text">
                                </li>
                            	<li>
                                    <b>密码</b><input id="lpCardInputpwd" class="input input_password" yz_input="true" type="password">
                                </li>
                                <li>
                                	<#-- 填写的验证码是否正确 -->
                                	<input type="hidden" id="lpk_checkCodeValid" value="0"/>
                                    <b>验证码</b><input class="input input_yzm" placeholder="点击刷新" id="lpk_pic_checkCode" yz_input="true" type="text">
                                    <img class="yzm_img" style="display:none"  id="lpk_createCheckCode" src="" width="36" height="24">
                                    <span class="yzm_next" style="display:none"  id="createCheckCodeLPKHref">换一个</span>

                                </li>
                                <li>
                                	<span id="lpstoreCardBtn" class="btn btn-small btn-orange js_lpk_true">使用</span>
                                    <span class="btn btn-small btn-default js_lpk_cancel">取消</span>
                                </li>
                            </ul>

                        	<table class="youhui_table lpk_table"></table>

                            <!--展开后，向上的箭头-->
                            <div class="info_arrow"><span>◆</span><i>◆</i></div>
                        </div>
                    </li>
                </ul>
            </div>
            
            <div class="price_info" id="priceInfoDiv" style="display:none">
            	<p class="fk_Amount"><b>产品总价：</b><span>&yen;--</span><input type="hidden" class="amountProuctHidden" name="amountProuctHidden" value="0"/></p>
            	<p class="fk_ExpressAmount"><b>快递：</b><span>&yen; 0</span></p>
            	<p class="fk_Insurance"><b>保险：</b><span>&yen; 0</span></p>
                <p class="fk_promotionAmount"><b>促销：</b><span>- &yen; 0</span></p>
                <p class="fk_couponAmount"><b>优惠券：</b><span>- &yen; 0</span></p>
                <p class="fk_bonusAmount"><b>奖金代扣：</b><span id="daikou_bonus">- &yen; 0</span></p>
                <p class="fk_cashAmount"><b>账户存款抵扣：</b><span id="daikou_cash">- &yen; 0</span></p>
                <p class="fk_paidAmount"><b>储值卡：</b><span>- &yen; 0</span></p>
                <p class="fk_giftCardAmount"><b>礼品卡：</b><span>- &yen; 0</span></p>
            </div>
            
            <div class="price_box fk_box_free" id="orderPriceDiv" style="display:none">
            	<p class="fk_p1"><b>应付金额：</b>&yen;<lable class="oughtPay">--</lable><input type="hideen" class="oughtPayHidden" val="0"/></p>
            	<!--<p><b>应付总价：</b><span><dfn>￥</dfn>1000.00</span></p>-->
            </div>
            
        </div>
        <!--新增促销优惠券模块---结束----------------------->