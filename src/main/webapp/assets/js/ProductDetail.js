$(document).ready(function () {
    /* Global variables */
    const productId = $('#productId').val();
    const contextPath = $('#contextPath').val();

    let currentPage = 1;
    let currentRating = '';
    let currentSort = 'newest';
    let isLoading = false;

    let lastMessage = '';
    let lastMessageTime = 0;

    function showMessage(message) {
        const now = Date.now();

        if (message === lastMessage && now - lastMessageTime < 1000) {
            return;
        }

        lastMessage = message;
        lastMessageTime = now;

        alert(message);
    }

    function updateCartPreview(data) {
        if (window.renderCartPreviewFromSummary) {
            window.renderCartPreviewFromSummary(data, true);
        }
    }

    function showCartAddedToast() {
        const productName = $('.product-title').text().trim() || 'Sản phẩm';
        let $toast = $('#cartAddedToast');

        if (!$toast.length) {
            $toast = $(`
                <div class="cart-action-toast" id="cartAddedToast" hidden>
                    <strong>Thông báo</strong>
                    <span></span>
                    <button type="button" aria-label="Đóng thông báo">
                        <i class="fa-solid fa-xmark"></i>
                    </button>
                </div>
            `);

            $('body').append($toast);

            $toast.find('button').on('click', function () {
                $toast.prop('hidden', true);
                clearTimeout($toast.data('timer'));
            });
        }

        $toast.find('span').text(`${productName} đã được thêm vào giỏ hàng của bạn`);
        $toast.prop('hidden', false);

        clearTimeout($toast.data('timer'));
        const timer = setTimeout(() => {
            $toast.prop('hidden', true);
        }, 3500);
        $toast.data('timer', timer);
    }

    $('.buy-form').on('submit', function (event) {
        const submitter = event.originalEvent?.submitter;

        if (submitter && submitter.name === 'buyNow') {
            return;
        }

        event.preventDefault();

        $.ajax({
            url: contextPath + '/cart/add',
            method: 'POST',
            data: $(this).serialize(),
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            success: function (data) {
                if (data.success) {
                    updateCartPreview(data);
                    showCartAddedToast();
                    return;
                }

                if (data.requireLogin) {
                    $('.header-cart').attr('aria-expanded', 'true');
                    $('#cartLoginPopover').prop('hidden', false).addClass('open').attr('aria-hidden', 'false');
                    return;
                }

                showMessage(data.message || 'Không thể thêm vào giỏ hàng');
            },
            error: function () {
                showMessage('Có lỗi xảy ra khi thêm vào giỏ hàng');
            }
        });
    });

    /* Image zoom modal */
    $(document).on('click', '.btn-zoom', function () {
        const imgSrc = $('.product-gallery img').attr('src');

        if (!imgSrc) {
            return;
        }

        $('#zoomImage').attr('src', imgSrc);
        $('#imageModal').addClass('show');
    });

    $(document).on('click', '#imageModal .modal-overlay, #imageModal .modal-close', function () {
        $('#imageModal').removeClass('show');
        $('#zoomImage').attr('src', '');
    });

    /* Quantity plus minus */
    $(document).on('click', '.qty-btn', function (e) {
        e.preventDefault();
        e.stopPropagation();

        const $control = $(this).closest('.quantity-control');
        const $input = $control.find('.qty-input');

        let value = parseInt($input.val(), 10) || 1;
        const min = parseInt($input.attr('min'), 10) || 1;
        const max = parseInt($input.attr('max'), 10) || 999;

        if ($(this).hasClass('plus') && value < max) {
            value++;
        }

        if ($(this).hasClass('minus') && value > min) {
            value--;
        }

        $input.val(value);
    });

    /* Esc key close modals */
    $(document).on('keydown', function (e) {
        if (e.key === 'Escape') {
            if ($('#imageModal').hasClass('show')) {
                $('#imageModal').removeClass('show');
                $('#zoomImage').attr('src', '');
            }

            if ($('#reviewModal').hasClass('show')) {
                $('#reviewModal').removeClass('show');
            }
        }
    });

    /* Review modal open close */
    $(document).off('click.reviewAction', '.review-action-btn');

    $(document).on('click.reviewAction', '.review-action-btn', function () {
        const isLoggedIn = String($(this).data('logged-in')) === 'true';
        const canReview = String($(this).data('can-review')) === 'true';

        if (!isLoggedIn) {
            showMessage('Vui lòng đăng nhập để đánh giá');
            return;
        }

        if (!canReview) {
            showMessage('Bạn chỉ có thể đánh giá sản phẩm đã mua');
            return;
        }

        $('#reviewModal').addClass('show');
    });

    $(document).on('click', '.review-overlay, .review-close', function () {
        $('#reviewModal').removeClass('show');
    });

    /* Star rating */
    $(document).on('click', '.rating-stars i', function () {
        const value = parseInt($(this).data('value'), 10);

        $('.rating-stars').attr('data-rating', value);

        $('.rating-stars i').each(function () {
            const starValue = parseInt($(this).data('value'), 10);

            if (starValue <= value) {
                $(this).addClass('active');
            } else {
                $(this).removeClass('active');
            }
        });
    });

    /* Submit review */
    $(document).on('click', '.submit-review', function () {
        const rating = parseInt($('.rating-stars').attr('data-rating'), 10);
        const comment = $('#reviewText').val().trim();

        if (!rating || rating < 1 || rating > 5) {
            showMessage('Vui lòng chọn số sao đánh giá ⭐');
            return;
        }

        if (comment.length < 5) {
            showMessage('Nhận xét quá ngắn ❗');
            return;
        }

        $.ajax({
            url: contextPath + '/reviews',
            method: 'POST',
            data: {
                productId: productId,
                rating: rating,
                comment: comment
            },
            success: function () {
                showMessage('Đã gửi đánh giá ✔');

                $('#reviewModal').removeClass('show');

                $('#reviewText').val('');
                $('.rating-stars').attr('data-rating', 0);
                $('.rating-stars i').removeClass('active');

                currentPage = 1;
                $('#reviewContainer').empty();

                loadReviews(true);
            },
            error: function (xhr) {
                let message = 'Không gửi được đánh giá ❌';

                if (xhr.status === 401) {
                    message = 'Vui lòng đăng nhập để đánh giá';
                } else if (xhr.status === 403) {
                    message = 'Bạn chỉ có thể đánh giá sản phẩm đã mua';
                } else if (xhr.responseText) {
                    try {
                        const res = JSON.parse(xhr.responseText);

                        if (res.message) {
                            message = res.message;
                        }
                    } catch (e) {
                        message = 'Không gửi được đánh giá ❌';
                    }
                }

                showMessage(message);
            }
        });
    });

    /* Load reviews */
    function loadReviews(reset = false) {
        if (isLoading) {
            return;
        }

        isLoading = true;

        if (reset) {
            currentPage = 1;
            $('#reviewContainer').empty();
        }

        $.ajax({
            url: contextPath + '/reviews',
            method: 'GET',
            data: {
                productId: productId,
                rating: currentRating,
                sort: currentSort,
                page: currentPage,
                size: 5
            },
            success: function (html) {
                if ($.trim(html) === '') {
                    $('#loadMoreReview').hide();
                } else {
                    $('#reviewContainer').append(html);
                    $('#loadMoreReview').show();
                }
            },
            error: function () {
                showMessage('Không tải được danh sách đánh giá');
            },
            complete: function () {
                isLoading = false;
            }
        });
    }

    /* Review filter and sort */
    $(document).on('click', '.filter-btn', function () {
        $('.filter-btn').removeClass('active');
        $(this).addClass('active');

        currentRating = $(this).data('rating') || '';
        loadReviews(true);
    });

    $(document).on('change', '.sort-select', function () {
        currentSort = $(this).val();
        loadReviews(true);
    });

    $(document).on('click', '#loadMoreReview', function () {
        currentPage++;
        loadReviews(false);
    });

    if ($('#reviewContainer').length) {
        loadReviews(true);
    }
});
