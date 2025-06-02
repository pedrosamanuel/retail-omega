package com.omega.retail.listener;



import com.omega.retail.event.PurchaseOrderEvent;
import com.omega.retail.event.SaleEvent;
import com.omega.retail.service.ProductService;
import com.omega.retail.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductListener {

    private final ProductService productService;
    private final PurchaseOrderService purchaseOrderService;

    @EventListener
    public void handleSale(SaleEvent saleEvent) {
        productService.reduceStock(saleEvent.getSaleDetails());
        purchaseOrderService.generatePurchaseOrder(saleEvent.getSaleDetails());
    }
    @EventListener
    public void handleOrder(PurchaseOrderEvent purchaseOrderEvent) {
        productService.increaseStock(purchaseOrderEvent.getDetails());
    }
}
