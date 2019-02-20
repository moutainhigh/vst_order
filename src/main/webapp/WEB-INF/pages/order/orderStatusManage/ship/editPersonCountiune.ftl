<#import "/base/spring.ftl" as s/>
	<div style="margin-left:25%;height: 80px;">
		<div><strong>游玩人信息锁定后，前台用户将无法修改，请确认操作</strong> </div>
		<div style="margin-top:20px;">
			<button class="pbtn pbtn-small btn-ok" id="travellerLockFlagTrue">确定</button>
			<button id="travellerLockFlagFalse" style="margin-left:30%;" class="pbtn pbtn-small btn-ok">取消</button>
		</div>
	</div>
	<script>
		$("#travellerLockFlagFalse").bind("click",function(){
			editTravellerLockFlagDialog.close();
		});
		
		$("#travellerLockFlagTrue").bind("click",function(){
			isTravellerLockFlagSubmit();
			editTravellerLockFlagDialog.close();
		});
	</script>