<html>
	<body>
		<strong>查看日志</strong>
		<table style="font-size: 12px" cellspacing="1" cellpadding="4"
					border="0" bgcolor="#B8C9D6" width="100%" class="newfont03">
					<tr bgcolor="#f4f4f4" align="center">
						<td height="30">
							日志名称
						</td>
						<td>
							内容
						</td>
						<td>
							操作人
						</td>
						<td>
							创建时间
						</td>
						<td>
							备注
						</td>
					</tr>
					<#list comLogList as log>
						<tr bgcolor="#ffffff" align="center">
							<td height="25">
								${log.logName!'' }
							</td>
							<td>
								${log.content!'' }
							<#if log.logType=='cancelToCreateNew_new'>
							老订单ID${log.parentId!''}
							</#if>
							<#if log.logType=='cancelToCreateNew_original'>
							新订单ID${log.parentId!''}
							</#if>
							</td>
							<td>
								${log.operatorName!'' }
							</td>
							<td>
								${log.createTime?string('yyyy-MM-dd')!''}
							</td>
							<td>
								${log.memo!'' }
							</td>
						</tr>
					</#list>
				</table>
	</body>	
</html>
