<#--页眉-->

<!DOCTYPE html>
<html>
<head>
<title></title>
</head>
<body>
<#if goodsPersonList?size!=0 >
<div id="logResultList" class="divClass">
<div class="order_msg clearfix">
</br>
                	<strong>
                	商品关联人
                	  </strong>
                	</div>
 <table class="p_table table_center mt20">
                <thead>
                 
                    <tr>
                        <th>商品名</th>
                        <th>关联人数</th>
                        <th>关联的人</th>
                    </tr>
                </thead>
                <tbody>
                    
                <#list goodsPersonList as goodsPerson> 
                    <tr>
                        <td>
                      ${goodsPerson.goodsName!''}
                        </td>
                        <td>
                        ${goodsPerson.associationCount!''}
						</td>
                        <td> ${goodsPerson.associationPerson!''}</td>
                        
                    </tr>
                </#list>
                </tbody>
            </table>
</div>
<#--页脚-->
<#include "/base/foot.ftl"/>

</#if>
</body>
</html>
<script type="text/javascript">

         
 </script>
