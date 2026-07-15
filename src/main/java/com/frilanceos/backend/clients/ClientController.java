package com.frilanceos.backend.clients;

import com.frilanceos.backend.clients.dto.ClientResponse.ClientDetailDto;
import com.frilanceos.backend.clients.dto.ClientResponse.ClientListItemDto;
import com.frilanceos.backend.clients.dto.CreateClientRequest;
import com.frilanceos.backend.clients.dto.UpdateClientRequest;
import com.frilanceos.backend.common.security.SecurityUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientListItemDto>> listClients(@AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(clientService.listClients(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDetailDto> getClient(@AuthenticationPrincipal SecurityUser currentUser,
                                                      @PathVariable UUID id) {
        return ResponseEntity.ok(clientService.getClient(currentUser, id));
    }

    @PostMapping
    public ResponseEntity<ClientDetailDto> createClient(@AuthenticationPrincipal SecurityUser currentUser,
                                                         @Valid @RequestBody CreateClientRequest request) {
        return ResponseEntity.ok(clientService.createClient(currentUser, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDetailDto> updateClient(@AuthenticationPrincipal SecurityUser currentUser,
                                                         @PathVariable UUID id,
                                                         @Valid @RequestBody UpdateClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@AuthenticationPrincipal SecurityUser currentUser,
                                              @PathVariable UUID id) {
        clientService.deleteClient(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
