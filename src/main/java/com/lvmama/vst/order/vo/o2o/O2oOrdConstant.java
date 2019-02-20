package com.lvmama.vst.order.vo.o2o;

public class O2oOrdConstant {

	public enum O2O_BACK_USERS {
		//陈思冰
		CHENSIBING("lv16978","0021990000chensibing"),
		// TESTER
		TESTER2("tester2", "qa_super"),
		// TESTER
		O2O_POS_FEFUND("O2O_POS_FEFUND", "O2O_POS_FEFUND");

		private String vstName;
		private String o2oName;

		O2O_BACK_USERS(String vstName, String o2oName) {
			this.vstName = vstName;
			this.o2oName = o2oName;
		}
		
		public String getvSTName() {
			return vstName;
		}

		public String getO2oName() {
			return o2oName;
		}

	}

}
