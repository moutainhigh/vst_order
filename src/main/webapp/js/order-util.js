var LVUTIL = {
	ORDER : {
		COMM : {
			/**
			 * 是否是身份证号码(包含15位18位)
			 * @param num
			 * @returns
			 */
			isIdCardNum : function(num){
				 num = num.toUpperCase();
				    if (!(/(^\d{15}$)|(^\d{17}([0-9]|X)$)/.test(num))) {
				        return false;
				    }
				    //校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。 
				    var len, re;
				    len = num.length;
				    if (len == 15) {
				        re = new RegExp(/^(\d{6})(\d{2})(\d{2})(\d{2})(\d{3})$/);
				        var arrSplit = num.match(re);
				        //检查生日日期是否正确 
				        var dtmBirth = new Date('19' + arrSplit[2] + '/' + arrSplit[3] + '/' + arrSplit[4]);
				        var bGoodDay;
				        bGoodDay = (dtmBirth.getYear() == Number(arrSplit[2])) && ((dtmBirth.getMonth() + 1) == Number(arrSplit[3])) && (dtmBirth.getDate() == Number(arrSplit[4]));
				        if (!bGoodDay) {
				            return false;
				        } else {
				            //将15位身份证转成18位 
				            //校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。 
				            var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
				            var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
				            var nTemp = 0, i;
				            num = num.substr(0, 6) + '19' + num.substr(6, num.length - 6);
				            for (i = 0; i < 17; i++) {
				                nTemp += num.substr(i, 1) * arrInt[i];
				            }
				            num += arrCh[nTemp % 11];
				            return num;
				        }
				    }
				    if (len == 18) {
				        re = new RegExp(/^(\d{6})(\d{4})(\d{2})(\d{2})(\d{3})([0-9]|X)$/);
				        var arrSplit = num.match(re);
				        //检查生日日期是否正确 
				        var dtmBirth = new Date(arrSplit[2] + "/" + arrSplit[3] + "/" + arrSplit[4]);
				        var bGoodDay;
				        bGoodDay = (dtmBirth.getFullYear() == Number(arrSplit[2])) && ((dtmBirth.getMonth() + 1) == Number(arrSplit[3])) && (dtmBirth.getDate() == Number(arrSplit[4]));
				        if (!bGoodDay) {
				            return false;
				        }
				        else {
				            //检验18位身份证的校验码是否正确。 
				            //校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。 
				            var valnum;
				            var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
				            var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
				            var nTemp = 0, i;
				            for (i = 0; i < 17; i++) {
				                nTemp += num.substr(i, 1) * arrInt[i];
				            }
				            valnum = arrCh[nTemp % 11];
				            if (valnum != num.substr(17, 1)) {
				                return false;
				            }
				            return num;
				        }
				    }
				    return false;
				
			}
		}

	}
};

