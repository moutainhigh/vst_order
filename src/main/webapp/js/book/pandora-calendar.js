(function (global, $, pandora, undefined) {
    "use strict" // 严格模式

    //if (global.pandora) {
    //    return;
    //}

    // Calendar 构造函数
    function Calendar() { }

    Calendar.prototype = {
        constructor: Calendar, // 重新指向构造函数 Calendar 

        init: function (config) {
            var defaults = pandora.calendar.defaults,
                config = config || {};

            // 合并默认配置
            for (var i in defaults) {
                if (config[i] === undefined) {
                    config[i] = defaults[i];
                };
            };

            var now = new Date(),
                year = now.getFullYear(),
                month = now.getMonth(),
                that = this;

            this.config = config;

            $(config.input).click(function () {
                var today = that._getToday(),
                    val = $(this).val(),
                    mos = config.mos,
                    cascadeVal;
                that.$input = $(this);
                cascadeVal = that._getCascadeVal();

                if ($('#js-calendar').length > 0) {
                    $('.calendar-wrap').detach();
                }

                // 级联日期 (bug) 
                if (val !== "" && that.$input.attr("data-cascade") !== "true") {
                    val = val.split("-");
                    year = parseInt(val[0], 10);
                    month = parseInt(val[1], 10) - 1 === -1 ? 0 : parseInt(val[1], 10) - 1;

                    if ((year - today.year) * 12 + month - today.month >= mos) {
                        month -= 1;
                    }

                } else if (cascadeVal.length > 1) {
                    year = parseInt(cascadeVal[0], 10);
                    month = parseInt(cascadeVal[1], 10) - 1 === -1 ? 0 : parseInt(cascadeVal[1], 10) - 1;

                    if ((year - today.year) * 12 + month - today.month >= mos) {
                        month -= 1;
                    }
                }


                that._drawDate(month, year);
                that.selectMonth();
                that.selectDate();

                if ((year - today.year) * 12 + month > today.month) {
                    $(".month-prev").show();
                } else {
                    $(".month-prev").hide();
                }

                if ((year - today.year) * 12 + month + 1 < mos + today.month) {
                    $(".month-next").show();
                } else {
                    $(".month-next").hide();
                }

                // 级联对象有选中
                if (that.$input.attr("data-cascade") === "true" && that._getCascadeVal().length !== 1 && config.area) {
                    that._bindEvent();
                }

            });


            this.inputBlur(config);
        },

        /**
         *  绘制日期布局
         */
        _drawDate: function (month, year, $obj) {
            var nextMonth = month + 1 > 11 ? 0 : month + 1,
                nextYear = month + 1 > 11 ? year + 1 : year,
                prevTitle = year + '年' + (month + 1) + '月',
                nextTitle = nextYear + '年' + (nextMonth + 1) + '月',
                prevCalendar = this._drawMonth(this._getDayIndex(month, year), month, year),
                nextCalendar = this._drawMonth(this._getDayIndex(nextMonth, nextYear), nextMonth, nextYear);

            this.month = [month, nextMonth];
            this.year = [year, nextYear];

            if ($obj !== undefined) {
                $obj.find('span').eq(0).text(prevTitle);
                $obj.find('span').eq(1).text(nextTitle);
                $obj.find('dd').eq(0).html(prevCalendar);
                $obj.find('dd').eq(1).html(nextCalendar);
            } else {
                var offset = this.offsets(),
                    wrap = $('<div id="js-calendar" class="calendar-wrap" style="top:' + offset.top + 'px;left:' + offset.left + 'px"></div>').prepend(this._getTemplate().replace('{monthPrev}', prevTitle).replace('{PrevMonthContent}', prevCalendar).replace('{monthNext}', nextTitle).replace('{NextMonthContent}', nextCalendar));

                $('body').prepend(wrap);
            }
        },

        /**
         *  绘制当月
         */
        _drawMonth: function (firstIndex, month, year) {
            var day = this._getDatesByMonth(month, year);

            return this._dateLayout(firstIndex, day, month, year);
        },

        /**
         *  日期布局
         */
        _dateLayout: function (firstIndex, day, month, year) {
            var html = '';

            html += this._setNullDate(firstIndex);
            html += this._setDisabledDate(month, year, day);
            html += this._setDisplayDate(firstIndex, month, year, day);
            html += this._setNullDate(42 - firstIndex - day);

            html = this._setToday(month, year, html);
            html = this._setSelectedDate(html);

            return html;
        },

        /**
         * 设置空的日期
         */
        _setNullDate: function (index) {
            var html = "";

            for (var i = 0 ; i < index; i++) {
                html += '<a class="day-no" href="javascript:;"></a>';
            }

            return html;
        },

        /**
         * 设置禁用日期
         */
        _setDisabledDate: function (month, year, day) {
            var html = "",
                today = this._getToday(),
                disabledDays = today.date - 1, // 当月已过天数
                days = this._getDisabledDays(month, year),
                selectedDate = this._getCascadeVal();

            // HACK    
            if (this.$input.attr("data-cascade") === "true") {

                if (days !== 0) {
                    disabledDays = days > day ? day : days - 1;
                };

            };

            // 当月已过日期
            if (year === today.year && today.month === month) {

                for (var i = 0 ; i < disabledDays; i++) {
                    html += '<a class="day-over" href="javascript:;">' + (i + 1) + '</a>';
                }

            } else {

                // 判断是否级联
                if (this.$input.attr("data-cascade") === "true" && month < (parseInt(selectedDate[0], 10) - year) * 12 + parseInt(selectedDate[1], 10)) {

                    for (var i = 0 ; i < disabledDays; i++) {
                        html += '<a class="day-over" href="javascript:;">' + (i + 1) + '</a>';
                    }

                };

            }

            return html;
        },

        /**
         *  设置今天日期 (有没更好的解决方法)
         */
        _setToday: function (month, year, html) {
            var today = this._getToday(),
                date = today.year + "-" + this._mend(month + 1) + "-" + this._mend(today.date);

            if (year === today.year && today.month === month) {
                html = html.replace("name=" + date + "", "name=" + date + " class=\"day\"");
            }

            return html;
        },

        /** 
         *  设置显示日期
         */
        _setDisplayDate: function (firstIndex, month, year, day) {
            var html = "",
                today = this._getToday(),
                date = today.date, // 当月当天
                displayDays = day - date, // 当月未过期日期
                days = this._getDisabledDays(month, year),
                selectedDate = this._getCascadeVal(), // HACK 后续改进
                i;

            // HACK
            if (this.$input.attr("data-cascade") === "true") {
                displayDays = date = days === 0 ? date : days;
            }

            displayDays = today.month === month ? day - date : day;

            if (((year - today.year) * 12 + month - today.month) === this.config.mos) {
                displayDays = today.date;
            }

            if (year === today.year && today.month === month) {

                for (i = 0; i <= displayDays; i++) {
                    html += '<a name=' + year + '-' + this._mend(month + 1) + '-' + this._mend(date + i) + ' week=' + ((firstIndex + date + i - 1) % 7) + ' href="javascript:;">' + (date + i) + '</a>';
                }

            } else {

                // 大于当月日期都未过期 
                if (this.$input.attr("data-cascade") === "true" && month < (parseInt(selectedDate[0], 10) - year) * 12 + parseInt(selectedDate[1], 10)) {

                    if (month + 1 === parseInt(selectedDate[1], 10)) {
                        date = parseInt(selectedDate[2], 10);
                    } else {
                        displayDays = 0;
                    }

                } else {
                    date = 1;
                }

                for (i = date; i <= displayDays; i++) {
                    html += '<a name=' + year + '-' + this._mend(month + 1) + '-' + this._mend(i) + ' week=' + (firstIndex + i - 1) % 7 + ' href="javascript:;">' + i + '</a>';
                }

                if (((year - today.year) * 12 + month - today.month) === this.config.mos) {

                    for (var i = displayDays ; i < day; i++) {
                        html += '<a class="day-over" href="javascript:;">' + (i + 1) + '</a>';
                    }

                }

            }

            return html;
        },

        /**
         *  设置选中日期 (有待改进)
         */
        _setSelectedDate: function (html) {
            var value = this.$input.val();

            if (value === "") {
                return html;
            }

            html = html.replace("name=" + value + "", "class=\"day-selected\" name=" + value + "");
            return html;
        },

        /**
         *  获取禁止点击的日期
         */
        _getDisabledDays: function (month, year) {
            var value = this._getCascadeVal(),
                days = 0,
                yearcon = parseInt(value[0], 10) - year,
                months = yearcon * 12 + (parseInt(value[1], 10) - month - 1),
                len = parseInt(value[1], 10) - month - 1;

            if (months > 0) {

                for (var i = 0; i < len; i++) {
                    days += this._getDatesByMonth(month + i, year);
                }

                if (yearcon > 0) {
                    len = parseInt(value[1], 10);
                    year = parseInt(value[0], 10);

                    for (var j = 0; j < len; j++) {
                        days += this._getDatesByMonth(j + 1, year);
                    }

                }

            }

            days += parseInt(value[2], 10);
            return value.length === 1 ? 0 : days;
        },

        /**
         *  获取级联对象目标对象的值
         */
        _getCascadeVal: function () {
            var inputArr = $("input[data-type=calendar]"),
                len = inputArr.size(),
                val = [];

            for (var i = 0; i < len; i++) {

                if (inputArr.eq(i)[0] === this.$input[0] && i !== 0) {
                    val = inputArr.eq(i - 1).val().split("-");
                }

            }

            return val;
        },

        /**
         *  获取 calendar 模板 (计划后续将模板放到html或者XML文件中 通过ajax 去读取)
         */
        _getTemplate: function () {
            var html = this.template;

            return html;
        },

        /**
         *  补零
         *  @param {Number} 月份、几号
         */
        _mend: function (value) {
            return value.toString().length === 1 ? "0" + value : value;
        },

        /**
         *  获取单天索引
         *  @param {Number} 月份
         *  @param {Number} 年份
         */
        _getDayIndex: function (month, year) {
            var ytd = year + '/' + (parseInt(month, 10) + 1) + '/' + 1,
                week = new Date(ytd).getDay();

            return week;
        },

        /**
         *  获取当月天数
         *  @param {Number} 月份
         *  @param {Number} 年份
         */
        _getDatesByMonth: function (month, year) {
            var monthDays = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
            0 == year % 4 && ((year % 100 != 0) || (year % 400 == 0)) ? monthDays[1] = 29 : null;
            return monthDays[month];
        },

        /**
         *  获取今天的 年 月 日
         */
        _getToday: function () {
            var d = new Date();
            return { year: d.getFullYear(), month: d.getMonth(), date: d.getDate() };
        },

        /**
         *  设置显示月份
         */
        _isMonth: function (turn) {
            var year = this.year,
                month = this.month,
                today = this._getToday(),
                mos = this.config.mos,
                bool = true;

            if (turn === "next") {
                bool = mos !== 0 ? ((year[1] - today.year) * 12 + month[1] - today.month + 1) >= mos ? true : false : false;
            } else {
                bool = year[0] === today.year ? month[0] - 1 >= 0 ? month[0] - 1 === today.month ? true : false : true : false;
            }

            return bool;
        },

        /**
         * 设置显示天数
         */
        _isDay: function () {
            var fatalism = this.config.fatalism,
                today = this.getToday(),
                days = this.getDatesByMonth(today.month, today.year);

            return days - today.date - fatalism;
        },

        /**
         * 鼠标滑过事件（添加滑过效果）
         */
        _bindEvent: function () {
            var that = this;

            $("#js-calendar").find("dl").find("a[class!=day-no][class!=day-over]").hover(function () {
                var index = $(this).index(),
                    parent = $(this).parents("div[class=calendar-month]"),
                    date = $(this).attr("name").split("-"),
                    week = that._getDayIndex(parseInt(date[1], 10) - 1, parseInt(date[0], 10));

                if (parent.attr("id") === "calendar0") {
                    index = index - week - parent.find("a[class=day-over]").size() + 1;

                    for (var i = 0 ; i < index; i++) {
                        parent.find("a[class!=day-no][class!=day-over]").eq(i).addClass("day-hover");
                    }

                } else {
                    parent.siblings().find("a[class!=day-no][class!=day-over]").addClass("day-hover");

                    index = Math.abs(index - week - parent.find("a[class=day-over]").size() + 1);

                    for (var i = 0 ; i < index; i++) {
                        parent.find("a[class!=day-no][class!=day-over]").eq(i).addClass("day-hover");
                    }

                }

            }, function () {
                $("#js-calendar").find("a").removeClass("day-hover");
            });
        },

        /**
         *  选择日期
         */
        selectDate: function () {
            var that = this;

            $('.calendar-day').find('a[class!=day-no][class!=day-over]').click(function (event) {
                that.$input.val($(this).attr('name'));

                var selectWeek = that.config.weeks[$(this).attr('week')];

                //如果需要显示星期
                if(that.config.showWeek){
                    that.$input.siblings('.date_info').find('.week_info').text(selectWeek); //此处代码做个性化处理
                }

                $('.calendar-wrap').detach();

                // 向外提供接口
                //var inputArr = $("input[class=" + that.config.inputClass + "]"),
                //    len = inputArr.size();

                //for (var i = 0; i < len; i++) {

                //    if (inputArr.eq(i)[0] === that.$input[0]) {
                //        inputArr.eq(i + 1).click();
                //        break;
                //    }

                //}

                // 待优化
                if (that.config.autoRender) {
                    if (typeof that.config.selectDateCallback === "function") {
                        that.config.selectDateCallback();
                    }

                } else {
                    if (typeof that.config.selectDateCallback === "function") {
                        return that.config.selectDateCallback.call(that);
                    }
                }
                
                // 设置鼠标离开 触发级联
                that.$input.blur();
            });

        },

        /**
         *  选择月份 上个月 下个月
         */
        selectMonth: function () {
            var that = this;

            $(".month-prev, .month-next").click(function () {

                var month = that.month,
                    year = that.year;

                if ($(this).attr("class") === "month-next") {
                    that._isMonth("next") && $(this).hide();
                    that._drawDate(month[1], year[1], $(".calendar-wrap"));

                    if (that.$input.attr("data-cascade") === "true" && that.config.area) {  // 级联对象有给选中
                        that._bindEvent();
                    }

                    $(".month-prev").show();
                } else {
                    that._isMonth() && $(this).hide();
                    that._drawDate((month[0] - 1 < 0 ? 11 : month[0] - 1), (month[0] - 1 < 0 ? year[0] - 1 : year[0]), $(".calendar-wrap"));

                    if (that.$input.attr("data-cascade") === "true" && that.config.area) {
                        that._bindEvent();
                    }

                    $(".month-next").show();
                }

                that.selectDate();
            });

        },

        /**
         *  设置 calendar 位置
         */
        offsets: function () {
            var $input = this.$input,
                offset = $input.offset(),
                left = offset.left,
                top = offset.top + $input.outerHeight() + 1;

            return { left: left, top: top };
        },

        inputBlur: function (settings) {
            $(document).click(function (e) {
                var target = $(e.target);
                if (!target.hasClass(settings.inputClass) && !target.parents().hasClass(settings.pageWrap)) {
                    $('.calendar-wrap').detach();
                }
            });
        },

        /**
         *  对外提供可扩展 calendar 对象属性、方法的接口
         *  @param {Object} 
         */
        extend: function (object) {
            var fn = Calendar.prototype;

            for (var i in object) {
                fn[i] = object[i];
            }

        }
    };

    //var pandora = {};

    pandora.calendar = new Calendar();

    pandora.calendar.defaults = {
        input: '.calendar',
        inputClass: 'calendar',
        pageWrap: 'calendar-wrap',
        weeks: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'],
        showWeek: false,
        area: false,
        hour: 0,
        fatalism: 20,
        mos: 12
    };

    pandora.calendar.template = '<div id="calendar0" class="calendar-month">' +
                                '        <div  class="calendar-title">' +
                                '            <a class="month-prev" href="javascript:;" style="display:none"></a><span>{monthPrev}</span>' +
                                '        </div>' +
                                '        <dl class="calendar-day">' +
                                '            <dt class="weekend">日</dt>' +
                                '            <dt>一</dt>' +
                                '            <dt>二</dt>' +
                                '            <dt>三</dt>' +
                                '            <dt>四</dt>' +
                                '            <dt>五</dt>' +
                                '            <dt class="weekend">六</dt>' +
                                '            <dd>' +
                                '                {PrevMonthContent}' +
                                '            </dd>' +
                                '        </dl>' +
                                '    </div>' +
                                '    <div id="calendar1" class="calendar-month">' +
                                '        <div class="calendar-title">' +
                                '            <a class="month-next" href="javascript:;"></a><span>{monthNext}</span>' +
                                '        </div>' +
                                '        <dl class="calendar-day">' +
                                '            <dt class="weekend">日</dt>' +
                                '            <dt>一</dt>' +
                                '            <dt>二</dt>' +
                                '            <dt>三</dt>' +
                                '            <dt>四</dt>' +
                                '            <dt>五</dt>' +
                                '            <dt class="weekend">六</dt>' +
                                '            <dd>' +
                                '                {NextMonthContent}' +
                                '            </dd>' +
                                '        </dl>' +
                                '</div>';

    global.pandora.calendar = pandora.calendar;
}(this, jQuery, this.pandora || {}));