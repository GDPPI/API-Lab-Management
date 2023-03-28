package br.com.ifce.easyflow.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.ifce.easyflow.model.Address;
import br.com.ifce.easyflow.service.AddressService;
import br.com.ifce.easyflow.controller.dto.address.AddressRequestDTO;
import br.com.ifce.easyflow.controller.dto.address.AddressResponseDTO;
import br.com.ifce.easyflow.controller.dto.address.AddressUpdateDTO;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/address")
public class AddressController {

    
    private final AddressService addressService;

    @Autowired
    private AddressController(AddressService addressService){
        this.addressService = addressService;
    }


    
    @ApiOperation(value = "Returns a list of Addresss", tags = {"Address"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful request"),
            @ApiResponse(code = 403, message = "Permission denied to access this resource"),
            @ApiResponse(code = 500, message = "Internal exception"),
    })
    @GetMapping
    public List<AddressResponseDTO> search(){
        return this.addressService
                .search()
                .stream()
                .map(AddressResponseDTO::new)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Returns a Address by id", tags = {"Address"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful request"),
            @ApiResponse(code = 403, message = "Permission denied to access this resource"),
            @ApiResponse(code = 404, message = "Address not found in database"),
            @ApiResponse(code = 500, message = "Internal exception"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> searchById(@PathVariable Long id) {
        Optional<Address> Address = this.addressService.searchByID(id);

        return Address.isPresent()
                ? ResponseEntity.ok(new AddressResponseDTO(Address.get()))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "Address Not Found");
    }



    @ApiOperation(value = "Save a Address", tags = {"Address"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Saved reservation"),
            @ApiResponse(code = 403, message = "Permission denied to access this resource"),
            @ApiResponse(code = 409, message = "Address login is already being used"),
            @ApiResponse(code = 500, message = "Internal exception"),
    })
    @PostMapping
    public ResponseEntity<Object> save(@RequestBody @Valid AddressRequestDTO addressRequestDTO, UriComponentsBuilder uriBuilder){

        Address Address = addressService.createAddress(addressRequestDTO);

        URI uri = uriBuilder.path("/addresss/{id}").buildAndExpand(Address.getId()).toUri();
        return ResponseEntity.created(uri).body(new AddressResponseDTO(Address));
    }

    @ApiOperation(value = "Update a Address by id", tags = {"Address"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Saved reservation"),
            @ApiResponse(code = 403, message = "Permission denied to access this resource"),
            @ApiResponse(code = 404, message = "Address not found in database"),
            @ApiResponse(code = 409, message = "Address login is already being used"),
            @ApiResponse(code = 500, message = "Internal exception"),
    })
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody @Valid AddressUpdateDTO addressUpdateDTO) {
        Optional<Address> address = this.addressService.searchByID(id);

        if(address.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    "Address Not Found");
        }

        address.get().setComplement(addressUpdateDTO.getComplement());
        address.get().setMunicipality(addressUpdateDTO.getMunicipality());
        address.get().setNeighborhood(addressUpdateDTO.getNeighborhood());
        address.get().setNumber(addressUpdateDTO.getNumber());
        address.get().setStateEnum(addressUpdateDTO.getStateEnum());
        address.get().setStreet(addressUpdateDTO.getStreet());
        
        address = this.addressService.update(addressUpdateDTO.toAddress(id));

        return address.isPresent()
                ? ResponseEntity.ok(new AddressResponseDTO(address.get()))
                :ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "Address Not Found");
    }

    @ApiOperation(value = "Delete a Address by id", tags = {"Address"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful request"),
            @ApiResponse(code = 403, message = "Permission denied to access this resource"),
            @ApiResponse(code = 404, message = "Address not found in database"),
            @ApiResponse(code = 500, message = "Internal exception"),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        boolean removed = this.addressService.delete(id);

        return removed
                ? ResponseEntity.status(HttpStatus.OK).body(
                        "Address was deleted")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "Address Not Found");
    }
}