package com.chiringuito.web.controller;

import com.chiringuito.service.action.BrowseMenuAction;
import com.chiringuito.service.dto.MenuItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final BrowseMenuAction browseMenuAction;

    @GetMapping
    public ResponseEntity<List<MenuItemDTO>> getMenu() {
        List<MenuItemDTO> menuItems = browseMenuAction.execute();
        return ResponseEntity.ok(menuItems);
    }
}