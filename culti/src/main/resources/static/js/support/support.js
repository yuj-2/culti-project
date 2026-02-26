(function($) {
    $(function() {
        var $window = $(window),
            $body = $('body');

        // 페이지 로드 시 애니메이션
        $window.on('load', function() {
            window.setTimeout(function() {
                $body.removeClass('is-preload');
            }, 100);
        });

        // 문의하기 버튼 클릭 로그 (필요 시 확장)
        $('.button').on('click', function() {
            console.log("Support action triggered.");
        });
    });
})(jQuery);