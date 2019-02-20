<#--页眉-->
<#import "/base/spring.ftl" as s/>
<!DOCTYPE html>
<html>
<head>
<title>订单管理-流程处理</title>
<#include "/base/head_meta.ftl"/>
</head>
<body>

    <div class="iframe_header">
        <i class="icon-home ihome"></i>
        <ul class="iframe_nav">
            <li><a href="javaScript:">首页</a>：</li>
            <li><a href="javaScript:">订单管理</a> ></li>
            <li class="active">流程处理</li>
        </ul>
    </div>
    <#--表单区域-->
<div class="iframe_search">
	<form id="searchForm" action="/vst_order/order/activitiTask/finishWorkflowTask.do" method="post">
        <table class="s_table  form-inline">
            <tbody>
                <tr>
                    <td width="">
                        <input type="hidden" id= "requiredFlg" value="${orderNoRequired}"/>
                    	<label>
                    	订单编号：
                    	<@s.formInput "param.orderId" 'class="w10" number="true"' />
                    	<!--<input type="text" name="param.orderId" id="orderId" value=""  >-->
                    	</label>
                    	<label>
                    	子订单编号：
                    	<@s.formInput "param.orderItemId" 'class="w9" number="true"'/>
                    	<!--<input type="text" name="param.orderItemId" id="orderItemId" value=""  >-->
                    	</label>
                        <label>睡眠节点名称：
                        
                        <@s.formSingleSelect "param.auditType" auditTypeMap 'class="w10"'/>
                        </label>
                    </td>
                 </tr>
            </tbody>
        </table>
        <div class="operate mt20" style="text-align:center">
        <!--关闭唤醒节点以防止滥用 -->
<!-- 				<a class="btn btn_cc1" id="search_button">唤醒</a> -->
                <a class="btn btn_cc1" id="reset_button">重置节点</a>
				<a class="btn btn_cc1" id="clear_button">清空</a>
        </div>
        <div>
        <div id="errorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>请选择节点类型</div>
	    <div id="requiredErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>订单号或者子订单号至少要输入一个！</div>
	    <div id="orderErrorMessage" class="no_data mt20" style="display:none;"><i class="icon-warn32"></i>订单号和子订单好只能输入一个！</div>
		</div>
	    
	</form>
</div>
<#--结果显示-->
<div id="result" class="iframe_content mt20">
<#if result?? >
		<div class="no_data mt20"><i class="icon-warn32"></i>${result}</div>
</#if> 
</div>

<#--页脚-->
<#include "/base/foot.ftl"/>
</body>
</html>
<#--js脚本-->
<script type="text/javascript">
	$(function(){
	
		//唤醒
		$("#search_button").bind("click",function(){
			if($(this).attr("disabled")) {
				return;
			}
			
			$("#result").empty();
			$("#requiredErrorMessage").hide();
			$("#orderErrorMessage").hide();
			$("#errorMessage").hide();
			
			// 去除input空格
			var tempTextFields = $("input[type=text]");
			$.each(tempTextFields,function(){
			 	this.value = $.trim(this.value);
			});
			
			//表单验证
			if(!$("#searchForm").validate().form()){
				return;
			}
			
			//判断订单号或者子订单号输入条件
			var orderNo=$("#orderId").val();
			var orderItemNo=$("#orderItemId").val();
			
			if(orderNo == "" && orderItemNo == ""){
				$("#requiredErrorMessage").show();
				return;
			} else if (orderNo != ""&& orderItemNo != ""){
			    $("#orderErrorMessage").show();
				return;
			}
			
			//遍历所有查询条件的值
			var value = "";
			
			//select
			var selectFields = $("select");
			$.each(selectFields,function(){
			 	value += this.value;
			});
			
			//客户必须选择audit类型
			if(value == ""){
				$("#errorMessage").show();
				return;
			}
			
			//假加载效果
			$("#result").empty();
		
			$(this).attr("disabled", true);
			$(this).css("border","1px solid #aaa").css("background-color","#bbb").css("color", "#000").css("pointer","");
			$("#searchForm").submit();
		});
		
		//清空
		$("#clear_button").bind("click",function(){
			window.location.href = "/vst_order/order/activitiTask/orderActivitiTask.do";
		});
		
		//当填写了订单编号、子订单编号
		$("#orderId,#orderItemId").blur(function(){
			var orderId=$("#orderId").val();
			var orderItemId=$("#orderItemId").val();
		});

        //重置节点
        $("#reset_button").bind("click",function(){
            if($(this).attr("disabled")) {
                return;
            }
            
            $("#result").empty();
            $("#requiredErrorMessage").hide();
            $("#orderErrorMessage").hide();
            $("#errorMessage").hide();
            
            // 去除input空格
            var tempTextFields = $("input[type=text]");
            $.each(tempTextFields,function(){
                this.value = $.trim(this.value);
            });
            
            //表单验证
            if(!$("#searchForm").validate().form()){
                return;
            }
            
            //判断订单号或者子订单号输入条件
            var orderNo=$("#orderId").val();
            var orderItemNo=$("#orderItemId").val();
            
            if(orderNo == "" && orderItemNo == ""){
                $("#requiredErrorMessage").show();
                return;
            } else if (orderNo != ""&& orderItemNo != ""){
                $("#orderErrorMessage").show();
                return;
            }
            
            //遍历所有查询条件的值
            var value = "";
            
            //select
            var selectFields = $("select");
            $.each(selectFields,function(){
                value += this.value;
            });
            
            //客户必须选择audit类型
            if(value == ""){
                $("#errorMessage").show();
                return;
            }
            
            //假加载效果
            $("#result").empty();
        
            $(this).attr("disabled", true);
            $(this).css("border","1px solid #aaa").css("background-color","#bbb").css("color", "#000").css("pointer","");
            $("#searchForm").attr("action","/vst_order/order/activitiTask/resetOrderWorkflowStatus.do"); 
            $("#searchForm").submit();
        });		
		
	});
	
	// 按enter键提交唤醒
// 	$(document).keyup(function(event){
// 	  if(event.keyCode ==13){
// 	    $("#search_button").trigger("click");
// 	  }
// 	});
	
</script>
