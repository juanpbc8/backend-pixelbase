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
        createProduct("Mouse Logitech G Pro X Superlight 2",
            "El mouse más rápido de la historia de Logitech.", 549.90, 599.00, 20,
            "logitech", "mouses-gamer",
            Map.of("sensor", "HERO 2", "tasa_sondeo", "2000Hz", "peso", "60g"),
            "910-006628");

        createProduct("Mouse Razer DeathAdder V3 Pro",
            "Ergonomía icónica perfeccionada para eSports.", 529.00, null, 15,
            "razer", "mouses-gamer",
            Map.of("sensor", "Focus Pro 30K", "botones", 5, "conectividad", "HyperSpeed"),
            "RZ01-04630100-R3U1");

        // 2. Teclados (Periféricos)
        createProduct("Teclado Razer Huntsman V3 Pro TKL",
            "Interruptores ópticos analógicos de última generación.", 899.00, 950.00, 10,
            "razer", "teclados-mecanicos",
            Map.of("interruptores", "Analog Optical Gen-2", "tamano", "TKL", "rgb", "Razer Chroma"),
            "RZ03-04980100-R3U1");

        createProduct("Teclado Corsair K70 RGB TKL",
            "Rendimiento de grado profesional en formato compacto.", 580.00, 620.00, 12,
            "corsair", "teclados-mecanicos",
            Map.of("interruptores", "Cherry MX Speed", "tasa_sondeo", "8000Hz", "teclas", "PBT Double-shot"),
            "CH-9119014-NA");

        // 3. Audífonos (Periféricos)
        createProduct("Audífonos Logitech G733 Lightspeed Blue",
            "Estilo y confort inalámbrico total.", 489.00, 550.00, 8,
            "logitech", "audifonos",
            Map.of("audio", "DTS Headphone:X 2.0", "bateria", "29h", "peso", "278g"),
            "981-000942");

        // 4. Tarjetas de Video (Componentes)
        createProduct("ASUS ROG Strix RTX 4070 Ti Super",
            "La cúspide del rendimiento en 1440p.", 4250.00, 4500.00, 5,
            "asus-rog", "tarjetas-de-video",
            Map.of("vram", "16GB GDDR6X", "ventiladores", 3, "energia", "750W Recom."),
            "ROG-STRIX-RTX4070TIS-O16G-GAMING");

        createProduct("ASUS TUF Gaming RTX 4060 Ti",
            "Durabilidad extrema y gran eficiencia térmica.", 1850.00, null, 7,
            "asus-rog", "tarjetas-de-video",
            Map.of("vram", "8GB GDDR6", "ventiladores", 3, "ranura", "2.5 slot"),
            "TUF-RTX4060TI-O8G-GAMING");

        // 5. Memorias RAM (Componentes)
        createProduct("RAM Kingston FURY Renegade 32GB DDR5",
            "Velocidades extremas para creadores y gamers.", 680.00, 720.00, 25,
            "kingston-fury", "memorias-ram",
            Map.of("capacidad", "32GB (2x16)", "velocidad", "6000MT/s", "latencia", "CL32"),
            "KF560C32RSK2-32");

        createProduct("RAM Corsair Vengeance RGB 16GB DDR4",
            "Iluminación RGB dinámica y alto rendimiento.", 245.00, 280.00, 30,
            "corsair", "memorias-ram",
            Map.of("capacidad", "16GB (2x8)", "velocidad", "3600MHz", "tipo", "DDR4"),
            "CMG16GX4M2D3600C18");

        // 6. Laptops Gamer (Laptops)
        createProduct("Laptop ASUS ROG Zephyrus G14",
            "La laptop gamer más potente de 14 pulgadas.", 7200.00, 7800.00, 4,
            "asus-rog", "laptops-gamer",
            Map.of("procesador", "Ryzen 9 7940HS", "graficos", "RTX 4060", "pantalla", "Nebula HDR 165Hz"),
            "GA402XV-G14.R94060");

        createProduct("Laptop Razer Blade 15",
            "Elegancia minimalista con potencia bruta.", 11500.00, null, 3,
            "razer", "laptops-gamer",
            Map.of("procesador", "Intel i7-13800H", "graficos", "RTX 4070", "pantalla", "QHD 240Hz"),
            "RZ09-0485YED3-R3U1");

        // 7. Monitores (Monitores)
        createProduct("Monitor ASUS ROG Swift 27 OLED",
            "Negros perfectos y respuesta de 0.03ms.", 2850.00, 3100.00, 6,
            "asus-rog", "monitores-144hz",
            Map.of("panel", "OLED", "resolucion", "1440p", "frecuencia", "240Hz"),
            "PG27AQDM");

        createProduct("Monitor Corsair XENEON 32",
            "Colores vibrantes para diseño y gaming.", 3400.00, null, 4,
            "corsair", "monitores-ultrawide",
            Map.of("panel", "IPS", "resolucion", "4K", "hdr", "600"),
            "CM-9030003-PE");

        // 8. Extras (Completando los 15)
        createProduct("Audífonos Razer BlackShark V2 Pro White",
            "Claridad de voz inigualable para comunicación táctica.", 599.00, 650.00, 10,
            "razer", "audifonos",
            Map.of("microfono", "HyperClear Super Wideband", "bateria", "70h", "controladores", "TriForce 50mm"),
            "RZ04-04530200-R3U1");

        createProduct("RAM Kingston FURY Beast 16GB DDR5",
            "El salto a la nueva generación de memorias.", 290.00, 320.00, 40,
            "kingston-fury", "memorias-ram",
            Map.of("velocidad", "5200MT/s", "tipo", "DDR5", "disipador", "Low-profile"),
            "KF552C40BB-16");

        log.info(" ✅ -> ProductSeeder: 15 productos de hardware peruano cargados.");
    }

    private void createProduct(String name, String desc, double price, Double originalPrice,
                               int stock, String brandSlug, String catSlug, Map<String, Object> specs,
                               String partNumber) {

        Long brandId = brandRepository.findBySlug(brandSlug)
            .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + brandSlug)).getId();

        Long catId = categoryRepository.findBySlug(catSlug)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + catSlug)).getId();

        ProductRequest request = new ProductRequest(
            name, desc,
            BigDecimal.valueOf(price),
            originalPrice != null ? BigDecimal.valueOf(originalPrice) : null,
            stock, partNumber, brandId, catId, specs,
            List.of(new ProductImageRequest("https://res.cloudinary.com/pixelbase/placeholder.png", name, 0))
        );

        productService.create(request);
    }
}
