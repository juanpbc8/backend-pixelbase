package com.pixelbase.backend.modules.order.infrastructure.payment;

import com.pixelbase.backend.modules.order.infrastructure.payment.dto.PaymentSessionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Profile("dev")
public class MockPaymentGatewayImpl implements PaymentGatewayService {

    @Value("${app.storefront.url}")
    private String frontendUrl;

    @Override
    public PaymentSessionResult createSession(String orderCode, BigDecimal total) {
        // Simulación agnóstica de parámetros de pasarela (Stubs de simulación)
        String fakeToken = "fake-gateway-session-token-xyz-12345";
        // La URL redirige de inmediato a la vista de éxito de Angular pasándole el código correlativo
        String paymentUrl = String.format("%s/checkout/success?code=%s", frontendUrl, orderCode);
        return new PaymentSessionResult(fakeToken, paymentUrl);
    }
}
