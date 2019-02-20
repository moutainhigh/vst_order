<script type="text/javascript"> 

</script>
		<strong>配送</strong>
		<p style="color:red">友好提示：一个用户只能添加20条地址.</p>
		<br />
		<input type="hidden" id="userAddressListCount" name="userAddressListCount" value="${userAddressListCount}"/>		
		<table class="p_table table_center">
			<thead>
				<tr>
					<th>
						发票
					</th>
					<th>
					             地址
				    </th>
					<th>
						接收人
					</th>
					<th>
						电话
					</th>
					<th>
						邮编
					</th>
					<th>
						操作
					</th>
				</tr>
				<thead>
				<tbody>
					<#list userAddressList as user>
					<tr id='addressNo${user.addressNo}'>
						<td>
						    <input type="radio" id="invoiceAddressId${index}" name="invoiceAddressId${index}" value="${user.addressNo}"/>
						</td>
						<td>
							${user.province!''} ${user.city!''}  ${user.address!''}
						</td>
						<td>
							${user.userName!''}
						</td>
						<td>
							${user.mobileNumber!''}
						</td>
						<td>
							${user.postCode!''}                  
						</td>
						<td>
							<a href="#delete" result="${user.addressNo}" class="delete" id="deleteOrder">删除</a>
							<input type="hidden" id ="userNo" name="userNo" value="${user.userNo}"/>
						</td>
					</tr>
				</#list>
			</tbody>
		</table>