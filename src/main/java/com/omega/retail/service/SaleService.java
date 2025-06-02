package com.omega.retail.service;

import com.omega.retail.dto.request.SaleRequest;
import com.omega.retail.dto.response.SaleDetailResponse;
import com.omega.retail.dto.response.SaleResponse;
import com.omega.retail.entity.Product;
import com.omega.retail.entity.Sale;
import com.omega.retail.entity.SaleDetail;
import com.omega.retail.event.SaleEvent;
import com.omega.retail.repository.ProductRepository;
import com.omega.retail.repository.SaleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository, ApplicationEventPublisher eventPublisher) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }


    public SaleResponse createSale(SaleRequest request) {
        Sale sale = Sale.builder()
                .date(LocalDateTime.now())
                .build();

        List<SaleDetail> details = request.getSaleDetail().stream().map(detailReq -> {
            Product product = productRepository.findById(detailReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + detailReq.getProductId()));

            if (product.getCurrentStock() < detailReq.getQuantity()) {
                throw new RuntimeException("No hay suficiente stock de: " + product.getDescription());
            }

            Double subtotal = detailReq.getQuantity() * detailReq.getUnitPrice();

            return SaleDetail.builder()
                    .quantity(detailReq.getQuantity())
                    .unitPrice(detailReq.getUnitPrice())
                    .subtotal(subtotal)
                    .product(product)
                    .sale(sale)
                    .build();
        }).toList();

        sale.setDetails(details);

        Double total = details.stream()
                .mapToDouble(SaleDetail::getSubtotal)
                .sum();
        sale.setTotal(total);

        Sale savedSale = saleRepository.save(sale);

        eventPublisher.publishEvent(new SaleEvent(this, details));

        return mapToResponse(savedSale);
    }

    public List<SaleResponse> getAllSales() {
        return saleRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }


    public SaleResponse getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found with id: " + id));
        return mapToResponse(sale);
    }
    public Page<SaleResponse> getSalesPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sale> salePage = saleRepository.findAllByOrderByDateDesc(pageable);

        return salePage.map(this::mapToResponse);
    }


    public void deleteSale(Long id) {
        if (!saleRepository.existsById(id)) {
            throw new EntityNotFoundException("Sale not found with id: " + id);
        }
        saleRepository.deleteById(id);
    }


    private SaleResponse mapToResponse(Sale sale) {
        List<SaleDetailResponse> detailResponses = sale.getDetails().stream().map(detail -> SaleDetailResponse.builder()
                .id(detail.getId())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .subtotal(detail.getSubtotal())
                .productId(detail.getProduct().getId())
                .productCode(detail.getProduct().getCode())
                .productDescription(detail.getProduct().getDescription())
                .build()).toList();

        return SaleResponse.builder()
                .id(sale.getId())
                .date(sale.getDate())
                .total(sale.getTotal())
                .saleDetailResponses(detailResponses)
                .build();
    }
}
