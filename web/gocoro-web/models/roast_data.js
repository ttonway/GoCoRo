module.exports = function (orm, db) {
    // var RoastProfile = require('./roast_profile')(orm, db);

    var RoastData = db.define('roast_data', {
            profile_id: {type: 'integer', key: true},

            time: {type: 'integer', key: true},
            fire: {type: 'integer'},
            temperature: {type: 'integer'},
            status: {type: 'integer'},
            event: {type: 'text'},
            manualCool: {type: 'boolean'}
        },
        {
            methods: {
                serialize: function () {
                    return {
                        time: this.time,
                        fire: this.fire,
                        temperature: this.temperature,
                        status: this.status,
                        event: this.event
                    }
                },
                getEventName: function() {
                    if (RoastData.EVENT_BURST1_START == this.event) {
                        return '一爆开始';
                    } else if (RoastData.EVENT_BURST1 == this.event) {
                        return '二爆开始';
                    } else if (RoastData.EVENT_BURST2_START == this.event) {
                        return '一爆密集';
                    } else if (RoastData.EVENT_BURST2 == this.event) {
                        return '二爆密集';
                    }
                }
            }
        });

    // RoastData.hasOne("profile", RoastProfile, {reverse: "plotDatas"});

    RoastData.STATUS_UNKNOWN = -1;
    RoastData.STATUS_IDLE = 0;
    RoastData.STATUS_PREHEATING = 1;
    RoastData.STATUS_ROASTING = 2;
    RoastData.STATUS_COOLING = 3;

    RoastData.EVENT_BURST1_START = "BURST1_START";//一爆開始
    RoastData.EVENT_BURST1 = "BURST1";//一爆密集
    RoastData.EVENT_BURST2_START = "BURST2_START";//二爆開始
    RoastData.EVENT_BURST2 = "BURST2";//二爆密集

    return RoastData;
};