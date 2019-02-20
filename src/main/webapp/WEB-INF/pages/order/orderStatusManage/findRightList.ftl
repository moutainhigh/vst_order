<!DOCTYPE html>
<#--<#include "/base/head_meta.ftl"/>-->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="/vst_back/js/jquery1.6.1.js"></script>
<link rel="stylesheet" href="/vst_back/css/ui-common.css" type="text/css" />
<link rel="stylesheet" href="/vst_back/css/ui-components.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/iframe.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/dialog.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/easyui.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/button.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/base.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/normalize.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/calendar.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/jquery.jsonSuggest.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/jquery.ui.autocomplete.css" type="text/css"/>
<link rel="stylesheet" href="/vst_back/css/jquery.ui.theme.css" type="text/css"/>
<link rel="stylesheet"  href="/vst_back/css/contentManage/kindEditorConf.css" type="text/css"/>
<html>
<head>
    <title>查看权益</title>
</head>
<body>
<br><br>
<!-- 订单详情页面EBK及传真列表查询 -->

<!-- 主要内容显示区域\\ -->
<div style="font-size:20px">
    <strong>
       	 查看权益
    </strong>
</div>
<br>

<div class="iframe-content">   
	<div class="p_box">产品信息：<strong>${orderItem.productName}（${orderItem.suppGoodsName}）</strong></div>  
	<div class="p_box">购买数量：<strong>${orderItem.quantity}</strong></div>  

    <div class="p_box">
    <table class="p_table table_center">
                <thead>
                    <tr>
                    <th>权益名称</th>
                    <th>权益说明</th>
                    </tr>
                </thead>
                <tbody>
					
					<tr>
						<td>
							${financeInterestsBonusVo.interestsBonusTitle}
						</td>
						<td>
							根据订单应付总额 ${financeInterestsBonusVo.interestsPercent}%发放权益金。自发放至日起，消费金有效期${financeInterestsBonusVo.consumLimit}天，权益金有效期${financeInterestsBonusVo.interestsLimit}天。权益金不可用品类：${financeInterestsBonusVo.banCategory}，权益金不可用产品ID：${financeInterestsBonusVo.banPid}。
						</td>
						
				     </tr>
				     
				     
				     <#if financeOtherInterestsVoList??>
				     	<#list financeOtherInterestsVoList as map>
					     	<tr>
								<td>
									${map['otherInterestsContent']}
								</td>
								<td>
									${map['otherInterestsBody']}
								</td>
						     </tr>
				     </#list>
				     </#if>
				     
                </tbody>
            </table>
        
	</div><!-- div p_box -->
</div>

<#include "/base/foot.ftl"/>
</body>
</html>

