
function showExpressCheck(){
	var showFlag=false;
	 $('select.hotel_sum').each(function(i,v){
		 var num=$(v).val();
		 var goodsType=$(v).attr("goodsType");
		 var integerReg = "^[1-9][0-9]*$";
			if(num.match(integerReg)&&goodsType=="EXPRESSTYPE_DISPLAY"){
				showFlag=true;
				return;
			}
	 });
	 return showFlag;
}


function souceFnCallBack(cal){
	pandora.calendar.mos = 12;
	    var suppGoodsId;
	    $("input[name *='itemMap']").each(function(){
			 var goodsId=$(this).attr("goodsId");
			 var mainItem=$(this).attr("mainItem");
			 if(mainItem=="true"){
				 suppGoodsId=goodsId;
				 return;
			 }
		});
	    var url;
	    var endFlag = "N";
	    var productType = getProductType();
	    var visitTime=$(cal.options.trigger).eq(0).val();
	    if(productType == "INNERLINE"){
	    	url = "/vst_order/ord/book/localPlay/getLocalPlayTimePrice.do";
	    	endFlag = 'N';
	   	 	calTimeprice(cal,url,suppGoodsId,visitTime,endFlag);
	    }else if(productType == "FOREIGNLINE"){
	    	url = "/vst_order/ord/book/localPlay/getLocalPlayTimePrice.do";
	    	endFlag = 'N';
	    	calTimeprice(cal,url,suppGoodsId,visitTime,endFlag);
	    }

}



function getProductType(){
	return  $(".hotel_sum").attr("productType");
	
}



function selectDateCallback(cal) {
	var productType = getProductType();
    if(productType == "INNERLINE"){
    	calendarConfirmCallback.invoke();
    }else if(productType == "FOREIGNLINE"){
    	calendarConfirmCallback.invoke();
    }else{
    	showErrorInfo(true,"获取数据失败");
    	
    }
    	
   }










function calTimeprice(cal,url,suppGoodsId,visitTime,endFlag){
	var tdElement=cal.warp.find("td");
    $.post(url,{suppGoodsId:suppGoodsId,visitTime:visitTime,endFlag:endFlag},function(data){
		if(data.success){
			var dateArr = new Array(); // 记录时间价格的日期
			var datePos = 0; // 时间价格日期的数组坐标位
			$(data.attributes.timePriceList).each(function(i,e){
				$(tdElement).each(function(t,v){
					var specDate=e.specDateStr;
					var date=$(v).attr("date-map");
  				if(date==specDate){
  					if(e.stockFlag=="Y"){
  						if(e.stock>0||e.oversellFlag=="Y"){
  							if(e.stock>0 && e.stock<10){
  								$(v).find("div span:eq(1)").html("余"+e.stock);
  							}
  						}else{
  							$(v).find("div span:eq(1)").html("售完");
  						}
  					}
  					$(v).find("div span:eq(2)").html("￥"+e.subZeroPriceYuan);
  					dateArr[datePos] = date;
						datePos++;
  				}
  			});
			});
			
			// 处理没有时间价格的日期
			$(tdElement).each(function(t,v){
				var date=$(v).attr("date-map");
				var isNodate = true;
				for(var i=0;i<datePos;i++){
					if(dateArr[i]==date){
						isNodate = false;
						break;
					}
				}	
				if(isNodate){
					$(v).find("div:eq(0)").removeClass().addClass("nodate");
				}
			});
			
		}else{
		}
	},"JSON");
	
	
	
}


function loadJtip(){
	$('.J_tip').lvtip({
        templete: 3,
        place: 'bottom-left',
        offsetX: 0,
        trigger : "click",
        events: "live" 
    });
}


//提交订单
$('#orderSubmitA').bind('click',function(){
	if(book_user_id==""){
      	//showQueryUserIdDialog();
      	//return;
		submitFormCallback.invoke();
      }else{
      	submitFormCallback.invoke();
      }
});

function submitOrder(){
	if(VST.WIFI.ORDER.orderValidate()){
		//验证
		if(!$("form[name='orderForm']").validate().form()){
			return false;
		}
		var checkFlag=true;
		$('input[type=text]').each(function(){
			 var text=$(this).val();
			 var nameType=$(this).attr("name_type");
			 if(nameType=="fullName"){
			 	  var patt1 = new RegExp("^[a-zA-Z]{2,20}$");
			 	  var patt2 = new RegExp("^[\u4e00-\u9fa5]{1,10}$");
			 	 if(!patt1.test(text)&&!patt2.test(text)){
					//提示
					$(this).next("span").css("display","");
					checkFlag=false;
				}else{
					$(this).next("span").css("display","none");
				}
			 }else if(nameType=="mobile"){
			 	 var myreg = /^\d+$/;
				if(!myreg.test(text)){
					//提示
					$(this).next("span").css("display","");
					checkFlag=false;
				}else{
					$(this).next("span").css("display","none");
				}
			 }else if(nameType=="youbian"){
		        var patt1= /^[1-9][0-9]{5}$/
		        if(text!=""){
			         if(!patt1.test(text)){
			           $(this).next("span").css("display","");
			           alert("邮政编码错误");
						checkFlag=false;
					}else{
			          $(this).next("span").css("display","none");
			        }
		        }else{
		        	 $(this).next("span").css("display","");
		        	 alert("邮政编码错误");
		        	 checkFlag=false;
		        }
			 }else if(nameType=="address"){
			 	var city2Code = $("#js_city2").find('option:selected').attr("value");
		        if($("#user_address").val().length>100||city2Code == "选择市"){
		           $(this).next("span").css("display","");
					checkFlag=false;
				}else
		        {
		          $(this).next("span").css("display","none");
		        }
			 }
		});
		if(!checkFlag){
				return;
		}
		if($('#orderSubmitA').attr('submitFlag')=="true"){
	      //生成订单开始
		  orderInfoDialog = new xDialog("/vst_order/ord/book/comm/createOrder.do",
						      $("form[name='orderForm']").serialize(),
						      {title:"订单信息填写",width:900});
		}
	}
}


function checkVisiTimeFill(){
	var flag = true;

	var timeinput = $(".J_calendar");

	if(timeinput.size()>1){
		if(timeinput.eq(0).val()==''&&timeinput.eq(1).val()=='' ){
			nameFull(timeinput.eq(1),'请选择开始结束日期',192,-25,'warning');
			//$(".timeInputValid p").html('<span class="tip-icon tip-icon-error"></span>请选择开始结束日期。');
			//$(".timeInputValid").show();
			flag = false;
		}else if(timeinput.eq(1).val()==''){
			nameFull(timeinput.eq(1),'请选择结束日期',192,-25,'warning');
			//$(".timeInputValid p").html('<span class="tip-icon tip-icon-error"></span>请选择结束日期。');
			//$(".timeInputValid").show();
			flag = false;
		}else if(timeinput.eq(0).val()==''){
			nameFull(timeinput.eq(1),'请选择开始日期',192,-25,'warning');
			//$(".timeInputValid p").html('<span class="tip-icon tip-icon-error"></span>请选择开始日期。');
			//$(".timeInputValid").show();
			flag = false;
		}else{
			//$(".timeInputValid").hide();
		}
	}else if(timeinput.size()>0){
		if(timeinput.val()==''){
			//$(".timeInputValid p").html('<span class="tip-icon tip-icon-error"></span>请选择游玩日期。');
			flag = false;
			nameFull(timeinput,'请选择游玩日期',192,-25,'warning');
			//$(".timeInputValid").show();
		}
		
	}

	if(!flag){
		$('body,html').scrollTop(100);	
		
	}

	return flag;

}



function checkPickingPiont(){
	
	var flag = true;
	var qu = $("input[type='radio'][pickType='qu']");
	var huan = $("input[type='radio'][pickType='huan']");
	var checkquSize = $("input[type='radio'][pickType='qu']:checked").size();
	var checkhuanSize = $("input[type='radio'][pickType='huan']:checked").size();
	if(qu.size()>0 && huan.size()>0){
		if(checkquSize ==0 && checkhuanSize==0){
			$(".cityVaild p").html('<span class="tip-icon tip-icon-error"></span>请选择取还件网点');
			flag = false;
			$(".cityVaild").show();
		}else if(checkquSize ==0 ){
			$(".cityVaild p").html('<span class="tip-icon tip-icon-error"></span>请选择取件网点');
			flag = false;
			$(".cityVaild").show();
		}else if(checkhuanSize==0){
			$(".cityVaild p").html('<span class="tip-icon tip-icon-error"></span>请选择还件网点');
			flag = false;
			$(".cityVaild").show();
		}else{
			$(".cityVaild").hide();
		}
		
	}else if(qu.size()>0){
		if(checkquSize ==0 ){
			$(".cityVaild p").html('<span class="tip-icon tip-icon-error"></span>请选择取件网点');
			flag = false;
			$(".cityVaild").show();
		}
	}else if(huan.size()>0){
		if(checkhuanSize==0){
			$(".cityVaild p").html('<span class="tip-icon tip-icon-error"></span>请选择还件网点');
			flag = false;
			$(".cityVaild").show();
		}
	}

	if(!flag){
		$('body,html').scrollTop(100);	
		
	}

	return flag;
	
}


$(".wifiOrderDd input[type='radio']").bind("change",function(){
	checkPickingPiont();
});


function bdPickingPoint(){
	$('.wifiOrderDl').each(function(){
        var $this=$(this);
        var len=$this.find('.wifiOrderDd').length;
        if(len<=4)
        {
            $this.find('.wfOrderTake').hide();
        }
        $this.find('.wifiOrderDd:gt(3)').hide();
        $this.find('.wfOrderTake').click(function(){
            var html=$(this).html().split('<');
            var str=html[0].substring(4,6);
            //console.log(html[0].substr(0,4));
            if(html[0].substr(0,4)=='查看全部')
            {
                $this.find('.wifiOrderDd').show();
                $(this).html('收起全部'+str+'地址<i class="arrow arrowUp"></i>');
            }
            else if(html[0].substr(0,4)=='收起全部')
            {
                $this.find('.wifiOrderDd:gt(3)').hide();
                $(this).html('查看全部'+str+'地址<i class="arrow"></i>');
            }
        });
    });
}

$('.wfDetailSelect').bind("click",function(event){
	 if(event.stopPropagation) {
           event.stopPropagation();
       }else {
           event.cancelBubble = true;
       }
   
   if(!$(".wfDetailOption").is(':visible')){
   	$('.wfDetailOption').show();
   	 $(this).find('.arrow').addClass('arrowUp');
	}else{
		$('.wfDetailOption').hide();
       $(this).find('.arrowUp').removeClass('arrowUp');
	}
   
   
  /* if( $('.wfDetailOption').css('display')=='none'){
       $('.wfDetailOption').show();
       $(this).find('.arrow').addClass('arrowUp');
   }
   else
   {
       $('.wfDetailOption').hide();
       $(this).find('.arrowUp').removeClass('arrowUp');
   }*/

});



$('.wfDetailOption').click(function(event){
	if(event.stopPropagation) {
        event.stopPropagation();
    }else {
        event.cancelBubble = true;
    }
});

$('.wfDetailOptionSub span').hover(function(){
    $('.wfDetailOptionSub span').removeClass('cur');
    $(this).addClass('cur');
},function(){
    $(this).removeClass('cur');
});

$('.cal_style').blur(function(){
    $(this).css('color','#333');
});



$(document).click(function(e){
    //e.preventDefault();
    $('.wfDetailOption').hide();
    $('.wfDetailSelect .arrowUp').removeClass('arrowUp');
});

$(".wfDetailOptionSub span").bind("click",function(){
	var cityId = $(this).attr("cityId");
	var goodId = $(this).attr("goodId");
	  var html=$(this).html();
      $('.wfDetailSelect').find('b').html(html).css('color','#333');
      $('.wfDetailOption').hide();
      $('.wfDetailSelect .arrowUp').removeClass('arrowUp');
      $('.wifiOrderDl').each(function(){
          $(this).find('.wifiOrderDd').eq(0).find('input').attr('checked',true);
      });
	$.get("/vst_order/ord/book/wifi/pickingPoint.do", { "cityId": cityId,"suppGoodsId" : goodId},
		function(data){
			$("#pickingPointtoBox").html(data);
			bdPickingPoint();
			checkPickingPiont();
		},'html');
		
}); 


var VST={
	WIFI:{
		ORDER:{
			orderValidate:function(){
			 if(!checkVisiTimeFill()){
				 return false;
			 }

			 $('#ticketDiv select.hotel_sum').each(function(i,v){
					 var num=$(v).val();
					 var mainItem=$(v).attr("mainItem");
					 var integerReg = "^[0-9]*[0-9][0-9]*$";
						if(!num.match(integerReg)&&mainItem=="true"){
							//提示
							return false;
						}
				});
			 return true;
		 },
		 bindHotelSum:function(){
			 $('select.hotel_sum').change(function(){
				 $("#depositGoodNumInput").attr("value",$('select.hotel_sum').val());
	        		 $("#depositVisitime").attr("value",$("#visitTimeInput").val());
					bookNumOnchangeCallback.invoke();
				});
		 },
			 getStrLength:function(str) {   
			    var cArr = str.match(/[^\x00-\xff]/ig);   
			    return str.length + (cArr == null ? 0 : cArr.length);   
			},
			 copyRemarkToFaxMemo:function(){
				 $("textarea[name=faxMemo]").val($("textarea[name=remark]").val());
				 $("textarea[name=faxMemo]").closest("div").find("div span").html(VST.WIFI.ORDER.getStrLength($("textarea[name=faxMemo]").val()));
			},
			travellersBindEvent:function(){ //绑定常用游客事件
				$("table [name=userInfoTb] tr:eq(0)").find("input[type=checkbox]").change(function(){
					VST.WIFI.ORDER.checkIocTxt(this);
				});
				
				$("table [name=userInfoTb] tr:eq(1)").find("input[type=text]").change(function(){
					VST.WIFI.ORDER.txtIocCheck(this);
					VST.WIFI.ORDER.nameTxtChange();
				});
			},
			 checkIocTxt:function(obj){ //常用游客checkbox控制客人姓名text
					$(obj).closest("td").find("input[type=checkbox]:checked").each(function(){
					    var receiverId=$(this).val();
					    var fullName=$(this).attr("personName");
					    var mobile=$(this).attr("mobile");
					    var email=$(this).attr("email");
					    var idNo=$(this).attr("idNo");
					    var idType=$(this).attr("idType");
					    var firstName=$(this).attr("firstName");
					    var lastName=$(this).attr("lastName");
					    var peopleType=$(this).attr("peopleType");
					    var checkedFlag=false;
					    
					    var fullNameIpt=$(obj).closest("tr").next("tr").find("input[type=text]");
					    var travellersId=$(fullNameIpt).attr("travellersId");
				    	var name=$(fullNameIpt).val();
				    	if(fullName==name&&travellersId==receiverId){
		    				checkedFlag=true;
				    	}
					    if(!checkedFlag){
					    	var tb=$(obj).closest("table");
					    	var index=$(tb).attr("index");
					    	var travellersId=$("#fullName" + index).attr("travellersId");
					    	var name=$("#fullName" + index).val();
				    		if(travellersId==''&&name==''){
								checkedFlag=true;
								$("#fullName" + index).val(fullName);
								$("#fullName" + index).attr("travellersId",receiverId);
								$("#firstName" + index).val(firstName);
								$("#lastName" + index).val(lastName);
								$("#peopleType" + index).val(peopleType);
								$("#mobile" + index).val(mobile);
								$("#email" + index).val(email);
								$("#idNo" + index).val(idNo);
								$("#idType" + index).val(idType);
								VST.WIFI.ORDER.nameTxtChange();
							}
					    }
					   	if(!checkedFlag){
					   		$(obj).removeAttr("checked");
					   	}
					  });
					
					$(obj).closest("td").find("input[type=checkbox]").not("input:checked").each(function(){
						    var receiverId=$(this).val();
						    var tb=$(obj).closest("table");
					    	var index=$(tb).attr("index");
					    	var travellersId=$("#fullName" + index).attr("travellersId");
					    	var name=$("#fullName" + index).val();
				    		if(receiverId==travellersId){
								checkedFlag=true;
								$("#fullName" + index).val("");
								$("#fullName" + index).attr("travellersId","");
								$("#firstName" + index).val("");
								$("#lastName" + index).val("");
								$("#peopleType" + index).val("");
								$("#mobile" + index).val("");
								$("#email" + index).val("");
								$("#idNo" + index).val("");
								$("#idType" + index).val("");
								VST.WIFI.ORDER.nameTxtChange();
							}
					
					  });
				},
				txtIocCheck:function(obj){//客人姓名text控制常用游客checkbox
					$(obj).closest("tr").prev("tr").find("td input[type=checkbox]:checked").each(function(){
					    var fullName=$(this).attr("personName");
					    var receiverId=$(this).val();
					    var checkedFlag=false;
					    
					    var travellersId=$(obj).attr("travellersId");
				    	var name=$(obj).val();
				    	if(fullName==name&&travellersId==receiverId){
		    				checkedFlag=true;
				    	}
					    
					   	if(!checkedFlag){
					   		var tb=$(obj).closest("table");
					    	var index=$(tb).attr("index");
							$("#fullName" + index).val("");
							$("#fullName" + index).attr("travellersId","");
							$("#firstName" + index).val("");
							$("#lastName" + index).val("");
							$("#peopleType" + index).val("");
							$("#mobile" + index).val("");
							$("#email" + index).val("");
							$("#idNo" + index).val("");
							$("#idType" + index).val("");
							$(this).removeAttr("checked");
							VST.WIFI.ORDER.nameTxtChange();
					   	}
				  });
					
					 var personId=$(obj).attr("travellersId");
				     var personName=$(obj).val();
				     $(obj).closest("tr").prev("tr").find("td input[type=checkbox]").each(function(){
							var receiverId=$(this).val();
					     	if(personName==$(this).attr("personName")){
					     		 var receiverId=$(this).val();
							    var fullName=$(this).attr("personName");
							    var mobile=$(this).attr("mobile");
							    var email=$(this).attr("email");
							    var idNo=$(this).attr("idNo");
							    var idType=$(this).attr("idType");
							    var firstName=$(this).attr("firstName");
							    var lastName=$(this).attr("lastName");
							    var peopleType=$(this).attr("peopleType");
							    
							    var tb=$(obj).closest("table");
						    	var index=$(tb).attr("index");
							    $("#fullName" + index).val(fullName);
								$("#fullName" + index).attr("travellersId",receiverId);
								$("#firstName" + index).val(firstName);
								$("#lastName" + index).val(lastName);
								$("#peopleType" + index).val(peopleType);
								$("#mobile" + index).val(mobile);
								$("#email" + index).val(email);
								$("#idNo" + index).val(idNo);
								$("#idType" + index).val(idType);
				    			$(this).attr("checked","checked");
				    			return false;
					    	}
						});
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
var bookNumOnchangeCallback = new VST.WIFI.ORDER.callback();
//日期确定后的回调接口
var calendarConfirmCallback = new VST.WIFI.ORDER.callback();