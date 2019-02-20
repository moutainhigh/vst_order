<div class="p_box">
            <table class="p_table table_center">
                <thead> 
                    <tr class="noborder">
                        <th colspan="2" style=" text-align:left;">联系人信息</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;"><i class="red">*</i>联系人：</label>
                            <input type="text" class="w8 fleft" id="contact-fullName" name="contact.fullName" required maxlength=10/>
                        </td>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;"><i class="red">*</i>联系手机：</label>
                            <input type="text" class="w8 fleft" id="contact-mobile" name="contact.mobile" required=true number=true/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;">传真号码：</label>
                            <input type="text" class="w4 fleft" errorEle="fax" placeholder="区号" name="fax1" id="fax1" number=true />
                            <input type="text" class="w8 fleft" errorEle="fax" placeholder="传真号码" name="fax2" id="fax2" number=true />
                            <input type="text" class="w4 fleft" errorEle="fax" placeholder="分机号" name="fax3" id="fax3" number=true />
                            <input type="hidden" class="w4 fleft" name="contact.fax"/>
                            <div id="faxError" style="display:inline"></div>
                        </td>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;">固定电话：</label>
                            <input type="text" class="w4 fleft" errorEle="phone" placeholder="区号" name="phone1" id="phone1" number=true />
                            <input type="text" class="w8 fleft" errorEle="phone" placeholder="电话号码" name="phone2" id="phone2" number=true />
                            <input type="text" class="w4 fleft" errorEle="phone" placeholder="分机号" name="phone3" id="phone3" number=true />
                            <input type="hidden" class="w4 fleft" name="contact.phone"/>
                            <div id="phoneError" style="display:inline"></div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;"><i class="red">*</i>邮件地址：</label>
                            <input type="text" class="w8 fleft" placeholder="a@b.c" name="contact.email" required email=true/>
                        </td>
                        <td>
                        	
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                        	<label class="checkbox mr10 fleft w8" style=" text-align:right;"><i class="red">*</i>紧急联系人：</label>
                            <input type="text" class="w8 fleft" name="emergencyPerson.fullName" required maxlength=10/>
							<label class="checkbox mr10 fleft w10" style=" text-align:right;"><i class="red">*</i>紧急联系电话：</label>
                            <input type="text" class="w8 fleft" name="emergencyPerson.mobile" required/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <!--这里是联系人信息 -->