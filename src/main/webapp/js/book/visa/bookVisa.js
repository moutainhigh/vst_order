$(function(){
	//签证数量改变
	$('select.hotel_sum').change(function(){
		var visaCount=VST.VISA.ORDER.calVisaPersonCount();
		var baoxianCount=VST.VISA.ORDER.calbaoxianPersonCount();
		if(visaCount<baoxianCount){
			$.alert("选择保险的份数不能超过游玩人数.");
			
			//$('#baoxianDiv select.hotel_sum').val(visaCount);
			
			return;
		}
		
		bookNumOnchangeCallback.invoke();
		//VST.VISA.ORDER.refereshInsurance();
		
	});
	
	/*$("#couponCode").change(function(){
        var code=$("#couponCode").val();
        if($.trim(code)==""){
       	 $("#couponChecked").val("false");
        }
	});
	$("#couponVerify").click(function(){
        var code=$("#couponCode").val();
        if($.trim(code)==""){
         $("#couponInfoMsg").html('<i class="tip-icon tip-icon-error"></i>请输入优惠券代码.');
       	 $("#couponChecked").val("false");
       	 return;
        }
        $("#couponChecked").val("true");
        $("#couponInfoMsg").html("");
        BACK.BOOK.CHECK.checkStock();
	});*/
	
});

function showExpressCheck(){
	var showFlag=false;
	 $('select.hotel_sum').each(function(i,v){
		 var num=$(v).val();
		 var goodsType=$(v).attr("goodsType");
		 var integerReg = "^[1-9]*[0-9]*$";
			if(num.match(integerReg)&&goodsType=="EXPRESSTYPE_DISPLAY"){
				showFlag=true;
				return;
			}
	 });
	 return showFlag;
}
var VST={
	 VISA:{
		ORDER:{
			  refereshTimePrice:function(){ //刷新价格
				var visitTime=$('#visitDate').val();	
				var goodsIds="";
				$("#visaDiv select.hotel_sum").each(function(i){
					var integerReg = "^[0-9]*[0-9][0-9]*$";
					if($.trim($(this).val()).match(integerReg)){
						var goodsId=$(this).attr("goodsId");
						if(goodsId!=undefined){
							goodsIds+=goodsId+",";
						}
					}
				});
				if($.trim(visitTime)==""){
					return;
				}
				$.post("/vst_order/ord/book/visa/refereshTimePrice.do",
						{
						visitTime:visitTime,
						suppGoodsIds:goodsIds,
						},
					function(data){
							if(data.success){
								$(data.attributes.visaTimePriceList).each(function(k,v){
									$("select.hotel_sum").each(function(){
										 var goodsId=$(this).attr("goodsId");
										 if(v.suppGoodsId==goodsId){
											 $(this).closest("td").prev('td').html("单价：￥"+v.priceYuan);
										 }
									});							
							    });
							}
						}
					);
			},
			//刷新保险
			refereshInsurance:function(){
				
				VST.VISA.ORDER.bindHotelSum();
			},
			bindHotelSum:function(){
				 $('select.hotel_sum').change(function(){
						var visaCount=VST.VISA.ORDER.calVisaPersonCount();
						var baoxianCount=VST.VISA.ORDER.calbaoxianPersonCount();
						if(visaCount<baoxianCount){
							$.alert("选择保险的份数不能超过游玩人数.");
							return;
						}
						//bookNumOnchangeCallback.invoke();
					});
			 },
			calVisaPersonCount:function(){ //选择的门票计算人数 				
				var personCount=0;
				$("#visaDiv select.hotel_sum").each(function(i){
					var integerReg = "^[1-9][0-9]*$";
					var num=$(this).val();
					if($.trim(num).match(integerReg)){
						personCount += new Number(num);
					}
				});
				return personCount;
			},

			calbaoxianPersonCount:function(){ //选择的保险计算人数 
				var personCount=0;
				$("#baoxianDiv select.hotel_sum").each(function(i){
					var integerReg = "^[1-9][0-9]*$";
					var num=$(this).val();
					if($.trim(num).match(integerReg)){
						personCount += new Number(num);
					}
				});
				return personCount;
			},
			goodsDetailMouseover:function(goodsId,obj){
					$(obj).attr("tip-content",$(obj).next("div").html());
				},
				sortNumber:function(a,b)
				{
					return a - b;
				},
				 callback:function(){//回调接口
					this.funs = [];
					this.pushFun = function(fun) {
						this.funs.push(fun);
					};
					this.invoke = function() {
						for (var i = 0; i < this.funs.length; i++) {
							try {
								this.funs[i]();
							} catch (err) {
								alert(err);
								return false;
							}
						}
					};
				}
		}
	}
}

//预订数量变更后的回调接口
var bookNumOnchangeCallback = new VST.VISA.ORDER.callback();
//日期确定后的回调接口
var calendarConfirmCallback = new VST.VISA.ORDER.callback();