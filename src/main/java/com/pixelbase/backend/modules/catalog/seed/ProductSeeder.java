package com.pixelbase.backend.modules.catalog.seed;

import com.pixelbase.backend.common.seed.DataSeeder;
import com.pixelbase.backend.modules.catalog.dto.request.ProductImageRequest;
import com.pixelbase.backend.modules.catalog.dto.request.ProductRequest;
import com.pixelbase.backend.modules.catalog.repository.BrandRepository;
import com.pixelbase.backend.modules.catalog.repository.CategoryRepository;
import com.pixelbase.backend.modules.catalog.repository.ProductRepository;
import com.pixelbase.backend.modules.catalog.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Order(3)
@RequiredArgsConstructor
public class ProductSeeder implements DataSeeder {

    private final IProductService productService;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void seed() {
        if (productRepository.count() > 0) return;

        // 1. Mouses Gamer (Periféricos)
        createProduct("Mouse Logitech G Pro X Superlight 2", "LOG-GPROX-SL2",
            "El mouse más rápido de la historia de Logitech.", 549.90, 599.00, 20,
            "logitech", "mouses-gamer",
            Map.of("sensor", "HERO 2", "polling_rate", "2000Hz", "weight", "60g"));

        createProduct("Mouse Razer DeathAdder V3 Pro", "RZ-DA-V3PRO",
            "Ergonomía icónica perfeccionada para eSports.", 529.00, null, 15,
            "razer", "mouses-gamer",
            Map.of("sensor", "Focus Pro 30K", "buttons", 5, "connectivity", "HyperSpeed"));

        // 2. Teclados (Periféricos)
        createProduct("Teclado Razer Huntsman V3 Pro TKL", "RZ-HUNT-V3P",
            "Interruptores ópticos analógicos de última generación.", 899.00, 950.00, 10,
            "razer", "teclados-mecanicos",
            Map.of("switches", "Analog Optical Gen-2", "size", "TKL", "rgb", "Razer Chroma"));

        createProduct("Teclado Corsair K70 RGB TKL", "COR-K70-TKL",
            "Rendimiento de grado profesional en formato compacto.", 580.00, 620.00, 12,
            "corsair", "teclados-mecanicos",
            Map.of("switches", "Cherry MX Speed", "polling_rate", "8000Hz", "keycaps", "PBT Double-shot"));

        // 3. Audífonos (Periféricos)
        createProduct("Audífonos Logitech G733 Lightspeed Blue", "LOG-G733-BL",
            "Estilo y confort inalámbrico total.", 489.00, 550.00, 8,
            "logitech", "audifonos",
            Map.of("audio", "DTS Headphone:X 2.0", "battery", "29h", "weight", "278g"));

        // 4. Tarjetas de Video (Componentes)
        createProduct("ASUS ROG Strix RTX 4070 Ti Super", "ASUS-4070TIS",
            "La cúspide del rendimiento en 1440p.", 4250.00, 4500.00, 5,
            "asus-rog", "tarjetas-de-video",
            Map.of("vram", "16GB GDDR6X", "fans", 3, "power", "750W Recom."));

        createProduct("ASUS TUF Gaming RTX 4060 Ti", "ASUS-4060TI-TUF",
            "Durabilidad extrema y gran eficiencia térmica.", 1850.00, null, 7,
            "asus-rog", "tarjetas-de-video",
            Map.of("vram", "8GB GDDR6", "fans", 3, "slot", "2.5 slot"));

        // 5. Memorias RAM (Componentes)
        createProduct("RAM Kingston FURY Renegade 32GB DDR5", "KNG-REN-32D5",
            "Velocidades extremas para creadores y gamers.", 680.00, 720.00, 25,
            "kingston-fury", "memorias-ram",
            Map.of("capacity", "32GB (2x16)", "speed", "6000MT/s", "latency", "CL32"));

        createProduct("RAM Corsair Vengeance RGB 16GB DDR4", "COR-VEN-16D4",
            "Iluminación RGB dinámica y alto rendimiento.", 245.00, 280.00, 30,
            "corsair", "memorias-ram",
            Map.of("capacity", "16GB (2x8)", "speed", "3600MHz", "type", "DDR4"));

        // 6. Laptops Gamer (Laptops)
        createProduct("Laptop ASUS ROG Zephyrus G14", "ASUS-ZEPH-G14",
            "La laptop gamer más potente de 14 pulgadas.", 7200.00, 7800.00, 4,
            "asus-rog", "laptops-gamer",
            Map.of("cpu", "Ryzen 9 7940HS", "gpu", "RTX 4060", "screen", "Nebula HDR 165Hz"));

        createProduct("Laptop Razer Blade 15", "RZ-BLADE-15",
            "Elegancia minimalista con potencia bruta.", 11500.00, null, 3,
            "razer", "laptops-gamer",
            Map.of("cpu", "Intel i7-13800H", "gpu", "RTX 4070", "screen", "QHD 240Hz"));

        // 7. Monitores (Monitores)
        createProduct("Monitor ASUS ROG Swift 27 OLED", "ASUS-SWIFT-OLED",
            "Negros perfectos y respuesta de 0.03ms.", 2850.00, 3100.00, 6,
            "asus-rog", "monitores-144hz",
            Map.of("panel", "OLED", "res", "1440p", "hz", "240Hz"));

        createProduct("Monitor Corsair XENEON 32", "COR-XEN-32",
            "Colores vibrantes para diseño y gaming.", 3400.00, null, 4,
            "corsair", "monitores-ultrawide",
            Map.of("panel", "IPS", "res", "4K", "hdr", "600"));

        // 8. Extras (Completando los 15)
        createProduct("Audífonos Razer BlackShark V2 Pro White", "RZ-BSV2P-W",
            "Claridad de voz inigualable para comunicación táctica.", 599.00, 650.00, 10,
            "razer", "audifonos",
            Map.of("mic", "HyperClear Super Wideband", "battery", "70h", "drivers", "TriForce 50mm"));

        createProduct("RAM Kingston FURY Beast 16GB DDR5", "KNG-BST-16D5",
            "El salto a la nueva generación de memorias.", 290.00, 320.00, 40,
            "kingston-fury", "memorias-ram",
            Map.of("speed", "5200MT/s", "type", "DDR5", "heatsink", "Low-profile"));

        log.info(" ✅ -> ProductSeeder: 15 productos de hardware peruano cargados.");
    }

    private void createProduct(String name, String sku, String desc, double price, Double originalPrice,
                               int stock, String brandSlug, String catSlug, Map<String, Object> specs) {

        Long brandId = brandRepository.findBySlug(brandSlug)
            .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + brandSlug)).getId();

        Long catId = categoryRepository.findBySlug(catSlug)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + catSlug)).getId();

        ProductRequest request = new ProductRequest(
            name, sku, desc,
            BigDecimal.valueOf(price),
            originalPrice != null ? BigDecimal.valueOf(originalPrice) : null,
            stock, brandId, catId, specs,
            List.of(new ProductImageRequest("https://res.cloudinary.com/pixelbase/placeholder.png", name, 0))
        );

        productService.create(request);
    }
}
