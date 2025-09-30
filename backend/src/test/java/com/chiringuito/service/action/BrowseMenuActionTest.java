package com.chiringuito.service.action;

import com.chiringuito.domain.entity.MenuItem;
import com.chiringuito.domain.repository.MenuItemRepository;
import com.chiringuito.service.dto.MenuItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrowseMenuActionTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private BrowseMenuAction browseMenuAction;

    private MenuItem paella;
    private MenuItem tortilla;

    @BeforeEach
    void setUp() {
        paella = MenuItem.builder()
                .id(UUID.randomUUID())
                .name("Paella Valenciana")
                .description("Traditional Spanish rice dish")
                .price(new BigDecimal("12.50"))
                .imageUrl("https://example.com/paella.jpg")
                .available(true)
                .build();

        tortilla = MenuItem.builder()
                .id(UUID.randomUUID())
                .name("Tortilla Española")
                .description("Spanish potato omelette")
                .price(new BigDecimal("6.00"))
                .imageUrl("https://example.com/tortilla.jpg")
                .available(true)
                .build();
    }

    @Test
    void execute_shouldReturnAllAvailableMenuItemsSortedByName() {
        // Given
        when(menuItemRepository.findByAvailableTrueOrderByNameAsc())
                .thenReturn(Arrays.asList(paella, tortilla));

        // When
        List<MenuItemDTO> result = browseMenuAction.execute();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Paella Valenciana");
        assertThat(result.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(result.get(1).getName()).isEqualTo("Tortilla Española");
    }

    @Test
    void execute_shouldReturnEmptyListWhenNoAvailableItems() {
        // Given
        when(menuItemRepository.findByAvailableTrueOrderByNameAsc())
                .thenReturn(List.of());

        // When
        List<MenuItemDTO> result = browseMenuAction.execute();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void execute_shouldMapAllFieldsCorrectly() {
        // Given
        when(menuItemRepository.findByAvailableTrueOrderByNameAsc())
                .thenReturn(List.of(paella));

        // When
        List<MenuItemDTO> result = browseMenuAction.execute();

        // Then
        assertThat(result).hasSize(1);
        MenuItemDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(paella.getId());
        assertThat(dto.getName()).isEqualTo(paella.getName());
        assertThat(dto.getDescription()).isEqualTo(paella.getDescription());
        assertThat(dto.getPrice()).isEqualByComparingTo(paella.getPrice());
        assertThat(dto.getImageUrl()).isEqualTo(paella.getImageUrl());
        assertThat(dto.getAvailable()).isEqualTo(paella.getAvailable());
    }
}