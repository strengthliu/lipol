(function($) {
  'use strict';
  $(function() {
    if ($("#chart-activity").length) {
      var areaData = {
        labels: ["1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1"],
        datasets: [{
            data: [60, 63, 68, 53, 52, 53, 42, 47, 45, 42, 49, 42, 48, 46, 41, 45],
            backgroundColor: [
              '#D6EEF3'
            ],
            borderColor: [
              '#1DBFD3'
            ],
            borderWidth: 2,
            fill: 'origin',
          },
          {
            data: [75, 93, 89, 93, 105, 93, 82, 89, 95, 111, 93, 85, 75, 96, 91, 85],
            backgroundColor: [
              '#ffdee0'
            ],
            borderColor: [
              '#ff5161'
            ],
            borderWidth: 2,
            fill: 'origin',
          }
        ]
      };
      var areaOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          filler: {
            propagate: false
          }
        },
        scales: {
          xAxes: [{
            display: false,
            gridLines: {
              lineWidth: 0,
              color: "rgba(255,255,255,0)"
            }
          }],
          yAxes: [{
            display: false,
            ticks: {
              display: false,
              autoSkip: false,
              maxRotation: 0,
              stepSize: 15,
              min: 0
            }
          }]
        },
        legend: {
          display: false
        },
        tooltips: {
          enabled: true
        },
        elements: {
          line: {
            tension: 0
          },
          point: {
            radius: 0
          }
        }
      }
      var activityChartCanvas = $("#chart-activity").get(0).getContext("2d");
      var activityChart = new Chart(activityChartCanvas, {
        type: 'line',
        data: areaData,
        options: areaOptions
      });
    }
    if ($("#star-rating").length) {
      $('#star-rating').barrating({
        theme: 'fontawesome-stars',
        showSelectedRating: false,
        initialRating: '4',
      });
    }
    if ($("#purchase-chart").length) {
      $("#purchase-chart").sparkline('html', {
        enableTagOptions: true,
        type: 'line',
        width: '100%',
        height: '50',
        lineColor: '#00b9b0',
        fillColor: 'rgba(0, 185, 176,0.4)',
        lineWidth: 2,
        chartRangeMin: 0,
        spotColor: false,
        minSpotColor: false,
        maxSpotColor: false
      });
    }
    if ($("#time-chart").length) {
      $("#time-chart").sparkline('html', {
        enableTagOptions: true,
        type: 'line',
        width: '100%',
        height: '50',
        fillColor: '#8dc799',
        lineColor: '#46c35f',
        lineWidth: 2,
        chartRangeMin: 0,
        spotColor: false,
        minSpotColor: false,
        maxSpotColor: false
      });
    }
    if ($("#revenueCircle1").length) {
      var bar = new ProgressBar.Circle(revenueCircle1, {
        color: '#000',
        // This has to be the same size as the maximum width to
        // prevent clipping
        strokeWidth: 6,
        trailWidth: 6,
        easing: 'easeInOut',
        duration: 1400,
        text: {
          autoStyleContainer: false
        },
        from: {
          color: '#d6d6d7',
          width: 6
        },
        to: {
          color: '#f90000',
          width: 6
        },
        // Set default step function for all animate calls
        step: function(state, circle) {
          circle.path.setAttribute('stroke', state.color);
          circle.path.setAttribute('stroke-width', state.width);

          var value = Math.round(circle.value() * 100);
          if (value === 0) {
            circle.setText('');
          } else {
            circle.setText('');
          }

        }
      });

      bar.text.style.fontSize = '1.5rem';
      bar.animate(.65); // Number from 0.0 to 1.0
    }
    if ($("#revenueCircle2").length) {
      var bar = new ProgressBar.Circle(revenueCircle2, {
        color: '#000',
        // This has to be the same size as the maximum width to
        // prevent clipping
        strokeWidth: 6,
        trailWidth: 6,
        easing: 'easeInOut',
        duration: 1400,
        text: {
          autoStyleContainer: false
        },
        from: {
          color: '#d6d6d7',
          width: 6
        },
        to: {
          color: '#ff9f00',
          width: 6
        },
        // Set default step function for all animate calls
        step: function(state, circle) {
          circle.path.setAttribute('stroke', state.color);
          circle.path.setAttribute('stroke-width', state.width);

          var value = Math.round(circle.value() * 100);
          if (value === 0) {
            circle.setText('');
          } else {
            circle.setText('');
          }

        }
      });

      bar.text.style.fontSize = '1.5rem';
      bar.animate(.80); // Number from 0.0 to 1.0
    }
    if ($("#revenueCircle3").length) {
      var bar = new ProgressBar.Circle(revenueCircle3, {
        color: '#000',
        // This has to be the same size as the maximum width to
        // prevent clipping
        strokeWidth: 16,
        trailWidth: 16,
        easing: 'easeInOut',
        duration: 1400,
        from: {
          color: '#e9ebef',
          width: 16
        },
        to: {
          color: '#ffa000',
          width: 16
        },
        // Set default step function for all animate calls
        step: function(state, circle) {
          circle.path.setAttribute('stroke', state.color);
          circle.path.setAttribute('stroke-width', state.width);

          var value = Math.round(circle.value() * 100);
          if (value === 0) {
            circle.setText('');
          } else {
            circle.setText('');
          }

        }
      });

      bar.text.style.fontSize = '1.5rem';
      bar.animate(.63); // Number from 0.0 to 1.0
    }
    if ($("#areaChart_1").length) {
      var areaChartCanvas = $("#areaChart_1").get(0).getContext("2d");
      var areaChart = new Chart(areaChartCanvas, {
        type: 'line',
        data: {
          labels: ["2013", "2014", "2015", "2016", "2017"],
          datasets: [{
            label: '# of Votes',
            data: [0, 9, 1, 4, 2, 0],
            backgroundColor: [
              'rgba(244,162,0,0.2)'
            ],
            borderColor: [
              'rgb(244,162,0)'
            ],
            borderWidth: 1,
            fill: true, // 3: no fill
          }]
        },
        options: {
          maintainAspectRatio: false,
          ticks: {
            beginAtZero: true
          },
          tooltips: {
            enabled: false
          },
          elements: {
            line: {
              tension: 0
            }
          },
          legend: {
            display: false
          },
          scales: {
            xAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }],
            yAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }]
          }
        }
      });
    }
    if ($("#areaChart_2").length) {
      var areaChartCanvas = $("#areaChart_2").get(0).getContext("2d");
      var areaChart = new Chart(areaChartCanvas, {
        type: 'line',
        data: {
          labels: ["2013", "2014", "2015", "2016", "2017"],
          datasets: [{
            label: '# of Votes',
            data: [0, 2, 9, 1, 5, 0],
            backgroundColor: [
              'rgba(73,147,222,0.2)'
            ],
            borderColor: [
              'rgba(73,147,222,1)'
            ],
            borderWidth: 1,
            fill: true, // 3: no fill
          }]
        },
        options: {
          maintainAspectRatio: false,
          ticks: {
            beginAtZero: true
          },
          tooltips: {
            enabled: false
          },
          elements: {
            line: {
              tension: 0
            }
          },
          legend: {
            display: false
          },
          scales: {
            xAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }],
            yAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }]
          }
        }
      });
    }
    if ($("#areaChart_3").length) {
      var areaChartCanvas = $("#areaChart_3").get(0).getContext("2d");
      var areaChart = new Chart(areaChartCanvas, {
        type: 'line',
        data: {
          labels: ["2013", "2014", "2015", "2016", "2017"],
          datasets: [{
            label: '# of Votes',
            data: [0, 7, 4, 9, 5, 0],
            backgroundColor: [
              'rgba(42,199,177,0.2)'
            ],
            borderColor: [
              'rgba(42,199,177,1)'
            ],
            borderWidth: 1,
            fill: true, // 3: no fill
          }]
        },
        options: {
          maintainAspectRatio: false,
          ticks: {
            beginAtZero: true
          },
          tooltips: {
            enabled: false
          },
          elements: {
            line: {
              tension: 0
            }
          },
          legend: {
            display: false
          },
          scales: {
            xAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }],
            yAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }]
          }
        }
      });
    }
    if ($("#areaChart_4").length) {
      var areaChartCanvas = $("#areaChart_4").get(0).getContext("2d");
      var areaChart = new Chart(areaChartCanvas, {
        type: 'line',
        data: {
          labels: ["2013", "2014", "2015", "2016", "2017"],
          datasets: [{
            label: '# of Votes',
            data: [0, 9, 2, 7, 1, 0],
            backgroundColor: [
              'rgba(248,0,130,0.2)'
            ],
            borderColor: [
              'rgba(248,0,130,1)'
            ],
            borderWidth: 1,
            fill: true, // 3: no fill
          }]
        },
        options: {
          maintainAspectRatio: false,
          ticks: {
            beginAtZero: true
          },
          tooltips: {
            enabled: false
          },
          elements: {
            line: {
              tension: 0
            }
          },
          legend: {
            display: false
          },
          scales: {
            xAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }],
            yAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }]
          }
        }
      });
    }
    if ($("#areaChart_5").length) {
      var areaChartCanvas = $("#areaChart_5").get(0).getContext("2d");
      var areaChart = new Chart(areaChartCanvas, {
        type: 'line',
        data: {
          labels: ["2013", "2014", "2015", "2016", "2017"],
          datasets: [{
            label: '# of Votes',
            data: [0, 2, 8, 3, 8, 0],
            backgroundColor: [
              'rgba(58,132,218,0.2)'
            ],
            borderColor: [
              'rgba(58,132,218,1)'
            ],
            borderWidth: 1,
            fill: true, // 3: no fill
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          ticks: {
            beginAtZero: true
          },
          tooltips: {
            enabled: false
          },
          elements: {
            line: {
              tension: 0
            }
          },
          legend: {
            display: false
          },
          scales: {
            xAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }],
            yAxes: [{
              display: false,
              gridLines: {
                display: false
              }
            }]
          }
        }
      });
    }
  });
})(jQuery);