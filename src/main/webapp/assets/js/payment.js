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
    const shippingFeeText = document.getElementById('shippingFeeText');
    const shippingFeeInput = document.getElementById('shippingFeeInput');
    const grandTotalText = document.getElementById('grandTotalText');
    const submitButton = document.getElementById('submitOrder');
    const submitText = submitButton?.querySelector('span');
    const locationsUrl = form.dataset.locationsUrl;
    const shippingFeeUrl = form.dataset.shippingFeeUrl;
    const subtotal = Number(form.dataset.subtotal) || 0;
    const hasSelect2 = window.jQuery && jQuery.fn && jQuery.fn.select2;

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

    function selectedText(select) {
        const option = select?.selectedOptions?.[0];
        return option && option.value ? option.textContent.trim() : '';
    }

    function syncSelectedNames() {
        provinceNameInput.value = selectedText(provinceSelect);
        districtNameInput.value = selectedText(districtSelect);
        wardNameInput.value = selectedText(wardSelect);
    }

    function setShippingFee(value, sourceText) {
        const fee = Number(value) || 0;
        shippingFeeInput.value = String(fee);
        shippingFeeText.textContent = fee > 0 ? `${formatVND(fee)}${sourceText ? ` (${sourceText})` : ''}` : text('chooseAddress', 'Choose address');
        grandTotalText.textContent = formatVND(subtotal + fee);
    }

    async function calculateShippingFee(params) {
        if (!shippingFeeUrl) return;
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
                return;
            }
            setShippingFee(data.shippingFee, data.source === 'SIMULATION' ? text('shippingSimulation', 'GHN simulation') : 'GHN');
        } catch (error) {
            setShippingFee(30000, text('shippingSimulation', 'GHN simulation'));
        }
    }

    function syncAddressMode() {
        const selected = document.querySelector('input[name="savedAddressId"]:checked');
        const usingSavedAddress = Boolean(selected && selected.value);
        addressInputs.forEach(input => {
            input.disabled = usingSavedAddress;
            input.required = !usingSavedAddress;
        });
        newAddressFields?.classList.toggle('is-disabled', usingSavedAddress);

        if (usingSavedAddress) {
            calculateShippingFee({
                addressId: selected.value
            });
            return;
        }

        if (wardSelect?.value && districtSelect?.value) {
            calculateShippingFee({
                districtId: districtSelect.value,
                wardCode: wardSelect.value
            });
        } else {
            setShippingFee(0, '');
        }
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
        setSelectDisabled(districtSelect, !provinceSelect.value);
        setSelectDisabled(wardSelect, true);
        setShippingFee(0, '');

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
        setSelectDisabled(wardSelect, !districtSelect.value);
        setShippingFee(0, '');

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
        if (districtSelect.value && wardSelect.value) {
            calculateShippingFee({
                districtId: districtSelect.value,
                wardCode: wardSelect.value
            });
        }
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
    document.querySelectorAll('input[name="paymentMethod"]').forEach(radio => {
        radio.addEventListener('change', syncPaymentButton);
    });

    form.addEventListener('submit', function () {
        syncSelectedNames();
        if (submitButton) submitButton.disabled = true;
        if (submitText) submitText.textContent = text('processing', 'Processing...');
    });

    loadProvinces();
    syncAddressMode();
    syncPaymentButton();
}());
