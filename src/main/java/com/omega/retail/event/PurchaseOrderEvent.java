package com.omega.retail.event;

import com.omega.retail.entity.PurchaseOrderDetail;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class PurchaseOrderEvent  extends ApplicationEvent {
    private final List<PurchaseOrderDetail> details;

    public PurchaseOrderEvent(Object source, List<PurchaseOrderDetail> details) {
        super(source);
        this.details = details;
    }
}
