<!DOCTYPE html>
<head>
<title>添加发布信息</title>
<html>
<#include "/base/head_meta.ftl"/>
     <script type="text/javascript" src="/vst_order/js/remoteUrlLoad.js"></script>
    
</head>
	<body>
	<div class="iframe_search" id="contentDiv">
		<form method="post" action="/vst_order/order/orderInvoice/ord/saveCompositeInvoice.do" id="saveForm" >
		<input name="totalYuan" type="hidden" id="totalYuan" value="${amountYuan}" />
		<input type="hidden" id="invoiceNumber" value="${invoiceNumber!''}">
		   <b class="cc7">本次可开票总金额:${amountYuan!''}</b>
		    <#list invoiceNumber as pos>
		     <input name="pos" type="hidden" id="pos" value="${pos}"/>
		    <p style="font-size:16px;"><b class="cc7">发票:${pos+1}</b></p> 
			  <#list ordInvoiceList as OrdInvoice>
		        <input name="orderId" type="hidden" id="orderId" value="${OrdInvoice.orderId!''}"/>
			 <table class="s_table" id="table${pos}">			
				<tbody>
				    <tr>
					    <td width="6%" class="s_label">
							购买方式：
						</td>
						<td width="12%">
							个人     <input name="form[${pos}].purchaseWay" id="purchaseWay${pos}" type="radio" value="personal" date="${pos}" height="12px"/>
							公司     <input name="form[${pos}].purchaseWay" id="purchaseWay${pos}" type="radio" value="company" checked="checked" height="12px" date="${pos}"/>
						</td>
						<td width="6%" class="s_label">
							购买方名称：
						</td>
						<td width="12%">
							<input name="form[${pos}].title" type="text" id="title${pos}" height="12px"/>
						</td>
						<td width="6%" class="s_label">
							纳税人识别号：				
						</td>
						<td width="12%">
						    <input name="form[${pos}].taxNumber" id="taxNumber${pos}" type="text"/>
						</td>
					</tr>
					<tr>
					    <td width="6%" class="s_label">
							购买方地址：
						</td>
						<td width="12%">
                           <input name="form[${pos}].buyerAddress" id="buyerAddress${pos}" type="text"/>
						</td>
						<td width="6%" class="s_label">
							购买方电话：				
						</td>
						<td width="12%">
						    <input name="form[${pos}].buyerTelephone" id="buyerTelephone${pos}" type="text" value=""/>
						</td>
						<td width="6%" class="s_label">
							发票金额：
						</td>
						<td width="12%">
						    <#if !pos_has_next>
                                                                                                                    以订单实际金额为准
                            <#else>
                                <input name="form[${pos}].amountYuan" type="text" id="amount${pos}" />
                            </#if>
						</td>
					</tr>
					<tr>
					    <td width="6%" class="s_label">
							开户银行：
						</td>
						<td width="12%">
                           <input name="form[${pos}].bankAccount" id="bankAccount${pos}" type="text"/>
						</td>
						<td width="6%" class="s_label">
							开户银行账号：
						</td>
						<td width="12%">
                           <input name="form[${pos}].accountBankAccount" id="accountBankAccount${pos}" type="text"/>
						</td>
						<td width="6%" class="s_label">
							发票内容：				
						</td>
						<td width="12%">
						    <#list selectSet as set>
						         <input name="form[${pos}].content" id="content${pos}" type="hidden" value="${set!''} "/>${set!''}
						    </#list>
						</td>
						  
					</tr>
					<tr>
					    <td width="6%" class="s_label">
						             送货方式:								
						</td>
						<td width="13%">
						    <select name="form[${pos}].deliveryType" id="deliveryType${pos}" size="1">
								<#list deliveryTypeList as deliveryType>
								     <option value="${deliveryType.code!''}">${deliveryType.cnName!''}</option>
								 </#list>
							</select>
						</td>
						<td width="6%" class="s_label">
							发票备注：
						</td>
						<td width="13%">
						    <textarea name="form[${pos}].memo" id="memo${pos}"></textarea>
						</td>
						<td width="9%" class="s_label">开票单位:</td>
						<td width="12%">
							<select name="form[${pos}].companyType" id="companyType${pos}" size="1">
								<#if OrdInvoice.companyType?exists >
									<#assign defaultCompanyType = OrdInvoice.companyType >
								<#else>
									<#assign defaultCompanyType = 'XINGLV'>
								</#if>
								<#list companyTypeMap?keys as key >
									<#if defaultCompanyType == key>
										<option value="${key}" selected="selected" >${companyTypeMap[key]}</option>
									<#else>
										<option value="${key}" >${companyTypeMap[key]}</option>
									</#if>
								</#list>
							</select>
						</td>
					</tr>
				</tbody>
			</table>
			<div class="s2-info-area">
	    	    <div href="/vst_order/order/orderInvoice/ord/loadAddresses.do?hidePhysical=true&hideButton=true" 
	    	         id="addressDiv${pos}" idx="${pos}" orderId="${OrdInvoice.orderId}" param="{orderId:'${OrdInvoice.orderId}',index:'${pos}'}">
	    	    </div>
			</div>
			</#list>
			<div class="operate mt20" style="text-align:center" id="operate_mt20${pos}"></div>
			</#list>
			<div class="operate mt20" style="text-align: left;margin:5px">
			    <input type="button" value="保存发票" class="btn btn_cc1 saveForm" />
			<div>
		</form>
		<input type="hidden" name="orderIds"  id="orderIds" value="${orderIds}"/> 
	</div>
	<#include "/base/foot.ftl"/>
	<script type="text/javascript">
	  
	  //发票购买方式
	  $("#[id^=purchaseWay]").change(function() {
	      var pos=$(this).attr("date"); 
	      //个人
	      if($(this).val() == 'personal'){
	          $('#taxNumber'+pos).attr('readonly',true);//纳税人识别号
	          $('#buyerAddress'+pos).attr('readonly',true);//购买方地址
	          $('#buyerTelephone'+pos).attr('readonly',true);//购买方电话
	          $('#bankAccount'+pos).attr('readonly',true);//开户银行
	          $('#accountBankAccount'+pos).attr('readonly',true);//开户银行账号
	      }else{
	          $('#taxNumber'+pos).attr('readonly',false);//纳税人识别号
	          $('#buyerAddress'+pos).attr('readonly',false);//购买方地址
	          $('#buyerTelephone'+pos).attr('readonly',false);//购买方电话
	          $('#bankAccount'+pos).attr('readonly',false);//开户银行
	          $('#accountBankAccount'+pos).attr('readonly',false);//开户银行账号
	      }
	  });
	
       $("a.delete").live("click",function(){ 
          if (confirm("您确定要删除吗！")){ 
          var result=$(this).attr("result"); 
          var orderId =  $("#orderId").val();
          var userNo=$("#userNo").val();
       $.ajax({ 
	      url : "/vst_order/order/orderInvoice/ord/removeAddressInvoice.do?addressNo="+result+"&userNo="+userNo, 
	      type : 'post', 
	      dataType : 'json', 
	      success : function(data) { 
		       if(data.code=="success"){ 
		          alert('删除成功！'); 
		          $.post("/vst_order/order/orderInvoice/ord/loadAddresses.do",
							{
								hidePhysical:true,
								hideButton:true,
								orderId:orderId,
								index:pos++
							},
							function(result){
								$("#[id^=addressDiv]").html(result);
								$("#[id^=addressDiv]").each(function(){
									var $idx = $(this).attr("idx");
									$(this).find("input[name^=invoiceAddressId]").attr("name","invoiceAddressId"+$idx);
								});
								
								var userAddressListCount = $("#userAddressListCount").val();
								if(userAddressListCount < 20){
        	                        str = '<input type="button" value="新增地址" class="btn btn_cc1" name="editPassed" onclick="showAddAddressDialg()"/>';
		                        }else{
		   	                        str = '<input type="button" value="新增地址" class="btn btn_cc1" name="editPassed" onclick="showAddAddressDialg()" disabled="disabled"/>';
		                        }
		                        $("#[id^=operate_mt20]").html(str);
								
							}
						);
		          //window.parent.frames.iframeMain.location.reload(); 
		          // window.location.reload();
		          //$('#addressNo'+result).remove();
		          
		       }else{ 
		           alert('删除失败！');  
		       } 
          } 
      }); 
        } 
      });       
	 </script>
	
	<script type="text/javascript">
            function showAddAddressDialg() {
               var orderId =  $("#orderId").val();
               showAmountDialog = new xDialog("/vst_order/order/orderInvoice/ord/doAddAddress.do",{"orderId":orderId},{title:"添加地址",width:700});
			}     
	 </script>
	
	<script type="text/javascript">
	     $(function(){
           $("input.saveForm").click(function(){
               var flag=true;
                $("#[id^=title]").each(function(i,e){  
						   if($(e).val()==""){
							   alert("购买方名称不可以为空");
							   flag=false;
							   return false;
						   }						  					   
			     });
			    
			    var count = $("#contentDiv").find(".s_table").length;
			    for(i=0;i<count;i++){
			       var value = $("input[name='form["+ i +"].purchaseWay']:checked").val();
			       if(value == 'company'){
			           if($("#taxNumber"+i).val() != "")
			           {
							if(!$("#taxNumber"+i).val().match(/[^\u4e00-\u9fa5]/))
							{
								alert("纳税人识别号不可以输入汉字");
								flag=false;
								return false;
							}
			           }

			           if($("#accountBankAccount"+i).val() != "")
			           {
			               if(isNaN($("#accountBankAccount"+i).val()))
			               {
			              		alert("开户银行账号必须要输入数字！");
			              		flag=false;
								return false;
			           		}
			           }
			           
			       } 
			    }
			     
                    var money=0;
					var reg=/^[1-9]\d*$/;					
					 $("#[id^=amount]").each(function(i,e){ 
						   if($(e).val()==""){
							   alert("发票金额不可以为空");
							   flag=false;
							   return false;
						   }else if(!reg.test($(e).val())){
							   alert("发票金额必须为整数");
							   flag=false;
							   return false;
						   }else{
						       money+=parseFloat($(e).val());
						   }			    
					 });	
					if(money>$("#totalYuan").val()){
						alert("发票金额不可以超过可开票总金额！");
						 flag= false;
						 return false;
					}	
				var tem=0;
				$("#[id^=deliveryType]").each(function(i,e){		
					if($(e).val()!='SELF'){	
						tem++;													
						var invoiceAddressId=$("input[name^=invoiceAddressId]:checked");							
						if(invoiceAddressId.length<tem){
							alert("送货方式不是自取时必须选择收件地址");
							flag=false;
							return false;
						}
					}
				});
               
               if(flag==true){
                    checkAndSubmit("/vst_order/order/orderInvoice/ord/saveCompositeInvoice.do","saveForm");
	           } 
			});
        });
	        
	        function checkAndSubmit(url,form) {
				var $form=$("#"+form);
				$.post($form.attr("action"),$form.serialize(),function(data){
		     		if(data.success){
		     		   $("#contentDiv").html("<h2>操作成功</h2><p><a href='/vst_order/order/orderInvoice/ord/goInvoceForm.do'>返回申请发票页面</a></p>");		
		     		}
		     		if(data.code == -1){
		     		   alert(data.msg);
		     		}
		     	},"JSON");
			}
		</script>
	
	<script type="text/javascript">
	    var pos = $("#pos").val();
		$(document).ready(function(){
				$("div[id^=addressDiv]").each(function(){
					$(this).loadUrlHtml();
				});
			});
			
		function loadUrlHtml() {
		    var $this = $(this);
	    	var url = $this.attr("href");
	    	var param = $this.attr("param");
		    var jsonObj = null;
		    if (param != null && "" != param) {
			    jsonObj = eval('(' + param + ')');
		    }

	        if (url != null || url == "") {
		        $.ajax({
				    type : "POST",
				    dataType : "html",
				    url : url,
				    async : false,
				    data : jsonObj,
				    beforeSend : function() {
					     $this.html("<img src=\"http://pic.lvmama.com/img/loading.gif\"/>loading...");
				    },
				    success : function(data) {
					     $this.html(data);
				    }
			   });
		  } 
	 }
</script>
	<script type="text/javascript"> 
	$(function(){ 
		var userAddressListCount = $("#userAddressListCount").val();
          var str = "";
          if(userAddressListCount < 20){
        	  str = '<input type="button" value="新增地址" class="btn btn_cc1" name="editPassed" onclick="showAddAddressDialg()"/>';
		   }else{
		   	  str = '<input type="button" value="新增地址" class="btn btn_cc1" name="editPassed" onclick="showAddAddressDialg()" disabled="disabled"/>';
		   }
		   
		  var invoiceNumber = $('#invoiceNumber').length;
		  for(var i = 0;i < invoiceNumber;i++){
	          $('#operate_mt20'+i).html(str);
          }
	});
     </script>
	</body>
</html>
