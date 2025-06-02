package com.omega.retail.event;

import com.omega.retail.entity.SaleDetail;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class SaleEvent extends ApplicationEvent {

    private final List<SaleDetail> saleDetails;

    public SaleEvent(Object source, List<SaleDetail> saleDetails) {
        super(source);
        this.saleDetails = saleDetails;
    }
}
