<!DOCTYPE html>
<html>
<body>
<style type="text/css">
	.text-center {
		text-align: center;
	}
</style>
		<#if changableFlag=='Y'>
            <div class="text-center" style="height: 300px; margin-top: 15%" id="changeDateDiv">
                <input type="hidden" name="orderId" value="${RequestParameters.orderId}">
                <input type="hidden" name="orderItemId" value="${RequestParameters.orderItemId}">
                <p align="center">
                    改期：
                    <input type = "hidden" id="visitDate" name="visitDate" value="${visitDate}"/>
                    <input type = "hidden" id="isShiyuanhuiTicket" name="isShiyuanhuiTicket" value="${isShiyuanhuiTicket}"/>
                    <input type = "hidden" id="changeDays" name="changeDays" value="${changeDays}"/>
                    <input type = "hidden" id="isFangteTicket" name="isFangteTicket" value="${isFangteTicket}"/>
                    <input type = "hidden" id="changeTimes" name="changeTimes" value="${changeTimes}"/>
                    <input type="text" name="changeVisitDate" class="Wdate" id="changeVisitDate"
                           onFocus="<#if changeDays?? && visitDate??>var date=getMinDate();WdatePicker({readOnly:true,minDate:date,maxDate:'#F{$dp.$DV(\'${visitDate}\',{d:${changeDays}})}'})
                           <#else>WdatePicker({readOnly:true,minDate:'%y-%M-{%d}'})</#if>" required/>
                    <button class="pbtn pbtn-small btn-ok" id="submitChangeDate">提交</button>
                </p>
                <#if isFangteTicket>
                    提示：改期仅针对方特，此订单仅支持整单修改且仅修改一次，改期后不可退票<br>
                    常规票可改期范围为申请改期当天后60天内, 活动票可改期范围为申请改期当天后60天内且活动时限内
                <#elseif isShiyuanhuiTicket>
                    提示：改期仅针对世园会订单，且此订单只能修改1次。
                <#else>
                    提示：改期仅针对迪士尼与万达的订单，且此子订单只能修改${changeTimes}次。
                </#if>
            </div>
		<#else>
            <div class="text-center" style="height: 100px; margin-top: 15%" id="changeDateDiv">
                <#if isFangteTicket>
                    <p style="font-size: 16px;text-align: center;">不能再改期！</p>
                    <p style="text-align: center;">${unchangableMsg}</p>
                <#elseif isShiyuanhuiTicket>
                    <p style="font-size: 16px;text-align: center;">该订单已改期成功，不能再次操作改期。</p>
                <#else>
                    <p style="font-size: 16px;">不能再改期${changableFlag}</p>
                </#if>
            </div>
		</#if>

</body>

</html>
<script>
    function getMinDate(){
        var visitDate = '"' + $("#visitDate").val() + '"';
        var changeDays = parseInt(-$("#changeDays").val());
        var changeMinDate = addDate(visitDate, changeDays);
        var today = new Date();
        if(changeMinDate < today){
            var month1=today.getMonth()+1;
            return today.getFullYear()+'-'+month1+'-'+today.getDate();
        } else {
            var month2=changeMinDate.getMonth()+1;
            return changeMinDate.getFullYear()+'-'+month2+'-'+changeMinDate.getDate();
        }

    }

    function addDate(date,days){
        var d=new Date(date);
        d.setDate(d.getDate()+days);
        return d;
    }

    $("#submitChangeDate").bind("click", function () {
        var changeVisitDate = $('#changeVisitDate').val();
        //针对世园会特殊票
        var isShiyuanhuiTicket = $('#isShiyuanhuiTicket').val();
        if(isShiyuanhuiTicket){
            //世园会特殊日期
            var shiYuanHuiSpecialDays = ['2019-04-29', '2019-05-01', '2019-05-28',  '2019-05-29' , '2019-05-30', '2019-09-10', '2019-09-11','2019-09-12', '2019-10-01', '2019-10-02', '2019-10-03', '2019-10-04', '2019-10-05', '2019-10-06', '2019-10-07'];
            console.log("世园会指定日票有以下日期:");
            shiYuanHuiSpecialDays.forEach(function (element, index, array) {
                console.log(element);
            });
            var visitDate = $('#visitDate').val();
            console.log("游玩日visitDate="+ visitDate);
            console.log("改期游玩日changeVisitDate="+ changeVisitDate);
            if(shiYuanHuiSpecialDays.indexOf(visitDate) <0 && shiYuanHuiSpecialDays.indexOf(changeVisitDate) >=0){
                alert("平日票不可改期指定日票,"+ changeVisitDate+ "为指定日票")
                return;
            }
        }
        var isFangteTicket = $('#isFangteTicket').val();
        var changeTimes = $('#changeTimes').val();
		if (changeVisitDate == "") {
			return;
		}
		var confirmDialog = pandora.dialog({
			content: "<div class='text-center'><p>只支持"+changeTimes+"次改期，<br>请确认是否将出游日改至"+changeVisitDate+"？</p> <a class='btn btn_cc1 ok'>确定</a> <a class='btn btn_cc1 cancel'>返回修改</a></div>",
			zIndex: 3000,
			mask: true
		});
		console.log(confirmDialog.wrap.find('.btn'));
		var clicked = false;
		confirmDialog.wrap.find('a.ok').click(
			function() {
				if (clicked) {
					alert('请勿重复点击');
                } else {
                    clicked = true;
                    $.ajax({
                        url: "/vst_order/order/orderManage/changeVisitDate.do",
                        data: $('#changeDateDiv').find('input').serialize(),
                        type: "POST",
                        dataType: "JSON",
                        success: function (data) {
                            var msg = data.message;
                            clicked = false;
                            confirmDialog.close();
                            if (data.code == "success") {
                                msg = "<div class='text-center'>" + msg + "</div>";
                                if(isFangteTicket){
                                    msg = "<div class='text-center'>改期成功！</div>" + "<br>改期票不可再次修改，不可退订。";
                                }
                                changeDateDialog.close();
                                pandora.dialog({height: 200, width: 400, content: msg, mask: true});
                            } else {
                                msg = "<div class='text-center'><p>改期失败<br>原因：" + msg + "</p><a class='btn btn_cc1 cancel'>返回修改</a></div>";
                                var cancelDialog = pandora.dialog({height: 200, width: 400, content: msg, mask: true});
                                cancelDialog.wrap.find('a.cancel').click(function () {
                                    cancelDialog.close();
                                })
                            }
                        }
                    });
                }
			}
		);
        confirmDialog.wrap.find('a.cancel').click(
                function() {confirmDialog.close();}
        );
    });
</script>
