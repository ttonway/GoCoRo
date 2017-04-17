var express = require('express');
var router = express.Router();

router.get('/list', function (req, res, next) {
    req.models.knowledge_message.find().order('-createdAt').all(function (err, knowledges) {
        if (err) return next(err);

        var items = knowledges.map(function (m) {
            return m.serialize();
        });

        res.send(items);
    });
});

module.exports = router;