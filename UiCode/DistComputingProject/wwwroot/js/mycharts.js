
var ctx = document.getElementById('azChart');
var az0 = document.getElementById('az0').getAttribute('data');
var az60 = document.getElementById('az60').getAttribute('data');
var az70 = document.getElementById('az70').getAttribute('data');
var az80 = document.getElementById('az80').getAttribute('data');
var az90 = document.getElementById('az90').getAttribute('data');
var azChart = new Chart(ctx, {
	type: 'bar',
	data: {
		labels: ['< 60%', '60-70%', '70-80%', '80-90%', '> 90%'],
		datasets: [{
            label: '# of tags',
            data: [az0, az60, az70, az80, az90],
			backgroundColor: [
			    'rgba(255, 99, 132, 0.2)',
			    'rgba(54, 162, 235, 0.2)',
			    'rgba(255, 206, 86, 0.2)',
			    'rgba(75, 192, 192, 0.2)',
			    'rgba(153, 102, 255, 0.2)',
			    // 'rgba(255, 159, 64, 0.2)'
			],
			borderColor: [
			    'rgba(255, 99, 132, 1)',
			    'rgba(54, 162, 235, 1)',
			    'rgba(255, 206, 86, 1)',
			    'rgba(75, 192, 192, 1)',
			    'rgba(153, 102, 255, 1)',
			    // 'rgba(255, 159, 64, 1)'
			],
			borderWidth: 1
		}]
	},
	options: {
		scales: {
			yAxes: [{
			    ticks: {
			        beginAtZero: true
			    }
			}]
		}
	}
});

var ctx1 = document.getElementById('aiChart');
var ai0 = document.getElementById('ai0').getAttribute('data');
var ai60 = document.getElementById('ai60').getAttribute('data');
var ai70 = document.getElementById('ai70').getAttribute('data');
var ai80 = document.getElementById('ai80').getAttribute('data');
var ai90 = document.getElementById('ai90').getAttribute('data');
var myChart1 = new Chart(ctx1, {
    type: 'bar',
    data: {
        labels: ['< 60%', '60-70%', '70-80%', '80-90%', '> 90%'],
        datasets: [{
            label: '# of tags',
            data: [ai0, ai60, ai70, ai80, ai90],
            backgroundColor: [
                'rgba(255, 99, 132, 0.2)',
                'rgba(54, 162, 235, 0.2)',
                'rgba(255, 206, 86, 0.2)',
                'rgba(75, 192, 192, 0.2)',
                'rgba(153, 102, 255, 0.2)',
                // 'rgba(255, 159, 64, 0.2)'
            ],
            borderColor: [
                'rgba(255, 99, 132, 1)',
                'rgba(54, 162, 235, 1)',
                'rgba(255, 206, 86, 1)',
                'rgba(75, 192, 192, 1)',
                'rgba(153, 102, 255, 1)',
                // 'rgba(255, 159, 64, 1)'
            ],
            borderWidth: 1
        }]
    },
    options: {
        scales: {
            yAxes: [{
                ticks: {
                    beginAtZero: true
                }
            }]
        }
    }
});