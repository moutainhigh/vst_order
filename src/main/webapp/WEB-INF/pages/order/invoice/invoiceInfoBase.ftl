<div class="solid_line mt10 mb10"></div>
<table>
    <tbody>
        <tr>
            <td class="e_label">购买方式：</td>
            <td><#if orderInvoiceInfoVst.purchaseWay == 'personal'>个人
            <#elseif orderInvoiceInfoVst.purchaseWay == 'company'>公司</#if></td>
        </tr>
        <tr>
            <td class="e_label">发票抬头：</td>
            <td>${orderInvoiceInfoVst.title}</td>
        </tr>
        <tr>
            <td class="e_label">纳税人识别号：</td>
            <td>${orderInvoiceInfoVst.taxNumber}</td>
        </tr>
      <tr>
         <td class="e_label">开票项目：</td>
         <td>${orderInvoiceInfoVst.content}</td>
     </tr>
      <tr>
         <td class="e_label">开票金额：</td>
         <td>RMB ${orderInvoiceInfoVst.amount/100}元</td>
      </tr>

      <tr>
         <td class="e_label">联系人姓名：</td>
         <td>${orderInvoiceInfoVst.contactName}</td>
     </tr>
      <tr>
         <td class="e_label">联系方式：</td>
         <td>${orderInvoiceInfoVst.contactMobile}</td>
     </tr>
      <tr>
         <td class="e_label">邮政编码：</td>
         <td>${orderInvoiceInfoVst.postcode}</td>
     </tr>
      <tr>
         <td class="e_label">送货方式：</td>
         <td>${orderInvoiceInfoVst.getZhDeliveryType()!''}</td>
     </tr>
      <tr>
         <td class="e_label">寄送地址：</td>
         <td>${orderInvoiceInfoVst.getAddress()!''}</td>
     </tr>
      <tr>
         <td class="e_label">处理状态：</td>
         <td>
           <#if orderInvoiceInfoVst??>
              <#if orderInvoiceInfoVst.status == "PENDING">
                                                                                            待申请
              <#elseif orderInvoiceInfoVst.status == "APPLIED" >
                                                                                              已申请
              <#elseif orderInvoiceInfoVst.status == "CANCEL" >
                                                                                              已取消
              <#elseif orderInvoiceInfoVst.status == "MANUAL" >
                                                                                              已人工申请
               <#elseif orderInvoiceInfoVst.status == "INVALID" >
                                                                                              无效                                                                                
              <#elseif orderInvoiceInfoVst.status == "FAILURE" >
                                                                                             申请失败
              <#else>                                                                             
              </#if>
            </#if>  
         </td>
          <#if orderInvoiceInfoVst??>
             <#if orderInvoiceInfoVst.status == "PENDING" || orderInvoiceInfoVst.status == "MANUAL" || orderInvoiceInfoVst.status == "FAILURE"> <td><input type="button" id="invoiceApply" value="手动申请"/></td></#if>
          </#if>   
     </tr>
    </tbody>
</table>