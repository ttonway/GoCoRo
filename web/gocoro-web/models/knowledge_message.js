module.exports = function (orm, db) {
    var Message = db.define('knowledge_message', {
            title: {type: 'text', required: true},
            posterUrl: {type: 'text'},
            description: {type: 'text'},
            url: {type: 'text', required: true},
            createdAt: {type: 'date', required: true, time: true}
        },
        {
            methods: {
                serialize: function () {
                    return {
                        title: this.title,
                        posterUrl: this.posterUrl,
                        description: this.description,
                        url: this.url,
                        createdAt: this.createdAt.valueOf()
                    }
                }
            }
        });
    return Message;
};
