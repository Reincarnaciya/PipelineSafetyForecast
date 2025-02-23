document.addEventListener('DOMContentLoaded', function () {
    const startTime = document.querySelector('meta[name="start-time"]').content;
    const endTime = document.querySelector('meta[name="end-time"]').content;

    const ctx = document.getElementById('chart').getContext('2d');
    const labels = [];
    const data = [];

    // Здесь нужно будет получить данные predictions из вашего бэкенда
    // Пример:
    const predictions = [
        {timestamp: new Date(startTime), leakProbability: 0.5},
        {timestamp: new Date(endTime), leakProbability: 0.8}
    ];


    predictions.forEach(prediction => {
        labels.push(prediction.timestamp.toLocaleString());
        data.push(prediction.leakProbability);
    });

    const chartData = {
        labels: labels,
        datasets: [{
            label: 'Вероятность утечки',
            data: data,
            borderColor: 'rgb(255, 99, 132)',
            tension: 0.1
        }]
    };

    new Chart(ctx, {
        type: 'line',
        data: chartData,
        options: {responsive: true}
    });
});