$(function(){
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
       	 //$("#couponChecked").val("false");
       	 return;
        }
        //$("#couponChecked").val("true");
        $("#couponInfoMsg").html("");
        if(VST.TICKET.ORDER.orderValidate()){
        	BACK.BOOK.CHECK.checkStock();
        }else{
        	$("#couponInfoMsg").html('<i class="tip-icon tip-icon-error"></i>预订的商品数量必须大于0.');
        }
        
	});*/
	
});

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

var VST={
	TICKET:{
		ORDER:{
			orderValidate:function(){
			 if($.trim($('#visitDate').val())==""){
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
		 },bindHotelSum:function(){
			 $('select.hotel_sum').change(function(){
					var ticketCount=VST.TICKET.ORDER.calTicketPersonCount();
					var baoxianCount=VST.TICKET.ORDER.calbaoxianPersonCount();
					if(ticketCount<baoxianCount){
						$.alert("选择保险的份数不能超过游玩人数.");
						return;
					}
					bookNumOnchangeCallback.invoke();
				});
		 },
		 nameTxtChange:function(){
				VST.TICKET.ORDER.baoxianRelatePerson();
			},
			baoxianRelatePerson:function(){
				$("td[name=baoxianTd]").each(function(i,v){
					var goodsId=$(this).attr("goodsId");
					var personStr="";
					$("table [name=userInfoTb] tr:eq(1) td input").each(function(i,v){
						var tb=$(this).closest("table");
				    	var index=$(tb).attr("index");
						personStr+="<label class='checkbox mr10'><input type='checkbox' name='personRelationMap[GOODS_"+goodsId+"].seq' value='"+index+"'>"+$(v).val()+"</label>";
					});
					$("td[name=baoxianTd]").html(personStr);
				});
			},
			 refereshTimePrice:function(){ //刷新价格
				var visitTime=$('#visitDate').val();	
				var productIds="";
				var goodsIds="";
				$("#ticketDiv select.hotel_sum").each(function(i){
					var integerReg = "^[0-9]*$";
					if($.trim($(this).val()).match(integerReg)){
						var goodsId=$(this).attr("goodsId");
						var productId=$(this).attr("productId");
						if(goodsId!=undefined){
							goodsIds+=goodsId+",";
						}
						if(productId!=undefined){
							productIds+=productId+",";
						}
					}
				});
				if($.trim(visitTime)==""){
					return;
				}
				$.post("/vst_order/ord/book/ticket/refereshTimePrice.do",
						{
						visitTime:visitTime,
						productIds:productIds,
						suppGoodsIds:goodsIds
						},
					function(data){
							if(data.success){
								$(data.attributes.ticketVoList).each(function(k,v){
									$("select.hotel_sum").each(function(){
										 var goodsId=$(this).attr("goodsId");
										 if(v.suppGoodsId==goodsId){
											 $(this).closest("td").prev('td').html("单价：￥"+v.priceYuan);
											 $(this).closest("tr").find('td:eq(1)').html("<p>游玩时间："+visitTime+"</p><p>"+v.aheadBookDate+"</p>");
										 }
									});
							    });
								var ticketProductVO =data.attributes.ticketProductVO;
								if(ticketProductVO!=null){
									$("select.hotel_sum").each(function(){
										 var productId=$(this).attr("productId");
										 if(ticketProductVO.product.productId==productId){
											 $(this).closest("td").prev('td').html("单价：￥"+ticketProductVO.totalPriceToYuan);
										 }
									});
								}
								/*$("#ticketDiv select.hotel_sum").each(function(){
									$(this).closest("tr").find('td:eq(1) p').html("游玩时间："+visitTime);
								});*/
							}
						}
					);
			},
			calTicketPersonCount:function(){ //选择的门票计算人数 
				/*var adultArr= new Array();
				var childArr= new Array();
				$("#ticketDiv select.hotel_sum").each(function(i){
						var integerReg = "^[1-9][0-9]*$";
						var num=$(this).val();
						var adult=$(this).attr("adult");
						var child=$(this).attr("child");
						if($.trim(num).match(integerReg)){
							if(adult!=undefined&&adult>0){
								adultArr.push(adult*num);
							}
							if(child!=undefined&&child>0){
								childArr.push(child*num);
							}
						}
				});
				
				adultArr.sort(VST.TICKET.ORDER.sortNumber);
				childArr.sort(VST.TICKET.ORDER.sortNumber);
				var sum=new Number(adultArr.slice(adultArr.length-1))+new Number(childArr.slice(childArr.length-1));
				*/
				var adultCount=0;
				var childCount=0;
				$("#ticketDiv select.hotel_sum").each(function(i){
					var integerReg = "^[1-9][0-9]*$";
					var num=$(this).val();
					var adult=$(this).attr("adult");
					var child=$(this).attr("child");
					if(num.match(integerReg)){
						if(adult!=undefined&&adult>0){
							adultCount=parseInt(adultCount)+parseInt(adult)*parseInt(num);
						}
						if(child!=undefined&&child>0){
							childCount=parseInt(childCount)+parseInt(child)*parseInt(num);
						}
						
					}
				});
				var sum=parseInt(adultCount)+parseInt(childCount);
				return sum;
			},
			calbaoxianPersonCount:function(){ //选择的保险计算人数 
				var personCount=0;
				$("#baoxianDiv select.hotel_sum").each(function(i){
					var integerReg = "^[1-9][0-9]*$";
					var num=$(this).val();
					if($.trim(num).match(integerReg)){
						personCount+=new Number(num);
					}
				});
				return personCount;
			},
			 getStrLength:function(str) {   
			    var cArr = str.match(/[^\x00-\xff]/ig);   
			    return str.length + (cArr == null ? 0 : cArr.length);   
			},
			 copyRemarkToFaxMemo:function(){
				 $("textarea[name=faxMemo]").val($("textarea[name=remark]").val());
				 $("textarea[name=faxMemo]").closest("div").find("div span").html(VST.TICKET.ORDER.getStrLength($("textarea[name=faxMemo]").val()));
			},
			travellersBindEvent:function(){ //绑定常用游客事件
				$("table [name=userInfoTb] tr:eq(0)").find("input[type=checkbox]").change(function(){
					VST.TICKET.ORDER.checkIocTxt(this);
				});
				
				$("table [name=userInfoTb] tr:eq(1)").find("input[type=text]").change(function(){
					VST.TICKET.ORDER.txtIocCheck(this);
					VST.TICKET.ORDER.nameTxtChange();
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
				    	/*$(obj).closest("tr").next("tr").find("input[type=text]").each(function(k,v){
						    	var travellersId=$(this).attr("travellersId");
						    	var name=$(this).val();
					    		if(fullName==name&&travellersId==receiverId){
					    				checkedFlag=true;
						    			return false;
						    	}
					    });*/
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
								VST.TICKET.ORDER.nameTxtChange();
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
								VST.TICKET.ORDER.nameTxtChange();
							}
				    		
						    //$(obj).closest("table").each(function(k,v){
						    //	var travellersId=$(this).attr("travellersId");
						    //	if(receiverId==travellersId){
						    //		$("#fullName" + k).val("");
							///		$("#fullName" + k).attr("travellersId","");
							//		$("#firstName" + k).val("");
							//		$("#lastName" + k).val("");
							//		$("#peopleType" + k).val("");
							//		$("#mobile" + k).val("");
							//		$("#email" + k).val("");
							//		$("#idNo" + k).val("");
							///		$("#idType" + k).val("");
							//		$("#cancelA" + k).attr("travellersId","");
						    //	}
					    	//});
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
							VST.TICKET.ORDER.nameTxtChange();
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
				},//刷新保险
				refereshInsurance:function(){
					var payTarget=$("#payTarget").val();
					var aperiodicFlag=$("#aperiodicFlag").val();
					if(payTarget!="PREPAID"||aperiodicFlag=="Y"){
						return;
					}
					var productId=$("#productIdTxt").val();
					var visitTime=$('#visitDate').val();
					$.post("/vst_order/ord/book/ticket/refereshInsurance.do",
						{
							productId:productId,
							visitTime:visitTime
						},
						function(data){
							$("#baoxianDiv").html(data);
							$('select.hotel_sum').unbind();
							VST.TICKET.ORDER.bindHotelSum();
							
						}
					);
				},refereshOtherTicket:function(){
					var payTarget=$("#payTarget").val();
					var aperiodicFlag=$("#aperiodicFlag").val();
					if(payTarget!="PREPAID"||aperiodicFlag=="Y"){
						return;
					}
					var suppGoodsId="";
					 $("#ticketDiv select.hotel_sum").each(function(i){
						 var goodsId=$(this).attr("goodsId");
						 var mainItem=$(this).attr("mainItem");
						 if(mainItem=="true"){
							 suppGoodsId=goodsId;
							 return;
						 }
					});
					var visitTime=$('#visitDate').val();
					if(suppGoodsId==""){
						return;
					}
					$.post("/vst_order/ord/book/ticket/refereshOtherTicket.do",
						{
						suppGoodsId:suppGoodsId,
							visitTime:visitTime
						},
						function(data){
							$("#otherTicketDiv").html(data);
							$('select.hotel_sum').unbind();
							VST.TICKET.ORDER.bindHotelSum();
						}
					);
				},
				sortNumber:function(a,b)
				{
					return a - b
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
var bookNumOnchangeCallback = new VST.TICKET.ORDER.callback();
//日期确定后的回调接口
var calendarConfirmCallback = new VST.TICKET.ORDER.callback();