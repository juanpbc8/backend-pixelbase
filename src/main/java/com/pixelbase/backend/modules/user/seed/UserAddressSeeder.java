package com.pixelbase.backend.modules.user.seed;

import com.pixelbase.backend.common.seed.DataSeeder;
import com.pixelbase.backend.modules.user.domain.Role;
import com.pixelbase.backend.modules.user.domain.UserAddressEntity;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import com.pixelbase.backend.modules.user.repository.UserAddressRepository;
import com.pixelbase.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@Order(5)
@RequiredArgsConstructor
public class UserAddressSeeder implements DataSeeder {

    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public void seed() {
        if (addressRepository.count() > 0) return;

        // Buscamos solo a los que tienen rol CUSTOMER
        List<UserEntity> customers = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.CLIENTE)
            .toList();

        for (UserEntity customer : customers) {
            // Generar 2 direcciones por cada cliente según su email para variar la data
            if (customer.getEmail().contains("juan")) {
                createAddress(customer,
                    "Av. Jose Pardo 123",
                    "Lima",
                    "Lima",
                    "Miraflores",
                    "Cerca al Parque Kennedy",
                    true);
                createAddress(customer,
                    "Calle Las Orquideas 456",
                    "Lima",
                    "Lima",
                    "San Isidro",
                    "Edificio Capital",
                    false);
            } else if (customer.getEmail().contains("maria")) {
                createAddress(customer,
                    "Calle Mercaderes 210",
                    "Arequipa",
                    "Arequipa",
                    "Cercado",
                    "A espaldas de la Plaza de Armas",
                    true);
                createAddress(customer,
                    "Urb. Yanahuara C-15",
                    "Arequipa",
                    "Arequipa",
                    "Yanahuara",
                    "Frente al mirador",
                    false);
            } else if (customer.getEmail().contains("lucho")) {
                createAddress(customer,
                    "Jr. Pizarro 550",
                    "La Libertad",
                    "Trujillo",
                    "Trujillo",
                    "Cerca a la Plazuela El Recreo",
                    true);
                createAddress(customer,
                    "Av. Larco 890",
                    "La Libertad",
                    "Trujillo",
                    "Huanchaco",
                    "Cerca al muelle",
                    false);
            } else { // Para Ana y otros
                createAddress(customer,
                    "Av. El Sol 400",
                    "Cusco",
                    "Cusco",
                    "Cusco",
                    "Frente al Qorikancha",
                    true);
                createAddress(customer,
                    "Urb. Larapa H-2",
                    "Cusco",
                    "Cusco",
                    "San Jeronimo",
                    "Cerca a la universidad",
                    false);
            }
        }
        log.info(" ✅ -> UserAddressSeeder: 2 direcciones asignadas a cada cliente.");
    }

    private void createAddress(UserEntity user, String line, String dept, String prov, String dist,
                               String ref, boolean isDefault) {
        UserAddressEntity address = UserAddressEntity.builder()
            .addressLine(line)
            .department(dept)
            .province(prov)
            .district(dist)
            .reference(ref)
            .defaulted(isDefault)
            .user(user)
            .build();
        addressRepository.save(address);
    }
}
