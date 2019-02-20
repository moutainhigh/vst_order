<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-订单监控</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>
<#import "/base/paginationMonitor.ftl" as pagination>
<#--页面导航-->
<div class="iframe_header">
	<i class="icon-home ihome"></i>
	<ul class="iframe_nav">
		<li><a href="#">首页</a> &gt;</li>
		<li><a href="#">订单管理</a> &gt;</li>
		<li class="active">订单列表</li>
	</ul>
</div>

<#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/ord/order/orderMarkList.do" method="post">
        <table class="s_table2 form-inline">
            <tbody>
                <tr>
                	
                    <td width="" colspan="2">
                        <input type="hidden" id= "requiredFlg" value="${orderNoRequired}"/>
                    	<label>订单编号：<@s.formInput "ordOrderMarkVo.orderId" 'class="w9" number="true"'/></label>
                        <label>下单时间：
                        	<input id="d4321" class="Wdate" type="text" value="${ordOrderMarkVo.createTimeBegin}" 
                        	onfocus="WdatePicker({readOnly:true,dateFmt:'yyyy-MM-dd HH:mm:00',minDate:'#F{$dp.$D(\'d4322\',{M:-6});}',maxDate:'#F{$dp.$D(\'d4322\',{d:0});}'})" errorele="selectDate" name="createTimeBegin" readonly="readonly">
	                    	 -- 
	                    	 <input id="d4322" class="Wdate" type="text" value="${ordOrderMarkVo.createTimeEnd}" 
	                    	 onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:59',maxDate:'#F{$dp.$D(\'d4321\',{M:6});}',readOnly:true,minDate:'#F{$dp.$D(\'d4321\',{d:0});}'})"
	                    	  errorele="selectDate" name="createTimeEnd">
	                    </label>
	                    <label>
                                                                                                          所属产品经理:
                              <@s.formHiddenInput "ordOrderMarkVo.managerId"/>
                              <input type="text" id="managerName" name="managerName" class="search searchInput" autocomplete="off" value="${managerName}">
                        </label>
                    </td>
                 </tr>
                 <tr>
                    　<td colspan="2">
	                        <label>订单状态：<@s.formSingleSelect "ordOrderMarkVo.orderStatus" orderStatusMap 'class="w10"'/></label>
	                        <label>支付状态：<@s.formSingleSelect "ordOrderMarkVo.paymentStatus" paymentStatusMap 'class="w10"'/></label>
	                        <label>搬单标记：
	                        <select id="markFlag" name="markFlag" class="w10" >
	                        <option value="">全部</option>
	                        <option value="1">已搬单</option>
	                        <option value="2">未搬单</option>
	                        </select>
	                        </label>
                    </td>
                </tr>
            </tbody>
        </table>
        <div class="operate mt20" style="text-align:center">
				<a class="btn btn_cc1" id="search_button">查询</a>
				<a class="btn btn_cc1" id="clear_button">清空</a>
        </div>
	</form>
</div>

<#--结果显示-->
<div id="result" class="iframe_content mt20">
<#if pageParam?? >
	<#if pageParam.items?size gt 0 >
	<img src="../../img/red_flag.png" width="30" height="30" >已搬单
    <img src="../../img/white_flag.png" width="30" height="30" style="margin-left: 20px;" >未搬单
			<div class="p_box">
				<table class="p_table table_center">
					<thead>
						<tr>
						    <th nowrap="nowrap" style="width:2%;">搬单状态</th>
							<th nowrap="nowrap" style="width:10%;">下单渠道</th>
							<th nowrap="nowrap" style="width:10%;">订单号</th>
							<th nowrap="nowrap" style="width:20%;">产品名称</th>
							<th nowrap="nowrap" style="width:10%;">支付方式</th>
							<th nowrap="nowrap" style="width:5%;">订购数量</th>
							<th nowrap="nowrap" style="width:10%;">下单时间</th>
							<th nowrap="nowrap" style="width:10%;">游玩日期</th>
							<th nowrap="nowrap" style="width:6%;">联系人</th>
							<th nowrap="nowrap" style="width:12%;">当前状态</th>
							<th nowrap="nowrap" style="width:5%;">所属BU</th>
						</tr>
					</thead>
					<tbody>
						<#list pageParam.items as result> 
							<tr>
							    <td>
							        <#if result.markFlag == 1>
							            <img src="../../img/red_flag.png" width="30" height="30" style="cursor: pointer;" class="markFlag" markFlag="${result.markFlag}" orderId="${result.orderId}">
							        <#else>
                                        <img src="../../img/white_flag.png" width="30" height="30" style="cursor: pointer;" class="markFlag" markFlag="${result.markFlag}" orderId="${result.orderId}">
                                    </#if>
							    </td>
								<td>${result.distributorName}</td>
								<td>
									<a title="点击查看订单详情" href="/vst_order/order/ordCommon/showOrderDetails.do?orderId=${result.orderId}" target="_blank">
										${result.orderId}
									</a>
									<#if result.guarantee == 'GUARANTEE'>
									<a title="该订单需要担保">#</a>
									</#if>
								</td>
								<td>${result.productName}</td>
								<td>
                                    <#list payTargetSet as payTarget> 
                                        <#if payTarget.code == result.paymentTarget>
                                            ${payTarget.cnName}
                                        </#if>
                                    </#list>
								</td>
								<td>${result.buyCount}</td>
								<td>${result.createTime}</td>
								<td>${result.visitTime}</td>
								<td>${result.contactName}</td>
								<td>${result.currentStatus}</td>
								<td>
								    <#list buSet as bu> 
								        <#if bu.code == result.buCode>
								            ${bu.cnName}
								        </#if>
								    </#list>
								</td>
							</tr>
						</#list>
					</tbody>
				</table>
				
				<#--分页标签-->
				<@pagination.paging pageParam/>
		</div>
	<#else>
		<div class="no_data mt20"><i class="icon-warn32"></i>暂无相关订单，请重新输入相关条件查询！</div>
	</#if>
</#if> 
</div>

<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>

<#--js脚本-->
<script type="text/javascript">
    $(function(){
     	$("#markFlag").val("${ordOrderMarkVo.markFlag}");
        //查询
        $("#search_button").bind("click",function(){
        	if($("#orderId").val()=="" && $("#d4321").val()=="" && $("#d4322").val()=="") {
        		$("#d4321").attr("required", "true");
        		$("#d4322").attr("required", "true");
        		$("#orderId").attr("required", "true");
        	}else if($("#orderId").val()==""){
        		if($("#d4321").val()==""){
        			$("#d4321").attr("required", "true");
        		}
        		if($("#d4322").val()==""){
        			$("#d4322").attr("required", "true");
        		}        		
        	}
        	    	
            $("#result").empty();
            
            // 去除input空格
            var tempTextFields = $("input[type=text]");
            $.each(tempTextFields,function(){
                this.value = $.trim(this.value);
            });
            
            //表单验证
            if(!$("#searchForm").validate().form()){
            	$("#d4321").removeAttr("required");
        		$("#d4322").removeAttr("required");
        		$("#orderId").removeAttr("required");
                return;
            }

            //假加载效果
            $("#result").empty();
            $("#result").append("<div class='loading mt20'>正在努力的加载数据中......</div>");
        
            $("#searchForm").submit();
        });
        
        //清空
        $("#clear_button").bind("click",function(){
            window.location.href = "/vst_order/ord/order/intoOrderMark.do";
        });
        
        
        $("#managerName").jsonSuggest({
            url:"${rc.contextPath}/ord/order/queryPermUserList.do",
            maxResults: 20,
            minCharacters:1,
            onSelect:function(item){
                $("#managerId").val(item.id);
            }
        });
        
    });
    
    // 按enter键提交查询
    $(document).keyup(function(event){
      if(event.keyCode ==13){
        $("#search_button").trigger("click");
      }
    });
    
//设置搬单
$("img.markFlag").bind("click",function(){
    var entity = $(this);
    var markFlag = entity.attr("markFlag");
    var orderId = entity.attr("orderId");
    var msg;
    if(markFlag == 1){
        msg = "确认取消搬单  ？";
        markFlag = 2;
    }else{
        msg = "确认设为搬单  ？";
        markFlag = 1;
    }
    
    $.confirm(msg, function () {
        $.ajax({
            url : "/vst_order/ord/order/updateMarkFlag.do",
            type : "post",
            dataType:"JSON",
            data : {"orderId":orderId,"markFlag":markFlag},
            success : function(result) {
                if(result.code == "success"){
                    $.alert(result.message,function(){
                        if(markFlag == 1){
                            entity.attr("src","../../img/red_flag.png");
                            entity.attr("markFlag", "1");
                        }else{
                            entity.attr("src","../../img/white_flag.png");
                            entity.attr("markFlag", "2");
                        }
                    });
                }else {
                    $.alert(result.message);
                }
            }
        });
    });
}); 
</script>
