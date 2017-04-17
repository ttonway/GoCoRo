var express = require('express');
var orm = require("orm");
var moment = require('moment');
var router = express.Router();

function getProfileInfo(req, res, next, callback) {
    var RoastData = req.models.roast_data;
    var RoastProfile = req.models.roast_profile;

    RoastProfile.get(req.params.id, function (err, profile) {
        if (err) {
            if (err.code == orm.ErrorCodes.NOT_FOUND) {
                res.status(404).send("Profile not found");
                return;
            } else {
                return next(err);
            }
        }

        RoastData.find({'profile_id': profile.id}, function (err, plotDatas) {
            if (err) return next(err);

            callback(profile, plotDatas);
        });
    });
}


function formatTime(seconds) {
    if (seconds === 0) {
        return '0';
    }

    var hour = parseInt(seconds / 3600);
    var min = parseInt(seconds / 60) - hour * 60;
    var sec = seconds - min * 60 - hour * 3600;

    var str = "";
    if (hour > 0) {
        str += hour + "h";
    }
    if (min > 0) {
        str += min + "min";
    }
    if (sec > 0) {
        str += sec + "sec";
    }
    return str;
}

router.get('/:id/plots', function (req, res, next) {
    var RoastData = req.models.roast_data;

    getProfileInfo(req, res, next, function (profile, plotDatas) {
        // var result = profile.serialize();
        // result.plotDatas = plotDatas.map(function (d) {
        //     return d.serialize();
        // });
        // res.send(result);

        var event = {
            label: 'event',
            yAxisID: 'temperature-axis',
            borderWidth: 0,
            borderColor: 'transparent',
            backgroundColor: 'transparent',
            pointBorderColor: '#fff100',
            pointBorderWidth: 2,
            pointBackgroundColor: '#e50014',
            pointRadius: 5,
            pointStyle: 'circle',
            steppedLine: true,
            data: []
        };
        var temperature = {
            label: 'temperature',
            yAxisID: 'temperature-axis',
            borderColor: '#fff100',
            borderWidth: 2,
            backgroundColor: 'transparent',
            pointRadius: 0,
            steppedLine: true,
            data: []
        };
        var fire = {
            label: 'fire',
            yAxisID: 'fire-axis',
            borderColor: 'rgba(117, 0, 27, 0.7)',
            borderWidth: 1,
            backgroundColor: 'rgba(117, 0, 27, 0.7)',
            pointRadius: 0,
            steppedLine: true,
            data: []
        };
        var preheatStatus = {
            label: 'preheat',
            yAxisID: 'temperature-axis',
            borderColor: 'transparent',
            borderWidth: 0,
            backgroundColor: 'rgba(238, 153, 153, 0.7)',
            pointRadius: 0,
            steppedLine: true,
            data: []
        };
        var roastStatus = {
            label: 'roast',
            yAxisID: 'temperature-axis',
            borderColor: 'transparent',
            borderWidth: 0,
            backgroundColor: 'rgba(251, 73, 72, 0.7)',
            pointRadius: 0,
            steppedLine: true,
            data: []
        };
        var coolStatus = {
            label: 'cool',
            yAxisID: 'temperature-axis',
            borderColor: 'transparent',
            borderWidth: 0,
            backgroundColor: 'rgba(130, 104, 162, 0.7)',
            pointRadius: 0,
            spanGaps: true,
            steppedLine: true,
            data: []
        };


        var length = plotDatas.length;
        var lastStatus = RoastData.STATUS_UNKNOWN;
        plotDatas.forEach(function (entry, index, array) {
            var val = {x: entry.time, y: entry.temperature, f: entry.fire, e: entry.getEventName()};
            temperature.data.push(val);
            fire.data.push({x: entry.time, y: entry.fire});

            if (entry.status != lastStatus || index === length - 1) {
                lastStatus = entry.status;
                if (entry.status === RoastData.STATUS_PREHEATING) {
                    preheatStatus.data.push({x: entry.time, y: 250});
                } else if (entry.status === RoastData.STATUS_ROASTING) {
                    preheatStatus.data.push({x: entry.time, y: 0});
                    roastStatus.data.push({x: entry.time, y: 250});
                } else if (entry.status === RoastData.STATUS_COOLING) {
                    roastStatus.data.push({x: entry.time, y: 0});
                    coolStatus.data.push({x: entry.time, y: 250});
                }
            }

            if (entry.event) {
                event.data.push(val);
            }
        });


        var maxX = length > 0 ? plotDatas[length - 1].time : 0;
        var data = {
            datasets: [event, temperature, fire, preheatStatus, roastStatus, coolStatus],
            maxX: maxX,
            preHeatTime: profile.preHeatTime,
            roastTime: profile.roastTime,
            coolTime: profile.coolTime
        };
        res.send(data);
    });
});

router.get('/:id/chart', function (req, res, next) {
    var RoastData = req.models.roast_data;

    getProfileInfo(req, res, next, function (orm_profile, orm_plotDatas) {

        var profile = orm_profile.serialize();
        profile.plotDatas = orm_plotDatas.map(function (d) {
            var obj = d.serialize();
            obj.eventName = d.getEventName();
            return obj;
        });

        profile.aspectRatio = profile.startWeight <= 0 ? '-' : new Number((1 - profile.endWeight / profile.startWeight) * 100).toFixed(2) + '%'

        var roastTime = profile.roastTime;
        var coolTime = profile.coolTime;
        var length = profile.plotDatas.length;

        if (profile.complete && length > 0) {
            var last = profile.plotDatas[length - 1];
            last.complete = true;
        }

        profile.preheatData = [];
        profile.roastData = [];
        profile.coolData = [];
        var lastFire = -1;
        profile.plotDatas.forEach(function (data, index, array) {
            if (data.status === RoastData.STATUS_ROASTING) {
                if (lastFire != -1 && lastFire != data.fire) {
                    data.fireChanged = true;
                }
                lastFire = data.fire;
            }

            data.allevents = [];
            if (data.eventName) data.allevents.push(data.eventName);
            if (data.fireChanged) data.allevents.push('火力设置');
            if (data.manualCool) data.allevents.push('冷却设置');
            if (data.complete) data.allevents.push('冷却停止');

            var hasEvents = data.event || data.fireChanged || data.isManualCool || data.complete;
            var time = data.time;
            if (data.status === RoastData.STATUS_PREHEATING) {
                hasEvents = false;
            } else if (data.status === RoastData.STATUS_ROASTING) {
                time = time - roastTime;
            } else if (data.status === RoastData.STATUS_COOLING) {
                time = time - coolTime;
            }
            data.showTime = formatTime(time);

            if (hasEvents || (time > 0 && time % 60 == 0)) {
                // add this data
            } else {
                return;
            }

            if (data.status === RoastData.STATUS_PREHEATING) {
                profile.preheatData.push(data);
            } else if (data.status === RoastData.STATUS_ROASTING) {
                profile.roastData.push(data);
            } else if (data.status === RoastData.STATUS_COOLING) {
                profile.coolData.push(data);
            }
        });

        profile.moment = moment;
        res.render('profile', profile);
    });
});


router.post('/upload', function (req, res, next) {
    var RoastData = req.models.roast_data;
    var RoastProfile = req.models.roast_profile;

    var body = req.body;
    var uuid = body.uuid;
    var values = {
        deviceId: body.deviceId,
        people: body.people,
        beanCountry: body.beanCountry,
        beanName: body.beanName,
        startTime: new Date(body.startTime),
        endTime: new Date(body.endTime),
        startWeight: body.startWeight,
        endWeight: body.endWeight,
        envTemperature: body.envTemperature,
        startFire: body.startFire,
        startDruation: body.startDruation,
        coolTemperature: body.coolTemperature,
        preHeatTime: body.preHeatTime,
        roastTime: body.roastTime,
        coolTime: body.coolTime
    };
    var result = {};

    if (typeof uuid !== "string") {
        return next(new Error('uuid is required'));
    }

    RoastProfile.exists({uuid: uuid}, function (err, exists) {
        if (err) return next(err);

        if (exists) {
            RoastProfile.find({uuid: uuid}, function (err, profiles) {
                if (err) return next(err);

                var profile = profiles[0];
                Object.assign(profile, values);
                profile.save(function (err) {
                    if (err) return next(err);

                    result.sid = profile.id;
                    res.send(result);
                });

                if (Array.isArray(body.plotDatas)) {

                    // only consider update & create
                    RoastData.find({'profile_id': profile.id}, function (err, plotDatas) {
                        var map = {};
                        plotDatas.forEach(function (data, index, array) {
                            map[data.time] = data;
                        });


                        body.plotDatas.forEach(function (data, index, array) {
                            var mdata = map[data.time];
                            if (mdata) {
                                if (data.event == mdata.event) {
                                    // nothing to do
                                } else {
                                    mdata.event = data.event;
                                    console.log('update roast data', data);
                                    mdata.save(function (err) {
                                        if (err) console.log("save roast_data fail.", err);
                                    });
                                }
                            } else {
                                data.profile_id = profile.id;
                                console.log('create roast data', data);
                                RoastData.create(data, function (err, item) {
                                    if (err) console.log('create plotdata fail.', err);
                                });
                            }
                        });
                    });
                }
            });

        } else {
            values.uuid = uuid;
            RoastProfile.create(values, function (err, profile) {
                if (err) return next(err);

                result.sid = profile.id;

                if (Array.isArray(body.plotDatas)) {
                    body.plotDatas.forEach(function (data, index, array) {
                        data.profile_id = profile.id;
                    });
                    RoastData.create(body.plotDatas, function (err, items) {
                        if (err) return next(err);

                        res.send(result);
                    });
                } else {
                    res.send(result);
                }
            });
        }
    });
});

module.exports = router;