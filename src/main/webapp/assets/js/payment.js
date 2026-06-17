(function () {
    const form = document.getElementById('checkoutForm');
    if (!form) return;

    const checkoutI18n = document.getElementById('checkoutI18n')?.dataset || {};
    const newAddressFields = document.getElementById('newAddressFields');
    const addressInputs = newAddressFields ? newAddressFields.querySelectorAll('input, select') : [];
    const savedAddressRadios = document.querySelectorAll('input[name="savedAddressId"]');
    const provinceSelect = document.getElementById('provinceSelect');
    const districtSelect = document.getElementById('districtSelect');
    const wardSelect = document.getElementById('wardSelect');
    const provinceNameInput = document.getElementById('provinceName');
    const districtNameInput = document.getElementById('districtName');
    const wardNameInput = document.getElementById('wardName');
    const wardStatus = document.getElementById('wardStatus');
    const addressValidationStatus = document.getElementById('addressValidationStatus');
    const receiverPhone = document.getElementById('receiverPhone');
    const addressDetail = document.getElementById('addressDetail');
    const shippingFeeText = document.getElementById('shippingFeeText');
    const shippingFeeInput = document.getElementById('shippingFeeInput');
    const grandTotalText = document.getElementById('grandTotalText');
    const submitButton = document.getElementById('submitOrder');
    const submitText = submitButton?.querySelector('span');
    const locationsUrl = form.dataset.locationsUrl;
    const shippingFeeUrl = form.dataset.shippingFeeUrl;
    const subtotal = Number(form.dataset.subtotal) || 0;
    const hasSelect2 = window.jQuery && jQuery.fn && jQuery.fn.select2;
    const ghnPhonePattern = /^0(3|5|7|8|9)\d{8}$/;

    let isSubmitting = false;
    let addressFeedbackTimer = null;

    const formatVND = (value) => `${Number(value || 0).toLocaleString('vi-VN')}₫`;
    const text = (key, fallback = '') => checkoutI18n[key] || fallback;

    function setOptions(select, placeholder, items, valueKey, textKey) {
        if (!select) return;
        select.innerHTML = `<option value="">${placeholder}</option>`;
        (items || []).forEach((item) => {
            const option = document.createElement('option');
            option.value = item[valueKey];
            option.textContent = item[textKey];
            select.appendChild(option);
        });
        if (hasSelect2) {
            jQuery(select).trigger('change.select2');
        }
    }

    async function fetchLocation(type, params = {}) {
        const url = new URL(locationsUrl, window.location.origin);
        url.searchParams.set('type', type);
        Object.entries(params).forEach(([key, value]) => {
            if (value) url.searchParams.set(key, value);
        });
        const response = await fetch(url);
        const json = await response.json();
        if (!response.ok) {
            throw new Error(json.message || json.error || text('locationPrefix', 'Location load error'));
        }
        return json.data || [];
    }

    function showLocationError(prefix, error) {
        if (!wardStatus) return;
        wardStatus.textContent = error?.message ? `${prefix}: ${error.message}` : prefix;
        wardStatus.classList.add('is-error');
    }

    function setValidationStatus(message, type = 'neutral') {
        if (!addressValidationStatus) return;
        addressValidationStatus.textContent = message || '';
        addressValidationStatus.classList.remove('is-error', 'is-success');
        if (type === 'error') {
            addressValidationStatus.classList.add('is-error');
        } else if (type === 'success') {
            addressValidationStatus.classList.add('is-success');
        }
    }

    function selectedText(select) {
        const option = select?.selectedOptions?.[0];
        return option && option.value ? option.textContent.trim() : '';
    }

    function syncSelectedNames() {
        provinceNameInput.value = selectedText(provinceSelect);
        districtNameInput.value = selectedText(districtSelect);
        wardNameInput.value = selectedText(wardSelect);
    }

    function normalizePhone(value) {
        let normalized = (value || '').trim().replace(/[\s\-().]/g, '');
        if (normalized.startsWith('+84')) {
            normalized = `0${normalized.slice(3)}`;
        } else if (normalized.startsWith('84') && normalized.length > 2) {
            normalized = `0${normalized.slice(2)}`;
        }
        return normalized;
    }

    function isValidGhnPhone(value) {
        return ghnPhonePattern.test(normalizePhone(value));
    }

    function getSelectedSavedAddress() {
        return document.querySelector('input[name="savedAddressId"]:checked');
    }

    function isUsingSavedAddress() {
        const selected = getSelectedSavedAddress();
        return Boolean(selected && selected.value);
    }

    function validateSavedAddressMeta(selected) {
        const data = selected?.dataset || {};
        const missing = [];

        if (!data.provinceId || !data.provinceName) {
            missing.push(text('addressMissingProvince', 'Thiếu tên tỉnh/thành phố hoặc mã GHN tỉnh'));
        }
        if (!data.districtId || !data.districtName) {
            missing.push(text('addressMissingDistrict', 'Thiếu tên quận/huyện hoặc mã GHN quận'));
        }
        if (!data.wardCode || !data.wardName) {
            missing.push(text('addressMissingWard', 'Thiếu tên phường/xã hoặc mã GHN phường'));
        }
        if (!isValidGhnPhone(data.receiverPhone)) {
            missing.push(text('addressPhoneInvalid', 'Số điện thoại chưa hợp lệ'));
        }

        if (missing.length > 0) {
            return { valid: false, message: missing.join(' · ') };
        }

        return {
            valid: true,
            message: text('addressSavedValid', 'Địa chỉ đã lưu có GHN mapping hợp lệ')
        };
    }

    function validateNewAddressMeta() {
        const hasAnyValue = Boolean(
            provinceSelect?.value ||
            districtSelect?.value ||
            wardSelect?.value ||
            provinceNameInput?.value ||
            districtNameInput?.value ||
            wardNameInput?.value ||
            receiverPhone?.value ||
            addressDetail?.value
        );
        if (!hasAnyValue) {
            return { valid: false, message: '' };
        }

        const missing = [];

        if (!provinceSelect?.value || !provinceNameInput?.value) {
            missing.push(text('addressMissingProvince', 'Thiếu tên tỉnh/thành phố hoặc mã GHN tỉnh'));
        }
        if (!districtSelect?.value || !districtNameInput?.value) {
            missing.push(text('addressMissingDistrict', 'Thiếu tên quận/huyện hoặc mã GHN quận'));
        }
        if (!wardSelect?.value || !wardNameInput?.value) {
            missing.push(text('addressMissingWard', 'Thiếu tên phường/xã hoặc mã GHN phường'));
        }
        if (!receiverPhone?.value || !isValidGhnPhone(receiverPhone.value)) {
            missing.push(text('addressPhoneInvalid', 'Số điện thoại chưa hợp lệ'));
        }
        if (!addressDetail?.value) {
            missing.push(text('addressMissingDetail', 'Thiếu địa chỉ chi tiết'));
        }

        if (missing.length > 0) {
            return { valid: false, message: missing.join(' · ') };
        }

        return {
            valid: true,
            message: text('addressValid', 'Địa chỉ GHN hợp lệ')
        };
    }

    function updateSubmitState(isValid) {
        if (submitButton) {
            submitButton.disabled = isSubmitting || !isValid;
        }
    }

    function scheduleRefreshAddressFeedback() {
        if (addressFeedbackTimer) {
            clearTimeout(addressFeedbackTimer);
        }
        addressFeedbackTimer = setTimeout(() => {
            refreshAddressFeedback();
        }, 250);
    }

    function setShippingFee(value, sourceText) {
        const fee = Number(value) || 0;
        shippingFeeInput.value = String(fee);
        shippingFeeText.textContent = fee > 0 ? `${formatVND(fee)}${sourceText ? ` (${sourceText})` : ''}` : text('chooseAddress', 'Choose address');
        grandTotalText.textContent = formatVND(subtotal + fee);
    }

    async function calculateShippingFee(params) {
        if (!shippingFeeUrl) return false;
        if (shippingFeeText) shippingFeeText.textContent = text('shippingPending', 'Calculating...');

        const url = new URL(shippingFeeUrl, window.location.origin);
        Object.entries(params).forEach(([key, value]) => {
            if (value) url.searchParams.set(key, value);
        });

        try {
            const response = await fetch(url, {
                headers: {'X-Requested-With': 'XMLHttpRequest'}
            });
            const data = await response.json();
            if (!response.ok || data.success === false) {
                setShippingFee(30000, text('shippingSimulation', 'GHN simulation'));
                return false;
            }
            setShippingFee(data.shippingFee, data.source === 'SIMULATION' ? text('shippingSimulation', 'GHN simulation') : 'GHN');
            return true;
        } catch (error) {
            setShippingFee(30000, text('shippingSimulation', 'GHN simulation'));
            return false;
        }
    }

    async function refreshAddressFeedback() {
        const selected = getSelectedSavedAddress();
        if (selected && selected.value) {
            const result = validateSavedAddressMeta(selected);
            setValidationStatus(result.message, result.message ? (result.valid ? 'success' : 'error') : 'neutral');
            updateSubmitState(result.valid);
            if (!result.valid) {
                setShippingFee(0, '');
                return false;
            }
            setValidationStatus(text('addressChecking', 'Đang kiểm tra địa chỉ...'), 'neutral');
            await calculateShippingFee({
                addressId: selected.value
            });
            setValidationStatus(result.message, 'success');
            return true;
        }

        const result = validateNewAddressMeta();
        setValidationStatus(result.message, result.message ? (result.valid ? 'success' : 'error') : 'neutral');
        updateSubmitState(result.valid);
        if (!result.valid) {
            setShippingFee(0, '');
            return false;
        }
        setValidationStatus(text('addressChecking', 'Đang kiểm tra địa chỉ...'), 'neutral');
        await calculateShippingFee({
            districtId: districtSelect.value,
            wardCode: wardSelect.value
        });
        setValidationStatus(result.message, 'success');
        return true;
    }

    function syncAddressMode() {
        const usingSavedAddress = isUsingSavedAddress();
        addressInputs.forEach(input => {
            input.disabled = usingSavedAddress;
            input.required = !usingSavedAddress;
        });
        newAddressFields?.classList.toggle('is-disabled', usingSavedAddress);
        refreshAddressFeedback();
    }

    function syncPaymentButton() {
        const method = document.querySelector('input[name="paymentMethod"]:checked')?.value;
        if (submitText) {
            submitText.textContent = method === 'VNPAY_QR'
                ? text('submitVnpay', 'Pay with VNPay')
                : text('submitCod', 'Place order');
        }
    }

    async function loadProvinces() {
        try {
            const provinces = await fetchLocation('province');
            setOptions(provinceSelect, text('selectProvince', 'Choose Province / City'), provinces, 'ProvinceID', 'ProvinceName');
        } catch (error) {
            showLocationError(text('locationProvinceError', 'Could not load the province/city list'), error);
        }
    }

    async function onProvinceChange() {
        syncSelectedNames();
        setOptions(districtSelect, text('selectDistrict', 'Choose District / County'), [], 'DistrictID', 'DistrictName');
        setOptions(wardSelect, text('selectWard', 'Choose Ward / Commune'), [], 'WardCode', 'WardName');
        districtNameInput.value = '';
        wardNameInput.value = '';
        setSelectDisabled(districtSelect, !provinceSelect.value);
        setSelectDisabled(wardSelect, true);
        setShippingFee(0, '');
        setValidationStatus('', 'neutral');

        if (!provinceSelect.value) return;

        try {
            const districts = await fetchLocation('district', {provinceId: provinceSelect.value});
            setOptions(districtSelect, text('selectDistrict', 'Choose District / County'), districts, 'DistrictID', 'DistrictName');
            setSelectDisabled(districtSelect, false);
        } catch (error) {
            showLocationError(text('locationDistrictError', 'Could not load the district/county list'), error);
        }
    }

    async function onDistrictChange() {
        syncSelectedNames();
        setOptions(wardSelect, text('selectWard', 'Choose Ward / Commune'), [], 'WardCode', 'WardName');
        wardNameInput.value = '';
        setSelectDisabled(wardSelect, !districtSelect.value);
        setShippingFee(0, '');
        setValidationStatus('', 'neutral');

        if (!districtSelect.value) return;

        try {
            const wards = await fetchLocation('ward', {districtId: districtSelect.value});
            setOptions(wardSelect, text('selectWard', 'Choose Ward / Commune'), wards, 'WardCode', 'WardName');
            setSelectDisabled(wardSelect, false);
            if (wardStatus) {
                wardStatus.textContent = '';
                wardStatus.classList.remove('is-error');
            }
        } catch (error) {
            showLocationError(text('locationWardError', 'Could not load the ward/commune list'), error);
        }
    }

    function setSelectDisabled(select, disabled) {
        if (!select) return;
        select.disabled = disabled;
        if (hasSelect2) {
            jQuery(select).prop('disabled', disabled).trigger('change.select2');
        }
    }

    function onWardChange() {
        syncSelectedNames();
        refreshAddressFeedback();
    }

    if (hasSelect2) {
        jQuery(provinceSelect).select2({width: '100%', placeholder: text('selectProvince', 'Choose Province / City'), allowClear: true});
        jQuery(districtSelect).select2({width: '100%', placeholder: text('selectDistrict', 'Choose District / County'), allowClear: true});
        jQuery(wardSelect).select2({width: '100%', placeholder: text('selectWard', 'Choose Ward / Commune'), allowClear: true});
    }

    if (hasSelect2) {
        jQuery(provinceSelect).on('change', onProvinceChange);
        jQuery(districtSelect).on('change', onDistrictChange);
        jQuery(wardSelect).on('change', onWardChange);
    } else {
        provinceSelect?.addEventListener('change', onProvinceChange);
        districtSelect?.addEventListener('change', onDistrictChange);
        wardSelect?.addEventListener('change', onWardChange);
    }
    savedAddressRadios.forEach(radio => radio.addEventListener('change', syncAddressMode));
    receiverPhone?.addEventListener('blur', refreshAddressFeedback);
    addressDetail?.addEventListener('blur', refreshAddressFeedback);
    receiverPhone?.addEventListener('input', () => {
        if (!isUsingSavedAddress()) {
            scheduleRefreshAddressFeedback();
        }
    });
    addressDetail?.addEventListener('input', () => {
        if (!isUsingSavedAddress()) {
            scheduleRefreshAddressFeedback();
        }
    });
    document.querySelectorAll('input[name="paymentMethod"]').forEach(radio => {
        radio.addEventListener('change', syncPaymentButton);
    });

    form.addEventListener('submit', async function (event) {
        if (isSubmitting) {
            return;
        }

        event.preventDefault();
        isSubmitting = true;
        if (submitButton) submitButton.disabled = true;
        syncSelectedNames();

        const isValid = await refreshAddressFeedback();
        if (!isValid) {
            isSubmitting = false;
            updateSubmitState(false);
            return;
        }

        if (!form.reportValidity()) {
            isSubmitting = false;
            updateSubmitState(true);
            return;
        }

        if (submitButton) submitButton.disabled = true;
        if (submitText) submitText.textContent = text('processing', 'Processing...');
        form.submit();
    });

    loadProvinces();
    syncAddressMode();
    syncPaymentButton();
}());
