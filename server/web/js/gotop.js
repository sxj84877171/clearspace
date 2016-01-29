$(document).ready(function () {
    b();
    $('#gotop').click(function () {
        $('body,html').animate({scrollTop: 0}, 300);
    });
//    if(navigator.userAgent.indexOf("MSIE")>0){
//        layer.alert('哎哟~~发现你在使用IE浏览器哦~~，本页面在IE下显示效果不佳，推荐使用Chrome~~',-1);
//    }

});
function b() {
    h = 200;
    t = $(document).scrollTop();
    if (t > h) {
        $('#gotop').fadeIn();
    } else {
        $('#gotop').fadeOut();
    }
}
$(window).scroll(function (e) {
    b();
});