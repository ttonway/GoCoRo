module.exports = function (orm, db) {
    var RoastProfile = require('./roast_profile')(orm, db);

    var Cupping = db.define('cupping', {
            uuid: {type: 'text', required: true, unique: true},

            name: {type: 'text'},
            comment: {type: 'text'},
            time: {type: 'date', time: true},

            score1: {type: 'number'},
            score2: {type: 'number'},
            score3: {type: 'number'},
            score4: {type: 'number'},
            score5: {type: 'number'},
            score6: {type: 'number'},
            score7: {type: 'number'},
            score8: {type: 'number'},
            score9: {type: 'number'},
            score10: {type: 'number'}
        },
        {
            methods: {
                serialize: function () {
                    return {
                        uuid: this.uuid,
                        name: this.name,
                        comment: this.comment,
                        time: this.time,
                        score1: this.score1,
                        score2: this.score2,
                        score3: this.score3,
                        score4: this.score4,
                        score5: this.score5,
                        score6: this.score6,
                        score7: this.score7,
                        score8: this.score8,
                        score9: this.score9,
                        score10: this.score10
                    }
                },
                getTotalScore: function() {
                    return this.score1 + this.score2 + this.score3 + this.score4 + this.score5 +
                        this.score6 + this.score7 + this.score8 + this.score9 + this.score10;
                }
            }
        });

    Cupping.hasOne("profile", RoastProfile);

    return Cupping;
};
