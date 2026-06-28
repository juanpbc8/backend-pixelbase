package com.pixelbase.backend.modules.user.service;

import com.pixelbase.backend.modules.user.api.customer.dto.request.CustomerAddressSaveRequest;
import com.pixelbase.backend.modules.user.api.customer.dto.request.CustomerProfileUpdateRequest;
import com.pixelbase.backend.modules.user.api.customer.dto.response.CustomerAddressResponse;
import com.pixelbase.backend.modules.user.api.customer.dto.response.CustomerProfileResponse;

import java.util.List;

public interface UserInternalService {
    boolean existsByEmail(String email);

    // --- SUBMÓDULO: MI PERFIL ---
    CustomerProfileResponse getProfile(Long userId);

    CustomerProfileResponse updateProfile(Long userId, CustomerProfileUpdateRequest request);

    // --- SUBMÓDULO: MIS DIRECCIONES ---
    List<CustomerAddressResponse> getAddresses(Long userId);

    CustomerAddressResponse createAddress(Long userId, CustomerAddressSaveRequest request);

    CustomerAddressResponse updateAddress(Long addressId, Long userId, CustomerAddressSaveRequest request);

    CustomerAddressResponse changeDefaultAddress(Long addressId, Long userId);

    void deleteAddress(Long addressId, Long userId);
}
