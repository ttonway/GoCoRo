$(document).ready(function () {
    var canvas = $("#canvas");
    var ctx = canvas.get(0).getContext("2d");

    var data = {
        labels: ["香气", "风味", "馀韵", "酸度", "口感", "平衡", "整体性"]
    };
    var radarChart = new Chart(ctx, {
        type: 'radar',
        data: data,
        options: {
            legend: {
                display: false
            },
            scale: {
                ticks: {
                    max: 10,
                    min: 5,
                    stepSize: 1
                }
            },
            tooltips: {
                callbacks: {
                    label: function (item, data) {
                        return item.yLabel + '分';
                    }
                }
            }
        }
    });

    $.getJSON("radars", function (json) {

        $.extend(data, json);
        radarChart.update();
    });
});