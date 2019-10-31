$.fn.tagcloud.defaults = {
    size: { start: 10, end: 36, unit: 'pt' },
    color: { start: '#306dcf', end: '#f5004ca8' }
    //color: { start: '#cde', end: '#f52' }
};

$(function () {
    $('#tags a').tagcloud();
});