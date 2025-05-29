package com.omega.retail.service;
import com.omega.retail.dto.request.ProviderRequest;
import com.omega.retail.dto.response.ProviderResponse;
import com.omega.retail.entity.Provider;
import com.omega.retail.enums.ProviderState;
import com.omega.retail.enums.PurchaseOrderState;
import com.omega.retail.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProviderService {

    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public ProviderResponse createProvider(ProviderRequest request) {
        Provider provider = Provider.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .providerState(ProviderState.ALTA)
                .build();
        Provider saved = providerRepository.save(provider);
        return toResponse(saved);
    }

    public ProviderResponse getProvider(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with " + id));
        return toResponse(provider);
    }

    public List<ProviderResponse> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProviderResponse updateProvider(Long id, ProviderRequest request) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with " + id));
        provider.setName(request.getName());
        provider.setEmail(request.getEmail());
        provider.setPhone(request.getPhone());
        Provider updated = providerRepository.save(provider);
        return toResponse(updated);
    }

    public void deleteProvider(Long id) {
        if (!providerRepository.existsById(id)) {
            throw new RuntimeException("Provider not found with " + id);
        }
        if(providerRepository.existsDefaultProvider(id)){
            throw new RuntimeException("Provider is default of at least one product");
        }
        if(providerRepository.existsByOrderStatus(id, List.of(PurchaseOrderState.PENDIENTE, PurchaseOrderState.ENVIADA))){
            throw new RuntimeException("There is a pending or sent purchase order");
        }
        Optional<Provider> providerOptional = providerRepository.findById(id);
        if(providerOptional.isPresent()){
            Provider provider = providerOptional.get();
            provider.setDeactivateDate(LocalDate.now());
            provider.setProviderState(ProviderState.BAJA);
            providerRepository.save(provider);
        }
    }

    private ProviderResponse toResponse(Provider provider) {
        return ProviderResponse.builder()
                .id(provider.getId())
                .name(provider.getName())
                .email(provider.getEmail())
                .phone(provider.getPhone())
                .providerState(provider.getProviderState())
                .deactivateDate(provider.getDeactivateDate())
                .build();
    }
}