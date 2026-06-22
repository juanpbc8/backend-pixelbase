package com.pixelbase.backend.modules.user.service.impl;

import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.modules.user.api.dto.request.CustomerAddressSaveRequest;
import com.pixelbase.backend.modules.user.api.dto.request.CustomerProfileUpdateRequest;
import com.pixelbase.backend.modules.user.api.dto.response.CustomerAddressResponse;
import com.pixelbase.backend.modules.user.api.dto.response.CustomerProfileResponse;
import com.pixelbase.backend.modules.user.domain.UserAddressEntity;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import com.pixelbase.backend.modules.user.mapper.CustomerMapper;
import com.pixelbase.backend.modules.user.repository.UserAddressRepository;
import com.pixelbase.backend.modules.user.repository.UserRepository;
import com.pixelbase.backend.modules.user.service.UserInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInternalServiceImpl implements UserInternalService {
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final CustomerMapper customerMapper;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // =========================================================================
    // --- SUBMÓDULO: MI PERFIL ---
    // =========================================================================
    @Override
    public CustomerProfileResponse getProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró el perfil del usuario solicitado.")
            );
        return customerMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateProfile(Long userId, CustomerProfileUpdateRequest request) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar; el usuario no existe" +
                "."));

        // Regla de Negocio: Validar unicidad del número de documento contra terceros
        if (userRepository.existsByDocumentNumberAndIdNot(request.documentNumber(), userId)) {
            throw new ConflictException(
                "El número de documento proporcionado ya se encuentra registrado por otra cuenta.");
        }

        // Mapeo selectivo inyectado sobre la entidad gestionada por JPA (Dirty Checking)
        customerMapper.updateEntityFromRequest(request, user);

        UserEntity updatedUser = userRepository.save(user);
        return customerMapper.toProfileResponse(updatedUser);
    }

    // =========================================================================
    // --- SUBMÓDULO: MIS DIRECCIONES ---
    // =========================================================================
    @Override
    public List<CustomerAddressResponse> getAddresses(Long userId) {
        List<UserAddressEntity> addresses = userAddressRepository.findAllByUserId(userId);
        return customerMapper.toAddressResponseList(addresses);
    }

    @Override
    @Transactional
    public CustomerAddressResponse createAddress(Long userId, CustomerAddressSaveRequest request) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No se puede registrar la dirección; el usuario no existe."));

        List<UserAddressEntity> existingAddresses = userAddressRepository.findAllByUserId(userId);
        boolean isFirstAddress = existingAddresses.isEmpty();

        // Orquestación del Flag por Defecto
        boolean flagDefaultToApply = request.isDefault();
        if (isFirstAddress) {
            flagDefaultToApply = true;
        } else if (flagDefaultToApply) {
            // Apaga masivamente las direcciones predeterminadas previas del usuario
            userAddressRepository.resetDefaultAddressesByUserId(userId);
        }

        // Conversión y establecimiento de la relación bidireccional
        UserAddressEntity addressEntity = customerMapper.toAddressEntity(request);
        addressEntity.setUser(user);
        addressEntity.setDefaulted(flagDefaultToApply);

        UserAddressEntity savedAddress = userAddressRepository.save(addressEntity);
        userRepository.save(user);
        return customerMapper.toAddressResponse(savedAddress);
    }

    @Override
    @Transactional
    public CustomerAddressResponse updateAddress(Long addressId,
                                                 Long userId,
                                                 CustomerAddressSaveRequest request) {
        // Blindaje Anti-IDOR: Se consulta forzando la pertenencia del recurso al userId del JWT
        UserAddressEntity address = userAddressRepository.findByIdAndUserId(addressId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "La dirección solicitada no existe o no pertenece a su cuenta."));

        // Si el request exige que sea por defecto y antes no lo era, limpiamos los otros flags
        if (request.isDefault() && !address.isDefaulted()) {
            userAddressRepository.resetDefaultAddressesByUserId(userId);
        }

        customerMapper.updateAddressEntityFromRequest(request, address);

        // Seguro de Vida: Si era la única dirección o se apagó por error, mantenemos consistencia estructural
        if (userAddressRepository.findAllByUserId(userId).size() == 1) {
            address.setDefaulted(true);
        }

        UserAddressEntity updatedAddress = userAddressRepository.save(address);
        return customerMapper.toAddressResponse(updatedAddress);
    }

    @Override
    @Transactional
    public CustomerAddressResponse changeDefaultAddress(Long addressId, Long userId) {
        UserAddressEntity address = userAddressRepository.findByIdAndUserId(addressId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "La dirección solicitada no existe o no pertenece a su cuenta."));

        if (address.isDefaulted()) {
            return customerMapper.toAddressResponse(address);
        }

        // Apagar anteriores y encender la actual
        userAddressRepository.resetDefaultAddressesByUserId(userId);
        address.setDefaulted(true);

        UserAddressEntity updatedAddress = userAddressRepository.save(address);
        return customerMapper.toAddressResponse(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        UserAddressEntity address = userAddressRepository.findByIdAndUserId(addressId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "La dirección solicitada no existe o no pertenece a su cuenta."));

        boolean wasDefault = address.isDefaulted();
        userAddressRepository.delete(address);

        // Si el cliente borra su dirección por defecto actual, heredamos
        // automáticamente el rol 'default' a cualquier otra dirección que le quede.
        if (wasDefault) {
            List<UserAddressEntity> remainingAddresses = userAddressRepository.findAllByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                UserAddressEntity nextDefault = remainingAddresses.getFirst();
                nextDefault.setDefaulted(true);
                userAddressRepository.save(nextDefault);
            }
        }
    }
}
