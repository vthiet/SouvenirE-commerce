(function () {
    const form = document.getElementById('checkoutForm');
    if (!form) return;

    const contextPath = document.querySelector('meta[name="context-path"]')?.content || '';
    const newAddressFields = document.getElementById('newAddressFields');
    const addressInputs = newAddressFields ? newAddressFields.querySelectorAll('input, select') : [];
    const savedAddressRadios = document.querySelectorAll('input[name="savedAddressId"]');
    const provinceSelect = document.getElementById('provinceSelect') || document.getElementById('provinceCode');
    const wardSelect = document.getElementById('wardSelect') || document.getElementById('wardCode');
    const wardStatus = document.getElementById('wardStatus');
    const submitButton = document.getElementById('submitOrder');
    const submitText = submitButton?.querySelector('span');
    const wardsUrl = form.dataset.wardsUrl || `${contextPath}/user/address/wards`;
    const hasSelect2 = window.jQuery && jQuery.fn && jQuery.fn.select2;

    function syncAddressMode() {
        const selected = document.querySelector('input[name="savedAddressId"]:checked');
        const usingSavedAddress = Boolean(selected && selected.value);
        addressInputs.forEach(input => {
            input.disabled = usingSavedAddress;
            input.required = !usingSavedAddress;
        });
        if (hasSelect2) {
            jQuery(provinceSelect).prop('disabled', usingSavedAddress).trigger('change.select2');
            jQuery(wardSelect).prop('disabled', usingSavedAddress || !provinceSelect?.value).trigger('change.select2');
        }
        newAddressFields?.classList.toggle('is-disabled', usingSavedAddress);
    }

    function syncPaymentButton() {
        const method = document.querySelector('input[name="paymentMethod"]:checked')?.value;
        if (submitText) {
            submitText.textContent = method === 'VNPAY_QR' ? 'Thanh toán qua VNPay' : 'Đặt hàng';
        }
    }

    savedAddressRadios.forEach(radio => radio.addEventListener('change', syncAddressMode));
    document.querySelectorAll('input[name="paymentMethod"]').forEach(radio => {
        radio.addEventListener('change', syncPaymentButton);
    });

    function setupSelect2AddressApi() {
        if (!hasSelect2 || !provinceSelect || !wardSelect) {
            return false;
        }

        jQuery(provinceSelect).select2({
            width: '100%',
            placeholder: 'Chọn tỉnh/thành phố',
            allowClear: true
        });

        jQuery(wardSelect).select2({
            width: '100%',
            placeholder: 'Chọn phường/xã',
            allowClear: true,
            ajax: {
                url: wardsUrl,
                dataType: 'json',
                delay: 200,
                data: function (params) {
                    return {
                        provinceCode: jQuery(provinceSelect).val(),
                        q: params.term || ''
                    };
                },
                processResults: function (data) {
                    return data;
                }
            }
        });

        jQuery(provinceSelect).on('change', function () {
            jQuery(wardSelect).val(null).trigger('change');
            jQuery(wardSelect).prop('disabled', !this.value);
            if (wardStatus) {
                wardStatus.textContent = this.value ? '' : 'Vui lòng chọn tỉnh/thành phố trước.';
                wardStatus.classList.remove('is-error');
            }
        });

        return true;
    }

    function setupFallbackAddressApi() {
        if (!provinceSelect || !wardSelect) {
            return;
        }

        provinceSelect.addEventListener('change', async function () {
            wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
            wardStatus?.classList.remove('is-error');
            if (!this.value) {
                if (wardStatus) {
                    wardStatus.textContent = '';
                }
                wardSelect.disabled = true;
                return;
            }

            wardSelect.disabled = true;
            if (wardStatus) {
                wardStatus.textContent = 'Đang tải danh sách phường/xã...';
            }
            try {
                const response = await fetch(`${wardsUrl}?provinceCode=${encodeURIComponent(this.value)}`);
                if (!response.ok) throw new Error('Request failed');
                const data = await response.json();
                (data.results || []).forEach(ward => {
                    const option = document.createElement('option');
                    option.value = ward.id;
                    option.textContent = ward.text;
                    wardSelect.appendChild(option);
                });
                if (wardStatus) {
                    wardStatus.textContent = data.results?.length ? '' : 'Không tìm thấy phường/xã.';
                }
            } catch (error) {
                if (wardStatus) {
                    wardStatus.textContent = 'Không thể tải phường/xã. Vui lòng thử lại.';
                    wardStatus.classList.add('is-error');
                }
            } finally {
                wardSelect.disabled = false;
            }
        });
    }

    form.addEventListener('submit', function () {
        if (submitButton) {
            submitButton.disabled = true;
        }
        if (submitText) {
            submitText.textContent = 'Đang xử lý...';
        }
    });

    if (!setupSelect2AddressApi()) {
        setupFallbackAddressApi();
    }
    syncAddressMode();
    syncPaymentButton();
}());
