package com.grash.controller;

import com.grash.dto.PartQuantityPatchDTO;
import com.grash.dto.PartQuantityShowDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.mapper.PartQuantityMapper;
import com.grash.model.OwnUser;
import com.grash.model.PartQuantity;
import com.grash.model.enums.BasicPermission;
import com.grash.model.enums.RoleType;
import com.grash.service.PartQuantityService;
import com.grash.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/part-quantities")
@Api(tags = "partQuantity")
@RequiredArgsConstructor
public class PartQuantityController {

    private final PartQuantityService partQuantityService;
    private final PartQuantityMapper partQuantityMapper;
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "PartQuantityCategory not found")})
    public Collection<PartQuantityShowDTO> getAll(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            return partQuantityService.findByCompany(user.getCompany().getId()).stream().map(partQuantityMapper::toShowDto).collect(Collectors.toList());
        } else
            return partQuantityService.getAll().stream().map(partQuantityMapper::toShowDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "PartQuantity not found")})
    public PartQuantityShowDTO getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<PartQuantity> optionalPartQuantity = partQuantityService.findById(id);
        if (optionalPartQuantity.isPresent()) {
            PartQuantity savedPartQuantity = optionalPartQuantity.get();
            if (partQuantityService.hasAccess(user, savedPartQuantity)) {
                return partQuantityMapper.toShowDto(savedPartQuantity);
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public PartQuantityShowDTO create(@ApiParam("PartQuantity") @Valid @RequestBody PartQuantity partQuantityReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (partQuantityService.canCreate(user, partQuantityReq)) {
            PartQuantity savedPartQuantity = partQuantityService.create(partQuantityReq);
            return partQuantityMapper.toShowDto(savedPartQuantity);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "PartQuantity not found")})
    public PartQuantityShowDTO patch(@ApiParam("PartQuantity") @Valid @RequestBody PartQuantityPatchDTO partQuantity, @ApiParam("id") @PathVariable("id") Long id,
                                     HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<PartQuantity> optionalPartQuantity = partQuantityService.findById(id);

        if (optionalPartQuantity.isPresent()) {
            PartQuantity savedPartQuantity = optionalPartQuantity.get();
            if (partQuantityService.hasAccess(user, savedPartQuantity) && partQuantityService.canPatch(user, partQuantity)) {
                PartQuantity patchedPartQuantity = partQuantityService.update(id, partQuantity);
                return partQuantityMapper.toShowDto(patchedPartQuantity);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("PartQuantity not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "PartQuantity not found")})
    public ResponseEntity<SuccessResponse> delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<PartQuantity> optionalPartQuantity = partQuantityService.findById(id);
        if (optionalPartQuantity.isPresent()) {
            PartQuantity savedPartQuantity = optionalPartQuantity.get();
            if (partQuantityService.hasAccess(user, savedPartQuantity)
                    && user.getRole().getPermissions().contains(BasicPermission.DELETE_PARTS_AND_MULTI_PARTS)) {
                partQuantityService.delete(id);
                return new ResponseEntity<>(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("PartQuantity not found", HttpStatus.NOT_FOUND);
    }

}
