package com.omega.retail.service;

import com.omega.retail.dto.request.PurchaseOrderDetailRequest;
import com.omega.retail.dto.request.PurchaseOrderRequest;
import com.omega.retail.dto.response.PurchaseOrderDetailResponse;
import com.omega.retail.dto.response.PurchaseOrderResponse;
import com.omega.retail.entity.*;
import com.omega.retail.enums.InventoryPolicy;
import com.omega.retail.enums.PurchaseOrderState;
import com.omega.retail.event.PurchaseOrderEvent;
import com.omega.retail.repository.ProductRepository;
import com.omega.retail.repository.ProviderRepository;
import com.omega.retail.repository.PurchaseOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final ProviderRepository providerRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PurchaseOrderService(
            PurchaseOrderRepository orderRepository,
            ProviderRepository providerRepository,
            ProductRepository productRepository, ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.providerRepository = providerRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        List<PurchaseOrderDetail> details = new ArrayList<>();
        double total = 0.0;

        PurchaseOrder order = PurchaseOrder.builder()
                .createdAt(LocalDate.now())
                .purchaseOrderState(PurchaseOrderState.PENDIENTE)
                .provider(provider)
                .build();

        for (PurchaseOrderDetailRequest d : request.getDetails()) {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            boolean exists = orderRepository.existsActiveByProductId(d.getProductId(), List.of(PurchaseOrderState.PENDIENTE, PurchaseOrderState.ENVIADA));
            if (exists) {
                throw new RuntimeException("Ya existe una orden activa para el producto: " + product.getDescription());
            }
            double price =  0.0d;

            for (ProductProvider pp : provider.getProducts()){
                if (pp.getProduct().getId() == product.getId()){
                    price = pp.getUnitCost();
                }
            }
            double subtotal = price * d.getQuantity();
            total += subtotal;

            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .product(product)
                    .quantity(d.getQuantity())
                    .price(price)
                    .subtotal(subtotal)
                    .purchaseOrder(order)
                    .build();

            details.add(detail);
        }

        order.setTotal(total);
        order.setDetails(details);

        return mapToResponse(orderRepository.save(order));
    }

    public PurchaseOrderResponse getById(Long id) {
        PurchaseOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));
        return mapToResponse(order);
    }

    public List<PurchaseOrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PurchaseOrderResponse update(Long id, PurchaseOrderRequest request) {
        PurchaseOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));

        if (order.getPurchaseOrderState() != PurchaseOrderState.PENDIENTE) {
            throw new RuntimeException("Solo se puede modificar una orden pendiente");
        }

        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        Map<Long, PurchaseOrderDetail> existingDetailsMap = order.getDetails().stream()
                .collect(Collectors.toMap(detail -> detail.getProduct().getId(), detail -> detail));

        List<PurchaseOrderDetail> updatedDetails = new ArrayList<>();
        double total = 0.0;

        for (PurchaseOrderDetailRequest d : request.getDetails()) {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            double price = 0.0;
            for (ProductProvider pp : provider.getProducts()) {
                if (pp.getProduct().getId().equals(product.getId())) {
                    price = pp.getUnitCost();
                    break;
                }
            }


            PurchaseOrderDetail existingDetail = existingDetailsMap.get(product.getId());
            if (existingDetail != null) {
                int newQuantity = existingDetail.getQuantity() + d.getQuantity();
                double newSubtotal = price * newQuantity;

                existingDetail.setQuantity(newQuantity);
                existingDetail.setPrice(price);
                existingDetail.setSubtotal(newSubtotal);

                updatedDetails.add(existingDetail);
                total += newSubtotal;
            } else {
                double subtotal = price * d.getQuantity();

                PurchaseOrderDetail newDetail = PurchaseOrderDetail.builder()
                        .product(product)
                        .quantity(d.getQuantity())
                        .price(price)
                        .subtotal(subtotal)
                        .purchaseOrder(order)
                        .build();

                updatedDetails.add(newDetail);
                total += subtotal;
            }
        }

        order.setProvider(provider);
        order.setTotal(total);

        order.getDetails().clear();
        order.getDetails().addAll(updatedDetails);

        return mapToResponse(orderRepository.save(order));
    }

    public void cancel(Long id) {
        PurchaseOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));

        if (order.getPurchaseOrderState() != PurchaseOrderState.PENDIENTE) {
            throw new RuntimeException("Solo se puede cancelar una orden pendiente");
        }

        order.setPurchaseOrderState(PurchaseOrderState.CANCELADA);
        orderRepository.save(order);
    }

    public void send(Long id) {
        PurchaseOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));

        if (order.getPurchaseOrderState() != PurchaseOrderState.PENDIENTE) {
            throw new RuntimeException("Solo se puede enviar una orden pendiente");
        }

        order.setPurchaseOrderState(PurchaseOrderState.ENVIADA);
        order.setSentAt(LocalDate.now());
        orderRepository.save(order);
    }

    public void finalize(Long id) {
        PurchaseOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));

        if (order.getPurchaseOrderState() != PurchaseOrderState.ENVIADA) {
            throw new RuntimeException("Solo se puede finalizar una orden enviada");
        }

        order.setPurchaseOrderState(PurchaseOrderState.FINALIZADA);
        order.setReceivedAt(LocalDate.now());
        orderRepository.save(order);

        eventPublisher.publishEvent(new PurchaseOrderEvent(this, order.getDetails()));
    }

    @Transactional
    public void generatePurchaseOrder(List<SaleDetail> saleDetails) {
        Map<Long, PurchaseOrder> pendingOrdersByProvider = new HashMap<>();

        for (SaleDetail detail : saleDetails) {
            Product product = productRepository.findById(detail.getProduct().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + detail.getProduct().getId()));

            if (product.getInventoryPolicy() != InventoryPolicy.LOTE_FIJO)
                continue;

            FixedLotPolicy policy = product.getFixedLotPolicy();
            if (policy == null)
                continue;

            boolean belowReorder = product.getCurrentStock() < policy.getReorderPoint();
            if (!belowReorder)
                continue;

            ProductProvider defaultProvider = product.getProductProviders().stream()
                    .filter(ProductProvider::getIsDefault)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No hay proveedor predeterminado para el producto: " + product.getDescription()));

            Provider provider = defaultProvider.getProvider();
            Long providerId = provider.getId();

            PurchaseOrder order = pendingOrdersByProvider.get(providerId);
            if (order == null) {
                // Buscamos si ya hay una orden pendiente para este proveedor
                order = orderRepository.findFirstByProviderIdAndPurchaseOrderState(providerId, PurchaseOrderState.PENDIENTE)
                        .orElse(null);
                if (order == null) {
                    // Creamos nueva orden
                    order = PurchaseOrder.builder()
                            .createdAt(LocalDate.now())
                            .purchaseOrderState(PurchaseOrderState.PENDIENTE)
                            .provider(provider)
                            .details(new ArrayList<>())
                            .total(0.0)
                            .build();
                }
                pendingOrdersByProvider.put(providerId, order);
            }

            boolean alreadyAdded = order.getDetails().stream()
                    .anyMatch(d -> d.getProduct().getId().equals(product.getId()));

            if (alreadyAdded) {
                continue;
            }

            Integer quantity = policy.getOptimalLotSize();
            Double price = defaultProvider.getUnitCost();
            Double subtotal = quantity * price;

            PurchaseOrderDetail orderDetail = PurchaseOrderDetail.builder()
                    .product(product)
                    .quantity(quantity)
                    .price(price)
                    .subtotal(subtotal)
                    .purchaseOrder(order)
                    .build();

            order.getDetails().add(orderDetail);
            order.setTotal(order.getTotal() + subtotal);
        }

        // Guardamos las órdenes (nuevas o modificadas)
        for (PurchaseOrder order : pendingOrdersByProvider.values()) {
            orderRepository.save(order);
        }
    }
    @Scheduled(cron = "0 0 2 * * *")// Todos los días a las 2:00 AM
    //@Scheduled(cron = "*/15 * * * * *") //para pruebas cada 15 segundos
    @Transactional
    public void generateScheduledPurchaseOrders() {
        LocalDate today = LocalDate.now();
        Map<Long, PurchaseOrder> pendingOrdersByProvider = new HashMap<>();

        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            if (product.getInventoryPolicy() != InventoryPolicy.INTERVALO_FIJO)
                continue;

            FixedIntervalPolicy policy = product.getFixedIntervalPolicy();
            if (policy == null || policy.getReviewIntervalDays() == null)
                continue;
            if(policy.getLastReviewDate() != null) {
                LocalDate nextReview = policy.getLastReviewDate().plusDays(policy.getReviewIntervalDays());
                if (!today.isAfter(nextReview) && !today.isEqual(nextReview))
                    continue;
            }

            // Proveedor predeterminado
            ProductProvider defaultProvider = product.getProductProviders().stream()
                    .filter(ProductProvider::getIsDefault)
                    .findFirst()
                    .orElse(null);

            if (defaultProvider == null)
                continue;

            int reviewInterval = policy.getReviewIntervalDays();
            int leadTime = defaultProvider.getLeadTime() != null ? defaultProvider.getLeadTime() : 0;
            int safetyStock = policy.getSafetyStock() != null ? policy.getSafetyStock() : 0;
            int currentStock = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
            int annualDemand = product.getAnnualDemand() != null ? product.getAnnualDemand() : 0;

            double dailyDemand = annualDemand / 365.0;
            int quantity = (int) Math.ceil(dailyDemand * (reviewInterval + leadTime) + safetyStock - currentStock);
            if (quantity <= 0)
                continue;

            Provider provider = defaultProvider.getProvider();
            Long providerId = provider.getId();

            PurchaseOrder order = pendingOrdersByProvider.get(providerId);
            if (order == null) {
                order = orderRepository.findFirstByProviderIdAndPurchaseOrderState(providerId, PurchaseOrderState.PENDIENTE)
                        .orElse(null);
                if (order == null) {
                    order = PurchaseOrder.builder()
                            .createdAt(today)
                            .purchaseOrderState(PurchaseOrderState.PENDIENTE)
                            .provider(provider)
                            .details(new ArrayList<>())
                            .total(0.0)
                            .build();
                }
                pendingOrdersByProvider.put(providerId, order);
            }

            double price = defaultProvider.getUnitCost();
            double subtotal = quantity * price;

            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .product(product)
                    .quantity(quantity)
                    .price(price)
                    .subtotal(subtotal)
                    .purchaseOrder(order)
                    .build();

            order.getDetails().add(detail);
            order.setTotal(order.getTotal() + subtotal);

            policy.setLastReviewDate(today);
        }

        for (PurchaseOrder order : pendingOrdersByProvider.values()) {
            orderRepository.save(order);
        }
    }


    private PurchaseOrderResponse mapToResponse(PurchaseOrder order) {
        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .createdAt(order.getCreatedAt())
                .sentAt(order.getSentAt())
                .receivedAt(order.getReceivedAt())
                .total(order.getTotal())
                .providerId(order.getProvider().getId())
                .providerName(order.getProvider().getName())
                .purchaseOrderState(order.getPurchaseOrderState())
                .details(order.getDetails().stream()
                        .map(d -> PurchaseOrderDetailResponse.builder()
                                .id(d.getId())
                                .quantity(d.getQuantity())
                                .price(d.getPrice())
                                .subtotal(d.getSubtotal())
                                .productId(d.getProduct().getId())
                                .productDescription(d.getProduct().getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}

