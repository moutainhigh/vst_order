<#--页眉-->
<#import "/base/spring.ftl" as spring/>
<#import "/base/pagination.ftl" as pagination>
<#--页面导航-->
<div class="p_box" id="logResultList">
			<input type="hidden" id="selectedEarnestHid" value=""/>
            <table class="p_table table_center product">
                <thead> 
                    <tr class="noborder">
                        <th colspan="4" style=" text-align:left;">产品选择</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="table_nav">
                        <td class="w10">推荐级别</td>
                        <td class="w10 text_left">出发港口</td>
                        <td colspan="2" class="text_left">产品名称</td>       
                    </tr>
                    <#if prodList?? && prodList?size &gt;  0>
                    <#list prodList  as prod>
                    <tr class="table_nav">
                    	<input type="hidden" id="${prod.productId!''}Hid" value="${prod.propValue['earnest']!''}"/>
                        <td class="w10"><#if prod.propValue['are_charter'] == 'Y'>5<#else>4</#if> </td>
                        <td class="w10 text_left"><#if prod.bizDistrict??>${prod.bizDistrict.districtName!''}</#if></td>
                        <td class="text_left">${prod.productName!''}</td>
                        <td class="w10"><div class="operate" style="text-align:center"><a data="${prod.productId!''}" id="${prod.productId!''}Btn" class="btn btn_cc1 xzBtn">选择</a></div></td>       
                    </tr>
                    </#list>
                    <#else>
                    <tr class="table_nav"><td colspan="4"><div class="no_data mt20"><i class="icon-warn32"></i>暂无相关产品，重新输入相关条件查询！</div></td>	</tr>
                    </#if>
                </tbody>
            </table>
            <#if prodList?? && prodList?size &gt;  0>
            <div class="pages darkstyle">	
            	<@pagination.paging pageParam true "#logResultList"/>
			 </div>
			  </#if>
  </div>
  <script>
  
  	function tanchu(){ 
		var _scrolltop = $(document).scrollTop()+150;
		var height_w =$(document).height();
		var width = $('.PopBox').width();
		$('.PopBox').css({'margin-left':-width/2});
		$('.pop_body_bg').css({'height':height_w,'width':$(document.body).width()}).show();
		$('.PopBox').show().css({'top':_scrolltop});
	};
	function guanbi(){ 
		$('.pop_body_bg,.PopBox').hide();
	};
	
	function selectGroupDate(){
		var productId = $(this).attr("data");
		//判断用户是否登录
		if(book_user_id==""){
			tempSelectProduct = productId;
			showQueryUserIdDialog();
			return;
		 }
		var loading1	= pandora.loading("正在努力加载...");
		//查询团期	
  		var beginDate = $("#beginDate").val();
		var endDate = $("#endDate").val();
		var distributionId = $("input[name=distributionId]").val();
		$("#selectedEarnestHid").val($("#"+productId+"Hid").val());
		//设置已选择的邮轮标准产品ID
		$.get("/vst_order/ord/order/queryShipProductGroupDateList.do?beginDate="+beginDate+"&endDate="+endDate+"&productId="+productId+"&distributorId="+distributionId,function(result){
			$(".PopBox").html(result);
			tanchu();
			loading1.close();
		 	$('.close_Btn').click(function(){
				guanbi();
			})
		});
	}
	
	
  	$('.xzBtn').bind("click",selectGroupDate);
  </script>