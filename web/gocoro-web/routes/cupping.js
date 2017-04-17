var express = require('express');
var orm = require("orm");
var moment = require('moment');
var router = express.Router();

router.get('/:id/radars', function (req, res, next) {
    var Cupping = req.models.cupping;
    Cupping.get(req.params.id, function (err, cupping) {
        if (err) {
            if (err.code == orm.ErrorCodes.NOT_FOUND) {
                res.status(404).send("Cupping not found");
                return;
            } else {
                return next(err);
            }
        }

        var data = {
            datasets: [
                {
                    label: "cupping",
                    backgroundColor: "rgba(184,63,46,0.7)",
                    borderWidth: 2,
                    borderColor: "#b83f2e",
                    pointRadius: 2,
                    pointBackgroundColor: "#b83f2e",
                    pointBorderColor: "#b83f2e",
                    pointHoverRadius: 4,
                    pointHoverBorderWidth: 2,
                    pointHoverBackgroundColor: "#b83f2e",
                    pointHoverBorderColor: "#fff100",
                    data: [cupping.score1, cupping.score2, cupping.score3, cupping.score4, cupping.score5, cupping.score6, cupping.score7]
                }
            ]
        };

        res.send(data);
    });
});

router.get('/:id/chart', function (req, res, next) {
    var Cupping = req.models.cupping;

    Cupping.get(req.params.id, function (err, orm_cupping) {
        if (err) {
            if (err.code == orm.ErrorCodes.NOT_FOUND) {
                res.send(404, "Cupping not found");
                return
            } else {
                return next(err);
            }
        }

        orm_cupping.getProfile(function (err, orm_profile) {
            if (err && err.code != orm.ErrorCodes.NOT_FOUND) {
                return next(err);
            }


            var cupping = orm_cupping.serialize();
            cupping.totalStore = orm_cupping.getTotalScore();
            cupping.profile = {};
            if (orm_profile) {
                cupping.profile = orm_profile.serialize();
                cupping.profile.id = orm_profile.id;
            }
            cupping.moment = moment;
            res.render('cupping', cupping);
        });
    });
});

router.post('/upload', function (req, res, next) {
    var Cupping = req.models.cupping;
    var RoastProfile = req.models.roast_profile;

    var body = req.body;
    var uuid = body.uuid;
    var values = {
        name: body.name,
        comment: body.comment,
        time: new Date(body.time),
        score1: body.score1,
        score2: body.score2,
        score3: body.score3,
        score4: body.score4,
        score5: body.score5,
        score6: body.score6,
        score7: body.score7,
        score8: body.score8,
        score9: body.score9,
        score10: body.score10
    };
    var result = {};

    if (typeof uuid !== "string") {
        return next(new Error('uuid is required'));
    }

    var profileUuid = body.profile;

    function setProfile(cupping) {
        if (profileUuid) {
            RoastProfile.find({uuid: profileUuid}, function (err, profiles) {
                if (err) {
                    console.log("find profile fail.", err);
                    return;
                }

                if (profiles.length === 1) {
                    cupping.setProfile(profiles[0], function (err) {
                        if (err) console.log("set cupping's profile fail.", err);
                    });
                } else {
                    console.log("set cupping's profile fail. NOT FOUND.");
                }
            })
        } else {
            if (cupping.profile) {
                cupping.removeProfile();
            }
        }
    }

    Cupping.exists({uuid: uuid}, function (err, exists) {
        if (err) return next(err);

        if (exists) {
            Cupping.find({uuid: uuid}, function (err, cuppings) {
                if (err) return next(err);

                var cupping = cuppings[0];
                Object.assign(cupping, values);
                setProfile(cupping);

                cupping.save(function (err) {
                    if (err) return next(err);

                    result.sid = cupping.id;
                    res.send(result);
                });
            });

        } else {
            values.uuid = uuid;
            Cupping.create(values, function (err, cupping) {
                if (err) return next(err);

                setProfile(cupping);

                result.sid = cupping.id;
                res.send(result);
            });
        }
    });
});

module.exports = router;