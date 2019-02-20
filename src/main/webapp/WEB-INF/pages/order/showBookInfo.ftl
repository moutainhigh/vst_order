<#import "/base/hotel_func.ftl" as hf>




<form method="POST" action="" name="holdBookInfoForm" id="holdBookInfoForm">
	<input type="hidden" id="itemMap_visitTime" name="itemMap[${suppGoods.suppGoodsId}].visitTime" value="${visitTime}"/>
	<input type="hidden" id="itemMap_hotelAdditation_leaveTime" name="itemMap[${suppGoods.suppGoodsId}].hotelAdditation.leaveTime" value="${leaveTime}"/>
	<input type="hidden" id="itemMap_categoryId" name="itemMap[${suppGoods.suppGoodsId}].categoryId" value="${suppGoods.prodProduct.categoryId}"/>
	<input type="hidden" id="categoryId" name="categoryId" value="${suppGoods.categoryId}"/>
	<input type="hidden" id ="suppGoods" name="itemMap[${suppGoods.suppGoodsId}].goodsId" value="${suppGoods.suppGoodsId}"/>
	<input type="hidden" id="payTarget" name="payTarget" value="${suppGoods.payTarget}"/>
	<input type="hidden" name="userId" value="${userId}"/>
	<input type="hidden" name="productId" value="${suppGoods.prodProduct.productId}"/>  
	<input type="hidden" id="needGuarantee" name="needGuarantee" value=""/>
	<input type="hidden" name="channel_code" value="${channelCode}"/>
	<input type="hidden" name="distributorCode" value="${channelCode}"/>
<div id="dialogBody" class="dialog-body">
<div data-content="content" class="dialog-content clearfix">
            <div class="iframe_content pd0">
                <div class="p_box" style="margin-bottom:0px;"><#--getProductNoticeByCondition(${suppGoods.productId},'${visitTime}','${leaveTime}')-->
                    <div class="co_title"><a href="javascript:void(0);" class="fr" onclick="showProductNotice();">重要通知</a><strong title="${suppGoods.prodProduct.productName}" class="f14">${suppGoods.prodProduct.productName}</strong></div>
                    <#list suppGoods.prodProduct.propValue?keys as value>
					 	<#if value=='star_rate'>
							<#list suppGoods.prodProduct.propValue[value] as propValue>
								<@hf.starlevel propValue.name/>
							</#list>
						</#if>
					</#list>
                    <table id="timePriceTb" class="pg_d_table table_center co">
                        <thead>
                            <tr>
                                <th colspan="7">${suppGoods.prodProductBranch.branchName}</th>
                            </tr>
                        </thead>
                        <tbody>
                        	<tr>
                                <td>
                                    <span class="co_info_line dis">周日</span>
                                </td>
                                <td>
                                    <span class="co_info_line dis">周一</span>
                                </td>
                                <td>
                                    <span class="co_info_line dis">周二</span>
                                </td>
                                <td>
                                    <span class="co_info_line dis">周三</span>
                                </td>
                                <td>
                                    <span class="co_info_line dis">周四</span>
                                </td>
                                <td>
                                    <span class="co_info_line dis">周五</span>
                                </td>
                                <td>
                                    <span class="co_info_line dis">周六</span>
                                </td>
                            </tr> 
                            	<#include "/order/hotel_time_price.ftl"/>
                            <tr>
                                <td colspan="7">
                                    <ul class="co_info cc9">
                                        <#--<li>早餐价格：不可加早</li> -->
                                        <li>房型楼层：
											<#if suppGoods.propValue??>
												${suppGoods.propValue['floor']}
											<#else>
												${suppGoods.prodProductBranch.propValue['floor']}
											</#if>
										</li>
                                        <li>宽带上网：
											<#if suppGoods.propValue??>
												<#list suppGoods.propValue['internet'] as propValue>
													<#if propValue??>
													${propValue.name}
													</#if>
												</#list>
											<#else>
												<#list suppGoods.prodProductBranch.propValue['internet'] as propValue>
													<#if propValue??>
													${propValue.name}
													</#if>
												</#list>
											</#if>
										</li>
                                        <li>无烟房：无烟处理</li>
                                        <li>加床价格：
											<#if suppGoods.propValue??>
												<#list suppGoods.propValue['add_bed_flag'] as propValue>
													<#if propValue??>
													${propValue.name}
													</#if>
												</#list>
											<#else>
												<#list suppGoods.prodProductBranch.propValue['add_bed_flag'] as propValue>
													<#if propValue??>
													${propValue.name}
													</#if>
												</#list>
											</#if>
                                        </li>
                                        <li>房间面积：
											<#if suppGoods.propValue??>
												${suppGoods.propValue['area']}
											<#else>
												${suppGoods.prodProductBranch.propValue['area']}
											</#if>
										</li>
                                        <li>房型：

											<#if suppGoods.propValue??>
												<#list suppGoods.propValue['bed_type'] as propValue>
													<#if propValue??>
                                                        ${propValue.name}
													</#if>
												</#list>
											<#else>
												<#list suppGoods.prodProductBranch.propValue['bed_type'] as propValue>
													<#if propValue??>
                                                        ${propValue.name}
													</#if>
												</#list>
											</#if>
                                        </li>
                                    </ul>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <table class="e_table form-inline mt10" id="orderInfoTable">
                        <tbody>
                            <tr>
                                <td class="w6 e_label">房间数量：</td>
                                <td>
                                    <select class="w8" name="itemMap[${suppGoods.suppGoodsId}].quantity" id="roomQuantity"  onchange="addPerson(this.value);">
                                    	<#list suppGoods.minQuantity..suppGoods.maxQuantity as num>
											<option value="${num}" <#if num==quantity>selected="selected"</#if>>${num}</option>
										</#list>
                                    </select>
                                    <div id="stockMsgDiv" style="display:none;"><i class="e_icon icon-error"></i>库存不足.</div>
                                </td>
                            </tr>
                            <#if suppGoods.prodProductBranch.maxVisitor &gt; 0 >
	                            <tr>
	                                <td class="w12 e_label">每间最大入住人数：</td>
	                                <td>
										<#if suppGoods.propValue??>
											${suppGoods.propValue['max_occupancy']}
										<#else>
											${suppGoods.prodProductBranch.maxVisitor}
										</#if>
	                                    <#--<select class="w8" id="personQuantity" onchange="addPerson(this.value);">
	                                    	<#assign suppGoods_adult=suppGoods.maxQuantity!1/>
											<#assign total_person_count=quantity?number*suppGoods_adult>
											<#list 1..total_person_count as num>
												<option value="${num}" <#if num==quantity>selected="selected"</#if>>${num}</option>
											</#list>
	                                    </select>-->
	                                </td>
	                            </tr>
                            </#if>
                            <tr>
                            <#if foreignFlag?? && foreignFlag=="Y">
                            	<td class="e_label td_top">客人姓名拼音：</td>
                                <td id="Jtenantlist2">
                                	<#list 1..1 as num>
										<input type="text" name="travellers[${num-1}].lastName" travellersId="" class="w8" />
                                        <input type="text" name="travellers[${num-1}].firstName" travellersId="" class="w8" />
										<input type="hidden" name="travellers[${num-1}].receiverId" />
									</#list>
                                    <div><i class="e_icon icon-warn"></i>请填写英文姓名，英文姓名格式为：Lastname/Firstname。</div>
                                </td>
                            <#else>
								<#if personList??>
                                <tr id="commonlyUsed">
                                    <td class="e_label">常用游客：</td>
                                    <td>
										<#list personList as person>
                                            <label class="checkbox mr10"><input type="checkbox" name="receiverId" value="${person.receiverId}" personName="${person.fullName}">${person.fullName}</label>
										</#list>
                                    </td>
                                </tr>
								</#if>
                            	<td class="e_label td_top">客人姓名：</td>
                                <td id="Jtenantlist">
                                	<#list 1..1 as num>
										<input type="text" name="travellers[${num-1}].fullName" travellersId="" class="w8" />
										<input type="hidden" name="travellers[${num-1}].receiverId" />
									</#list>
                                    <div><i class="e_icon icon-warn"></i>请填写中文/英文姓名，英文姓名格式为：Lastname/Firstname。</div>
                                </td>
                            </#if>
                            </tr>
                            <tr>
                                <td class="e_label">到店时间：</td>
                                <td><#assign earliest_arrive_time =suppGoods.prodProduct.propValue['earliest_arrive_time']/>
                                	 <#if earliestArriveTime??>
                                        <#assign earliest_arrive_hour=earliestArriveTime?split(":")[0]?number/>
                                        <#assign earliest_arrive_minute=earliestArriveTime?split(":")[1]?number/>
                                	</#if>
                                    <select id="arrivalTime" class="w9" name="itemMap[${suppGoods.suppGoodsId}].hotelAdditation.arrivalTime">
                                        <#list earliest_arrive_hour..23 as num>
                                        <#if num_index ==0>
                                                <#if earliest_arrive_minute == 30>
                                                        <option value="${num}:${earliest_arrive_minute}">${num}:30</option>
                                                <#else>
                                                        <option value="${num}:00" <#if num==14>selected="selected"</#if>>${num}:00</option>
                                                        <option value="${num}:${earliest_arrive_minute}">${num}:30</option>
                                                </#if>
                                        <#else>
                                                <#if num==14>
                                                        <option value="${num}:00" selected="selected">${num}:00</option>
                                                <#else>
                                                        <option value="${num}:00">${num}:00</option>
                                                </#if>
                                                <option value="${num}:30">${num}:30</option>
                                        </#if>
                                        </#list>
										<option value="23:59">23:59</option>
                                    </select>
                                    <div class="e_error"><i class="e_icon icon-warn"></i>通常酒店${earliest_arrive_time}办理入住，早到可能需要稍作等待</div>
                                </td>
                            </tr>
                            <tr style="display: none;">
                                <td class="e_label">退改规则：</td>
                                <td><div class="claim"></div></td>
                            </tr>
                             <tr>
	                            <td class="e_label">订单备注：</td>
	                            <td>
	                            	<input type="checkbox" name="remarkCk" value="客人要求一定安排大床房，">一定大床&nbsp;
	                            	<input type="checkbox" name="remarkCk" value="客人要求尽量安排大床房，">尽量大床&nbsp;
	                            	<input type="checkbox" name="remarkCk" value="客人要求一定安排双床房，">一定双床&nbsp;
	                            	<input type="checkbox" name="remarkCk" value="客人要求尽量安排双床房，">尽量双床&nbsp;
	                            	<input type="checkbox" name="remarkCk" value="此订单与订单号：*******关联，">订单关联&nbsp;
	                            	<input type="checkbox" name="remarkCk" value="客人原订单已取消，现要求恢复预订，凡请尽快确认。">订取订
	                                <div style="width: 345px;">
	                                    <textarea class="w37" name="remark"></textarea>
	                                    <div class="cc3 tr"><span class="cc1">0</span>/100字（中文(50个)/英文(100个)）</div>
	                                </div>
	                            </td>
	                        </tr>
	                        <tr>
	                        	<td colspan="2" style="color:blue;height:10px;text-align:center;"><a href="javascript:void(0);" onclick="copyRemarkToFaxMemo();">复制粘贴订单备注</a></td>
	                        </tr>
	                        <tr>
	                            <td class="e_label">传真/ebk备注：</td>
	                            <td>
	                            	<div style="width: 345px;">
	                                    <textarea class="w37" name="faxMemo"></textarea>
	                                    <div class="cc3 tr"><span class="cc1">0</span>/100字（中文(50个)/英文(100个)）</div>
	                                </div>
	                            </td>
	                        </tr>
	                         <tr>
	                            <td width="100" class="e_label td_top"><i class="cc1">*</i>联系人：</td>
	                            <td><input type="text" name="contact.fullName" value="${user.userName}"></td>
	                        </tr>
	                        <tr>
	                            <td class="e_label td_top"><i class="cc1">*</i>联系人手机：</td>
	                            <td><input type="text" id="contactMobile" name="contact.mobile" value="${user.mobileNumber}" onchange="validMobile(this);">
	                            <div id="mobileMsg"></div>
	                            </td>
	                        </tr>	    
	                        <tr id="mz">
	                        	<td colspan="3">
	                        		<!---这里是买赠信息-->
									<#include "/order/coupon/buy_present.ftl"/>
	                        	</td>	
	                        </tr> 
                             <tr id="promotionTr" style="display: none;">
                                <td class="e_label">促销：</td>
                                <td>
                                	<table id="promPromotionTb" class="p_table form-inline">
								        <tbody>
													<#--<tr>
													<td width="20"><input type="checkbox" class="w6" name="promotionIdList" value="111"/></td>
													<td>优惠80元</td>
													</tr>-->
								        </tbody>
								    </table>
								</td>
                            </tr>
                             <#if prepaidFalg>
                              <tr id="couponHotel">
	                        	<td colspan="3">
	                        		<!---这里是优惠信息-->	                        		
									<#include "/order/payment/order_pay.ftl"/>									
	                        	</td>	
	                        </tr>
	                        </#if>
                            <!--是否是测试单-->
                            <tr>
		                        <td class="e_label td_top"><i class="cc1">*</i>测试订单：</td>
		                    	<td>
		                		 	<input type='radio' value='Y' name='isTestOrder'>是 </input>
									<input type='radio' value='N' name='isTestOrder' checked='checked'> 否</input> 
								</td>
	                        </tr>	
                            
                            <tr>
	                            <td colspan="2">
	                            <div id="promotionDiv" style=""></div>
	                            <div id="totalOrderPriceDiv" style="display:none;color:red;">
	                            	<b style="font-size:14px;font-weight: bold;">订单总价：</b>房费0元+保险0元-优惠券0元-促销活动0元=0元</div></td>
	                        </tr>
	                        <tr>
	                            <td colspan="2" style="display:none;">
	                            	<table id="guaranteeTb" class="p_table form-inline">
								        <tbody>
								        	<tr>
												<td colspan="2" style="display:none;"><span style="font-size:14px;font-weight: bold;color:#EE3388;">信用卡信息</span>(请您核对入住信息无误后再填写信用卡信息。) </td>
											</tr>
											<tr>
												<td colspan="2" style="font-size:14px;font-weight: bold;color:#EE3388;">填写信用卡信息</td>
											</tr>
											<tr>
												<td style="text-align:right;">卡号：</td>
												<td><input type="text" name="guarantee.cardNo" autocomplete="off"  onchange="checkNeedCvv(this.value);" class="input-text w200"><span class="help-inline"  id="card_error">请输入信用卡15位或16位卡号。</span></td>
											</tr>
											<tr>
												<td style="text-align:right;">CVV2验证码：</td>
												<td><input type="text" name="guarantee.cvv" autocomplete="off" class="input-text w78"><span class="help-inline">信用卡背面签名栏里的后三位。</span></td>
											</tr>
											<tr>
												<td style="text-align:right;">有效期限：</td>
												<td>
													<select class="w8" name="guarantee.expirationYear">
															<option value="2014" selected="selected">2014</option>
															<option value="2015">2015</option>
															<option value="2016">2016</option>
															<option value="2017">2017</option>
															<option value="2018">2018</option>
															<option value="2019">2019</option>
															<option value="2020">2020</option>
															<option value="2021">2021</option>
															<option value="2022">2022</option>
															<option value="2023">2023</option>
															<option value="2024">2024</option>
															<option value="2025">2025</option>
															<option value="2026">2026</option>
															<option value="2027">2027</option>
															<option value="2028">2028</option>
															<option value="2029">2029</option>
				                                    </select>
				                                    <select class="w8"  name="guarantee.expirationMonth">
															<option value="1" selected="selected">1</option>
															<option value="2">2</option>
															<option value="3">3</option>
															<option value="4">4</option>
															<option value="5">5</option>
															<option value="6">6</option>
															<option value="7">7</option>
															<option value="8">8</option>
															<option value="9">9</option>
															<option value="10">10</option>
															<option value="11">11</option>
															<option value="12">12</option>
				                                    </select>
													<span class="help-inline">位于信用卡卡号下方有效期。</span>
												</td>
											</tr>
											<tr>
												<td style="text-align:right;">持卡人姓名：</td>
												<td><input type="text" name="guarantee.holderName" autocomplete="off" class="input-text w150"></td>
											</tr>
											<tr>
												<td style="text-align:right;">持卡人证件号：</td>
												<td><select class="w8" name="guarantee.idType">
															<option value="0" selected="selected">身份证</option>
															<option value="2">护照</option>
				                                    </select>
				                                    <input type="text" name="guarantee.idNo" autocomplete="off" class="input-text w150">
				                                    <span class="help-inline">请输入持卡人证件号码。</span>
				                                </td>
											</tr>
								        </tbody>
								    </table>
	                            </td>
	                        </tr>              
                            <tr>
                                <td class="e_label"></td>
                                <td><div class="fl operate"><a id="holdBookInfo" class="btn btn_cc1">核对订单</a><a id="closeBookInfoPage" class="btn">取消</a></div></td>
                            </tr>
                        </tbody>
                    </table>
	                  <div id="productNotice" style="display: none;">
	                  			<#if productNoticeList??>
                                  	<#list productNoticeList as productNotice>
								 		${productNotice.startTime?string("yyyy-MM-dd")} 至 ${productNotice.endTime?string("yyyy-MM-dd")} 期间： ${productNotice.content}<br/>
									</#list>
                	            <#else>
                                       	无重要通知
                                </#if>
						</div>
                    <div>
                </div>
            </div>
        </div>
		</div>
</form>
<script src="/vst_order/js/hotel_payment_page.js"></script>
<script>var thisProCategoryId = '${suppGoods.categoryId}';</script>	
	<script type="text/javascript">
		//常用游客控制事件绑定
		travellersBindEvent();
		//促销选择事件绑定
		//bindPromCkEvent();

		var foreignFlag = "${foreignFlag}";
		//初始化客户填写数量
		addPerson(jQuery("#roomQuantity  option:selected").text());
		
		function refereshTimePrice(){
			$.post("/vst_order/ord/book/queryRate.do",
				{
					visitTime:$("#itemMap_visitTime").val(),
					leaveTime:$("#itemMap_hotelAdditation_leaveTime").val(),
					suppGoods:${suppGoods.suppGoodsId},
					quantity:${quantity}
				},
				function(data){
					$("#timePriceTb tbody tr:eq(1)").after(data);
                    if(typeof updatePriceTimePre==="function"){
                        updatePriceTimePre('${suppGoods.suppGoodsId!''}');//携程促销修改时间价格
                    }
				}
			);
		}
		
		function showRemark(){
			var display = $("#buyInfo_remark").css("display");
			if(display=='none'){
				$("#buyInfo_remark").show();
			}else{
				$("#buyInfo_remark").hide();
				$("#buyInfo_remark").val("");
			}
		}
		
		refereshTimePrice();
		checkStock();
		$("#roomQuantity").change(function () {
                //添加入住人
				//addPerson($("#roomQuantity").val());
                
		 		//检查库存
                checkStock();
            });
            
            $("#arrivalTime").change(function () {
		 		//检查库存
                checkStock();
            });
            
            $("#holdBookInfo").click(function () {
            	var quantity=$("#roomQuantity").val();
            	var patt1 = new RegExp("^[a-zA-Z][a-z A-Z]{0,19}$"); // 英文姓名验证表达式（如：zhang san）
				var patt2 = new RegExp("^[\u4e00-\u9fa5]{1,10}$"); // 中文姓名验证表达式
				var checkFlag = true; // 验证客人姓名格式用
				
                var person =[];
                if($("#Jtenantlist").length > 0) { // 境内的情况（只能填写英文名或中文名）
                	$("#Jtenantlist").find("input[type=text]").each(function(){
                		var personFullName = $.trim($(this).val());
				   		if(personFullName != ""){
				   			person.push(personFullName);
				   			if(patt1.test(personFullName)||patt2.test(personFullName)){
				   				// 正确
				   			}else{
				   				checkFlag = false;
				   			}
				    	}
			    	});
                }else if($("#Jtenantlist2").length > 0) { // 境外的情况（只能填写英文名）
                	$("#Jtenantlist2").find("input[type=text]").each(function(){
                		var personFullName = $.trim($(this).val());
				   		if(personFullName != ""){
				   			person.push(personFullName);
				   			if(patt1.test(personFullName)){
				   				// 正确
				   			}else{
				   				checkFlag = false;
				   			}
				    	}
			    	});
                }
                	
			    	if(Number(quantity)>person.length){
			    		$.alert("每个房间至少有一个入住人");
			    		return false;
				    }
				    if(!checkFlag){
				    	alert("客人姓名格式不对！");
				    	return false;
				    }
				    
				   	if(!validMobile("#contactMobile")){
						return false;			
					}
					var remark=$("textarea[name=remark]").val();
					var excludeReg=/[<>&\'"';\\:=]/;
					if(getStrLength(remark)>100){
			    		$.alert("订单备注内容过多（中文(50个)/英文(100个)）");
			    		return false;
				    }
				    if(excludeReg.test(remark)){
				    	$.alert("订单备注内容不能包含特殊字符 。如：< > & \\ \" ; \\\\ : = ");
			    		return false;
				    }
				    var faxMemo=$("textarea[name=faxMemo]").val();
				    if(getStrLength(faxMemo)>100){
			    		$.alert("传真/ebk备注内容过多（中文(50个)/英文(100个)）");
			    		return false;
				    }
				    if(excludeReg.test(faxMemo)){
				    	$.alert("传真/ebk备注内容不能包含特殊字符 。如：< > & \\ \" ; \\\\ : = ");
			    		return false;
				    }
					if($("#payTarget").val()=="PAY"&&$("#needGuarantee").val()=="GUARANTEE"){
						var cardNoReg = /^[0-9]*$/;
						var cardNo=$("input[type=text][name='guarantee.cardNo']").val();
						if(cardNo==""||!cardNoReg.test(cardNo)||(cardNo.length!=15&&cardNo.length!=16)){
							$.alert("请输入正确的卡号.");
			    			return false;
						}
						
						if($("input[type=text][name='guarantee.holderName']").val()==""){
							$.alert("请输入持卡人姓名.");
			    			return false;
						}
						if($("input[type=text][name='guarantee.idNo']").val()==""){
							$.alert("请输入持卡人证件号码.");
			    			return false;
						}
						var myDate=new Date();
						var year=myDate.getFullYear();
						var now=""+year;
						var month=myDate.getMonth()+1;
						if(month<10){
							now+="0";
						}
						now+=month;
						var expirationDate=$("select[name='guarantee.expirationYear']").val();
						var expirationMonth=parseInt($("select[name='guarantee.expirationMonth']").val());
						if(expirationMonth<10){
							expirationMonth+="0";
						}
						expirationDate+=expirationMonth;
						if(parseInt(expirationDate)<parseInt(now)){
							$.alert("信用卡有效期，不能小于当前日期");
							return false;
						}
					}
				    //生成订单开始
				    confirmOrderDialog = new xDialog("/vst_order/ord/book/createOrder.do",
								      $("#holdBookInfoForm").serialize(),
								      {title:"核对订单",width:750});
					//关闭订单填写窗口
      				updateDistOrderDialog.close();
            });
            
             $("#closeBookInfoPage").click(function () {
                updateDistOrderDialog.close();
            });
		
		 var showProductNoticeDialog
		 function showProductNotice(){
		 	$.dialog({
                width: 300,
                title: "重要通知",
                content: $("#productNotice").html()
            });
		 }
		 
function validMobile(obj){
		var mobile=$(obj).val();
        //var myreg = /^(13[0-9]|15[0|3|6|7|8|9]|18[8|9])\d{8}$/;
        var myreg = /^\d+$/;
        if(!myreg.test(mobile))
        {
        	$.alert("请输入正确的手机号码！");
           //$("#mobileMsg").html('<i style="color:red;">请输入正确的手机号码！</i>');
            return false;
        }
        $("#mobileMsg").html('');
        return true;
}

	 $("input[type=checkbox][name=remarkCk]").change(function(){
	 	var remark=$("textarea[name=remark]").val();
		 $("input[type=checkbox][name=remarkCk]:checked").each(function(){
			   var ckValue=this.value;
			    var txtValue=$("textarea[name=remark]").val();
			   	if(txtValue.indexOf(ckValue)<0){
			   		remark+=ckValue;
			    } 
		  });
		   $("input[type=checkbox][name=remarkCk]").not("input:checked").each(function(){
			   remark=remark.replace(this.value,'');
		  });
		  $("textarea[name=remark]").val(remark);
		  $("textarea[name=remark]").closest("div").find("div span").html(getStrLength(remark));
	});
	
	$("textarea[name=remark]").change(function(){
		$(this).closest("div").find("div span").html(getStrLength(this.value));
	});
	$("textarea[name=faxMemo]").change(function(){
		$(this).closest("div").find("div span").html(getStrLength(this.value));
	});
	

function getStrLength(str) {   
    var cArr = str.match(/[^\x00-\xff]/ig);   
    return str.length + (cArr == null ? 0 : cArr.length);   
}
function copyRemarkToFaxMemo(){
	 $("textarea[name=faxMemo]").val($("textarea[name=remark]").val());
}

	//1111	

	</script>