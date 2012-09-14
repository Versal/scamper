exports.fast = function(req, res) {
    res.send('<h1>slept for 0 ms</h1>');
}

exports.medium = function(req, res) {
    setTimeout(function () {
        res.send('<h1>slept for 150 ms</h1>');
    }, 150);
}

exports.slow = function(req, res) {
    setTimeout(function () {
        res.send('<h1>slept for 300 ms</h1>');
    }, 300);
}

