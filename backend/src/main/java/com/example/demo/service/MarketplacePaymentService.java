package com.example.demo.service;

import com.example.demo.config.StripeProperties;
import com.example.demo.model.*;
import com.example.demo.repository.MarketplaceOrderRepository;
import com.example.demo.repository.OfferRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.security.SecurityUtils;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarketplacePaymentService {

    private final StripeProperties stripeProperties;
    private final MarketplaceOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OfferRepository offerRepository;
    private final VerificationGuard verificationGuard;

    @Value("${rentix.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        if (stripeProperties.getSecretKey() != null && !stripeProperties.getSecretKey().isBlank()) {
            Stripe.apiKey = stripeProperties.getSecretKey();
        }
    }

    @Transactional
    public Map<String, String> createBuyNowCheckout(Long listingId, Long buyerId) throws Exception {
        verificationGuard.requireVerified(buyerId);
        Product product = productRepository.findById(listingId).orElseThrow();
        validateAvailable(product);
        if (product.getUserId().equals(buyerId)) {
            throw new IllegalArgumentException("Nu poți cumpăra propriul produs.");
        }
        preventDuplicateOrder(listingId);

        long amountCents = Math.round(product.getPret() * 100);
        MarketplaceOrder order = newOrder(listingId, buyerId, product.getUserId(), null, amountCents, true);
        order = orderRepository.save(order);

        Session session = createEscrowCheckoutSession(
                order.getId(),
                "Cumpărare: " + product.getTitlu(),
                amountCents,
                buyerId,
                "/checkout/success?orderId=" + order.getId(),
                "/checkout/cancel?orderId=" + order.getId()
        );

        order.setStripeCheckoutSessionId(session.getId());
        orderRepository.save(order);
        product.setStatus("PENDING_PAYMENT");
        productRepository.save(product);

        return Map.of("url", session.getUrl(), "orderId", order.getId().toString());
    }

    @Transactional
    public Map<String, String> createOfferPaymentCheckout(Long offerId, Long buyerId) throws Exception {
        verificationGuard.requireVerified(buyerId);
        Offer offer = offerRepository.findByIdAndBuyerId(offerId, buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Ofertă inexistentă."));
        if (offer.getStatus() != OfferStatus.ACCEPTED) {
            throw new IllegalArgumentException("Oferta trebuie acceptată înainte de plată.");
        }
        Product product = productRepository.findById(offer.getListingId()).orElseThrow();
        long amountCents = Math.round(offer.getAmount() * 100);

        MarketplaceOrder order = newOrder(offer.getListingId(), buyerId, offer.getSellerId(), offer.getId(), amountCents, false);
        order = orderRepository.save(order);

        Session session = createEscrowCheckoutSession(
                order.getId(),
                "Ofertă acceptată: plată",
                amountCents,
                buyerId,
                "/checkout/success?orderId=" + order.getId() + "&offerId=" + offerId,
                "/checkout/cancel?orderId=" + order.getId()
        );

        order.setStripeCheckoutSessionId(session.getId());
        orderRepository.save(order);
        offer.setMarketplaceOrderId(order.getId());
        offerRepository.save(offer);

        return Map.of("url", session.getUrl(), "orderId", order.getId().toString());
    }

    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) throws Exception {
        if (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank()) {
            return;
        }
        Event event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                onCheckoutCompleted(session);
            }
        }
    }

    @Transactional
    public void onCheckoutCompleted(Session session) throws Exception {
        MarketplaceOrder order = orderRepository.findByStripeCheckoutSessionId(session.getId())
                .orElseThrow();
        if (order.getEscrowStatus() != EscrowStatus.PENDING_PAYMENT) {
            return;
        }

        String paymentIntentId = session.getPaymentIntent();
        order.setStripePaymentIntentId(paymentIntentId);
        order.setEscrowStatus(EscrowStatus.ESCROW_ACTIVE);
        order.setPaidAt(java.time.LocalDateTime.now());
        orderRepository.save(order);

        Product product = productRepository.findById(order.getListingId()).orElseThrow();
        product.setStatus("SOLD");
        productRepository.save(product);

        if (order.getOfferId() != null) {
            offerRepository.findById(order.getOfferId()).ifPresent(o -> {
                o.setStatus(OfferStatus.PAID);
                offerRepository.save(o);
            });
        }
    }

    @Transactional
    public MarketplaceOrder markShipped(Long orderId, Long sellerId) {
        MarketplaceOrder order = getOrderForSeller(orderId, sellerId);
        if (order.getEscrowStatus() != EscrowStatus.ESCROW_ACTIVE) {
            throw new IllegalArgumentException("Comanda nu este în escrow activ.");
        }
        order.setEscrowStatus(EscrowStatus.SHIPPED);
        order.setShippedAt(java.time.LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public MarketplaceOrder confirmDelivery(Long orderId, Long buyerId) throws Exception {
        MarketplaceOrder order = getOrderForBuyer(orderId, buyerId);
        if (order.getEscrowStatus() != EscrowStatus.SHIPPED && order.getEscrowStatus() != EscrowStatus.DELIVERED) {
            throw new IllegalArgumentException("Comanda nu poate fi confirmată.");
        }
        captureFunds(order);
        order.setEscrowStatus(EscrowStatus.COMPLETED);
        order.setDeliveredAt(java.time.LocalDateTime.now());
        order.setCompletedAt(java.time.LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Transactional
    public MarketplaceOrder openDispute(Long orderId, Long userId, String reason) {
        MarketplaceOrder order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("Acces interzis.");
        }
        order.setEscrowStatus(EscrowStatus.DISPUTED);
        return orderRepository.save(order);
    }

    @Transactional
    public MarketplaceOrder refundOrder(Long orderId) throws Exception {
        SecurityUtils.requireRole(UserRole.ADMIN);
        MarketplaceOrder order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStripePaymentIntentId() != null) {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(order.getStripePaymentIntentId())
                    .build();
            com.stripe.model.Refund.create(params);
        }
        order.setEscrowStatus(EscrowStatus.REFUNDED);
        Product product = productRepository.findById(order.getListingId()).orElseThrow();
        product.setStatus("AVAILABLE");
        productRepository.save(product);
        return orderRepository.save(order);
    }

    public List<MarketplaceOrder> myOrders(Long userId) {
        return orderRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId);
    }

    private void captureFunds(MarketplaceOrder order) throws Exception {
        if (order.getStripePaymentIntentId() == null) return;
        PaymentIntent intent = PaymentIntent.retrieve(order.getStripePaymentIntentId());
        if ("requires_capture".equals(intent.getStatus())) {
            intent.capture(PaymentIntentCaptureParams.builder().build());
        }
    }

    private Session createEscrowCheckoutSession(
            Long orderId, String name, long amountCents, Long buyerId,
            String successPath, String cancelPath
    ) throws Exception {
        return Session.create(
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                        .setPaymentIntentData(
                                SessionCreateParams.PaymentIntentData.builder()
                                        .setCaptureMethod(SessionCreateParams.PaymentIntentData.CaptureMethod.MANUAL)
                                        .putMetadata("orderId", orderId.toString())
                                        .putMetadata("buyerId", buyerId.toString())
                                        .build()
                        )
                        .setSuccessUrl(frontendUrl + successPath)
                        .setCancelUrl(frontendUrl + cancelPath)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("ron")
                                                        .setUnitAmount(amountCents)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName(name)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .putMetadata("orderId", orderId.toString())
                        .build()
        );
    }

    private MarketplaceOrder newOrder(Long listingId, Long buyerId, Long sellerId, Long offerId, long amountCents, boolean buyNow) {
        MarketplaceOrder order = new MarketplaceOrder();
        order.setListingId(listingId);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setOfferId(offerId);
        order.setAmountCents(amountCents);
        order.setBuyNow(buyNow);
        order.setEscrowStatus(EscrowStatus.PENDING_PAYMENT);
        return order;
    }

    private void validateAvailable(Product product) {
        String status = product.getStatus() != null ? product.getStatus().toUpperCase() : "AVAILABLE";
        if (List.of("SOLD", "RESERVED", "PENDING_PAYMENT").contains(status)) {
            throw new IllegalArgumentException("Produsul nu mai este disponibil.");
        }
    }

    private void preventDuplicateOrder(Long listingId) {
        List<EscrowStatus> blocking = List.of(
                EscrowStatus.PENDING_PAYMENT, EscrowStatus.PAID,
                EscrowStatus.ESCROW_ACTIVE, EscrowStatus.SHIPPED
        );
        if (orderRepository.existsByListingIdAndEscrowStatusIn(listingId, blocking)) {
            throw new IllegalArgumentException("Există deja o tranzacție activă pentru acest produs.");
        }
    }

    private MarketplaceOrder getOrderForSeller(Long orderId, Long sellerId) {
        MarketplaceOrder order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("Nu ești vânzătorul acestei comenzi.");
        }
        return order;
    }

    private MarketplaceOrder getOrderForBuyer(Long orderId, Long buyerId) {
        MarketplaceOrder order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("Nu ești cumpărătorul acestei comenzi.");
        }
        return order;
    }
}
