package com.chiringuito.service.action;

import com.chiringuito.domain.entity.MenuItem;
import com.chiringuito.domain.repository.MenuItemRepository;
import com.chiringuito.service.dto.MenuItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrowseMenuAction {

    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public List<MenuItemDTO> execute() {
        return menuItemRepository.findByAvailableTrueOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private MenuItemDTO toDTO(MenuItem menuItem) {
        return MenuItemDTO.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .imageUrl(menuItem.getImageUrl())
                .available(menuItem.getAvailable())
                .build();
    }
}