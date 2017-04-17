module.exports = function (orm, db) {

    var RoastProfile = db.define('roast_profile', {
            uuid: {type: 'text', required: true, unique: true},

            deviceId: {type: 'text'},

            people: {type: 'text'},
            beanCountry: {type: 'text'},
            beanName: {type: 'text'},

            startTime: {type: 'date', time: true},
            endTime: {type: 'date', time: true},
            startWeight: {type: 'integer'},
            endWeight: {type: 'integer'},
            envTemperature: {type: 'integer'},
            startFire: {type: 'integer'},
            startDruation: {type: 'integer'},
            coolTemperature: {type: 'integer'},

            preHeatTime: {type: 'integer'},
            roastTime: {type: 'integer'},
            coolTime: {type: 'integer'},
            complete: {type: 'boolean'}
        },
        {
            autoFetch: true,
            methods: {
                serialize: function () {
                    return {
                        uuid: this.uuid,
                        people: this.people,
                        beanCountry: this.beanCountry,
                        beanName: this.beanName,
                        startTime: this.startTime,
                        endTime: this.endTime,
                        startWeight: this.startWeight,
                        endWeight: this.endWeight,
                        envTemperature: this.envTemperature,
                        startFire: this.startFire,
                        startDruation: this.startDruation,
                        coolTemperature: this.coolTemperature,
                        preHeatTime: this.preHeatTime,
                        roastTime: this.roastTime,
                        coolTime: this.coolTime,
                        complete: this.complete,
                        fullName: this.getFullName()
                    }
                },
                getFullName: function () {
                    return this.beanCountry + '-' + this.beanName;
                }
            }
        });

    return RoastProfile;
};
