<h5 class="hotel_tab_title">优惠</h5>
<div class="hotel_tab">
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
<#if !productCouponLimit?? || productCouponLimit != 'N' >
<div class="table_t">优惠券</div>
<table width="100%">
	<tbody>
    	<tr>
        	<td width="5%">
        		<input class="radio" name="youhui"  id="youhui" value="coupon"  type="radio" <#if productCouponLimit == 'N'>disabled="true"</#if>>
        	</td>
        	
            <td width="60%" class="orange" >
            
            	一笔订单仅能使用一张优惠券，且无法与奖金同时使用。使用优惠券:
            	<input type="text" id="couponCode" name="couponList[0].code" class="w12" />
             
             <a id="couponVerify" class="btn btn-orange" >验证</a>
             
             <span id="couponInfoMsg" style="color:red;"></span>
            </td>
            <td width="5%">
    		</td>
            <td width="5%" class="orange"></td>
            <td></td>
        </tr>
    </tbody>
</table>
</#if>
<div class="table_t">奖金抵扣</div>
<table width="100%">
	<tbody>
    	<tr>
        	<td width="5%"><input class="radio" id="youhui" name="youhui" value="bonus" type="radio"></td>
            <td width="80%" class="orange">
            	<input type="hidden" id="target" name="target" value=""/>
            	&nbsp;&nbsp;奖金返现:<input class="w12" name="bonusYuan"  id="bonus_number" type="text">  <a id="couponVerify" onclick="bonusChange();" class="btn btn-orange" >验证</a>
            	（最多可输入<span id="max_bonus_lable">0</span>元奖金）
            	<span id="bonusInfoMsg" style="color:red;"></span>
            </td>
            <td width="1%"></td>
            <td width="1%">
            </td>
            <td></td>
        </tr>
    </tbody>
</table>