var orm = require("orm");

function setup(db, models, cb) {
    models.knowledge_message = require('./knowledge_message')(orm, db);
    models.roast_data = require('./roast_data')(orm, db);
    models.roast_profile = require('./roast_profile')(orm, db);
    models.cupping = require('./cupping')(orm, db);

    db.sync(function (err) {
        if (err) throw err;

        // models.knowledge_message.create({
        //     id: 1,
        //     title: "GoCoRo ProAPP 自動烘焙試驗分享",
        //     posterUrl: "http://mmbiz.qpic.cn/mmbiz_jpg/Gz17wClbj4ag9Oiaq0wbPdZiayiayDNaz6TuZEjxz8fungChVxW3pXHy100xlDCXohVkFbooZquLdicQ8I5fpLvX0A/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1",
        //     description: "GoCoRo 數位咖啡烘豆機自從發表以來, 不斷的有朋友詢問有關於APP與烘焙曲線紀錄方面的問題, 我們的設計團隊了解了現有市場上的烘焙曲線, 發現大部分的烘焙曲線 “僅僅”只是份曲線圖表, 我個人不禁要思考, 難道一份漂亮的曲線圖就代表好的咖啡風味? ",
        //     url: "http://mp.weixin.qq.com/s/8iR1VxqCIhhxLd3HB6VOPw",
        //     createdAt: new Date()
        // }, function (err) {
        //     if (err) throw err;
        // });
        // models.knowledge_message.create({
        //     id: 2,
        //     title: "全自動化咖啡烘焙的時代即將來臨!! 自己的咖啡風格,自己決定!!",
        //     posterUrl: "http://mmbiz.qpic.cn/mmbiz_jpg/Gz17wClbj4ag9Oiaq0wbPdZiayiayDNaz6TRLCCQj6BRIDCWC8GRzYzl8d4Gr3N34Ehx8qq9DA4ubDJibalnkV161g/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1",
        //     description: "GoCoRo 從開始計劃發展APP控制以來，一直得到各界朋友的關心上架日期，目前的規劃在2017年3月25日先發佈安卓的版本。",
        //     url: "http://mp.weixin.qq.com/s/ic7NMoZxpaRjwYk5y2jUtw",
        //     createdAt: new Date(0)
        // }, function (err) {
        //     if (err) throw err;
        // });
    });

    return cb(null, db);
}

module.exports = function (db, models, cb) {
    db.settings.set('instance.returnAllErrors', true);
    setup(db, models, cb);
};

