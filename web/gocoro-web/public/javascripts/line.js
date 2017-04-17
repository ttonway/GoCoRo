$(document).ready(function () {
    function formatTimeInStatus(seconds, data) {
        if (data.coolTime && seconds >= data.coolTime) {
            seconds -= data.coolTime;
        } else if (data.roastTime && seconds >= data.roastTime) {
            seconds -= data.roastTime;
        }

        if (seconds === 0) return '0';

        var hour = parseInt(seconds / 3600);
        var min = parseInt(seconds / 60) - hour * 60;
        var sec = seconds - min * 60 - hour * 3600;

        if (sec < 10) sec = '0' + sec;
        return min + ':' + sec;
    }

    var canvas = $("#canvas");
    var ctx = canvas.get(0).getContext("2d");

    var data = {};
    var chart = new Chart(ctx, {
        type: 'line',
        data: data,
        options: {
            legend: {
                display: false
            },
            scales: {
                xAxes: [{
                    type: 'linear',
                    position: 'bottom',
                    ticks: {
                        callback: function (value, index, values) {
                            return formatTimeInStatus(parseInt(value), data);
                        }
                    }
                }],
                yAxes: [{
                    id: 'temperature-axis',
                    position: 'left',
                    ticks: {
                        max: 250,
                        min: 0,
                        callback: function (value, index, values) {
                            return value + '℃';
                        }
                    }
                }, {
                    id: 'fire-axis',
                    position: 'right',
                    ticks: {
                        max: 10,
                        min: 0,
                        stepSize: 1,
                        callback: function (value, index, values) {
                            return value > 0 && value <= 5 ? '火力' + value : '';
                        }
                    }
                }]
            },
            tooltips: {
                filter: function (item, data) {
                    return item.datasetIndex <= 1;// only show tooltips for events & temperature line
                },
                callbacks: {
                    title: function (items, data) {
                        if (items && items.length > 0) {
                            return formatTimeInStatus(parseInt(items[0].xLabel), data);
                        }
                    },
                    label: function (item, data) {
                        var entry = data.datasets[item.datasetIndex].data[item.index];
                        var str = entry.y + '℃';
                        if (entry.f) {
                            str += '-火力' + entry.f;
                        }
                        if (entry.e) {
                            str += '-' + entry.e;
                        }
                        return str;
                    }
                }
            }
        }
    });

    $.getJSON("plots", function (json) {

        var h = canvas.height();
        var preheatGrad = ctx.createLinearGradient(0, 0, 0, h);
        preheatGrad.addColorStop(0, 'rgba(238, 153, 153, 0.7)');
        preheatGrad.addColorStop(1, 'rgba(249, 222, 222, 0.7)');
        var roastGrad = ctx.createLinearGradient(0, 0, 0, h);
        roastGrad.addColorStop(0, 'rgba(251, 73, 72, 0.7)');
        roastGrad.addColorStop(1, 'rgba(253, 196, 195, 0.7)');
        var coolGrad = ctx.createLinearGradient(0, 0, 0, h);
        coolGrad.addColorStop(0, 'rgba(130, 104, 162, 0.7)');
        coolGrad.addColorStop(1, 'rgba(84, 76, 93, 0.7)');

        json.datasets.forEach(function (dataset) {
            if (dataset.label === 'preheat') {
                dataset.backgroundColor = preheatGrad;
            } else if (dataset.label === 'roast') {
                dataset.backgroundColor = roastGrad;
            } else if (dataset.label === 'cool') {
                dataset.backgroundColor = coolGrad;
            }
        });

        for (var i = 1; i <= 5; i++) {
            var limitLine = {
                label: 'limit-' + i,
                yAxisID: 'fire-axis',
                borderColor: '#2a2630',
                borderWidth: 1,
                backgroundColor: 'transparent',
                pointRadius: 0,
                steppedLine: true,
                data: [{x: 0, y: i}, {x: json.maxX, y: i}]
            }
            json.datasets.push(limitLine);
        }

        $.extend(data, json);
        chart.update();
    });
});